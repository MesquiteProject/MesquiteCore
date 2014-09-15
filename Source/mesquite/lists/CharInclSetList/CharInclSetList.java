/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.CharInclSetList;
/*~~  */

import mesquite.lists.lib.*;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class CharInclSetList extends DataSpecssetList {
	/*.................................................................................................................*/
	public String getName() {
		return "List of Character Inclusion Sets";
	}
	public String getExplanation() {
		return "Makes windows listing character sets." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(CharInclSetListAsst.class, "The List of Character Inclusion Sets window can display columns showing information for each character inclusion set.",
		"You can request that columns be shown using the Columns menu of the List of Character Inclusion Sets Window. ");
	}
	/*.................................................................................................................*/
	public int currentDataSet = 0;
	public CharacterData data = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		makeMenu("List");
		addMenuItem("Make New Inclusion Set...", makeCommand("newInclusionSet",  this));
		return true;
	}
	public Class getItemType(){
		return CharInclusionSet.class;
	}
	/** returns a String of annotation for a row*/
	public String getAnnotation(int row){ return null;}

	/** sets the annotation for a row*/
	public void setAnnotation(int row, String s, boolean notify){}
	public String getItemTypeName(){
		return "Character inclusion set";
	}
	public String getItemTypeNamePlural(){
		return "Character inclusion sets";
	}
	public SpecsSet makeNewSpecsSet(CharacterData data){
		if (data!=null)
			return new CharInclusionSet("Inclusion Set", data.getNumChars(), data);
		return null;
	}
	/*.................................................................................................................*/
	public void showListWindow(Object obj){ ///TODO: change name to makeLIstWindow
		super.showListWindow(obj);
		CharInclSetListAsst assistant = (CharInclSetListAsst)hireNamedEmployee(CharInclSetListAsst.class, "#CharInclSetListNum");
		if (assistant!= null){
			((ListWindow)getModuleWindow()).addListAssistant(assistant);
			assistant.setUseMenubar(false);
		}
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Instructs user as how to make new character inclusion set", null, commandName, "newInclusionSet")){
			Object obj = getMainObject();
			if (!(obj instanceof CharacterData))
				return null;
			CharacterData data = (CharacterData)obj;
			if (data !=null &&AlertDialog.query(containerOfModule(), "New Partition", "To make a new character inclusion set, go to the List of Characters window, make sure that a column for Inclusion appears, edit the column, then save the inclusion set.  Would you like to go to the List of Characters window now?", "OK", "Cancel")) {
				Object obj2 = data.doCommand("showMe", null, checker);
				if (obj2 !=null && obj2 instanceof Commandable){
					Commandable c = (Commandable)obj2;
					c.doCommand("newAssistant", "#CharListInclusion", checker);
				}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
}


