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

import mesquite.io.InterpretPhylipTreesBasic.InterpretPhylipTreesBasic;
import mesquite.io.lib.TryNexusFirstTreeFileInterpreter;
import mesquite.lib.CommandChecker;
import mesquite.lib.CommandRecord;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.Puppeteer;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.FileCoordinator;
import mesquite.lib.duties.GeneralFileMakerSingle;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.QueryDialogs;
import mesquite.lib.ui.RadioButtons;

/* ======================================================================== */
public class OpenLiveTreeFile extends GeneralFileMakerSingle {
	String path;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	/* ................................................................................................................. */
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("showConsensus " + showConsensus);
		temp.addLine("sample " + sample);
		return temp;
	}
	boolean showConsensus = false;
	boolean sample = false;
	String sampleString = "";
	/* ................................................................................................................. */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets whether to show the consensus or last tree", "[true or false]", commandName, "showConsensus")) {
			showConsensus = MesquiteBoolean.fromTrueFalseString(arguments);
		}
		else if (checker.compare(this.getClass(), "Sets whether to sample", "[true or false]", commandName, "sample")) {
			sample = MesquiteBoolean.fromTrueFalseString(arguments);
		}

		else
			return super.doCommand(commandName, arguments, checker);
		return null;
	}	

	/*.................................................................................................................*/
	public MesquiteProject readFile(MesquiteProject project, String path, boolean NEXUSOnly){
		ExtensibleDialog dialog = null; //to be formed later
		// === the file chosen
		boolean newFile = false;
		MesquiteString fileName = new MesquiteString();
		if (path == null) {
			String choice = "Choose NEXUS file with trees";
			if (!NEXUSOnly)
				choice = "Choose NEXUS or Newick/Phylip tree file";
			path = MesquiteFile.openFileDialog( choice,  null, fileName);
			newFile = true;
		}
		else
			fileName.setValue(StringUtil.getLastItem(path, MesquiteFile.fileSeparator));
		this.path = path;
		if (path == null)
			return null;
		String startOfFile = MesquiteFile.getFileContentsAsString(path, 200); 
		String token = parser.getFirstToken(startOfFile);
		boolean isNexus = "#NEXUS".equalsIgnoreCase(token);
		if (NEXUSOnly && !isNexus) {
			discreetAlert("Sorry, this is not a NEXUS file");
			return null;
		}
		if (!MesquiteThread.isScripting()){
			MesquiteInteger buttonPressed = new MesquiteInteger(1);
			ExtensibleDialog eDialog = new ExtensibleDialog(containerOfModule(),  "Live trees options",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
			eDialog.addLabel("Do you want to show the majority rules consensus, or the last tree?");
			RadioButtons radios = new RadioButtons(eDialog, new String[] { "Consensus", "Last Tree"}, 0);
			/*
			eDialog.addLabel("Do you want to see all of the trees, or just a sample of the trees?");
			RadioButtons radios2 = new RadioButtons(eDialog, new String[] { "All Trees", "Sample (e.g., minus burn-in, or every 100th)"}, 0);
			eDialog.addLabel("If you choose to sample, you can set the parameters by items in the Tree menu.");*/
			eDialog.completeAndShowDialog(true);
			if (buttonPressed.getValue()==0)  {
				showConsensus = radios.getValue() == 0;
				/*sample = radios2.getValue() == 1;
				sampleString = "";
				if (sample)
					sampleString = "Sample";*/
			}
			eDialog.dispose();
			if (buttonPressed.getValue()!=0)
				return null;
			//int choice = QueryDialogs.queryTwoRadioButtons(containerOfModule(), "Consensus or last?", "Do you want to show the majority rules consensus, or the last tree?", null, "Consensus", "Last Tree");
		}

		if (project != null){  
			//figure out taxa block if needed
			Taxa taxa = project.chooseTaxa(containerOfModule(), "For which block of taxa does the incoming tree file pertain?");
			FileCoordinator bfc = project.getCoordinatorModule();
		
			if (!MesquiteThread.isScripting()){
				String commands = "getEmployee #BasicTreeWindowCoord; tell it; makeTreeWindow " + getProject().getTaxaReferenceInternal(taxa) + "  #BasicTreeWindowMaker; tell It;";  
				if (showConsensus){
					commands += " setTreeSource  #ConsensusTree; tell It;  suspend; " + 
							" setTreeSource  #" + sampleString +"ManyTreesFromFile; tell It;  setFilePath " + StringUtil.tokenize(path) + "; toggleLive on;   endTell;" + 
							" setConsenser  #MajRuleTree; tell It; frequencyLimit 0.5; endTell; desuspend; endTell;" +  
							" showWindowForce; endTell; endTell;";
				}
				else {
					commands += "  setTreeSource  #" + sampleString +"ManyTreesFromFile; tell It;  setFilePath " + StringUtil.tokenize(path) + "; toggleLive on;   endTell;  " + 
							"getWindow; tell It; setTreeNumber 1; pinToLastTree true;   endTell; showWindowForce; endTell; endTell;";
				}
				
				MesquiteInteger pos = new MesquiteInteger(0);
				Puppeteer p = new Puppeteer(this);
				CommandRecord prev = MesquiteThread.getCurrentCommandRecord();
				CommandRecord cRec = new CommandRecord(true);
				MesquiteThread.setCurrentCommandRecord(cRec);
				p.execute(bfc, commands, pos, null, false, null, null);
				MesquiteThread.setCurrentCommandRecord(prev);
			}
			return null;
		}

		String commands = "getEmployee #BasicTreeWindowCoord; tell it; makeTreeWindow 0  #BasicTreeWindowMaker; tell It;";  
		if (showConsensus){
			commands += " setTreeSource  #ConsensusTree; tell It;  suspend; " + 
					" setTreeSource  #" + sampleString +"ManyTreesFromFile; tell It;  setFilePath " + StringUtil.tokenize(path) + "; toggleLive on;   endTell;" + 
					" setConsenser  #MajRuleTree; tell It; frequencyLimit 0.5; endTell; desuspend; endTell;" +  
					"showWindowForce; endTell; endTell;";
		}
		else {
			commands += "  setTreeSource  #" + sampleString +"ManyTreesFromFile; tell It;  setFilePath " + StringUtil.tokenize(path) + "; toggleLive on;   endTell;  " + 
					"getWindow; tell It; setTreeNumber 1; pinToLastTree true;   endTell; showWindowForce; endTell; endTell;";
		}
		if (newFile) {
			String extension = "";
			if (!isNexus)
				extension = ".nex";
			commands = "requireSaveAs true; renameFile ? " + StringUtil.tokenize("(Monitoring) " + fileName + extension) + "; getEmployee #ManageTrees; tell It; getTreeBlock 0; tell It; deleteMe; endTell; endTell;" + commands;
		}
		MesquiteProject pr = MesquiteTrunk.mesquiteTrunk.openOrImportFileHandler(path, " @justTheseBlocks.TAXA.DATA @autosaveImported @scriptToFileCoordinator." + StringUtil.tokenize(commands), TryNexusFirstTreeFileInterpreter.class);

		return pr;
	}

	public String getPath(){
		return path;
	}
	/*.................................................................................................................*/
	public MesquiteProject establishProject(String arguments) {
		incrementMenuResetSuppression();

		MesquiteProject proj = readFile(null, null, false);  //set to false when allow non-NEXUS
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
		return false;
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
		return "Monitor Live Tree File";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Monitor Live Tree File...";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Opens a NEXUS or simple Newick/Phylip tree file with trees and shows the trees in a tree window. You can use this to monitor an ongoing tree inference analysis, because if the file changes, new trees are automatically read. By default, the tree window shows the LAST tree in the file.\n"
				+" This is designed to work with a file that includes only one block of taxa." ;
	}


}
