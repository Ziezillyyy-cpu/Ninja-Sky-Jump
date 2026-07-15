//package game;
//
//import java.awt.Color;
//import java.awt.Graphics2D;
//
///**
// * STRUKTUR DATA 1 (ADT): Kelas Ninja.java
// * Merepresentasikan entitas Ninja sebagai karakter utama (Player) dalam game.
// * Menyimpan data posisi, kecepatan, ukuran, dan logika fisika pergerakan.
// */
//public class Ninja {
//    private double x;
//    private double y;
//    private double vx;
//    private double vy;
//    private int width;
//    private int height;
//
//    // Konstanta Fisika
//    private static final double GRAVITY = 0.35;
//    private static final double JUMP_STRENGTH = -11.0;
//    private static final double HORIZONTAL_SPEED = 7.0;
//
//    public Ninja(double x, double y, int size) {
//        this.x = x;
//        this.y = y;
//        this.width = size;
//        this.height = size;
//        this.vx = 0;
//        this.vy = JUMP_STRENGTH; // Mulai dengan melompat ke atas
//    }
//
//    /**
//     * Memperbarui posisi Ninja berdasarkan fisika (Gravitasi & Kecepatan)
//     */
//    public void update(int screenWidth) {
//        // Terapkan gravitasi ke kecepatan vertikal (jatuh bebas)
//        vy += GRAVITY;
//
//        // Perbarui koordinat posisi
//        x += vx;
//        y += vy;
//
//        // Implementasi Mekanik Screen Wrapping:
//        // Jika ninja keluar dari batas kanan, muncul di kiri, dan sebaliknya.
//        if (x + width < 0) {
//            x = screenWidth;
//        } else if (x > screenWidth) {
//            x = -width;
//        }
//    }
//
//    /**
//     * Membuat Ninja memantul ke atas
//     */
//    public void bounce() {
//        this.vy = JUMP_STRENGTH;
//    }
//
//    /**
//     * Mengatur arah gerakan horizontal Ninja berdasarkan input
//     * @param direction -1 untuk kiri, 1 untuk kanan, 0 untuk diam
//     */
//    public void move(int direction) {
//        this.vx = direction * HORIZONTAL_SPEED;
//    }
//
//    /**
//     * Menggambar Ninja dengan estetika premium (bukan sekadar lingkaran polos).
//     * Ninja digambar dengan jubah hitam, ikat kepala merah berkibar, dan topeng dengan mata bersinar.
//     */
//    public void draw(Graphics2D g) {
//        // 1. Gambar ikat kepala merah yang melambai di belakang ninja
//        g.setColor(new Color(220, 20, 60)); // Crimson Red
//        g.fillOval((int) x - 5, (int) y + (height / 3), 12, 12);
//        g.fillOval((int) x - 10, (int) y + (height / 2), 10, 8);
//
//        // 2. Gambar tubuh bulat Ninja (Hitam Pekat/Abu Gelap)
//        g.setColor(new Color(30, 30, 30));
//        g.fillOval((int) x, (int) y, width, height);
//
//        // 3. Gambar strip topeng ninja (Putih/Warna Kulit untuk area mata)
//        g.setColor(new Color(245, 222, 179)); // Wheat color (skin)
//        g.fillRoundRect((int) x + (width / 8), (int) y + (height / 4), (int) (width * 0.75), height / 4, 6, 6);
//
//        // 4. Gambar mata (Mata ninja yang fokus dan tajam)
//        g.setColor(Color.BLACK);
//        // Mata Kiri
//        g.fillOval((int) x + (width / 4), (int) y + (height / 4) + 3, 4, 4);
//        // Mata Kanan
//        g.fillOval((int) x + (int) (width * 0.55), (int) y + (height / 4) + 3, 4, 4);
//
//        // 5. Gambar pita ikat kepala merah melintang di atas mata
//        g.setColor(new Color(220, 20, 60));
//        g.fillRect((int) x + 2, (int) y + (height / 6), width - 4, 4);
//    }
//
//    // Getters & Setters
//    public double getX() { return x; }
//    public void setX(double x) { this.x = x; }
//    public double getY() { return y; }
//    public void setY(double y) { this.y = y; }
//    public double getVx() { return vx; }
//    public double getVy() { return vy; }
//    public void setVy(double vy) { this.vy = vy; }
//    public int getWidth() { return width; }
//    public int getHeight() { return height; }
//}


//package game;
//
//import java.awt.Color;
//import java.awt.Graphics2D;
//import java.awt.Image;
//import javax.imageio.ImageIO;
//import java.io.IOException;
//
//public class Ninja {
//    private double x;
//    private double y;
//    private double vx;
//    private double vy;
//    private int width;
//    private int height;
//
//    // ==== Sprite Animation ====
//    private Image[] frames = new Image[9];
//    private int currentFrame = 0;
//    private int frameCounter = 0;
//    private static final int FRAME_DELAY = 5; // ganti frame tiap 5 tick (atur kecepatan animasi)
//
//    private static final double GRAVITY = 0.35;
//    private static final double JUMP_STRENGTH = -11.0;
//    private static final double HORIZONTAL_SPEED = 7.0;
//
//    public Ninja(double x, double y, int size) {
//        this.x = x;
//        this.y = y;
//        this.width = size;
//        this.height = size;
//        this.vx = 0;
//        this.vy = JUMP_STRENGTH;
//
//        loadFrames();
//    }
//
//    private void loadFrames() {
//        for (int i = 0; i < 9; i++) {
//            String num = String.format("%03d", i + 1); // 001, 002, ..., 009
//            String path = "/game/resources/Jump__" + num + ".png";
//            try {
//                frames[i] = ImageIO.read(getClass().getResource(path));
//            } catch (IOException | NullPointerException e) {
//                System.out.println("Gagal load frame: " + path + " -> " + e.getMessage());
//                frames[i] = null;
//            }
//        }
//    }
//
//    public void update(int screenWidth) {
//        vy += GRAVITY;
//        x += vx;
//        y += vy;
//
//        if (x + width < 0) {
//            x = screenWidth;
//        } else if (x > screenWidth) {
//            x = -width;
//        }
//
//        // Update animasi frame
//        frameCounter++;
//        if (frameCounter >= FRAME_DELAY) {
//            frameCounter = 0;
//            currentFrame = (currentFrame + 1) % frames.length;
//        }
//    }
//
//    public void bounce() {
//        this.vy = JUMP_STRENGTH;
//    }
//
//    public void move(int direction) {
//        this.vx = direction * HORIZONTAL_SPEED;
//    }
//
//    public void draw(Graphics2D g) {
//        Image img = frames[currentFrame];
//        if (img != null) {
//            if (vx < 0) {
//                // hadap kiri -> flip gambar horizontal
//                g.drawImage(img, (int) x + width, (int) y, -width, height, null);
//            } else {
//                g.drawImage(img, (int) x, (int) y, width, height, null);
//            }
//        } else {
//            // fallback shape kalau gambar gagal load
//            g.setColor(Color.BLACK);
//            g.fillOval((int) x, (int) y, width, height);
//        }
//    }
//
//    // Getters & Setters
//    public double getX() { return x; }
//    public void setX(double x) { this.x = x; }
//    public double getY() { return y; }
//    public void setY(double y) { this.y = y; }
//    public double getVx() { return vx; }
//    public double getVy() { return vy; }
//    public void setVy(double vy) { this.vy = vy; }
//    public int getWidth() { return width; }
//    public int getHeight() { return height; }
//}


package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Ninja {
    private double x;
    private double y;
    private double vx;
    private double vy;
    private int width;
    private int height;

    // ==== Sprite Animation ====
    private Image[] frames;
    private int currentFrame = 0;
    private int frameCounter = 0;
    private static final int FRAME_DELAY = 5;

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

        loadFramesFromFolder("src/game/resources");
    }

    /**
     * Load semua file PNG di dalam folder, urutkan nama, tanpa peduli nama filenya apa.
     */
    private void loadFramesFromFolder(String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));

        List<Image> loadedFrames = new ArrayList<>();

        if (files != null && files.length > 0) {
            Arrays.sort(files); // urutkan berdasarkan nama file (alfabetis)

            for (File file : files) {
                try {
                    Image img = ImageIO.read(file);
                    loadedFrames.add(img);
                    System.out.println("Berhasil load: " + file.getName());
                } catch (IOException e) {
                    System.out.println("Gagal load: " + file.getName() + " -> " + e.getMessage());
                }
            }
        } else {
            System.out.println("Tidak ada file PNG ditemukan di folder: " + folderPath);
        }

        frames = loadedFrames.toArray(new Image[0]);
    }

    public void update(int screenWidth) {
        vy += GRAVITY;
        x += vx;
        y += vy;

        if (x + width < 0) {
            x = screenWidth;
        } else if (x > screenWidth) {
            x = -width;
        }

        if (frames.length > 0) {
            frameCounter++;
            if (frameCounter >= FRAME_DELAY) {
                frameCounter = 0;
                currentFrame = (currentFrame + 1) % frames.length;
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
        if (frames.length > 0) {
            Image img = frames[currentFrame];
            if (vx < 0) {
                g.drawImage(img, (int) x + width, (int) y, -width, height, null);
            } else {
                g.drawImage(img, (int) x, (int) y, width, height, null);
            }
        } else {
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