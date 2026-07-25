package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;
import javax.swing.ImageIcon;

public class ThreeWayUpgrade extends PowerUp {
    public ThreeWayUpgrade(int x, int y) {
        super(x, y);
        ImageIcon ii = new ImageIcon(IMG_POWERUP_TRIPLESHOT);
        var scaledImage = ii.getImage().getScaledInstance(
                ii.getIconWidth() * SCALE_FACTOR,
                ii.getIconHeight() * SCALE_FACTOR,
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
    }

    @Override public void act() { this.x -= 2; }
    @Override public void upgrade(Player player) {
        player.setHasThreeWay(true);
        this.die();
    }
    @Override
    public boolean hasThreeWay() {
        return false;
    }
}