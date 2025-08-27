package com.chess.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;

@Entity
public class Game {
    private String fen;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Winner winner = Winner.NONE;
    private Instant lastMoveTimestamp;
    private String turn;
    private String playerColor;
    private int whiteClock;
    private int blackClock;
    private String status;
    private LocalDateTime createdAt;

    // Nueva lista de movimientos
    @ElementCollection
    private List<String> moves = new ArrayList<>();

    public Game() {
        this.createdAt = LocalDateTime.now();
        this.status = "active";
    }

    // Getters y Setters (agrega el de moves)
    public Long getId() {
        return id;
    }

    public String getPlayerColor() {
        return playerColor;
    }

    public void setPlayerColor(String playerColor) {
        this.playerColor = playerColor;
    }

    public int getWhiteClock() {
        return whiteClock;
    }

    public void setWhiteClock(int whiteClock) {
        this.whiteClock = whiteClock;
    }

    public int getBlackClock() {
        return blackClock;
    }

    public void setBlackClock(int blackClock) {
        this.blackClock = blackClock;
    }

    public String getStatus() {
        return status;
    }

    public String getFen() {
        return fen;
    }

    public void setFen(String fen) {
        this.fen = fen;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getMoves() {
        return moves;
    }

    public void setMoves(List<String> moves) {
        this.moves = moves;
    }

    public String getTurn() {
        return turn;
    }

    public void setTurn(String turn) {
        this.turn = turn;
    }

    public Instant getLastMoveTimestamp() {
        return lastMoveTimestamp;
    }

    public void setLastMoveTimestamp(Instant lastMoveTimestamp) {
        this.lastMoveTimestamp = lastMoveTimestamp;
    }

    public Winner getWinner() {
        return winner;
    }

    public void setWinner(Winner winner) {
        this.winner = winner;
    }

}