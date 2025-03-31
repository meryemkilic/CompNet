package com.mycompany.savasgemisi.server;

import com.mycompany.savasgemisi.common.Move;
import com.mycompany.savasgemisi.server.Player.Board;
import java.util.Random;

/**
 * Savaş Gemisi oyununun temel mantığını içeren sınıf.
 */
public class BattleshipGame {
    private Player player1;
    private Player player2;
    private int currentPlayerId;
    private GameState state;
    private Random random = new Random();
    
    public enum GameState {
        WAITING_FOR_PLAYERS,
        PLACING_SHIPS,
        PLAYER1_TURN,
        PLAYER2_TURN,
        GAME_OVER
    }
    
    public BattleshipGame() {
        this.state = GameState.WAITING_FOR_PLAYERS;
    }
    
    public void initializeBoards() {
        if (player1 == null || player2 == null) {
            throw new IllegalStateException("Oyun başlatılamıyor: İki oyuncu da bağlı değil.");
        }
        
        // Oyun tahtalarını sıfırla
        player1.setBoard(new Player.Board(10, 10));
        player2.setBoard(new Player.Board(10, 10));
        
        // Gemi yerleştirmeye hazır
        state = GameState.PLACING_SHIPS;
    }
    
    public void placeShips(Player player) {
        // Oyuncunun gemilerini rastgele yerleştir
        // Bu gerçek bir uygulamada, oyuncudan gelen verilere göre yerleştirilir
        Board board = player.getBoard();
        
        // Gemi boyutları
        int[] shipSizes = {5, 4, 3, 3, 2};
        
        for (int size : shipSizes) {
            boolean placed = false;
            while (!placed) {
                // Rastgele konum ve yön (yatay/dikey)
                int x = random.nextInt(board.getWidth());
                int y = random.nextInt(board.getHeight());
                boolean horizontal = random.nextBoolean();
                
                // Gemiyi yerleştirmeyi dene
                if (canPlaceShip(board, x, y, size, horizontal)) {
                    placeShip(board, x, y, size, horizontal);
                    placed = true;
                }
            }
        }
        
        // İki oyuncu da gemilerini yerleştirdiyse, oyuna başla
        if (allShipsPlaced()) {
            state = GameState.PLAYER1_TURN;
            currentPlayerId = player1.getId();
        }
    }
    
    private boolean canPlaceShip(Board board, int x, int y, int size, boolean horizontal) {
        int width = board.getWidth();
        int height = board.getHeight();
        
        // Geminin sınırlar içinde olup olmadığını kontrol et
        if (horizontal) {
            if (x + size > width) return false;
        } else {
            if (y + size > height) return false;
        }
        
        // Geminin diğer gemilerle çakışmadığını kontrol et
        for (int i = 0; i < size; i++) {
            int checkX = horizontal ? x + i : x;
            int checkY = horizontal ? y : y + i;
            
            // Sınır kontrolü ve hücre kontrolü
            if (checkX < 0 || checkX >= width || checkY < 0 || checkY >= height || 
                board.getCell(checkX, checkY) == Player.Board.CellState.SHIP) {
                return false;
            }
        }
        
        return true;
    }
    
    private void placeShip(Board board, int x, int y, int size, boolean horizontal) {
        for (int i = 0; i < size; i++) {
            int placeX = horizontal ? x + i : x;
            int placeY = horizontal ? y : y + i;
            board.setCell(placeX, placeY, Player.Board.CellState.SHIP);
        }
    }
    
    private boolean allShipsPlaced() {
        // İki oyuncunun da gemilerini yerleştirdiğini kontrol et
        return true; // Bu basitleştirilmiş versiyonda her zaman true
    }
    
    public boolean makeMove(Player player, int x, int y) {
        if (state != GameState.PLAYER1_TURN && state != GameState.PLAYER2_TURN) {
            throw new IllegalStateException("Şu anda hamle yapılamaz.");
        }
        
        if (player.getId() != currentPlayerId) {
            throw new IllegalStateException("Sıra sizde değil.");
        }
        
        // Hedef oyuncuyu belirle
        Player targetPlayer = (player.getId() == player1.getId()) ? player2 : player1;
        Board targetBoard = targetPlayer.getBoard();
        
        // Hamlenin geçerli olup olmadığını kontrol et
        if (x < 0 || x >= targetBoard.getWidth() || y < 0 || y >= targetBoard.getHeight()) {
            return false;
        }
        
        // Zaten atış yapılmış bir hücre mi kontrol et
        Player.Board.CellState cellState = targetBoard.getCell(x, y);
        if (cellState == Player.Board.CellState.HIT || cellState == Player.Board.CellState.MISS) {
            return false;
        }
        
        // Atışı işle
        boolean isHit = cellState == Player.Board.CellState.SHIP;
        targetBoard.setCell(x, y, isHit ? Player.Board.CellState.HIT : Player.Board.CellState.MISS);
        
        // Atış sonucunu oyuncunun rakip görünümüne de kaydet
        player.getOpponentView().setCell(x, y, isHit ? Player.Board.CellState.HIT : Player.Board.CellState.MISS);
        
        // Sırayı değiştir
        currentPlayerId = (currentPlayerId == player1.getId()) ? player2.getId() : player1.getId();
        state = (currentPlayerId == player1.getId()) ? GameState.PLAYER1_TURN : GameState.PLAYER2_TURN;
        
        // Oyun bitmiş mi kontrol et
        if (checkVictory()) {
            state = GameState.GAME_OVER;
        }
        
        return isHit;
    }
    
    public GameState getBoardState(Player player) {
        return state;
    }
    
    public boolean checkVictory() {
        // Oyunun bitip bitmediğini kontrol et
        // Bir oyuncunun tüm gemileri batmış mı?
        
        boolean player1HasShips = hasRemainingShips(player1.getBoard());
        boolean player2HasShips = hasRemainingShips(player2.getBoard());
        
        if (!player1HasShips || !player2HasShips) {
            return true;
        }
        
        return false;
    }
    
    private boolean hasRemainingShips(Board board) {
        // Tahtada hala gemi var mı kontrol et
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                if (board.getCell(x, y) == Player.Board.CellState.SHIP) {
                    return true;
                }
            }
        }
        return false;
    }
    
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
        
        return null; // Beraberlik veya oyun bitmedi
    }
    
    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }
    
    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }
    
    public Player getPlayer1() {
        return player1;
    }
    
    public Player getPlayer2() {
        return player2;
    }
    
    public GameState getState() {
        return state;
    }
    
    public int getCurrentPlayerId() {
        return currentPlayerId;
    }
} 