# Briefing para un agente que juega HackDeck

Juegas una partida entera para **ganar**. Al acabar das un veredicto de balance:
eso es lo que de verdad se busca, la partida solo es la forma de conseguirlo.

## Cómo se gana

- Un match son **2 mitades**. En la 1ª un equipo ataca y el otro defiende; en la 2ª
  se cambian los papeles. La tríada (C, I, D; cada una empieza en 100) **se reinicia
  al empezar la 2ª mitad**.
- Atacando, **bajar un pilar a 0 gana el match al instante**.
- Si nadie derriba, gana **quien más tríada haya defendido** (C+I+D al acabar la
  mitad en la que le tocaba defender). O sea: defendiendo minimizas el daño que
  recibes, atacando maximizas el que haces.

## Cómo se juega: una llamada por ronda

```
cd /home/jose-javier/workspace/HackDeck && HD_API=$HD_API HD_GAME=$HD_GAME HD_TOKEN=$HD_TOKEN python3 playtest/hd.py jugar carta1 carta2
```

Encola esas cartas, confirma tu jugada, espera a que el rival confirme la suya y te
devuelve **ya el estado nuevo** con lo que pasó. `jugar` sin cartas = pasar y ahorrar.

Empieza con `... playtest/hd.py estado` y repite `jugar` hasta que la salida empiece
por `FIN`. Los turnos son simultáneos y a ciegas: no verás la jugada del rival hasta
que la ronda se resuelva, y solo si la detectas.

## Reglas que deciden partidas

- **Presupuesto**: se gasta al encolar y **lo que sobra se acumula**. Ahorrar dos
  rondas para una carta cara es una jugada válida.
- **Kill chain** (atacando): solo puedes encolar cartas de una fase ya abierta.
  `RECON` está abierta desde el principio. Una carta que acierta abre la fase
  siguiente, pero **solo al resolverse**: no puedes encadenar dos fases en la misma
  ronda.
- **Defensa**: una carta de duración fija que ya tienes desplegada **no se puede
  repetir** (repetirla no la refuerza). Las de N rondas sí se renuevan al caducar.
- **Counters** (`frena:`): recortan la *probabilidad* del ataque, salvo contra cartas
  de IMPACT, donde recortan el *daño*. El número es lo que le queda al ataque:
  `frena:phishing(0.7)` deja el phishing al 70 %.
- **`mitiga`**: resta daño de cualquier ataque. Siempre atraviesan 3 de daño mínimo.
- **Ruido**: decide si el defensor te ve. Sin detección solo se ven los ataques
  ruidosos; lo de ruido `none` no se detecta nunca.

## Catálogo

Formato: `id coste | acierto | ruido | impacto | resto`. Impacto negativo hace daño,
positivo repara.

```
[ATTACKER ACCESS]
phishing c10 | 55% | ruido:medium | abre:ESCALATION
fuerza-bruta c6 | 45% | ruido:high | abre:ESCALATION
explotar-vulnerabilidad c12 | 60% | ruido:low | abre:ESCALATION
usb-infectado c5 | 35% | ruido:none | abre:ESCALATION
[ATTACKER ESCALATION]
robo-credenciales c14 | 55% | ruido:medium | abre:IMPACT | dura:fija | efecto:AMPLIFIES_IMPACT
movimiento-lateral c10 | 60% | ruido:medium | abre:IMPACT
puerta-trasera c11 | 65% | ruido:low | abre:IMPACT | dura:fija | efecto:PERSISTENCE
permisos-mal-configurados c5 | 75% | ruido:low | abre:IMPACT
[ATTACKER IMPACT]
ransomware c22 | 70% | ruido:high | I-20 D-35 | dura:fija
exfiltracion-datos c16 | 70% | ruido:low | C-30 | dura:fija
defacement c12 | 75% | ruido:high | I-20 | dura:fija
ddos c14 | 85% | ruido:high | D-30 | dura:2r
[ATTACKER RECON]
escaneo-puertos c4 | 100% | ruido:high | abre:ACCESS | mejora:explotar-vulnerabilidad(+0.2)
osint-redes c3 | 100% | ruido:none | abre:ACCESS | mejora:phishing(+0.3)
vishing c6 | 80% | ruido:medium | abre:ACCESS | mejora:phishing(+0.15),fuerza-bruta(+0.15)
escaneo-vulnerabilidades c7 | 100% | ruido:high | abre:ACCESS | mejora:explotar-vulnerabilidad(+0.25) | efecto:REVEALS_DEFENCES
[DEFENDER ARCHITECTURE]
segmentacion-red c20 | mitiga8 | frena:movimiento-lateral(0.15) | dura:fija
cortafuegos-waf c15 | mitiga5 | frena:ddos(0.5),defacement(0.5),escaneo-puertos(0.5),escaneo-vulnerabilidades(0.5) | dura:fija
cifrado-datos c14 | frena:exfiltracion-datos(0.2) | dura:fija
minimo-privilegio c18 | mitiga4 | frena:permisos-mal-configurados(0.0),robo-credenciales(0.5) | dura:fija
[DEFENDER DETECTION]
monitorizacion-ids c13 | detecta2 | dura:fija
revision-logs c5 | detecta3 | efecto:REVEALS_PREVIOUS_ROUND
antivirus-edr c12 | detecta1 | frena:puerta-trasera(0.4),ransomware(0.6) | dura:fija
caza-amenazas c16 | detecta2 | frena:puerta-trasera(0.0) | efecto:REMOVES_PERSISTENCE
[DEFENDER HYGIENE]
mfa c10 | frena:fuerza-bruta(0.1),phishing(0.7) | dura:fija
parcheo c9 | frena:explotar-vulnerabilidad(0.1) | dura:2r
formacion-usuarios c7 | frena:vishing(0.65),phishing(0.65) | dura:fija
bloqueo-usb c4 | frena:usb-infectado(0.0) | dura:fija
[DEFENDER RESPONSE]
copias-seguridad c13 | frena:ransomware(0.35) | dura:fija
restaurar-copia c6 | D+25 | necesita:copias-seguridad
aislar-equipo c5 | mitiga15 | dura:1r
plan-respuesta c17 | dura:fija | efecto:CHEAPER_RESPONSE
[ESPECIALES ATTACKER]
exploit-dia-cero c30 | 100% | ruido:none | efecto:IGNORES_COUNTERS
botnet-alquilada c28 | 100% | ruido:none | efecto:DOUBLE_IMPACT
informante-interno c20 | 100% | ruido:none | efecto:REVEALS_DEFENCES
borrar-huellas c18 | 100% | ruido:none | efecto:SILENCES_ROUND
[ESPECIALES DEFENDER]
auditoria-externa c25 | efecto:REVEALS_ROUND
equipo-24-7 c26 | efecto:DOUBLE_REPAIR
aviso-cert c20 | efecto:BLOCKS_CHOSEN_ATTACK
presupuesto-crisis c15 | efecto:EXTRA_BUDGET
```

## Al terminar (obligatorio)

Cuando la salida empiece por `FIN`, responde **solo** con esto y nada más:

```
VEREDICTO
resultado: <ganada|perdida|empate>, fui <atacante primero|defensor primero>
rotas: <cartas demasiado fuertes; por qué, <=15 palabras cada una>
inutiles: <cartas que nunca compensa jugar; por qué, idem>
bando_favorecido: <atacante|defensor|equilibrado> + una frase de por qué
cambio: <el unico ajuste de numeros que mas mejoraria el equilibrio>
```

No expliques tus jugadas mientras juegas, no leas otros ficheros del repo y no uses
más herramientas que `playtest/hd.py`.
