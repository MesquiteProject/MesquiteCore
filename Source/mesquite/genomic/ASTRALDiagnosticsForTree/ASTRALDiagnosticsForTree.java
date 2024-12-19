/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genomic.ASTRALDiagnosticsForTree;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.event.KeyEvent;

import mesquite.genomic.ASTRALFromGeneTrees.ASTRALLiaison;
import mesquite.io.lib.IOUtil;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.pairwise.lib.*;

/* ======================================================================== */
public class ASTRALDiagnosticsForTree extends TreeDisplayAssistantA {
	ASTRALLiaison liaison;
	TreeSource treeSourceTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		treeSourceTask= (TreeSource)hireNamedEmployee(TreeSource.class, "#StoredTrees");
		if (treeSourceTask == null)
			return false;
		liaison = new ASTRALLiaison(this);
		loadPreferences();
		if (!MesquiteThread.isScripting()) {
			if (!liaison.queryOptions())
				return false;
		}
		return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "ASTRAL Diagnostics";
	}
	
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Gives ASTRAL diagnostics based on stored gene trees." ;
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
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		Debugg.println("createTreeDisplayExtra %%%%%");
		ASTRALDiagnosticsDisplayer newDisplayer = new ASTRALDiagnosticsDisplayer(this, treeDisplay, 0);
		return newDisplayer;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("requireCalculate"); 
		temp.addLine("calculate");
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	boolean requireCalculate = false;
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Goes to next pairing", null, commandName, "nextPairing")) {
		}
		
		else if (checker.compare(this.getClass(), "Turn off pairwise comparison", null, commandName, "closeShowPairs")) {
			iQuit();
			resetContainingMenuBar();
		}
		return null;
	}
	
	public void diagnoseTree(Tree tree){
		Taxa taxa = tree.getTaxa();
		treeSourceTask.setPreferredTaxa(taxa);
		int numGTrees = treeSourceTask.getNumberOfTrees(taxa);
		if (numGTrees==0)
			return;
		String rootDir = createSupportDirectory() + MesquiteFile.fileSeparator;  
		String unique = MesquiteFile.massageStringToFilePathSafe(MesquiteTrunk.getUniqueIDBase() + Math.abs((new Random(System.currentTimeMillis())).nextInt()));

		MesquiteStringBuffer outputBuffer = new MesquiteStringBuffer(numGTrees*taxa.getNumTaxa()*10);
		for (int itr = 0; itr<numGTrees; itr++) {
			MesquiteTree geneTree = (MesquiteTree)treeSourceTask.getTree(taxa, itr);
			outputBuffer.append(geneTree.writeTreeSimpleByNames()); 
			outputBuffer.append(StringUtil.lineEnding());
		}
		MesquiteFile.putFileContents(rootDir + unique+"genes.tre", outputBuffer,false);
		MesquiteFile.putFileContents(rootDir + unique+"species.tre", tree.writeTreeSimpleByNames(),false);

		String scriptPath = rootDir + "astralScipt" + unique + ".bat";
		String outputPath = rootDir + unique + "output.tre";
		
		Debugg.println("=====  " + liaison.getASTRALPath());

		String script = ShellScriptUtil.getChangeDirectoryCommand(rootDir) + "\n";
		if (liaison.usingASTRAL_III())
			script += "java -jar " +StringUtil.protectFilePath(liaison.getASTRALPath()) + "  -i " + unique + "genes.tre -t 8 -o " + unique + "output.tre 2> " + unique + "Astral.log";
		else
			script += StringUtil.protectFilePath(liaison.getASTRALPath()) + " -C -c " + unique+"species.tre" + "  -s 0 -u 2 -o " + unique + "output.tre  " + unique + "genes.tre 2> " + unique + "Astral.log";
		MesquiteFile.putFileContents(scriptPath, script, false);
		boolean success = ShellScriptUtil.executeAndWaitForShell(scriptPath);

		if (success){

			if (!MesquiteFile.fileExists(outputPath)) {
				//deleteSupportDirectory();
				return;
			}
			MesquiteFile outputFile = new MesquiteFile();
			outputFile.setPath(outputPath);
			Debugg.println("outputPath " + outputPath);
			if (outputFile.openReading()) {
				TreeVector trees = IOUtil.readPhylipTrees(this,getProject(), outputFile, null, null, taxa, true, null, "ASTRAL", false, true);
				if (trees != null)
					for (int itr = 0; itr<trees.size(); itr++) {
						MesquiteTree treeO = (MesquiteTree)trees.elementAt(itr);
						// Debugg.println(treeO.writeTreeSimpleByNames());
					/*	MesquiteTree tree = (MesquiteTree)trees.elementAt(itr);
						tree = liaison.extractASTRALInfo((MesquiteTree)tree);
						treeList.addElement(tree, false);*/
					}
				outputFile.closeReading();
			}

		}
		//deleteSupportDirectory();

	}
	/*.................................................................................................................*/
	public void resetAllOperators() {
		Debugg.println("RAO=========");
	}
	public String nameForWritableResults(){
		return "ASTRAL Diagnostics";
	}
	
	public boolean suppliesWritableResults(){
		return true;
	}
	public Object getWritableResults(){
		String results = "";
		
		
		return results;
	}
 	/*.................................................................................................................*/
	public void endJob() {
		super.endJob();
	}
	public boolean isPrerelease(){
		return false;
	} 
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}

}

/* ======================================================================== */
class ASTRALDiagnosticsDisplayer extends TreeDisplayDrawnExtra {
	public TaxaPairing pairing;

	ASTRALDiagnosticsForTree ownerModule;

	public ASTRALDiagnosticsDisplayer (ASTRALDiagnosticsForTree ownerModule, TreeDisplay treeDisplay, int numTaxa) {
		super(ownerModule, treeDisplay);
		this.ownerModule = ownerModule;
	}
	public void initiate() {
		Debugg.println("initiate id displayer %%%%%");

	}

	public String textForLegend(){
		return getWritableText();
	}
	
	public String getWritableText(){
		return getTextVersion();
	}
	
	public String textAtNode(Tree tree, int node){
		
		return"";
	}

	
	String getTextVersion(){
		return "";
	}
	

	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		
	}
	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		Debugg.println("setTree in displayer " + tree.getID());
		ownerModule.diagnoseTree(tree);
	
	}

	public void turnOff() {
		super.turnOff();
	}
}




