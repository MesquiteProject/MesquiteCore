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

package mesquite.correl.CategContrasts;


import mesquite.categ.lib.*;
import mesquite.correl.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.lib.duties.*;

public class CategContrasts extends NumberFor2CharAndTree {

	private CLogger logger;
	private CategoricalDistribution observedStates1;
	private CategoricalDistribution observedStates2;
	ContrastsForCharAndTree contrastsTask;
	MesquiteString contrastsTaskName;


	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		contrastsTask = (ContrastsForCharAndTree)hireEmployee(commandRec, ContrastsForCharAndTree.class, "Contrasts calculator");
		if (contrastsTask == null)
			return sorry(commandRec, getName() + " couldn't start because no contrasts calculator obtained.");
		contrastsTaskName = new MesquiteString(contrastsTask.getName());
		MesquiteSubmenuSpec mss = addSubmenu(null, "Contrast calculator", makeCommand("setContrastCalculator", this), ContrastsForCharAndTree.class);
		mss.setCompatibilityCheck(CategoricalState.class);
		mss.setSelected(contrastsTaskName);
		if (getEmployer() instanceof CLogger)
			setLogger((CLogger)getEmployer());
		return true;
	}

	public void setLogger(CLogger logger){
		this.logger = logger;
	}

	/*.................................................................................................................*/
	public void employeeQuit(MesquiteModule m){
		if (m == contrastsTask)
			iQuit();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setContrastCalculator ",contrastsTask);
		return temp;
	}
	public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets module used to calculate contrasts", "[name of module]", commandName, "setContrastCalculator")) {
			ContrastsForCharAndTree temp=  (ContrastsForCharAndTree)replaceEmployee(commandRec, ContrastsForCharAndTree.class, arguments, "Contrast calculator", contrastsTask);
			if (temp!=null) {
				contrastsTask= temp;
				contrastsTaskName.setValue(contrastsTask.getName());
				parametersChanged(null, commandRec);

			}
			return contrastsTask;
		}
		else
			return super.doCommand(commandName, arguments, commandRec, checker);

	}


	public void initialize(Tree tree, CharacterDistribution charStates1, CharacterDistribution charStates2, CommandRecord commandRec) {
	}
	NumberArray contrasts1 = new NumberArray();
	NumberArray contrasts2 = new NumberArray();

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
		contrasts1.resetSize(tree.getNumNodeSpaces());
		contrasts2.resetSize(tree.getNumNodeSpaces());
		contrasts1.deassignArray();
		contrasts2.deassignArray();
		MesquiteString rs1 = new MesquiteString();
		MesquiteString rs2 = new MesquiteString();
		contrastsTask.calculateContrasts(tree, charStates1, contrasts1, rs1, commandRec);
		contrastsTask.calculateContrasts(tree, charStates2, contrasts2, rs2, commandRec);

		boolean assignedContrastsFound = false;
		double[][] contrasts = new double[2][tree.getNumNodeSpaces()];
		Double2DArray.deassignArray(contrasts);
		for (int i=0; i<tree.getNumNodeSpaces(); i++){
			contrasts[0][i] = contrasts1.getDouble(i);
			contrasts[1][i] = contrasts2.getDouble(i);
			if (MesquiteDouble.isCombinable(contrasts[0][i]) && MesquiteDouble.isCombinable(contrasts[1][i]))
				assignedContrastsFound = true;
		}

		for (int i=0; i<tree.getNumNodeSpaces(); i++){
			if (contrasts[0][i] == 0 || contrasts[1][i]  == 0){
				contrasts[0][i] = MesquiteDouble.unassigned; 
				contrasts[1][i] = MesquiteDouble.unassigned; 
			}
		}

		double r2 = calculateR2(contrasts[0], contrasts[1], true);
		if (!MesquiteDouble.isCombinable(r2)){
			if (assignedContrastsFound){
				result.setValue(0);
				if (resultString != null)
					resultString.setValue("No countable contrasts found");
				if (logger!= null){ 
					logger.cwrite("\n\n CategContrasts r squared: \n" + result);
				
				}

			}
			else {
				result.setToUnassigned();
				if (resultString != null)
					resultString.setValue("No valid contrasts found");
			}
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
		return "CategContrasts Correlation";
	}

	public String getExplanation(){
		return "A correlation test for two categorical characters using an ad hoc procedure analogous to Felsenstein's contrasts";
	}

	public boolean isPrerelease(){
		return true;
	}

}

