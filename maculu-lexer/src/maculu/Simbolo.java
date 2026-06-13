package maculu;

/**
 * Entrada da tabela de símbolos: representa um identificador encontrado no
 * código-fonte.
 *
 * Guarda o {@code nome} (sempre em minúsculas, por a linguagem ser
 * case-insensitive) e a {@code primeiraLinha} em que o identificador apareceu.
 * O campo {@code tipo} fica reservado para ser preenchido pelo futuro
 * analisador sintáctico/semântico (na fase léxica permanece {@code null}).
 */
public class Simbolo {

    private final String nome;         // sempre em minúsculas
    private final int primeiraLinha;   // 1-based
    private String tipo;               // reservado para o analisador sintáctico (pode ser null)

    public Simbolo(String nome, int primeiraLinha) {
        this.nome = nome;
        this.primeiraLinha = primeiraLinha;
        this.tipo = null;
    }

    public String getNome() {
        return nome;
    }

    public int getPrimeiraLinha() {
        return primeiraLinha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return nome + " (1ª linha: " + primeiraLinha + ")";
    }
}
