/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.categ.ProportionUniqueStates;
/*~~  */

import mesquite.categ.lib.CategoricalState;
import mesquite.lib.CommandChecker;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.Notification;
import mesquite.lib.Snapshot;
import mesquite.lib.characters.CharInclusionSet;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterState;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.NumberForTaxonAndMatrix;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.Taxon;

/* ======================================================================== */
public class ProportionUniqueStates extends NumberForTaxonAndMatrix {
	Taxa currentTaxa = null;
	MCharactersDistribution observedStates =null;
	MesquiteBoolean countExcluded;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		countExcluded = new MesquiteBoolean(true);
		addCheckMenuItem(null, "Count Excluded Characters", makeCommand("toggleCountExcluded",  this), countExcluded);
		return true;
	}


	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("toggleCountExcluded " + countExcluded.toOffOnString());
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		 if (checker.compare(this.getClass(), "Sets whether or not excluded characters are considered in the calculation", "[on or off]", commandName, "toggleCountExcluded")) {
			countExcluded.toggleValue(parser.getFirstToken(arguments));
			parametersChanged();
		}
		else return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		currentTaxa = taxa;
	}

	public boolean taxonIsUnique(CharacterData data, int ic, int it){
		if (data==null || observedStates==null)
			return false;
		CategoricalState cs = null;
		cs = (CategoricalState)observedStates.getCharacterState(cs, ic, it);   // state of original
		if (cs.isInapplicable())
			return false;
		CategoricalState cs2 = null;
		
		for (int it2=0; it2<data.getNumTaxa(); it2++) {
			if (it!=it2){
				cs2 = (CategoricalState)observedStates.getCharacterState(cs2, ic, it2);  // now let's get state of other taxon
				if (!cs2.isInapplicable()){   // only count ones with non-applicable
					if (cs.statesShared(cs2))   // if any states shared then non unique
						return false;
				}
			}
		}

		return true;
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
		CharInclusionSet incl = null;
		if (data !=null)
			incl = (CharInclusionSet)data.getCurrentSpecsSet(CharInclusionSet.class);
		int numChars = observedStates.getNumChars();
		int charExc = 0;
		
		
		if (numChars != 0) {
			CharacterState cs = null;
			int useNumChars = 0;
			int numUnique = 0;
			for (int ic=0; ic<numChars; ic++) {
				if (countExcluded.getValue() || incl == null || incl.isBitOn(ic)){
					cs = observedStates.getCharacterState(cs, ic, it);
					if (!cs.isInapplicable()){   // only count ones with non-applicable
						useNumChars++;
						if (taxonIsUnique(data,ic, it)) 
							numUnique++;
					}

				}
			}

			if (useNumChars == 0)
				result.setValue(0.0);
			else
				result.setValue(((double)numUnique)/useNumChars);
		}	
		String exs = "";
		if (charExc > 0)
			exs = " (" + Integer.toString(charExc) + " characters excluded)";

		if (resultString!=null)
			resultString.setValue("Proportion of unique states in matrix "+ observedStates.getName() + exs + ": " + result.toString());
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		observedStates = null;
		super.employeeParametersChanged(employee, source, notification);
	}
	/*.................................................................................................................*/
	public String getVeryShortName() {
		return "Prop. Unique";  
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Proportion Unique States in Taxon";  
	}

	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	public String getParameters() {
		if (observedStates != null && getProject().getNumberCharMatricesVisible()>1){
			CharacterData d = observedStates.getParentData();
			if (d != null && d.getName()!= null) {
				String n =  d.getName();
				if (n.length()>12)
					n = n.substring(0, 12); 
				return "Proportion unique states in taxon in matrix (" + n + ")";
			}
		}
		return "Proportion unique states in taxon in matrix";
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Reports the proportion of unique states in a taxon for a data matrix." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 303;  
	}

}




