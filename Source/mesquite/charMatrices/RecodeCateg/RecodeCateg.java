/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.RecodeCateg;
/*~~  */

import java.util.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class RecodeCateg extends CategDataAlterer implements AltererSimpleCell {
	long[] rules;
	int maxState = 0;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		rules = new long[64];
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
   	public void alterCell(CharacterData cData, int ic, int it){
   		if (cData instanceof CategoricalData) { //this does not do state names recoding!
   		}
   	}
   	
	/*.................................................................................................................*/
   	/** Called to alter data in those cells selected in table*/
   	public boolean alterData(CharacterData cData, MesquiteTable table,  UndoReference undoReference){
			
			if (!(cData instanceof CategoricalData)){
				discreetAlert( "Only categorical characters can be recoded by this module.");
				return false;
			}
			if (!cData.anySelected()){
				discreetAlert( "To recode characters, please select entire characters first.");
				return false;
			}
			CategoricalData data = (CategoricalData)cData;
   			if (data instanceof DNAData || data instanceof ProteinData) {
				if (!MesquiteThread.isScripting())
					alert("Molecular characters cannot be recoded.");
				return false;
			}
			else {
		  		UndoInstructions undoInstructions = data.getUndoInstructionsAllMatrixCells(new int[] {UndoInstructions.NO_CHAR_TAXA_CHANGES});
				// first find maximum state among character selected
				maxState = 0;
				for (int ic = 0; ic < data.getNumChars(); ic++){
					if (data.getSelected(ic)){
						int icMax = data.getMaxState(ic);
						if (icMax > maxState)
							maxState = icMax;
					}
				}
				for (int ic = 0; ic < data.getNumChars(); ic++){
					if (data.getSelected(ic)){
						int m = -1;
						for (int is = maxState; is<= CategoricalState.maxCategoricalState; is++)
							if (data.hasStateName(ic, is))
								m = is;
						if (m > maxState)
							maxState = m;
					}
				}
				String[] states = new String[maxState+3];
				for (int i = 0; i< maxState +1; i++) {
					rules[i] = CategoricalState.makeSet(i);
					states[i] = "state " + RecodeDialog.stateToString(rules[i], data);
				}
				states[maxState+1] = "? (missing)";
				rules[maxState+1] = CategoricalState.unassigned;
				states[maxState+2] = "- (inapplicable)";
				rules[maxState+2] = CategoricalState.inapplicable;
				
				
				MesquiteInteger chosen = new MesquiteInteger();
				RecodeDialog dialog = new RecodeDialog(this, data, states, rules, chosen, maxState);
				dialog.dispose();
				if (!anyChanges(maxState) || chosen.isUnassigned())
					return false;
				for (int ic = 0; ic< data.getNumChars(); ic++){
					if (data.getSelected(ic))
						data.recodeStates(ic, rules, maxState);
				}
				if (undoInstructions!=null) {
					undoInstructions.setNewData(data);
					if (undoReference!=null){
						undoReference.setUndoer(undoInstructions);
						undoReference.setResponsibleModule(this);
					}
				}
			}
   			return true;
   	}

   	private boolean anyChanges(int maxState){
		for (int i = 0; i< maxState +1; i++) {
			if (rules[i] != CategoricalState.makeSet(i))
				return true;
		}
		if (rules[maxState+1] != CategoricalState.unassigned || rules[maxState+2] != CategoricalState.inapplicable)
			return true;
		return false;
   	}
	/*.................................................................................................................*/
  	 public boolean showCitation() {
		return false;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Recode Characters...";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Recode Characters";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Recodes categorical data (e.g., state 1 to state 0)." ;
   	 }
   	 
}

class RecodeDialog extends ListDialog {
	MesquiteModule ownerModule;
	String[] states;
	long[] rules;
	SingleLineTextField text;
	MesquiteInteger chosen;
	int maxState=0;
	CategoricalData data;
	
	public RecodeDialog(MesquiteModule ownerModule, CategoricalData data, String[] states, long[] rules, MesquiteInteger chosen, int maxState){
		super(ownerModule.containerOfModule(), "Recode States", "Indicate recoding of states of selected characters", false, null, states, chosen, null, false, false); 

		//parent, String title, String message, String helpString, Object names, MesquiteInteger selected, String thirdButton, boolean hasDefault, boolean multipleMode
		this.ownerModule = ownerModule;
		this.maxState = maxState;
		this.chosen = chosen;
		this.states = states;
		this.data = data;
		this.rules = rules;
		text = addTextField("Specify new state and press \"Set New State\":", "", 3);
		suppressNewPanel();
		appendToHelpString("For each state you want to recode to another state, select the line for that state (e.g., \"state 0\"), ");
		appendToHelpString("and type into the small box the new state, then press \"Set New State\" to specify that as the new state.  ");
		appendToHelpString("Once you have done this for all of the states you wish to recode, press the Recode button. Only then will the data be recoded.");

		addAListenedButton("Set New State",null,this);
		suppressNewPanel();
		addAListenedButton("Reset",null,this);

		getList().setEnableDoubleClicks(false);
		completeAndShowDialog("Recode", "Cancel", null, "Recode");
		
	}

	private void enter(){
		String s = text.getText();
		if (StringUtil.blank(s)) {
			return;
		}
		CategoricalState cat = stateFromString(s);
		if (!cat.isImpossible()){
			long recoding = cat.getValue();
			java.awt.List list = getList();
			for (int i=0; i<states.length; i++)
				if (list.isIndexSelected(i)) {
					assignRule(i, recoding);
					//getList().replaceItem(states[i], i);
				}
			resetList();
		}
	}
	/*.................................................................................................................*/
	 public  void actionPerformed(ActionEvent e) {
	 	String buttonLabel = e.getActionCommand();
		if ("Reset".equalsIgnoreCase(buttonLabel)){  // Reset button is pressed
			java.awt.List list = getList();
			for (int i=0; i<states.length; i++)
				if (list.isIndexSelected(i)) {
					if (i<=maxState) {
						rules[i] = CategoricalState.makeSet(i);
						states[i] = "state " + RecodeDialog.stateToString(rules[i], data);
					}
					else if (i == maxState+1){
						states[maxState+1] = "? (missing)";
						rules[maxState+1] = CategoricalState.unassigned;
					}
					else if (i == maxState +2){
						states[maxState+2] = "- (inapplicable)";
						rules[maxState+2] = CategoricalState.inapplicable;
					}
				}
			text.setText("");
			resetList();
		}
		else if ("Set New State".equalsIgnoreCase(buttonLabel)){   // Enter button is pressed
			enter();
		}
		else if ("Cancel".equalsIgnoreCase(buttonLabel)){   // Enter button is pressed
			dispose();
		}
		else
			super.actionPerformed(e);
	}
		/*.................................................................................................................*/
		public void buttonHit(String buttonLabel, Button button) {
			if (buttonLabel!=null)
				if (buttonLabel.equalsIgnoreCase("Reset")){
					java.awt.List list = getList();
					for (int i=0; i<states.length; i++)
						if (list.isIndexSelected(i)) {
							if (i<=maxState) {
								rules[i] = CategoricalState.makeSet(i);
								states[i] = "state " + RecodeDialog.stateToString(rules[i], data);
							}
							else if (i == maxState+1){
								states[maxState+1] = "? (missing)";
								rules[maxState+1] = CategoricalState.unassigned;
							}
							else if (i == maxState +2){
								states[maxState+2] = "- (inapplicable)";
								rules[maxState+2] = CategoricalState.inapplicable;
							}
						}
					text.setText("");
					resetList();
				}
				else if (buttonLabel.equalsIgnoreCase("Set New State")){
					enter();
				}
				else {
					super.buttonHit(buttonLabel, button);
				}
		
	}

	public void itemStateChanged(ItemEvent e){
		if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof Integer){
			if (super.numSelected() > 1)
				text.setText("");
			else {
				int state = ((Integer)e.getItem()).intValue();
				getList().makeVisible(state);
				text.setText(stateToString(rules[state]));
			}
		}
		else if (e.getStateChange() == ItemEvent.DESELECTED && e.getItem() instanceof Integer){
			int state = ((Integer)e.getItem()).intValue();
			String s = text.getText();
			if (StringUtil.blank(s))
				return;
			CategoricalState cat = stateFromString(s);
			if (cat.isImpossible())
				return;
			long recoding = cat.getValue();
			assignRule(state, recoding);
			resetList();
			//getList().replaceItem(states[state], state);
		}
	}
   	public CategoricalState stateFromString(String s){
   		CategoricalState cs = new CategoricalState();
   		cs.setValue(s, data);
   		return cs;
   	}
	static String stateToString(long s, CategoricalData data){
		return CategoricalState.toString(s, data, 0, false, true);
	}
	private String stateToString(long s){
		return CategoricalState.toString(s, data, 0, false, true);
	}
	public String textForState(int state, int maxState, long recoding){
		if (state == maxState + 1) { // unassigned
			if (recoding == CategoricalState.unassigned)
				return "? (missing)";
			else
				return  "? (missing) recode to " + stateToString(rules[state]);
		}
		else if (state ==  maxState + 2) { // inapplicable
			if (recoding == CategoricalState.inapplicable)
				return  "- (inapplicable)";
			else
				return  "- (inapplicable) recode to " + stateToString(rules[state]);
		}
		else if (state <=maxState) {
			long orig = CategoricalState.makeSet(state);
			if (recoding == orig)
				return  "state " + stateToString(orig);
			else
				return  "state " + stateToString(orig) + " recode to " + stateToString(rules[state]);
		}
		return "";
	}
	private void assignRule(int state, long recoding){
		if (recoding == CategoricalState.impossible)
			return;
		if (state>=rules.length || recoding == rules[state]) //no change
			return;
		rules[state] = recoding;
		states[state] = textForState(state, maxState, recoding);
	}
	
}



