package benstd.sprites;

import java.awt.Color;
import java.awt.Graphics;

import benstd.Main;
import benstd.Sprite;
import benstd.Statics;

public class Sun extends Sprite {

	public Sun(Main m, float x) {
		super(m, "Sun", "pig.gif", x, 0, Statics.LANE_HEIGHT/2, Statics.LANE_HEIGHT/2);
	}

	@Override
	public void process(float interpol) {
		this.y += Statics.ZOMBIE_SPEED * interpol;
		if (this.y > Statics.HEIGHT) {
			this.remove();
		}
	}

}
