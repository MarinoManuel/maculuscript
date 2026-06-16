# MaculuScript Lexer

Analisador léxico da linguagem **MaculuScript**, implementado em **Java** e gerado com **jFlex**. O projeto lê código `.maculu`, produz tokens, mantém tabela de símbolos, recolhe erros léxicos com linha e coluna e também expõe uma API HTTP simples para integrar com um frontend web.

O código principal fica na pasta [maculu-lexer](maculu-lexer), que contém o lexer, os testes, o frontend e os scripts de build e execução.

## Visão Geral

- Lexer completo para MaculuScript.
- Saída em modo CLI para ficheiros `.maculu` ou `stdin`.
- API interna via `AnalisadorLexico.analisar(String)` para reutilização em backend/REST.
- Servidor web local com endpoints JSON e frontend estático.
- Testes de exemplos válidos e com erro na pasta `testes/`.

## Estrutura

```text
maculuscript/
├── PROMPT_Gerar_AnalisadorLexico_MaculuScript.md
└── maculu-lexer/
    ├── build.sh
    ├── DEPLOY.md
    ├── README.md
    ├── run-api.sh
    ├── deploy/
    ├── flex/
    ├── lib/
    ├── src/
    ├── testes/
    └── web/
```

## Requisitos

- **JDK 11+** com `java` e `javac` no `PATH`.
- **jFlex** em `maculu-lexer/lib/` como `jflex-full-*.jar`.

## Como executar

Entre na pasta do projeto principal antes de compilar ou correr:

```bash
cd maculu-lexer
```

### Build rápido

```bash
./build.sh
```

Isto gera o `AnaLex.java`, compila tudo para `out/` e corre um teste de exemplo.

### Compilar sem executar teste

```bash
./build.sh --no-run
```

### Executar pela linha de comandos

```bash
java -cp out maculu.Main testes/valido_06_completo.maculu
```

Também é possível ler de `stdin`:

```bash
echo 'classe X { inteiro i = 1; }' | java -cp out maculu.Main
```

### Ver palavras reservadas

```bash
java -cp out maculu.Main --reservadas
```

## Servidor Web

O projeto inclui um servidor HTTP simples, sem dependências externas, que serve o frontend e a API JSON.

```bash
./run-api.sh 8090
```

Endereços úteis:

- `POST /api/analisar` — analisa código MaculuScript e devolve tokens, erros e símbolos.
- `GET /api/reservadas` — devolve o mapeamento de palavras reservadas para Java.

## Testes

A pasta `testes/` contém ficheiros `.maculu` válidos e com erros para validar o comportamento do lexer.

## Documentação adicional

- [README detalhado](maculu-lexer/README.md)
- [Deploy em Ubuntu + nginx](maculu-lexer/DEPLOY.md)
