package com.chess.controller;

import com.chess.model.Game;
import com.chess.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

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

    @PostMapping("/{id}/move")
    public Game makeMove(@PathVariable Long id, @RequestParam String move) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Partida no encontrada"));

        // Validación sencilla: el movimiento no puede estar vacío
        if (move == null || move.trim().isEmpty()) {
            throw new IllegalArgumentException("El movimiento no puede estar vacío");
        }

        game.getMoves().add(move);
        return gameRepository.save(game);
    }

    @GetMapping("/{id}/moves")
    public java.util.List<String> getMoves(@PathVariable Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Partida no encontrada"));
        return game.getMoves();
    }

}