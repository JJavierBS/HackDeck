#!/usr/bin/env python3
"""Monta una partida lista para que la jueguen dos agentes.

Crea, sienta a los dos equipos y enciende el cierre automatico: sin eso
haria falta un tercer agente de instructor solo para pulsar "resolver".

    arbitro.py crea [nombre-mesa]   -> imprime el entorno de cada bando
    arbitro.py espera <gameId>      -> bloquea hasta que el match termina
"""
import json
import os
import sys
import time
import urllib.error
import urllib.request

API = os.environ.get("HD_API", "http://localhost:8080/api/v1")
# Los del juego (GameSettings.DEFAULTS), salvo el reloj: un agente tarda mas
# que una persona en decidir y no interesa que la ronda se cierre sola a medias.
# Tocar el presupuesto invalida la tanda entera: con mas dinero se compra el
# catalogo completo y desaparecen las decisiones que se quieren medir.
AJUSTES = {"roundsPerHalf": 8, "roundTimeoutSeconds": 600, "initialBudget": 20, "incomePerRound": 10}


def pide(metodo, ruta, cuerpo=None, token=None):
    datos = None if cuerpo is None else json.dumps(cuerpo).encode()
    peticion = urllib.request.Request(f"{API}{ruta}", data=datos, method=metodo)
    if token:
        peticion.add_header("Authorization", f"Bearer {token}")
    if datos is not None:
        peticion.add_header("Content-Type", "application/json")
    try:
        with urllib.request.urlopen(peticion) as respuesta:
            crudo = respuesta.read()
            return json.loads(crudo) if crudo else None
    except urllib.error.HTTPError as e:
        raise SystemExit(f"{metodo} {ruta}: {e.code} {e.read().decode()}")


def crea(mesa):
    partida = pide("POST", "/games", AJUSTES)
    gid, codigo, instructor = partida["gameId"], partida["joinCode"], partida["token"]
    equipos = {}
    for nombre in (f"{mesa}-uno", f"{mesa}-dos"):
        entrada = pide("POST", "/games/join", {"code": codigo, "displayName": nombre})
        equipos[nombre] = entrada["token"]
    pide("POST", f"/games/{gid}/start", token=instructor)
    # El cierre automatico es lo que permite jugar sin instructor humano.
    pide("POST", f"/games/{gid}/auto-resolve/true", token=instructor)
    print(json.dumps({
        "gameId": gid, "joinCode": codigo, "instructor": instructor,
        "equipos": [{"nombre": n, "token": t} for n, t in equipos.items()],
    }))


def espera(gid, token):
    limite = time.time() + 3600
    while time.time() < limite:
        vista = pide("GET", f"/games/{gid}", token=token)
        if vista.get("phase") == "FINISHED":
            print(json.dumps(vista.get("result")))
            return
        time.sleep(5)
    raise SystemExit("La partida no termino a tiempo")


if __name__ == "__main__":
    if len(sys.argv) >= 2 and sys.argv[1] == "crea":
        crea(sys.argv[2] if len(sys.argv) > 2 else "mesa")
    elif len(sys.argv) == 4 and sys.argv[1] == "espera":
        espera(sys.argv[2], sys.argv[3])
    else:
        print(__doc__)
        sys.exit(2)
