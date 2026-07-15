package game;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;

/**
 * STRUKTUR DATA 1 (ADT): Kelas Platform.java
 * Merepresentasikan pijakan (platform) yang dapat diinjak oleh Ninja.
 * Menyimpan tipe platform, posisi, dimensi, status dilewati, dan status rusak.
 */
public class Platform {
    private int x;
    private int y;
    private int width;
    private int height;
    private String type; // "NORMAL" atau "BREAKABLE"
    private boolean isPassed; // Penanda apakah platform ini sudah dilewati oleh Ninja untuk hitung skor
    private boolean isBroken; // Penanda apakah platform ini sudah hancur (khusus BREAKABLE)

    public Platform(int x, int y, int width, int height, String type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.isPassed = false;
        this.isBroken = false;
    }

    /**
     * Memeriksa apakah Ninja melakukan tumbukan dari atas ke platform.
     * Tumbukan valid jika Ninja sedang bergerak turun (vy > 0), 
     * posisi kaki Ninja berada di area atas platform, dan secara horizontal sejajar.
     */
    public boolean checkCollision(Ninja ninja) {
        if (isBroken) {
            return false; // Platform rusak tidak bisa diinjak lagi
        }

        // Ninja harus bergerak turun (vy > 0)
        if (ninja.getVy() > 0) {
            double ninjaBottom = ninja.getY() + ninja.getHeight();
            double ninjaPrevBottom = ninjaBottom - ninja.getVy();
            
            // Cek keselarasan horizontal (overlap koordinat X)
            if (ninja.getX() + ninja.getWidth() - 4 > this.x && ninja.getX() + 4 < this.x + this.width) {
                // Cek apakah di frame ini atau frame sebelumnya melewati permukaan platform (Y)
                if (ninjaPrevBottom <= this.y + 4 && ninjaBottom >= this.y) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Menggambar platform dengan efek estetika premium (Glassmorphism & Gradient)
     */
    public void draw(Graphics2D g) {
        if (isBroken) return;

        if (type.equals("NORMAL")) {
            // Gradient Ungu ke Biru Indigo
            GradientPaint gp = new GradientPaint(x, y, new Color(138, 43, 226), x + width, y, new Color(75, 0, 130));
            g.setPaint(gp);
            g.fillRoundRect(x, y, width, height, 10, 10);

            // Garis tepi halus
            g.setColor(new Color(255, 255, 255, 100));
            g.drawRoundRect(x, y, width, height, 10, 10);
            
            // Aksen cahaya di bagian atas
            g.setColor(new Color(255, 255, 255, 80));
            g.fillRect(x + 5, y + 2, width - 10, 3);
            
        } else if (type.equals("BREAKABLE")) {
            // Platform Rapuh/Retak (Warna Orange kecoklatan dengan tekstur retakan)
            GradientPaint gp = new GradientPaint(x, y, new Color(210, 105, 30), x + width, y, new Color(139, 69, 19));
            g.setPaint(gp);
            g.fillRoundRect(x, y, width, height, 10, 10);

            // Garis tepi halus merah tua
            g.setColor(new Color(255, 69, 0, 150));
            g.drawRoundRect(x, y, width, height, 10, 10);

            // Menggambar garis-garis retakan (Cracks)
            g.setColor(new Color(255, 255, 255, 180));
            // Retakan 1
            g.drawLine(x + width / 4, y, x + width / 3, y + height);
            // Retakan 2
            g.drawLine(x + width / 2, y, x + width / 2 - 5, y + height);
            // Retakan 3
            g.drawLine(x + (3 * width) / 4, y, x + (2 * width) / 3, y + height);
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
