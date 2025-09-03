/**
 * Chess API - Communication with the backend REST API
 */
class ChessAPI {
    constructor(baseURL = '') {
        this.baseURL = baseURL;
        this.currentGameId = null;
    }

    /**
     * Make HTTP request
     * @param {string} url - Request URL
     * @param {Object} options - Request options
     * @returns {Promise<Object>} Response data
     */
    async request(url, options = {}) {
        const defaultOptions = {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            }
        };

        const response = await fetch(this.baseURL + url, {
            ...defaultOptions,
            ...options
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(`HTTP ${response.status}: ${error}`);
        }

        const contentType = response.headers.get('Content-Type');
        if (contentType && contentType.includes('application/json')) {
            return await response.json();
        } else {
            return await response.text();
        }
    }

    /**
     * Create a new game
     * @param {string} playerColor - 'WHITE' or 'BLACK'
     * @param {number} timeControlSeconds - Time in seconds
     * @returns {Promise<Object>} Game data
     */
    async createGame(playerColor, timeControlSeconds) {
        try {
            const formData = new URLSearchParams();
            formData.append('playerColor', playerColor);
            formData.append('timeControlSeconds', timeControlSeconds);

            const response = await this.request('/games/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: formData
            });

            // Parse the response (it's a text response with game info)
            const gameInfo = this.parseGameResponse(response);
            this.currentGameId = gameInfo.id;
            
            return gameInfo;
        } catch (error) {
            console.error('Error creating game:', error);
            throw error;
        }
    }

    /**
     * Import game from PGN
     * @param {string} pgn - PGN moves string
     * @param {number} whiteClock - White player time in seconds
     * @param {number} blackClock - Black player time in seconds
     * @returns {Promise<Object>} Game data
     */
    async importFromPgn(pgn, whiteClock, blackClock) {
        try {
            const formData = new URLSearchParams();
            formData.append('pgn', pgn);
            formData.append('whiteClock', whiteClock);
            formData.append('blackClock', blackClock);

            const response = await this.request('/games/import/pgn', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: formData
            });

            const gameInfo = this.parseGameResponse(response);
            this.currentGameId = gameInfo.id;
            
            return gameInfo;
        } catch (error) {
            console.error('Error importing PGN:', error);
            throw error;
        }
    }

    /**
     * Make a move
     * @param {number} gameId - Game ID
     * @param {string} move - Move in PGN format (e.g., "e2e4")
     * @returns {Promise<Object>} Updated game data
     */
    async makeMove(gameId, move) {
        try {
            const formData = new URLSearchParams();
            formData.append('move', move);

            const response = await this.request(`/games/${gameId}/move`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: formData
            });

            return this.parseGameResponse(response);
        } catch (error) {
            console.error('Error making move:', error);
            throw error;
        }
    }

    /**
     * Get game information
     * @param {number} gameId - Game ID
     * @returns {Promise<Object>} Game data
     */
    async getGame(gameId) {
        try {
            const response = await this.request(`/games/${gameId}`);
            return this.parseGameResponse(response);
        } catch (error) {
            console.error('Error getting game:', error);
            throw error;
        }
    }

    /**
     * Get game moves
     * @param {number} gameId - Game ID
     * @returns {Promise<Array>} Array of moves
     */
    async getMoves(gameId) {
        try {
            const response = await this.request(`/games/${gameId}/moves`);
            return this.parseMovesResponse(response);
        } catch (error) {
            console.error('Error getting moves:', error);
            throw error;
        }
    }

    /**
     * Export game as PGN
     * @param {number} gameId - Game ID
     * @returns {Promise<string>} PGN string
     */
    async exportPgn(gameId) {
        try {
            const response = await this.request(`/games/${gameId}/pgn`);
            return response; // Should be a plain text PGN
        } catch (error) {
            console.error('Error exporting PGN:', error);
            throw error;
        }
    }

    /**
     * Parse game response from server
     * @param {string} response - Server response text
     * @returns {Object} Parsed game object
     */
    parseGameResponse(response) {
        const game = {
            id: null,
            playerColor: null,
            currentTurn: null,
            gameStatus: null,
            winner: null,
            whiteClock: null,
            blackClock: null,
            currentPosition: null,
            moves: [],
            pgn: null,
            moveCount: 0
        };

        // Parse the response line by line
        const lines = response.split('\n');
        
        for (const line of lines) {
            const trimmedLine = line.trim();
            
            if (trimmedLine.startsWith('ID del Juego:')) {
                game.id = parseInt(trimmedLine.split(':')[1].trim());
            } else if (trimmedLine.startsWith('Tu Color:')) {
                game.playerColor = trimmedLine.split(':')[1].trim();
            } else if (trimmedLine.startsWith('Turno Actual:')) {
                game.currentTurn = trimmedLine.split(':')[1].trim();
            } else if (trimmedLine.startsWith('Estado:')) {
                game.gameStatus = trimmedLine.split(':')[1].trim();
            } else if (trimmedLine.startsWith('Ganador:')) {
                game.winner = trimmedLine.split(':')[1].trim();
            } else if (trimmedLine.startsWith('Tiempo Blancas:')) {
                const timeStr = trimmedLine.split(':')[1].trim();
                game.whiteClock = this.parseTimeString(timeStr);
            } else if (trimmedLine.startsWith('Tiempo Negras:')) {
                const timeStr = trimmedLine.split(':')[1].trim();
                game.blackClock = this.parseTimeString(timeStr);
            } else if (trimmedLine.startsWith('Posición Actual (FEN):')) {
                game.currentPosition = trimmedLine.split('): ')[1];
            } else if (trimmedLine.startsWith('PGN:')) {
                game.pgn = trimmedLine.split(': ')[1];
                if (game.pgn) {
                    game.moves = game.pgn.split(' ').filter(move => move.trim());
                    game.moveCount = game.moves.length;
                }
            }
        }

        return game;
    }

    /**
     * Parse moves response from server
     * @param {string} response - Server response text
     * @returns {Array} Array of move objects
     */
    parseMovesResponse(response) {
        const moves = [];
        const lines = response.split('\n');
        
        for (const line of lines) {
            const trimmedLine = line.trim();
            if (trimmedLine && !trimmedLine.startsWith('Movimientos:')) {
                // Assuming format: "1. e2e4" or just "e2e4"
                const moveMatch = trimmedLine.match(/(?:\d+\.\s*)?([a-h][1-8][a-h][1-8](?:[qrbn])?)/i);
                if (moveMatch) {
                    moves.push({
                        notation: moveMatch[1],
                        fullText: trimmedLine
                    });
                }
            }
        }
        
        return moves;
    }

    /**
     * Parse time string to seconds
     * @param {string} timeStr - Time string (e.g., "5:00" or "300")
     * @returns {number} Time in seconds
     */
    parseTimeString(timeStr) {
        if (!timeStr) return 0;
        
        // If it's just a number, return it
        if (/^\d+$/.test(timeStr)) {
            return parseInt(timeStr);
        }
        
        // If it's in mm:ss format
        const timeMatch = timeStr.match(/(\d+):(\d+)/);
        if (timeMatch) {
            const minutes = parseInt(timeMatch[1]);
            const seconds = parseInt(timeMatch[2]);
            return minutes * 60 + seconds;
        }
        
        return 0;
    }

    /**
     * Format seconds to mm:ss
     * @param {number} seconds - Time in seconds
     * @returns {string} Formatted time string
     */
    formatTime(seconds) {
        if (seconds < 0) seconds = 0;
        
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = seconds % 60;
        
        return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`;
    }

    /**
     * Check if move is valid format
     * @param {string} move - Move string
     * @returns {boolean} True if valid format
     */
    isValidMoveFormat(move) {
        // Check for standard algebraic notation: e2e4, e7e8q, etc.
        return /^[a-h][1-8][a-h][1-8][qrbn]?$/i.test(move);
    }

    /**
     * Convert move to standard format
     * @param {string} move - Move string
     * @returns {string} Standardized move
     */
    standardizeMove(move) {
        if (!move) return '';
        
        // Remove spaces and convert to lowercase
        let standardMove = move.replace(/\s+/g, '').toLowerCase();
        
        // Handle castling notation
        if (standardMove === 'o-o' || standardMove === '0-0') {
            // We'll need to determine the color and return appropriate king move
            // For now, return as is and let the server handle it
            return 'o-o';
        }
        
        if (standardMove === 'o-o-o' || standardMove === '0-0-0') {
            return 'o-o-o';
        }
        
        return standardMove;
    }

    /**
     * Get current game ID
     * @returns {number} Current game ID or null
     */
    getCurrentGameId() {
        return this.currentGameId;
    }

    /**
     * Set current game ID
     * @param {number} gameId - Game ID
     */
    setCurrentGameId(gameId) {
        this.currentGameId = gameId;
    }

    /**
     * Clear current game
     */
    clearCurrentGame() {
        this.currentGameId = null;
    }

    /**
     * Health check - verify server connection
     * @returns {Promise<boolean>} True if server is responsive
     */
    async healthCheck() {
        try {
            // Try to access a simple endpoint
            await this.request('/games/0', { method: 'GET' });
            return true;
        } catch (error) {
            // If we get a 404, server is up but game doesn't exist (which is fine)
            if (error.message.includes('404')) {
                return true;
            }
            console.warn('Server health check failed:', error);
            return false;
        }
    }
}
