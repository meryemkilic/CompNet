package com.mycompany.savasgemisi.client;

import com.mycompany.savasgemisi.common.Message;
import com.mycompany.savasgemisi.common.MessageType;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * İstemci tarafı ana sınıfı.
 */
public class GameClient extends Thread {
    private Socket socket;
    private OutputStream output;
    private InputStream input;
    private GameController controller;
    private boolean connected = false;
    private boolean running = true;
    private int clientId = -1;
    
    public GameClient(GameController controller) {
        this.controller = controller;
    }
    
    public void connectToServer(String ip, int port) throws UnknownHostException, IOException {
        this.socket = new Socket(ip, port);
        this.output = socket.getOutputStream();
        this.input = socket.getInputStream();
        this.connected = true;
        
        // Sunucuya bağlantı isteği gönder
        sendMessage(Message.generateMessage(MessageType.CONNECTION_REQUEST, "CONNECT"));
    }
    
    public void sendMessage(String msg) throws IOException {
        if (!connected) {
            throw new IOException("Sunucuya bağlı değil");
        }
        
        byte[] msgBytes = msg.getBytes();
        output.write(msgBytes.length);
        output.write(msgBytes);
        output.flush();
    }
    
    public void listenForMessages() {
        if (connected) {
            this.start();
        }
    }
    
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
    
    public boolean isConnected() {
        return connected;
    }
    
    public int getClientId() {
        return clientId;
    }
} 