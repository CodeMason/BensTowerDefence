package benstd.sprites;

import benstd.Main;

public class BombPlant extends AbstractPlant {

	public BombPlant(Main _main, int _x, int _y) {
		super(_main, "Bomb", "gas.png", _x, _y, 10);
	}


	public void damage(int amt, Zombie by) {
		by.remove();
		this.remove();
	}
	
	
	@Override
	public void process(float interpol) {
		
	}
}
