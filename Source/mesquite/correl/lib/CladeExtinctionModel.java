package mesquite.correl.lib;


public class CladeExtinctionModel implements DESystem {
	
	
	private double e0;   //extinction rate in state 0
	private double s0;   //speciation rate in state 0
	private double e1;   //extinction rate in state 1
	private double s1;   //speciation rate in state 1
	private double t01;  //transition rate from state 0 to state 1
	private double t10;  //transition rate from state 1 to state 0
	

	public CladeExtinctionModel(double e0, double s0, double e1, double s1, double t01, double t10){
		this.e0 = e0;
		this.s0 = s0;
		this.e1 = e1;
		this.s1 = s1;
		this.t01 = t01;
		this.t10 = t10;
	}

	
	/*
	 *  The probability values are passed the probs array in the following order
	 *  probs[0] = E0 = extinction probability in lineage starting at t in the past at state 0
	 *  probs[1] = E1 = extinction probability in lineage starting at t in the past at state 1
	 *  probs[2] = P0 = probability of explaining the data, given system is in state 0 at time t
	 *  probs[3] = P1 = probability of explaining the data, given system in in state 1 at time t
	 * @see mesquite.correl.lib.DESystem#calculateDerivative(double, double[])
	 */
	
	public double[]calculateDerivative(double t,double probs[],double[] result){
		// for clarity
		double extProb0 = probs[0];
		double extProb1 = probs[1];
		double dataProb0 = probs[2];
		double dataProb1 = probs[3];
		result[0] = -(e0+t01+s0)*extProb0 + s0*extProb0*extProb0 + e0 + t01*extProb1;
		result[1] = -(e1+t10+s1)*extProb1 + s1*extProb1*extProb1 + e1 + t10*extProb0;
		result[2] = -(e0+t01+s0)*dataProb0 + s0*extProb0*dataProb0 + t01*dataProb1;
		result[3] = -(e1+t10+s1)*dataProb1 + s1*extProb1*dataProb1 + t10*dataProb0;
		return result;
	}

	public void resetParameters(double e0, double s0, double e1, double s1, double t01, double t10){
		this.e0 = e0;
		this.s0 = s0;
		this.e1 = e1;
		this.s1 = s1;
		this.t01 = t01;
		this.t10 = t10;
	}
	
	
}
