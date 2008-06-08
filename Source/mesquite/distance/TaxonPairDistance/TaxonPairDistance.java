/* Mesquite source code.  Copyright 1997-2008 W. Maddison and D. Maddison.
Version 2.5, June 2008.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
/*Oliver May 2008
 * Mostly copied from TaxonDistance*/
package mesquite.distance.TaxonPairDistance;

import mesquite.distance.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/**Supplies a number for a character matrix and a pair of taxa.  Currently, it is just a distance 
 * calculator for a specific pair of taxa, but could be made abstract for additional uses.*/
public class TaxonPairDistance extends NumberForMatrix{//NumberForMatrixAndTaxonPair{
	TaxaDistance taxaDistance;
	TaxaDistFromMatrix distFromMatrix;
	MesquiteString distFromMatrixName;
	MesquiteCommand dtC;
	MesquiteSubmenuSpec msd;
	MesquiteInteger pos = new MesquiteInteger(); //For doCommand navigation
	Taxa taxa;
	int t1, t2;

	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		if(!MesquiteThread.isScripting()){
			taxa = getProject().chooseTaxa(containerOfModule(), "Select Taxa");
			if (taxa == null)
				return sorry("Could not start Pairwise Taxon Distance because no taxa blocks found");
			t1 = taxa.userChooseTaxon(containerOfModule(), "Choose Taxon 1").getNumber();
			t2 = taxa.userChooseTaxon(containerOfModule(), "Choose Taxon 2").getNumber();
		}
		addMenuItem("Choose Taxon 1...", makeCommand("chooseTaxon1", this));
		addMenuItem("Choose Taxon 2...", makeCommand("chooseTaxon2", this));

		//TODO: should hiring be inside a !MesquiteThread.isScripting() conditional to prevent queries on startup?
		distFromMatrix = (TaxaDistFromMatrix)hireEmployee(TaxaDistFromMatrix.class, "Source of distance");
		if (distFromMatrix == null)
			return sorry("Could not start Pairwise Taxon Distance because no distance source found");
		dtC = makeCommand("setDistanceSource", this);
		distFromMatrix.setHiringCommand(dtC);
		distFromMatrixName = new MesquiteString(distFromMatrix.getName());
		if(numModulesAvailable(TaxaDistFromMatrix.class)>1){
			msd = addSubmenu(null, "Source of Distance", dtC, TaxaDistFromMatrix.class);
			msd.setSelected(distFromMatrixName);
		}
		return true;
	}
	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return 250;  
	}
	/*.............................................................................*/
	public void initialize(MCharactersDistribution data){
		if(data != null){
			taxa = data.getTaxa();
			distFromMatrix.initialize(taxa);
		}
	}
	/*.............................................................................*/
	public void endJob(){
		if (taxa!=null)
			taxa.removeListener(this);
		super.endJob();
	}
	/*.............................................................................*/
	public Snapshot getSnapshot(MesquiteFile file){
		Snapshot temp = new Snapshot();
		temp.addLine("setTaxa " + getProject().getTaxaNumber(taxa));
		temp.addLine("setDistanceSource " , distFromMatrix);
		temp.addLine("setTaxon1 " + t1);
		temp.addLine("setTaxon2 " + t2);
  	 	return temp;
	}
	/*.............................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker){
		if (checker.compare(this.getClass(), "Sets the module supplying distances", "[name of module]", commandName, "setDistanceSource")) {
	 		TaxaDistFromMatrix temp =  (TaxaDistFromMatrix)replaceEmployee(TaxaDistFromMatrix.class, arguments, "Source of distances", distFromMatrix);
	 		if (temp!=null) {
	 			distFromMatrix = temp;
	 			taxaDistance = null;
	 			distFromMatrix.setHiringCommand(dtC);
	 			distFromMatrixName = new MesquiteString(distFromMatrix.getName());
	 			if (msd!=null){
	 				msd.setSelected(distFromMatrixName);
	 			}
	 			parametersChanged();
	 		}
			return temp;
	 	}
		else if (checker.compare(this.getClass(), "Sets the block of taxa to use for comparisons", "[taxa number]", commandName, "setTaxa")){
			int temp = MesquiteInteger.fromFirstToken(arguments, pos);
			if (temp >= 0 && temp <= getProject().getNumberTaxas())
				taxa = getProject().getTaxa(temp);
		}
		else if (checker.compare(this.getClass(), "Sets first taxon to use for comparison", "[taxon number]", commandName, "setTaxon1")){
			int temp = MesquiteInteger.fromFirstToken(arguments, pos);
			if (taxa != null && temp >= 0 && temp <= taxa.getNumTaxa())
				t1 = temp;
		}
		else if (checker.compare(this.getClass(), "Sets second taxon to use for comparison", "[taxon number]", commandName, "setTaxon2")){
			int temp = MesquiteInteger.fromFirstToken(arguments, pos);
			if (taxa != null && temp >= 0 && temp <= taxa.getNumTaxa())
				t2 = temp;
		}
		else if (checker.compare(this.getClass(), "Prompts user to choose first taxon for comparison", "[taxon number]", commandName, "chooseTaxon1")){
			if (taxa != null){
			int temp = taxa.userChooseTaxon(containerOfModule(), "Choose Taxon 1").getNumber();
				if (temp >= 0 && temp <= taxa.getNumTaxa()){
					t1 = temp;
					parametersChanged();
				}
			}
		}
		else if (checker.compare(this.getClass(), "Prompts user to choose second taxon for comparison", "[taxon number]", commandName, "chooseTaxon2")){
			if (taxa != null){
				int temp = taxa.userChooseTaxon(containerOfModule(), "Choose Taxon 2").getNumber();
				if (temp >= 0 && temp <= taxa.getNumTaxa()){
					t2 = temp;
		 			parametersChanged();
				}
			}
		}
		else return super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.............................................................................*/
	public void calculateNumber(MCharactersDistribution data, MesquiteNumber result, MesquiteString resultString){
		if(result == null || data == null)
			return;
		clearResultAndLastResult(result);
		if (taxa == null)
			initialize(data);
		if(distFromMatrix != null){
			taxaDistance = distFromMatrix.getTaxaDistance(taxa, data);
			if (taxaDistance != null){
				result.setValue(taxaDistance.getDistance(t1, t2));
				if(resultString != null)
					resultString.setValue(result.toString());
			}
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if(employee == distFromMatrix)
			parametersChanged(notification);
		taxaDistance=null; //to force recalculation
		super.employeeParametersChanged(this, source, notification);
	}
	/*.................................................................................................................*/
	public String getNameAndParameters() {
		String s = distFromMatrix.getNameAndParameters();
		if(taxa != null){
			s += " using " + taxa.getName();
			s += " comparing " + taxa.getTaxonName(t1) + " and " + taxa.getTaxonName(t2);
		}
		return s;
	}
	/*.................................................................................................................*/
	public String getParameters() {
		String s = "";
		if (distFromMatrix != null){
			s += "Distances calculated by " + distFromMatrix.getNameAndParameters();
		}
		if(taxa != null){
			s += " using " + taxa.getName();
			s += " comparing " + taxa.getTaxonName(t1) + " and " + taxa.getTaxonName(t2);
		}
		return s;
  	 }
 	/*.............................................................................*/
 	public String getName() {
 		return "Pairwise Taxon Difference";
 	}
 	/*.............................................................................*/
 	public String getExplanation(){
 		return "Calculates distance between a specific pair of taxa based on a character matrix.  Differs from Distance Between Taxa in that" +
 				"it performs calculation for only a single pair of taxa.";
 	}
 	/*.............................................................................*/
 	public boolean isPrerelease(){
 		return false;
 	}
 	/*.............................................................................*/
 	public boolean requestPrimaryChoice(){
 		return false;
 	}
}
