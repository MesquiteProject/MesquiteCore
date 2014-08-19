/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.stochchar.lib;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.cont.lib.*;

/* ======================================================================== */
public class BrownianMotionModel  extends ProbabilityContCharModel {
	Random rng = new Random(System.currentTimeMillis());
	double rate = 1.0;
	long seedSet = 0;
	public BrownianMotionModel (String name, Class dataClass) {
		super(name, dataClass);
	}
 	/*.................................................................................................................*/
	/** Randomly generates according to model an end state on branch from beginning states*/
	public double evolveState (double beginState, Tree tree, int node){
		return beginState + rate* rng.nextGaussian()*Math.sqrt(tree.getBranchLength(node, 1.0));   //pre-1.05 this failed to take the sqrt!!!!
	}
 	/*.................................................................................................................*/
	/** Returns (possibly by randomly generating) according to model an ancestral state for root of tree*/
	public double getRootState (Tree tree){
		return 0;  //todo: stochastic?
	}
 	/*.................................................................................................................*/
	public void fromString (String description, MesquiteInteger stringPos, int format) {
   		ParseUtil.getToken(description, stringPos); //eating token "rate"
   		rate =  MesquiteDouble.fromString(description, stringPos);
	}
	
 	/*.................................................................................................................*/
	public String getParameters() {
		return "rate " + MesquiteDouble.toString(rate);
	}
 	/*.................................................................................................................*/
	public String getNexusSpecification () {
		return "rate " + MesquiteDouble.toString(rate);
	}
 	/*.................................................................................................................*/
	public void setRate(double rate){
		this.rate = rate;
		notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));
	}
 	/*.................................................................................................................*/
	public double getRate(){
		return rate;
	}
	MesquiteInteger pos = new MesquiteInteger();
 	/*.................................................................................................................*/
 	/** Performs command (for Commandable interface) */
   	public Object doCommand(String commandName, String arguments, CommandChecker checker){
    	 	if (checker.compare(this.getClass(), "Sets the instantaneous rate of change in the model", "[rate of change; must be > 0]", commandName, "setRate")) {
    	 		pos.setValue(0);
			double newRate = MesquiteDouble.fromString(arguments, pos);
			double a = getRate();
			if (!MesquiteDouble.isCombinable(newRate)) {
				newRate= MesquiteDouble.queryDouble(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Set Rate", "Rate of change:", a);
    	 		}
    	 		if (newRate>=0  && newRate!=a && MesquiteDouble.isCombinable(newRate)) {
    	 			setRate(newRate);
    	 		}
		}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
 	}
	public CharacterModel cloneModelWithMotherLink(CharacterModel formerClone){
		BrownianMotionModel bmm = new BrownianMotionModel(name, getStateClass());
		completeDaughterClone(formerClone, bmm);
		return bmm;
	}
 	/* copy information from this to model passed (used in cloneModelWithMotherLink to ensure that superclass info is copied); should call super.copyToClone(pm) */
	public void copyToClone(CharacterModel md){
		if (md == null || !(md instanceof BrownianMotionModel))
			return;
		BrownianMotionModel model = (BrownianMotionModel)md;
		model.setRate(rate);
		super.copyToClone(md);
	}
	public boolean isFullySpecified(){
		return rate != MesquiteDouble.unassigned;
	}
	public void setSeed(long seed){
		seedSet = seed;
		rng.setSeed(seed);
	}
	
	public long getSeed(){
		return rng.nextLong();
	}
	
	/** return an explanation of the model. */
	public String getExplanation (){
		return "A stochastic model in which expected change is distributed normally with mean 0 and variance proportional to branch length times the rate.  The current rate is " + MesquiteDouble.toString(rate);
	}
	public String getNEXUSClassName(){
		return "Brownian";
	}
	public String getModelTypeName(){
		return "Brownian motion model";
	}
}

