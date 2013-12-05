/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison. 
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.WeightSetList;
/*~~  */

import mesquite.lists.lib.*;
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class WeightSetList extends DataSpecssetList {
	/*.................................................................................................................*/
	public String getName() {
		return "List of Character Weight Sets";
	}

	public String getExplanation() {
		return "Makes windows listing character weight sets." ;
	}
	/*.................................................................................................................*/
	public int currentDataSet = 0;
	public CharacterData data = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		makeMenu("List");
		addMenuItem("Make New Weight Set...", makeCommand("newWeightSet",  this));
		return true;
	}

	/** returns a String of annotation for a row*/
	public String getAnnotation(int row){ return null;}

	/** sets the annotation for a row*/
	public void setAnnotation(int row, String s, boolean notify){}

	public Class getItemType(){
		return CharWeightSet.class;
	}
	public String getItemTypeName(){
		return "Character weight set";
	}
	public String getItemTypeNamePlural(){
		return "Character weight sets";
	}
	public SpecsSet makeNewSpecsSet(CharacterData data){
		if (data!=null)
			return new CharWeightSet("Weight Set", data.getNumChars(), data);
		return null;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Instructs user as how to make new character weight set", null, commandName, "newWeightSet")){
			Object obj = getMainObject();
			if (!(obj instanceof CharacterData))
				return null;
			CharacterData data = (CharacterData)obj;
			if (data !=null &&AlertDialog.query(containerOfModule(), "New Weight Set", "To make a new charcter weight set, go to the List of Characters window, make sure that a column for Current Weight Set appears, edit the column, then save the weight set.  Would you like to go to the List of Characters window now?", "OK", "Cancel")) {
				Object obj2 = data.doCommand("showMe", null, checker);
				if (obj2 !=null && obj2 instanceof Commandable){
					Commandable c = (Commandable)obj2;
					c.doCommand("newAssistant", "#CharListWeights", checker);
				}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}


}


