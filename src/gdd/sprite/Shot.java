package gdd.sprite;

import static gdd.Global.*;
import javax.swing.ImageIcon;

public class Shot extends Sprite {

    private static final int H_SPACE = 20, V_SPACE = 10;
    private int dy = 0;

    public Shot(int x, int y, int dy) {
        this.dy = dy;
        var ii = new ImageIcon(IMG_SHOT);
        var scaledImage = ii.getImage().getScaledInstance(ii.getIconWidth() * SCALE_FACTOR,
                ii.getIconHeight() * SCALE_FACTOR, java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
        setX(x + H_SPACE);
        setY(y + V_SPACE);
    }

    @Override
    public void act() {
        this.x += 10;
        this.y += dy;
        if (this.x > BOARD_WIDTH || this.y < 0 || this.y > BOARD_HEIGHT) this.die();
    }
}