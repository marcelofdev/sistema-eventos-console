public class Usuario {
    private int id;
    private String nome;
    private String endereco;
    private String categoria; // perfil do usuário
    private String descricao;

    public Usuario(int id, String nome, String endereco, String categoria, String descricao) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.categoria = categoria;
        this.descricao = descricao;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
    public String getCategoria() { return categoria; }
    public String getDescricao() { return descricao; }

    // ==== SETTERS para edição ====
    public void setNome(String nome) { this.nome = nome; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s | %s | %s", id, nome, endereco, categoria, descricao);
    }

    // Persistência simples (CSV)
    public String toCsv() {
        return id + ";" + escape(nome) + ";" + escape(endereco) + ";" + escape(categoria) + ";" + escape(descricao);
    }

    public static Usuario fromCsv(String line) {
        String[] p = splitKeepEmpty(line);
        int id = Integer.parseInt(p[0]);
        return new Usuario(id, unescape(p[1]), unescape(p[2]), unescape(p[3]), unescape(p[4]));
    }

    // Helpers CSV
    private static String escape(String s) { return s.replace(";", "\\;"); }
    private static String unescape(String s) { return s.replace("\\;", ";"); }
    private static String[] splitKeepEmpty(String s) {
        return s.split("(?<!\\\\);", -1);
    }
}
