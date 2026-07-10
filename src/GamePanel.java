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
            new ImageIcon("src/assets/menu/exit_button.png").getImage()
    };

    private Rectangle[] buttons = {
            new Rectangle(250, 200, 300, 60), // JOGAR
            new Rectangle(40, 500, 60, 60), // CONFIGURAÇÕES
            new Rectangle(276, 320, 250, 50), // GARAGEM
            new Rectangle(700, 500, 60, 60)  // SAIR
    };

    private int botaoPressionado = -1;

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        this.activeVehicle = new Fusca();

        // --- AJUSTE DO FUSCA NO MENU ---
        // Se a estrada do menu for na mesma altura do jogo, use Y = 280.
        // Se quiser mover o Fusca mais para o lado para não cobrir os botões, mude o X (ex: 480).
        this.activeVehicle.setX(250);
        this.activeVehicle.setY(380);

        this.activeMap = new GameMap("src/assets/map/default_map.png");

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (currentState == GameStage.MENU) {
                    for (int i = 0; i < buttons.length; i++) {
                        if (buttons[i].contains(e.getPoint())) {
                            botaoPressionado = i;
                            repaint();
                        }
                    }
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (botaoPressionado != -1 && buttons[botaoPressionado].contains(e.getPoint())) {
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
                // --- TELEPORTE PARA A CORRIDA ---
                // Reseta o Fusca exatamente para a posição do chão da pista que estava no construtor dele!
                this.activeVehicle.setX(50);
                this.activeVehicle.setY(280);
                currentState = GameStage.PLAYING;
                break; // JOGAR

            case 3:
                // CORREÇÃO: Mudei de case 4 para case 3, pois seu array de botões vai de 0 a 3.
                // Agora o botão SAIR vai fechar o jogo de verdade!
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

            // Desenha o Fusca na posição configurada para o menu
            activeVehicle.draw(g2d, this);

            // Desenha os botões intactos, mantendo o seu tamanho e clique original
            for (int i = 0; i < buttonImages.length; i++) {
                int offset = (botaoPressionado == i) ? 4 : 0;
                g2d.drawImage(buttonImages[i], buttons[i].x, buttons[i].y + offset, buttons[i].width, buttons[i].height, null);
            }

            // CORREÇÃO: Removi a linha duplicada do activeVehicle.draw que estava aqui no final repetida!
        }
    }
}