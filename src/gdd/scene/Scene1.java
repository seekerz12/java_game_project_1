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
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Scene1 extends JPanel {

    // Pacing & Transition Variables
    private final int BOSS_THRESHOLD = 50;
    private boolean isCinematicMode = false;
    private boolean hasLoadedScene2 = false;

    // Fade Transition Variables
    private boolean isTransitioning = false;
    private float fadeAlpha = 0.0f;
    private int lastCarePackage = 0;
    private int frame = 0;
    private List<PowerUp> powerUpList = new ArrayList<>();
    private List<Point> distantLayer = new ArrayList<>();
    private List<Point> closeLayer = new ArrayList<>();
    private List<PowerUp> powerups;
    private List<Enemy> enemies;
    private List<Asteroid> asteroids;
    private List<Explosion> explosions;
    private List<Shot> shots;
    private List<Alien1.Bomb> activeBombs;
    private List<Alien2.WaveBomb> activeAlien2Bombs;
    private Player player;
    private int lastSpawnY = -100;
    private boolean endMusicPlayed = false;

    private boolean spacePressed = false;
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
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
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

        // 2. The Procedural Infinite Script
        int powerUpCycle = 0;

        // Starts at frame 1200 and schedules drops all the way up to frame 20,000 (over 5 minutes of gameplay)
        // Drops a new item every 400 frames (roughly every 6.5 seconds)
        for (int futureFrame = 1200; futureFrame <= 20000; futureFrame += 400) {

            // Randomize the Y position so the player has to move to catch it
            int randomY = 50 + randomizer.nextInt(Math.max(1, BOARD_HEIGHT - 100));

            // Cycle evenly through the three power-ups
            if (powerUpCycle == 0) {
                spawnMap.put(futureFrame, new SpawnDetails("PowerUp-SpeedUp", BOARD_WIDTH, randomY));
            } else if (powerUpCycle == 1) {
                spawnMap.put(futureFrame, new SpawnDetails("PowerUp-MultiShot", BOARD_WIDTH, randomY));
            } else {
                spawnMap.put(futureFrame, new SpawnDetails("PowerUp-ThreeWay", BOARD_WIDTH, randomY));
            }

            // Increment the cycle and reset it when it hits 3
            powerUpCycle++;
            if (powerUpCycle > 2) {
                powerUpCycle = 0;
            }
        }
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
        } catch (Exception e) {
        }
    }

    public void stop() {
        timer.stop();
        try {
            if (audioPlayer != null) audioPlayer.stop();
        } catch (Exception e) {
        }
    }

    private void gameInit() {
        enemies = new ArrayList<>();
        asteroids = new ArrayList<>();
        powerups = new ArrayList<>();
        explosions = new ArrayList<>();
        shots = new ArrayList<>();
        activeBombs = new ArrayList<>();
        activeAlien2Bombs = new ArrayList<>();
        player = new Player();
        for (int i = 0; i < 100; i++) {
            distantLayer.add(new Point(randomizer.nextInt(BOARD_WIDTH), randomizer.nextInt(BOARD_HEIGHT)));
        }
        for (int i = 0; i < 50; i++) {
            closeLayer.add(new Point(randomizer.nextInt(BOARD_WIDTH), randomizer.nextInt(BOARD_HEIGHT)));
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g.setColor(Color.black);
        g.fillRect(0, 0, d.width, d.height);

        // Draw distant layer
        g.setColor(Color.DARK_GRAY);
        for (Point p : distantLayer) g.fillRect(p.x, p.y, 1, 1);

        // Draw close layer
        g.setColor(Color.WHITE);
        for (Point p : closeLayer) g.fillRect(p.x, p.y, 2, 2);

        if (inGame) {
            drawMap(g);
            if (!isTransitioning) {
                float intensity = Math.min(0.4f, (float) deaths / BOSS_THRESHOLD * 0.4f);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, intensity));
                g2d.setColor(new Color(200, 0, 0)); // Deep blood red
                g2d.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)); // Reset alpha
            }
            drawEntities(g);
            drawDashboard(g);
        } else {
            g.setColor(Color.white);
            var font = new Font("Helvetica", Font.BOLD, 18);
            g.setFont(font);
            g.drawString(message, (BOARD_WIDTH - getFontMetrics(font).stringWidth(message)) / 2, BOARD_HEIGHT / 2);
        }

        // --- CINEMATIC FADE TO BLACK TRANSITION ---
        if (isTransitioning) {
            fadeAlpha += 0.01f;

            if (fadeAlpha >= 1.0f && !hasLoadedScene2) {
                fadeAlpha = 1.0f;
                hasLoadedScene2 = true;
                inGame = false;
                stop();

                game.loadScene2(player.getLives(), player.getSpeed(), player.getShotsUpgrade(), player.hasThreeWay());
            }

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha));
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
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
        g.setColor(new Color(20, 20, 20, 220));
        int[] xPoints = {0, 250, 190, 0};
        int[] yPoints = {0, 0, 130, 130};
        g.fillPolygon(xPoints, yPoints, 4);

        g.setColor(Color.YELLOW);
        g.drawLine(250, 0, 190, 130);
        g.drawLine(190, 130, 0, 130);

        g.setFont(new Font("Courier New", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        g.drawString("PLAYER", 15, 25);

        g.setColor(Color.LIGHT_GRAY);
        g.drawString("SYS.SPEED: " + player.getSpeed(), 15, 50);

        // --- WEAPON SYSTEM HUD LOGIC ---
        // Determine the text based on the player's active boolean flags
        String activeArmament = "DEFAULT BLASTER";

        // We check the most powerful upgrades first
        if (player.hasThreeWay()) { // Replace with your exact boolean check for Three-Way
            activeArmament = "THREE-WAY SPREAD";
        } else if (player.hasMultiShot()) { // Replace with your exact boolean check for Multi-Shot
            activeArmament = "TWIN BLASTER";
        }

        // Draw the dynamic string to the screen
        // (Adjust the X and Y coordinates to match your current HUD layout)
        g.drawString( activeArmament, 15, 60);

        g.setFont(new Font("Helvetica", Font.BOLD, 18));
        g.setColor(Color.YELLOW);

        g.drawString("TARGETS: " + deaths + "/" + BOSS_THRESHOLD, 15, 100);

        if (player.getLives() == 1 && frame % 20 < 10) {
            g.setColor(Color.RED);
        } else {
            g.setColor(new Color(255, 50, 50));
        }
        g.drawString("HEALTH: " + player.getLives(), 15, 125);
    }

    private void drawEntities(Graphics g) {
        for (Enemy e : enemies) {
            if (e.isVisible()) g.drawImage(e.getImage(), e.getX(), e.getY(), this);
            if (e.isDying()) e.die();
        }
        for (Asteroid a : asteroids) {
            if (a.isVisible()) g.drawImage(a.getImage(), a.getX(), a.getY(), this);
        }
        for (PowerUp p : powerups) {
            if (p.isVisible()) g.drawImage(p.getImage(), p.getX(), p.getY(), this);
            if (p.isDying()) p.die();
        }
        for (Shot s : shots) {
            if (s.isVisible()) g.drawImage(s.getImage(), s.getX(), s.getY(), this);
        }
        for (Alien1.Bomb b : activeBombs) {
            if (!b.isDestroyed()) g.drawImage(b.getImage(), b.getX(), b.getY(), this);
        }
        for (Alien2.WaveBomb b : activeAlien2Bombs) {
            if (!b.isDestroyed()) g.drawImage(b.getImage(), b.getX(), b.getY(), this);
        }

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
            if (!player.isInvulnerable() || frame % 10 < 5)
                g.drawImage(player.getImage(), player.getX(), player.getY(), this);
        } else {
            inGame = false;
            message = "Game Over";
        }
    }

    private void update() {

        // Inside your update() method, right when the player dies
        if (!inGame && !endMusicPlayed) {

            // 1. Cut the main level music (if you have one playing in Scene 1)
            try {
                if (audioPlayer != null) {
                    audioPlayer.stop();
                }
            } catch (Exception e) {}

            // 2. Play the Game Over track exactly once
            try {
                AudioPlayer defeatMusic = new AudioPlayer("src/audio/defeat.wav");
                defeatMusic.play();
            } catch (Exception e) {}

            // 3. Lock the flag so it doesn't loop infinitely
            endMusicPlayed = true;
        }

        int tensionSpeed = (deaths / 2);

        for (Point p : distantLayer) {
            p.x -= (1 + tensionSpeed);
            if (p.x < 0) p.x = BOARD_WIDTH;
        }
        for (Point p : closeLayer) {
            // The close layer moves twice as fast to create intense depth distortion
            p.x -= (3 + (tensionSpeed * 2));
            if (p.x < 0) p.x = BOARD_WIDTH;
        }

        for (Point p : distantLayer) {
            p.x -= (1 + tensionSpeed);
            if (p.x < 0) p.x = BOARD_WIDTH;
        }
        for (Point p : closeLayer) {
            p.x -= (3 + (tensionSpeed * 2));
            if (p.x < 0) p.x = BOARD_WIDTH;
        }

        // 1. PACING LOGIC
        if (deaths >= BOSS_THRESHOLD) {
            isCinematicMode = true;
        }

        // Trigger transition only when all on-screen threats are cleared
        if (isCinematicMode) {
            boolean threatsRemain = false;

            for (Enemy e : enemies) {
                if (e.isVisible() && e.getX() > -50) {
                    threatsRemain = true;
                }
            }
            for (Asteroid a : asteroids) {
                if (a.isVisible() && a.getX() > -50) {
                    threatsRemain = true;
                }
            }
            if (!activeBombs.isEmpty() || !activeAlien2Bombs.isEmpty()) {
                threatsRemain = true;
            }
            if (!threatsRemain) {
                isTransitioning = true;
            }
        }

        // 2. ONLY SPAWN ENEMIES IF WE ARE NOT IN CINEMATIC MODE
        if (!isCinematicMode) {
            int enemyRate = Math.max(30, 150 - (deaths * 5));
            int asteroidRate = Math.max(50, 180 - (deaths * 4));

            if (randomizer.nextInt(enemyRate) == 0) {
                int randomY;
                do {
                    randomY = 50 + randomizer.nextInt(Math.max(1, BOARD_HEIGHT - 150));
                } while (Math.abs(randomY - lastSpawnY) < 60);

                lastSpawnY = randomY;
                int spawnX = BOARD_WIDTH + 100;

                if (randomizer.nextInt(100) < 30) {
                    enemies.add(new Alien2(spawnX, randomY));
                } else {
                    enemies.add(new Alien1(spawnX, randomY));
                }
            }

            if (randomizer.nextInt(asteroidRate) == 0) {
                int randomY;
                do {
                    randomY = 50 + randomizer.nextInt(Math.max(1, BOARD_HEIGHT - 150));
                } while (Math.abs(randomY - lastSpawnY) < 60);

                lastSpawnY = randomY;
                int spawnX = BOARD_WIDTH + 100;
                asteroids.add(new Asteroid(spawnX, randomY));
            }

        }

        // Dynamic PowerUp Collision and Movement
        for (int i = 0; i < powerUpList.size(); i++) {
            PowerUp item = powerUpList.get(i);
            if (player.getBounds().intersects(item.getBounds())) {
                item.upgrade(player);
            }
            if (!item.isVisible() || item.getX() < 0) {
                powerUpList.remove(i);
                i--;
            }
        }
        for (PowerUp p : powerUpList) {
            p.act();
        }

        // Static Map Spawns
        SpawnDetails sd = spawnMap.get(frame);
        if (sd != null) {
            switch (sd.type) {
                case "PowerUp-SpeedUp":
                    powerups.add(new SpeedUp(sd.x, sd.y));
                    break;
                case "PowerUp-MultiShot":
                    powerups.add(new MultiShot(sd.x, sd.y));
                    break;
                case "PowerUp-ThreeWay":
                    powerups.add(new ThreeWayUpgrade(sd.x, sd.y));
                    break;
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
                // FIXED: Wrapped the audio logic inside the invulnerability check
                if (player.isVisible() && !player.isInvulnerable() && a.collidesWith(player)) {
                    player.takeDamage();
                    playSFX("player_hit");
                }
            }
        }

        List<Enemy> deadEnemies = new ArrayList<>();
        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                enemy.act(-1);

                if (enemy.getX() < -100) {
                    enemy.die(); // Sets visible to false so they get cleaned up
                    continue;    // Instantly skips the rest of the loop so they can't shoot!
                }

                // FIXED: Added !enemy.isDying() and deaths++
                if (player.isVisible() && !player.isInvulnerable() && !enemy.isDying() && enemy.collidesWith(player)) {
                    player.takeDamage();
                    playSFX("player_hit");
                    enemy.setDying(true);
                    explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                    deaths++; // You now get a point for destroying them with your ship!
                }

                // --- ALIEN 1 FIRING LOGIC ---
                if (enemy instanceof Alien1) {
                    Alien1 a1 = (Alien1) enemy;
                    if (randomizer.nextInt(40) == 1 && a1.getBomb().isDestroyed()) {
                        a1.getBomb().setDestroyed(false);

                        // Centered X and perfectly centered Y!
                        int centerX = a1.getX() + (a1.getImage().getWidth(null) / 2) - (a1.getBomb().getImage().getWidth(null) / 2);
                        int centerY = a1.getY() + (a1.getImage().getHeight(null) / 2) - (a1.getBomb().getImage().getHeight(null) / 2);

                        a1.getBomb().setX(centerX);
                        a1.getBomb().setY(centerY);

                        activeBombs.add(a1.getBomb());

                            playSFX("enemies_shoot");

                    }
                }

                // --- ALIEN 2 FIRING LOGIC ---
                if (enemy instanceof Alien2) {
                    Alien2 a2 = (Alien2) enemy;
                    if (randomizer.nextInt(60) == 1 && a2.getBomb().isDestroyed()) {
                        a2.getBomb().setDestroyed(false);

                        // Centered X and perfectly centered Y!
                        int centerX = a2.getX() + (a2.getImage().getWidth(null) / 2) - (a2.getBomb().getImage().getWidth(null) / 2);
                        int centerY = a2.getY() + (a2.getImage().getHeight(null) / 2) - (a2.getBomb().getImage().getHeight(null) / 2);

                        a2.getBomb().setX(centerX);
                        a2.getBomb().setBombStartY(centerY);

                        activeAlien2Bombs.add(a2.getBomb());

                        if (a2.getX() <= BOARD_WIDTH) {
                            playSFX("enemies_shoot");
                        }
                    }
                }
            } else {
                deadEnemies.add(enemy);
            }
        }
        enemies.removeAll(deadEnemies);

        for (Alien1.Bomb bomb : activeBombs) {
            if (!bomb.isDestroyed()) {
                bomb.setX(bomb.getX() - 6);
                if (bomb.getX() < 0) bomb.setDestroyed(true);
                // FIXED: Shielded the audio logic for Alien 1 bombs
                if (player.isVisible() && !player.isInvulnerable() && bomb.getX() >= player.getX() && bomb.getX() <= (player.getX() + 32) &&
                        bomb.getY() >= player.getY() && bomb.getY() <= (player.getY() + 32)) {
                    player.takeDamage();
                    playSFX("player_hit");
                    bomb.setDestroyed(true);
                }
            }
        }

        activeBombs.removeIf(Alien1.Bomb::isDestroyed);

        for (Alien2.WaveBomb bomb : activeAlien2Bombs) {
            if (!bomb.isDestroyed()) {
                bomb.act();
                if (bomb.getX() < 0) bomb.setDestroyed(true);
                // FIXED: Shielded the audio logic for Alien 2 bombs
                if (player.isVisible() && !player.isInvulnerable() && bomb.getX() >= player.getX() && bomb.getX() <= (player.getX() + 32) &&
                        bomb.getY() >= player.getY() && bomb.getY() <= (player.getY() + 32)) {
                    player.takeDamage();
                    playSFX("player_hit");
                    bomb.setDestroyed(true);
                }
            }
        }
        activeAlien2Bombs.removeIf(Alien2.WaveBomb::isDestroyed);

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
                    if (enemy.isVisible() && !enemy.isDying() && shot.collidesWith(enemy))  {
                        enemy.setDying(true);
                        playSFX("enemies_dead");
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

    private class GameCycle implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            frame++;
            update();
            repaint();
        }
    }

    private class TAdapter extends KeyAdapter {
        @Override
        public void keyReleased(KeyEvent e) {
            player.keyReleased(e);

            // Unlock the spacebar when the player physically lets go of the key
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                spacePressed = false;
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            player.keyPressed(e);

            // Only fire if the spacebar is pressed AND it wasn't already being held down
            if (e.getKeyCode() == KeyEvent.VK_SPACE && inGame && !spacePressed) {
                spacePressed = true; // Lock it so holding down the key does nothing
                player.fireWeapon(shots);

                playSFX("player_shot");
            }
        }
    }
    // --- LIGHTWEIGHT AUDIO FOLEY DIRECTOR ---
    private void playSFX(String filename) {
        if (!inGame) return;
        new Thread(() -> {
            try {
                java.io.File soundFile = new java.io.File("src/audio/" + filename + ".wav");
                javax.sound.sampled.AudioInputStream audioIn = javax.sound.sampled.AudioSystem.getAudioInputStream(soundFile);
                javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
                clip.addLineListener(event -> {
                    if (event.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            } catch (Exception e) {}
        }).start();
    }
}