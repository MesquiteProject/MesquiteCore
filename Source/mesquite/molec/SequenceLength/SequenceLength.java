/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.SequenceLength;
/*~~  */

import mesquite.lib.CommandChecker;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteCommandAbsorber;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.Snapshot;
import mesquite.lib.characters.CharInclusionSet;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterState;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.NumberForTaxonAndMatrix;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.Taxon;

/* ======================================================================== */
public class SequenceLength extends NumberForTaxonAndMatrix {

	Taxa currentTaxa = null;
	MCharactersDistribution observedStates =null;
	MesquiteBoolean countExcluded = new MesquiteBoolean(false);
	MesquiteBoolean countMissing = new MesquiteBoolean(true);    // have this as true so that it matches previous values
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addCheckMenuItem(null, "Count Excluded Characters", makeCommand("toggleCountExcluded",  this), countExcluded);
		addCheckMenuItem(null, "Count Missing Data", makeCommand("toggleCountMissing",  this), countMissing);
		return true;
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("toggleCountExcluded " + countExcluded.toOffOnString());
		temp.addLine("toggleCountMissing " + countMissing.toOffOnString());
		return temp;
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		 if (checker.compare(this.getClass(), "Eats command", "[on or off]", commandName, "getEmployee")) {  //to eat command for matrix source
			return new MesquiteCommandAbsorber();
		} else
		 if (checker.compare(this.getClass(), "Sets whether or not excluded characters are considered in the calculation", "[on or off]", commandName, "toggleCountExcluded")) {
			boolean current = countExcluded.getValue();
			countExcluded.toggleValue(parser.getFirstToken(arguments));
			if (current!=countExcluded.getValue()) {
				outputInvalid();
				parametersChanged();
			}
		} else
		 if (checker.compare(this.getClass(), "Sets whether or not missing data are considered in the calculation", "[on or off]", commandName, "toggleCountMissing")) {
			boolean current = countMissing.getValue();
			countMissing.toggleValue(parser.getFirstToken(arguments));
			if (current!=countMissing.getValue()) {
				outputInvalid();
				parametersChanged();
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}


	public void initialize(Taxa taxa) {
	}
	public  void calculateNumber(Taxon taxon, MCharactersDistribution matrix, MesquiteNumber result, MesquiteString resultString){
		if (result==null)
			return;
		result.setToUnassigned();
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
		if (numChars != 0) {
			CharacterState cs = null;
			int seqLen = 0;
			for (int ic=0; ic<numChars; ic++) {
				if (countExcluded.getValue() || (incl == null || incl.isBitOn(ic)) && observedStates!=null){  // adjusted 2. 01 to consider inclusion  // added control in 3.0
					cs = observedStates.getCharacterState(cs, ic, it);
					if (countMissing.getValue()) {
						if (!cs.isInapplicable())
							seqLen++;
					} else
						if (!cs.isInapplicable() && !cs.isUnassigned())
							seqLen++;

				}
			}
			result.setValue(seqLen);
		}	
	
		if (resultString!=null)
			resultString.setValue("Length of sequence in matrix "+ observedStates.getName()  + ": " + result.toString());
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	public String getName() {
			return "Sequence Length";
	}
	/*.................................................................................................................*/
	public String getVeryShortName() {
		return "Seq. Length";
	}

	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	public String getParameters() {
		String s="";
		if (countExcluded.getValue())
			s = "Excluded characters are counted.";
		else
			s="Excluded characters not counted.";
		if (countMissing.getValue())
			s = "Missing data are counted.";
		else
			s="Missing data are not counted.";
		if (observedStates !=null)
			return "Sequence Length in matrix from: " + observedStates.getName() + ". " + s;
		return "Sequence Length in matrix. " + s;
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Reports the length (total minus gaps) of a molecular sequence in a taxon." ;
	}



}




