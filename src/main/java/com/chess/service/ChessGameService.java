package com.chess.service;

import com.chess.model.Game;
import com.chess.model.GameConstants;
import com.chess.model.Winner;
import com.chess.model.PlayerColor;
import com.chess.repository.GameRepository;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChessGameService {

    @Autowired
    private GameRepository gameRepository;

    /**
     * Crea un nuevo juego
     */
    public Game createGame(String playerColor, int whiteClock, int blackClock) {
        validateGameCreation(playerColor, whiteClock, blackClock);

        Game game = new Game(PlayerColor.fromString(playerColor), whiteClock, blackClock);
        return gameRepository.save(game);
    }

    /**
     * Aplica un movimiento al juego - OPTIMIZADO
     */
    public Game applyMove(Long gameId, String moveNotation) {
        Game game = findGameById(gameId);
        validateMoveRequest(game, moveNotation);

        // Actualizar tiempo ANTES de aplicar el movimiento
        updateGameClock(game);

        if (game.isTimedOut()) {
            return handleTimeout(game);
        }

        // Reconstruir tablero (esto podría optimizarse más con caché)
        Board board = reconstructBoard(game.getMoves());

        // Aplicar movimiento
        Move move = parseAndValidateMove(board, moveNotation.trim());
        board.doMove(move);

        // Actualizar estado del juego
        game.addMove(moveNotation.trim());
        game.setPgn(generatePgn(game.getMoves()));

        // Evaluar estado final
        evaluateGameState(board, game);

        // Actualizar turno solo si la partida sigue activa
        if (game.isActive()) {
            game.switchTurn();
            game.setLastMoveTimestamp(Instant.now());
        }

        return gameRepository.save(game);
    }

    /**
     * Encuentra un juego por ID
     */
    @Transactional(readOnly = true)
    public Optional<Game> findGame(Long id) {
        return gameRepository.findById(id);
    }

    /**
     * Obtiene los movimientos de un juego
     */
    @Transactional(readOnly = true)
    public List<String> getGameMoves(Long gameId) {
        Game game = findGameById(gameId);
        return game.getMoves() != null ? new ArrayList<>(game.getMoves()) : new ArrayList<>();
    }

    // Métodos privados de utilidad

    private Game findGameById(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException(GameConstants.MSG_GAME_NOT_FOUND));
    }

    private void validateGameCreation(String playerColor, int whiteClock, int blackClock) {
        if (!GameConstants.COLOR_WHITE.equals(playerColor) && !GameConstants.COLOR_BLACK.equals(playerColor)) {
            throw new IllegalArgumentException(GameConstants.MSG_INVALID_COLOR);
        }
        if (whiteClock <= 0 || blackClock <= 0) {
            throw new IllegalArgumentException(GameConstants.MSG_INVALID_TIME);
        }
    }

    private void validateMoveRequest(Game game, String moveNotation) {
        if (moveNotation == null || moveNotation.trim().isEmpty()) {
            throw new IllegalArgumentException(GameConstants.MSG_EMPTY_MOVE);
        }

        if (!game.isActive()) {
            throw new IllegalArgumentException(GameConstants.MSG_GAME_NOT_ACTIVE);
        }
    }

    private Board reconstructBoard(List<String> moves) {
        Board board = new Board();
        if (moves != null) {
            for (String moveNotation : moves) {
                Move move = parseAndValidateMove(board, moveNotation);
                board.doMove(move);
            }
        }
        return board;
    }

    private Move parseAndValidateMove(Board board, String algebraic) {
        for (Move legalMove : board.legalMoves()) {
            if (legalMove.toString().equals(algebraic)) {
                return legalMove;
            }
        }
        throw new IllegalArgumentException(GameConstants.MSG_ILLEGAL_MOVE + algebraic);
    }

    private void updateGameClock(Game game) {
        if (game.getLastMoveTimestamp() != null) {
            long secondsElapsed = Duration.between(game.getLastMoveTimestamp(), Instant.now()).getSeconds();
            game.updateClock(secondsElapsed);
        }
    }

    private Game handleTimeout(Game game) {
        if (game.getWhiteClock() <= 0) {
            game.setStatusEnum(com.chess.model.GameStatus.TIMEOUT);
            game.setWinner(Winner.BLACK);
        } else {
            game.setStatusEnum(com.chess.model.GameStatus.TIMEOUT);
            game.setWinner(Winner.WHITE);
        }
        return gameRepository.save(game);
    }

    private String generatePgn(List<String> moves) {
        if (moves == null || moves.isEmpty()) {
            return GameConstants.EMPTY_PGN;
        }

        StringBuilder pgnBuilder = new StringBuilder();
        int moveNumber = 1;
        boolean whiteToMove = true;

        for (String moveNotation : moves) {
            if (whiteToMove) {
                pgnBuilder.append(moveNumber).append(". ");
            }
            pgnBuilder.append(moveNotation).append(" ");

            whiteToMove = !whiteToMove;
            if (whiteToMove) {
                moveNumber++;
            }
        }

        return pgnBuilder.toString().trim();
    }

    private void evaluateGameState(Board board, Game game) {
        if (board.isMated()) {
            if (board.getSideToMove() == com.github.bhlangonijr.chesslib.Side.WHITE) {
                game.setWinner(Winner.BLACK);
                game.setStatusEnum(com.chess.model.GameStatus.MATE);
            } else {
                game.setWinner(Winner.WHITE);
                game.setStatusEnum(com.chess.model.GameStatus.MATE);
            }
        } else if (board.isStaleMate()) {
            game.setWinner(Winner.DRAW);
            game.setStatusEnum(com.chess.model.GameStatus.STALEMATE);
        } else if (board.isDraw()) {
            game.setWinner(Winner.DRAW);
            game.setStatusEnum(com.chess.model.GameStatus.DRAW);
        }
        // Si no es ninguno de los anteriores, el juego continúa activo
    }

    public String getGameStateMessage(Board board, Game game) {
        if (board.isMated()) {
            return board.getSideToMove() == com.github.bhlangonijr.chesslib.Side.WHITE
                    ? GameConstants.MSG_CHECKMATE_BLACK_WINS
                    : GameConstants.MSG_CHECKMATE_WHITE_WINS;
        } else if (board.isStaleMate()) {
            return GameConstants.MSG_STALEMATE;
        } else if (board.isDraw()) {
            return GameConstants.MSG_DRAW;
        } else if (board.isKingAttacked()) {
            return GameConstants.MSG_CHECK;
        } else {
            return GameConstants.MSG_MOVE_SUCCESS;
        }
    }

    /**
     * Importa un juego desde PGN
     */
    public Game importFromPgn(String pgn, int whiteClock, int blackClock) {
        validatePgnInput(pgn, whiteClock, blackClock);

        try {
            // Crear un nuevo tablero para validar el PGN
            Board board = new Board();

            // Limpiar y parsear el PGN
            String cleanPgn = cleanPgn(pgn);

            // Aplicar los movimientos del PGN para validarlo
            String[] moves = parsePgnMoves(cleanPgn);

            // Validar cada movimiento
            for (String moveStr : moves) {
                if (moveStr.trim().isEmpty())
                    continue;

                Move move = findLegalMoveFromPgn(board, moveStr);
                if (move == null) {
                    throw new IllegalArgumentException("Movimiento inválido en PGN: " + moveStr);
                }
                board.doMove(move);
            }

            // Crear el juego con el PGN importado
            Game game = new Game(PlayerColor.WHITE, whiteClock, blackClock);
            game.setPgn(cleanPgn);

            // Establecer el turno actual basado en el tablero
            game.setTurn(board.getSideToMove() == com.github.bhlangonijr.chesslib.Side.WHITE ? "WHITE" : "BLACK");

            // Evaluar el estado del juego
            evaluateGameState(board, game);

            return gameRepository.save(game);

        } catch (Exception e) {
            throw new IllegalArgumentException("Error al importar PGN: " + e.getMessage());
        }
    }

    private void validatePgnInput(String pgn, int whiteClock, int blackClock) {
        if (pgn == null || pgn.trim().isEmpty()) {
            throw new IllegalArgumentException("PGN no puede estar vacío");
        }
        if (whiteClock < 0 || blackClock < 0) {
            throw new IllegalArgumentException("El tiempo del reloj no puede ser negativo");
        }
    }

    private String cleanPgn(String pgn) {
        // Remover números de movimiento, comentarios y metadatos
        String cleaned = pgn.replaceAll("\\d+\\.", "") // Números de movimiento
                .replaceAll("\\{[^}]*\\}", "") // Comentarios entre llaves
                .replaceAll("\\([^)]*\\)", "") // Comentarios entre paréntesis
                .replaceAll("\\[[^]]*\\]", "") // Metadatos entre corchetes
                .replaceAll("\\s+", " ") // Espacios múltiples
                .trim();

        // Remover resultado final si existe
        cleaned = cleaned.replaceAll("(1-0|0-1|1/2-1/2)\\s*$", "").trim();

        return cleaned;
    }

    private String[] parsePgnMoves(String cleanPgn) {
        if (cleanPgn.isEmpty()) {
            return new String[0];
        }
        return cleanPgn.split("\\s+");
    }

    private Move findLegalMoveFromPgn(Board board, String pgnMove) {
        // Intentar convertir PGN a LAN (Long Algebraic Notation)
        for (Move legalMove : board.legalMoves()) {
            // Chesslib puede manejar tanto SAN como LAN
            if (legalMove.toString().equals(pgnMove) ||
                    matchesPgnMove(legalMove, pgnMove, board)) {
                return legalMove;
            }
        }
        return null;
    }

    private boolean matchesPgnMove(Move move, String pgnMove, Board board) {
        // Lógica simplificada para matching de movimientos PGN
        // En un sistema completo, esto sería más sofisticado
        String moveStr = move.toString();

        // Casos básicos: movimiento directo
        if (moveStr.equals(pgnMove)) {
            return true;
        }

        // TODO: Implementar matching más sofisticado para SAN si es necesario
        // Por ahora, chesslib maneja la mayoría de casos automáticamente

        return false;
    }

    /**
     * Método temporal para limpiar datos de prueba
     */
    @Transactional
    public void cleanupTestData() {
        // Eliminar todos los juegos (esto eliminará también los movimientos por
        // cascada)
        gameRepository.deleteAll();
    }
}
