package game;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame();

            frame.setTitle("Ninja Sky Jump");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            Menu menu = new Menu(frame);

            frame.add(menu);

            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

        });
    }
}