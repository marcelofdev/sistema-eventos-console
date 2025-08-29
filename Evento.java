import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Evento {
    private int id;
    private String nome;
    private String endereco;   // obrigatório
    private String categoria;
    private String descricao;
    private LocalDateTime horario; // início
    private int duracaoMinutos;    // para saber se está ocorrendo agora
    private List<String> participantes = new ArrayList<>(); // nomes

    public Evento(int id, String nome, String endereco, String categoria, String descricao,
                  LocalDateTime horario, int duracaoMinutos) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.categoria = categoria;
        this.descricao = descricao;
        this.horario = horario;
        this.duracaoMinutos = duracaoMinutos;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
    public String getCategoria() { return categoria; }
    public String getDescricao() { return descricao; }
    public LocalDateTime getHorario() { return horario; }
    public int getDuracaoMinutos() { return duracaoMinutos; }
    public List<String> getParticipantes() { return participantes; }

    // ==== SETTERS para edição ====
    public void setNome(String nome) { this.nome = nome; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setHorario(LocalDateTime horario) { this.horario = horario; }
    public void setDuracaoMinutos(int duracaoMinutos) { this.duracaoMinutos = duracaoMinutos; }

    public void adicionarParticipante(String nomeUsuario) {
        if (!participantes.contains(nomeUsuario)) participantes.add(nomeUsuario);
    }
    public void removerParticipante(String nomeUsuario) {
        participantes.remove(nomeUsuario);
    }

    public boolean jaOcorreu() {
        return LocalDateTime.now().isAfter(horario.plusMinutes(duracaoMinutos));
    }
    public boolean emAndamento() {
        LocalDateTime agora = LocalDateTime.now();
        return !agora.isBefore(horario) && agora.isBefore(horario.plusMinutes(duracaoMinutos));
    }

    @Override
    public String toString() {
        String quando = Utils.fmt(horario);
        String status = emAndamento() ? " (EM ANDAMENTO)" : (jaOcorreu() ? " (JÁ OCORREU)" : "");
        return String.format("[%d] %s | %s | %s | End: %s | %s | %d min | Participantes: %d%s",
                id, nome, categoria, descricao, endereco, quando, duracaoMinutos, participantes.size(), status);
    }

    // ===== Persistência CSV =====
    // Formato: id;nome;endereco;categoria;descricao;inicioISO;duracao;participantesCSV
    public String toCsv() {
        String parts = String.join(",", participantes).replace(";", "\\;");
        return id + ";" + esc(nome) + ";" + esc(endereco) + ";" + esc(categoria) + ";" +
               esc(descricao) + ";" + horario + ";" + duracaoMinutos + ";" + parts;
    }

    public static Evento fromCsv(String line) {
        String[] p = line.split("(?<!\\\\);", -1);
        try {
            if (p.length >= 7) {
                int id = Integer.parseInt(p[0]);
                Evento e = new Evento(id, unesc(p[1]), unesc(p[2]), unesc(p[3]),
                        unesc(p[4]), LocalDateTime.parse(p[5]), Integer.parseInt(p[6]));
                if (p.length >= 8 && !p[7].isEmpty()) {
                    for (String nome : p[7].split(",")) if (!nome.isBlank()) e.participantes.add(nome);
                }
                return e;
            } else {
                // BACKCOMPAT (versões antigas sem endereço/duração)
                int id = Integer.parseInt(p[0]);
                Evento e = new Evento(id, unesc(p[1]), "", unesc(p[2]), unesc(p[3]),
                        LocalDateTime.parse(p[4]), 120);
                if (p.length >= 6 && !p[5].isEmpty()) {
                    for (String nome : p[5].split(",")) if (!nome.isBlank()) e.participantes.add(nome);
                }
                return e;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Falha ao ler evento do arquivo: " + ex.getMessage());
        }
    }

    private static String esc(String s){ return s.replace(";", "\\;"); }
    private static String unesc(String s){ return s.replace("\\;", ";"); }
}
