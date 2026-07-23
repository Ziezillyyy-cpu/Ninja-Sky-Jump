package game;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.ImageIcon;

/**
 * STRUKTUR DATA 1 (ADT): Kelas Platform.java
 */
public class Platform {
    private int x;
    private int y;
    private int width;
    private int height;
    private String type; // "NORMAL", "BREAKABLE", atau "MOVING"
    private boolean isPassed;
    private boolean isBroken;

    // =========================================================================
    // BARIS BARU: Variabel untuk pergerakan platform bergerak
    // =========================================================================
    private int vx = 2;              // Kecepatan gerak ke samping
    private final int SCREEN_WIDTH = 600; // Ubah angka 400 ini sesuai dengan lebar layar game kamu!

    private static Image imgNormal;
    private static Image imgBreakable;

    static {
        try {
            java.net.URL normalURL = Platform.class.getResource("/game/resources/platform_normal.png");
            java.net.URL breakableURL = Platform.class.getResource("/game/resources/platform_breakable.png");

            if (normalURL != null) imgNormal = new ImageIcon(normalURL).getImage();
            if (breakableURL != null) imgBreakable = new ImageIcon(breakableURL).getImage();
        } catch (Exception e) {
            System.out.println("[WARNING]: Gagal me-load gambar platform: " + e.getMessage());
        }
    }

    public Platform(int x, int y, int width, int height, String type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.isPassed = false;
        this.isBroken = false;

        // Jika tipenya bergerak, tentukan arah awal secara acak (kanan atau kiri)
        if ("MOVING".equals(type)) {
            this.vx = Math.random() > 0.5 ? 2 : -2;
        }
    }

    // =========================================================================
    // METHOD BARU: Untuk mengupdate posisi platform bergerak (Kanan-Kiri)
    // =========================================================================
    public void update() {
        if ("MOVING".equals(type)) {
            x += vx; // Geser posisi x sebesar kecepatan vx

            // Memantul jika menabrak batas kiri layar
            if (x <= 0) {
                x = 0;
                vx = -vx; // Balik arah ke kanan
            }
            // Memantul jika menabrak batas kanan layar
            else if (x + width >= SCREEN_WIDTH) {
                x = SCREEN_WIDTH - width;
                vx = -vx; // Balik arah ke kiri
            }
        }
    }

    public boolean checkCollision(Ninja ninja) {
        if (isBroken) return false;

        // Hanya cek jika Ninja sedang jatuh ke bawah (vy > 0)
        if (ninja.getVy() > 0) {

            // =========================================================================
            // PERBAIKAN: POTONG AREA TRANSPARAN KANAN NINJA & KIRI PLATFORM
            // =========================================================================

            // 1. Besarkan padding kanan Ninja agar ujung kanan fisiknya masuk lebih ke dalam ke badannya
            int ninjaPaddingKiri = 20;
            int ninjaPaddingKanan = 45; // <-- DITAMBAH (pangkas area transparan sebelah kanan Ninja)

            double ninjaLeftEdge = ninja.getX() + ninjaPaddingKiri;
            double ninjaRightEdge = ninja.getX() + ninja.getWidth() - ninjaPaddingKanan;

            // 2. Beri margin di sisi kiri platform agar Ninja harus benar-benar di atas bambu
            int platformMarginKiri = 10;  // <-- DITAMBAH (geser batas deteksi platform ke kanan)
            int platformMarginKanan = 5;

            double platformLeftEdge = this.x + platformMarginKiri;
            double platformRightEdge = (this.x + this.width) - platformMarginKanan;

            // Cek overlap horizontal
            boolean horizontalOverlap = (ninjaRightEdge > platformLeftEdge) && (ninjaLeftEdge < platformRightEdge);

            // Jika badan Ninja tidak tepat di atas bambu, BATALKAN pementalan!
            if (!horizontalOverlap) {
                return false;
            }

            // =========================================================================
            // DETEKSI VERTIKAL (TINGGI PIJAKAN)
            // =========================================================================
            int offsetBawahNinja = 35;
            double ninjaBottom = ninja.getY() + ninja.getHeight() - offsetBawahNinja;
            double ninjaPrevBottom = ninjaBottom - ninja.getVy();

            int platformSurfaceY = this.y;

            boolean verticalOverlap = (ninjaPrevBottom <= platformSurfaceY + 15) && (ninjaBottom >= platformSurfaceY - 5);

            if (verticalOverlap) {
                return true;
            }
        }
        return false;
    }

    /**
     * Menggambar platform dengan gambar batuan berlumut yang DIBESARKAN SECARA EKSTREM
     * untuk memangkas ruang transparan bawaan gambar.
     */
    public void draw(Graphics2D g) {
        if (isBroken) return;


        if (type.equals("NORMAL")) {
            if (imgNormal != null) {
                g.drawImage(imgNormal, x, y - 35, width, height + 80, null);
            } else {
                GradientPaint gp = new GradientPaint(x, y, new Color(138, 43, 226), x + width, y, new Color(75, 0, 130));
                g.setPaint(gp);
                g.fillRoundRect(x, y, width, height, 10, 10);
                g.setColor(new Color(255, 255, 255, 100));
                g.drawRoundRect(x, y, width, height, 10, 10);
            }

        } else if (type.equals("BREAKABLE")) {
            if (imgBreakable != null) {
                g.drawImage(imgBreakable, x, y - 35, width, height + 65, null);
            } else {
                GradientPaint gp = new GradientPaint(x, y, new Color(210, 105, 30), x + width, y, new Color(139, 69, 19));
                g.setPaint(gp);
                g.fillRoundRect(x, y, width, height, 10, 10);
                g.setColor(new Color(255, 69, 0, 150));
                g.drawRoundRect(x, y, width, height, 10, 10);
            }
        }
        // =====================================================================
        // LOGIKA BARU: Menggambar Platform Bergerak (MOVING)
        // =====================================================================
        else if (type.equals("MOVING")) {
            if (imgNormal != null) {
                // Menggunakan aset batu berlumut yang sama seperti tipe NORMAL
                g.drawImage(imgNormal, x, y - 35, width, height + 80, null);
            } else {
                // Cadangan warna Cyan ke Biru jika gambar bermasalah
                GradientPaint gp = new GradientPaint(x, y, new Color(0, 191, 255), x + width, y, new Color(0, 0, 139));
                g.setPaint(gp);
                g.fillRoundRect(x, y, width, height, 10, 10);
                g.setColor(new Color(255, 255, 255, 120));
                g.drawRoundRect(x, y, width, height, 10, 10);
            }
        }
    }

    // Getters & Setters
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getType() { return type; }
    public boolean isPassed() { return isPassed; }
    public void setPassed(boolean passed) { isPassed = passed; }
    public boolean isBroken() { return isBroken; }
    public void setBroken(boolean broken) { isBroken = broken; }
}