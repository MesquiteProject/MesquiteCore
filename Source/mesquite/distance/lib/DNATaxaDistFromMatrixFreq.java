/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */package mesquite.distance.lib;

import mesquite.distance.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.categ.lib.*;

public abstract class DNATaxaDistFromMatrixFreq extends DNATaxaDistFromMatrix {
	MesquiteBoolean baseFreqEntireMatrix = new MesquiteBoolean(true);  //note:  Swofford uses this as true in PAUP*4.0b10
	double[] pi = new double[4];
	/*.................................................................................................................*/
	public boolean superStartJob(String arguments, Object condition, boolean hiredByName) {
		super.superStartJob(arguments,  condition,  hiredByName);
		for (int i = 0; i<4; i++)
			pi[i]=0.25;
		addCheckMenuItemToSubmenu(null,distParamSubmenu, "Base Freq on Entire Matrix", MesquiteModule.makeCommand("toggleBaseFreqEntireMatrix", this), baseFreqEntireMatrix);
		return true;
  	 }	 
	/*.................................................................................................................*/
	 public Snapshot getSnapshot(MesquiteFile file) {
	 	Snapshot snapshot = new Snapshot();
	 	snapshot.addLine("toggleBaseFreqEntireMatrix  " + baseFreqEntireMatrix.toOffOnString());
	 	return snapshot;
	 }
		/*.................................................................................................................*/
		public String getParameters(){
			String s = super.getParameters();
			if (getBaseFreqEntireMatrix())
				s+= " Base frequencies estimated over entire matrix.";
			else
				s+= " Base frequencies estimated over each pair of sequences separately.";
			return s;
		}
	 /*.................................................................................................................*/
	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
	 	if (checker.compare(this.getClass(), "Sets whether the base frequency values used in distance calculations are based upon the entire matrix (if on) or just the pair of sequences being compared (if off).", "[on; off]", commandName, "toggleBaseFreqEntireMatrix")) {
	 		baseFreqEntireMatrix.toggleValue(new Parser().getFirstToken(arguments));
	 		parametersChanged();
	 }
		else
	 		return  super.doCommand(commandName, arguments, checker);
	 	return null;
	 }
		/*.................................................................................................................*/
	public double getBaseFreq(int i) {
		if (i>=0 && i<4)
			return pi[i];
		else 
			return 0.0;
	}
	public boolean getBaseFreqEntireMatrix() {
		return baseFreqEntireMatrix.getValue();
	}
	public double[] getPi(MCharactersDistribution observedStates, int it1, int it2){
		CharacterData data = observedStates.getParentData();
		if (!(data instanceof DNAData))
			return null;
		return ((DNAData)data).getFrequencies(true, true, it1, it2);  
	}
}
