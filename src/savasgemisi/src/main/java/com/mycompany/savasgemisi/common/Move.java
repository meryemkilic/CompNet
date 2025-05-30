package com.mycompany.savasgemisi.common;

/**
 * Oyuncuların hamle bilgilerini temsil eden sınıf.
 * Her hamle, bir oyuncunun belirli bir koordinata yaptığı atışı temsil eder.
 */
public class Move {
    private int x;
    private int y;
    private int playerId;
    
    /**
     * Yeni bir hamle oluşturur
     * @param x Hamlenin x koordinatı
     * @param y Hamlenin y koordinatı
     * @param playerId Hamleyi yapan oyuncunun ID'si
     */
    public Move(int x, int y, int playerId) {
        this.x = x;
        this.y = y;
        this.playerId = playerId;
    }
    
    /**
     * Hamlenin x koordinatını döndürür
     * @return x koordinatı
     */
    public int getX() {
        return x;
    }
    
    /**
     * Hamlenin y koordinatını döndürür
     * @return y koordinatı
     */
    public int getY() {
        return y;
    }
    
    /**
     * Hamleyi yapan oyuncunun ID'sini döndürür
     * @return oyuncu ID'si
     */
    public int getPlayerId() {
        return playerId;
    }
    
    /**
     * Hamleyi string formatına dönüştürür
     * @return "playerId,x,y" formatında string
     */
    @Override
    public String toString() {
        return playerId + "," + x + "," + y;
    }
    
    /**
     * String formatındaki hamle verisini ayrıştırarak Move nesnesine dönüştürür.
     * 
     * @param moveData "playerId,x,y" formatında hamle verisi
     * @return Ayrıştırılmış Move nesnesi
     * @throws IllegalArgumentException Geçersiz veri formatı durumunda
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