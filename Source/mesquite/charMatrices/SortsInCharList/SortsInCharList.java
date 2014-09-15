/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.SortsInCharList;
/*~~  */

import mesquite.lists.lib.*;
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class SortsInCharList extends CharListAssistant {
	CharacterData data=null;
	MesquiteTable table=null;
	MesquiteMenuItemSpec mScs, mStc, mRssc;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
  	 }
  	 MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Stores current character order (CHARSORTING)", null, commandName, "storeCurrent")) {
    	 		if (data!=null){
    	 			SpecsSetVector ssv = data.getSpecSetsVector(CharSort.class);
    	 			if (ssv == null || ssv.getCurrentSpecsSet() == null) {
			 		CharSort sort= new CharSort("Character Order", data.getNumChars(), data);
			 		for (int i = 0; i<data.getNumChars(); i++)
			 			sort.setValue(i, i+1);
			 		sort.addToFile(data.getFile(), getProject(), findElementManager(CharSort.class)); //THIS
					data.setCurrentSpecsSet(sort, CharSort.class);
					ssv = data.getSpecSetsVector(CharSort.class);
    	 				sort.setName(ssv.getUniqueName("Character Order"));
	 			}
	 			if (ssv!= null){
    	 				CharSort sorting =  (CharSort)ssv.storeCurrentSpecsSet();
    					if (sorting.getFile() == null)
    						sorting.addToFile(data.getFile(), getProject(), findElementManager(CharSort.class));
  	 				sorting.setName(ssv.getUniqueName("Character Order"));
		 			String name = MesquiteString.queryString(containerOfModule(), "Name", "Name of Character order to be stored", sorting.getName());
    	 				if (!StringUtil.blank(name))
    	 					sorting.setName(name);
			 		for (int i = 0; i<data.getNumChars(); i++)
			 			sorting.setValue(i, i+1);
    	 				ssv.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  
    	 				data.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  
			 	}
   	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Replace stored Character Order (CHARSORTING) by the current one", null, commandName, "replaceWithCurrent")) {
    	 		if (data!=null){
    	 				SpecsSetVector ssv = data.getSpecSetsVector(CharSort.class);
  	 				if (ssv!=null) {
  	 					SpecsSet chosen = (SpecsSet)ListDialog.queryList(containerOfModule(), "Replace stored order", "Choose stored Character Order to replace by current order", MesquiteString.helpString,ssv, 0);
		    	 			if (chosen!=null){
					 		CharSort sorting= new CharSort(chosen.getName(), data.getNumChars(), data);
					 		for (int i = 0; i<data.getNumChars(); i++)
					 			sorting.setValue(i, i+1);
		    	 				ssv.replaceStoredSpecsSet(chosen, sorting);
							data.setCurrentSpecsSet(sorting, CharSort.class);
		    	 				data.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED)); //TODO: bogus! should notify via specs not data???
		    	 			}
	    	 			}
    	 			
    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Loads the stored Character Order to be the current one", "[number of Character Order to load]", commandName, "loadToCurrent")) {
 			if (data !=null) {
 				int which = MesquiteInteger.fromFirstToken(arguments, stringPos);
 				if (MesquiteInteger.isCombinable(which)){
		 			SpecsSetVector ssv = data.getSpecSetsVector(CharSort.class);
					if (ssv!=null) {
						SpecsSet chosen = ssv.getSpecsSet(which);
		    	 			if (chosen!=null){
		    	 				ssv.setCurrentSpecsSet(chosen.cloneSpecsSet()); 
		    	 				data.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED)); //TODO: bogus! should notify via specs not data???
		    	 				return chosen;
		    	 			}
			 		}
 				}
	 		}
	    	 }
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
   
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		deleteMenuItem(mScs);
		deleteMenuItem(mRssc);
		deleteMenuItem(mStc);
		mScs = addMenuItem("Store current order", makeCommand("storeCurrent",  this));
		mRssc = addMenuItem("Replace stored order by current", makeCommand("replaceWithCurrent",  this));
		if (data !=null)
			mStc = addSubmenu(null, "Load character order", makeCommand("loadToCurrent",  this), data.getSpecSetsVector(CharSort.class));
		this.data = data;
		this.table = table;
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		outputInvalid();
		parametersChanged(notification);
	}
	public String getTitle() {
		return "Stored Order";
	}
	public String getStringForCharacter(int ic){
		if (data!=null) {
			CharSort sorting = (CharSort)data.getCurrentSpecsSet(CharSort.class);
			if (sorting != null) {
				return sorting.toString(ic);
			}
			else {
				return "?";
			}
		}
		return "?";
	}
	public String getWidestString(){
		return "888888888  ";
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Stored Character Order";
   	 }
	/*.................................................................................................................*/
    	 public String getVeryShortName() {
		return "Stored Order";
   	 }
 	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
  	 
	/*.................................................................................................................*/
   	public boolean isPrerelease(){
   		return false;  
   	}
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Supplies current order applied to characters for character list window." ;
   	 }
}

