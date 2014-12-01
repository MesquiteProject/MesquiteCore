package mesquite.charMatrices.NumTaxaWithDataInMatrix;



import mesquite.categ.lib.MolecularData;
import mesquite.categ.lib.RequiresAnyMolecularData;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

public class NumTaxaWithDataInMatrix extends NumberForMatrix {

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
		int count = 0;
		for (int it = 0; it<data.getNumTaxa(); it++){
			if (hasData(data, it))
				count++;
		}


		if (count>0) {
			result.setValue(count); 
		}  else
			result.setValue(0.0); 

		if (resultString!=null) {
			resultString.setValue("Number of taxa with data: " + result.toString());
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	} 
	boolean hasData(MCharactersDistribution data, int it){
		CharacterState cs = null;
		try {
			for (int ic=0; ic<data.getNumChars(); ic++) {
				cs = data.getCharacterState(cs, ic, it);
				if (cs == null)
					return false;
				if (!cs.isInapplicable() && !cs.isUnassigned()) 
					return true;

			}
		}
		catch (NullPointerException e){
		}
		return false;
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
		return "Number of Taxa with Data in Matrix";
	} 

	public String getExplanation(){
		return "Counts the number of taxa with data (not ? and not gaps) the matrix.";
	} 

} 
