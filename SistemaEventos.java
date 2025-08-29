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

    // ===== Usuários =====
    public Usuario cadastrarUsuario(String nome, String end, String cat, String desc) {
        Usuario u = new Usuario(nextUserId++, nome, end, cat, desc);
        usuarios.add(u);
        return u;
    }
    public List<Usuario> getUsuarios() { return usuarios; }

    // ===== Eventos =====
    public Evento cadastrarEvento(String nome, String cat, String desc, LocalDateTime horario) {
        Evento e = new Evento(nextEventId++, nome, cat, desc, horario);
        eventos.add(e);
        return e;
    }
    public List<Evento> getEventos() { return eventos; }

    public List<Evento> filtrarPorCategoria(String categoria) {
        return eventos.stream()
                .filter(e -> e.getCategoria().equalsIgnoreCase(categoria))
                .sorted(Comparator.comparing(Evento::getHorario))
                .collect(Collectors.toList());
    }

    public Evento buscarEventoPorId(int id) {
        return eventos.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
    }
    public Usuario buscarUsuarioPorId(int id) {
        return usuarios.stream().filter(u -> u.getId() == id).findFirst().orElse(null);
    }

    public List<Evento> proximosEventos() {
        return eventos.stream()
                .filter(e -> !e.jaOcorreu())
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
