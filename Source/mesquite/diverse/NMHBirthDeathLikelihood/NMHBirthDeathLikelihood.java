package mesquite.diverse.NMHBirthDeathLikelihood;

import java.util.Arrays;

import mesquite.lib.duties.NumberForTree;
import mesquite.lib.duties.ParametersExplorer;
import mesquite.lib.*;

public class NMHBirthDeathLikelihood extends NumberForTree implements ParametersExplorable {
   	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(ParametersExplorer.class, getName() + "  uses a Parameters Explorer to show likelihood surfaces.",
		"The parameter explorer is arranged automatically");
	}

    MesquiteParameter lambdaP = new MesquiteParameter();
    MesquiteParameter muP = new MesquiteParameter();
    
    double savedLambda;
    double savedMu;

    double lambda = 0.4;
    double mu = 0.2;
    
    
    MesquiteParameter lambdap = new MesquiteParameter();
    MesquiteParameter mup = new MesquiteParameter();

    MesquiteParameter[] parameters;
    ParametersExplorer explorer;

    Tree lastTree;
    
    double height;
    double []branchTimes;
    
    public boolean startJob(String arguments, Object condition, boolean hiredByName) {
        addMenuItem("Set lambda (Speciation Rate)...", makeCommand("setLambda", this));
        addMenuItem("Set mu (Extinction Rate)...", makeCommand("setMu", this));
        addMenuItem("-", null);
        addMenuItem("Show Parameters Explorer", makeCommand("showParamExplorer",this));
        //following is for the parameters explorer
        lambdap.setName("lambda");
        lambdap.setExplanation("Rate of speciation");
        lambdap.setMinimumAllowed(0);
        lambdap.setMaximumAllowed(MesquiteDouble.infinite);
        lambdap.setMinimumSuggested(0.000);
        lambdap.setMaximumSuggested(1);
        lambdap.setValue(lambda);
        mup.setName("mu");
        mup.setExplanation("Rate of extinction");
        mup.setMinimumSuggested(0.000);
        mup.setMaximumSuggested(1);
        mup.setMinimumAllowed(0);
        mup.setMaximumAllowed(MesquiteDouble.infinite);
        mup.setValue(mu);
        parameters = new MesquiteParameter[]{lambdap,mup};

        return true;
    }
    
    public void employeeQuit(MesquiteModule employee){
        if (employee == explorer)
            explorer = null;
    }

    public Snapshot getSnapshot(MesquiteFile file) {
        Snapshot temp = new Snapshot();

            temp.addLine("setLambda " + MesquiteDouble.toString(lambda));
            temp.addLine("setMu " + MesquiteDouble.toString(mu));
           if (explorer != null)
                temp.addLine("showParamExplorer ", explorer);
        
        return temp;
    }

    public Object doCommand(String commandName, String arguments, CommandChecker checker) {
        // Should be removed when debugged
        if (checker.compare(getClass(), "Sets mu (extinction rate)", "[double]", commandName, "setMu")) {
            double newMu = MesquiteDouble.fromString(parser.getFirstToken(arguments));
            if (!MesquiteDouble.isCombinable(newMu) && !MesquiteThread.isScripting())
                newMu = MesquiteDouble.queryDouble(containerOfModule(), "mu", "mu (extinction rate)", (double)mu);
            if (MesquiteDouble.isCombinable(newMu) && newMu >=0 && newMu != mu){
                mu = newMu; //change mode
                parametersChangedNotifyExpl(null); //this tells employer module that things changed, and recalculation should be requested
            }
        }
        else if (checker.compare(getClass(), "Sets lambda (speciation rate)", "[double]", commandName, "setLambda")) {
            double newLambda = MesquiteDouble.fromString(parser.getFirstToken(arguments));
            if (!MesquiteDouble.isCombinable(newLambda) && !MesquiteThread.isScripting())
                newLambda = MesquiteDouble.queryDouble(containerOfModule(), "lambda", "lambda (speciation rate)", (double)lambda);
            if (MesquiteDouble.isCombinable(newLambda) && newLambda >=0 && newLambda != lambda){
                lambda = newLambda; //change mode
                parametersChangedNotifyExpl(null); //this tells employer module that things changed, and recalculation should be requested
            }
        }
        else if (checker.compare(getClass(), "Displays parameter explorer", "", commandName, "showParamExplorer")) {
            explorer = (ParametersExplorer)hireEmployee(ParametersExplorer.class, "Parameters explorer");
            if (explorer == null)
                return null;
            explorer.setExplorable(this);
            return explorer;
        }
        else
            return  super.doCommand(commandName, arguments, checker);
        return null;
    }

    
    public void parametersChangedNotifyExpl(Notification n){
        if (!MesquiteThread.isScripting())
            parametersChanged(n);
        if (explorer != null)
            explorer.explorableChanged(this);
    }

    
    private void fillBranchTimes(Tree tree,int node, double height, MesquiteInteger count){
        if (tree.nodeIsInternal(node)){
            branchTimes[count.getValue()] = height;
            count.increment();
            for (int daughter=tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter) ) {
                fillBranchTimes(tree,daughter,height+tree.getBranchLength(daughter,1.0),count);
            }
        }
    }
    
    
    private double P(double startT, double endT){
        return (lambda-mu)/(lambda - mu*Math.exp(-(lambda-mu)*(endT-startT)));
    }
    
    private double u(double t){
        return (lambda*(1-Math.exp(-(lambda-mu)*t)))/(lambda-mu*(Math.exp(-(lambda-mu)*t)));
    }

    public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString) {
        // TODO Auto-generated method stub
        if (result == null)
            return;
       	clearResultAndLastResult(result);

        if (tree == null)
            return;
        int n = tree.numberOfTerminalsInClade(tree.getRoot());

        /*
         * Note: currently does not refresh parameters explorer if tree changed.
         * This would be tough to do automatically, because in response to a
         * change in tree here, calculate number could then be called by the
         * explorer! Perhaps always require user request? if (lastTree != tree)
         * explorer.explorableChanged(this);
         */
        if (tree != lastTree){
            //recalculate branch times
            branchTimes = new double[n-1]; 
            MesquiteInteger counter = new MesquiteInteger(0);
            fillBranchTimes(tree,tree.getRoot(),0.0,counter);
            Arrays.sort(branchTimes);
            height = tree.tallestPathAboveNode(tree.getRoot());
            lastTree = tree;
        }
     //   Debugg.println("height " + height);
    //    Debugg.println("branchTimes array = " + DoubleArray.toString(branchTimes));
        double lik;
        double prod1 = 1.0;
        double x2 = height;
        double ux2 = u(x2); 
        for(int i = 1;i<branchTimes.length;i++){  // skip root branching at 0
            prod1*=P(branchTimes[i],height);
        }

        double prod2 = 1.0;
        for(int i=1;i<branchTimes.length;i++){
            prod2*=1-u(height-branchTimes[i]);
        }
        
        lik = Math.pow(lambda,n-2)*prod1*(1-ux2)*(1-ux2)*prod2;
      
        //lik = prod1*(1-ux2)*(1-ux2)*prod2;
        if (lik > 0) lik = -1*Math.log(lik);
        else lik = MesquiteDouble.unassigned;
        result.setValue(lik);
        if (resultString != null)
            resultString.setValue("Result is " + lik);
		saveLastResult(result);
		saveLastResultString(resultString);
   }

    public String getName() {
        return "Nee et al. Birth/Death Tree Likelihood";
    }

    public String getAuthors() {
        return "Peter E. Midford & Wayne P. Maddison";
    }

    public String getVersion() {
        return "0.1";
    }

    public String getExplanation(){
        return "Calculates likelihood of tree using birth/death models described in Nee, May & Harvey 1994";
    }

    public boolean isPrerelease(){
        return true;
    }

    /*------------------------------------------------------------------------------------------*/
    /** these methods for ParametersExplorable interface */
    public MesquiteParameter[] getExplorableParameters(){
        return parameters;
    }
    MesquiteNumber likelihood = new MesquiteNumber();

    public double calculate(MesquiteString resultString){
        lambda = lambdap.getValue();
        mu = mup.getValue();
        calculateNumber( lastTree, likelihood, resultString);
        return likelihood.getDoubleValue();
    }


    public void restoreAfterExploration() {
        // TODO Auto-generated method stub
        
    }



}
