import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SistemaEventos {
    private List<Usuario> usuarios = new ArrayList<>();
    private List<Evento> eventos = new ArrayList<>();
    private int nextUserId = 1;
    private int nextEventId = 1;

    // Categorias delimitadas (requisito)
    public static final String[] CATEGORIAS = {
        "Festa", "Esporte", "Show", "Cultural", "Tecnologia", "Outros"
    };
    public static boolean categoriaValida(String cat) {
        for (String c : CATEGORIAS) if (c.equalsIgnoreCase(cat)) return true;
        return false;
    }

    // ===== Usuários =====
    public Usuario cadastrarUsuario(String nome, String end, String cat, String desc) {
        Usuario u = new Usuario(nextUserId++, nome, end, cat, desc);
        usuarios.add(u);
        return u;
    }
    public List<Usuario> getUsuarios() { return usuarios; }
    public Usuario buscarUsuarioPorId(int id) {
        return usuarios.stream().filter(u -> u.getId() == id).findFirst().orElse(null);
    }
    public boolean editarUsuario(int id, String nome, String end, String cat, String desc) {
        Usuario u = buscarUsuarioPorId(id);
        if (u == null) return false;
        u.setNome(nome);
        u.setEndereco(end);
        u.setCategoria(cat);
        u.setDescricao(desc);
        return true;
    }
    public boolean excluirUsuario(int id) {
        Usuario u = buscarUsuarioPorId(id);
        if (u == null) return false;
        // remove das listas de participantes
        for (Evento e : eventos) e.removerParticipante(u.getNome());
        return usuarios.remove(u);
    }

    // ===== Eventos =====
    public Evento cadastrarEvento(String nome, String endereco, String cat, String desc,
                                  LocalDateTime horario, int duracaoMin) {
        Evento e = new Evento(nextEventId++, nome, endereco, cat, desc, horario, duracaoMin);
        eventos.add(e);
        return e;
    }
    public List<Evento> getEventos() { return eventos; }
    public Evento buscarEventoPorId(int id) {
        return eventos.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
    }
    public boolean editarEvento(int id, String nome, String endereco, String cat, String desc,
                                LocalDateTime horario, int duracaoMin) {
        Evento e = buscarEventoPorId(id);
        if (e == null) return false;
        e.setNome(nome);
        e.setEndereco(endereco);
        e.setCategoria(cat);
        e.setDescricao(desc);
        e.setHorario(horario);
        e.setDuracaoMinutos(duracaoMin);
        return true;
    }
    public boolean excluirEvento(int id) {
        Evento e = buscarEventoPorId(id);
        if (e == null) return false;
        return eventos.remove(e);
    }

    // ===== Consultas =====
    public List<Evento> filtrarPorCategoria(String categoria) {
        return eventos.stream()
                .filter(e -> e.getCategoria().equalsIgnoreCase(categoria))
                .sorted(Comparator.comparing(Evento::getHorario))
                .collect(Collectors.toList());
    }
    public List<Evento> buscarPorEndereco(String termo) {
    String t = termo == null ? "" : termo.toLowerCase();
    return eventos.stream()
            .filter(e -> e.getEndereco() != null && e.getEndereco().toLowerCase().contains(t))
            .sorted(Comparator.comparing(Evento::getHorario))
            .collect(Collectors.toList());
    }
    public List<Evento> eventosNaMesmaRegiao(Usuario u) {
    if (u == null || u.getEndereco() == null) return List.of();
    return buscarPorEndereco(u.getEndereco());
    }
    public List<Evento> proximosEventos() {
        LocalDateTime agora = LocalDateTime.now();
        return eventos.stream()
                .filter(e -> e.getHorario().isAfter(agora))
                .sorted(Comparator.comparing(Evento::getHorario))
                .collect(Collectors.toList());
    }
    public List<Evento> emAndamento() {
        return eventos.stream().filter(Evento::emAndamento)
                .sorted(Comparator.comparing(Evento::getHorario))
                .collect(Collectors.toList());
    }
    public List<Evento> jaOcorreram() {
        return eventos.stream().filter(Evento::jaOcorreu)
                .sorted(Comparator.comparing(Evento::getHorario))
                .collect(Collectors.toList());
    }
    public List<Evento> eventosDoDia() {
        LocalDate hoje = LocalDate.now();
        return eventos.stream()
                .filter(e -> e.getHorario().toLocalDate().equals(hoje))
                .sorted(Comparator.comparing(Evento::getHorario))
                .collect(Collectors.toList());
    }
    public List<Evento> eventosDoUsuario(String nomeUsuario) {
        return eventos.stream()
                .filter(e -> e.getParticipantes().contains(nomeUsuario))
                .sorted(Comparator.comparing(Evento::getHorario))
                .collect(Collectors.toList());
    }

    // ===== Exportação CSV =====
    
    // exportacão de usuários
    public void exportEventosCsv(String path, List<Evento> lista) throws Exception {
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
        // Cabeçalho
        bw.write("id;nome;endereco;categoria;descricao;inicio;duracaoMin;participantes");
        bw.newLine();

        // Uma linha por evento
        for (Evento e : lista) {
            String participantes = String.join("|", e.getParticipantes()).replace(";", "\\;");
            bw.write(
                e.getId() + ";" +
                esc(e.getNome()) + ";" +
                esc(e.getEndereco()) + ";" +
                esc(e.getCategoria()) + ";" +
                esc(e.getDescricao()) + ";" +
                Utils.fmt(e.getHorario()) + ";" +
                e.getDuracaoMinutos() + ";" +
                participantes
            );
            bw.newLine();
        }
    }
}

        // helper 
    private static String esc(String s) {
    return s == null ? "" : s.replace(";", "\\;");
}

        // exportacão de usuários
    public void exportUsuariosCsv(String path, List<Usuario> lista) throws Exception {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            bw.write("id;nome;endereco;categoria;descricao");
            bw.newLine();
            for (Usuario u : lista) {
                bw.write(u.getId() + ";" +
                        esc(u.getNome()) + ";" +
                        esc(u.getEndereco()) + ";" +
                        esc(u.getCategoria()) + ";" +
                        esc(u.getDescricao()));
                bw.newLine();
            }
        }
    }
    
    

    // Participação
    public void confirmarPresenca(Usuario u, Evento e) { e.adicionarParticipante(u.getNome()); }
    public void cancelarPresenca(Usuario u, Evento e) { e.removerParticipante(u.getNome()); }

    // ===== Persistência =====
    public void salvar(String usersPath, String eventsPath) throws Exception {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(usersPath))) {
            for (Usuario u : usuarios) bw.write(u.toCsv() + System.lineSeparator());
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(eventsPath))) {
            for (Evento e : eventos) bw.write(e.toCsv() + System.lineSeparator());
        }
    }
    public void carregar(String usersPath, String eventsPath) throws Exception {
        usuarios.clear(); eventos.clear();
        nextUserId = 1; nextEventId = 1;

        try (BufferedReader br = new BufferedReader(new FileReader(usersPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                Usuario u = Usuario.fromCsv(line);
                usuarios.add(u);
                nextUserId = Math.max(nextUserId, u.getId() + 1);
            }
        } catch (Exception ignored) { }

        try (BufferedReader br = new BufferedReader(new FileReader(eventsPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                Evento e = Evento.fromCsv(line);
                eventos.add(e);
                nextEventId = Math.max(nextEventId, e.getId() + 1);
            }
        } catch (Exception ignored) { }
    }
}
