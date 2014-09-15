/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.CharListUniqueID;
/*~~  */

import mesquite.lists.lib.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class CharListUniqueID extends CharListAssistant  {
	/*.................................................................................................................*/
	public String getName() {
		return "Unique ID (characters)";
	}
	public String getExplanation() {
		return "Shows unique id assigned to character." ;
	}

	CharacterData data=null;
	MesquiteTable table = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		/* hire employees here */
		addMenuItem("Replace Unique IDs", makeCommand("replaceIDs",  this));
		addMenuItem("Fill Missing Unique IDs", makeCommand("fillMissingIDs",  this));
		addMenuItem("Remove Unique IDs", makeCommand("removeIDs",  this));
		return true;
	}

	public void setTableAndData(MesquiteTable table, CharacterData data){
		//if (this.data !=null)
		//	this.data.removeListener(this);
		this.data = data;
		//data.addListener(this);
		this.table = table;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Replaces the uniqueIDs of selected", null, commandName, "replaceIDs")) {
			if (data != null) {
				stamp(true);
			}
			//outputInvalid();
		}
		else if (checker.compare(this.getClass(), "Removes the uniqueIDs of selected", null, commandName, "removeIDs")) {
			if (data != null) {
				remove();
			}
			//outputInvalid();
		}
		else if (checker.compare(this.getClass(), "Fills in any missing uniqueIDs of selected", null, commandName, "fillMissingIDs")) {
			if (data != null)
				stamp(false);
			//outputInvalid();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	void stamp(boolean replace){
		if (data ==null)
			return;
		boolean changed=false;
		boolean noneSelected = !table.anyCellSelectedAnyWay();
		if (employer!=null && employer instanceof ListModule) {
			int c = ((ListModule)employer).getMyColumn(this);
			for (int i=0; i<data.getNumChars(); i++) {
				if (noneSelected || table.isCellSelectedAnyWay(c, i)) {
					data.stampUniqueID(i, replace);
					if (!changed)
						outputInvalid();
					changed = true;
				}
			}
		}
		if (changed)
			parametersChanged();
	}
	void remove(){
		if (data ==null)
			return;
		boolean changed=false;
		boolean noneSelected = !table.anyCellSelectedAnyWay();
		if (employer!=null && employer instanceof ListModule) {
			int c = ((ListModule)employer).getMyColumn(this);
			for (int i=0; i<data.getNumChars(); i++) {
				if (noneSelected || table.isCellSelectedAnyWay(c, i)) {
					data.setUniqueID(i, null);
					if (!changed)
						outputInvalid();
					changed = true;
				}
			}
		}
		if (changed)
			parametersChanged();
	}
	public String getStringForCharacter(int ic){
		String s = null;
		if (data != null)
			s = data.getUniqueID(ic); //+1 because zero based
		if (StringUtil.blank(s))
			s = "?";
		return s;
	}
	public String getWidestString(){
		return "88888888888888888";
	}
	/*.................................................................................................................*/
	public String getTitle() {
		return "ID";
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


}

