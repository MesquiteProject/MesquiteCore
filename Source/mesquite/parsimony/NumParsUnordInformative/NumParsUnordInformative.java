/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.parsimony.NumParsUnordInformative;


import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.MCategoricalDistribution;
import mesquite.categ.lib.RequiresAnyCategoricalData;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.NumberForMatrix;

public class NumParsUnordInformative extends NumberForMatrix {

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	} 

	/** Called to provoke any necessary initialization.  This helps prevent the module's initialization queries to the user from happening at inopportune times (e.p., while a long chart calculation is in mid-progress*/
	public void initialize(MCharactersDistribution data) {
	} 

	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyCategoricalData();
	}
	public void calculateNumber(MCharactersDistribution data, MesquiteNumber result, MesquiteString resultString) {
		if (result == null || data == null)
			return;
		clearResultAndLastResult(result);
		if (!(data instanceof MCategoricalDistribution))
			return;
		long total = 0;
		CategoricalData parentData = (CategoricalData)data.getParentData();
		if (parentData == null){
			total = data.getNumChars();
		}
		else {
			int numChars = parentData.getNumChars();

			for (int ic=0; ic<numChars; ic++)
				if (parentData.charIsUnorderedInformative(ic))
					total++;
		}
		result.setValue(total); 

		if (resultString!=null) {
			resultString.setValue("Number of characters that are unordered parsimony informative: " + result.toString());
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	} 

	public boolean isPrerelease (){
		return false;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 400;  
	}

	public String getName() {
		return "Num. Chars. Parsimony Informative (Undord.)";
	}
	public String getVeryShortName() {
		return "# Chars. Pars. Informative";
	}

	public String getExplanation(){
		return "Calculates the number of characters in a matrix that are parsimony-informative under the unordered model.";
	} 

} 
