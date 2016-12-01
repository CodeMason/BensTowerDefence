package benstd;

public abstract class Entity {
	
	protected Main main;
	
	public Entity(Main _main) {
		main = _main;
	}

	public void remove() {
		main.remove(this);
	}
	
	public abstract void process(float interpol);
	
}
