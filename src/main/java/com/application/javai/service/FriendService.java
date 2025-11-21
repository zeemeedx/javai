package com.application.javai.service;

import com.application.javai.dto.FriendCandidateDTO;
import com.application.javai.dto.FriendOverviewDTO;
import com.application.javai.dto.FriendRequestDTO;
import com.application.javai.dto.UserSummaryDTO;
import com.application.javai.model.FriendRequest;
import com.application.javai.model.FriendRequestStatus;
import com.application.javai.model.Friendship;
import com.application.javai.model.User;
import com.application.javai.repository.FriendRequestRepository;
import com.application.javai.repository.FriendshipRepository;
import com.application.javai.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

@Service
public class FriendService {

    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    public FriendService(FriendRequestRepository friendRequestRepository,
                         UserRepository userRepository,
                         FriendshipRepository friendshipRepository) {
        this.friendRequestRepository = friendRequestRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    // ------------------ Visão geral (friends + pendentes) ------------------

    @Transactional(readOnly = true)
    public FriendOverviewDTO getOverview(User currentUser) {
        // Amigos = todos requests ACCEPTED onde currentUser é requester ou receiver
        List<FriendRequest> accepted = friendRequestRepository
                .findAllByUserAndStatus(currentUser, FriendRequestStatus.ACCEPTED);

        Set<User> friends = new LinkedHashSet<>();
        for (FriendRequest fr : accepted) {
            if (fr.getRequester().equals(currentUser)) {
                friends.add(fr.getReceiver());
            } else {
                friends.add(fr.getRequester());
            }
        }

        List<UserSummaryDTO> friendsDto = friends.stream()
                .map(this::toUserSummary)
                .toList();

        // Pedidos pendentes recebidos
        List<FriendRequest> incoming = friendRequestRepository
                .findByReceiverAndStatusOrderByCreatedAtDesc(
                        currentUser,
                        FriendRequestStatus.PENDING
                );

        List<FriendRequest> outgoing = friendRequestRepository
                .findByRequesterAndStatusOrderByCreatedAtDesc(
                        currentUser,
                        FriendRequestStatus.PENDING
                );

        List<FriendRequestDTO> incomingDto = incoming.stream()
                .map(this::toFriendRequestDTO)
                .toList();

        List<FriendRequestDTO> outgoingDto = outgoing.stream()
                .map(this::toFriendRequestDTO)
                .toList();

        return new FriendOverviewDTO(friendsDto, incomingDto, outgoingDto);
    }

    // ------------------ Busca de usuários para adicionar ------------------

    @Transactional(readOnly = true)
    public List<FriendCandidateDTO> searchUsers(User currentUser, String query) {
        String term = query == null ? "" : query.trim();
        if (term.isEmpty()) {
            return List.of();
        }

        List<User> found = userRepository.searchByNameOrEmail(term);

        return found.stream()
                .filter(u -> !u.getId().equals(currentUser.getId()))
                .map(u -> {
                    String status = relationshipStatus(currentUser, u);
                    return new FriendCandidateDTO(
                            u.getId(),
                            u.getNome(),
                            u.getEmail(),
                            status
                    );
                })
                .toList();
    }

    // ------------------ Enviar pedido de amizade ------------------

    @Transactional
    public FriendRequestDTO sendFriendRequest(User currentUser, Long targetUserId) {
        if (currentUser.getId().equals(targetUserId)) {
            throw new IllegalArgumentException("Você não pode enviar pedido de amizade para si mesmo.");
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NoSuchElementException("Usuário alvo não encontrado."));

        // Verifica pedidos anteriores entre os dois
        List<FriendRequest> history = friendRequestRepository
                .findAllBetweenUsers(currentUser, target);

        Optional<FriendRequest> last = history.stream().findFirst();

        if (last.isPresent()) {
            FriendRequest lastReq = last.get();
            if (lastReq.getStatus() == FriendRequestStatus.ACCEPTED) {
                throw new IllegalStateException("Vocês já são amigos.");
            }
            if (lastReq.getStatus() == FriendRequestStatus.PENDING) {
                throw new IllegalStateException("Já existe um pedido de amizade pendente entre vocês.");
            }
            // Se REJECTED → permitimos criar NOVO pedido
        }

        FriendRequest fr = new FriendRequest();
        fr.setRequester(currentUser);
        fr.setReceiver(target);
        fr.setStatus(FriendRequestStatus.PENDING);

        FriendRequest saved = friendRequestRepository.save(fr);
        return toFriendRequestDTO(saved);
    }

    // ------------------ Responder pedido (aceitar / recusar) ------------------

    @Transactional
    public FriendRequestDTO respondToRequest(User currentUser, Long requestId, boolean accept) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Pedido de amizade não encontrado."));

        if (!request.getReceiver().getId().equals(currentUser.getId())) {
            throw new SecurityException("Você não pode responder pedidos que não são endereçados a você.");
        }

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalStateException("Este pedido já foi respondido.");
        }

        request.setStatus(accept ? FriendRequestStatus.ACCEPTED : FriendRequestStatus.REJECTED);
        FriendRequest saved = friendRequestRepository.save(request);

        return toFriendRequestDTO(saved);
    }

    // ------------------ Pendentes recebidos (para os pop-ups) ------------------

    @Transactional(readOnly = true)
    public List<FriendRequestDTO> findIncomingPending(User currentUser) {
        return friendRequestRepository
                .findByReceiverAndStatusOrderByCreatedAtDesc(
                        currentUser,
                        FriendRequestStatus.PENDING
                )
                .stream()
                .map(this::toFriendRequestDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FriendRequestDTO> listarPedidosRecebidosDoUsuarioLogado() {
        User currentUser = getAuthenticatedUser();
        return friendRequestRepository
                .findByRequestedIdAndStatus(currentUser.getId(), FriendRequestStatus.PENDING)
                .stream()
                .map(this::toFriendRequestDTO)
                .toList();
    }

    @Transactional
    public void responderPedido(Long requestId, boolean aceitar) {
        User currentUser = getAuthenticatedUser();
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Pedido de amizade não encontrado."));

        if (!request.getReceiver().getId().equals(currentUser.getId())) {
            throw new SecurityException("Somente o destinatário pode responder este pedido.");
        }

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalStateException("Este pedido já foi respondido.");
        }

        if (aceitar) {
            request.setStatus(FriendRequestStatus.ACCEPTED);
            friendRequestRepository.save(request);
            createFriendships(request.getRequester(), currentUser);
        } else {
            request.setStatus(FriendRequestStatus.REJECTED);
            friendRequestRepository.save(request);
        }
    }

    @Transactional
    public void removerAmigo(Long friendUserId) {
        User currentUser = getAuthenticatedUser();
        if (currentUser.getId().equals(friendUserId)) {
            throw new IllegalArgumentException("Não é possível remover a si mesmo.");
        }

        userRepository.findById(friendUserId)
                .orElseThrow(() -> new NoSuchElementException("Usuário amigo não encontrado."));

        friendshipRepository.deleteByUserIdAndFriendId(currentUser.getId(), friendUserId);
        friendshipRepository.deleteByUserIdAndFriendId(friendUserId, currentUser.getId());
    }

    // ------------------ Helpers ------------------

    private UserSummaryDTO toUserSummary(User u) {
        return new UserSummaryDTO(u.getId(), u.getNome(), u.getEmail());
    }

    private FriendRequestDTO toFriendRequestDTO(FriendRequest fr) {
        return new FriendRequestDTO(
                fr.getId(),
                toUserSummary(fr.getRequester()),
                toUserSummary(fr.getReceiver()),
                fr.getStatus().name()
        );
    }

    /**
     * Retorna o status de relacionamento entre currentUser e outro:
     * NONE, FRIEND, PENDING_SENT, PENDING_RECEIVED, REJECTED
     */
    private String relationshipStatus(User currentUser, User other) {
        List<FriendRequest> history = friendRequestRepository
                .findAllBetweenUsers(currentUser, other);

        if (history.isEmpty()) {
            return "NONE";
        }

        FriendRequest last = history.get(0); // mais recente (ordem desc)

        if (last.getStatus() == FriendRequestStatus.ACCEPTED) {
            return "FRIEND";
        }
        if (last.getStatus() == FriendRequestStatus.PENDING) {
            if (last.getRequester().equals(currentUser)) {
                return "PENDING_SENT";
            } else {
                return "PENDING_RECEIVED";
            }
        }
        // REJECTED
        return "REJECTED";
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Usuário não autenticado.");
        }

        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (principal instanceof User user) {
            username = user.getEmail();
        } else if (principal instanceof String str) {
            username = str;
        } else {
            throw new IllegalStateException("Principal de autenticação inválido.");
        }

        return userRepository.findByEmail(username)
                .orElseThrow(() -> new NoSuchElementException("Usuário autenticado não encontrado."));
    }

    private void createFriendships(User requester, User receiver) {
        if (!friendshipRepository.existsByUserAndFriend(requester, receiver)) {
            Friendship friendship = new Friendship(requester, receiver);
            friendshipRepository.save(friendship);
        }

        if (!friendshipRepository.existsByUserAndFriend(receiver, requester)) {
            Friendship friendship = new Friendship(receiver, requester);
            friendshipRepository.save(friendship);
        }
    }
}
