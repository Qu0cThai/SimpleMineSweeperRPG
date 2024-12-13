import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class Minesweeper {
    private class MineTile extends JButton {
        int r;
        int c;

        public MineTile(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    int tileSize = 70;
    int numRows = 8;
    int numCols = numRows;
    int boardWidth = numCols * tileSize;
    int boardHeight = numRows * tileSize;

    JFrame frame = new JFrame("Minesweeper RPG");
    JLabel textLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();

    int mineCount = 10;
    MineTile[][] board = new MineTile[numRows][numCols];
    ArrayList<MineTile> mineList;
    Random random = new Random();

    int tilesClicked = 0;
    boolean gameOver = false;
    int playerHealth = 100;
    int playerXP = 0;
    int playerLevel = 1;

    
    JPanel startPanel = new JPanel();
    JLabel titleLabel = new JLabel("Minesweeper RPG");
    JButton startButton = new JButton("Start Game");

    Minesweeper() {
        
        startPanel.setLayout(new BorderLayout());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 40));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        startPanel.add(titleLabel, BorderLayout.CENTER);

        startButton.setFont(new Font("Arial", Font.BOLD, 20));
        startButton.addActionListener(e -> showDifficultyPopup());
        startPanel.add(startButton, BorderLayout.SOUTH);

        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(startPanel);

        frame.setVisible(true);
    }

    void showDifficultyPopup() {
        String[] options = {"Easy", "Medium", "Hard"};
        int choice = JOptionPane.showOptionDialog(frame, "Choose Difficulty", "Select Difficulty",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            setDifficulty(8, 8, 10); 
        } else if (choice == 1) {
            setDifficulty(10, 10, 20); 
        } else if (choice == 2) {
            setDifficulty(12, 12, 30); 
        }

        startGame();
    }

    void setDifficulty(int rows, int cols, int mines) {
        this.numRows = rows;
        this.numCols = cols;
        this.mineCount = mines;
        this.boardWidth = numCols * tileSize;
        this.boardHeight = numRows * tileSize;

        frame.setSize(boardWidth, boardHeight);
        startPanel.setVisible(false); 
        resetGame();
    }

    void startGame() {
        textLabel.setFont(new Font("Arial", Font.BOLD, 25));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Health: " + playerHealth + " XP: " + playerXP);
        textLabel.setOpaque(true);

        textPanel.setLayout(new BorderLayout());
        textPanel.add(textLabel);
        frame.add(textPanel, BorderLayout.NORTH);

        boardPanel.setLayout(new GridLayout(numRows, numCols));
        frame.add(boardPanel);

        frame.setVisible(true);

        setMines();
    }

    void resetGame() {
        tilesClicked = 0;
        gameOver = false;
        playerHealth = 100;
        playerXP = 0;
        playerLevel = 1;
        board = new MineTile[numRows][numCols];
        mineList = new ArrayList<MineTile>();
        boardPanel.setLayout(new GridLayout(numRows, numCols));

        textLabel.setText("Health: " + playerHealth + " XP: " + playerXP);

        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                MineTile tile = new MineTile(r, c);
                board[r][c] = tile;

                tile.setFocusable(false);
                tile.setMargin(new Insets(0, 0, 0, 0));
                tile.setFont(new Font("Arial Unicode MS", Font.PLAIN, 45));
                tile.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (gameOver) {
                            return;
                        }
                        MineTile tile = (MineTile) e.getSource();

                        
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            if (tile.getText() == "") {
                                if (mineList.contains(tile)) {
                                    
                                    tile.setText("💣");
                                    playerHealth -= 10;
                                    textLabel.setText("You Hit a Mine! Health: " + playerHealth + " XP: " + playerXP);

                                    if (playerHealth <= 0) {
                                        gameOver = true;
                                        textLabel.setText("Game Over! You Died!");
                                    }
                                } else {
                                    checkMine(tile.r, tile.c);
                                    playerXP += 10; 
                                    textLabel.setText("Health: " + playerHealth + " XP: " + playerXP);

                                    
                                    if (playerXP >= playerLevel * 100) {
                                        playerLevel++;
                                        textLabel.setText("Level Up! Health: " + playerHealth + " XP: " + playerXP);
                                    }
                                }
                            }
                        }
                        
                        else if (e.getButton() == MouseEvent.BUTTON3) {
                            if (tile.getText() == "" && tile.isEnabled()) {
                                tile.setText("🚩");
                            } else if (tile.getText() == "🚩") {
                                tile.setText("");
                            }
                        }
                    }
                });

                boardPanel.add(tile);
            }
        }

        setMines();
        boardPanel.revalidate();
        boardPanel.repaint();
    }

    void setMines() {
        int mineLeft = mineCount;
        while (mineLeft > 0) {
            int r = random.nextInt(numRows);
            int c = random.nextInt(numCols);

            MineTile tile = board[r][c];
            if (!mineList.contains(tile)) {
                mineList.add(tile);
                mineLeft--;
            }
        }
    }

    void revealMines() {
        for (int i = 0; i < mineList.size(); i++) {
            MineTile tile = mineList.get(i);
            tile.setText("💣");
        }
        gameOver = true;
        textLabel.setText("Game Over!");
    }

    void checkMine(int r, int c) {
        if (r < 0 || r >= numRows || c < 0 || c >= numCols) {
            return;
        }

        MineTile tile = board[r][c];
        if (!tile.isEnabled()) {
            return;
        }
        tile.setEnabled(false);
        tilesClicked++;

        int minesFound = 0;

        minesFound += countMine(r-1, c-1);
        minesFound += countMine(r-1, c);
        minesFound += countMine(r-1, c+1);
        minesFound += countMine(r, c-1);
        minesFound += countMine(r, c+1);
        minesFound += countMine(r+1, c-1);
        minesFound += countMine(r+1, c);
        minesFound += countMine(r+1, c+1);

        if (minesFound > 0) {
            tile.setText(Integer.toString(minesFound));
        } else {
            tile.setText("");
            checkMine(r-1, c-1);
            checkMine(r-1, c);
            checkMine(r-1, c+1);
            checkMine(r, c-1);
            checkMine(r, c+1);
            checkMine(r+1, c-1);
            checkMine(r+1, c);
            checkMine(r+1, c+1);
        }

        if (tilesClicked == numRows * numCols - mineList.size()) {
            gameOver = true;
            textLabel.setText("Mines Cleared!");
        }
    }

    int countMine(int r, int c) {
        if (r < 0 || r >= numRows || c < 0 || c >= numCols) {
            return 0;
        }
        if (mineList.contains(board[r][c])) {
            return 1;
        }
        return 0;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Minesweeper::new);
    }
}

