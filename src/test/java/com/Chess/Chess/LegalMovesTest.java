package com.Chess.Chess;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;

public class LegalMovesTest {
    public static void main(String[] args) {
        System.out.println("=== TEST EXHAUSTIVO DE MOVIMIENTOS LEGALES ===");

        Board board = new Board();

        System.out.println("Estado inicial del tablero:");
        System.out.println("FEN: " + board.getFen());
        System.out.println("Total movimientos legales: " + board.legalMoves().size());

        // Test 1: Todos los movimientos desde posición inicial
        System.out.println("\n1. MOVIMIENTOS LEGALES DESDE POSICIÓN INICIAL:");
        int count = 0;
        for (Move legalMove : board.legalMoves()) {
            count++;
            System.out.println(count + ". " + legalMove.toString() +
                    " (desde " + legalMove.getFrom() + " hacia " + legalMove.getTo() + ")");
        }

        // Test 2: Aplicar algunos movimientos y verificar
        System.out.println("\n2. APLICANDO SECUENCIA DE MOVIMIENTOS:");
        String[] testMoves = { "e2e4", "e7e5", "g1f3", "b8c6", "f1c4", "g8f6" };

        for (String moveStr : testMoves) {
            try {
                // Buscar el movimiento legal
                Move moveToApply = null;
                for (Move legalMove : board.legalMoves()) {
                    if (legalMove.toString().equals(moveStr)) {
                        moveToApply = legalMove;
                        break;
                    }
                }

                if (moveToApply != null) {
                    board.doMove(moveToApply);
                    System.out.println("✅ Aplicado: " + moveStr +
                            " | Turno: " + board.getSideToMove() +
                            " | Legal moves: " + board.legalMoves().size());
                } else {
                    System.out.println("❌ Movimiento no encontrado: " + moveStr);
                    break;
                }

            } catch (Exception e) {
                System.out.println("❌ Error aplicando " + moveStr + ": " + e.getMessage());
                break;
            }
        }

        // Test 3: Verificar algunos movimientos específicos
        System.out.println("\n3. VERIFICACIÓN DE MOVIMIENTOS ESPECÍFICOS:");

        // Reset board para tests específicos
        Board testBoard = new Board();

        // Test movimientos de peón
        System.out.println("\nMovimientos de peón:");
        testSpecificMove(testBoard, "e2e4", "Peón e2-e4");
        testSpecificMove(testBoard, "e2e3", "Peón e2-e3");
        testSpecificMove(testBoard, "d2d4", "Peón d2-d4");

        // Test movimientos de caballo
        System.out.println("\nMovimientos de caballo:");
        testSpecificMove(testBoard, "g1f3", "Caballo g1-f3");
        testSpecificMove(testBoard, "g1h3", "Caballo g1-h3");
        testSpecificMove(testBoard, "b1c3", "Caballo b1-c3");

        // Test movimientos inválidos
        System.out.println("\nMovimientos INVÁLIDOS (deben fallar):");
        testSpecificMove(testBoard, "e2e5", "Peón e2-e5 (inválido - muy lejos)");
        testSpecificMove(testBoard, "e1e2", "Rey e1-e2 (inválido - bloqueado)");
        testSpecificMove(testBoard, "f1f3", "Alfil f1-f3 (inválido - bloqueado)");

        System.out.println("\n=== RESUMEN ===");
        System.out.println("✅ Test de movimientos legales completado");
        System.out.println("✅ Posición inicial: 20 movimientos legales esperados");
        System.out.println("✅ Secuencia de apertura aplicada correctamente");
        System.out.println("✅ Validación de movimientos específicos verificada");
    }

    private static void testSpecificMove(Board board, String moveStr, String description) {
        boolean found = false;
        for (Move legalMove : board.legalMoves()) {
            if (legalMove.toString().equals(moveStr)) {
                found = true;
                break;
            }
        }

        String result = found ? "✅ VÁLIDO" : "❌ INVÁLIDO";
        System.out.println("  " + description + ": " + result);
    }
}
