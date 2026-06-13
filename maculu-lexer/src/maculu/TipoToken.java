package maculu;

/**
 * Enumeração de TODOS os tipos de token da linguagem MaculuScript.
 *
 * Os tipos estão organizados por categoria, seguindo exactamente as tabelas das
 * secções 7 e 8 da especificação:
 *   - Palavras reservadas (tipos de dado, controlo de fluxo, valores lógicos,
 *     operadores lógicos, OO, modificadores, excepções, I/O) -> 36 tipos;
 *   - Operadores -> 18 tipos;
 *   - Pontuação -> 10 tipos;
 *   - Literais + IDENTIFICADOR -> 5 tipos;
 *   - Especiais (EOF) -> 1 tipo.
 *
 * Os operadores lógicos "e", "ou" e "nao" são PALAVRAS reservadas: por isso os
 * seus tipos (E_LOGICO, OU_LOGICO, NAO_LOGICO) vivem aqui, mas não existem como
 * regras de símbolo no analisador (vêm da tabela de palavras reservadas).
 */
public enum TipoToken {

    // ----- Tipos de dado (palavras reservadas) -----
    TIPO_INTEIRO,
    TIPO_REAL,
    TIPO_CARACTER,
    TIPO_TEXTO,
    TIPO_LOGICO,
    TIPO_VAZIO,
    TIPO_VECTOR,

    // ----- Controlo de fluxo (palavras reservadas) -----
    SE,
    SENAO,
    ENQUANTO,
    PARA,
    FACA,
    ESCOLHA,
    CASO,
    PADRAO,
    PARE,
    CONTINUE,
    RETORNA,

    // ----- Valores lógicos (palavras reservadas) -----
    VERDADEIRO,
    FALSO,

    // ----- Operadores lógicos (palavras reservadas) -----
    E_LOGICO,
    OU_LOGICO,
    NAO_LOGICO,

    // ----- Orientação a objectos (palavras reservadas) -----
    CLASSE,
    NOVO,
    NULO,
    ESTE,

    // ----- Modificadores (palavras reservadas) -----
    PUBLICO,
    PRIVADO,
    ESTATICO,

    // ----- Excepções (palavras reservadas) -----
    TENTE,
    CAPTURE,
    FINALMENTE,
    LANCE,

    // ----- Entrada / saída (palavras reservadas) -----
    ESCREVA,
    LEIA,

    // ----- Operadores -----
    ATRIBUICAO,      // =
    SOMA,            // +
    SUBTRACAO,       // -
    MULTIPLICACAO,   // *
    DIVISAO,         // /
    MODULO,          // %
    INCREMENTO,      // ++
    DECREMENTO,      // --
    SOMA_ATRIB,      // +=
    SUB_ATRIB,       // -=
    MULT_ATRIB,      // *=
    DIV_ATRIB,       // /=
    IGUAL,           // ==
    DIFERENTE,       // !=
    MAIOR,           // >
    MENOR,           // <
    MAIOR_IGUAL,     // >=
    MENOR_IGUAL,     // <=

    // ----- Pontuação -----
    PONTO_VIRGULA,   // ;
    VIRGULA,         // ,
    PONTO,           // .
    DOIS_PONTOS,     // :
    ABRE_PAR,        // (
    FECHA_PAR,       // )
    ABRE_CHAVE,      // {
    FECHA_CHAVE,     // }
    ABRE_COLCHETE,   // [
    FECHA_COLCHETE,  // ]

    // ----- Literais e identificadores (têm atributo) -----
    IDENTIFICADOR,   // [a-zA-Z][a-zA-Z0-9_]*  (atributo: nome em minúsculas)
    NUM_INTEIRO,     // [0-9]+
    NUM_REAL,        // [0-9]+.[0-9]+
    LITERAL_TEXTO,   // "..." (com escapes)
    LITERAL_CHAR,    // '...' (com escapes)

    // ----- Especiais -----
    EOF              // fim de ficheiro (sem atributo)
}
