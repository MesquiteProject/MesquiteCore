package mesquite.molec.AvgSequenceLengthMatrix;



import mesquite.categ.lib.MolecularData;
import mesquite.categ.lib.RequiresAnyMolecularData;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

public class AvgSequenceLengthMatrix extends NumberForMatrix {

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
				resultString.setValue("Average sequence length can be calculated only for stored matrices");
			return;
		}
		if (!(parentData instanceof MolecularData)){
			if (resultString != null)
				resultString.setValue("Average sequence length can be calculated only for molecular matrices");
			return;
		}
		int numTaxa = parentData.getNumTaxa();
		int numChars = parentData.getNumChars();
		
		int count = 0;
		for (int it=0; it<numTaxa; it++)
			for (int ic=0; ic<numChars; ic++)
				if (!parentData.isInapplicable(ic,  it))
					count ++;
		
		if (count>0) {
			result.setValue(count*1.0/numTaxa); 
		}  else
			result.setValue(0.0); 

		if (resultString!=null) {
			resultString.setValue("Average sequence length: " + result.toString());
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
		return "Average Sequence Length in Matrix";
	} 

	public String getExplanation(){
		return "Calculates the average sequence length (non-gaps) in the matrix.";
	} 

} 
