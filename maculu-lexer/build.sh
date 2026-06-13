#!/usr/bin/env bash
#
# build.sh — gera o AnaLex.java (jFlex), compila tudo e corre um teste.
#
# Uso:
#   ./build.sh            # gera + compila + corre o teste completo
#   ./build.sh --no-run   # apenas gera + compila
#
set -euo pipefail
cd "$(dirname "$0")"

# Localiza o jar do jFlex (qualquer versão jflex-full-*.jar dentro de lib/).
JFLEX_JAR="$(ls lib/jflex-full-*.jar 2>/dev/null | head -n1 || true)"
if [[ -z "${JFLEX_JAR}" ]]; then
  echo "ERRO: não encontrei 'lib/jflex-full-*.jar'."
  echo "      Baixe o jFlex (https://www.jflex.de/) e coloque o jar em lib/."
  exit 1
fi

echo ">> jFlex: ${JFLEX_JAR}"

echo ">> [1/3] Gerando src/maculu/AnaLex.java a partir de flex/MaculuScript.flex ..."
java -jar "${JFLEX_JAR}" -d src/maculu flex/MaculuScript.flex

echo ">> [2/3] Compilando (javac -encoding UTF-8) ..."
mkdir -p out
javac -encoding UTF-8 -d out src/maculu/*.java

echo ">> [2/3] OK. Classes em out/."

if [[ "${1:-}" == "--no-run" ]]; then
  echo ">> Pulando execução (--no-run). Para correr:"
  echo "   java -cp out maculu.Main testes/valido_06_completo.maculu"
  exit 0
fi

echo ">> [3/3] Executando teste de exemplo (valido_06_completo.maculu):"
echo "----------------------------------------------------------------"
java -cp out maculu.Main testes/valido_06_completo.maculu
