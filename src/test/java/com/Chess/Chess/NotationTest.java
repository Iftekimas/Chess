package com.Chess.Chess;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.Square;

public class NotationTest {
    public static void main(String[] args) {
        Board board = new Board();
        
        System.out.println("=== TESTING CHESS NOTATION FORMATS ===");
        System.out.println("Initial FEN: " + board.getFen());
        System.out.println();
        
        // Test different move formats
        System.out.println("Testing move formats:");
        
        // Create a move from e2 to e4
        Move move1 = new Move(Square.E2, Square.E4);
        System.out.println("Move e2-e4 toString(): " + move1.toString());
        
        // Test SAN format if available
        try {
            board.doMove(move1);
            System.out.println("After e2-e4, FEN: " + board.getFen());
            
            // Try another move
            Move move2 = new Move(Square.E7, Square.E5);
            System.out.println("Move e7-e5 toString(): " + move2.toString());
            board.doMove(move2);
            
            // Try knight move
            Move knightMove = new Move(Square.G1, Square.F3);
            System.out.println("Knight move g1-f3 toString(): " + knightMove.toString());
            board.doMove(knightMove);
            
            System.out.println("Final FEN: " + board.getFen());
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        System.out.println();
        System.out.println("=== TESTING LEGAL MOVES FORMAT ===");
        Board testBoard = new Board();
        System.out.println("Legal moves from starting position:");
        int count = 0;
        for (Move legalMove : testBoard.legalMoves()) {
            System.out.println(legalMove.toString());
            count++;
            if (count >= 10) { // Show only first 10 moves
                System.out.println("... and " + (testBoard.legalMoves().size() - 10) + " more moves");
                break;
            }
        }
        
        System.out.println();
        System.out.println("=== CONCLUSION ===");
        System.out.println("The format used by chesslib Move.toString() appears to be: ");
        System.out.println("- LAN (Long Algebraic Notation) format: e2e4, e7e5, g1f3");
        System.out.println("- NOT SAN (Standard Algebraic Notation) format like: e4, e5, Nf3");
    }
}
