package gdd.powerup;

import gdd.sprite.Sprite;
import gdd.sprite.Player;

public abstract class PowerUp extends Sprite {

    public PowerUp(int x, int y) {
        setX(x);
        setY(y);
    }

    // Forces all child classes to have an upgrade method
    public abstract void upgrade(Player player);

    @Override
    public boolean hasThreeWay() {
        return false;
    }
}