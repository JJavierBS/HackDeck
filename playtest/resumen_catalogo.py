#!/usr/bin/env python3
"""Catalogo en una linea por carta, para meterlo en el prompt del agente.

Sale del servidor y no del YAML a proposito: asi describe las cartas que la
partida esta usando de verdad, aunque se haya cambiado hackdeck.catalog.path.

    resumen_catalogo.py <token-instructor>
"""
import json
import os
import sys
import urllib.request

API = os.environ.get("HD_API", "http://localhost:8080/api/v1")
PILARES = {"CONFIDENTIALITY": "C", "INTEGRITY": "I", "AVAILABILITY": "D"}


def linea(carta):
    partes = [f"{carta['id']} c{carta['cost']}"]
    if carta["side"] == "ATTACKER":
        partes.append(f"{int(carta['successRate'] * 100)}%")
        partes.append(f"ruido:{carta['noise'].lower()}")
    impacto = carta.get("impact") or {}
    dano = " ".join(f"{PILARES[p]}{v:+d}" for p, v in impacto.items() if p in PILARES)
    if dano:
        partes.append(dano)
    if carta.get("mitigation"):
        partes.append(f"mitiga{carta['mitigation']}")
    if carta.get("detection"):
        partes.append(f"detecta{carta['detection']}")
    if carta.get("unlocks"):
        partes.append("abre:" + ",".join(carta["unlocks"]))
    if carta.get("requires"):
        partes.append("necesita:" + ",".join(carta["requires"]))
    counters = carta.get("counters") or {}
    if counters:
        partes.append("frena:" + ",".join(f"{k}({v})" for k, v in counters.items()))
    bonus = carta.get("bonus") or {}
    if bonus:
        partes.append("mejora:" + ",".join(f"{k}(+{v})" for k, v in bonus.items()))
    if carta.get("duration") == "PERMANENT":
        partes.append("dura:fija")
    elif carta.get("duration") == "ROUNDS":
        partes.append(f"dura:{carta['rounds']}r")
    if carta.get("effects"):
        partes.append("efecto:" + ",".join(carta["effects"]))
    return " | ".join(partes)


def main(token):
    peticion = urllib.request.Request(f"{API}/catalog")
    peticion.add_header("Authorization", f"Bearer {token}")
    with urllib.request.urlopen(peticion) as respuesta:
        cartas = json.load(respuesta)

    grupos = {}
    for carta in cartas:
        if carta["type"] == "TWIST":
            continue
        if carta["type"] == "POWERUP":
            clave = f"ESPECIALES {carta['side']}"
        else:
            clave = f"{carta['side']} {carta.get('phase') or carta.get('category')}"
        grupos.setdefault(clave, []).append(carta)

    for clave in sorted(grupos):
        print(f"[{clave}]")
        for carta in grupos[clave]:
            print(linea(carta))


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(__doc__)
        sys.exit(2)
    main(sys.argv[1])
