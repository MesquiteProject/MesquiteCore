package mesquite.diverse.DiversificationLikeCategChar;

import mesquite.lib.CommandRecord;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.Tree;
import mesquite.diverse.lib.*;
import mesquite.diverse.IntegLikeCateg.IntegLikeCateg;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.lib.duties.NumberForCharAndTree;
import mesquite.lib.duties.ParametersExplorer;

    public class DiversificationLikeCategChar extends NumberForCharAndTree implements ParametersExplorable {

        DEQNumSolver solver;
        CladeDiversificationModel speciesModel;
        IntegLikeCateg calcTask;

        // hooks for capturing context for table dump.
        Tree lastTree;
        CharacterDistribution lastCharDistribution;
        
        boolean conditionBySurvival = false;

        double r0 = 0.001;   //user specified diversification rate in state 0
        double a0 = 0.001;   //user specified extinction/speciation ratio in state 0
        double r1 = 0.005;   //user specified diversification rate in state 1
        double a1 = 0.001;   //user specified extinction/speciation ratio in state 1
        double t01 = 0.01;   //user specified transition rate from state 0 to state 1
        double t10 = 0.01;   //user specifiedtransition rate from state 1 to state 0

        MesquiteParameter r0p = new MesquiteParameter();
        MesquiteParameter r1p = new MesquiteParameter();
        MesquiteParameter a0p = new MesquiteParameter();
        MesquiteParameter a1p = new MesquiteParameter();
        MesquiteParameter t01p = new MesquiteParameter();
        MesquiteParameter t10p = new MesquiteParameter();
        MesquiteParameter[] parameters;
        ParametersExplorer explorer;

        public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
            calcTask = (IntegLikeCateg)hireEmployee(commandRec, IntegLikeCateg.class, "Integrating Likelihood");
            if (calcTask == null)
                return sorry(commandRec, getName() + " couldn't start because no integrating likelihood calculator module obtained.");

            solver = new RK4Solver();
            addMenuItem("Set State 0 Diversification Rate...", makeCommand("setR0", this));
            addMenuItem("Set State 0 Extinction/Speciation Ratio...", makeCommand("setA0", this));
            addMenuItem("Set State 1 Diversification Rate...", makeCommand("setR1", this));
            addMenuItem("Set State 1 Extinction/Speciation Ratio...", makeCommand("setA1", this));
            addMenuItem("Set 0 to 1 Transition Rate...", makeCommand("setT01", this));
            addMenuItem("Set 1 to 0 Transition Rate...", makeCommand("setT10", this));
            addMenuItem("-", null);
            addMenuItem("Show Parameters Explorer", makeCommand("showParamExplorer",this));
            addMenuItem("Write table to console", makeCommand("writeTable",this));
            addMenuItem("Write code for R to console", makeCommand("writeForExternalApp",this));
            speciesModel = new CladeDiversificationModel(a0, r0, a1, r1, t01, t10);

            //following is for the parameters explorer
            r0p.setName("r0");
            r0p.setExplanation("Rate of diversification with state 0");
            r0p.setMinimumAllowed(0);
            r0p.setMaximumAllowed(MesquiteDouble.infinite);
            r0p.setMinimumSuggested(0.0001);
            r0p.setMaximumSuggested(0.1);
            r0p.setValue(r0);
            r1p.setName("r1");
            r1p.setExplanation("Rate of diversification with state 1");
            r1p.setMinimumSuggested(0.0001);
            r1p.setMaximumSuggested(0.1);
            r1p.setMinimumAllowed(0);
            r1p.setMaximumAllowed(MesquiteDouble.infinite);
            r1p.setValue(r1);
            a0p.setName("a0");
            a0p.setExplanation("Ratio of extinction/speciation with state 0");
            a0p.setMinimumSuggested(0.0001);
            a0p.setMaximumSuggested(0.1);
            a0p.setMinimumAllowed(0);
            a0p.setMaximumAllowed(0.9999999);
            a0p.setValue(a0);
            a1p.setName("a1");
            a1p.setExplanation("Ratio of extinction/speciation with state 1");
            a1p.setMinimumSuggested(0.0001);
            a1p.setMaximumSuggested(0.1);
            a1p.setMinimumAllowed(0);
            a1p.setMaximumAllowed(0.9999999);
            a1p.setValue(a1);
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
            parameters = new MesquiteParameter[]{r0p, r1p, a0p, a1p, t01p, t10p};
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

                temp.addLine("setR0 " + MesquiteDouble.toString(r0));
                temp.addLine("setR1 " + MesquiteDouble.toString(r1));
                temp.addLine("setA0 " + MesquiteDouble.toString(a0));
                temp.addLine("setA1 " + MesquiteDouble.toString(a1));
                temp.addLine("setT01 " + MesquiteDouble.toString(t01));
                temp.addLine("setT10 " + MesquiteDouble.toString(t10));
                if (explorer != null)
                    temp.addLine("showParamExplorer ", explorer);
            
            return temp;
        }
        /*.................................................................................................................*/
        /*  the main command handling method.  */
        public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {
            // Should be removed when debugged
            double [] testvals = { 1E-11,1E-10,1E-9,1E-8,1E-7,5E-7,1E-6,2E-6,1E-5,1E-4,5E-4,1E-3,5E-3,1E-2,2E-2,5E-2,1E-1,2E-1,5E-01};
            if (checker.compare(getClass(), "Sets diversification rate in state 0", "[double]", commandName, "setR0")) {
                double newR0 = MesquiteDouble.fromString(parser.getFirstToken(arguments));
                if (!MesquiteDouble.isCombinable(newR0) && !commandRec.scripting())
                    newR0 = MesquiteDouble.queryDouble(containerOfModule(), "r0", "Instantaneous diversification rate in state 0", r0);
                if (MesquiteDouble.isCombinable(newR0) && newR0 >=0 && newR0 != r0){
                    r0 = newR0; //change mode
                    if (speciesModel != null)
                        speciesModel.setR0(r0);
                    else
                        speciesModel = new CladeDiversificationModel(a0,r0,a1,r1,t01,t10);
                    parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
                }
            }
            else if (checker.compare(getClass(), "Sets extinction/speciation ratio in state 0", "[double]", commandName, "setA0")) {
                double newA0 = MesquiteDouble.fromString(parser.getFirstToken(arguments));
                if (!MesquiteDouble.isCombinable(newA0) && !commandRec.scripting())
                    newA0 = MesquiteDouble.queryDouble(containerOfModule(), "s0", "Instantaneous speciation rate in state 0", a0);
                if (MesquiteDouble.isCombinable(newA0) && newA0 >=0 && newA0 < 1 && newA0 != a0){
                    a0 = newA0; //change mode
                    if (speciesModel != null)
                        speciesModel.setA0(a0);
                    else
                        speciesModel = new CladeDiversificationModel(a0,r0,a1,r1,t01,t10);
                    parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
                }
            }
            else if (checker.compare(getClass(), "Sets diversification rate in state 1", "[double]", commandName, "setR1")) {
                double newR1 = MesquiteDouble.fromString(parser.getFirstToken(arguments));
                if (!MesquiteDouble.isCombinable(newR1) && !commandRec.scripting())
                    newR1 = MesquiteDouble.queryDouble(containerOfModule(), "e1", "Instantaneous diversification rate in state 1", r1);
                if (MesquiteDouble.isCombinable(newR1) && newR1 >=0 && newR1 != r1){
                    r1 = newR1; //change mode
                    if (speciesModel != null)
                        speciesModel.setR1(r1);
                    else
                        speciesModel = new CladeDiversificationModel(a0,r0,a1,r1,t01,t10);
                    parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
                }
            }
            else if (checker.compare(getClass(), "Sets extinction/speciation ratio in state 1", "[double]", commandName, "setS1")) {
                double newA1 = MesquiteDouble.fromString(parser.getFirstToken(arguments));
                if (!MesquiteDouble.isCombinable(newA1) && !commandRec.scripting())
                    newA1 = MesquiteDouble.queryDouble(containerOfModule(), "a1", "Instantaneous extinction/speciation ratio in state 1", a1);
                if (MesquiteDouble.isCombinable(newA1) && newA1 >=0 && newA1 < 1 && newA1 != a1){
                    a1 = newA1; //change mode
                    if (speciesModel != null)
                        speciesModel.setA1(a1);
                    else
                        speciesModel = new CladeDiversificationModel(a0,r0,a1,r1,t01,t10);
                    parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
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
                        speciesModel = new CladeDiversificationModel(a0,r0,a1,r1,t01,t10);
                    parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
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
                        speciesModel = new CladeDiversificationModel(a0,r0,a1,r1,t01,t10);
                    parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
                }
            }
            else if (checker.compare(getClass(), "Writes table to console", "", commandName, "showParamExplorer")) {
                explorer = (ParametersExplorer)hireEmployee(commandRec, ParametersExplorer.class, "Parameters explorer");
                if (explorer == null)
                    return null;
                explorer.setExplorable(this, commandRec);
                return explorer;
            }
            else if (checker.compare(getClass(), "Writes table to console", "", commandName, "writeTable")) {
                MesquiteMessage.println("a0 = " + a0);
                MesquiteMessage.println("a1 = " + a1);
                MesquiteMessage.println("t01 = " + t01);
                MesquiteMessage.println("t10 = " + t10);
                MesquiteMessage.println("r1/r0");
                MesquiteNumber savedResult = new MesquiteNumber();
                MesquiteMessage.print("           ");
                for(int j=0;j<testvals.length;j++)
                    MesquiteMessage.print(MesquiteDouble.toFixedWidthString(testvals[j],10)+ " ");
                MesquiteMessage.println("");
                for(int i=0;i<testvals.length;i++){
                    speciesModel.setR1(testvals[i]);
                    MesquiteMessage.print(MesquiteDouble.toFixedWidthString(testvals[i],10) + " ");
                    for(int j=0;j<testvals.length;j++){
                        speciesModel.setR0(testvals[j]);
                        calculateNumber(lastTree,lastCharDistribution,savedResult,null,commandRec);
                        MesquiteMessage.print(MesquiteDouble.toFixedWidthString(savedResult.getDoubleValue(),10) + " ");
                    }
                    MesquiteMessage.println("");
                }
            }
            else if (checker.compare(getClass(), "Writes text for external app to console", "", commandName, "writeForExternalApp")) {
                MesquiteMessage.println("a0 = " + a0);
                MesquiteMessage.println("a1 = " + a1);
                MesquiteMessage.println("t01 = " + t01);
                MesquiteMessage.println("t10 = " + t10);
                MesquiteMessage.println("r1/r0");
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
                    speciesModel.setR1(testvals[i]);
                    MesquiteMessage.print("z[" + (i+1) + ",] <- c(");
                    for(int j=0;j<testvals.length;j++){
                        speciesModel.setR0(testvals[j]);
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

        /*------------------------------------------------------------------------------------------*/
        /** these methods for ParametersExplorable interface */
        public MesquiteParameter[] getExplorableParameters(){
            return parameters;
        }
        MesquiteNumber likelihood = new MesquiteNumber();
        public double calculate(MesquiteString resultString, CommandRecord commandRec){
            speciesModel.setR1(r1p.getValue());
            speciesModel.setR0(r0p.getValue());
            speciesModel.setA1(a1p.getValue());
            speciesModel.setA0(a0p.getValue());
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
                speciesModel = new CladeDiversificationModel(a0,r0,a1,r1,t01,t10);
            calcTask.calculateLogProbability(tree, speciesModel, conditionBySurvival, solver, charStates, resultString, result, commandRec);

        }
        /*------------------------------------------------------------------------------------------*/


        public String getName() {
            // TODO Auto-generated method stub
            return "Diversification Likelihood (Categ. Char.)";
        }

        public String getAuthors() {
            return "Peter E. Midford, Sarah P. Otto & Wayne P. Maddison";
        }

        public String getVersion() {
            return "0.1";
        }

        public String getExplanation(){
            return "Calculates likelihoods using a diversification model whose probabilities depend on the state of a single categorical character";
        }

        public boolean isPrerelease(){
            return true;
        }

        public void restoreAfterExploration() {
            // TODO Auto-generated method stub
            
        }


    }
