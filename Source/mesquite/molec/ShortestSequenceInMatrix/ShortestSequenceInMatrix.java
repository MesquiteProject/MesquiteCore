package mesquite.molec.ShortestSequenceInMatrix;



import mesquite.categ.lib.MolecularData;
import mesquite.categ.lib.RequiresAnyMolecularData;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.NumberForMatrix;

public class ShortestSequenceInMatrix extends NumberForMatrix {

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
				resultString.setValue("Shortest sequence length can be calculated only for stored matrices");
			return;
		}
		if (!(parentData instanceof MolecularData)){
			if (resultString != null)
				resultString.setValue("Shortest sequence length can be calculated only for molecular matrices");
			return;
		}
		int numTaxa = parentData.getNumTaxa();
		int numChars = parentData.getNumChars();
		int shortest = MesquiteInteger.unassigned;
		for (int it=0; it<numTaxa; it++) {
			int count = 0;
			for (int ic=0; ic<numChars; ic++)
				if (!parentData.isInapplicable(ic,  it))
					count ++;
			if (count>0)
				shortest = MesquiteInteger.minimum(shortest, count);
		}
		
		if (MesquiteInteger.isCombinable(shortest)) {
			result.setValue(shortest); 
		}  else
			result.setValue(0); 

		if (resultString!=null) {
			resultString.setValue("Shortest non-empty sequence length: " + result.toString());
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	} 
	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return 403;  
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
		return "Shortest Sequence Length in Matrix";
	} 

	public String getExplanation(){
		return "Calculates the shortest non-empty sequence length (non-gaps) in the matrix.";
	} 

} 
