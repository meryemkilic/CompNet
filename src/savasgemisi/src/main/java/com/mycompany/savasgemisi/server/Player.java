package com.mycompany.savasgemisi.server;

import java.util.Arrays;

/**
 * Oyuncu bilgilerini ve oyun tahtasını temsil eden sınıf.
 * Her oyuncu kendi tahtasına ve rakibinin tahtasının görünen kısmına sahiptir.
 */
public class Player {
    private int id;
    private String name;
    private Board board;
    private Board opponentView;
    
    /**
     * Yeni bir oyuncu oluşturur
     * @param id Oyuncu ID'si
     * @param name Oyuncu adı
     */
    public Player(int id, String name) {
        this.id = id;
        this.name = name;
        this.board = new Board(10, 10); 
        this.opponentView = new Board(10, 10);
    }
    
    /**
     * Oyuncu ID'sini döndürür
     * @return Oyuncu ID'si
     */
    public int getId() {
        return id;
    }
    
    /**
     * Oyuncu adını döndürür
     * @return Oyuncu adı
     */
    public String getName() {
        return name;
    }
    
    /**
     * Oyuncunun kendi tahtasını döndürür
     * @return Oyun tahtası
     */
    public Board getBoard() {
        return board;
    }
    
    /**
     * Oyuncunun kendi tahtasını ayarlar
     * @param board Yeni oyun tahtası
     */
    public void setBoard(Board board) {
        this.board = board;
    }
    
    /**
     * Rakibin tahtasının görünen kısmını döndürür
     * @return Rakip tahtası görünümü
     */
    public Board getOpponentView() {
        return opponentView;
    }
    
    /**
     * Oyun tahtasını temsil eden iç sınıf
     * Tahta, hücrelerin durumlarını ve boyutlarını yönetir
     */
    public static class Board {
        /**
         * Tahta hücrelerinin olası durumları
         */
        public enum CellState {
            EMPTY,      // Boş
            SHIP,       // Gemi var
            HIT,        // İsabet
            MISS        // Iskalama
        }
        
        private CellState[][] cells;
        private int width;
        private int height;
        
        /**
         * Yeni bir tahta oluşturur
         * @param width Tahta genişliği
         * @param height Tahta yüksekliği
         */
        public Board(int width, int height) {
            this.width = width;
            this.height = height;
            this.cells = new CellState[height][width];
            
            for (CellState[] row : cells) {
                Arrays.fill(row, CellState.EMPTY);
            }
        }
        
        /**
         * Belirtilen koordinattaki hücrenin durumunu döndürür
         * @param x X koordinatı
         * @param y Y koordinatı
         * @return Hücre durumu
         * @throws IllegalArgumentException Geçersiz koordinatlar için
         */
        public CellState getCell(int x, int y) {
            if (x < 0 || x >= width || y < 0 || y >= height) {
                throw new IllegalArgumentException("Geçersiz hücre koordinatları: " + x + "," + y);
            }
            return cells[y][x];
        }
        
        /**
         * Belirtilen koordinattaki hücrenin durumunu ayarlar
         * @param x X koordinatı
         * @param y Y koordinatı
         * @param state Yeni hücre durumu
         * @throws IllegalArgumentException Geçersiz koordinatlar için
         */
        public void setCell(int x, int y, CellState state) {
            if (x < 0 || x >= width || y < 0 || y >= height) {
                throw new IllegalArgumentException("Geçersiz hücre koordinatları: " + x + "," + y);
            }
            cells[y][x] = state;
        }
        
        /**
         * Tahta genişliğini döndürür
         * @return Tahta genişliği
         */
        public int getWidth() {
            return width;
        }
        
        /**
         * Tahta yüksekliğini döndürür
         * @return Tahta yüksekliği
         */
        public int getHeight() {
            return height;
        }
        
        /**
         * Tahtayı string formatına dönüştürür
         * @return Tahta durumlarının string temsili
         */
        public String serialize() {
            StringBuilder sb = new StringBuilder();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    sb.append(cells[y][x].ordinal());
                }
            }
            return sb.toString();
        }
        
        /**
         * String formatındaki tahta verisini ayrıştırır
         * @param data Tahta durumlarının string temsili
         * @throws IllegalArgumentException Geçersiz veri formatı için
         */
        public void deserialize(String data) {
            if (data.length() != width * height) {
                throw new IllegalArgumentException("Geçersiz tahta verisi uzunluğu");
            }
            
            int index = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int ordinal = Character.getNumericValue(data.charAt(index++));
                    cells[y][x] = CellState.values()[ordinal];
                }
            }
        }
    }
} 