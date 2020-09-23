package pieces;

import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import main.gameMain;



public class PieceObject {
	 private ImageView character;
	 private Point2D movement;

	 public PieceObject(ImageView im, int x, int y) {
		 this.character = im;
		 this.character.setFitWidth(50);
		 this.character.setFitHeight(50);
		 
		 this.character.setTranslateX(x);
		 this.character.setTranslateY(y);	 
		 this.movement = new Point2D(0, 0);
	 }
	 
	 public ImageView getPiece() {
		 return character;
	 }	
	 
	 public double getPosX() {
		 return this.character.getTranslateX();
	 }
	 
	 public double getPosY() {
		 return this.character.getTranslateY();
	 }

	 public void setSize(int size){
		 this.character.setFitHeight(size);
		 this.character.setFitWidth(size);
	 }
	 
	 public Point2D getMovement() {
		 return this.movement;
	 }
	 
	 public void setMovement(Point2D move) {
		 this.movement = move;
	 }
	 
	 public void turnLeft() {
		 this.character.setRotate(this.character.getRotate() - 5);

		 double changeX = Math.cos(Math.toRadians(this.character.getRotate()));
		 double changeY = Math.sin(Math.toRadians(this.character.getRotate()));
		 this.movement = new Point2D(changeX, changeY);
	 }

	 public void turnRight() {
		 this.character.setRotate(this.character.getRotate() + 5);
		 
		 double changeX = Math.cos(Math.toRadians(this.character.getRotate()));
		 double changeY = Math.sin(Math.toRadians(this.character.getRotate()));
		 this.movement = new Point2D(changeX, changeY);
	 }

	 public void move() {	
		 this.character.setTranslateX(this.character.getTranslateX() + this.movement.getX());
		 this.character.setTranslateY(this.character.getTranslateY() + this.movement.getY());
		   if (this.character.getTranslateX() < 0) {
		        this.character.setTranslateX(this.character.getTranslateX() + gameMain.WIDTH);
		    }

		    if (this.character.getTranslateX() > gameMain.WIDTH) {
		        this.character.setTranslateX(this.character.getTranslateX() % gameMain.WIDTH);
		    }

		    if (this.character.getTranslateY() < 0) {
		        this.character.setTranslateY(this.character.getTranslateY() + gameMain.HEIGHT);
		    }

		    if (this.character.getTranslateY() > gameMain.HEIGHT) {
		        this.character.setTranslateY(this.character.getTranslateY() % gameMain.HEIGHT);
		    }
	 }

	 public void accelerate() {
		 double changeX = Math.cos(Math.toRadians(this.character.getRotate()));
		 double changeY = Math.sin(Math.toRadians(this.character.getRotate()));

		 changeX *= 0.05;
		 changeY *= 0.05;
		 
		 Point2D newMovement = this.movement.add(changeX, changeY);
		 if (newMovement.magnitude() < 4) {
			 this.movement = newMovement;
		 }
	 }
	 
	 public void stop() {
		 this.movement = new Point2D(0, 0);
	 }
	 
	 // Ship hitting the asteroids
	 public boolean collide(PieceObject other) {
		 	boolean inter = this.character.getBoundsInParent().intersects(other.getPiece().getBoundsInParent());
		 	return inter;
		}
}
