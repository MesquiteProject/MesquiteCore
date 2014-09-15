/* Mesquite source code (Genesis package).  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.genesis.lib;

import java.awt.*;
import java.awt.event.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
/**
This class of modules curates a subclass of character models for use in calculation routines.*/

public abstract class ProbSubModelCurator extends CharSubmodelCurator implements ActionListener  {
	boolean exitLoop=false;
	ExtensibleDialog dialog;
	ProbSubModel model;
	MesquiteInteger buttonPressed;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
  	 }
	/*.................................................................................................................*/
   	public MesquiteModule showEditor(CharacterModel model){
 		if (model == null) 
			return null;
		this.model = (ProbSubModel)model;
		buttonPressed = new MesquiteInteger(1);
		dialog = new ExtensibleDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(),getName(),buttonPressed);
		dialog.addStackedLabels(getName(),"\"" + (((ProbSubModel)model).getName()) + "\"",2);
		dialog.setAutoDispose(false);
		if (!StringUtil.blank(getHelpString())){
			dialog.appendToHelpString(getHelpString());
		}
		if (!StringUtil.blank(getHelpURL())){
			dialog.setHelpURL(getHelpURL());
		}
		
		((ProbSubModel)model).addOptions(dialog);
		dialog.completeAndShowDialog(true,this);
		((ProbSubModel)model).recoverOptions();
		if (dialog!=null)
			dialog.dispose();
		if (buttonPressed.getValue()==0) {
			((ProbSubModel)model).setOptions();
			model.setEditCancel(false);
		}
		else
			model.setEditCancel(true);
		return this;
   	}
   	
	/*.................................................................................................................*/
	 public  String getHelpString() {
	 	return "";
	 }   	
	/*.................................................................................................................*/
	 public  String getHelpURL() {
	 	return "";
	 }
	/*.................................................................................................................*/
	 public  void actionPerformed(ActionEvent e) {
	 	if   (e.getActionCommand() == "OK") {
	 		if ((model==null)||model.checkOptions()) {
	 			buttonPressed.setValue(0);
	 			dialog.dispose();
	 			dialog=null;
	 		}
	 		else
	 			alert(model.checkOptionsReport());
	 	}
	 	else if   (e.getActionCommand() == "Apply") {
	 		if ((model==null)||model.checkOptions()) {
	 			((ProbSubModel)model).setOptions();
	 			Button cancelButton = dialog.getCancelButton();
	 			if (cancelButton!=null)
	 				cancelButton.setEnabled(false);
	 		}
	 		else
	 			alert(model.checkOptionsReport());
	 	}
	 	else {
	 		buttonPressed.setValue(1);
	 		dialog.dispose();
	 		dialog=null;
	 	}
	 }


}


