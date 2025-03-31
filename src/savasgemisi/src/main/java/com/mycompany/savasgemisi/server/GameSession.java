package com.mycompany.savasgemisi.server;

import com.mycompany.savasgemisi.common.Message;
import com.mycompany.savasgemisi.common.MessageType;
import com.mycompany.savasgemisi.common.Move;

/**
 * İki oyuncu arasındaki oyun oturumunu yöneten sınıf.
 */
public class GameSession {
    private SClient client1;
    private SClient client2;
    private BattleshipGame game;
    private GameServer server;
    
    public GameSession(SClient client1, SClient client2, GameServer server) {
        this.client1 = client1;
        this.client2 = client2;
        this.server = server;
        this.game = new BattleshipGame();
        
        // Oyunculara ID'lerini ayarla
        this.game.setPlayer1(new Player(client1.getClientId(), "Player " + client1.getClientId()));
        this.game.setPlayer2(new Player(client2.getClientId(), "Player " + client2.getClientId()));
    }
    
    public void startSession() {
        // Oyunu başlat
        game.initializeBoards();
        
        // Gemileri yerleştir
        game.placeShips(game.getPlayer1());
        game.placeShips(game.getPlayer2());
        
        // Oyuncuları bilgilendir
        String startMsg = Message.generateMessage(MessageType.GAME_START, "Oyun başladı!");
        try {
            client1.sendMessage(startMsg);
            client2.sendMessage(startMsg);
            
            // Tahta durumlarını gönder
            broadcastGameState();
            
            // İlk oyuncuya sıra bilgisini gönder
            String turnMsg = Message.generateMessage(MessageType.GAME_UPDATE, "Sıra sizde");
            client1.sendMessage(turnMsg);
            client2.sendMessage(Message.generateMessage(MessageType.GAME_UPDATE, "Rakibin sırası"));
        } catch (Exception e) {
            System.err.println("Oyun oturumu başlatılırken hata: " + e.getMessage());
        }
    }
    
    public void processPlayerMove(int playerId, Move move) {
        // Hangi oyuncunun hamle yaptığını belirle
        Player currentPlayer = (playerId == game.getPlayer1().getId()) 
                             ? game.getPlayer1() 
                             : game.getPlayer2();
        
        // Hamleyi oyun mantığına ilet
        boolean isHit = game.makeMove(currentPlayer, move.getX(), move.getY());
        
        // Oyun durumunu güncelle
        updateGameState();
        
        // Hamle sonucunu bildir
        String resultMsg = Message.generateMessage(
            MessageType.GAME_UPDATE, 
            "Hamle: " + move.getX() + "," + move.getY() + " - " + (isHit ? "İSABET!" : "ISKA")
        );
        
        try {
            // Tüm oyunculara hamle sonucunu bildir
            SClient senderClient = (playerId == client1.getClientId()) ? client1 : client2;
            SClient otherClient = (playerId == client1.getClientId()) ? client2 : client1;
            
            senderClient.sendMessage(resultMsg);
            otherClient.sendMessage(resultMsg);
            
            // Güncel oyun durumunu gönder
            broadcastGameState();
            
            // Sıra bilgisini gönder
            int currentPlayerId = game.getCurrentPlayerId();
            if (game.getState() != BattleshipGame.GameState.GAME_OVER) {
                SClient currentTurnClient = (currentPlayerId == client1.getClientId()) ? client1 : client2;
                SClient waitingClient = (currentPlayerId == client1.getClientId()) ? client2 : client1;
                
                currentTurnClient.sendMessage(Message.generateMessage(MessageType.GAME_UPDATE, "Sıra sizde"));
                waitingClient.sendMessage(Message.generateMessage(MessageType.GAME_UPDATE, "Rakibin sırası"));
            }
        } catch (Exception e) {
            System.err.println("Oyuncu hamlesi işlenirken hata: " + e.getMessage());
        }
    }
    
    public void updateGameState() {
        // Oyun durumunu kontrol et
        if (game.getState() == BattleshipGame.GameState.GAME_OVER) {
            // Oyun bitti, kazananı belirle
            Player winner = game.getWinner();
            endSession(winner);
        }
    }
    
    public void endSession() {
        endSession(null);
    }
    
    public void endSession(Player winner) {
        try {
            String endMsg;
            if (winner != null) {
                endMsg = Message.generateMessage(
                    MessageType.GAME_OVER, 
                    "Oyun bitti! Kazanan: Player " + winner.getId()
                );
                
                // Kazanan ve kaybedene özel mesajlar
                SClient winnerClient = (winner.getId() == client1.getClientId()) ? client1 : client2;
                SClient loserClient = (winner.getId() == client1.getClientId()) ? client2 : client1;
                
                winnerClient.sendMessage(Message.generateMessage(MessageType.GAME_OVER, "Tebrikler! Kazandınız!"));
                loserClient.sendMessage(Message.generateMessage(MessageType.GAME_OVER, "Üzgünüz, kaybettiniz."));
            } else {
                endMsg = Message.generateMessage(MessageType.GAME_OVER, "Oyun bitti! Beraberlik.");
                client1.sendMessage(endMsg);
                client2.sendMessage(endMsg);
            }
            
            // Sunucuya oturumun bittiğini bildir
            server.endGameSession(this);
        } catch (Exception e) {
            System.err.println("Oyun oturumu sonlandırılırken hata: " + e.getMessage());
        }
    }
    
    public void broadcastGameState() {
        try {
            // Her oyuncuya kendi tahta durumunu ve rakip tahtasını gönder
            Player player1 = game.getPlayer1();
            Player player2 = game.getPlayer2();
            
            // Player 1 için veri
            String p1Data = player1.getBoard().serialize() + "," + player1.getOpponentView().serialize();
            client1.sendMessage(Message.generateMessage(MessageType.GAME_UPDATE, "BOARD:" + p1Data));
            
            // Player 2 için veri
            String p2Data = player2.getBoard().serialize() + "," + player2.getOpponentView().serialize();
            client2.sendMessage(Message.generateMessage(MessageType.GAME_UPDATE, "BOARD:" + p2Data));
        } catch (Exception e) {
            System.err.println("Oyun durumu yayınlanırken hata: " + e.getMessage());
        }
    }
    
    public SClient getClient1() {
        return client1;
    }
    
    public SClient getClient2() {
        return client2;
    }
} 