/* Mesquite source code.  Copyright 1997-2008 W. Maddison and D. Maddison.
Version 2.5, June 2008.
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

import mesquite.lib.duties.*;
import mesquite.lib.*;
import mesquite.consensus.lib.*;


/* ======================================================================== */
/** Does majority rule consensus .*/

public class MajRuleTree extends BasicTreeConsenser   {
	double frequencyLimit = 0.5;
	MesquiteBoolean useWeights = new MesquiteBoolean(true);
	Checkbox useWeightsBox;
	Checkbox dumpTableBox;
	boolean dumpTable = false;
	DoubleField frequencyField;


	public String getName() {
		return "Majority Rules Consensus";
	}
	public String getExplanation() {
		return "Calculates the majority rules consensus tree." ;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		bipartitions = new BipartitionVector();
		loadPreferences();
		if (!MesquiteThread.isScripting()) 
			if (!queryOptions()) 
				return false;
		return true;
	}
	/*.................................................................................................................*/
	public void processMorePreferences (String tag, String content) {
		if ("useWeights".equalsIgnoreCase(tag))
			useWeights.setFromTrueFalseString(content);
		else if ("frequencyLimit".equalsIgnoreCase(tag))
			frequencyLimit = MesquiteDouble.fromString(content);
		else if ("dumpTable".equalsIgnoreCase(tag))
			dumpTable = MesquiteBoolean.fromTrueFalseString(content);
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
	public void queryOptionsSetup(ExtensibleDialog dialog) {
		String helpString = "\n";
		dialog.appendToHelpString(helpString);

		useWeightsBox = dialog.addCheckBox("consider tree weights", useWeights.getValue());
		frequencyField = dialog.addDoubleField("required frequency of clades: ", frequencyLimit, 5, 0.50, 1.00);
		dumpTableBox = dialog.addCheckBox("write group frequency list", dumpTable);
	}

	/*.................................................................................................................*/
	public void queryOptionsProcess(ExtensibleDialog dialog) {
		useWeights.setValue(useWeightsBox.getState());
		double freq = frequencyField.getValue();
		if (MesquiteDouble.isCombinable(freq))
			frequencyLimit=freq;
		dumpTable = dumpTableBox.getState();
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
		if (bipartitions!=null) {
			bipartitions.setMode(BipartitionVector.MAJRULEMODE);
		}
	}
	/*.................................................................................................................*/
 	public void afterConsensus() {
		if (dumpTable)
			bipartitions.dump();
 	}
 

	public Tree getConsensus(){
		Tree t = bipartitions.makeTree(consensusFrequencyLimit());
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
		return true;  
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