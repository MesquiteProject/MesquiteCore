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

import mesquite.lib.CommandChecker;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteModule;
import mesquite.lib.Snapshot;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MatrixFlags;


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
		return new String[] {"#FlagByPhyIN", "#FlagLowOccupancySites", "#FlagBySpruceup", "#FlagByTrimAl"};
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

	/*.................................................................................................................*/
	protected String status = "?";
	boolean oldStyle = false;
	/* As a service to flaggers that call programs to run of fasta files*/
	FileInterpreterI exporter;	

	protected boolean saveFastaFile(CharacterData data, String path, String fileName){
		
			if (exporter == null)
				exporter = (FileInterpreterI)hireNamedEmployee(FileInterpreterI.class, "#InterpretFastaDNA");
			exporter.doCommand("includeGaps","true", CommandChecker.defaultChecker);
			exporter.doCommand("simplifyTaxonName","true", CommandChecker.defaultChecker);
			exporter.doCommand("writeExcludedCharacters","false", CommandChecker.defaultChecker);
			boolean success = false;
			if (exporter!=null) {
				status = "exportFile";
				//coord.export(exporter, tempDataFile, s);
				success = exporter.writeMatrixToFile(data, path + fileName ); 
			}
			return success;
		/* oldStyle
			getProject().incrementProjectWindowSuppression();
			incrementMenuResetSuppression();
			FileCoordinator coord = getFileCoordinator();
			status = "newLinkedFile";
			MesquiteFile tempDataFile = (MesquiteFile)coord.doCommand("newLinkedFile", StringUtil.tokenize(fileName+".nex"), CommandChecker.defaultChecker); //TODO: never scripting???
			TaxaManager taxaManager = (TaxaManager)findElementManager(Taxa.class);
			CharacterData newMatrix=null;
			status = "cloneTaxa";
			Taxa newTaxa =data.getTaxa().cloneTaxa(); 
			newTaxa.addToFile(tempDataFile, null, taxaManager);
			CharactersManager manageCharacters = (CharactersManager)findElementManager(CharacterData.class);
			MCharactersDistribution matrix = data.getMCharactersDistribution();
			CharMatrixManager manager = manageCharacters.getMatrixManager(matrix.getCharacterDataClass());
			status = "makeCharacterData";
			newMatrix = matrix.makeCharacterData(manager, newTaxa);
			newMatrix.setName(data.getName());

			status = "addToFile";
			newMatrix.addToFile(tempDataFile, getProject(), null);

			status = "doneAddToFile";
			if (exporter == null)
				exporter = (FileInterpreterI)hireNamedEmployee(FileInterpreterI.class, "#InterpretFastaDNA");
			exporter.doCommand("includeGaps","true", CommandChecker.defaultChecker);
			exporter.doCommand("simplifyTaxonName","true", CommandChecker.defaultChecker);
			exporter.doCommand("writeExcludedCharacters","false", CommandChecker.defaultChecker);

			if (exporter!=null) {
				String ext = exporter.preferredDataFileExtension();
				String s = "file = " + StringUtil.tokenize(fileName) + " directory = " + StringUtil.tokenize(path) + " usePrevious ";
				status = "exportFile";
				//coord.export(exporter, tempDataFile, s);
				boolean success = exporter.exportFile(tempDataFile, s); 
			}
			status = "deleteMe";
			newMatrix.deleteMe(false);
			newTaxa.deleteMe(false);
			status = "closeFile";
			coord.closeFile(tempDataFile, true);
			status = "fileClosed";
			decrementMenuResetSuppression();
			getProject().decrementProjectWindowSuppression();
			return true;
		}
		*/

	}

}


