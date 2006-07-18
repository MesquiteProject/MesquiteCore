/* Mesquite source code.  Copyright 1997-2006 W. Maddison and D. Maddison. 
 This module copyright 2006 P. Midford and W. Maddison

Version 1.11, June 2006.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.correl.SCMCorrelation;


import pal.statistics.ChiSquareDistribution;
import pal.statistics.NormalDistribution;
import mesquite.categ.lib.CategoricalDistribution;
import mesquite.categ.lib.CategoricalHistory;
import mesquite.categ.lib.RequiresExactlyCategoricalData;
import mesquite.correl.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.lib.duties.*;

public class SCMCorrelation extends NumberFor2CharAndTree {

	MesquiteLong seed;
	long originalSeed=System.currentTimeMillis(); //0L;

	private CLogger logger;
	private int numRealizations = 10;
	private CategoricalDistribution observedStates1;
	private CategoricalDistribution observedStates2;
	NumFor2CharHistAndTree  realizationCounter;
	CharMapper mapper1, mapper2;

	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		seed = new MesquiteLong(originalSeed);
		realizationCounter = (NumFor2CharHistAndTree)hireEmployee(commandRec, NumFor2CharHistAndTree.class, "Correlation assessment on stochastic-character-mapping realizations");
		if (realizationCounter == null)
			return sorry(commandRec, "Sorry, realization counter could not be started");
		mapper1 = (CharMapper)hireNamedEmployee(commandRec, CharMapper.class, "#StochCharMapper");
		if (mapper1 == null)
			return sorry(commandRec, getName() + " couldn't start because no mapping module was obtained.");
		mapper2 = (CharMapper)hireNamedEmployee(commandRec, CharMapper.class, "#StochCharMapper");
		if (mapper2 == null)
			return sorry(commandRec, getName() + " couldn't start because no mapping module was obtained.");
		addMenuItem("Realizations examined (SCM Correlation)...", makeCommand("setNumRealizations", this));
		addMenuItem("Set Seed (SCM Correlation)...", makeCommand("setSeed", this));
		if (getEmployer() instanceof CLogger)
			setLogger((CLogger)getEmployer());
		return true;
	}
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresExactlyCategoricalData();
	}

	public void setLogger(CLogger logger){
		this.logger = logger;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setSeed " + originalSeed); 
		temp.addLine("setNumRealizations " + numRealizations);
		return temp;
	}
	public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the random number seed to that passed", "[long integer seed]", commandName, "setSeed")) {
			long s = MesquiteLong.fromString(parser.getFirstToken(arguments));
			if (!MesquiteLong.isCombinable(s) && !commandRec.scripting()){
				s = MesquiteLong.queryLong(containerOfModule(), "Random number seed", "Enter an integer value for the random number seed for character evolution simulation", originalSeed);
			}
			if (MesquiteLong.isCombinable(s)){
				originalSeed = s;
				seed.setValue(originalSeed);
				if (!commandRec.scripting()) parametersChanged(null, commandRec); //?
			}
			return null;
		}
		else if (checker.compare(this.getClass(), "Sets the number of realizations examined for the quadrats method", "[number]", commandName, "setNumRealizations")) {
			MesquiteInteger pos = new MesquiteInteger();
			int newNum= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newNum))
				newNum = MesquiteInteger.queryInteger(containerOfModule(), "SCM realizations", "Number of realizations examined for the calculating correlation:", numRealizations, 0, MesquiteInteger.infinite);
			if (newNum>0  && newNum!=numRealizations) {
				numRealizations = newNum;
				if (!commandRec.scripting()){
					parametersChanged(null, commandRec);
				}
			}
		}
		//        else if (checker.compare(this.getClass(),"(Currently) saves surface plot of 2 parameters as text file","",commandName,"showSurface")){
//		return null;
//		}
		else
			return super.doCommand(commandName, arguments, commandRec, checker);
		return null;
	}


	public void initialize(Tree tree, CharacterDistribution charStates1, CharacterDistribution charStates2, CommandRecord commandRec) {
		if (!(charStates1 instanceof CategoricalDistribution ||
				charStates2 instanceof CategoricalDistribution)) {

		}
		observedStates1 = (CategoricalDistribution)charStates1;
		observedStates2 = (CategoricalDistribution)charStates2;

	}


	public  void calculateNumber(Tree tree, CharacterDistribution charStates1, CharacterDistribution charStates2, MesquiteNumber result, MesquiteString resultString, CommandRecord commandRec){
		if (result == null)
			return;
		result.setToUnassigned();
		if (tree == null || charStates1 == null || charStates2 == null)
			return;
		if (!(charStates1 instanceof CategoricalDistribution) || !(charStates2 instanceof CategoricalDistribution)){
			if (resultString != null)
				resultString.setValue("SCM correlation can't be calculated because one or both of the character are not categorical");
			return;
		}
		observedStates1 = (CategoricalDistribution)charStates1;
		observedStates2 = (CategoricalDistribution)charStates2;
		if (observedStates1.getMaxState() > 1 ||
				observedStates2.getMaxState() > 1) {
			if (resultString != null)
				resultString.setValue("SCM correlation can't be calculated because one or both of the characters are not binary");
			return;
		}
		MesquiteNumber correl = new MesquiteNumber();
		MesquiteNumber sum = new MesquiteNumber(0.0);
		MesquiteNumber logSum = new MesquiteNumber(0.0);
		MesquiteNumber chiResult = null;
		MesquiteNumber quantileSum = new MesquiteNumber(0.0);
		MesquiteNumber zResult = null;
		mapper1.setObservedStates( tree,  observedStates1,  commandRec);
		int numMaps1 = mapper1.getNumberOfMappings(commandRec);
		if (!MesquiteInteger.isCombinable(numMaps1))
			numMaps1 = numRealizations;  
		mapper2.setObservedStates( tree,  observedStates2,  commandRec);

		int numMaps2 = mapper2.getNumberOfMappings(commandRec);
		if (!MesquiteInteger.isCombinable(numMaps2))
			numMaps2 = numRealizations;  
		CategoricalHistory history1 =(CategoricalHistory)observedStates1.adjustHistorySize(tree, null);
		CategoricalHistory history2 =(CategoricalHistory)observedStates2.adjustHistorySize(tree, null);
		int n = 0;
		for (int i=0; i<MesquiteInteger.minimum(numMaps1, numMaps2); i++){
			mapper1.getMapping( i,  history1, null,  commandRec); 
			mapper2.getMapping( i,  history2, null,  commandRec); 
			realizationCounter.calculateNumber(tree, history1, history2, correl, resultString, commandRec);
			if (correl.isCombinable()){
				n++;
				sum.add(correl);
				logSum.add(Math.log(correl.getDoubleValue()));
				quantileSum.add(NormalDistribution.quantile(correl.getDoubleValue(),0.0,1.0));
			}
			
		}
		if (n == 0){
			sum.setToUnassigned();
			chiResult.setToUnassigned();
			quantileSum.setToUnassigned();
		}
		else{
			sum.divideBy(n);
			double chiSum = -2*logSum.getDoubleValue();
			if (MesquiteDouble.isInfinite(chiSum))
				chiResult = new MesquiteNumber(0);
			else
				chiResult = new MesquiteNumber(1-ChiSquareDistribution.cdf(chiSum,2*n));
			zResult = new MesquiteNumber(NormalDistribution.cdf((quantileSum.getDoubleValue()/Math.sqrt((double)n)),0.0,1.0));
		}
		if (zResult != null)
		result.setValue(zResult);
		if (resultString != null)
			resultString.setValue(" n = " + n);
		if (logger!= null){ 
			logger.cwrite("\n\nCorrelation Result (Mean of p-values) : \n " + sum);
			logger.cwrite("\nSCM Correlation Result (Fisher's method) : \n" + chiResult);
			logger.cwrite("\nSCM Correlation Result (Z-transform method) : \n" + zResult);
			logger.cwrite("\n" + resultString);
		}
	}

	/*.................................................................................................................*/


	public String getVeryShortName() {
		return "SCM Correlation test";
	}
	public String getNameAndParameters() {
		if (realizationCounter == null)
			return getName();
		return realizationCounter.getNameAndParameters();
	}

	public String getAuthors() {
		return "Peter E. Midford & Wayne P. Maddison";
	}

	public String getVersion() {
		return "0.1";
	}

	public String getName() {
		return "SCM Correlation test";
	}

	public String getExplanation(){
		return "A correlation test for two categorical characters using stochastic character mapping";
	}

	public boolean isPrerelease(){
		return true;
	}

}


