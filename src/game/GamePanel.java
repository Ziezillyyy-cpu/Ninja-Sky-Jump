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

/**
 * Kelas GamePanel.java
 * Merupakan area game loop utama yang menangani rendering, pembaruan logika,
 * input keyboard/mouse pemain, serta pengintegrasian seluruh struktur data.
 *
 * REVISI SDAT - 8 Juli 2026:
 *  - Layar Menu Utama: input nama pemain, tampilan High Score, tombol OK untuk mulai
 *  - HUD dalam game: Nyawa (di atas Skor) dan Waktu bermain
 *  - Tombol Pause ("Back/Stop") saat bermain -> Resume / Kembali ke Menu
 *  - Layar Game Over: tombol Main Lagi dan Kembali ke Menu
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

    // Data pemain & high score
    private String playerName = "Ninja";
    private HighScoreManager.HighScoreData highScore;

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
    // PEMBANGUNAN KOMPONEN UI
    // ==========================================

    private void buildMenuUI() {
        nameField = new RoundedTextField(16);
        nameField.setBounds(WIDTH / 2 - 130, 358, 260, 46);
        nameField.setText(playerName);
        nameField.addActionListener(e -> startGame()); // tekan ENTER langsung mulai
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

    /** Menampilkan/menyembunyikan komponen sesuai state saat ini */
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
        isNewHighScore = score > highScore.score;
        if (isNewHighScore) {
            highScore = new HighScoreManager.HighScoreData(score, playerName);
            HighScoreManager.save(highScore);
        }
        applyVisibilityForState();
        System.out.println("--- GAME OVER! Skor Akhir: " + score + " ---");
    }

    public void restartGame() {
        ninja = new Ninja(180, 400, 70);
        score = 0;
        lives = MAX_LIVES;
        keyLeft = false;
        keyRight = false;
        isNewHighScore = false;

        gameStartMillis = System.currentTimeMillis();
        pausedAccumMillis = 0;

        activePlatforms = new LinkedList<>();

        difficultyTree = new DifficultyTree();
        currentDifficulty = difficultyTree.searchDifficulty(score);

        platformPatternQueue = new java.util.LinkedList<>();
        replenishQueue();

        generateInitialPlatforms();
        System.out.println("--- GAME DIMULAI: Pemain = " + playerName + " ---");
    }

    private void replenishQueue() {
        System.out.println("\n[STRUKTUR 3 - Queue]: Mengisi ulang pola platform (FIFO)...");
        double breakableChance = currentDifficulty.getBreakableProb();

        for (int i = 0; i < 8; i++) {
            String type = "NORMAL";
            if (!currentDifficulty.getLevelName().equals("EASY") && random.nextDouble() < breakableChance) {
                type = "BREAKABLE";
            }
            platformPatternQueue.add(type);
        }
        System.out.println("[STRUKTUR 3 - Queue]: Isi Queue saat ini = " + platformPatternQueue);
    }

    private void generateInitialPlatforms() {
        activePlatforms.clear();

        Platform basePlatform = new Platform(150, 500, 100, 15, "NORMAL");
        activePlatforms.add(basePlatform);

        int currentY = 500;
        while (currentY > 0) {
            int gap = random.nextInt(currentDifficulty.getMaxGapY() - currentDifficulty.getMinGapY() + 1) + currentDifficulty.getMinGapY();
            currentY -= gap;

            int x = random.nextInt(WIDTH - 80);

            if (platformPatternQueue.isEmpty()) {
                replenishQueue();
            }
            String type = platformPatternQueue.poll();

            activePlatforms.add(new Platform(x, currentY, 80, 15, type));
        }
    }

    /** Ninja jatuh, tapi masih ada nyawa tersisa: respawn di tengah layar */
    private void respawnNinja() {
        ninja.setX(WIDTH / 2.0 - ninja.getWidth() / 2.0);
        ninja.setY(HEIGHT / 2.0);
        ninja.bounce();
        System.out.println("[GAMEPLAY]: Nyawa berkurang! Sisa nyawa: " + lives);
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

        int direction = 0;
        if (keyLeft) direction = -1;
        if (keyRight) direction = 1;
        ninja.move(direction);

        ninja.update(WIDTH);

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
            System.out.println("[MEKANIK]: Platform BREAKABLE hancur dan dihapus dari activePlatforms!");
        }

        if (ninja.getY() < HEIGHT / 2) {
            double shiftY = HEIGHT / 2 - ninja.getY();
            ninja.setY(HEIGHT / 2);

            for (Platform p : activePlatforms) {
                p.setY(p.getY() + (int) shiftY);
            }
        }

        Platform highestPlatform = activePlatforms.getLast();
        while (highestPlatform.getY() > 0) {
            int gap = random.nextInt(currentDifficulty.getMaxGapY() - currentDifficulty.getMinGapY() + 1) + currentDifficulty.getMinGapY();
            int nextY = highestPlatform.getY() - gap;
            int nextX = random.nextInt(WIDTH - 80);

            if (platformPatternQueue.isEmpty()) {
                replenishQueue();
            }
            String nextType = platformPatternQueue.poll();

            Platform newPlatform = new Platform(nextX, nextY, 80, 15, nextType);
            activePlatforms.add(newPlatform);
            highestPlatform = newPlatform;
        }

        while (!activePlatforms.isEmpty() && activePlatforms.getFirst().getY() > HEIGHT) {
            Platform removed = activePlatforms.removeFirst();
            System.out.println("[STRUKTUR 2 - LinkedList]: Menghapus platform lama di Y = " + removed.getY() + " (Tenggelam)");
        }

        double ninjaBottom = ninja.getY() + ninja.getHeight();
        for (Platform p : activePlatforms) {
            if (!p.isPassed() && ninjaBottom < p.getY()) {
                p.setPassed(true);
                score++;
                System.out.println("[GAMEPLAY]: Skor Bertambah! Skor saat ini: " + score);

                DifficultyNode newDiff = difficultyTree.searchDifficulty(score);
                if (newDiff != null && newDiff != currentDifficulty) {
                    currentDifficulty = newDiff;
                    System.out.println("[STRUKTUR 4 - Tree]: Kesulitan berubah menjadi -> " + currentDifficulty.getLevelName());
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
        Color topColor;
        Color bottomColor;

        int refScore = (state == State.MENU) ? 0 : score;

        if (refScore < 15) {
            topColor = new Color(135, 206, 250);
            bottomColor = new Color(70, 130, 180);
        } else if (refScore < 35) {
            topColor = new Color(25, 25, 112);
            bottomColor = new Color(186, 85, 211);
        } else {
            topColor = new Color(10, 10, 25);
            bottomColor = new Color(40, 20, 60);
        }

        GradientPaint gp = new GradientPaint(0, 0, topColor, 0, HEIGHT, bottomColor);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        if (refScore >= 15 || state == State.MENU) {
            g2d.setColor(new Color(255, 255, 255, 180));
            for (int i = 0; i < 25; i++) {
                int starX = (i * 31) % WIDTH;
                int starY = (i * 47) % HEIGHT;
                int size = (i % 3 == 0) ? 3 : 2;
                g2d.fillOval(starX, starY, size, size);
            }
        }
    }

    private void drawHUD(Graphics2D g2d) {
        int hudWidth = WIDTH - 90;

        g2d.setColor(new Color(0, 0, 0, 130));
        g2d.fill(new RoundRectangle2D.Double(10, 10, hudWidth, 108, 14, 14));
        g2d.setColor(new Color(255, 255, 255, 80));
        g2d.draw(new RoundRectangle2D.Double(10, 10, hudWidth, 108, 14, 14));

        // Baris 1: Score & Waktu
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2d.drawString("Score: " + score, 22, 34);

        g2d.setColor(new Color(255, 215, 0));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2d.drawString("Waktu: " + formatTime(elapsedMillis()), 150, 34);

        // Baris 2: Nyawa (tampil di atas skor secara berurutan visual) & Difficulty
        drawLives(g2d, 22, 44);

        String level = currentDifficulty.getLevelName();
        if (level.equals("EASY")) {
            g2d.setColor(new Color(50, 205, 50));
        } else if (level.equals("MEDIUM")) {
            g2d.setColor(new Color(255, 165, 0));
        } else {
            g2d.setColor(new Color(220, 20, 60));
        }
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2d.drawString("Difficulty: " + level, hudWidth - 130, 63);

        // Baris 3-4: info struktur data (edukasi SDAT)
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

    /** Menggambar ikon nyawa (hati) - ditampilkan di atas skor sesuai revisi */
    private void drawLives(Graphics2D g2d, int x, int y) {
        g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
        int cursorX = x;
        for (int i = 0; i < MAX_LIVES; i++) {
            if (i < lives) {
                g2d.setColor(new Color(220, 20, 60));
            } else {
                g2d.setColor(new Color(255, 255, 255, 60));
            }
            g2d.drawString("\u2665", cursorX, y + 18);
            cursorX += 24;
        }
    }

    /**
     * Layar menu utama: input nama, tampilan High Score, tombol OK.
     * Komponen interaktif (nameField, okButton) sudah diposisikan via setBounds,
     * di sini hanya digambar teks pendukung di sekitarnya.
     */
    private void drawMenuScreen(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Kartu kaca (glass card) sebagai latar form
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

        // (nameField & okButton digambar otomatis oleh Swing di atas ini)

        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.setFont(new Font("SansSerif", Font.ITALIC, 12));
        drawCentered(g2d, "Tekan ENTER atau klik tombol untuk mulai", 500);

        // Kotak High Score
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

    // ==========================================
    // KEYLISTENER IMPLEMENTATIONS
    // ==========================================

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (state == State.PLAYING) {
            if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) {
                keyLeft = true;
            }
            if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) {
                keyRight = true;
            }
        }

        if (keyCode == KeyEvent.VK_ESCAPE) {
            if (state == State.PLAYING) {
                pauseGame();
            } else if (state == State.PAUSED) {
                resumeGame();
            }
        }

        if (keyCode == KeyEvent.VK_SPACE && state == State.GAME_OVER) {
            startGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) {
            keyLeft = false;
        }
        if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) {
            keyRight = false;
        }
    }
}
