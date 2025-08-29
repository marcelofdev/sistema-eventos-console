import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final String USERS_FILE = "users.data";
    private static final String EVENTS_FILE = "events.data";

    public static void main(String[] args) {
        SistemaEventos sistema = new SistemaEventos();
        try {
            sistema.carregar(USERS_FILE, EVENTS_FILE);
        } catch (Exception e) {
            System.out.println("Nenhum arquivo carregado (primeira execução).");
        }

        // Notificação: eventos de hoje
        var hoje = sistema.eventosDoDia();
        System.out.println("\n=== Eventos de HOJE ===");
        if (hoje.isEmpty()) System.out.println("Nenhum evento para hoje.");
        else listarEventos(hoje);

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n===== MENU =====");
            System.out.println("1) Cadastrar usuário");
            System.out.println("2) Cadastrar evento");
            System.out.println("3) Listar eventos");
            System.out.println("4) Filtrar eventos por categoria");
            System.out.println("5) Confirmar presença em evento");
            System.out.println("6) Ver próximos eventos");
            System.out.println("7) Salvar agora");
            System.out.println("8) Recarregar do arquivo");
            System.out.println("0) Sair (salva automaticamente)");
            System.out.print("Escolha: ");
            String op = sc.nextLine().trim();

            try {
                switch (op) {
                    case "1" -> cadastrarUsuario(sc, sistema);
                    case "2" -> cadastrarEvento(sc, sistema);
                    case "3" -> listarEventos(sistema.getEventos());
                    case "4" -> filtrar(sc, sistema);
                    case "5" -> confirmar(sc, sistema);
                    case "6" -> listarEventos(sistema.proximosEventos());
                    case "7" -> { sistema.salvar(USERS_FILE, EVENTS_FILE); System.out.println("Salvo!"); }
                    case "8" -> { sistema.carregar(USERS_FILE, EVENTS_FILE); System.out.println("Recarregado!"); }
                    case "0" -> {
                        sistema.salvar(USERS_FILE, EVENTS_FILE);
                        System.out.println("Até mais! Dados salvos.");
                        sc.close();
                        return;
                    }
                    default -> System.out.println("Opção inválida.");
                }
            } catch (Exception ex) {
                System.out.println("Erro: " + ex.getMessage());
            }
        }
    }

    // === Fluxos ===
    private static void cadastrarUsuario(Scanner sc, SistemaEventos s) {
        System.out.print("Nome: "); String nome = sc.nextLine();
        System.out.print("Endereço (cidade/bairro): "); String end = sc.nextLine();
        System.out.print("Categoria (perfil do usuário): "); String cat = sc.nextLine();
        System.out.print("Descrição: "); String desc = sc.nextLine();
        var u = s.cadastrarUsuario(nome, end, cat, desc);
        System.out.println("Usuário criado: " + u);
    }

    private static void cadastrarEvento(Scanner sc, SistemaEventos s) {
        System.out.print("Nome do evento: "); String nome = sc.nextLine();
        System.out.print("Categoria do evento: "); String cat = sc.nextLine();
        System.out.print("Descrição: "); String desc = sc.nextLine();
        LocalDateTime quando = lerDataHora(sc);
        var e = s.cadastrarEvento(nome, cat, desc, quando);
        System.out.println("Evento criado: " + e);
    }

    private static void listarEventos(List<Evento> eventos) {
        if (eventos.isEmpty()) { System.out.println("Nenhum evento."); return; }
        System.out.println("\n=== Eventos ===");
        for (Evento e : eventos) System.out.println(e);
    }

    private static void filtrar(Scanner sc, SistemaEventos s) {
        System.out.print("Categoria para filtrar: ");
        String cat = sc.nextLine();
        listarEventos(s.filtrarPorCategoria(cat));
    }

    private static void confirmar(Scanner sc, SistemaEventos s) {
        if (s.getUsuarios().isEmpty() || s.getEventos().isEmpty()) {
            System.out.println("Cadastre ao menos 1 usuário e 1 evento.");
            return;
        }
        System.out.println("\n-- Usuários --");
        s.getUsuarios().forEach(System.out::println);
        int uid = lerInteiro(sc, "ID do usuário: ");
        var u = s.buscarUsuarioPorId(uid);
        if (u == null) { System.out.println("Usuário não encontrado."); return; }

        System.out.println("\n-- Eventos --");
        s.getEventos().forEach(System.out::println);
        int eid = lerInteiro(sc, "ID do evento: ");
        var e = s.buscarEventoPorId(eid);
        if (e == null) { System.out.println("Evento não encontrado."); return; }

        e.adicionarParticipante(u.getNome());
        System.out.println("Presença confirmada: " + u.getNome() + " -> " + e.getNome());
    }

    // === Helpers de Entrada ===
    private static int lerInteiro(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine();
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException ex) {
                System.out.println("Valor inválido. Digite um número inteiro.");
            }
        }
    }

    private static LocalDateTime lerDataHora(Scanner sc) {
        while (true) {
            System.out.print("Data e hora (dd/MM/yyyy HH:mm): ");
            String data = sc.nextLine();
            var dt = Utils.tryParse(data);
            if (dt.isPresent()) return dt.get();
            System.out.println("Formato inválido. Exemplo válido: 25/12/2025 20:30");
        }
    }
}
