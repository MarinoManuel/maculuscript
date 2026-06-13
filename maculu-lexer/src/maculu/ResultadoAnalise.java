package maculu;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Objecto de retorno da análise léxica, pensado para a futura API/site.
 *
 * Agrega tudo o que o consumidor (CLI {@link Main} ou um endpoint REST) precisa:
 * a lista de {@link Token}s, a lista de {@link ErroLexico}s e a tabela de
 * símbolos. Todos os campos têm getters simples, fáceis de serializar para JSON.
 *
 * É imutável (as colecções são embrulhadas como não modificáveis) para ser
 * seguro de partilhar.
 */
public class ResultadoAnalise {

    private final List<Token> tokens;
    private final List<ErroLexico> erros;
    private final Collection<Simbolo> tabelaSimbolos;

    public ResultadoAnalise(List<Token> tokens,
                            List<ErroLexico> erros,
                            Collection<Simbolo> tabelaSimbolos) {
        this.tokens = Collections.unmodifiableList(tokens);
        this.erros = Collections.unmodifiableList(erros);
        this.tabelaSimbolos = Collections.unmodifiableCollection(tabelaSimbolos);
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public List<ErroLexico> getErros() {
        return erros;
    }

    public Collection<Simbolo> getTabelaSimbolos() {
        return tabelaSimbolos;
    }

    /** @return {@code true} se foram detectados erros léxicos. */
    public boolean temErros() {
        return !erros.isEmpty();
    }
}
