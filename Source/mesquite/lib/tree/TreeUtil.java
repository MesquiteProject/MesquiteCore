/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.tree;

import java.awt.Checkbox;
import java.awt.Label;

import mesquite.assoc.lib.*;
import mesquite.lib.Associable;
import mesquite.lib.Bits;
import mesquite.lib.Debugg;
import mesquite.lib.DoubleArray;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteString;
import mesquite.lib.NameReference;
import mesquite.lib.Parser;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.TreesManager;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.TaxonNamer;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.ProgressIndicator;

public class TreeUtil {
	
	public static String translationTableFileName = "taxonNamesTranslationTable.txt";

	
	/*.................................................................................................................*/
	/** Returns true iff the two trees have the same topologies.  The two trees must use the same block of taxa */
	public static boolean identicalTopologies (Tree tree1, Tree tree2, boolean checkBranchLengths) {
		if (tree1==null || tree2==null)
			return false;
		if (tree1.getTaxa().equals(tree2.getTaxa())) {
			return tree1.equalsTopology(tree2, checkBranchLengths);
		}
		return false;
	}
	/*.................................................................................................................*/
	/** Returns true iff the contained taxa in all containing taxa are monophyletic . */
	public static boolean containedAllMonophyletic (Tree containedTree, Tree containingTree, TaxaAssociation association) {
		if (containedTree==null || containingTree==null || association == null)
			return false;
		Taxa containingTaxa = containingTree.getTaxa();
		
		for (int outer=0;  outer<containingTaxa.getNumTaxa(); outer++) {  
			Bits associates = association.getAssociatesAsBits(containingTaxa, outer);
			if (!containedTree.isClade(associates))
				return false;
		}
		return true;
	}
	
	/*.................................................................................................................*/
	public static TreeVector readNewickTreeFile (MesquiteFile file, String line, Taxa taxa, boolean permitTaxaBlockEnlarge, TaxonNamer namer, String treeNameBase) {
		Parser treeParser = new Parser();
		treeParser.setQuoteCharacter((char)0);
		int numTrees = MesquiteInteger.infinite;
		if (line != null){
			treeParser.setString(line); 
			String token = treeParser.getNextToken();  // numTaxa
			numTrees = MesquiteInteger.fromString(token);
		}
		int iTree = 0;
		TreeVector trees = null;
		boolean abort = false;
		line = file.readNextDarkLine();		
		while (!StringUtil.blank(line) && !abort && (iTree<numTrees)) {
			treeParser.setString(line); //sets the string to be used by the parser to "line" and sets the pos to 0

			if (trees == null) {
				trees = new TreeVector(taxa);
				trees.setName("Imported trees");
			}
			MesquiteTree t = new MesquiteTree(taxa);
			t.setPermitTaxaBlockEnlargement(permitTaxaBlockEnlarge);
			//t.setTreeVector(treeVector);
			t.readTree(line,namer, null, "():;,[]\'", true);  //tree reading adjusted to use Newick punctuation rather than NEXUS
			/*MesquiteInteger pos = new MesquiteInteger(0);
			treeParser.setString(line);
			readClade(t, t.getRoot());
			t.setAsDefined(true);*/
			t.setName(treeNameBase + (iTree+1));
			trees.addElement(t, false);


			iTree++;
			line = file.readNextDarkLine();		
			if (file.getFileAborted())
				abort = true;
		}
		return trees;
		
	}
	/*.................................................................................................................*
	public static Tree readPhylipTree (String line, Taxa taxa, boolean permitTaxaBlockEnlarge, boolean permitSpaceUnderscoreEquivalent, TaxonNamer namer) {
		if (StringUtil.blank(line))
			return null;
		if (!validNewickEnds(line))
			return null;
		MesquiteTree t = new MesquiteTree(taxa);
		t.setPermitTaxaBlockEnlargement(permitTaxaBlockEnlarge);
		t.setPermitSpaceUnderscoreEquivalent(permitSpaceUnderscoreEquivalent);
		t.readTree(line, namer, null, "():;,[]\'"); //tree reading adjusted to use Newick punctuation rather than NEXUS
		return t;
	}

	/*.................................................................................................................*/
	public static void reinterpretNodeLabels(Tree tree, int node, NameReference[] nameRefs, boolean asText, double divisor){
		if (nameRefs==null || nameRefs.length == 0)
			return;
		MesquiteTree t = null;
		if (tree instanceof MesquiteTree)
			t=(MesquiteTree)tree;
		if (t==null)
			return;
		if (divisor==0.0) divisor=1.0;
		if (t.nodeIsInternal(node)){
			if (t.nodeHasLabel(node)){
				String label = t.getNodeLabel(node);
				if (asText) {
					t.setAssociatedObject(nameRefs[0], node, label, true);
				} else {
					Parser parser = new Parser(label);
					parser.setWhitespaceString(parser.getWhitespaceString()+"/");
					double[] support = new double[nameRefs.length];
					for (int i=0; i<nameRefs.length; i++) {
						support[i] = MesquiteDouble.fromString(parser.getNextToken());
						if (MesquiteDouble.isCombinable(support[i]))
							t.setAssociatedDouble(nameRefs[i], node, support[i]/divisor, true);
					}
				}
				t.setNodeLabel(null, node);
			}
			for (int daughter = t.firstDaughterOfNode(node); t.nodeExists(daughter); daughter = t.nextSisterOfNode(daughter)) {
				reinterpretNodeLabels(t, daughter, nameRefs, asText, divisor);
			}
		}

	}
	/*.................................................................................................................*/
	//THIS IS NOT CALLED
	public  static boolean showAssociatedChoiceDialog(Associable tree, ListableVector names, String message, MesquiteModule module) {
		if (tree == null)
			return false;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ListableVector v = new ListableVector();
		int num = tree.getNumberAssociatedDoubles();
		boolean[] shown = new boolean[num + names.size()]; //bigger than needed probably
		for (int i = 0; i< num; i++){
			DoubleArray da = tree.getAssociatedDoubles(i);
			if (da != null){
				v.addElement(new MesquiteString(da.getName(), ""), false);
			if (names.indexOfByName(da.getName())>=0)
				shown[i] = true;
			}
		}
		for (int i = 0; i<names.size(); i++){
			String name = ((MesquiteString)names.elementAt(i)).getName();
			if (v.indexOfByName(name)<0){
				v.addElement(new MesquiteString(name, " (not in current tree)"), false);
				if (v.size()-1>= shown.length)
					shown[v.size()-1] = true;
			}
		}
		if (v.size()==0)
			module.alert("This Tree has no values associated with nodes");
		else {
			ExtensibleDialog queryDialog = new ExtensibleDialog(module.containerOfModule(), message,  buttonPressed);
			queryDialog.addLabel(message, Label.CENTER);
			Checkbox[] checks = new Checkbox[v.size()];
			for (int i=0; i<v.size(); i++){
				MesquiteString ms = (MesquiteString)v.elementAt(i);
				checks[i] = queryDialog.addCheckBox (ms.getName() + ms.getValue(), shown[i]);
			}

			queryDialog.completeAndShowDialog(true);

			boolean ok = (queryDialog.query()==0);

			if (ok) {
				names.removeAllElements(false);
				for (int i=0; i<checks.length; i++){
					MesquiteString ms = (MesquiteString)v.elementAt(i);
					if (checks[i].getState())
						names.addElement(new MesquiteString(ms.getName(), ms.getName()), false);
				}
			}

			queryDialog.dispose();
			return ok;

		}
		return false;
	}

	
}
