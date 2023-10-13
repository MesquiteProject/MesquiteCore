/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.CodesetList;
/*~~  */

import mesquite.lists.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.molec.lib.*;

/* ======================================================================== */
public class CodesetList extends DataSpecssetList {
	public int currentDataSet = 0;
	public CharacterData data = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		makeMenu("List");
		addMenuItem("Make New Genetic Code Set...", makeCommand("newCodeset",  this));
		return true;
	}
	/** returns a String of annotation for a row*/
	public String getAnnotation(int row){ return null;}

	/** sets the annotation for a row*/
	public void setAnnotation(int row, String s, boolean notify){}
	public Class getItemType(){
		return GenCodeModelSet.class;
	}
	public String getItemTypeName(){
		return "Genetic code set";
	}
	public String getItemTypeNamePlural(){
		return "Genetic code sets";
	}
	public SpecsSet makeNewSpecsSet(CharacterData data){
		if (data!=null)
			return new GenCodeModelSet("Genetic Code Set", data.getNumChars(),data.getDefaultModel("GeneticCode"), data);
		return null;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Instructs user as how to make new genetic code set (CODESET)", null, commandName, "newCodeset")){
			Object obj = getCharacterMatrix();
			if (!(obj instanceof CharacterData))
				return null;
			CharacterData data = (CharacterData)obj;
			if (data !=null &&AlertDialog.query(containerOfModule(), "New Model Set", "To make a new genetic code set, go to the List of Characters window, make sure that a column for Current Genetic Code appears, edit the column, then save the model set.  Would you like to go to the List of Characters window now?", "OK", "Cancel")) {
				Object obj2 = data.doCommand("showMe", null, checker);
				if (obj2 !=null && obj2 instanceof Commandable){
					Commandable c = (Commandable)obj2;
					c.doCommand("newAssistant", "#CharListGenCodeModels", checker);
				}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "List of Genetic Code Sets";
	}

	/*.................................................................................................................*/
	public boolean isSubstantive() {
		return false;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 110;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Makes windows listing genetic code sets." ;
	}
	/*.................................................................................................................*/




}


