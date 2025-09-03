package com.chess.model;

public enum PlayerColor {
    WHITE("WHITE"),
    BLACK("BLACK");

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
        if (color == null) {
            throw new IllegalArgumentException("El color no puede ser null");
        }

        for (PlayerColor playerColor : values()) {
            if (playerColor.value.equalsIgnoreCase(color.trim())) {
                return playerColor;
            }
        }
        throw new IllegalArgumentException("El color solo puede ser 'WHITE' o 'BLACK'");
    }
}
