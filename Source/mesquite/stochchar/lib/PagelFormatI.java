/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.stochchar.lib;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;

/* ============  a file interpreter for Pagel's format files; extended to handle files for programs Discrete and Multistate ============*/

public abstract class PagelFormatI extends FileInterpreterI {
/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;  
  	 }
  	 
/*.................................................................................................................*/
	public String preferredDataFileExtension() {
 		return "ppy";
   	 }
/*.................................................................................................................*/
	public boolean isPrerelease() {  
		 return false; 
	}
	/*.................................................................................................................*/
	public boolean canExportEver() {  
		 return true;
	}
/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		return (dataClass==CategoricalState.class);
	}
/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		return project.getNumberCharMatrices(CategoricalState.class) > 0; 
	}
/*.................................................................................................................*/
	public boolean canImport() {  
		 return true;
	}
	public abstract boolean readTreeAndCharacters(MesquiteFile file, String line, Vector nodes, MesquiteInteger nC);
/*.................................................................................................................*/
	public void readFile(MesquiteProject mf, MesquiteFile file, String arguments) {
		incrementMenuResetSuppression();
		ProgressIndicator progIndicator = new ProgressIndicator(mf,"Importing File "+ file.getName(), file.existingLength());
		progIndicator.start();
		file.linkProgressIndicator(progIndicator);
		if (file.openReading()) {
			boolean abort = false;
			TaxaManager taxaTask = (TaxaManager)findElementManager(Taxa.class);
			 CharactersManager charTask = (CharactersManager)findElementManager(CharacterData.class);
			
			Taxa taxa = taxaTask.makeNewTaxa(getProject().getTaxas().getUniqueName("Taxa from Pagel format file"), 0, false);
			taxa.addToFile(file, getProject(), taxaTask);
			CategoricalData data = (CategoricalData)charTask.newCharacterData(taxa, 0, CategoricalData.DATATYPENAME);
			data.addToFile(file, getProject(), null);
			boolean wassave = data.saveChangeHistory;
			data.saveChangeHistory = false;

			TreeVector trees = new TreeVector(taxa);
			trees.setName("Trees imported from Pagel file");
			int numChars = 1;
			StringBuffer sb = new StringBuffer(1000);
			file.readLine(sb);
			String line = sb.toString();
			Vector nodes = new Vector();
			MesquiteInteger nC = new MesquiteInteger(1);
			MesquiteInteger nT = new MesquiteInteger(1);
			
			abort = !readTreeAndCharacters(file, line, nodes, nC);
			
			numChars = nC.getValue();
			
			//Now parse the nodes to count taxa
			int numTaxa = 0;
			String root = null;
			data.addCharacters(data.getNumChars()-1, numChars, false);   // add at least one character
			for (int i=0; i< nodes.size(); i++){
				PagNodeRecord pnr = (PagNodeRecord)nodes.elementAt(i);
				if (!nodeIsAncestor(nodes, pnr.node)) {
					taxa.addTaxa(numTaxa-1, 1, true);
					Taxon t = taxa.getTaxon(numTaxa);
					if (t!=null) {
						t.setName(pnr.node);
						for (int ic=0; ic<numChars; ic++){
							if (pnr.states != null && ic < pnr.states.length && MesquiteInteger.isCombinable(pnr.states[ic]))
								data.setState(ic, numTaxa, CategoricalState.makeSet(pnr.states[ic]));
						}
					}
					numTaxa++;
				}
				if (getNodeWithName(nodes, pnr.ancestor)==null){ //ancestor doesn't have node; ancestor must be root
					root = pnr.ancestor;
				}
					
			}
			PagNodeRecord pnrRoot = null;
			if (root!=null){
				pnrRoot = new PagNodeRecord(-1, root, null, 1, null);
				nodes.addElement(pnrRoot);
			}
			if (nodes.size() == 0) {
				discreetAlert( "Reading of the Pagel format file failed because tree could not be read");
				finishImport(progIndicator, file, true); //change this to true
				decrementMenuResetSuppression();
				return;
			}
			
			MesquiteTree tree = new MesquiteTree(taxa);
			buildTree(tree, tree.getRoot(), pnrRoot, nodes);
			
			tree.setAsDefined(true);
			tree.setName("Imported tree");
			trees.addElement(tree, false);
			trees.addToFile(file,file.getProject(), findElementManager(TreeVector.class));
			data.saveChangeHistory = wassave;
			data.resetChangedSinceSave();

			finishImport(progIndicator, file, abort);
	 		MesquiteModule treeWindowCoord = getFileCoordinator().findEmployeeWithName("#BasicTreeWindowCoord");
			if (treeWindowCoord!=null){
				String commands = "makeTreeWindow " + getProject().getTaxaReferenceExternal(taxa) + "  #BasicTreeWindowMaker; tell It; ";
				commands += "setTreeSource  #StoredTrees; tell It; setTaxa " + getProject().getTaxaReferenceExternal(taxa) + " ;  setTreeBlock 1; endTell; ";
				commands += "getTreeDrawCoordinator #BasicTreeDrawCoordinator; tell It; suppress; setTreeDrawer #BallsNSticks; ";
				commands += "tell It;  getEmployee #NodeLocsStandard; tell It; branchLengthsToggle on; ";
				commands += " endTell; endTell; desuppress; endTell; getTreeWindow; tell It; setLocation 200 60; ";
				commands += "newAssistant  #TraceCharacterHistory; tell It; suspend; setHistorySource  #RecAncestralStates;";
				commands += "tell It; getCharacterSource  #CharSrcCoordObed; tell It; setCharacterSource #StoredCharacters; endTell;";
				commands += "setMethod  #MargProbAncStates; tell It; setModelSource  #StoredProbModel; tell It; setModel 0 'Mk1 (est.)'; endTell;";
				commands += "endTell; endTell; resume; endTell; endTell; ";
				commands += "  showWindow; endTell; ";
				MesquiteInteger pos = new MesquiteInteger(0);
				Puppeteer p = new Puppeteer(this);
				p.execute(treeWindowCoord, commands, pos, null, false);
			}
		}
		decrementMenuResetSuppression();
	}
	
	protected void buildTree(MesquiteTree tree, int node, PagNodeRecord pagNode, Vector nodes){ //if terminal then
		if (nodes == null || pagNode == null || pagNode.node == null)
			return;
		boolean daughtersBuilt = false;
		for (int i=0; i<nodes.size(); i++) {
			PagNodeRecord pnr = (PagNodeRecord)nodes.elementAt(i);
			if (pagNode.node.equals(pnr.ancestor)) {//this is a daughter node!
				int daughter = tree.sproutDaughter(node, false);
				buildTree(tree, daughter, pnr, nodes);
				daughtersBuilt = true;
			}
		}
		if (!daughtersBuilt){
			tree.setTaxonNumber(node, pagNode.taxonNumber, false);
		}
		tree.setBranchLength(node, pagNode.length, false);
	}
	protected PagNodeRecord getNodeWithName(Vector nodes, String node){
		if (node == null)
			return null;
		for (int i=0; i<nodes.size(); i++) {
			PagNodeRecord pnr = (PagNodeRecord)nodes.elementAt(i);
			if (node.equals(pnr.node))
				return pnr;
		}
		return null;
	}
	protected boolean nodeIsAncestor(Vector nodes, String node){
		if (node == null)
			return false;
		for (int i=0; i<nodes.size(); i++) {
			PagNodeRecord pnr = (PagNodeRecord)nodes.elementAt(i);
			if (node.equals(pnr.ancestor))
				return true;
		}
		return false;
	}
	protected boolean nodeHasAncestor(Vector nodes, String node){
		PagNodeRecord pnr = getNodeWithName(nodes, node);
		if (pnr == null)
			return false;
		return (getNodeWithName(nodes, pnr.ancestor)!=null);
	}

/* ============================  exporting ============================*/
	boolean includeGaps = false;
	/*.................................................................................................................*/
	
	public boolean getExportOptions(boolean dataSelected, boolean taxaSelected){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), "Export Pag-format Options", buttonPressed);
		
		exportDialog.completeAndShowDialog(dataSelected, taxaSelected);
			
		boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);
		
		exportDialog.dispose();
		return ok;
	}	

	/*-----------------------------------------*/
	/** Outputs internal nodes.*/
	protected void numberNodes(Tree tree, int node, int[] numbers, MesquiteInteger nodeCount) {
		if (tree.nodeIsTerminal(node))
			numbers[node] = tree.taxonNumberOfNode(node);
	else  {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				 numberNodes(tree, d, numbers, nodeCount);
			numbers[node] = nodeCount.getValue();
			nodeCount.increment();
		}
	}
	/** Outputs internal nodes.*/
	protected void outputInternals(Tree tree, int node, int[] numbers, StringBuffer buffer, StringBuffer equivalenceBuffer) {
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			 outputInternals(tree, d, numbers, buffer, equivalenceBuffer);
		if (tree.nodeIsInternal(node)) {
			if (node != tree.getRoot()){
				buffer.append(Integer.toString(numbers[node]));
				buffer.append(',');
				buffer.append(Integer.toString(numbers[tree.motherOfNode(node)]));
				buffer.append(',');
				buffer.append(MesquiteDouble.toStringNoNegExponential(tree.getBranchLength(node, 1.0)));
				buffer.append("\r\n");
			}
			equivalenceBuffer.append("# node " + numbers[node] + " = Mesquite node " + node + "\r\n");
		}
		
	}
	/*-----------------------------------------*/
	/** Returns a polytomous node.*/
	protected void unassignedToOne(AdjustableTree tree, int node) {
		if (!MesquiteDouble.isCombinable(tree.getBranchLength(node)))
			tree.setBranchLength(node, 1.0, false);
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			 unassignedToOne(tree, d);
	}
	/*-----------------------------------------*/
	/** Returns a polytomous node.*/
	protected int findPolytomy(AdjustableTree tree, int node) {
		if (tree.nodeIsTerminal(node))
			return -1;
		if (tree.nodeIsPolytomous(node))
			return node;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
				if (tree.hasPolytomies(d))
					return findPolytomy(tree, d);
		return -1;
	}

	/*-----------------------------------------*/
	/** converts polytomies to zero length branches, and unassigned lengths to 1*/
	protected boolean sanitizeTree(Tree ptree){
		if (ptree.hasPolytomies(ptree.getRoot()) && !(ptree instanceof AdjustableTree))
			return false;
		AdjustableTree tree = (AdjustableTree)ptree;
		while (tree.hasPolytomies(tree.getRoot())){
			int p = findPolytomy(tree, tree.getRoot());
			int d = tree.firstDaughterOfNode(p);
			double origDL = tree.getBranchLength(d);
			int ns = tree.nextSisterOfNode(d);
			double origNSL = tree.getBranchLength(ns);
			tree.moveBranch(d, ns, false);
			tree.setBranchLength(d, origDL, false);
			tree.setBranchLength(ns, origNSL, false);
			tree.setBranchLength(tree.firstDaughterOfNode(p), 0, false);
		}
		tree.setBranchLength(tree.getRoot(), 0, false); //setting branch length to zero of root
		unassignedToOne(tree, tree.getRoot());
		return true;
	}
   	 
}

