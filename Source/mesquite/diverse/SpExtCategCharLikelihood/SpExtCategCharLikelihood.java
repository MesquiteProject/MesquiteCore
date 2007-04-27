package mesquite.diverse.SpExtCategCharLikelihood;


import java.util.Iterator;
import java.util.Vector;

import mesquite.diverse.lib.*;
import mesquite.diverse.SpExtCategCharMLCalculator.SpExtCategCharMLCalculator;
import mesquite.diverse.SpecExtincMLCalculator.SpecExtincMLCalculator;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.lib.duties.NumberForCharAndTree;

public class SpExtCategCharLikelihood extends NumberForCharAndTree {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(SpExtCategCharMLCalculator.class, getName() + "  needs a method to calculate likelihoods.",
		"The method to calculate likelihoods is arranged initially");
	}

	SpExtCategCharMLCalculator calcTask;


	MesquiteParameter e0;   //user specified extinction rate in state 0
	MesquiteParameter s0;   //user specified speciation rate in state 0
	MesquiteParameter e1;   //user specified extinction rate in state 1
	MesquiteParameter s1;   //user specified speciation rate in state 1
	MesquiteParameter t01;   //user specified transition rate from state 0 to state 1
	MesquiteParameter t10;   //user specifiedtransition rate from state 1 to state 0

	/*
	MesquiteDouble e0 = new MesquiteDouble(0.01);   //user specified extinction rate in state 0
	MesquiteDouble s0 = new MesquiteDouble(0.1);   //user specified speciation rate in state 0
	MesquiteDouble e1 = new MesquiteDouble(0.001);   //user specified extinction rate in state 1
	MesquiteDouble s1 = new MesquiteDouble(0.1);   //user specified speciation rate in state 1
	MesquiteDouble t01 = new MesquiteDouble(0.01);   //user specified transition rate from state 0 to state 1
	MesquiteDouble t10 = new MesquiteDouble(0.01);   //user specifiedtransition rate from state 1 to state 0
	 */
	MesquiteParameter[] params;
	MesquiteParameter[] paramsCopy;
	boolean[] selected;

	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		calcTask = (SpExtCategCharMLCalculator)hireEmployee(commandRec, SpExtCategCharMLCalculator.class, "Integrating Likelihood");
		if (calcTask == null)
			return sorry(commandRec, getName() + " couldn't start because no integrating likelihood calculator module obtained.");
		double def = MesquiteDouble.unassigned;
		//following is for the parameters explorer
		s0 = new MesquiteParameter("s0", "Rate of speciation with state 0", def, 0, MesquiteDouble.infinite, 0.000, 1);
		s1 = new MesquiteParameter("s1", "Rate of speciation with state 1", def, 0, MesquiteDouble.infinite, 0.000, 1);
		e0 = new MesquiteParameter("e0", "Rate of extinction with state 0", def, 0, MesquiteDouble.infinite, 0.000, 1);
		e1 = new MesquiteParameter("e1", "Rate of extinction with state 1", def, 0, MesquiteDouble.infinite, 0.000, 1);
		t01 = new MesquiteParameter("r01", "Rate of 0->1 changes", def, 0, MesquiteDouble.infinite, 0.000, 1);
		t10 = new MesquiteParameter("r10", "Rate of 1->0 changes", def, 0, MesquiteDouble.infinite, 0.000, 1);
		params = new MesquiteParameter[]{s0, s1, e0, e1, t01, t10};
		/*TEMPORARY
		for (int i= 0; i<6; i++)
			params[i].setValue(0.1);
	*/
		if (!commandRec.scripting()){
			showDialog();
		}
		addMenuItem("Set Parameters...", makeCommand("setParameters", this));
		/*	addMenuItem("Set State 0 Extinction Rate...", makeCommand("setE0", this));
		addMenuItem("Set State 1 Speciation Rate...", makeCommand("setS1", this));
		addMenuItem("Set State 1 Extinction Rate...", makeCommand("setE1", this));
		addMenuItem("Set 0 to 1 Transition Rate...", makeCommand("setT01", this));
		addMenuItem("Set 1 to 0 Transition Rate...", makeCommand("setT10", this));
		addMenuItem("-", null);
		 */
		addMenuItem("Write table to console", makeCommand("writeTable",this));
		addMenuItem("Write code for R to console", makeCommand("writeForExternalApp",this));

		/* Test model will dump values to console for fixed branch lenght and different e values
        testModel = new SpecExtincCategModel(1E-6, 0.001, 1E-6, 0.01, 0.05, 0.05);
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
		 */
        
        // running RK4 to compare with RKF45
/*        DEQNumSolver testSolver4 = new RK4Solver();
        Vector results = new Vector();
        DESystem ts = new TestModel();
        double [] starts = {0.5,0.5};
        testSolver4.integrate(0.0, starts, 0.025, 1.1, ts, results, true);
        
        Iterator foo = results.iterator();
        Debugg.println("RKF4 results");
        while (foo.hasNext()){
            double[] aResult = (double[]) foo.next();
            Debugg.println(DoubleArray.toString(aResult));
        }
        
        
        // testing RKF45
        DEQNumSolver testSolver45 = new RKF45Solver();
        results = new Vector();
        testSolver45.integrate(0.0, starts, 0.25, 1.1, ts, results, true);
        
        foo = results.iterator();
        Debugg.println("RKF45 results");
        while (foo.hasNext()){
            double[] aResult = (double[]) foo.next();
            Debugg.println(DoubleArray.toString(aResult));
        }
*/

		return true;
	}
	/*.................................................................................................................*/
	boolean showDialog(){
		if (params == null)
			return false;
		ParametersDialog dlog = new ParametersDialog(containerOfModule(), "Parameters", params, null, 2, 2, false);
		dlog.completeAndShowDialog(true);
		boolean ok = (dlog.query()==0);
		if (ok) 
			dlog.acceptParameters();
		dlog.dispose();
		return ok;
	}

	public void initialize(Tree tree, CharacterDistribution charStates1, CommandRecord commandRec) {
		// TODO Auto-generated method stub

	}
	/*.................................................................................................................*/

	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();

		temp.addLine("getIntegTask ", calcTask);
		String pLine = MesquiteParameter.paramsToScriptString(params);
		if (!StringUtil.blank(pLine))
			temp.addLine("setParameters " + pLine);
		/*
		 * temp.addLine("setS0 " + s0);
		temp.addLine("setS1 " + s1);
		temp.addLine("setE0 " + e0);
		temp.addLine("setE1 " + e1);
		temp.addLine("setT01 " + t01);
		temp.addLine("setT10 " + t10);
	*/
		return temp;
	}
	boolean setParam(MesquiteParameter p, MesquiteParameter[] params, Parser parser){
		double newValue = MesquiteDouble.fromString(parser);
		int loc = parser.getPosition();
		String token = parser.getNextToken();
		if (token != null && "=".equals(token)){
			int con = MesquiteInteger.fromString(parser);
			if (MesquiteInteger.isCombinable(con) && con>=0 && con < params.length)
				p.setConstrainedTo(params[con], false);
		}
		else
			parser.setPosition(loc);
		if ((MesquiteDouble.isUnassigned(newValue) ||  newValue >=0) && newValue != e0.getValue()){
			p.setValue(newValue); //change mode
			return true;
		}
		return false;
	}

	/*.................................................................................................................*/
	/*  the main command handling method.  */
	public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {
		// Should be removed when debugged
		//double [] testvals = { 1E-11,1E-10,1E-9,1E-8,1E-7,5E-7,1E-6,2E-6,1E-5,1E-4,5E-4,1E-3,5E-3,1E-2,2E-2,5E-2,1E-1,2E-1,5E-01};
		if (checker.compare(getClass(), "Sets rate parameters", "[double double double double double double]", commandName, "setParameters")) {
			if (StringUtil.blank(arguments)){
				if (!commandRec.scripting() && showDialog())
					parametersChanged(null, commandRec);
			}
			else {
				parser.setString(arguments);
				boolean changed =  setParam(s0, params, parser);
				boolean more = setParam(s1, params, parser);
				changed = changed || more;
				more = setParam(e0, params, parser);
				changed = changed || more;
				more = setParam(e1, params, parser);
				changed = changed || more;
				more = setParam(t01, params, parser);
				changed = changed || more;
				more = setParam(t10, params, parser);
				changed = changed || more;
				if (changed && !commandRec.scripting())
					parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
			}

		}
		/*
		else if (checker.compare(getClass(), "Sets extinction rate in state 0", "[double]", commandName, "setE0")) {
			double newValue = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (!MesquiteDouble.isCombinable(newValue) && !commandRec.scripting())
				newValue = MesquiteDouble.queryDouble(containerOfModule(), "e0", "Instantaneous extinction rate in state 0", e0.getValue());
			if ((MesquiteDouble.isUnassigned(newValue) ||  newValue >=0) && newValue != e0.getValue()){
				e0.setValue(newValue); //change mode
				if (!commandRec.scripting())
					parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(getClass(), "Sets speciation rate in state 0", "[double]", commandName, "setS0")) {
			double newValue = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (!MesquiteDouble.isCombinable(newValue) && !commandRec.scripting())
				newValue = MesquiteDouble.queryDouble(containerOfModule(), "s0", "Instantaneous speciation rate in state 0", (double)s0.getValue());
			if ((MesquiteDouble.isUnassigned(newValue) ||  newValue >=0) && newValue != s0.getValue()){
				s0.setValue(newValue); //change mode
				if (!commandRec.scripting())
					parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(getClass(), "Sets extinction rate in state 1", "[double]", commandName, "setE1")) {
			double newValue = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (!MesquiteDouble.isCombinable(newValue) && !commandRec.scripting())
				newValue = MesquiteDouble.queryDouble(containerOfModule(), "e1", "Instantaneous extinction rate in state 1", (double)e1.getValue());
			if ((MesquiteDouble.isUnassigned(newValue) ||  newValue >=0) && newValue != e1.getValue()){
				e1.setValue(newValue); //change mode
				if (!commandRec.scripting())
					parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(getClass(), "Sets speciation rate in state 1", "[double]", commandName, "setS1")) {
			double newValue = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (!MesquiteDouble.isCombinable(newValue) && !commandRec.scripting())
				newValue = MesquiteDouble.queryDouble(containerOfModule(), "s1", "Instantaneous speciation rate in state 1", (double)s1.getValue());
			if ((MesquiteDouble.isUnassigned(newValue) ||  newValue >=0) && newValue != s1.getValue()){
				s1.setValue(newValue); //change mode
				if (!commandRec.scripting())
					parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(getClass(), "Sets transition rate from state 0 to state 1", "[double]", commandName, "setT01")) {
			double newValue = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (!MesquiteDouble.isCombinable(newValue) && !commandRec.scripting())
				newValue = MesquiteDouble.queryDouble(containerOfModule(), "t01", "Instantaneous transition rate from 0 to 1", (double)t01.getValue());
			if ((MesquiteDouble.isUnassigned(newValue) ||  newValue >=0) && newValue != t01.getValue()){
				t01.setValue(newValue); //change mode
				if (!commandRec.scripting())
					parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(getClass(), "Sets transition rate from state 1 to state 0", "[double]", commandName, "setT10")) {
			double newValue = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (!MesquiteDouble.isCombinable(newValue) && !commandRec.scripting())
				newValue = MesquiteDouble.queryDouble(containerOfModule(), "t10", "Instantaneous transition rate from 1 to 0", (double)t10.getValue());
			if ((MesquiteDouble.isUnassigned(newValue) ||  newValue >=0) && newValue != t10.getValue()){
				t10.setValue(newValue); //change mode
				if (!commandRec.scripting())
					parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
			}
		}*/
		else if (checker.compare(getClass(), "Returns integrating module", null, commandName, "getIntegTask")) {
			return calcTask;
		}
		/*		else if (checker.compare(getClass(), "Writes table to console", "", commandName, "writeTable")) {
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
		 */
		else
			return super.doCommand(commandName, arguments, commandRec, checker);
		return null;
	}


	public void calculateNumber(Tree tree, CharacterDistribution charStates, MesquiteNumber result, MesquiteString resultString, CommandRecord commandRec) {
		if (result == null)
			return;
		result.setToUnassigned();

		if (tree == null || charStates == null)
			return;
		
		paramsCopy = MesquiteParameter.cloneArray(params, paramsCopy);

		calcTask.calculateLogProbability(tree, charStates, paramsCopy, result, resultString, commandRec);

	}


	/*------------------------------------------------------------------------------------------*/


	public String getName() {
		return "BiSSE Speciation/Extinction Likelihood";
	}

	public String getAuthors() {
		return "Peter E. Midford & Wayne P. Maddison";
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

class TestModel implements DESystem {

    //private double e;   //extinction rate in state 0
    //private double s;   //speciation rate in state 0

    public TestModel(){
    }
    public double[]calculateDerivative(double t,double probs[],double[] result){
        // for clarity
        result[0] = 1+probs[0]*probs[0]; 
        result[1] = t*t + probs[1]*probs[1];
        return result;
    }


}

