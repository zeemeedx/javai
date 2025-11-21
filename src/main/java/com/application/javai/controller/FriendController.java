package com.application.javai.controller;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.application.javai.dto.FriendCandidateDTO;
import com.application.javai.dto.FriendOverviewDTO;
import com.application.javai.dto.FriendRequestDTO;
import com.application.javai.model.User;
import com.application.javai.repository.UserRepository;
import com.application.javai.service.FriendService;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;
    private final UserRepository userRepository;

    public FriendController(FriendService friendService,
                            UserRepository userRepository) {
        this.friendService = friendService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("Usuário não autenticado.");
        }
        String email = userDetails.getUsername();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado."));
    }

    // GET /api/friends → overview (amigos + pendentes)
    @GetMapping
    public FriendOverviewDTO getOverview(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        return friendService.getOverview(currentUser);
    }

    // GET /api/friends/search?query=xxx → busca de usuários para adicionar
    @GetMapping("/search")
    public List<FriendCandidateDTO> search(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("query") String query) {
        User currentUser = getCurrentUser(userDetails);
        return friendService.searchUsers(currentUser, query);
    }

    // POST /api/friends/requests → enviar pedido
    // body: { "targetUserId": 123 }
    public static class SendRequestBody {
        private Long targetUserId;
        public Long getTargetUserId() { return targetUserId; }
        public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
    }

    @PostMapping("/requests")
    public ResponseEntity<?> sendRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody SendRequestBody body) {
        try {
            User currentUser = getCurrentUser(userDetails);
            FriendRequestDTO dto = friendService.sendFriendRequest(currentUser, body.getTargetUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // GET /api/friends/requests/incoming → pendentes recebidos (para pop-ups)
    @GetMapping("/requests/incoming")
    public List<FriendRequestDTO> incoming() {
        return friendService.listarPedidosRecebidosDoUsuarioLogado();
    }

    // POST /api/friends/requests/{id}/respond → aceitar/recusar
    // body: { "action": "ACCEPT" } ou { "action": "REJECT" }
    public static class RespondBody {
        private String action;
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
    }

    @PostMapping("/requests/{id}/respond")
    public ResponseEntity<?> respond(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody RespondBody body) {

        User currentUser = getCurrentUser(userDetails);
        boolean accept;

        if ("ACCEPT".equalsIgnoreCase(body.getAction())) {
            accept = true;
        } else if ("REJECT".equalsIgnoreCase(body.getAction())) {
            accept = false;
        } else {
            return ResponseEntity.badRequest().body("Ação inválida. Use ACCEPT ou REJECT.");
        }

        try {
            FriendRequestDTO dto = friendService.respondToRequest(currentUser, id, accept);
            return ResponseEntity.ok(dto);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/requests/{id}/accept")
    public ResponseEntity<?> acceptRequest(@PathVariable Long id) {
        try {
            friendService.responderPedido(id, true);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/requests/{id}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable Long id) {
        try {
            friendService.responderPedido(id, false);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
