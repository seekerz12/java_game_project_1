package gdd.sprite;

import static gdd.Global.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Alien2 extends Enemy {

    private int startY;
    private int waveOffset = 0;

    private Image[] frames;
    private int currentFrame = 0;
    private int animationTick = 0;
    private final int ANIMATION_SPEED = 5;

    public Alien2(int x, int y) {
        super(x, y);
        this.startY = y;
        initEnemy2();
    }

    private void initEnemy2() {
        try {
            BufferedImage spriteSheet = ImageIO.read(new File(IMG_ALIEN2));
            int frameWidth = spriteSheet.getWidth();
            int frameHeight = spriteSheet.getHeight() / 3;

            frames = new Image[3];
            for (int i = 0; i < 3; i++) {
                BufferedImage subImg = spriteSheet.getSubimage(0, i * frameHeight, frameWidth, frameHeight);

                // Lowered the scale multiplier here to fix the massive size
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
}