package gdd.sprite;

import static gdd.Global.*;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;

public class Player extends Sprite {

    private static final int START_X = 50;
    private static final int START_Y = BOARD_HEIGHT / 2;
    private int width;
    private int height;
    private int currentSpeed = 2;
    private int shotsUpgrade = 1;
    protected int dy;

    public Player() {
        initPlayer();
    }

    private void initPlayer() {
        var ii = new ImageIcon(IMG_PLAYER);
        var scaledImage = ii.getImage().getScaledInstance(ii.getIconWidth() * SCALE_FACTOR,
                ii.getIconHeight() * SCALE_FACTOR,
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);

        width = scaledImage.getWidth(null);
        height = scaledImage.getHeight(null);
        setX(START_X);
        setY(START_Y);
    }

    public int getSpeed() { return currentSpeed; }

    public int setSpeed(int speed) {
        if (speed < 1) speed = 1;
        if (speed > 10) speed = 10;
        this.currentSpeed = speed;
        return currentSpeed;
    }

    public int getShotsUpgrade() { return shotsUpgrade; }

    public void setShotsUpgrade(int limit) {
        if (limit > 4) limit = 4;
        this.shotsUpgrade = limit;
    }

    @Override
    public void act() {
        x += dx;
        y += dy;

        if (x <= 2) x = 2;
        if (x >= BOARD_WIDTH - width) x = BOARD_WIDTH - width;
        if (y <= 2) y = 2;
        if (y >= BOARD_HEIGHT - height) y = BOARD_HEIGHT - height;
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) dx = -currentSpeed;
        if (key == KeyEvent.VK_RIGHT) dx = currentSpeed;
        if (key == KeyEvent.VK_UP) dy = -currentSpeed;
        if (key == KeyEvent.VK_DOWN) dy = currentSpeed;
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) dx = 0;
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) dy = 0;
    }
}