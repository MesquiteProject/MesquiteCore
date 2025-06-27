/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.consensus.MajRuleTree;


import java.awt.Checkbox;

import mesquite.consensus.lib.BasicTreeConsenser;
import mesquite.consensus.lib.BipartitionVector;
import mesquite.lib.Attachable;
import mesquite.lib.CommandChecker;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.TreesManager;
import mesquite.lib.tree.Tree;
import mesquite.lib.ui.DoubleField;
import mesquite.lib.ui.ExtensibleDialog;


/* ======================================================================== */
/** Does majority rule consensus .*/

public class MajRuleTree extends BasicTreeConsenser   {
	double frequencyLimit = 0.5;
	MesquiteBoolean useWeights = new MesquiteBoolean(true);
	Checkbox useWeightsBox;
	Checkbox dumpTableBox;
	MesquiteBoolean dumpTable = new MesquiteBoolean( false);
	DoubleField frequencyField;


	public String getName() {
		return "Majority-Rule Consensus";
	}
	public String getExplanation() {
		return "Calculates the majority-rule consensus tree." ;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return super.startJob(arguments, condition, hiredByName);
	}
	/*.................................................................................................................*/
	public void processMorePreferences (String tag, String content) {
		if ("useWeights".equalsIgnoreCase(tag))
			useWeights.setFromTrueFalseString(content);
		else if ("frequencyLimit".equalsIgnoreCase(tag))
			frequencyLimit = MesquiteDouble.fromString(content);
		else if ("dumpTable".equalsIgnoreCase(tag))
			dumpTable.setFromTrueFalseString(content);
	}
	/*.................................................................................................................*/
	public String prepareMorePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "useWeights", useWeights);  
		StringUtil.appendXMLTag(buffer, 2, "frequencyLimit", frequencyLimit);  
		StringUtil.appendXMLTag(buffer, 2, "dumpTable", dumpTable);  
		return buffer.toString();
	}
	
	
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = super.getSnapshot(file);

		temp.addLine("useWeights " + useWeights.toOffOnString());
		temp.addLine("dumpTable " + dumpTable.toOffOnString());
		temp.addLine("frequencyLimit " + MesquiteDouble.toString(frequencyLimit));
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets frequency limit.", "[value]", commandName, "frequencyLimit")) {
			double s = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (MesquiteDouble.isCombinable(s)){
				frequencyLimit = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 

			}
		}

		else if (checker.compare(this.getClass(), "Sets whether or not to use weights.", "[on or off]", commandName, "useWeights")) {
			boolean current = useWeights.getValue();
			useWeights.toggleValue(parser.getFirstToken(arguments));
			if (current!=useWeights.getValue()) {
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to dumpTable.", "[true or false]", commandName, "dumpTable")) {
			boolean current = dumpTable.getValue();
			dumpTable.toggleValue(parser.getFirstToken(arguments));
			if (current!=dumpTable.getValue()) {
				parametersChanged();
			}
		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public void queryOptionsSetup(ExtensibleDialog dialog) {
		String helpString = "\n";
		dialog.appendToHelpString(helpString);

		useWeightsBox = dialog.addCheckBox("consider tree weights", useWeights.getValue());
		frequencyField = dialog.addDoubleField("required frequency of clades: ", frequencyLimit, 5, 0.5, 1.00);
		dumpTableBox = dialog.addCheckBox("write group frequency list", dumpTable.getValue());
	}

	/*.................................................................................................................*/
	public void queryOptionsProcess(ExtensibleDialog dialog) {
		useWeights.setValue(useWeightsBox.getState());
		double freq = frequencyField.getValue();
		if (MesquiteDouble.isCombinable(freq))
			if (freq<0.5) {
				frequencyLimit=0.5;
				logln("Required frequency must be >0.5");
			}
			else
				frequencyLimit=freq;
		dumpTable.setValue( dumpTableBox.getState());
	}


	public void addTree(Tree t){
		if (t==null)
			return;
		if (useWeights.getValue()) {
			bipartitions.setUseWeights(useWeights.getValue());
			MesquiteDouble md = (MesquiteDouble)((Attachable)t).getAttachment(TreesManager.WEIGHT);
			if (md != null) {
				if (md.isCombinable())
					bipartitions.setWeight(md.getValue());
				else
					bipartitions.setWeight(1.0);
			} else
				bipartitions.setWeight(1.0);

		}
		bipartitions.addTree(t);
	}
	/*.................................................................................................................*/
	public void initialize() {
		bipartitions.initialize();
		if (bipartitions!=null) {
			bipartitions.setMode(BipartitionVector.MAJRULEMODE);
		}
	}
	/*.................................................................................................................*/
 	public void afterConsensus() {
		if (dumpTable.getValue())
			bipartitions.dump();
 	}
 

	public Tree getConsensus(){
		Tree t = bipartitions.makeTree(consensusFrequencyLimit());
		afterConsensus();
		return t;
	}
	/*.................................................................................................................*/
	public boolean useWeights() {
		return useWeights.getValue();
	}

	/*.................................................................................................................*/
	public double consensusFrequencyLimit() {
		return frequencyLimit;
	}
	/*.................................................................................................................*/
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}   	 
	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return 250;  
	}
	public double getFrequencyLimit() {
		return frequencyLimit;
	}
	public void setFrequencyLimit(double frequencyLimit) {
		this.frequencyLimit = frequencyLimit;
	}
	public boolean getUseWeights() {
		return useWeights.getValue();
	}
	public void setUseWeights(boolean b) {
		useWeights.setValue(b);
	}

}