package maculu;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Fachada / "API" do analisador léxico.
 *
 * Este é o ponto de entrada que a futura API REST (e o {@link Main}) chamam:
 * recebe o código-fonte em {@code String} e devolve um {@link ResultadoAnalise}
 * com tokens + erros + tabela de símbolos.
 *
 * Cumpre o requisito 4: é o {@link AnaLex#proximoToken()} que produz um token de
 * cada vez; é esta fachada que consome esses tokens e organiza o resultado
 * (nada é escrito no ecrã aqui).
 *
 * Cada análise cria NOVAS instâncias das tabelas (sem estado global partilhado),
 * para o servidor ser seguro com vários utilizadores em simultâneo (secção 12).
 */
public final class AnalisadorLexico {

    private AnalisadorLexico() {
        // classe utilitária: não instanciar
    }

    /**
     * Analisa lexicalmente o código-fonte fornecido.
     *
     * @param codigoFonte o código MaculuScript (pode ser {@code null} -> tratado como vazio)
     * @return o resultado com tokens, erros e tabela de símbolos
     */
    public static ResultadoAnalise analisar(String codigoFonte) {
        String fonte = (codigoFonte == null) ? "" : codigoFonte;

        // 1. Tabelas partilhadas, novas a cada análise.
        TabelaPalavrasReservadas reservadas = new TabelaPalavrasReservadas();
        TabelaSimbolos simbolos = new TabelaSimbolos();

        // 2. Analisador léxico sobre um Reader da String.
        AnaLex lex = new AnaLex(new StringReader(fonte));
        lex.setTabelas(reservadas, simbolos);

        // 3. Loop: pede um token de cada vez até ao EOF (que não é guardado).
        List<Token> tokens = new ArrayList<>();
        try {
            Token t;
            while ((t = lex.proximoToken()).getTipo() != TipoToken.EOF) {
                tokens.add(t);
            }
        } catch (IOException e) {
            // Um StringReader não lança IOException na leitura normal;
            // este catch é apenas defensivo (a assinatura gerada declara-a).
            throw new RuntimeException("Erro inesperado ao ler o código-fonte.", e);
        }

        // 4./5. Recolhe erros e tabela de símbolos e devolve o resultado.
        return new ResultadoAnalise(tokens, lex.getErros(), simbolos.todos());
    }
}
