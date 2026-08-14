#!/usr/bin/env python3
"""Estadisticas de balance a partir del historial de varias partidas.

Esto no necesita ningun modelo: el historial ya lo cuenta todo, y lo que se
puede medir con un script no se le pregunta a un agente que cobra por token.

    analiza.py mesas.json     # [{"gameId": ..., "instructor": ...}, ...]
"""
import json
import os
import sys
import urllib.request
from collections import defaultdict

API = os.environ.get("HD_API", "http://localhost:8080/api/v1")
PILARES = {"CONFIDENTIALITY": "C", "INTEGRITY": "I", "AVAILABILITY": "D"}


def historial(gid, token):
    peticion = urllib.request.Request(f"{API}/games/{gid}/history")
    peticion.add_header("Authorization", f"Bearer {token}")
    with urllib.request.urlopen(peticion) as respuesta:
        return json.load(respuesta)


def main(ruta):
    mesas = json.load(open(ruta))
    ataques = defaultdict(lambda: {"jugada": 0, "acierto": 0, "dano": 0, "detectada": 0, "frenada": 0})
    defensas = defaultdict(int)
    defendidas, derribos, resultados = [], 0, defaultdict(int)
    partidas = 0

    for mesa in mesas:
        try:
            datos = historial(mesa["gameId"], mesa["instructor"])
        except Exception as e:
            print(f"aviso: no se pudo leer {mesa['gameId']}: {e}", file=sys.stderr)
            continue
        resultado = datos.get("result")
        if not resultado:
            continue
        partidas += 1
        resultados[resultado["outcome"]] += 1
        defendidas.extend(resultado["defendedCia"].values())
        derribos += len(resultado.get("takedownRound") or {})

        for evento in datos["events"]:
            carta, detalle = evento.get("cardId"), evento.get("detail") or {}
            if not carta:
                continue
            if evento["type"] == "ATTACK":
                registro = ataques[carta]
                registro["jugada"] += 1
                if detalle.get("success"):
                    registro["acierto"] += 1
                    registro["dano"] += sum(abs(v) for v in (detalle.get("impact") or {}).values())
                elif detalle.get("failureReason") == "COUNTERED":
                    registro["frenada"] += 1
                if detalle.get("detected"):
                    registro["detectada"] += 1
            elif evento["type"] == "DEFENCE":
                defensas[carta] += 1

    if not partidas:
        print("Sin partidas terminadas que analizar")
        return 1

    print(f"PARTIDAS: {partidas}")
    print("DESENLACES: " + ", ".join(f"{k}={v}" for k, v in sorted(resultados.items())))
    print(f"MITADES CON DERRIBO: {derribos}/{partidas * 2}")
    defendidas.sort()
    media = sum(defendidas) / len(defendidas)
    print(f"TRIADA DEFENDIDA (300 = intacta): media {media:.0f}  min {defendidas[0]}  max {defendidas[-1]}")
    print(f"  reparto: {defendidas}")

    print("\nATAQUES (jugada/acierto/%/dano medio/detectada/frenada)")
    for carta, r in sorted(ataques.items(), key=lambda x: -x[1]["jugada"]):
        tasa = 100 * r["acierto"] / r["jugada"]
        dano = r["dano"] / r["acierto"] if r["acierto"] else 0
        print(f"  {carta:28} {r['jugada']:3} {r['acierto']:3} {tasa:5.0f}% {dano:5.1f} "
              f"{r['detectada']:3} {r['frenada']:3}")

    print("\nDEFENSAS (veces jugada)")
    for carta, veces in sorted(defensas.items(), key=lambda x: -x[1]):
        print(f"  {carta:28} {veces:3}")
    return 0


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(__doc__)
        sys.exit(2)
    sys.exit(main(sys.argv[1]))
