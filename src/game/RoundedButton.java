package game;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * Kelas RoundedButton.java
 * Tombol custom bergaya "premium" (rounded corner + efek hover/pressed)
 * agar tampilan menu, tombol OK, Pause, Restart, dan Back to Menu
 * konsisten dengan tema visual game (Glassmorphism ungu-indigo-emas).
 */
public class RoundedButton extends JButton {
    private final Color baseColor;
    private final Color hoverColor;

    public RoundedButton(String text, Color baseColor, Color hoverColor, Color textColor) {
        super(text);
        this.baseColor = baseColor;
        this.hoverColor = hoverColor;

        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setForeground(textColor);
        setFont(new Font("SansSerif", Font.BOLD, 16));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fill = (getModel().isRollover() || getModel().isPressed()) ? hoverColor : baseColor;
        if (getModel().isPressed()) {
            fill = fill.darker();
        }

        int arc = Math.min(20, getHeight());
        g2.setColor(fill);
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, arc, arc));

        g2.setColor(new Color(255, 255, 255, 90));
        g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, arc, arc));

        g2.dispose();
        super.paintComponent(g);
    }
}
