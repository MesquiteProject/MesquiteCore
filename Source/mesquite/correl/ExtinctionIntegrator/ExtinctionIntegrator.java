package mesquite.correl.ExtinctionIntegrator;


import mesquite.correl.IntegLikeCateg.IntegLikeCateg;
import mesquite.correl.lib.DEQNumSolver;
import mesquite.correl.lib.DESystem;
import mesquite.correl.lib.RK4Solver;
import mesquite.correl.lib.CladeExtinctionModel;
import mesquite.lib.CommandChecker;
import mesquite.lib.CommandRecord;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.Tree;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.lib.duties.NumberForCharAndTree;

public class ExtinctionIntegrator extends NumberForCharAndTree {
	
	DEQNumSolver solver;
	DESystem speciesModel;
	IntegLikeCateg calcTask;
    
	double e0 = 0.001;   //user specified extinction rate in state 0
    double s0 = 0.001;   //user specified speciation rate in state 0
    double e1 = 0.005;   //user specified extinction rate in state 1
    double s1 = 0.001;   //user specified speciation rate in state 1
    double t01 = 0.01;  //user specified transition rate from state 0 to state 1
    double t10 = 0.01;  //user specifiedtransition rate from state 1 to state 0


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

        speciesModel = null; //new CladeExtinctionModel(0.001, 0.001, 0.005, 0.001, 0.01, 0.01);
		return true;
	}

	public void initialize(Tree tree, CharacterDistribution charStates1, CommandRecord commandRec) {
		// TODO Auto-generated method stub

	}
    
    /*.................................................................................................................*/
    /*  the main command handling method.  */
    public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {
        if (checker.compare(getClass(), "Sets extinction rate in state 0", "[double]", commandName, "setE0")) {
            double newE0 = MesquiteDouble.fromString(parser.getFirstToken(arguments));
            if (!MesquiteDouble.isCombinable(newE0) && !commandRec.scripting())
                newE0 = MesquiteDouble.queryDouble(containerOfModule(), "e0", "Instantaneous extinction rate in state 0", (double)e0);
            if (MesquiteDouble.isCombinable(newE0) && newE0 >=0 && newE0 != e0){
                e0 = newE0; //change mode
                speciesModel = new CladeExtinctionModel(e0,s0,e1,s1,t01,t10);
                parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
            }
        }
        else if (checker.compare(getClass(), "Sets speciation rate in state 0", "[double]", commandName, "setS0")) {
            double newS0 = MesquiteDouble.fromString(parser.getFirstToken(arguments));
            if (!MesquiteDouble.isCombinable(newS0) && !commandRec.scripting())
                newS0 = MesquiteDouble.queryDouble(containerOfModule(), "s0", "Instantaneous speciation rate in state 0", (double)s0);
            if (MesquiteDouble.isCombinable(newS0) && newS0 >=0 && newS0 != s0){
                s0 = newS0; //change mode
                speciesModel = new CladeExtinctionModel(e0,s0,e1,s1,t01,t10);
                parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
            }
        }
        else if (checker.compare(getClass(), "Sets extinction rate in state 1", "[double]", commandName, "setE1")) {
            double newE1 = MesquiteDouble.fromString(parser.getFirstToken(arguments));
            if (!MesquiteDouble.isCombinable(newE1) && !commandRec.scripting())
                newE1 = MesquiteDouble.queryDouble(containerOfModule(), "e1", "Instantaneous extinction rate in state 1", (double)e1);
            if (MesquiteDouble.isCombinable(newE1) && newE1 >=0 && newE1 != e1){
                e1 = newE1; //change mode
                speciesModel = new CladeExtinctionModel(e0,s0,e1,s1,t01,t10);
                parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
            }
        }
        else if (checker.compare(getClass(), "Sets speciation rate in state 1", "[double]", commandName, "setS1")) {
            double newS1 = MesquiteDouble.fromString(parser.getFirstToken(arguments));
            if (!MesquiteDouble.isCombinable(newS1) && !commandRec.scripting())
                newS1 = MesquiteDouble.queryDouble(containerOfModule(), "s1", "Instantaneous speciation rate in state 1", (double)s1);
            if (MesquiteDouble.isCombinable(newS1) && newS1 >=0 && newS1 != s1){
                s1 = newS1; //change mode
                speciesModel = new CladeExtinctionModel(e0,s0,e1,s1,t01,t10);
                parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
            }
        }
        else if (checker.compare(getClass(), "Sets transition rate from state 0 to state 1", "[double]", commandName, "setT01")) {
            double newT01 = MesquiteDouble.fromString(parser.getFirstToken(arguments));
            if (!MesquiteDouble.isCombinable(newT01) && !commandRec.scripting())
                newT01 = MesquiteDouble.queryDouble(containerOfModule(), "t01", "Instantaneous transition rate from 0 to 1", (double)t01);
            if (MesquiteDouble.isCombinable(newT01) && newT01 >=0 && newT01 != t01){
                t01 = newT01; //change mode
                speciesModel = new CladeExtinctionModel(e0,s0,e1,s1,t01,t10);
                parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
            }
        }
        else if (checker.compare(getClass(), "Sets transition rate from state 1 to state 0", "[double]", commandName, "setT10")) {
            double newT10 = MesquiteDouble.fromString(parser.getFirstToken(arguments));
            if (!MesquiteDouble.isCombinable(newT10) && !commandRec.scripting())
                newT10 = MesquiteDouble.queryDouble(containerOfModule(), "t10", "Instantaneous transition rate from 1 to 0", (double)t10);
            if (MesquiteDouble.isCombinable(newT10) && newT10 >=0 && newT10 != t10){
                t10 = newT10; //change mode
                speciesModel = new CladeExtinctionModel(e0,s0,e1,s1,t01,t10);
                parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
            }
        }
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
        if (speciesModel == null)
            speciesModel = new CladeExtinctionModel(e0,s0,e1,s1,t01,t10);
		calcTask.calculateLogProbability(tree, speciesModel, solver, charStates, resultString, result, commandRec);

	}


	public String getName() {
		// TODO Auto-generated method stub
		return "Speciation/Extinction Integrator";
	}
	
	public String getAuthors() {
		return "Peter E. Midford, Sarah P. Otto & Wayne P. Maddison";
	}

	public String getVersion() {
		return "0.1";
	}

	public String getExplanation(){
		return "Calculates likelihoods using a speciation/extinction model";
	}

	public boolean isPrerelease(){
		return true;
	}


}
