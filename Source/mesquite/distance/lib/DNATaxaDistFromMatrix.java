/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.distance.lib;
/*~~  */


import java.awt.Checkbox;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.categ.lib.*;
import mesquite.cont.lib.GeographicStateTest;
import mesquite.distance.lib.*;

/* ======================================================================== */
/* incrementable, with each being based on a different matrix */
public abstract class DNATaxaDistFromMatrix extends TaxaDistFromMatrix {
	MesquiteBoolean estimateAmbiguityDifferences = new MesquiteBoolean(MolecularTaxaDistance.DEFAULTESTIMATEAMBIGUITYDIFFERENCES);
	MesquiteBoolean countDifferencesIfGapInPair = new MesquiteBoolean(MolecularTaxaDistance.DEFAULTCOUNTDIFFERENCESIFGAPINPAIR);
	/*.................................................................................................................*/
	public boolean superStartJob(String arguments, Object condition, boolean hiredByName) {
		super.superStartJob(arguments, condition, hiredByName);
		addCheckMenuItemToSubmenu(null, distParamSubmenu, "Count Sites with Gap in Pair", MesquiteModule.makeCommand("toggleCountDifferencesIfGapInPair", this), countDifferencesIfGapInPair);
		addCheckMenuItemToSubmenu(null, distParamSubmenu, "Estimate Ambiguity Differences", MesquiteModule.makeCommand("toggleEstimateAmbiguityDifferences", this), estimateAmbiguityDifferences);
		return true;
	}	 
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot snapshot = new Snapshot();
		snapshot.addLine("toggleEstimateAmbiguityDifferences  " + estimateAmbiguityDifferences.toOffOnString());
		snapshot.addLine("toggleCountDifferencesIfGapInPair  " + countDifferencesIfGapInPair.toOffOnString());
		return snapshot;
	}
	public boolean optionsAdded() {
		return true;
	}
	Checkbox estimateAmbiguityDifferencesBox;
	Checkbox countDifferencesIfGapInPairBox;
	public void addOptions(ExtensibleDialog dialog) {
		estimateAmbiguityDifferencesBox = dialog.addCheckBox("estimate ambiguity differences", estimateAmbiguityDifferences.getValue());
		countDifferencesIfGapInPairBox = dialog.addCheckBox("count as a difference one taxon having a gap and the other a non-gap", countDifferencesIfGapInPair.getValue());

	}
	public void processOptions(ExtensibleDialog dialog) {
		estimateAmbiguityDifferences.setValue(estimateAmbiguityDifferencesBox.getState());
		countDifferencesIfGapInPair.setValue(countDifferencesIfGapInPairBox.getState());
	}


	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets whether sites at which one element of the pair of sequences being compared has a gap are not excluded or are.", "[on; off]", commandName, "toggleCountDifferencesIfGapInPair")) {
			countDifferencesIfGapInPair.toggleValue(new Parser().getFirstToken(arguments));
			parametersChanged();
		}
		else	if (checker.compare(this.getClass(), "Sets whether sites with missing data, gaps, or ambiguities have differences estimated by other sites or are ignored.", "[on; off]", commandName, "toggleEstimateAmbiguityDifferences")) {
			estimateAmbiguityDifferences.toggleValue(new Parser().getFirstToken(arguments));
			parametersChanged();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public String getParameters(){
		String s = super.getParameters();
		if (getEstimateAmbiguityDifferences())
			s+= " Ambiguity differences estimated from patterns for non-ambiguous bases when similar patterns exist, distributed equally otherwise.";
		if (getCountDifferencesIfGapInPair())
			s+= " Sites at which one element of the pair of sequences being compared has a gap are not excluded .";
		else
			s+= " Sites at which one element of the pair of sequences being compared has a gap ARE excluded .";
		return s;
	}
	/*.................................................................................................................*/
	public boolean getCountDifferencesIfGapInPair() {
		return countDifferencesIfGapInPair.getValue();  
	}
	/*.................................................................................................................*/
	public boolean getEstimateAmbiguityDifferences() {
		return estimateAmbiguityDifferences.getValue();  
	}
	public Class getRequiredStateClass(){
		return DNAState.class;
	}
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyDNAData();
	}
	public boolean isSubstantive(){
		return true;
	}
}





