package mesquite.diverse.IntegLikeCateg;

import java.util.Vector;

import mesquite.categ.lib.CategoricalDistribution;
import mesquite.categ.lib.CategoricalState;
import mesquite.diverse.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterDistribution;

public class IntegLikeCateg extends MesquiteModule {

	// bunch of stuff copied from zMargLikeCateg - need to prune!!
	
	
	double [][] probsExt, probsData;
    double[] yStart;  //initial value for numerical integration
    double[] d,e; //intermediate results
	double[][] savedRootEstimates; // holds the estimates at the root (for simulation priors, etc.)
	MesquiteNumber probabilityValue;
	int numStates;

	long underflowCheckFrequency = 2; //2;  //how often to check that not about to underflow; 1 checks every time
	long underflowCheck = 1;
	double underflowCompensation = 1;
	MesquiteNumber minChecker;
	
	// Number of steps per branch, reduce for a faster, possibily sloppier result
    double stepCount = 1000;  //default 10000

	//In version 1.1. the assumption about the root prior for model estimation, ancestral state reconstruction and simulation is assumed to be embedded in the model
	//Thus, the control is removed here
	public static final int ROOT_IGNOREPRIOR = 0;  // likelihoodignore's model's prior
	public static final int ROOT_USEPRIOR = 1;  // calculates ancestral states imposing model's prior
	public boolean showRootModeChoices = false; 
	int rootMode = ROOT_USEPRIOR; //what if anything is done with prior probabilities of states at subroot?  
	StringArray rootModes;
	MesquiteString rootModeName;

	static final int REPORT_Proportional = 0;  
	static final int REPORT_Raw = 1;
	static final int REPORT_Log = 2;
	StringArray reportModes;
	int reportMode = REPORT_Proportional;
	MesquiteString reportModeName;
    MesquiteBoolean intermediatesToConsole = new MesquiteBoolean(false);

	boolean[] deleted = null;

	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		loadPreferences();
		probabilityValue = new MesquiteNumber();
		minChecker = new MesquiteNumber(MesquiteDouble.unassigned);
        
		addMenuItem("Steps per Branch...", makeCommand("setStepCount", this));
		addCheckMenuItem(null, "Intermediates to console", makeCommand("toggleIntermediatesToConsole",this), intermediatesToConsole);
		rootModes = new StringArray(2);  
		rootModes.setValue(ROOT_IGNOREPRIOR, "Ignore Root State Frequencies");  //the strings passed will be the menu item labels
		rootModes.setValue(ROOT_USEPRIOR, "Use Root State Frequencies as Prior");
		rootModeName = new MesquiteString(rootModes.getValue(rootMode));  //this helps the menu keep track of checkmenuitems

		//if (showRootModeChoices && getHiredAs() != MargLikeAncStCLForModel.class){
			/*Treatment of prior at root; currently user interface hidden unless preferences file put in place*/
		//	MesquiteSubmenuSpec mssr = addSubmenu(null, "Root Reconstruction", makeCommand("setRootMode", this), rootModes); 
		//	mssr.setSelected(rootModeName);
			/**/
		MesquiteSubmenuSpec mLO = addSubmenu(null, "Likelihood Optimization", null); 
		addItemToSubmenu(null, mLO, "Underflow Checking...", makeCommand("setUnderflowCheckFreq", this));

		return true;
	}
	boolean reportCladeValues = false;
	public void setReportCladeLocalValues(boolean reportCladeValues){
		this.reportCladeValues = reportCladeValues;
	}
	public boolean getReportCladeLocalValues(){
		return reportCladeValues;
	}
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setUnderflowCheckFreq " + underflowCheckFrequency);
		temp.addLine("setStepCount " + stepCount);
		return temp;
	}

	/*.................................................................................................................*/
	/*  the main command handling method.  */
	public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {
		if (checker.compare(getClass(), "Sets the frequency of checking for underflow", "[integer, 1 or greater]", commandName, "setUnderflowCheckFreq")) {
			int freq = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (!MesquiteInteger.isCombinable(freq) && !commandRec.scripting())
				freq = MesquiteInteger.queryInteger(containerOfModule(), "Checking frequency", "Frequency at which underflow checking is performed in likelihood calculations.  A value of n means checking is performed on each nth calculation; higher numbers mean the calculations go faster but are at risk of underflow problems.  Values over 10 are not recommended", (int)underflowCheckFrequency, 1, 10000);

			if (MesquiteInteger.isCombinable(freq) && freq >=0 && freq!=underflowCheckFrequency){
				underflowCheckFrequency = freq; //change mode
				if (!commandRec.scripting())
					parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
			}
		}
	       else if (checker.compare(getClass(), "Sets the number of steps per branch", "[integer, 1 or greater]", commandName, "setStepCount")) {
				double steps = MesquiteDouble.fromString(parser.getFirstToken(arguments));
	            if (!MesquiteDouble.isCombinable(steps) && !commandRec.scripting())
	            	steps = MesquiteDouble.queryDouble(containerOfModule(), "Steps per branch", "Number of divisions of each branch for numerical integration.  Higher numbers mean the calculations are more accurate but go more slowly.  Values under 100 are not recommended", stepCount, 10, 1000000);

	            if (MesquiteDouble.isCombinable(steps) && steps >=0 && steps!=stepCount){
	            	stepCount = steps; //change mode
					if (!commandRec.scripting())
	                parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
	            }
	        }
       else if (checker.compare(getClass(),"Sets whether to write intermediate branch values to console","[on; off]", commandName, "toggleIntermediatesToConsole")){
            intermediatesToConsole.toggleValue(parser.getFirstToken(arguments));
        }
 		else
			return super.doCommand(commandName, arguments, commandRec, checker);
		return null;
	}
	/*.................................................................................................................*/
	private void initProbs(int nodes, int numStates) {
		this.numStates = numStates;
		if (probsData==null || probsData.length!=nodes || probsData[0].length!=numStates){
			probsData = new double[nodes][numStates];
			probsExt = new double[nodes][numStates];
            yStart = new double[2*numStates];
            d = new double[numStates];
            e = new double[numStates];
		}
		Double2DArray.zeroArray(probsData);
		Double2DArray.zeroArray(probsExt);
	}
	

	/*.................................................................................................................*/
	private double checkUnderflow(double[] probs){
		minChecker.setValue(MesquiteDouble.unassigned);
		for (int i=0; i<probs.length; i++)
			minChecker.setMeIfIAmMoreThan(probs[i]);
		double q = minChecker.getDoubleValue();
		if (q == 0)
			return 0;
		else {
//Debugg.println("underflow comp used " + q);
			for (int i=0; i<probs.length; i++)
				probs[i] /= q;
		}
		return -Math.log(q);
	}
	/*.................................................................................................................*/
	/* assumes hard polytomies */

    // This might hold the intermediate step results if requested for debugging
    Vector integrationResults = null;

    /* now returns underflow compensation */
	private double downPass(int node, Tree tree, DESpeciationSystemCateg model, DEQNumSolver solver, CategoricalDistribution observedStates) {
        double logComp;
		if (tree.nodeIsTerminal(node)) { //initial conditions from observations if terminal
			long observed = ((CategoricalDistribution)observedStates).getState(tree.taxonNumberOfNode(node));
			int obs = CategoricalState.minimum(observed); //NOTE: just minimum observed!
			//Debugg.println("node " + node + " state " + CategoricalState.toString(observed));
			for (int state = 0;state < numStates;state++){
				e[state]=0;
				if ((state == obs) || (model instanceof ReducedCladeModel))
					d[state] = 1;
				else
					d[state] = 0;
			}
			logComp = 0.0;  // no compensation required yet
		}
		else { //initial conditions from daughters if internal
			logComp = 0.0;
			for (int nd = tree.firstDaughterOfNode(node, deleted); tree.nodeExists(nd); nd = tree.nextSisterOfNode(nd, deleted)) {
				logComp += downPass(nd,tree,model,solver,observedStates);
			}
			for(int state = 0; state < numStates;state++){
				d[state] = 1;  //two here to anticipate two daughters???
				e[state] = 1;
				//TODO: either filter to permit only ultrametric, or redo calculations to permit extinct nodes.
				//TODO: likewise for polytomies
				for (int nd = tree.firstDaughterOfNode(node, deleted); tree.nodeExists(nd); nd = tree.nextSisterOfNode(nd, deleted)) {
                   // Debugg.println("probsExt["+nd +"][" + state + "] = " + probsExt[nd][state]);
                   // Debugg.println("probsData["+nd +"][" + state + "] = " + probsData[nd][state]);
					e[state] = probsExt[nd][state];
					d[state] *= probsData[nd][state];
				}

                if (node != tree.getRoot()){  //condition on splitting at root; thus don't include rate if at root
                     d[state] *= model.getSRate(state);
                }
                 
			}
            if (underflowCheckFrequency>=0 && ++underflowCheck % underflowCheckFrequency == 0){
            	logComp += checkUnderflow(d);
            }
		}
        if (node == tree.getRoot()){
            for(int i=0;i<numStates;i++){
                probsExt[node][i] = e[i];
                probsData[node][i] = d[i];
            }
            return logComp;
        }
        else{
            double x = 0;
            double length = tree.getBranchLength(node,1.0,deleted);
            double h = length/stepCount;       //this will need tweaking!
            for(int i=0;i<numStates;i++){
                yStart[i] = e[i];
                yStart[i+numStates] = d[i];
            }
            if (intermediatesToConsole.getValue()){
                MesquiteMessage.print("node " + node);
                if (node == tree.getRoot())
                    MesquiteMessage.println(" is root");
                else
                    MesquiteMessage.println("");
                MesquiteMessage.println("At start, y is " + DoubleArray.toString(yStart));
            }
            integrationResults = solver.integrate(x,yStart,h,length,model,integrationResults,intermediatesToConsole.getValue());        
            double[] yEnd = (double[])integrationResults.lastElement();
            if (yEnd.length == 2*numStates){
                for(int i=0;i<numStates;i++){
                    probsExt[node][i] = yEnd[i];
                    probsData[node][i] = yEnd[i+numStates];
                }
            }
            else {
                MesquiteMessage.warnProgrammer("Vector returned by solver not the same size supplied!");
                for(int i=0;i<numStates;i++){
                    probsExt[node][i]=0;   //is this the right thing here?
                    probsData[node][i]=0;  //this is probably the best choice here
                }
            }
            if (intermediatesToConsole.getValue()){
                MesquiteMessage.println("Intermediate values");
                StringBuffer stateMsg = new StringBuffer(1000);
                //stateMsg.delete(0,stateMsg.length());  //flush everything
                x = h;
                double [] tempResults;
                for(int i=0;i<integrationResults.size();i++){
                    if(i%100 == 0){
                        String xString = MesquiteDouble.toFixedWidthString(x, 13, false);
                        stateMsg.append("x= "+ xString + " y =[");
                        tempResults = (double[])integrationResults.get(i);
                        for(int j=0;j<tempResults.length;j++)
                            stateMsg.append(MesquiteDouble.toFixedWidthString(tempResults[j],13,false)+" ");
                        stateMsg.append("]\n");
                    }   
                    x += h;
                }
                stateMsg.append("Final value; \n");
                stateMsg.append("x= " + h*stepCount + " y =[");
                tempResults = (double[])integrationResults.lastElement();
                for(int j=0;j<tempResults.length;j++)
                    stateMsg.append(MesquiteDouble.toFixedWidthString(tempResults[j],13,false)+" ");
                stateMsg.append("]\n");
                MesquiteMessage.println(stateMsg.toString());
            }
            return logComp;
        }
	}
	
	/*.................................................................................................................*/
	public void calculateLogProbability(Tree tree, DESpeciationSystemCateg speciesModel, boolean conditionBySurvival, DEQNumSolver solver, CharacterDistribution obsStates, MesquiteString resultString, MesquiteNumber prob, CommandRecord commandRec) {  
		if (speciesModel==null || obsStates==null || prob == null)
			return;
		estCount =0;
		zeroHit = false;
		prob.setToUnassigned();
		CategoricalDistribution observedStates = (CategoricalDistribution)obsStates;
		String rep = speciesModel.toString();
		double logComp;
		int root = tree.getRoot(deleted);
		boolean estimated = false;
        int workingMaxState;
        
		if (observedStates.getMaxState() == 0){
		    MesquiteMessage.warnProgrammer("Character Distribution appears to be constant; will try to proceed assuming 2 possible states");
		    workingMaxState = 1;
        }
        else
            workingMaxState = observedStates.getMaxState()+1; 
        
        initProbs(tree.getNumNodeSpaces(),workingMaxState);

        logComp = downPass(root, tree,speciesModel, solver, observedStates);
		double likelihood = 0.0;
		/*~~~~~~~~~~~~~~ calculateLogProbability ~~~~~~~~~~~~~~*/
		if (conditionBySurvival){
			
		for (int i=0;  i<workingMaxState; i++) {
			likelihood += probsData[root][i]/(1-probsExt[root][i])/(1-probsExt[root][i]);
			}
		}
		else
			for (int i=0;  i<workingMaxState; i++) 
				likelihood += probsData[root][i];
			
		double negLogLikelihood = -(Math.log(likelihood) - logComp);
		if (prob!=null)
			prob.setValue(negLogLikelihood);
		if (resultString!=null) {
			String s = "Likelihood from integration with model " + rep + "over branches;  -log L.:"+ MesquiteDouble.toString(negLogLikelihood) + " [L. "+ MesquiteDouble.toString(likelihood * Math.exp(-logComp)) + "]";
			s += "  " + getParameters();
			resultString.setValue(s);
		}
	}
	
			
	int estCount =0;
	boolean zeroHit = false;

/*	public double logLikelihoodCalc(Tree tree, DESystem speciesModel, boolean conditionBySurvival, DEQNumSolver solver, CharacterDistribution states) {
		if (speciesModel == null)
			return 0;
		if (solver == null)
			solver = new RK4Solver();
		if (zeroHit)
			return -0.001*(++estCount);  // to shortcircuit the optimizer wandering around zero with very high rates
		CategoricalDistribution observedStates = (CategoricalDistribution)states;
        if (observedStates.getMaxState() == 0){
            MesquiteMessage.warnProgrammer("Character Distribution appears to be constant; will try to proceed assuming 2 possible states");
            initProbs(tree.getNumNodeSpaces(),2);
        }
        else
            initProbs(tree.getNumNodeSpaces(), observedStates.getMaxState()+1); //the model should use the observedStates to set this; observedStates should carry info about its maximum conceivable number of states?

		estCount++;
		int root = tree.getRoot(deleted);
		double comp = downPass(root, tree,speciesModel, solver, observedStates);
		double likelihood = 0.0;
		/*~~~~~~~~~~~~~~ logLikelihoodCalc ~~~~~~~~~~~~~~*
		if (conditionBySurvival){
			
			for (int i=0;  i<workingMaxState; i++) {
				likelihood += probsData[root][i]/(1-probsExt[root][i])/(1-probsExt[root][i]);
//	Debugg.println("pe " + probsExt[root][i]);
				}
			}
			else
				for (int i=0;  i<workingMaxState; i++) 
					likelihood += probsData[root][i];

		for (int i=0;  i<=observedStates.getMaxState(); i++) 
			likelihood += probsData[root][i];

		double logLike = Math.log(likelihood) - comp;
		if (logLike> -0.00001) {
			zeroHit = true;
		}

		return (logLike);
	}
*/

	public Class getDutyClass() {
		// TODO Auto-generated method stub
		return IntegLikeCateg.class;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "Integrating (speciation/extinction) Likelihood Calculator for Categorical Data";
	}
	public String getAuthors() {
		return "Peter E. Midford & Wayne P. Maddison";
	}

	public String getVersion() {
		return "0.1";
	}

	public String getExplanation(){
		return "Calculates likelihood of a tree and tip values using an extinction/speciation model expressed as a system of differential equations.";
	}

	public boolean isPrerelease(){
		return true;
	}

	
}
