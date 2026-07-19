package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import static gdd.Global.*;
import gdd.SpawnDetails;
import gdd.powerup.PowerUp;
import gdd.powerup.SpeedUp;
import gdd.powerup.MultiShot;
import gdd.sprite.Alien1;
import gdd.sprite.Alien2;
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
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Scene1 extends JPanel {

    private int frame = 0;
    private List<PowerUp> powerups;
    private List<Enemy> enemies;
    private List<Explosion> explosions;
    private List<Shot> shots;
    private List<Alien1.Bomb> activeBombs;
    private Player player;

    final int BLOCKHEIGHT = 50;
    final int BLOCKWIDTH = 50;

    private int deaths = 0;
    private boolean inGame = true;
    private String message = "Stage 1 Clear!";

    private final Dimension d = new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
    private final Random randomizer = new Random();

    private Timer timer;
    private final Game game;

    private final int[][] MAP = {
            {1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0},
            {0, 1, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1},
            {0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0}
    };

    private HashMap<Integer, SpawnDetails> spawnMap = new HashMap<>();
    private AudioPlayer audioPlayer;

    public Scene1(Game game) {
        this.game = game;
        loadSpawnDetails();
    }

    private void initAudio() {
        try {
            audioPlayer = new AudioPlayer("src/audio/scene1.wav");
            audioPlayer.play();
        } catch (Exception e) {}
    }

    private void loadSpawnDetails() {
        spawnMap.put(100, new SpawnDetails("PowerUp-SpeedUp", BOARD_WIDTH, 200));
        spawnMap.put(300, new SpawnDetails("PowerUp-MultiShot", BOARD_WIDTH, 300));
        spawnMap.put(200, new SpawnDetails("Alien1", BOARD_WIDTH, 100));
        spawnMap.put(400, new SpawnDetails("Alien2", BOARD_WIDTH, 300));

        // 5 Minutes of gameplay (approx 18,000 frames)
        for (int i = 500; i < 17800; i += 120) {
            int randomY = 50 + randomizer.nextInt(Math.max(1, BOARD_HEIGHT - 150));
            if (i % 360 == 0) {
                spawnMap.put(i, new SpawnDetails("Alien2", BOARD_WIDTH, randomY));
            } else {
                spawnMap.put(i, new SpawnDetails("Alien1", BOARD_WIDTH, randomY));
            }
        }

        // Consistent Power-up spawns
        for (int i = 1800; i < 17800; i += 1800) {
            int randomY = 50 + randomizer.nextInt(Math.max(1, BOARD_HEIGHT - 150));
            if (i % 3600 == 0) {
                spawnMap.put(i, new SpawnDetails("PowerUp-MultiShot", BOARD_WIDTH, randomY));
            } else {
                spawnMap.put(i, new SpawnDetails("PowerUp-SpeedUp", BOARD_WIDTH, randomY));
            }
        }

        spawnMap.put(18000, new SpawnDetails("EndStage", 0, 0));
    }

    public void start() {
        addKeyListener(new TAdapter());
        setFocusable(true);
        requestFocusInWindow();
        setBackground(Color.black);

        timer = new Timer(1000 / 60, new GameCycle());
        timer.start();

        gameInit();
        initAudio();
    }

    public void stop() {
        timer.stop();
        try { if (audioPlayer != null) audioPlayer.stop(); } catch (Exception e) {}
    }

    private void gameInit() {
        enemies = new ArrayList<>();
        powerups = new ArrayList<>();
        explosions = new ArrayList<>();
        shots = new ArrayList<>();
        activeBombs = new ArrayList<>();
        player = new Player();
    }

    private void drawMap(Graphics g) {
        int scrollOffset = frame % BLOCKWIDTH;
        int baseCol = frame / BLOCKWIDTH;
        int colsNeeded = (BOARD_WIDTH / BLOCKWIDTH) + 2;

        for (int row = 0; row < MAP.length; row++) {
            for (int screenCol = 0; screenCol < colsNeeded; screenCol++) {
                int mapCol = (baseCol + screenCol) % MAP[row].length;
                int x = BOARD_WIDTH - ((screenCol * BLOCKWIDTH) - scrollOffset);
                int y = row * (BOARD_HEIGHT / MAP.length);

                if (MAP[row][mapCol] == 1) {
                    drawStarCluster(g, x, y, BLOCKWIDTH, BLOCKHEIGHT);
                }
            }
        }
    }

    private void drawStarCluster(Graphics g, int x, int y, int width, int height) {
        g.setColor(Color.WHITE);
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        g.fillOval(centerX - 2, centerY - 2, 4, 4);
        g.fillOval(centerX - 15, centerY - 10, 2, 2);
        g.fillOval(centerX + 12, centerY - 8, 2, 2);
    }

    private void drawDashboard(Graphics g) {
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Helvetica", Font.BOLD, 14));
        g.drawString("Score: " + deaths, 10, 20);
        g.drawString("Speed: " + player.getSpeed(), 10, 40);
        g.drawString("Shots Upgrade: " + player.getShotsUpgrade() + "/4", 10, 60);
    }

    private void drawEntities(Graphics g) {
        for (Enemy enemy : enemies) { if (enemy.isVisible()) g.drawImage(enemy.getImage(), enemy.getX(), enemy.getY(), this); if (enemy.isDying()) enemy.die(); }
        for (PowerUp p : powerups) { if (p.isVisible()) g.drawImage(p.getImage(), p.getX(), p.getY(), this); if (p.isDying()) p.die(); }
        if (player.isVisible()) g.drawImage(player.getImage(), player.getX(), player.getY(), this); else { player.die(); inGame = false; message = "Game Over"; }
        for (Shot shot : shots) { if (shot.isVisible()) g.drawImage(shot.getImage(), shot.getX(), shot.getY(), this); }
        for (Alien1.Bomb b : activeBombs) { if (!b.isDestroyed()) g.drawImage(b.getImage(), b.getX(), b.getY(), this); }
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
            drawMap(g);
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
        if (sd != null) {
            switch (sd.type) {
                case "Alien1": enemies.add(new Alien1(sd.x, sd.y)); break;
                case "Alien2": enemies.add(new Alien2(sd.x, sd.y)); break;
                case "PowerUp-SpeedUp": powerups.add(new SpeedUp(sd.x, sd.y)); break;
                case "PowerUp-MultiShot": powerups.add(new MultiShot(sd.x, sd.y)); break;
                case "EndStage": inGame = false; timer.stop(); game.loadScene2(); break;
            }
        }

        player.act();

        for (PowerUp p : powerups) {
            if (p.isVisible()) {
                p.act();
                if (p.collidesWith(player)) p.upgrade(player);
            }
        }

        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                enemy.act(-1);
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
                        enemy.setDying(true);
                        explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                        deaths++;
                        shot.die();
                        shotsToRemove.add(shot);
                    }
                }
                if (!shot.isVisible()) shotsToRemove.add(shot);
            }
        }
        shots.removeAll(shotsToRemove);
    }

    private void doGameCycle() {
        frame++;
        update();
        repaint();
    }

    private class GameCycle implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) { doGameCycle(); }
    }

    private class TAdapter extends KeyAdapter {
        @Override
        public void keyReleased(KeyEvent e) { player.keyReleased(e); }

        @Override
        public void keyPressed(KeyEvent e) {
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