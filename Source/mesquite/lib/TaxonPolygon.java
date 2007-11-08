package mesquite.lib;

import java.awt.Polygon;
import java.awt.Rectangle;

public class TaxonPolygon  extends Polygon {
	Rectangle b;

	public void setB(int x, int y, int w, int h){
		if (b == null)
			b = new Rectangle(x, y, w, h);
		else {
			b.x = x;
			b.y = y;
			b.width = w;
			b.height = h;
		}
	}

	public Rectangle getB(){
		return b;
	}
}