/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.NumIncludedMatrix;


import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

public class NumIncludedMatrix extends NumberForMatrix {

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	} 

	/** Called to provoke any necessary initialization.  This helps prevent the module's initialization queries to the user from happening at inopportune times (e.p., while a long chart calculation is in mid-progress*/
	public void initialize(MCharactersDistribution data) {
	} 

	public void calculateNumber(MCharactersDistribution data, MesquiteNumber result, MesquiteString resultString) {
		if (result == null || data == null)
			return;
		clearResultAndLastResult(result);

		long total = 0;
		CharacterData parentData = data.getParentData();
		int numChars = parentData.getNumChars();

		for (int ic=0; ic<numChars; ic++)
			if (parentData.isCurrentlyIncluded(ic))
				total++;

		result.setValue(total); 

		if (resultString!=null) {
			resultString.setValue("Number of characters included: " + result.toString());
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	} 

	public boolean isPrerelease (){
		return true;
	}

	public String getName() {
		return "Number of Characters Included";
	} 

	public String getExplanation(){
		return "Calculates the number of characters in a matrix that are included.";
	} 

} 
