package com.battleship.common;

import java.io.Serializable;

/**
 * Bir savaş gemisini temsil eder.
 */
public class Ship implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private ShipType type;
    private int length;
    private boolean[] hitMarks;
    private boolean isHorizontal;
    private int startX;
    private int startY;
    
    /**
     * Yeni bir gemi oluşturur.
     * @param type Gemi türü
     */
    public Ship(ShipType type) {
        this.type = type;
        this.length = type.getLength();
        this.hitMarks = new boolean[length];
    }
    
    /**
     * Geminin türünü döndürür.
     * @return Gemi türü
     */
    public ShipType getType() {
        return type;
    }
    
    /**
     * Geminin uzunluğunu döndürür.
     * @return Gemi uzunluğu
     */
    public int getLength() {
        return length;
    }
    
    /**
     * Geminin yatay mı yoksa dikey mi yerleştirildiğini döndürür.
     * @return Yatay ise true, dikey ise false
     */
    public boolean isHorizontal() {
        return isHorizontal;
    }
    
    /**
     * Gemiyi yerleştirir.
     * @param startX Başlangıç X koordinatı
     * @param startY Başlangıç Y koordinatı
     * @param isHorizontal Yatay yerleştirme ise true, dikey ise false
     */
    public void place(int startX, int startY, boolean isHorizontal) {
        this.startX = startX;
        this.startY = startY;
        this.isHorizontal = isHorizontal;
    }
    
    /**
     * Gemi üzerindeki bir pozisyona vuruş olduğunda çağrılır.
     * @param position Vurulan pozisyon
     * @return Eğer pozisyon geçerli ise true, aksi halde false
     */
    public boolean hit(int position) {
        if (position >= 0 && position < length) {
            hitMarks[position] = true;
            return true;
        }
        return false;
    }
    
    /**
     * Gemi üzerindeki hangi konuma karşılık geldiğini bulur.
     * @param x X koordinatı
     * @param y Y koordinatı
     * @return Gemi üzerindeki pozisyon, eğer bulunamazsa -1
     */
    public int getPositionFromCoordinates(int x, int y) {
        if (isHorizontal) {
            if (y == startY && x >= startX && x < startX + length) {
                return x - startX;
            }
        } else {
            if (x == startX && y >= startY && y < startY + length) {
                return y - startY;
            }
        }
        return -1;
    }
    
    /**
     * Geminin başlangıç X koordinatını döndürür.
     * @return Başlangıç X koordinatı
     */
    public int getStartX() {
        return startX;
    }
    
    /**
     * Geminin başlangıç Y koordinatını döndürür.
     * @return Başlangıç Y koordinatı
     */
    public int getStartY() {
        return startY;
    }
    
    /**
     * Geminin batıp batmadığını kontrol eder.
     * @return Batmış ise true, aksi halde false
     */
    public boolean isSunk() {
        for (boolean hit : hitMarks) {
            if (!hit) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Gemi türlerini temsil eden bir enum.
     */
    public enum ShipType {
        CARRIER(5, "Uçak Gemisi"),
        BATTLESHIP(4, "Savaş Gemisi"),
        CRUISER(3, "Kruvazör"),
        SUBMARINE(3, "Denizaltı"),
        DESTROYER(2, "Muhrip");
        
        private final int length;
        private final String name;
        
        ShipType(int length, String name) {
            this.length = length;
            this.name = name;
        }
        
        public int getLength() {
            return length;
        }
        
        public String getName() {
            return name;
        }
    }
} 