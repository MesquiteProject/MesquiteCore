/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.MatrixFromClipboard;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import mesquite.lib.*;
import mesquite.lib.table.*;
import mesquite.lib.characters.*;
import mesquite.categ.lib.*;
import mesquite.lib.duties.*;

/** Makes a character matrix from the clipboard.*/
public class MatrixFromClipboard extends CharMatrixFiller implements MesquiteListener {
	Taxa taxa=null;
	CharacterData data;
	MCharactersDistribution states;
	Class dataClass = null;
	MesquiteString dataName;
	MesquiteSubmenuSpec mss;
	boolean iveQuit = false; //need special means to quit if multiple taxa blocks, because then harder to know on startup if matrices are available
	
	
	/*.................................................................................................................*/
	/** condition passed to this module must be subclass of CharacterState */
	public boolean startJob(String arguments, Object condition, boolean hiredByName) { 
 		return true;
  	 }
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
    	public  int getNumberOfMatrices(Taxa taxa){
    		return 1;
    	}
    	private void captureClipboard(){
    	}
	/*.................................................................................................................*/
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Taxa taxa){
   	}
	/*.................................................................................................................*/
    	 public String getMatrixName(Taxa taxa, int ic) {
    		return "Matrix from clipboard";
   	 }
   	 
   	 
	/*.................................................................................................................*/
   	public  MCharactersDistribution getMatrix(Taxa taxa, int im){
   		if (im == 0) {
			Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable t = clip.getContents(this);
			try {
				String s = (String)t.getTransferData(DataFlavor.stringFlavor);
	   			int[] lines = MesquiteTable.getTabbedLinesCount(s);
	   			boolean columnNamesPresent = true;
	   			boolean rowNamesPresent = false;
	   			if (lines!=null && IntegerArray.equalValues(lines) && lines.length>0 && lines[0]>0){
					CharactersManager charManager = (CharactersManager)findElementManager(CharacterData.class);
					String[] names = charManager.dataClassesAvailable();
					MesquiteString dataTypeName = new MesquiteString();
					MesquiteBoolean cNames = new MesquiteBoolean(true);
					MesquiteBoolean rNames = new MesquiteBoolean(false);
					MesquiteInteger buttonPressed = new MesquiteInteger(0);
	   				MFCDialog dialog = new MFCDialog(this, s, names, dataTypeName, cNames, rNames, buttonPressed);
	 				if (buttonPressed.getValue() == 1) //cancel
	 					return null;
	   				columnNamesPresent = cNames.getValue();
	   				rowNamesPresent = rNames.getValue();
	   				int numTaxa = lines.length;
	   				if (columnNamesPresent)
	   					numTaxa = lines.length-1;
	   				int numChars = lines[0];
	   				if (rowNamesPresent)
	   					numChars = lines[0]-1;
					CharacterData data = charManager.newCharacterData(taxa, numChars, dataTypeName.toString());
					String title= getProject().getCharacterMatrices().getUniqueName("Matrix from clipboard");
	   				data.setName(title);
					boolean wassave = data.saveChangeHistory;
					data.saveChangeHistory = false;
	   				pasteIt(s, data, columnNamesPresent, rowNamesPresent);
					data.saveChangeHistory = wassave;
	   				data.resetCellMetadata();
	   				return data.getMCharactersDistribution();
	   			}
			}
			catch(Exception e){
			}
   		}
	   	alert("Sorry, there was a problem in making the matrix from the clipboard");
   		return null;
   	}
 	private void pasteIt(String s, CharacterData data, boolean columnNamesPresent, boolean rowNamesPresent){
		int count = 0;
		Parser parser = new Parser();
		CharacterState state = data.makeCharacterState();
		MesquiteInteger pos = new MesquiteInteger(0);
		MesquiteInteger pos2 = new MesquiteInteger(0);
		if (columnNamesPresent) {
			if (rowNamesPresent)
				StringUtil.getNextTabbedToken(s, pos); //eat up first tab of corner cell
			for (int ic = 0; ic<data.getNumChars(); ic++) {
				String t = StringUtil.getNextTabbedToken(s, pos);
				data.setCharacterName(ic, t);//charactername;
			}
		}
		String token = "";
		for (int it = 0; it<data.getNumTaxa() && token !=null; it++) {
			int taxonNumber = it;
			if (rowNamesPresent) {
				token = StringUtil.getNextTabbedToken(s, pos);
				int tn = data.getTaxa().whichTaxonNumber(token);
				if (tn>=0)
					taxonNumber = tn;
				//taxon name; skip
			}
			for (int ic = 0; ic<data.getNumChars() && token !=null; ic++) {
				token = StringUtil.getNextTabbedToken(s, pos);
				pos2.setValue(0);
				state.setValue(token, data);
				if (!state.isCombinable())
					state.setToUnassigned();
				data.setState(ic, taxonNumber, state);//state;
			}
		}
 	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
   	/** gets the current matrix.*/
   	public MCharactersDistribution getCurrentMatrix(Taxa taxa){
   		return getMatrix(taxa, 0);
   	}
   	/** returns the number of the current matrix*/
   	public int getNumberCurrentMatrix(){
   		return 0;
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Matrix from Clipboard";  
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Clipboard...";  
   	 }
   	 
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Makes a character matrix from text in the clipboard." ;
   	 }
	/*.................................................................................................................*/
 	/** returns current parameters, for logging etc..*/
 	public String getParameters() {
 		return "";
   	 }
   	 
   	 
}

class MFCDialog extends ExtensibleDialog implements ItemListener {
	MatrixFromClipboard ownerModule;
	Choice dataChoice;
	MesquiteString dataChoiceName;
	MesquiteBoolean cNames, rNames;
	Checkbox cNamesBox, rNamesBox;
	public MFCDialog (MatrixFromClipboard ownerModule, String clip, String[] dataTypeNames, MesquiteString dataChoiceName, MesquiteBoolean cNames, MesquiteBoolean rNames, MesquiteInteger buttonPressed){
		super(ownerModule.containerOfModule(), "Matrix from Clipboard", buttonPressed);
		this.dataChoiceName = dataChoiceName;
		this.ownerModule = ownerModule;
		this.cNames = cNames;
		this.rNames = rNames;
		
		//need to choose matrix type, whether 
		addLargeOrSmallTextLabel("If the contents of the clipboard are suitable, a new character matrix can be made from them.  The current contents of clipboard are:");
		if (StringUtil.blank(clip))
			clip = "Sorry, there is nothing in the clipboard.";
		addTextAreaSmallFont(clip, 8);
		addLargeOrSmallTextLabel("If the matrix represented in the clipboard uses special symbols (e.g., A B C D instead of 0 1 2 3), it may not be translatable.");
		dataChoice = addPopUpMenu ("Data type", dataTypeNames, 0);
		if (dataTypeNames.length > 0)
			dataChoiceName.setValue(dataTypeNames[0]);
		dataChoice.addItemListener(this);
		cNamesBox = addCheckBox("Includes character names", cNames.getValue());
		cNamesBox.addItemListener(this);
		rNamesBox = addCheckBox("Includes taxon names", rNames.getValue());
		rNamesBox.addItemListener(this);
		completeAndShowDialog();
		
	}
/*.................................................................................................................*/
  	public void itemStateChanged(ItemEvent e){
  		if (e.getItemSelectable() == dataChoice){
	  		dataChoiceName.setValue((String)e.getItem());
  		}
  		else if (e.getItemSelectable() == cNamesBox){
  			cNames.setValue(cNamesBox.getState());
  		}
  		else if (e.getItemSelectable() == rNamesBox){
  			rNames.setValue(rNamesBox.getState());
  		}

  	}
}


