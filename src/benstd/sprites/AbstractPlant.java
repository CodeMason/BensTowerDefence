package benstd.sprites;

import benstd.Main;
import benstd.Sprite;
import benstd.Statics;

public abstract class AbstractPlant extends Sprite {
	
	protected int health;
	
	protected AbstractPlant(Main _main, String _name, String filename, int _x, int _y, int _health) {
		super(_main, _name, filename, _x, _y, Statics.LANE_HEIGHT, Statics.LANE_HEIGHT);
		
		health = _health;
	}

	
	public void damage(int amt, Zombie by) {
		health--;
		if (health <= 0) {
			this.remove();
		}
	}
	
	
	protected void shoot() {
		main.add(new Bullet(main, this.x + this.w, this.y + (this.h/2)));
	}
	
}
