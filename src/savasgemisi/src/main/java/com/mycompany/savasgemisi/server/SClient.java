package com.mycompany.savasgemisi.server;

import com.mycompany.savasgemisi.common.Message;
import com.mycompany.savasgemisi.common.MessageType;
import com.mycompany.savasgemisi.common.Move;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Sunucu tarafında her istemci için oluşturulan thread sınıfı.
 */
public class SClient extends Thread {
    private Socket socket;
    private OutputStream output;
    private InputStream input;
    private GameServer gameServer;
    private int clientId;
    private boolean running = true;
    
    public SClient(Socket socket, GameServer gameServer, int clientId) throws IOException {
        this.socket = socket;
        this.gameServer = gameServer;
        this.clientId = clientId;
        this.output = socket.getOutputStream();
        this.input = socket.getInputStream();
    }
    
    public void listen() {
        this.start();
    }
    
    public void sendMessage(String msg) throws IOException {
        byte[] msgBytes = msg.getBytes();
        output.write(msgBytes.length);
        output.write(msgBytes);
        output.flush();
    }
    
    public void disconnect() {
        running = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("İstemci bağlantısı kapatılırken hata: " + e.getMessage());
        }
    }
    
    private void parseMessage(String msg) {
        try {
            Message.ParsedMessage parsedMsg = Message.parseMessage(msg);
            MessageType type = parsedMsg.getType();
            String data = parsedMsg.getData();
            
            switch (type) {
                case CONNECTION_REQUEST:
                    // Bağlantı isteği işle
                    handleConnectionRequest(data);
                    break;
                case MOVE:
                    // Hamle işle
                    handleMove(data);
                    break;
                case GAME_START:
                    // Oyun başlatma isteği
                    handleGameStart();
                    break;
                default:
                    System.out.println("Tanınmayan mesaj tipi: " + type);
            }
        } catch (Exception e) {
            System.err.println("Mesaj ayrıştırılırken hata: " + e.getMessage());
        }
    }
    
    private void handleConnectionRequest(String data) throws IOException {
        // İstemciye bağlantısının kabul edildiğini bildir
        String response = Message.generateMessage(
            MessageType.CONNECTION_REQUEST,
            "OK:" + clientId
        );
        sendMessage(response);
        
        // Sunucuya bağlantıyı bildir
        gameServer.clientConnected(this);
    }
    
    private void handleMove(String moveData) {
        try {
            Move move = Move.parse(moveData);
            gameServer.processPlayerMove(this, move);
        } catch (Exception e) {
            System.err.println("Hamle işlenirken hata: " + e.getMessage());
        }
    }
    
    private void handleGameStart() {
        gameServer.requestGameStart(this);
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
                
                parseMessage(message);
            }
        } catch (IOException e) {
            System.err.println("İstemci dinleme döngüsünde hata: " + e.getMessage());
        } finally {
            gameServer.clientDisconnected(this);
            disconnect();
        }
    }
    
    public int getClientId() {
        return clientId;
    }
} 