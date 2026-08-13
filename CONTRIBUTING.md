# Contribuir a HackDeck

Gracias por querer echar una mano. Este proyecto nace para el plan de apoyo del
*Cyberbootcamp Málaga*, así que la vara de medir siempre es la misma: **¿esto se entiende en
un aula de bachillerato?** Un cambio técnicamente brillante que haga el juego más difícil de
explicar no entra.

## Antes de escribir código

- **Para algo grande, abre un issue primero.** Es más rápido discutir el enfoque que revisar
  200 líneas que había que tirar.
- **Las librerías se consultan.** El stack es minimalista a propósito: el backend solo tiene
  Spring Boot y jjwt, y el frontend solo React. No hay router, ni gestor de estado, ni
  librería de estilos, ni de i18n, y no es un descuido. Si crees que hace falta una,
  explícalo en el issue antes.

## Cómo trabajamos

- **Una rama por tarea**, saliendo de `main`: `feature/lo-que-sea`, `fix/lo-que-sea`.
- **Commits atómicos.** Un commit es un cambio con sentido propio que deja el código
  coherente. Nada de "varios arreglos".
- **Formato del mensaje:** `[ÁMBITO] Descripción en español`, con ámbitos `BACKEND`,
  `FRONTEND`, `BUILD`, `DOCS` o `REFACTOR`. El asunto dice **qué** cambia; si hace falta el
  **porqué**, va en el cuerpo.
- **Nada de firmas de herramientas de IA** en los commits ni en las descripciones de PR.

## Estilo

- **Español** en el código, los comentarios y el dominio (`ronda`, `partida`, `tríada`,
  `presupuesto`…). La interfaz, en cambio, tiene que funcionar en **español e inglés**: los
  textos van al diccionario de `frontend/src/i18n/`, nunca incrustados en el JSX.
- **Comentarios: solo los imprescindibles.** No se comenta lo que el código ya dice. Se
  comenta una regla de juego no evidente, una decisión y su porqué, o una trampa. Si un
  comentario explica *qué* hace el código, sobra: renombra en su lugar.
- **Respeta las capas.** El dominio (`domain/`) no sabe nada de Spring y **toda la lógica de
  juego vive en `domain/rules`**, nunca en los modelos ni en el servicio de aplicación ni en
  los adaptadores. La aplicación depende de puertos, no de implementaciones.
- **El servidor es la única fuente de verdad.** No te fíes del cliente para nada: el bando,
  el coste y el efecto de una carta los pone el servidor.
- **La niebla de guerra se decide en un solo sitio**, `GameViewProjector`. Si añades un dato
  al estado, pregúntate quién debe verlo; dos caminos distintos acabarían filtrando cosas
  distintas.

## Cambiar el equilibrio del juego

Los costes, impactos, probabilidades y counters de las 44 cartas están en
`src/main/resources/catalog/cartas.yaml`, comentados carta a carta. Para reequilibrar **no
hace falta tocar código**: edita el YAML y arranca con
`--hackdeck.catalog.path=/ruta/a/tu-catalogo.yaml`.

Si propones un cambio de balance, cuenta con qué lo has probado. El juego todavía no ha
pasado por un aula real, así que los datos de partidas de verdad valen más que cualquier
razonamiento.

## Antes de abrir el PR

```bash
./build.sh   # tiene que construir front y backend sin avisos nuevos
```

Y prueba lo que tocas jugando una partida: crear, unirse con dos equipos, jugar cartas y
resolver. Buena parte de los fallos de este proyecto han salido pulsando botones, no leyendo
código.
