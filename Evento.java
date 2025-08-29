import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Evento {
    private int id;
    private String nome;
    private String categoria; // ex.: Música, Esporte, Show…
    private String descricao;
    private LocalDateTime horario;
    private List<String> participantes = new ArrayList<>();

    public Evento(int id, String nome, String categoria, String descricao, LocalDateTime horario) {
        this.id = id;
        this.nome = nome;
        this.categoria = categoria;
        this.descricao = descricao;
        this.horario = horario;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getCategoria() { return categoria; }
    public String getDescricao() { return descricao; }
    public LocalDateTime getHorario() { return horario; }
    public List<String> getParticipantes() { return participantes; }

    public void adicionarParticipante(String nomeUsuario) {
        if (!participantes.contains(nomeUsuario)) participantes.add(nomeUsuario);
    }

    public boolean jaOcorreu() {
        return horario.isBefore(LocalDateTime.now());
    }

    @Override
    public String toString() {
        String quando = Utils.fmt(horario);
        return String.format("[%d] %s | %s | %s | %s | Participantes: %d%s",
                id, nome, categoria, descricao, quando, participantes.size(),
                jaOcorreu() ? " (JÁ OCORREU)" : "");
    }

    // Persistência simples (CSV)
    public String toCsv() {
        String parts = String.join(",", participantes).replace(";", "\\;");
        return id + ";" + esc(nome) + ";" + esc(categoria) + ";" + esc(descricao) + ";" + horario + ";" + parts;
    }

    public static Evento fromCsv(String line) {
        String[] p = line.split("(?<!\\\\);", -1);
        int id = Integer.parseInt(p[0]);
        Evento e = new Evento(id, unesc(p[1]), unesc(p[2]), unesc(p[3]),
                LocalDateTime.parse(p[4])); // ISO_LOCAL_DATE_TIME
        if (p.length >= 6 && !p[5].isEmpty()) {
            for (String nome : p[5].split(",")) {
                if (!nome.isBlank()) e.participantes.add(nome);
            }
        }
        return e;
    }

    private static String esc(String s){ return s.replace(";", "\\;"); }
    private static String unesc(String s){ return s.replace("\\;", ";"); }
}
