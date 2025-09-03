package com.Chess.Chess;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.Piece;

public class SanGenerationTest {
    public static void main(String[] args) {
        System.out.println("=== TESTING SAN GENERATION WITH BOARD CONTEXT ===");
        
        Board board = new Board();
        
        // Check all methods available in Board class
        System.out.println("\nChecking Board methods for SAN generation:");
        java.lang.reflect.Method[] methods = Board.class.getMethods();
        for (java.lang.reflect.Method method : methods) {
            String methodName = method.getName().toLowerCase();
            if (methodName.contains("san") || methodName.contains("notation") || methodName.contains("move")) {
                System.out.println("Found method: " + method.getName() + 
                                 " - Return type: " + method.getReturnType().getSimpleName() +
                                 " - Parameters: " + method.getParameterCount());
            }
        }
        
        // Test manual SAN generation
        System.out.println("\n=== TESTING MANUAL SAN GENERATION ===");
        
        // Test a pawn move
        Move pawnMove = new Move(Square.E2, Square.E4);
        String pawnSan = generateSAN(board, pawnMove);
        System.out.println("Pawn move e2-e4: SAN = " + pawnSan);
        
        board.doMove(pawnMove);
        
        // Test a knight move
        Move knightMove = new Move(Square.G1, Square.F3);
        String knightSan = generateSAN(board, knightMove);
        System.out.println("Knight move g1-f3: SAN = " + knightSan);
        
        board.doMove(knightMove);
        
        // Test if there are other approaches
        System.out.println("\n=== TESTING ALTERNATIVE APPROACHES ===");
        
        // Reset board
        Board testBoard = new Board();
        Move testMove = new Move(Square.E2, Square.E4);
        
        // Try to see if Move has any other methods
        java.lang.reflect.Method[] moveMethods = Move.class.getMethods();
        for (java.lang.reflect.Method method : moveMethods) {
            String methodName = method.getName().toLowerCase();
            if (methodName.contains("san") || methodName.contains("string") || methodName.contains("text")) {
                System.out.println("Move method: " + method.getName() + 
                                 " - Return type: " + method.getReturnType().getSimpleName() +
                                 " - Parameters: " + method.getParameterCount());
            }
        }
        
        System.out.println("\n=== FINAL ANSWER ===");
        System.out.println("chesslib provides:");
        System.out.println("✓ LAN: Via Move.toString() - always works");
        System.out.println("? SAN: Via Move.getSan()/setSan() - manual setting only");
        System.out.println("✗ Auto SAN: No automatic SAN generation found");
        System.out.println("→ Project currently uses LAN format (e2e4) which is sufficient");
        System.out.println("→ Could implement custom SAN generation if needed for user interface");
    }
    
    // Manual SAN generation method
    private static String generateSAN(Board board, Move move) {
        try {
            // Get piece at source square
            Piece piece = board.getPiece(move.getFrom());
            String san = "";
            
            // Add piece prefix (except for pawns)
            if (piece != null && piece.getPieceType() != null) {
                switch (piece.getPieceType()) {
                    case PAWN:
                        // For pawns, only add file if it's a capture
                        if (move.getTo().getFile() != move.getFrom().getFile()) {
                            san += move.getFrom().getFile().toString().toLowerCase();
                            san += "x";
                        }
                        san += move.getTo().toString().toLowerCase();
                        break;
                    case KNIGHT:
                        san = "N" + move.getTo().toString().toLowerCase();
                        break;
                    case BISHOP:
                        san = "B" + move.getTo().toString().toLowerCase();
                        break;
                    case ROOK:
                        san = "R" + move.getTo().toString().toLowerCase();
                        break;
                    case QUEEN:
                        san = "Q" + move.getTo().toString().toLowerCase();
                        break;
                    case KING:
                        // Check for castling
                        if (Math.abs(move.getTo().getFile().ordinal() - move.getFrom().getFile().ordinal()) > 1) {
                            if (move.getTo().getFile().ordinal() > move.getFrom().getFile().ordinal()) {
                                san = "O-O";
                            } else {
                                san = "O-O-O";
                            }
                        } else {
                            san = "K" + move.getTo().toString().toLowerCase();
                        }
                        break;
                    case NONE:
                    default:
                        san = move.getTo().toString().toLowerCase();
                        break;
                }
            }
            
            return san.isEmpty() ? move.getTo().toString().toLowerCase() : san;
            
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}
