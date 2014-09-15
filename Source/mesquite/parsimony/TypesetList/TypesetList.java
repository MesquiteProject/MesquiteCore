/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.parsimony.TypesetList;
/*~~  */

import mesquite.lists.lib.*;
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.parsimony.lib.*;

/* ======================================================================== */
public class TypesetList extends DataSpecssetList {
	public String getName() {
		return "List of Parsimony Model Sets";
	}
	public String getExplanation() {
		return "Makes a window listing parsimony model sets." ;
	}
	/*.................................................................................................................*/
	public int currentDataSet = 0;
	public CharacterData data = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		makeMenu("List");
		addMenuItem("Make New Parsimony Model Set...", makeCommand("newTypeset",  this));
		return true;
	}
	/** returns a String of annotation for a row*/
	public String getAnnotation(int row){ return null;}

	/** sets the annotation for a row*/
	public void setAnnotation(int row, String s, boolean notify){}
	public Class getItemType(){
		return ParsimonyModelSet.class;
	}
	public String getItemTypeName(){
		return "Parsimony model set";
	}
	public String getItemTypeNamePlural(){
		return "Parsimony model sets";
	}
	public SpecsSet makeNewSpecsSet(CharacterData data){
		if (data!=null)
			return new ParsimonyModelSet("Parsimony Model Set", data.getNumChars(),data.getDefaultModel("Parsimony"), data);
		return null;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Instructs user as how to make new parsimony model set (TYPESET)", null, commandName, "newTypeset")){
			Object obj = getMainObject();
			if (!(obj instanceof CharacterData))
				return null;
			CharacterData data = (CharacterData)obj;
			if (data !=null &&AlertDialog.query(containerOfModule(), "New Model Set", "To make a new parsimony model set, go to the List of Characters window, make sure that a column for Current Parsimony Model appears, edit the column, then save the model set.  Would you like to go to the List of Characters window now?", "OK", "Cancel")) {
				Object obj2 = data.doCommand("showMe", null, checker);
				if (obj2 !=null && obj2 instanceof Commandable){
					Commandable c = (Commandable)obj2;
					c.doCommand("newAssistant", "#CharListParsModels", checker);
				}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive() {
		return false;
	}
	/*.................................................................................................................*/





}


