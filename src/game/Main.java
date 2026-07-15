
package game;

import javax.swing.JFrame;

/**
 * Kelas Main.java
 * Entry point utama program untuk menjalankan game Ninja Sky Jump.
 * Menginisialisasi kontainer utama GUI menggunakan JFrame.
 */
public class Main {
    public static void main(String[] args) {
        // Jalankan GUI Swing pada event-dispatching thread untuk memastikan thread-safety
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame();
                frame.setTitle("Ninja Sky Jump - 2D Endless Jumper");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                
                // Menambahkan panel utama game
                GamePanel gamePanel = new GamePanel();
                frame.add(gamePanel);
                
                frame.setResizable(false);
                frame.pack(); // Mengatur ukuran frame sesuai dengan preferred size panel
                frame.setLocationRelativeTo(null); // Menaruh window tepat di tengah layar desktop
                frame.setVisible(true);
            }
        });
    }
}
