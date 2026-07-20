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

        loadFramesFromFolder("src/game/resources/charcters");
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