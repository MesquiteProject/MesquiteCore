package mesquite.charMatrices.NumApplicableNonMissingMatrix;


import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

public class NumApplicableNonMissingMatrix extends NumberForMatrix {

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

		long valid = 0;
		long total = 0;
		CharacterData parentData = data.getParentData();
		int numTaxa = parentData.getNumTaxa();
		int numChars = parentData.getNumChars();

		for (int it=0; it<numTaxa; it++)
			for (int ic=0; ic<numChars; ic++){
				if (!parentData.isInapplicable(ic,it) && !parentData.isUnassigned(ic,it))
					valid++;
				total++;
			}
		if (total>0) {
			result.setValue((valid*1.0)/total); 
		}  else
			result.setValue(0.0); 

		if (resultString!=null) {
			resultString.setValue("Fraction Applicable and Not Missing: " + result.toString());
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	} 

	public boolean isPrerelease (){
		return false;
	}

	public String getName() {
		return "Fraction Applicable and Not Missing";
	} 

	public String getExplanation(){
		return "Calculates the fraction of the matrix that is applicable and not missing data.";
	} 

} 
