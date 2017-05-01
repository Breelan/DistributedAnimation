import java.awt.Point;
import java.io.Serializable;
import java.util.Observable;

public class OurSprite extends Observable implements Serializable {

	private Point currentPoint;
	private int direction;
	
	
	public OurSprite() {
		this.currentPoint = new Point();
		this.direction = 1;
	}
	
	public void setPoint(Point newPoint) {
		currentPoint = newPoint;
		setChanged();
		notifyObservers();
	}
	
	public Point getPoint() {
		return currentPoint;
	}
	
	public void changeDirection() {
		if((direction - 1) == 0) {
			direction = -1;
		} else {
			direction = 1;
		}
//		TODO figure out if this is needed
//		setChanged();
//		notifyObservers();
	}
	
	public int getDirection() {
		return this.direction;
	}
	
	public void setSprite(OurSprite newSprite) {
		this.currentPoint = newSprite.getPoint();
		this.direction = newSprite.getDirection();
		setChanged();
		notifyObservers();
	}
}
