package mesquite.diverse.IntegLikeNoChar;

import java.util.Vector;

import mesquite.categ.lib.CategoricalDistribution;
import mesquite.categ.lib.CategoricalState;
import mesquite.diverse.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterDistribution;

public class IntegLikeNoChar extends MesquiteModule {

	// bunch of stuff copied from zMargLikeCateg - need to prune!!
	
	
	double [] probsExt, probsData;
    double[] yStart;  //initial value for numerical integration
 	double[][] savedRootEstimates; // holds the estimates at the root (for simulation priors, etc.)
	MesquiteNumber probabilityValue;
	int numStates;

	long underflowCheckFrequency = 2; //2;  //how often to check that not about to underflow; 1 checks every time
	long underflowCheck = 1;
	double underflowCompensation = 1;
	MesquiteNumber minChecker;
	
	// Number of steps per branch, reduce for a faster, possibily sloppier result
    double stepCount = 1000;  //default 10000

    MesquiteBoolean intermediatesToConsole = new MesquiteBoolean(false);

	boolean[] deleted = null;

	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		loadPreferences();
		probabilityValue = new MesquiteNumber();
		minChecker = new MesquiteNumber(MesquiteDouble.unassigned);
        
		addMenuItem("Steps per Branch...", makeCommand("setStepCount", this));
		addCheckMenuItem(null, "Intermediates to console", makeCommand("toggleIntermediatesToConsole",this), intermediatesToConsole);

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
		if (probsData==null || probsData.length!=nodes){
			probsData = new double[nodes];
			probsExt = new double[nodes];
            yStart = new double[2];
  		}
		DoubleArray.zeroArray(probsData);
		DoubleArray.zeroArray(probsExt);
	}
	

	/*.................................................................................................................*/
	private double checkUnderflow(double prob, MesquiteDouble logComp){

		if (prob > 0.01)
			return prob;
		else {
			logComp.setValue(logComp.getValue()*( -Math.log(prob)));
				prob = 1.0;
		}
		return prob;
	}
	/*.................................................................................................................*/
	/* assumes hard polytomies */

    // This might hold the intermediate step results if requested for debugging
    Vector integrationResults = null;

    /* now returns underflow compensation */
	private double downPass(int node, Tree tree, DESpeciationSystemNoChar model, DEQNumSolver solver, CategoricalDistribution observedStates) {
        double logComp;
		double d = 1;
		double e = 0;
		if (tree.nodeIsTerminal(node)) { //initial conditions from observations if terminal
			long observed = ((CategoricalDistribution)observedStates).getState(tree.taxonNumberOfNode(node));
			int obs = CategoricalState.minimum(observed); //NOTE: just minimum observed!
			//Debugg.println("node " + node + " state " + CategoricalState.toString(observed));
			e = 0;
			d = 1;
			logComp = 0.0;  // no compensation required yet
		}
		else { //initial conditions from daughters if internal
			logComp = 0.0;
			for (int nd = tree.firstDaughterOfNode(node, deleted); tree.nodeExists(nd); nd = tree.nextSisterOfNode(nd, deleted)) {
				logComp += downPass(nd,tree,model,solver,observedStates);
			}
			
				d = 1;  //two here to anticipate two daughters???
				e = 1;
				//TODO: either filter to permit only ultrametric, or redo calculations to permit extinct nodes.
				//TODO: likewise for polytomies
				for (int nd = tree.firstDaughterOfNode(node, deleted); tree.nodeExists(nd); nd = tree.nextSisterOfNode(nd, deleted)) {
                   // Debugg.println("probsExt["+nd +"][" + state + "] = " + probsExt[nd][state]);
                   // Debugg.println("probsData["+nd +"][" + state + "] = " + probsData[nd][state]);
					e = probsExt[nd];
					d *= probsData[nd];
				}

                if (node != tree.getRoot()){  //condition on splitting at root; thus don't include rate if at root
                    d *= model.getSRate();
                 }
                 
			
            if (underflowCheckFrequency>=0 && ++underflowCheck % underflowCheckFrequency == 0){
        		if (d  < 0.01) {
        			logComp *=  -Math.log(d);
        			d = 1.0;
        		}
             }
		}
        if (node == tree.getRoot()){
                probsExt[node]= e;
                probsData[node] = d;
            
            return logComp;
        }
        else{
            double x = 0;
            double length = tree.getBranchLength(node,1.0,deleted);
            double h = length/stepCount;       //this will need tweaking!

                yStart[0] = e;
                yStart[1] = d;
            
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
            if (yEnd.length == 2){
              
                    probsExt[node] = yEnd[0];
                    probsData[node] = yEnd[1];
                
            }
            else {
                MesquiteMessage.warnProgrammer("Vector returned by solver not the same size supplied!");

                    probsExt[node]=0;   //is this the right thing here?
                    probsData[node]=0;  //this is probably the best choice here
                
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
	public void calculateLogProbability(Tree tree, DESpeciationSystemNoChar speciesModel, boolean conditionBySurvival, DEQNumSolver solver, CharacterDistribution obsStates, MesquiteString resultString, MesquiteNumber prob, CommandRecord commandRec) {  
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
        
		if (speciesModel instanceof ReducedCladeModel)
            workingMaxState =1;
        else if (observedStates.getMaxState() == 0){
		    MesquiteMessage.warnProgrammer("Character Distribution appears to be constant; will try to proceed assuming 2 possible states");
		    workingMaxState = 1;
        }
        else
            workingMaxState = observedStates.getMaxState()+1; 
        
        initProbs(tree.getNumNodeSpaces(),workingMaxState);

        logComp = downPass(root, tree,speciesModel, solver, observedStates);
		double likelihood = 0.0;
		/*~~~~~~~~~~~~~~ calculateLogProbability ~~~~~~~~~~~~~~*/
		if (conditionBySurvival)
			likelihood = probsData[root]/(1-probsExt[root])/(1-probsExt[root]);

		else

				likelihood = probsData[root];
			
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
		if (conditionBySurvival)
			likelihood = probsData[root]/(1-probsExt[root])/(1-probsExt[root]);

		else

				likelihood = probsData[root];

		double logLike = Math.log(likelihood) - comp;
		if (logLike> -0.00001) {
			zeroHit = true;
		}

		return (logLike);
	}

*/
	public Class getDutyClass() {
		// TODO Auto-generated method stub
		return IntegLikeNoChar.class;
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
