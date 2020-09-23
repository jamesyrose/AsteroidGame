package pieces;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Health extends PieceObject {

	public Health(int x, int y) {
		super(new ImageView(new Image("file:health.png")), x, y);
	}

}
