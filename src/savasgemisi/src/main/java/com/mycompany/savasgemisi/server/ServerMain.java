package com.mycompany.savasgemisi.server;

import java.util.Scanner;

/**
 * Sunucu uygulamasını başlatan ana sınıf.
 * Bu sınıf, sunucuyu başlatır ve konsol komutlarını işler.
 */
public class ServerMain {
    /**
     * Uygulamanın ana giriş noktası
     * @param args Komut satırı argümanları (opsiyonel port numarası)
     */
    public static void main(String[] args) {
        int port = 5000; // Varsayılan port
        
        try {
            if (args.length > 0) {
                port = Integer.parseInt(args[0]);
            }
            
            GameServer server = new GameServer(port);
            server.startServer();
            
            System.out.println("Savaş Gemisi Sunucusu başlatıldı. (Port: " + port + ")");
            System.out.println("Çıkış için 'quit' yazın.");
            
            Scanner scanner = new Scanner(System.in);
            String command;
            
            while (true) {
                command = scanner.nextLine();
                
                if (command.equalsIgnoreCase("quit")) {
                    System.out.println("Sunucu kapatılıyor...");
                    server.shutdown();
                    break;
                } else if (command.equalsIgnoreCase("status")) {
                    System.out.println("Sunucu çalışıyor.");
                } else {
                    System.out.println("Bilinmeyen komut. Geçerli komutlar: quit, status");
                }
            }
            
            scanner.close();
        } catch (Exception e) {
            System.err.println("Sunucu hatası: " + e.getMessage());
        }
    }
} 