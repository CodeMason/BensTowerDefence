package benstd;

public class Statics {

	public static final int WIDTH = 1000;
	public static final int HEIGHT = 600;
	
	public static final int DROP = 20;
	
	public static final float FPS = 60;
	public static final float FRAME_DUR_SECS = 1/FPS;
	
	public static final int NUM_LANES = 6;
	public static final int LANE_HEIGHT = (HEIGHT/NUM_LANES) - 2;
	
	public static final int PEASHOOTER_HEALTH = 20;
	public static final int WALNUT_HEALTH = 60;
	
	public static final float ZOMBIE_SPEED = WIDTH / 50f;
	public static final float BULLET_SPEED = WIDTH / 6f;
	public static final float LAWNMOWER_SPEED = WIDTH / 4f;
	
	public static final int SHOT_INT_SECONDS = 6;
	public static final float ZOMBIE_SPAWN_SECS = 5;
	
}
