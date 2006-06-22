/* Mesquite source code.  Copyright 1997-2006 W. Maddison and D. Maddison. 
 This module copyright 2006 P. Midford and W. Maddison

Version 1.11, June 2006.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.correl.CategContrastsChar;


import mesquite.categ.lib.CategoricalDistribution;
import mesquite.categ.lib.CategoricalHistory;
import mesquite.correl.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.lib.characters.ModelCompatibilityInfo;
import mesquite.lib.duties.*;
import mesquite.stochchar.lib.MargLikeAncStCLForModel;
import mesquite.stochchar.lib.ProbModelSourceLike;
import mesquite.stochchar.lib.ProbPhenCategCharModel;
import mesquite.stochchar.lib.ProbabilityCategCharModel;

public class CategContrastsChar extends ContrastsForCharAndTree {

	MargLikeAncStCLForModel reconstructTask;
	ProbModelSourceLike modelTask;
	MesquiteString modelTaskName;
	ProbPhenCategCharModel model;  //CharacterModel model;  - temporary restriction to nonMolecular models PEM 6-Jan-2006

	private CategoricalDistribution observedStates1;

	
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		hireAllEmployees(commandRec, MargLikeAncStCLForModel.class);
		for (int i = 0; i<getNumberOfEmployees() && reconstructTask==null; i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof MargLikeAncStCLForModel){
				((MargLikeAncStCLForModel)e).setReportCladeLocalValues(true);
			}


		}
		modelTask = (ProbModelSourceLike)hireEmployee(commandRec, ProbModelSourceLike.class, "Source of probability character models (for likelihood calculations)");
		if (modelTask == null)
			return sorry(commandRec, getName() + " couldn't start because no source of models of character evolution obtained.");
		modelTaskName = new MesquiteString(modelTask.getName());
		MesquiteSubmenuSpec mss = addSubmenu(null, "Source of probability models", makeCommand("setModelSource", this), ProbModelSourceLike.class);
		mss.setCompatibilityCheck(new ModelCompatibilityInfo(ProbabilityCategCharModel.class, null));
		mss.setSelected(modelTaskName);
		return true;
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setModelSource ",modelTask);
		return temp;
	}
	public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets module used to supply character models", "[name of module]", commandName, "setModelSource")) {
			ProbModelSourceLike temp=  (ProbModelSourceLike)replaceEmployee(commandRec, ProbModelSourceLike.class, arguments, "Source of probability character models", modelTask);
			if (temp!=null) {
				modelTask= temp;
				incrementMenuResetSuppression();
				modelTaskName.setValue(modelTask.getName());
				parametersChanged(null, commandRec);
				decrementMenuResetSuppression();
			}
			return modelTask;
		}
		else
			return super.doCommand(commandName, arguments, commandRec, checker);
		
	}

	/*.................................................................................................................*/
	public void employeeQuit(MesquiteModule m){
		if (m == modelTask)
			iQuit();
	}


	CategoricalHistory cladeLikelihoods1 = null;
	ProbabilityCategCharModel tempModel;


	public  void calculateContrasts(Tree tree, CharacterDistribution charStates1, NumberArray result, MesquiteString resultString, CommandRecord commandRec){
		if (result == null)
			return;
		result.deassignArray();
		
		if (tree == null || charStates1 == null )
			return;
		if (!(charStates1 instanceof CategoricalDistribution)){
			if (resultString != null)
				resultString.setValue("CategContrasts can't be calculated because character is not categorical");
			return;
		}
		observedStates1 = (CategoricalDistribution)charStates1;
		if (observedStates1.getMaxState() > 1) {
			if (resultString != null)
				resultString.setValue("CategContrasts can't be calculated because character is not binary");
			return;
		}
		MesquiteString rs1 = new MesquiteString();
		cladeLikelihoods1 = calculateLikelihoods(tree, (CategoricalDistribution)charStates1, cladeLikelihoods1, rs1, commandRec);
		
		if (!cladeLikelihoods1.frequenciesExist()){
			if (resultString != null)
				resultString.setValue("CategContrasts can't be calculated because clade likelihoods not obtained");
			return;
		}
		double[] contrasts = new double[tree.getNumNodeSpaces()];
		DoubleArray.deassignArray(contrasts);
		getContrasts(tree, tree.getRoot(), cladeLikelihoods1, contrasts);
		for (int i = 0; i<contrasts.length; i++)
			result.setValue(i, contrasts[i]);
		if (resultString != null)
			resultString.setValue("CategContrasts");
		
	}
	
	private void getContrasts(Tree tree, int node, CategoricalHistory cladeLikelihoods, double[] contrasts){
		for(int d=tree.firstDaughterOfNode(node);tree.nodeExists(d);d=tree.nextSisterOfNode(d))
			getContrasts(tree,d,cladeLikelihoods, contrasts);
		if (tree.nodeIsInternal(node) && tree.numberOfDaughtersOfNode(node)==2){
			int left = tree.firstDaughterOfNode(node);
			int right = tree.lastDaughterOfNode(node);
			double L0 = cladeLikelihoods.getFrequency(left,0);
			double R0 = cladeLikelihoods.getFrequency(right,0);
			if (Math.abs(L0-R0)>0.5){
				contrasts[node] = L0-R0;
			}
			else
				contrasts[node] = 0;
		}
		
	}
	private CategoricalHistory calculateLikelihoods(Tree tree, CategoricalDistribution observedStates, CategoricalHistory cladeLikelihoods, MesquiteString resultString, CommandRecord commandRec){
		cladeLikelihoods = (CategoricalHistory)observedStates.adjustHistorySize(tree, cladeLikelihoods);
		cladeLikelihoods.deassignStates();
		ProbPhenCategCharModel origModel = null;
		//getting the model
		if (modelTask.getCharacterModel(observedStates, commandRec) instanceof ProbPhenCategCharModel )
			origModel = (ProbPhenCategCharModel)modelTask.getCharacterModel(observedStates, commandRec);
		if (origModel == null && !commandRec.scripting()){
			if (resultString != null)
				resultString.setValue("No model obtained");
			return cladeLikelihoods;
		}


		//getting the reconstructing module
		if (reconstructTask == null || !reconstructTask.compatibleWithContext(origModel, observedStates)) { 
			reconstructTask = null;
			for (int i = 0; i<getNumberOfEmployees() && reconstructTask==null; i++) {
				Object e=getEmployeeVector().elementAt(i);
				if (e instanceof MargLikeAncStCLForModel)
					if (((MargLikeAncStCLForModel)e).compatibleWithContext(origModel, observedStates)) {
						reconstructTask=(MargLikeAncStCLForModel)e;
					}
			}
		}
		MesquiteNumber likelihood = new MesquiteNumber();
		
		//doing the likelihood calculations
		if (reconstructTask != null) { 
			
			if (tempModel == null || tempModel.getClass() != origModel.getClass()){
				tempModel = (ProbabilityCategCharModel)origModel.cloneModelWithMotherLink(null);
			}
			origModel.copyToClone(tempModel);
			model = (ProbPhenCategCharModel)tempModel;
			cladeLikelihoods.deassignStates();
			reconstructTask.estimateParameters( tree,  observedStates,  model, null,   commandRec);
			reconstructTask.calculateStates( tree,  observedStates,  cladeLikelihoods, model, null, likelihood, commandRec);

		}
		else {
			if (resultString != null)
				resultString.setValue("No appropriate likelihood module obtained");
		}
		return cladeLikelihoods;
	}
	/*.................................................................................................................*/


	public String getVeryShortName() {
		return "CategContrasts calculator";
	}
	
	public String getAuthors() {
		return "Wayne P. Maddison & Peter E. Midford";
	}

	public String getVersion() {
		return "0.1";
	}

	public String getName() {
		return "CategContrasts calculator";
	}

	public String getExplanation(){
		return "Calculates contrasts for categorical characters using an ad hoc procedure analogous to Felsenstein's contrasts";
	}

	public boolean isPrerelease(){
		return true;
	}

}

