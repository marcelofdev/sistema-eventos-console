# Sistema de Eventos (Console – Java)

Protótipo em **Java (POO)** para cadastro e notificação de eventos, com persistência simples em arquivo.

## Requisitos
- **JDK 24+**
- **VS Code** com *Extension Pack for Java* (ou qualquer IDE de sua preferência)

## Como executar

### Via VS Code
1. Abra a pasta do projeto no VS Code.
2. Abra `Main.java` e clique em **Run** acima do método `main`.

### Via Terminal
```bash
# compilar (na pasta do projeto)
javac *.java

# executar
java Main
```

> **Formato de data aceito:** `dd/MM/yyyy HH:mm` (ex.: `25/12/2025 20:30`)

## Funcionalidades
- Cadastro de **Usuários** e **Eventos**
- **Confirmação de presença** (usuário ↔ evento)
- **Filtro por categoria** de evento
- **Próximos eventos** (a partir de agora)
- **Eventos do dia** (exibidos ao iniciar)
- **Persistência** dos dados em `users.data` e `events.data` (CSV simples)

## Menu (opções do programa)
```
1) Cadastrar usuário
2) Cadastrar evento
3) Listar eventos
4) Filtrar eventos por categoria
5) Confirmar presença em evento
6) Ver próximos eventos
7) Salvar agora
8) Recarregar do arquivo
0) Sair (salva automaticamente)
```

## Estrutura do projeto
```
.
├── Evento.java
├── Main.java
├── SistemaEventos.java
├── Usuario.java
├── Utils.java
└── .gitignore
```

## Persistência de dados
- `users.data` – usuários (CSV com escape de `;`)
- `events.data` – eventos + participantes  
> Os `.data` estão ignorados via `.gitignore`. Se precisar versioná-los, remova `*.data` do `.gitignore`.

## Exemplo de execução
```text
=== Eventos de HOJE ===
Nenhum evento para hoje.

===== MENU =====
1) Cadastrar usuário
2) Cadastrar evento
...
```

## Roadmap
- Editar/Excluir usuários e eventos
- Busca por cidade/bairro
- Exportar CSV/relatórios
- Organização em pacotes (MVC)
- Testes automatizados

## Como contribuir
1. Crie uma *branch*: `git checkout -b feature/sua-feature`
2. Faça commits pequenos e claros
3. `git push -u origin feature/sua-feature`
4. Abra um **Pull Request** no GitHub
