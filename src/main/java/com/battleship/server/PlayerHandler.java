package com.battleship.server;

import com.battleship.common.MessageProtocol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Bir oyuncuyla iletişimi yöneten sınıf.
 */
public class PlayerHandler {
    private Socket playerSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private String playerName;
    private int playerId;
    private boolean connected;
    
    /**
     * Yeni bir PlayerHandler oluşturur.
     * @param playerSocket Oyuncunun soket bağlantısı
     * @param playerId Oyuncu ID'si
     * @throws IOException I/O hatası durumunda fırlatılır
     */
    public PlayerHandler(Socket playerSocket, int playerId) throws IOException {
        this.playerSocket = playerSocket;
        this.playerId = playerId;
        this.connected = true;
        
        // Output stream'i önce oluşturmalıyız, aksi takdirde deadlock oluşabilir
        this.outputStream = new ObjectOutputStream(playerSocket.getOutputStream());
        this.outputStream.flush();
        this.inputStream = new ObjectInputStream(playerSocket.getInputStream());
        
        // Oyuncu ID'sini bildir
        sendMessage(MessageProtocol.createPlayerIdMessage(playerId));
        
        // Oyuncu adını al
        try {
            MessageProtocol connectMessage = (MessageProtocol) inputStream.readObject();
            if (connectMessage.getType() == MessageProtocol.MessageType.CONNECT) {
                this.playerName = (String) connectMessage.getData();
                System.out.println("Oyuncu " + playerId + " bağlandı: " + playerName);
            } else {
                throw new IOException("Beklenmeyen mesaj türü: " + connectMessage.getType());
            }
        } catch (ClassNotFoundException e) {
            throw new IOException("Mesaj okunamadı", e);
        }
    }
    
    /**
     * Oyuncuya mesaj gönderir.
     * @param message Gönderilecek mesaj
     */
    public synchronized void sendMessage(MessageProtocol message) {
        try {
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            System.err.println("Oyuncu " + playerId + "'e mesaj gönderilemedi: " + e.getMessage());
            connected = false;
        }
    }
    
    /**
     * Oyuncudan mesaj alır.
     * @return Alınan mesaj veya bağlantı kesildiyse null
     */
    public MessageProtocol receiveMessage() {
        try {
            return (MessageProtocol) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Oyuncu " + playerId + "'den mesaj alınamadı: " + e.getMessage());
            connected = false;
            return null;
        }
    }
    
    /**
     * Bağlantının durumunu döndürür.
     * @return Bağlantı açıksa true, kapandıysa false
     */
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * Oyuncu adını döndürür.
     * @return Oyuncu adı
     */
    public String getPlayerName() {
        return playerName;
    }
    
    /**
     * Oyuncu ID'sini döndürür.
     * @return Oyuncu ID'si
     */
    public int getPlayerId() {
        return playerId;
    }
    
    /**
     * Bağlantıyı kapatır.
     */
    public void closeConnection() {
        try {
            if (outputStream != null) {
                // Bağlantı kesileceğini bildir
                sendMessage(new MessageProtocol(MessageProtocol.MessageType.DISCONNECT, null));
                outputStream.close();
            }
            
            if (inputStream != null) {
                inputStream.close();
            }
            
            if (playerSocket != null && !playerSocket.isClosed()) {
                playerSocket.close();
            }
            
            connected = false;
            System.out.println("Oyuncu " + playerId + " bağlantısı kapatıldı");
        } catch (IOException e) {
            System.err.println("Bağlantı kapatılırken hata oluştu: " + e.getMessage());
        }
    }
} 