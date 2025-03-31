package com.battleship.common;

import java.io.Serializable;

/**
 * Oyun tahtasındaki bir hücreyi temsil eder.
 */
public class Cell implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int x;
    private int y;
    private CellState state;
    private Ship ship;
    
    /**
     * Yeni bir hücre oluşturur.
     * @param x X koordinatı
     * @param y Y koordinatı
     */
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.state = CellState.EMPTY;
        this.ship = null;
    }
    
    /**
     * Hücrenin X koordinatını döndürür.
     * @return X koordinatı
     */
    public int getX() {
        return x;
    }
    
    /**
     * Hücrenin Y koordinatını döndürür.
     * @return Y koordinatı
     */
    public int getY() {
        return y;
    }
    
    /**
     * Hücrenin durumunu döndürür.
     * @return Hücre durumu
     */
    public CellState getState() {
        return state;
    }
    
    /**
     * Hücrenin durumunu günceller.
     * @param state Yeni hücre durumu
     */
    public void setState(CellState state) {
        this.state = state;
    }
    
    /**
     * Hücredeki gemiyi döndürür.
     * @return Hücredeki gemi veya null
     */
    public Ship getShip() {
        return ship;
    }
    
    /**
     * Hücreye bir gemi yerleştirir.
     * @param ship Yerleştirilecek gemi
     */
    public void setShip(Ship ship) {
        this.ship = ship;
        if (ship != null) {
            this.state = CellState.SHIP;
        }
    }
    
    /**
     * Hücreye ateş edildiğinde çağrılır.
     * @return Eğer bir gemiye isabet edilirse true, aksi halde false
     */
    public boolean shoot() {
        if (state == CellState.EMPTY) {
            state = CellState.MISS;
            return false;
        } else if (state == CellState.SHIP) {
            state = CellState.HIT;
            return true;
        }
        return false;
    }
    
    /**
     * Hücrenin durumunu temsil eden bir enum.
     */
    public enum CellState {
        EMPTY,  // Boş hücre
        SHIP,   // Gemi var, vurulmadı
        HIT,    // Gemi var, vuruldu
        MISS    // Boş hücreye atış yapıldı
    }
} 