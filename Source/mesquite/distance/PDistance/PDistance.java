/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.distance.PDistance;
/*~~  */


import java.awt.Checkbox;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.categ.lib.DNAData;
import mesquite.distance.lib.*;

/* ======================================================================== */
/* incrementable, with each being based on a different matrix */
public class PDistance extends DNATaxaDistFromMatrix {
	MesquiteBoolean transversionsOnly = new MesquiteBoolean(false);
	MesquiteBoolean transitionsOnly = new MesquiteBoolean(false);
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addCheckMenuItemToSubmenu(null, distParamSubmenu, "Transversions Only", MesquiteModule.makeCommand("toggleTransversionsOnly", this), transversionsOnly);
		addCheckMenuItemToSubmenu(null, distParamSubmenu, "Transitions Only", MesquiteModule.makeCommand("toggleTransitionsOnly", this), transitionsOnly);
		return true;
	}	 

	public boolean optionsAdded() {
		return true;
	}
	RadioButtons radios;
	public void addOptions(ExtensibleDialog dialog) {
		super.addOptions(dialog);
		String[] labels =  {"all changes", "transversions only", "transitions only"};
		int defaultValue= 0;
		if (transversionsOnly.getValue())
			defaultValue = 1;
		else	if (transitionsOnly.getValue())
			defaultValue = 2;
		radios = dialog.addRadioButtons(labels, defaultValue);

	}
	public void processOptions(ExtensibleDialog dialog) {
		super.processOptions(dialog);
		if (radios.getValue()==0) {
			transversionsOnly.setValue(false);
			transitionsOnly.setValue(false);
		}
		else if (radios.getValue()==1) {
			transversionsOnly.setValue(true);
			transitionsOnly.setValue(false);
		}
		else if (radios.getValue()==2) {
			transversionsOnly.setValue(false);
			transitionsOnly.setValue(true);
		}
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot snapshot = new Snapshot();
		snapshot.addLine("toggleTransversionsOnly  " + transversionsOnly.toOffOnString());
		snapshot.addLine("toggleTransitionsOnly  " + transitionsOnly.toOffOnString());
		return snapshot;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets whether only transversions are counted.", "[on; off]", commandName, "toggleTransversionsOnly")) {
			transversionsOnly.toggleValue(new Parser().getFirstToken(arguments));
			if (transversionsOnly.getValue())
				transitionsOnly.setValue(false);
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets whether only transitions are counted.", "[on; off]", commandName, "toggleTransitionsOnly")) {
			transitionsOnly.toggleValue(new Parser().getFirstToken(arguments));
			if (transitionsOnly.getValue())
				transversionsOnly.setValue(false);
			parametersChanged();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public boolean getTransversionsOnly(){
		return transversionsOnly.getValue();
	}
	/*.................................................................................................................*/
	public boolean getTransitionsOnly(){
		return transitionsOnly.getValue();
	}
	/*.................................................................................................................*/
	public String getParameters(){
		String s = super.getParameters();
		if (getTransversionsOnly())
			s+= " Transversions only.";
		if (getTransitionsOnly())
			s+= " Transitions only.";
		return s;
	}
	/*.................................................................................................................*/
	public TaxaDistance getTaxaDistance(Taxa taxa, MCharactersDistribution observedStates){
		if (observedStates==null) {
			MesquiteMessage.warnProgrammer("Observed states null in "+ getName());
			return null;
		}
		if (!(observedStates.getParentData() instanceof DNAData)) {
			return null;
		}
		PTD simpleTD = new PTD( this,taxa, observedStates,getEstimateAmbiguityDifferences(), getCountDifferencesIfGapInPair());
		return simpleTD;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Uncorrected (p) distance (DNA)";  
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Uncorrected (p) distance from a DNA matrix." ;
	}

	public boolean requestPrimaryChoice(){
		return true;
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

	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
}

class PTD extends DNATaxaDistance {
	PDistance PD;
	public PTD(MesquiteModule ownerModule, Taxa taxa, MCharactersDistribution observedStates, boolean estimateAmbiguityDifferences, boolean countDifferencesIfGapInPair){
		super(ownerModule, taxa, observedStates,estimateAmbiguityDifferences, countDifferencesIfGapInPair);
		PD = (PDistance)ownerModule;
		MesquiteDouble N = new MesquiteDouble();
		MesquiteDouble D = new MesquiteDouble();
		setEstimateAmbiguityDifferences(((DNATaxaDistFromMatrix)ownerModule).getEstimateAmbiguityDifferences());


		for (int taxon1=0; taxon1<getNumTaxa(); taxon1++) {
			for (int taxon2=taxon1; taxon2<getNumTaxa(); taxon2++) {
				double[][] fxy = calcPairwiseDistance(taxon1, taxon2, N, D);
				if (PD.getTransversionsOnly())
					distances[taxon1][taxon2]= fxy[0][1] + fxy[1][0] + fxy[0][3] + fxy[3][0] + fxy[1][2] + fxy[2][1] + fxy[2][3] + fxy[3][2];  //trasnversion
				else if (PD.getTransitionsOnly())
					distances[taxon1][taxon2]= fxy[0][2] + fxy[2][0] + fxy[1][3] + fxy[3][1];  //transitions
				else
					distances[taxon1][taxon2]= D.getValue();
			}
		}
		copyDistanceTriangle();

		logDistancesIfDesired(ownerModule.getName());	}
	
	public String getName() {
		return PD.getName();
	}

}





