package mesquite.distance.lib;

import mesquite.distance.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.categ.lib.*;

public abstract class DNATaxaDistFromMatrixFreq extends DNATaxaDistFromMatrix {
	MesquiteBoolean baseFreqEntireMatrix = new MesquiteBoolean(true);  //note:  Swofford uses this as true in PAUP*4.0b10
	double[] pi = new double[4];
	/*.................................................................................................................*/
	public boolean superStartJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		super.superStartJob(arguments,  condition,  commandRec,  hiredByName);
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
	 public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {
	 	if (checker.compare(this.getClass(), "Sets whether the base frequency values used in distance calculations are based upon the entire matrix (if on) or just the pair of sequences being compared (if off).", "[on; off]", commandName, "toggleBaseFreqEntireMatrix")) {
	 		baseFreqEntireMatrix.toggleValue(new Parser().getFirstToken(arguments));
	 		parametersChanged(null, commandRec);
	 }
		else
	 		return super.doCommand(commandName, arguments, commandRec, checker);
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
