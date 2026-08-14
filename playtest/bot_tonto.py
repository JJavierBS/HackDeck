#!/usr/bin/env python3
"""Bot de guion para probar el arbitro sin gastar un agente de verdad.

No juega bien y no pretende hacerlo: solo recorre una partida entera para
comprobar que el cliente, el cierre automatico y el cambio de bando
funcionan antes de poner a jugar a nadie que cueste dinero.

    bot_tonto.py <gameId> <token>
"""
import os
import subprocess
import sys

AQUI = os.path.dirname(os.path.abspath(__file__))
ATAQUE_POR_FASE = {
    "IMPACT": ["ransomware"],
    "ESCALATION": ["permisos-mal-configurados"],
    "ACCESS": ["phishing"],
    "RECON": ["escaneo-puertos"],
}
DEFENSAS = ["mfa", "cortafuegos-waf", "monitorizacion-ids", "segmentacion-red",
            "copias-seguridad", "parcheo", "formacion-usuarios", "minimo-privilegio"]


def hd(entorno, *args):
    return subprocess.run([sys.executable, os.path.join(AQUI, "hd.py"), *args],
                          capture_output=True, text=True, env=entorno).stdout.strip()


def main(gid, token):
    entorno = dict(os.environ, HD_GAME=gid, HD_TOKEN=token)
    salida = hd(entorno, "estado")
    puestas = set()
    for _ in range(40):
        print(salida, "\n---", flush=True)
        if salida.startswith("FIN") or salida.startswith("ERROR") or salida.startswith("TIMEOUT"):
            return 0
        atacando = "BANDO: ATTACKER" in salida
        if atacando:
            fases = next((l for l in salida.splitlines() if l.startswith("FASES")), "")
            for fase, cartas in ATAQUE_POR_FASE.items():
                if fase in fases:
                    eleccion = cartas
                    break
            else:
                eleccion = ["escaneo-puertos"]
        else:
            eleccion = [c for c in DEFENSAS if c not in puestas][:1]
            puestas.update(eleccion)
        salida = hd(entorno, "jugar", *eleccion)
    return 1


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print(__doc__)
        sys.exit(2)
    sys.exit(main(sys.argv[1], sys.argv[2]))
