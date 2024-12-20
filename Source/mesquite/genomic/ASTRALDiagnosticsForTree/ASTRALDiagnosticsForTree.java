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

import mesquite.genomic.lib.ASTRALLiaison;
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
		liaison = new ASTRALLiaison(this, "ASTRAL3", "ASTRAL-III");
		loadPreferences();
		if (!MesquiteThread.isScripting()) {
			if (!liaison.queryOptions())
				return false;
		}
		addMenuItem("Close ASTRAL diagnostics", makeCommand("close",  this));
	return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "ASTRAL-III Diagnostics";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Gives ASTRAL-III diagnostics based on stored gene trees." ;
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

	public void diagnoseTree(MesquiteTree tree){
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
		MesquiteTree speciesTree = tree.cloneTree();
		speciesTree.deassignAssociated();
		speciesTree.removeAllInternalNodeLabels(speciesTree.getRoot());
		
		//Debugg.println if name as _ or other special, gets quoted, therefore ASTRAL fails. need to use t0!!!!!!!
		MesquiteFile.putFileContents(rootDir + unique+"species.tre", speciesTree.writeTreeSimpleByNames(),false);

		String scriptPath = rootDir + "astralScipt" + unique + ".bat";
		String outputPath = rootDir + unique + "output.tre";


		String script = ShellScriptUtil.getChangeDirectoryCommand(rootDir) + "\n";
		//	if (liaison.usingASTRAL_III())
 script += "java -jar " +StringUtil.protectFilePath(liaison.getASTRALPath()) + "  -i " + unique + "genes.tre  -q " + unique+"species.tre -t 8 -o " + unique + "output.tre 2> " + unique + "Astral.log";
		//	else
		//		script += StringUtil.protectFilePath(liaison.getASTRALPath()) + " -C -c " + unique+"species.tre" + "  -s 0 -u 2 -o " + unique + "output.tre  " + unique + "genes.tre 2> " + unique + "Astral.log";
		MesquiteFile.putFileContents(scriptPath, script, false);
		boolean success = ShellScriptUtil.executeAndWaitForShell(scriptPath);
		NameReference 	q1Ref = NameReference.getNameReference("q1");
		NameReference 	q2Ref = NameReference.getNameReference("q2");
		NameReference 	q3Ref = NameReference.getNameReference("q3");
		NameReference 	colorRGBNameRef = NameReference.getNameReference("colorRGB");
		NameReference 	palenessRef = NameReference.getNameReference("drawPale");
		if (success){
			if (!MesquiteFile.fileExists(outputPath)) {
				//deleteSupportDirectory(); //Debugg.print
				return;
			}
			MesquiteFile outputFile = new MesquiteFile();
			outputFile.setPath(outputPath);
			if (outputFile.openReading()) {
				TreeVector trees = IOUtil.readPhylipTrees(this,getProject(), outputFile, null, null, taxa, true, null, "ASTRAL", false, true);
				if (trees != null) {
					for (int itr = 0; itr<trees.size(); itr++) {
						MesquiteTree treeO = (MesquiteTree)trees.elementAt(itr);
						treeO =	liaison.moveASTRALSupportToAssociated(treeO);
						//Debugg.println("===============incoming======================\n" + treeO.writeTreeSimpleByNames());
						
						//Debugg.println: NOTE: if tree differs in topology
						if (treeO.equalsTopology(tree, false)) {
							tree.transferAssociated(treeO, 1, q1Ref);
							tree.transferAssociated(treeO, 1, q2Ref);
							tree.transferAssociated(treeO, 1, q3Ref);
							tree.transferAssociated(treeO, 2, colorRGBNameRef);
							tree.transferAssociated(treeO, 1, palenessRef);
						}
						//	Debugg.println("===============current======================\n" + tree.writeTreeSimpleByNames());
						/*	MesquiteTree tree = (MesquiteTree)trees.elementAt(itr);
						tree = liaison.extractASTRALInfo((MesquiteTree)tree);
						treeList.addElement(tree, false);*/
					}
				}
				outputFile.closeReading();
			}

		}
		else
			MesquiteMessage.warnUser("ASTRAL communication didn't work!");
		//deleteSupportDirectory(); //Debugg.print
		if (false) {
			MesquiteModule mb = findEmployerWithDuty(TreeWindowMaker.class);
		if (mb != null) {
	  		String commands = "getEmployee #mesquite.ornamental.DrawTreeAssocDoubles.DrawTreeAssocDoubles; tell It; setOn on; toggleShow q1; toggleShow q2; toggleShow q3; endTell; ";
			Puppeteer p = new Puppeteer(this);
			MesquiteInteger pos = new MesquiteInteger(0);

			pos.setValue(0);
			CommandRecord cRecord = new CommandRecord(true);
			CommandRecord prevR = MesquiteThread.getCurrentCommandRecord();
			MesquiteThread.setCurrentCommandRecord(cRecord);
			
			p.execute(mb, commands, pos, "", false);
			MesquiteThread.setCurrentCommandRecord(prevR);
		}
		}
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
		//Debugg.println("setTree in displayer " + tree.getID());
		ownerModule.diagnoseTree(((MesquiteTree)tree));

	}

	public void turnOff() {
		super.turnOff();
	}
}




