package com.mycompany.savasgemisi.common;

/**
 * İstemci ve sunucu arasındaki mesaj tiplerini tanımlar.
 */
public enum MessageType {
    CONNECTION_REQUEST,    // Bağlantı isteği
    GAME_START,           // Oyun başlangıcı
    PLACE_SHIPS,          // Gemi yerleştirme
    MOVE,                 // Hamle
    GAME_UPDATE,          // Oyun durumu güncellemesi
    GAME_OVER,            // Oyun sonu
    ERROR                 // Hata
} 