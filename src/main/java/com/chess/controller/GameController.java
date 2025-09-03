package com.chess.controller;

import com.chess.model.Game;
import com.chess.model.GameConstants;
import com.chess.model.Winner;
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

    @PostMapping("/create")
    public ResponseEntity<String> createGame(@RequestParam String playerColor,
            @RequestParam(defaultValue = "300") int timeControlSeconds) {
        try {
            Game game = chessGameService.createGame(playerColor, timeControlSeconds, timeControlSeconds);
            String response = buildGameResponse(game);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error inesperado: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getGame(@PathVariable Long id) {
        try {
            Optional<Game> game = chessGameService.findGame(id);
            if (game.isPresent()) {
                String response = buildGameResponse(game.get());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al obtener el juego: " + e.getMessage());
        }
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

    @PostMapping("/import/pgn")
    public ResponseEntity<Game> importPgn(@RequestParam String pgn,
            @RequestParam(defaultValue = "300") int whiteClock,
            @RequestParam(defaultValue = "300") int blackClock) {
        try {
            Game game = chessGameService.importFromPgn(pgn, whiteClock, blackClock);
            return ResponseEntity.ok(game);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Endpoint temporal para limpiar datos de prueba
    @DeleteMapping("/cleanup")
    public ResponseEntity<String> cleanupTestData() {
        try {
            chessGameService.cleanupTestData();
            return ResponseEntity.ok("Datos de prueba eliminados exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al limpiar datos: " + e.getMessage());
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    // Método privado para construir respuesta
    private String buildGameResponse(Game game) {
        StringBuilder response = new StringBuilder();

        // Información completa del juego para el frontend
        response.append("ID del Juego: ").append(game.getId()).append("\n");
        response.append("Tu Color: ").append(game.getPlayerColor()).append("\n");
        response.append("Turno Actual: ").append(game.getTurn()).append("\n");
        response.append("Estado: ").append(game.getStatus()).append("\n");

        if (game.getWinner() != null && game.getWinner() != Winner.NONE) {
            response.append("Ganador: ").append(game.getWinner()).append("\n");
        }

        response.append("Tiempo Blancas: ").append(formatTime(game.getWhiteClock())).append("\n");
        response.append("Tiempo Negras: ").append(formatTime(game.getBlackClock())).append("\n");
        response.append("PGN: ").append(game.getPgn() != null ? game.getPgn() : "").append("\n");

        return response.toString();
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }
}