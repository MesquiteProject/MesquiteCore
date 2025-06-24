/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.CodePosSetsList;
/*~~  */

import mesquite.lib.SpecsSet;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CodonPositionsSet;
import mesquite.lists.lib.DataSpecssetList;

/* ======================================================================== */
public class CodePosSetsList extends DataSpecssetList {
	/*.................................................................................................................*/
	public String getName() {
		return "List of Codon Positions Sets";
	}
	public String getExplanation() {
		return "Makes windows listing codon positions sets." ;
	}
	/*.................................................................................................................*/
	public int currentDataSet = 0;
	public CharacterData data = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		makeMenu("List");
		return true;
	}
	/** returns a String of annotation for a row*/
	public String getAnnotation(int row){ return null;}

	/** sets the annotation for a row*/
	public void setAnnotation(int row, String s, boolean notify){}

	public Class getItemType(){
		return CodonPositionsSet.class;
	}
	public String getItemTypeName(){
		return "Codon positions set";
	}
	public String getItemTypeNamePlural(){
		return "Codon positions sets";
	}
	public SpecsSet makeNewSpecsSet(CharacterData data){
		if (data!=null)
			return new CodonPositionsSet("Model Set", data.getNumChars(), data);
		return null;
	}
}


