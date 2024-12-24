/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.stgt.lib;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.ImageObserver;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.stgt.SpeciesTreeGeneTreeSearcher.SpeciesTreeGeneTreeSearcher;
import mesquite.stgt.lib.ASTRALLiaison;
import mesquite.stgt.lib.SpeciesTreeGeneTreeAnalysis;
import mesquite.distance.lib.*;
import mesquite.externalCommunication.lib.AppChooser;
import mesquite.io.lib.IOUtil;

/* ======================================================================== */
public abstract class ASTRALFromGeneTrees extends TreeSearcher implements SpeciesTreeGeneTreeAnalysis {  
	ASTRALLiaison liaison;

	TreeSource treeSourceTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		treeSourceTask= (TreeSource)hireNamedEmployee(TreeSource.class, "#StoredTrees");
		if (treeSourceTask == null)
			return false;
		liaison = new ASTRALLiaison(this, getOfficialAppNameInAppInfo(), getProgramNameForDisplay());
		loadPreferences();
		if (!MesquiteThread.isScripting()) {
			if (!liaison.queryOptions())
				return false;
		}
		return true;
	}
	
	protected abstract String getOfficialAppNameInAppInfo();
	protected abstract String getProgramNameForDisplay();
	protected abstract String executionCommand(String ASTRALPath, String geneTreesPath, String outputPath, String logPath);

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
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public boolean initialize(Taxa taxa){
		return true;
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if (liaison != null)
			liaison.processSingleXMLPreference(tag, content);


		super.processSingleXMLPreference(tag, content);
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		return super.preparePreferencesForXML()+liaison.preparePreferencesForXML();
	}
	/*.................................................................................................................*/
	public void queryLocalOptions () {
		if (liaison.queryOptions())
			storePreferences();
	}
	/*.................................................................................................................*/
	
	/*.................................................................................................................*/
   /** TreeBlockFillers should override this if they want special commands to be sent to a tree window if a tree window is created after they are used. */
 	 public String getExtraTreeWindowCommands (boolean finalTree, long treeBlockID){
  		String extras = super.getExtraTreeWindowCommands(finalTree, treeBlockID);
  		extras += "getOwnerModule; tell it; ";
  		extras += "getEmployee #mesquite.ornamental.DrawTreeAssocDoubles.DrawTreeAssocDoubles; tell It; setOn on; toggleShow q1; toggleShow q2; toggleShow q3; endTell; ";
  		extras += "getTreeDrawCoordinator #mesquite.trees.BasicTreeDrawCoordinator.BasicTreeDrawCoordinator; tell It;";
  		extras +="getEmployee #mesquite.trees.BasicDrawTaxonNames.BasicDrawTaxonNames; tell It; toggleNodeLabels off; endTell;";
  		extras += "setTreeDrawer  #mesquite.trees.SquareLineTree.SquareLineTree; tell It; setEdgeWidth 6; endTell;";
 		extras += "endTell;";
  		extras += "endTell;";
 		return extras;
	 }
	/*.................................................................................................................*/
 	public int fillTreeBlock(TreeVector treeList) {
		if (treeList==null)
			return NULLVALUE;
		Taxa taxa = treeList.getTaxa();
		treeSourceTask.setPreferredTaxa(taxa);
		int numGTrees = treeSourceTask.getNumberOfTrees(taxa);
		if (numGTrees==0)
			return -3;
		String rootDir = createSupportDirectory() + MesquiteFile.fileSeparator;  
		String unique = MesquiteFile.massageStringToFilePathSafe(MesquiteTrunk.getUniqueIDBase() + Math.abs((new Random(System.currentTimeMillis())).nextInt()));

		MesquiteStringBuffer outputBuffer = new MesquiteStringBuffer(numGTrees*taxa.getNumTaxa()*10);
		for (int itr = 0; itr<numGTrees; itr++) {
			MesquiteTree geneTree = (MesquiteTree)treeSourceTask.getTree(taxa, itr);
			outputBuffer.append(geneTree.writeTreeSimpleByNames()); 
			outputBuffer.append(StringUtil.lineEnding());
		}
		MesquiteFile.putFileContents(rootDir + unique+"genes.tre", outputBuffer,false);

		String scriptPath = rootDir + "astralScipt" + unique + ".bat";
		String outputPath = rootDir + unique + "output.tre";


		String script = ShellScriptUtil.getChangeDirectoryCommand(rootDir) + "\n";
		//if (liaison.usingASTRAL_III())
		//	script += "java -jar " +StringUtil.protectFilePath(liaison.getASTRALPath()) + "  -i " + unique + "genes.tre -t 8 -o " + unique + "output.tre 2> " + unique + "Astral.log\n";
		//else
		//script += StringUtil.protectFilePath(liaison.getASTRALPath()) + "  -u 2 -o " + unique + "output.tre " + unique + "genes.tre 2> " + unique + "Astral.log\n";
		script += executionCommand(liaison.getASTRALPath(), unique + "genes.tre", unique + "output.tre ", unique + "Astral.log");
		MesquiteFile.putFileContents(scriptPath, script, false);
		boolean success = ShellScriptUtil.executeAndWaitForShell(scriptPath);

		if (success){

			if (!MesquiteFile.fileExists(outputPath)) {
				deleteSupportDirectory();
				return -4;
			}
			MesquiteFile outputFile = new MesquiteFile();
			outputFile.setPath(outputPath);
			if (outputFile.openReading()) {
				TreeVector trees = IOUtil.readPhylipTrees(this,getProject(), outputFile, null, null, taxa, true, null, "ASTRAL", false, true);
				if (trees != null)
					for (int itr = 0; itr<trees.size(); itr++) {
						MesquiteTree tree = (MesquiteTree)trees.elementAt(itr);
						
						tree = liaison.moveASTRALSupportToAssociated(tree);
						treeList.addElement(tree, false);
					}
				outputFile.closeReading();
			}

		}
		deleteSupportDirectory();

		treeList.setName("Trees from " + getProgramNameForDisplay());
		treeList.setAnnotation ("Parameters:  ", false);
		return NOERROR;
	}
	/*.................................................................................................................*/
	
	/*.................................................................................................................*/
	public boolean hasLimitedTrees(Taxa taxa){
		return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return getProgramNameForDisplay() + " Tree from Gene Trees";
	}
	public String getNameForMenuItem() {
		return getProgramNameForDisplay() + " Tree from Gene Trees...";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Supplies species tree from " + getProgramNameForDisplay() + " obtained from available gene trees.";
	}

	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}

	public Class getCharacterClass() {
		return null;
	}

}


