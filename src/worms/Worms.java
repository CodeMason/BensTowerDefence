package worms;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.MemoryImageSource;
import java.util.Random;

// http://glenn.sanson.free.fr/v2/?select=w4k

// 0, 1 -> Position ver
// 2 -> Sens du ver -1/1 gauche, droite
// 3 -> Compteur pour animation
// 4 -> Angle de tir (-90 -> +90)
// 5 -> Vie
// 6, 7 -> Deplacement (Explosion)


public class Worms extends Frame implements WindowListener, KeyListener {

	private static int WIDTH = 800;//641;
	private static int HEIGHT = 600;//449;

	private static final int BALL_SPEED = 20;//20;
	private static final int GRASS_THICKNESS = 7;

	private Image image; // Double buffer
	private boolean game_being_played = false;
	private boolean keys[] = new boolean[256];

	static String[] names = {"Dumbguy", "Bathan", "Poophead", "Jeff", "Bighead", "Billy", "Bigbutt", "Bob"};

	public Worms() {
		super("Worms");

		boolean computer = true;
		boolean demo = true;

		int[] map = new int[WIDTH * HEIGHT];
		Random rand = new Random(System.currentTimeMillis());

		MemoryImageSource source = new MemoryImageSource(WIDTH, HEIGHT, map, 0, WIDTH);
		source.setAnimated(true);
		Image img = createImage(source);       

		setVisible(true);
		setResizable(false);
		setSize(WIDTH, HEIGHT);
		image = createImage(WIDTH, HEIGHT);
		Graphics g = image.getGraphics();       
		addWindowListener(this);
		addKeyListener(this);       

		while(true) {
			int tmpVar = 64;//64;  // Changes resolution of map

			for (int y=0; y<HEIGHT ; y+=tmpVar) {
				for (int x=0; x<WIDTH ; x+=tmpVar) {
					map[y * WIDTH + x] = rand.nextInt() % 256;
				}
			}

			for (;tmpVar > 1 ; tmpVar = tmpVar >> 1) {
				for (int y=0 ; y<HEIGHT ; y+=tmpVar) {
					for (int x=tmpVar ; x<WIDTH ; x+=tmpVar) map[y*WIDTH + x-(tmpVar >> 1)] = (map[y*WIDTH + x-tmpVar] + map[y*WIDTH + x]) >> 1;
				}

				for (int y=tmpVar ; y<HEIGHT ; y+=tmpVar) {
					for (int x=0 ; x<WIDTH ; x+=(tmpVar >> 1)) map[(y - (tmpVar >> 1)) * WIDTH + x] = (map[(y - tmpVar)*WIDTH + x] + map[y * WIDTH + x]) >> 1;
				}
			}

			for (int i=0 ; i<WIDTH*HEIGHT ; i++) {
				if (map[i] > 0) {
					int top = 1;
					while(i - top * WIDTH > 0 && map[i - top * WIDTH] > 0xFF0000FF) {
						top++;
					}

					int bottom = 1;
					while (i + bottom * WIDTH < HEIGHT * WIDTH && map[i + bottom * WIDTH] > 0) {
						bottom++;
					}

					if (top < GRASS_THICKNESS) { // Grass thickness
						map[i] = rand.nextInt()> 0 ? 0xFF0DAB0D : 0xFF00C800;
					}
					// Retirer ce cas si pb pour atteindre 4Ko
					else if (top == GRASS_THICKNESS)    {
						map[i] = 0xFF007848;
					} else {
						if (i % 50 == 0) { // Brown stripes interval
							map[i] = 0xFFE89058;
						} else {
							map[i] = (i/10)%2 == 0 ? 0xFF985818 : 0xFFC07830;
						}

						tmpVar = Math.min(bottom, 16);//16); // Shadow
						tmpVar = ((tmpVar * tmpVar) >> 3) + rand.nextInt() % 10;

						map[i] = 0xFF000000
								+ (Math.min(255, ((map[i] >> 16) & 0xFF) + tmpVar) << 16)
								+ (Math.min(255, ((map[i] >> 8) & 0xFF) + tmpVar) << 8)
								+ Math.min(255, (map[i] & 0xFF) + tmpVar);
					}
				} else {
					map[i] = 0xFF0000FF - ((i / (WIDTH)) >> 1); // Background
				}
			}

			source.newPixels(0, 0, WIDTH, HEIGHT, false);

			// Init joueurs
			int[][] worms = new int[8][8];

			for (int i=0 ; i<8 ; i++) {
				tmpVar = 0;
				worms[i][0] = 320 + rand.nextInt() % 300;
				worms[i][1] = 220 + rand.nextInt() % 200;

				while (tmpVar < 6 || map[worms[i][0] + WIDTH * (worms[i][1] + 1)] < 0xFF0000FF) {
					if (map[worms[i][0] + WIDTH * worms[i][1]] > 0xFF0000FF || (worms[i][1] > HEIGHT - 10) ) {
						tmpVar = 0;
						worms[i][0] = 320 + rand.nextInt() % 300;
						worms[i][1] = 220 + rand.nextInt() % 200;
					}

					tmpVar++;
					worms[i][1]++;
				}
				worms[i][2] = -1;
				worms[i][5] = 100;
			}       

			int x = 0;
			int y = 0;
			int e = 0;

			int power = 0;
			double[] ball = new double[4]; // Ball
			ball[0] = -1.;

			int currentPlayer = 0;
			int refPlayer = 0;
			boolean fired = false;           

			tmpVar = 0;

			boolean victory = false;
			boolean play = true;
			int blink = 0;

			while (play) {
				long time = System.currentTimeMillis();
				boolean noMoreAction = true;

				// Controls demo
				if (demo & keys[KeyEvent.VK_UP]) {
					computer = true;
				}
				if (demo & keys[KeyEvent.VK_DOWN]) {
					computer = false;
				}
				if (demo & (keys[KeyEvent.VK_SPACE] | keys[KeyEvent.VK_ENTER])) {
					demo = false;
					victory = true;
					game_being_played = false;
				}

				// Action
				for (int player=0 ; player<8 ; player++) {
					if (victory & !game_being_played) {
						play = false;
					}
					if (victory & keys[KeyEvent.VK_SPACE]) {
						play = false;
						demo = true;
					}
					else if (worms[player][0] < 0); // Elimine de la carte
					else if (worms[player][6] != 0 || worms[player][7] != 0) {
						if (worms[player][0] + worms[player][6] < 5
								|| worms[player][0] + worms[player][6] + 5 > WIDTH
								|| worms[player][1] + worms[player][7] < 5
								|| worms[player][1] + worms[player][7] + 5 > HEIGHT) {
							worms[player][6] = 0;
							worms[player][7] = 0;
							worms[player][0] = -100;
							fired = true; // Redirection vers changement de joueur
						}
						else if (map[worms[player][0] + worms[player][6] + (worms[player][1] + worms[player][7]) * WIDTH] > 0xFF0000FF) {
							worms[player][6] = 0;
							worms[player][7] = 0;
							noMoreAction = false;
						}
						else if (map[worms[player][0] + worms[player][1] * WIDTH] < 0xFF0000FF) {
							double dx = ((double)worms[player][6]) / 20.;
							double dy = ((double)worms[player][7]) / 20.;

							boolean wall = false;
							for (int i=0 ; i<20 ; i++)
								wall |= map[worms[player][0] + (int)(i * dx) + (worms[player][1] + (int)(i * dy)) * WIDTH] > 0xFF0000FF;

								if (wall) {
									worms[player][6] = 0;
									worms[player][7] = 0;
								}

								worms[player][0] += worms[player][6];
								worms[player][1] += worms[player][7];                       
								noMoreAction = false;         

								if (!wall) worms[player][7]++;
						}
						else {                       
							worms[player][0] += worms[player][6];
							worms[player][1] += worms[player][7];
							worms[player][7]++;
							noMoreAction = false;
						}
					}
					else if (map[worms[player][0] + (worms[player][1] + 1) * WIDTH] < 0xFF0000FF) {
						worms[player][7] = 1;
						noMoreAction = false;
					}
					else if (player != currentPlayer);
					// Gestion ordinateur
					else if (!fired & (demo | (computer & player % 2 == 1)) & power < tmpVar) {
						power += 5;
					}
					else if (!fired & (demo | (computer & player % 2 == 1)) & tmpVar == 0) {
						// Recherche du ver ennemi le plus proche
						int nearest = -1;
						int dist = 1 << 24;

						for (int i=(1 - (currentPlayer % 2)) ; i<8 ; i+= 2) {
							if (worms[i][0] > 0 && worms[i][5] > 0) {
								int newDist = (worms[player][0] - worms[i][0]) * (worms[player][0] - worms[i][0]) + (worms[player][1] - worms[i][1]) * (worms[player][1] - worms[i][1]);
								if (newDist < dist) {
									nearest = i;
									dist = newDist;
								}
							}
						}

						if (nearest != -1) { // Il reste au moins 1 ver ennemi
							// Orienter vers ver ennemi
							worms[player][2] = worms[player][0] < worms[nearest][0] ? 1 : -1;
							// Ajuster tir (si necessaire)
							int angle = (int)(180. * Math.atan(((double)(worms[nearest][1] - worms[player][1])) / ((double)Math.abs(worms[player][0] - worms[nearest][0]))) / Math.PI);

							if (worms[player][4] < angle) {
								worms[player][4] += 5;
							}
							else if (worms[player][4] > angle + 5) {
								worms[player][4] -= 5;
							}
							else {
								tmpVar = 5 + Math.min(95, dist >> 6);
							}
						}
					}
					// Gestion clavier
					else if ((keys[KeyEvent.VK_LEFT] | keys[KeyEvent.VK_RIGHT]) & !(demo | (computer & player % 2 == 1))) {
						if (keys[KeyEvent.VK_LEFT]) {
							worms[player][2] = -1;
						} else {
							worms[player][2] = 1;
						}
						worms[player][3]++;
						worms[player][6] = worms[player][2];
						worms[player][7] = -2;
					}
					else if (keys[KeyEvent.VK_UP] & !(demo | (computer & player % 2 == 1))) {
						if (worms[player][4] > -90) {
							worms[player][4] -= 5;
						}
					}
					else if (keys[KeyEvent.VK_DOWN] & !(demo | (computer & player % 2 == 1))) {
						if (worms[player][4] < 90) {
							worms[player][4] += 5;
						}
					}
					else if (keys[KeyEvent.VK_SPACE] & ball[0] == -1. & !(demo | (computer & player % 2 == 1))) {
						if (power < 100) {
							power += 5;
						}
					}
					else if (power > 0 && ball[0] == -1.) {
						ball[0] = worms[player][0];
						ball[1] = worms[player][1] - 5;
						ball[2] = worms[player][2] * (double)power * 0.2 * Math.cos(worms[player][4] * Math.PI / 180.);
						ball[3] = (double)power * 0.2 * Math.sin(worms[player][4] * Math.PI / 180.);
						fired = true;
						noMoreAction = false;
						tmpVar = 0;
					}
				}

				// Reaffichage
				if (e >= 1) {
					for (int i=0 ; i<WIDTH * HEIGHT ; i++) {
						int d = (i % WIDTH - x) * (i % WIDTH - x) + (i / WIDTH - y) * (i / WIDTH - y);                        
						if (d < 600) {//400) {
							map[i] = d + 1 >= e ? 0xFF0000FF - ((i / WIDTH) >> 1) : 0xFFFFFFFF;
						}
					}       
					e >>= 1;

					source.newPixels(0, 0, WIDTH, HEIGHT, false);               
				}

				g.drawImage(img, 0, 0, this);

				g.setFont(new Font(g.getFont().getFontName(), Font.BOLD, 14));

				// Draw worms
				for (int i=0 ; i<8 ; i++) {
					g.setColor((i==currentPlayer & ((blink >> 3) % 2) == 0) ? Color.RED : Color.WHITE);
					g.drawString(names[i], worms[i][0] - 3*names[i].length(), worms[i][1] - 27);

					g.setColor(i%2==0?Color.GREEN:Color.YELLOW);

					if (worms[i][5] > 0) {
						g.fillOval(worms[i][0] - 4, worms[i][1] - 6 + (worms[i][3]%2), 8, 8);
						g.fillOval(worms[i][0] - 2 - worms[i][2] * 7, worms[i][1]-2 - (worms[i][3]%2), 4, 4);
						g.setColor(i%2==0?new Color(0x00CA00):new Color(0xCACA00));
						g.fillOval((int)(worms[i][0] - 2.5 - worms[i][2] * 4.5), worms[i][1]-4 + (worms[i][3]%2), 5, 5);
						g.setColor(Color.PINK);
						g.fillOval(worms[i][0] - 4 + worms[i][2] * 3, worms[i][1] - 10, 8, 8);
						g.setColor(Color.BLACK);
						g.drawLine(worms[i][0] + 6 * worms[i][2], worms[i][1] - 7, worms[i][0] + 6 * worms[i][2], worms[i][1] - 6);
						g.drawLine(worms[i][0] + 4 * worms[i][2], worms[i][1] - 7, worms[i][0] + 4 * worms[i][2], worms[i][1] - 6);
						g.setColor(Color.WHITE);
						g.drawLine(worms[i][0] + 6 * worms[i][2], worms[i][1] - 8, worms[i][0] + 6 * worms[i][2], worms[i][1] - 8);
						g.drawLine(worms[i][0] + 4 * worms[i][2], worms[i][1] - 8, worms[i][0] + 4 * worms[i][2], worms[i][1] - 8);     
						g.drawString(String.valueOf(worms[i][5]), worms[i][0] - 10, worms[i][1] - 14);
					}
					else {
						g.fillRect(worms[i][0] - 4, worms[i][1] - 11, 9, 11);
						g.setColor(Color.BLACK);
						g.drawLine(worms[i][0], worms[i][1] - 8, worms[i][0], worms[i][1] - 2);
						g.drawLine(worms[i][0] - 2, worms[i][1] - 6, worms[i][0] + 2, worms[i][1] - 6);
						g.setColor(Color.WHITE);
					}               
				}

				// Cross
				if (!fired) {
					g.drawLine((int)(worms[currentPlayer][0] - 3 + worms[currentPlayer][2] * 20 * Math.cos(worms[currentPlayer][4] * Math.PI / 180.)), (int)(worms[currentPlayer][1] - 5 + 20 * Math.sin(worms[currentPlayer][4] * Math.PI / 180.)), (int)(worms[currentPlayer][0] + 3 + 20 * worms[currentPlayer][2] * Math.cos(worms[currentPlayer][4] * Math.PI / 180.)), (int)(worms[currentPlayer][1] - 5 + 20 * Math.sin(worms[currentPlayer][4] * Math.PI / 180.)));
					g.drawLine((int)(worms[currentPlayer][0] + worms[currentPlayer][2] * 20 * Math.cos(worms[currentPlayer][4] * Math.PI / 180.)), (int)(worms[currentPlayer][1] - 8 + 20 * Math.sin(worms[currentPlayer][4] * Math.PI / 180.)), (int)(worms[currentPlayer][0] + 20 * worms[currentPlayer][2] * Math.cos(worms[currentPlayer][4] * Math.PI / 180.)), (int)(worms[currentPlayer][1] - 2 + 20 * Math.sin(worms[currentPlayer][4] * Math.PI / 180.)));
				}

				// Power
				if (power != 0) {
					g.setColor(new Color(0x9999FF));
					g.fillRect(15, 30, 100, 10);
					for (int i=0 ; i<power ; i++) {
						g.setColor(new Color(0xFFC800 - (i<<9)));
						g.drawLine(15+i, 29, 15+i, 41);
					}
				}

				if (demo) {
					g.setColor(Color.WHITE);
					g.drawString("4K contest '06", 276, 255);                   
					g.setFont(new Font(g.getFont().getFontName(), Font.BOLD, 144));
					g.drawString("W4K", 160, 240);
					g.setFont(new Font(g.getFont().getFontName(), Font.BOLD, 32));
					g.setColor((computer & ((blink >> 3) % 2) == 0) ? Color.RED : Color.WHITE);
					g.drawString("1 Player", 260, 305);
					g.setColor((!computer & ((blink >> 3) % 2) == 0) ? Color.RED : Color.WHITE);
					g.drawString("2 Players", 250, 340);
				}              

				// Water
				for (int i=2 ; i>=0 ; i--) {
					g.setColor(new Color(0x0000FF + 0x666600 * i));
					for (int j=0 ; j<WIDTH ; j++) {
						g.drawLine(j, (int)(Math.cos((double)(j+(blink<<(3-i))) / 10.) * (5. - i) + (HEIGHT-7) - i * 3), j, HEIGHT);
					}
				}               

				g.setColor(Color.WHITE);               

				// Ball
				if (ball[0] != -1.) {
					noMoreAction = false;

					for (int step=0 ; step<BALL_SPEED ; step++) { // was 20
						ball[0] += ball[2] / 20;
						ball[1] += ball[3] / 20;

						// Test collision avec les vers
						for (int i=0 ; i<8 ; i++) {
							if (i != currentPlayer) {
								noMoreAction |= ball[0] > worms[i][0] - 1
										&& ball[0] < worms[i][0] + 1
										&& ball[1] > worms[i][1] - 8
										&& ball[1] < worms[i][1];
							}
						}                   

						if (ball[0] < 5 || ball[0] + 5 > WIDTH
								|| ball[1] < 0 || ball[1] + 5 > HEIGHT) {
							power = 0;
							ball[0] = -1.;
							noMoreAction = true;
							break;
						} else if (map[(int)ball[0] + (int)ball[1] * WIDTH] > 0xFF0000FF || noMoreAction) {
							x = (int)ball[0];
							y = (int)ball[1];

							// Gestion collision explosion + joueurs
							for (int i=0 ; i<8 ; i++) {
								int dist = ((worms[i][0] - x) * (worms[i][0] - x) + (worms[i][1] - y) * (worms[i][1] - y));

								if (dist < 400) { // scs
									worms[i][5] -= (400 - dist) >> 3;
									worms[i][6] = noMoreAction ? (int)(ball[2] / 2) : ((worms[i][0] - x) /2); // x ejection
									worms[i][7] = (dist - 500) / 40; // y ejection
								}
							}

							power = 0;                   
							ball[0] = -1.;
							e = 400;                       
							noMoreAction = false;
							break;
						}

						ball[3] += 0.02;
					}

					// Draw ball
					g.fillOval((int)ball[0] - 1, (int)ball[1] - 1, 3, 3);
				}

				if (fired & noMoreAction) {
					// Check game statut               
					byte[] alive = new byte[2];
					for (int i=0 ; i<8 ; i++) {
						if (worms[i][0] > 0 && worms[i][5] > 0) {
							alive[i%2]++;
						}
					}

					if (alive[0] * alive[1] == 0) {
						if (demo) {
							play = false;
						}
						else {
							g.setFont(new Font(g.getFont().getFontName(), Font.BOLD, 32));
							g.drawString((alive[0]==alive[1]?"Draw game!!!":(alive[0]==0?"Dalton":"Beatles")+" Win!"), 240, 240);

							victory = true;
						}
					}
					else {
						refPlayer++;
						currentPlayer = refPlayer % 8;                   
						while (worms[currentPlayer][0] < 0 || worms[currentPlayer][5] <= 0) {
							currentPlayer += 2;
							currentPlayer %= 8;
						}

						fired = false;
					}
				}

				// Refresh
				try {
					Thread.sleep(50 + time - System.currentTimeMillis());
				} catch (Exception ex) {}

				paint(this.getGraphics());

				blink++;

				if (demo & blink > 2400) {
					play = false;
				}
			}
		}
	}

	public void paint(Graphics g) {
		if (image != null) g.drawImage(image, 0, 0, this);
	}

	public void windowClosing(WindowEvent w) {
		System.exit(0);
	}

	public void windowOpened(WindowEvent w) {}
	public void windowClosed(WindowEvent w) {}
	public void windowIconified(WindowEvent w) {}
	public void windowDeiconified(WindowEvent w) {}
	public void windowActivated(WindowEvent w) {}
	public void windowDeactivated(WindowEvent w) {}

	public void keyPressed(KeyEvent e) {
		game_being_played = true;
		keys[e.getKeyCode()] = true;
	}

	public void keyReleased(KeyEvent e) {
		keys[e.getKeyCode()] = false;
	}

	public void keyTyped(KeyEvent e) {}

	public static void main(String[] args) {
		new Worms();
	}   
}

