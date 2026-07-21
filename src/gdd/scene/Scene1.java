package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import static gdd.Global.*;
import gdd.SpawnDetails;
import gdd.powerup.*;
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

public class Scene1 extends JPanel {

    private int frame = 0;
    private List<PowerUp> powerups;
    private List<Enemy> enemies;
    private List<Asteroid> asteroids;
    private List<Explosion> explosions;
    private List<Shot> shots;
    private List<Alien1.Bomb> activeBombs;
    private Player player;

    final int BLOCKHEIGHT = 50, BLOCKWIDTH = 50;
    private int deaths = 0;
    private boolean inGame = true;
    private String message = "Stage 1 Clear!";

    private final Dimension d = new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
    private final Random randomizer = new Random();
    private Timer timer;
    private final Game game;
    private AudioPlayer audioPlayer;

    private final int[][] MAP = {
            {1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0},
            {0, 1, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1},
            {0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0}
    };

    private HashMap<Integer, SpawnDetails> spawnMap = new HashMap<>();

    public Scene1(Game game) {
        this.game = game;
        loadSpawnDetails();
    }

    private void loadSpawnDetails() {
        spawnMap.put(100, new SpawnDetails("PowerUp-SpeedUp", BOARD_WIDTH, 200));
        spawnMap.put(300, new SpawnDetails("PowerUp-MultiShot", BOARD_WIDTH, 300));
        spawnMap.put(800, new SpawnDetails("PowerUp-ThreeWay", BOARD_WIDTH, 250));

        spawnMap.put(200, new SpawnDetails("Alien1", BOARD_WIDTH, 100));
        spawnMap.put(400, new SpawnDetails("Alien2", BOARD_WIDTH, 300));

        for (int i = 500; i < 21400; i += 120) {
            int randomY = 50 + randomizer.nextInt(Math.max(1, BOARD_HEIGHT - 150));
            if (i % 360 == 0) spawnMap.put(i, new SpawnDetails("Alien2", BOARD_WIDTH, randomY));
            else spawnMap.put(i, new SpawnDetails("Alien1", BOARD_WIDTH, randomY));
        }

        for (int i = 600; i < 21400; i += 200) {
            int randomY = 50 + randomizer.nextInt(Math.max(1, BOARD_HEIGHT - 150));
            spawnMap.put(i + 50, new SpawnDetails("Asteroid", BOARD_WIDTH, randomY));
        }

        spawnMap.put(21600, new SpawnDetails("EndStage", 0, 0));
    }

    public void start() {
        addKeyListener(new TAdapter());
        setFocusable(true);
        requestFocusInWindow();
        setBackground(Color.black);

        timer = new Timer(1000 / 60, new GameCycle());
        timer.start();

        gameInit();
        try {
            audioPlayer = new AudioPlayer("src/audio/scene1.wav");
            audioPlayer.play();
        } catch (Exception e) {}
    }

    public void stop() {
        timer.stop();
        try { if (audioPlayer != null) audioPlayer.stop(); } catch (Exception e) {}
    }

    private void gameInit() {
        enemies = new ArrayList<>();
        asteroids = new ArrayList<>();
        powerups = new ArrayList<>();
        explosions = new ArrayList<>();
        shots = new ArrayList<>();
        activeBombs = new ArrayList<>();
        player = new Player();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.black);
        g.fillRect(0, 0, d.width, d.height);

        if (inGame) {
            drawMap(g);
            drawEntities(g);
            drawDashboard(g);
        } else {
            timer.stop();
            g.setColor(Color.white);
            var font = new Font("Helvetica", Font.BOLD, 18);
            g.setFont(font);
            g.drawString(message, (BOARD_WIDTH - getFontMetrics(font).stringWidth(message)) / 2, BOARD_HEIGHT / 2);
        }
        Toolkit.getDefaultToolkit().sync();
    }

    private void drawMap(Graphics g) {
        int scrollOffset = frame % BLOCKWIDTH;
        int baseCol = frame / BLOCKWIDTH;
        int colsNeeded = (BOARD_WIDTH / BLOCKWIDTH) + 2;

        g.setColor(Color.WHITE);
        for (int row = 0; row < MAP.length; row++) {
            for (int screenCol = 0; screenCol < colsNeeded; screenCol++) {
                int mapCol = (baseCol + screenCol) % MAP[row].length;
                if (MAP[row][mapCol] == 1) {
                    int x = BOARD_WIDTH - ((screenCol * BLOCKWIDTH) - scrollOffset);
                    int y = row * (BOARD_HEIGHT / MAP.length);
                    g.fillOval(x + 25 - 2, y + 25 - 2, 4, 4);
                    g.fillOval(x + 25 - 15, y + 25 - 10, 2, 2);
                    g.fillOval(x + 25 + 12, y + 25 - 8, 2, 2);
                }
            }
        }
    }

    private void drawDashboard(Graphics g) {
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Helvetica", Font.BOLD, 14));
        g.drawString("Kills: " + deaths + "/50", 10, 20);
        g.drawString("Speed: " + player.getSpeed(), 10, 40);
        g.drawString("Weapon: " + (player.isHasThreeWay() ? "3-WAY" : (player.getShotsUpgrade() + "/4 Multi")), 10, 60);
        g.setColor(Color.RED);
        g.drawString("LIVES: " + player.getLives(), 10, 80);
    }

    private void drawEntities(Graphics g) {
        for (Enemy e : enemies) { if (e.isVisible()) g.drawImage(e.getImage(), e.getX(), e.getY(), this); if (e.isDying()) e.die(); }
        for (Asteroid a : asteroids) { if (a.isVisible()) g.drawImage(a.getImage(), a.getX(), a.getY(), this); }
        for (PowerUp p : powerups) { if (p.isVisible()) g.drawImage(p.getImage(), p.getX(), p.getY(), this); if (p.isDying()) p.die(); }
        for (Shot s : shots) { if (s.isVisible()) g.drawImage(s.getImage(), s.getX(), s.getY(), this); }
        for (Alien1.Bomb b : activeBombs) { if (!b.isDestroyed()) g.drawImage(b.getImage(), b.getX(), b.getY(), this); }

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
    }

    private void update() {
        SpawnDetails sd = spawnMap.get(frame);
        if (sd != null) {
            switch (sd.type) {
                case "Alien1": enemies.add(new Alien1(sd.x, sd.y)); break;
                case "Alien2": enemies.add(new Alien2(sd.x, sd.y)); break;
                case "Asteroid": asteroids.add(new Asteroid(sd.x, sd.y)); break;
                case "PowerUp-SpeedUp": powerups.add(new SpeedUp(sd.x, sd.y)); break;
                case "PowerUp-MultiShot": powerups.add(new MultiShot(sd.x, sd.y)); break;
                case "PowerUp-ThreeWay": powerups.add(new ThreeWayUpgrade(sd.x, sd.y)); break;
                case "EndStage": inGame = false; stop(); game.loadScene2(); break;
            }
        }

        player.act();

        for (PowerUp p : powerups) {
            if (p.isVisible()) {
                p.act();
                if (p.collidesWith(player)) p.upgrade(player);
            }
        }

        for (Asteroid a : asteroids) {
            if (a.isVisible()) {
                a.act();
                if (player.isVisible() && a.collidesWith(player)) player.takeDamage();
            }
        }

        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                enemy.act(-1);
                if (player.isVisible() && enemy.collidesWith(player)) {
                    player.takeDamage();
                    enemy.setDying(true);
                    explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                }

                if (enemy instanceof Alien1) {
                    Alien1 a1 = (Alien1) enemy;
                    if (randomizer.nextInt(100) == 1 && a1.getBomb().isDestroyed()) {
                        a1.getBomb().setDestroyed(false);
                        a1.getBomb().setX(a1.getX());
                        a1.getBomb().setY(a1.getY());
                        activeBombs.add(a1.getBomb());
                    }
                }
            }
        }

        for (Alien1.Bomb bomb : activeBombs) {
            if (!bomb.isDestroyed()) {
                bomb.setX(bomb.getX() - 4);
                if (bomb.getX() < 0) bomb.setDestroyed(true);
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

                for (Asteroid a : asteroids) {
                    if (a.isVisible() && shot.collidesWith(a)) {
                        shot.die();
                        shotsToRemove.add(shot);
                    }
                }

                for (Enemy enemy : enemies) {
                    if (enemy.isVisible() && shot.collidesWith(enemy)) {
                        enemy.setDying(true);
                        explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                        deaths++;
                        shot.die();
                        shotsToRemove.add(shot);

                        // 50 Kill Progression
                        if (deaths >= 50) {
                            inGame = false;
                            stop();
                            game.loadScene2();
                            return;
                        }
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