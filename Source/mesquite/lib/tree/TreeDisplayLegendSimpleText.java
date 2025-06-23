/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib.tree;

import java.awt.*;

import mesquite.lib.MesquiteInteger;
import mesquite.lib.ui.MesquiteFrame;
import mesquite.lib.ui.StringInABox;


/* ======================================================================== */
/** A simple moveable legend with text, for tree displays. */
/* ======================================================================== */
public class TreeDisplayLegendSimpleText extends TreeDisplayLegend {
	StringInABox text;
	public TreeDisplayLegendSimpleText(TreeDisplay treeDisplay) {
		super(treeDisplay,254, 64);
		setVisible(false);

		setOffsetX(50);
		setOffsetY(50);
		setLayout(null);
		setSize(legendWidth, legendHeight);
		int fontsize = MesquiteInteger.maximum(MesquiteFrame.resourcesFontSize, 12);
		text = new StringInABox("", new Font("SansSerif", Font.PLAIN, fontsize), getWidth());
	}

	public void checkSize(){
		int boxRequest = text.getHeight();
		if (boxRequest<16)
			boxRequest = 16;
		setSize(getWidth(), boxRequest +16);
	}
	public void paint(Graphics g){
		if (text != null){
			//g.setColor(Color.gray);
			g.drawRect(0, 0, legendWidth-1, legendHeight-1);
			text.setWidth(legendWidth-12);
			text.draw(g, 6,0);
		}
	}

	public void setText(String s){
		text.setWidth(legendWidth-12);
		text.setString(s);
		checkSize();
	}


}


