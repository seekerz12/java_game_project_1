package gdd.sprite;

import static gdd.Global.*;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Alien1 extends Enemy {

    private Bomb bomb;

    private Image[] frames;
    private int currentFrame = 0;
    private int animationTick = 0;
    private final int ANIMATION_SPEED = 10;

    public Alien1(int x, int y) {
        super(x, y);
        this.x = x;
        this.y = y;
        bomb = new Bomb(x, y);
        initEnemy();
    }

    private void initEnemy() {
        try {
            BufferedImage spriteSheet = ImageIO.read(new File(IMG_ALIEN1));
            int frameWidth = spriteSheet.getWidth() / 2;
            int frameHeight = spriteSheet.getHeight();

            frames = new Image[2];
            for (int i = 0; i < 2; i++) {
                // 1. Clip the original frame
                BufferedImage subImg = spriteSheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);

                // 2. Create a blank image to hold the flipped version
                BufferedImage flippedImg = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = flippedImg.createGraphics();

                // 3. Draw the image backwards (X starts at width, width is negative)
                g.drawImage(subImg, frameWidth, 0, -frameWidth, frameHeight, null);
                g.dispose();

                // 4. Scale the newly flipped image
                frames[i] = flippedImg.getScaledInstance(frameWidth * SCALE_FACTOR,
                        frameHeight * SCALE_FACTOR, java.awt.Image.SCALE_SMOOTH);
            }

            setImage(frames[0]);
        } catch (Exception e) {
            System.err.println("Error loading alien1 spritesheet: " + e.getMessage());
        }
    }

    @Override
    public void act(int direction) {
        this.x -= 2;

        animationTick++;
        if (animationTick >= ANIMATION_SPEED) {
            animationTick = 0;
            currentFrame = (currentFrame + 1) % frames.length;
            setImage(frames[currentFrame]);
        }
    }

    public Bomb getBomb() { return bomb; }

    public class Bomb extends Sprite {
        private boolean destroyed;
        public Bomb(int x, int y) {
            setDestroyed(true);
            this.x = x; this.y = y;
            var ii = new ImageIcon(IMG_BOMB);
            setImage(ii.getImage());
        }
        public void setDestroyed(boolean destroyed) { this.destroyed = destroyed; }
        public boolean isDestroyed() { return destroyed; }
        @Override public void act() {}
    }
}