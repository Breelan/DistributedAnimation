import java.awt.Point;
import java.util.Observable;

public class OurSprite extends Observable {

	private Point currentPoint;
	
	
	public OurSprite() {
		currentPoint = new Point();
	}
	
	public void setPoint(Point newPoint) {
		currentPoint = newPoint;
		setChanged();
		notifyObservers();
	}
	
	public Point getPoint() {
		return currentPoint;
	}
}
