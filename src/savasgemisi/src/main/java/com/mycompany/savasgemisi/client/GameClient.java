package com.mycompany.savasgemisi.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.mycompany.savasgemisi.common.Message;
import com.mycompany.savasgemisi.common.MessageType;

/**
 * İstemci tarafı ana sınıfı.
 * Bu sınıf, sunucu ile iletişimi yönetir ve gelen mesajları işler.
 */
public class GameClient extends Thread {
    /** Sunucu soketi */
    private Socket socket;
    
    /** Sunucuya veri göndermek için çıkış akışı */
    private OutputStream output;
    
    /** Sunucudan veri almak için giriş akışı */
    private InputStream input;
    
    /** Oyun kontrolcüsü referansı */
    private GameController controller;
    
    /** Bağlantı durumu */
    private boolean connected = false;
    
    /** Thread'in çalışma durumu */
    private boolean running = true;
    
    /** İstemci ID'si */
    private int clientId = -1;
    
    /**
     * Yeni bir istemci oluşturur
     * @param controller Oyun kontrolcüsü
     */
    public GameClient(GameController controller) {
        this.controller = controller;
    }
    
    /**
     * Sunucuya bağlanır
     * @param ip Sunucu IP adresi
     * @param port Sunucu port numarası
     * @throws UnknownHostException Sunucu bulunamazsa
     * @throws IOException Bağlantı kurulamazsa
     */
    public void connectToServer(String ip, int port) throws UnknownHostException, IOException {
        this.socket = new Socket(ip, port);
        this.output = socket.getOutputStream();
        this.input = socket.getInputStream();
        this.connected = true;
        
        // Sunucuya bağlantı isteği gönder
        sendMessage(Message.generateMessage(MessageType.CONNECTION_REQUEST, "CONNECT"));
    }
    
    /**
     * Sunucuya mesaj gönderir
     * @param msg Gönderilecek mesaj
     * @throws IOException Mesaj gönderilemezse
     */
    public void sendMessage(String msg) throws IOException {
        if (!connected) {
            throw new IOException("Sunucuya bağlı değil");
        }
        
        byte[] msgBytes = msg.getBytes();
        output.write(msgBytes.length);
        output.write(msgBytes);
        output.flush();
    }
    
    /**
     * Sunucudan gelen mesajları dinlemeye başlar
     */
    public void listenForMessages() {
        if (connected) {
            this.start();
        }
    }
    
    /**
     * Sunucudan gelen mesajı ayrıştırır ve uygun işleyiciye yönlendirir
     * @param msg Ayrıştırılacak mesaj
     */
    private void parseServerMessage(String msg) {
        try {
            Message.ParsedMessage parsedMsg = Message.parseMessage(msg);
            MessageType type = parsedMsg.getType();
            String data = parsedMsg.getData();
            
            switch (type) {
                case CONNECTION_REQUEST:
                    handleConnectionResponse(data);
                    break;
                case GAME_START:
                    controller.notifyGameStart(data);
                    break;
                case GAME_UPDATE:
                    controller.updateGameState(data);
                    break;
                case GAME_OVER:
                    controller.notifyGameOver(data);
                    break;
                case ERROR:
                    controller.showError(data);
                    break;
                default:
                    System.out.println("Tanınmayan mesaj tipi: " + type);
            }
        } catch (Exception e) {
            System.err.println("Mesaj ayrıştırılırken hata: " + e.getMessage());
            controller.showError("Mesaj ayrıştırma hatası: " + e.getMessage());
        }
    }
    
    /**
     * Bağlantı yanıtını işler
     * @param data Sunucudan gelen yanıt verisi
     */
    private void handleConnectionResponse(String data) {
        if (data.startsWith("OK:")) {
            try {
                clientId = Integer.parseInt(data.substring(3));
                controller.notifyConnected(clientId);
            } catch (NumberFormatException e) {
                System.err.println("Geçersiz istemci ID: " + data);
            }
        } else {
            controller.showError("Bağlantı hatası: " + data);
        }
    }
    
    /**
     * Thread'in ana döngüsü. Sunucudan gelen mesajları dinler ve işler
     */
    @Override
    public void run() {
        try {
            while (running && socket.isConnected()) {
                int size = input.read();
                if (size == -1) break;
                
                byte[] buffer = new byte[size];
                input.read(buffer);
                String message = new String(buffer);
                
                parseServerMessage(message);
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Sunucu dinleme hatası: " + e.getMessage());
                controller.notifyDisconnected("Sunucu bağlantısı kesildi: " + e.getMessage());
            }
        } finally {
            disconnect();
        }
    }
    
    /**
     * Sunucu bağlantısını kapatır ve kaynakları temizler
     */
    public void disconnect() {
        running = false;
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Soket kapatılırken hata: " + e.getMessage());
        }
    }
    
    /**
     * Bağlantı durumunu döndürür
     * @return Bağlı mı?
     */
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * İstemci ID'sini döndürür
     * @return İstemci ID'si
     */
    public int getClientId() {
        return clientId;
    }
} 