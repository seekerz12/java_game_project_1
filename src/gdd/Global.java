package gdd;

public class Global {
    private Global() {
        // Prevent instantiation
    }

    public static final int SCALE_FACTOR = 3; // Scaling factor for sprites

    public static final int BOARD_WIDTH = 716; // Doubled from 358
    public static final int BOARD_HEIGHT = 700; // Doubled from 350
    public static final int BORDER_RIGHT = 60; // Doubled from 30
    public static final int BORDER_LEFT = 10; // Doubled from 5

    public static final int GROUND = 580; // Doubled from 290
    public static final int BOMB_HEIGHT = 10; // Doubled from 5

    public static final int ALIEN_HEIGHT = 24; // Doubled from 12
    public static final int ALIEN_WIDTH = 24; // Doubled from 12
    public static final int ALIEN_INIT_X = 300; // Doubled from 150
    public static final int ALIEN_INIT_Y = 10; // Doubled from 5
    public static final int ALIEN_GAP = 30; // Gap between aliens

    public static final int GO_DOWN = 30; // Doubled from 15

    // Updated from 24 to 50 to match the new Scene 1 progression logic
    public static final int NUMBER_OF_ALIENS_TO_DESTROY = 50;

    public static final int CHANCE = 5;
    public static final int DELAY = 17;
    public static final int PLAYER_WIDTH = 30; // Doubled from 15
    public static final int PLAYER_HEIGHT = 20; // Doubled from 10

    // --- NEW SPRITESHEET IMAGES ---
    public static final String IMG_PLAYER = "src/images/plane1.png";
    public static final String IMG_ALIEN1 = "src/images/alien1.png";
    public static final String IMG_ALIEN2 = "src/images/alien2.png";
    public static final String IMG_BOSS = "src/images/boss1.png";
    public static final String IMG_TITLE = "src/images/main_menu.png";
    public static final String IMG_POWERUP_MULTISHOT = "src/images/Multishot_icon.png";
    public static final String IMG_POWERUP_TRIPLESHOT = "src/images/Triple_shot_icon.png";
    public static final String IMG_POWERUP_SPEED = "src/images/Speed_boost_icon.png";



    //ORIGINAL IMAGES ---
    public static final String IMG_SHOT = "src/images/shot.png";
    public static final String IMG_EXPLOSION = "src/images/explosion.png";

    public static final String IMG_POWERUP_SPEEDUP = "src/images/powerup-s.png";
    public static final String IMG_BOMB = "src/images/bomb.png";
}