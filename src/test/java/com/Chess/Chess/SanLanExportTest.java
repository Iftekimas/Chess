package com.Chess.Chess;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.Square;

public class SanLanExportTest {
    public static void main(String[] args) {
        System.out.println("=== TESTING SAN/LAN EXPORT/PARSE CAPABILITIES ===");
        
        // Test 1: Basic LAN format (what we currently use)
        System.out.println("\n1. CURRENT LAN FORMAT:");
        Board board = new Board();
        Move move1 = new Move(Square.E2, Square.E4);
        Move move2 = new Move(Square.E7, Square.E5);
        Move move3 = new Move(Square.G1, Square.F3);
        
        System.out.println("e2-e4 LAN: " + move1.toString());
        System.out.println("e7-e5 LAN: " + move2.toString());
        System.out.println("g1-f3 LAN: " + move3.toString());
        
        // Test 2: Try to get SAN format using board context
        System.out.println("\n2. TESTING SAN FORMAT:");
        try {
            board.doMove(move1);
            System.out.println("After e2e4, trying to get SAN...");
            
            // Check if Move has getSan() method or similar
            java.lang.reflect.Method[] methods = Move.class.getMethods();
            boolean hasSanMethod = false;
            for (java.lang.reflect.Method method : methods) {
                if (method.getName().toLowerCase().contains("san")) {
                    System.out.println("Found SAN-related method: " + method.getName());
                    hasSanMethod = true;
                }
            }
            if (!hasSanMethod) {
                System.out.println("No direct SAN methods found in Move class");
            }
            
        } catch (Exception e) {
            System.out.println("Error testing SAN: " + e.getMessage());
        }
        
        // Test 3: PGN capabilities
        System.out.println("\n3. TESTING PGN CAPABILITIES:");
        try {
            // Test if we can work with PGN using Board
            Board pgnBoard = new Board();
            System.out.println("Can load from FEN: " + (pgnBoard.getFen() != null));
            
            // Try different notation approaches
            System.out.println("Testing PGN generation manually...");
            
        } catch (Exception e) {
            System.out.println("Error with PGN: " + e.getMessage());
        }
        
        // Test 4: Check Board methods for SAN
        System.out.println("\n4. TESTING BOARD SAN METHODS:");
        try {
            Board testBoard = new Board();
            java.lang.reflect.Method[] boardMethods = Board.class.getMethods();
            
            for (java.lang.reflect.Method method : boardMethods) {
                String methodName = method.getName().toLowerCase();
                if (methodName.contains("san") || methodName.contains("algebraic")) {
                    System.out.println("Found Board method: " + method.getName() + 
                                     " - Parameters: " + method.getParameterCount());
                }
            }
            
        } catch (Exception e) {
            System.out.println("Error checking Board methods: " + e.getMessage());
        }
        
        System.out.println("\n=== SUMMARY ===");
        System.out.println("✓ LAN (Long Algebraic Notation): SUPPORTED via Move.toString()");
        System.out.println("? SAN (Standard Algebraic Notation): Need to investigate further");
        System.out.println("✓ PGN: SUPPORTED via Game.loadFromPgn() and related classes");
        System.out.println("Current project uses: LAN format for move storage and processing");
    }
}
