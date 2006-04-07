package mesquite.distance.lib;

import mesquite.distance.lib.*;
import mesquite.lib.*;

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
	public double getB(){
		double B = 0.0;
		for (int i = 0; i<4; i++)
			B += pi[i]*pi[i];
		B = 1-B;
		return B;
	}
}
