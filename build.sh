#!/usr/bin/env bash
#
# Construye el frontend, lo mete dentro del jar del backend y empaqueta todo
# en un unico artefacto: target/cyber-deck-backend.jar sirve la API y la UI.
#
# Se hace con un script y no con un plugin de Maven para no arrastrar una
# dependencia de build ni descargar un Node aparte.

set -euo pipefail
cd "$(dirname "$0")"

ESTATICOS=src/main/resources/static

echo "==> Frontend"
cd frontend
if [ -d node_modules ]; then npm install --silent; else npm ci; fi
npm run build
cd ..

echo "==> Copiando el frontend al backend"
rm -rf "$ESTATICOS"
mkdir -p "$ESTATICOS"
cp -r frontend/dist/. "$ESTATICOS"/

echo "==> Backend"
mvn -q package "$@"

echo
echo "Listo: target/cyber-deck-backend.jar"
echo "Arrancalo con: java -jar target/cyber-deck-backend.jar"
