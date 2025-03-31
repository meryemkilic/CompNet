package com.mycompany.savasgemisi.client;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * İstemci uygulamasını başlatan ana sınıf.
 */
public class ClientMain {
    public static void main(String[] args) {
        try {
            // Sistem görünümünü ayarla
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Görünüm ayarlanırken hata: " + e.getMessage());
        }
        
        // GUI'yi başlat
        SwingUtilities.invokeLater(() -> {
            ClientUI clientUI = new ClientUI();
            clientUI.setVisible(true);
        });
    }
} 