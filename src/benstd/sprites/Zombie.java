package benstd.sprites;

import java.awt.Color;
import java.awt.Graphics;

import benstd.Main;
import benstd.Sprite;
import benstd.Statics;

public class Zombie extends Sprite {

	public int health = 4;

	public Zombie(Main _main, String _name, int _y) {
		super(_main, "Zombie", "pig.gif", Statics.WIDTH, _y, Statics.LANE_HEIGHT, Statics.LANE_HEIGHT);
	}


	@Override
	public void process(float interpol) {
		float old_x = x;
		if (Statics.ZOMBIE_SPEED <= 0) {
			throw new RuntimeException("Zero zombie speed");
		}
		this.x -= Statics.ZOMBIE_SPEED * interpol;
		Sprite s = main.getColliders(this);
		if (s != null) {
			if (s instanceof Lawnmower) {
				Lawnmower lm = (Lawnmower)s;
				lm.activated = true;
				this.remove();
			} else if (s instanceof AbstractPlant) {
				AbstractPlant p = (AbstractPlant)s;
				p.damage(1, this);
				x = old_x;
			}
		}
		if (x <= 0) {
			main.gameOver();
		}

	}


	public void damage(int amt) {
		health--;
		if (health <= 0) {
			this.remove();
		}
	}


	@Override
	public void draw(Graphics g) {
		super.draw(g);
		g.setColor(Color.white);
		g.drawString(""+this.health, (int)x, (int)y);
	}
}
