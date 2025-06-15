/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.

 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org

 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.treefarm.OpenLiveTreeFile;

import java.awt.Checkbox;
import mesquite.io.InterpretPhylipTreesBasic.InterpretPhylipTreesBasic;
import mesquite.io.lib.TryNexusFirstTreeFileInterpreter;
import mesquite.lib.*;
import mesquite.lib.duties.FileCoordinator;
import mesquite.lib.duties.GeneralFileMakerSingle;
import mesquite.lib.duties.NexusFileInterpreter;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.NewickDialect;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeVector;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.ListDialog;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.trees.ChronogramDisplay.ChronogramDisplay;

/* ======================================================================== */
public class OpenLiveTreeFile extends GeneralFileMakerSingle {

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}

	/*.................................................................................................................*/
	public MesquiteProject readFile(MesquiteProject project, boolean NEXUSOnly){
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

	
		if (project != null){  //set to false when allow non-NEXUS
			//figure out taxa block if needed
			Taxa taxa = project.chooseTaxa(containerOfModule(), "For which block of taxa does the incoming tree file pertain?");
			FileCoordinator bfc = project.getCoordinatorModule();
			if (isNexus){
				String fra = " @justTheseBlocks.TAXA.DATA.TREES";
				bfc.includeFile(path, NexusFileInterpreter.class, " @autodeleteDuplicateOrSubsetTaxa" + fra, 0, null);
			}
			else
				bfc.includeFile(path, InterpretPhylipTreesBasic.class, " @autodeleteDuplicateOrSubsetTaxa", 0, null);
			String commands = "requireSaveAs true; getEmployee #BasicTreeWindowCoord; tell it; makeTreeWindow " + getProject().getTaxaReferenceInternal(taxa) + "  #BasicTreeWindowMaker; tell It;";  
			commands += "  setTreeSource  #ManyTreesFromFile; tell It;  setFilePath " + StringUtil.tokenize(path) + "; toggleLive on;   endTell;  " + 
			"getWindow; tell It; setTreeNumber 1; pinToLastTree true;   endTell; showWindowForce; endTell; endTell;";
			MesquiteInteger pos = new MesquiteInteger(0);
			Puppeteer p = new Puppeteer(this);
			CommandRecord prev = MesquiteThread.getCurrentCommandRecord();
			CommandRecord cRec = new CommandRecord(true);
			MesquiteThread.setCurrentCommandRecord(cRec);
			p.execute(bfc, commands, pos, null, false, null, null);
			MesquiteThread.setCurrentCommandRecord(prev);
		
			return null;
		}

		String commands = "requireSaveAs true; getEmployee #BasicTreeWindowCoord; tell it; makeTreeWindow 0  #BasicTreeWindowMaker; tell It;";  
		commands += "  setTreeSource  #ManyTreesFromFile; tell It;  setFilePath " + StringUtil.tokenize(path) + "; toggleLive on;   endTell;  " + 
		"getWindow; tell It; setTreeNumber 1; pinToLastTree true;   endTell; showWindowForce; endTell; endTell;";
		MesquiteProject pr = MesquiteTrunk.mesquiteTrunk.openOrImportFileHandler(path, " @justTheseBlocks.TAXA.DATA @scriptToFileCoordinator." + StringUtil.tokenize(commands), TryNexusFirstTreeFileInterpreter.class);
	
		return pr;
	}
	
	/*.................................................................................................................*/
	public MesquiteProject establishProject(String arguments) {
		incrementMenuResetSuppression();

		MesquiteProject proj = readFile(null, true);  //set to false when allow non-NEXUS
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
		return "Open Live NEXUS Tree File";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Open Live NEXUS Tree File...";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Opens a NEXUS file with trees and shows the trees in a tree window. You can use this to monitor an ongoing tree inference analysis, because if the file changes, new trees are automatically read. By default, the tree window shows the last tree in the file.\n"
				+" This is designed to work with a file that includes only one taxa block." ;
	}


}
