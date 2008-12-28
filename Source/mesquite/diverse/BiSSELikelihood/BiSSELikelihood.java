/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison.
Version 2.6, January 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.diverse.BiSSELikelihood;


import mesquite.categ.lib.CategoricalDistribution;
import mesquite.categ.lib.RequiresExactlyCategoricalData;
import mesquite.diverse.lib.*;
import mesquite.diverse.BiSSELikelihoodCalculator.BiSSELikelihoodCalculator;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.stochchar.lib.MargLikelihoodForModel;

public class BiSSELikelihood extends NumForCharAndTreeDivers {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(BiSSELikelihoodCalculator.class, getName() + "  needs a method to calculate likelihoods.",
		"The method to calculate likelihoods is arranged initially");
		e.setSuppressListing(true);
	}

	BiSSELikelihoodCalculator calcTask;


	MesquiteParameter mu0;   //user specified extinction rate in state 0
	MesquiteParameter lambda0;   //user specified speciation rate in state 0
	MesquiteParameter mu1;   //user specified extinction rate in state 1
	MesquiteParameter lambda1;   //user specified speciation rate in state 1
	MesquiteParameter q01;   //user specified transition rate from state 0 to state 1
	MesquiteParameter q10;   //user specifiedtransition rate from state 1 to state 0

	MesquiteParameter[] params;
	MesquiteParameter[] paramsCopy;
	boolean[] selected;
	boolean suspended = false;
	MesquiteString reportModeName;
	StringArray reportModes;
	int reportMode = 0;

	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		calcTask = (BiSSELikelihoodCalculator)hireEmployee(BiSSELikelihoodCalculator.class, "Calculator for BiSSE Likelihood");
		if (calcTask == null)
			return sorry(getName() + " couldn't start because no integrating likelihood calculator module obtained.");
		double def = MesquiteDouble.unassigned;
		//following is for the parameters explorer
		lambda0 = new MesquiteParameter("lambda0", "Rate of speciation with state 0", def, 0, MesquiteDouble.infinite, 0.000, 1);
		lambda1 = new MesquiteParameter("lambda1", "Rate of speciation with state 1", def, 0, MesquiteDouble.infinite, 0.000, 1);
		mu0 = new MesquiteParameter("mu0", "Rate of extinction with state 0", def, 0, MesquiteDouble.infinite, 0.000, 1);
		mu1 = new MesquiteParameter("mu1", "Rate of extinction with state 1", def, 0, MesquiteDouble.infinite, 0.000, 1);
		q01 = new MesquiteParameter("q01", "Rate of 0->1 changes", def, 0, MesquiteDouble.infinite, 0.000, 1);
		q10 = new MesquiteParameter("q10", "Rate of 1->0 changes", def, 0, MesquiteDouble.infinite, 0.000, 1);
		params = new MesquiteParameter[]{lambda0, lambda1, mu0, mu1, q01, q10};
		reportModes = new StringArray(7);  
		reportModes.setValue(0, "Likelihood");  //the strings passed will be the menu item labels
		reportModes.setValue(1, lambda0.getName());  //the strings passed will be the menu item labels
		reportModes.setValue(2, lambda1.getName());  //the strings passed will be the menu item labels
		reportModes.setValue(3, mu0.getName());  //the strings passed will be the menu item labels
		reportModes.setValue(4, mu1.getName());  //the strings passed will be the menu item labels
		reportModes.setValue(5, q01.getName());  //the strings passed will be the menu item labels
		reportModes.setValue(6, q10.getName());  //the strings passed will be the menu item labels
		reportModeName = new MesquiteString(reportModes.getValue(reportMode));  //this helps the menu keep track of checkmenuitems
		MesquiteSubmenuSpec mss = addSubmenu(null, "Report BiSSE Results As", makeCommand("setReportMode", this), reportModes); 
		mss.setSelected(reportModeName);

		if (MesquiteThread.isScripting()  && !MesquiteThread.actingAsLibrary)
			suspended = true;
		if (!MesquiteThread.isScripting()){
			if (!showDialog())
				return sorry(getName() + " couldn't start because parameters not specified.");
		}
		addMenuItem("Set Parameters...", makeCommand("setParameters", this));
		addMenuItem("Recalculate", makeCommand("resume", this));
		
		return true;
	}
 	public CompatibilityTest getCompatibilityTest(){
		return new RequiresExactlyCategoricalData();
	}
	/*.................................................................................................................*/
	boolean showDialog(){
		if (params == null)
			return false;
		String s = whatIsMyPurpose();
		if (!StringUtil.blank(s))
			s = "BiSSE Parameters for " + s;
		else
			s = "BiSSE Parameters";
		ParametersDialog dlog = new ParametersDialog(containerOfModule(), "BiSSE Parameters", s, params, null, 2, 2, false);
		dlog.appendToHelpString("Parameters for BiSSE model.  Indicate the rates of speciation when in state 0 (lambda0), speciation when in state 1 (lambda1), ");
		dlog.appendToHelpString("rates of extinction when in state 0 (mu0), extinction when in state 1 (mu1), ");
		dlog.appendToHelpString("rates of character change 0 to 1(q01), and rates of character change 1 to 0 (q10). ");
		dlog.completeAndShowDialog(true);

		boolean ok = (dlog.query()==0);
		if (ok) 
			dlog.acceptParameters();
		//	dlog.setInWizard(false);
		dlog.dispose();

		return ok;
	}

	public void initialize(Tree tree, CharacterDistribution charStates1) {
		// TODO Auto-generated method stub

	}
	/*.................................................................................................................*/

	public Snapshot getSnapshot(MesquiteFile file) {
		final Snapshot temp = new Snapshot();
		String pLine = MesquiteParameter.paramsToScriptString(params);
		temp.addLine("suspend ");
		if (!StringUtil.blank(pLine))
			temp.addLine("setParameters " + pLine);
		temp.addLine("setReportMode " + ParseUtil.tokenize(reportModes.getValue(reportMode)));

		temp.addLine("getIntegTask ", calcTask);
		temp.addLine("resume ");
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
		if ((MesquiteDouble.isUnassigned(newValue) ||  newValue >=0) && newValue != p.getValue()){
			p.setValue(newValue); //change mode
			return true;
		}
		return false;
	}

	/*.................................................................................................................*/
	/*  the main command handling method.  */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(getClass(), "Sets rate parameters", "[double double double double double double]", commandName, "setParameters")) {
			if (StringUtil.blank(arguments)){
				if (!MesquiteThread.isScripting() && showDialog())
					parametersChanged();
			}
			else {
				parser.setString(arguments);
				boolean changed =  setParam(lambda0, params, parser);
				boolean more = setParam(lambda1, params, parser);
				changed = changed || more;
				more = setParam(mu0, params, parser);
				changed = changed || more;
				more = setParam(mu1, params, parser);
				changed = changed || more;
				more = setParam(q01, params, parser);
				changed = changed || more;
				more = setParam(q10, params, parser);
				changed = changed || more;
				if (changed && !MesquiteThread.isScripting())
					parametersChanged(); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(getClass(), "Sets the report mode", null, commandName, "setReportMode")) {
			if (getHiredAs() == MargLikelihoodForModel.class)
				return null;
			String name = parser.getFirstToken(arguments); //get argument passed of option chosen
			int newMode = reportModes.indexOf(name); //see if the option is recognized by its name
			if (newMode >=0 && newMode!=reportMode){
				reportMode = newMode; //change mode
				reportModeName.setValue(reportModes.getValue(reportMode)); //so that menu item knows to become checked
				if (!MesquiteThread.isScripting())
					parametersChanged(); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(getClass(), "Suspends calculations", null, commandName, "suspend")) {
			suspended = true;
		}
		else if (checker.compare(getClass(), "Resumes calculations", null, commandName, "resume")) {
			suspended = false;
			parametersChanged();
		}
		else if (checker.compare(getClass(), "Returns integrating module", null, commandName, "getIntegTask")) {
			return calcTask;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}


	/*------------------------------------------------------------------------------------------*/
	public void calculateNumber(Tree tree, CharacterDistribution charStates, MesquiteNumber result, MesquiteString resultString) {
		if (result == null)
			return;
		clearResultAndLastResult(result);
		if (resultString != null)
			resultString.setValue("Calculation not completed");
		if (suspended)
			return;
		if (tree == null || charStates == null)
			return;
		if (!CategoricalDistribution.isBinaryNoMissing(charStates, tree)){
			if (resultString!=null)
	            resultString.setValue(getName() + " unassigned because the character is not binary or has missing data");
			return;
		}

		paramsCopy = MesquiteParameter.cloneArray(params, paramsCopy);

		calcTask.calculateLogProbability(tree, charStates, paramsCopy, result, resultString);
		if (reportMode >0) {
			result.setValue(paramsCopy[reportMode-1].getValue());
			if (resultString != null)
				resultString.setValue(reportModeName.getValue() + ": " + paramsCopy[reportMode-1] + "; " + resultString);
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	}

 	public boolean returnsMultipleValues(){
  		return true;
  	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}

	/*------------------------------------------------------------------------------------------*/
	public String getName() {
		return "BiSSE Speciation/Extinction Likelihood";
	}
	
	public String getVeryShortName(){
		if (reportMode>0)
			return "BiSSE Likelihood (" + reportModes.getValue(reportMode) + ")";
		return "BiSSE Likelihood";
	}

	public String getAuthors() {
		return "Peter E. Midford & Wayne P. Maddison";
	}

	public String getVersion() {
		return "1.0";
	}

	public String getExplanation(){
		return "Calculates likelihood with a tree of a species diversification model whose speciation and extinction rates depend on the state of a binary character (BiSSE model, Maddison, Midford & Otto, 2007).";
	}

	/*.................................................................................................................*/
	/** returns keywords related to what the module does, for help-related searches. */ 
	public  String getKeywords()  {
		return "diversification birth death";
	}

	public boolean isPrerelease(){
		return false;
	}


}

