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
import java.awt.event.*;

import javax.swing.JLabel;


public class GroupDialog extends ExtensibleDialog implements Colorable, ItemListener, ActionListener {
	ColorPickerPanel colors;
	MesquiteWindow frame;
	Color color;
	MesquiteSymbol symbol, currentSymbol;
	SingleLineTextField name;
	Choice symbolsPopUp;
	String symbolOptionsButtonName = "Symbol Options...";
	SymbolsVector symVector;
	int defaultSymbolElement = 0;
	SymbolGraphicsPanel symbolsViewPanel;
	boolean supportsSymbols=true;
	Button symbolOptionsButton;
	JLabel symbolLabel;
	static final String chooseTemplate = "Choose Template";
	
	public GroupDialog(MesquiteProject proj, MesquiteWindow f, String title, String groupName, Color initColor, MesquiteSymbol initSymbol, boolean supportsSymbols){
		super(f, title);
		frame = f;
		color = initColor;
		this.supportsSymbols = supportsSymbols;
		
		
		colors = new ColorPickerPanel(this, initColor, 30);
		name = addTextField("Name:", groupName, 30);
		addNewDialogPanel(colors);
		if (isInWizard())
			appendToHelpString("<h3>Name and Color</h3>Please enter a name for the group, and choose a color");


		
		if (supportsSymbols) {
			Listable[] list = proj.getFileElements(SymbolsVector.class);
			if (list != null && list.length>0){
				symVector = (SymbolsVector)list[0];
				String[] symbolNames = symVector.getStringArrayList();
				symbolNames = StringArray.addToStart(symbolNames,chooseTemplate);
				currentSymbol = initSymbol;
	//			currentSymbol = (MesquiteSymbol)symVector.elementAt(defaultSymbolElement);
		  		if (currentSymbol instanceof FillableMesquiteSymbol)
		  			((FillableMesquiteSymbol)currentSymbol).setFillColor(getColor());
				if (currentSymbol!=null) {
					symbolLabel = addLabel("Current Symbol: " + currentSymbol.getName(), Label.LEFT);
				}
				else {
					symbolLabel = addLabel("Current Symbol: none", Label.LEFT);
				}
				Panel panel = addNewDialogPanel();
				symbolOptionsButton = addAButton(symbolOptionsButtonName, panel);
				symbolsViewPanel = new SymbolGraphicsPanel(this,currentSymbol);
				addGraphicsPanel(symbolsViewPanel);
				symbolOptionsButton.addActionListener(this);
				if (currentSymbol!=null) {
					int symIndex = symVector.indexOfByName(currentSymbol.getName());
					symbolsPopUp = addPopUpMenu("Symbol Templates: ", symbolNames, symIndex+1); // need to do +1 for symIndex because of "Choose Template" item
					symbol = (MesquiteSymbol)symVector.elementAt(symIndex);
					symbolOptionsButton.setEnabled(true);
				}
				else {
					symbolsPopUp = addPopUpMenu("Symbol Templates: ", symbolNames, 0);
					symbolOptionsButton.setEnabled(false);
				}
				symbolsPopUp.addItemListener(this);
			}
		}
		
	}
	public void setColor(Color c){
		color = c;
  		if (currentSymbol instanceof FillableMesquiteSymbol)
  			((FillableMesquiteSymbol)currentSymbol).setFillColor(color);
		if (symbolsViewPanel != null)
			symbolsViewPanel.setSymbol(currentSymbol);
	}
	
	public Color getColor(){
		return colors.getColor();
	}
	
	public MesquiteSymbol getSymbol(){
		if (symbol!=null)
			symbol.setToCloned(currentSymbol);
		return currentSymbol;
	}
	
	public String getName(){
		return name.getText();
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
		  		if (currentSymbol instanceof FillableMesquiteSymbol)
		  			((FillableMesquiteSymbol)currentSymbol).setFillColor(getColor());
		  		if (symbolsViewPanel != null)
		  			symbolsViewPanel.setSymbol(currentSymbol);
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
			currentSymbol.addDialogElements(d, true);
    	 		d.completeAndShowDialog();
			boolean ok = d.query()==0;
			if (ok)
    	 			currentSymbol.getDialogOptions();
			d.dispose();
	  		if (currentSymbol instanceof FillableMesquiteSymbol)
	  			((FillableMesquiteSymbol)currentSymbol).setFillColor(getColor());
			if (symbolsViewPanel != null)
				symbolsViewPanel.setSymbol(currentSymbol);
		}
		else super.actionPerformed(e);
	}
}


