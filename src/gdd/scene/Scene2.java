package gdd.scene;

import gdd.Game;
import static gdd.Global.*;
import gdd.SpawnDetails;
import gdd.sprite.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Scene2 extends JPanel {

    private int frame = 0;
    private List<Enemy> enemies;
    private List<Explosion> explosions;
    private List<Shot> shots;
    private List<Boss.Bomb> activeBombs;
    private Player player;

    private boolean inGame = true;
    private String message = "You Win!";
    private final Dimension d = new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
    private final Random randomizer = new Random();
    private Timer timer;
    private final Game game;
    private HashMap<Integer, SpawnDetails> spawnMap = new HashMap<>();

    public Scene2(Game game) {
        this.game = game;
        spawnMap.put(100, new SpawnDetails("Boss", BOARD_WIDTH + 100, BOARD_HEIGHT / 2));
    }

    public void start() {
        addKeyListener(new TAdapter());
        setFocusable(true);
        requestFocusInWindow();
        setBackground(Color.black);
        timer = new Timer(1000 / 60, new GameCycle());
        timer.start();

        enemies = new ArrayList<>();
        explosions = new ArrayList<>();
        shots = new ArrayList<>();
        activeBombs = new ArrayList<>();
        player = new Player();
    }

    public void stop() { timer.stop(); }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.black);
        g.fillRect(0, 0, d.width, d.height);

        if (inGame) {
            for (Enemy e : enemies) { if (e.isVisible()) g.drawImage(e.getImage(), e.getX(), e.getY(), this); if (e.isDying()) e.die(); }
            for (Shot s : shots) { if (s.isVisible()) g.drawImage(s.getImage(), s.getX(), s.getY(), this); }
            for (Boss.Bomb b : activeBombs) { if (!b.isDestroyed()) g.drawImage(b.getImage(), b.getX(), b.getY(), this); }

            List<Explosion> toRemove = new ArrayList<>();
            for (Explosion exp : explosions) {
                if (exp.isVisible()) {
                    g.drawImage(exp.getImage(), exp.getX(), exp.getY(), this);
                    exp.visibleCountDown();
                    if (!exp.isVisible()) toRemove.add(exp);
                }
            }
            explosions.removeAll(toRemove);

            if (player.isVisible()) {
                if (!player.isInvulnerable() || frame % 10 < 5) g.drawImage(player.getImage(), player.getX(), player.getY(), this);
            } else {
                inGame = false;
                message = "Game Over";
            }

            g.setColor(Color.YELLOW);
            g.setFont(new Font("Helvetica", Font.BOLD, 14));
            g.drawString("Speed: " + player.getSpeed(), 10, 20);
            g.drawString("Weapon: " + (player.isHasThreeWay() ? "3-WAY" : (player.getShotsUpgrade() + "/4 Multi")), 10, 40);
            g.setColor(Color.RED);
            g.drawString("LIVES: " + player.getLives(), 10, 60);

            for (Enemy e : enemies) {
                if (e instanceof Boss) g.drawString("BOSS HP: " + ((Boss)e).getHp(), BOARD_WIDTH / 2 - 50, 20);
            }
        } else {
            timer.stop();
            g.setColor(Color.white);
            var font = new Font("Helvetica", Font.BOLD, 18);
            g.setFont(font);
            g.drawString(message, (BOARD_WIDTH - getFontMetrics(font).stringWidth(message)) / 2, BOARD_HEIGHT / 2);
        }
        Toolkit.getDefaultToolkit().sync();
    }

    private void update() {
        SpawnDetails sd = spawnMap.get(frame);
        if (sd != null && sd.type.equals("Boss")) enemies.add(new Boss(sd.x, sd.y));

        player.act();

        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                enemy.act(-1);
                if (player.isVisible() && enemy.collidesWith(player)) player.takeDamage();

                if (enemy instanceof Boss) {
                    Boss boss = (Boss) enemy;
                    if (randomizer.nextInt(180) == 1 && boss.getX() <= BOARD_WIDTH - 200) {
                        activeBombs.addAll(boss.fireMultipleShots());
                    }
                }
            }
        }

        for (Boss.Bomb bomb : activeBombs) {
            if (!bomb.isDestroyed()) {
                // Tell the bomb to use the custom math we wrote in Boss.java!
                bomb.act();

                // Destroy the bomb if it flies off ANY side of the screen
                if (bomb.getX() < 0 || bomb.getX() > BOARD_WIDTH ||
                        bomb.getY() < 0 || bomb.getY() > BOARD_HEIGHT) {
                    bomb.setDestroyed(true);
                }

                // Keep your existing collision logic
                if (player.isVisible() && bomb.getX() >= player.getX() && bomb.getX() <= (player.getX() + 32) &&
                        bomb.getY() >= player.getY() && bomb.getY() <= (player.getY() + 32)) {
                    player.takeDamage();
                    bomb.setDestroyed(true);
                }
            }
        }

        List<Shot> shotsToRemove = new ArrayList<>();
        for (Shot shot : shots) {
            if (shot.isVisible()) {
                shot.act();
                for (Enemy enemy : enemies) {
                    if (enemy.isVisible() && shot.collidesWith(enemy)) {
                        if (enemy instanceof Boss) {
                            Boss boss = (Boss) enemy;
                            boss.takeDamage();
                            if (boss.isDying()) {
                                explosions.add(new Explosion(boss.getX(), boss.getY()));
                                inGame = false;
                                message = "You Beat The Boss!";
                            }
                        }
                        shot.die();
                        shotsToRemove.add(shot);
                    }
                }
                if (!shot.isVisible()) shotsToRemove.add(shot);
            }
        }
        shots.removeAll(shotsToRemove);
    }

    private class GameCycle implements ActionListener { @Override public void actionPerformed(ActionEvent e) { frame++; update(); repaint(); } }

    private class TAdapter extends KeyAdapter {
        @Override public void keyReleased(KeyEvent e) { player.keyReleased(e); }
        @Override public void keyPressed(KeyEvent e) {
            player.keyPressed(e);
            if (e.getKeyCode() == KeyEvent.VK_SPACE && inGame) {
                if (player.isHasThreeWay()) {
                    shots.add(new Shot(player.getX(), player.getY(), 0));
                    shots.add(new Shot(player.getX(), player.getY(), -3));
                    shots.add(new Shot(player.getX(), player.getY(), 3));
                } else {
                    int shotsCount = player.getShotsUpgrade();
                    for (int i = 0; i < shotsCount; i++) {
                        int offset = (i - (shotsCount / 2)) * 15;
                        shots.add(new Shot(player.getX(), player.getY() + offset, 0));
                    }
                }
            }
        }
    }
}