#!/usr/bin/env python3
"""Cliente minimo para que un agente juegue una partida de HackDeck.

Un agente cuesta dinero por llamada, asi que "jugar" hace la ronda entera en
una sola: encola, confirma y espera a que el rival confirme tambien, y
devuelve ya el estado nuevo con lo que paso. Una llamada = una ronda.

Uso (con HD_API, HD_GAME y HD_TOKEN en el entorno):
    hd.py estado
    hd.py jugar mfa parcheo              # encolar y cerrar la ronda
    hd.py jugar aviso-cert:blocks=ddos   # carta con parametro
    hd.py jugar                          # pasar sin gastar
"""
import json
import os
import sys
import time
import urllib.error
import urllib.request

API = os.environ.get("HD_API", "http://localhost:8080/api/v1")
GAME = os.environ.get("HD_GAME", "")
TOKEN = os.environ.get("HD_TOKEN", "")

ESPERA_MAX = 900
PILARES = {"CONFIDENTIALITY": "C", "INTEGRITY": "I", "AVAILABILITY": "D"}


def pide(metodo, ruta, cuerpo=None):
    datos = None if cuerpo is None else json.dumps(cuerpo).encode()
    peticion = urllib.request.Request(f"{API}{ruta}", data=datos, method=metodo)
    peticion.add_header("Authorization", f"Bearer {TOKEN}")
    if datos is not None:
        peticion.add_header("Content-Type", "application/json")
    try:
        with urllib.request.urlopen(peticion) as respuesta:
            crudo = respuesta.read()
            return json.loads(crudo) if crudo else None
    except urllib.error.HTTPError as e:
        detalle = e.read().decode()
        try:
            return {"_error": json.loads(detalle).get("message", detalle)}
        except json.JSONDecodeError:
            return {"_error": f"HTTP {e.code}"}


def estado():
    return pide("GET", f"/games/{GAME}")


def triada(niveles):
    if not niveles:
        return "-"
    return " ".join(f"{PILARES[p]}={n}" for p, n in niveles.items() if p in PILARES)


def resumen_ronda(vista, ronda):
    """Solo lo de la ronda pedida: el registro entero crece sin parar."""
    lineas = []
    for evento in vista.get("events", []):
        if evento.get("roundNumber") != ronda or evento.get("type") not in ("ATTACK", "DEFENCE"):
            continue
        detalle = evento.get("detail") or {}
        quien = "tuya" if evento.get("actor") == vista.get("yourSide") else "RIVAL"
        parte = [f"  {quien} {evento.get('cardId')}"]
        if evento.get("type") == "ATTACK":
            parte.append("ACIERTA" if detalle.get("success") else f"falla({detalle.get('failureReason')})")
            impacto = detalle.get("impact") or {}
            if impacto:
                parte.append(" ".join(f"{PILARES[p]}{v}" for p, v in impacto.items() if p in PILARES))
            if detalle.get("mitigated"):
                parte.append(f"(mitigado {detalle['mitigated']})")
            if detalle.get("detected") is True:
                parte.append("[detectado]")
        else:
            reparado = detalle.get("impact") or {}
            parte.append("desplegada" if not reparado else "repara " + triada(reparado))
        lineas.append(" ".join(parte))
    return lineas


def pinta(vista, ronda_anterior=None):
    if vista.get("_error"):
        return f"ERROR: {vista['_error']}"
    if vista.get("phase") == "FINISHED":
        resultado = vista.get("result") or {}
        equipos = vista.get("teams", {})
        ganador = resultado.get("winner")
        quien = "EMPATE" if ganador is None else f"GANA {equipos.get(ganador, ganador)}"
        tuyo = vista.get("yourTeam")
        desenlace = "" if ganador is None else (" (TU)" if ganador == tuyo else " (RIVAL)")
        return (f"FIN {quien}{desenlace} por {resultado.get('outcome')}\n"
                f"triada defendida: {resultado.get('defendedCia')}")

    lineas = [
        f"MITAD {vista.get('halfNumber')}/2  RONDA {vista.get('currentRoundNumber')}/{vista.get('roundsPerHalf')}"
        f"  BANDO: {vista.get('yourSide')}  PRESUPUESTO: {vista.get('yourBudget')}",
        f"CIA {triada(vista.get('ciaLevels'))}",
    ]
    if vista.get("yourSide") == "ATTACKER":
        lineas.append("FASES ABIERTAS: " + ",".join(vista.get("yourKillChain") or []))
    capas = vista.get("yourActiveCards") or []
    if capas:
        lineas.append("TUS CARTAS ACTIVAS: " + ", ".join(
            f"{c['cardId']}({'fija' if c.get('roundsRemaining') is None else c['roundsRemaining']})" for c in capas))
    anterior = vista.get("previousHalf")
    if anterior:
        lineas.append(f"MITAD {anterior['number']} CERRADA: {anterior['defendedCia']} puntos a batir")
    if ronda_anterior is not None:
        sucesos = resumen_ronda(vista, ronda_anterior)
        if sucesos:
            lineas.append(f"RONDA {ronda_anterior}:")
            lineas.extend(sucesos)
    return "\n".join(lineas)


def descompone(carta):
    """`aviso-cert:blocks=ddos` -> ("aviso-cert", {"blocks": "ddos"}).

    Sin esto las cartas con parametro no funcionan y el agente cree que la
    carta esta rota, cuando lo que falla es el cliente.
    """
    if ":" not in carta:
        return carta, {}
    nombre, resto = carta.split(":", 1)
    parametros = {}
    for pareja in resto.split(","):
        if "=" in pareja:
            clave, valor = pareja.split("=", 1)
            parametros[clave] = valor
    return nombre, parametros


def jugar(cartas):
    antes = estado()
    if antes.get("_error"):
        print(pinta(antes))
        return 1
    if antes.get("phase") == "FINISHED":
        print(pinta(antes))
        return 0

    ronda = antes.get("currentRoundNumber")
    mitad = antes.get("halfNumber")
    fallos = []
    for carta in cartas:
        nombre, parametros = descompone(carta)
        respuesta = pide("POST", f"/games/{GAME}/actions", {"cardId": nombre, "parameters": parametros})
        if respuesta and respuesta.get("_error"):
            fallos.append(f"  {nombre}: {respuesta['_error']}")

    pide("POST", f"/games/{GAME}/rounds/ready")

    # Esperar al rival: sin esto el agente tendria que sondear por su cuenta,
    # y cada sondeo es una llamada suya que cuesta.
    limite = time.time() + ESPERA_MAX
    while time.time() < limite:
        ahora = estado()
        if ahora.get("_error"):
            break
        if (ahora.get("phase") == "FINISHED"
                or ahora.get("currentRoundNumber") != ronda
                or ahora.get("halfNumber") != mitad):
            salida = pinta(ahora, ronda_anterior=ronda if ahora.get("halfNumber") == mitad else None)
            if fallos:
                salida = "RECHAZADAS:\n" + "\n".join(fallos) + "\n" + salida
            print(salida)
            return 0
        time.sleep(2)

    print("TIMEOUT esperando al rival\n" + pinta(estado()))
    return 1


def main():
    if len(sys.argv) < 2 or sys.argv[1] not in ("estado", "jugar"):
        print(__doc__)
        return 2
    if not GAME or not TOKEN:
        print("Faltan HD_GAME o HD_TOKEN en el entorno")
        return 2
    if sys.argv[1] == "estado":
        print(pinta(estado()))
        return 0
    return jugar(sys.argv[2:])


if __name__ == "__main__":
    sys.exit(main())
