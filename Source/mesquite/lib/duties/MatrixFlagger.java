/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.duties;

import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.MesquiteTable;


/* ======================================================================== */
/**Flags sites in molecular data, e.g. for trimming*/

public abstract class MatrixFlagger extends MesquiteModule  {

   	 public Class getDutyClass() {
   	 	return MatrixFlagger.class;
   	 }
 	public String getDutyName() {
 		return "Matrix Flagger";
   	 }
 	 public String[] getDefaultModule() {
 	 	return new String[] {"#TrimByPhyIN", "#TrimGappySites", "#TrimBySpruceup", "#TrimByTrimAl"};
 	 }

	public abstract MatrixFlags flagMatrix(CharacterData data, MatrixFlags flags); //if input flags is null, make one and return it. Otherwise, adjust the one input.
	
	protected boolean forTrimming(){
		return getHiredAs() == MatrixFlaggerForTrimming.class;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		if (exporter != null)
			temp.addLine("getInterpreter", exporter);
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the option.", "[integer]", commandName, "getInterpreter")) {
			if (exporter == null)
				exporter = (FileInterpreterI)hireNamedEmployee(FileInterpreterI.class, "#InterpretFastaDNA");
			return exporter;
		}
		else
			return super.doCommand(commandName, arguments, checker);
	}
	
	/* As a service to flaggers that call programs to run of fasta files*/
	FileInterpreterI exporter;	
	protected boolean saveFastaFile(CharacterData data, String path, String fileName){
		getProject().incrementProjectWindowSuppression();
		FileCoordinator coord = getFileCoordinator();
		MesquiteFile tempDataFile = (MesquiteFile)coord.doCommand("newLinkedFile", StringUtil.tokenize(fileName+".nex"), CommandChecker.defaultChecker); //TODO: never scripting???
		TaxaManager taxaManager = (TaxaManager)findElementManager(Taxa.class);
		CharacterData newMatrix=null;
		Taxa newTaxa =data.getTaxa().cloneTaxa(); 
		newTaxa.addToFile(tempDataFile, null, taxaManager);
		CharactersManager manageCharacters = (CharactersManager)findElementManager(CharacterData.class);
		MCharactersDistribution matrix = data.getMCharactersDistribution();
		CharMatrixManager manager = manageCharacters.getMatrixManager(matrix.getCharacterDataClass());
		newMatrix = matrix.makeCharacterData(manager, newTaxa);
		newMatrix.setName(data.getName());

		newMatrix.addToFile(tempDataFile, getProject(), null);

		if (exporter == null)
			exporter = (FileInterpreterI)hireNamedEmployee(FileInterpreterI.class, "#InterpretFastaDNA");
		exporter.doCommand("includeGaps","true", CommandChecker.defaultChecker);
		exporter.doCommand("simplifyTaxonName","true", CommandChecker.defaultChecker);
		exporter.doCommand("writeExcludedCharacters","false", CommandChecker.defaultChecker);

		if (exporter!=null) {
			String ext = exporter.preferredDataFileExtension();
			String s = "file = " + StringUtil.tokenize(fileName) + " directory = " + StringUtil.tokenize(path) + " usePrevious ";
			coord.export(exporter, tempDataFile, s);
		}
		newMatrix.deleteMe(false);
		newTaxa.deleteMe(false);
		coord.closeFile(tempDataFile, true);
		getProject().decrementProjectWindowSuppression();
		return true;
	}
}


