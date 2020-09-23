package pieces;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Polygon;

public class SpaceShip extends PieceObject {

	public SpaceShip(int x, int y) {
		super(new ImageView(new Image("file:spaceship2.png")), x, y);
    }

}
