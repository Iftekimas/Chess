package com.chess.model;

public final class GameConstants {

    // Estados del juego
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_MATE = "mate";
    public static final String STATUS_STALEMATE = "stalemate";
    public static final String STATUS_DRAW = "draw";
    public static final String STATUS_TIMEOUT = "timeout";

    // Colores de jugadores
    public static final String COLOR_WHITE = "WHITE";
    public static final String COLOR_BLACK = "BLACK";

    // Valores por defecto
    public static final int DEFAULT_CLOCK_SECONDS = 300;
    public static final String EMPTY_PGN = "";

    // Mensajes
    public static final String MSG_EMPTY_MOVE = "El movimiento no puede estar vacío";
    public static final String MSG_GAME_NOT_ACTIVE = "La partida ya no está activa.";
    public static final String MSG_GAME_NOT_FOUND = "Partida no encontrada";
    public static final String MSG_INVALID_COLOR = "El color solo puede ser 'WHITE' o 'BLACK'";
    public static final String MSG_INVALID_TIME = "El tiempo debe ser positivo";
    public static final String MSG_ILLEGAL_MOVE = "Movimiento no válido: ";
    public static final String MSG_CHECKMATE_WHITE_WINS = "Jaque mate. Ganador: Blanco";
    public static final String MSG_CHECKMATE_BLACK_WINS = "Jaque mate. Ganador: Negro";
    public static final String MSG_STALEMATE = "Rey ahogado (Stalemate). Tablas";
    public static final String MSG_DRAW = "Tablas por repetición, material insuficiente o regla de los 50 movimientos";
    public static final String MSG_CHECK = "Jaque al rey";
    public static final String MSG_MOVE_SUCCESS = "Movimiento realizado correctamente.";
    public static final String MSG_TIMEOUT_BLACK_WINS = "Timeout: gana negro";
    public static final String MSG_TIMEOUT_WHITE_WINS = "Timeout: gana blanco";

    private GameConstants() {
        // Utility class - no instances
    }
}
