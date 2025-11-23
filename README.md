# JaVai

Aplicativo Spring Boot para organizar encontros presenciais no Rio de Janeiro. A API serve uma interface web single-page (`src/main/resources/static/mapa-rj.html`) que combina autenticação, mapa com pontos do OpenStreetMap, gerenciamento de amigos, chat (direto e em grupos), favoritos e votação Ranked Choice para decidir o próximo rolê.

## Tecnologias

- **Java 21** + **Spring Boot 3** (Web, Security, Data JPA e WebFlux).
- **PostgreSQL** para armazenamento relacional.
- **JPA/Hibernate** com esquema auto atualizável (`spring.jpa.hibernate.ddl-auto=update`).
- **Leaflet / HTML estático** para a interface do mapa e chat.
- **Lombok** para reduzir boilerplate.

## Funcionalidades principais

- Registro, login e logout com sessão HTTP.
- CRUD básico de usuários e lista de amigos (REQ/ACEITE).
- Favoritar lugares do mapa e compartilhar no chat.
- Chat direto com amigos ou grupos, incluindo envio de favoritos.
- Criação de grupos com definição automática de admin.
- Votação com Ranked Choice Voting (RCV) dentro de grupos para decidir lugares favoritos.
- Integração com **Overpass API** (OSM) para buscar lugares próximos ao usuário.

## Estrutura do projeto

```
src/
 └── main/
     ├── java/com/application/javai/
     │    ├── auth/..., config/..., controller/, dto..., service/...
     │    ├── model/                 # Entidades JPA
     │    ├── repository/            # Repositórios Spring Data
     │    └── voting/RankedChoiceVoting.java
     └── resources/
          ├── static/mapa-rj.html    # Única página do front
          └── application.properties # Configuração (Postgres, JPA)
```

## Pré-requisitos

- **Java 21**
- **Gradle wrapper** (já incluído)
- **PostgreSQL**: configure um banco local e ajuste `spring.datasource.*` em `src/main/resources/application.properties`.

Valores default:
```
spring.datasource.url=jdbc:postgresql://localhost:54509/postgres
spring.datasource.username=postgres
spring.datasource.password=javai123
```

## Como executar

1. Instale dependências e compile:
   ```bash
   ./gradlew compileJava
   ```
2. Suba a aplicação:
   ```bash
   ./gradlew bootRun
   ```
3. Acesse `http://localhost:8080/mapa-rj.html`.

## Testes

```bash
./gradlew test
```

## Endpoints principais

| Módulo | Endpoints |
| --- | --- |
| **Auth** | `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/logout` |
| **Usuários** | `GET /api/users`, `GET /api/user/{id}`, `DELETE /api/user/{id}` |
| **Conta** | `GET /api/me`, `PATCH /api/me/name`, `POST /api/me/avatar` |
| **Amigos** | `GET /api/friends`, `GET /api/friends/search`, `GET/POST /api/friends/requests...` |
| **Favoritos** | `GET/POST /api/favorites`, `DELETE /api/favorites/{id}` |
| **Chat** | `GET /api/chat/rooms`, `POST /api/chat/rooms/direct`, `POST /api/chat/rooms/group`, `GET/POST /api/chat/rooms/{id}/messages` |
| **Votação** | `POST /api/voting/sessions`, `POST /api/voting/sessions/{id}/votes`, `POST /api/voting/sessions/{id}/close`, `GET /api/voting/rooms/{roomId}/current` |
| **Locais** | `GET /api/places?lat=...&lon=...&radius=...` |

Todos os endpoints exigem sessão autenticada (exceto `/api/auth/**`).

## Fluxos de uso

1. **Autenticação**: Registre-se ou faça login. A sessão é baseada em cookies (`JSESSIONID`).
2. **Mapa**: ao carregar, o mapa usa sua localização para buscar pontos de interesse via Overpass.
3. **Favoritos**: clique em “⭐ Favoritar” no popup de um lugar. Use o botão “+” no chat para enviar favoritos.
4. **Amigos**: na aba “Conta” você envia/aceita pedidos e gerencia amigos.
5. **Chat**:
   - Use a sidebar para abrir conversas diretas.
   - Clique em “Criar grupo”, dê um nome e selecione amigos para abrir um chat de grupo (você passa a ser o admin).
6. **Votação**:
   - Apenas grupos mostram o painel de votação.
   - Admins conseguem “Iniciar rolê / votação” escolhendo favoritos e ordenando-os.
   - Participantes ordenam as opções e enviam seu ranking.
   - O admin encerra a sessão; a aplicação roda RCV e exibe o vencedor.

## Próximos passos sugeridos

- Persistir métricas e histórico de votações no front.
- Implementar notificações em tempo real (WebSocket) para novos chats/votos.
- Externalizar configurações sensíveis (datasource) via variáveis de ambiente.

---

Feito por estudantes da IME XXVII — sugestões e PRs são sempre bem-vindos.
