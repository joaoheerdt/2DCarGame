import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

import assets.vehicles.*;
import map.GameMap;
import sound.AudioManager;

public class GamePanel extends JPanel implements ActionListener {
    // 1. Definição do Canvas Virtual (Referência Base de Altura 720p)
    private final int VIRTUAL_WIDTH = 1280;
    private final int VIRTUAL_HEIGHT = 720;

    private Vehicle activeVehicle;
    private List<Vehicle> garageCars;
    private int currentCarIndex = 0;

    private GameMap activeMap;
    private boolean isAccelerating = false, isBraking = false;
    private Timer loop;

    private enum GameStage {MENU, GARAGE, PLAYING, PAUSED, CONFIG}
    private GameStage currentState = GameStage.MENU;

    private AudioManager audioManager = new AudioManager();

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

    // 2. Coordenadas base dos botões do menu
    private Rectangle[] buttons = {
            new Rectangle(490, 320, 300, 60), // 0: JOGAR
            new Rectangle(50, 600, 60, 60),   // 1: CONFIGURAÇÕES
            new Rectangle(515, 420, 250, 50), // 2: GARAGEM
            new Rectangle(1170, 600, 60, 60)  // 3: SAIR
    };

    private Rectangle btnContinuar = new Rectangle(515, 230, 250, 45);
    private Rectangle btnIrGaragem = new Rectangle(515, 300, 250, 45);
    private Rectangle btnMenuPrincipal = new Rectangle(515, 370, 250, 45);

    private Rectangle btnMusicaMenos = new Rectangle(480, 480, 45, 40);
    private Rectangle btnMusicaMais = new Rectangle(755, 480, 45, 40);
    private Rectangle btnEfeitosMenos = new Rectangle(480, 540, 45, 40);
    private Rectangle btnEfeitosMais = new Rectangle(755, 540, 45, 40);

    // Botões da Garagem
    private Rectangle btnCarroAnterior = new Rectangle(200, 400, 60, 60);
    private Rectangle btnProximoCarro = new Rectangle(1020, 400, 60, 60);
    private Rectangle btnSelecionarCarro = new Rectangle(520, 620, 240, 50);

    private int botaoPressionado = -1;
    private int volumeMusica = 80;
    private int volumeEfeitos = 80;

    public GamePanel() {
        setFocusable(true);

        this.garageCars = VehicleLoader.loadVehicles("src/assets/vehicles/vehicles.xml");
        if (this.garageCars != null && !this.garageCars.isEmpty()) {
            this.activeVehicle = this.garageCars.get(currentCarIndex);
            this.activeVehicle.setX(480);
            this.activeVehicle.setY(480);
        }

        this.activeMap = new GameMap("src/assets/map/default_map.png");

        audioManager.playMusic("src/assets/menu/sound/top_gear.wav");
        audioManager.setVolume(volumeMusica);

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int vWidth = getVirtualWidth();

                buttons[0].x = (vWidth - buttons[0].width) / 2;
                buttons[2].x = (vWidth - buttons[2].width) / 2;
                buttons[3].x = vWidth - 110;
                btnProximoCarro.x = vWidth - 260;

                Point p = getVirtualPoint(e.getPoint());

                if (currentState == GameStage.MENU) {
                    for (int i = 0; i < buttons.length; i++) {
                        if (buttons[i].contains(p)) {
                            botaoPressionado = i;
                            repaint();
                        }
                    }
                } else if (currentState == GameStage.GARAGE) {
                    if (btnProximoCarro.contains(p)) {
                        currentCarIndex = (currentCarIndex + 1) % garageCars.size();
                    } else if (btnCarroAnterior.contains(p)) {
                        currentCarIndex = (currentCarIndex - 1 + garageCars.size()) % garageCars.size();
                    } else if (btnSelecionarCarro.contains(p)) {
                        activeVehicle = garageCars.get(currentCarIndex);
                        activeVehicle.setX(480);
                        activeVehicle.setY(480);
                        currentState = GameStage.MENU;
                    }
                    repaint();
                } else if (currentState == GameStage.PAUSED || currentState == GameStage.CONFIG) {
                    if (currentState == GameStage.PAUSED && btnContinuar.contains(p)) {
                        currentState = GameStage.PLAYING;
                    } else if (btnIrGaragem.contains(p)) {
                        currentState = GameStage.GARAGE;
                    } else if (btnMenuPrincipal.contains(p)) {
                        currentState = GameStage.MENU;
                    } else if (btnMusicaMenos.contains(p)) {
                        volumeMusica = Math.max(0, volumeMusica - 10);
                        audioManager.setVolume(volumeMusica);
                    } else if (btnMusicaMais.contains(p)) {
                        volumeMusica = Math.min(100, volumeMusica + 10);
                        audioManager.setVolume(volumeMusica);
                    } else if (btnEfeitosMenos.contains(p)) {
                        volumeEfeitos = Math.max(0, volumeEfeitos - 10);
                        if (activeVehicle != null) activeVehicle.setVolumeEfeitos(volumeEfeitos);
                    } else if (btnEfeitosMais.contains(p)) {
                        volumeEfeitos = Math.min(100, volumeEfeitos + 10);
                        if (activeVehicle != null) activeVehicle.setVolumeEfeitos(volumeEfeitos);
                    }
                    repaint();
                }
            }

            public void mouseReleased(MouseEvent e) {
                Point p = getVirtualPoint(e.getPoint());
                if (currentState == GameStage.MENU && botaoPressionado != -1 && buttons[botaoPressionado].contains(p)) {
                    executarAcao(botaoPressionado);
                }
                botaoPressionado = -1;
                repaint();
            }
        });

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (currentState == GameStage.PLAYING) {
                        currentState = GameStage.PAUSED;
                        isAccelerating = false;
                        isBraking = false;
                        if (activeVehicle != null) activeVehicle.stopEngineSounds();
                    } else if (currentState == GameStage.PAUSED) {
                        currentState = GameStage.PLAYING;
                    } else if (currentState == GameStage.GARAGE || currentState == GameStage.CONFIG) {
                        currentState = GameStage.MENU;
                    }
                }

                if (currentState == GameStage.PLAYING && activeVehicle != null) {
                    if (e.getKeyCode() == KeyEvent.VK_D) isAccelerating = true;
                    if (e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_S) isBraking = true;
                    if (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_5)
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

    private double getScale() {
        return (double) getHeight() / VIRTUAL_HEIGHT;
    }

    private int getVirtualWidth() {
        return (int) Math.round(getWidth() / getScale());
    }

    private Point getVirtualPoint(Point p) {
        double scale = getScale();
        int virtualX = (int) (p.x / scale);
        int virtualY = (int) (p.y / scale);
        return new Point(virtualX, virtualY);
    }

    private void executarAcao(int index) {
        switch (index) {
            case 0:
                if (activeVehicle != null) {
                    activeVehicle.setX(100);
                    activeVehicle.setY(350);
                }
                currentState = GameStage.PLAYING;
                break;
            case 1:
                currentState = GameStage.CONFIG;
                break;
            case 2:
                currentState = GameStage.GARAGE;
                break;
            case 3:
                System.exit(0);
                break;
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (currentState == GameStage.PLAYING && activeVehicle != null) {
            activeVehicle.updatePhysics(isAccelerating, isBraking);
            activeMap.update(activeVehicle.getCurrentSpeed());
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        double scale = getScale();
        int virtualWidthAtual = getVirtualWidth();

        g2d.scale(scale, scale);

        if (currentState == GameStage.PLAYING || currentState == GameStage.PAUSED) {

            if (activeMap != null) {
                activeMap.draw(g2d, this);
            }

            if (activeVehicle != null) activeVehicle.draw(g2d, this);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 22));
            if (activeVehicle != null) {
                int currentGear = activeVehicle.getCurrentGear();
                double visualSpeed = activeVehicle.getCurrentSpeed() * 1.65;
                g2d.drawString("Gear: " + (currentGear == 0 ? "N" : currentGear), 20, 40);
                g2d.drawString(String.format("RPM: %.0f", activeVehicle.getCurrentRpm()), 20, 70);
                g2d.drawString(String.format("Speed: %.0f km/h", visualSpeed), 20, 100);
            }

            if (currentState == GameStage.PAUSED) desenharMenuPause(g2d, virtualWidthAtual);

        } else if (currentState == GameStage.MENU) {
            g2d.drawImage(menuBackground, 0, 0, virtualWidthAtual, VIRTUAL_HEIGHT, this);

            if (activeVehicle != null) {
                activeVehicle.setX(virtualWidthAtual / 2 - activeVehicle.getWidth() / 2);
                activeVehicle.setY(480);
                activeVehicle.draw(g2d, this);
            }

            buttons[0].x = (virtualWidthAtual - buttons[0].width) / 2;
            buttons[2].x = (virtualWidthAtual - buttons[2].width) / 2;
            buttons[3].x = virtualWidthAtual - 110;

            for (int i = 0; i < buttonImages.length; i++) {
                int offset = (botaoPressionado == i) ? 4 : 0;
                g2d.drawImage(buttonImages[i], buttons[i].x, buttons[i].y + offset, buttons[i].width, buttons[i].height, null);
            }

        } else if (currentState == GameStage.GARAGE) {
            g2d.drawImage(garageBackgound, 0, 0, virtualWidthAtual, VIRTUAL_HEIGHT, this);

            btnProximoCarro.x = virtualWidthAtual - 260;
            btnSelecionarCarro.x = virtualWidthAtual / 2 - btnSelecionarCarro.width / 2;

            if (garageCars != null && !garageCars.isEmpty()) {
                Vehicle previewVehicle = garageCars.get(currentCarIndex);
                previewVehicle.setX(virtualWidthAtual / 2 - previewVehicle.getWidth() / 2);
                previewVehicle.setY(420);
                previewVehicle.draw(g2d, this);

                String nomeCarro = previewVehicle.name.toUpperCase();

                Font fontNome = new Font("Segoe UI", Font.BOLD, 32);
                g2d.setFont(fontNome);
                FontMetrics fm = g2d.getFontMetrics();

                int paddingX = 40;
                int paddingY = 15;
                int boxWidth = fm.stringWidth(nomeCarro) + (paddingX * 2);
                int boxHeight = fm.getHeight() + (paddingY * 2);
                int boxX = (virtualWidthAtual - boxWidth) / 2;
                int boxY = 140;

                g2d.setColor(new Color(15, 18, 28, 220));
                g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 16, 16);

                g2d.setColor(new Color(255, 200, 0));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 16, 16);

                int textX = boxX + paddingX;
                int textY = boxY + paddingY + fm.getAscent() - 2;

                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.drawString(nomeCarro, textX + 2, textY + 2);

                g2d.setColor(Color.WHITE);
                g2d.drawString(nomeCarro, textX, textY);
            }

            desenharBotaoMenu(g2d, btnCarroAnterior, "<", new Color(52, 152, 219));
            desenharBotaoMenu(g2d, btnProximoCarro, ">", new Color(52, 152, 219));
            desenharBotaoMenu(g2d, btnSelecionarCarro, "SELECIONAR", new Color(46, 204, 113));

        } else if (currentState == GameStage.CONFIG) {
            desenharMenuConfig(g2d, virtualWidthAtual);
        }

        g2d.dispose();
    }

    private void desenharMenuPause(Graphics2D g2d, int virtualWidth) {
        g2d.drawImage(menuConfigBackground, 0, 0, virtualWidth, VIRTUAL_HEIGHT, this);

        int menuW = 450, menuH = 500;
        int menuX = (virtualWidth - menuW) / 2;
        int menuY = 120;

        g2d.setColor(new Color(40, 42, 54));
        g2d.fillRoundRect(menuX, menuY, menuW, menuH, 20, 20);
        g2d.setColor(new Color(255, 200, 0));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(menuX, menuY, menuW, menuH, 20, 20);

        g2d.setFont(new Font("Arial", Font.BOLD, 28));
        g2d.drawString("JOGO PAUSADO", menuX + 115, menuY + 60);

        btnContinuar.x = menuX + 100;
        btnIrGaragem.x = menuX + 100;
        btnMenuPrincipal.x = menuX + 100;

        desenharBotaoMenu(g2d, btnContinuar, "CONTINUAR", new Color(46, 204, 113));
        desenharBotaoMenu(g2d, btnIrGaragem, "GARAGEM", new Color(52, 152, 219));
        desenharBotaoMenu(g2d, btnMenuPrincipal, "MENU PRINCIPAL", new Color(231, 76, 60));

        desenharControlesDeVolume(g2d, menuX);
    }

    private void desenharMenuConfig(Graphics2D g2d, int virtualWidth) {
        g2d.drawImage(menuConfigBackground, 0, 0, virtualWidth, VIRTUAL_HEIGHT, this);

        int menuW = 450, menuH = 500;
        int menuX = (virtualWidth - menuW) / 2;
        int menuY = 120;

        g2d.setColor(new Color(40, 42, 54));
        g2d.fillRoundRect(menuX, menuY, menuW, menuH, 20, 20);
        g2d.setColor(new Color(255, 200, 0));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(menuX, menuY, menuW, menuH, 20, 20);

        g2d.setFont(new Font("Arial", Font.BOLD, 28));
        g2d.drawString("CONFIGURAÇÃO", menuX + 115, menuY + 60);

        btnIrGaragem.x = menuX + 100;
        btnMenuPrincipal.x = menuX + 100;

        desenharBotaoMenu(g2d, btnIrGaragem, "GARAGEM", new Color(52, 152, 219));
        desenharBotaoMenu(g2d, btnMenuPrincipal, "MENU PRINCIPAL", new Color(231, 76, 60));

        desenharControlesDeVolume(g2d, menuX);
    }

    private void desenharControlesDeVolume(Graphics2D g2d, int menuX) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));

        btnMusicaMenos.x = menuX + 65;
        btnMusicaMais.x = menuX + 340;
        btnEfeitosMenos.x = menuX + 65;
        btnEfeitosMais.x = menuX + 340;

        g2d.drawString("MÚSICA: " + volumeMusica + "%", menuX + 130, 505);
        desenharBotaoMenu(g2d, btnMusicaMenos, "-", new Color(100, 100, 100));
        desenharBotaoMenu(g2d, btnMusicaMais, "+", new Color(100, 100, 100));

        g2d.drawString("EFEITOS: " + volumeEfeitos + "%", menuX + 130, 565);
        desenharBotaoMenu(g2d, btnEfeitosMenos, "-", new Color(100, 100, 100));
        desenharBotaoMenu(g2d, btnEfeitosMais, "+", new Color(100, 100, 100));
    }

    private void desenharBotaoMenu(Graphics2D g2d, Rectangle rect, String texto, Color cor) {
        g2d.setColor(cor);
        g2d.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
        g2d.setColor(Color.WHITE);
        g2d.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);

        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = rect.x + (rect.width - fm.stringWidth(texto)) / 2;
        int textY = rect.y + ((rect.height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(texto, textX, textY);
    }
}