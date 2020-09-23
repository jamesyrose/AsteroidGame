package pieces;

import java.util.Random;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Polygon;

public class Asteroid extends PieceObject {
    private double rotationalMovement;
    private int size;

	
	public Asteroid(int x, int y) {
		super(new ImageView(new Image("file:asteroid1.png")), x, y);
		Random rand = new Random();
		this.size = rand.nextInt(25) + 25;
		super.setSize(size);
		super.getPiece().setRotate(rand.nextInt(360));

        int accelerationAmount = 1 + rand.nextInt(10);
        for (int i = 0; i < accelerationAmount; i++) {
            accelerate();
        }

        this.rotationalMovement = 0.5 - rand.nextDouble();
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
