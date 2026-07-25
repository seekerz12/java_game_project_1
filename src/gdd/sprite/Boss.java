package gdd.sprite;

import static gdd.Global.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Boss extends Enemy {

    private int hp = 100;
    private int yDirection = 3;
    private int stopX;
    private int bossWidth;
    private int bossHeight;

    private Image[] frames;
    private int currentFrame = 0;
    private int animationTick = 0;
    private final int ANIMATION_SPEED = 10;

    // Standard random initialization
    private Random randomGenerator = new Random();

    public Boss(int x, int y) {
        super(x, y);
        initBoss();
    }

    public int getHealth() {
        return this.hp;
    }


    private void initBoss() {
        try {
            BufferedImage spriteSheet = ImageIO.read(new File(IMG_BOSS));
            int frameWidth = spriteSheet.getWidth();
            int frameHeight = spriteSheet.getHeight() / 3;

            int cropTop = 2;
            int cropBottom = 8;
            int safeHeight = frameHeight - cropTop - cropBottom;

            bossWidth = frameWidth * SCALE_FACTOR;
            bossHeight = safeHeight * SCALE_FACTOR;

            stopX = BOARD_WIDTH - bossWidth - 50;

            frames = new Image[3];
            for (int i = 0; i < 3; i++) {
                int yStart = (i * frameHeight) + cropTop;
                BufferedImage subImg = spriteSheet.getSubimage(0, yStart, frameWidth, safeHeight);
                frames[i] = subImg.getScaledInstance(bossWidth, bossHeight, java.awt.Image.SCALE_SMOOTH);
            }

            setImage(frames[0]);
        } catch (Exception e) {
            System.err.println("Error loading boss1 spritesheet: " + e.getMessage());
        }
    }

    @Override
    public void act(int direction) {
        if (this.x > stopX) {
            this.x -= 2;
        } else {
            this.y += yDirection;

            if (this.y <= 0 || this.y >= BOARD_HEIGHT - bossHeight - 40) {
                yDirection *= -1;
            }
        }

        animationTick++;
        if (animationTick >= ANIMATION_SPEED) {
            animationTick = 0;
            currentFrame = (currentFrame + 1) % frames.length;
            setImage(frames[currentFrame]);
        }
    }

    public void takeDamage() {
        hp--;
        if (hp <= 0) this.setDying(true);
    }

    public int getHp() { return hp; }

    // --- COMPLEX RANDOMIZED ATTACK SYSTEM ---
    public List<Bomb> fireMultipleShots() {
        List<Bomb> spread = new ArrayList<>();

        // Pick exactly 0, 1, or 2
        int attackPattern = randomGenerator.nextInt(3);
        int centerY = this.y + (bossHeight / 2);

        if (attackPattern == 0) {
            System.out.println("[BOSS] Firing Pattern 0: The Rain Attack");
            int raindrops = 10 + randomGenerator.nextInt(5);

            for (int i = 0; i < raindrops; i++) {
                // Keep X within safe bounds so they don't instantly wipe
                int spawnX = 20 + randomGenerator.nextInt(BOARD_WIDTH - 100);

                // Spawn safely inside the top edge
                int spawnY = 20 + randomGenerator.nextInt(30);

                double fallSpeed = 3.0 + randomGenerator.nextDouble() * 2.5;
                spread.add(new Bomb(spawnX, spawnY, 0.0, fallSpeed));
            }

        } else if (attackPattern == 1) {
            System.out.println("[BOSS] Firing Pattern 1: The Circle Attack");
            int numBullets = 10;
            double speed = 4.0;
            for (int i = 0; i < numBullets; i++) {
                double angle = Math.toRadians((360.0 / numBullets) * i);
                double dx = Math.cos(angle) * speed;
                double dy = Math.sin(angle) * speed;
                spread.add(new Bomb(this.x, centerY, dx, dy));
            }

        } else {
            System.out.println("[BOSS] Firing Pattern 2: The Shotgun Burst");
            spread.add(new Bomb(this.x, centerY, -6.0, 0.0));
            spread.add(new Bomb(this.x, centerY - 20, -5.0, -1.5));
            spread.add(new Bomb(this.x, centerY + 20, -5.0, 1.5));
            spread.add(new Bomb(this.x, centerY - 40, -4.0, -3.0));
            spread.add(new Bomb(this.x, centerY + 40, -4.0, 3.0));
        }

        return spread;
    }

    // --- UPGRADED BOMB CLASS ---
    public static class Bomb extends Sprite {
        private boolean destroyed;
        private double dx;
        private double dy;
        private double exactX;
        private double exactY;

        public Bomb(int x, int y) {
            this(x, y, -4.0, 0.0);
        }

        public Bomb(int x, int y, double dx, double dy) {
            setDestroyed(false);
            this.x = x;
            this.y = y;
            this.exactX = x;
            this.exactY = y;
            this.dx = dx;
            this.dy = dy;
            var ii = new ImageIcon(IMG_BOMB);
            setImage(ii.getImage());
        }

        public void setDestroyed(boolean destroyed) { this.destroyed = destroyed; }
        public boolean isDestroyed() { return destroyed; }

        @Override
        public void act() {
            exactX += dx;
            exactY += dy;
            this.x = (int) exactX;
            this.y = (int) exactY;
        }
        @Override
        public boolean hasThreeWay() {
            return false;
        }
    }

}