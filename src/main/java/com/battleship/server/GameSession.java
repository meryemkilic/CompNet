package com.battleship.server;

import com.battleship.common.*;

import java.io.IOException;
import java.net.Socket;

/**
 * İki oyuncu arasındaki bir oyun oturumunu yöneten sınıf.
 */
public class GameSession implements Runnable {
    private static final int BOARD_SIZE = 10;
    private final int sessionId;
    private final PlayerHandler player1Handler;
    private final PlayerHandler player2Handler;
    private GameState gameState;
    private volatile boolean running;
    
    /**
     * Yeni bir oyun oturumu oluşturur.
     * @param sessionId Oturum ID'si
     * @param player1Socket 1. oyuncunun soket bağlantısı
     * @param player2Socket 2. oyuncunun soket bağlantısı
     * @throws IOException I/O hatası durumunda fırlatılır
     */
    public GameSession(int sessionId, Socket player1Socket, Socket player2Socket) throws IOException {
        this.sessionId = sessionId;
        this.running = true;
        
        // Oyuncu işleyicilerini oluştur
        this.player1Handler = new PlayerHandler(player1Socket, 1);
        this.player2Handler = new PlayerHandler(player2Socket, 2);
        
        System.out.println("Oturum " + sessionId + " başladı: " + player1Handler.getPlayerName() + " vs " + player2Handler.getPlayerName());
    }
    
    @Override
    public void run() {
        try {
            // Oyunu hazırla
            initializeGame();
            
            // Gemi yerleştirme aşaması
            setupShips();
            
            // Oyun bitene kadar devam et
            while (running && !gameState.isGameOver() && 
                  player1Handler.isConnected() && player2Handler.isConnected()) {
                // Oyun aşamasını PLAYING olarak ayarla
                gameState.setPhase(GameState.GamePhase.PLAYING);
                
                // Sıradaki oyuncuya sırasını bildir
                PlayerHandler currentPlayer = getCurrentPlayerHandler();
                PlayerHandler opponent = getOpponentHandler(currentPlayer.getPlayerId());
                
                currentPlayer.sendMessage(new MessageProtocol(MessageProtocol.MessageType.YOUR_TURN, null));
                opponent.sendMessage(new MessageProtocol(MessageProtocol.MessageType.OPPONENT_TURN, currentPlayer.getPlayerName()));
                
                // Hamleyi bekle
                MessageProtocol moveMessage = currentPlayer.receiveMessage();
                
                if (moveMessage == null || moveMessage.getType() != MessageProtocol.MessageType.MOVE) {
                    // Hatalı mesaj veya bağlantı kesildi
                    running = false;
                    break;
                }
                
                // Hamleyi işle
                Move move = (Move) moveMessage.getData();
                processMove(move, currentPlayer, opponent);
                
                // Oyun durumunu kontrol et ve gerekirse sırayı değiştir
                if (!gameState.isGameOver()) {
                    gameState.nextTurn();
                }
            }
            
            // Oyun bitti
            if (gameState.isGameOver()) {
                finishGame();
            }
            
        } catch (Exception e) {
            System.err.println("Oturum " + sessionId + " hata ile sonlandı: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Bağlantıları kapat
            cleanup();
        }
    }
    
    /**
     * Oyunu başlatır ve gerekli yapılandırmaları yapar.
     */
    private void initializeGame() {
        // Oyun durumunu oluştur
        gameState = new GameState(player1Handler.getPlayerName(), player2Handler.getPlayerName(), BOARD_SIZE);
        
        // Başlangıç durumunu oyunculara bildir
        MessageProtocol startGameMessage = new MessageProtocol(MessageProtocol.MessageType.START_GAME, gameState);
        player1Handler.sendMessage(startGameMessage);
        player2Handler.sendMessage(startGameMessage);
    }
    
    /**
     * Gemilerin yerleştirilme aşamasını yönetir.
     */
    private void setupShips() {
        // Oyun aşamasını SETUP olarak ayarla
        gameState.setPhase(GameState.GamePhase.SETUP);
        
        // Oyunculara gemi yerleştirme aşamasını bildir
        MessageProtocol placeShipsMessage = new MessageProtocol(MessageProtocol.MessageType.PLACE_SHIPS, null);
        player1Handler.sendMessage(placeShipsMessage);
        player2Handler.sendMessage(placeShipsMessage);
        
        // 1. oyuncunun gemilerini yerleştirmesini bekle
        waitForPlayerToPlaceShips(player1Handler, gameState.getPlayer1Board());
        
        // 2. oyuncunun gemilerini yerleştirmesini bekle
        waitForPlayerToPlaceShips(player2Handler, gameState.getPlayer2Board());
        
        // Gemilerin yerleştirildiğini bildir
        player1Handler.sendMessage(new MessageProtocol(MessageProtocol.MessageType.SHIPS_PLACED, null));
        player2Handler.sendMessage(new MessageProtocol(MessageProtocol.MessageType.SHIPS_PLACED, null));
    }
    
    /**
     * Bir oyuncunun gemilerini yerleştirmesini bekler.
     * @param playerHandler Oyuncu işleyicisi
     * @param board Oyun tahtası
     */
    private void waitForPlayerToPlaceShips(PlayerHandler playerHandler, Board board) {
        int shipsToPlace = Ship.ShipType.values().length;
        int shipsPlaced = 0;
        
        // Tüm gemiler yerleştirilene kadar bekle
        while (shipsPlaced < shipsToPlace && playerHandler.isConnected() && running) {
            MessageProtocol message = playerHandler.receiveMessage();
            
            if (message == null || message.getType() != MessageProtocol.MessageType.MOVE) {
                running = false;
                break;
            }
            
            Move move = (Move) message.getData();
            
            if (move.getType() == Move.MoveType.PLACE_SHIP) {
                Ship ship = move.getShip();
                boolean placed = board.placeShip(ship, move.getX(), move.getY(), move.isHorizontal());
                
                if (placed) {
                    shipsPlaced++;
                    playerHandler.sendMessage(new MessageProtocol(MessageProtocol.MessageType.MOVE_RESULT, true));
                } else {
                    playerHandler.sendMessage(new MessageProtocol(MessageProtocol.MessageType.MOVE_RESULT, false));
                }
            }
        }
    }
    
    /**
     * Bir hamleyi işler.
     * @param move Hamle
     * @param currentPlayer Hamleyi yapan oyuncu
     * @param opponent Rakip oyuncu
     */
    private void processMove(Move move, PlayerHandler currentPlayer, PlayerHandler opponent) {
        if (move.getType() == Move.MoveType.SHOOT) {
            // Atış sonucunu hesapla
            Board opponentBoard = gameState.getOpponentBoard(currentPlayer.getPlayerId());
            Board.ShotResult result = opponentBoard.shoot(move.getX(), move.getY());
            
            // Atış sonucunu oyunculara bildir
            currentPlayer.sendMessage(MessageProtocol.createMoveResultMessage(result));
            opponent.sendMessage(MessageProtocol.createMoveMessage(move));
            
            // Güncellenen oyun durumunu gönder
            MessageProtocol gameStateMessage = MessageProtocol.createGameStateMessage(gameState);
            player1Handler.sendMessage(gameStateMessage);
            player2Handler.sendMessage(gameStateMessage);
            
            // Tüm gemiler battıysa oyunu bitir
            if (opponentBoard.areAllShipsSunk()) {
                gameState.setGameOver(currentPlayer.getPlayerId());
            }
        }
    }
    
    /**
     * Oyun bitiminde gerekli işlemleri yapar.
     */
    private void finishGame() {
        gameState.setPhase(GameState.GamePhase.FINISHED);
        
        // Oyun sonu mesajını gönder
        MessageProtocol gameOverMessage = MessageProtocol.createGameOverMessage(gameState.getWinnerId());
        player1Handler.sendMessage(gameOverMessage);
        player2Handler.sendMessage(gameOverMessage);
        
        System.out.println("Oturum " + sessionId + " sona erdi. Kazanan: Oyuncu " + gameState.getWinnerId() + 
                           " (" + (gameState.getWinnerId() == 1 ? player1Handler.getPlayerName() : player2Handler.getPlayerName()) + ")");
    }
    
    /**
     * Bağlantıları ve kaynakları kapatır.
     */
    private void cleanup() {
        if (player1Handler != null) {
            player1Handler.closeConnection();
        }
        
        if (player2Handler != null) {
            player2Handler.closeConnection();
        }
        
        running = false;
        System.out.println("Oturum " + sessionId + " kaynakları temizlendi");
    }
    
    /**
     * Mevcut sıradaki oyuncunun işleyicisini döndürür.
     * @return PlayerHandler nesnesi
     */
    private PlayerHandler getCurrentPlayerHandler() {
        return gameState.getCurrentPlayerTurn() == 1 ? player1Handler : player2Handler;
    }
    
    /**
     * Belirtilen oyuncu ID'sine göre rakip oyuncunun işleyicisini döndürür.
     * @param playerId Oyuncu ID'si
     * @return Rakip oyuncunun PlayerHandler nesnesi
     */
    private PlayerHandler getOpponentHandler(int playerId) {
        return playerId == 1 ? player2Handler : player1Handler;
    }
    
    /**
     * Oturum ID'sini döndürür.
     * @return Oturum ID'si
     */
    public int getSessionId() {
        return sessionId;
    }
    
    /**
     * Oturumun halen çalışıp çalışmadığını döndürür.
     * @return Çalışıyorsa true, aksi halde false
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Oturumu durdurur.
     */
    public void stop() {
        running = false;
    }
} 