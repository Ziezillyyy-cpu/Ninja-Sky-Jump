

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

        // ====================================================================
        // REVISI: Buat hitbox horizontal LEBIH RAMPING daripada tingginya
        // ====================================================================
        this.width = (int) (size * 0.55); // Memotong lebar fisik hingga setengahnya (Ramping)
        this.height = size;               // Tingginya tetap sesuai skala visual game
        // ====================================================================

        this.vx = 0;
        this.vy = JUMP_STRENGTH;

        loadFramesFromFolder("src/game/resources");
    }
    /**
     * Memotong sprite sheet menjadi potongan frame animasi individual
     */
    private void loadFramesFromFolder(String folderPath) {
        try {
            // Membaca file lembaran sprite sheet langsung
            File sheetFile = new File(folderPath + "/ninja_spritesheet.png");
            java.awt.image.BufferedImage spriteSheet = javax.imageio.ImageIO.read(sheetFile);

            // TENTUKAN UKURAN DAN JUMLAH FRAME
            int totalFrames = 4;
            int frameWidth = spriteSheet.getWidth() / totalFrames;
            int frameHeight = spriteSheet.getHeight();

            List<Image> loadedFrames = new ArrayList<>();

            // Potong gambar menggunakan subimage
            for (int i = 0; i < totalFrames; i++) {
                Image frame = spriteSheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
                loadedFrames.add(frame);
            }

            frames = loadedFrames.toArray(new Image[0]);
            System.out.println("Berhasil memotong Sprite Sheet! Total frame: " + frames.length);

            // ====================================================================
            // JURUS PAMUNGKAS: PAKSA UKURAN HITBOX RAMPIING DI SINI
            // ====================================================================
            // Kita bypass nilai 'size' bawaan dan paksa kotak merahnya mengecil
            // pas dengan badan Ninja, mengabaikan padding transparan bawaan gambar.
            this.width = 50;   // Mengecilkan lebar hitbox secara agresif menjadi 50px
            this.height = 75;  // Menyesuaikan tinggi fisik menjadi 75px
            // ====================================================================

        } catch (IOException e) {
            System.out.println("[ERROR] Gagal memuat ninja_spritesheet.png: " + e.getMessage());
            frames = new Image[0];
        }
    }

    public void update(int screenWidth) {
        vy += GRAVITY;
        x += vx;
        y += vy;

        // Batasan layar kanan-kiri
        if (x + width < 0) {
            x = screenWidth;
        } else if (x > screenWidth) {
            x = -width;
        }

        // =======================================================
        // KONTROL ANIMASI BERDASARKAN KONDISI FISIK (STATE) NINJA
        // =======================================================
        if (frames.length > 0) {

            // KONDISI 1: Ninja sedang melompat naik (vy bernilai negatif tajam)
            if (vy < -2.0) {
                // Gunakan frame melompat (misal frame indeks ke-2)
                // Sesuaikan angka indeks ini dengan letak frame melompat di spritesheet kamu
                currentFrame = Math.min(2, frames.length - 1);
            }

            // KONDISI 2: Ninja sedang jatuh bebas ke bawah (vy bernilai positif)
            else if (vy > 1.0) {
                // Gunakan frame jatuh/melayang turun (misal frame indeks ke-3)
                currentFrame = Math.min(3, frames.length - 1);
            }

            // KONDISI 3: Ninja sedang bergerak normal / di sekitar platform
            else {
                // Hanya di kondisi ini kita putar animasi looping-nya (misal bergantian frame 0 dan 1)
                frameCounter++;
                if (frameCounter >= FRAME_DELAY) {
                    frameCounter = 0;

                    // Melakukan loop hanya pada frame 0 dan 1
                    // Jika animasi berjalan kamu ada di indeks lain, sesuaikan batasnya di sini
                    if (frames.length > 2) {
                        currentFrame = (currentFrame + 1) % 2;
                    } else {
                        currentFrame = (currentFrame + 1) % frames.length;
                    }
                }
            }
        }
        // =======================================================
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

            // ==========================================
            // KONFIGURASI PENYESUAIAN UKURAN DAN PIJAKAN
            // ==========================================
            // Tambahkan ekstra ukuran jika dirasa gambar sprite terlalu kecil di dalam kotak hitbox
            int extraSize = 25;
            int drawWidth = width + extraSize;
            int drawHeight = height + extraSize;

            // Geser posisi gambar ke bawah/ke samping agar kaki pas di pijakan platform
            // Jika kaki ninja melayang di atas platform, naikkan angka YOffset (misal: 5, 10, dst)
            // Jika kaki ninja melesek ke dalam platform, buat angkanya minus (misal: -5, -10)
            int xOffset = -extraSize / 2;
            int yOffset = -30; // Jalankan trial-error dengan angka ini sampai pas di kaki

            int drawX = (int) x + xOffset;
            int drawY = (int) y + yOffset;
            // ==========================================

            if (vx < 0) {
                // Menghadap kiri
                g.drawImage(img, drawX + drawWidth, drawY, -drawWidth, drawHeight, null);
            } else {
                // Menghadap kanan
                g.drawImage(img, drawX, drawY, drawWidth, drawHeight, null);
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