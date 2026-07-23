package game;

import java.awt.Graphics2D;
import java.awt.Image;

public class Item {
    private Platform platform; // Simpan platform pemilik item
    private int offsetX;       // Jarak X dari kiri platform
    private int width = 30;
    private int height = 30;
    private String type;
    private boolean collected = false;

    // CONSTRUCTOR BARU: Mengikat Item langsung ke Platform
    public Item(Platform platform, String type) {
        this.platform = platform;
        this.type = type;
        // Posisikan item tepat di tengah-tengah platform
        this.offsetX = (platform.getWidth() / 2) - (this.width / 2);
    }

    // Koordinat X dan Y sekarang SELALU MENGIKUTI platform secara otomatis!
    public double getX() { return platform.getX() + offsetX; }
    public double getY() { return platform.getY() - height +10; } // Pas di atas permukaan bambu

    public boolean checkCollision(Ninja ninja) {
        if (collected) return false;

        double curX = getX();
        double curY = getY();

        boolean hitX = ninja.getX() + ninja.getWidth() > curX && ninja.getX() < curX + this.width;
        boolean hitY = ninja.getY() + ninja.getHeight() > curY && ninja.getY() < curY + this.height;

        if (hitX && hitY) {
            collected = true;
            if (type.equals("SPRING")) {
                ninja.applySuperJump();
            } else if (type.equals("PARACHUTE")) {
                ninja.activateParachute();
            }
            return true;
        }
        return false;
    }

    public void draw(Graphics2D g2d, Image parachuteImg, Image springImg) {
        if (collected) return;

        int renderX = (int) getX();
        int renderY = (int) getY();

        if (type.equals("PARACHUTE") && parachuteImg != null) {
            g2d.drawImage(parachuteImg, renderX, renderY, width, height, null);
        } else if (type.equals("SPRING") && springImg != null) {
            g2d.drawImage(springImg, renderX, renderY, width, height, null);
        }
    }

    public boolean isCollected() { return collected; }
}