package mesquite.distance.lib;

import mesquite.distance.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.categ.lib.*;

public abstract class DNATaxaDistFromMatrixFreq extends DNATaxaDistFromMatrix {
	double[] pi = new double[4];
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		super.startJob( arguments,  condition,  commandRec,  hiredByName);
		for (int i = 0; i<4; i++)
			pi[i]=0.25;
		return true;
  	 }	 
	public double getBaseFreq(int i) {
		if (i>=0 && i<4)
			return pi[i];
		else 
			return 0.0;
	}
	public double getB(MCharactersDistribution observedStates, int it1, int it2){
		DNAData data = (DNAData)observedStates.getParentData();
		pi = data.getFrequencies(true, true, it1, it2);  
		double B = 0.0;
		for (int i = 0; i<4; i++)
			B += pi[i]*pi[i];
		B = 1.0-B;
		return B;
	}
}
