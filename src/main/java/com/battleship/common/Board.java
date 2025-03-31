package com.battleship.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Savaş gemisi oyununda bir oyun tahtasını temsil eder.
 */
public class Board implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Cell[][] cells;
    private List<Ship> ships;
    private int size;
    
    /**
     * Belirtilen boyutta yeni bir tahta oluşturur.
     * @param size Tahtanın boyutu
     */
    public Board(int size) {
        this.size = size;
        this.cells = new Cell[size][size];
        this.ships = new ArrayList<>();
        
        // Tahta hücrelerini oluştur
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                cells[x][y] = new Cell(x, y);
            }
        }
    }
    
    /**
     * Tahta boyutunu döndürür.
     * @return Tahta boyutu
     */
    public int getSize() {
        return size;
    }
    
    /**
     * Belirtilen koordinattaki hücreyi döndürür.
     * @param x X koordinatı
     * @param y Y koordinatı
     * @return Hücre nesnesi
     */
    public Cell getCell(int x, int y) {
        if (isValidCoordinate(x, y)) {
            return cells[x][y];
        }
        return null;
    }
    
    /**
     * Tahtaya bir gemi yerleştirir.
     * @param ship Yerleştirilecek gemi
     * @param startX Başlangıç X koordinatı
     * @param startY Başlangıç Y koordinatı
     * @param isHorizontal Yatay yerleştirme için true, dikey için false
     * @return Yerleştirme başarılı ise true, aksi halde false
     */
    public boolean placeShip(Ship ship, int startX, int startY, boolean isHorizontal) {
        int length = ship.getLength();
        
        // Geminin sınırlar içinde olup olmadığını kontrol et
        if (isHorizontal) {
            if (startX < 0 || startX + length > size || startY < 0 || startY >= size) {
                return false;
            }
        } else {
            if (startX < 0 || startX >= size || startY < 0 || startY + length > size) {
                return false;
            }
        }
        
        // Geminin diğer gemilerle çakışıp çakışmadığını kontrol et
        if (isHorizontal) {
            for (int x = startX; x < startX + length; x++) {
                if (cells[x][startY].getShip() != null) {
                    return false;
                }
            }
        } else {
            for (int y = startY; y < startY + length; y++) {
                if (cells[startX][y].getShip() != null) {
                    return false;
                }
            }
        }
        
        // Gemiyi yerleştir
        ship.place(startX, startY, isHorizontal);
        ships.add(ship);
        
        // Tahta hücrelerini güncelle
        if (isHorizontal) {
            for (int x = startX; x < startX + length; x++) {
                cells[x][startY].setShip(ship);
            }
        } else {
            for (int y = startY; y < startY + length; y++) {
                cells[startX][y].setShip(ship);
            }
        }
        
        return true;
    }
    
    /**
     * Belirtilen koordinata ateş eder.
     * @param x X koordinatı
     * @param y Y koordinatı
     * @return Ateş sonucunu içeren bir ShotResult nesnesi
     */
    public ShotResult shoot(int x, int y) {
        if (!isValidCoordinate(x, y)) {
            return new ShotResult(false, false, null);
        }
        
        Cell cell = cells[x][y];
        
        // Aynı hücreye daha önce ateş edilmiş mi kontrol et
        if (cell.getState() == Cell.CellState.HIT || cell.getState() == Cell.CellState.MISS) {
            return new ShotResult(false, false, null);
        }
        
        boolean isHit = cell.shoot();
        
        // Eğer bir gemiye vurduysa, geminin o bölümünde hasar işaretle
        Ship ship = cell.getShip();
        boolean isSunk = false;
        
        if (isHit && ship != null) {
            int position = ship.getPositionFromCoordinates(x, y);
            ship.hit(position);
            isSunk = ship.isSunk();
        }
        
        return new ShotResult(true, isHit, isSunk ? ship : null);
    }
    
    /**
     * Tüm gemilerin batıp batmadığını kontrol eder.
     * @return Tüm gemiler battıysa true, aksi halde false
     */
    public boolean areAllShipsSunk() {
        for (Ship ship : ships) {
            if (!ship.isSunk()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Tahtadaki gemilerin listesini döndürür.
     * @return Gemilerin listesi
     */
    public List<Ship> getShips() {
        return ships;
    }
    
    /**
     * Koordinatın tahta sınırları içinde olup olmadığını kontrol eder.
     * @param x X koordinatı
     * @param y Y koordinatı
     * @return Geçerli ise true, aksi halde false
     */
    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < size && y >= 0 && y < size;
    }
    
    /**
     * Atış sonucunu temsil eden bir sınıf.
     */
    public static class ShotResult implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private boolean isValid;
        private boolean isHit;
        private Ship sunkShip;
        
        public ShotResult(boolean isValid, boolean isHit, Ship sunkShip) {
            this.isValid = isValid;
            this.isHit = isHit;
            this.sunkShip = sunkShip;
        }
        
        public boolean isValid() {
            return isValid;
        }
        
        public boolean isHit() {
            return isHit;
        }
        
        public Ship getSunkShip() {
            return sunkShip;
        }
    }
} 