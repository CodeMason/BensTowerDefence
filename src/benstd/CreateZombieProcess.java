package benstd;

public class CreateZombieProcess extends Entity {

	private float timer;
	private float max = Statics.ZOMBIE_SPAWN_SECS;
	
	public CreateZombieProcess(Main main) {
		super(main);
	}

	
	@Override
	public void process(float interpol) {
		timer -= interpol;
		if (timer <= 0) {
			main.createZombie();
			main.createSun();
			timer = max;
			max -= 0.1f;
			if (max < 2) {
				max = 2;
			}
		}
		
	}
}
