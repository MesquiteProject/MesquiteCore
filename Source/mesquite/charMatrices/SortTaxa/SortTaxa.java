/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.SortTaxa; 


import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.misc.MesquiteCollator;
import mesquite.lib.table.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;

/* ======================================================================== */
public class SortTaxa extends DataWindowAssistantI {
	TableTool taxaSortTool; 
	MesquiteTable table;
	CharacterData data;
	Taxa taxa;
	MesquiteCollator collator;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		if (containerOfModule() instanceof MesquiteWindow) {
			taxaSortTool = new TableTool(this, "taxaSort", getPath() , "taxaSort.gif", 8,2,"Sort taxa with column touched", "This tool sorts the taxa according to the column touched.", MesquiteModule.makeCommand("taxaSortTouch",  this) , null, null);
			taxaSortTool.setOptionImageFileName("revTaxaSort.gif", 8, 13);
			taxaSortTool.setWorksOnRowNames(true);
			((MesquiteWindow)containerOfModule()).addTool(taxaSortTool);
		}
		else return false;
		collator = new MesquiteCollator();
		return true;
	}
	 public String getFunctionIconPath(){
   		 return getPath() + "taxaSort.gif";
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
		if (data !=null) {
			this.taxa = data.getTaxa();
		}
		this.data = data;
		data.addListener(this);
		inhibitionChanged();
	}

	/* ................................................................................................................. */
	void inhibitionChanged(){
		if (taxaSortTool!=null && data!=null)
			taxaSortTool.setEnabled(!data.isEditInhibited());
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
	void swapParts(Taxa taxa, int first, int second, String[] text){  
  		String temp = text[first];
  		text[first] = text[second];
  		text[second] = temp;
		taxa.swapParts(first, second, true); 
	}
	MesquiteInteger pos = new MesquiteInteger();
	 
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Touches on a cell with the taxa sort tool to sort the taxa according to the values in the column touched", "[column touched][row touched]", commandName, "taxaSortTouch")) {
	   	 		if (taxa == null)
	   	 			return null;
	   	 		MesquiteInteger io = new MesquiteInteger(0);
	   			boolean gT = true;
    				if (arguments.indexOf("option")>=0)
    					gT = false;
	   			int column= MesquiteInteger.fromString(arguments, io);
	   			int row= MesquiteInteger.fromString(arguments, io);
	   			if (!MesquiteInteger.isCombinable(column) && !MesquiteInteger.isCombinable(row))
	   				return null;
	   			boolean noneSelected = !taxa.anySelected();

	   			UndoInstructions undoInstructions = new UndoInstructions(UndoInstructions.PARTS_MOVED,taxa);
	   			undoInstructions.recordPreviousOrder(taxa);
	   			UndoReference undoReference = new UndoReference(undoInstructions, this);
				
				if (column>=0 && row >=0) {
					String[] text = new String[taxa.getNumTaxa()];
					for (int i=0; i<taxa.getNumTaxa(); i++)
						text[i] = table.getMatrixText(column, i);
					for (int i=1; i<taxa.getNumTaxa(); i++) {
						if (i % 10 == 0) CommandRecord.tick("Sorting from taxon " + i);
						for (int j= i-1; j>=0 && compare(gT, text[j], text[j+1]); j--) {
							if (noneSelected || (taxa.getSelected(j) && taxa.getSelected(j+1)))
								swapParts(taxa, j, j+1, text);
						}
					}
					CommandRecord.tick("Sorting finished");
					taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_MOVED,undoReference));
				}
				else if (column == -1 && row >=0) { //row names selected; sort by taxon name
					for (int i=1; i<taxa.getNumTaxa(); i++) {
						if (i % 10 == 0) CommandRecord.tick("Sorting from taxon " + i);
						for (int j= i-1; j>=0 && compare(gT, taxa.getTaxonName(j), taxa.getTaxonName(j+1)); j--) {
							taxa.swapTaxa(j, j+1, false);
						}
					}
					CommandRecord.tick("Sorting finished");
					taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_MOVED,undoReference));
				}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Sort Taxa (data)";
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Provides a tool with which to sort taxa automatically.";
   	 }
}


	


