/* Mesquite source code.  Copyright 1997-2002 W. Maddison & D. Maddison. 
Version 0.992.  September 2002.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

package mesquite.collab.lib; 

import mesquite.lib.*;
import java.awt.*;

public abstract class DWPanel extends MousePanel{
	public static final int width = 300;
	public static final int height = 36;
	int ic;
	protected int numImagesVertical = 0;
	Image goaway;
	PanelOwner pw;
	DWImagePanel iPanel;
	public DWPanel (PanelOwner pw){
		super();
		this.pw = pw;
		iPanel = makePanel();
		iPanel.setVisible(true);
		add(iPanel);
		
		setLayout(null);
		goaway = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "goaway.gif");
		setSize(width, 500);
		setCursor(Cursor.getDefaultCursor());
	}
	public abstract DWImagePanel makePanel();
	public DWImagePanel getPanel(){
		return iPanel;
	}
	public void setSize(int w, int h){
		iPanel.setSize(w, h-height);
		super.setSize(w, h);
	}
	public void setBounds(int x, int y, int w, int h){
		iPanel.setBounds(0, height, w, h-height);
		super.setBounds(x, y, w, h);
	}
	public abstract String getTitleString();
	
	public void paint(Graphics g){
		int oW = 8;
		int oH = 12;
	   	g.drawImage(goaway, 2, 2, this);
	   	String s = getTitleString();
		if (s != null)
			g.drawString(s, 20, 18);
		g.fillRect(0, height-2, getBounds().width, 2);
	   	g.setColor(Color.white);
	   	g.drawOval(getBounds().width-oW-5, 3, oW+2, oH+2);
	   	if (iPanel.showLocation.getValue()){
	   		g.setColor(Color.blue);
		   	g.fillOval(getBounds().width-oW-4, 4, oW, oH);
	   	}
	   	g.setColor(Color.black);
	   	g.drawOval(getBounds().width-oW-4, 4, oW, oH);
	}
	/* to be used by subclasses to tell that panel touched */
   	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		if (y> 2 && y< 2 + 16 && x >= 2 && x <= 2 + 16) 
			pw.panelGoAway();
		else if (y> 2 && y< 2 + 16 && x >= getBounds().width-18 && x <= getBounds().width) {
			iPanel.showLocation.toggleValue();
			iPanel.repaint();
			repaint();
			Debugg.println("showLocation " + iPanel.showLocation);
			
		}
		
	}
}
