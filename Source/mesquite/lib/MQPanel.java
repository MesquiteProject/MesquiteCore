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
import mesquite.lib.*;

/* ======================================================================== */
/** An intervening class to attempt to avoid StackOverflowError on validate on Ubuntu.*/
public class MQPanel extends Panel {

	public void doLayout(){
		Debugg.println("Layout " + this);
		listComponents(this, "  ");
		super.doLayout();
	}
	
	/** calls repaint of all components*/
	void listComponents(Component c, String s){
		if (c==null)
			return;
		System.out.println(s + c.getClass());
		if (c instanceof Container){
			Component[] cc = ((Container)c).getComponents();
			if (cc!=null && cc.length>0)
				for (int i=0; i<cc.length; i++)
					listComponents(cc[i], s+ "  ");
		}
		
	}
	boolean validating = false;
	public void validate(){
		if (!validating){
			validating = true;
			super.validate();
			validating = false;
		}
	}
}

