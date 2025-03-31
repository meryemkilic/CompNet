package com.battleship.common;

import java.io.Serializable;

/**
 * Oyunun mevcut durumunu temsil eden sınıf.
 */
public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Board player1Board;
    private Board player2Board;
    private int currentPlayerTurn; // 1 veya 2
    private GamePhase phase;
    private boolean gameOver;
    private int winnerId; // 0: henüz kazanan yok, 1 veya 2: kazanan oyuncu
    private String player1Name;
    private String player2Name;
    
    /**
     * Yeni bir oyun durumu oluşturur.
     * @param player1Name 1. oyuncunun adı
     * @param player2Name 2. oyuncunun adı
     * @param boardSize Tahta boyutu
     */
    public GameState(String player1Name, String player2Name, int boardSize) {
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.player1Board = new Board(boardSize);
        this.player2Board = new Board(boardSize);
        this.currentPlayerTurn = 1; // Oyun 1. oyuncu ile başlar
        this.phase = GamePhase.SETUP;
        this.gameOver = false;
        this.winnerId = 0;
    }
    
    /**
     * 1. oyuncunun tahtasını döndürür.
     * @return 1. oyuncunun tahtası
     */
    public Board getPlayer1Board() {
        return player1Board;
    }
    
    /**
     * 2. oyuncunun tahtasını döndürür.
     * @return 2. oyuncunun tahtası
     */
    public Board getPlayer2Board() {
        return player2Board;
    }
    
    /**
     * Şu anki oyuncunun sırasını döndürür.
     * @return Mevcut oyuncu ID'si (1 veya 2)
     */
    public int getCurrentPlayerTurn() {
        return currentPlayerTurn;
    }
    
    /**
     * Mevcut oyun aşamasını döndürür.
     * @return Oyun aşaması
     */
    public GamePhase getPhase() {
        return phase;
    }
    
    /**
     * Oyun aşamasını günceller.
     * @param phase Yeni oyun aşaması
     */
    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }
    
    /**
     * Oyunun bitip bitmediğini döndürür.
     * @return Oyun bittiyse true, aksi halde false
     */
    public boolean isGameOver() {
        return gameOver;
    }
    
    /**
     * Kazanan oyuncunun ID'sini döndürür.
     * @return Kazanan oyuncu ID'si (1 veya 2), henüz kazanan yoksa 0
     */
    public int getWinnerId() {
        return winnerId;
    }
    
    /**
     * 1. oyuncunun adını döndürür.
     * @return 1. oyuncunun adı
     */
    public String getPlayer1Name() {
        return player1Name;
    }
    
    /**
     * 2. oyuncunun adını döndürür.
     * @return 2. oyuncunun adı
     */
    public String getPlayer2Name() {
        return player2Name;
    }
    
    /**
     * Sırayı diğer oyuncuya geçirir.
     */
    public void nextTurn() {
        currentPlayerTurn = (currentPlayerTurn == 1) ? 2 : 1;
    }
    
    /**
     * Oyunun bittiğini ve kazanan oyuncuyu belirler.
     * @param winnerId Kazanan oyuncu ID'si (1 veya 2)
     */
    public void setGameOver(int winnerId) {
        this.gameOver = true;
        this.winnerId = winnerId;
    }
    
    /**
     * Verilen oyuncu ID'sine ait tahtayı döndürür.
     * @param playerId Oyuncu ID'si (1 veya 2)
     * @return İlgili oyuncunun tahtası
     */
    public Board getBoardByPlayerId(int playerId) {
        return (playerId == 1) ? player1Board : player2Board;
    }
    
    /**
     * Rakip oyuncunun ID'sini döndürür.
     * @param playerId Mevcut oyuncu ID'si
     * @return Rakip oyuncu ID'si
     */
    public int getOpponentId(int playerId) {
        return (playerId == 1) ? 2 : 1;
    }
    
    /**
     * Rakip oyuncunun tahtasını döndürür.
     * @param playerId Mevcut oyuncu ID'si
     * @return Rakip oyuncunun tahtası
     */
    public Board getOpponentBoard(int playerId) {
        return (playerId == 1) ? player2Board : player1Board;
    }
    
    /**
     * Oyun aşamalarını temsil eden enum.
     */
    public enum GamePhase {
        SETUP,    // Gemilerin yerleştirildiği aşama
        PLAYING,  // Atış yapılan aşama
        FINISHED  // Oyunun bittiği aşama
    }
} 