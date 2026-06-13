# Analisador Léxico — MaculuScript (Java + jFlex)

Analisador léxico completo da linguagem **MaculuScript**, escrito em **Java** e
gerado com **jFlex**. Lê código `.maculu`, produz a lista de tokens, mantém uma
tabela de palavras reservadas e uma tabela de símbolos (ambas tabelas hash) e
recolhe todos os erros léxicos com **linha e coluna** (recuperação em modo
pânico — nunca aborta na primeira ocorrência).

Está preparado para ser usado como **API**: o método
`AnalisadorLexico.analisar(String)` devolve um `ResultadoAnalise` com tokens,
erros e tabela de símbolos (tudo fácil de serializar para JSON num futuro site).

---

## 1. Requisitos

- **JDK 11 ou superior** (`java` e `javac` no PATH).
- **jFlex** (testado com a versão **1.9.1**): baixe `jflex-full-1.9.1.jar` em
  <https://www.jflex.de/> e coloque-o em **`lib/`**.

> O `build.sh` aceita qualquer `lib/jflex-full-*.jar`.

---

## 2. Estrutura do projeto

```
maculu-lexer/
├── flex/
│   └── MaculuScript.flex          # especificação jFlex
├── src/maculu/
│   ├── TipoToken.java             # enum com todos os tipos de token
│   ├── Token.java                 # <linha, tipo, atributo>
│   ├── Simbolo.java               # entrada da tabela de símbolos
│   ├── TabelaPalavrasReservadas.java
│   ├── TabelaSimbolos.java
│   ├── ErroLexico.java            # <linha, coluna, mensagem>
│   ├── ResultadoAnalise.java      # { tokens, erros, tabelaSimbolos }
│   ├── AnaLex.java                # GERADO pelo jFlex (não editar à mão)
│   ├── AnalisadorLexico.java      # fachada / API
│   └── Main.java                  # driver de linha de comando
├── testes/                        # programas .maculu (válidos e com erros)
├── lib/                           # coloque aqui o jflex-full-1.9.1.jar
├── build.sh
└── README.md
```

---

## 3. Build e execução

### Forma rápida (recomendada)

```bash
./build.sh                 # gera o AnaLex.java, compila e corre um teste
./build.sh --no-run        # apenas gera e compila
```

### Forma manual (igual à secção 13 do enunciado)

```bash
# 1. Gerar AnaLex.java a partir da especificação jFlex
java -jar lib/jflex-full-1.9.1.jar -d src/maculu flex/MaculuScript.flex

# 2. Compilar (UTF-8 é importante por causa dos acentos das mensagens)
javac -encoding UTF-8 -d out src/maculu/*.java

# 3. Correr um teste
java -cp out maculu.Main testes/valido_06_completo.maculu
```

Também pode ler de `stdin`:

```bash
echo 'classe X { inteiro i = 1; }' | java -cp out maculu.Main
```

Para ver a **tabela de mapeamento** palavra reservada → equivalente em Java
(requisito 5 do docente):

```bash
java -cp out maculu.Main --reservadas
```

> **Windows:** para ver os acentos correctamente na consola, execute antes
> `chcp 65001`. A saída do programa é sempre gerada em UTF-8.

---

## 4. Formato da saída

```
=== TOKENS ===
<1, CLASSE, ->
<1, IDENTIFICADOR, "principal">
<1, ABRE_CHAVE, ->
...

=== TABELA DE SÍMBOLOS ===
principal  (1ª linha: 1)
idade      (1ª linha: 2)

=== ERROS LÉXICOS ===
Linha 7, Coluna 12: Caractere inválido: '@'
(ou)  Sem erros léxicos.
```

- Tokens **com** atributo: `<linha, TIPO, "atributo">`.
- Tokens **sem** atributo: `<linha, TIPO, ->`.

---

## 5. Uso como API (futuro site/REST)

```java
ResultadoAnalise r = AnalisadorLexico.analisar(codigoColadoPeloUtilizador);

r.getTokens();          // List<Token>
r.getErros();           // List<ErroLexico>
r.getTabelaSimbolos();  // Collection<Simbolo>
r.temErros();           // boolean
```

- O método produtor de tokens é `AnaLex.proximoToken()` (devolve um `Token` por
  chamada); existe também o alias `AnaLex.analex()` com o nome usado no enunciado
  do docente.
- Nada é escrito no ecrã dentro de `AnaLex` nem de `AnalisadorLexico`
  (só o `Main` imprime), por isso a mesma lógica serve CLI e servidor.

### Servidor web (frontend + API REST)

Camada **aditiva** (não altera o núcleo): `maculu.ServidorApi` usa o HTTP server
do próprio JDK (sem dependências) e serve o frontend `web/index.html` mais a API.

```bash
./build.sh --no-run          # compila (inclui ServidorApi e JsonUtil)
./run-api.sh 8080            # abre http://127.0.0.1:8080
```

Endpoints:
- `POST /api/analisar` — corpo = código MaculuScript (texto); devolve
  `{ temErros, tokens[], erros[], tabelaSimbolos[] }`.
- `GET /api/reservadas` — mapeamento reservada → Java em JSON.

Para colocar online (Ubuntu + nginx + HTTPS), ver **[DEPLOY.md](DEPLOY.md)**.
- Cada chamada a `analisar(...)` cria **novas** instâncias das tabelas, sem
  estado global partilhado — seguro para vários pedidos em simultâneo.

---

## 6. A linguagem MaculuScript (resumo)

- Extensão `.maculu`; **case-insensitive** (`ARRAY` ≡ `array`).
- Comentários: `// linha` e `/* bloco */`.
- Identificadores: `[a-zA-Z][a-zA-Z0-9_]*` (sem acentos).
- Tipos: `inteiro`, `real`, `caracter`, `texto`, `logico`, `vazio`, `vector`.
- Operadores lógicos são **palavras**: `e`, `ou`, `nao`.
  Um `!` isolado é **erro léxico** (só é válido dentro de `!=`).
- Escapes em texto/caracter: `\n`, `\t`, `\"`, `\'`, `\\`.

A lista completa de palavras reservadas e o respectivo equivalente em Java está
em `TabelaPalavrasReservadas.java`.

---

## 7. Erros léxicos detectados

Todos com linha + coluna, recolhidos numa lista (modo pânico):

1. **Caractere inválido** — símbolo fora do alfabeto (`@`, `#`, `~`, `!` isolado).
2. **Texto (string) não terminado** — `"` sem fecho até ao fim da linha/ficheiro.
3. **Caracter não terminado / inválido** — `'` sem fecho, vazio, ou com mais de um caractere.
4. **Comentário de bloco não terminado** — `/*` sem `*/` até ao fim do ficheiro.

---

## 8. Ficheiros de teste (`testes/`)

O conjunto cresce em tamanho e cobertura (metodologia sugerida pelo docente).

**Válidos** (sem erros léxicos):
- `valido_01_ola` — classe mínima com `escreva`.
- `valido_02_variaveis` — todos os tipos + escapes.
- `valido_03_controlo` — `se/senao`, `enquanto`, `para`, `escolha/caso`.
- `valido_04_funcoes` — funções com/sem retorno e chamadas.
- `valido_05_classe` — atributos privados, construtor, `novo`, `este`.
- `valido_06_completo` — vectores, classe, excepções, I/O, todos os operadores.
- `valido_07_numeros_expressoes` — inteiros, reais e expressões.
- `valido_08_operadores_densos` — todos os operadores + *maximal munch* (`a+++a`).
- `valido_09_comentarios` — formas de comentário, não-aninhamento, símbolos dentro.
- `valido_10_vectores` — vectores e indexação.

**Com erros** (provam a recuperação em modo pânico):
- `erro_01_caractere_invalido` — `@`, `#`, `!` isolado.
- `erro_02_texto_nao_terminado` — `"` sem fecho (fim de linha).
- `erro_03_comentario_nao_terminado` — `/*` sem `*/` (EOF).
- `erro_04_misturado` — vários erros + código válido.
- `erro_05_simbolos_invalidos` — `@ # ~ ? & | ^ $`.
- `erro_06_caracteres_literais` — `''` vazio, `'xy'`, `'abc'`.
- `erro_07_texto_no_eof` — `"` sem fecho no fim do ficheiro.
- `erro_08_escapes_invalidos` — `\x`, `\q`, `\z` em texto.
- `erro_09_identificador_underscore` — identificador a começar por `_`.
- `erro_10_stress_recuperacao` — mistura densa de 6 tipos de erro.
