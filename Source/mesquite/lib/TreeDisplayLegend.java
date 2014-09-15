/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib;

import java.awt.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
/** Legends for TreeDisplays, e.g. the TraceCharacter Legend. */
public abstract class TreeDisplayLegend extends ResizableLegend {
	private TreeDisplay treeDisplay;
	private int oldOrientation;

	public TreeDisplayLegend (TreeDisplay treeDisplay, int defaultWidth, int defaultHeight) {
		super(defaultWidth, defaultHeight);
		this.treeDisplay = treeDisplay;
		defaultOffsets();
	}
	/*.................................................................................................................*/
	public void defaultOffsets() {
		if (treeDisplay==null)
			return;
		int buffer = 20;
		if (treeDisplay.getOrientation() == TreeDisplay.DOWN) {
			setOffsetX(buffer);
			setOffsetY(buffer);
		}
		else if (treeDisplay.getOrientation() == TreeDisplay.RIGHT) {
			setOffsetX(buffer);
			setOffsetY(-(defaultHeight+buffer));
		}
		else {
			setOffsetX(-(defaultWidth+buffer));
			setOffsetY(-(defaultHeight+buffer));
		}
		oldOrientation = treeDisplay.getOrientation();
	}
	public void adjustLocation() {
		if (treeDisplay==null)
			return;
		if (oldOrientation != treeDisplay.getOrientation())
			defaultOffsets();
		int legendX=0;
		int legendY =0;
		int conWidth=treeDisplay.getWidth();
		int conHeight =treeDisplay.getHeight();
		int conX = 0;
		int conY = 0;
		if (constrainingContainer !=null ) {
			conWidth = constrainingContainer.getBounds().width;
			//conX = constrainingContainer.getX();
			conHeight = constrainingContainer.getBounds().height;
			//conY = constrainingContainer.getY();
		}
		else if (constrainingRectangle != null){
			conWidth = constrainingRectangle.width;
			//conX = constrainingRectangle.x;
			
			conHeight = constrainingRectangle.height;
			//conY = constrainingRectangle.y;
		}
		int baseX;
		int baseY;
		if (treeDisplay.getOrientation() == TreeDisplay.UP) {
			baseX = 0;
			baseY =  conHeight;
		}
		else if (treeDisplay.getOrientation() == TreeDisplay.DOWN) {
			baseX = 0;
			baseY = 0;
		}
		else if (treeDisplay.getOrientation() == TreeDisplay.RIGHT) {
			baseX = 0;
			baseY = 0;
		}
		else if (treeDisplay.getOrientation() == TreeDisplay.LEFT) {
			baseX = conWidth;
			baseY = 0;
		}
		else {
			baseX = 0;
			baseY = conHeight;
		}
		baseX += conX;
		baseY += conY;
		legendX = baseX+getOffsetX();
		legendY = baseY+getOffsetY();
		if (constrainingRectangle == null && constrainingContainer == null)	 {
		}
		else {
			if (legendX + legendWidth>conX + conWidth) {
				setOffsetX(conWidth+conX-baseX-legendWidth);
				legendX = baseX+getOffsetX();
			}
			if (legendX<conX) {
				setOffsetX(-baseX+conX+4);
				legendX = baseX+getOffsetX();
			}

			if (legendY + legendHeight>conY + conHeight) {
				setOffsetY(conHeight +conY- baseY-legendHeight);
				legendY = baseY+getOffsetY();
			}
			if (legendY<conY) {
				setOffsetY(-baseY+conY+4);
				legendY = baseY+getOffsetY();
			}
		}
		if ((legendX!=getBounds().x) || (legendY!=getBounds().y)) {
			setLocation(legendX, legendY);
			if (okToRecurse){
				MesquiteWindow w = MesquiteWindow.windowOfItem(this);
				okToRecurse = false;
				if (w != null)
					w.checkPanelPositionsLegal();
				okToRecurse = true;
			}
			repaint();
		}
	}
	boolean okToRecurse = true;
	/*public void mouseUp (int modifiers, int x, int y, MesquiteTool tool) {
	   	super.mouseUp(modifiers, x, y, tool);
	   	treeDisplay.pleaseUpdate(false);
		repaint();
	}*/
}

