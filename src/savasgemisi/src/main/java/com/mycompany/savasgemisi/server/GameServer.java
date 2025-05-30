package com.mycompany.savasgemisi.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.mycompany.savasgemisi.common.Message;
import com.mycompany.savasgemisi.common.MessageType;
import com.mycompany.savasgemisi.common.Move;

/**
 * Sunucu ana sınıfı.
 * Bu sınıf, oyun sunucusunun temel işlevlerini yönetir:
 * - İstemci bağlantılarını kabul eder
 * - Oyuncuları eşleştirir
 * - Oyun oturumlarını yönetir
 * - İstemciler arası iletişimi koordine eder
 */
public class GameServer {
    private int port;
    private ServerSocket serverSocket;
    private List<SClient> waitingClients = new ArrayList<>();
    private List<SClient> connectedClients = new ArrayList<>();
    private Map<Integer, GameSession> gameSessions = new HashMap<>();
    private AtomicInteger clientIdCounter = new AtomicInteger(1);
    private boolean running = false;
    
    /**
     * GameServer yapıcı metodu
     * @param port Sunucunun dinleyeceği port numarası
     */
    public GameServer(int port) {
        this.port = port;
    }
    
    /**
     * Sunucuyu başlatır ve istemci bağlantılarını kabul etmeye başlar
     */
    public void startServer() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("Sunucu " + port + " portunda başlatıldı. Bağlantılar bekleniyor...");
            
            acceptConnections();
        } catch (IOException e) {
            System.err.println("Sunucu başlatılırken hata: " + e.getMessage());
        }
    }
    
    /**
     * Yeni istemci bağlantılarını kabul eden thread'i başlatır
     * Her yeni bağlantı için bir SClient nesnesi oluşturur
     */
    public void acceptConnections() {
        new Thread(() -> {
            try {
                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Yeni bağlantı: " + clientSocket.getInetAddress());
                    
                    int clientId = clientIdCounter.getAndIncrement();
                    SClient client = new SClient(clientSocket, this, clientId);
                    
                    synchronized (connectedClients) {
                        connectedClients.add(client);
                    }
                    
                    client.listen();
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("Bağlantı kabul edilirken hata: " + e.getMessage());
                }
            }
        }).start();
    }
    
    /**
     * Yeni bağlanan istemciyi işler ve gerekirse oyun eşleştirmesi yapar
     * @param client Bağlanan istemci
     */
    public void clientConnected(SClient client) {
        System.out.println("İstemci bağlandı: ID=" + client.getClientId());
        
        synchronized (waitingClients) {
            waitingClients.add(client);
            
            if (waitingClients.size() >= 2) {
                SClient client1 = waitingClients.get(0);
                SClient client2 = waitingClients.get(1);
                
                GameSession session = new GameSession(client1, client2, this);
                
                synchronized (gameSessions) {
                    gameSessions.put(client1.getClientId(), session);
                    gameSessions.put(client2.getClientId(), session);
                }
                
                waitingClients.remove(client1);
                waitingClients.remove(client2);
                
                session.startSession();
            } else {
                try {
                    client.sendMessage(Message.generateMessage(MessageType.GAME_UPDATE, "Rakip bekleniyor..."));
                } catch (IOException e) {
                    System.err.println("Bekleme mesajı gönderilirken hata: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Bağlantısı kesilen istemciyi işler ve gerekli temizlik işlemlerini yapar
     * @param client Bağlantısı kesilen istemci
     */
    public void clientDisconnected(SClient client) {
        System.out.println("İstemci bağlantısı kesildi: ID=" + client.getClientId());
        
        synchronized (connectedClients) {
            connectedClients.remove(client);
        }
        
        synchronized (gameSessions) {
            GameSession session = gameSessions.get(client.getClientId());
            if (session != null) {
                session.endSession();
                
                gameSessions.remove(client.getClientId());
                
                SClient otherClient = (session.getClient1() == client) ? session.getClient2() : session.getClient1();
                gameSessions.remove(otherClient.getClientId());
            }
        }
        
        synchronized (waitingClients) {
            waitingClients.remove(client);
        }
    }
    
    /**
     * İstemcinin oyun başlatma isteğini işler
     * @param client İstekte bulunan istemci
     */
    public void requestGameStart(SClient client) {
        synchronized (waitingClients) {
            if (!waitingClients.contains(client)) {
                waitingClients.add(client);
            }
            if (waitingClients.size() >= 2) {
                SClient client1 = waitingClients.get(0);
                SClient client2 = waitingClients.get(1);
                GameSession session = new GameSession(client1, client2, this);
                synchronized (gameSessions) {
                    gameSessions.put(client1.getClientId(), session);
                    gameSessions.put(client2.getClientId(), session);
                }
                waitingClients.remove(client1);
                waitingClients.remove(client2);
                session.startSession();
            } else {
                try {
                    client.sendMessage(Message.generateMessage(MessageType.GAME_UPDATE, "Rakip bekleniyor..."));
                } catch (IOException e) {
                    System.err.println("Bekleme mesajı gönderilirken hata: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Oyuncunun hamlesini işler ve ilgili oyun oturumuna iletir
     * @param client Hamle yapan istemci
     * @param move Yapılan hamle
     */
    public void processPlayerMove(SClient client, Move move) {
        synchronized (gameSessions) {
            GameSession session = gameSessions.get(client.getClientId());
            if (session != null) {
                session.processPlayerMove(client.getClientId(), move);
            } else {
                try {
                    client.sendMessage(Message.generateMessage(
                        MessageType.ERROR, 
                        "Aktif bir oyunda değilsiniz."
                    ));
                } catch (IOException e) {
                    System.err.println("Hata mesajı gönderilirken hata: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Oyun oturumunu sonlandırır ve kaynakları temizler
     * @param session Sonlandırılacak oyun oturumu
     */
    public void endGameSession(GameSession session) {
        synchronized (gameSessions) {
            gameSessions.remove(session.getClient1().getClientId());
            gameSessions.remove(session.getClient2().getClientId());
        }
    }
    
    /**
     * Tüm bağlı istemcilere mesaj gönderir
     * @param msg Gönderilecek mesaj
     */
    public void broadcastMessage(String msg) {
        synchronized (connectedClients) {
            for (SClient client : connectedClients) {
                try {
                    client.sendMessage(msg);
                } catch (IOException e) {
                    System.err.println("Toplu mesaj gönderilirken hata: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Belirli bir istemciye mesaj gönderir
     * @param clientId Hedef istemci ID'si
     * @param msg Gönderilecek mesaj
     */
    public void sendToClient(int clientId, String msg) {
        synchronized (connectedClients) {
            for (SClient client : connectedClients) {
                if (client.getClientId() == clientId) {
                    try {
                        client.sendMessage(msg);
                    } catch (IOException e) {
                        System.err.println("İstemciye mesaj gönderilirken hata: " + e.getMessage());
                    }
                    break;
                }
            }
        }
    }
    
    /**
     * Sunucuyu güvenli bir şekilde kapatır
     * Tüm bağlantıları ve kaynakları temizler
     */
    public void shutdown() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            synchronized (connectedClients) {
                for (SClient client : connectedClients) {
                    client.disconnect();
                }
                connectedClients.clear();
            }
            
            waitingClients.clear();
            gameSessions.clear();
        } catch (IOException e) {
            System.err.println("Sunucu kapatılırken hata: " + e.getMessage());
        }
    }
} 