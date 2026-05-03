package mesquite.molec.RCFVofMatrix;


import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.DNAState;
import mesquite.categ.lib.MolecularData;
import mesquite.categ.lib.MolecularDataUtil;
import mesquite.categ.lib.ProteinData;
import mesquite.categ.lib.ProteinState;
import mesquite.categ.lib.RequiresAnyMolecularData;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.Debugg;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.NumberForMatrix;

public class RCFVofMatrix extends NumberForMatrix {

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	} 

	/** Called to provoke any necessary initialization.  This helps prevent the module's initialization queries to the user from happening at inopportune times (e.p., while a long chart calculation is in mid-progress*/
	public void initialize(MCharactersDistribution data) {
	} 
	/*.................................................................................................................*/

	public void calculateNumber(MCharactersDistribution data, MesquiteNumber result, MesquiteString resultString) {
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
		double rcfv=0.0;
		boolean analyzeAsAminoAcids = false;   // control
		
		if (parentData instanceof DNAData){
			DNAData dnaData = (DNAData)parentData;			
			rcfv=MolecularDataUtil.getRCFVofMatrix(this, dnaData, analyzeAsAminoAcids);
		}
		else  if (parentData instanceof ProteinData){
			ProteinData pData = (ProteinData)parentData;
			rcfv=MolecularDataUtil.getRCFVofMatrix(this,pData, true);
		}
		
		
		if (rcfv>0) {
			result.setValue(rcfv); 
		}  else
			result.setValue(0.0); 

		if (resultString!=null) {
			resultString.setValue("RCFV: " + result.toString());
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
		return "RCFV of Matrix";
	} 

	public String getExplanation(){
		return "Calculates the RCFV (Relative Composition Frequency Variability) of the matrix.";
	} 

} 
