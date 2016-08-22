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

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import mesquite.lib.characters.ProbabilityModelSet;
import mesquite.lib.duties.*;
import mesquite.lib.*;
import mesquite.lib.table.*;


/* ======================================================================== */
public abstract class TaxaSpecssetList extends ListModule {
	public Taxa taxa = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		//windows = new Vector();
		return true;
  	 }
	/*.................................................................................................................*/
  	 public boolean showing(Object obj){
	 	if (obj instanceof String) {
    	 		String arguments = (String)obj;
    	 		Taxa queryTaxa = getProject().getTaxa(null, parser.getFirstToken(arguments));
    	 		return (queryTaxa == taxa && getModuleWindow()!=null);
	 	}
	 	else if (obj instanceof Taxa) {
    	 		return (obj == taxa && getModuleWindow()!=null);
	 	}
	 	return false;
  	 }
  	 
	public abstract SpecsSet makeNewSpecsSet(Taxa taxa);
	/*.................................................................................................................*/
  	 public void showListWindow(Object obj){ ///TODO: change name to makeLIstWindow
	 	if (obj instanceof Taxa) {
			taxa = (Taxa)obj;
	 	}
 		if (taxa==null){
 			alert("Sorry, no taxa set found for list window");
 			iQuit();
  			return;
 		}
 		taxa.addListener(this);
 		setModuleWindow(new TaxaSpecsListWindow(this, taxa));

		addMenuItem("Store current set", makeCommand("storeCurrent",  this));
		addMenuItem("Set stored to current", makeCommand("setToCurrent",  this));
		addMenuItem("Replace stored set by current", makeCommand("replaceWithCurrent",  this));
 		addMenuSeparator();
 		resetContainingMenuBar();
		resetAllWindowsMenus();
  	 }
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (taxa== obj) {
			iQuit();
		}
	}
	/*.................................................................................................................*/
	public void endJob(){
			if (taxa!=null) {
				taxa.removeListener(this);
			}
			super.endJob();
	}
	/*.................................................................................................................*/
	/* following required by ListModule*/
  	 public Object getMainObject(){
  	 	return taxa;
  	 }
  	 public int getNumberOfRows(){
  	 	if (taxa==null)
  	 		return 0;
  	 	else
  	 		return taxa.getNumSpecsSets(getItemType());
  	 }
	public Class getAssistantClass(){
		return null;//return CharListAssistant.class;
	}
	public abstract Class getItemType();
	/*.................................................................................................................*/
	public boolean rowsDeletable(){
		return true;
	}
	public boolean deleteRow(int row, boolean notify){
		if (taxa!=null) {
			SpecsSet ss = taxa.getSpecsSet(row, getItemType());
			if (ss!=null) {
				taxa.removeSpecsSet(ss,getItemType());
				return true;
			}
		}
		return false;
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	if (getModuleWindow()==null || !getModuleWindow().isVisible())
   	 		return null;
   	 	Snapshot temp = new Snapshot();
		

  	 	temp.addLine("setTaxa " + getProject().getTaxaReferenceExternal(taxa)); 
  	 	
      	 	if (getModuleWindow()!=null)
			getModuleWindow().incorporateSnapshot(temp, file);
  	 	//if (window!=null && !window.isVisible())
  	 	temp.addLine("showWindow"); 
 	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets the taxa block", "[block reference, number, or name]", commandName, "setTaxa")) {
   	 		Taxa t = getProject().getTaxa(checker.getFile(), parser.getFirstToken(arguments));
   	 		if (getModuleWindow()!=null && t!=null){
	   	 		//if (taxa!=null)
	   	 		//	taxa.removeListener(this);
	   	 		taxa = t;
	   	 		//if (taxa!=null)
	   	 		//	taxa.addListener(this);
    	 			((ListWindow)getModuleWindow()).setCurrentObject(taxa);
    	 			((ListWindow)getModuleWindow()).repaintAll();
	   	 		return taxa;
   	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Returns the current taxa block", null, commandName, "getTaxa")) {
    	 		return ((ListWindow)getModuleWindow()).getCurrentObject();
    	 	}
    	 	else if (checker.compare(this.getClass(), "Stores the current specification set", null, commandName, "storeCurrent")) {
    	 		if (taxa!=null){
    	 			SpecsSetVector ssv = taxa.getSpecSetsVector(getItemType());
    	 			if (ssv == null || ssv.getCurrentSpecsSet() == null) {
			 		SpecsSet specsSet= makeNewSpecsSet(taxa);
			 		specsSet.addToFile(taxa.getFile(), getProject(), findElementManager(getItemType())); 
					taxa.setCurrentSpecsSet(specsSet, getItemType());
					ssv = taxa.getSpecSetsVector(getItemType());
    	 			}
    	 			if (ssv!=null) {
    	 				SpecsSet s = ssv.storeCurrentSpecsSet();
    					if (s.getFile() == null)
    						s.addToFile(taxa.getFile(), getProject(), findElementManager(getItemType()));
   	 				if (s!=null)
    	 					s.setName(ssv.getUniqueName(getItemTypeName()));
    	 				else
    	 					alert("Error: could not make " + getItemTypeName());
    	 				ssv.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));  
    	 				//MesquiteTable t = ((ListWindow)getModuleWindow()).getTable(); //handled via listeners
    	 				//t.setNumRows(t.getNumRows()+1);
    	 				//t.repaintAll();
    	 			}
     	 			else MesquiteMessage.warnProgrammer("storeCurrent in DataSpecs: sorry, can't store because no specssetvector");
   	 		}
    	 		//return ((ListWindow)getModuleWindow()).getCurrentObject();
    	 	}
    	 	else if (checker.compare(this.getClass(), "Replaces a stored character specification set by the current one", null, commandName, "replaceWithCurrent")) {
    	 		if (taxa!=null){
    	 			MesquiteTable t = ((ListWindow)getModuleWindow()).getTable();
    	 			int numRows = t.numRowsSelected();
    	 			if (numRows>1) 
    	 				alert("Exactly one row must be selected to indicate which stored " + getItemTypeName() + " is to be replaced by the current one");
    	 			else  {
    	 				SpecsSetVector ssv = taxa.getSpecSetsVector(getItemType());
  	 				if (ssv!=null) {
  	 					SpecsSet chosen;
	  	 				if (numRows==0)
    	 						chosen = (SpecsSet)ListDialog.queryList(containerOfModule(), "Replace stored set", "Choose stored " + getItemTypeName() + " to replace by current set", MesquiteString.helpString,ssv, 0);
	    	 				else {
	    	 					int f = t.firstRowSelected();
		    	 				if (f<0)
		    	 					return null;
		    	 				chosen = ssv.getSpecsSet(f);
		    	 			}
		    	 			if (chosen!=null){
		    	 				SpecsSet current = ssv.getCurrentSpecsSet();
		    	 				ssv.replaceStoredSpecsSet(chosen, current);
		    	 				t.repaintAll();
		    	 			}
	    	 			}
    	 			}
    	 		}
    	 		return ((ListWindow)getModuleWindow()).getCurrentObject();
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the selected specification set to be the current one", null, commandName, "setToCurrent")) {
    	 		if (taxa!=null){
    	 			MesquiteTable t = ((ListWindow)getModuleWindow()).getTable();
    	 			int numRows = t.numRowsSelected();
    	 			if (numRows>1) 
    	 				alert("Exactly one row must be selected to indicate which stored " + getItemTypeName() + " is to be set as the current one");
    	 			else  {
    	 				SpecsSetVector ssv = taxa.getSpecSetsVector(getItemType());
  	 				if (ssv!=null) {
  	 					SpecsSet chosen;
	  	 				if (numRows==0)
    	 						chosen = (SpecsSet)ListDialog.queryList(containerOfModule(), "Set to current", "Choose which stored " + getItemTypeName() + " is to be set as the current one",MesquiteString.helpString, ssv, 0);
	    	 				else {
	    	 					int f = t.firstRowSelected();
		    	 				if (f<0)
		    	 					return null;
		    	 				chosen = ssv.getSpecsSet(f);
		    	 			}
		    	 			if (chosen!=null){
		    	 				ssv.setCurrentSpecsSet(chosen.cloneSpecsSet()); 
		    	 				taxa.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED)); //TODO: bogus! should notify via specs not taxa???
    	 						t.repaintAll();
		    	 				return chosen;
		    	 			}
	    	 			}
    	 			}
    	 		}
    	 		return ((ListWindow)getModuleWindow()).getCurrentObject();
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/*.................................................................................................................*/
   	 
   	 
	/*.................................................................................................................*/
	/** Requests a window to close.  In the process, subclasses of MesquiteWindow might close down their owning MesquiteModules etc.*/
 	public void windowGoAway(MesquiteWindow whichWindow) {
 			//Debug.println("disposing of window");
		if (whichWindow == null)
			return;
			whichWindow.hide();
	}
   	 
   	 
}



