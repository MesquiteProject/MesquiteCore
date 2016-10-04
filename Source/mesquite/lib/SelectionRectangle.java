package mesquite.lib;

import java.awt.*;
import java.awt.geom.*;

// not yet used; only partially built
/* ======================================================================== */
public class SelectionRectangle  {
	Rectangle selectionRect;
	
	public SelectionRectangle(Graphics2D g2, int x, int y, int w, int h){
		this.selectionRect = new Rectangle(x,y,w,h);
		GraphicsUtil.fillTransparentSelectionRectangle(g2, x,y,w,h);
	}
	public Rectangle setRectangle() {
		return selectionRect;
	}
	public void setRectangle(Rectangle selectionRect) {
		this.selectionRect = selectionRect;
	}
	public void zeroRectangle(Rectangle selectionRect) {
		this.selectionRect = null;
	}
	public void setRectangle(int x, int y, int w, int h) {
		this.selectionRect = new Rectangle(x,y,w,h);
		GraphicsUtil.fixRectangle(selectionRect);
	}

	public static Area createAreaFromRectangle(Rectangle rect) {
		try{
			MesquitePath2DFloat path = new MesquitePath2DFloat();
		path.moveTo(rect.x, rect.y);
		path.lineTo(rect.x+rect.width, rect.y);
		path.lineTo(rect.x+rect.width, rect.y+rect.height);
		path.lineTo(rect.x, rect.y+rect.height);
		path.lineTo(rect.x, rect.y);
		path.closePath();
		return path.getArea();
		}
		catch (Error e){
			return null;
		}
		catch (Exception e){
			return null;
		}
	}
	public void drawSelectionDifference(Graphics2D g2, Component comp, int x, int y, int w, int h) {
		Rectangle newRect = new Rectangle(x,y,w,h);
		GraphicsUtil.fixRectangle(newRect);
		Area newArea = createAreaFromRectangle(newRect);
		Area differenceArea = createAreaFromRectangle(selectionRect);
		
		if (differenceArea!=null) {
			differenceArea.exclusiveOr(newArea);
			Shape oldClip = g2.getClip();
			g2.setClip(differenceArea);
			if (selectionRect.contains(newRect)) { // original rect is bigger
				comp.repaint(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
				GraphicsUtil.fillTransparentSelectionRectangle(g2, x,y,w,h);
			} else if (newRect.contains(selectionRect)) { // new rect is bigger
				GraphicsUtil.fillTransparentSelectionArea(g2, differenceArea);
			}
			g2.setClip(oldClip);
		}

		
		selectionRect.setRect(newRect);
	
		
	}
}