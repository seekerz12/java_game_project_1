package gdd.scene;

import gdd.Game;
import static gdd.Global.*;
import gdd.SpawnDetails;
import gdd.sprite.*;

import java.awt.*;
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
import gdd.AudioPlayer;

public class Scene2 extends JPanel {

    private Boss boss;
    private boolean endMusicPlayed = false;
    private AudioPlayer audioPlayer;
    private int frame = 0;
    private List<Enemy> enemies;
    private List<Explosion> explosions;
    private List<Shot> shots;
    private List<Boss.Bomb> activeBombs;
    private Player player;
    private int incomingLives, incomingSpeed, incomingShots;
    private boolean incomingThreeWay;
    private boolean inGame = true;

    // NEW: Lock variable to prevent holding the spacebar from spamming audio/bullets
    private boolean spacePressed = false;

    private String message = "You Win!";
    private final Dimension d = new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
    private final Random randomizer = new Random();
    private Timer timer;
    private final Game game;
    private HashMap<Integer, SpawnDetails> spawnMap = new HashMap<>();

    public Scene2(Game game, int lives, int speed, int shots, boolean threeWay) {
        this.game = game;
        this.incomingLives = lives;
        this.incomingSpeed = speed;
        this.incomingShots = shots;
        this.incomingThreeWay = threeWay;
        spawnMap.put(100, new SpawnDetails("Boss", BOARD_WIDTH + 100, BOARD_HEIGHT / 2));

        // Build the player immediately so the screen doesn't crash!
        gameInit();
    }

    private void gameInit() {
        enemies = new ArrayList<>();
        explosions = new ArrayList<>();
        shots = new ArrayList<>();
        activeBombs = new ArrayList<>();

        player = new Player();
        player.setLives(incomingLives);
        player.setSpeed(incomingSpeed);
        player.setShotsUpgrade(incomingShots);
        player.setHasThreeWay(incomingThreeWay);
    }

    public void start() {
        addKeyListener(new TAdapter());
        setFocusable(true);
        requestFocusInWindow();
        setBackground(Color.black);
        timer = new Timer(1000 / 60, new GameCycle());
        timer.start();
        try {
            audioPlayer = new AudioPlayer("src/audio/scene2.wav"); // Make sure you have a scene2.wav in your audio folder!
            audioPlayer.play();
        } catch (Exception e) {
            // Silently ignore if the audio file is missing
        }
    }

    public void stop() { timer.stop(); }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // --- DRAMATIC PULSING BACKGROUND ---
        int pulse = (int) (Math.sin(frame * 0.05) * 15 + 15);
        g2d.setColor(new Color(pulse, 0, pulse / 2));
        g2d.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

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
                if (!player.isInvulnerable() || frame % 10 < 5) {
                    g.drawImage(player.getImage(), player.getX(), player.getY(), this);
                }
            } else {
                // The GATE: Only run this once, the exact moment the game ends
                if (inGame) {
                    inGame = false;
                    message = "Game Over";

                    // Trigger the defeat music safely!
                    try {
                        AudioPlayer finaleMusic = new AudioPlayer("src/audio/defeat.wav");
                        finaleMusic.play();
                    } catch (Exception e) {
                        System.out.println("Audio Error: " + e.getMessage());
                    }
                }
            }

            // --- CINEMATIC VIGNETTE ---
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
            g2d.setColor(Color.BLACK);

            // Top and bottom shadows
            g2d.fillRect(0, 0, BOARD_WIDTH, 40);
            g2d.fillRect(0, BOARD_HEIGHT - 40, BOARD_WIDTH, 40);

            // Left and right shadows
            g2d.fillRect(0, 0, 40, BOARD_HEIGHT);
            g2d.fillRect(BOARD_WIDTH - 40, 0, 40, BOARD_HEIGHT);

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)); // Reset for UI

            // Draw the dashboard over the vignette so the UI remains pristine
            drawDashboard(g);

            // --- ACTION: THE BOSS HEALTH BAR IS CALLED HERE ---
            drawBossUI(g2d);

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

        // Inside Scene2's update() method when the game ends
        if (!inGame && !endMusicPlayed) {

            // 1. Immediately cut the heavy boss BGM
            try {
                if (audioPlayer != null) {
                    audioPlayer.stop();
                }
            } catch (Exception e) {}

            // 3. Lock the cue
            endMusicPlayed = true;
        }

        SpawnDetails sd = spawnMap.get(frame);
        if (sd != null && sd.type.equals("Boss")) enemies.add(new Boss(sd.x, sd.y));

        player.act();

        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                enemy.act(-1);

                // FIXED: Shielded the audio logic for Boss collision
                if (player.isVisible() && !player.isInvulnerable() && enemy.collidesWith(player)) {
                    player.takeDamage();
                    playSFX("player_hit");
                }if (enemy instanceof Boss) {
                    Boss boss = (Boss) enemy;

                    // Trigger the custom Boss.java attack logic!
                    // Rolls a 150-sided die 60 times a second; if it rolls < 2, the boss fires.
                    if (randomizer.nextInt(150) < 2) {

                        // Grab the entire list of bombs from your custom attack patterns
                        List<Boss.Bomb> newBombs = boss.fireMultipleShots();

                        // Add all of them to the active screen at once
                        activeBombs.addAll(newBombs);

                        playSFX("enemy_shot");
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

                // FIXED: Shielded the audio logic for Boss bombs
                if (player.isVisible() && !player.isInvulnerable() && bomb.getX() >= player.getX() && bomb.getX() <= (player.getX() + 32) &&
                        bomb.getY() >= player.getY() && bomb.getY() <= (player.getY() + 32)) {
                    player.takeDamage();
                    playSFX("player_hit");
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
                            playSFX("enemies_dead");

                            if (boss.isDying()) {
                                explosions.add(new Explosion(boss.getX(), boss.getY()));

                                // FIXED: Added the gate and audio stop command here too!
                                if (inGame) {
                                    inGame = false;
                                    message = "You Stopped The Alien Attacks!";

                                    try {
                                        if (audioPlayer != null) {
                                            audioPlayer.stop();
                                        }
                                    } catch (Exception e) {}

                                    // Trigger the victory music right here!
                                    try {
                                        AudioPlayer finaleMusic = new AudioPlayer("src/audio/victory.wav");
                                        finaleMusic.play();
                                    } catch (Exception e) {
                                        System.out.println("Audio Error: " + e.getMessage());
                                    }
                                }
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
        activeBombs.removeIf(Boss.Bomb::isDestroyed);
    }

    private class GameCycle implements ActionListener { @Override public void actionPerformed(ActionEvent e) { frame++; update(); repaint(); } }

    private class TAdapter extends KeyAdapter {
        // NEW: Add a timer and a cooldown limit inside the adapter
        private long lastFireTime = 0;
        private final long FIRE_COOLDOWN = 500; // cooldown

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

                long currentTime = System.currentTimeMillis();

                // NEW: Check if enough time has passed since the last shot
                if (currentTime - lastFireTime >= FIRE_COOLDOWN) {
                    spacePressed = true; // Lock it so holding down the key does nothing
                    player.fireWeapon(shots);
                    playSFX("player_shot");

                    // Reset the timer
                    lastFireTime = currentTime;
                }
            }
        }
    }
    private void drawBossUI(Graphics2D g2d) {
        int bossHp = 0;
        boolean bossAlive = false;

        for (Enemy e : enemies) {
            if (e instanceof Boss) {
                bossHp = ((Boss) e).getHp();
                bossAlive = true;
            }
        }

        if (bossAlive && bossHp > 0) {
            int barWidth = 400;
            int barHeight = 15;
            int x = (BOARD_WIDTH - barWidth) / 2;
            int y = BOARD_HEIGHT - 70;

            // Dark, ominous background track
            g2d.setColor(new Color(50, 0, 0, 200));
            g2d.fillRect(x, y, barWidth, barHeight);

            // The bleeding health fill (dynamically scales based on HP)
            g2d.setColor(new Color(220, 20, 20));
            g2d.fillRect(x, y, Math.min(barWidth, bossHp * 4), barHeight);

            // Cinematic Boss Title
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Courier New", Font.BOLD, 14));
            g2d.drawString("UNKNOWN THREAT", x, y - 5);
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
        String activeArmament = "DEFAULT BLASTER";

        // We check the most powerful upgrades first
        if (player.hasThreeWay()) { // Replace with your exact boolean check for Three-Way
            activeArmament = "THREE-WAY SPREAD";
        } else if (player.hasMultiShot()) { // Replace with your exact boolean check for Multi-Shot
            activeArmament = "TWIN BLASTER";
        }

        g.drawString(activeArmament, 15, 60);
        g.setFont(new Font("Helvetica", Font.BOLD, 18));

        // --- RESTORED PLAYER HEALTH ---
        // Flashes white desperately when on the last life to build tension
        if (player.getLives() == 1 && frame % 20 < 10) {
            g.setColor(Color.WHITE);
        } else {
            g.setColor(Color.RED);
        }
        g.drawString("HEALTH: " + player.getLives(), 15, 100);
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