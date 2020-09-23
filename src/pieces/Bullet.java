package pieces;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Bullet extends PieceObject {

	public Bullet(int x, int y) {
		super(new ImageView(new Image("file:bullet.png")), x, y);
		super.setSize(15);
	}
}
