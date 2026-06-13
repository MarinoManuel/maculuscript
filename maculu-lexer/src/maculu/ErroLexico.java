package maculu;

/**
 * Representa um erro léxico detectado durante a análise.
 *
 * Cada erro guarda a posição exacta (linha e coluna, 1-based), o {@code lexema}
 * que o causou (quando aplicável — útil para a futura API/destaque no site) e a
 * {@code mensagem} já formatada para apresentação.
 *
 * A análise nunca aborta no primeiro erro: todos os erros são recolhidos numa
 * lista (recuperação em modo pânico).
 */
public class ErroLexico {

    private final int linha;       // 1-based
    private final int coluna;      // 1-based
    private final String lexema;   // texto que causou o erro (pode ser null)
    private final String mensagem;

    public ErroLexico(int linha, int coluna, String lexema, String mensagem) {
        this.linha = linha;
        this.coluna = coluna;
        this.lexema = lexema;
        this.mensagem = mensagem;
    }

    public int getLinha() {
        return linha;
    }

    public int getColuna() {
        return coluna;
    }

    /** @return o lexema que causou o erro, ou {@code null} se não aplicável. */
    public String getLexema() {
        return lexema;
    }

    public String getMensagem() {
        return mensagem;
    }

    /**
     * Formato amigável para a saída de consola e para a futura API (secção 10):
     *   Linha &lt;linha&gt;, Coluna &lt;coluna&gt;: &lt;mensagem&gt;
     */
    @Override
    public String toString() {
        return "Linha " + linha + ", Coluna " + coluna + ": " + mensagem;
    }
}
