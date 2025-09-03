# Chess Notation Capabilities Report

## Resumen de Capacidades SAN/LAN

### Estado Actual del Proyecto

**✅ LAN (Long Algebraic Notation)**
- **Formato**: `e2e4`, `g1f3`, `a7a5`
- **Soporte**: Completamente implementado via `Move.toString()`
- **Uso**: Formato principal usado en todo el proyecto
- **Ubicación**: `GameController.getMoveFromAlgebraic()` parsea LAN
- **Almacenamiento**: Se almacena en formato LAN en la base de datos

**✅ SAN (Standard Algebraic Notation)**
- **Formato**: `e4`, `Nf3`, `O-O`, `Qxd5`
- **Soporte**: Parcialmente disponible
- **Métodos disponibles**:
  - `Move.getSan()` - retorna null por defecto
  - `Move.setSan(String)` - permite establecer SAN manualmente
- **Generación manual**: Posible implementar método customizado

**✅ PGN (Portable Game Notation)**
- **Soporte**: Limitado pero funcional
- **Implementación actual**: `GameController.generatePgn()` 
- **Formato generado**: `1. e2e4 e7e5 2. g1f3` (LAN dentro de PGN)
- **Capacidad**: Genera numeración correcta de movimientos

## Capacidades de Exportación/Parseo

### ✅ EXPORTAR (desde objetos Move)
```java
// LAN - Siempre funciona
String lan = move.toString(); // "e2e4"

// SAN - Requiere configuración manual
move.setSan("e4");
String san = move.getSan(); // "e4"

// PGN - Funcional con LAN
String pgn = generatePgn(movesList); // "1. e2e4 e7e5 2. g1f3"
```

### ✅ PARSEAR (hacia objetos Move)
```java
// LAN - Completamente funcional
Move mv = getMoveFromAlgebraic(board, "e2e4");

// SAN - Requiere búsqueda en movimientos legales
for (Move legalMove : board.legalMoves()) {
    if (legalMove.getSan().equals("e4")) {
        // Found the move
    }
}
```

## Implementación en el Proyecto

### Métodos Existentes
1. **`getMoveFromAlgebraic(Board, String)`**: Parsea LAN → Move
2. **`generatePgn(List<String>)`**: Genera PGN desde lista de movimientos LAN
3. **`Move.toString()`**: Convierte Move → LAN
4. **`Move.getSan()/setSan()`**: Manejo manual de SAN

### Flujo de Datos Actual
```
Usuario → LAN string → Move object → Board.doMove() → DB storage (LAN)
                ↓
            PGN generation → Response con formato PGN
```

## Recomendaciones

### ✅ Mantener LAN como formato principal
- **Razón**: Funciona perfectamente
- **Ventajas**: Sin ambigüedad, parseo simple, almacenamiento eficiente
- **Compatible**: Con todas las funciones de chesslib

### 🔄 Añadir SAN como feature opcional
- **Para**: Mejor experiencia de usuario
- **Implementar**: Método `convertToSan(Board, Move)` 
- **Usar**: Solo para display/UI, no para almacenamiento

### ✅ PGN está bien implementado
- **Funcional**: Para exportar partidas completas
- **Mejorar**: Podrían añadirse headers PGN estándar si se necesita

## Código de Ejemplo para SAN

```java
// Método para generar SAN automáticamente
private String generateSAN(Board board, Move move) {
    Piece piece = board.getPiece(move.getFrom());
    
    switch (piece.getPieceType()) {
        case PAWN:
            if (move.getTo().getFile() != move.getFrom().getFile()) {
                return move.getFrom().getFile().toString().toLowerCase() + "x" + 
                       move.getTo().toString().toLowerCase();
            }
            return move.getTo().toString().toLowerCase();
        case KNIGHT: return "N" + move.getTo().toString().toLowerCase();
        case BISHOP: return "B" + move.getTo().toString().toLowerCase();
        // etc...
    }
}
```

## Conclusión

**El proyecto EXPORTA/PARSEA:**
- ✅ **LAN**: Completamente ✅
- ✅ **SAN**: Parcialmente (con desarrollo manual) 
- ✅ **PGN**: Funcional

**Formato principal usado**: LAN (Long Algebraic Notation)
**Capacidad de extensión**: Alta para implementar SAN completo si se requiere
