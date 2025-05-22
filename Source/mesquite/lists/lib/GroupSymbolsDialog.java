/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lists.lib;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.MesquiteSymbol;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.SymbolsVector;

import java.awt.event.*;

import javax.swing.JLabel;


public class GroupSymbolsDialog extends ExtensibleDialog implements ItemListener, ActionListener {
	MesquiteWindow frame;
	MesquiteSymbol symbol, currentSymbol;
	Choice symbolsPopUp;
	String symbolOptionsButtonName = "Symbol Options...";
	SymbolsVector symVector;
	int defaultSymbolElement = 0;
	Button symbolOptionsButton;
	JLabel symbolLabel;
	static final String chooseTemplate = "Choose Template";

	public GroupSymbolsDialog(MesquiteProject proj, MesquiteWindow f, String title, String groupName, MesquiteSymbol initSymbol){
		super(f, title);
		frame = f;

		Listable[] list = proj.getFileElements(SymbolsVector.class);
		if (list != null && list.length>0){
			symVector = (SymbolsVector)list[0];
			String[] symbolNames = symVector.getStringArrayList();
			symbolNames = StringArray.addToStart(symbolNames,chooseTemplate);
			currentSymbol = initSymbol;
			//			currentSymbol = (MesquiteSymbol)symVector.elementAt(defaultSymbolElement);
			if (currentSymbol!=null) {
				symbolLabel = addLabel("Current Symbol: " + currentSymbol.getName(), Label.LEFT);
			}
			else {
				symbolLabel = addLabel("", Label.LEFT);
			}
			if (currentSymbol!=null) {
				int symIndex = symVector.indexOfByName(currentSymbol.getName());
				symbolsPopUp = addPopUpMenu("Symbol Templates: ", symbolNames, symIndex+1); // need to do +1 for symIndex because of "Choose Template" item
				symbol = (MesquiteSymbol)symVector.elementAt(symIndex);
			}
			else {
				symbolsPopUp = addPopUpMenu("Symbol Templates: ", symbolNames, 0);
			}
			Panel panel = addNewDialogPanel();
			symbolOptionsButton = addAButton(symbolOptionsButtonName, panel);
			symbolOptionsButton.addActionListener(this);
			if (currentSymbol!=null) {
				symbolOptionsButton.setEnabled(true);
			}
			else {
				symbolOptionsButton.setEnabled(false);
			}
			symbolsPopUp.addItemListener(this);

		}

	}
	
	public MesquiteSymbol getSymbol(){
		if (symbol!=null)
			symbol.setToCloned(currentSymbol);
		return currentSymbol;
	}
	
/*.................................................................................................................*/
  	public void itemStateChanged(ItemEvent e){
  		if (e.getItemSelectable() == symbolsPopUp){
	  		String itemName = (String)e.getItem();
	  		if (!itemName.equalsIgnoreCase(chooseTemplate)) {
		  		symbol = (MesquiteSymbol)symVector.elementWithName(itemName);
		  		symbolLabel.setText("Current Symbol: " + symbol.getName());
		  		symbolLabel.invalidate();
		  		
		  		currentSymbol = symbol.cloneMethod();
		  		if (symbolOptionsButton != null)
		  			symbolOptionsButton.setEnabled(true);
	  		} 
	  		else if (currentSymbol!=null) {  //switch popup back
	  			int symIndex = symVector.indexOfByName(currentSymbol.getName());
	  			symbolsPopUp.select(symIndex+1);  // need to do +1 because of "Choose Template" item
	  		}
  		}
  	}
  	/*.................................................................................................................*/
  	public void actionPerformed(ActionEvent e){
  		if (symbolOptionsButtonName.equalsIgnoreCase(e.getActionCommand())) {
  			MesquiteInteger buttonPressed = new MesquiteInteger(1);
  			ExtensibleDialog d = new ExtensibleDialog(frame, "Symbol Options: " + currentSymbol.getName(), buttonPressed);
  			currentSymbol.addDialogElements(d, false);
  			d.completeAndShowDialog();
  			boolean ok = d.query()==0;
  			if (ok)
  				currentSymbol.getDialogOptions();
  			d.dispose();
  		}
  		else super.actionPerformed(e);
  	}
}


