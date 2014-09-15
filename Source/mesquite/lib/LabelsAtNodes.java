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
/** A class to hold an array of charts for the nodes of a tree. */
public class LabelsAtNodes extends PanelsAtNodes  {
	
	public LabelsAtNodes(MesquiteModule ownerModule, int numNodes, TreeDisplay treeDisplay){
		super(ownerModule, numNodes, treeDisplay);
	}
	public Panel makePanel(int i){
		MesquiteLabel c = new MesquiteLabel(ownerModule, i);
		return c;
	}
	
	public void setColor(Color c){
		if (c==null)
			return;
		for (int i=0; i<numNodes; i++){
			Panel p = getPanel(i);
			if (p !=null && p instanceof MesquiteLabel){
				((MesquiteLabel)p).setColor(c);
			}
		}
	}
	
}


