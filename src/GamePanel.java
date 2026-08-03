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

    private enum GameStage {MENU, GARAGE, PLAYING, PAUSED, CONFIG}
    private GameStage currentState = GameStage.MENU;

    // Imagens
    private Image menuBackground = new ImageIcon("src/assets/menu/menu_background.png").getImage();
    private Image garageBackgound = new ImageIcon("src/assets/menu/garage_background.png").getImage();
    private Image menuConfigBackground = new ImageIcon("src/assets/menu/fundo_menu_config.png").getImage();
    private Image[] buttonImages = {
            new ImageIcon("src/assets/menu/play_button.png").getImage(),
            new ImageIcon("src/assets/menu/config_button.png").getImage(),
            new ImageIcon("src/assets/menu/garage_button.png").getImage(),
            new ImageIcon("src/assets/menu/exit_button.png").getImage()
    };

    // Botões do Menu Principal
    private Rectangle[] buttons = {
            new Rectangle(250, 200, 300, 60), // 0: JOGAR
            new Rectangle(40, 500, 60, 60),   // 1: CONFIGURAÇÕES
            new Rectangle(276, 320, 250, 50), // 2: GARAGEM
            new Rectangle(700, 500, 60, 60)   // 3: SAIR
    };

    // Botões do Menu Pause
    private Rectangle btnContinuar = new Rectangle(275, 180, 250, 45);
    private Rectangle btnIrGaragem = new Rectangle(275, 240, 250, 45);
    private Rectangle btnMenuPrincipal = new Rectangle(275, 300, 250, 45);
    private Rectangle btnVolDiminuir = new Rectangle(280, 370, 45, 40); // Botão "-"
    private Rectangle btnVolAumentar = new Rectangle(475, 370, 45, 40); // Botão "+"

    private int botaoPressionado = -1;
    private int volumeGame = 80; // Volume inicial em % (0 a 100)

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        this.activeVehicle = new Fusca();

        // Posição do Fusca no pátio do Menu
        this.activeVehicle.setX(250);
        this.activeVehicle.setY(380);

        this.activeMap = new GameMap("src/assets/map/default_map.png");

        // Mouse Listener para detectar cliques no Menu e no Pause
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();

                if (currentState == GameStage.MENU) {
                    for (int i = 0; i < buttons.length; i++) {
                        if (buttons[i].contains(p)) {
                            botaoPressionado = i;
                            repaint();
                        }
                    }
                }
                else if (currentState == GameStage.PAUSED) {
                    // Trata os cliques do menu de Pause
                    if (btnContinuar.contains(p)) {
                        currentState = GameStage.PLAYING;
                    } else if (btnIrGaragem.contains(p)) {
                        currentState = GameStage.GARAGE;
                        activeVehicle.setX(250);
                        activeVehicle.setY(380);
                    } else if (btnMenuPrincipal.contains(p)) {
                        // Reseta o Fusca para o pátio do menu
                        activeVehicle.setX(250);
                        activeVehicle.setY(380);
                        currentState = GameStage.MENU;
                    } else if (btnVolDiminuir.contains(p)) {
                        volumeGame = Math.max(0, volumeGame - 10);
                        // TODO: Adicione aqui o método para diminuir volume do áudio se tiver um Mixer
                    } else if (btnVolAumentar.contains(p)) {
                        volumeGame = Math.min(100, volumeGame + 10);
                        // TODO: Adicione aqui o método para aumentar volume do áudio
                    }}

                    else if (currentState == GameStage.CONFIG) {
                        // Trata os cliques do menu de Pause
                          if (btnIrGaragem.contains(p)) {
                            currentState = GameStage.GARAGE;
                            activeVehicle.setX(250);
                            activeVehicle.setY(380);
                        } else if (btnMenuPrincipal.contains(p)) {
                            // Reseta o Fusca para o pátio do menu
                            activeVehicle.setX(250);
                            activeVehicle.setY(380);
                            currentState = GameStage.MENU;
                        } else if (btnVolDiminuir.contains(p)) {
                            volumeGame = Math.max(0, volumeGame - 10);
                            // TODO: Adicione aqui o método para diminuir volume do áudio se tiver um Mixer
                        } else if (btnVolAumentar.contains(p)) {
                            volumeGame = Math.min(100, volumeGame + 10);
                            // TODO: Adicione aqui o método para aumentar volume do áudio
                        }}
                    repaint();
                }


            public void mouseReleased(MouseEvent e) {
                if (currentState == GameStage.MENU && botaoPressionado != -1 && buttons[botaoPressionado].contains(e.getPoint())) {
                    executarAcao(botaoPressionado);
                }
                botaoPressionado = -1;
                repaint();
            }
        });

        // Key Listener para controles e tecla ESC
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                // Tecla ESC para Pausar/Despausar ou Voltar da Garagem
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (currentState == GameStage.PLAYING) {
                        currentState = GameStage.PAUSED;
                        isAccelerating = false; // Parar de acelerar ao pausar
                        isBraking = false;
                    } else if (currentState == GameStage.PAUSED) {
                        currentState = GameStage.PLAYING;
                    } else if (currentState == GameStage.GARAGE) {
                        currentState = GameStage.MENU;
                    }
                }

                // Controles de pilotagem (Apenas em jogo)
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
            case 0: // JOGAR
                this.activeVehicle.setX(50);
                this.activeVehicle.setY(280);
                currentState = GameStage.PLAYING;
                break;
            case 1: // CONFIGURAÇÕES (Do Menu Principal)
                currentState = GameStage.CONFIG; // Abre a tela de ajustes
                break;
            case 2: // GARAGEM
                currentState = GameStage.GARAGE;
                break;
            case 3: // SAIR
                System.exit(0);
                break;
        }
    }

    public void actionPerformed(ActionEvent e) {
        // A física só roda se o jogo estiver ativamente RODANDO (não roda no Pause)
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

        if (currentState == GameStage.PLAYING || currentState == GameStage.PAUSED) {
            // 1. Desenha o fundo do game
            activeMap.draw(g2d, this);
            activeVehicle.draw(g2d, this);

            // HUD
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            int currentGear = activeVehicle.getCurrentGear();
            double visualSpeed = activeVehicle.getCurrentSpeed() * 1.65;

            g2d.drawString("Gear: " + (currentGear == 0 ? "N" : currentGear), 20, 30);
            g2d.drawString(String.format("RPM: %.0f", activeVehicle.getCurrentRpm()), 20, 55);
            g2d.drawString(String.format("Speed: %.0f km/h", visualSpeed), 20, 80);

            // 2. Se estiver PAUSADO, desenha o menu pause
            if (currentState == GameStage.PAUSED) {
                desenharMenuPause(g2d);
            }

        } else if (currentState == GameStage.MENU) {
            g2d.drawImage(menuBackground, 0, 0, getWidth(), getHeight(), this);
            activeVehicle.draw(g2d, this);

            for (int i = 0; i < buttonImages.length; i++) {
                int offset = (botaoPressionado == i) ? 4 : 0;
                g2d.drawImage(buttonImages[i], buttons[i].x, buttons[i].y + offset, buttons[i].width, buttons[i].height, null);
            }

        } else if (currentState == GameStage.GARAGE) {
            g2d.drawImage(garageBackgound, 0, 0, getWidth(), getHeight(), this);
            // Dica: Pressione ESC para voltar ao Menu
        }
        else if ( currentState == GameStage.CONFIG) {
            desenharMenuConfig(g2d);
        }
    }

    private void desenharMenuPause(Graphics2D g2d) {
        g2d.drawImage(menuConfigBackground, 0, 0, getWidth(), getHeight(), this);
        /* Escurece o fundo do jogo (Filtro escuro transparente)
        g2d.setColor(new Color(0, 0, 0, 170));
        g2d.fillRect(0, 0, getWidth(), getHeight()); */

        // Caixas da janela de Pause
        int menuX = 220, menuY = 100, menuW = 360, menuH = 340;
        g2d.setColor(new Color(40, 42, 54));
        g2d.fillRoundRect(menuX, menuY, menuW, menuH, 20, 20);
        g2d.setColor(new Color(255, 200, 0));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(menuX, menuY, menuW, menuH, 20, 20);

        // Título "PAUSA"
        g2d.setFont(new Font("Arial", Font.BOLD, 26));
        g2d.drawString("JOGO PAUSADO", menuX + 85, menuY + 45);

        // Desenhar Botões do Pause
        desenharBotaoMenu(g2d, btnContinuar, "CONTINUAR", new Color(46, 204, 113));
        desenharBotaoMenu(g2d, btnIrGaragem, "GARAGEM", new Color(52, 152, 219));
        desenharBotaoMenu(g2d, btnMenuPrincipal, "MENU PRINCIPAL", new Color(231, 76, 60));

        // Controle de Volume
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("VOLUME: " + volumeGame + "%", menuX + 120, 395);

        desenharBotaoMenu(g2d, btnVolDiminuir, "-", new Color(100, 100, 100));
        desenharBotaoMenu(g2d, btnVolAumentar, "+", new Color(100, 100, 100));
    }
    private void desenharMenuConfig(Graphics2D g2d) {
        g2d.drawImage(menuConfigBackground, 0, 0, getWidth(), getHeight(), this);

        // Caixas da janela de MenuConfig
        int menuX = 220, menuY = 100, menuW = 360, menuH = 340;
        g2d.setColor(new Color(40, 42, 54));
        g2d.fillRoundRect(menuX, menuY, menuW, menuH, 20, 20);
        g2d.setColor(new Color(255, 200, 0));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(menuX, menuY, menuW, menuH, 20, 20);

        // Título "Config"
        g2d.setFont(new Font("Arial", Font.BOLD, 26));
        g2d.drawString("CONFIGURAÇÃO", menuX + 85, menuY + 45);

        // Desenhar Botões do menuConfig
        desenharBotaoMenu(g2d, btnIrGaragem, "GARAGEM", new Color(52, 152, 219));
        desenharBotaoMenu(g2d, btnMenuPrincipal, "MENU PRINCIPAL", new Color(231, 76, 60));

        // Controle de Volume
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("VOLUME: " + volumeGame + "%", menuX + 120, 395);

        desenharBotaoMenu(g2d, btnVolDiminuir, "-", new Color(100, 100, 100));
        desenharBotaoMenu(g2d, btnVolAumentar, "+", new Color(100, 100, 100));
    }

    private void desenharBotaoMenu(Graphics2D g2d, Rectangle rect, String texto, Color cor) {
        g2d.setColor(cor);
        g2d.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
        g2d.setColor(Color.WHITE);
        g2d.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);

        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = rect.x + (rect.width - fm.stringWidth(texto)) / 2;
        int textY = rect.y + ((rect.height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(texto, textX, textY);
    }
}