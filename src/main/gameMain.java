package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.prism.paint.Color;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import pieces.Asteroid;
import pieces.Bullet;
import pieces.SpaceShip;

public class gameMain extends Application {
	public static int WIDTH = 900;
	public static int HEIGHT = 900;
	public static double MAX_AMMO = 200;
	public static int STAR_COUNT = 75;
	
	@Override
	public void start(Stage window) throws Exception {
		// KeyMap and Pane
		Map<KeyCode, Boolean> pressedKeys = new HashMap<>();
		Pane pane = new Pane();
		pane.setPrefSize(WIDTH, HEIGHT);
		pane.setStyle("-fx-background-color: #000000;");

		// Scoring 
	    Text score = new Text(10, 20, "Points: 0");
	    score.setStyle("-fx-stroke: white;"
	    		+ "-fx-fill: white;"
	    		+ "fx-stroke-width: 2;");
	    score.setFont(Font.font("Verdana", 20));
	    pane.getChildren().add(score);
	    AtomicInteger points = new AtomicInteger();
	    
	    
	    // Ammo left
	    Text ammoLab = new Text(10, 20, "Ammo");
	    ammoLab.setStyle("-fx-stroke: white;"
	    		+ "fx-stroke-width: 2;");
	    ammoLab.setFont(Font.font("Verdana", 20));
	    ammoLab.setTranslateY(HEIGHT - 55);
	    ammoLab.setTranslateX(10);
	    pane.getChildren().add(ammoLab);
	    
	    AtomicInteger ammo = new AtomicInteger();
	    ammo.addAndGet(200);
	    ProgressBar ammoBar = new ProgressBar(ammo.get() / MAX_AMMO);
	    ammoBar.setTranslateY(HEIGHT - 50);
	    ammoBar.setTranslateX(100);
	    ammoBar.setMinWidth(WIDTH - 150);
	    pane.getChildren().add(ammoBar);
	    
	    // Add Stars 
	    Random rnd = new Random();
	    for (int i=0; i<STAR_COUNT; i++) {
	    	Circle star = new Circle();
	    	star.setRadius(rnd.nextDouble() * 4);
	    	star.setCenterX(rnd.nextInt(WIDTH));
	    	star.setCenterY(rnd.nextInt(HEIGHT));
	    	star.setFill(javafx.scene.paint.Color.WHITE);
	    	pane.getChildren().add(star);
	    }
		
		// Space Ship
		SpaceShip ship = new SpaceShip(WIDTH / 2, HEIGHT / 2);
		pane.getChildren().add(ship.getPiece());

		// Asteroids
		List<Asteroid> asteroids = new ArrayList<>();
		List<Asteroid> asteroidRemove = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			boolean asteroidNeeded= true;
			while (asteroidNeeded) {
				int xPos = rnd.nextInt(WIDTH);
				int yPos =  rnd.nextInt(HEIGHT);
			    Asteroid asteroid = new Asteroid(xPos, yPos);
				// Make sure the asteroid isnt going to spawn in the ship
				if (!asteroid.collide(ship)) {
					asteroid.turnRight();
					asteroid.turnRight();
					asteroid.accelerate();
					asteroid.accelerate();
				    asteroids.add(asteroid);
					pane.getChildren().add(asteroid.getPiece());;
					asteroidNeeded = false;
				}
			}
		}
		// Bullets
		List<Bullet> bullets = new ArrayList<>();
		List<Bullet> bulletRemove  = new ArrayList<>();



		
		Scene scene = new Scene(pane, WIDTH, HEIGHT);
		

		scene.setOnKeyPressed(event -> {
		    pressedKeys.put(event.getCode(), Boolean.TRUE);
		});
		
		scene.setOnKeyReleased(event -> {
		    pressedKeys.put(event.getCode(), Boolean.FALSE);
		});
		
		new AnimationTimer() {
		
		    @Override
		    public void handle(long now) {
		    	// Move Ship
		        if(pressedKeys.getOrDefault(KeyCode.LEFT, false)) {
		            ship.turnLeft();
		        }
		
		        if(pressedKeys.getOrDefault(KeyCode.RIGHT, false)) {
		            ship.turnRight();
		        }
		        
		        if(pressedKeys.getOrDefault(KeyCode.UP, false)) {
		            ship.accelerate();
		        }
		        
		        if(pressedKeys.getOrDefault(KeyCode.DOWN, false)) {
		            ship.stop();
		        }
		        // Shooting
		        if (pressedKeys.getOrDefault(KeyCode.SPACE, false) && ammo.get() > 0) {
		            Bullet bullet = new Bullet((int) ship.getPiece().getTranslateX(), (int) ship.getPiece().getTranslateY());
		            bullet.getPiece().setRotate(ship.getPiece().getRotate());
		            bullets.add(bullet);

		            bullet.accelerate();
		            bullet.setMovement(bullet.getMovement().normalize().multiply(3));
		            double ammoLeft = ammo.decrementAndGet() / MAX_AMMO;
		            ammoBar.setProgress(ammoLeft);
		            pane.getChildren().add(bullet.getPiece());
		        }
		        
		        // Add Ammo and Asteroids
		        if(Math.random() < 0.01) {
		            Asteroid asteroid = new Asteroid(WIDTH, HEIGHT);
		            if(!asteroid.collide(ship)) {
		                asteroids.add(asteroid);
		                if (ammo.get() < MAX_AMMO - 10) {
		                	ammoBar.setProgress(ammo.addAndGet(10) / MAX_AMMO);
		                } else {
		                	int fill = (int) (MAX_AMMO - ammo.get());
		                	ammoBar.setProgress(ammo.addAndGet(fill) / MAX_AMMO);
		                }
		                pane.getChildren().add(asteroid.getPiece());
		            }
		        }
		        
		        // Check if the ship has hit a rock  - end Game
		        asteroids.forEach(asteroid -> {
		            if (ship.collide(asteroid)) {
			        	pane.getChildren().remove(ship.getPiece());
			        	pane.getChildren().remove(asteroid.getPiece());
		                stop();
		            }
		        });

		        // Bullets hitting Asteroids
		        bullets.forEach((bullet) -> {
		        	if (bullet.getPosX() > WIDTH - 5| bullet.getPosY() > HEIGHT - 5 | 
		        			bullet.getPosX() < 5  | bullet.getPosY() < 5) {
		        		pane.getChildren().remove(bullet.getPiece());
		        		bulletRemove.add(bullet);
		        	}
		        	asteroids.forEach(asteroid -> {
		        		if (asteroid.collide(bullet)) {
		        			pane.getChildren().remove(bullet.getPiece());
		        			pane.getChildren().remove(asteroid.getPiece());
		        			asteroidRemove.add(asteroid);
		        			score.setText("Points: " + points.addAndGet(asteroid.getPoint()));
		        			int addAmmo = Math.floorDiv(asteroid.getPoint(), 10);
		        			int ammoLeft = ammo.get();
		        			if (ammoLeft + addAmmo > MAX_AMMO) {
		        				addAmmo = (int) (MAX_AMMO - ammoLeft);
		        			}
		        			ammoBar.setProgress(ammo.addAndGet(addAmmo) / MAX_AMMO);
		        			System.out.println(ammo.get());
		        		}
		        	});
		        });
		        
		        // Removing bullets and asteroids from arrayLists
		        bulletRemove.stream().forEach(bullet -> bullets.remove(bullet));
		        asteroidRemove.stream().forEach(asteroid -> asteroids.remove(asteroid));
		        
		        // Move objects
		        ship.move();
		        asteroids.forEach(asteroid -> asteroid.move());
		        bullets.forEach((bullet) -> bullet.move());
		        
		        
		    }
		}.start();
		
		window.setScene(scene);
		window.setTitle("Asteroid game");
		window.show();
	}



	public static void main(String[] args) {
		launch();
	}

}
