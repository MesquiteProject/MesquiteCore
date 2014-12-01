package mesquite.molec.NumStopCodonsMatrix;


import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.ProteinData;
import mesquite.categ.lib.RequiresAnyDNAData;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

public class NumStopCodonsMatrix extends NumberForMatrix {

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

		CharacterData parentData = data.getParentData();
		if (parentData == null){
			if (resultString != null)
				resultString.setValue("Number of stop codons can be calculated only for stored matrices");
			return;
		}
		if (!(parentData instanceof DNAData)){
			if (resultString != null)
				resultString.setValue("Number of stop codons can be calculated only for DNA matrices");
			return;
		}
		DNAData dnaData = (DNAData)parentData;
		int numTaxa = parentData.getNumTaxa();
		
		int count = 0;
		for (int it=0; it<numTaxa; it++)
			count += dnaData.getAminoAcidNumbers(it,ProteinData.TER, false);

		if (count>0) {
			result.setValue(count); 
		}  else
			result.setValue(0.0); 

		if (resultString!=null) {
			resultString.setValue("Number of stop codons: " + result.toString());
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	} 

	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyDNAData();
	}
	public boolean isPrerelease (){
		return false;
	}

	public String getName() {
		return "Number of Stop Codons in Matrix";
	} 

	public String getExplanation(){
		return "Calculates the number of stop codons in the matrix.";
	} 

} 
