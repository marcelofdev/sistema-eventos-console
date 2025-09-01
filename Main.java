import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class Main {
    // -> salva em ./data/
    private static final String DATA_DIR    = "data";
    private static final String REPORTS_DIR = DATA_DIR + "/reports";
    private static final String USERS_FILE  = DATA_DIR + "/users.data";
    private static final String EVENTS_FILE = DATA_DIR + "/events.data";

    public static void main(String[] args) {
        SistemaEventos sistema = new SistemaEventos();

        // garante que a pasta existe
        new java.io.File(DATA_DIR).mkdirs();
        new java.io.File(REPORTS_DIR).mkdirs();

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
            System.out.println("9) Editar evento");
            System.out.println("10) Excluir evento");
            System.out.println("11) Editar usuário");
            System.out.println("12) Excluir usuário");
            System.out.println("13) Salvar agora");
            System.out.println("14) Recarregar do arquivo");
            System.out.println("15) Buscar eventos por endereço");
            System.out.println("16) Eventos na minha região (por usuário)");
            System.out.println("17) Exportar CSV - todos os eventos");
            System.out.println("18) Exportar CSV - meus eventos (por usuário)");
            System.out.println("19) Exportar CSV - usuários");
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
                    case "9" -> editarEvento(sc, sistema);
                    case "10" -> excluirEvento(sc, sistema);
                    case "11" -> editarUsuario(sc, sistema);
                    case "12" -> excluirUsuario(sc, sistema);
                    case "13" -> { sistema.salvar(USERS_FILE, EVENTS_FILE); System.out.println("Salvo!"); }
                    case "14" -> { sistema.carregar(USERS_FILE, EVENTS_FILE); System.out.println("Recarregado!"); }
                    case "15" -> buscarPorEndereco(sc, sistema);
                    case "16" -> eventosDaMinhaRegiao(sc, sistema);
                    case "17" -> exportarTodosEventos(sc, sistema);
                    case "18" -> exportarMeusEventos(sc, sistema);
                    case "19" -> exportarUsuarios(sistema);
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

    private static void buscarPorEndereco(Scanner sc, SistemaEventos s) {
        System.out.print("Digite parte do endereço (cidade, bairro ou rua): ");
        String termo = sc.nextLine().trim();
        var lista = s.buscarPorEndereco(termo);
        if (lista.isEmpty()) System.out.println("Nenhum evento encontrado para \"" + termo + "\".");
        else listarEventos(lista);
    }

    private static void eventosDaMinhaRegiao(Scanner sc, SistemaEventos s) {
        if (s.getUsuarios().isEmpty()) { System.out.println("Cadastre um usuário primeiro."); return; }
        System.out.println("\n-- Usuários --");
        s.getUsuarios().forEach(System.out::println);
        int uid = lerInteiro(sc, "ID do usuário: ");
        var u = s.buscarUsuarioPorId(uid);
        if (u == null) { System.out.println("Usuário não encontrado."); return; }

        System.out.println("Buscando por endereço do usuário: " + u.getEndereco());
        var lista = s.eventosNaMesmaRegiao(u);
        if (lista.isEmpty()) System.out.println("Nenhum evento encontrado na região do usuário.");
        else listarEventos(lista);
    }

    private static void cadastrarEvento(Scanner sc, SistemaEventos s) {
        System.out.print("Nome do evento: "); String nome = sc.nextLine();
        System.out.print("Endereço do evento: "); String endereco = sc.nextLine();

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

    private static void editarEvento(Scanner sc, SistemaEventos s) {
        listarEventos(s.getEventos());
        int id = lerInteiro(sc, "ID do evento para editar: ");
        Evento e = s.buscarEventoPorId(id);
        if (e == null) { System.out.println("Evento não encontrado."); return; }

        String nome = lerOpcional(sc, "Nome [" + e.getNome() + "]: ", e.getNome());
        String end  = lerOpcional(sc, "Endereço [" + e.getEndereco() + "]: ", e.getEndereco());

        String cat;
        while (true) {
            String cats = String.join(", ", SistemaEventos.CATEGORIAS);
            cat = lerOpcional(sc, "Categoria [" + e.getCategoria() + "] (" + cats + "): ", e.getCategoria());
            if (SistemaEventos.categoriaValida(cat)) break;
            System.out.println("Categoria inválida.");
        }

        String desc = lerOpcional(sc, "Descrição [" + e.getDescricao() + "]: ", e.getDescricao());
        LocalDateTime quando = lerDataHoraOpcional(sc, "Data/hora [" + Utils.fmt(e.getHorario()) + "]: ", e.getHorario());
        int dur = lerInteiroPositivoOpcional(sc, "Duração (min) [" + e.getDuracaoMinutos() + "]: ", e.getDuracaoMinutos());

        if (s.editarEvento(id, nome, end, cat, desc, quando, dur)) System.out.println("Evento atualizado.");
    }

    private static void excluirEvento(Scanner sc, SistemaEventos s) {
        listarEventos(s.getEventos());
        int id = lerInteiro(sc, "ID do evento para excluir: ");
        System.out.print("Confirma exclusão? (s/N): ");
        if (sc.nextLine().trim().equalsIgnoreCase("s")) {
            System.out.println(s.excluirEvento(id) ? "Excluído." : "Não encontrado.");
        }
    }

    private static void editarUsuario(Scanner sc, SistemaEventos s) {
        s.getUsuarios().forEach(System.out::println);
        int id = lerInteiro(sc, "ID do usuário para editar: ");
        Usuario u = s.buscarUsuarioPorId(id);
        if (u == null) { System.out.println("Usuário não encontrado."); return; }

        String nome = lerOpcional(sc, "Nome [" + u.getNome() + "]: ", u.getNome());
        String end  = lerOpcional(sc, "Endereço [" + u.getEndereco() + "]: ", u.getEndereco());
        String cat  = lerOpcional(sc, "Categoria [" + u.getCategoria() + "]: ", u.getCategoria());
        String desc = lerOpcional(sc, "Descrição [" + u.getDescricao() + "]: ", u.getDescricao());

        if (s.editarUsuario(id, nome, end, cat, desc)) System.out.println("Usuário atualizado.");
    }

    private static void excluirUsuario(Scanner sc, SistemaEventos s) {
        s.getUsuarios().forEach(System.out::println);
        int id = lerInteiro(sc, "ID do usuário para excluir: ");
        System.out.print("Confirma exclusão? (s/N): ");
        if (sc.nextLine().trim().equalsIgnoreCase("s")) {
            System.out.println(s.excluirUsuario(id) ? "Excluído." : "Não encontrado.");
        }
    }

    private static void exportarTodosEventos(Scanner sc, SistemaEventos s) {
    var lista = s.getEventos();
    if (lista.isEmpty()) { System.out.println("Não há eventos para exportar."); return; }
    String file = REPORTS_DIR + "/eventos_" + ts() + ".csv";
    try {
        s.exportEventosCsv(file, lista);
        System.out.println("Relatório gerado: " + file);
    } catch (Exception ex) {
        System.out.println("Falha ao exportar: " + ex.getMessage());
    }
}

private static void exportarMeusEventos(Scanner sc, SistemaEventos s) {
    if (s.getUsuarios().isEmpty()) { System.out.println("Cadastre um usuário primeiro."); return; }
    s.getUsuarios().forEach(System.out::println);
    int uid = lerInteiro(sc, "ID do usuário: ");
    var u = s.buscarUsuarioPorId(uid);
    if (u == null) { System.out.println("Usuário não encontrado."); return; }

    var lista = s.eventosDoUsuario(u.getNome());
    if (lista.isEmpty()) { System.out.println("O usuário não possui eventos confirmados."); return; }

    String file = REPORTS_DIR + "/meus_eventos_u" + u.getId() + "_" + ts() + ".csv";
    try {
        s.exportEventosCsv(file, lista);
        System.out.println("Relatório gerado: " + file);
    } catch (Exception ex) {
        System.out.println("Falha ao exportar: " + ex.getMessage());
    }
}

private static void exportarUsuarios(SistemaEventos s) {
    var lista = s.getUsuarios();
    if (lista.isEmpty()) { System.out.println("Não há usuários para exportar."); return; }
    String file = REPORTS_DIR + "/usuarios_" + ts() + ".csv";
    try {
        s.exportUsuariosCsv(file, lista);
        System.out.println("Relatório gerado: " + file);
    } catch (Exception ex) {
        System.out.println("Falha ao exportar: " + ex.getMessage());
    }
}


    // timestamp p/ nomes de arquivos
    private static String ts() {
        return java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
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
    private static int lerInteiroPositivoOpcional(Scanner sc, String prompt, int atual) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            if (s.isEmpty()) return atual;
            try {
                int n = Integer.parseInt(s);
                if (n > 0) return n;
            } catch (NumberFormatException ignored) {}
            System.out.println("Informe número positivo ou ENTER para manter.");
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
    private static LocalDateTime lerDataHoraOpcional(Scanner sc, String prompt, LocalDateTime atual) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            if (s.isEmpty()) return atual;
            var dt = Utils.tryParse(s);
            if (dt.isPresent()) return dt.get();
            System.out.println("Formato inválido. ENTER mantém.");
        }
    }
    private static String lerOpcional(Scanner sc, String prompt, String atual) {
        System.out.print(prompt);
        String s = sc.nextLine();
        return s.isBlank() ? atual : s;
    }
}
