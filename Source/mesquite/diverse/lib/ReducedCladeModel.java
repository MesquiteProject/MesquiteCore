package mesquite.diverse.lib;

import mesquite.lib.MesquiteDouble;

public class ReducedCladeModel implements DESpeciationSystemCateg {

    private double e;   //extinction rate in state 0
    private double s;   //speciation rate in state 0
 
    public ReducedCladeModel(double e, double s){
        this.e = e;
        this.s = s;
    }

    
    /*
     *  The probability values are passed the probs array in the following order
     *  probs[0] = E0 = extinction probability in lineage starting at t in the past at state 0
     *  probs[1] = P0 = probability of explaining the data, given system is in state 0 at time t
     * @see mesquite.correl.lib.DESystem#calculateDerivative(double, double[])
     */
    public String toString(){
        return "Reduced Clade Model s=" + MesquiteDouble.toString(s, 4) + " e=" + MesquiteDouble.toString(e, 4);
    }
    public double[]calculateDerivative(double t,double probs[],double[] result){
        // for clarity
        double extProb = probs[0];
        double dataProb = probs[1];
        result[0] = -(e+s)*extProb + s*extProb*extProb + e; 
        result[1] = -(e+s)*dataProb + 2*s*extProb*dataProb;
        return result;
    }
    
    public void setE(double e){
        this.e = e;
    }
    
    public void setS(double s){
        this.s = s;
    }
    
 
    public void resetParameters(double e, double s){
        this.e = e;
        this.s = s;
    }


    public double getSRate(int state) {
        if (state == 0)
            return s;
        //else if (state == 1)
        //    return s1;
        else return MesquiteDouble.unassigned;
    }
    
    public double getERate(int state) {
        if (state == 0)
            return e;
        //else if (state == 1)
        //    return e1;
        else return MesquiteDouble.unassigned;
    }
}
