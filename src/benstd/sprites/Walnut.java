package benstd.sprites;

import java.awt.Color;
import java.awt.Graphics;

import benstd.Main;
import benstd.Statics;

public class Walnut extends AbstractPlant {
	
	public int cost = 10;
	
	public Walnut(Main main, int x, int y) {
		super(main, "Walnut", "plant2.png", x, y, Statics.WALNUT_HEALTH);
		
	}
	

	@Override
	public void process(float interpol) {
		// Do nothing
	}

	
}
