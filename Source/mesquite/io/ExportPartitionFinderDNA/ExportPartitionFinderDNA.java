package mesquite.io.ExportPartitionFinderDNA;

import java.awt.Checkbox;

import mesquite.assoc.lib.AssociationSource;
import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.CategoricalState;
import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.DNAState;
import mesquite.lib.Arguments;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Parser;
import mesquite.lib.Taxa;
import mesquite.lib.TreeVector;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.FileInterpreterI;
import mesquite.io.lib.*;



public class ExportPartitionFinderDNA extends ExportPartitionFinder {
	/*.................................................................................................................*/


	public boolean isPrerelease(){
		return true;
	}

	/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		return (project.getNumberCharMatricesVisible(DNAState.class) > 0) ;
	}

	/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		return DNAData.class.isAssignableFrom(dataClass);
	}

	/*.................................................................................................................*
	public void appendPhylipStateToBuffer(CharacterData data, int ic, int it, StringBuffer outputBuffer){
		data.statesIntoStringBuffer(ic, it, outputBuffer, false);
	}
/*.................................................................................................................*/
	public CharacterData findDataToExport(MesquiteFile file, String arguments) { 
		return getProject().chooseData(containerOfModule(), file, null, DNAState.class, "Select data to export");
	}


	public String getProgramName() {
		return "PartitionFinder";
	}
}
