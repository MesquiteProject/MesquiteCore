/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.

 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org

 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.basic.OpenFileSpecifyTreeDialect;

import java.awt.Checkbox;
import mesquite.io.InterpretPhylipTreesBasic.InterpretPhylipTreesBasic;
import mesquite.io.lib.TryNexusFirstTreeFileInterpreter;
import mesquite.lib.*;
import mesquite.lib.duties.GeneralFileMakerSingle;
import mesquite.lib.duties.NexusFileInterpreter;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.NewickDialect;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.ListDialog;

/* ======================================================================== */
public class OpenFileSpecifyTreeDialect extends GeneralFileMakerSingle {

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}

	/*.................................................................................................................*/
	public MesquiteProject readFileWTreeDialect(MesquiteProject project, boolean NEXUSOnly, boolean treesOnly){
		ExtensibleDialog dialog = null; //to be formed later
		// === the file chosen
		MesquiteString fileName = new MesquiteString();
		String choice = "Choose NEXUS file with trees";
		if (!NEXUSOnly)
			choice += " or Phylip/Newick tree file";
		String path = MesquiteFile.openFileDialog( choice,  null, fileName);
		if (path == null)
			return null;
		String startOfFile = MesquiteFile.getFileContentsAsString(path, 200); 
		String token = parser.getFirstToken(startOfFile);
		boolean isNexus = "#NEXUS".equalsIgnoreCase(token);
		if (NEXUSOnly && !isNexus) {
			discreetAlert("Sorry, this is not a NEXUS file");
			return null;
		}

		boolean includingInExistingProject = project != null;

		//Make list of available Newick dialects
		String dialectName = null;
		String[] dialectHumanNames = null;
		String[] dialectInternalNames = null;
		MesquiteInteger selectedInDialog = new MesquiteInteger(0);
		if (MesquiteTree.dialects.size()>0){ //multiple dialects; ask which one
			dialectHumanNames = new String[MesquiteTree.dialects.size()];
			dialectInternalNames = new String[MesquiteTree.dialects.size()];
			for (int i=0; i<dialectHumanNames.length; i++){
				NewickDialect dialect = (NewickDialect)MesquiteTree.dialects.elementAt(i);
				dialectHumanNames[i] = dialect.getHumanName();
				dialectInternalNames[i] = dialect.getName();
			}
			dialog = new ListDialog(containerOfModule(), "Reading file \"" + fileName + "\"", "In which dialect of Newick are the trees described?", false,null, dialectHumanNames, 8, selectedInDialog, "OK", null, false, true);
		}
		else if (!isNexus) //no dialects, but since Phylip/Newick, must ask about auto-convert and also warn about taxon names
			dialog = new ExtensibleDialog(containerOfModule(), "Reading tree file");
		//	else if (includingInExistingProject)
		//		dialog = new ExtensibleDialog(containerOfModule(), "Reading NEXUS file with trees");

		//Completing the dialog with extra items
		String autoSaveString = "";

		if (dialog != null){
			Checkbox autoSaveB = null;
			//NOTE these two check boxes might seem to interact, but will autosave only if Phylip, 
			//and will include only if NEXUS. The reason inclusion is allowed only with NEXUS is that 
			//phylip reading will try to use an existing taxa block, which can cause unrecognized taxon name warnings.
			//Debugg.println fix this by file reading hint to switch to invent taxa block if taxon name not recognized?!?!
			//or warn user that only to be used if same taxa block???
			if (!isNexus)
				autoSaveB = dialog.addCheckBox("Auto-save converted NEXUS file", true); 
			dialog.addHorizontalLine(1);
			if (!isNexus) {
				String warning = "";
				if (includingInExistingProject)
					warning = "\n\nNote!: Because this file is not a NEXUS file, Mesquite will assume (1) that the taxon names match those in the already-open project and (2) that it is a Phylip/Newick tree file. "
							+ "If the taxon names don't match, tree reading will fail, and you may want to open the file separately using File>Open Special>Open File (Specify Tree Dialect). "
							+ "If it is not a Phylip/Newick tree file, you may want to try the Open File menu Item.";
				dialog.addLargeTextLabel("This file opener can be used only with NEXUS and Phylip/Newick files!"
						+ warning); 
			}

			dialog.completeAndShowDialog(true);
			
			Debugg.println("@dc " + dialog.buttonPressed.getValue());
			if (dialog.buttonPressed.getValue() == 0)  {
				//DIALECT
				if (dialog instanceof ListDialog){
					int result = selectedInDialog.getValue();
					dialectName = "@newickDialect.";
					if (result <0)
						return null;
					else if (result== 0)
						dialectName += "Mesquite";
					else
						dialectName += dialectInternalNames[result];
				}
				if (autoSaveB != null && autoSaveB.getState())
					autoSaveString = " @autosaveImported";
			}
			else return null;
		}

		if (project != null){
			if (isNexus){
				String fra = "";
				if (treesOnly)
					fra = " @justTheseBlocks.TAXA.DATA.TREES";
				project.getCoordinatorModule().includeFile(path, NexusFileInterpreter.class, ParseUtil.tokenize(dialectName) + autoSaveString + " @autodeleteDuplicateOrSubsetTaxa" + fra, 0, null);
			}
			else
				project.getCoordinatorModule().includeFile(path, InterpretPhylipTreesBasic.class, ParseUtil.tokenize(dialectName) + autoSaveString + " @autodeleteDuplicateOrSubsetTaxa", 0, null);
			return null;
		}

		MesquiteProject p = MesquiteTrunk.mesquiteTrunk.openOrImportFileHandler(path, ParseUtil.tokenize(dialectName) + autoSaveString, TryNexusFirstTreeFileInterpreter.class);

		return p;
	}
	/*.................................................................................................................*/
	public MesquiteProject establishProject(String arguments) {
		incrementMenuResetSuppression();

		MesquiteProject proj = readFileWTreeDialect(null, false, false);
		decrementMenuResetSuppression();
		return proj;
	}

	/*.................................................................................................................*/
	public boolean requestPrimaryChoice() {
		return true;
	}
	/*.................................................................................................................*/
	public boolean loadModule() {
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive() {
		return true;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Open File (Specify Tree Dialect)";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Open File (Specify Tree Dialect)...";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Reads NEXUS files and PHYLIP/Newick tree files "
				+"with more flexible reading of trees than in the normal \"Open File\". It can adjust for the diverse and incompatible formats (\"dialects\") used by different programs "
				+"(BEAST, MrBayes, IQ-TREE, etc.) to store special properties in trees such as posterior probabilities, "
				+ "divergence times, or concordance factors. (The regular \"Open File\" can read such tree information only if saved in the standard BEAST/Mesquite format.)"
				+ " When reading NEXUS files, Open File (Specify Tree Dialect) reads more than just the trees, including matrices and other information. For PHYLIP format, it can read only tree files."
				+" Note: if you want to merge such trees into an existing project, use the similar File>Include & Merge>Include File (Specify Tree Dialect)." ;
	}


}
