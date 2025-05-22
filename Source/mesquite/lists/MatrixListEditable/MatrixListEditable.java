/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.MatrixListEditable;
/* created May 02 */

import mesquite.lists.lib.*;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class MatrixListEditable extends CharMatricesListAssistant implements MesquiteListener {
	/*.................................................................................................................*/
	public String getName() {
		return "Editability of Matrix";
	}

	public String getExplanation() {
		return "Indicates whether editing of the matrix is allowed or inhibited." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 310;  
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem("Allow Editing", makeCommand("unlockMatrix", this));
		addMenuItem("Inhibit Editing", makeCommand("lockMatrix", this));
		return true;
	}
	MesquiteTable table;
	public void setTableAndObject(MesquiteTable table, Object obj){
		this.table = table;
		if (datas !=null)
			datas.removeListener(this);
		if (obj instanceof ListableVector)
			this.datas = (ListableVector)obj;
		datas.addListener(this);
	}
	
	public Object setEditorInhibition(boolean value) {
		if (datas == null || table == null)
			return null;
		boolean changed = false;
		for (int ir = 0; ir<table.getNumRows(); ir++){
			if (table.isRowSelected(ir)){
				if (ir>= datas.size())
					return null;
				CharacterData data =((CharacterData)datas.elementAt(ir));
				if (value)
					data.incrementEditInhibition();
				else
					data.decrementEditInhibition();
				changed = true;
			}
		}
		if (changed){
		parametersChanged(null);
		getProject().getCoordinatorModule().refreshProjectWindow();
		resetAllMenuBars();
		}
		return table;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {

		if (checker.compare(this.getClass(), "Inhibits selected matrices from being edited", null, commandName, "lockMatrix")) {
			Object obj = setEditorInhibition(true);
			if (obj==null)
				return null;
		}
		else if (checker.compare(this.getClass(), "Allows selected matrices to be edited", null, commandName, "unlockMatrix")) {
			Object obj = setEditorInhibition(false);
			if (obj==null)
				return null;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		//TODO: respond
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //TODO: respond
	}
	public void changed(Object caller, Object obj, Notification notification){
		parametersChanged(notification);
	}
	public String getTitle() {
		return "Editable?";
	}
	public String getStringForRow(int ic){
		try {
			if (datas ==null || ic<0 || ic>= datas.size())
				return "";
			CharacterData data =((CharacterData)datas.elementAt(ic));
			if (data==null)
				return "";
			if (data.isEditInhibited()){
				return "Inhibited";
			}
				return "Editable";
		}
		catch (NullPointerException e){
			return "";
		}
	}
	


	public String getWidestString(){
		
		return " 888888";

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
	public void endJob() {
		if (datas !=null)
			datas.removeListener(this);
		super.endJob();
	}

}

