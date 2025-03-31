package com.mycompany.savasgemisi.client;

import com.mycompany.savasgemisi.common.Message;
import com.mycompany.savasgemisi.common.MessageType;
import com.mycompany.savasgemisi.common.Move;
import java.io.IOException;

/**
 * İstemci tarafında oyun kontrolünü sağlayan sınıf.
 */
public class GameController {
    private GameClient client;
    private ClientUI ui;
    private boolean gameActive = false;
    private int playerId = -1;
    private boolean myTurn = false;
    
    public GameController(ClientUI ui) {
        this.ui = ui;
        this.client = new GameClient(this);
    }
    
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
    
    public void notifyGameStart(String data) {
        gameActive = true;
        ui.updateGameStatus("Oyun başladı! " + data);
    }
    
    public void notifyGameOver(String data) {
        gameActive = false;
        myTurn = false;
        ui.updateGameStatus("Oyun bitti! " + data);
        ui.showGameOverDialog(data);
    }
    
    public void notifyConnected(int clientId) {
        this.playerId = clientId;
        ui.updateGameStatus("Sunucuya bağlandı. Oyuncu ID: " + clientId);
    }
    
    public void notifyDisconnected(String reason) {
        gameActive = false;
        myTurn = false;
        ui.updateGameStatus("Bağlantı kesildi: " + reason);
        ui.showMessage("Sunucu bağlantısı kesildi: " + reason);
    }
    
    public void showError(String errorMsg) {
        ui.showMessage("Hata: " + errorMsg);
    }
    
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
    
    public void disconnect() {
        if (client != null) {
            client.disconnect();
        }
    }
    
    public boolean isGameActive() {
        return gameActive;
    }
    
    public boolean isMyTurn() {
        return myTurn;
    }
    
    public int getPlayerId() {
        return playerId;
    }
} 