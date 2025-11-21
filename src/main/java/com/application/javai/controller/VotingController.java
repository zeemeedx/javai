package com.application.javai.controller;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.application.javai.dto.CreateVotingSessionRequest;
import com.application.javai.dto.RankingVoteRequest;
import com.application.javai.dto.VotingSessionDTO;
import com.application.javai.service.VotingService;

@RestController
@RequestMapping("/api/voting")
public class VotingController {

    private final VotingService votingService;

    public VotingController(VotingService votingService) {
        this.votingService = votingService;
    }

    @PostMapping("/sessions")
    public ResponseEntity<?> abrirSessao(@RequestBody CreateVotingSessionRequest request) {
        try {
            VotingSessionDTO dto = votingService.abrirSessao(request.roomId(), request.favoritePlaceIds());
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/sessions/{sessionId}/votes")
    public ResponseEntity<?> registrarVoto(@PathVariable Long sessionId,
                                           @RequestBody RankingVoteRequest request) {
        try {
            votingService.registrarVoto(sessionId, request.orderedOptionIds());
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/sessions/{sessionId}/close")
    public ResponseEntity<?> encerrarSessao(@PathVariable Long sessionId) {
        try {
            VotingSessionDTO dto = votingService.encerrarSessao(sessionId);
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<?> detalhes(@PathVariable Long sessionId) {
        try {
            VotingSessionDTO dto = votingService.detalhesSessao(sessionId);
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/rooms/{roomId}/current")
    public ResponseEntity<?> sessaoAbertaDoGrupo(@PathVariable Long roomId) {
        try {
            VotingSessionDTO dto = votingService.sessaoAbertaDoGrupo(roomId);
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}
