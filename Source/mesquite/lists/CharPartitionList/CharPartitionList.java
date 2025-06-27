/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.CharPartitionList;
/*~~  */

import mesquite.lib.CommandChecker;
import mesquite.lib.Commandable;
import mesquite.lib.SpecsSet;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterPartition;
import mesquite.lib.ui.AlertDialog;
import mesquite.lists.lib.DataSpecssetList;

/* ======================================================================== */
public class CharPartitionList extends DataSpecssetList {
	/*.................................................................................................................*/
	public String getName() {
		return "List of Character Partitions";
	}
	public String getExplanation() {
		return "Makes windows listing character partitions." ;
	}
	/*.................................................................................................................*/
	public int currentDataSet = 0;
	public CharacterData data = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		makeMenu("List");
		addMenuItem("Make New Character Partition...", makeCommand("newCharPartition",  this));
		return true;
	}
	/** returns a String of annotation for a row*/
	public String getAnnotation(int row){ return null;}

	/** sets the annotation for a row*/
	public void setAnnotation(int row, String s, boolean notify){}
	public Class getItemType(){
		return CharacterPartition.class;
	}
	public String getItemTypeName(){
		return "Character partition";
	}
	public String getItemTypeNamePlural(){
		return "Character partitions";
	}
	public SpecsSet makeNewSpecsSet(CharacterData data){
		if (data!=null)
			return new CharacterPartition("Partition", data.getNumChars(), null, data);
		return null;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Instructs user as how to make new character partition", null, commandName, "newCharPartition")){
			Object obj = getCharacterMatrix();
			if (!(obj instanceof CharacterData))
				return null;
			CharacterData data = (CharacterData)obj;
			if (data !=null &&AlertDialog.query(containerOfModule(), "New Partition", "To make a new partition of characters, go to the List of Characters window, make sure that a column for Current Partition appears, edit the column, then save the partition.  Would you like to go to the List of Characters window now?", "OK", "Cancel")) {
				Object obj2 = data.doCommand("showMe", null, checker);
				if (obj2 !=null && obj2 instanceof Commandable){
					Commandable c = (Commandable)obj2;
					c.doCommand("newAssistant", "#CharListPartition", checker);
				}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
}


