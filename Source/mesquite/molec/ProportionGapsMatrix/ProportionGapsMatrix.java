package mesquite.molec.ProportionGapsMatrix;


import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.NumberForMatrix;

public class ProportionGapsMatrix extends NumberForMatrix {

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
				resultString.setValue("Proportion of gaps can be calculated only for stored matrices");
			return;
		}
		int numTaxa = parentData.getNumTaxa();
		int numChars = parentData.getNumChars();
		long total = 0;

		for (int it=0; it<numTaxa; it++)
			total += parentData.numInapplicable(it);

		if (total>0) 
			result.setValue(total*1.0/(numTaxa*numChars)); 
		else
			result.setValue(0.0); 

		if (resultString!=null) 
			resultString.setValue("Proportion of gaps: " + result.toString());

		saveLastResult(result);
		saveLastResultString(resultString);
	} 

	public boolean isPrerelease (){
		return false;
	}

	public String getName() {
		return "Proportion of Gaps in Matrix";
	} 

	public String getExplanation(){
		return "Calculates the proportion of gaps in the matrix.";
	} 

} 
