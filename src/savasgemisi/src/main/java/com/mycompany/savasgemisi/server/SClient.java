package com.mycompany.savasgemisi.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.mycompany.savasgemisi.common.Message;
import com.mycompany.savasgemisi.common.MessageType;
import com.mycompany.savasgemisi.common.Move;

/**
 * Sunucu tarafında her istemci için oluşturulan thread sınıfı.
 * Her istemci bağlantısı için ayrı bir SClient nesnesi oluşturulur ve
 * bu nesne istemci ile sunucu arasındaki iletişimi yönetir.
 */
public class SClient extends Thread {
    private Socket socket;
    private OutputStream output;
    private InputStream input;
    private GameServer gameServer;
    private int clientId;
    /** Thread'in çalışma durumu */
    private boolean running = true;
    
    /**
     * Yeni bir istemci bağlantısı oluşturur
     * @param socket İstemci soketi
     * @param gameServer Sunucu referansı
     * @param clientId İstemci ID'si
     * @throws IOException Soket bağlantısı kurulamazsa
     */
    public SClient(Socket socket, GameServer gameServer, int clientId) throws IOException {
        this.socket = socket;
        this.gameServer = gameServer;
        this.clientId = clientId;
        this.output = socket.getOutputStream();
        this.input = socket.getInputStream();
    }
    
    /**
     * İstemci dinleme thread'ini başlatır
     */
    public void listen() {
        this.start();
    }
    
    /**
     * İstemciye mesaj gönderir
     * @param msg Gönderilecek mesaj
     * @throws IOException Mesaj gönderilemezse
     */
    public void sendMessage(String msg) throws IOException {
        byte[] msgBytes = msg.getBytes();
        output.write(msgBytes.length);
        output.write(msgBytes);
        output.flush();
    }
    
    /**
     * İstemci bağlantısını kapatır ve kaynakları temizler
     */
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
    
    /**
     * Gelen mesajı ayrıştırır ve uygun işleyiciye yönlendirir
     * @param msg Ayrıştırılacak mesaj
     */
    private void parseMessage(String msg) {
        try {
            Message.ParsedMessage parsedMsg = Message.parseMessage(msg);
            MessageType type = parsedMsg.getType();
            String data = parsedMsg.getData();
            
            switch (type) {
                case CONNECTION_REQUEST:
                    handleConnectionRequest(data);
                    break;
                case MOVE:
                    handleMove(data);
                    break;
                case GAME_START:
                    handleGameStart();
                    break;
                default:
                    System.out.println("Tanınmayan mesaj tipi: " + type);
            }
        } catch (Exception e) {
            System.err.println("Mesaj ayrıştırılırken hata: " + e.getMessage());
        }
    }
    
    /**
     * Bağlantı isteğini işler ve istemciye yanıt gönderir
     * @param data İstek verisi
     * @throws IOException Mesaj gönderilemezse
     */
    private void handleConnectionRequest(String data) throws IOException {
        String response = Message.generateMessage(
            MessageType.CONNECTION_REQUEST,
            "OK:" + clientId
        );
        sendMessage(response);
        
        gameServer.clientConnected(this);
    }
    
    /**
     * Oyuncu hamlesini işler
     * @param moveData Hamle verisi
     */
    private void handleMove(String moveData) {
        try {
            Move move = Move.parse(moveData);
            gameServer.processPlayerMove(this, move);
        } catch (Exception e) {
            System.err.println("Hamle işlenirken hata: " + e.getMessage());
        }
    }
    
    /**
     * Oyun başlatma isteğini işler
     */
    private void handleGameStart() {
        gameServer.requestGameStart(this);
    }
    
    /**
     * Thread'in ana döngüsü. İstemciden gelen mesajları dinler ve işler
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
                
                parseMessage(message);
            }
        } catch (IOException e) {
            System.err.println("İstemci dinleme döngüsünde hata: " + e.getMessage());
        } finally {
            gameServer.clientDisconnected(this);
            disconnect();
        }
    }
    
    /**
     * İstemci ID'sini döndürür
     * @return İstemci ID'si
     */
    public int getClientId() {
        return clientId;
    }
} 