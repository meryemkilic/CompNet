package com.mycompany.savasgemisi.client;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * İstemci uygulamasını başlatan ana sınıf.
 * Bu sınıf, istemci arayüzünü başlatır ve sistem görünümünü ayarlar.
 */
public class ClientMain {
    /**
     * Uygulamanın ana giriş noktası
     * @param args Komut satırı argümanları (kullanılmıyor)
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Görünüm ayarlanırken hata: " + e.getMessage());
        }
        
        SwingUtilities.invokeLater(() -> {
            ClientUI clientUI = new ClientUI();
            clientUI.setVisible(true);
        });
    }
} 