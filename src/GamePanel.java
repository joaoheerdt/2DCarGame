import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import vehicle.*;
import map.GameMap;

public class GamePanel extends JPanel implements ActionListener {
    private Vehicle activeVehicle;
    private GameMap activeMap;
    private boolean isAccelerating = false, isBraking = false;
    private Timer loop;

    private enum GameStage {MENU, PLAYING, PAUSED}

    private GameStage currentState = GameStage.MENU;

    // Imagens
    private Image menuBackground = new ImageIcon("src/assets/menu/menu_background.png").getImage();
    private Image[] buttonImages = {
            new ImageIcon("src/assets/menu/play_button.png").getImage(),
            new ImageIcon("src/assets/menu/config_button.png").getImage(),
            new ImageIcon("src/assets/menu/garage_button.png").getImage(),
            new ImageIcon("src/assets/menu/rank_button.png").getImage(),
            new ImageIcon("src/assets/menu/exit_button.png").getImage()
    };

    // Ajustado para 200x40 (Proporção 5:1) e movido para o céu (X=300, Y começando em 180)
    private Rectangle[] botoes = {
            new Rectangle(250, 220, 300, 60), // JOGAR
            new Rectangle(273, 280, 250, 50), // CONFIGURAÇÕES
            new Rectangle(300, 400, 200, 40), // GARAGEM
            new Rectangle(300, 330, 200, 40), // RANKING
            new Rectangle(300, 380, 200, 40)  // SAIR
    };

    private int botaoPressionado = -1;

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        this.activeVehicle = new Fusca();
        this.activeMap = new GameMap("src/assets/map/default_map.png");

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (currentState == GameStage.MENU) {
                    for (int i = 0; i < botoes.length; i++) {
                        if (botoes[i].contains(e.getPoint())) {
                            botaoPressionado = i;
                            repaint();
                        }
                    }
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (botaoPressionado != -1 && botoes[botaoPressionado].contains(e.getPoint())) {
                    executarAcao(botaoPressionado);
                }
                botaoPressionado = -1;
                repaint();
            }
        });

        // Controles (apenas se estiver jogando)
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (currentState == GameStage.PLAYING) {
                    if (e.getKeyCode() == KeyEvent.VK_D) isAccelerating = true;
                    if (e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_S) isBraking = true;
                    if (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_4)
                        activeVehicle.changeGear(e.getKeyCode() - KeyEvent.VK_0);
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) activeVehicle.toggleEngine();
                }
            }

            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_D) isAccelerating = false;
                if (e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_S) isBraking = false;
            }
        });

        loop = new Timer(16, this);
        loop.start();
    }

    private void executarAcao(int index) {
        switch (index) {
            case 0:
                currentState = GameStage.PLAYING;
                break; // JOGAR
            case 4:
                System.exit(0);
                break; // SAIR
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (currentState == GameStage.PLAYING) {
            activeVehicle.updatePhysics(isAccelerating, isBraking);
            activeMap.update(activeVehicle.getCurrentSpeed());
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (currentState == GameStage.PLAYING) {
            // --- MODO JOGO ---
            activeMap.draw(g2d, this);
            activeVehicle.draw(g2d, this);

            // Desenha o HUD
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 18));

            int currentGear = activeVehicle.getCurrentGear();
            double currentRpm = activeVehicle.getCurrentRpm();
            double visualSpeed = activeVehicle.getCurrentSpeed() * 1.65;

            g2d.drawString("Gear: " + (currentGear == 0 ? "N" : currentGear), 20, 30);
            g2d.drawString(String.format("RPM: %.0f", currentRpm), 20, 55);
            g2d.drawString(String.format("Speed: %.0f km/h", visualSpeed), 20, 80);

        } else {
            // --- MODO MENU ---
            // Desenha o fundo
            g2d.drawImage(menuBackground, 0, 0, getWidth(), getHeight(), this);

            // Desenha os botões respeitando o novo tamanho compacto e posições no céu
            for (int i = 0; i < buttonImages.length; i++) {
                int offset = (botaoPressionado == i) ? 4 : 0; // Reduzido o clique para 4px para combinar com o tamanho menor
                g2d.drawImage(buttonImages[i], botoes[i].x, botoes[i].y + offset, botoes[i].width, botoes[i].height, null);
            }
        }
    }
}