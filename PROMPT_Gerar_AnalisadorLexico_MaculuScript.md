# PROMPT — Geração do Analisador Léxico da linguagem MaculuScript (Java + jFlex)

> Copia este documento inteiro e entrega-o à IA que vai gerar o código.
> O documento é auto-suficiente: contém a especificação da linguagem, os requisitos
> do docente, a estrutura de ficheiros, as assinaturas de todas as classes, o
> conteúdo do ficheiro `.flex`, o tratamento de erros e os programas de teste.

---

## 1. PAPEL E OBJETIVO

Aja como um engenheiro de compiladores sénior. A sua tarefa é **implementar um
Analisador Léxico completo e funcional** para a linguagem de programação
**MaculuScript**, usando **Java** e a ferramenta **jFlex**.

O resultado tem de:

1. Compilar e correr sem erros.
2. Cumprir TODOS os requisitos do docente (secção 3).
3. Ser organizado em classes limpas (secção 5).
4. Reportar erros léxicos com número de linha e coluna, **sem parar** na primeira
   ocorrência (recuperação de erro — recolher todos os erros).
5. Incluir uma pasta `testes/` com programas de exemplo, válidos e com erros.
6. Estar preparado para ser usado depois como **API** (um método que recebe o
   código-fonte em `String` e devolve uma estrutura com tokens + erros), pois mais
   tarde será integrado num site onde o utilizador cola código e vê a análise.

Não invente palavras reservadas, tokens ou regras: use **exactamente** as tabelas
fornecidas nas secções 7 e 8.

---

## 2. CONTEXTO DA LINGUAGEM (MaculuScript)

| Característica | Valor |
|---|---|
| Nome | MaculuScript |
| Extensão | `.maculu` |
| Case-sensitive | **Não** (converter tudo para minúsculas antes de comparar) |
| Paradigma | Imperativo + OO simples |
| Tipagem | Estática e explícita |
| Comentário de linha | `// ...` |
| Comentário de bloco | `/* ... */` (pode abranger várias linhas) |
| Fim de instrução | `;` |
| Blocos | `{ }` |
| Identificadores | `[a-zA-Z][a-zA-Z0-9_]*` — **sem acentos** |
| Escapes suportados em texto/caracter | `\n` `\t` `\"` `\'` `\\` |

---

## 3. REQUISITOS OBRIGATÓRIOS DO DOCENTE (cumprir todos)

1. **Construir a classe `AnaLex`** com métodos para análise léxica. Pelo menos um
   método produtor de tokens deve existir.
2. O método deve **ler** o conteúdo do programa passado como parâmetro e **devolver
   os tokens** encontrados.
3. **Controlar a numeração das linhas** e **ignorar comentários**. Para cada token
   reconhecido deve ser possível exibir:
   `<Número da linha, Token, Atributo (quando possuir)>`.
4. O método produtor de tokens **devolve um token sempre que for chamado**. Esse
   método **NÃO escreve o token no ecrã** — apenas produz o token para outro método
   (ex.: `Main`) que é o responsável pela saída.
5. As **palavras-chave são reservadas** (não podem ser identificadores). Construir
   uma tabela própria e uma **tabela de mapeamento** entre cada palavra reservada da
   MaculuScript e o seu equivalente em Java.
6. **Criar e gerir uma tabela de palavras reservadas** (hash). Sempre que um
   identificador for encontrado, faz-se busca nessa tabela. Se encontrar →
   palavra-chave → devolve o token da palavra-chave. Caso contrário → identificador →
   inserir na tabela de símbolos (se ainda não existir).
7. **Implementar a tabela de símbolos como tabela hash.** Ao reconhecer um
   identificador: 1.º procurar nas palavras reservadas; se falhar, 2.º procurar na
   tabela de símbolos e devolver o token associado se existir; caso contrário,
   inserir e devolver o token.
8. **Linguagem case-insensitive:** `ARRAY` ≡ `array`. Converter para minúsculas
   antes de qualquer comparação.

---

## 4. ESTRUTURA DE PASTAS E FICHEIROS A CRIAR

```
maculu-lexer/
├── flex/
│   └── MaculuScript.flex          # especificação jFlex (secção 6)
├── src/
│   └── maculu/
│       ├── TipoToken.java         # enum com todos os tipos de token
│       ├── Token.java             # representa um token <linha, tipo, atributo>
│       ├── Simbolo.java           # entrada da tabela de símbolos
│       ├── TabelaPalavrasReservadas.java
│       ├── TabelaSimbolos.java
│       ├── ErroLexico.java        # representa um erro léxico <linha, coluna, msg>
│       ├── ResultadoAnalise.java  # { List<Token> tokens; List<ErroLexico> erros }
│       ├── AnaLex.java            # GERADO pelo jFlex a partir do .flex
│       ├── AnalisadorLexico.java  # fachada/API: analisar(String) -> ResultadoAnalise
│       └── Main.java              # driver de linha de comando / saída no ecrã
├── testes/
│   ├── valido_01_ola.maculu
│   ├── valido_02_variaveis.maculu
│   ├── valido_03_controlo.maculu
│   ├── valido_04_funcoes.maculu
│   ├── valido_05_classe.maculu
│   ├── valido_06_completo.maculu
│   ├── erro_01_caractere_invalido.maculu
│   ├── erro_02_texto_nao_terminado.maculu
│   ├── erro_03_comentario_nao_terminado.maculu
│   └── erro_04_misturado.maculu
├── lib/
│   └── (colocar aqui o jflex-full-x.y.z.jar)
├── build.sh                       # gera AnaLex.java, compila e corre
└── README.md                      # instruções de build e uso
```

---

## 5. ESPECIFICAÇÃO DAS CLASSES (Java)

Implemente exactamente estas classes (pode adicionar métodos auxiliares).

### 5.1 `TipoToken` (enum)
Enum com TODOS os tipos da secção 8 (tipos de dado, controlo de fluxo, valores
lógicos, OO, modificadores, excepções, I/O, operadores, pontuação, literais,
`IDENTIFICADOR`, `EOF`). Não inclua `e/ou/nao` como tokens de símbolo — eles vêm da
tabela de palavras reservadas.

### 5.2 `Token`
```java
public class Token {
    private final int linha;          // 1-based
    private final TipoToken tipo;
    private final String atributo;    // null quando o token não tem atributo

    public Token(int linha, TipoToken tipo, String atributo) { ... }
    // getters
    // toString() deve produzir o formato pedido pelo docente:
    //   com atributo:  <linha, TIPO, "atributo">
    //   sem atributo:  <linha, TIPO, ->
}
```

### 5.3 `Simbolo`
Entrada da tabela de símbolos. Guardar pelo menos: `nome` (em minúsculas),
`primeiraLinha` (linha onde apareceu pela primeira vez). Campos extra como `tipo`
podem ficar reservados para o futuro analisador sintáctico.

### 5.4 `TabelaPalavrasReservadas`
- Estrutura interna: `HashMap<String, TipoToken>` (chave em minúsculas).
- Inicializa-se no construtor com TODAS as entradas da secção 7.
- Manter também `HashMap<String, String>` com o **mapeamento para Java**
  (requisito 5) e um método `String equivalenteJava(String palavra)`.
- Métodos: `boolean ehReservada(String palavra)`,
  `TipoToken tokenDe(String palavra)`.
- Toda a comparação faz `palavra.toLowerCase()` primeiro.

### 5.5 `TabelaSimbolos`
- Estrutura interna: `HashMap<String, Simbolo>` (chave em minúsculas).
- Métodos: `boolean contem(String nome)`,
  `Simbolo inserir(String nome, int linha)` (insere só se não existir),
  `Simbolo obter(String nome)`,
  `Collection<Simbolo> todos()` (para imprimir a tabela no fim).

### 5.6 `ErroLexico`
```java
public class ErroLexico {
    private final int linha;     // 1-based
    private final int coluna;    // 1-based
    private final String lexema; // texto que causou o erro (quando aplicável)
    private final String mensagem;
    // construtor, getters, toString() amigável p/ a saída e p/ a futura API
}
```

### 5.7 `ResultadoAnalise`
Objecto de retorno pensado para a **API/site**:
```java
public class ResultadoAnalise {
    private final List<Token> tokens;
    private final List<ErroLexico> erros;
    private final Collection<Simbolo> tabelaSimbolos;
    public boolean temErros() { return !erros.isEmpty(); }
    // getters
}
```

### 5.8 `AnaLex` (gerado pelo jFlex)
- Gerado a partir de `flex/MaculuScript.flex`.
- A classe chama-se `AnaLex` (via `%class AnaLex`).
- O método produtor de tokens chama-se `proximoToken()` (via `%function`), devolve
  um `Token` por chamada e devolve um token de tipo `EOF` no fim.
- **Não imprime nada** (requisito 4).
- Mantém internamente uma `List<ErroLexico>` acessível via `getErros()` e uma
  referência partilhada à `TabelaPalavrasReservadas` e à `TabelaSimbolos`.
- Em caso de caractere inválido / literal não terminado: **regista o erro na lista,
  recupera e continua** a análise (modo pânico: salta o caractere ofensivo). Nunca
  lança excepção que aborte tudo.

### 5.9 `AnalisadorLexico` (fachada — esta é a “API”)
```java
public class AnalisadorLexico {
    // Método central que o site/REST vai chamar no futuro:
    public static ResultadoAnalise analisar(String codigoFonte) {
        // 1. cria TabelaPalavrasReservadas e TabelaSimbolos
        // 2. cria AnaLex sobre um Reader da String
        // 3. faz o loop: Token t; while ((t = lex.proximoToken()).getTipo() != EOF) tokens.add(t);
        // 4. recolhe lex.getErros() e tabela de símbolos
        // 5. devolve ResultadoAnalise
    }
}
```
> É aqui que se cumpre o requisito 4: `AnaLex.proximoToken()` produz um token de
> cada vez; é esta fachada (e o `Main`) que consome e organiza a saída.

### 5.10 `Main`
- Recebe por argumento o caminho de um ficheiro `.maculu` (ou lê de `stdin`).
- Chama `AnalisadorLexico.analisar(...)`.
- Imprime no ecrã, por esta ordem:
  1. A lista de tokens, um por linha, no formato `<linha, TIPO, atributo>`.
  2. A tabela de símbolos final.
  3. A lista de erros léxicos (linha, coluna, mensagem) — ou “Sem erros léxicos.”.

---

## 6. ESPECIFICAÇÃO DO FICHEIRO `flex/MaculuScript.flex`

Gere o ficheiro completo seguindo estas regras.

### 6.1 Secção de opções (directivas jFlex)
- `%class AnaLex`
- `%public`
- `%unicode`
- `%line`  → activa `yyline` (0-based; reportar sempre `yyline + 1`)
- `%column` → activa `yycolumn` (0-based; reportar `yycolumn + 1`)
- `%type Token`
- `%function proximoToken`
- `%eofval{ return new Token(yyline + 1, TipoToken.EOF, null); %eofval}`
- **NÃO** usar `%caseless`. A insensibilidade a maiúsculas trata-se na acção do
  identificador, fazendo `yytext().toLowerCase()` antes de consultar a tabela. Assim
  os literais de texto mantêm a capitalização original.

### 6.2 Membros da classe (bloco `%{ ... %}`)
Declarar e inicializar:
- `private TabelaPalavrasReservadas reservadas;`
- `private TabelaSimbolos simbolos;`
- `private java.util.List<ErroLexico> erros = new java.util.ArrayList<>();`
- `private StringBuilder buffer = new StringBuilder();` (para acumular texto/char)
- `private int inicioLinha, inicioColuna;` (guardar início de literal/comentário)
- Construtor adicional ou *setters* para injectar `reservadas` e `simbolos` vindos
  da fachada (para serem **partilhados**).
- `public java.util.List<ErroLexico> getErros() { return erros; }`
- Método auxiliar `private void registarErro(String msg, String lexema)` que faz
  `erros.add(new ErroLexico(yyline+1, yycolumn+1, lexema, msg));`

### 6.3 Macros (definições)
```
DIGITO       = [0-9]
LETRA        = [a-zA-Z]
IDENT        = {LETRA}({LETRA}|{DIGITO}|_)*
INTEIRO      = {DIGITO}+
REAL         = {DIGITO}+ "." {DIGITO}+
ESPACO       = [ \t\f\r\n]+
FIM_LINHA    = \r|\n|\r\n
COMENT_LINHA = "//" [^\r\n]*
```

### 6.4 Estados léxicos
Declarar estados para tratar correctamente literais e comentários de bloco
(incluindo os casos “não terminado”):
```
%state TEXTO
%state CARACTER
%state COMENT_BLOCO
```

### 6.5 Regras (na ordem indicada)
Ordem importa apenas para empates; o jFlex usa *maximal munch* (a maior
correspondência ganha). Siga esta ordem por clareza:

**Estado `YYINITIAL`:**

1. `{ESPACO}` → ignorar.
2. `{COMENT_LINHA}` → ignorar (comentário de linha).
3. `"/*"` → guardar `inicioLinha/inicioColuna`, `yybegin(COMENT_BLOCO)`.
4. `\"` → `buffer.setLength(0)`, guardar início, `yybegin(TEXTO)`.
5. `\'` → `buffer.setLength(0)`, guardar início, `yybegin(CARACTER)`.
6. `{REAL}` → `return new Token(yyline+1, TipoToken.NUM_REAL, yytext());`
7. `{INTEIRO}` → `return new Token(yyline+1, TipoToken.NUM_INTEIRO, yytext());`
8. `{IDENT}` → **regra central** (ver 6.6 abaixo).
9. Operadores de **vários** caracteres ANTES dos de um caractere (boa prática,
   embora o *maximal munch* já resolva): `"=="` `"!="` `">="` `"<="` `"++"` `"--"`
   `"+="` `"-="` `"*="` `"/="`.
10. Operadores de um caractere: `"="` `"+"` `"-"` `"*"` `"/"` `"%"` `">"` `"<"`.
11. Pontuação: `";"` `","` `"."` `":"` `"("` `")"` `"{"` `"}"` `"["` `"]"`.
    (mapear cada um ao token correcto da secção 8).
12. Catch-all de erro: `.` → `registarErro("Caractere inválido", yytext());`
    (não devolve token; salta e continua).

**Estado `COMENT_BLOCO`:**
- `"*/"` → `yybegin(YYINITIAL);`
- `[^]` (qualquer caractere, incluindo `\n`) → consumir (o `yyline` actualiza-se
  automaticamente nas mudanças de linha).
- `<<EOF>>` → registar erro “Comentário de bloco não terminado” (usando a linha
  guardada) e `yybegin(YYINITIAL)` antes do EOF normal.

**Estado `TEXTO`:**
- `\"` → fim do texto: `yybegin(YYINITIAL); return new Token(inicioLinha, TipoToken.LITERAL_TEXTO, buffer.toString());`
- `\\n` → `buffer.append('\n');`  | `\\t` → tab | `\\\"` → `"` | `\\\'` → `'` |
  `\\\\` → `\` (tratar os escapes suportados).
- `{FIM_LINHA}` → erro “Texto (string) não terminado”; `yybegin(YYINITIAL);`
- `<<EOF>>` → erro “Texto não terminado no fim do ficheiro”.
- `[^\"\\\r\n]+` → `buffer.append(yytext());` (qualquer outro caractere normal).

**Estado `CARACTER`:** análogo a `TEXTO`, mas:
- deve conter **exactamente um** caractere (ou um escape). Se vier `\'` de fecho
  imediato (vazio) ou mais do que um caractere antes do fecho → erro “Literal de
  caracter inválido”.
- ao fechar: `return new Token(inicioLinha, TipoToken.LITERAL_CHAR, buffer.toString());`

### 6.6 Regra central do identificador (cumpre requisitos 5–8)
```java
{IDENT} {
    String lexema = yytext().toLowerCase();          // requisito 8 (case-insensitive)
    if (reservadas.ehReservada(lexema)) {            // requisito 5/6
        return new Token(yyline + 1, reservadas.tokenDe(lexema), null);
    }
    if (!simbolos.contem(lexema)) {                  // requisito 7
        simbolos.inserir(lexema, yyline + 1);
    }
    return new Token(yyline + 1, TipoToken.IDENTIFICADOR, lexema);
}
```
> Use UMA só regra para identificadores + consulta às tabelas. **Não** crie 35
> regras (uma por palavra-chave). É o que o docente pede e evita erros.

---

## 7. TABELA DE PALAVRAS RESERVADAS (usar exactamente isto)

| MaculuScript | TipoToken | Equivalente Java |
|---|---|---|
| inteiro | TIPO_INTEIRO | int |
| real | TIPO_REAL | double |
| caracter | TIPO_CARACTER | char |
| texto | TIPO_TEXTO | String |
| logico | TIPO_LOGICO | boolean |
| vazio | TIPO_VAZIO | void |
| vector | TIPO_VECTOR | (array `[]`) |
| se | SE | if |
| senao | SENAO | else |
| enquanto | ENQUANTO | while |
| para | PARA | for |
| faca | FACA | do |
| escolha | ESCOLHA | switch |
| caso | CASO | case |
| padrao | PADRAO | default |
| pare | PARE | break |
| continue | CONTINUE | continue |
| retorna | RETORNA | return |
| verdadeiro | VERDADEIRO | true |
| falso | FALSO | false |
| e | E_LOGICO | && |
| ou | OU_LOGICO | \|\| |
| nao | NAO_LOGICO | ! |
| classe | CLASSE | class |
| novo | NOVO | new |
| nulo | NULO | null |
| este | ESTE | this |
| publico | PUBLICO | public |
| privado | PRIVADO | private |
| estatico | ESTATICO | static |
| tente | TENTE | try |
| capture | CAPTURE | catch |
| finalmente | FINALMENTE | finally |
| lance | LANCE | throw |
| escreva | ESCREVA | System.out.println |
| leia | LEIA | Scanner |

---

## 8. TABELA COMPLETA DE TOKENS

**Tipos de dado / controlo / OO / excepções / I/O:** todos os `TipoToken` da
tabela da secção 7 (TIPO_INTEIRO, …, LEIA).

**Operadores:**

| Token | Símbolo |
|---|---|
| ATRIBUICAO | `=` |
| SOMA | `+` |
| SUBTRACAO | `-` |
| MULTIPLICACAO | `*` |
| DIVISAO | `/` |
| MODULO | `%` |
| INCREMENTO | `++` |
| DECREMENTO | `--` |
| SOMA_ATRIB | `+=` |
| SUB_ATRIB | `-=` |
| MULT_ATRIB | `*=` |
| DIV_ATRIB | `/=` |
| IGUAL | `==` |
| DIFERENTE | `!=` |
| MAIOR | `>` |
| MENOR | `<` |
| MAIOR_IGUAL | `>=` |
| MENOR_IGUAL | `<=` |

> Os operadores lógicos `e`, `ou`, `nao` são **palavras** (vêm da tabela de
> reservadas → E_LOGICO, OU_LOGICO, NAO_LOGICO). **Um `!` isolado é erro léxico**
> (a negação escreve-se `nao`); `!` só é válido dentro de `!=`.

**Pontuação:**

| Token | Símbolo |
|---|---|
| PONTO_VIRGULA | `;` |
| VIRGULA | `,` |
| PONTO | `.` |
| DOIS_PONTOS | `:` |
| ABRE_PAR | `(` |
| FECHA_PAR | `)` |
| ABRE_CHAVE | `{` |
| FECHA_CHAVE | `}` |
| ABRE_COLCHETE | `[` |
| FECHA_COLCHETE | `]` |

**Literais e identificadores (têm atributo):**

| Token | Padrão | Atributo |
|---|---|---|
| IDENTIFICADOR | `[a-zA-Z][a-zA-Z0-9_]*` | o nome (em minúsculas) |
| NUM_INTEIRO | `[0-9]+` | o valor |
| NUM_REAL | `[0-9]+\.[0-9]+` | o valor |
| LITERAL_TEXTO | `"(\.|[^"\\])*"` (com escapes) | o texto sem aspas |
| LITERAL_CHAR | `'(\.|[^'\\])'` (com escapes) | o caractere |

**Especiais:** `EOF` (fim de ficheiro, sem atributo).

---

## 9. TRATAMENTO E REPORTE DE ERROS

Tipos de erro léxico a detectar (todos com **linha e coluna**, recolhidos numa
lista, **sem abortar** a análise):

1. **Caractere inválido** — qualquer símbolo fora do alfabeto da linguagem
   (ex.: `@`, `#`, `~`, `!` isolado). Mensagem: `Caractere inválido: '<x>'`.
2. **Texto (string) não terminado** — abre `"` e chega ao fim da linha ou do
   ficheiro sem fechar.
3. **Caracter não terminado / inválido** — `'` sem fecho, ou com mais de um
   caractere.
4. **Comentário de bloco não terminado** — `/*` sem `*/` até ao fim do ficheiro.

A recuperação é em **modo pânico**: regista o erro, salta o que causou o problema e
continua para listar todos os erros possíveis (essencial para o site mostrar tudo).

---

## 10. FORMATO DE SAÍDA (Main)

```
=== TOKENS ===
<1, CLASSE, ->
<1, IDENTIFICADOR, "principal">
<1, ABRE_CHAVE, ->
<2, TIPO_INTEIRO, ->
<2, IDENTIFICADOR, "idade">
<2, ATRIBUICAO, ->
<2, NUM_INTEIRO, "25">
<2, PONTO_VIRGULA, ->
...

=== TABELA DE SÍMBOLOS ===
principal  (1ª linha: 1)
idade      (1ª linha: 2)
...

=== ERROS LÉXICOS ===
Linha 7, Coluna 12: Caractere inválido: '@'
Linha 9, Coluna 5: Texto (string) não terminado
(ou)  Sem erros léxicos.
```

---

## 11. FICHEIROS DE TESTE A CRIAR (pasta `testes/`)

Crie os ficheiros abaixo, **de tamanho crescente** (como pede o docente), com
conteúdo MaculuScript real. Conteúdo sugerido:

- `valido_01_ola.maculu` — classe mínima com `escreva("Ola, MaculuScript!");`.
- `valido_02_variaveis.maculu` — declarações dos vários tipos + escapes (`\n`).
- `valido_03_controlo.maculu` — `se/senao`, `enquanto`, `para`, `escolha/caso`.
- `valido_04_funcoes.maculu` — funções com e sem retorno, chamada de função.
- `valido_05_classe.maculu` — classe com atributos privados, construtor, métodos,
  `novo`, `este`.
- `valido_06_completo.maculu` — programa grande que junta vectores, classe,
  excepções (`tente/capture/finalmente/lance`), I/O, todos os operadores.
- `erro_01_caractere_invalido.maculu` — contém `@`, `#`.
- `erro_02_texto_nao_terminado.maculu` — `texto t = "sem fecho;`.
- `erro_03_comentario_nao_terminado.maculu` — `/*` sem `*/`.
- `erro_04_misturado.maculu` — vários erros + código válido à mistura, para provar a
  recuperação (deve listar todos os erros e continuar a produzir tokens).

> Use apenas palavras reservadas e construções da secção 7/8. Não use acentos em
> identificadores. Lembre-se: `e` é reservada — não a use como nome de variável.

---

## 12. PREPARAÇÃO PARA A API / SITE (futuro)

- `AnalisadorLexico.analisar(String codigo)` é o ponto de entrada da futura API:
  recebe o código colado pelo utilizador e devolve `ResultadoAnalise`
  (tokens + erros + tabela de símbolos), tudo serializável.
- Não use `System.out` dentro de `AnaLex` nem de `AnalisadorLexico` (só no `Main`),
  para que a mesma lógica sirva CLI e servidor.
- Garanta que `Token`, `ErroLexico` e `Simbolo` têm getters simples (fáceis de
  converter para JSON quando se montar o endpoint REST).
- Cada análise deve criar **novas instâncias** das tabelas (sem estado global
  partilhado entre pedidos), para o servidor ser seguro com vários utilizadores.

---

## 13. BUILD E EXECUÇÃO

Crie um `build.sh` (e descreva no `README.md`) com passos equivalentes a:

```bash
# 1. Gerar AnaLex.java a partir da especificação jFlex
java -jar lib/jflex-full-1.9.1.jar -d src/maculu flex/MaculuScript.flex

# 2. Compilar
javac -d out src/maculu/*.java

# 3. Correr um teste
java -cp out maculu.Main testes/valido_06_completo.maculu
```

(Indique a versão do jFlex usada e onde a obter: https://www.jflex.de/ .)

---

## 14. CHECKLIST DE ACEITAÇÃO (a IA deve garantir tudo)

- [ ] Classe `AnaLex` gerada pelo jFlex, com `proximoToken()` que devolve um `Token`
      por chamada e não imprime nada.
- [ ] Numeração de linhas correcta, **inclusive dentro de comentários de bloco**.
- [ ] Comentários de linha e de bloco são ignorados.
- [ ] Formato de saída `<linha, TOKEN, atributo>` exactamente como na secção 10.
- [ ] Tabela de palavras reservadas (hash) com mapeamento para Java.
- [ ] Tabela de símbolos (hash); identificadores inseridos só uma vez.
- [ ] Case-insensitive via `toLowerCase()` antes de comparar.
- [ ] Literais de texto e caracter com os escapes `\n \t \" \' \\`.
- [ ] Erros léxicos com linha+coluna, recuperação em modo pânico (lista todos).
- [ ] Pasta `testes/` com os ficheiros válidos (crescentes) e de erro.
- [ ] `AnalisadorLexico.analisar(String) -> ResultadoAnalise` pronto para a API.
- [ ] `build.sh` + `README.md` com instruções de geração, compilação e execução.
- [ ] Tudo compila e corre sem erros.

> Entregue **todos os ficheiros completos** (sem reticências “…” no código),
> incluindo o `.flex` inteiro, as classes Java inteiras e os programas de teste.
