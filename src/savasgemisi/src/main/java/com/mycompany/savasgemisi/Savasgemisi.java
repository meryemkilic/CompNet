/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.savasgemisi;

import com.mycompany.savasgemisi.client.ClientUI;
import com.mycompany.savasgemisi.server.GameServer;
import java.util.Scanner;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Savaş Gemisi oyununun ana sınıfı.
 * Bu sınıf, kullanıcıya sunucu veya istemci başlatma seçenekleri sunar.
 *
 * @author merye
 */
public class Savasgemisi {

    /**
     * Program başlangıç noktası
     * @param args komut satırı argümanları
     */
    public static void main(String[] args) {
        try {
            // Sistem görünümünü ayarla
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Karşılama mesajı ve seçenek menüsü
            System.out.println("*************************************");
            System.out.println("*    SAVAŞ GEMİSİ OYUNU v1.0       *");
            System.out.println("*************************************");
            System.out.println("Lütfen bir seçenek seçin:");
            System.out.println("1 - Sunucu Başlat");
            System.out.println("2 - İstemci (Oyuncu) Olarak Bağlan");
            System.out.println("q - Çıkış");
            
            Scanner scanner = new Scanner(System.in);
            boolean exit = false;
            
            while (!exit) {
                System.out.print("Seçiminiz: ");
                String choice = scanner.nextLine().trim();
                
                switch (choice) {
                    case "1":
                        startServer(scanner);
                        exit = true;
                        break;
                    case "2":
                        startClient();
                        exit = true;
                        break;
                    case "q":
                    case "Q":
                        System.out.println("Programdan çıkılıyor...");
                        exit = true;
                        break;
                    default:
                        System.out.println("Geçersiz seçenek. Lütfen tekrar deneyin.");
                        break;
                }
            }
            
            scanner.close();
        } catch (Exception e) {
            System.err.println("Program başlatılırken hata: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Sunucu uygulamasını başlatır
     * @param scanner Kullanıcı girdisi için scanner
     */
    private static void startServer(Scanner scanner) {
        try {
            System.out.print("Sunucu port numarası (varsayılan: 5000): ");
            String portStr = scanner.nextLine().trim();
            
            int port = 5000; // Varsayılan port
            if (!portStr.isEmpty()) {
                try {
                    port = Integer.parseInt(portStr);
                } catch (NumberFormatException e) {
                    System.out.println("Geçersiz port numarası. Varsayılan (5000) kullanılıyor.");
                }
            }
            
            // Sunucuyu başlat
            GameServer server = new GameServer(port);
            server.startServer();
            
            System.out.println("Savaş Gemisi Sunucusu başlatıldı. (Port: " + port + ")");
            System.out.println("Çıkış için 'quit' yazın.");
            
            // Konsol komutlarını dinle
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
        } catch (Exception e) {
            System.err.println("Sunucu başlatılırken hata: " + e.getMessage());
        }
    }
    
    /**
     * İstemci uygulamasını başlatır
     */
    private static void startClient() {
        try {
            // Grafiksel kullanıcı arayüzünde istemci uygulamasını başlat
            SwingUtilities.invokeLater(() -> {
                ClientUI clientUI = new ClientUI();
                clientUI.setVisible(true);
            });
            
            System.out.println("İstemci uygulaması başlatıldı.");
            System.out.println("Not: Konsol penceresini kapatabilirsiniz. İstemci uygulaması ayrı bir pencerede çalışıyor.");
        } catch (Exception e) {
            System.err.println("İstemci başlatılırken hata: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "İstemci başlatılırken hata: " + e.getMessage(), 
                                        "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
}
