package pieces;

import java.util.Random;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Polygon;

public class Asteroid extends PieceObject {
    private double rotationalMovement;
    private int size;
    private int asteroidNumber;
	private static Random rand = new Random();
	private static int asteroidHealth;


	public Asteroid(int x, int y, int asteroidNumber) {
		this(x, y, asteroidNumber, rand.nextInt(50) + 25);
	}

    
	public Asteroid(int x, int y, int asteroidNumber, int asteroidSize) {
		super(new ImageView(new Image("file:asteroid" + asteroidNumber + ".png")), x, y);
		this.size = asteroidSize;
		this.asteroidHealth = asteroidSize;
		this.asteroidNumber = asteroidNumber;
		super.setSize(size);
		super.getPiece().setRotate(rand.nextInt(360));

        int accelerationAmount = 1 + rand.nextInt(10);
        for (int i = 0; i < accelerationAmount; i++) {
            accelerate();
        }

        this.rotationalMovement = 0.5 - rand.nextDouble();
	}
	
	public int getAsteroidNumber() {
		return this.asteroidNumber;
	}
	
	public void takeHealth(int hp) {
		this.asteroidHealth -= hp;
	}
	
	public int getHp() {
		return this.asteroidHealth;
	}
	
	
	public int getPoint() {
		return this.size;
	}
	
	
    @Override
    public void move() {
        super.move();
        super.getPiece().setRotate(super.getPiece().getRotate() + rotationalMovement);
    }

}
