package com.battleship.common;

import java.io.Serializable;

/**
 * Bir oyuncunun hamlesini temsil eden sınıf.
 */
public class Move implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int x;
    private int y;
    private int playerId;
    private MoveType type;
    private Ship ship;
    private boolean isHorizontal;
    
    /**
     * Atış yapmak için bir hamle oluşturur.
     * @param x X koordinatı
     * @param y Y koordinatı
     * @param playerId Oyuncu ID'si
     */
    public Move(int x, int y, int playerId) {
        this.x = x;
        this.y = y;
        this.playerId = playerId;
        this.type = MoveType.SHOOT;
    }
    
    /**
     * Gemi yerleştirmek için bir hamle oluşturur.
     * @param x X koordinatı
     * @param y Y koordinatı
     * @param playerId Oyuncu ID'si
     * @param ship Yerleştirilecek gemi
     * @param isHorizontal Yatay yerleştirilecekse true, dikey ise false
     */
    public Move(int x, int y, int playerId, Ship ship, boolean isHorizontal) {
        this.x = x;
        this.y = y;
        this.playerId = playerId;
        this.type = MoveType.PLACE_SHIP;
        this.ship = ship;
        this.isHorizontal = isHorizontal;
    }
    
    /**
     * X koordinatını döndürür.
     * @return X koordinatı
     */
    public int getX() {
        return x;
    }
    
    /**
     * Y koordinatını döndürür.
     * @return Y koordinatı
     */
    public int getY() {
        return y;
    }
    
    /**
     * Oyuncu ID'sini döndürür.
     * @return Oyuncu ID'si
     */
    public int getPlayerId() {
        return playerId;
    }
    
    /**
     * Hamle türünü döndürür.
     * @return Hamle türü
     */
    public MoveType getType() {
        return type;
    }
    
    /**
     * Yerleştirilecek gemiyi döndürür.
     * @return Gemi nesnesi
     */
    public Ship getShip() {
        return ship;
    }
    
    /**
     * Geminin yatay yerleştirilip yerleştirilmediğini döndürür.
     * @return Yatay ise true, dikey ise false
     */
    public boolean isHorizontal() {
        return isHorizontal;
    }
    
    /**
     * Hamle türleri
     */
    public enum MoveType {
        PLACE_SHIP, // Gemi yerleştirme hamlesi
        SHOOT       // Atış hamlesi
    }
} 