/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.diverse.SpecExtincLikelihood;


import mesquite.diverse.SpecExtincMLCalculator.SpecExtincMLCalculator;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.diverse.lib.*;

public class SpecExtincLikelihood extends NumberForTreeDivers  {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(SpecExtincMLCalculator.class, getName() + "  needs a method to calculate likelihoods.",
		"The method to calculate likelihoods is arranged initially");
	}
	/*.................................................................................................................*/

	static final double root10=Math.sqrt(10.0);

	SpecExtincMLCalculator calcTask;

/*	MesquiteParameter sp = new MesquiteParameter(); //used only for parameter exploration
	MesquiteParameter ep = new MesquiteParameter();//used only for parameter exploration

	MesquiteParameter[] parameters;
	ParametersExplorer explorer;
*/

	MesquiteDouble lambda = new MesquiteDouble();   //speciation rate
	MesquiteDouble mu = new MesquiteDouble();   //extinction rate


	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}

	public boolean startJob(String arguments, Object condition,
			boolean hiredByName) {
		calcTask = (SpecExtincMLCalculator)hireEmployee(SpecExtincMLCalculator.class, "Integrating Likelihood");
		if (calcTask == null)
			return sorry(getName() + " couldn't start because no integrating likelihood calculator module obtained.");

		addMenuItem("Set Speciation Rate...", makeCommand("setS", this));
		addMenuItem("Set Extinction Rate...", makeCommand("setE", this));
		addMenuSeparator();
		addMenuItem("Write table to console", makeCommand("writeTable",this));
		addMenuItem("Write code for R to console", makeCommand("writeForExternalApp",this));


		/*
		// Test model will dump values to console for fixed branch lenght and different e values
		testModel = new SpecExtincCladeModel(1E6,0.001);
		//testModel.setS(0.000001);
		Vector integrationResults = null;
		double x = 0;
		double length = 1.0;
		int STEP_COUNT = 1000;
		double h = length/STEP_COUNT;       //this will need tweaking!
		double[] yStart = new double[2];
		yStart[0] = 0;
		yStart[1] = 1;
		//double [] eVals = {0, 1E-8,(root10*1E-8),1E-7,(root10*1E-7),1E-6,(root10*1E-6),1E-5,(root10*1E-5),1E-4,(root10*1E-4),1E-3,(root10*1E-3),1E-2,(root10*1E-2),1E-1,(root10*1E-1),1,root10,10};
		//double [] sVals = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30};
		double [] eVals = {0.3,0.6,0.9,1.2,1.5,1.8,2.1,2.4,2.7,3.0};
		double [] sVals = {0.3,0.6,0.9,1.2,1.5,1.8,2.1,2.4,2.7,3.0};
		//System.out.println("S is " + testModel.getSRate(0));
		System.out.println("Test model: columns are s, e, E, D, D/1-E");
		for (int i=0;i<sVals.length;i++){
			testModel.setS(sVals[i]);
			for (int j=0;j<eVals.length;j++){
				testModel.setE(eVals[j]);
				integrationResults = solver.integrate(x,yStart,h,length,testModel,integrationResults,false); 

				double[] yEnd = (double[])integrationResults.lastElement();

				System.out.println(sVals[i]+"\t" + eVals[j]+"\t" + yEnd[0] + "\t"+ yEnd[1] + "\t"+ (yEnd[1]/(1-yEnd[0])));
			}
		}
		*/
		return true;
	}


	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("getIntegTask ", calcTask);
		temp.addLine("setS " + MesquiteDouble.toString(lambda.getValue()));
		temp.addLine("setE " + MesquiteDouble.toString(mu.getValue()));
		return temp;
	}

	/*.................................................................................................................*/
	/*  the main command handling method.  */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(getClass(), "Sets extinction rate", "[double]", commandName, "setE")) {
			double newE = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (!MesquiteDouble.isCombinable(newE) && !MesquiteThread.isScripting())
				newE = MesquiteDouble.queryDouble(containerOfModule(), "mu", "Instantaneous extinction rate", mu.getValue());
			if ((MesquiteDouble.isUnassigned(newE) ||  newE >=0) && newE != mu.getValue()){
				mu.setValue(newE); //change mode
				if (!MesquiteThread.isScripting())
				parametersChanged(); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(getClass(), "Returns integrating module", null, commandName, "getIntegTask")) {
			return calcTask;
		}
		else if (checker.compare(getClass(), "Sets speciation rate", "[double]", commandName, "setS")) {
			double newS = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (!MesquiteDouble.isCombinable(newS) && !MesquiteThread.isScripting())
				newS = MesquiteDouble.queryDouble(containerOfModule(), "lambda", "Instantaneous speciation rate", lambda.getValue());
			if ((MesquiteDouble.isUnassigned(newS) || newS >=0) && newS != lambda.getValue()){
				lambda.setValue(newS); //change mode
				if (!MesquiteThread.isScripting())
					parametersChanged(); //this tells employer module that things changed, and recalculation should be requested
			}
		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	MesquiteDouble tempLambda = new MesquiteDouble();
	MesquiteDouble tempMu = new MesquiteDouble();
	/*---------------------------------------------------------------------------------*/
	public void calculateNumber(Tree tree,MesquiteNumber result, MesquiteString resultString) {
		if (result == null)
			return;
	   	clearResultAndLastResult(result);

		if (tree == null)
			return;
		tempLambda.setValue(lambda.getValue());
		tempMu.setValue(mu.getValue());
		calcTask.calculateLogProbability(tree, result, tempLambda, tempMu, resultString);
		saveLastResult(result);
		saveLastResultString(resultString);

	}
	
	/*---------------------------------------------------------------------------------*/
	public void initialize(Tree tree, CharacterDistribution charStates) {
		// TODO Auto-generated method stub
	}
	/*---------------------------------------------------------------------------------*/

	public String getName() {
		return "Speciation/Extinction Likelihood";
	}

	public String getAuthors() {
		return "Peter E. Midford, Wayne P. Maddison & Sarah P. Otto";
	}

	public String getVersion() {
		return "1.0";
	}

	public String getExplanation(){
		return "Calculates likelihoods using a speciation/extinction model reduced from the BiSSE model (Maddison, Midford & Otto 2007) ";
	}

	public boolean isPrerelease(){
		return false;
	}




}
