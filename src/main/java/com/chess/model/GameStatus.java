package com.chess.model;

public enum GameStatus {
    ACTIVE("active"),
    MATE("mate"),
    STALEMATE("stalemate"),
    DRAW("draw"),
    TIMEOUT("timeout");

    private final String value;

    GameStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
