#!/usr/bin/env bash
#
# run-api.sh — arranca o servidor web da MaculuScript (frontend + API REST).
#
# Uso:
#   ./run-api.sh            # porta 8080
#   ./run-api.sh 8090       # outra porta
#
# Pre-requisito: ter corrido ./build.sh (ou ./build.sh --no-run) para gerar out/.
#
set -euo pipefail
cd "$(dirname "$0")"

if [[ ! -d out ]]; then
  echo "ERRO: pasta 'out/' nao existe. Corra primeiro:  ./build.sh --no-run"
  exit 1
fi

PORT="${1:-8090}"
echo ">> MaculuScript Web em http://127.0.0.1:${PORT}  (Ctrl+C para parar)"
exec java -cp out maculu.ServidorApi "${PORT}" web
