# Documentação da API Javai

Este documento fornece uma visão geral detalhada dos endpoints da API para a aplicação Javai.

---

## API de Autenticação (`/api/auth`)

Lida com o registro e login de usuários.

### 1. Registrar Usuário

* **Endpoint:** `POST /api/auth/register`
* **Descrição:** Registra um novo usuário no sistema.
* **Corpo da Requisição (Request Body):**
    ```json
    {
      "nome": "string",
      "email": "string",
      "senha": "string"
    }
    ```
* **Respostas:**
    * `200 OK`: Retorna a string "User registered successfully".
    * `400 Bad Request`: Se o corpo da requisição for inválido ou o email já estiver em uso.

### 2. Login de Usuário

* **Endpoint:** `POST /api/auth/login`
* **Descrição:** Autentica um usuário e inicia uma sessão. Este endpoint é gerenciado pelo Spring Security.
* **Corpo da Requisição (Request Body):**
    ```json
    {
      "email": "string",
      "senha": "string"
    }
    ```
* **Respostas:**
    * `200 OK`: Na autenticação bem-sucedida, retorna um cookie de sessão (`JSESSIONID`).
    * `302 Found`: Redireciona para `/mapa-rj.html` em caso de sucesso.
    * `401 Unauthorized`: Se a autenticação falhar.
    * `302 Found`: Redireciona para `/?error` em caso de falha.

### 3. Logout de Usuário

* **Endpoint:** `POST /api/auth/logout`
* **Descrição:** Desloga o usuário atual e invalida sua sessão.
* **Respostas:**
    * `200 OK`: Em caso de logout bem-sucedido.

---

## API da Conta (`/api/me`)

Endpoints para gerenciar os detalhes da conta do próprio usuário autenticado.

### 1. Obter Minhas Informações de Usuário

* **Endpoint:** `GET /api/me`
* **Descrição:** Recupera as informações do perfil do usuário atualmente autenticado.
* **Respostas:**
    * `200 OK`: Retorna um objeto `UserDTO`.
        ```json
        {
          "id": "long",
          "nome": "string",
          "email": "string"
        }
        ```
    * `401 Unauthorized`: Se o usuário não estiver autenticado.
    * `404 Not Found`: Se o registro do usuário autenticado não puder ser encontrado no banco de dados.

### 2. Atualizar Meu Nome

* **Endpoint:** `PATCH /api/me/name`
* **Descrição:** Atualiza o nome do usuário atualmente autenticado.
* **Corpo da Requisição (Request Body):**
    ```json
    {
      "nome": "string"
    }
    ```
* **Respostas:**
    * `200 OK`: Retorna o `UserDTO` atualizado.
    * `400 Bad Request`: Se o campo `nome` estiver ausente ou vazio.
    * `401 Unauthorized`: Se o usuário não estiver autenticado.

### 3. Enviar Meu Avatar

* **Endpoint:** `POST /api/me/avatar`
* **Descrição:** Envia uma nova imagem de avatar para o usuário.
* **Corpo da Requisição (Request Body):** `multipart/form-data` com uma única parte de arquivo chamada `avatar`.
* **Respostas:**
    * `200 OK`: No processamento bem-sucedido do upload.
    * `401 Unauthorized`: Se o usuário não estiver autenticado.

---

## API de Chat (`/api/chat`)

Gerencia salas de bate-papo e mensagens.

### 1. Criar Sala de Chat Direto

* **Endpoint:** `POST /api/chat/rooms/direct`
* **Descrição:** Cria uma nova sala de chat direta (1 para 1) com outro usuário.
* **Corpo da Requisição (Request Body):**
    ```json
    {
      "targetUserId": "long"
    }
    ```
* **Respostas:**
    * `201 Created`: Retorna o `ChatRoomDTO` recém-criado.
    * `400 Bad Request`: Se o usuário alvo for inválido ou vocês não forem amigos.
    * `404 Not Found`: Se o usuário alvo não existir.
    * `403 Forbidden`: Se você não for amigo do usuário alvo.

### 2. Criar Sala de Chat em Grupo

* **Endpoint:** `POST /api/chat/rooms/group`
* **Descrição:** Cria um novo chat em grupo com múltiplos participantes. O criador torna-se o administrador.
* **Corpo da Requisição (Request Body):**
    ```json
    {
      "name": "string",
      "participantIds": ["long"]
    }
    ```
* **Respostas:**
    * `201 Created`: Retorna o `ChatRoomDTO` recém-criado.
    * `400 Bad Request`: Se o nome do grupo estiver ausente ou houver menos de 2 participantes.
    * `404 Not Found`: Se um participante especificado não existir.

### 3. Listar Minhas Salas de Chat

* **Endpoint:** `GET /api/chat/rooms`
* **Descrição:** Recupera uma lista de todas as salas de chat (tanto diretas quanto em grupo) das quais o usuário atual participa.
* **Respostas:**
    * `200 OK`: Retorna uma lista de `ChatRoomDTO`.
        ```json
        [{
          "id": "long",
          "name": "string",
          "displayName": "string",
          "type": "DIRECT | GROUP",
          "adminId": "long",
          "participants": [{"id": "long", "nome": "string", "email": "string"}]
        }]
        ```

### 4. Listar Mensagens em uma Sala

* **Endpoint:** `GET /api/chat/rooms/{roomId}/messages`
* **Descrição:** Recupera todas as mensagens de uma sala de chat específica.
* **Respostas:**
    * `200 OK`: Retorna uma lista de `ChatMessageDTO`.
    * `403 Forbidden`: Se o usuário atual não for um participante da sala.
    * `404 Not Found`: Se a sala não existir.

### 5. Enviar Mensagem para uma Sala

* **Endpoint:** `POST /api/chat/rooms/{roomId}/messages`
* **Descrição:** Envia uma nova mensagem para uma sala de chat específica. Pode incluir conteúdo de texto e/ou um local favorito compartilhado.
* **Corpo da Requisição (Request Body):**
    ```json
    {
      "content": "string",
      "favoritePlaceId": "long" // (opcional)
    }
    ```
* **Respostas:**
    * `201 Created`: Retorna o `ChatMessageDTO` criado.
    * `400 Bad Request`: Se o conteúdo da mensagem estiver vazio (e nenhum favorito estiver anexado).
    * `403 Forbidden`: Se o usuário não for um participante ou tentar compartilhar um favorito que não possui.
    * `404 Not Found`: Se a sala ou o local favorito não existir.

---

## API de Favoritos (`/api/favorites`)

Gerencia os locais favoritos de um usuário.

### 1. Listar Meus Favoritos

* **Endpoint:** `GET /api/favorites`
* **Descrição:** Recupera uma lista de todos os locais favoritados pelo usuário atual.
* **Respostas:**
    * `200 OK`: Retorna uma lista de `FavoritePlaceDTO`.
        ```json
        [{
          "id": "long",
          "externalId": "string",
          "name": "string",
          "type": "string",
          "lat": "double",
          "lon": "double",
          "source": "string"
        }]
        ```

### 2. Favoritar um Local

* **Endpoint:** `POST /api/favorites`
* **Descrição:** Adiciona um novo local aos favoritos do usuário.
* **Corpo da Requisição (Request Body):** Um objeto `FavoritePlaceDTO`.
* **Respostas:**
    * `200 OK`: Retorna o `FavoritePlaceDTO` criado.

### 3. Remover Favorito de um Local

* **Endpoint:** `DELETE /api/favorites/{id}`
* **Descrição:** Remove um local dos favoritos do usuário.
* **Respostas:**
    * `204 No Content`: Na remoção bem-sucedida.
    * `403 Forbidden`: Se o usuário tentar deletar um favorito que não é seu.
    * `404 Not Found`: Se o favorito não existir.
    * `500 Internal Server Error`: Se o favorito não puder ser deletado devido a outras restrições (ex: fazer parte de uma sessão de votação ativa).

---

## API de Amigos (`/api/friends`)

Gerencia amizades e solicitações de amizade.

### 1. Obter Visão Geral de Amigos

* **Endpoint:** `GET /api/friends`
* **Descrição:** Recupera uma visão geral dos amigos do usuário e solicitações de amizade pendentes/enviadas.
* **Respostas:**
    * `200 OK`: Retorna um `FriendOverviewDTO`.

### 2. Pesquisar Usuários

* **Endpoint:** `GET /api/friends/search`
* **Descrição:** Pesquisa outros usuários por nome ou email para adicionar como amigos.
* **Parâmetros de Consulta (Query Parameters):** `query=string`
* **Respostas:**
    * `200 OK`: Retorna uma lista de `FriendCandidateDTO` com o status de amizade em relação ao usuário atual.

### 3. Enviar Solicitação de Amizade

* **Endpoint:** `POST /api/friends/requests`
* **Descrição:** Envia uma solicitação de amizade para outro usuário.
* **Corpo da Requisição (Request Body):**
    ```json
    {
      "targetUserId": "long"
    }
    ```
* **Respostas:**
    * `201 Created`: Retorna o `FriendRequestDTO` criado.
    * `400 Bad Request`: Se já houver uma solicitação pendente ou se eles já forem amigos.
    * `404 Not Found`: Se o usuário alvo não existir.

### 4. Remover Amigo

* **Endpoint:** `DELETE /api/friends/{friendId}`
* **Descrição:** Remove um amigo. Esta ação também deleta o chat direto e remove os usuários de grupos compartilhados onde um deles era o administrador.
* **Respostas:**
    * `204 No Content`: Na remoção bem-sucedida.
    * `404 Not Found`: Se o usuário amigo não existir.

### 5. Obter Solicitações de Amizade Recebidas

* **Endpoint:** `GET /api/friends/requests/incoming`
* **Descrição:** Obtém uma lista de solicitações de amizade pendentes recebidas pelo usuário.
* **Respostas:**
    * `200 OK`: Retorna uma lista de `FriendRequestDTO`.

### 6. Responder à Solicitação de Amizade

* **Endpoint:** `POST /api/friends/requests/{id}/respond`
* **Descrição:** Aceita ou rejeita uma solicitação de amizade pendente.
* **Corpo da Requisição (Request Body):**
    ```json
    {
      "action": "ACCEPT" | "REJECT"
    }
    ```
* **Respostas:**
    * `200 OK`: Retorna o `FriendRequestDTO` atualizado.
    * `400 Bad Request`: Se a ação for inválida ou a solicitação não estiver pendente.
    * `403 Forbidden`: Se o usuário não for o receptor da solicitação.

### 7. Aceitar Solicitação de Amizade

* **Endpoint:** `POST /api/friends/requests/{id}/accept`
* **Descrição:** Aceita uma solicitação de amizade pendente pelo seu ID.
* **Respostas:**
    * `200 OK`: Na aceitação bem-sucedida.
    * `400 Bad Request`: Se a solicitação não estiver pendente ou já tiver sido respondida.
    * `403 Forbidden`: Se o usuário atual não for o destinatário da solicitação.
    * `404 Not Found`: Se a solicitação de amizade não existir.

### 8. Rejeitar Solicitação de Amizade

* **Endpoint:** `POST /api/friends/requests/{id}/reject`
* **Descrição:** Rejeita uma solicitação de amizade pendente pelo seu ID.
* **Respostas:**
    * `200 OK`: Na rejeição bem-sucedida.
    * `400 Bad Request`: Se a solicitação não estiver pendente ou já tiver sido respondida.
    * `403 Forbidden`: Se o usuário atual não for o destinatário da solicitação.
    * `404 Not Found`: Se a solicitação de amizade não existir.

---

## API de Locais (`/api/places`)

Para descobrir locais no mapa.

### 1. Listar Locais

* **Endpoint:** `GET /api/places`
* **Descrição:** Encontra locais (restaurantes, bares, etc.) próximos a uma coordenada geográfica fornecida.
* **Parâmetros de Consulta (Query Parameters):**
    * `lat`: `double` (Latitude)
    * `lon`: `double` (Longitude)
    * `radius`: `integer` (Raio de busca em metros, padrão é 1000)
* **Respostas:**
    * `200 OK`: Retorna uma lista de `PlaceDTO`.

---

## API de Usuários (`/api`)

Endpoints gerais relacionados a usuários.

### 1. Obter Todos os Usuários

* **Endpoint:** `GET /api/users`
* **Descrição:** Recupera uma lista de todos os usuários no sistema.
* **Respostas:**
    * `200 OK`: Retorna uma lista de objetos `User`.

### 2. Obter Usuário por ID

* **Endpoint:** `GET /api/user/{uid}`
* **Descrição:** Recupera um usuário específico pelo seu ID.
* **Respostas:**
    * `200 OK`: Retorna um objeto `User`.
    * `404 Not Found`: Se o usuário com o ID fornecido não existir.

### 3. Deletar Usuário por ID

* **Endpoint:** `DELETE /api/user/{uid}`
* **Descrição:** Deleta um usuário específico pelo seu ID.
* **Respostas:**
    * `204 No Content`: Na exclusão bem-sucedida.

---

## API de Votação (`/api/voting`)

Gerencia sessões de votação por ordem de preferência (ranked-choice) dentro de chats em grupo.

### 1. Criar Sessão de Votação

* **Endpoint:** `POST /api/voting/sessions`
* **Descrição:** Cria uma nova sessão de votação em um chat de grupo com uma lista de locais favoritos como opções.
* **Corpo da Requisição (Request Body):**
    ```json
    {
      "roomId": "long",
      "favoritePlaceIds": ["long"]
    }
    ```
* **Respostas:**
    * `201 Created`: Retorna o `VotingSessionDTO` recém-criado.
    * `400 Bad Request`: Se houver menos de duas opções ou uma sessão já estiver aberta.
    * `403 Forbidden`: Se o usuário não for o administrador do grupo.
    * `404 Not Found`: Se a sala ou um local favorito não existir.

### 2. Enviar Voto

* **Endpoint:** `POST /api/voting/sessions/{sessionId}/votes`
* **Descrição:** Envia o voto ranqueado de um usuário para uma sessão de votação aberta.
* **Corpo da Requisição (Request Body):**
    ```json
    {
      "orderedOptionIds": ["long"]
    }
    ```
* **Respostas:**
    * `200 OK`: No envio de voto bem-sucedido.
    * `400 Bad Request`: Se o ranking for inválido (ex: opções ausentes, duplicatas).
    * `403 Forbidden`: Se o usuário não fizer parte do grupo.
    * `404 Not Found`: Se a sessão não existir.

### 3. Fechar Sessão de Votação

* **Endpoint:** `POST /api/voting/sessions/{sessionId}/close`
* **Descrição:** Fecha uma sessão de votação aberta, calcula o vencedor usando votação por ordem de preferência e retorna o resultado final.
* **Respostas:**
    * `200 OK`: Retorna o `VotingSessionDTO` atualizado com o vencedor incluído.
    * `403 Forbidden`: Se o usuário não for o administrador do grupo.
    * `404 Not Found`: Se a sessão não existir.

### 4. Obter Detalhes da Sessão de Votação

* **Endpoint:** `GET /api/voting/sessions/{sessionId}`
* **Descrição:** Recupera os detalhes e o status de uma sessão de votação específica.
* **Respostas:**
    * `200 OK`: Retorna um `VotingSessionDTO`.
    * `403 Forbidden`: Se o usuário não fizer parte do grupo.
    * `404 Not Found`: Se a sessão não existir.

### 5. Obter Sessão de Votação Atual para uma Sala

* **Endpoint:** `GET /api/voting/rooms/{roomId}/current`
* **Descrição:** Recupera a sessão de votação atualmente ativa (`OPEN`) para uma sala de chat de grupo específica.
* **Respostas:**
    * `200 OK`: Retorna um `VotingSessionDTO`.
    * `404 Not Found`: Se não houver sessão de votação ativa para a sala.