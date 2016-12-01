package benstd;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import benstd.sprites.AbstractPlant;
import benstd.sprites.BombPlant;
import benstd.sprites.Lawnmower;
import benstd.sprites.PeaShooter;
import benstd.sprites.Sun;
import benstd.sprites.Walnut;
import benstd.sprites.Zombie;
import ssmith.awt.BufferedImageCache;

/*
 * 
 */
public class Main extends JFrame implements MouseListener, KeyListener {
	
	private BufferStrategy bs;
	
	public static Random rnd = new Random();

	private List<Entity> objects = new ArrayList<Entity>();
	private List<Entity> to_remove = new ArrayList<Entity>();
	private List<Entity> to_add = new ArrayList<Entity>();

	private int game_stage = 0;
	private int creds = 100;
	private Class selectedType = PeaShooter.class;
	private String msg = "";
	public BufferedImageCache bic;

	public static void main(String[] args) {
		new Main();
	}

	public Main() {
		this.setTitle("Ben's Pigs v Zombies");
		this.addMouseListener(this);
		this.addKeyListener(this);
		this.setSize(Statics.WIDTH, Statics.HEIGHT);
		this.setVisible(true);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		bic = new BufferedImageCache(this);

		this.createBufferStrategy(2);
		bs = this.getBufferStrategy();

		this.to_add.add(new CreateZombieProcess(this));

		for (int i=0 ; i<Statics.NUM_LANES ; i++) {
			Lawnmower lm = new Lawnmower(this, getYForLane(i));
			this.add(lm);
		}
		gameLoop();

	}


	private void gameLoop() {
		float interpol = 1f;
		while (true) {
			long start = System.currentTimeMillis();

			Graphics g = bs.getDrawGraphics();
			g.clearRect(0,  0, Statics.WIDTH, Statics.HEIGHT);

			this.objects.addAll(this.to_add);
			this.to_add.clear();

			this.objects.removeAll(this.to_remove);
			this.to_remove.clear();

			for(Entity p : objects) {
				if (game_stage == 0) {
					p.process(interpol);
				}
				if (p instanceof Sprite) {
					Sprite s = (Sprite)p;
					s.draw(g);
				}
			}

			g.setColor(Color.BLACK);
			g.drawString(""+creds, 50, 50);
			g.drawString(msg, 10, Statics.HEIGHT - 20);

			bs.show();

			long dur = System.currentTimeMillis() - start;
			float secs = (float)dur/1000f;

			// Is the computer too fast?
			if (secs < Statics.FRAME_DUR_SECS) {
				float rem = (Statics.FRAME_DUR_SECS - secs)*1000;
				try {
					Thread.sleep((int)rem);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			dur = System.currentTimeMillis() - start;
			secs = (float)dur/1000f;

			interpol = secs;
			//msg = "i:" + interpol;
		}
	}


	public void remove(Entity o) {
		this.to_remove.add(o);
	}


	public void createZombie() {
		Zombie z = new Zombie(this, "Zombie", getRandomY());
		this.to_add.add(z);
	}


	public void createSun() {
		Sun z = new Sun(this, getRandomX());
		this.to_add.add(z);
	}


	private int getRandomX() {
		return rnd.nextInt(Statics.WIDTH);
	}


	private int getRandomY() {
		int lane = rnd.nextInt(Statics.NUM_LANES);
		return getYForLane(lane);
	}


	public int getYForLane(int lane) {
		return Statics.DROP + (lane * Statics.LANE_HEIGHT);

	}


	public Sprite getColliders(Sprite sprite) {
		for(Entity p : objects) {
			if (p != sprite) {
				if (p instanceof Sprite) {
					Sprite s = (Sprite)p;
					if (s.checkIfCollided(sprite)) {
						return s;
					}
				}
			}
		}
		return null;
	}


	public void gameOver() {
		game_stage = 1;
	}


	public void add(Entity e) {
		this.to_add.add(e);
	}


	@Override
	public void mouseClicked(MouseEvent event) {
		// todo - do this in sep main thread, not UI thread!
		try {
			// Check for Suns first
			for(Entity p : objects) {
				if (p instanceof Sun) {
					Sun s = (Sun)p;
					if (s.checkIfContains(event.getX(), event.getY())) {
						s.remove();
						this.creds += 5;
						return;
					}
				}
			}

			// Check we've not clicked on a planet
			for(Entity p : objects) {
				if (p instanceof AbstractPlant) {
					AbstractPlant ap = (AbstractPlant)p;
					if (ap.checkIfContains(event.getX(), event.getY())) {
						return;
					}
				}
			}

			int gx = event.getX() / Statics.LANE_HEIGHT;
			gx = gx * Statics.LANE_HEIGHT;
			int gy = (event.getY()-Statics.DROP) / Statics.LANE_HEIGHT;
			gy = getYForLane(gy);// * Statics.LANE_HEIGHT;

			if (selectedType == PeaShooter.class) {
				if (creds >= 10) {
					this.add(new PeaShooter(this, gx, gy));
					creds -= 10;
				} else {
					msg = "Not enough credits!";
				}
			} else if (selectedType == Walnut.class) {
				if (creds >= 6) {
					this.add(new Walnut(this, gx, gy));
					creds -= 6;
				} else {
					msg = "Not enough credits!";
				}
			} else if (selectedType == BombPlant.class) {
				if (creds >= 6) {
					this.add(new BombPlant(this, gx, gy));
					creds -= 6;
				} else {
					msg = "Not enough credits!";
				}
			} else {
				System.err.println("Unknown type: " + selectedType);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	@Override
	public void keyTyped(KeyEvent key) {
		if (this.game_stage == 0) {
			if (key.getKeyChar() == '1') {
				selectedType = PeaShooter.class;
				msg = "PEA SHOOTER selected";
			} else if (key.getKeyChar() == '2') {
				selectedType = Walnut.class;
				msg = "WALNUT selected";
			} else if (key.getKeyChar() == '3') {
				selectedType = BombPlant.class;
				msg = "BOMB selected";
			} else {
				msg = "Unknown keypress";
			}
		}
	}


	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	public void mousePressed(MouseEvent arg0) {

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {

	}

	@Override
	public void keyPressed(KeyEvent arg0) {

	}

	@Override
	public void keyReleased(KeyEvent arg0) {

	}

}
