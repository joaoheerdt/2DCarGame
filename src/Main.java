import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {
    public Main() {
        setTitle("2D Car Game");
        setResizable(true);
        setLayout(new BorderLayout());

        GamePanel gamePanel = new GamePanel();
        add(gamePanel, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1280, 720));
        pack();

        setLocationRelativeTo(null); // Centraliza na tela
        // setExtendedState(JFrame.MAXIMIZED_BOTH);

        setVisible(true); //[cite: 5]
    }
    public static void main(String[] args) {
        new Main();
    }
}