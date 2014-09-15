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
/** An extensible dialog box containing a list with standard buttons.  */
public class ParametersDialog extends ExtensibleDialog implements ItemListener {
	MesquiteParameter[] parameters;
	Checkbox[] boxes;
	DoubleField[] min, max, value;
	Choice[] constraintMenu;
//	int[] constraints;
	boolean usingSelection = false;
	int minSelect, maxSelect;
	int[] selectionAge;
	boolean[] selected;
	int age = 0;
	boolean showRange; 
	/*.................................................................................................................*/
	public ParametersDialog (MesquiteWindow parent, String title, String label, MesquiteParameter[] parameters, boolean[] selected, int minSelect, int maxSelect, boolean showRange) {
		super(parent, title, new MesquiteInteger());
		this.parameters  = parameters;
		this.selected = selected;
		this.showRange = showRange;
		if (selected !=null){
			boxes = new Checkbox[parameters.length];
			selectionAge = new int[parameters.length];
			usingSelection = true;
		}
		min = new DoubleField[parameters.length];
		max = new DoubleField[parameters.length];
		value = new DoubleField[parameters.length];
		constraintMenu = new Choice[parameters.length];
		/*
		 * 		constraints = new int[parameters.length];
			for (int i= 0; i<parameters.length; i++) {
				if (parameters[i].getConstrainedTo() == null) 
					constraints[i] = i;
		}
		 */
		if (label != null)
			addLabel(label);
		this.minSelect = minSelect;
		this.maxSelect = maxSelect;
		for (int i=0; i<parameters.length; i++){
			if (selected !=null && i < selected.length && selected.length>1 && minSelect<parameters.length){
				boxes[i] = addCheckBox(parameters[i].getName(), selected[i]);
				if (selected[i])
					selectionAge[i] = age++;
				boxes[i].addItemListener(this);
			}
			else {
				addLabel(parameters[i].getName());
			}
			suppressNewPanel();
			value[i] = addDoubleField("Current Value: ", parameters[i].getValue(), 10, parameters[i].getMinimumAllowed(), parameters[i].getMaximumAllowed());
			value[i].setDigits(6);
			value[i].setPermitUnassigned(true);
			if (showRange){
				suppressNewPanel();
				min[i] = addDoubleField("Min: ", parameters[i].getMinimumSuggested(), 6, parameters[i].getMinimumAllowed(), parameters[i].getMaximumAllowed());
				suppressNewPanel();
				max[i] = addDoubleField("Max: ", parameters[i].getMaximumSuggested(), 6, parameters[i].getMinimumAllowed(), parameters[i].getMaximumAllowed());
			}
			//	if (parameters[i].getConstrainedTo()!= null){
			suppressNewPanel();
			String[] constraintNames = new String[parameters.length];
			for (int k = 0; k<parameters.length; k++)
				if (k == i)
					constraintNames [k]= "-";
				else
					constraintNames[k] = parameters[k].getName();
			int constraint = i;
			if (parameters[i].getConstrainedTo()!= null)
				constraint = whichParameter(parameters[i].getConstrainedTo());
			constraintMenu[i] = addPopUpMenu ("Constrain = ", constraintNames, constraint); 
			constraintMenu[i].addItemListener(this);

			//		}
			if (!StringUtil.blank(parameters[i].getExplanation())){
				addLabel (parameters[i].getExplanation(), Label.LEFT);
			}
			addHorizontalLine(1);
		}

	}
	int whichParameter(MesquiteParameter p){
		for (int i=0; i<parameters.length; i++){
			if (p == parameters[i])
				return i;
		}
		return -1;
	}
	void constraintChosen(int i, int chosen){
		if (i == chosen)
			parameters[i].setConstrainedTo(null, false);
		else {
			parameters[i].setConstrainedTo(parameters[chosen], true);
		}
	}
	public void acceptParameters(){
		for (int i=0; i<parameters.length; i++){

			parameters[i].setValue(value[i].getValue());
				if (showRange){
				parameters[i].setMinimumSuggested(min[i].getValue());
				if (max[i].getValue()<min[i].getValue())
					max[i].setValue(min[i].getValue());
				parameters[i].setMaximumSuggested(max[i].getValue());
			}
			constraintChosen(i, constraintMenu[i].getSelectedIndex());
		}

	}

	int getNumSelected(){
		if (!usingSelection || selected == null)
			return 0;
		int count = 0;
		for (int i=0; i<selected.length; i++){
			if (selected[i])
				count++;
		}
		return count;
	}
	int getOldest(boolean selectedYes){
		if (!usingSelection)
			return -1;
		int min = age+1;
		int oldest = 0;
		for (int i=0; i<selectionAge.length; i++){
			if (selected[i]==selectedYes && selectionAge[i] < min){
				min = selectionAge[i];
				oldest = i;
			}
		}
		return oldest;
	}

	void expandSelectToMin(int changed){

		while (getNumSelected()<minSelect && getNumSelected() < selected.length){
			int oldest = getOldest(false);
			dontListen = true;
			boxes[oldest].setState(true);
			selected[oldest] = true;
			selectionAge[oldest] = age++;
			dontListen = false;
		}
	}
	void contractSelectToMax(int changed){		
		while (getNumSelected()>maxSelect && (getNumSelected()> 0)){
			int oldest = getOldest(true);
			dontListen = true;
			boxes[oldest].setState(false);
			selected[oldest] = false;
			//selectionAge[oldest] = 0;
			dontListen = false;
		}

	}
	boolean dontListen = false;
	/*.................................................................................................................*/
	public void itemStateChanged(ItemEvent e){
		if (e.getItemSelectable() instanceof Choice){
			for (int i=0; i< constraintMenu.length; i++){
				if (e.getItemSelectable() == constraintMenu[i]){
					constraintChosen(i, constraintMenu[i].getSelectedIndex());
				}
			}
		}

		if (!usingSelection || dontListen){

			return;
		}

		for (int i=0; i<boxes.length; i++){
			if (e.getItem().equals(boxes[i].getLabel())){

				if (boxes[i].getState())
					selectionAge[i] = age++;
				selected[i] = boxes[i].getState();
				int num = getNumSelected();

				if (num< minSelect)
					expandSelectToMin(i);
				else if(num > maxSelect)
					contractSelectToMax(i);

				return;
			}
		}
	}

}




