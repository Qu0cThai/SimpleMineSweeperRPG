import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*;
import javax.swing.*;

public class Minesweeper {
    private class MineTile extends JButton {
        int r, c;

        public MineTile(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    private static class Boss {
        String name;
        int health;
        int attack;
        int defense;

        public Boss(String name, int health, int attack, int defense) {
            this.name = name;
            this.health = health;
            this.attack = attack;
            this.defense = defense;
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

    int score = 0;
    int tilesClicked = 0;
    boolean gameOver = false;
    int playerHealth = 100;
    int playerCoins = 100;
    int playerAttack = 10;
    int playerDefense = 5;

    int bombDamage = 10;
    int healthCounter = 0;
    int currentFloor = 1;
    int maxFloors = 3;

    Boss[] bosses = {
        new Boss("Goblin King", 100, 20, 10),
        new Boss("Dragon", 200, 40, 20),
        new Boss("Dark Knight", 400, 80, 40)
    };
    int currentBossIndex = 0;

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

        if (playerHealth <= 0) {
            resetPlayerStats();
        }

        JLabel titleLabel = new JLabel("Minesweeper RPG", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 30));
        frame.add(titleLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));

        JButton sweepButton = new JButton("Sweep");
        sweepButton.setFont(new Font("Arial", Font.PLAIN, 20));
        sweepButton.addActionListener(e -> {
            playSound("button_click.wav"); // Play button click sound
            startGame();
        });

        JButton shopButton = new JButton("Shop");
        shopButton.setFont(new Font("Arial", Font.PLAIN, 20));
        shopButton.addActionListener(e -> {
            playSound("button_click.wav");
            openShop();
        });

        JButton bossFightButton = new JButton("Boss Fight");
        bossFightButton.setFont(new Font("Arial", Font.PLAIN, 20));
        bossFightButton.addActionListener(e -> {
            playSound("button_click.wav");
            startBossFight();
        });

        buttonPanel.add(sweepButton);
        buttonPanel.add(shopButton);
        buttonPanel.add(bossFightButton);

        frame.add(buttonPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    void startGame() {
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
        if (playerHealth <= 0) {
            resetPlayerStats();
        }

        numRows = 8 + (currentFloor - 1) * 2;
        numCols = numRows;
        mineCount = 10 + (currentFloor - 1) * 5;

        tilesClicked = 0;
        gameOver = false;
        boardWidth = numCols * tileSize;
        boardHeight = numRows * tileSize;
        frame.setSize(boardWidth, boardHeight);

        board = new MineTile[numRows][numCols];
        mineList = new ArrayList<>();
        boardPanel.removeAll();
        boardPanel.setLayout(new GridLayout(numRows, numCols));

        textLabel.setText("Health: " + playerHealth + " Coins: " + playerCoins + " Floor: " + currentFloor + " Score: " + score);

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
                                    playSound("explosion.wav");
                                    textLabel.setText("You Hit a Mine! Health: " + playerHealth + " Score: " + score);

                                    if (playerHealth <= 0) {
                                        loseGame();
                                    }
                                } else {
                                    checkMine(tile.r, tile.c);
                                    textLabel.setText("Health: " + playerHealth + " Coins: " + playerCoins + " Floor: " + currentFloor + " Score: " + score);
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
        score += 10;

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

        int coinsEarned = score / 10;
        playerCoins += coinsEarned;
        playSound("lose_sound.wav");
        JOptionPane.showMessageDialog(frame, "Game Over! Coins Earned: " + coinsEarned + "\nReturning to Main Menu.", "Game Over", JOptionPane.INFORMATION_MESSAGE);
        setupMainMenu();
    }

    void winGame() {
        int coinsEarned = score / 10;
        playerCoins += coinsEarned;
        playSound("win_sound.wav");
        JOptionPane.showMessageDialog(frame, "Congratulations! You cleared all floors and won the game!\nCoins Earned: " + coinsEarned, "Victory", JOptionPane.INFORMATION_MESSAGE);
        setupMainMenu();
    }

    void openShop() {
        String[] options = { "Increase Health (100 Coins)", "Increase Attack (150 Coins)", "Increase Defense (150 Coins)", "Exit" };
        while (true) {
            int choice = JOptionPane.showOptionDialog(frame, "Coins: " + playerCoins, "Shop",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

            if (choice == 0 && playerCoins >= 100) {
                playerHealth += 10;
                playerCoins -= 100;
                healthCounter += 10;
            } else if (choice == 1 && playerCoins >= 150) {
                playerAttack += 5;
                playerCoins -= 150;
            } else if (choice == 2 && playerCoins >= 150) {
                playerDefense += 3;
                playerCoins -= 150;
            } else if (choice == 3 || choice == -1) {
                break;
            } else {
                JOptionPane.showMessageDialog(frame, "Not enough coins!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    void startBossFight() {
        if (currentBossIndex >= bosses.length) {
            JOptionPane.showMessageDialog(frame, "You have defeated all bosses! Congratulations!", "Victory", JOptionPane.INFORMATION_MESSAGE);
            setupMainMenu();
            return;
        }

        frame.getContentPane().removeAll();
        frame.setSize(400, 400);
        frame.setLayout(new BorderLayout());

        Boss boss = bosses[currentBossIndex];
        
        
        JLabel bossLabel = new JLabel(boss.name + " - Health: " + boss.health, JLabel.CENTER);
        bossLabel.setFont(new Font("Arial", Font.BOLD, 20));
        frame.add(bossLabel, BorderLayout.NORTH);

        
        JLabel playerStatsLabel = new JLabel("Player Health: " + playerHealth + " | Attack: " + playerAttack + " | Defense: " + playerDefense, JLabel.CENTER);
        playerStatsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        frame.add(playerStatsLabel, BorderLayout.SOUTH);

        JPanel combatPanel = new JPanel(new GridLayout(1, 1, 10, 10));

        JButton attackButton = new JButton("Attack");

        attackButton.addActionListener(e -> {
            if (playerHealth <= 0 || boss.health <= 0) return; 

            int damageToBoss = Math.max(0, playerAttack - boss.defense);
            int damageToPlayer = Math.max(0, boss.attack - playerDefense);

            boss.health -= damageToBoss;
            playerHealth -= damageToPlayer;

            bossLabel.setText(boss.name + " - Health: " + Math.max(0, boss.health));
            playerStatsLabel.setText("Player Health: " + Math.max(0, playerHealth) + " | Attack: " + playerAttack + " | Defense: " + playerDefense);

            if (boss.health <= 0) {
                currentBossIndex++;
                JOptionPane.showMessageDialog(frame, "You defeated " + boss.name + "!", "Victory", JOptionPane.INFORMATION_MESSAGE);
                setupMainMenu();
            } else if (playerHealth <= 0) {
                endBossFight("The boss defeated you!", "Game Over");
            }
        });

        combatPanel.add(attackButton);
        frame.add(combatPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    
    void endBossFight(String message, String title) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);

        
        if (playerHealth <= 0) {
            currentBossIndex = 0;
            resetBossHealth();
        }

        setupMainMenu(); 
    }

    void resetPlayerStats() {
        playerHealth = 100; 
        playerHealth += healthCounter; 
        score = 0;
        currentFloor = 1;
    }

    void resetBossHealth() {
    bosses = new Boss[] {
        new Boss("Goblin King", 50, 10, 5),
        new Boss("Dragon", 100, 20, 10),
        new Boss("Dark Knight", 150, 30, 20)
        }; 
    }

    public void playSound(String soundFileName) {
        try {
            // Construct the path to the sound file in the sfx folder
            File soundFile = new File("sfx/" + soundFileName);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);

            // Get a clip resource
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            // Play the sound
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace(); // Handle error if sound file is missing or invalid
        }
    }

}

