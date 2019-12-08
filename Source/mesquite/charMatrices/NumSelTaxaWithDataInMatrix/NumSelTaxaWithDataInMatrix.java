package mesquite.charMatrices.NumSelTaxaWithDataInMatrix;



import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

public class NumSelTaxaWithDataInMatrix extends NumberForMatrix {

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	} 

	public void endJob(){
		for (int i = 0; i< getProject().getNumberTaxas(); i++)
			getProject().getTaxa(i).removeListener(this);
		super.endJob();
	}
	/** Called to provoke any necessary initialization.  This helps prevent the module's initialization queries to the user from happening at inopportune times (e.p., while a long chart calculation is in mid-progress*/
	public void initialize(MCharactersDistribution data) {
	} 
	/* ---------------------------------------------------------*/
	/** passes which object changed, along with optional Notification object with details (e.g., code number (type of change) and integers (e.g. which character))*/
	public void changed(Object caller, Object obj, Notification notification){
		outputInvalid();
		parametersChanged();
	}

	public void calculateNumber(MCharactersDistribution data, MesquiteNumber result, MesquiteString resultString) {
		if (result == null || data == null)
			return;
		clearResultAndLastResult(result);
		int count = 0;
		Taxa taxa = data.getTaxa();
		taxa.addListener(this); //this doesn't add if it's already there
		boolean anySelected = taxa.anySelected();
		for (int it = 0; it<data.getNumTaxa(); it++){
			if ((!anySelected || taxa.getSelected(it)) && hasData(data, it))
				count++;
		}


		if (count>0) {
			result.setValue(count); 
		}  else
			result.setValue(0.0); 

		if (resultString!=null) {
			resultString.setValue("Number of selected taxa with data: " + result.toString());
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

	public boolean isPrerelease (){
		return false;
	}

	public String getName() {
		return "Number of Selected Taxa with Data in Matrix";
	} 

	public String getExplanation(){
		return "Counts the number of taxa, among those selected, with data (not ? and not gaps) the matrix.";
	} 

} 
