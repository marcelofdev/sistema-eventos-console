import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final String USERS_FILE = "users.data";
    private static final String EVENTS_FILE = "events.data";

    public static void main(String[] args) {
        SistemaEventos sistema = new SistemaEventos();
        try { sistema.carregar(USERS_FILE, EVENTS_FILE); }
        catch (Exception e) { System.out.println("Nenhum arquivo carregado (primeira execução)."); }

        // Notificação inicial
        var hoje = sistema.eventosDoDia();
        System.out.println("\n=== Eventos de HOJE ===");
        if (hoje.isEmpty()) System.out.println("Nenhum evento para hoje."); else listarEventos(hoje);

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n===== MENU =====");
            System.out.println("1) Cadastrar usuário");
            System.out.println("2) Cadastrar evento");
            System.out.println("3) Listar eventos");
            System.out.println("4) Filtrar por categoria");
            System.out.println("5) Confirmar presença em evento");
            System.out.println("6) Ver próximos eventos");
            System.out.println("7) Ver eventos EM ANDAMENTO");
            System.out.println("8) Meus eventos + Cancelar participação");
            System.out.println("9) Salvar agora");
            System.out.println("10) Recarregar do arquivo");
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
                    case "7" -> listarEventos(sistema.emAndamento());
                    case "8" -> meusEventosECancelar(sc, sistema);
                    case "9" -> { sistema.salvar(USERS_FILE, EVENTS_FILE); System.out.println("Salvo!"); }
                    case "10" -> { sistema.carregar(USERS_FILE, EVENTS_FILE); System.out.println("Recarregado!"); }
                    case "0" -> {
                        sistema.salvar(USERS_FILE, EVENTS_FILE);
                        System.out.println("Até mais! Dados salvos.");
                        sc.close(); return;
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
        System.out.print("Endereço do evento: "); String endereco = sc.nextLine();

        // Categoria entre as predefinidas
        String cat;
        while (true) {
            System.out.println("Categorias disponíveis:");
            for (String c : SistemaEventos.CATEGORIAS) System.out.print(c + "  ");
            System.out.println();
            System.out.print("Categoria: ");
            cat = sc.nextLine().trim();
            if (SistemaEventos.categoriaValida(cat)) break;
            System.out.println("Categoria inválida. Tente novamente.");
        }

        System.out.print("Descrição: "); String desc = sc.nextLine();

        LocalDateTime quando = lerDataHora(sc);
        int dur = lerInteiroPositivo(sc, "Duração em minutos: ");

        var e = s.cadastrarEvento(nome, endereco, cat, desc, quando, dur);
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
            System.out.println("Cadastre ao menos 1 usuário e 1 evento."); return;
        }
        System.out.println("\n-- Usuários --"); s.getUsuarios().forEach(System.out::println);
        int uid = lerInteiro(sc, "ID do usuário: ");
        var u = s.buscarUsuarioPorId(uid); if (u == null) { System.out.println("Usuário não encontrado."); return; }

        System.out.println("\n-- Eventos --"); s.getEventos().forEach(System.out::println);
        int eid = lerInteiro(sc, "ID do evento: ");
        var e = s.buscarEventoPorId(eid); if (e == null) { System.out.println("Evento não encontrado."); return; }

        s.confirmarPresenca(u, e);
        System.out.println("Presença confirmada: " + u.getNome() + " -> " + e.getNome());
    }

    private static void meusEventosECancelar(Scanner sc, SistemaEventos s) {
        if (s.getUsuarios().isEmpty()) { System.out.println("Cadastre um usuário primeiro."); return; }
        System.out.println("\n-- Usuários --"); s.getUsuarios().forEach(System.out::println);
        int uid = lerInteiro(sc, "ID do usuário: ");
        var u = s.buscarUsuarioPorId(uid); if (u == null) { System.out.println("Usuário não encontrado."); return; }

        var lista = s.eventosDoUsuario(u.getNome());
        if (lista.isEmpty()) { System.out.println("O usuário não confirmou nenhum evento."); return; }
        listarEventos(lista);

        System.out.print("Deseja cancelar participação em algum? (s/N): ");
        if (sc.nextLine().trim().equalsIgnoreCase("s")) {
            int eid = lerInteiro(sc, "ID do evento para cancelar: ");
            var e = s.buscarEventoPorId(eid);
            if (e == null || !e.getParticipantes().contains(u.getNome())) {
                System.out.println("Esse evento não está confirmado para o usuário.");
            } else {
                s.cancelarPresenca(u, e);
                System.out.println("Cancelado: " + u.getNome() + " em " + e.getNome());
            }
        }
    }

    // === Helpers ===
    private static int lerInteiro(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException ex) { System.out.println("Valor inválido. Digite um número inteiro."); }
        }
    }
    private static int lerInteiroPositivo(Scanner sc, String prompt) {
        while (true) {
            int n = lerInteiro(sc, prompt);
            if (n > 0) return n;
            System.out.println("Informe um número positivo.");
        }
    }
    private static LocalDateTime lerDataHora(Scanner sc) {
        while (true) {
            System.out.print("Data e hora (dd/MM/yyyy HH:mm): ");
            var dt = Utils.tryParse(sc.nextLine());
            if (dt.isPresent()) return dt.get();
            System.out.println("Formato inválido. Ex.: 25/12/2025 20:30");
        }
    }
}
