package com.chess.controller;

import com.chess.model.Game;
import com.chess.model.GameConstants;
import com.chess.service.ChessGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/games")
public class GameController {

    @Autowired
    private ChessGameService chessGameService;

    @PostMapping("/{gameId}/move/pgn")
    public ResponseEntity<String> makeMovePgn(@PathVariable Long gameId, @RequestParam String move) {
        try {
            Game game = chessGameService.applyMove(gameId, move);

            // Si el juego terminó por timeout
            if ("timeout".equals(game.getStatus())) {
                String timeoutMessage = game.getWhiteClock() <= 0
                        ? GameConstants.MSG_TIMEOUT_BLACK_WINS
                        : GameConstants.MSG_TIMEOUT_WHITE_WINS;
                return ResponseEntity.ok(timeoutMessage);
            }

            // Construir respuesta normal
            String response = buildGameResponse(game);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error inesperado: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<Game> createGame(@RequestParam String playerColor,
            @RequestParam(defaultValue = "300") int whiteClock,
            @RequestParam(defaultValue = "300") int blackClock) {
        try {
            Game game = chessGameService.createGame(playerColor, whiteClock, blackClock);
            return ResponseEntity.ok(game);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Game> getGame(@PathVariable Long id) {
        Optional<Game> game = chessGameService.findGame(id);
        return game.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/moves")
    public ResponseEntity<List<String>> getMoves(@PathVariable Long id) {
        try {
            List<String> moves = chessGameService.getGameMoves(id);
            return ResponseEntity.ok(moves);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    // Método privado para construir respuesta
    private String buildGameResponse(Game game) {
        StringBuilder response = new StringBuilder();

        // Mensaje según el estado del juego
        String statusMessage = getStatusMessage(game);
        response.append(statusMessage).append("\n");

        // Información del juego
        response.append("PGN: ").append(game.getPgn()).append("\n");
        response.append("Tiempo blanco: ").append(game.getWhiteClock()).append(" segundos\n");
        response.append("Tiempo negro: ").append(game.getBlackClock()).append(" segundos\n");
        response.append("Turno: ").append(game.getTurn()).append("\n");
        response.append("Estado: ").append(game.getStatus()).append("\n");

        return response.toString();
    }

    private String getStatusMessage(Game game) {
        switch (game.getStatus()) {
            case "mate":
                return game.getWinner().name().equals("WHITE")
                        ? GameConstants.MSG_CHECKMATE_WHITE_WINS
                        : GameConstants.MSG_CHECKMATE_BLACK_WINS;
            case "stalemate":
                return GameConstants.MSG_STALEMATE;
            case "draw":
                return GameConstants.MSG_DRAW;
            case "timeout":
                return game.getWhiteClock() <= 0
                        ? GameConstants.MSG_TIMEOUT_BLACK_WINS
                        : GameConstants.MSG_TIMEOUT_WHITE_WINS;
            case "active":
            default:
                return GameConstants.MSG_MOVE_SUCCESS;
        }
    }
}