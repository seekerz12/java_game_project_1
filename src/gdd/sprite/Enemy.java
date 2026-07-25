package gdd.sprite;

public class Enemy extends Sprite {

    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void act(int direction) {
        this.x += direction;
    }

    @Override
    public void act() {
        // Left intentionally empty as subclasses override this
    }
    @Override
    public boolean hasThreeWay() {
        // Enemies don't use the player's 3-way upgrade, so we just return false
        // to satisfy the Sprite abstract class requirements.
        return false;
    }
}