package mesquite.molec.RCFVofTaxonAndMatrix;


import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.DNAState;
import mesquite.categ.lib.MolecularData;
import mesquite.categ.lib.MolecularDataUtil;
import mesquite.categ.lib.ProteinData;
import mesquite.categ.lib.ProteinState;
import mesquite.categ.lib.RequiresAnyMolecularData;
import mesquite.lib.CommandChecker;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.Debugg;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteCommandAbsorber;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.Snapshot;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.NumberForMatrix;
import mesquite.lib.duties.NumberForTaxonAndMatrix;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.Taxon;

public class RCFVofTaxonAndMatrix extends NumberForTaxonAndMatrix {
	Taxa currentTaxa = null;
	MesquiteBoolean countAsAAs = new MesquiteBoolean(false);

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		//addCheckMenuItem(null, "Calculate RCFV Based on AA Translation", makeCommand("toggleCountAsAA",  this), countAsAAs);
		return true;
	} 

	/** Called to provoke any necessary initialization.  This helps prevent the module's initialization queries to the user from happening at inopportune times (e.p., while a long chart calculation is in mid-progress*/
	public void initialize(Taxa taxa) {
		currentTaxa = taxa;

	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("toggleCountAsAA " + countAsAAs.toOffOnString());
		return temp;
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		 if (checker.compare(this.getClass(), "Eats command", "[on or off]", commandName, "getEmployee")) {  //to eat command for matrix source
			return new MesquiteCommandAbsorber();
		} else
		 if (checker.compare(this.getClass(), "Sets whether the calculation is based on translated AAs", "[on or off]", commandName, "toggleCountAsAA")) {
			boolean current = countAsAAs.getValue();
			countAsAAs.toggleValue(parser.getFirstToken(arguments));
			if (current!=countAsAAs.getValue()) {
				outputInvalid();
				parametersChanged();
			}
		} 
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/

	public void calculateNumber(Taxon taxon, MCharactersDistribution data, MesquiteNumber result, MesquiteString resultString) {
		if (result == null || data == null)
			return;
		clearResultAndLastResult(result);

		CharacterData parentData = data.getParentData();
		if (parentData == null){
			if (resultString != null)
				resultString.setValue("RCFV can be calculated only for stored matrices");
			return;
		}

		if (!(parentData instanceof DNAData) && !(parentData instanceof ProteinData)){
			if (resultString != null)
				resultString.setValue("RCFV can be calculated only for DNA or Protein matrices");
			return;
		}
		
		Taxa taxa = taxon.getTaxa();
		int it = taxa.whichTaxonNumber(taxon);
		currentTaxa = taxa;

		double rcfv=0.0;
		boolean analyzeAsAminoAcids = countAsAAs.getValue();   // Debuggg DAVIDCHECK

		if (parentData instanceof DNAData){
			DNAData dnaData = (DNAData)parentData;			
			rcfv=MolecularDataUtil.getRCFVofTaxon(dnaData, it, analyzeAsAminoAcids);
		}
		else  if (parentData instanceof ProteinData){
			ProteinData pData = (ProteinData)parentData;
			rcfv=MolecularDataUtil.getRCFVofTaxon(pData, it, true);
		}
		
		
		if (!MesquiteDouble.isCombinable(rcfv)) {
			result.setValue(MesquiteDouble.unassigned); 
			if (resultString!=null)
				resultString.setValue("RCFV cannot be calculated for this taxon");
		}
		else {
			if (rcfv>0) {
			result.setValue(rcfv); 
		}  else
			result.setValue(0.0); 
			if (resultString!=null) {
				resultString.setValue("RCFV: " + result.toString());
			}
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	} 

	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyMolecularData();
	}
	public boolean isPrerelease (){
		return false;
	}

	public String getName() {
		return "RCFV of Taxon";
	} 

	public String getExplanation(){
		return "Calculates the RCFV (Relative Composition Frequency Variability) of the taxon for a matrix.";
	}



} 
