package maculu;

/**
 * Representa um token reconhecido pelo analisador léxico.
 *
 * Estrutura pedida pelo docente: &lt;Número da linha, Token, Atributo (quando
 * possuir)&gt;. O atributo é {@code null} quando o token não tem atributo
 * (palavras reservadas, operadores e pontuação) e não-nulo para
 * identificadores e literais.
 *
 * Imutável e com getters simples (fácil de serializar para JSON na futura API).
 */
public class Token {

    private final int linha;          // 1-based
    private final TipoToken tipo;
    private final String atributo;    // null quando o token não tem atributo

    public Token(int linha, TipoToken tipo, String atributo) {
        this.linha = linha;
        this.tipo = tipo;
        this.atributo = atributo;
    }

    public int getLinha() {
        return linha;
    }

    public TipoToken getTipo() {
        return tipo;
    }

    /** @return o atributo do token, ou {@code null} se não possuir. */
    public String getAtributo() {
        return atributo;
    }

    public boolean temAtributo() {
        return atributo != null;
    }

    /**
     * Formato exigido pelo docente (secção 10):
     *   com atributo:  &lt;linha, TIPO, "atributo"&gt;
     *   sem atributo:  &lt;linha, TIPO, -&gt;
     */
    @Override
    public String toString() {
        String attr = (atributo == null) ? "-" : "\"" + atributo + "\"";
        return "<" + linha + ", " + tipo + ", " + attr + ">";
    }
}
