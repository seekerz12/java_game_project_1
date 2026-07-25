package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;
import javax.swing.ImageIcon;

public class MultiShot extends PowerUp {

    public MultiShot(int x, int y) {
        super(x, y);
        ImageIcon ii = new ImageIcon(IMG_POWERUP_MULTISHOT);
        var scaledImage = ii.getImage().getScaledInstance(
                ii.getIconWidth() * SCALE_FACTOR,
                ii.getIconHeight() * SCALE_FACTOR,
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
    }

    @Override
    public void act() {
        this.x -= 2; // Moves left towards player
    }

    @Override
    public void upgrade(Player player) {
        player.setShotsUpgrade(player.getShotsUpgrade() + 1);
        this.die();
    }
    @Override
    public boolean hasThreeWay() {
        return false;
    }
}