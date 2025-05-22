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
import java.awt.event.*;

import mesquite.lib.Explainable;
import mesquite.lib.MesquiteInteger;

/*===============================================*/
/** An extensible dialog box with an explanation.  */
public class ExtensibleExplDialog extends ExtensibleDialog {
	TextArea explanationArea;
	Explainable explainable;
	/*.................................................................................................................*/
	public ExtensibleExplDialog (MesquiteWindow parent, String title, MesquiteInteger buttonPressed) {
		super(parent, title, buttonPressed);
	}
	/*.................................................................................................................*/
	public ExtensibleExplDialog (MesquiteWindow parent, String title) {
		super(parent, title);
	}
	public void setExplainable(Explainable e){
		this.explainable = e;
		if (explanationArea!=null && explainable!=null) {
			explanationArea.setText(explainable.getExplanation());
		}
	}
	/*.................................................................................................................*/
	/**Can be overrided to provide extra panels above button row*/
	public void addAuxiliaryDefaultPanels(){
		explanationArea = addTextAreaSmallFont("", 6);
		explanationArea.setEditable(false);
		if (explainable!=null) {
			explanationArea.setText(explainable.getExplanation());
			explanationArea.setBackground(explanationArea.getParent().getBackground());
		}
	}
}




