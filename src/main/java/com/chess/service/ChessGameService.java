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
}
