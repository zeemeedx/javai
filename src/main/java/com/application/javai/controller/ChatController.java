package com.application.javai.controller;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.application.javai.dto.ChatMessageDTO;
import com.application.javai.dto.ChatRoomDTO;
import com.application.javai.dto.CreateDirectRoomRequest;
import com.application.javai.dto.CreateGroupRoomRequest;
import com.application.javai.dto.SendMessageRequest;
import com.application.javai.service.ChatService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/rooms/direct")
    public ResponseEntity<?> criarRoomDireto(@RequestBody CreateDirectRoomRequest request) {
        try {
            ChatRoomDTO dto = chatService.criarRoomDirect(request.targetUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/rooms/group")
    public ResponseEntity<?> criarRoomGrupo(@RequestBody CreateGroupRoomRequest request) {
        try {
            ChatRoomDTO dto = chatService.criarRoomGroup(request.name(), request.participantIds());
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/rooms")
    public List<ChatRoomDTO> listarRooms() {
        return chatService.listarRoomsDoUsuarioLogado();
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<?> listarMensagens(@PathVariable Long roomId) {
        try {
            List<ChatMessageDTO> mensagens = chatService.listarMensagens(roomId);
            return ResponseEntity.ok(mensagens);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<?> enviarMensagem(@PathVariable Long roomId,
                                            @RequestBody SendMessageRequest request) {
        try {
            ChatMessageDTO dto = chatService.enviarMensagem(roomId, request.content(), request.favoritePlaceId());
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}
