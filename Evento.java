import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Evento {
    private int id;
    private String nome;
    private String endereco;   // NOVO: obrigatório no requisito
    private String categoria;
    private String descricao;
    private LocalDateTime horario; // início
    private int duracaoMinutos;    // NOVO: para saber se está ocorrendo agora
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
    // Formato novo: id;nome;endereco;categoria;descricao;inicioISO;duracao;participantesCSV
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
                String nome = unesc(p[1]);
                String end = unesc(p[2]);
                String cat = unesc(p[3]);
                String desc = unesc(p[4]);
                LocalDateTime inicio = LocalDateTime.parse(p[5]);
                int dur = Integer.parseInt(p[6]);
                Evento e = new Evento(id, nome, end, cat, desc, inicio, dur);
                if (p.length >= 8 && !p[7].isEmpty()) {
                    for (String nomePart : p[7].split(",")) if (!nomePart.isBlank()) e.participantes.add(nomePart);
                }
                return e;
            } else {
                // BACKCOMPAT (versões antigas sem endereço/duração)
                int id = Integer.parseInt(p[0]);
                String nome = unesc(p[1]);
                String cat = unesc(p[2]);
                String desc = unesc(p[3]);
                LocalDateTime inicio = LocalDateTime.parse(p[4]);
                Evento e = new Evento(id, nome, "", cat, desc, inicio, 120);
                if (p.length >= 6 && !p[5].isEmpty()) {
                    for (String nomePart : p[5].split(",")) if (!nomePart.isBlank()) e.participantes.add(nomePart);
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
