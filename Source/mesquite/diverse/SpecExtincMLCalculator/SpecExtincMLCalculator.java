package mesquite.diverse.SpecExtincMLCalculator;

import java.util.Vector;

import mesquite.categ.lib.CategoricalDistribution;
import mesquite.categ.lib.CategoricalState;
import mesquite.diverse.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterDistribution;

public class SpecExtincMLCalculator extends MesquiteModule {

	// bunch of stuff copied from zMargLikeCateg - need to prune!!
	
	
	double [] probsExt, probsData;
    double[] yStart;  //initial value for numerical integration
 
	MesquiteNumber probabilityValue;

	long underflowCheckFrequency = -1; //2;  //how often to check that not about to underflow; 1 checks every time
	long underflowCheck = 1;
	double underflowCompensation = 1;
	MesquiteNumber minChecker;
	
	// Number of steps per branch, reduce for a faster, possibily sloppier result
    double stepCount = 1000;  //default 10000

    MesquiteBoolean intermediatesToConsole = new MesquiteBoolean(false);
	MesquiteBoolean conditionOnSurvival;

	boolean[] deleted = null;
	SpecExtincModel model;
	DEQNumSolver solver;

	
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		loadPreferences();
		solver = new RK4Solver();
		probabilityValue = new MesquiteNumber();
		minChecker = new MesquiteNumber(MesquiteDouble.unassigned);
		model = new SpecExtincModel(MesquiteDouble.unassigned, MesquiteDouble.unassigned);
		conditionOnSurvival = new MesquiteBoolean(false);
		addCheckMenuItem(null, "Condition on Survival", MesquiteModule.makeCommand("conditionOnSurvival", this), conditionOnSurvival);
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
		temp.addLine("conditionOnSurvival  " + conditionOnSurvival.toOffOnString());
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
		else if (checker.compare(this.getClass(), "Sets whether to condition by survival", "[on; off]", commandName, "conditionOnSurvival")) {
			conditionOnSurvival.toggleValue(new Parser().getFirstToken(arguments));
			parametersChanged(null, commandRec);
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
	private void initProbs(int nodes) {
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
	private double downPass(int node, Tree tree, SpecExtincModel model, DEQNumSolver solver) {
        double logComp;
		double d = 1;
		double e = 0;
		if (tree.nodeIsTerminal(node)) { //initial conditions from observations if terminal
			e = 0;
			d = 1;
			logComp = 0.0;  // no compensation required yet
		}
		else { //initial conditions from daughters if internal
			logComp = 0.0;
			for (int nd = tree.firstDaughterOfNode(node, deleted); tree.nodeExists(nd); nd = tree.nextSisterOfNode(nd, deleted)) {
				logComp += downPass(nd,tree,model,solver);
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
        			logComp +=  -Math.log(d);
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
	public void calculateLogProbability(Tree tree, MesquiteNumber prob, MesquiteDouble lambda, MesquiteDouble mu, MesquiteString resultString, CommandRecord commandRec) {  
		if (model==null ||  prob == null)
			return;
		estCount =0;
		zeroHit = false;
		prob.setToUnassigned();
		String rep = model.toString();
		int root = tree.getRoot(deleted);
		boolean estimated = false;
		model.setE(mu.getValue());
		model.setS(lambda.getValue());
       
        initProbs(tree.getNumNodeSpaces());

		
        
		double  logComp = downPass(root, tree,model, solver);
		double likelihood = 0.0;
		/*~~~~~~~~~~~~~~ calculateLogProbability ~~~~~~~~~~~~~~*/
		if (conditionOnSurvival.getValue())
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
		return SpecExtincMLCalculator.class;
	}

	public String getName() {
		return "Integrating (speciation/extinction) Likelihood Calculator";
	}
	public String getAuthors() {
		return "Peter E. Midford & Wayne P. Maddison";
	}

	public String getVersion() {
		return "0.1";
	}

	public String getExplanation(){
		return "Calculates likelihood of a tree using an extinction/speciation model expressed as a system of differential equations.";
	}

	public boolean isPrerelease(){
		return true;
	}

	
}

class SpecExtincModel implements DESystem {

    private double e;   //extinction rate in state 0
    private double s;   //speciation rate in state 0
 
    public SpecExtincModel(double e, double s){
        this.e = e;
        this.s = s;
    }

    public boolean isFullySpecified(){
    	return MesquiteDouble.isCombinable(e) && MesquiteDouble.isCombinable(s);
    }
   
    /*
     *  The probability values are passed the probs array in the following order
     *  probs[0] = E0 = extinction probability in lineage starting at t in the past at state 0
     *  probs[1] = P0 = probability of explaining the data, given system is in state 0 at time t
     * @see mesquite.correl.lib.DESystem#calculateDerivative(double, double[])
     */
    public String toString(){
        return "Reduced Clade Model s=" + MesquiteDouble.toString(s, 4) + " e=" + MesquiteDouble.toString(e, 4);
    }
    public double[]calculateDerivative(double t,double probs[],double[] result){
        // for clarity
        double extProb = probs[0];
        double dataProb = probs[1];
        result[0] = -(e+s)*extProb + s*extProb*extProb + e; 
        result[1] = -(e+s)*dataProb + 2*s*extProb*dataProb;
        return result;
    }
    
    public void setE(double e){
        this.e = e;
    }
    
    public void setS(double s){
        this.s = s;
    }
    

    public double getSRate() {
            return s;
    }
    
    public double getERate() {
            return e;
    }
}

