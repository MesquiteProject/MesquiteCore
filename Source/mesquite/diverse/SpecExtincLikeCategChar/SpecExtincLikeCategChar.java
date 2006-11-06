package mesquite.diverse.SpecExtincLikeCategChar;


import java.util.Vector;

import mesquite.diverse.lib.*;
import mesquite.diverse.IntegLikeCateg.IntegLikeCateg;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.lib.duties.NumberForCharAndTree;
import mesquite.lib.duties.ParametersExplorer;

public class SpecExtincLikeCategChar extends NumberForCharAndTree implements ParametersExplorable {

	DEQNumSolver solver;
	CladeExtinctionModel speciesModel;
    CladeExtinctionModel testModel;
	IntegLikeCateg calcTask;

	// hooks for capturing context for table dump.
	Tree lastTree;
	CharacterDistribution lastCharDistribution;

	double e0 = 0.001;   //user specified extinction rate in state 0
	double s0 = 0.001;   //user specified speciation rate in state 0
	double e1 = 0.005;   //user specified extinction rate in state 1
	double s1 = 0.001;   //user specified speciation rate in state 1
	double t01 = 0.01;   //user specified transition rate from state 0 to state 1
	double t10 = 0.01;   //user specifiedtransition rate from state 1 to state 0

	MesquiteParameter s0p = new MesquiteParameter();
	MesquiteParameter s1p = new MesquiteParameter();
	MesquiteParameter e0p = new MesquiteParameter();
	MesquiteParameter e1p = new MesquiteParameter();
	MesquiteParameter t01p = new MesquiteParameter();
	MesquiteParameter t10p = new MesquiteParameter();
	MesquiteParameter[] parameters;
	ParametersExplorer explorer;
	   MesquiteBoolean conditionBySurvival;

	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		calcTask = (IntegLikeCateg)hireEmployee(commandRec, IntegLikeCateg.class, "Integrating Likelihood");
		if (calcTask == null)
			return sorry(commandRec, getName() + " couldn't start because no integrating likelihood calculator module obtained.");

		solver = new RK4Solver();
		addMenuItem("Set State 0 Extinction Rate...", makeCommand("setE0", this));
		addMenuItem("Set State 0 Speciation Rate...", makeCommand("setS0", this));
		addMenuItem("Set State 1 Extinction Rate...", makeCommand("setE1", this));
		addMenuItem("Set State 1 Speciation Rate...", makeCommand("setS1", this));
		addMenuItem("Set 0 to 1 Transition Rate...", makeCommand("setT01", this));
		addMenuItem("Set 1 to 0 Transition Rate...", makeCommand("setT10", this));
	       conditionBySurvival = new MesquiteBoolean(false);
			addCheckMenuItem(null, "Condition by Survival", MesquiteModule.makeCommand("conditionBySurvivial", this), conditionBySurvival);
		addMenuItem("-", null);
		addMenuItem("Show Parameters Explorer", makeCommand("showParamExplorer",this));
		addMenuItem("Write table to console", makeCommand("writeTable",this));
		addMenuItem("Write code for R to console", makeCommand("writeForExternalApp",this));
		speciesModel = new CladeExtinctionModel(0.001, 0.001, 0.005, 0.001, 0.01, 0.01);

		//following is for the parameters explorer
		s0p.setName("s0");
		s0p.setExplanation("Rate of speciation with state 0");
		s0p.setMinimumAllowed(0);
		s0p.setMaximumAllowed(MesquiteDouble.infinite);
		s0p.setMinimumSuggested(0.0001);
		s0p.setMaximumSuggested(0.1);
		s0p.setValue(s0);
		s1p.setName("s1");
		s1p.setExplanation("Rate of speciation with state 1");
		s1p.setMinimumSuggested(0.0001);
		s1p.setMaximumSuggested(0.1);
		s1p.setMinimumAllowed(0);
		s1p.setMaximumAllowed(MesquiteDouble.infinite);
		s1p.setValue(s1);
		e0p.setName("e0");
		e0p.setExplanation("Rate of extinction with state 0");
		e0p.setMinimumSuggested(0.0001);
		e0p.setMaximumSuggested(0.1);
		e0p.setMinimumAllowed(0);
		e0p.setMaximumAllowed(MesquiteDouble.infinite);
		e0p.setValue(e0);
		e1p.setName("e1");
		e1p.setExplanation("Rate of extinction with state 1");
		e1p.setMinimumSuggested(0.0001);
		e1p.setMaximumSuggested(0.1);
		e1p.setMinimumAllowed(0);
		e1p.setMaximumAllowed(MesquiteDouble.infinite);
		e1p.setValue(e1);
		t01p.setName("t01");
		t01p.setExplanation("Rate of 0->1 changes");
		t01p.setMinimumSuggested(0.0001);
		t01p.setMaximumSuggested(0.1);
		t01p.setMinimumAllowed(0);
		t01p.setMaximumAllowed(MesquiteDouble.infinite);
		t01p.setValue(t01);
		t10p.setName("t10");
		t10p.setExplanation("Rate of 1->0 changes");
		t10p.setMinimumSuggested(0.0001);
		t10p.setMaximumSuggested(0.1);
		t10p.setMinimumAllowed(0);
		t10p.setMaximumAllowed(MesquiteDouble.infinite);
		t10p.setValue(t10);
		parameters = new MesquiteParameter[]{s0p, s1p, e0p, e1p, t01p, t10p};
        
        // Test model will dump values to console for fixed branch lenght and different e values
        testModel = new CladeExtinctionModel(1E-6, 0.001, 1E-6, 0.01, 0.05, 0.05);
        Vector integrationResults = null;
        double x = 0;
        double length = 1.0;
        int STEP_COUNT = 1000;
        double h = length/STEP_COUNT;       //this will need tweaking!
        double[] yStart = new double[4];
        yStart[0] = 1;
        yStart[1] = 1;
        yStart[2] = 0;
        yStart[1] = 1;
        //double [] eVals = {0, 1E-8,(root10*1E-8),1E-7,(root10*1E-7),1E-6,(root10*1E-6),1E-5,(root10*1E-5),1E-4,(root10*1E-4),1E-3,(root10*1E-3),1E-2,(root10*1E-2),1E-1,(root10*1E-1),1,root10,10};
        //double [] sVals = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30};
        double [] eVals = {0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};
        double [] sVals = {0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};
        System.out.println("Test model: columns are s, e, E0, E1, D0, D1");
        for (int i=0;i<sVals.length;i++){
            testModel.setS0(sVals[i]);
            testModel.setS1(sVals[i]);
            for (int j=0;j<eVals.length;j++){
                testModel.setE0(eVals[j]);
                testModel.setE1(eVals[j]);
                integrationResults = solver.integrate(x,yStart,h,length,testModel,integrationResults,false); 
            
                double[] yEnd = (double[])integrationResults.lastElement();

                System.out.println(sVals[i]+"\t" + eVals[j]+"\t" + yEnd[0] + "\t"+ yEnd[1] + "\t"+ yEnd[2] + "\t" + yEnd[3]);
            }
        }

        
        
		return true;
	}

	public void initialize(Tree tree, CharacterDistribution charStates1, CommandRecord commandRec) {
		// TODO Auto-generated method stub

	}
	public void employeeQuit(MesquiteModule employee){
		if (employee == explorer)
			explorer = null;
	}
	/*.................................................................................................................*/

	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();

			temp.addLine("setS0 " + MesquiteDouble.toString(s0));
			temp.addLine("setS1 " + MesquiteDouble.toString(s1));
			temp.addLine("setE0 " + MesquiteDouble.toString(e0));
			temp.addLine("setE1 " + MesquiteDouble.toString(e1));
			temp.addLine("setT01 " + MesquiteDouble.toString(t01));
			temp.addLine("setT10 " + MesquiteDouble.toString(t10));
			temp.addLine("conditionBySurvival  " + conditionBySurvival.toOffOnString());
			if (explorer != null)
				temp.addLine("showParamExplorer ", explorer);
		
		return temp;
	}
	/*.................................................................................................................*/
	/*  the main command handling method.  */
	public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {
		// Should be removed when debugged
		double [] testvals = { 1E-11,1E-10,1E-9,1E-8,1E-7,5E-7,1E-6,2E-6,1E-5,1E-4,5E-4,1E-3,5E-3,1E-2,2E-2,5E-2,1E-1,2E-1,5E-01};
		if (checker.compare(getClass(), "Sets extinction rate in state 0", "[double]", commandName, "setE0")) {
			double newE0 = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (!MesquiteDouble.isCombinable(newE0) && !commandRec.scripting())
				newE0 = MesquiteDouble.queryDouble(containerOfModule(), "e0", "Instantaneous extinction rate in state 0", (double)e0);
			if (MesquiteDouble.isCombinable(newE0) && newE0 >=0 && newE0 != e0){
				e0 = newE0; //change mode
				if (speciesModel != null)
					speciesModel.setE0(e0);
				else
					speciesModel = new CladeExtinctionModel(e0,s0,e1,s1,t01,t10);
				parametersChangedNotifyExpl(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(getClass(), "Sets speciation rate in state 0", "[double]", commandName, "setS0")) {
			double newS0 = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (!MesquiteDouble.isCombinable(newS0) && !commandRec.scripting())
				newS0 = MesquiteDouble.queryDouble(containerOfModule(), "s0", "Instantaneous speciation rate in state 0", (double)s0);
			if (MesquiteDouble.isCombinable(newS0) && newS0 >=0 && newS0 != s0){
				s0 = newS0; //change mode
				if (speciesModel != null)
					speciesModel.setS0(s0);
				else
					speciesModel = new CladeExtinctionModel(e0,s0,e1,s1,t01,t10);
				parametersChangedNotifyExpl(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(getClass(), "Sets extinction rate in state 1", "[double]", commandName, "setE1")) {
			double newE1 = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (!MesquiteDouble.isCombinable(newE1) && !commandRec.scripting())
				newE1 = MesquiteDouble.queryDouble(containerOfModule(), "e1", "Instantaneous extinction rate in state 1", (double)e1);
			if (MesquiteDouble.isCombinable(newE1) && newE1 >=0 && newE1 != e1){
				e1 = newE1; //change mode
				if (speciesModel != null)
					speciesModel.setE1(e1);
				else
					speciesModel = new CladeExtinctionModel(e0,s0,e1,s1,t01,t10);
				parametersChangedNotifyExpl(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(getClass(), "Sets speciation rate in state 1", "[double]", commandName, "setS1")) {
			double newS1 = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (!MesquiteDouble.isCombinable(newS1) && !commandRec.scripting())
				newS1 = MesquiteDouble.queryDouble(containerOfModule(), "s1", "Instantaneous speciation rate in state 1", (double)s1);
			if (MesquiteDouble.isCombinable(newS1) && newS1 >=0 && newS1 != s1){
				s1 = newS1; //change mode
				if (speciesModel != null)
					speciesModel.setS1(s1);
				else
					speciesModel = new CladeExtinctionModel(e0,s0,e1,s1,t01,t10);
				parametersChangedNotifyExpl(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(getClass(), "Sets transition rate from state 0 to state 1", "[double]", commandName, "setT01")) {
			double newT01 = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (!MesquiteDouble.isCombinable(newT01) && !commandRec.scripting())
				newT01 = MesquiteDouble.queryDouble(containerOfModule(), "t01", "Instantaneous transition rate from 0 to 1", (double)t01);
			if (MesquiteDouble.isCombinable(newT01) && newT01 >=0 && newT01 != t01){
				t01 = newT01; //change mode
				if (speciesModel != null)
					speciesModel.setT01(t01);
				else
					speciesModel = new CladeExtinctionModel(e0,s0,e1,s1,t01,t10);
				parametersChangedNotifyExpl(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(getClass(), "Sets transition rate from state 1 to state 0", "[double]", commandName, "setT10")) {
			double newT10 = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (!MesquiteDouble.isCombinable(newT10) && !commandRec.scripting())
				newT10 = MesquiteDouble.queryDouble(containerOfModule(), "t10", "Instantaneous transition rate from 1 to 0", (double)t10);
			if (MesquiteDouble.isCombinable(newT10) && newT10 >=0 && newT10 != t10){
				t10 = newT10; //change mode
				if (speciesModel != null)
					speciesModel.setT10(t10);
				else
					speciesModel = new CladeExtinctionModel(e0,s0,e1,s1,t01,t10);
				parametersChangedNotifyExpl(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether to condition by survival", "[on; off]", commandName, "conditionBySurvival")) {
			conditionBySurvival.toggleValue(new Parser().getFirstToken(arguments));
			parametersChangedNotifyExpl(null, commandRec);
		}
 		else if (checker.compare(getClass(), "Writes table to console", "", commandName, "showParamExplorer")) {
			explorer = (ParametersExplorer)hireEmployee(commandRec, ParametersExplorer.class, "Parameters explorer");
			if (explorer == null)
				return null;
			explorer.setExplorable(this, commandRec);
			return explorer;
		}
		else if (checker.compare(getClass(), "Writes table to console", "", commandName, "writeTable")) {
			MesquiteMessage.println("e0 = " + e0);
			MesquiteMessage.println("e1 = " + e1);
			MesquiteMessage.println("t01 = " + t01);
			MesquiteMessage.println("t10 = " + t10);
			MesquiteMessage.println("s1/s0");
			MesquiteNumber savedResult = new MesquiteNumber();
			MesquiteMessage.print("           ");
			for(int j=0;j<testvals.length;j++)
				MesquiteMessage.print(MesquiteDouble.toFixedWidthString(testvals[j],10)+ " ");
			MesquiteMessage.println("");
			for(int i=0;i<testvals.length;i++){
				speciesModel.setS1(testvals[i]);
				MesquiteMessage.print(MesquiteDouble.toFixedWidthString(testvals[i],10) + " ");
				for(int j=0;j<testvals.length;j++){
					speciesModel.setS0(testvals[j]);
					calculateNumber(lastTree,lastCharDistribution,savedResult,null,commandRec);
					MesquiteMessage.print(MesquiteDouble.toFixedWidthString(savedResult.getDoubleValue(),10) + " ");
				}
				MesquiteMessage.println("");
			}
		}
		else if (checker.compare(getClass(), "Writes text for external app to console", "", commandName, "writeForExternalApp")) {
			MesquiteMessage.println("e0 = " + e0);
			MesquiteMessage.println("e1 = " + e1);
			MesquiteMessage.println("t01 = " + t01);
			MesquiteMessage.println("t10 = " + t10);
			MesquiteMessage.println("s1/s0");
			MesquiteNumber savedResult = new MesquiteNumber();
			MesquiteMessage.println("Cut here......");
			MesquiteMessage.print("x <- c(");
			for(int j=0;j<testvals.length;j++){
				MesquiteMessage.print("log10(" + MesquiteDouble.toFixedWidthString(testvals[j],10)+ ")");
				if (j<(testvals.length-1))
					MesquiteMessage.print(", ");
			}
			MesquiteMessage.println(");");
			MesquiteMessage.println("y<-x;");
			MesquiteMessage.println("z <- matrix(nrow=length(y),ncol=length(x));");
			for(int i=0;i<testvals.length;i++){
				speciesModel.setS1(testvals[i]);
				MesquiteMessage.print("z[" + (i+1) + ",] <- c(");
				for(int j=0;j<testvals.length;j++){
					speciesModel.setS0(testvals[j]);
					calculateNumber(lastTree,lastCharDistribution,savedResult,null,commandRec);
					MesquiteMessage.print(MesquiteDouble.toFixedWidthString(-1*savedResult.getDoubleValue(),10));
					if (j<(testvals.length-1))
						MesquiteMessage.print(", ");
				}
				MesquiteMessage.println(");");
			}
			MesquiteMessage.println("persp(x,y,z,xlab='log10(s0)',ylab='log10(s1)',zlab='logLike',ticktype='detailed',theta=125,phi=30,col='lightblue');");
		}

		else
			return super.doCommand(commandName, arguments, commandRec, checker);
		return null;
	}
	   public void parametersChangedNotifyExpl(Notification n,  CommandRecord commandRec){
	    	if (!commandRec.scripting())
	    		parametersChanged(n, commandRec);
	    	if (explorer != null)
	    		explorer.explorableChanged(this, commandRec);
	    }

	/*------------------------------------------------------------------------------------------*/
	/** these methods for ParametersExplorable interface */
	public MesquiteParameter[] getExplorableParameters(){
		return parameters;
	}
	MesquiteNumber likelihood = new MesquiteNumber();
	public double calculate(MesquiteString resultString, CommandRecord commandRec){
		speciesModel.setS1(s1p.getValue());
		speciesModel.setS0(s0p.getValue());
		speciesModel.setE1(e1p.getValue());
		speciesModel.setE0(e0p.getValue());
		speciesModel.setT01(t01p.getValue());
		speciesModel.setT10(t10p.getValue());
		calculateNumber( lastTree,  lastCharDistribution, likelihood, resultString, commandRec);
		return likelihood.getDoubleValue();
	}
	/*------------------------------------------------------------------------------------------*/

	public void calculateNumber(Tree tree, CharacterDistribution charStates, MesquiteNumber result, MesquiteString resultString, CommandRecord commandRec) {
		if (result == null)
			return;
		result.setToUnassigned();

		if (tree == null || charStates == null)
			return;

		/* Note: currently does not refresh parameters explorer if tree changed.  This would be tough to do automatically, 
		 * because in response to a change in tree here, calculate number could then be called by the explorer!   Perhaps always require user request? 
	       if (lastTree != tree)
	        	explorer.explorableChanged(this, commandRec);*/
		lastTree = tree;
		lastCharDistribution = charStates;
		if (speciesModel == null)
			speciesModel = new CladeExtinctionModel(e0,s0,e1,s1,t01,t10);
		calcTask.calculateLogProbability(tree, speciesModel, conditionBySurvival.getValue(),solver, charStates, resultString, result, commandRec);

	}
	public void restoreAfterExploration(){

		if (speciesModel == null)
			speciesModel = new CladeExtinctionModel(e0,s0,e1,s1,t01,t10);
		else{
			speciesModel.setE0(e0);
	        	speciesModel.setE1(e1);
	        	speciesModel.setS0(s0);
	        	speciesModel.setS1(s1);
	        	speciesModel.setT01(t01);
	        	speciesModel.setT10(t10);
	       }
	}

 	
	/*------------------------------------------------------------------------------------------*/


	public String getName() {
		// TODO Auto-generated method stub
		return "Speciation/Extinction Likelihood (Categ. Char.)";
	}

	public String getAuthors() {
		return "Peter E. Midford, Sarah P. Otto & Wayne P. Maddison";
	}

	public String getVersion() {
		return "0.1";
	}

	public String getExplanation(){
		return "Calculates likelihoods using a speciation/extinction model whose probabilities depend on the state of a single categorical character";
	}

	public boolean isPrerelease(){
		return true;
	}


}
