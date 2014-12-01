package mesquite.molec.NumInternalGapsMatrix;


import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

public class NumInternalGapsMatrix extends NumberForMatrix {

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
		if (parentData == null){
			if (resultString != null)
				resultString.setValue("Number of internal gaps can be calculated only for stored matrices");
			return;
		}
		int numTaxa = parentData.getNumTaxa();
		int numChars = parentData.getNumChars();

		int confirmedInternal = 0;
		for (int it=0; it<numTaxa; it++){
			boolean stateFound = false;
			int countGaps = 0;
					
			for (int ic=0; ic<numChars; ic++){
				if (parentData.isInapplicable(ic,it)){
					if (stateFound)  //to the right of sequence, just possibly an internal gap
						countGaps++;
				}
				else {
					stateFound = true;
					if (countGaps >0){ // some gap counting accumulated; consider them confirmed as internal
						confirmedInternal += countGaps;
						countGaps = 0;
					}
				}
				total++;
			}
		}
		if (confirmedInternal>0) {
			result.setValue(confirmedInternal); 
		}  else
			result.setValue(0.0); 

		if (resultString!=null) {
			resultString.setValue("Number of internal gaps: " + result.toString());
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	} 

	public boolean isPrerelease (){
		return false;
	}

	public String getName() {
		return "Number of Internal Gaps in Matrix";
	} 

	public String getExplanation(){
		return "Calculates the number of internal gaps in the matrix.";
	} 

} 
