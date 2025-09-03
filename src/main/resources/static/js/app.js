/**
 * App.js - Main application entry point
 */

// Global game instance
let chessGame = null;

/**
 * Initialize the application when DOM is loaded
 */
document.addEventListener('DOMContentLoaded', async function() {
    console.log('🎮 Inicializando aplicación de ajedrez...');
    
    try {
        // Create the main game instance
        chessGame = new ChessGame();
        
        // Initialize the game
        await chessGame.initialize();
        
        console.log('✅ Aplicación inicializada exitosamente');
        
    } catch (error) {
        console.error('❌ Error al inicializar la aplicación:', error);
        
        // Show error message to user
        showError('Error al inicializar la aplicación. Por favor recarga la página.');
    }
});

/**
 * Handle application errors
 */
window.addEventListener('error', function(event) {
    console.error('❌ Error no manejado:', event.error);
    
    // Show generic error message
    showError('Ha ocurrido un error inesperado. Por favor recarga la página.');
});

/**
 * Handle unhandled promise rejections
 */
window.addEventListener('unhandledrejection', function(event) {
    console.error('❌ Promise rechazada no manejada:', event.reason);
    
    // Show generic error message
    showError('Ha ocurrido un error inesperado. Por favor recarga la página.');
});

/**
 * Show error message to user
 * @param {string} message - Error message to display
 */
function showError(message) {
    // Create error toast if game is not initialized yet
    const existingToast = document.getElementById('messageToast');
    
    if (existingToast && chessGame) {
        chessGame.showMessage(message, 'error');
    } else {
        // Create a simple alert as fallback
        alert(message);
    }
}

/**
 * Utility functions for global access
 */
window.ChessApp = {
    /**
     * Get current game instance
     * @returns {ChessGame} Current game instance
     */
    getGame() {
        return chessGame;
    },
    
    /**
     * Create a new game
     * @param {string} playerColor - 'WHITE' or 'BLACK'
     * @param {number} timeControl - Time in seconds
     */
    async createGame(playerColor, timeControl) {
        if (chessGame) {
            return await chessGame.api.createGame(playerColor, timeControl);
        }
        throw new Error('Game not initialized');
    },
    
    /**
     * Import a game from PGN
     * @param {string} pgn - PGN moves string
     * @param {number} whiteTime - White time in seconds
     * @param {number} blackTime - Black time in seconds
     */
    async importPgn(pgn, whiteTime, blackTime) {
        if (chessGame) {
            return await chessGame.api.importFromPgn(pgn, whiteTime, blackTime);
        }
        throw new Error('Game not initialized');
    },
    
    /**
     * Make a move
     * @param {string} move - Move in algebraic notation
     */
    async makeMove(move) {
        if (chessGame && chessGame.gameData) {
            return await chessGame.attemptMove(move);
        }
        throw new Error('No active game');
    },
    
    /**
     * Get current game state
     * @returns {Object} Game data
     */
    getGameState() {
        return chessGame ? chessGame.gameData : null;
    },
    
    /**
     * Reset to setup screen
     */
    resetToSetup() {
        if (chessGame) {
            chessGame.stopClock();
            chessGame.hideAllPanels();
            chessGame.showGameSetup();
        }
    },
    
    /**
     * Check server connection
     */
    async checkServer() {
        if (chessGame) {
            return await chessGame.api.healthCheck();
        }
        return false;
    }
};

/**
 * Development utilities (only available in debug mode)
 */
if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
    window.ChessDebug = {
        /**
         * Get internal game state for debugging
         */
        getInternalState() {
            return {
                game: chessGame,
                board: chessGame ? chessGame.board : null,
                api: chessGame ? chessGame.api : null,
                gameData: chessGame ? chessGame.gameData : null
            };
        },
        
        /**
         * Simulate a move without API call
         */
        simulateMove(from, to) {
            if (chessGame && chessGame.board) {
                chessGame.board.makeMove(from, to);
            }
        },
        
        /**
         * Set board position directly
         */
        setPosition(fen) {
            if (chessGame && chessGame.board) {
                chessGame.board.setPosition(fen);
            }
        },
        
        /**
         * Toggle board flip
         */
        flipBoard() {
            if (chessGame && chessGame.board) {
                chessGame.board.flipBoard();
            }
        },
        
        /**
         * Show test message
         */
        testMessage(message, type = 'info') {
            if (chessGame) {
                chessGame.showMessage(message, type);
            }
        },
        
        /**
         * Get chess pieces utilities
         */
        getPiecesUtil() {
            return ChessPieces;
        }
    };
    
    console.log('🔧 Utilidades de desarrollo disponibles en window.ChessDebug');
}

/**
 * Service Worker registration (for future PWA support)
 */
if ('serviceWorker' in navigator) {
    window.addEventListener('load', function() {
        // Uncomment when implementing PWA features
        /*
        navigator.serviceWorker.register('/sw.js')
            .then(function(registration) {
                console.log('✅ Service Worker registrado:', registration.scope);
            })
            .catch(function(error) {
                console.log('❌ Error registrando Service Worker:', error);
            });
        */
    });
}

/**
 * Export for testing purposes
 */
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { ChessApp: window.ChessApp };
}
