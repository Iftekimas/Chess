/**
 * Chess Pieces - Unicode representation and utilities
 */
class ChessPieces {
    static WHITE_PIECES = {
        'p': '♙', // Pawn
        'r': '♖', // Rook
        'n': '♘', // Knight
        'b': '♗', // Bishop
        'q': '♕', // Queen
        'k': '♔'  // King
    };

    static BLACK_PIECES = {
        'p': '♟', // Pawn
        'r': '♜', // Rook
        'n': '♞', // Knight
        'b': '♝', // Bishop
        'q': '♛', // Queen
        'k': '♚'  // King
    };

    /**
     * Get the Unicode character for a piece
     * @param {string} piece - FEN piece character (K, Q, R, B, N, P for white; k, q, r, b, n, p for black)
     * @returns {string} Unicode character
     */
    static getUnicode(piece) {
        if (!piece || piece === ' ') return '';
        
        const isWhite = piece === piece.toUpperCase();
        const pieceType = piece.toLowerCase();
        
        if (isWhite) {
            return this.WHITE_PIECES[pieceType] || '';
        } else {
            return this.BLACK_PIECES[pieceType] || '';
        }
    }

    /**
     * Get piece color
     * @param {string} piece - FEN piece character
     * @returns {string} 'white' or 'black'
     */
    static getColor(piece) {
        if (!piece || piece === ' ') return null;
        return piece === piece.toUpperCase() ? 'white' : 'black';
    }

    /**
     * Get piece type
     * @param {string} piece - FEN piece character
     * @returns {string} Piece type (p, r, n, b, q, k)
     */
    static getType(piece) {
        if (!piece || piece === ' ') return null;
        return piece.toLowerCase();
    }

    /**
     * Get piece name in Spanish
     * @param {string} piece - FEN piece character
     * @returns {string} Piece name in Spanish
     */
    static getName(piece) {
        const type = this.getType(piece);
        const color = this.getColor(piece);
        
        const names = {
            'p': 'Peón',
            'r': 'Torre',
            'n': 'Caballo',
            'b': 'Alfil',
            'q': 'Reina',
            'k': 'Rey'
        };
        
        const colorNames = {
            'white': 'Blanco',
            'black': 'Negro'
        };
        
        if (!type || !color) return '';
        
        return `${names[type]} ${colorNames[color]}`;
    }

    /**
     * Convert FEN notation to our piece representation
     * @param {string} fen - FEN string
     * @returns {Array<Array<string>>} 8x8 board array
     */
    static fenToBoard(fen) {
        const board = Array(8).fill(null).map(() => Array(8).fill(''));
        
        if (!fen) return board;
        
        const rows = fen.split('/');
        
        for (let row = 0; row < 8; row++) {
            let col = 0;
            const fenRow = rows[row] || '';
            
            for (const char of fenRow) {
                if (char >= '1' && char <= '8') {
                    // Empty squares
                    const emptyCount = parseInt(char);
                    for (let i = 0; i < emptyCount; i++) {
                        if (col < 8) {
                            board[row][col] = '';
                            col++;
                        }
                    }
                } else {
                    // Piece
                    if (col < 8) {
                        board[row][col] = char;
                        col++;
                    }
                }
            }
        }
        
        return board;
    }

    /**
     * Convert board array to FEN notation
     * @param {Array<Array<string>>} board - 8x8 board array
     * @returns {string} FEN string
     */
    static boardToFen(board) {
        const rows = [];
        
        for (let row = 0; row < 8; row++) {
            let fenRow = '';
            let emptyCount = 0;
            
            for (let col = 0; col < 8; col++) {
                const piece = board[row][col];
                
                if (!piece || piece === '') {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fenRow += emptyCount.toString();
                        emptyCount = 0;
                    }
                    fenRow += piece;
                }
            }
            
            if (emptyCount > 0) {
                fenRow += emptyCount.toString();
            }
            
            rows.push(fenRow);
        }
        
        return rows.join('/');
    }

    /**
     * Get initial chess position in FEN
     * @returns {string} Starting position FEN
     */
    static getInitialPosition() {
        return 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR';
    }

    /**
     * Parse square notation (e.g., "e4") to coordinates
     * @param {string} square - Square notation (e.g., "e4")
     * @returns {Object} {row: number, col: number} or null if invalid
     */
    static parseSquare(square) {
        if (!square || square.length !== 2) return null;
        
        const file = square.charAt(0).toLowerCase();
        const rank = square.charAt(1);
        
        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') {
            return null;
        }
        
        const col = file.charCodeAt(0) - 'a'.charCodeAt(0);
        const row = 8 - parseInt(rank);
        
        return { row, col };
    }

    /**
     * Convert coordinates to square notation
     * @param {number} row - Row (0-7)
     * @param {number} col - Column (0-7)
     * @returns {string} Square notation (e.g., "e4")
     */
    static coordsToSquare(row, col) {
        if (row < 0 || row > 7 || col < 0 || col > 7) return null;
        
        const file = String.fromCharCode('a'.charCodeAt(0) + col);
        const rank = (8 - row).toString();
        
        return file + rank;
    }

    /**
     * Check if a square is light or dark
     * @param {number} row - Row (0-7)
     * @param {number} col - Column (0-7)
     * @returns {string} 'light' or 'dark'
     */
    static getSquareColor(row, col) {
        return (row + col) % 2 === 0 ? 'dark' : 'light';
    }

    /**
     * Get all squares of a specific color
     * @param {string} color - 'light' or 'dark'
     * @returns {Array<Object>} Array of {row, col} objects
     */
    static getSquaresByColor(color) {
        const squares = [];
        
        for (let row = 0; row < 8; row++) {
            for (let col = 0; col < 8; col++) {
                if (this.getSquareColor(row, col) === color) {
                    squares.push({ row, col });
                }
            }
        }
        
        return squares;
    }

    /**
     * Check if two squares are the same
     * @param {string} square1 - First square (e.g., "e4")
     * @param {string} square2 - Second square (e.g., "e4")
     * @returns {boolean} True if squares are the same
     */
    static isSameSquare(square1, square2) {
        return square1 === square2;
    }

    /**
     * Get distance between two squares
     * @param {string} square1 - First square
     * @param {string} square2 - Second square
     * @returns {number} Distance between squares
     */
    static getDistance(square1, square2) {
        const coords1 = this.parseSquare(square1);
        const coords2 = this.parseSquare(square2);
        
        if (!coords1 || !coords2) return -1;
        
        const dx = Math.abs(coords1.col - coords2.col);
        const dy = Math.abs(coords1.row - coords2.row);
        
        return Math.max(dx, dy);
    }

    /**
     * Check if a move is diagonal
     * @param {string} from - From square
     * @param {string} to - To square
     * @returns {boolean} True if move is diagonal
     */
    static isDiagonalMove(from, to) {
        const coords1 = this.parseSquare(from);
        const coords2 = this.parseSquare(to);
        
        if (!coords1 || !coords2) return false;
        
        const dx = Math.abs(coords1.col - coords2.col);
        const dy = Math.abs(coords1.row - coords2.row);
        
        return dx === dy && dx > 0;
    }

    /**
     * Check if a move is horizontal or vertical
     * @param {string} from - From square
     * @param {string} to - To square
     * @returns {boolean} True if move is straight
     */
    static isStraightMove(from, to) {
        const coords1 = this.parseSquare(from);
        const coords2 = this.parseSquare(to);
        
        if (!coords1 || !coords2) return false;
        
        return coords1.row === coords2.row || coords1.col === coords2.col;
    }

    /**
     * Get all squares in a line between two squares (exclusive)
     * @param {string} from - From square
     * @param {string} to - To square
     * @returns {Array<string>} Array of squares between from and to
     */
    static getSquaresBetween(from, to) {
        const coords1 = this.parseSquare(from);
        const coords2 = this.parseSquare(to);
        
        if (!coords1 || !coords2) return [];
        
        const squares = [];
        const dx = coords2.col - coords1.col;
        const dy = coords2.row - coords1.row;
        
        // Not in a line
        if (dx !== 0 && dy !== 0 && Math.abs(dx) !== Math.abs(dy)) {
            return [];
        }
        
        const steps = Math.max(Math.abs(dx), Math.abs(dy));
        if (steps <= 1) return [];
        
        const stepX = dx === 0 ? 0 : dx / Math.abs(dx);
        const stepY = dy === 0 ? 0 : dy / Math.abs(dy);
        
        for (let i = 1; i < steps; i++) {
            const row = coords1.row + (stepY * i);
            const col = coords1.col + (stepX * i);
            
            if (row >= 0 && row < 8 && col >= 0 && col < 8) {
                squares.push(this.coordsToSquare(row, col));
            }
        }
        
        return squares;
    }
}
