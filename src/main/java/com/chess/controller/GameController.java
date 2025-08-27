package com.chess.controller;

import com.chess.model.Game;
import com.chess.model.Winner;
import com.chess.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.time.Duration;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;

@RestController
@RequestMapping("/games")
public class GameController {

    @Autowired
    private GameRepository gameRepository;

    @PostMapping("/{gameId}/move/fen")
    public ResponseEntity<String> makeMoveFen(@PathVariable Long gameId, @RequestParam String move) {
        Game game = gameRepository.findById(gameId).orElse(null);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Board board = new Board();
            board.loadFromFen(game.getFen());

            move = move.trim().toUpperCase();
            System.out.println("MOVE recibido: [" + move + "]");

            if (move == null || move.length() < 4 || move.length() > 5) {
                return ResponseEntity.badRequest()
                        .body("Formato de movimiento inválido. Debe ser tipo 'e2e4' o 'e7e8q'.");
            }

            // Control de tiempo y validaciones
            Instant now = Instant.now();
            if (game.getLastMoveTimestamp() != null) {
                long secondsElapsed = Duration.between(game.getLastMoveTimestamp(), now).getSeconds();
                String turn = game.getTurn();
                if ("white".equals(turn)) {
                    game.setWhiteClock(game.getWhiteClock() - (int) secondsElapsed);
                } else {
                    game.setBlackClock(game.getBlackClock() - (int) secondsElapsed);
                }
                // Verifica timeout y asigna ganador
                if (game.getWhiteClock() <= 0) {
                    game.setStatus("timeout");
                    game.setWinner(Winner.BLACK);
                    gameRepository.save(game);
                    return ResponseEntity.ok("Timeout: gana negro");
                }
                if (game.getBlackClock() <= 0) {
                    game.setStatus("timeout");
                    game.setWinner(Winner.WHITE);
                    gameRepository.save(game);
                    return ResponseEntity.ok("Timeout: gana blanco");
                }
                if (!"active".equals(game.getStatus())) {
                    return ResponseEntity.badRequest().body("La partida ya no está activa.");
                }
            }

            // Validación de estado activo
            if (!"active".equals(game.getStatus())) {
                return ResponseEntity.badRequest().body("La partida ya no está activa.");
            }

            // Validación de reloj
            if (game.getWhiteClock() <= 0) {
                return ResponseEntity.badRequest().body("El jugador blanco no tiene tiempo restante.");
            }
            if (game.getBlackClock() <= 0) {
                return ResponseEntity.badRequest().body("El jugador negro no tiene tiempo restante.");
            }

            String fromStr = move.substring(0, 2);
            String toStr = move.substring(2, 4);
            System.out.println("MOVE recibido: [" + move + "], from: [" + fromStr + "], to: [" + toStr + "]");

            com.github.bhlangonijr.chesslib.Square from, to;
            try {
                from = com.github.bhlangonijr.chesslib.Square.fromValue(fromStr);
                to = com.github.bhlangonijr.chesslib.Square.fromValue(toStr);
            } catch (Exception sqe) {
                return ResponseEntity.badRequest().body("Las casillas de origen o destino no son válidas.");
            }

            Move chessMove;
            if (move.length() == 5) {
                // Soporte para promoción
                char promo = move.charAt(4);
                com.github.bhlangonijr.chesslib.PieceType promotedType = null;
                switch (promo) {
                    case 'q':
                    case 'Q':
                        promotedType = com.github.bhlangonijr.chesslib.PieceType.QUEEN;
                        break;
                    case 'r':
                    case 'R':
                        promotedType = com.github.bhlangonijr.chesslib.PieceType.ROOK;
                        break;
                    case 'b':
                    case 'B':
                        promotedType = com.github.bhlangonijr.chesslib.PieceType.BISHOP;
                        break;
                    case 'n':
                    case 'N':
                        promotedType = com.github.bhlangonijr.chesslib.PieceType.KNIGHT;
                        break;
                    default:
                        return ResponseEntity.badRequest().body("Pieza de promoción inválida. Usa 'q', 'r', 'b', 'n'.");
                }
                com.github.bhlangonijr.chesslib.Side side = board.getSideToMove();
                com.github.bhlangonijr.chesslib.Piece promotedPiece = null;
                if (promotedType == com.github.bhlangonijr.chesslib.PieceType.QUEEN) {
                    promotedPiece = (side == com.github.bhlangonijr.chesslib.Side.WHITE)
                            ? com.github.bhlangonijr.chesslib.Piece.WHITE_QUEEN
                            : com.github.bhlangonijr.chesslib.Piece.BLACK_QUEEN;
                } else if (promotedType == com.github.bhlangonijr.chesslib.PieceType.ROOK) {
                    promotedPiece = (side == com.github.bhlangonijr.chesslib.Side.WHITE)
                            ? com.github.bhlangonijr.chesslib.Piece.WHITE_ROOK
                            : com.github.bhlangonijr.chesslib.Piece.BLACK_ROOK;
                } else if (promotedType == com.github.bhlangonijr.chesslib.PieceType.BISHOP) {
                    promotedPiece = (side == com.github.bhlangonijr.chesslib.Side.WHITE)
                            ? com.github.bhlangonijr.chesslib.Piece.WHITE_BISHOP
                            : com.github.bhlangonijr.chesslib.Piece.BLACK_BISHOP;
                } else if (promotedType == com.github.bhlangonijr.chesslib.PieceType.KNIGHT) {
                    promotedPiece = (side == com.github.bhlangonijr.chesslib.Side.WHITE)
                            ? com.github.bhlangonijr.chesslib.Piece.WHITE_KNIGHT
                            : com.github.bhlangonijr.chesslib.Piece.BLACK_KNIGHT;
                }
                chessMove = new Move(from, to, promotedPiece);
            } else {
                chessMove = new Move(from, to);
            }

            if (!board.legalMoves().contains(chessMove)) {
                return ResponseEntity.badRequest().body("Movimiento ilegal para el estado actual del tablero.");
            }

            List<String> moves = game.getMoves();
            if (moves == null) {
                moves = new ArrayList<>();
                game.setMoves(moves);
            }
            moves.add(move.toUpperCase());

            board.doMove(chessMove);
            game.setFen(board.getFen());

            // Alternar turno y actualizar timestamp
            game.setTurn(game.getTurn().equals("white") ? "black" : "white");
            game.setLastMoveTimestamp(now);

            gameRepository.save(game);

            // Construir respuesta con movimientos y tiempos
            List<String> movesList = game.getMoves();
            int whiteClock = game.getWhiteClock();
            int blackClock = game.getBlackClock();
            StringBuilder response = new StringBuilder();
            response.append("Movimiento realizado correctamente.\n");
            response.append("Movimientos: ").append(movesList).append("\n");
            response.append("Tiempo blanco: ").append(whiteClock).append(" segundos\n");
            response.append("Tiempo negro: ").append(blackClock).append(" segundos\n");
            response.append("Turno: ").append(game.getTurn()).append("\n");
            return ResponseEntity.ok(response.toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error procesando el FEN o el movimiento: " + e.getMessage());
        }
    }

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
        // FEN inicial
        game.setFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
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
            } else {
                // Descuenta tiempo del jugador negro
                game.setBlackClock(game.getBlackClock() - (int) secondsElapsed);
            }
            // Verifica timeout y asigna ganador
            if (game.getWhiteClock() <= 0) {
                game.setStatus("timeout");
                game.setWinner(Winner.BLACK);
                return gameRepository.save(game);
            }
            if (game.getBlackClock() <= 0) {
                game.setStatus("timeout");
                game.setWinner(Winner.WHITE);
                return gameRepository.save(game);
            }

            if (!"active".equals(game.getStatus())) {
                throw new IllegalStateException("La partida ya no está activa.");
            }
        }

        // 1. Validación: partida debe estar activa
        if (!"active".equals(game.getStatus())) {
            throw new IllegalStateException("La partida ya no está activa.");
        }

        // 2. Validación: reloj del jugador debe ser mayor a cero
        if ("white".equals(color) && game.getWhiteClock() <= 0) {
            throw new IllegalStateException("El jugador blanco no tiene tiempo restante.");
        }
        if ("black".equals(color) && game.getBlackClock() <= 0) {
            throw new IllegalStateException("El jugador negro no tiene tiempo restante.");
        }

        // 3. Validación: debe ser el turno del jugador
        if (!game.getTurn().equals(color)) {
            throw new IllegalArgumentException("No es el turno del jugador " + color);
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