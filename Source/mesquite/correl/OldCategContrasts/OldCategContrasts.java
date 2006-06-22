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

package mesquite.correl.OldCategContrasts;


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

public class OldCategContrasts extends NumberFor2CharAndTree {
//ContrastsForCharAndTree,  calcCharContrast(tree, chardist, numberArray)
	//hire by number for nodes
	//hire by contrast correl (this)
	MargLikeAncStCLForModel reconstructTask;
	ProbModelSourceLike modelTask;
	MesquiteString modelTaskName;
	ProbPhenCategCharModel model;  //CharacterModel model;  - temporary restriction to nonMolecular models PEM 6-Jan-2006

	private CLogger logger;
	private CategoricalDistribution observedStates1;
	private CategoricalDistribution observedStates2;

	
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
		if (getEmployer() instanceof CLogger)
			setLogger((CLogger)getEmployer());
		return true;
	}

	public void setLogger(CLogger logger){
		this.logger = logger;
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

	public void initialize(Tree tree, CharacterDistribution charStates1, CharacterDistribution charStates2, CommandRecord commandRec) {
	}

	CategoricalHistory cladeLikelihoods1, cladeLikelihoods2 = null;
	ProbabilityCategCharModel tempModel;


	public  void calculateNumber(Tree tree, CharacterDistribution charStates1, CharacterDistribution charStates2, MesquiteNumber result, MesquiteString resultString, CommandRecord commandRec){
		if (result == null)
			return;
		result.setToUnassigned();
		if (tree == null || charStates1 == null || charStates2 == null)
			return;
		if (!(charStates1 instanceof CategoricalDistribution) || !(charStates2 instanceof CategoricalDistribution)){
			if (resultString != null)
				resultString.setValue("CategContrasts correlation can't be calculated because one or both of the character are not categorical");
			return;
		}
		observedStates1 = (CategoricalDistribution)charStates1;
		observedStates2 = (CategoricalDistribution)charStates2;
		if (observedStates1.getMaxState() > 1 ||
				observedStates2.getMaxState() > 1) {
			if (resultString != null)
				resultString.setValue("CategContrasts correlation can't be calculated because one or both of the characters are not binary");
			return;
		}
		MesquiteString rs1 = new MesquiteString();
		MesquiteString rs2 = new MesquiteString();
		cladeLikelihoods1 = calculateLikelihoods(tree, (CategoricalDistribution)charStates1, cladeLikelihoods1, rs1, commandRec);
		cladeLikelihoods2 = calculateLikelihoods(tree, (CategoricalDistribution)charStates2, cladeLikelihoods2, rs2, commandRec);
		
		if (!cladeLikelihoods1.frequenciesExist() || !cladeLikelihoods2.frequenciesExist()){
			if (resultString != null)
				resultString.setValue("CategContrasts correlation can't be calculated because clade likelihoods not obtained");
			return;
		}
		double[][] contrasts = new double[2][tree.getNumNodeSpaces()];
		Double2DArray.deassignArray(contrasts);
		getContrasts(tree, tree.getRoot(), cladeLikelihoods1, contrasts[0]);
		getContrasts(tree, tree.getRoot(), cladeLikelihoods2, contrasts[1]);
		for (int i=0; i<tree.getNumNodeSpaces(); i++){
			if (contrasts[0][i] == 0 && contrasts[1][i]  == 0){
					contrasts[0][i] = MesquiteDouble.unassigned; 
					contrasts[1][i] = MesquiteDouble.unassigned; 
			}
		}

		double r2 = calculateR2(contrasts[0], contrasts[1], true);
		if (!MesquiteDouble.isCombinable(r2)){
			if (resultString != null)
				resultString.setValue("No valid contrasts found");
			return;
		}
		result.setValue(r2);
		if (resultString != null)
			resultString.setValue(" r2 = " + r2);
		if (logger!= null){ 
			logger.cwrite("\n\n CategContrasts r squared: \n" + result);
			logger.cwrite("\n" + resultString);
		}
	}
	
	private double calculateR2(double[] c0, double[] c1, boolean positivize){
		int n = 0;
		double sum0 = 0;
		double sumSq0 = 0;
		double sum1 = 0;
		double sumSq1 = 0;
		double sumProducts = 0;
		//equation 53 in http://mathworld.wolfram.com/CorrelationCoefficient.html
		for (int i=0; i<c0.length && i<c1.length; i++){
			double c0i = c0[i];
			double c1i = c1[i];
			if (MesquiteDouble.isCombinable(c0i) && MesquiteDouble.isCombinable(c1i)){
				if (positivize && c0i <0){
					c0i = -c0i;//positivizing
						c1i = -c1i;
				}
				sum0 += c0i;
				sumSq0 += c0i*c0i;
				sum1 += c1i;
				Debugg.println("node " + i + ": " + c0i + " - " + c1i);
				sumSq1 += c1i*c1i;
				sumProducts += c0i*c1i;
				n++;
			}
		}
		if (n == 0)
			return MesquiteDouble.unassigned;
		double mean0 = sum0/n;
		double mean1 = sum1/n;
		double rd = (sumProducts  - n * mean0 * mean1);
		double r2 = rd*rd / (sumSq0 - (n * mean0 * mean0))/ (sumSq1 - (n * mean1 * mean1));
		return r2;
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
		return "CategContrasts Correlation test";
	}
	
	public String getAuthors() {
		return "Wayne P. Maddison & Peter E. Midford";
	}

	public String getVersion() {
		return "0.1";
	}

	public String getName() {
		return "OLD CategContrasts Correlation";
	}

	public String getExplanation(){
		return "A correlation test for two categorical characters using an ad hoc procedure analogous to Felsenstein's contrasts";
	}

	public boolean isPrerelease(){
		return true;
	}

}

