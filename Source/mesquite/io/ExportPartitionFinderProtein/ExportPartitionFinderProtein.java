/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.io.ExportPartitionFinderProtein;

import mesquite.categ.lib.ProteinData;
import mesquite.categ.lib.ProteinState;
import mesquite.io.lib.ExportPartitionFinder;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteProject;
import mesquite.lib.characters.CharacterData;



public class ExportPartitionFinderProtein extends ExportPartitionFinder {
	/*.................................................................................................................*/


	public boolean isPrerelease(){
		return false;
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
	public void appendPhylipStateToBuffer(CharacterData data, int ic, int it, MesquiteStringBuffer outputBuffer){
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
