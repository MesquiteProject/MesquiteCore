package mesquite.molec.NumInternalMissingMatrix;


import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.NumberForMatrix;

public class NumInternalMissingMatrix extends NumberForMatrix {

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
			int countMissing = 0;
					
			for (int ic=0; ic<numChars; ic++){
				if (parentData.isUnassigned(ic,it)){   // we have found a missing
					if (stateFound)  //to the right of sequence, just possibly an internal missing
						countMissing++;
				}
				else if (!parentData.isInapplicable(ic, it)){  // found a non-gap, non-missing
					stateFound = true;
					if (countMissing >0){ // some missing counting accumulated; consider them confirmed as internal
						confirmedInternal += countMissing;
						countMissing = 0;
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
			resultString.setValue("Number of internal missing data: " + result.toString());
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	} 

	public boolean isPrerelease (){
		return false;
	}

	public String getName() {
		return "Number of Internal Missing Data Entries in Matrix";
	} 

	public String getExplanation(){
		return "Calculates the number of internal missing data entries in the matrix.";
	} 

} 
