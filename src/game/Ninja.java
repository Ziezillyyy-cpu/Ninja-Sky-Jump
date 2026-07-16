///**


package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class Ninja {
    private double x;
    private double y;
    private double vx;
    private double vy;
    private int width;
    private int height;

    // ==== Sprite Animation (Spritesheet Mode) ====
    private BufferedImage[] runningFrames;
    private BufferedImage jumpingFrame;
    private BufferedImage fallingFrame;

    private int animIndex = 0;
    private int frameCounter = 0;
    private static final int FRAME_DELAY = 6; // Mengatur kecepatan animasi berlari

    private static final double GRAVITY = 0.35;
    private static final double JUMP_STRENGTH = -11.0;
    private static final double HORIZONTAL_SPEED = 7.0;

    public Ninja(double x, double y, int size) {
        this.x = x;
        this.y = y;
        this.width = size;
        this.height = size;
        this.vx = 0;
        this.vy = JUMP_STRENGTH;

        // Load dan potong spritesheet ninja kustom
        loadSpritesheet("resources/ninja_spritesheet.png");
    }

    /**
     * Membaca file spritesheet tunggal dan memotongnya menjadi beberapa frame aksi.
     */
    private void loadSpritesheet(String path) {
        try {
            // Load gambar utama sebagai BufferedImage agar bisa menggunakan getSubimage
            BufferedImage spriteSheet = ImageIO.read(getClass().getResource(path));

            int totalWidth = spriteSheet.getWidth();
            int totalHeight = spriteSheet.getHeight();
            int frameWidth = totalWidth / 4; // Dibagi 4 kolom horizontal sesuai aset gambarmu
            int frameHeight = totalHeight;

            // 1. Potong Frame 0 & 1 untuk animasi berlari/idle
            runningFrames = new BufferedImage[2];
            runningFrames[0] = spriteSheet.getSubimage(0, 0, frameWidth, frameHeight);
            runningFrames[1] = spriteSheet.getSubimage(frameWidth, 0, frameWidth, frameHeight);

            // 2. Potong Frame 2 untuk pose melompat ke atas
            jumpingFrame = spriteSheet.getSubimage(frameWidth * 2, 0, frameWidth, frameHeight);

            // 3. Potong Frame 3 untuk pose jatuh ke bawah
            fallingFrame = spriteSheet.getSubimage(frameWidth * 3, 0, frameWidth, frameHeight);

            // Menghitung proporsi tinggi gambar asli dibandingkan lebarnya
            double ratio = (double) frameHeight / frameWidth;
            // Menyesuaikan tinggi karakter di game agar sesuai dengan rasio asli gambar (lebar tetap sesuai ukuran awal)
            this.height = (int) (this.width * ratio);
            System.out.println("Spritesheet Berhasil dipotong menjadi 4 komponen aksi!");
        } catch (IOException | NullPointerException e) {
            System.out.println("Gagal memuat spritesheet Ninja: " + path + " -> " + e.getMessage());
        }
    }

    public void update(int screenWidth) {
        vy += GRAVITY;
        x += vx;
        y += vy;

        // Logika warp layar kiri-kanan bawaan game
        if (x + width < 0) {
            x = screenWidth;
        } else if (x > screenWidth) {
            x = -width;
        }

        // Jalankan timer untuk animasi index berlari bergantian (Frame 0 & 1)
        if (runningFrames != null) {
            frameCounter++;
            if (frameCounter >= FRAME_DELAY) {
                frameCounter = 0;
                animIndex = (animIndex + 1) % runningFrames.length;
            }
        }
    }

    public void bounce() {
        this.vy = JUMP_STRENGTH;
    }

    public void move(int direction) {
        this.vx = direction * HORIZONTAL_SPEED;
    }

    public void draw(Graphics2D g) {
        BufferedImage img = null;

        // === Logika Penentuan Frame Gambar Berdasarkan Gerakan Fisik ===
        if (vy < 0) {
            // Jika kecepatan Y bernilai negatif, ninja sedang meluncur KE ATAS
            img = jumpingFrame;
        } else if (vy > 1.5) {
            // Jika kecepatan Y positif cukup besar, ninja sedang JATUH
            img = fallingFrame;
        } else {
            // Jika dalam posisi stabil di platform, mainkan animasi berlari
            if (runningFrames != null) {
                img = runningFrames[animIndex];
            }
        }

        // Gambar karakter ke layar (dan membalikkan arah gambar secara horizontal jika menghadap kiri)
        if (img != null) {
            if (vx < 0) {
                // Menghadap ke Kiri (Ukuran lebar dibalik menggunakan nilai minus)
                g.drawImage(img, (int) x + width, (int) y, -width, height, null);
            } else {
                // Menghadap ke Kanan (Normal)
                g.drawImage(img, (int) x, (int) y, width, height, null);
            }
        } else {
            // Fallback jika aset gambar gagal dibaca
            g.setColor(Color.BLACK);
            g.fillOval((int) x, (int) y, width, height);
        }
    }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public void setVy(double vy) { this.vy = vy; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}