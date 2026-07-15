package game;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * Kelas RoundedTextField.java
 * Input field custom (rounded, latar gelap transparan, aksen emas)
 * digunakan untuk kolom "Masukkan Nama" pada layar menu utama.
 */
public class RoundedTextField extends JTextField {

    public RoundedTextField(int columns) {
        super(columns);
        setOpaque(false);
        setForeground(Color.WHITE);
        setCaretColor(Color.WHITE);
        setSelectionColor(new Color(255, 215, 0, 120));
        setFont(new Font("SansSerif", Font.PLAIN, 18));
        setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(20, 20, 45, 210));
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));

        g2.setColor(new Color(255, 215, 0, 170));
        g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, 16, 16));

        g2.dispose();
        super.paintComponent(g);
    }
}
