import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class Minesweeper {
    private class MineTile extends JButton {
        int r, c;

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
    MineTile[][] board;
    ArrayList<MineTile> mineList;
    Random random = new Random();

    int tilesClicked = 0;
    boolean gameOver = false;
    int playerHealth = 100;
    int playerXP = 0;
    int playerLevel = 1;
    int playerCoins = 0;

    int bombDamage = 10;
    int currentFloor = 1;
    int maxFloors = 3;

    Minesweeper() {
        setupMainMenu();
    }

    void setupMainMenu() {
        frame.getContentPane().removeAll();
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Minesweeper RPG", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 30));
        frame.add(titleLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));

        JButton sweepButton = new JButton("Sweep");
        sweepButton.setFont(new Font("Arial", Font.PLAIN, 20));
        sweepButton.addActionListener(e -> startGame());

        JButton shopButton = new JButton("Shop");
        shopButton.setFont(new Font("Arial", Font.PLAIN, 20));
        shopButton.addActionListener(e -> openShop());

        JButton bossFightButton = new JButton("Boss Fight");
        bossFightButton.setFont(new Font("Arial", Font.PLAIN, 20));
        bossFightButton.addActionListener(e -> startBossFight());

        buttonPanel.add(sweepButton);
        buttonPanel.add(shopButton);
        buttonPanel.add(bossFightButton);

        frame.add(buttonPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    void startGame() {
        playerHealth = 100;
        playerXP = 0;
        currentFloor = 1;

        frame.getContentPane().removeAll();
        frame.setSize(boardWidth, boardHeight);
        frame.setLayout(new BorderLayout());

        textLabel.setFont(new Font("Arial", Font.BOLD, 25));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textPanel.setLayout(new BorderLayout());
        textPanel.add(textLabel);
        frame.add(textPanel, BorderLayout.NORTH);

        boardPanel.setLayout(new GridLayout(numRows, numCols));
        frame.add(boardPanel);
        frame.setVisible(true);

        setupFloor();
    }

    void setupFloor() {
        tilesClicked = 0;
        gameOver = false;
        boardWidth = numCols * tileSize;
        boardHeight = numRows * tileSize;
        frame.setSize(boardWidth, boardHeight);

        board = new MineTile[numRows][numCols];
        mineList = new ArrayList<>();
        boardPanel.removeAll();
        boardPanel.setLayout(new GridLayout(numRows, numCols));

        textLabel.setText("Health: " + playerHealth + " XP: " + playerXP + " Coins: " + playerCoins + " Floor: " + currentFloor);

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
                            if (tile.getText().equals("")) {
                                if (mineList.contains(tile)) {
                                    tile.setText("\uD83D\uDCA3");
                                    playerHealth -= bombDamage;
                                    textLabel.setText("You Hit a Mine! Health: " + playerHealth + " XP: " + playerXP);

                                    if (playerHealth <= 0) {
                                        loseGame();
                                    }
                                } else {
                                    checkMine(tile.r, tile.c);
                                    if (!tile.getText().equals("")) {
                                        playerXP += 10;
                                    }
                                    textLabel.setText("Health: " + playerHealth + " XP: " + playerXP + " Coins: " + playerCoins + " Floor: " + currentFloor);
                                }
                            }
                        } else if (e.getButton() == MouseEvent.BUTTON3) {
                            if (tile.getText().equals("")) {
                                tile.setText("\uD83D\uDEA9");
                                tile.setEnabled(false);
                            } else if (tile.getText().equals("\uD83D\uDEA9")) {
                                tile.setText("");
                                tile.setEnabled(true);
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

        minesFound += countMine(r - 1, c - 1);
        minesFound += countMine(r - 1, c);
        minesFound += countMine(r - 1, c + 1);
        minesFound += countMine(r, c - 1);
        minesFound += countMine(r, c + 1);
        minesFound += countMine(r + 1, c - 1);
        minesFound += countMine(r + 1, c);
        minesFound += countMine(r + 1, c + 1);

        if (minesFound > 0) {
            tile.setText(Integer.toString(minesFound));
        } else {
            tile.setText("");
            checkMine(r - 1, c - 1);
            checkMine(r - 1, c);
            checkMine(r - 1, c + 1);
            checkMine(r, c - 1);
            checkMine(r, c + 1);
            checkMine(r + 1, c - 1);
            checkMine(r + 1, c);
            checkMine(r + 1, c + 1);
        }

        if (tilesClicked == (numRows * numCols - mineList.size())) {
            if (currentFloor < maxFloors) {
                currentFloor++;
                JOptionPane.showMessageDialog(frame, "Floor Cleared! Moving to Floor " + currentFloor + ".");
                setupFloor();
            } else {
                winGame();
            }
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

    void loseGame() {
        gameOver = true;
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                MineTile tile = board[r][c];
                if (mineList.contains(tile)) {
                    tile.setText("\uD83D\uDCA3");
                }
                tile.setEnabled(false);
            }
        }
        playerCoins += playerXP / 10;
        JOptionPane.showMessageDialog(frame, "Game Over! Coins Earned: " + (playerXP / 10) + "\nReturning to Main Menu.", "Game Over", JOptionPane.INFORMATION_MESSAGE);
        setupMainMenu();
    }

    void winGame() {
        playerCoins += playerXP / 10;
        JOptionPane.showMessageDialog(frame, "Congratulations! You cleared all floors and won the game!\nCoins Earned: " + (playerXP / 10), "Victory", JOptionPane.INFORMATION_MESSAGE);
        setupMainMenu();
    }

    void openShop() {
        JOptionPane.showMessageDialog(frame, "Shop coming soon!", "Shop", JOptionPane.INFORMATION_MESSAGE);
    }

    void startBossFight() {
        JOptionPane.showMessageDialog(frame, "Boss Fight coming soon!", "Boss Fight", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        new Minesweeper();
    }
}
