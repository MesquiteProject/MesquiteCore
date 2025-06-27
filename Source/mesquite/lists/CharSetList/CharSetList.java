/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.CharSetList;
/*~~  */

import mesquite.lib.CommandChecker;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.SpecsSet;
import mesquite.lib.characters.CharSelectionSet;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.ui.AlertDialog;
import mesquite.lists.lib.CharSetListAsst;
import mesquite.lists.lib.DataSpecsListWindow;
import mesquite.lists.lib.DataSpecssetList;
import mesquite.lists.lib.ListWindow;

/* ======================================================================== */
public class CharSetList extends DataSpecssetList {
	/*.................................................................................................................*/
	public String getName() {
		return "List of Character Sets";
	}
	public String getExplanation() {
		return "Makes windows listing character sets." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(CharSetListAsst.class, "The List of Character Sets window can display columns showing information for each character set.",
		"You can request that columns be shown using the Columns menu of the List of Character Sets Window. ");
	}
	/*.................................................................................................................*/
	public int currentDataSet = 0;
	public CharacterData data = null;
	DataSpecsListWindow window;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		makeMenu("List");
		addMenuItem("Make New Character Set...", makeCommand("newCharSet",  this));
		return true;
	}
	/** returns a String of annotation for a row*/
	public String getAnnotation(int row){ return null;}

	/** sets the annotation for a row*/
	public void setAnnotation(int row, String s, boolean notify){}
	/*.................................................................................................................*/
	public void showListWindow(Object obj){ ///TODO: change name to makeLIstWindow
		super.showListWindow(obj);
		CharSetListAsst assistant = (CharSetListAsst)hireNamedEmployee(CharSetListAsst.class, "#CharSetListNum");
		if (assistant!= null){
			((ListWindow)getModuleWindow()).addListAssistant(assistant);
			assistant.setUseMenubar(false);
		}
	}
	public boolean usesCurrentSet(){
		return false;
	}
	public Class getItemType(){
		return CharSelectionSet.class;
	}
	public String getItemTypeName(){
		return "Character set";
	}
	public String getItemTypeNamePlural(){
		return "Character sets";
	}
	public SpecsSet makeNewSpecsSet(CharacterData data){
		if (data!=null)
			return new CharSelectionSet("Character Set", data.getNumChars(), data);
		return null;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Instructs user as how to make new character set (CHARSET)", null, commandName, "newCharSet")){
			Object obj = getCharacterMatrix();
			if (!(obj instanceof CharacterData))
				return null;
			CharacterData data = (CharacterData)obj;
			if (data !=null &&AlertDialog.query(containerOfModule(), "New Character Set", "To make a new character set (CHARSET), go to the List of Characters window, select the characters you want to be part of the set, and choose Save Selected as Set.  Would you like to go to the List of Characters window now?", "OK", "Cancel")) {
				Object obj2 = data.doCommand("showMe", null, checker);
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
}


