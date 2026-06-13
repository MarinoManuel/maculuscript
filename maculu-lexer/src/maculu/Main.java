package maculu;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Driver de linha de comando do analisador léxico da MaculuScript.
 *
 * Uso:
 *   java -cp out maculu.Main caminho/para/ficheiro.maculu
 *   java -cp out maculu.Main            (lê o código de stdin)
 *
 * Responsável APENAS pela saída no ecrã (requisito 4): chama a fachada
 * {@link AnalisadorLexico#analisar(String)} e imprime, por esta ordem, os
 * tokens, a tabela de símbolos e os erros léxicos (formato da secção 10).
 *
 * A saída é forçada a UTF-8 para que os acentos das mensagens apareçam
 * correctamente (no Windows, use "chcp 65001" na consola).
 */
public class Main {

    public static void main(String[] args) {
        PrintStream out = new PrintStream(
                new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8);

        // 0. Modo especial: imprimir a tabela de mapeamento reservada -> Java (req. 5).
        if (args.length >= 1 && args[0].equals("--reservadas")) {
            imprimirMapeamentoReservadas(out);
            return;
        }

        // 1. Obter o código-fonte (de um ficheiro ou de stdin).
        String codigo;
        try {
            if (args.length >= 1) {
                codigo = new String(Files.readAllBytes(Paths.get(args[0])), StandardCharsets.UTF_8);
            } else {
                codigo = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            out.println("Erro ao ler a entrada: " + e.getMessage());
            return;
        }

        // 2. Analisar.
        ResultadoAnalise resultado = AnalisadorLexico.analisar(codigo);

        // 3. Imprimir os tokens.
        out.println("=== TOKENS ===");
        for (Token t : resultado.getTokens()) {
            out.println(t);
        }

        // 4. Imprimir a tabela de símbolos (com alinhamento).
        out.println();
        out.println("=== TABELA DE SÍMBOLOS ===");
        Collection<Simbolo> simbolos = resultado.getTabelaSimbolos();
        if (simbolos.isEmpty()) {
            out.println("(vazia)");
        } else {
            int largura = 0;
            for (Simbolo s : simbolos) {
                largura = Math.max(largura, s.getNome().length());
            }
            for (Simbolo s : simbolos) {
                out.println(String.format("%-" + largura + "s  (1ª linha: %d)",
                        s.getNome(), s.getPrimeiraLinha()));
            }
        }

        // 5. Imprimir os erros léxicos.
        out.println();
        out.println("=== ERROS LÉXICOS ===");
        if (resultado.temErros()) {
            for (ErroLexico erro : resultado.getErros()) {
                out.println(erro);
            }
        } else {
            out.println("Sem erros léxicos.");
        }
    }

    /**
     * Imprime a tabela de mapeamento entre cada palavra reservada da
     * MaculuScript e o seu equivalente em Java (requisito 5 do docente).
     */
    private static void imprimirMapeamentoReservadas(PrintStream out) {
        TabelaPalavrasReservadas reservadas = new TabelaPalavrasReservadas();
        out.println("=== PALAVRAS RESERVADAS (MaculuScript -> TipoToken -> Java) ===");
        int largura = 0;
        for (String palavra : reservadas.palavrasReservadas()) {
            largura = Math.max(largura, palavra.length());
        }
        for (String palavra : reservadas.palavrasReservadas()) {
            out.println(String.format("%-" + largura + "s  ->  %-14s  ->  %s",
                    palavra, reservadas.tokenDe(palavra), reservadas.equivalenteJava(palavra)));
        }
        out.println("Total: " + reservadas.tamanho() + " palavras reservadas.");
    }
}
