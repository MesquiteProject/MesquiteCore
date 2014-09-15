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
import java.awt.event.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
/* ======================================================================== */
/** This is used to list elements of a ListableVector; it is designed for quick and dirty use.  It is expected to be replaced.  */
public class MesquiteListWindow extends MesquiteWindow {
	public ListableVector vector; 
	public MesquiteListTable table;
	int windowWidth=200;
	int windowHeight=400;
	String assignedTitle;
	public MesquiteListWindow (String assignedTitle, MesquiteModule ownerModule, ListableVector vector, boolean showInfoBar) {
		super(ownerModule, showInfoBar);
		this.assignedTitle = assignedTitle;
		setWindowSize(windowWidth, windowHeight);
		//setLayout(new BorderLayout());
		int numItems = vector.size();

		table = new MesquiteListTable (vector, ownerModule, numItems, 0, windowWidth, windowHeight, 160);
		table.setUserAdjust(MesquiteTable.NOADJUST, MesquiteTable.RESIZE);
 		addToWindow(table);
 		table.setVisible(true);
		table.setLocation(0,0);
		resetTitle();
 		setVisible(true);
 
	}
	
	public MesquiteListWindow (String assignedTitle, MesquiteModule ownerModule, Listable[] array, boolean showInfoBar) {
		super( ownerModule, showInfoBar);
		this.assignedTitle = assignedTitle;
		setWindowSize(windowWidth, windowHeight);
		//setLayout(null);
		int numItems=array.length;
		
		table = new MesquiteListTable (array, ownerModule, numItems, 0, windowWidth, windowHeight, 90);
 		addToWindow(table);
 		table.setVisible(true);
		table.setLocation(0,0);
 		setVisible(true);

	}
	
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree blocks, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		setTitle(assignedTitle);
	}
	public void windowResized() {
		super.windowResized();
	   	if (MesquiteWindow.checkDoomed(this))
	   		return;
   	 	if (table!=null && (getHeight()!=windowHeight) || (getWidth()!=windowWidth)) {
   	 		windowHeight =getHeight();
   	 		windowWidth = getWidth();
   			table.setSize(windowWidth, windowHeight);
   	 	}
		MesquiteWindow.uncheckDoomed(this);
	}
}

