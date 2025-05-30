package com.mycompany.savasgemisi.server;

import java.util.Random;

import com.mycompany.savasgemisi.server.Player.Board;

/**
 * Savaş Gemisi oyununun temel mantığını içeren sınıf.
 * Bu sınıf, oyun kurallarını, oyun durumunu ve oyuncu hamlelerini yönetir.
 */
public class BattleshipGame {
    private Player player1;
    private Player player2;
    private int currentPlayerId;
    private GameState state;
    private Random random = new Random();
    
    /**
     * Oyunun olası durumlarını tanımlayan enum
     */
    public enum GameState {
        WAITING_FOR_PLAYERS,  // Oyuncular bekleniyor
        PLACING_SHIPS,        // Gemiler yerleştiriliyor
        PLAYER1_TURN,         // 1. oyuncunun sırası
        PLAYER2_TURN,         // 2. oyuncunun sırası
        GAME_OVER            // Oyun bitti
    }
    
    /**
     * Yeni bir oyun oluşturur
     */
    public BattleshipGame() {
        this.state = GameState.WAITING_FOR_PLAYERS;
    }
    
    /**
     * Oyun tahtalarını başlatır ve oyuncuları hazırlar
     * @throws IllegalStateException İki oyuncu da bağlı değilse
     */
    public void initializeBoards() {
        if (player1 == null || player2 == null) {
            throw new IllegalStateException("Oyun başlatılamıyor: İki oyuncu da bağlı değil.");
        }
        
        player1.setBoard(new Player.Board(10, 10));
        player2.setBoard(new Player.Board(10, 10));
        
        state = GameState.PLACING_SHIPS;
    }
    
    /**
     * Oyuncunun gemilerini rastgele yerleştirir
     * @param player Gemi yerleştirilecek oyuncu
     */
    public void placeShips(Player player) {
        // Oyuncunun gemilerini rastgele yerleştir
        Board board = player.getBoard();
        
        int[] shipSizes = {5, 4, 3, 3, 2};
        
        for (int size : shipSizes) {
            boolean placed = false;
            while (!placed) {
                int x = random.nextInt(board.getWidth());
                int y = random.nextInt(board.getHeight());
                boolean horizontal = random.nextBoolean();
                
                if (canPlaceShip(board, x, y, size, horizontal)) {
                    placeShip(board, x, y, size, horizontal);
                    placed = true;
                }
            }
        }
        
        if (allShipsPlaced()) {
            state = GameState.PLAYER1_TURN;
            currentPlayerId = player1.getId();
        }
    }
    
    /**
     * Belirtilen konuma gemi yerleştirilebilir mi kontrol eder
     * @param board Oyun tahtası
     * @param x Başlangıç x koordinatı
     * @param y Başlangıç y koordinatı
     * @param size Gemi boyutu
     * @param horizontal Yatay yerleştirme mi?
     * @return Gemi yerleştirilebilir mi?
     */
    private boolean canPlaceShip(Board board, int x, int y, int size, boolean horizontal) {
        int width = board.getWidth();
        int height = board.getHeight();
        
        if (horizontal) {
            if (x + size > width) return false;
        } else {
            if (y + size > height) return false;
        }
        
        // Geminin diğer gemilerle çakışmadığını kontrol et
        for (int i = 0; i < size; i++) {
            int checkX = horizontal ? x + i : x;
            int checkY = horizontal ? y : y + i;
            
            if (checkX < 0 || checkX >= width || checkY < 0 || checkY >= height || 
                board.getCell(checkX, checkY) == Player.Board.CellState.SHIP) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Gemiyi tahtaya yerleştirir
     * @param board Oyun tahtası
     * @param x Başlangıç x koordinatı
     * @param y Başlangıç y koordinatı
     * @param size Gemi boyutu
     * @param horizontal Yatay yerleştirme mi?
     */
    private void placeShip(Board board, int x, int y, int size, boolean horizontal) {
        for (int i = 0; i < size; i++) {
            int placeX = horizontal ? x + i : x;
            int placeY = horizontal ? y : y + i;
            board.setCell(placeX, placeY, Player.Board.CellState.SHIP);
        }
    }
    
    /**
     * Tüm oyuncuların gemilerini yerleştirip yerleştirmediğini kontrol eder
     * @return Tüm gemiler yerleştirildi mi?
     */
    private boolean allShipsPlaced() {
        return true;
    }
    
    /**
     * Oyuncunun hamlesini işler
     * @param player Hamle yapan oyuncu
     * @param x Hedef x koordinatı
     * @param y Hedef y koordinatı
     * @return Hamle isabetli mi?
     * @throws IllegalStateException Oyun durumu uygun değilse veya sıra oyuncuda değilse
     */
    public boolean makeMove(Player player, int x, int y) {
        if (state != GameState.PLAYER1_TURN && state != GameState.PLAYER2_TURN) {
            throw new IllegalStateException("Şu anda hamle yapılamaz.");
        }
        if (player.getId() != currentPlayerId) {
            throw new IllegalStateException("Sıra sizde değil.");
        }
        Player targetPlayer = (player.getId() == player1.getId()) ? player2 : player1;
        Board targetBoard = targetPlayer.getBoard();
        if (x < 0 || x >= targetBoard.getWidth() || y < 0 || y >= targetBoard.getHeight()) {
            return false;
        }
        Player.Board.CellState cellState = targetBoard.getCell(x, y);
        if (cellState == Player.Board.CellState.HIT || cellState == Player.Board.CellState.MISS) {
            return false;
        }
        boolean isHit = cellState == Player.Board.CellState.SHIP;
        targetBoard.setCell(x, y, isHit ? Player.Board.CellState.HIT : Player.Board.CellState.MISS);
        player.getOpponentView().setCell(x, y, isHit ? Player.Board.CellState.HIT : Player.Board.CellState.MISS);
        if (!isHit) {
            currentPlayerId = (currentPlayerId == player1.getId()) ? player2.getId() : player1.getId();
            state = (currentPlayerId == player1.getId()) ? GameState.PLAYER1_TURN : GameState.PLAYER2_TURN;
        }
        if (checkVictory()) {
            state = GameState.GAME_OVER;
        }
        return isHit;
    }
    
    /**
     * Oyuncunun tahta durumunu döndürür
     * @param player Oyuncu
     * @return Oyun durumu
     */
    public GameState getBoardState(Player player) {
        return state;
    }
    
    /**
     * Oyunun bitip bitmediğini kontrol eder
     * @return Oyun bitti mi?
     */
    public boolean checkVictory() {
        
        boolean player1HasShips = hasRemainingShips(player1.getBoard());
        boolean player2HasShips = hasRemainingShips(player2.getBoard());
        
        if (!player1HasShips || !player2HasShips) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Tahtada hala gemi olup olmadığını kontrol eder
     * @param board Kontrol edilecek tahta
     * @return Tahtada gemi var mı?
     */
    private boolean hasRemainingShips(Board board) {
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                if (board.getCell(x, y) == Player.Board.CellState.SHIP) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Oyunun kazananını döndürür
     * @return Kazanan oyuncu (null ise beraberlik veya oyun bitmedi)
     */
    public Player getWinner() {
        if (state != GameState.GAME_OVER) {
            return null;
        }
        
        boolean player1HasShips = hasRemainingShips(player1.getBoard());
        boolean player2HasShips = hasRemainingShips(player2.getBoard());
        
        if (!player1HasShips) {
            return player2;
        } else if (!player2HasShips) {
            return player1;
        }
        
        return null;
    }
    
    /**
     * Birinci oyuncuyu ayarlar
     * @param player1 Birinci oyuncu
     */
    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }
    
    /**
     * İkinci oyuncuyu ayarlar
     * @param player2 İkinci oyuncu
     */
    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }
    
    /**
     * Birinci oyuncuyu döndürür
     * @return Birinci oyuncu
     */
    public Player getPlayer1() {
        return player1;
    }
    
    /**
     * İkinci oyuncuyu döndürür
     * @return İkinci oyuncu
     */
    public Player getPlayer2() {
        return player2;
    }
    
    /**
     * Oyunun mevcut durumunu döndürür
     * @return Oyun durumu
     */
    public GameState getState() {
        return state;
    }
    
    /**
     * Sıradaki oyuncunun ID'sini döndürür
     * @return Sıradaki oyuncunun ID'si
     */
    public int getCurrentPlayerId() {
        return currentPlayerId;
    }
} 