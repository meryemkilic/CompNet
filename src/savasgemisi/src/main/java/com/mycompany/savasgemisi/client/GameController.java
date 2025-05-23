package com.mycompany.savasgemisi.client;

import java.io.IOException;

import com.mycompany.savasgemisi.common.Message;
import com.mycompany.savasgemisi.common.MessageType;
import com.mycompany.savasgemisi.common.Move;

/**
 * İstemci tarafında oyun kontrolünü sağlayan sınıf.
 * Bu sınıf, kullanıcı arayüzü ile sunucu arasındaki iletişimi yönetir
 * ve oyun mantığını kontrol eder.
 */
public class GameController {
    /** Sunucu ile iletişimi sağlayan istemci nesnesi */
    private GameClient client;
    
    /** Kullanıcı arayüzü referansı */
    private ClientUI ui;
    
    /** Oyunun aktif olup olmadığını belirten bayrak */
    private boolean gameActive = false;
    
    /** Oyuncunun benzersiz kimlik numarası */
    private int playerId = -1;
    
    /** Oyuncunun sırasının gelip gelmediğini belirten bayrak */
    private boolean myTurn = false;
    
    /**
     * Yeni bir oyun kontrolcüsü oluşturur
     * @param ui Kullanıcı arayüzü referansı
     */
    public GameController(ClientUI ui) {
        this.ui = ui;
        this.client = new GameClient(this);
    }
    
    /**
     * Oyunu başlatır ve sunucuya bağlanır
     */
    public void startGame() {
        ui.updateGameStatus("Sunucuya bağlanılıyor...");
        String serverIP = ui.getServerIP();
        int serverPort = ui.getServerPort();
        
        try {
            client.connectToServer(serverIP, serverPort);
            client.listenForMessages();
        } catch (IOException e) {
            ui.showMessage("Bağlantı hatası: " + e.getMessage());
        }
    }
    
    /**
     * Kullanıcının hamlesini işler ve sunucuya gönderir
     * @param x Hamlenin x koordinatı
     * @param y Hamlenin y koordinatı
     */
    public void handleUserInput(int x, int y) {
        if (!gameActive || !myTurn) {
            ui.showMessage("Şu anda hamle yapamazsınız.");
            return;
        }
        
        try {
            Move move = new Move(x, y, playerId);
            String moveMsg = Message.generateMessage(MessageType.MOVE, move.toString());
            client.sendMessage(moveMsg);
            myTurn = false; // Hamle yaptıktan sonra sırayı bekle
        } catch (IOException e) {
            ui.showMessage("Hamle gönderilirken hata: " + e.getMessage());
        }
    }
    
    /**
     * Oyun durumunu günceller
     * @param data Sunucudan gelen güncelleme verisi
     */
    public void updateGameState(String data) {
        if (data.startsWith("BOARD:")) {
            // Tahta güncellemesini işle
            String boardData = data.substring("BOARD:".length());
            ui.displayBoard(boardData);
        } else if (data.equals("Sıra sizde")) {
            myTurn = true;
            ui.updateGameStatus("Sıra sizde. Hamle yapın.");
        } else if (data.equals("Rakibin sırası")) {
            myTurn = false;
            ui.updateGameStatus("Rakibin hamlesi bekleniyor...");
        } else if (data.equals("Rakip bekleniyor...")) {
            ui.updateGameStatus("Oyun başlaması için rakip bekleniyor...");
        } else {
            // Diğer oyun güncellemeleri
            ui.updateGameStatus(data);
        }
    }
    
    /**
     * Oyun başlangıcını işler
     * @param data Sunucudan gelen başlangıç verisi
     */
    public void notifyGameStart(String data) {
        gameActive = true;
        ui.updateGameStatus("Oyun başladı! " + data);
    }
    
    /**
     * Oyun sonunu işler
     * @param data Sunucudan gelen sonuç verisi
     */
    public void notifyGameOver(String data) {
        gameActive = false;
        myTurn = false;
        ui.updateGameStatus("Oyun bitti! " + data);
        ui.showGameOverDialog(data);
    }
    
    /**
     * Sunucuya bağlantı başarılı olduğunda çağrılır
     * @param clientId Sunucudan atanan istemci ID'si
     */
    public void notifyConnected(int clientId) {
        this.playerId = clientId;
        ui.updateGameStatus("Sunucuya bağlandı. Oyuncu ID: " + clientId);
    }
    
    /**
     * Sunucu bağlantısı kesildiğinde çağrılır
     * @param reason Bağlantının kesilme nedeni
     */
    public void notifyDisconnected(String reason) {
        gameActive = false;
        myTurn = false;
        ui.updateGameStatus("Bağlantı kesildi: " + reason);
        ui.showMessage("Sunucu bağlantısı kesildi: " + reason);
    }
    
    /**
     * Hata mesajını kullanıcıya gösterir
     * @param errorMsg Hata mesajı
     */
    public void showError(String errorMsg) {
        ui.showMessage("Hata: " + errorMsg);
    }
    
    /**
     * Yeni bir oyun başlatma isteği gönderir
     */
    public void requestNewGame() {
        if (client.isConnected()) {
            try {
                String startMsg = Message.generateMessage(MessageType.GAME_START, "REQUEST");
                client.sendMessage(startMsg);
            } catch (IOException e) {
                ui.showMessage("Yeni oyun isteği gönderilirken hata: " + e.getMessage());
            }
        } else {
            ui.showMessage("Sunucuya bağlı değilsiniz.");
            startGame(); // Yeniden bağlanmayı dene
        }
    }
    
    /**
     * Sunucu bağlantısını kapatır
     */
    public void disconnect() {
        if (client != null) {
            client.disconnect();
        }
    }
    
    /**
     * Oyunun aktif olup olmadığını döndürür
     * @return Oyun aktif mi?
     */
    public boolean isGameActive() {
        return gameActive;
    }
    
    /**
     * Oyuncunun sırasının gelip gelmediğini döndürür
     * @return Sıra oyuncuda mı?
     */
    public boolean isMyTurn() {
        return myTurn;
    }
    
    /**
     * Oyuncunun ID'sini döndürür
     * @return Oyuncu ID'si
     */
    public int getPlayerId() {
        return playerId;
    }
} 