package com.mycompany.savasgemisi.server;

import com.mycompany.savasgemisi.common.Message;
import com.mycompany.savasgemisi.common.MessageType;
import com.mycompany.savasgemisi.common.Move;

/**
 * İki oyuncu arasındaki oyun oturumunu yöneten sınıf.
 * Bu sınıf, iki oyuncu arasındaki oyun akışını kontrol eder ve
 * oyuncular arasındaki iletişimi koordine eder.
 */
public class GameSession {
    /** Birinci oyuncunun istemci bağlantısı */
    private SClient client1;
    
    /** İkinci oyuncunun istemci bağlantısı */
    private SClient client2;
    
    /** Oyun mantığını yöneten BattleshipGame nesnesi */
    private BattleshipGame game;
    
    /** Sunucu referansı */
    private GameServer server;
    
    /**
     * Yeni bir oyun oturumu oluşturur
     * @param client1 Birinci oyuncunun istemci bağlantısı
     * @param client2 İkinci oyuncunun istemci bağlantısı
     * @param server Sunucu referansı
     */
    public GameSession(SClient client1, SClient client2, GameServer server) {
        this.client1 = client1;
        this.client2 = client2;
        this.server = server;
        this.game = new BattleshipGame();
        
        // Oyunculara ID'lerini ayarla
        this.game.setPlayer1(new Player(client1.getClientId(), "Player " + client1.getClientId()));
        this.game.setPlayer2(new Player(client2.getClientId(), "Player " + client2.getClientId()));
    }
    
    /**
     * Oyun oturumunu başlatır ve oyuncuları hazırlar
     */
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
    
    /**
     * Oyuncunun hamlesini işler ve sonuçları oyunculara bildirir
     * @param playerId Hamle yapan oyuncunun ID'si
     * @param move Yapılan hamle
     */
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
    
    /**
     * Oyun durumunu günceller ve gerekirse oyunu sonlandırır
     */
    public void updateGameState() {
        // Oyun durumunu kontrol et
        if (game.getState() == BattleshipGame.GameState.GAME_OVER) {
            // Oyun bitti, kazananı belirle
            Player winner = game.getWinner();
            endSession(winner);
        }
    }
    
    /**
     * Oyun oturumunu beraberlik durumunda sonlandırır
     */
    public void endSession() {
        endSession(null);
    }
    
    /**
     * Oyun oturumunu kazanan belirli olarak sonlandırır
     * @param winner Kazanan oyuncu (null ise beraberlik)
     */
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
    
    /**
     * Her iki oyuncuya güncel oyun durumunu gönderir
     */
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
    
    /**
     * Birinci oyuncunun istemci bağlantısını döndürür
     * @return Birinci oyuncunun istemci bağlantısı
     */
    public SClient getClient1() {
        return client1;
    }
    
    /**
     * İkinci oyuncunun istemci bağlantısını döndürür
     * @return İkinci oyuncunun istemci bağlantısı
     */
    public SClient getClient2() {
        return client2;
    }
} 