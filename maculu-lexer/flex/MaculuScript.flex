/*
 * ============================================================================
 *  MaculuScript.flex  —  Especificação jFlex do Analisador Léxico da
 *  linguagem MaculuScript.
 *
 *  Gera a classe maculu.AnaLex com o método produtor de tokens
 *  proximoToken(), que devolve UM Token por chamada (e EOF no fim) e NUNCA
 *  escreve no ecrã (requisito 4 do docente).
 *
 *  Comandar a geração:
 *    java -jar lib/jflex-full-1.9.1.jar -d src/maculu flex/MaculuScript.flex
 * ============================================================================
 */

/* ---------- Secção de código do utilizador (copiada para o topo do .java) ---------- */
package maculu;

%%

/* =====================  OPÇÕES / DIRECTIVAS  ===================== */

%public
%class AnaLex
%unicode
%line                 /* activa yyline (0-based) -> reportamos yyline + 1 */
%column               /* activa yycolumn (0-based) -> reportamos yycolumn + 1 */
%type Token
%function proximoToken

/*
 * EOF: devolve sempre um Token EOF. Antes, se o ficheiro acabou DENTRO de um
 * comentário de bloco ou de um literal de texto/caracter, regista o erro de
 * "não terminado" (recuperação em modo pânico). Verificamos yystate() em vez de
 * usar regras <<EOF>> por estado, para não misturar mecanismos de EOF do jFlex.
 */
%eofval{
  switch (yystate()) {
      case COMENT_BLOCO:
          registarErroEm(inicioLinha, inicioColuna,
                         "Comentário de bloco não terminado", "/*");
          yybegin(YYINITIAL);
          break;
      case TEXTO:
          registarErroEm(inicioLinha, inicioColuna,
                         "Texto (string) não terminado", buffer.toString());
          yybegin(YYINITIAL);
          break;
      case CARACTER:
          registarErroEm(inicioLinha, inicioColuna,
                         "Caracter não terminado", buffer.toString());
          yybegin(YYINITIAL);
          break;
      default:
          break;
  }
  return new Token(yyline + 1, TipoToken.EOF, null);
%eofval}

/* =====================  MEMBROS DA CLASSE  ===================== */

%{
    /* Tabelas PARTILHADAS, injectadas pela fachada AnalisadorLexico. */
    private TabelaPalavrasReservadas reservadas;
    private TabelaSimbolos simbolos;

    /* Erros léxicos recolhidos (modo pânico: nunca aborta). */
    private java.util.List<ErroLexico> erros = new java.util.ArrayList<>();

    /* Acumulador de texto/caracter e início do literal/comentário. */
    private StringBuilder buffer = new StringBuilder();
    private int inicioLinha, inicioColuna;

    /** Injecta as tabelas partilhadas vindas da fachada. */
    public void setTabelas(TabelaPalavrasReservadas reservadas, TabelaSimbolos simbolos) {
        this.reservadas = reservadas;
        this.simbolos = simbolos;
    }

    /** Lista de erros recolhidos durante a análise. */
    public java.util.List<ErroLexico> getErros() {
        return erros;
    }

    /**
     * Alias do método produtor de tokens com o nome usado no enunciado do
     * docente ("método AnaLex"). Delega em proximoToken(): devolve um Token a
     * cada chamada e não escreve nada no ecrã (requisitos 1 e 4).
     */
    public Token analex() throws java.io.IOException {
        return proximoToken();
    }

    /** Regista um erro na posição CORRENTE (yyline/yycolumn). */
    private void registarErro(String msg, String lexema) {
        erros.add(new ErroLexico(yyline + 1, yycolumn + 1, lexema, msg));
    }

    /** Regista um erro numa posição EXPLÍCITA (ex.: início de um literal). */
    private void registarErroEm(int linha, int coluna, String msg, String lexema) {
        erros.add(new ErroLexico(linha, coluna, lexema, msg));
    }

    /** Guarda a posição (1-based) de início do literal/comentário actual. */
    private void marcarInicio() {
        inicioLinha = yyline + 1;
        inicioColuna = yycolumn + 1;
    }
%}

/* =====================  MACROS  ===================== */

DIGITO       = [0-9]
LETRA        = [a-zA-Z]
IDENT        = {LETRA}({LETRA}|{DIGITO}|_)*
INTEIRO      = {DIGITO}+
REAL         = {DIGITO}+ "." {DIGITO}+
ESPACO       = [ \t\f\r\n]+
FIM_LINHA    = \r|\n|\r\n
COMENT_LINHA = "//" [^\r\n]*

/* =====================  ESTADOS LÉXICOS  ===================== */

%state TEXTO
%state CARACTER
%state COMENT_BLOCO

%%

/* =====================  REGRAS  ===================== */

/* ---------- Estado inicial ---------- */
<YYINITIAL> {

    /* Espaços e comentários: ignorados (o yyline actualiza-se sozinho). */
    {ESPACO}        { /* ignorar */ }
    {COMENT_LINHA}  { /* ignorar comentário de linha */ }
    "/*"            { marcarInicio(); yybegin(COMENT_BLOCO); }

    /* Início de literais: limpa buffer, guarda início e muda de estado. */
    \"              { buffer.setLength(0); marcarInicio(); yybegin(TEXTO); }
    \'              { buffer.setLength(0); marcarInicio(); yybegin(CARACTER); }

    /* Números (REAL antes de INTEIRO; o maximal munch também já o garante). */
    {REAL}          { return new Token(yyline + 1, TipoToken.NUM_REAL, yytext()); }
    {INTEIRO}       { return new Token(yyline + 1, TipoToken.NUM_INTEIRO, yytext()); }

    /* Identificador / palavra reservada — regra central (requisitos 5-8). */
    {IDENT} {
        String lexema = yytext().toLowerCase();          /* case-insensitive (req. 8) */
        if (reservadas.ehReservada(lexema)) {            /* palavras reservadas (req. 5/6) */
            return new Token(yyline + 1, reservadas.tokenDe(lexema), null);
        }
        if (!simbolos.contem(lexema)) {                  /* tabela de símbolos (req. 7) */
            simbolos.inserir(lexema, yyline + 1);
        }
        return new Token(yyline + 1, TipoToken.IDENTIFICADOR, lexema);
    }

    /* Operadores de vários caracteres (antes dos de um caractere). */
    "=="            { return new Token(yyline + 1, TipoToken.IGUAL, null); }
    "!="            { return new Token(yyline + 1, TipoToken.DIFERENTE, null); }
    ">="            { return new Token(yyline + 1, TipoToken.MAIOR_IGUAL, null); }
    "<="            { return new Token(yyline + 1, TipoToken.MENOR_IGUAL, null); }
    "++"            { return new Token(yyline + 1, TipoToken.INCREMENTO, null); }
    "--"            { return new Token(yyline + 1, TipoToken.DECREMENTO, null); }
    "+="            { return new Token(yyline + 1, TipoToken.SOMA_ATRIB, null); }
    "-="            { return new Token(yyline + 1, TipoToken.SUB_ATRIB, null); }
    "*="            { return new Token(yyline + 1, TipoToken.MULT_ATRIB, null); }
    "/="            { return new Token(yyline + 1, TipoToken.DIV_ATRIB, null); }

    /* Operadores de um caractere. */
    "="             { return new Token(yyline + 1, TipoToken.ATRIBUICAO, null); }
    "+"             { return new Token(yyline + 1, TipoToken.SOMA, null); }
    "-"             { return new Token(yyline + 1, TipoToken.SUBTRACAO, null); }
    "*"             { return new Token(yyline + 1, TipoToken.MULTIPLICACAO, null); }
    "/"             { return new Token(yyline + 1, TipoToken.DIVISAO, null); }
    "%"             { return new Token(yyline + 1, TipoToken.MODULO, null); }
    ">"             { return new Token(yyline + 1, TipoToken.MAIOR, null); }
    "<"             { return new Token(yyline + 1, TipoToken.MENOR, null); }

    /* Pontuação. */
    ";"             { return new Token(yyline + 1, TipoToken.PONTO_VIRGULA, null); }
    ","             { return new Token(yyline + 1, TipoToken.VIRGULA, null); }
    "."             { return new Token(yyline + 1, TipoToken.PONTO, null); }
    ":"             { return new Token(yyline + 1, TipoToken.DOIS_PONTOS, null); }
    "("             { return new Token(yyline + 1, TipoToken.ABRE_PAR, null); }
    ")"             { return new Token(yyline + 1, TipoToken.FECHA_PAR, null); }
    "{"             { return new Token(yyline + 1, TipoToken.ABRE_CHAVE, null); }
    "}"             { return new Token(yyline + 1, TipoToken.FECHA_CHAVE, null); }
    "["             { return new Token(yyline + 1, TipoToken.ABRE_COLCHETE, null); }
    "]"             { return new Token(yyline + 1, TipoToken.FECHA_COLCHETE, null); }

    /* Catch-all: qualquer outro caractere é inválido (inclui '!' isolado, '@', '#', '~'...). */
    .               { registarErro("Caractere inválido: '" + yytext() + "'", yytext()); }
}

/* ---------- Comentário de bloco ---------- */
<COMENT_BLOCO> {
    "*/"            { yybegin(YYINITIAL); }
    [^]             { /* consome qualquer caractere, incl. quebras de linha */ }
}

/* ---------- Literal de texto ("...") ---------- */
<TEXTO> {
    \"              { yybegin(YYINITIAL);
                      return new Token(inicioLinha, TipoToken.LITERAL_TEXTO, buffer.toString()); }

    \\n             { buffer.append('\n'); }
    \\t             { buffer.append('\t'); }
    \\\"            { buffer.append('"');  }
    \\\'            { buffer.append('\''); }
    \\\\            { buffer.append('\\'); }
    \\              { registarErro("Sequência de escape inválida em texto", yytext()); }

    {FIM_LINHA}     { registarErroEm(inicioLinha, inicioColuna,
                                     "Texto (string) não terminado", buffer.toString());
                      yybegin(YYINITIAL); }

    [^\"\\\r\n]+    { buffer.append(yytext()); }
}

/* ---------- Literal de caracter ('...') ---------- */
<CARACTER> {
    \'              { yybegin(YYINITIAL);
                      if (buffer.length() == 1) {
                          return new Token(inicioLinha, TipoToken.LITERAL_CHAR, buffer.toString());
                      } else {
                          registarErroEm(inicioLinha, inicioColuna,
                                         "Caracter inválido (deve conter exactamente um caractere)",
                                         buffer.toString());
                          /* não devolve token: recupera e continua */
                      } }

    \\n             { buffer.append('\n'); }
    \\t             { buffer.append('\t'); }
    \\\"            { buffer.append('"');  }
    \\\'            { buffer.append('\''); }
    \\\\            { buffer.append('\\'); }
    \\              { registarErro("Sequência de escape inválida em caracter", yytext()); }

    {FIM_LINHA}     { registarErroEm(inicioLinha, inicioColuna,
                                     "Caracter não terminado", buffer.toString());
                      yybegin(YYINITIAL); }

    [^\'\\\r\n]+    { buffer.append(yytext()); }
}
