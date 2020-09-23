package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;


import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;
import javafx.stage.Stage;
import pieces.Asteroid;
import pieces.Bullet;
import pieces.Health;
import pieces.SpaceShip;

public class gameMain extends Application {
	// Hypers
	public static int WIDTH = 900;
	public static int HEIGHT = 900;
	public static int STAR_COUNT = 75;
	public static int ASTEROID_COUNT = 15;
	public static int MAX_HEALTH = 100;
	public static int MAX_AMMO = 150;

	// Frame
    private static Random rnd = new Random();
    private static List<Circle> stars = new ArrayList<>();
    private static Pane pane = new Pane();
    private static Map<KeyCode, Boolean> pressedKeys = new HashMap<>();
    // Counters
    private static AtomicInteger health = new AtomicInteger();
    private static AtomicInteger ammo = new AtomicInteger();
    private static AtomicInteger points = new AtomicInteger();
    // Objects
    private static List<Bullet> bullets = new ArrayList<>();
	private static List<Bullet> bulletRemove  = new ArrayList<>();
	private static List<Asteroid> asteroids = new ArrayList<>();
	private static List<Asteroid> asteroidRemove = new ArrayList<>();
	private static List<Health> healthList = new ArrayList<>();
	private static List<Health> healthRemove = new ArrayList<>();
    private static Text score = new Text();
    // Bars
    private static ProgressBar healthBar = new ProgressBar(1.0);
    private static ProgressBar ammoBar = new ProgressBar(1.0);
    // button
	private static Button restart = new Button("Play Again");    
    private static Button closeGame = new Button("Close Game");
	
	public void cleanup() {
		// frame
		rnd = new Random();
		pane = new Pane();
		pressedKeys = new HashMap<>();
		// Counters
		health = new AtomicInteger();
		ammo = new AtomicInteger();
		points = new AtomicInteger();
		// Objects
		stars = new ArrayList<>();
		bullets = new ArrayList<>();
		bulletRemove = new ArrayList<>();
		asteroids = new ArrayList<>();
		asteroidRemove = new ArrayList<>();
		healthList = new ArrayList<>();
		healthRemove = new ArrayList<>();
		score = new Text();
		// Bars
		healthBar = new ProgressBar(1.0);
		ammoBar = new ProgressBar(1.0);
		//restart
		restart = new Button("Play Again");
		closeGame = new Button("Close Game");
	}
	
	public void startGame(Stage window) {
		// restart 
		restart.setOnAction(e -> {
			restart(window);
		});
		
		closeGame.setOnAction(e -> {
			window.close();
		});
		
		// MainWindow Setup 
		pane.setPrefSize(WIDTH, HEIGHT);
		pane.setStyle("-fx-background-color: #000000;");
	    generateStars(); // Stars
	    stars.stream().forEach(star -> pane.getChildren().add(star));
	    
		// Scoring  
	    createUserInfo();	    
		
		// Space Ship
		SpaceShip ship = new SpaceShip(WIDTH / 2, HEIGHT / 2);
		pane.getChildren().add(ship.getPiece());

		// Asteroids

		for (int i = 0; i < ASTEROID_COUNT; i++) {
			boolean asteroidNeeded= true;
			while (asteroidNeeded) {
				Asteroid asteroid = createAsteroid();
				if (!asteroid.collide(ship)) {
				    asteroids.add(asteroid);
					pane.getChildren().add(asteroid.getPiece());;
					asteroidNeeded = false;
				}
			}
		}

		
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
		        	generateShot(ship);
		        }
		        
		        // Add Ammo and Asteroids
		        if(Math.random() < 0.01) {
		            Asteroid asteroid = createAsteroid();
		            if(!asteroid.collide(ship)) {
		                asteroids.add(asteroid);
		                addAmmoAndHealth();
		                pane.getChildren().add(asteroid.getPiece());
		            }
		        }
		        
		        // Add Ammo and Asteroids
		        if(Math.random() < 0.001) {
		            Health hp = createHealth();
		            healthList.add(hp);
		            pane.getChildren().add(hp.getPiece());
		        }
		        healthList.stream().forEach(hp -> {
		        	if (ship.collide(hp)) {
		        		hpCollideShip(hp);
		        	}
		        });
		        healthRemove.stream().forEach(hp -> {healthList.remove(hp);
		        					pane.getChildren().remove(hp.getPiece());});
		        healthRemove.clear();
		        
		        // Check if the ship has hit a rock  - end Game
		        asteroids.forEach(asteroid -> {
		            if (ship.collide(asteroid)) {
			        	pane.getChildren().remove(asteroid.getPiece());
			        	bullets.stream().forEach(bullet -> 
			        		pane.getChildren().remove(bullet.getPiece()));
		                shipAsteroidCollision(asteroid);
		                if (health.get() < 0) {
		                	healthBar.setProgress(0);
		                	pane.getChildren().remove(ship.getPiece());
		                	endGame();
		                	stop();
		                }
		            }
		        });

		        // Bullets hitting Asteroids
		        bullets.forEach((bullet) -> {
		        	bulletOffMap(bullet);
		        	asteroids.forEach(asteroid -> {
		        		bulletAsteroidCollision(asteroid, bullet);
		        	});
		        });
		        
		        // Removing bullets and asteroids from arrayLists
		        bulletRemove.stream().forEach(bullet -> {bullets.remove(bullet); 
		        										pane.getChildren().remove(bullet.getPiece());
		        										});
		        asteroidRemove.stream().forEach(asteroid -> {asteroids.remove(asteroid); 
		        											 pane.getChildren().remove(asteroid.getPiece()); 
		        											 });
		        
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

	public void restart(Stage window) {
		cleanup();
		startGame(window);
	}
	
	@Override
	public void start(Stage window) throws Exception {
		startGame(window);
	}
	
	public static void endGame() {
		// Background
		Rectangle endGameWindow = new Rectangle(WIDTH * 2 / 3, HEIGHT * 2 / 3);
		endGameWindow.setStyle("-fx-border-style: solid; -fx-border-width: 5; -fx-border-color: white");
		endGameWindow.setTranslateX(WIDTH / 6);
		endGameWindow.setTranslateY(WIDTH / 6);
		pane.getChildren().add(endGameWindow);
		
		// VBOX 
		VBox info = new VBox();
		info.setLayoutX(WIDTH / 3);
		info.setLayoutY(HEIGHT / 3);
		info.setAlignment(Pos.CENTER);
		info.setSpacing(10);
		
		// Text 
		Text gameOver = new Text(0, 0, "GAME OVER");
		gameOver.setTextAlignment(TextAlignment.CENTER);
		gameOver.setStyle("-fx-stroke: white;"
	    		+ "-fx-fill: white;"
	    		+ "fx-stroke-width: 2;");
		gameOver.setFont(Font.font("Verdana", 50));
		info.getChildren().add(gameOver);
		// Score
		boolean newHighScore = checkAndWriteHighScore();
		if (newHighScore) {
			Text hs  = new Text(0, 0, "HIGH SCORE!!!!");
			hs.setTextAlignment(TextAlignment.CENTER);
			hs.setStyle("-fx-stroke: red;"
		    		+ "-fx-fill: red;"
		    		+ "fx-stroke-width: 2;");
			hs.setFont(Font.font("Verdana", 40));
			info.getChildren().add(hs);
		}
		Text myScore = new Text(0, 0, "Your Score: " + points.get());
		myScore.setTextAlignment(TextAlignment.CENTER);
		myScore.setStyle("-fx-stroke: white;"
	    		+ "-fx-fill: white;"
	    		+ "fx-stroke-width: 2;");
		myScore.setFont(Font.font("Verdana", 30));
		info.getChildren().add(myScore);
		
		// Restart Button
		restart.setMinWidth(WIDTH / 3);
		restart.setTextAlignment(TextAlignment.CENTER);
		restart.setStyle("-fx-stroke: white;"
	    		+ "-fx-fill: white;"
	    		+ "fx-stroke-width: 2;");
		restart.setFont(Font.font("Verdana", 25));
		info.getChildren().add(restart);
		// close
		closeGame.setMinWidth(WIDTH / 3);
		closeGame.setTextAlignment(TextAlignment.CENTER);
		closeGame.setStyle("-fx-stroke: white;"
	    		+ "-fx-fill: white;"
	    		+ "fx-stroke-width: 2;");
		closeGame.setFont(Font.font("Verdana", 25));
		info.getChildren().add(closeGame);
		// Add to pane
		pane.getChildren().add(info);

	}
	
	public static boolean checkAndWriteHighScore() {
	    // determine the high score
		String file = "file:highScore.txt";
	    int highScore = 0;
	    try {
	        BufferedReader reader = new BufferedReader(new FileReader(file));
	        String line = reader.readLine();
	        while (line != null)                 // read the score file line by line
	        {
	            try {
	                int oldScore = Integer.parseInt(line.trim());   // parse each line as an int
	                if (oldScore > highScore){ 
	                    highScore = oldScore; 
	                }
	            } catch (NumberFormatException e1) {
	                // ignore invalid scores
	                //System.err.println("ignoring invalid score: " + line);
	            }
	            line = reader.readLine();
	        }
	        reader.close();

	    } catch (IOException ex) {
	        System.err.println("ERROR reading scores from file");
	    }
	    
	    
	    FileWriter myWriter;
		try {
			myWriter = new FileWriter(file);
			myWriter.write("" + points.get());
			myWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (points.get() > highScore) {
			return true;
		}
	    return false;
	}

	public Health createHealth() {
		int xPos = rnd.nextInt(WIDTH);
		int yPos = rnd.nextInt(HEIGHT);
		Health hp = new Health(xPos, yPos);
		hp.turnRight();
		hp.turnRight();
		hp.accelerate();
		hp.accelerate();
		return hp;
	}
	
	public static Asteroid createAsteroid() {
		int asteroidColor = rnd.nextInt(3) + 1;
		int xPos = rnd.nextInt(WIDTH);
		int yPos =  rnd.nextInt(HEIGHT);
	    Asteroid asteroid = new Asteroid(xPos, yPos, asteroidColor);
		asteroid.turnRight();
		asteroid.turnRight();
		asteroid.accelerate();
		asteroid.accelerate();
		return asteroid;
	}
	
	public static void generateShot(SpaceShip ship) {
		ImageView ss = ship.getPiece();
		int xPos = (int) (ss.getTranslateX() + ss.getFitWidth() / 3 + (Math.cos(Math.toRadians(ss.getRotate()))  * ss.getFitWidth() / 1.8));
		int yPos = (int) (ss.getTranslateY() + ss.getFitHeight() / 3 + (Math.sin(Math.toRadians(ss.getRotate()))  * ss.getFitHeight() / 2));
        Bullet bullet = new Bullet(xPos, yPos);
        bullet.getPiece().setRotate(ship.getPiece().getRotate());
        bullet.accelerate();
        bullet.setMovement(bullet.getMovement().normalize().multiply(3));
        bullets.add(bullet);
        double ammoLeft = ammo.decrementAndGet() / (double) MAX_AMMO;
        ammoBar.setProgress(ammoLeft);
        pane.getChildren().add(bullet.getPiece());
	}
	
	public static void generateStars() {
	    // Add Stars 
	    for (int i=0; i<STAR_COUNT; i++) {
	    	Circle star = new Circle();
	    	star.setRadius(rnd.nextDouble() * 4);
	    	star.setCenterX(rnd.nextInt(WIDTH));
	    	star.setCenterY(rnd.nextInt(HEIGHT));
	    	star.setFill(javafx.scene.paint.Color.WHITE);
	    	stars.add(star);
	    }
	}

	private static void createUserInfo() {
		// Scoring 
	    score.setText("Score: 0");
	    score.setTranslateX(15);
	    score.setTranslateY(30);
	    score.setStyle("-fx-stroke: white;"
	    		+ "-fx-fill: white;"
	    		+ "fx-stroke-width: 2;");
	    score.setFont(Font.font("Verdana", 20));
	    
	    // Ammo left
	    Text healthLab = new Text(10, 20, "Health");
	    healthLab.setStyle("-fx-stroke: white;"
	    		+ "-fx-fill: white;"
	    		+ "fx-stroke-width: 2;");
	    healthLab.setFont(Font.font("Verdana", 20));
	    healthLab.setTranslateY(HEIGHT - 105);
	    healthLab.setTranslateX(10);
	    // Ammo Bar
	    health.addAndGet(MAX_HEALTH);
	    healthBar.setTranslateY(HEIGHT - 100);
	    healthBar.setTranslateX(100);
	    healthBar.setMinWidth(WIDTH - 150);
	    healthBar.setStyle("-fx-accent: red; ");
	    
	    // Ammo left
	    Text ammoLab = new Text(10, 20, "Ammo");
	    ammoLab.setStyle("-fx-stroke: white;"
	    		+ "-fx-fill: white;"
	    		+ "fx-stroke-width: 2;");
	    ammoLab.setFont(Font.font("Verdana", 20));
	    ammoLab.setTranslateY(HEIGHT - 55);
	    ammoLab.setTranslateX(10);
	    
	    // Ammo Bar
	    ammo.addAndGet(MAX_AMMO);
	    ammoBar.setTranslateY(HEIGHT - 50);
	    ammoBar.setTranslateX(100);
	    ammoBar.setMinWidth(WIDTH - 150);
	    ammoBar.setStyle("-fx-accent: green;");
	    
	    // Add items to pane
	    pane.getChildren().add(score);
	    pane.getChildren().add(healthBar);
	    pane.getChildren().add(healthLab);
	    pane.getChildren().add(ammoBar);
	    pane.getChildren().add(ammoLab);
	}
	
	private static void hpCollideShip(Health hp) {
		int healthleft = health.get();
		int addHp = 10;
		if (healthleft > MAX_HEALTH - 10) {
			addHp = MAX_HEALTH - healthleft;
		}
		healthRemove.add(hp);
		healthBar.setProgress((double) health.addAndGet(addHp) / (double) MAX_HEALTH);
	}
	
	private static void shipAsteroidCollision(Asteroid asteroid) {
		int points = asteroid.getPoint();
		int healthLost = Math.floorDiv(points, 10);
		double healthLeft = health.getAndAdd(-healthLost) / (double) MAX_HEALTH;
		asteroid.takeHealth(healthLost);
		healthBar.setProgress(healthLeft);
		if (asteroid.getHp() < 1) {
			asteroidRemove.add(asteroid);
		}
	}
	
	private static void bulletAsteroidCollision(Asteroid asteroid, Bullet bullet) {
		if (asteroid.collide(bullet)) {
			asteroid.takeHealth(2);
			bulletRemove.add(bullet);
			if (asteroid.getHp() < 1) {
				asteroidRemove.add(asteroid);
				score.setText("Points: " + points.addAndGet(asteroid.getPoint()));
				int addAmmo = (int) Math.floorDiv(asteroid.getPoint() * 5,  4);
				int ammoLeft = ammo.get();
				if (ammoLeft + addAmmo > MAX_AMMO) {
					addAmmo = (int) (MAX_AMMO - ammoLeft);
				}
				ammoBar.setProgress(ammo.addAndGet(addAmmo) / (double) MAX_AMMO);
			}
		}
	}
	
	private static void bulletOffMap(Bullet bullet) {
    	if (bullet.getPosX() > WIDTH - 5| bullet.getPosY() > HEIGHT - 5 | 
    			bullet.getPosX() < 5  | bullet.getPosY() < 5) {
    		bulletRemove.add(bullet);
    	}
	}
	
	private static void addAmmoAndHealth() {
        if (ammo.get() < MAX_AMMO - 5) {
        	ammoBar.setProgress(ammo.addAndGet(5) / (double) MAX_AMMO);
        } else {
        	int fill = (int) (MAX_AMMO - ammo.get());
        	ammoBar.setProgress(ammo.addAndGet(fill) / (double) MAX_AMMO);
        }
        if (health.get() < MAX_HEALTH) {
        	healthBar.setProgress(health.getAndIncrement() / (double) MAX_HEALTH);
        }
        
	}
	
	public static void main(String[] args) {
		launch();
	}

}
