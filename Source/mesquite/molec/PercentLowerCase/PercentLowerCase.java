/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.PercentLowerCase;
/*~~  */


import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.CategoricalState;
import mesquite.categ.lib.RequiresAnyMolecularData;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.characters.CharInclusionSet;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterState;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.NumberForTaxonAndMatrix;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.Taxon;

/* ======================================================================== */
public class PercentLowerCase extends NumberForTaxonAndMatrix {

	Taxa currentTaxa = null;
	MCharactersDistribution observedStates =null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		currentTaxa = taxa;
	}

	public void calculateNumber(Taxon taxon, MCharactersDistribution matrix, MesquiteNumber result, MesquiteString resultString){
		if (result==null)
			return;
	   	clearResultAndLastResult(result);
		Taxa taxa = taxon.getTaxa();
		int it = taxa.whichTaxonNumber(taxon);
		observedStates = matrix;
		currentTaxa = taxa;
		if (observedStates==null)
			return;
		CharacterData data = observedStates.getParentData();
		if (!(data instanceof CategoricalData))
			return;
		CharacterState cs = null;
		CategoricalData cData = (CategoricalData)data;
		CharInclusionSet incl = null;
		if (data !=null)
			incl = (CharInclusionSet)data.getCurrentSpecsSet(CharInclusionSet.class);
		int numChars = observedStates.getNumChars();
		int charExc = 0;
		if (numChars != 0) {
			int numLowerCase = 0;
			int numAssigned = 0;
			for (int ic=0; ic<numChars; ic++) {
				if (incl == null || incl.isBitOn(ic)){
					cs = observedStates.getCharacterState(cs, ic, it);
					if (!cs.isInapplicable()){
						numAssigned++;
						long s = cData.getStateRaw(ic, it);
						if (CategoricalState.isLowerCase(s))
							numLowerCase++;

					}
				}
			}
			if (numAssigned>0)
				result.setValue(((double)numLowerCase)/numAssigned);
		}	
		String exs = "";
		if (charExc > 0)
			exs = " (" + Integer.toString(charExc) + " characters excluded)";

		if (resultString!=null)
			resultString.setValue("Proportion of lower case codings in matrix "+ observedStates.getName() + exs + ": " + result.toString());
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Proportion lower case codings in taxon";  
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Proportion lower case codings";  
	}
 	public String getParameters() {
		if (observedStates != null && getProject().getNumberCharMatricesVisible()>1){
			CharacterData d = observedStates.getParentData();
			if (d != null && d.getName()!= null) {
				String n =  d.getName();
				if (n.length()>12)
					n = n.substring(0, 12); 
				return "Prop. Lower Case (" + n + ")";
			}
		}
		return "Prop. Lower Case";  
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
		return false;
	}
	public String getVeryShortName() {
		return "Prop. Lower Case";  
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Reports the percentage of lower case codings in a taxon for a data matrix." ;
	}
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyMolecularData();
	}

}




