/* Mesquite source code.  Copyright 2001 and onward, D. Maddison and W. Maddison. 


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



/*===============================================*/
/** This class holds radio buttons (CheckboxGroup) for an ExtensibleDialog. */
public class RadioButtons implements ItemListener {
	ExtensibleDialog dialog;
	CheckboxGroup cbg;
	Checkbox[] checkboxArray;
	int numCheckBoxes;
	Panel newPanel;
	GridBagConstraints constraints;
	/*.................................................................................................................*/
	public RadioButtons (ExtensibleDialog dialog, String[] labels, Panel[] subPanels, int defaultBox) {

		initialize(dialog,labels,subPanels, defaultBox);

	}
	/*.................................................................................................................*/
	public RadioButtons (ExtensibleDialog dialog, String[] labels, int defaultBox) {
		initialize(dialog,labels,null, defaultBox);
		
	}
	
	/*.................................................................................................................*/
	void initialize (ExtensibleDialog dialog, String[] labels, Panel[] subPanels, int defaultBox) {

		this.dialog = dialog;
		cbg = new CheckboxGroup();
		numCheckBoxes = labels.length;
		checkboxArray = new Checkbox[numCheckBoxes];
		dialog.forceNewPanel();
		newPanel = dialog.addNewDialogPanel();  

		GridBagLayout gridBag = new GridBagLayout();
		constraints = new GridBagConstraints();
		constraints.gridx=0;
		constraints.gridy = GridBagConstraints.RELATIVE;
		constraints.gridwidth=1;
		constraints.gridheight=1;
		constraints.fill=GridBagConstraints.BOTH;
		constraints.anchor=GridBagConstraints.WEST;
		constraints.insets = new Insets(0,25,0,20);
		constraints.weighty=1.0;
		newPanel.setLayout(gridBag);


		for (int i=0; i< numCheckBoxes; i++) {
			checkboxArray[i] = new Checkbox(labels[i], cbg, true);
			checkboxArray[i].addItemListener(this);
			newPanel.add(checkboxArray[i]);
			if (subPanels!=null && i<subPanels.length)
				newPanel.add(subPanels[i]);
			gridBag.setConstraints(checkboxArray[i],constraints);
		}
		if (defaultBox>=0 && defaultBox<numCheckBoxes) {
			cbg.setSelectedCheckbox(checkboxArray[defaultBox]);
			checkboxArray[defaultBox].requestFocusInWindow();
		}
		else if (labels.length>0) {
			cbg.setSelectedCheckbox(checkboxArray[0]);
			checkboxArray[0].requestFocusInWindow();
		}

	}

	/*.................................................................................................................*/
	public void itemStateChanged(ItemEvent arg0) {
		dialog.requestFocus();
	}

	public void setEnabled(int button, boolean b){
		if (button>=0 && button<numCheckBoxes)
			checkboxArray[button].setEnabled(b);
	}
	
	public void setEnabledCheckboxGroup(boolean b){
		for (int i=0; i< numCheckBoxes; i++) {
			checkboxArray[i].setEnabled(b);
		}
	}

	public void enableRadioButtons(){
		for (int i=0; i< numCheckBoxes; i++) {
			checkboxArray[i].setEnabled(true);
		}
	}
	public void disableRadioButtons(){
		for (int i=0; i< numCheckBoxes; i++) {
			checkboxArray[i].setEnabled(false);
		}
	}

	
	/*.................................................................................................................*/
	public boolean isAButton(ItemSelectable itemSelectable) {
		for (int i=0; i< numCheckBoxes; i++) {
			if (checkboxArray[i]==itemSelectable)
				return true;
		}
		return false;
	}
	/*.................................................................................................................*/
    public void addItemListener(ItemListener itemListener) {
		for (int i=0; i< numCheckBoxes; i++) {
			checkboxArray[i].addItemListener(itemListener);
		}
    }
	/*.................................................................................................................*/
    public void removeItemListener(ItemListener itemListener) {
		for (int i=0; i< numCheckBoxes; i++) {
			checkboxArray[i].removeItemListener(itemListener);
		}
    }
	/*.................................................................................................................*/
	public Checkbox getSelectedCheckbox () {
		return cbg.getSelectedCheckbox();
	}
	/*.................................................................................................................*/
	public CheckboxGroup getCheckBoxGroup () {
		return cbg;
	}
	/*.................................................................................................................*/
	public Checkbox getCheckbox(int i) {
		if (i>=0 && i<numCheckBoxes)
			return checkboxArray[i];
		return null;
	}
	/*.................................................................................................................*/
	public int getValue () {
		Checkbox checkbox  = cbg.getSelectedCheckbox();
		for (int i=0; i< numCheckBoxes; i++) {
			if (checkbox == checkboxArray[i]) 
				return i;
		}
		return 0;
	}
	/*.................................................................................................................*/
	public void setValue (int k) {
		for (int i=0; i< numCheckBoxes; i++) {
			if (k == i)
				checkboxArray[i].setState(true);
			else
				checkboxArray[i].setState(false);
		}
	}

}


