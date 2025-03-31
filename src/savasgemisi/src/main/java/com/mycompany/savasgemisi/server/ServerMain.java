package com.mycompany.savasgemisi.server;

import java.util.Scanner;

/**
 * Sunucu uygulamasını başlatan ana sınıf.
 */
public class ServerMain {
    public static void main(String[] args) {
        int port = 5000; // Varsayılan port
        
        try {
            // Komut satırı argümanlarından port numarasını al
            if (args.length > 0) {
                port = Integer.parseInt(args[0]);
            }
            
            // Sunucuyu başlat
            GameServer server = new GameServer(port);
            server.startServer();
            
            System.out.println("Savaş Gemisi Sunucusu başlatıldı. (Port: " + port + ")");
            System.out.println("Çıkış için 'quit' yazın.");
            
            // Konsol komutlarını dinle
            Scanner scanner = new Scanner(System.in);
            String command;
            
            while (true) {
                command = scanner.nextLine();
                
                if (command.equalsIgnoreCase("quit")) {
                    System.out.println("Sunucu kapatılıyor...");
                    server.shutdown();
                    break;
                } else if (command.equalsIgnoreCase("status")) {
                    // Burada sunucu durumunu gösterebilirsiniz
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