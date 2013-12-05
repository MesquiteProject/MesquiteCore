/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.*;


public class MesquiteTabbedPane extends JTabbedPane {
	MesquiteTabbedPanel panel;
	public MesquiteTabbedPane(MesquiteTabbedPanel panel){
		super();
		this.panel = panel;
	}
	
	int count = 0;
	
	public void setSelectedIndex2(int i){
	
		 int current = getSelectedIndex();
		if (current>=0) {
			JPanel p2 = panel.getTabPanel(current);
			if (p2 != null)
				p2.setVisible(false);
		}

		super.setSelectedIndex(i);


		JPanel p = panel.getTabPanel(i);
		//	setVisible(true);
		if (p != null) {
			p.setVisible(true);
			Graphics g = p.getGraphics();
			if (g!=null)
				g.setClip(null);
			p.invalidate();
		}
		invalidate();
		//	try {Thread.sleep(20);} catch (Exception e) {}
	//	super.setSelectedIndex(i);
	}
}
