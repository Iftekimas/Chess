package com.chess.model;

public enum PlayerColor {
    WHITE("white"),
    BLACK("black");

    private final String value;

    PlayerColor(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public PlayerColor opposite() {
        return this == WHITE ? BLACK : WHITE;
    }

    @Override
    public String toString() {
        return value;
    }

    public static PlayerColor fromString(String color) {
        for (PlayerColor playerColor : values()) {
            if (playerColor.value.equals(color)) {
                return playerColor;
            }
        }
        throw new IllegalArgumentException("Color inválido: " + color);
    }
}
