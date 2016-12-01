package benstd.sprites;

import java.awt.Color;
import java.awt.Graphics;

import benstd.Main;
import benstd.Sprite;
import benstd.Statics;

public class Bullet extends Sprite {

	public Bullet(Main _main, float _x, float _y) {
		super(_main, "Bullet", "pig.gif", _x, _y, Statics.LANE_HEIGHT/4, Statics.LANE_HEIGHT/4);
	}


	@Override
	public void process(float interpol) {
		this.x += Statics.BULLET_SPEED * interpol;
		Sprite s = main.getColliders(this);
		if (s != null) {
			if (s instanceof Zombie) {
				this.remove();
				Zombie z = (Zombie)s;
				z.damage(1);
			}
		}

	}


}
