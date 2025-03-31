package com.mycompany.savasgemisi.server;

import java.util.Arrays;

/**
 * Oyuncu bilgilerini ve oyun tahtasını temsil eden sınıf.
 */
public class Player {
    private int id;
    private String name;
    private Board board;
    private Board opponentView; // Rakibin tahtasının görünen kısmı
    
    public Player(int id, String name) {
        this.id = id;
        this.name = name;
        this.board = new Board(10, 10); // 10x10 standart tahta boyutu
        this.opponentView = new Board(10, 10);
    }
    
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public Board getBoard() {
        return board;
    }
    
    public void setBoard(Board board) {
        this.board = board;
    }
    
    public Board getOpponentView() {
        return opponentView;
    }
    
    /**
     * Oyun tahtasını temsil eden iç sınıf
     */
    public static class Board {
        public enum CellState {
            EMPTY,      // Boş
            SHIP,       // Gemi var
            HIT,        // İsabet
            MISS        // Iskalama
        }
        
        private CellState[][] cells;
        private int width;
        private int height;
        
        public Board(int width, int height) {
            this.width = width;
            this.height = height;
            this.cells = new CellState[height][width];
            
            // Başlangıçta tüm hücreler boş
            for (CellState[] row : cells) {
                Arrays.fill(row, CellState.EMPTY);
            }
        }
        
        public CellState getCell(int x, int y) {
            if (x < 0 || x >= width || y < 0 || y >= height) {
                throw new IllegalArgumentException("Geçersiz hücre koordinatları: " + x + "," + y);
            }
            return cells[y][x];
        }
        
        public void setCell(int x, int y, CellState state) {
            if (x < 0 || x >= width || y < 0 || y >= height) {
                throw new IllegalArgumentException("Geçersiz hücre koordinatları: " + x + "," + y);
            }
            cells[y][x] = state;
        }
        
        public int getWidth() {
            return width;
        }
        
        public int getHeight() {
            return height;
        }
        
        public String serialize() {
            StringBuilder sb = new StringBuilder();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    sb.append(cells[y][x].ordinal());
                }
            }
            return sb.toString();
        }
        
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