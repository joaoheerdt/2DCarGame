package map;
import java.awt.*;
import javax.swing.ImageIcon;

public class GameMap {

    private Image mapImage;
    private double mapMovement;
    private int mapWidth;
    private int mapHeight;

    public GameMap(String imagePath) {
        this.mapImage = new ImageIcon(imagePath).getImage();
        this.mapMovement = 0;

        // Pega dinamicamente a largura e altura reais da nova imagem longa
        this.mapWidth = mapImage.getWidth(null);
        this.mapHeight = mapImage.getHeight(null);

        // Fallback caso as dimensões demorem um instante para carregar
        if (this.mapWidth <= 0) this.mapWidth = 4096;
        if (this.mapHeight <= 0) this.mapHeight = 720;
    }

    public void update(double vehicleSpeed) {
        mapMovement -= (vehicleSpeed * 0.2);

        // Reseta o ciclo de movimento baseado na largura real da imagem
        if (mapMovement <= -mapWidth) {
            mapMovement += mapWidth;
        }
    }

    public void draw(Graphics2D g2d, Component component) {
        double visualOffset = mapMovement % mapWidth;

        // Desenha a imagem repetida lado a lado cobrindo exatamente 720px de altura sem distorcer
        for (int posX = -mapWidth; posX < 1280 + mapWidth; posX += mapWidth) {
            g2d.drawImage(mapImage, (int) (posX + visualOffset), 0, mapWidth, 720, component);
        }
    }
}