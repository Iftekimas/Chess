package com.Chess.Chess;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveException;

public class ChessLibTest {
    public static void main(String[] args) {
        Board board = new Board();

        try {
            Move move = new Move(com.github.bhlangonijr.chesslib.Square.E2, com.github.bhlangonijr.chesslib.Square.E4);
            board.doMove(move);
            System.out.println("Movimiento legal, FEN: " + board.getFen());
        } catch (MoveException e) {
            System.out.println("Movimiento ilegal: " + e.getMessage());
        }
    }
}