# playtest — partidas automáticas para ajustar el balance

Arnés para que agentes (o guiones) jueguen partidas enteras y dejen datos con los
que ajustar `catalog/cartas.yaml`. Nace de la fase 10 del roadmap: el balance no se
decide leyendo el YAML, se decide viendo partidas.

**No forma parte del juego** y no lo usa el backend: es utillaje de desarrollo.

## Cómo se lanza una tanda

```bash
java -jar target/hack-deck-backend.jar --server.port=8081 &
export HD_API=http://localhost:8081/api/v1

python3 playtest/arbitro.py crea mesa1     # imprime gameId y un token por equipo
```

`arbitro.py` monta la partida, sienta a los dos equipos y **enciende el cierre
automático**: sin eso haría falta un instructor humano pulsando «resolver» en cada
ronda. Cada equipo juega con:

```bash
HD_GAME=<id> HD_TOKEN=<token> python3 playtest/hd.py jugar mfa parcheo
```

`hd.py jugar` encola, confirma y **espera a que el rival confirme**, devolviendo ya
el estado nuevo. Una llamada = una ronda: es lo que hace viable pagar un agente por
jugar, porque sondear el estado en bucle sale caro.

## Qué mide cada pieza

| Guion | Para qué |
|---|---|
| `arbitro.py` | Crea la mesa y espera a que acabe. |
| `hd.py` | Cliente del jugador: estado compacto y ronda completa por llamada. |
| `briefing.md` | Lo que se le pasa al agente: reglas, catálogo y formato del veredicto. |
| `resumen_catalogo.py` | Regenera el catálogo del briefing **desde el servidor**, para que no se desincronice del YAML. |
| `analiza.py` | Estadísticas del historial de varias partidas. Sin modelo: lo que mide un script no se le pregunta a un agente. |
| `bot_tonto.py` | Bot de guion para validar el arnés antes de gastar agentes. |

Tras cambiar `cartas.yaml`, regenera el catálogo del briefing:

```bash
python3 playtest/resumen_catalogo.py <token-instructor>   # y pégalo en briefing.md
```

## Lo que estos datos no son

Los agentes juegan razonablemente, pero **no son alumnos de bachillerato**. Sirven
para detectar cartas rotas, cartas que nadie compra y tendencias de bando; no
sustituyen al playtesting con la clase, que es lo que dirá si el juego se entiende.
Con el azar que lleva el juego, ocho partidas dan tendencia, no certeza.
