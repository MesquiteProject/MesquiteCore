/* Mesquite source code.  Copyright 1997-2006 W. Maddison and D. Maddison.
Version 1.11, June 2006.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.diverse.SpExtCategCharLikelihood;


import mesquite.diverse.lib.*;
import mesquite.diverse.SpExtCategCharMLCalculator.SpExtCategCharMLCalculator;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.lib.duties.NumberForCharAndTree;

public class SpExtCategCharLikelihood extends NumberForCharAndTree {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(SpExtCategCharMLCalculator.class, getName() + "  needs a method to calculate likelihoods.",
		"The method to calculate likelihoods is arranged initially");
		e.setSuppressListing(true);
	}
	
	SpExtCategCharMLCalculator calcTask;


	MesquiteParameter e0;   //user specified extinction rate in state 0
	MesquiteParameter s0;   //user specified speciation rate in state 0
	MesquiteParameter e1;   //user specified extinction rate in state 1
	MesquiteParameter s1;   //user specified speciation rate in state 1
	MesquiteParameter t01;   //user specified transition rate from state 0 to state 1
	MesquiteParameter t10;   //user specifiedtransition rate from state 1 to state 0

	MesquiteParameter[] params;
	MesquiteParameter[] paramsCopy;
	boolean[] selected;
	boolean suspended = false;

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
		if (MesquiteThread.isScripting())
			suspended = true;
		if (!MesquiteThread.isScripting()){
			showDialog();
		}
		addMenuItem("Set Parameters...", makeCommand("setParameters", this));
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
	//	dlog.setInWizard(false);
		dlog.dispose();
	
		return ok;
	}

	public void initialize(Tree tree, CharacterDistribution charStates1, CommandRecord commandRec) {
		// TODO Auto-generated method stub

	}
	/*.................................................................................................................*/

	public Snapshot getSnapshot(MesquiteFile file) {
		final Snapshot temp = new Snapshot();
		String pLine = MesquiteParameter.paramsToScriptString(params);
		temp.addLine("suspend ");
		if (!StringUtil.blank(pLine))
			temp.addLine("setParameters " + pLine);

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
	public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {
		if (checker.compare(getClass(), "Sets rate parameters", "[double double double double double double]", commandName, "setParameters")) {
			if (StringUtil.blank(arguments)){
				if (!MesquiteThread.isScripting() && showDialog())
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
				if (changed && !MesquiteThread.isScripting())
					parametersChanged(null, commandRec); //this tells employer module that things changed, and recalculation should be requested
			}
		}
		else if (checker.compare(getClass(), "Suspends calculations", null, commandName, "suspend")) {
			suspended = true;
		}
		else if (checker.compare(getClass(), "Resumes calculations", null, commandName, "resume")) {
			suspended = false;
			parametersChanged(null, commandRec);
		}
		else if (checker.compare(getClass(), "Returns integrating module", null, commandName, "getIntegTask")) {
			return calcTask;
		}
		else
			return super.doCommand(commandName, arguments, commandRec, checker);
		return null;
	}


    /*------------------------------------------------------------------------------------------*/
	public void calculateNumber(Tree tree, CharacterDistribution charStates, MesquiteNumber result, MesquiteString resultString, CommandRecord commandRec) {
		if (result == null)
			return;
	   	clearResultAndLastResult(result);
		if (suspended)
			return;
		if (tree == null || charStates == null)
			return;
		
		paramsCopy = MesquiteParameter.cloneArray(params, paramsCopy);

		calcTask.calculateLogProbability(tree, charStates, paramsCopy, result, resultString, commandRec);
		saveLastResult(result);
		saveLastResultString(resultString);
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
	
	/*.................................................................................................................*/
 	/** returns keywords related to what the module does, for help-related searches. */ 
 	public  String getKeywords()  {
 		return "diversification birth death";
   	 }

	public boolean isPrerelease(){
		return true;
	}


}

