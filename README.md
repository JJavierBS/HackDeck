# HackDeck

Videojuego educativo de ciberseguridad por turnos para el aula. Dos equipos se enfrentan:
el **rojo** ataca una infraestructura y el **azul** la defiende. Gana quien derribe un pilar
de la tríada CIA (confidencialidad, integridad, disponibilidad) o quien mejor la defienda.

Está pensado para alumnado de bachillerato sin conocimientos previos: las 44 cartas son
técnicas reales (phishing, MFA, ransomware, segmentación de red…) explicadas en una frase.

## Cómo se juega

Un **match** son dos mitades. En la primera el equipo A ataca y B defiende; en la segunda se
cambian los bandos y la tríada vuelve a 100, así que se puede comparar directamente quién
defendió mejor.

Cada ronda, ambos equipos **eligen sus cartas en secreto** dentro de su presupuesto y el
instructor resuelve. Entonces se revela lo que ha pasado:

- El **atacante** progresa por la kill chain (reconocimiento → acceso → escalada → impacto).
  Puede atacar directamente, pero sin las fases previas se queda en el 30 % de su
  probabilidad.
- El **defensor** solo ve los ataques que ha llegado a **detectar**: hace falta ruido del
  ataque más capacidad de detección propia. Un DDoS se nota aunque no haya nadie mirando;
  una exfiltración silenciosa solo si hay con qué mirar.
- Al que va por detrás se le da presupuesto extra, para que una partida perdida no se
  vuelva aburrida.

Al terminar, el instructor puede **rebobinar el match ronda a ronda** para comentarlo en
clase, viendo también lo que cada bando no llegó a enterarse.

## Arrancarlo en 3 comandos

Necesitas **JDK 25**, **Maven** y **Node**.

```bash
git clone <este-repositorio> && cd HackDeck
./build.sh
java -jar target/hack-deck-backend.jar
```

Abre <http://localhost:8080>. El jar sirve la API y la interfaz, no hace falta nada más.

### En el aula (LAN)

Arranca el jar en el portátil del instructor. Al iniciarse imprime por qué direcciones
pueden entrar los equipos, y esas mismas direcciones aparecen en su pantalla mientras
espera en el lobby:

```
HackDeck listo. Instructor: http://localhost:8080
Los equipos entran por: http://192.168.1.50:8080
```

Los equipos entran con el **código de partida** de seis letras, como en Kahoot. Si no
llegan, casi siempre es el **cortafuegos** del portátil bloqueando el puerto 8080.

El instructor tiene además una **pantalla de proyección** en su propia URL, pensada para el
cañón: código, marcador y reloj en grande. No muestra el registro ni las cartas encoladas,
porque en la pared se leerían los ataques que el defensor todavía no ha detectado.

### Desarrollo

Con dos terminales, y recarga en caliente del frontend:

```bash
mvn spring-boot:run          # backend en :8080
cd frontend && npm run dev   # frontend en :5173
```

### En un servidor

```bash
export HACKDECK_JWT_SECRET='una-clave-larga-de-al-menos-32-caracteres'
export HACKDECK_INSTRUCTOR_KEY='la-clave-para-crear-partidas'
docker compose up --build
```

O sin Docker, con `java -jar hack-deck-backend.jar --spring.profiles.active=cloud`.

> **Importante detrás de un proxy inverso.** Crear partidas está abierto desde la propia
> máquina y exige clave desde fuera. Si el proxy no reenvía la IP real, aparece como cliente
> local y **cualquiera podría crear partidas**. El perfil `cloud` ya lo desactiva y activa
> `forward-headers-strategy`; asegúrate de usarlo.

## Cambiar las cartas sin recompilar

El catálogo está en [`src/main/resources/catalog/cartas.yaml`](src/main/resources/catalog/cartas.yaml),
con el coste, el ruido, la probabilidad y los counters de cada carta comentados. Para usar
otro:

```bash
java -jar target/hack-deck-backend.jar --hackdeck.catalog.path=/ruta/a/mi-catalogo.yaml
```

Si algo está mal escrito el servidor no arranca y dice qué carta y qué campo falla, en vez
de fallar a mitad de una clase.

## Configuración

| Variable | Por defecto | Para qué |
|---|---|---|
| `PORT` | `8080` | Puerto del servidor. |
| `HACKDECK_JWT_SECRET` | aleatoria | Clave de firma de los tokens. Si no se fija, los tokens mueren al reiniciar. |
| `HACKDECK_INSTRUCTOR_KEY` | vacía | Clave para crear partidas desde fuera de la máquina. |
| `HACKDECK_ALLOW_LOOPBACK` | `true` | Exime de la clave a las peticiones locales. **Ponlo a `false` detrás de un proxy.** |
| `HACKDECK_CORS_ORIGINS` | `http://localhost:5173` | Solo hace falta en desarrollo. |
| `HACKDECK_CATALOG_PATH` | catálogo empaquetado | Catálogo de cartas externo. |

Las partidas viven **en memoria**: al reiniciar el servidor se pierden, igual que los
tokens. Es lo previsto para el uso en el aula.

## Arquitectura

Backend en **Java 25 + Spring Boot 4** con arquitectura hexagonal: el dominio no sabe nada
de Spring y toda la lógica de juego vive en `domain/rules`. Frontend en **React 19 + Vite +
TypeScript**, sin router ni gestor de estado ni librerías de estilos.

El servidor es la única fuente de verdad, y el estado se proyecta **filtrado por rol** en un
solo sitio (`GameViewProjector`) para el REST y para el WebSocket por igual: lo que un
equipo no debe saber no sale del servidor.

Los detalles del dominio, las reglas y el roadmap están en `CLAUDE.md`.

## Contribuir

Las convenciones del proyecto están en [CONTRIBUTING.md](CONTRIBUTING.md). En resumen: una
rama por tarea, commits atómicos, español en el código e inglés y español en la interfaz, y
consultar antes de añadir librerías.

Para reequilibrar el juego no hace falta tocar código: el catálogo es un YAML.

## Licencia

[MIT](LICENSE). Puedes usarlo, modificarlo y redistribuirlo, incluso con fines comerciales,
**manteniendo el aviso de copyright** en las copias o derivados.
