package benstd;

import java.awt.Graphics;
import java.awt.Image;

/**
 * Parent class of all drawable objects
 *
 */
public abstract class Sprite extends Entity {

	public String name;
	public float x, y, w, h;
	public Image img;

	public Sprite(Main _main, String _name, String filename, float _x, float _y, float _w, float _h) {
		super(_main);

		main = _main;
		name = _name;
		x = _x;
		y = _y;
		w = _w;
		h = _h;
		
		img = main.bic.getImage("res/" + filename, (int)w, (int)h);
	}


	public void draw(Graphics g) {
		g.drawImage(img, (int)x, (int)y, main);
	}

	
	public boolean checkIfCollided(Sprite r) {
		return x < r.x + r.w && x + w > r.x && y < r.y + r.h && y + h > r.y;
	}


	public boolean checkIfContains(float x2, float y2) {
		return x < x2 && y < y2 && x+w > x2 && y+h > y2;
	}

}
