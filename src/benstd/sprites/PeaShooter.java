package benstd.sprites;

import java.awt.Color;
import java.awt.Graphics;

import benstd.Main;
import benstd.Statics;

public class PeaShooter extends AbstractPlant {
	
	private float timeTilnextShot = 0;
	public int cost = 10;
	
	public PeaShooter(Main main, int x, int y) {
		super(main, "Pea Shooter", "peashooter.png", x, y, Statics.PEASHOOTER_HEALTH);
		
	}
	

	@Override
	public void process(float interpol) {
		timeTilnextShot -= interpol;
		if (timeTilnextShot <= 0) {
			shoot();
			timeTilnextShot = Statics.SHOT_INT_SECONDS + Main.rnd.nextInt(2);
		}
		
	}

	
	@Override
	public void draw(Graphics g) {
		super.draw(g);
		
		g.setColor(Color.black);
		g.drawString(""+this.health, (int)x, (int)y);
	}

}
