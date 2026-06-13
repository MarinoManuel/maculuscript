package maculu;

import java.util.HashMap;
import java.util.Map;

/**
 * Tabela de palavras reservadas da MaculuScript (implementada como tabela hash).
 *
 * Cumpre os requisitos 5 e 6 do docente:
 *   - guarda o mapeamento palavra reservada -> {@link TipoToken};
 *   - guarda também o mapeamento palavra reservada -> equivalente em Java;
 *   - todas as comparações são feitas em minúsculas (linguagem case-insensitive,
 *     requisito 8).
 *
 * As 36 entradas correspondem EXACTAMENTE à tabela da secção 7 da especificação.
 */
public class TabelaPalavrasReservadas {

    private final Map<String, TipoToken> tokens = new HashMap<>();
    private final Map<String, String> equivalentesJava = new HashMap<>();

    public TabelaPalavrasReservadas() {
        // ----- Tipos de dado -----
        registar("inteiro",   TipoToken.TIPO_INTEIRO,  "int");
        registar("real",      TipoToken.TIPO_REAL,     "double");
        registar("caracter",  TipoToken.TIPO_CARACTER, "char");
        registar("texto",     TipoToken.TIPO_TEXTO,    "String");
        registar("logico",    TipoToken.TIPO_LOGICO,   "boolean");
        registar("vazio",     TipoToken.TIPO_VAZIO,    "void");
        registar("vector",    TipoToken.TIPO_VECTOR,   "[]");

        // ----- Controlo de fluxo -----
        registar("se",        TipoToken.SE,        "if");
        registar("senao",     TipoToken.SENAO,     "else");
        registar("enquanto",  TipoToken.ENQUANTO,  "while");
        registar("para",      TipoToken.PARA,      "for");
        registar("faca",      TipoToken.FACA,      "do");
        registar("escolha",   TipoToken.ESCOLHA,   "switch");
        registar("caso",      TipoToken.CASO,      "case");
        registar("padrao",    TipoToken.PADRAO,    "default");
        registar("pare",      TipoToken.PARE,      "break");
        registar("continue",  TipoToken.CONTINUE,  "continue");
        registar("retorna",   TipoToken.RETORNA,   "return");

        // ----- Valores lógicos -----
        registar("verdadeiro", TipoToken.VERDADEIRO, "true");
        registar("falso",      TipoToken.FALSO,      "false");

        // ----- Operadores lógicos (são palavras, não símbolos) -----
        registar("e",   TipoToken.E_LOGICO,   "&&");
        registar("ou",  TipoToken.OU_LOGICO,  "||");
        registar("nao", TipoToken.NAO_LOGICO, "!");

        // ----- Orientação a objectos -----
        registar("classe", TipoToken.CLASSE, "class");
        registar("novo",   TipoToken.NOVO,   "new");
        registar("nulo",   TipoToken.NULO,   "null");
        registar("este",   TipoToken.ESTE,   "this");

        // ----- Modificadores -----
        registar("publico",  TipoToken.PUBLICO,  "public");
        registar("privado",  TipoToken.PRIVADO,  "private");
        registar("estatico", TipoToken.ESTATICO, "static");

        // ----- Excepções -----
        registar("tente",      TipoToken.TENTE,      "try");
        registar("capture",    TipoToken.CAPTURE,    "catch");
        registar("finalmente", TipoToken.FINALMENTE, "finally");
        registar("lance",      TipoToken.LANCE,      "throw");

        // ----- Entrada / saída -----
        registar("escreva", TipoToken.ESCREVA, "System.out.println");
        registar("leia",    TipoToken.LEIA,    "Scanner");
    }

    private void registar(String palavra, TipoToken tipo, String equivalenteJava) {
        String chave = palavra.toLowerCase();
        tokens.put(chave, tipo);
        equivalentesJava.put(chave, equivalenteJava);
    }

    /** @return {@code true} se a palavra (em qualquer caixa) é reservada. */
    public boolean ehReservada(String palavra) {
        return tokens.containsKey(palavra.toLowerCase());
    }

    /** @return o {@link TipoToken} da palavra reservada, ou {@code null} se não existir. */
    public TipoToken tokenDe(String palavra) {
        return tokens.get(palavra.toLowerCase());
    }

    /** @return o equivalente em Java da palavra reservada, ou {@code null} se não existir. */
    public String equivalenteJava(String palavra) {
        return equivalentesJava.get(palavra.toLowerCase());
    }

    /** @return número de palavras reservadas registadas (deve ser 36). */
    public int tamanho() {
        return tokens.size();
    }

    /**
     * @return as palavras reservadas por ordem alfabética, para listar o
     *         mapeamento (req. 5 do docente: "tabela que mostra o mapeamento").
     */
    public java.util.List<String> palavrasReservadas() {
        java.util.List<String> lista = new java.util.ArrayList<>(tokens.keySet());
        java.util.Collections.sort(lista);
        return lista;
    }
}
