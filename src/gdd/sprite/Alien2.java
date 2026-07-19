package gdd.sprite;

import static gdd.Global.*;
import javax.swing.ImageIcon;

public class Alien2 extends Enemy {

    private int startY;
    private int waveOffset = 0;

    public Alien2(int x, int y) {
        super(x, y);
        this.startY = y;
        initEnemy2();
    }

    private void initEnemy2() {
        var ii = new ImageIcon(IMG_ENEMY);
        var scaledImage = ii.getImage().getScaledInstance(ii.getIconWidth() * SCALE_FACTOR,
                ii.getIconHeight() * SCALE_FACTOR,
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
    }

    @Override
    public void act(int direction) {
        this.x -= 3;
        waveOffset++;
        this.y = startY + (int)(Math.sin(waveOffset * 0.1) * 30);
    }
}