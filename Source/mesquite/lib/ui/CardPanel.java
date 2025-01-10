/* Mesquite source code.  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.ui;

import java.awt.*;

import mesquite.lib.MesquiteModule;


/*===============================================*/
/** The panel making up one card of a PanelOfCards */
public class CardPanel extends MQPanel  {
	public Image tabImageOn = null;
	public Image tabImageOff = null;
	
	CardPanel (String tabImageOnFile, String tabImageOffFile) {
		super();
		if (tabImageOnFile != null)
			tabImageOn = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + tabImageOnFile);
		if (tabImageOffFile != null)
			tabImageOff = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + tabImageOffFile);
	}

	CardPanel () {
		super();
	}
	/*.................................................................................................................*/
	public Image getTabImageOn(){
		return tabImageOn;
	}
	/*.................................................................................................................*/
	public Image getTabImageOff(){
		return tabImageOff;
	}
}

