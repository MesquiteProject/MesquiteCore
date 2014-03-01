package mesquite.io.ExportPartitionFinderProtein;

import java.awt.Checkbox;

import mesquite.assoc.lib.AssociationSource;
import mesquite.categ.lib.*;
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



public class ExportPartitionFinderProtein extends ExportPartitionFinder {
	/*.................................................................................................................*/


	public boolean isPrerelease(){
		return true;
	}
	public boolean isProtein(){
		return true;
	}

	/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		return (project.getNumberCharMatricesVisible(ProteinState.class) > 0) ;
	}

	/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		return ProteinData.class.isAssignableFrom(dataClass);
	}

	/*.................................................................................................................*
	public void appendPhylipStateToBuffer(CharacterData data, int ic, int it, StringBuffer outputBuffer){
		data.statesIntoStringBuffer(ic, it, outputBuffer, false);
	}
/*.................................................................................................................*/
	public CharacterData findDataToExport(MesquiteFile file, String arguments) { 
		return getProject().chooseData(containerOfModule(), file, null, ProteinState.class, "Select data to export");
	}


	public String getProgramName() {
		return "PartitionFinderProtein";
	}
}
