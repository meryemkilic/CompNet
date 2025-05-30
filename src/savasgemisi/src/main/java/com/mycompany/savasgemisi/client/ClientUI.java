package com.mycompany.savasgemisi.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * İstemci kullanıcı arayüzü sınıfı.
 * Bu sınıf, oyunun grafiksel kullanıcı arayüzünü oluşturur ve yönetir.
 * İki oyun tahtası (oyuncunun kendi tahtası ve rakip tahtası),
 * bağlantı ayarları ve durum bilgisi içerir.
 */
public class ClientUI extends JFrame {
    /** Oyun tahtasının boyutu */
    private static final int BOARD_SIZE = 10;
    
    /** Her bir hücrenin piksel cinsinden boyutu */
    private static final int CELL_SIZE = 30;
    
    /** Oyun kontrolcüsü referansı */
    private GameController gameController;
    
    /** Oyuncunun kendi tahtasını gösteren panel */
    private JPanel myBoardPanel;
    
    /** Rakip tahtasını gösteren panel */
    private JPanel opponentBoardPanel;
    
    /** Durum bilgisini gösteren etiket */
    private JLabel statusLabel;
    
    /** Sunucu IP adresi giriş alanı */
    private JTextField serverIPField;
    
    /** Sunucu port numarası giriş alanı */
    private JTextField serverPortField;
    
    /** Sunucuya bağlanma düğmesi */
    private JButton connectButton;
    
    /** Yeni oyun başlatma düğmesi */
    private JButton newGameButton;
    
    /** Oyuncunun kendi tahtasındaki hücreler */
    private JPanel[][] myCells = new JPanel[BOARD_SIZE][BOARD_SIZE];
    
    /** Rakip tahtasındaki hücreler */
    private JPanel[][] opponentCells = new JPanel[BOARD_SIZE][BOARD_SIZE];
    
    /** Oyuncu ID bilgisini gösteren etiket */
    private JLabel playerIdLabel;
    
    /** Oyun bilgisi (ör: session ve oyuncu id'leri) gösteren etiket */
    private JLabel gameInfoLabel;
    
    /**
     * Yeni bir istemci arayüzü oluşturur ve bileşenleri başlatır
     */
    public ClientUI() {
        initComponents();
        gameController = new GameController(this);
    }
    
    /**
     * Arayüz bileşenlerini oluşturur ve düzenler
     */
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Savaş Gemisi Oyunu");
        setResizable(false);
        
        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Üst panel - Bağlantı alanı
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(2, 1)); // 2 satırlı grid
        serverIPField = new JTextField("localhost", 10);
        serverPortField = new JTextField("5000", 5);
        connectButton = new JButton("Bağlan");
        newGameButton = new JButton("Yeni Oyun");
        newGameButton.setEnabled(false);
        playerIdLabel = new JLabel("ID: -");
        gameInfoLabel = new JLabel("Oyun Bilgisi: -");
        
        JPanel connectionPanel = new JPanel();
        connectionPanel.add(new JLabel("Sunucu IP:"));
        connectionPanel.add(serverIPField);
        connectionPanel.add(new JLabel("Port:"));
        connectionPanel.add(serverPortField);
        connectionPanel.add(connectButton);
        connectionPanel.add(newGameButton);
        
        JPanel infoPanel = new JPanel();
        infoPanel.add(playerIdLabel);
        infoPanel.add(gameInfoLabel);
        
        topPanel.add(connectionPanel);
        topPanel.add(infoPanel);
        
        // Orta panel - Oyun tahtaları
        JPanel centerPanel = new JPanel(new BorderLayout(10, 0));
        
        // Sol tahta - Benim tahtam
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JLabel("Benim Tahtam", JLabel.CENTER), BorderLayout.NORTH);
        myBoardPanel = createBoardPanel(false);
        leftPanel.add(myBoardPanel, BorderLayout.CENTER);
        
        // Sağ tahta - Rakip tahtası
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel("Rakip Tahtası", JLabel.CENTER), BorderLayout.NORTH);
        opponentBoardPanel = createBoardPanel(true);
        rightPanel.add(opponentBoardPanel, BorderLayout.CENTER);
        
        centerPanel.add(leftPanel, BorderLayout.WEST);
        centerPanel.add(rightPanel, BorderLayout.EAST);
        
        // Alt panel - Durum çubuğu
        statusLabel = new JLabel("Sunucuya bağlanmak için 'Bağlan' düğmesine tıklayın.");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Panel düzeni
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // Olay dinleyicileri
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToServer();
            }
        });
        
        newGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameController.requestNewGame();
            }
        });
        
        // Pencere kapandığında bağlantıyı kapat
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gameController.disconnect();
            }
        });
        
        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }
    
    /**
     * Oyun tahtası panelini oluşturur
     * @param isOpponent Rakip tahtası mı yoksa oyuncunun kendi tahtası mı?
     * @return Oluşturulan tahta paneli
     */
    private JPanel createBoardPanel(boolean isOpponent) {
        JPanel boardPanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE));
        boardPanel.setPreferredSize(new Dimension(BOARD_SIZE * CELL_SIZE, BOARD_SIZE * CELL_SIZE));
        boardPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                JPanel cellPanel = new JPanel();
                cellPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                cellPanel.setBackground(Color.BLUE);
                
                final int cellX = x;
                final int cellY = y;
                
                if (isOpponent) {
                    // Rakip tahtasına tıklanabilir
                    cellPanel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (gameController.isGameActive() && gameController.isMyTurn()) {
                                gameController.handleUserInput(cellX, cellY);
                            }
                        }
                    });
                    opponentCells[y][x] = cellPanel;
                } else {
                    myCells[y][x] = cellPanel;
                }
                
                boardPanel.add(cellPanel);
            }
        }
        
        return boardPanel;
    }
    
    /**
     * Oyun tahtalarını günceller
     * @param boardData Sunucudan gelen tahta verisi
     */
    public void displayBoard(String boardData) {
        String[] boards = boardData.split(",");
        if (boards.length != 2) {
            showMessage("Geçersiz tahta verisi");
            return;
        }
        
        String myBoardData = boards[0];
        String opponentBoardData = boards[1];
        
        updateBoard(myBoardData, myCells, false);
        
        updateBoard(opponentBoardData, opponentCells, true);
    }
    
    /**
     * Belirli bir tahtayı günceller
     * @param boardData Tahta verisi
     * @param cells Güncellenecek hücreler
     * @param isOpponent Rakip tahtası mı?
     */
    private void updateBoard(String boardData, JPanel[][] cells, boolean isOpponent) {
        if (boardData.length() != BOARD_SIZE * BOARD_SIZE) {
            showMessage("Geçersiz tahta boyutu: " + boardData.length());
            return;
        }
        
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                int index = y * BOARD_SIZE + x;
                int cellState = Character.getNumericValue(boardData.charAt(index));
                
                Color cellColor = getCellColor(cellState, isOpponent);
                cells[y][x].setBackground(cellColor);
            }
        }
    }
    
    /**
     * Hücre durumuna göre renk döndürür
     * @param cellState Hücre durumu (0-EMPTY, 1-SHIP, 2-HIT, 3-MISS)
     * @param isOpponent Rakip tahtası mı?
     * @return Hücre rengi
     */
    private Color getCellColor(int cellState, boolean isOpponent) {
        // CellState enum değerleri: 0-EMPTY, 1-SHIP, 2-HIT, 3-MISS
        switch (cellState) {
            case 0: // EMPTY
                return Color.BLUE;
            case 1: // SHIP
                return isOpponent ? Color.BLUE : Color.DARK_GRAY; // Rakip gemileri gizle
            case 2: // HIT
                return Color.RED;
            case 3: // MISS
                return Color.WHITE;
            default:
                return Color.BLUE;
        }
    }
    
    /**
     * Oyun durumunu günceller
     * @param status Yeni durum mesajı
     */
    public void updateGameStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(status);
            if (status.contains("bağlandı")) {
                connectButton.setEnabled(false);
                newGameButton.setEnabled(true);
            }
        });
    }
    
    /**
     * Mesaj kutusu gösterir
     * @param message Gösterilecek mesaj
     */
    public void showMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message);
        });
    }
    
    /**
     * Oyun sonu diyaloğunu gösterir
     * @param message Oyun sonu mesajı
     */
    public void showGameOverDialog(String message) {
        SwingUtilities.invokeLater(() -> {
            int option = JOptionPane.showConfirmDialog(
                this,
                message + "\nYeni oyun başlatmak ister misiniz?",
                "Oyun Bitti",
                JOptionPane.YES_NO_OPTION
            );
            
            if (option == JOptionPane.YES_OPTION) {
                gameController.requestNewGame();
            }
        });
    }
    
    /**
     * Sunucu IP adresini döndürür
     * @return Sunucu IP adresi
     */
    public String getServerIP() {
        return serverIPField.getText().trim();
    }
    
    /**
     * Sunucu port numarasını döndürür
     * @return Sunucu port numarası
     */
    public int getServerPort() {
        try {
            return Integer.parseInt(serverPortField.getText().trim());
        } catch (NumberFormatException e) {
            return 5000; 
        }
    }
    
    /**
     * Sunucuya bağlanma işlemini başlatır
     */
    private void connectToServer() {
        gameController.startGame();
    }
    
    /**
     * Oyuncu ID bilgisini günceller
     * @param id Oyuncu ID'si
     */
    public void setPlayerId(int id) {
        SwingUtilities.invokeLater(() -> playerIdLabel.setText("ID: " + id));
    }
    
    /**
     * Oyun bilgisi etiketini günceller
     * @param info Oyun bilgisi
     */
    public void setGameInfo(String info) {
        SwingUtilities.invokeLater(() -> gameInfoLabel.setText(info));
    }
} 