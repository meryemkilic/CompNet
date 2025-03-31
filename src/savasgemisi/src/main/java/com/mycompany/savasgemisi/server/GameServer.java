package com.mycompany.savasgemisi.server;

import com.mycompany.savasgemisi.common.Message;
import com.mycompany.savasgemisi.common.MessageType;
import com.mycompany.savasgemisi.common.Move;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sunucu ana sınıfı.
 */
public class GameServer {
    private int port;
    private ServerSocket serverSocket;
    private List<SClient> waitingClients = new ArrayList<>();
    private List<SClient> connectedClients = new ArrayList<>();
    private Map<Integer, GameSession> gameSessions = new HashMap<>();
    private AtomicInteger clientIdCounter = new AtomicInteger(1);
    private boolean running = false;
    
    public GameServer(int port) {
        this.port = port;
    }
    
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
    
    public void acceptConnections() {
        new Thread(() -> {
            try {
                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Yeni bağlantı: " + clientSocket.getInetAddress());
                    
                    // Her yeni istemci için bir ID ata
                    int clientId = clientIdCounter.getAndIncrement();
                    SClient client = new SClient(clientSocket, this, clientId);
                    
                    // İstemciyi listeleye ekle
                    synchronized (connectedClients) {
                        connectedClients.add(client);
                    }
                    
                    // İstemciyi dinlemeye başla
                    client.listen();
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("Bağlantı kabul edilirken hata: " + e.getMessage());
                }
            }
        }).start();
    }
    
    public void clientConnected(SClient client) {
        System.out.println("İstemci bağlandı: ID=" + client.getClientId());
        
        // İstemciyi bekleyen oyuncular listesine ekle
        synchronized (waitingClients) {
            waitingClients.add(client);
            
            // İki bekleyen oyuncu varsa, oyun başlat
            if (waitingClients.size() >= 2) {
                SClient client1 = waitingClients.get(0);
                SClient client2 = waitingClients.get(1);
                
                // Oyun oturumu oluştur
                GameSession session = new GameSession(client1, client2, this);
                
                // Oturumu kaydet
                synchronized (gameSessions) {
                    gameSessions.put(client1.getClientId(), session);
                    gameSessions.put(client2.getClientId(), session);
                }
                
                // Bekleyen oyuncuları listeden çıkar
                waitingClients.remove(client1);
                waitingClients.remove(client2);
                
                // Oyunu başlat
                session.startSession();
            } else {
                try {
                    // Oyuncuya bekleme mesajı gönder
                    client.sendMessage(Message.generateMessage(MessageType.GAME_UPDATE, "Rakip bekleniyor..."));
                } catch (IOException e) {
                    System.err.println("Bekleme mesajı gönderilirken hata: " + e.getMessage());
                }
            }
        }
    }
    
    public void clientDisconnected(SClient client) {
        System.out.println("İstemci bağlantısı kesildi: ID=" + client.getClientId());
        
        // İstemciyi bağlı listesinden çıkar
        synchronized (connectedClients) {
            connectedClients.remove(client);
        }
        
        // İstemci bir oyun oturumunda mı kontrol et
        synchronized (gameSessions) {
            GameSession session = gameSessions.get(client.getClientId());
            if (session != null) {
                // Oyun oturumunu sonlandır
                session.endSession();
                
                // Oturumu kaldır
                gameSessions.remove(client.getClientId());
                
                // Diğer oyuncunun ID'sini de kaldır
                SClient otherClient = (session.getClient1() == client) ? session.getClient2() : session.getClient1();
                gameSessions.remove(otherClient.getClientId());
            }
        }
        
        // İstemciyi bekleyen listesinden de çıkar
        synchronized (waitingClients) {
            waitingClients.remove(client);
        }
    }
    
    public void requestGameStart(SClient client) {
        // İstemci bekleme listesindeyse, oyun başlatma isteğini işle
        // (Bu örnekte, otomatik eşleştirme kullanıldığı için ek işlem yapmıyoruz)
    }
    
    public void processPlayerMove(SClient client, Move move) {
        // İstemcinin oturumunu bul
        synchronized (gameSessions) {
            GameSession session = gameSessions.get(client.getClientId());
            if (session != null) {
                // Hamleyi oturuma ilet
                session.processPlayerMove(client.getClientId(), move);
            } else {
                try {
                    // İstemci bir oturumda değilse hata mesajı gönder
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
    
    public void endGameSession(GameSession session) {
        // Oturumu sonlandır ve kaynakları temizle
        synchronized (gameSessions) {
            gameSessions.remove(session.getClient1().getClientId());
            gameSessions.remove(session.getClient2().getClientId());
        }
    }
    
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
    
    public void shutdown() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            // Tüm istemci bağlantılarını kapat
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