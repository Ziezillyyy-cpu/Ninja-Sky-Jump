package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Ninja {
    private double x;
    private double y;
    private double vx;
    private double vy;
    private int width;
    private int height;

    // ==== Sprite Animation ====
    private Image[] frames = new Image[0];
    private int currentFrame = 0;
    private int frameCounter = 0;
    private static final int FRAME_DELAY = 5;
    private String currentSkinName = "ninja_1.png";

    private static final double GRAVITY = 0.35;
    private static final double JUMP_STRENGTH = -11.0;
    private static final double HORIZONTAL_SPEED = 7.0;

    // ==== Status Buff Efek ====
    private boolean hasParachute = false;
    private int parachuteTimer = 0;

    public Ninja(double x, double y, int size) {
        this.x = x;
        this.y = y;

        // Hitbox fisik Ramping
        this.width = 55;
        this.height = 85;

        this.vx = 0;
        this.vy = JUMP_STRENGTH;

        // Load skin spritesheet default saat pertama kali dibuat
        setSkin("ninja_1.png");
    }

    /**
     * Memotong Spritesheet menjadi potongan-potongan frame animasi secara dinamis
     */
    public void setSkin(String spritesheetName) {
        this.currentSkinName = spritesheetName;
        try {
            InputStream is = getClass().getResourceAsStream("resources/" + spritesheetName);
            if (is == null) {
                is = getClass().getResourceAsStream("/game/resources/" + spritesheetName);
            }

            if (is != null) {
                BufferedImage spriteSheet = ImageIO.read(is);
                int totalFrames = 4; // Total 4 frame potongan animasi
                int frameWidth = spriteSheet.getWidth() / totalFrames;
                int frameHeight = spriteSheet.getHeight();

                List<Image> loadedFrames = new ArrayList<>();
                for (int i = 0; i < totalFrames; i++) {
                    Image frame = spriteSheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
                    loadedFrames.add(frame);
                }

                this.frames = loadedFrames.toArray(new Image[0]);
                this.currentFrame = 0; // Reset frame awal
            } else {
                System.out.println("[ERROR] File Spritesheet tidak ditemukan: " + spritesheetName);
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Gagal memuat spritesheet " + spritesheetName + ": " + e.getMessage());
        }
    }

    public void update(int screenWidth) {
        vy += GRAVITY;
        x += vx;
        y += vy;

        // Logika Efek Parasut
        if (hasParachute) {
            if (vy > 0) {
                vy = 1.5; // Melayang pelan saat jatuh
            }
            parachuteTimer--;
            if (parachuteTimer <= 0) {
                hasParachute = false;
            }
        }

        // Pembatas Layar (Screen Wrap)
        if (x + width < 0) {
            x = screenWidth;
        } else if (x > screenWidth) {
            x = -width;
        }

        // Kontrol Animasi Berdasarkan State
        if (frames.length > 0) {
            if (vy < -2.0) {
                currentFrame = Math.min(2, frames.length - 1);
            } else if (vy > 1.0) {
                currentFrame = Math.min(3, frames.length - 1);
            } else {
                frameCounter++;
                if (frameCounter >= FRAME_DELAY) {
                    frameCounter = 0;
                    if (frames.length > 2) {
                        currentFrame = (currentFrame + 1) % 2;
                    } else {
                        currentFrame = (currentFrame + 1) % frames.length;
                    }
                }
            }
        }
    }

    public void bounce() {
        this.vy = JUMP_STRENGTH;
    }

    public void applySuperJump() {
        this.vy = -22.0;
    }

    public void activateParachute() {
        this.hasParachute = true;
        this.parachuteTimer = 200;
    }

    public boolean hasParachute() {
        return hasParachute;
    }

    public void move(int direction) {
        this.vx = direction * HORIZONTAL_SPEED;
    }

    public void draw(Graphics2D g) {
        if (frames.length > 0 && currentFrame < frames.length) {
            Image img = frames[currentFrame];

            int extraWidth = 20;
            int extraHeight = 55;
            int drawWidth = width + extraWidth;
            int drawHeight = height + extraHeight;

            int drawX = (int) x - (extraWidth / 2);
            int drawY = (int) y - 35;

            if (vx < 0) {
                g.drawImage(img, drawX + drawWidth, drawY, -drawWidth, drawHeight, null);
            } else {
                g.drawImage(img, drawX, drawY, drawWidth, drawHeight, null);
            }
        } else {
            g.setColor(Color.RED);
            g.fillRect((int) x, (int) y, width, height);
        }
    }

    // ==== Getter & Setter ====
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public void setVy(double vy) { this.vy = vy; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getCurrentSkinName() { return currentSkinName; }
}