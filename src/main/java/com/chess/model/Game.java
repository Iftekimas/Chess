package com.chess.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;

@Entity
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
        this.status = "ongoing";
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

}