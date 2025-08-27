package com.Chess.Chess;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ChessLibUnitTest {

    @Test
    public void testLegalMove() {
        Board board = new Board();
        Move move = new Move(com.github.bhlangonijr.chesslib.Square.E2, com.github.bhlangonijr.chesslib.Square.E4);
        assertTrue(board.legalMoves().contains(move), "e2e4 debe ser legal");
    }

    @Test
    public void testIllegalMove() {
        Board board = new Board();
        Move move = new Move(com.github.bhlangonijr.chesslib.Square.E2, com.github.bhlangonijr.chesslib.Square.E5); // Peón
                                                                                                                    // no
                                                                                                                    // puede
                                                                                                                    // avanzar
                                                                                                                    // 3
        assertFalse(board.legalMoves().contains(move), "e2e5 debe ser ilegal");
    }
}