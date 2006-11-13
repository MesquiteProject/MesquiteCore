package mesquite.diverse.SpecExtincLikelihood;

import java.util.Vector;

import mesquite.diverse.SpecExtincMLCalculator.SpecExtincMLCalculator;
import mesquite.diverse.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.lib.duties.*;

public class SpecExtincLikelihood extends NumberForTree  {

	static final double root10=Math.sqrt(10.0);

	SpecExtincMLCalculator calcTask;

/*	MesquiteParameter sp = new MesquiteParameter(); //used only for parameter exploration
	MesquiteParameter ep = new MesquiteParameter();//used only for parameter exploration

	MesquiteParameter[] parameters;
	ParametersExplorer explorer;
*/

	MesquiteDouble lambda = new MesquiteDouble();   //speciation rate
	MesquiteDouble mu = new MesquiteDouble();   //extinction rate


	public boolean startJob(String arguments, Object condition,
			CommandRecord commandRec, boolean hiredByName) {
		calcTask = (SpecExtincMLCalculator)hireEmployee(commandRec, SpecExtincMLCalculator.class, "Integrating Likelihood");
		if (calcTask == null)
			return sorry(commandRec, getName() + " couldn't start because no integrating likelihood calculator module obtained.");

		addMenuItem("Set Speciation Rate...", makeCommand("setS", this));
		addMenuItem("Set Extinction Rate...", makeCommand("setE", this));
		addMenuItem("-", null);
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
	public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {
		// Should be removed when debugged
		//  double [] testvals = { 1E-11,1E-10,1E-9,1E-8,1E-7,5E-7,1E-6,2E-6,1E-5,1E-4,5E-4,1E-3,5E-3,1E-2,2E-2,5E-2,1E-1,2E-1,5E-01};
		if (checker.compare(getClass(), "Sets extinction rate", "[double]", commandName, "setE")) {
			double newE = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (!MesquiteDouble.isCombinable(newE) && !commandRec.scripting())
				newE = MesquiteDouble.queryDouble(containerOfModule(), "mu", "Instantaneous extinction rate", mu.getValue());
			if ((MesquiteDouble.isUnassigned(newE) ||  newE >=0) && newE != mu.getValue()){
				mu.setValue(newE); //change mode
				if (!commandRec.scripting())
				parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(getClass(), "Returns integrating module", null, commandName, "getIntegTask")) {
			return calcTask;
		}
		else if (checker.compare(getClass(), "Sets speciation rate", "[double]", commandName, "setS")) {
			double newS = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (!MesquiteDouble.isCombinable(newS) && !commandRec.scripting())
				newS = MesquiteDouble.queryDouble(containerOfModule(), "lambda", "Instantaneous speciation rate", lambda.getValue());
			if ((MesquiteDouble.isUnassigned(newS) || newS >=0) && newS != lambda.getValue()){
				lambda.setValue(newS); //change mode
				if (!commandRec.scripting())
					parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
			}
		}

		else
			return super.doCommand(commandName, arguments, commandRec, checker);
		return null;
	}
	MesquiteDouble tempLambda = new MesquiteDouble();
	MesquiteDouble tempMu = new MesquiteDouble();
	/*---------------------------------------------------------------------------------*/
	public void calculateNumber(Tree tree, 
			MesquiteNumber result, MesquiteString resultString,
			CommandRecord commandRec) {
		if (result == null)
			return;
		result.setToUnassigned();

		if (tree == null)
			return;
		tempLambda.setValue(lambda.getValue());
		tempMu.setValue(mu.getValue());
		calcTask.calculateLogProbability(tree, result, tempLambda, tempMu, resultString, commandRec);

	}
	
	/*---------------------------------------------------------------------------------*/
	public void initialize(Tree tree, CharacterDistribution charStates,
			CommandRecord commandRec) {
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
		return "0.1";
	}

	public String getExplanation(){
		return "Calculates likelihoods using a speciation/extinction model ";
	}

	public boolean isPrerelease(){
		return true;
	}




}
