package game;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    public static final int WIDTH = 600;
    public static final int HEIGHT = 900;

    // State Tambahan: SHOP
    private enum State { MENU, PLAYING, PAUSED, GAME_OVER, SHOP }

    private State state = State.MENU;

    private Timer timer;
    private int score;
    private int lives;
    private static final int MAX_LIVES = 3;
    private Ninja ninja;
    private Random random;
    private boolean isNewHighScore = false;

    // Waktu bermain
    private long gameStartMillis;
    private long pausedAccumMillis;
    private long pauseStartMillis;
    private long finalPlayDurationMillis = 0;

    private double bgOffsetY = 0;

    private String playerName = "Ninja";
    private HighScoreManager.HighScoreData highScore;
    private java.awt.Image customBgImage;
    private java.awt.Image topBgImage;
    private java.awt.Image skyBgImage;

    // Fitur Skin (3 Pilihan Skin Spritesheet)
    private String equippedSkin = "ninja_1.png";

    // Aset Gambar Buff Item
    private java.awt.Image parachuteImage;
    private java.awt.Image springImage;

    private Clip backgroundMusic;

    // Struktur Data
    private LinkedList<Platform> activePlatforms;
    private LinkedList<Item> activeItems;
    private Queue<String> platformPatternQueue;
    private DifficultyTree difficultyTree;
    private DifficultyNode currentDifficulty;

    private boolean keyLeft = false;
    private boolean keyRight = false;

    // Komponen UI Menu & Game
    private RoundedTextField nameField;
    private RoundedButton okButton;
    private RoundedButton shopButton;
    private RoundedButton exitButton;
    private RoundedButton pauseButton;
    private RoundedButton resumeButton;
    private RoundedButton menuFromPauseButton;
    private RoundedButton restartButton;
    private RoundedButton menuFromGameOverButton;

    // Komponen UI Toko Skin
    private RoundedButton btnSkinDefault;
    private RoundedButton btnSkinBlue;
    private RoundedButton btnSkinGold;
    private RoundedButton btnBackToMenu;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        setLayout(null);
        addKeyListener(this);
        random = new Random();
        highScore = HighScoreManager.load();

        try {
            customBgImage = new javax.swing.ImageIcon(getClass().getResource("resources/ninja_bg.png")).getImage();
            topBgImage = new javax.swing.ImageIcon(getClass().getResource("resources/ninja_bg_2.png")).getImage();
            skyBgImage = new javax.swing.ImageIcon(getClass().getResource("resources/ninja_bg_3.png")).getImage();

            parachuteImage = new javax.swing.ImageIcon(getClass().getResource("resources/parachute.png")).getImage();
            springImage = new javax.swing.ImageIcon(getClass().getResource("resources/spring.png")).getImage();
        } catch (Exception e) {
            System.out.println("[ERROR]: Gagal memuat gambar resources! " + e.getMessage());
        }

        buildMenuUI();
        buildShopUI(); // Inisialisasi UI Toko Skin
        buildInGameUI();
        buildPauseUI();
        buildGameOverUI();

        applyVisibilityForState();
        SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());

        timer = new Timer(16, this);
        timer.start();
    }

    private void buildMenuUI() {
        nameField = new RoundedTextField(16);
        nameField.setBounds(WIDTH / 2 - 130, 355, 260, 44);
        nameField.setText(playerName);
        nameField.addActionListener(e -> startGame());
        add(nameField);

        okButton = new RoundedButton("OK - MULAI", new Color(46, 160, 90), new Color(60, 200, 115), Color.WHITE);
        okButton.setBounds(WIDTH / 2 - 110, 412, 220, 46);
        okButton.addActionListener(e -> startGame());
        add(okButton);

        shopButton = new RoundedButton("SKIN", new Color(218, 165, 32), new Color(238, 200, 50), Color.WHITE);
        shopButton.setBounds(WIDTH / 2 - 110, 468, 220, 46);
        shopButton.addActionListener(e -> goToShop());
        add(shopButton);

        exitButton = new RoundedButton("KELUAR", new Color(178, 34, 52), new Color(214, 55, 75), Color.WHITE);
        exitButton.setBounds(WIDTH / 2 - 110, 524, 220, 46);
        exitButton.addActionListener(e -> System.exit(0)); // Menutup aplikasi saat diklik
        add(exitButton);
    }

    private void buildShopUI() {
        btnSkinDefault = new RoundedButton("PAKAI", new Color(46, 160, 90), new Color(60, 200, 115), Color.WHITE);
        btnSkinDefault.setBounds(380, 318, 140, 40);
        btnSkinDefault.addActionListener(e -> selectSkin("ninja_1.png"));
        add(btnSkinDefault);

        btnSkinBlue = new RoundedButton("PAKAI", new Color(46, 160, 90), new Color(60, 200, 115), Color.WHITE);
        btnSkinBlue.setBounds(380, 458, 140, 40);
        btnSkinBlue.addActionListener(e -> selectSkin("ninja_2.png"));
        add(btnSkinBlue);

        btnSkinGold = new RoundedButton("PAKAI", new Color(46, 160, 90), new Color(60, 200, 115), Color.WHITE);
        btnSkinGold.setBounds(380, 598, 140, 40);
        btnSkinGold.addActionListener(e -> selectSkin("ninja_3.png"));
        add(btnSkinGold);

        btnBackToMenu = new RoundedButton("KEMBALI", new Color(80, 80, 95), new Color(105, 105, 125), Color.WHITE);
        btnBackToMenu.setBounds(WIDTH / 2 - 100, 720, 200, 45);
        btnBackToMenu.addActionListener(e -> goToMenu());
        add(btnBackToMenu);
    }

    private void buildInGameUI() {
        pauseButton = new RoundedButton("\u275A\u275A", new Color(178, 34, 52), new Color(214, 55, 75), Color.WHITE);
        pauseButton.setBounds(WIDTH - 54, 16, 44, 34);
        pauseButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        pauseButton.addActionListener(e -> pauseGame());
        add(pauseButton);
    }

    private void buildPauseUI() {
        resumeButton = new RoundedButton("LANJUTKAN", new Color(46, 160, 90), new Color(60, 200, 115), Color.WHITE);
        resumeButton.setBounds(WIDTH / 2 - 110, HEIGHT / 2, 220, 52);
        resumeButton.addActionListener(e -> resumeGame());
        add(resumeButton);

        menuFromPauseButton = new RoundedButton("MENU UTAMA", new Color(80, 80, 95), new Color(105, 105, 125), Color.WHITE);
        menuFromPauseButton.setBounds(WIDTH / 2 - 110, HEIGHT / 2 + 68, 220, 52);
        menuFromPauseButton.addActionListener(e -> goToMenu());
        add(menuFromPauseButton);
    }

    private void buildGameOverUI() {
        restartButton = new RoundedButton("MAIN LAGI", new Color(46, 160, 90), new Color(60, 200, 115), Color.WHITE);
        restartButton.setBounds(WIDTH / 2 - 170, HEIGHT / 2 + 80, 160, 52);
        restartButton.addActionListener(e -> startGame());
        add(restartButton);

        menuFromGameOverButton = new RoundedButton("MENU UTAMA", new Color(80, 80, 95), new Color(105, 105, 125), Color.WHITE);
        menuFromGameOverButton.setBounds(WIDTH / 2 + 10, HEIGHT / 2 + 80, 160, 52);
        menuFromGameOverButton.addActionListener(e -> goToMenu());
        add(menuFromGameOverButton);
    }

    private void applyVisibilityForState() {
        nameField.setVisible(state == State.MENU);
        okButton.setVisible(state == State.MENU);
        if (shopButton != null) shopButton.setVisible(state == State.MENU);
        if (exitButton != null) exitButton.setVisible(state == State.MENU);

        pauseButton.setVisible(state == State.PLAYING);
        resumeButton.setVisible(state == State.PAUSED);
        menuFromPauseButton.setVisible(state == State.PAUSED);
        restartButton.setVisible(state == State.GAME_OVER);
        menuFromGameOverButton.setVisible(state == State.GAME_OVER);

        // Visibilitas Tombol Toko
        boolean isShop = (state == State.SHOP);
        if (btnSkinDefault != null) btnSkinDefault.setVisible(isShop);
        if (btnSkinBlue != null) btnSkinBlue.setVisible(isShop);
        if (btnSkinGold != null) btnSkinGold.setVisible(isShop);
        if (btnBackToMenu != null) btnBackToMenu.setVisible(isShop);
    }

    private void goToShop() {
        state = State.SHOP;
        updateShopButtons();
        applyVisibilityForState();
        repaint();
    }

    private void selectSkin(String skinName) {
        equippedSkin = skinName;
        if (ninja != null) {
            ninja.setSkin(equippedSkin);
        }
        updateShopButtons();
        repaint();
    }

    private void updateShopButtons() {
        btnSkinDefault.setText(equippedSkin.equals("ninja_1.png") ? "DIPAKAI" : "PAKAI");
        btnSkinDefault.setEnabled(!equippedSkin.equals("ninja_1.png"));

        btnSkinBlue.setText(equippedSkin.equals("ninja_2.png") ? "DIPAKAI" : "PAKAI");
        btnSkinBlue.setEnabled(!equippedSkin.equals("ninja_2.png"));

        btnSkinGold.setText(equippedSkin.equals("ninja_3.png") ? "DIPAKAI" : "PAKAI");
        btnSkinGold.setEnabled(!equippedSkin.equals("ninja_3.png"));
    }

    private void startGame() {
        String typed = nameField.getText().trim();
        playerName = typed.isEmpty() ? "Ninja" : typed;
        restartGame();
        state = State.PLAYING;
        playBGM();
        applyVisibilityForState();
        requestFocusInWindow();
    }

    private void pauseGame() {
        if (state != State.PLAYING) return;
        pauseStartMillis = System.currentTimeMillis();
        state = State.PAUSED;
        applyVisibilityForState();
    }

    private void resumeGame() {
        if (state != State.PAUSED) return;
        pausedAccumMillis += (System.currentTimeMillis() - pauseStartMillis);
        state = State.PLAYING;
        applyVisibilityForState();
        requestFocusInWindow();
    }

    private void goToMenu() {
        state = State.MENU;
        nameField.setText(playerName);
        applyVisibilityForState();
        nameField.requestFocusInWindow();
    }

    private void triggerGameOver() {
        state = State.GAME_OVER;
        finalPlayDurationMillis = System.currentTimeMillis() - gameStartMillis - pausedAccumMillis;

        stopBGM();
        isNewHighScore = score > highScore.score;
        if (isNewHighScore) {
            highScore = new HighScoreManager.HighScoreData(score, playerName);
            HighScoreManager.save(highScore);
        }
        applyVisibilityForState();
    }

    public void restartGame() {
        ninja = new Ninja(180, 700, 100);
        ninja.setSkin(equippedSkin); // Pasang skin terpilih saat awal bermain

        score = 0;
        lives = MAX_LIVES;
        keyLeft = false;
        keyRight = false;
        isNewHighScore = false;
        gameStartMillis = System.currentTimeMillis();
        pausedAccumMillis = 0;
        finalPlayDurationMillis = 0;

        bgOffsetY = 0;

        activePlatforms = new LinkedList<>();
        activeItems = new LinkedList<>();
        difficultyTree = new DifficultyTree();
        currentDifficulty = difficultyTree.searchDifficulty(score);
        platformPatternQueue = new java.util.LinkedList<>();
        replenishQueue();
        generateInitialPlatforms();
    }

    private void replenishQueue() {
        double breakableChance = currentDifficulty.getBreakableProb();
        for (int i = 0; i < 8; i++) {
            String type = "NORMAL";
            if (!currentDifficulty.getLevelName().equals("EASY")) {
                double roll = random.nextDouble();
                if (roll < breakableChance) {
                    type = "BREAKABLE";
                } else if (roll < breakableChance + 0.30) {
                    type = "MOVING";
                }
            }
            platformPatternQueue.add(type);
        }
    }

    private void generateInitialPlatforms() {
        activePlatforms.clear();
        activeItems.clear();

        Platform basePlatform = new Platform(150, 770, 100, 15, "NORMAL");
        activePlatforms.add(basePlatform);

        int currentY = 770;
        while (currentY > 0) {
            int gap = random.nextInt(currentDifficulty.getMaxGapY() - currentDifficulty.getMinGapY() + 1) + currentDifficulty.getMinGapY();
            currentY -= gap;
            int x = random.nextInt(WIDTH - 80);
            if (platformPatternQueue.isEmpty()) replenishQueue();
            String type = platformPatternQueue.poll();

            Platform p = new Platform(x, currentY, 80, 15, type);
            activePlatforms.add(p);

            if (random.nextDouble() < 0.15 && !type.equals("BREAKABLE")) {
                String itemType = random.nextBoolean() ? "SPRING" : "PARACHUTE";
                activeItems.add(new Item(p, itemType));
            }
        }
    }

    private void respawnNinja() {
        ninja.setX(WIDTH / 2.0 - ninja.getWidth() / 2.0);
        ninja.setY(700.0);
        ninja.bounce();
    }

    private long elapsedMillis() {
        if (state == State.PLAYING) {
            return System.currentTimeMillis() - gameStartMillis - pausedAccumMillis;
        } else if (state == State.PAUSED) {
            return pauseStartMillis - gameStartMillis - pausedAccumMillis;
        } else if (state == State.GAME_OVER) {
            return finalPlayDurationMillis;
        }
        return 0;
    }

    private String formatTime(long millis) {
        long totalSeconds = Math.max(0, millis / 1000);
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == timer) {
            gameTick();
            repaint();
        }
    }

    private void gameTick() {
        if (state != State.PLAYING) return;

        for (Platform p : activePlatforms) {
            p.update();
        }

        int direction = 0;
        if (keyLeft) direction = -1;
        if (keyRight) direction = 1;
        ninja.move(direction);
        ninja.update(WIDTH);

        // Tabrakan Platform
        Platform collidedPlatform = null;
        for (Platform p : activePlatforms) {
            if (p.checkCollision(ninja)) {
                ninja.bounce();
                collidedPlatform = p;
                break;
            }
        }

        if (collidedPlatform != null && collidedPlatform.getType().equals("BREAKABLE")) {
            collidedPlatform.setBroken(true);
            activePlatforms.remove(collidedPlatform);
        }

        // Kamera & Scrolling
        double cameraShift = 0;
        if (ninja.getY() < HEIGHT / 2) {
            double targetShift = HEIGHT / 2.0 - ninja.getY();
            cameraShift = targetShift * 0.12;

            ninja.setY(ninja.getY() + cameraShift);
            bgOffsetY += (cameraShift * 0.4);

            for (Platform p : activePlatforms) {
                p.setY(p.getY() + (int) cameraShift);
            }
        }

        // Update Item Buff
        java.util.Iterator<Item> itemIt = activeItems.iterator();
        while (itemIt.hasNext()) {
            Item item = itemIt.next();
            item.checkCollision(ninja);

            if (item.getY() > HEIGHT || item.isCollected()) {
                itemIt.remove();
            }
        }

        // Generasi Platform Baru
        Platform highestPlatform = activePlatforms.getLast();
        while (highestPlatform.getY() > 0) {
            int gap = random.nextInt(currentDifficulty.getMaxGapY() - currentDifficulty.getMinGapY() + 1) + currentDifficulty.getMinGapY();
            int nextY = highestPlatform.getY() - gap;

            if (platformPatternQueue.isEmpty()) replenishQueue();
            String nextType = platformPatternQueue.poll();

            int platformWidth = nextType.equals("MOVING") ? 120 : (nextType.equals("BREAKABLE") ? 80 : 100);
            int nextX = random.nextInt(WIDTH - platformWidth);

            Platform newPlatform = new Platform(nextX, nextY, platformWidth, 15, nextType);
            activePlatforms.add(newPlatform);
            highestPlatform = newPlatform;

            if (random.nextDouble() < 0.20 && !nextType.equals("BREAKABLE")) {
                String itemType = random.nextBoolean() ? "SPRING" : "PARACHUTE";
                activeItems.add(new Item(newPlatform, itemType));
            }
        }

        while (!activePlatforms.isEmpty() && activePlatforms.getFirst().getY() > HEIGHT) {
            activePlatforms.removeFirst();
        }

        double ninjaBottom = ninja.getY() + ninja.getHeight();
        for (Platform p : activePlatforms) {
            if (!p.isPassed() && ninjaBottom < p.getY()) {
                p.setPassed(true);
                score++;

                DifficultyNode newDiff = difficultyTree.searchDifficulty(score);
                if (newDiff != null && newDiff != currentDifficulty) {
                    currentDifficulty = newDiff;
                }
            }
        }

        if (ninja.getY() > HEIGHT) {
            lives--;
            if (lives <= 0) {
                triggerGameOver();
            } else {
                respawnNinja();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawDynamicBackground(g2d);

        if (state == State.MENU) {
            drawMenuScreen(g2d);
            return;
        }

        if (state == State.SHOP) {
            drawShopScreen(g2d);
            return;
        }

        // Draw Platform
        for (Platform p : activePlatforms) {
            p.draw(g2d);
        }

        // Draw Item Buff
        for (Item item : activeItems) {
            item.draw(g2d, parachuteImage, springImage);
        }

        // Draw Ninja
        ninja.draw(g2d);

        // Draw Parasut
        if (ninja.hasParachute() && parachuteImage != null) {
            int parachuteX = (int) ninja.getX() - 5;
            int parachuteY = (ninja.getVy() >= 0) ? (int) ninja.getY() - 5 : (int) ninja.getY() - 35;
            g2d.drawImage(parachuteImage, parachuteX, parachuteY, 60, 45, this);
        }

        drawHUD(g2d);

        if (state == State.PAUSED) {
            drawPauseOverlay(g2d);
        } else if (state == State.GAME_OVER) {
            drawGameOverScreen(g2d);
        }
    }

    private void drawDynamicBackground(Graphics2D g2d) {
        int textureHeight = 2172;
        int yOffset = (int) (bgOffsetY % textureHeight);
        int startY = -1200 + yOffset;

        if (bgOffsetY < textureHeight) {
            if (customBgImage != null) {
                g2d.drawImage(customBgImage, 0, startY, WIDTH, textureHeight, this);
                if (topBgImage != null) {
                    g2d.drawImage(topBgImage, 0, startY - textureHeight, WIDTH, textureHeight, this);
                }
            }
        } else if (bgOffsetY >= textureHeight && bgOffsetY < textureHeight * 2) {
            if (topBgImage != null) {
                g2d.drawImage(topBgImage, 0, startY, WIDTH, textureHeight, this);
                if (skyBgImage != null) {
                    g2d.drawImage(skyBgImage, 0, startY - textureHeight, WIDTH, textureHeight, this);
                } else {
                    g2d.drawImage(topBgImage, 0, startY - textureHeight, WIDTH, textureHeight, this);
                }
            }
        } else {
            if (skyBgImage != null) {
                g2d.drawImage(skyBgImage, 0, startY, WIDTH, textureHeight, this);
                g2d.drawImage(skyBgImage, 0, startY - textureHeight, WIDTH, textureHeight, this);
                g2d.drawImage(skyBgImage, 0, startY + textureHeight, WIDTH, textureHeight, this);
            }
        }

        if (customBgImage == null && topBgImage == null && skyBgImage == null) {
            Color topColor = new Color(135, 206, 250);
            Color bottomColor = new Color(70, 130, 180);
            GradientPaint gp = new GradientPaint(0, 0, topColor, 0, HEIGHT, bottomColor);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, WIDTH, HEIGHT);
        }
    }

    private void drawHUD(Graphics2D g2d) {
        int hudWidth = WIDTH - 90;
        g2d.setColor(new Color(0, 0, 0, 130));
        g2d.fill(new RoundRectangle2D.Double(10, 10, hudWidth, 108, 14, 14));
        g2d.setColor(new Color(255, 255, 255, 80));
        g2d.draw(new RoundRectangle2D.Double(10, 10, hudWidth, 108, 14, 14));

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2d.drawString("Score: " + score, 22, 34);

        g2d.setColor(new Color(255, 215, 0));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2d.drawString("Waktu: " + formatTime(elapsedMillis()), 150, 34);

        drawLives(g2d, 22, 44);

        String level = currentDifficulty.getLevelName();
        if (level.equals("EASY")) g2d.setColor(new Color(50, 205, 50));
        else if (level.equals("MEDIUM")) g2d.setColor(new Color(255, 165, 0));
        else g2d.setColor(new Color(220, 20, 60));

        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2d.drawString("Difficulty: " + level, hudWidth - 130, 63);

        g2d.setColor(new Color(245, 245, 245));
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2d.drawString("LinkedList Platform: " + activePlatforms.size() + " aktif", 22, 85);

        String queuePreview = "Queue (FIFO): ";
        if (!platformPatternQueue.isEmpty()) {
            java.util.Iterator<String> it = platformPatternQueue.iterator();
            queuePreview += "[" + it.next().substring(0, 4) + ", "
                    + (it.hasNext() ? it.next().substring(0, 4) : "") + ", "
                    + (it.hasNext() ? it.next().substring(0, 4) : "") + ", ...]";
        } else {
            queuePreview += "[]";
        }
        g2d.drawString(queuePreview, 22, 101);
    }

    private void drawLives(Graphics2D g2d, int x, int y) {
        g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
        int cursorX = x;
        for (int i = 0; i < MAX_LIVES; i++) {
            if (i < lives) g2d.setColor(new Color(220, 20, 60));
            else g2d.setColor(new Color(255, 255, 255, 60));
            g2d.drawString("\u2665", cursorX, y + 18);
            cursorX += 24;
        }
    }

    private void drawMenuScreen(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // KOTAK CONTAINER (Ditinggikan sedikit menjadi 550px)
        g2d.setColor(new Color(20, 20, 40, 150));
        g2d.fill(new RoundRectangle2D.Double(50, 160, WIDTH - 100, 550, 24, 24));
        g2d.setColor(new Color(255, 215, 0, 120));
        g2d.draw(new RoundRectangle2D.Double(50, 160, WIDTH - 100, 550, 24, 24));

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 34));
        drawCentered(g2d, "NINJA SKY JUMP", 230);

        g2d.setColor(new Color(255, 215, 0));
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 15));
        drawCentered(g2d, "Gunakan A/D atau <- -> untuk bergerak", 265);

        g2d.setColor(new Color(220, 220, 230));
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
        drawCentered(g2d, "Masukkan Nama Pemain", 325);

        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.setFont(new Font("SansSerif", Font.ITALIC, 12));
        drawCentered(g2d, "Tekan ENTER atau klik tombol untuk mulai", 590);

        // KOTAK HIGH SCORE (Diturunkan posisi Y nya ke 605)
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fill(new RoundRectangle2D.Double(WIDTH / 2 - 170, 605, 340, 58, 16, 16));
        g2d.setColor(new Color(255, 215, 0, 160));
        g2d.draw(new RoundRectangle2D.Double(WIDTH / 2 - 170, 605, 340, 58, 16, 16));

        g2d.setColor(new Color(255, 215, 0));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 15));
        drawCentered(g2d, "\u2605 HIGH SCORE: " + highScore.score, 628);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        drawCentered(g2d, "Dipegang oleh: " + highScore.name, 648);

        g2d.setColor(new Color(200, 200, 210));
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        drawCentered(g2d, "Nyawa: " + MAX_LIVES + "x kesempatan  |  Skor & waktu tercatat tiap sesi", 690);
    }

    private void drawShopScreen(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        g2d.setColor(new Color(25, 20, 45, 180));
        g2d.fill(new RoundRectangle2D.Double(40, 80, WIDTH - 80, 720, 24, 24));
        g2d.setColor(new Color(218, 165, 32, 120));
        g2d.draw(new RoundRectangle2D.Double(40, 80, WIDTH - 80, 720, 24, 24));

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 36));
        drawCentered(g2d, "PILIH SKIN NINJA", 140);

        g2d.setColor(new Color(255, 215, 0));
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 15));
        drawCentered(g2d, "Pilih tampilan ninja favoritmu secara gratis!", 175);

        drawSkinCard(g2d, "Ninja 1", "ninja_1.png", 280);
        drawSkinCard(g2d, "Ninja 2", "ninja_2.png", 420);
        drawSkinCard(g2d, "Ninja 3", "ninja_3.png", 560);
    }

    private void drawSkinCard(Graphics2D g2d, String name, String spritesheetName, int y) {
        g2d.setColor(new Color(255, 255, 255, 20));
        g2d.fillRoundRect(60, y, WIDTH - 120, 120, 16, 16);
        g2d.setColor(new Color(255, 255, 255, 40));
        g2d.drawRoundRect(60, y, WIDTH - 120, 120, 16, 16);

        // Frame Pertama untuk Pratinjau
        try {
            java.net.URL url = getClass().getResource("resources/" + spritesheetName);
            if (url == null) {
                url = getClass().getResource("/game/resources/" + spritesheetName);
            }

            if (url != null) {
                java.awt.image.BufferedImage sheet = javax.imageio.ImageIO.read(url);
                int totalFrames = 4;
                int frameWidth = sheet.getWidth() / totalFrames;

                java.awt.Image previewFrame = sheet.getSubimage(0, 0, frameWidth, sheet.getHeight());
                g2d.drawImage(previewFrame, 80, y + 20, 50, 75, this);
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Gagal memuat preview " + spritesheetName + ": " + e.getMessage());
        }

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2d.drawString(name, 150, y + 50);

        g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
        if (equippedSkin.equals(spritesheetName)) {
            g2d.setColor(new Color(50, 205, 50));
            g2d.drawString("Sedang Digunakan", 150, y + 80);
        } else {
            g2d.setColor(new Color(200, 200, 200));
            g2d.drawString("Gratis", 150, y + 80);
        }
    }

    private void drawPauseOverlay(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 170));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 34));
        drawCentered(g2d, "PAUSED", HEIGHT / 2 - 70);
        g2d.setColor(new Color(255, 215, 0));
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 15));
        drawCentered(g2d, "Skor saat ini: " + score + "   |   Waktu: " + formatTime(elapsedMillis()), HEIGHT / 2 - 30);
    }

    private void drawGameOverScreen(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        g2d.setColor(new Color(220, 20, 60));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 36));
        drawCentered(g2d, "GAME OVER", HEIGHT / 2 - 90);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 18));
        drawCentered(g2d, playerName + " - Skor Akhir: " + score, HEIGHT / 2 - 40);

        g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
        drawCentered(g2d, "Waktu bermain: " + formatTime(elapsedMillis()), HEIGHT / 2 - 12);

        if (isNewHighScore) {
            g2d.setColor(new Color(255, 215, 0));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 18));
            drawCentered(g2d, "\u2605 HIGH SCORE BARU! \u2605", HEIGHT / 2 + 20);
        } else {
            g2d.setColor(new Color(200, 200, 210));
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
            drawCentered(g2d, "High Score: " + highScore.score + " (" + highScore.name + ")", HEIGHT / 2 + 20);
        }
    }

    private void drawCentered(Graphics2D g2d, String text, int y) {
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g2d.drawString(text, (WIDTH - textWidth) / 2, y);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (state == State.PLAYING) {
            if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) keyLeft = true;
            if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) keyRight = true;
        }
        if (keyCode == KeyEvent.VK_ESCAPE) {
            if (state == State.PLAYING) pauseGame();
            else if (state == State.PAUSED) resumeGame();
        }
        if (keyCode == KeyEvent.VK_SPACE && state == State.GAME_OVER) startGame();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) keyLeft = false;
        if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) keyRight = false;
    }

    private void playBGM() {
        try {
            if (backgroundMusic != null && backgroundMusic.isRunning()) {
                backgroundMusic.stop();
            }

            java.net.URL soundURL = getClass().getResource("resources/bgm_game.wav");

            if (soundURL != null) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
                backgroundMusic = AudioSystem.getClip();
                backgroundMusic.open(audioStream);
                backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
                backgroundMusic.start();
            }
        } catch (Exception e) {
            System.out.println("[ERROR]: Gagal memutar musik latar: " + e.getMessage());
        }
    }

    private void stopBGM() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }
}