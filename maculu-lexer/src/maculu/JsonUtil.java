package maculu;

/**
 * Serializador JSON minimalista e SEM dependências externas.
 *
 * Converte um {@link ResultadoAnalise} (e a tabela de reservadas) para JSON,
 * para ser devolvido pela API web ({@link ServidorApi}). Não usa nenhuma
 * biblioteca: apenas constrói a string com o escape correcto.
 *
 * Esta classe é exclusiva da camada web — o núcleo do analisador não depende
 * dela.
 */
public final class JsonUtil {

    private JsonUtil() {
        // utilitário estático
    }

    /** Faz escape de uma string para a colocar entre aspas num JSON. */
    public static String escape(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder b = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  b.append("\\\""); break;
                case '\\': b.append("\\\\"); break;
                case '\n': b.append("\\n");  break;
                case '\r': b.append("\\r");  break;
                case '\t': b.append("\\t");  break;
                case '\b': b.append("\\b");  break;
                case '\f': b.append("\\f");  break;
                default:
                    if (c < 0x20) {
                        b.append(String.format("\\u%04x", (int) c));
                    } else {
                        b.append(c);
                    }
            }
        }
        return b.toString();
    }

    /** @return a string entre aspas, ou {@code null} (literal JSON) se for null. */
    private static String quote(String s) {
        return (s == null) ? "null" : "\"" + escape(s) + "\"";
    }

    /**
     * Serializa o resultado completo da análise:
     * { temErros, tokens[], erros[], tabelaSimbolos[] }.
     */
    public static String resultadoJson(ResultadoAnalise r) {
        StringBuilder b = new StringBuilder(1024);
        b.append('{');
        b.append("\"temErros\":").append(r.temErros()).append(',');

        b.append("\"tokens\":[");
        boolean primeiro = true;
        for (Token t : r.getTokens()) {
            if (!primeiro) b.append(',');
            primeiro = false;
            b.append("{\"linha\":").append(t.getLinha())
             .append(",\"tipo\":\"").append(t.getTipo().name()).append('"')
             .append(",\"atributo\":").append(quote(t.getAtributo()))
             .append('}');
        }
        b.append("],");

        b.append("\"erros\":[");
        primeiro = true;
        for (ErroLexico e : r.getErros()) {
            if (!primeiro) b.append(',');
            primeiro = false;
            b.append("{\"linha\":").append(e.getLinha())
             .append(",\"coluna\":").append(e.getColuna())
             .append(",\"lexema\":").append(quote(e.getLexema()))
             .append(",\"mensagem\":").append(quote(e.getMensagem()))
             .append('}');
        }
        b.append("],");

        b.append("\"tabelaSimbolos\":[");
        primeiro = true;
        for (Simbolo s : r.getTabelaSimbolos()) {
            if (!primeiro) b.append(',');
            primeiro = false;
            b.append("{\"nome\":").append(quote(s.getNome()))
             .append(",\"primeiraLinha\":").append(s.getPrimeiraLinha())
             .append('}');
        }
        b.append(']');

        b.append('}');
        return b.toString();
    }

    /**
     * Serializa a tabela de palavras reservadas:
     * [ { palavra, tipo, java } ... ].
     */
    public static String reservadasJson(TabelaPalavrasReservadas tab) {
        StringBuilder b = new StringBuilder(2048);
        b.append('[');
        boolean primeiro = true;
        for (String palavra : tab.palavrasReservadas()) {
            if (!primeiro) b.append(',');
            primeiro = false;
            b.append("{\"palavra\":").append(quote(palavra))
             .append(",\"tipo\":\"").append(tab.tokenDe(palavra).name()).append('"')
             .append(",\"java\":").append(quote(tab.equivalenteJava(palavra)))
             .append('}');
        }
        b.append(']');
        return b.toString();
    }
}
