package com.battleship.common;

import java.io.Serializable;

/**
 * İstemci ve sunucu arasındaki haberleşme protokolünü tanımlar.
 */
public class MessageProtocol implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private MessageType type;
    private Object data;
    
    /**
     * Yeni bir mesaj protokolü oluşturur.
     * @param type Mesaj türü
     * @param data Mesaj verisi
     */
    public MessageProtocol(MessageType type, Object data) {
        this.type = type;
        this.data = data;
    }
    
    /**
     * Mesaj türünü döndürür.
     * @return Mesaj türü
     */
    public MessageType getType() {
        return type;
    }
    
    /**
     * Mesaj verisini döndürür.
     * @return Mesaj verisi
     */
    public Object getData() {
        return data;
    }
    
    /**
     * Mesaj türlerini tanımlayan enum.
     */
    public enum MessageType {
        CONNECT,       // Sunucuya bağlanma mesajı
        CONNECT_ACK,   // Bağlantı onay mesajı
        PLAYER_ID,     // Oyuncu ID'si bildirimi
        WAIT_OPPONENT, // Rakip bekleme durumu
        START_GAME,    // Oyun başlama mesajı
        PLACE_SHIPS,   // Gemi yerleştirme aşaması
        SHIPS_PLACED,  // Gemilerin yerleştirildiği bilgisi
        YOUR_TURN,     // Sıra bildirimi
        OPPONENT_TURN, // Rakibin sırası bildirimi
        MOVE,          // Hamle mesajı
        MOVE_RESULT,   // Hamle sonucu
        GAME_STATE,    // Oyun durumu güncellemesi
        GAME_OVER,     // Oyun sonu bildirimi
        RESTART,       // Yeni oyun isteği
        ERROR,         // Hata mesajı
        DISCONNECT     // Bağlantı kesme mesajı
    }
    
    /**
     * Bağlantı mesajı oluşturur.
     * @param playerName Oyuncu adı
     * @return MessageProtocol nesnesi
     */
    public static MessageProtocol createConnectMessage(String playerName) {
        return new MessageProtocol(MessageType.CONNECT, playerName);
    }
    
    /**
     * Oyuncu ID mesajı oluşturur.
     * @param playerId Oyuncu ID'si
     * @return MessageProtocol nesnesi
     */
    public static MessageProtocol createPlayerIdMessage(int playerId) {
        return new MessageProtocol(MessageType.PLAYER_ID, playerId);
    }
    
    /**
     * Hamle mesajı oluşturur.
     * @param move Hamle
     * @return MessageProtocol nesnesi
     */
    public static MessageProtocol createMoveMessage(Move move) {
        return new MessageProtocol(MessageType.MOVE, move);
    }
    
    /**
     * Hamle sonucu mesajı oluşturur.
     * @param result Hamle sonucu
     * @return MessageProtocol nesnesi
     */
    public static MessageProtocol createMoveResultMessage(Board.ShotResult result) {
        return new MessageProtocol(MessageType.MOVE_RESULT, result);
    }
    
    /**
     * Oyun durumu mesajı oluşturur.
     * @param gameState Oyun durumu
     * @return MessageProtocol nesnesi
     */
    public static MessageProtocol createGameStateMessage(GameState gameState) {
        return new MessageProtocol(MessageType.GAME_STATE, gameState);
    }
    
    /**
     * Oyun sonu mesajı oluşturur.
     * @param winnerId Kazanan oyuncu ID'si
     * @return MessageProtocol nesnesi
     */
    public static MessageProtocol createGameOverMessage(int winnerId) {
        return new MessageProtocol(MessageType.GAME_OVER, winnerId);
    }
    
    /**
     * Hata mesajı oluşturur.
     * @param errorMessage Hata mesajı
     * @return MessageProtocol nesnesi
     */
    public static MessageProtocol createErrorMessage(String errorMessage) {
        return new MessageProtocol(MessageType.ERROR, errorMessage);
    }
} 