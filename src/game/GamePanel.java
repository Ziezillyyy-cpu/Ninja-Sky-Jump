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

/**
 * Kelas GamePanel.java
 * Merupakan area game loop utama yang menangani rendering, pembaruan logika,
 * input keyboard/mouse pemain, serta pengintegrasian seluruh struktur data.
 */
public class GamePanel extends JPanel implements ActionListener, KeyListener {
    public static final int WIDTH = 600;
    public static final int HEIGHT = 900;

    /** State mesin utama layar game */
    private enum State { MENU, PLAYING, PAUSED, GAME_OVER }

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

    // Posisi pergeseran latar belakang (Scrolling Background)
    private double bgOffsetY = 0;

    // Data pemain & high score
    private String playerName = "Ninja";
    private HighScoreManager.HighScoreData highScore;
    private java.awt.Image customBgImage;
    private java.awt.Image topBgImage;
    private java.awt.Image skyBgImage;
    private Clip backgroundMusic;
    // ==========================================
    // INTEGRASI STRUKTUR DATA
    // ==========================================
    private LinkedList<Platform> activePlatforms;
    private Queue<String> platformPatternQueue;
    private DifficultyTree difficultyTree;
    private DifficultyNode currentDifficulty;

    private boolean keyLeft = false;
    private boolean keyRight = false;

    // ==========================================
    // KOMPONEN UI (Menu, Pause, Game Over)
    // ==========================================
    private RoundedTextField nameField;
    private RoundedButton okButton;
    private RoundedButton pauseButton;
    private RoundedButton resumeButton;
    private RoundedButton menuFromPauseButton;
    private RoundedButton restartButton;
    private RoundedButton menuFromGameOverButton;


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
        } catch (Exception e) {
            System.out.println("[ERROR]: Gagal memuat gambar latar belakang! " + e.getMessage());
        }

        buildMenuUI();
        buildInGameUI();
        buildPauseUI();
        buildGameOverUI();

        applyVisibilityForState();
        SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());

        timer = new Timer(16, this);
        timer.start();
    }

    // ==========================================
    // PEMBANGUNAN KOMKOMPONEN UI
    // ==========================================
    private void buildMenuUI() {
        nameField = new RoundedTextField(16);
        nameField.setBounds(WIDTH / 2 - 130, 358, 260, 46);
        nameField.setText(playerName);
        nameField.addActionListener(e -> startGame());
        add(nameField);

        okButton = new RoundedButton("OK - MULAI", new Color(46, 160, 90), new Color(60, 200, 115), Color.WHITE);
        okButton.setBounds(WIDTH / 2 - 100, 424, 200, 54);
        okButton.addActionListener(e -> startGame());
        add(okButton);
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
        pauseButton.setVisible(state == State.PLAYING);
        resumeButton.setVisible(state == State.PAUSED);
        menuFromPauseButton.setVisible(state == State.PAUSED);
        restartButton.setVisible(state == State.GAME_OVER);
        menuFromGameOverButton.setVisible(state == State.GAME_OVER);
    }

    // ==========================================
    // TRANSISI STATE
    // ==========================================
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

        stopBGM();
        isNewHighScore = score > highScore.score;
        if (isNewHighScore) {
            highScore = new HighScoreManager.HighScoreData(score, playerName);
            HighScoreManager.save(highScore);
        }
        applyVisibilityForState();
    }
    public void restartGame() {
        ninja = new Ninja(180, 700, 100); // Y diset di 700
        score = 0;
        lives = MAX_LIVES;
        keyLeft = false;
        keyRight = false;
        isNewHighScore = false;
        gameStartMillis = System.currentTimeMillis();
        pausedAccumMillis = 0;

        // Reset background offset ke 0
        bgOffsetY = 0;

        activePlatforms = new LinkedList<>();
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

            // Mengatur kemunculan platform BREAKABLE atau MOVING di luar mode EASY
            if (!currentDifficulty.getLevelName().equals("EASY")) {
                double roll = random.nextDouble();
                if (roll < breakableChance) {
                    type = "BREAKABLE";
                } else if (roll < breakableChance + 0.30) { // Peluang 30% untuk tipe bergerak
                    type = "MOVING";
                }
            }
            platformPatternQueue.add(type);
        }
    }
    private void generateInitialPlatforms() {
        activePlatforms.clear();

        // Pijakan dasar dibuat di Y = 770 agar menopang ninja saat frame pertama
        Platform basePlatform = new Platform(150, 770, 100, 15, "NORMAL");
        activePlatforms.add(basePlatform);

        int currentY = 770;
        while (currentY > 0) {
            int gap = random.nextInt(currentDifficulty.getMaxGapY() - currentDifficulty.getMinGapY() + 1) + currentDifficulty.getMinGapY();
            currentY -= gap;
            int x = random.nextInt(WIDTH - 80);
            if (platformPatternQueue.isEmpty()) replenishQueue();
            String type = platformPatternQueue.poll();
            activePlatforms.add(new Platform(x, currentY, 80, 15, type));
        }
    }


    private void respawnNinja() {
        ninja.setX(WIDTH / 2.0 - ninja.getWidth() / 2.0);
        ninja.setY(700.0); // Ubah dari 1400.0 ke 700.0 agar aman di layar 900
        ninja.bounce();
    }

    private long elapsedMillis() {
        if (state == State.PLAYING) {
            return System.currentTimeMillis() - gameStartMillis - pausedAccumMillis;
        } else if (state == State.PAUSED) {
            return pauseStartMillis - gameStartMillis - pausedAccumMillis;
        } else if (state == State.GAME_OVER) {
            return System.currentTimeMillis() - gameStartMillis - pausedAccumMillis;
        }
        return 0;
    }

    private String formatTime(long millis) {
        long totalSeconds = Math.max(0, millis / 1000);
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // ==========================================
    // GAME LOOP
    // ==========================================
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

        // =========================================================================
        // PERBAIKAN DETEKSI TABRAKAN (ANTI LOMPAT GAIB SEBELAH KIRI)
        // =========================================================================
        Platform collidedPlatform = null;
        for (Platform p : activePlatforms) {

            // 1. Ambil batas koordinat kaki vertikal Ninja
            double kakiNinjaY = ninja.getY() + ninja.getHeight() - 45;
            int jarakVertikal = Math.abs((int) (kakiNinjaY - p.getY()));

            // Toleransi vertikal diperketat dari 30px menjadi 18px agar tidak floating
            if (jarakVertikal < 18) {

                // 2. KUNCI HORIZONTAL: Pangkas padding kosong di sebelah kiri gambar Ninja
                // (Menggeser hitbox kiri visual sejauh 35px agar pas di tubuh real karakter)
                int paddingKiriNinja = 35;
                int paddingKananNinja = 8;

                double batasKiriNinja = ninja.getX() + paddingKiriNinja;
                double batasKananNinja = ninja.getX() + ninja.getWidth() - paddingKananNinja;

                // 3. Cek apakah badan visual Ninja benar-benar berada di atas jangkauan platform X
                boolean adaDiAtasPlatform = (batasKananNinja > p.getX()) && (batasKiriNinja < p.getX() + p.getWidth());

                if (adaDiAtasPlatform) {
                    // Panggil fungsi checkCollision internal milik platform sebagai validasi final
                    if (p.checkCollision(ninja)) {
                        ninja.bounce();
                        collidedPlatform = p;
                        break;
                    }
                }
            }
        }

        // Eksekusi jika platform yang diinjak adalah jenis yang bisa hancur
        if (collidedPlatform != null && collidedPlatform.getType().equals("BREAKABLE")) {
            collidedPlatform.setBroken(true);
            activePlatforms.remove(collidedPlatform);
        }
        // =========================================================================
        // =========================================================================
        // REVISI PERGERAKAN KAMERA SMOOTH & LERPED SCROLLING
        // =========================================================================
        // PERGERAKAN KAMERA KE ATAS YANG SMOOTH (Kembali ke basis HEIGHT = 900)
        if (ninja.getY() < HEIGHT / 2) {
            double targetShift = HEIGHT / 2.0 - ninja.getY();
            double smoothShift = targetShift * 0.12; // Efek smooth ease-out

            ninja.setY(ninja.getY() + smoothShift);
            bgOffsetY += (smoothShift * 0.4); // Background bambu ikut bergulir smooth

            for (Platform p : activePlatforms) {
                p.setY(p.getY() + (int) smoothShift);
            }
        }
        // =========================================================================

        Platform highestPlatform = activePlatforms.getLast();
        while (highestPlatform.getY() > 0) {
            int gap = random.nextInt(currentDifficulty.getMaxGapY() - currentDifficulty.getMinGapY() + 1) + currentDifficulty.getMinGapY();
            int nextY = highestPlatform.getY() - gap;

            // 1. Ambil tipe platform terlebih dahulu dari queue
            if (platformPatternQueue.isEmpty()) {
                replenishQueue();
            }
            String nextType = platformPatternQueue.poll();

            // 2. Tentukan lebar platform secara konsisten berdasarkan tipenya
            int platformWidth;
            if (nextType.equals("MOVING")) {
                platformWidth = 120; // Agak besar untuk platform bergerak
            } else if (nextType.equals("BREAKABLE")) {
                platformWidth = 80;  // Kecil untuk platform yang bisa hilang
            } else {
                platformWidth = 80;  // Kecil untuk platform normal yang diam
            }

            // 3. Hitung koordinat X secara dinamis mengikuti lebar platformWidth baru
            int nextX = random.nextInt(WIDTH - platformWidth);

            // 4. Buat objek platform baru dengan spesifikasi yang sudah dinamis
            Platform newPlatform = new Platform(nextX, nextY, platformWidth, 15, nextType);
            activePlatforms.add(newPlatform);
            highestPlatform = newPlatform;
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

    // ==========================================
    // RENDERING
    // ==========================================
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

        for (Platform p : activePlatforms) {
            p.draw(g2d);
        }

        ninja.draw(g2d);
        drawHUD(g2d);

        if (state == State.PAUSED) {
            drawPauseOverlay(g2d);
        } else if (state == State.GAME_OVER) {
            drawGameOverScreen(g2d);
        }
    }

    private void drawDynamicBackground(Graphics2D g2d) {
        int textureHeight = 2172; // Tinggi asli gambar
        int yOffset = (int) (bgOffsetY % textureHeight);
        int startY = -1200 + yOffset;

        // FASE 1: Awal permainan (Ninja masih di area bawah hingga ketinggian pertama)
        if (bgOffsetY < textureHeight) {
            if (customBgImage != null) {
                g2d.drawImage(customBgImage, 0, startY, WIDTH, textureHeight, this);

                // Di atasnya sudah menyambung Gambar 2
                if (topBgImage != null) {
                    g2d.drawImage(topBgImage, 0, startY - textureHeight, WIDTH, textureHeight, this);
                }
            }
        }
        // FASE 2: Ninja sudah naik lebih tinggi (Transisi dari Gambar 2 ke Gambar 3)
        else if (bgOffsetY >= textureHeight && bgOffsetY < textureHeight * 2) {
            if (topBgImage != null) {
                g2d.drawImage(topBgImage, 0, startY, WIDTH, textureHeight, this);

                // Di atas Gambar 2, sekarang Gambar 3 yang menunggu
                if (skyBgImage != null) {
                    g2d.drawImage(skyBgImage, 0, startY - textureHeight, WIDTH, textureHeight, this);
                } else {
                    g2d.drawImage(topBgImage, 0, startY - textureHeight, WIDTH, textureHeight, this);
                }
            }
        }
        // FASE 3: Tertinggi / Langit Puncak (Gambar 3 meloop secara seamless ke atas tanpa batas)
        else {
            if (skyBgImage != null) {
                g2d.drawImage(skyBgImage, 0, startY, WIDTH, textureHeight, this);
                g2d.drawImage(skyBgImage, 0, startY - textureHeight, WIDTH, textureHeight, this);
                g2d.drawImage(skyBgImage, 0, startY + textureHeight, WIDTH, textureHeight, this);
            }
        }

        // Fallback warna jika semua gambar gagal dimuat
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

        g2d.setColor(new Color(20, 20, 40, 150));
        g2d.fill(new RoundRectangle2D.Double(50, 190, WIDTH - 100, 460, 24, 24));
        g2d.setColor(new Color(255, 215, 0, 120));
        g2d.draw(new RoundRectangle2D.Double(50, 190, WIDTH - 100, 460, 24, 24));

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 34));
        drawCentered(g2d, "NINJA SKY JUMP", 260);

        g2d.setColor(new Color(255, 215, 0));
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 15));
        drawCentered(g2d, "Gunakan A/D atau <- -> untuk bergerak", 295);

        g2d.setColor(new Color(220, 220, 230));
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
        drawCentered(g2d, "Masukkan Nama Pemain", 340);

        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.setFont(new Font("SansSerif", Font.ITALIC, 12));
        drawCentered(g2d, "Tekan ENTER atau klik tombol untuk mulai", 500);

        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fill(new RoundRectangle2D.Double(WIDTH / 2 - 170, 525, 340, 62, 16, 16));
        g2d.setColor(new Color(255, 215, 0, 160));
        g2d.draw(new RoundRectangle2D.Double(WIDTH / 2 - 170, 525, 340, 62, 16, 16));

        g2d.setColor(new Color(255, 215, 0));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
        drawCentered(g2d, "\u2605 HIGH SCORE: " + highScore.score, 552);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 13));
        drawCentered(g2d, "Dipegang oleh: " + highScore.name, 573);

        g2d.setColor(new Color(200, 200, 210));
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        drawCentered(g2d, "Nyawa: " + MAX_LIVES + "x kesempatan  |  Skor & waktu tercatat tiap sesi", 618);
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
            // Hentikan musik jika sebelumnya sudah ada yang berjalan (mencegah suara bertumpuk)
            if (backgroundMusic != null && backgroundMusic.isRunning()) {
                backgroundMusic.stop();
            }

            // Ambil file lagu dari folder resources
            java.net.URL soundURL = getClass().getResource("resources/bgm_game.wav");

            if (soundURL != null) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
                backgroundMusic = AudioSystem.getClip();
                backgroundMusic.open(audioStream);

                // Mengatur agar lagu berputar terus-menerus (looping) tanpa henti
                backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
                backgroundMusic.start();
            } else {
                System.out.println("[WARNING]: File musik bgm_game.wav tidak ditemukan!");
            }
        } catch (Exception e) {
            System.out.println("[ERROR]: Gagal memutar musik latar: " + e.getMessage());
        }
    }

    // Method tambahan untuk menghentikan musik (saat Game Over / Menu)
    private void stopBGM() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }
}