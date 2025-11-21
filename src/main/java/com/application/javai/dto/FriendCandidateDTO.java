package com.application.javai.dto;

/**
 * Representa um usuário retornado na busca de amigos,
 * com o status da relação com o usuário logado.
 *
 * status pode ser:
 * - "NONE"            → sem relação
 * - "FRIEND"          → já são amigos
 * - "PENDING_SENT"    → você já enviou um pedido (pendente)
 * - "PENDING_RECEIVED"→ a outra pessoa enviou um pedido pra você
 * - "REJECTED"        → pedido anterior foi recusado (pode reenviar)
 */
public record FriendCandidateDTO(
        Long id,
        String nome,
        String email,
        String status
) {}
