package gdd.sprite;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Asteroid extends Sprite {
    private int speed;
    private int diameter = 40; // Size of the asteroid

    public Asteroid(int x, int y) {
        this.x = x;
        this.y = y;
        this.speed = 3 + new Random().nextInt(3);

        // 1. Create a blank, transparent image in memory
        BufferedImage img = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        // 2. Draw the base circle for the asteroid
        g2d.setColor(Color.GRAY);
        g2d.fillOval(0, 0, diameter, diameter);

        // 3. Optional: Draw a few smaller dark circles to look like craters!
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillOval(6, 10, 10, 10);
        g2d.fillOval(20, 22, 12, 12);
        g2d.fillOval(24, 6, 8, 8);

        // 4. Clean up the graphics tool
        g2d.dispose();

        // 5. Set our newly drawn image as the sprite's image
        setImage(img);
    }

    @Override
    public void act() {
        this.x -= speed;
        if (this.x < -50) {
            this.die();
        }
    }
}