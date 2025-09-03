package com.chess.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;

@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String pgn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Winner winner = Winner.NONE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status = GameStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerColor playerColor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerColor turn = PlayerColor.WHITE;

    private Instant lastMoveTimestamp;

    @Column(nullable = false)
    private int whiteClock;

    @Column(nullable = false)
    private int blackClock;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "game_moves", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "move_notation")
    private List<String> moves = new ArrayList<>();

    public Game() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructor con parámetros para mejor inicialización
    public Game(PlayerColor playerColor, int whiteClock, int blackClock) {
        this();
        this.playerColor = playerColor;
        this.whiteClock = whiteClock;
        this.blackClock = blackClock;
        this.pgn = GameConstants.EMPTY_PGN;
    }

    // Métodos de utilidad
    public boolean isActive() {
        return status == GameStatus.ACTIVE;
    }

    public boolean isGameOver() {
        return status != GameStatus.ACTIVE;
    }

    public void addMove(String move) {
        if (moves == null) {
            moves = new ArrayList<>();
        }
        moves.add(move);
    }

    public void switchTurn() {
        this.turn = this.turn.opposite();
    }

    public int getCurrentPlayerClock() {
        return turn == PlayerColor.WHITE ? whiteClock : blackClock;
    }

    public void updateClock(long secondsElapsed) {
        if (turn == PlayerColor.WHITE) {
            whiteClock = Math.max(0, whiteClock - (int) secondsElapsed);
        } else {
            blackClock = Math.max(0, blackClock - (int) secondsElapsed);
        }
    }

    public boolean isTimedOut() {
        return whiteClock <= 0 || blackClock <= 0;
    }

    // Getters y Setters con compatibilidad String
    public Long getId() {
        return id;
    }

    public String getPlayerColor() {
        return playerColor != null ? playerColor.getValue() : null;
    }

    public void setPlayerColor(String playerColor) {
        this.playerColor = PlayerColor.fromString(playerColor);
    }

    public PlayerColor getPlayerColorEnum() {
        return playerColor;
    }

    public void setPlayerColorEnum(PlayerColor playerColor) {
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
        return status != null ? status.getValue() : null;
    }

    public String getPgn() {
        return pgn;
    }

    public void setPgn(String pgn) {
        this.pgn = pgn;
    }

    public void setStatus(String status) {
        switch (status.toLowerCase()) {
            case "active":
                this.status = GameStatus.ACTIVE;
                break;
            case "mate":
                this.status = GameStatus.MATE;
                break;
            case "stalemate":
                this.status = GameStatus.STALEMATE;
                break;
            case "draw":
                this.status = GameStatus.DRAW;
                break;
            case "timeout":
                this.status = GameStatus.TIMEOUT;
                break;
            default:
                throw new IllegalArgumentException("Estado inválido: " + status);
        }
    }

    public GameStatus getStatusEnum() {
        return status;
    }

    public void setStatusEnum(GameStatus status) {
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
        return turn != null ? turn.getValue() : null;
    }

    public void setTurn(String turn) {
        this.turn = PlayerColor.fromString(turn);
    }

    public PlayerColor getTurnEnum() {
        return turn;
    }

    public void setTurnEnum(PlayerColor turn) {
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