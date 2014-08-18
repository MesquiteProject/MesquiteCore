package mesquite.charMatrices.NumIncludedMatrix;


import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

public class NumIncludedMatrix extends NumberForMatrix {

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

		long total = 0;
		CharacterData parentData = data.getParentData();
		int numChars = parentData.getNumChars();

		for (int ic=0; ic<numChars; ic++)
			if (parentData.isCurrentlyIncluded(ic))
				total++;

		result.setValue(total); 

		if (resultString!=null) {
			resultString.setValue("Number of characters included: " + result.toString());
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	} 

	public boolean isPrerelease (){
		return true;
	}

	public String getName() {
		return "Number of Characters Included";
	} 

	public String getExplanation(){
		return "Calculates the number of characters in a matrix that are included.";
	} 

} 
