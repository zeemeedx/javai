package com.application.javai.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.application.javai.dto.ChatMessageDTO;
import com.application.javai.dto.ChatRoomDTO;
import com.application.javai.dto.FavoritePlaceDTO;
import com.application.javai.dto.UserSummaryDTO;
import com.application.javai.model.ChatMessage;
import com.application.javai.model.ChatRoom;
import com.application.javai.model.ChatRoomType;
import com.application.javai.model.FavoritePlace;
import com.application.javai.model.User;
import com.application.javai.repository.ChatMessageRepository;
import com.application.javai.repository.ChatRoomRepository;
import com.application.javai.repository.FavoritePlaceRepository;
import com.application.javai.repository.FriendshipRepository;
import com.application.javai.repository.UserRepository;

@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final FavoritePlaceRepository favoritePlaceRepository;
    private final FriendshipRepository friendshipRepository;

    public ChatService(ChatRoomRepository chatRoomRepository,
                       ChatMessageRepository chatMessageRepository,
                       UserRepository userRepository,
                       FavoritePlaceRepository favoritePlaceRepository,
                       FriendshipRepository friendshipRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.favoritePlaceRepository = favoritePlaceRepository;
        this.friendshipRepository = friendshipRepository;
    }

    @Transactional
    public ChatRoomDTO criarRoomDirect(Long targetUserId) {
        User currentUser = getAuthenticatedUser();
        if (targetUserId == null || currentUser.getId().equals(targetUserId)) {
            throw new IllegalArgumentException("Usuário alvo inválido.");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NoSuchElementException("Usuário alvo não encontrado."));

        boolean saoAmigos = friendshipRepository.existsByUserAndFriend(currentUser, targetUser)
            || friendshipRepository.existsByUserAndFriend(targetUser, currentUser);

        if (!saoAmigos) {
            throw new IllegalStateException("Vocês ainda não são amigos.");
        }

        return chatRoomRepository
                .findDirectRoomBetween(currentUser, targetUser, ChatRoomType.DIRECT)
                .stream()
                .findFirst()
                .map(room -> toRoomDTO(room, currentUser))
                .orElseGet(() -> {
                    ChatRoom room = new ChatRoom();
                    room.setType(ChatRoomType.DIRECT);
                    room.setName(buildDirectRoomName(currentUser, targetUser));
                    room.getParticipants().add(currentUser);
                    room.getParticipants().add(targetUser);
                    ChatRoom saved = chatRoomRepository.save(room);
                    return toRoomDTO(saved, currentUser);
                });
    }

    @Transactional
    public ChatRoomDTO criarRoomGroup(String name, List<Long> participantIds) {
        User admin = getAuthenticatedUser();
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da sala é obrigatório.");
        }

        Set<User> participants = new LinkedHashSet<>();
        participants.add(admin);

        if (participantIds != null) {
            participantIds.stream()
                    .filter(id -> id != null && !id.equals(admin.getId()))
                    .map(id -> userRepository.findById(id)
                            .orElseThrow(() -> new NoSuchElementException("Participante não encontrado: " + id)))
                    .forEach(participants::add);
        }

        if (participants.size() < 2) {
            throw new IllegalArgumentException("Uma sala precisa de pelo menos duas pessoas.");
        }

        ChatRoom room = new ChatRoom();
        room.setName(name.trim());
        room.setType(ChatRoomType.GROUP);
        room.setAdmin(admin);
        room.setParticipants(participants);

        ChatRoom saved = chatRoomRepository.save(room);
        return toRoomDTO(saved, admin);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomDTO> listarRoomsDoUsuarioLogado() {
        User currentUser = getAuthenticatedUser();
        return chatRoomRepository.findAllByParticipant(currentUser)
                .stream()
                .map(room -> toRoomDTO(room, currentUser))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDTO> listarMensagens(Long roomId) {
        User currentUser = getAuthenticatedUser();
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("Sala não encontrada."));
        ensureParticipant(room, currentUser);

        return chatMessageRepository.findByRoomOrderByCreatedAtAsc(room)
                .stream()
                .map(this::toMessageDTO)
                .toList();
    }

    @Transactional
    public ChatMessageDTO enviarMensagem(Long roomId, String content, Long favoritePlaceId) {
        User currentUser = getAuthenticatedUser();
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("Sala não encontrada."));
        ensureParticipant(room, currentUser);

        FavoritePlace favoritePlace = null;
        if (favoritePlaceId != null) {
            favoritePlace = favoritePlaceRepository.findById(favoritePlaceId)
                    .orElseThrow(() -> new NoSuchElementException("Favorito não encontrado."));
            if (!favoritePlace.getUser().getId().equals(currentUser.getId())) {
                throw new SecurityException("Você não pode compartilhar este favorito.");
            }
        }

        String sanitizedContent = content == null ? "" : content.trim();
        if ((sanitizedContent.isEmpty()) && favoritePlace == null) {
            throw new IllegalArgumentException("Mensagem vazia.");
        }

        ChatMessage message = new ChatMessage();
        message.setRoom(room);
        message.setSender(currentUser);
        message.setContent(sanitizedContent);
        message.setFavoritePlace(favoritePlace);

        ChatMessage saved = chatMessageRepository.save(message);
        return toMessageDTO(saved);
    }

    private String buildDirectRoomName(User current, User target) {
        String currentName = current.getNome() != null ? current.getNome() : "Você";
        String targetName = target.getNome() != null ? target.getNome() : target.getEmail();
        return currentName + " & " + targetName;
    }

    private void ensureParticipant(ChatRoom room, User user) {
        boolean isParticipant = room.getParticipants().stream()
                .anyMatch(participant -> participant.getId().equals(user.getId()));
        if (!isParticipant) {
            throw new SecurityException("Você não participa desta sala.");
        }
    }

    private ChatRoomDTO toRoomDTO(ChatRoom room, User currentUser) {
        String displayName = resolveDisplayName(room, currentUser);
        Long adminId = room.getAdmin() != null ? room.getAdmin().getId() : null;
        List<UserSummaryDTO> participants = room.getParticipants()
                .stream()
                .map(this::toUserSummary)
                .toList();
        return new ChatRoomDTO(
                room.getId(),
                room.getName(),
                displayName,
                room.getType(),
                adminId,
                participants
        );
    }

    private String resolveDisplayName(ChatRoom room, User currentUser) {
        if (room.getType() == ChatRoomType.DIRECT) {
            return room.getParticipants()
                    .stream()
                    .filter(participant -> !participant.getId().equals(currentUser.getId()))
                    .map(user -> user.getNome() != null ? user.getNome() : user.getEmail())
                    .findFirst()
                    .orElse(room.getName());
        }
        return room.getName();
    }

    private ChatMessageDTO toMessageDTO(ChatMessage message) {
        FavoritePlaceDTO favorite = message.getFavoritePlace() != null
                ? toFavoritePlaceDTO(message.getFavoritePlace())
                : null;
        return new ChatMessageDTO(
                message.getId(),
                message.getRoom().getId(),
                toUserSummary(message.getSender()),
                message.getContent(),
                message.getCreatedAt(),
                favorite
        );
    }

    private FavoritePlaceDTO toFavoritePlaceDTO(FavoritePlace favorite) {
        return new FavoritePlaceDTO(
                favorite.getId(),
                favorite.getExternalId(),
                favorite.getName(),
                favorite.getType(),
                favorite.getLat(),
                favorite.getLon(),
                favorite.getSource()
        );
    }

    private UserSummaryDTO toUserSummary(User user) {
        return new UserSummaryDTO(
                user.getId(),
                user.getNome(),
                user.getEmail()
        );
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
}
