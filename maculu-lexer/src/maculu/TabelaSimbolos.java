package maculu;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tabela de símbolos da MaculuScript (implementada como tabela hash).
 *
 * Usa-se {@link LinkedHashMap} — que é uma tabela hash — para preservar a ordem
 * de aparição dos identificadores, tornando a impressão final estável e igual à
 * ordem em que surgem no código (ver secção 10).
 *
 * Cumpre o requisito 7: ao reconhecer um identificador, insere-se apenas se
 * ainda não existir; comparações sempre em minúsculas (requisito 8).
 */
public class TabelaSimbolos {

    private final Map<String, Simbolo> simbolos = new LinkedHashMap<>();

    /** @return {@code true} se o identificador já existe na tabela. */
    public boolean contem(String nome) {
        return simbolos.containsKey(nome.toLowerCase());
    }

    /**
     * Insere o identificador apenas se ainda não existir.
     *
     * @return o {@link Simbolo} existente (se já estava lá) ou o recém-criado.
     */
    public Simbolo inserir(String nome, int linha) {
        String chave = nome.toLowerCase();
        Simbolo existente = simbolos.get(chave);
        if (existente != null) {
            return existente;
        }
        Simbolo novo = new Simbolo(chave, linha);
        simbolos.put(chave, novo);
        return novo;
    }

    /** @return o {@link Simbolo} associado ao nome, ou {@code null} se não existir. */
    public Simbolo obter(String nome) {
        return simbolos.get(nome.toLowerCase());
    }

    /** @return todos os símbolos, na ordem de inserção (para impressão final). */
    public Collection<Simbolo> todos() {
        return simbolos.values();
    }

    /** @return número de identificadores distintos registados. */
    public int tamanho() {
        return simbolos.size();
    }
}
