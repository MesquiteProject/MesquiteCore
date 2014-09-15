/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.ancstates.ExportAncRec;

import java.util.*;

import mesquite.categ.lib.*;
import mesquite.ancstates.TraceCharacterHistory.TraceCharacterOperator;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class ExportAncRec extends TraceCharacterInit {
	public String getName() {
		return "Export Ancestral States Trace";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Exports the ancestral state trace from Trace Character History.";
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	Vector traces;

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		MesquiteSubmenuSpec treeStringSM = addSubmenu(null, "Export Ancestral States Trace"); 
		addItemToSubmenu(null, treeStringSM, "SIMMAP 1.5 Format...", makeCommand("showTreeStringSIMMAP15",  this));
		return true;
	}

	/*.................................................................................................................*/
	public void setCharacterHistoryContainers(Vector historyContainers){
		traces = historyContainers;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Shows the trace as a tree string", null, commandName, "showTreeStringSIMMAP15")) {
			//todo: if more than one traced, query user which one to store��������������������

			if (traces == null || traces.size()<1 || !(traces.elementAt(0) instanceof CharHistoryContainer))
				return null;
			TraceCharacterOperator trace = (TraceCharacterOperator)traces.elementAt(0);
			if (!(trace.history instanceof mesquite.categ.lib.CategoricalHistory)){
				discreetAlert("Sorry, SIMMAP format tree exporting works only for categorical data");
				return null;
			}
			MesquiteTree tree = (MesquiteTree)trace.getTree();
			String s = getTreeBlockSIMMAP(tree, trace);
			if (s == null)
				return null;
			MesquiteFile.putFileContentsQuery("Save Ancestral States On Tree (SIMMAP)", s, true);
			return s;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}

	/*-%%%%%%%%%%%%%%%%%%  SIMMAP 1.5 %%%%%%%%%%%%%%%%%%%%%%%%%%%%--*/
	/** Writes a tree description into the StringBuffer using translation table labels if available */
	private void writeTreeByLabelsSIMMAP(MesquiteTree tree, int node, StringBuffer buffer, mesquite.categ.lib.CategoricalHistory history, int writeMode) {
		if (tree.nodeIsInternal(node)) {
			buffer.append('(');
			int thisSister = tree.firstDaughterOfNode(node);
			writeTreeByLabelsSIMMAP(tree,thisSister, buffer, history, writeMode);
			while (tree.nodeExists(thisSister = tree.nextSisterOfNode(thisSister))) {
				buffer.append(',');
				writeTreeByLabelsSIMMAP(tree, thisSister, buffer, history, writeMode);
			}
			buffer.append(')');
			if (tree.nodeHasLabel(node))
				buffer.append(StringUtil.tokenize(tree.getNodeLabel(node)));
		}
		else {
			if (writeMode == Tree.BY_TABLE)//use treeVector's translation table if one is available
				buffer.append(tree.getTreeVector().getTranslationLabel(tree.taxonNumberOfNode(node))); 
			else if (writeMode == Tree.BY_NUMBERS)
				buffer.append(Integer.toString(Taxon.toExternal(tree.taxonNumberOfNode(node))));
			else
				buffer.append(StringUtil.tokenize(tree.getTaxa().getTaxonName(tree.taxonNumberOfNode(node))));
		}
		buffer.append(':');
		int num = history.getNumberOfEvents(node);
		if (num>0 && node != tree.getRoot()){
			String par = "";

			boolean found = false;
			double lastPos = 0;
			for (int i= num-1; i>=0; i--){
				CategInternodeEvent event = history.getInternodeEvent(node, i);

				if (event.getChangeVersusSample()){
					if (found)
						par += ", ";
					found = true;
					par += CategoricalState.toString(event.getState(), false);
					par += ", " + MesquiteDouble.toStringDigitsSpecified(tree.getBranchLength(node, 1)*((1.0-event.getPosition())-lastPos), 4);
					lastPos = 1.0-event.getPosition();
				}

			}
			long state = history.getState(tree.motherOfNode(node));
			if (CategoricalState.cardinality(state) == 1){
				if (found)
					par += ", ";
				par += CategoricalState.toString(state, false);  //ancestorward state
			}

			buffer.append("[&map={");
			buffer.append(par);
			buffer.append("}]");
		}
		else {
			long state = history.getState(node);
			if (CategoricalState.cardinality(state) == 1){
				buffer.append("[&map={");
				buffer.append(CategoricalState.toString(state, false));
				buffer.append("}]");
			}
		}
		buffer.append(MesquiteDouble.toStringDigitsSpecified(tree.getBranchLength(node, 1.0), -1)); 

	}
	/*-----------------------------------------*/
	public String getTreeBlockSIMMAP(MesquiteTree tree, TraceCharacterOperator trace){
		String endLine = ";" + StringUtil.lineEnding();
		StringBuffer block = new StringBuffer(5000);
		Taxa taxa = tree.getTaxa();
		block.append("#NEXUS");
		block.append( StringUtil.lineEnding());
		block.append("BEGIN TREES");
		block.append(endLine);
		block.append("\tTRANSLATE" + StringUtil.lineEnding());
		int writeMode = Tree.BY_NUMBERS;
		String tt = null;

		if (tree.getTreeVector() != null){
			TreeVector trees = tree.getTreeVector();
			tt =trees.getTranslationTable();
			if (tt != null)
				writeMode = Tree.BY_TABLE;
		}
		if (tt==null) {
			tt = "";
			if (taxa!=null)
				for(int i=0; i<taxa.getNumTaxa(); i++) {
					if (i>0)
						tt += ","+ StringUtil.lineEnding();
					tt += "\t\t" + Taxon.toExternal(i) + "\t" + StringUtil.tokenize(taxa.getTaxonName(i)) ;
				}
			writeMode = Tree.BY_NUMBERS;
		}
		block.append( tt);

		block.append(endLine);

		block.append("\tTREE ");
		block.append(StringUtil.tokenize(tree.getName() )+ " = " );
		writeTreeByLabelsSIMMAP( tree, tree.getRoot(), block, (mesquite.categ.lib.CategoricalHistory)trace.history, writeMode);
		block.append(endLine);
		block.append("END;" + StringUtil.lineEnding()+ StringUtil.lineEnding());
		return block.toString();
	}
	/*-%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%--*/
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 273;  
	}

}


