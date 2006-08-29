package mesquite.diverse.IntegLikeCateg;

import java.util.Vector;

import mesquite.categ.lib.CategoricalDistribution;
import mesquite.categ.lib.CategoricalState;
import mesquite.diverse.lib.*;
import mesquite.lib.CommandChecker;
import mesquite.lib.CommandRecord;
import mesquite.lib.Debugg;
import mesquite.lib.Double2DArray;
import mesquite.lib.DoubleArray;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteSubmenuSpec;
import mesquite.lib.Snapshot;
import mesquite.lib.StringArray;
import mesquite.lib.Tree;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.stochchar.lib.ProbabilityCategCharModel;

public class IntegLikeCateg extends MesquiteModule {

	// bunch of stuff copied from zMargLikeCateg - need to prune!!
	
	
	double[][] downProbs,  finalProbs;  // probability at node N given state s at base
	double[][] downProbs2; // probabilty at node N given state s1 for char1 and s2 for char2 at base
	double [][] probsExt, probsData;
    double[] yStart;  //initial value for numerical integration
    double[] d,e; //intermediate results
	double[][] savedRootEstimates; // holds the estimates at the root (for simulation priors, etc.)
	double[] underflowCompDown, underflowCompUp;  // ln of compensation for underflow
	double[] empirical;
	MesquiteNumber probabilityValue;
	int numStates;
	ProbabilityCategCharModel tempModel;
	ProbabilityCategCharModel passedModel;
	long underflowCheckFrequency = 2;  //how often to check that not about to underflow; 1 checks every time
	long underflowCheck = 1;
	MesquiteNumber minChecker;
	
	// Number of steps per branch, reduce for a faster, possibily sloppier result
    public static final double STEP_COUNT = 1000;

	//In version 1.1. the assumption about the root prior for model estimation, ancestral state reconstruction and simulation is assumed to be embedded in the model
	//Thus, the control is removed here
	public static final int ROOT_IGNOREPRIOR = 0;  // likelihoodignore's model's prior
	public static final int ROOT_USEPRIOR = 1;  // calculates ancesteral states imposing model's prior
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
        
		addMenuItem("Likelihood Decision Threshold...", makeCommand("setDecision", this));
        this.addCheckMenuItem(null, "Intermediates to console", makeCommand("toggleIntermediatesToConsole",this), intermediatesToConsole);
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
				parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
			}
		}
        else if (checker.compare(getClass(),"Sets whether to write intermediate branch values to console","[on; off]", commandName, "toggleIntermediatesToConsole")){
            intermediatesToConsole.toggleValue(parser.getFirstToken(arguments));
        }
        else if (checker.compare(getClass(), "Sets the frequency of checking for underflow", "[integer, 1 or greater]", commandName, "setUnderflowCheckFreq")) {
            int freq = MesquiteInteger.fromString(parser.getFirstToken(arguments));
            if (!MesquiteInteger.isCombinable(freq) && !commandRec.scripting())
                freq = MesquiteInteger.queryInteger(containerOfModule(), "Checking frequency", "Frequency at which underflow checking is performed in likelihood calculations.  A value of n means checking is performed on each nth calculation; higher numbers mean the calculations go faster but are at risk of underflow problems.  Values over 10 are not recommended", (int)underflowCheckFrequency, 1, 10000);

            if (MesquiteInteger.isCombinable(freq) && freq >=0 && freq!=underflowCheckFrequency){
                underflowCheckFrequency = freq; //change mode
                parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
            }
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
			downProbs = new double[nodes][numStates];
			finalProbs = new double[nodes][numStates];
			underflowCompDown = new double[nodes];
			underflowCompUp = new double[nodes];
			empirical = new double[numStates];
            yStart = new double[2*numStates];
            d = new double[numStates];
            e = new double[numStates];
		}
		Double2DArray.zeroArray(downProbs);
		Double2DArray.zeroArray(probsData);
		Double2DArray.zeroArray(probsExt);
		Double2DArray.zeroArray(finalProbs);
		DoubleArray.zeroArray(underflowCompDown);
		DoubleArray.zeroArray(underflowCompUp);
		DoubleArray.zeroArray(empirical);
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
			for (int i=0; i<probs.length; i++)
				probs[i] /= q;
		}
		return -Math.log(q);
	}
	/*.................................................................................................................*/
	/* assumes hard polytomies */
	Vector integrationResults = null;
    

    /* now returns underflow compensation */
	private double downPass(int node, Tree tree, DESystem model, DEQNumSolver solver, CategoricalDistribution observedStates) {
        double comp;
		if (tree.nodeIsTerminal(node)) {
			long observed = ((CategoricalDistribution)observedStates).getState(tree.taxonNumberOfNode(node));
			int obs = CategoricalState.minimum(observed); //NOTE: just minimum observed!
			//Debugg.println("node " + node + " state " + CategoricalState.toString(observed));
			for (int state = 0;state < numStates;state++){
				e[state]=0;
				if (state == obs)
					d[state] = 1;
				else
					d[state] = 0;
			}
            comp = 0.0;  // no compensation required yet
		}
		else {
            comp = 0.0;
			for (int nd = tree.firstDaughterOfNode(node, deleted); tree.nodeExists(nd); nd = tree.nextSisterOfNode(nd, deleted)) {
				comp += downPass(nd,tree,model,solver,observedStates);
			}
			for(int state = 0; state < numStates;state++){
				d[state] = 1;
				e[state] = 1;
				for (int nd = tree.firstDaughterOfNode(node, deleted); tree.nodeExists(nd); nd = tree.nextSisterOfNode(nd, deleted)) {
					e[state] *= probsExt[nd][state];
					d[state] *= probsData[nd][state];
				}
                if (model instanceof DESpeciationSystem){
                    DESpeciationSystem ms = (DESpeciationSystem)model;
                    d[state] *= ms.getSRate(state);
                }
			}
		}
        if (node == tree.getRoot()){
            for(int i=0;i<numStates;i++){
                probsExt[node][i] = e[i];
                probsData[node][i] = d[i];
            }
            return comp;
        }
        else{
            double x = 0;
            double length = tree.getBranchLength(node,1.0,deleted);
            double h = length/STEP_COUNT;       //this will need tweaking!
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
                if (++underflowCheck % underflowCheckFrequency == 0){
                    comp += checkUnderflow(probsData[node]);
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
                x = 0;
                for(int i=0;i<integrationResults.size();i++){
                    if(i%100 == 0){
                        String xString = MesquiteDouble.toFixedWidthString(x, 13, false);
                        MesquiteMessage.print("x= "+ xString + " y =[");
                        double [] tempResults = (double[])integrationResults.get(i);
                        for(int j=0;j<tempResults.length;j++)
                            MesquiteMessage.print(MesquiteDouble.toFixedWidthString(tempResults[j],13,false)+" ");
                        MesquiteMessage.println("]");
                    }   
                    x += h;
                }
            }
            return comp;
        }
	}
	
	/*.................................................................................................................*/
	public void calculateLogProbability(Tree tree, DESystem speciesModel, DEQNumSolver solver, CharacterDistribution obsStates, MesquiteString resultString, MesquiteNumber prob, CommandRecord commandRec) {  
		if (speciesModel==null || obsStates==null || prob == null)
			return;
		estCount =0;
		zeroHit = false;
		prob.setToUnassigned();
		CategoricalDistribution observedStates = (CategoricalDistribution)obsStates;
		String rep = speciesModel.toString();
		double comp;
		int root = tree.getRoot(deleted);
		boolean estimated = false;
        
		if (observedStates.getMaxState() == 0){
		    MesquiteMessage.warnProgrammer("Character Distribution appears to be constant; will try to proceed assuming 2 possible states");
		    initProbs(tree.getNumNodeSpaces(),2);
        }
        else
            initProbs(tree.getNumNodeSpaces(), observedStates.getMaxState()+1); 

		comp = downPass(root, tree,speciesModel, solver, observedStates);
		double likelihood = 0.0;
		/*~~~~~~~~~~~~~~ calculateLogProbability ~~~~~~~~~~~~~~*/

		for (int i=0;  i<=observedStates.getMaxState(); i++) 
			likelihood += probsData[root][i];

		comp = underflowCompDown[root];
		double negLogLikelihood = -(Math.log(likelihood) - comp);
		if (prob!=null)
			prob.setValue(negLogLikelihood);
		if (resultString!=null) {
			String s = "Likelihood from integration with model " + rep + "over branches;  -log L.:"+ MesquiteDouble.toString(negLogLikelihood) + " [L. "+ MesquiteDouble.toString(likelihood * Math.exp(-comp)) + "]";
			if (estimated){
				String set = passedModel.getSettingsString();
				if (set !=null)
					s += " " + set;
			}
			s += "  " + getParameters();
			resultString.setValue(s);
		}
	}
	
			
	int estCount =0;
	boolean zeroHit = false;

	public double logLikelihoodCalc(Tree tree, DESystem speciesModel, DEQNumSolver solver, CharacterDistribution states) {
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
		downPass(root, tree,speciesModel, solver, observedStates);
		double likelihood = 0.0;
		double comp;
		/*~~~~~~~~~~~~~~ logLikelihoodCalc ~~~~~~~~~~~~~~*/

		for (int i=0;  i<=observedStates.getMaxState(); i++) 
			likelihood += probsData[root][i];

		comp = underflowCompDown[root];
		double logLike = Math.log(likelihood) - comp;
		if (logLike> -0.00001) {
			zeroHit = true;
		}

		return (logLike);
	}


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
