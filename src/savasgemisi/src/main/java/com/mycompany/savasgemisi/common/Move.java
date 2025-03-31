package com.mycompany.savasgemisi.common;

/**
 * Oyuncuların hamle bilgilerini temsil eden sınıf.
 */
public class Move {
    private int x;
    private int y;
    private int playerId;
    
    public Move(int x, int y, int playerId) {
        this.x = x;
        this.y = y;
        this.playerId = playerId;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getPlayerId() {
        return playerId;
    }
    
    @Override
    public String toString() {
        return playerId + "," + x + "," + y;
    }
    
    /**
     * String formatındaki hamle verisini ayrıştırarak Move nesnesine dönüştürür.
     * 
     * @param moveData "playerId,x,y" formatında hamle verisi
     * @return Ayrıştırılmış Move nesnesi
     */
    public static Move parse(String moveData) {
        String[] parts = moveData.split(",");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Geçersiz hamle verisi: " + moveData);
        }
        
        try {
            int playerId = Integer.parseInt(parts[0]);
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            return new Move(x, y, playerId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Geçersiz hamle verisi formatı: " + moveData);
        }
    }
} 