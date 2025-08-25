package com.chess.controller;

import com.chess.model.Game;
import com.chess.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.time.Instant;
import java.time.Duration;

@RestController
@RequestMapping("/games")
public class GameController {

    @Autowired
    private GameRepository gameRepository;

    // Crear una partida nueva
    @PostMapping
    public Game createGame(@RequestParam String playerColor,
            @RequestParam(defaultValue = "300") int whiteClock,
            @RequestParam(defaultValue = "300") int blackClock) {
        if (!playerColor.equals("white") && !playerColor.equals("black")) {
            throw new IllegalArgumentException("El color solo puede ser 'white' o 'black'");
        }
        if (whiteClock <= 0 || blackClock <= 0) {
            throw new IllegalArgumentException("El tiempo debe ser positivo");
        }
        Game game = new Game();
        game.setPlayerColor(playerColor);
        game.setWhiteClock(whiteClock);
        game.setBlackClock(blackClock);
        game.setTurn("white");
        return gameRepository.save(game);
    }

    // Consultar estado de partida
    @GetMapping("/{id}")
    public Game getGame(@PathVariable Long id) {
        return gameRepository.findById(id).orElse(null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @GetMapping("/{id}/moves")
    public java.util.List<String> getMoves(@PathVariable Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Partida no encontrada"));
        return game.getMoves();
    }

    @PostMapping("/{id}/move")
    public Game makeMove(@PathVariable Long id, @RequestParam String move, @RequestParam String color) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Partida no encontrada"));

        // Verificación de turno
        if (!game.getTurn().equals(color)) {
            throw new IllegalArgumentException("No es el turno del jugador " + color);
        }

        // Lógica para descontar tiempo y actualizar timestamp
        Instant now = Instant.now();

        if (game.getLastMoveTimestamp() != null) {
            long secondsElapsed = Duration.between(game.getLastMoveTimestamp(), now).getSeconds();

            if ("white".equals(color)) {
                // Descuenta tiempo del jugador blanco
                game.setWhiteClock(game.getWhiteClock() - (int) secondsElapsed);
                if (game.getWhiteClock() <= 0) {
                    game.setStatus("timeout");
                }
            } else {
                // Descuenta tiempo del jugador negro
                game.setBlackClock(game.getBlackClock() - (int) secondsElapsed);
                if (game.getBlackClock() <= 0) {
                    game.setStatus("timeout");
                }
            }
        }

        // Actualiza timestamp para el siguiente movimiento
        game.setLastMoveTimestamp(now);

        // Validación sencilla: el movimiento no puede estar vacío
        if (move == null || move.trim().isEmpty()) {
            throw new IllegalArgumentException("El movimiento no puede estar vacío");
        }

        // Agregar el movimiento a la lista y persistir
        List<String> moves = game.getMoves();
        if (moves == null) {
            moves = new java.util.ArrayList<>();
            game.setMoves(moves);
        }
        moves.add(move);

        // Alternar turno
        game.setTurn(color.equals("white") ? "black" : "white");

        return gameRepository.save(game);
    }

}