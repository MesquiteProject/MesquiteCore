/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genomic.ASTRALFromGeneTrees;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.distance.lib.*;
import mesquite.externalCommunication.lib.AppChooser;
import mesquite.io.lib.IOUtil;

/* ======================================================================== */
public class ASTRALFromGeneTrees extends TreeInferer {  
	boolean useBuiltInIfAvailable = false;
	String builtinVersion;
	String astralPath = ""; 
	String alternativeManualPath ="";

	
	TreeSource treeSourceTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		treeSourceTask= (TreeSource)hireNamedEmployee(TreeSource.class, "#StoredTrees");
		if (treeSourceTask == null)
			return false;
		if (!MesquiteThread.isScripting()) {
			if (!queryOptions())
				return false;
		}
		return true;
	}

	public boolean isReconnectable(){
		return false;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the source of distances for use in cluster analysis", "[name of module]", commandName, "setDistanceSource")) { 
			return null;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}

	/*.................................................................................................................*/
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("alternativeManualPath".equalsIgnoreCase(tag)) {
			alternativeManualPath = content;
		}
		else if ("useBuiltInIfAvailable".equalsIgnoreCase(tag)) {
			useBuiltInIfAvailable = MesquiteBoolean.fromTrueFalseString(content);
		}
		super.processSingleXMLPreference(tag, content);
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		if (!StringUtil.blank(alternativeManualPath))
			StringUtil.appendXMLTag(buffer, 2, "alternativeManualPath", alternativeManualPath);  
		StringUtil.appendXMLTag(buffer, 2, "useBuiltInIfAvailable", useBuiltInIfAvailable);  
		return super.preparePreferencesForXML()+buffer.toString();
	}
	/*.................................................................................................................*/
	public void queryLocalOptions () {
		if (queryOptions())
			storePreferences();
	}
	/*.................................................................................................................*/
	SingleLineTextField programPathField =  null;
	SingleLineTextField manualOptionsField =  null;

	public boolean queryOptions() {
		if (!okToInteractWithUser(CAN_PROCEED_ANYWAY, "Querying Options")) 
			return true;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(),  "Options for ASTRAL",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		
		
		AppChooser appChooser = new AppChooser("ASTRAL", "ASTRAL", useBuiltInIfAvailable, alternativeManualPath);
		appChooser.addToDialog(dialog);
		dialog.addHorizontalLine(1);
		dialog.addLargeOrSmallTextLabel("This is a very early draft of ASTRAL communication. Eventually, there were will options for running ASTRAL. "
		+" Also, there will be a choice to either use existing gene trees, or to infer them now, before sending them to ASTRAL. "
		+"For now, it will use a block of gene trees already in the file.");


		/*programPathField = dialog.addTextField("Path to trimAl:", trimAlPath, 40);
		Button programBrowseButton = dialog.addAListenedButton("Browse...",null, this);
		programBrowseButton.setActionCommand("programBrowse");
		*/
		dialog.addBlankLine();
		dialog.addHorizontalLine(1);

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			astralPath = appChooser.getPathToUse();
 			alternativeManualPath = appChooser.getManualPath(); //for preference writing
			useBuiltInIfAvailable = appChooser.useBuiltInExecutable(); //for preference writing
			builtinVersion = appChooser.getVersion(); //for informing user; only if built-in
		storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	public void fillTreeBlock(TreeVector treeList, int numberIfUnlimited){
		if (treeList==null)
			return;
		Taxa taxa = treeList.getTaxa();
		treeSourceTask.setPreferredTaxa(taxa);
		int numGTrees = treeSourceTask.getNumberOfTrees(taxa);
		if (numGTrees==0)
			return;
		String rootDir = createSupportDirectory() + MesquiteFile.fileSeparator;  
		String unique = MesquiteFile.massageStringToFilePathSafe(MesquiteTrunk.getUniqueIDBase() + Math.abs((new Random(System.currentTimeMillis())).nextInt()));

		MesquiteStringBuffer outputBuffer = new MesquiteStringBuffer(numGTrees*taxa.getNumTaxa()*10);
		for (int itr = 0; itr<numGTrees; itr++) {
			MesquiteTree tree = (MesquiteTree)treeSourceTask.getTree(taxa, itr);
			outputBuffer.append(tree.writeTreeSimpleByNames()); 
			outputBuffer.append(StringUtil.lineEnding());
		}
		MesquiteFile.putFileContents(rootDir + unique+"genes.tre", outputBuffer,false);

		String scriptPath = rootDir + "astralScipt" + unique + ".bat";
		String outputPath = rootDir + unique + "output.tre";
	//	String astralPath = "/Users/wmaddisn/Mesquite Workspace/MesquiteCore/Mesquite_Folder/apps/astral.app/Contents/Resources/astral.jar";


		String script = ShellScriptUtil.getChangeDirectoryCommand(rootDir) + "\n";
		script += "java -jar " + StringUtil.protectFilePath(astralPath) + "  -i " + unique + "genes.tre -t 8 -o " + unique + "output.tre 2> " + unique + "Astral.log";
		MesquiteFile.putFileContents(scriptPath, script, false);
		boolean success = ShellScriptUtil.executeAndWaitForShell(scriptPath);

		if (success){

			if (!MesquiteFile.fileExists(outputPath)) {
				deleteSupportDirectory();
				return;
			}
			MesquiteFile outputFile = new MesquiteFile();
			outputFile.setPath(outputPath);
			if (outputFile.openReading()) {
				TreeVector trees = IOUtil.readPhylipTrees(this,getProject(), outputFile, null, null, taxa, true, null, "ASTRAL", false);
				if (trees != null)
					for (int itr = 0; itr<trees.size(); itr++)
						treeList.addElement(trees.elementAt(itr), false);
				outputFile.closeReading();
			}

		}
		deleteSupportDirectory();

		treeList.setName("Trees from ASTRAL");
		treeList.setAnnotation ("Parameters:  ", false);
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public boolean hasLimitedTrees(Taxa taxa){
		return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "ASTRAL from Gene Trees";
	}
	public String getNameForMenuItem() {
		return "ASTRAL from Gene Trees...";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Supplies species tree obtained from available gene trees.";
	}

	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
}


