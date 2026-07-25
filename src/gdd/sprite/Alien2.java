package gdd.sprite;

import static gdd.Global.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Alien2 extends Enemy {

    private int startY;
    private int waveOffset = 0;

    private Image[] frames;
    private int currentFrame = 0;
    private int animationTick = 0;
    private final int ANIMATION_SPEED = 5;

    // 1. Declare the new custom Wavy Bomb
    private WaveBomb bomb;

    public Alien2(int x, int y) {
        super(x, y);
        this.startY = y;
        this.bomb = new WaveBomb(x, y);
        initEnemy2();
    }

    public WaveBomb getBomb() { return bomb; }

    private void initEnemy2() {
        try {
            BufferedImage spriteSheet = ImageIO.read(new File(IMG_ALIEN2));
            int frameWidth = spriteSheet.getWidth();
            int frameHeight = spriteSheet.getHeight() / 3;

            // 2. The 2-pixel crop to prevent screen bleeding
            int crop = 2;

            frames = new Image[3];
            for (int i = 0; i < 3; i++) {
                BufferedImage subImg = spriteSheet.getSubimage(
                        0 + crop,
                        (i * frameHeight) + crop,
                        frameWidth - (crop * 2),
                        frameHeight - (crop * 2)
                );

                frames[i] = subImg.getScaledInstance(frameWidth * 2,
                        frameHeight * 2, java.awt.Image.SCALE_SMOOTH);
            }

            setImage(frames[0]);
        } catch (Exception e) {
            System.err.println("Error loading alien2 spritesheet: " + e.getMessage());
        }
    }

    @Override
    public void act(int direction) {
        this.x -= 3;
        waveOffset++;
        this.y = startY + (int)(Math.sin(waveOffset * 0.1) * 30);

        animationTick++;
        if (animationTick >= ANIMATION_SPEED) {
            animationTick = 0;
            currentFrame = (currentFrame + 1) % frames.length;
            setImage(frames[currentFrame]);
        }
    }

    // 3. THE NEW SHOOTING STYLE
    public class WaveBomb extends Sprite {
        private boolean destroyed;
        private int bombStartY;
        private int tick = 0;

        public WaveBomb(int x, int y) {
            setDestroyed(true);
            this.x = x;
            this.y = y;
            this.bombStartY = y;
            var ii = new ImageIcon(IMG_BOMB);
            setImage(ii.getImage());
        }

        public void setDestroyed(boolean destroyed) { this.destroyed = destroyed; }
        public boolean isDestroyed() { return destroyed; }
        public void setBombStartY(int y) { this.bombStartY = y; }

        @Override
        public void act() {
            this.x -= 5; // Flies left
            tick++;
            // Wiggles up and down wildly as it travels
            this.y = bombStartY + (int)(Math.sin(tick * 0.25) * 50);
        }
        @Override
        public boolean hasThreeWay() {
            return false;
        }
    }
}