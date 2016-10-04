/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.SortChar; 

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;


/* ======================================================================== */
public class SortChar extends DataWindowAssistantI { 
	TableTool charSortTool;
	CharacterData data;
	MesquiteTable table;
	MesquiteCollator collator;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		if (containerOfModule() instanceof MesquiteWindow) {
			charSortTool = new TableTool(this, "charSort", getPath(), "charSort.gif", 2, 8,"Sorts characters", "This tool sorts characters according to row selected", MesquiteModule.makeCommand("charSortTouch",  this) , null, null);
			charSortTool.setOptionImageFileName("revCharSort.gif", 13, 8);
			charSortTool.setWorksOnColumnNames(true);
			((MesquiteWindow)containerOfModule()).addTool(charSortTool);
		}
		else return false;
		collator = new MesquiteCollator();
		return true;
	}
 	 public String getFunctionIconPath(){
   		 return getPath() + "charSort.gif";
   	 }
 	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		this.data = data;
		data.addListener(this);
		inhibitionChanged();
	}
	/* ................................................................................................................. */
	void inhibitionChanged(){
		if (charSortTool!=null && data!=null)
			charSortTool.setEnabled(!data.isEditInhibited() && data.canMoveChars());
	}
	/* ................................................................................................................. */
	/** passes which object changed, along with optional integer (e.g. for character) (from MesquiteListener interface) */
	public void changed(Object caller, Object obj, Notification notification) {
		int code = Notification.getCode(notification);
		if (obj instanceof CharacterData && (CharacterData) obj == data) {
			if (code == MesquiteListener.LOCK_CHANGED) {
				inhibitionChanged();
			}
		}
		table.setMessage(data.getCellContentsDescription());
		super.changed(caller, obj, notification);
	}
	/*.................................................................................................................*/
  	 boolean compare(boolean greaterThan, String one, String two){
		int order = collator.compare(one, two);
		if ((order == 1 && greaterThan) ||(order == -1 && !greaterThan))
			return true;
		return false;
  	 }
	/*.................................................................................................................*/
 	/** Swaps parts first and second*/
	void swapParts(CharacterData data, int first, int second, String[] text){  
  		String temp = text[first];
  		text[first] = text[second];
  		text[second] = temp;
		data.swapParts(first, second); 
		data.swapInLinked(first, second, false); 
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(),  "Touches on a cell with the sort characters tool", "[column touched][row touched]", commandName, "charSortTouch")) {
	   	 		if (data == null)
	   	 			return null;
	    	 	if (data.isEditInhibited()){
					discreetAlert("This matrix is marked as locked against editing. To unlock, uncheck the menu item Matrix>Current Matrix>Editing Not Permitted");
	    	 		return null;
	    	 	}
//	   	 		if (data.isMolecularSequence())
//	   	 			if (!AlertDialog.query(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Sort Sites?", "These are molecular sequences. Are you sure you want to reorder the sites?  It cannot be undone.", "Sort", "Cancel", 2))
//	   	 				return null;
	   	 		MesquiteInteger io = new MesquiteInteger(0);
	   			int column= MesquiteInteger.fromString(arguments, io);
	   			int row= MesquiteInteger.fromString(arguments, io);
	   			boolean gT = true;
    				if (arguments.indexOf("option")>=0)
    					gT = false;
	   			if (!MesquiteInteger.isCombinable(column) && !MesquiteInteger.isCombinable(row))
	   				return null;


	   			boolean noneSelected = !data.anySelected();
	   			UndoInstructions undoInstructions = new UndoInstructions(UndoInstructions.PARTS_MOVED,data);
	   			undoInstructions.recordPreviousOrder(data);
	   			UndoReference undoReference = new UndoReference(undoInstructions, this);

				if (column>=0 && row >=0) {
					long[] fullChecksumBefore =data.getIDOrderedFullChecksum();
					String[] text = new String[data.getNumChars()];
					for (int i=0; i<data.getNumChars(); i++)
						text[i] = table.getMatrixText(i, row);
					for (int i=1; i<data.getNumChars(); i++) {
							if (i % 10 == 0) CommandRecord.tick("Sorting from character " + i);
							for (int j= i-1; j>=0 && compare(gT, text[j], text[j+1]); j--) {
								if (noneSelected || (data.getSelected(j) && data.getSelected(j+1))){
									swapParts(data, j, j+1, text);
							}
							}
						
					}
					CommandRecord.tick("Sorting finished");
					long[] fullChecksumAfter =data.getIDOrderedFullChecksum();
					data.compareChecksums(fullChecksumBefore, fullChecksumAfter, true, "sorting of characters");
					data.notifyListeners(this, new Notification(MesquiteListener.PARTS_MOVED, undoReference));
					data.notifyInLinked(new Notification(MesquiteListener.PARTS_MOVED, undoReference));    //TODO: will this work in linked?????
				}
				else if (row == -1 && column >=0) { //row names selected; sort by taxon name
					long[] fullChecksumBefore =data.getIDOrderedFullChecksum();
					for (int i=1; i<data.getNumChars(); i++) {
						if (i % 10 == 0) CommandRecord.tick("Sorting from character " + i);
						for (int j= i-1; j>=0 && compare(gT, data.getCharacterName(j), data.getCharacterName(j+1)); j--) {
							data.swapParts(j, j+1);
							data.swapInLinked(j, j+1, false);
						}
					}
					CommandRecord.tick("Sorting finished");
					long[] fullChecksumAfter =data.getIDOrderedFullChecksum();
					data.compareChecksums(fullChecksumBefore, fullChecksumAfter, true, "sorting of characters");
					data.notifyListeners(this, new Notification(MesquiteListener.PARTS_MOVED, undoReference));
					data.notifyInLinked(new Notification(MesquiteListener.PARTS_MOVED));  
				}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Character Sort (data)";
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Provides a tool with which to sort characters automatically.";
   	 }
}


	


