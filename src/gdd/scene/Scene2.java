package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import static gdd.Global.*;
import gdd.SpawnDetails;
import gdd.sprite.Boss;
import gdd.sprite.Enemy;
import gdd.sprite.Explosion;
import gdd.sprite.Player;
import gdd.sprite.Shot;
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

    private int deaths = 0;
    private boolean inGame = true;
    private String message = "You Win!";

    private final Dimension d = new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
    private final Random randomizer = new Random();
    private Timer timer;
    private final Game game;
    private HashMap<Integer, SpawnDetails> spawnMap = new HashMap<>();

    public Scene2(Game game) {
        this.game = game;
        loadSpawnDetails();
    }

    private void loadSpawnDetails() {
        spawnMap.put(100, new SpawnDetails("Boss", BOARD_WIDTH + 100, BOARD_HEIGHT / 2));
    }

    public void start() {
        addKeyListener(new TAdapter());
        setFocusable(true);
        requestFocusInWindow();
        setBackground(Color.black);

        timer = new Timer(1000 / 60, new GameCycle());
        timer.start();
        gameInit();
    }

    public void stop() {
        timer.stop();
    }

    private void gameInit() {
        enemies = new ArrayList<>();
        explosions = new ArrayList<>();
        shots = new ArrayList<>();
        activeBombs = new ArrayList<>();
        player = new Player();
    }

    private void drawDashboard(Graphics g) {
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Helvetica", Font.BOLD, 14));
        g.drawString("Speed: " + player.getSpeed(), 10, 20);
        g.drawString("Shots Upgrade: " + player.getShotsUpgrade() + "/4", 10, 40);

        // Display Boss HP if spawned
        for (Enemy e : enemies) {
            if (e instanceof Boss) {
                g.setColor(Color.RED);
                g.drawString("BOSS HP: " + ((Boss)e).getHp(), BOARD_WIDTH / 2 - 50, 20);
            }
        }
    }

    private void drawEntities(Graphics g) {
        for (Enemy enemy : enemies) { if (enemy.isVisible()) g.drawImage(enemy.getImage(), enemy.getX(), enemy.getY(), this); if (enemy.isDying()) enemy.die(); }
        if (player.isVisible()) g.drawImage(player.getImage(), player.getX(), player.getY(), this); else { player.die(); inGame = false; message = "Game Over"; }
        for (Shot shot : shots) { if (shot.isVisible()) g.drawImage(shot.getImage(), shot.getX(), shot.getY(), this); }
        for (Boss.Bomb b : activeBombs) { if (!b.isDestroyed()) g.drawImage(b.getImage(), b.getX(), b.getY(), this); }
    }

    private void drawExplosions(Graphics g) {
        List<Explosion> toRemove = new ArrayList<>();
        for (Explosion exp : explosions) {
            if (exp.isVisible()) {
                g.drawImage(exp.getImage(), exp.getX(), exp.getY(), this);
                exp.visibleCountDown();
                if (!exp.isVisible()) toRemove.add(exp);
            }
        }
        explosions.removeAll(toRemove);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }

    private void doDrawing(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, d.width, d.height);

        if (inGame) {
            drawEntities(g);
            drawExplosions(g);
            drawDashboard(g);
        } else {
            timer.stop();
            gameOver(g);
        }
        Toolkit.getDefaultToolkit().sync();
    }

    private void gameOver(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        g.setColor(Color.white);
        var font = new Font("Helvetica", Font.BOLD, 18);
        g.setFont(font);
        g.drawString(message, (BOARD_WIDTH - getFontMetrics(font).stringWidth(message)) / 2, BOARD_HEIGHT / 2);
    }

    private void update() {
        SpawnDetails sd = spawnMap.get(frame);
        if (sd != null && sd.type.equals("Boss")) {
            enemies.add(new Boss(sd.x, sd.y));
        }

        player.act();

        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                enemy.act(-1);
                if (enemy instanceof Boss) {
                    Boss boss = (Boss) enemy;
                    if (randomizer.nextInt(60) == 1 && boss.getX() <= BOARD_WIDTH - 200) {
                        activeBombs.addAll(boss.fireMultipleShots());
                    }
                }
            }
        }

        for (Boss.Bomb bomb : activeBombs) {
            if (!bomb.isDestroyed()) {
                bomb.setX(bomb.getX() - 5);
                if (bomb.getX() < 0) bomb.setDestroyed(true);

                if (player.isVisible() && bomb.getX() >= player.getX() && bomb.getX() <= (player.getX() + 32) &&
                        bomb.getY() >= player.getY() && bomb.getY() <= (player.getY() + 32)) {
                    player.setDying(true);
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
                                deaths++;
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

    private void doGameCycle() { frame++; update(); repaint(); }
    private class GameCycle implements ActionListener { @Override public void actionPerformed(ActionEvent e) { doGameCycle(); } }
    private class TAdapter extends KeyAdapter {
        @Override public void keyReleased(KeyEvent e) { player.keyReleased(e); }
        @Override public void keyPressed(KeyEvent e) {
            player.keyPressed(e);
            if (e.getKeyCode() == KeyEvent.VK_SPACE && inGame) {
                int shotsCount = player.getShotsUpgrade();
                for (int i = 0; i < shotsCount; i++) {
                    int offset = (i - (shotsCount / 2)) * 15;
                    shots.add(new Shot(player.getX(), player.getY() + offset));
                }
            }
        }
    }
}