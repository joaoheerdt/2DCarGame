import javax.swing.*;

public class Main extends JFrame {
    public Main() {
        setTitle("2D Car Game");
        setResizable(true);

        GamePanel gamePanel = new GamePanel();
        add(gamePanel);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    public static void main(String[] args) {
        new Main();
    }
}