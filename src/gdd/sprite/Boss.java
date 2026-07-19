package gdd.sprite;

import static gdd.Global.*;
import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.List;

public class Boss extends Enemy {

    private int hp = 30;
    private int yDirection = 3;
    private int stopX = BOARD_WIDTH - 200;

    public Boss(int x, int y) {
        super(x, y);
        initBoss();
    }

    private void initBoss() {
        var ii = new ImageIcon(IMG_ENEMY);
        var scaledImage = ii.getImage().getScaledInstance(
                ii.getIconWidth() * SCALE_FACTOR * 2,
                ii.getIconHeight() * SCALE_FACTOR * 2,
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
    }

    @Override
    public void act(int direction) {
        if (this.x > stopX) {
            this.x -= 2;
        } else {
            this.y += yDirection;
            if (this.y <= 0 || this.y >= BOARD_HEIGHT - 120) {
                yDirection *= -1;
            }
        }
    }

    public void takeDamage() {
        hp--;
        if (hp <= 0) {
            this.setDying(true);
        }
    }

    public int getHp() {
        return hp;
    }

    public List<Bomb> fireMultipleShots() {
        List<Bomb> spread = new ArrayList<>();
        spread.add(new Bomb(this.x, this.y + 20));
        spread.add(new Bomb(this.x, this.y + 50));
        spread.add(new Bomb(this.x, this.y + 80));
        return spread;
    }

    public class Bomb extends Sprite {
        private boolean destroyed;
        public Bomb(int x, int y) { initBomb(x, y); }
        private void initBomb(int x, int y) {
            setDestroyed(false);
            this.x = x;
            this.y = y;
            var bombImg = "src/images/bomb.png";
            var ii = new ImageIcon(bombImg);
            setImage(ii.getImage());
        }
        public void setDestroyed(boolean destroyed) { this.destroyed = destroyed; }
        public boolean isDestroyed() { return destroyed; }
        @Override public void act() {} // Satisfy abstract
    }
}