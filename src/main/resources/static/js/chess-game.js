/**
 * Chess Game - Main game logic and state management
 */
class ChessGame {
    constructor() {
        this.api = new ChessAPI();
        this.board = null;
        this.gameData = null;
        this.isPlayerTurn = false;
        this.selectedSquare = null;
        this.clockInterval = null;
        this.lastMoveTime = Date.now();
        
        this.initializeElements();
        this.bindEvents();
    }

    /**
     * Initialize DOM elements
     */
    initializeElements() {
        // Panels
        this.gameSetup = document.getElementById('gameSetup');
        this.importPanel = document.getElementById('importPanel');
        this.gameArea = document.getElementById('gameArea');
        this.moveHistory = document.getElementById('moveHistory');
        
        // Forms
        this.gameSetupForm = document.getElementById('gameSetupForm');
        this.importPgnForm = document.getElementById('importPgnForm');
        this.moveForm = document.getElementById('moveForm');
        
        // Buttons
        this.newGameBtn = document.getElementById('newGameBtn');
        this.importPgnBtn = document.getElementById('importPgnBtn');
        this.cancelSetup = document.getElementById('cancelSetup');
        this.cancelImport = document.getElementById('cancelImport');
        this.resignBtn = document.getElementById('resignBtn');
        this.drawBtn = document.getElementById('drawBtn');
        this.exportBtn = document.getElementById('exportBtn');
        
        // Game info elements
        this.topPlayerName = document.getElementById('topPlayerName');
        this.topPlayerColor = document.getElementById('topPlayerColor');
        this.topPlayerClock = document.getElementById('topPlayerClock');
        this.topPlayerStatus = document.getElementById('topPlayerStatus');
        
        this.bottomPlayerName = document.getElementById('bottomPlayerName');
        this.bottomPlayerColor = document.getElementById('bottomPlayerColor');
        this.bottomPlayerClock = document.getElementById('bottomPlayerClock');
        this.bottomPlayerStatus = document.getElementById('bottomPlayerStatus');
        
        this.currentTurn = document.getElementById('currentTurn');
        this.gameStatus = document.getElementById('gameStatus');
        this.moveCount = document.getElementById('moveCount');
        this.moveInput = document.getElementById('moveInput');
        this.movesList = document.getElementById('movesList');
        
        // Overlays
        this.loadingOverlay = document.getElementById('loadingOverlay');
        this.messageToast = document.getElementById('messageToast');
        
        // Initialize chess board
        const boardElement = document.getElementById('chessBoard');
        this.board = new ChessBoard(boardElement);
        this.board.onSquareClick = this.handleSquareClick.bind(this);
    }

    /**
     * Bind event handlers
     */
    bindEvents() {
        // Header buttons
        this.newGameBtn.addEventListener('click', () => this.showGameSetup());
        this.importPgnBtn.addEventListener('click', () => this.showImportPanel());
        
        // Setup form events
        this.gameSetupForm.addEventListener('submit', (e) => this.handleGameSetup(e));
        this.cancelSetup.addEventListener('click', () => this.hideAllPanels());
        
        // Import form events
        this.importPgnForm.addEventListener('submit', (e) => this.handleImportPgn(e));
        this.cancelImport.addEventListener('click', () => this.hideAllPanels());
        
        // Game control events
        this.moveForm.addEventListener('submit', (e) => this.handleMoveSubmit(e));
        this.resignBtn.addEventListener('click', () => this.handleResign());
        this.drawBtn.addEventListener('click', () => this.handleDrawOffer());
        this.exportBtn.addEventListener('click', () => this.handleExportPgn());
        
        // Keyboard shortcuts
        document.addEventListener('keydown', (e) => this.handleKeydown(e));
    }

    /**
     * Show game setup panel
     */
    showGameSetup() {
        this.hideAllPanels();
        this.gameSetup.style.display = 'flex';
        this.gameSetup.classList.add('fade-in');
    }

    /**
     * Show import PGN panel
     */
    showImportPanel() {
        this.hideAllPanels();
        this.importPanel.style.display = 'flex';
        this.importPanel.classList.add('fade-in');
    }

    /**
     * Hide all panels
     */
    hideAllPanels() {
        this.gameSetup.style.display = 'none';
        this.importPanel.style.display = 'none';
        this.gameArea.style.display = 'none';
        this.moveHistory.style.display = 'none';
    }

    /**
     * Show game area
     */
    showGameArea() {
        this.hideAllPanels();
        this.gameArea.style.display = 'grid';
        this.moveHistory.style.display = 'block';
        this.gameArea.classList.add('fade-in');
        this.moveHistory.classList.add('fade-in');
    }

    /**
     * Handle game setup form submission
     */
    async handleGameSetup(event) {
        event.preventDefault();
        
        const formData = new FormData(this.gameSetupForm);
        const playerColor = formData.get('playerColor');
        const timeControlValue = formData.get('timeControl');
        const timeControl = parseInt(timeControlValue);
        
        // Validate inputs
        if (!playerColor) {
            this.showMessage('Error: Debes seleccionar un color', 'error');
            return;
        }
        
        if (!timeControlValue || isNaN(timeControl) || timeControl <= 0) {
            this.showMessage('Error: Control de tiempo inválido', 'error');
            return;
        }
        
        try {
            this.showLoading('Creando nueva partida...');
            
            const gameData = await this.api.createGame(playerColor, timeControl);
            await this.startGame(gameData);
            
            this.showMessage('¡Nueva partida creada exitosamente!', 'success');
        } catch (error) {
            console.error('Error creating game:', error);
            this.showMessage('Error al crear la partida: ' + error.message, 'error');
        } finally {
            this.hideLoading();
        }
    }

    /**
     * Handle PGN import form submission
     */
    async handleImportPgn(event) {
        event.preventDefault();
        
        const formData = new FormData(this.importPgnForm);
        const pgn = formData.get('pgnInput').trim();
        const whiteTime = parseInt(formData.get('whiteTime'));
        const blackTime = parseInt(formData.get('blackTime'));
        
        if (!pgn) {
            this.showMessage('Por favor ingresa los movimientos PGN', 'warning');
            return;
        }
        
        try {
            this.showLoading('Importando partida...');
            
            const gameData = await this.api.importFromPgn(pgn, whiteTime, blackTime);
            await this.startGame(gameData);
            
            this.showMessage('¡Partida importada exitosamente!', 'success');
        } catch (error) {
            console.error('Error importing PGN:', error);
            this.showMessage('Error al importar PGN: ' + error.message, 'error');
        } finally {
            this.hideLoading();
        }
    }

    /**
     * Start a new game with given data
     */
    async startGame(gameData) {
        this.gameData = gameData;
        this.isPlayerTurn = gameData.currentTurn === gameData.playerColor;
        
        // Set up the board
        if (gameData.currentPosition) {
            this.board.setPosition(gameData.currentPosition);
        } else {
            this.board.resetToInitialPosition();
        }
        
        // Flip board if player is black
        if (gameData.playerColor === 'BLACK' || gameData.playerColor === 'Negras') {
            this.board.flipBoard();
        }
        
        // Update UI
        this.updateGameInfo();
        this.updatePlayerInfo();
        this.updateMoveHistory();
        this.startClock();
        
        // Show game area
        this.showGameArea();
        
        // Focus move input if it's player's turn
        if (this.isPlayerTurn) {
            this.moveInput.focus();
        }
    }

    /**
     * Update game information display
     */
    updateGameInfo() {
        if (!this.gameData) return;
        
        this.currentTurn.textContent = this.gameData.currentTurn || 'Desconocido';
        this.gameStatus.textContent = this.gameData.gameStatus || 'En progreso';
        this.moveCount.textContent = this.gameData.moveCount || 0;
        
        // Update turn indicator
        const isWhiteTurn = this.gameData.currentTurn === 'WHITE' || this.gameData.currentTurn === 'Blancas';
        
        // Remove active class from both players
        document.querySelector('.player-top').classList.remove('active');
        document.querySelector('.player-bottom').classList.remove('active');
        
        // Add active class to current player
        const playerIsWhite = this.gameData.playerColor === 'WHITE' || this.gameData.playerColor === 'Blancas';
        const topIsPlayerTurn = (playerIsWhite && !isWhiteTurn) || (!playerIsWhite && isWhiteTurn);
        
        if (topIsPlayerTurn) {
            document.querySelector('.player-top').classList.add('active');
        } else {
            document.querySelector('.player-bottom').classList.add('active');
        }
    }

    /**
     * Update player information display
     */
    updatePlayerInfo() {
        if (!this.gameData) return;
        
        const playerIsWhite = this.gameData.playerColor === 'WHITE' || this.gameData.playerColor === 'Blancas';
        
        // Set player names and colors
        if (playerIsWhite) {
            this.bottomPlayerName.textContent = 'Tú';
            this.bottomPlayerColor.textContent = '⚪ Blancas';
            this.bottomPlayerColor.className = 'player-color white';
            
            this.topPlayerName.textContent = 'Oponente';
            this.topPlayerColor.textContent = '⚫ Negras';
            this.topPlayerColor.className = 'player-color black';
        } else {
            this.bottomPlayerName.textContent = 'Tú';
            this.bottomPlayerColor.textContent = '⚫ Negras';
            this.bottomPlayerColor.className = 'player-color black';
            
            this.topPlayerName.textContent = 'Oponente';
            this.topPlayerColor.textContent = '⚪ Blancas';
            this.topPlayerColor.className = 'player-color white';
        }
        
        // Update clocks
        this.updateClocks();
    }

    /**
     * Update clock displays
     */
    updateClocks() {
        if (!this.gameData) return;
        
        const playerIsWhite = this.gameData.playerColor === 'WHITE' || this.gameData.playerColor === 'Blancas';
        const whiteTime = this.api.formatTime(this.gameData.whiteClock || 0);
        const blackTime = this.api.formatTime(this.gameData.blackClock || 0);
        
        if (playerIsWhite) {
            this.bottomPlayerClock.querySelector('.time-display').textContent = whiteTime;
            this.topPlayerClock.querySelector('.time-display').textContent = blackTime;
            
            // Add low-time warning
            if (this.gameData.whiteClock < 60) {
                this.bottomPlayerClock.classList.add('low-time');
            } else {
                this.bottomPlayerClock.classList.remove('low-time');
            }
            
            if (this.gameData.blackClock < 60) {
                this.topPlayerClock.classList.add('low-time');
            } else {
                this.topPlayerClock.classList.remove('low-time');
            }
        } else {
            this.bottomPlayerClock.querySelector('.time-display').textContent = blackTime;
            this.topPlayerClock.querySelector('.time-display').textContent = whiteTime;
            
            // Add low-time warning
            if (this.gameData.blackClock < 60) {
                this.bottomPlayerClock.classList.add('low-time');
            } else {
                this.bottomPlayerClock.classList.remove('low-time');
            }
            
            if (this.gameData.whiteClock < 60) {
                this.topPlayerClock.classList.add('low-time');
            } else {
                this.topPlayerClock.classList.remove('low-time');
            }
        }
    }

    /**
     * Update move history display
     */
    updateMoveHistory() {
        if (!this.gameData || !this.gameData.moves) return;
        
        this.movesList.innerHTML = '';
        
        for (let i = 0; i < this.gameData.moves.length; i++) {
            const move = this.gameData.moves[i];
            const moveNumber = Math.floor(i / 2) + 1;
            const isWhiteMove = i % 2 === 0;
            
            const moveItem = document.createElement('div');
            moveItem.className = 'move-item';
            
            const moveNumberSpan = document.createElement('span');
            moveNumberSpan.className = 'move-number';
            moveNumberSpan.textContent = isWhiteMove ? `${moveNumber}.` : '';
            
            const moveNotation = document.createElement('span');
            moveNotation.className = 'move-notation';
            moveNotation.textContent = move;
            
            moveItem.appendChild(moveNumberSpan);
            moveItem.appendChild(moveNotation);
            
            this.movesList.appendChild(moveItem);
        }
        
        // Scroll to bottom
        this.movesList.scrollTop = this.movesList.scrollHeight;
    }

    /**
     * Start the game clock
     */
    startClock() {
        if (this.clockInterval) {
            clearInterval(this.clockInterval);
        }
        
        this.lastMoveTime = Date.now();
        
        this.clockInterval = setInterval(() => {
            if (!this.gameData || this.gameData.gameStatus !== 'EN_PROGRESO') {
                this.stopClock();
                return;
            }
            
            const now = Date.now();
            const elapsed = Math.floor((now - this.lastMoveTime) / 1000);
            
            // Subtract time from current player
            const isWhiteTurn = this.gameData.currentTurn === 'WHITE' || this.gameData.currentTurn === 'Blancas';
            
            if (isWhiteTurn) {
                this.gameData.whiteClock = Math.max(0, (this.gameData.whiteClock || 0) - elapsed);
            } else {
                this.gameData.blackClock = Math.max(0, (this.gameData.blackClock || 0) - elapsed);
            }
            
            this.lastMoveTime = now;
            this.updateClocks();
            
            // Check for time out
            if ((isWhiteTurn && this.gameData.whiteClock <= 0) || 
                (!isWhiteTurn && this.gameData.blackClock <= 0)) {
                this.handleTimeOut();
            }
        }, 1000);
    }

    /**
     * Stop the game clock
     */
    stopClock() {
        if (this.clockInterval) {
            clearInterval(this.clockInterval);
            this.clockInterval = null;
        }
    }

    /**
     * Handle time out
     */
    handleTimeOut() {
        this.stopClock();
        const winner = this.gameData.currentTurn === 'WHITE' ? 'BLACK' : 'WHITE';
        this.gameData.gameStatus = 'TERMINADO';
        this.gameData.winner = winner;
        
        this.updateGameInfo();
        this.showMessage('¡Tiempo agotado! Ganó por tiempo.', 'warning');
    }

    /**
     * Handle square click on the board
     */
    handleSquareClick(square, piece) {
        if (!this.isPlayerTurn || !this.gameData || this.gameData.gameStatus !== 'EN_PROGRESO') {
            return;
        }
        
        // If no piece is selected, select this square if it has a piece of our color
        if (!this.selectedSquare) {
            if (piece && this.isPieceOurs(piece)) {
                this.selectedSquare = square;
                this.board.selectSquare(square);
                // TODO: Show possible moves (would need server API for this)
            }
            return;
        }
        
        // If clicking the same square, deselect
        if (this.selectedSquare === square) {
            this.selectedSquare = null;
            this.board.clearSelection();
            this.board.clearPossibleMoves();
            return;
        }
        
        // If clicking another piece of ours, select it instead
        if (piece && this.isPieceOurs(piece)) {
            this.selectedSquare = square;
            this.board.selectSquare(square);
            return;
        }
        
        // Otherwise, try to make a move
        const move = this.selectedSquare + square;
        this.attemptMove(move);
    }

    /**
     * Check if a piece belongs to the current player
     */
    isPieceOurs(piece) {
        if (!piece || !this.gameData) return false;
        
        const pieceColor = ChessPieces.getColor(piece);
        const playerColor = this.gameData.playerColor;
        
        if (playerColor === 'WHITE' || playerColor === 'Blancas') {
            return pieceColor === 'white';
        } else {
            return pieceColor === 'black';
        }
    }

    /**
     * Handle move form submission
     */
    async handleMoveSubmit(event) {
        event.preventDefault();
        
        const move = this.moveInput.value.trim();
        if (!move) return;
        
        await this.attemptMove(move);
        this.moveInput.value = '';
    }

    /**
     * Attempt to make a move
     */
    async attemptMove(move) {
        if (!this.gameData || !move) return;
        
        try {
            this.showLoading('Realizando movimiento...');
            
            const updatedGame = await this.api.makeMove(this.gameData.id, move);
            
            // Update game state
            this.gameData = updatedGame;
            this.isPlayerTurn = this.gameData.currentTurn === this.gameData.playerColor;
            this.lastMoveTime = Date.now();
            
            // Update board
            if (this.gameData.currentPosition) {
                this.board.setPosition(this.gameData.currentPosition);
            }
            
            // Clear selection
            this.selectedSquare = null;
            this.board.clearSelection();
            this.board.clearPossibleMoves();
            
            // Update UI
            this.updateGameInfo();
            this.updatePlayerInfo();
            this.updateMoveHistory();
            
            // Check for game end
            if (this.gameData.gameStatus !== 'EN_PROGRESO') {
                this.handleGameEnd();
            } else {
                this.showMessage('Movimiento realizado', 'success');
            }
            
        } catch (error) {
            console.error('Error making move:', error);
            this.showMessage('Movimiento inválido: ' + error.message, 'error');
        } finally {
            this.hideLoading();
        }
    }

    /**
     * Handle game end
     */
    handleGameEnd() {
        this.stopClock();
        
        let message = '';
        if (this.gameData.winner) {
            if (this.gameData.winner === this.gameData.playerColor) {
                message = '¡Felicidades! Has ganado la partida.';
            } else {
                message = 'Has perdido la partida.';
            }
        } else {
            message = 'La partida terminó en empate.';
        }
        
        this.showMessage(message, 'info');
    }

    /**
     * Handle resign button
     */
    handleResign() {
        if (confirm('¿Estás seguro de que quieres rendirte?')) {
            this.gameData.gameStatus = 'TERMINADO';
            this.gameData.winner = this.gameData.playerColor === 'WHITE' ? 'BLACK' : 'WHITE';
            this.handleGameEnd();
        }
    }

    /**
     * Handle draw offer button
     */
    handleDrawOffer() {
        this.showMessage('Oferta de empate enviada (funcionalidad por implementar)', 'info');
    }

    /**
     * Handle export PGN button
     */
    async handleExportPgn() {
        if (!this.gameData) return;
        
        try {
            this.showLoading('Exportando PGN...');
            
            const pgn = await this.api.exportPgn(this.gameData.id);
            
            // Download the PGN file
            const blob = new Blob([pgn], { type: 'text/plain' });
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `chess-game-${this.gameData.id}.pgn`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            URL.revokeObjectURL(url);
            
            this.showMessage('PGN exportado exitosamente', 'success');
            
        } catch (error) {
            console.error('Error exporting PGN:', error);
            this.showMessage('Error al exportar PGN: ' + error.message, 'error');
        } finally {
            this.hideLoading();
        }
    }

    /**
     * Handle keyboard shortcuts
     */
    handleKeydown(event) {
        // Escape key - clear selection
        if (event.key === 'Escape') {
            this.selectedSquare = null;
            this.board.clearSelection();
            this.board.clearPossibleMoves();
        }
        
        // Enter key - focus move input
        if (event.key === 'Enter' && !event.target.matches('input, textarea, button')) {
            this.moveInput.focus();
        }
    }

    /**
     * Show loading overlay
     */
    showLoading(message = 'Cargando...') {
        this.loadingOverlay.querySelector('p').textContent = message;
        this.loadingOverlay.style.display = 'flex';
    }

    /**
     * Hide loading overlay
     */
    hideLoading() {
        this.loadingOverlay.style.display = 'none';
    }

    /**
     * Show message toast
     */
    showMessage(message, type = 'info') {
        const toast = this.messageToast;
        const content = toast.querySelector('.toast-content');
        const icon = toast.querySelector('.toast-icon');
        const messageSpan = toast.querySelector('.toast-message');
        
        // Set message
        messageSpan.textContent = message;
        
        // Set type and icon
        content.className = `toast-content ${type}`;
        
        switch (type) {
            case 'success':
                icon.className = 'toast-icon fas fa-check-circle';
                break;
            case 'error':
                icon.className = 'toast-icon fas fa-exclamation-circle';
                break;
            case 'warning':
                icon.className = 'toast-icon fas fa-exclamation-triangle';
                break;
            default:
                icon.className = 'toast-icon fas fa-info-circle';
                break;
        }
        
        // Show toast
        toast.style.display = 'block';
        toast.classList.add('slide-in');
        
        // Hide after 4 seconds
        setTimeout(() => {
            toast.style.display = 'none';
            toast.classList.remove('slide-in');
        }, 4000);
    }

    /**
     * Initialize the game
     */
    async initialize() {
        try {
            // Check server connection
            const serverOnline = await this.api.healthCheck();
            if (!serverOnline) {
                this.showMessage('No se puede conectar al servidor. Verifica que esté ejecutándose.', 'error');
                return;
            }
            
            this.showMessage('¡Bienvenido al juego de ajedrez!', 'success');
            
        } catch (error) {
            console.error('Error initializing game:', error);
            this.showMessage('Error al inicializar el juego', 'error');
        }
    }
}
