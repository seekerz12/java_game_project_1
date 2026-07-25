package gdd;

import gdd.scene.Scene1;
import gdd.scene.Scene2;
import gdd.scene.TitleScene;
import static gdd.Global.*;
import javax.swing.JFrame;
import java.awt.EventQueue;

public class Game extends JFrame {

    public Game() {
        initUI();
    }

    private void initUI() {
        // Configure the main game window
        setTitle("Better Call Swan Presents Defense Against Aliens");
        setSize(BOARD_WIDTH, BOARD_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        // Command the window to load the Title Scene first
        loadTitleScene();
    }

    public void loadTitleScene() {
        getContentPane().removeAll(); // Clear current screen
        TitleScene title = new TitleScene(this);
        add(title);
        revalidate(); // Refresh the frame to show the new panel
        repaint();
        title.start(); // Trigger the scene's start method
    }

    public void loadScene1() {
        getContentPane().removeAll();
        Scene1 scene1 = new Scene1(this);
        add(scene1);
        revalidate();
        repaint();
        scene1.start();
    }

    public void loadScene2(int lives, int speed, int shotsUpgrade, boolean hasThreeWay) {
        this.getContentPane().removeAll();
        Scene2 scene2 = new Scene2(this, lives, speed, shotsUpgrade, hasThreeWay);
        add(scene2);
        revalidate();
        repaint();
        scene2.start();
    }
}