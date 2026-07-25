package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import static gdd.Global.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class TitleScene extends JPanel {

    private int frame = 0;
    private Image image;
    private AudioPlayer audioPlayer;
    private final Dimension d = new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
    private Timer timer;
    private Game game;

    public TitleScene(Game game) {
        this.game = game;
    }

    public void start() {
        addKeyListener(new TAdapter());
        setFocusable(true);
        setBackground(Color.black);

        timer = new Timer(1000 / 60, new GameCycle());
        timer.start();

        initTitle();
        initAudio();
    }

    public void stop() {
        try {
            if (timer != null) timer.stop();
            if (audioPlayer != null) audioPlayer.stop();
        } catch (Exception e) {
            System.err.println("Error closing audio player.");
        }
    }

    private void initTitle() {
        var ii = new ImageIcon(IMG_TITLE);
        image = ii.getImage();
    }

    private void initAudio() {
        try {
            audioPlayer = new AudioPlayer("src/audio/title.wav");
            audioPlayer.play();
        } catch (Exception e) {}
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.black);
        g.fillRect(0, 0, d.width, d.height);
        g.drawImage(image, 0, -80, d.width, d.height, this);

        if (frame % 60 < 30) g.setColor(Color.blue);
        else g.setColor(Color.white);

        g.setFont(g.getFont().deriveFont(32f));
        String text = "Press SPACE to Start";
        g.drawString(text, (d.width - g.getFontMetrics().stringWidth(text)) / 2, 600);

        g.setColor(Color.gray);
        g.setFont(g.getFont().deriveFont(16f));
        g.drawString("Team Members: SWAN YI AUNG & PETANAN ARUNOTAYAKORN", 10, 650);

        Toolkit.getDefaultToolkit().sync();
    }

    private void doGameCycle() {
        frame++;
        repaint();
    }

    private class GameCycle implements ActionListener {
        @Override public void actionPerformed(ActionEvent e) { doGameCycle(); }
    }

    private class TAdapter extends KeyAdapter {
        @Override public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                stop();
                game.loadScene1();
            }
        }
    }
}