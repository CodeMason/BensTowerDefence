package benstd.sprites;

import benstd.Main;
import benstd.Sprite;
import benstd.Statics;

public class Lawnmower extends Sprite {

	public boolean activated = false;

	public Lawnmower(Main _main, int _y) {
		super(_main, "Lawnmower", "pig.gif", 0, _y, Statics.LANE_HEIGHT, Statics.LANE_HEIGHT);
	}
	

	@Override
	public void process(float interpol) {
		if (activated) {
			this.x += Statics.LAWNMOWER_SPEED * interpol;
			Sprite s = main.getColliders(this);
			if (s != null) {
				if (s instanceof Zombie) {
					s.remove();
				}
			}
			if (x > Statics.WIDTH) {
				this.remove();
			}
		}
	}
	
}
