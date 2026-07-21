package gdd.sprite;

import static gdd.Global.*;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Player extends Sprite {

    private static final int START_X = 50, START_Y = BOARD_HEIGHT / 2;
    private int width, height, currentSpeed = 2, shotsUpgrade = 1;
    private boolean hasThreeWay = false;
    private int lives = 3, invulnerableTimer = 0;
    protected int dy;

    // Animation Variables
    private Image[] frames;
    private int currentFrame = 0;
    private int animationTick = 0;
    private final int ANIMATION_SPEED = 10;

    public Player() {
        initPlayer();
    }

    private void initPlayer() {
        try {
            // 1. Load the new 2-frame spritesheet
            BufferedImage spriteSheet = ImageIO.read(new File("src/images/plane1.png"));

            // 2. Calculate the size of a single frame (divide by 2 instead of 3)
            int frameWidth = spriteSheet.getWidth();
            int frameHeight = spriteSheet.getHeight() / 2;

            // Initialize array for 2 frames
            frames = new Image[2];

            // 3. Loop through and clip each of the 2 frames
            for (int i = 0; i < 2; i++) {
                BufferedImage subImg = spriteSheet.getSubimage(0, i * frameHeight, frameWidth, frameHeight);

                frames[i] = subImg.getScaledInstance(frameWidth * SCALE_FACTOR,
                        frameHeight * SCALE_FACTOR, java.awt.Image.SCALE_SMOOTH);
            }

            setImage(frames[0]);
            width = frames[0].getWidth(null);
            height = frames[0].getHeight(null);

        } catch (Exception e) {
            System.err.println("Error loading player spritesheet: " + e.getMessage());
        }

        setX(START_X);
        setY(START_Y);
    }

    public int getSpeed() { return currentSpeed; }
    public void setSpeed(int speed) { this.currentSpeed = Math.min(Math.max(speed, 1), 10); }
    public int getShotsUpgrade() { return shotsUpgrade; }
    public void setShotsUpgrade(int limit) { this.shotsUpgrade = Math.min(limit, 4); }
    public boolean isHasThreeWay() { return hasThreeWay; }
    public void setHasThreeWay(boolean hasThreeWay) { this.hasThreeWay = hasThreeWay; }
    public int getLives() { return lives; }
    public boolean isInvulnerable() { return invulnerableTimer > 0; }

    public void takeDamage() {
        if (invulnerableTimer == 0) {
            lives--;
            invulnerableTimer = 60;
            if (lives <= 0) this.setVisible(false);
        }
    }

    @Override
    public void act() {
        animationTick++;
        if (animationTick >= ANIMATION_SPEED) {
            animationTick = 0;
            currentFrame = (currentFrame + 1) % frames.length; // Will seamlessly cycle 0 -> 1 -> 0
            setImage(frames[currentFrame]);
        }

        if (invulnerableTimer > 0) invulnerableTimer--;

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