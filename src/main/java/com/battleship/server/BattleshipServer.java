package com.battleship.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Savaş Gemisi oyunu sunucusu.
 */
public class BattleshipServer {
    private static final int DEFAULT_PORT = 12345;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private List<GameSession> activeSessions;
    private volatile boolean running;
    private int nextSessionId;
    
    /**
     * Yeni bir sunucu örneği oluşturur.
     */
    public BattleshipServer() {
        this.threadPool = Executors.newCachedThreadPool();
        this.activeSessions = new ArrayList<>();
        this.running = false;
        this.nextSessionId = 1;
    }
    
    /**
     * Sunucuyu belirtilen portta başlatır.
     * @param port Kullanılacak port numarası
     */
    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            
            System.out.println("Savaş Gemisi sunucusu başlatıldı. Port: " + port);
            System.out.println("Oyuncuların bağlanması bekleniyor...");
            
            // Ana döngü
            while (running) {
                try {
                    // İlk oyuncuyu bekle
                    Socket player1Socket = serverSocket.accept();
                    System.out.println("İlk oyuncu bağlandı. İkinci oyuncu bekleniyor...");
                    
                    // İkinci oyuncuyu bekle
                    Socket player2Socket = serverSocket.accept();
                    System.out.println("İkinci oyuncu bağlandı. Oyun oturumu başlatılıyor...");
                    
                    // Yeni oyun oturumu başlat
                    createGameSession(player1Socket, player2Socket);
                    
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Bağlantı kabul edilirken hata oluştu: " + e.getMessage());
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("Sunucu başlatılırken hata oluştu: " + e.getMessage());
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }
    
    /**
     * Yeni bir oyun oturumu oluşturur ve başlatır.
     * @param player1Socket İlk oyuncunun soket bağlantısı
     * @param player2Socket İkinci oyuncunun soket bağlantısı
     */
    private void createGameSession(Socket player1Socket, Socket player2Socket) {
        try {
            // Yeni oturum oluştur
            GameSession session = new GameSession(nextSessionId++, player1Socket, player2Socket);
            
            // Oturumu aktif oturumlar listesine ekle
            synchronized (activeSessions) {
                activeSessions.add(session);
            }
            
            // Oturumu ayrı bir thread'de başlat
            threadPool.execute(session);
            
            System.out.println("Yeni oyun oturumu başlatıldı: " + session.getSessionId());
            
        } catch (IOException e) {
            System.err.println("Oyun oturumu oluşturulurken hata oluştu: " + e.getMessage());
            
            // Soketleri temizle
            try {
                if (player1Socket != null && !player1Socket.isClosed()) {
                    player1Socket.close();
                }
                if (player2Socket != null && !player2Socket.isClosed()) {
                    player2Socket.close();
                }
            } catch (IOException ex) {
                System.err.println("Soketler kapatılırken hata oluştu: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Tamamlanan oyun oturumlarını temizler.
     */
    private void cleanupCompletedSessions() {
        synchronized (activeSessions) {
            activeSessions.removeIf(session -> !session.isRunning());
        }
    }
    
    /**
     * Sunucuyu kapatır ve kaynakları temizler.
     */
    public void shutdown() {
        running = false;
        
        // Aktif oturumları durdur
        synchronized (activeSessions) {
            for (GameSession session : activeSessions) {
                session.stop();
            }
            activeSessions.clear();
        }
        
        // Thread havuzunu kapat
        if (threadPool != null) {
            threadPool.shutdown();
        }
        
        // Sunucu soketini kapat
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Sunucu soketi kapatılırken hata oluştu: " + e.getMessage());
        }
        
        System.out.println("Sunucu kapatıldı");
    }
    
    /**
     * Ana metot, sunucuyu başlatır.
     * @param args Komut satırı parametreleri, ilk parametre port numarası olabilir
     */
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        
        // Port numarası verilmişse kullan
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Geçersiz port numarası, varsayılan port kullanılıyor: " + DEFAULT_PORT);
            }
        }
        
        // Sunucuyu başlat
        BattleshipServer server = new BattleshipServer();
        server.start(port);
    }
} 