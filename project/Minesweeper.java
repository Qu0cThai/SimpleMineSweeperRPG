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
    
    JFrame frame = new JFrame("Minesweeper");
    JLabel textLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();

    int mineCount = 10;
    MineTile[][] board = new MineTile[numRows][numCols];
    ArrayList<MineTile> mineList;
    Random random = new Random();

    int tilesClicked = 0; //goal is to click all tiles except the ones containing mines
    boolean gameOver = false;

    // Start Screen components
    JPanel startPanel = new JPanel();
    JLabel titleLabel = new JLabel("Minesweeper");
    JButton startButton = new JButton("Start Game");

    Minesweeper() {
        // Setup the start screen
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
        // Show difficulty pop-up before starting the game
        String[] options = {"Easy", "Medium", "Hard"};
        int choice = JOptionPane.showOptionDialog(frame, "Choose Difficulty", "Select Difficulty",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        // Set the difficulty based on the choice
        if (choice == 0) {
            setDifficulty(8, 8, 10); // Easy
        } else if (choice == 1) {
            setDifficulty(10, 10, 20); // Medium
        } else if (choice == 2) {
            setDifficulty(12, 12, 30); // Hard
        }

        // Proceed to game setup after difficulty selection
        startGame();
    }

    void setDifficulty(int rows, int cols, int mines) {
        this.numRows = rows;
        this.numCols = cols;
        this.mineCount = mines;
        this.boardWidth = numCols * tileSize;
        this.boardHeight = numRows * tileSize;

        // Update the frame size
        frame.setSize(boardWidth, boardHeight);
        startPanel.setVisible(false); // Hide start screen
        resetGame(); // Reset the game with new parameters
    }

    void startGame() {
        // Setup game board and start the game
        textLabel.setFont(new Font("Arial", Font.BOLD, 25));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Minesweeper: " + Integer.toString(mineCount));
        textLabel.setOpaque(true);

        textPanel.setLayout(new BorderLayout());
        textPanel.add(textLabel);
        frame.add(textPanel, BorderLayout.NORTH);

        boardPanel.setLayout(new GridLayout(numRows, numCols)); // 8x8 by default
        frame.add(boardPanel);

        frame.setVisible(true);

        setMines();
    }

    void resetGame() {
        tilesClicked = 0;
        gameOver = false;
        board = new MineTile[numRows][numCols];
        mineList = new ArrayList<MineTile>();
        boardPanel.setLayout(new GridLayout(numRows, numCols));

        textLabel.setText("Minesweeper: " + Integer.toString(mineCount));

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

                        //left click
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            if (tile.getText() == "") {
                                if (mineList.contains(tile)) {
                                    revealMines();
                                }
                                else {
                                    checkMine(tile.r, tile.c);
                                }
                            }
                        }
                        //right click
                        else if (e.getButton() == MouseEvent.BUTTON3) {
                            if (tile.getText() == "" && tile.isEnabled()) {
                                tile.setText("🚩");
                            }
                            else if (tile.getText() == "🚩") {
                                tile.setText("");
                            }
                        }
                    } 
                });

                boardPanel.add(tile);
            }
        }

        setMines(); // Set mines for the new board
        boardPanel.revalidate();
        boardPanel.repaint();
    }

    void setMines() {
        int mineLeft = mineCount;
        while (mineLeft > 0) {
            int r = random.nextInt(numRows); //0 - numRows-1
            int c = random.nextInt(numCols); //0 - numCols-1

            MineTile tile = board[r][c]; 
            if (!mineList.contains(tile)) {
                mineList.add(tile);
                mineLeft -= 1;
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
        tilesClicked += 1;

        int minesFound = 0;

        // Check surrounding tiles for mines
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
        }
        else {
            tile.setText("");
            // Recursively check surrounding tiles if no mines are found
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
