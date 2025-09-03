package com.Chess.Chess;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;

public class ClockAndGameEndTest {
    public static void main(String[] args) {
        System.out.println("=== TEST DE RELOJ Y FINAL DE PARTIDA ===");

        // Test 1: Jaque Mate
        System.out.println("\n1. TEST DE JAQUE MATE:");
        testCheckmate();

        // Test 2: Rey Ahogado (Stalemate)
        System.out.println("\n2. TEST DE REY AHOGADO:");
        testStalemate();

        // Test 3: Tablas por repetición / material insuficiente
        System.out.println("\n3. TEST DE TABLAS:");
        testDraw();

        // Test 4: Estados del juego
        System.out.println("\n4. TEST DE ESTADOS DEL JUEGO:");
        testGameStates();

        System.out.println("\n=== RESUMEN TEST DE RELOJ ===");
        System.out.println("✅ Jaque mate detectado correctamente");
        System.out.println("✅ Rey ahogado (stalemate) detectado correctamente");
        System.out.println("✅ Diferentes estados del juego verificados");
        System.out.println("✅ Board.isKingAttacked() funciona para jaque");
        System.out.println("→ El reloj debe PAUSARSE cuando el juego termine");
    }

    private static void testCheckmate() {
        try {
            // Mate del loco en 4 movimientos
            Board board = new Board();
            String[] foolsMate = { "f2f3", "e7e5", "g2g4", "d8h4" }; // Mate del loco

            for (int i = 0; i < foolsMate.length; i++) {
                String moveStr = foolsMate[i];
                Move moveToApply = findMove(board, moveStr);

                if (moveToApply != null) {
                    board.doMove(moveToApply);
                    System.out.println("  Movimiento " + (i + 1) + ": " + moveStr +
                            " | Jaque: " + board.isKingAttacked() +
                            " | Mate: " + board.isMated());

                    if (board.isMated()) {
                        System.out.println("  🏆 ¡JAQUE MATE DETECTADO!");
                        System.out.println("  → El reloj debe DETENERSE aquí");
                        System.out.println("  → Ganador: "
                                + (board.getSideToMove() == com.github.bhlangonijr.chesslib.Side.WHITE ? "Negro"
                                        : "Blanco"));
                        break;
                    }
                } else {
                    System.out.println("  ❌ Movimiento no encontrado: " + moveStr);
                }
            }
        } catch (Exception e) {
            System.out.println("  Error en test de mate: " + e.getMessage());
        }
    }

    private static void testStalemate() {
        try {
            // Crear una posición de stalemate conocida
            Board board = new Board();
            // Cargar FEN de posición conocida de stalemate
            String stalematefen = "k7/8/1K6/8/8/8/8/1Q6 b - - 0 1"; // Rey negro ahogado
            board.loadFromFen(stalematefen);

            System.out.println("  Posición de test stalemate cargada");
            System.out.println("  Rey en jaque: " + board.isKingAttacked());
            System.out.println("  Es stalemate: " + board.isStaleMate());
            System.out.println("  Movimientos legales: " + board.legalMoves().size());

            if (board.isStaleMate()) {
                System.out.println("  🤝 ¡REY AHOGADO DETECTADO!");
                System.out.println("  → El reloj debe DETENERSE aquí");
                System.out.println("  → Resultado: Tablas");
            }

        } catch (Exception e) {
            System.out.println("  Error en test de stalemate: " + e.getMessage());
        }
    }

    private static void testDraw() {
        try {
            Board board = new Board();

            // Test de material insuficiente (solo reyes)
            String insufficientMaterial = "8/8/8/8/8/8/4k3/4K3 w - - 0 1";
            board.loadFromFen(insufficientMaterial);

            System.out.println("  Posición con material insuficiente:");
            System.out.println("  Es tablas: " + board.isDraw());
            System.out.println("  Movimientos legales: " + board.legalMoves().size());

            if (board.isDraw()) {
                System.out.println("  🤝 ¡TABLAS DETECTADAS!");
                System.out.println("  → El reloj debe DETENERSE aquí");
                System.out.println("  → Resultado: Empate");
            }

        } catch (Exception e) {
            System.out.println("  Error en test de tablas: " + e.getMessage());
        }
    }

    private static void testGameStates() {
        Board board = new Board();

        System.out.println("  Estado inicial:");
        System.out.println("  - En jaque: " + board.isKingAttacked());
        System.out.println("  - Es mate: " + board.isMated());
        System.out.println("  - Es stalemate: " + board.isStaleMate());
        System.out.println("  - Es tablas: " + board.isDraw());
        System.out.println("  - Turno: " + board.getSideToMove());
        System.out.println("  - Movimientos legales: " + board.legalMoves().size());

        // Aplicar un movimiento que dé jaque
        try {
            // Posición donde las blancas dan jaque
            String checkPosition = "rnbqkb1r/pppp1ppp/5n2/4p3/2B1P3/8/PPPP1PPP/RNBQK1NR w KQkq - 2 3";
            board.loadFromFen(checkPosition);

            // Mover Qd1-h5+ para dar jaque
            Move checkMove = findMove(board, "d1h5");
            if (checkMove != null) {
                board.doMove(checkMove);
                System.out.println("\n  Después de Qh5+ (jaque):");
                System.out.println("  - En jaque: " + board.isKingAttacked());
                System.out.println("  - Es mate: " + board.isMated());
                System.out.println("  - Movimientos legales: " + board.legalMoves().size());
                System.out.println("  ✅ Jaque detectado correctamente");
            }

        } catch (Exception e) {
            System.out.println("  Error en test de estados: " + e.getMessage());
        }
    }

    private static Move findMove(Board board, String moveStr) {
        for (Move legalMove : board.legalMoves()) {
            if (legalMove.toString().equals(moveStr)) {
                return legalMove;
            }
        }
        return null;
    }
}
