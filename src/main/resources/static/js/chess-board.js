/**
 * Chess Board - Visual board management and interactions
 */
class ChessBoard {
    constructor(boardElement) {
        this.boardElement = boardElement;
        this.board = [];
        this.selectedSquare = null;
        this.highlightedSquares = [];
        this.possibleMoves = [];
        this.isFlipped = false;
        this.onSquareClick = null;
        this.onPieceSelect = null;
        
        this.initializeBoard();
    }

    /**
     * Initialize the visual board
     */
    initializeBoard() {
        this.boardElement.innerHTML = '';
        this.board = Array(8).fill(null).map(() => Array(8).fill(''));
        
        for (let row = 0; row < 8; row++) {
            for (let col = 0; col < 8; col++) {
                const square = this.createSquare(row, col);
                this.boardElement.appendChild(square);
            }
        }
    }

    /**
     * Create a square element
     * @param {number} row - Row index (0-7)
     * @param {number} col - Column index (0-7)
     * @returns {HTMLElement} Square element
     */
    createSquare(row, col) {
        const square = document.createElement('div');
        square.className = `square ${ChessPieces.getSquareColor(row, col)}`;
        square.dataset.row = row;
        square.dataset.col = col;
        square.dataset.square = ChessPieces.coordsToSquare(row, col);
        
        square.addEventListener('click', (e) => {
            this.handleSquareClick(row, col, e);
        });
        
        return square;
    }

    /**
     * Handle square click
     * @param {number} row - Row index
     * @param {number} col - Column index
     * @param {Event} event - Click event
     */
    handleSquareClick(row, col, event) {
        const square = ChessPieces.coordsToSquare(row, col);
        const piece = this.board[row][col];
        
        // If there's a callback, call it
        if (this.onSquareClick) {
            this.onSquareClick(square, piece, event);
        }
        
        // Handle piece selection
        if (piece && this.onPieceSelect) {
            this.onPieceSelect(square, piece, event);
        }
    }

    /**
     * Set the board position from FEN
     * @param {string} fen - FEN string
     */
    setPosition(fen) {
        this.board = ChessPieces.fenToBoard(fen);
        this.updateVisualBoard();
    }

    /**
     * Get current position as FEN
     * @returns {string} FEN string
     */
    getPosition() {
        return ChessPieces.boardToFen(this.board);
    }

    /**
     * Update the visual representation of the board
     */
    updateVisualBoard() {
        const squares = this.boardElement.querySelectorAll('.square');
        
        squares.forEach(square => {
            const row = parseInt(square.dataset.row);
            const col = parseInt(square.dataset.col);
            const piece = this.board[row][col];
            
            // Clear previous piece
            square.textContent = '';
            
            // Add piece if present
            if (piece) {
                const pieceElement = document.createElement('span');
                pieceElement.className = 'piece';
                pieceElement.textContent = ChessPieces.getUnicode(piece);
                pieceElement.title = ChessPieces.getName(piece);
                square.appendChild(pieceElement);
            }
        });
    }

    /**
     * Select a square
     * @param {string} square - Square notation (e.g., "e4")
     */
    selectSquare(square) {
        this.clearSelection();
        this.selectedSquare = square;
        
        const coords = ChessPieces.parseSquare(square);
        if (coords) {
            const squareElement = this.getSquareElement(coords.row, coords.col);
            if (squareElement) {
                squareElement.classList.add('selected');
            }
        }
    }

    /**
     * Clear square selection
     */
    clearSelection() {
        if (this.selectedSquare) {
            const coords = ChessPieces.parseSquare(this.selectedSquare);
            if (coords) {
                const squareElement = this.getSquareElement(coords.row, coords.col);
                if (squareElement) {
                    squareElement.classList.remove('selected');
                }
            }
        }
        this.selectedSquare = null;
    }

    /**
     * Highlight squares
     * @param {Array<string>} squares - Array of square notations
     * @param {string} className - CSS class to add ('highlighted', 'possible-move', 'capture-move')
     */
    highlightSquares(squares, className = 'highlighted') {
        this.clearHighlights();
        
        squares.forEach(square => {
            const coords = ChessPieces.parseSquare(square);
            if (coords) {
                const squareElement = this.getSquareElement(coords.row, coords.col);
                if (squareElement) {
                    squareElement.classList.add(className);
                    this.highlightedSquares.push({ square, className });
                }
            }
        });
    }

    /**
     * Clear all highlights
     */
    clearHighlights() {
        this.highlightedSquares.forEach(({ square, className }) => {
            const coords = ChessPieces.parseSquare(square);
            if (coords) {
                const squareElement = this.getSquareElement(coords.row, coords.col);
                if (squareElement) {
                    squareElement.classList.remove(className);
                }
            }
        });
        this.highlightedSquares = [];
    }

    /**
     * Show possible moves for a piece
     * @param {Array<string>} moves - Array of possible move squares
     * @param {Array<string>} captures - Array of possible capture squares
     */
    showPossibleMoves(moves = [], captures = []) {
        this.clearHighlights();
        this.possibleMoves = [...moves, ...captures];
        
        if (moves.length > 0) {
            this.highlightSquares(moves, 'possible-move');
        }
        
        if (captures.length > 0) {
            this.highlightSquares(captures, 'capture-move');
        }
    }

    /**
     * Clear possible moves
     */
    clearPossibleMoves() {
        this.possibleMoves = [];
        this.clearHighlights();
    }

    /**
     * Make a move on the board
     * @param {string} from - From square
     * @param {string} to - To square
     * @param {string} promotion - Promotion piece (optional)
     */
    makeMove(from, to, promotion = null) {
        const fromCoords = ChessPieces.parseSquare(from);
        const toCoords = ChessPieces.parseSquare(to);
        
        if (!fromCoords || !toCoords) return false;
        
        const piece = this.board[fromCoords.row][fromCoords.col];
        if (!piece) return false;
        
        // Move the piece
        this.board[toCoords.row][toCoords.col] = promotion || piece;
        this.board[fromCoords.row][fromCoords.col] = '';
        
        // Handle special moves (castling, en passant, etc.)
        this.handleSpecialMoves(from, to, piece);
        
        this.updateVisualBoard();
        this.clearSelection();
        this.clearPossibleMoves();
        
        return true;
    }

    /**
     * Handle special moves like castling and en passant
     * @param {string} from - From square
     * @param {string} to - To square
     * @param {string} piece - Moving piece
     */
    handleSpecialMoves(from, to, piece) {
        const pieceType = ChessPieces.getType(piece);
        const fromCoords = ChessPieces.parseSquare(from);
        const toCoords = ChessPieces.parseSquare(to);
        
        // Castling
        if (pieceType === 'k' && Math.abs(toCoords.col - fromCoords.col) === 2) {
            const isKingSide = toCoords.col > fromCoords.col;
            const rookFromCol = isKingSide ? 7 : 0;
            const rookToCol = isKingSide ? 5 : 3;
            const color = ChessPieces.getColor(piece);
            const rook = color === 'white' ? 'R' : 'r';
            
            // Move the rook
            this.board[fromCoords.row][rookToCol] = rook;
            this.board[fromCoords.row][rookFromCol] = '';
        }
        
        // En passant capture
        if (pieceType === 'p' && fromCoords.col !== toCoords.col && 
            !this.board[toCoords.row][toCoords.col]) {
            // Remove the captured pawn
            const captureRow = fromCoords.row;
            this.board[captureRow][toCoords.col] = '';
        }
    }

    /**
     * Get square element by coordinates
     * @param {number} row - Row index
     * @param {number} col - Column index
     * @returns {HTMLElement} Square element
     */
    getSquareElement(row, col) {
        return this.boardElement.querySelector(
            `[data-row="${row}"][data-col="${col}"]`
        );
    }

    /**
     * Get piece at square
     * @param {string} square - Square notation
     * @returns {string} Piece character or empty string
     */
    getPieceAt(square) {
        const coords = ChessPieces.parseSquare(square);
        if (!coords) return '';
        return this.board[coords.row][coords.col] || '';
    }

    /**
     * Set piece at square
     * @param {string} square - Square notation
     * @param {string} piece - Piece character
     */
    setPieceAt(square, piece) {
        const coords = ChessPieces.parseSquare(square);
        if (coords) {
            this.board[coords.row][coords.col] = piece;
            this.updateVisualBoard();
        }
    }

    /**
     * Flip the board (rotate 180 degrees)
     */
    flipBoard() {
        this.isFlipped = !this.isFlipped;
        this.boardElement.style.transform = this.isFlipped ? 'rotate(180deg)' : 'rotate(0deg)';
        
        // Also rotate the pieces to keep them upright
        const pieces = this.boardElement.querySelectorAll('.piece');
        pieces.forEach(piece => {
            piece.style.transform = this.isFlipped ? 'rotate(180deg)' : 'rotate(0deg)';
        });
    }

    /**
     * Animate a move
     * @param {string} from - From square
     * @param {string} to - To square
     * @param {Function} callback - Callback when animation completes
     */
    animateMove(from, to, callback = null) {
        const fromCoords = ChessPieces.parseSquare(from);
        const toCoords = ChessPieces.parseSquare(to);
        
        if (!fromCoords || !toCoords) {
            if (callback) callback();
            return;
        }
        
        const fromElement = this.getSquareElement(fromCoords.row, fromCoords.col);
        const toElement = this.getSquareElement(toCoords.row, toCoords.col);
        const piece = fromElement.querySelector('.piece');
        
        if (!piece) {
            if (callback) callback();
            return;
        }
        
        // Calculate movement
        const fromRect = fromElement.getBoundingClientRect();
        const toRect = toElement.getBoundingClientRect();
        const deltaX = toRect.left - fromRect.left;
        const deltaY = toRect.top - fromRect.top;
        
        // Animate
        piece.style.transition = 'transform 0.3s ease-in-out';
        piece.style.transform = `translate(${deltaX}px, ${deltaY}px)`;
        piece.style.zIndex = '1000';
        
        setTimeout(() => {
            // Reset styles and update board
            piece.style.transition = '';
            piece.style.transform = '';
            piece.style.zIndex = '';
            
            this.makeMove(from, to);
            
            if (callback) callback();
        }, 300);
    }

    /**
     * Add visual effect for check
     * @param {string} kingSquare - Square of the king in check
     */
    showCheck(kingSquare) {
        const coords = ChessPieces.parseSquare(kingSquare);
        if (coords) {
            const squareElement = this.getSquareElement(coords.row, coords.col);
            if (squareElement) {
                squareElement.classList.add('in-check');
                setTimeout(() => {
                    squareElement.classList.remove('in-check');
                }, 3000);
            }
        }
    }

    /**
     * Reset board to initial position
     */
    resetToInitialPosition() {
        this.setPosition(ChessPieces.getInitialPosition());
        this.clearSelection();
        this.clearPossibleMoves();
    }

    /**
     * Get all pieces of a specific color
     * @param {string} color - 'white' or 'black'
     * @returns {Array<Object>} Array of {square, piece} objects
     */
    getPiecesByColor(color) {
        const pieces = [];
        
        for (let row = 0; row < 8; row++) {
            for (let col = 0; col < 8; col++) {
                const piece = this.board[row][col];
                if (piece && ChessPieces.getColor(piece) === color) {
                    pieces.push({
                        square: ChessPieces.coordsToSquare(row, col),
                        piece: piece
                    });
                }
            }
        }
        
        return pieces;
    }

    /**
     * Find king of specific color
     * @param {string} color - 'white' or 'black'
     * @returns {string} King square or null
     */
    findKing(color) {
        const kingChar = color === 'white' ? 'K' : 'k';
        
        for (let row = 0; row < 8; row++) {
            for (let col = 0; col < 8; col++) {
                if (this.board[row][col] === kingChar) {
                    return ChessPieces.coordsToSquare(row, col);
                }
            }
        }
        
        return null;
    }

    /**
     * Destroy the board and clean up event listeners
     */
    destroy() {
        this.boardElement.innerHTML = '';
        this.board = [];
        this.selectedSquare = null;
        this.highlightedSquares = [];
        this.possibleMoves = [];
        this.onSquareClick = null;
        this.onPieceSelect = null;
    }
}
