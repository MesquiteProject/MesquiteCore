package mesquite.diverse.lib;


import mesquite.lib.MesquiteMessage;

/**
 * This wraps a CladeExtinctionModel so that s and e can be expressed as r=s-e (diversification rate)
 * and a=e/s.
 * 
 * @author pmidford
 * created Sep 28, 2006
 *
 */
public class SpecExtincRatioDiffCategModel implements DESpeciationSystemCateg {

    private SpecExtincCategModel wrapped;

    public SpecExtincRatioDiffCategModel(double a0, double r0, double a1, double r1, double t01, double t10){
        if (a0==1.0 || a1==1.0){
            wrapped = null;
            MesquiteMessage.warnProgrammer("Either a0 or a1 == 1, no wrapped model created.");
            return;
        }
        double e0=r0*a0/(1-a0);
        double s0=r0+e0;
        double e1=r1*a1/(1-a1);
        double s1=r1+e1;
        wrapped = new SpecExtincCategModel(e0,s0,e1,s1,t01,t10);
    }

    public double getSRate(int state) {
        return wrapped.getSRate(state);
    }
    public double getERate(int state) {
        return wrapped.getERate(state);
    }

    public double[] calculateDerivative(double x, double[] y, double[] result) {
        return wrapped.calculateDerivative(x, y, result);
    }

    public String toString(){
        return "Wrapper of " + wrapped.toString();
    }

    public void setA0(double a0){
        if (a0 == 1){
            MesquiteMessage.warnProgrammer("Attempt to set diversification model a0 to 1; not changed.");
        }
        double olds0 = wrapped.getSRate(0);
        double olde0 = wrapped.getERate(0);
        double r0 = olds0-olde0;
        double newE0 = r0*a0/(1-a0);
        wrapped.setE0(newE0);
        wrapped.setS0(r0+newE0);
    }

    public void setR0(double r0){
        double olds0 = wrapped.getSRate(0);
        double olde0 = wrapped.getERate(0);
        double a0 = olde0/olds0;
        double newE0 = r0*a0/(1-a0);
        wrapped.setE0(newE0);
        wrapped.setS0(r0+newE0);
    }

    public void setA1(double a1){
        if (a1 == 1){
            MesquiteMessage.warnProgrammer("Attempt to set diversification model a1 to 1; not changed.");
        }
        double olds1 = wrapped.getSRate(1);
        double olde1 = wrapped.getERate(1);
        double r1 = olds1-olde1;
        double newE1 = r1*a1/(1-a1);
        wrapped.setE1(newE1);
        wrapped.setS1(r1+newE1);
    }

    public void setR1(double r1){
        double olds1 = wrapped.getSRate(1);
        double olde1 = wrapped.getERate(1);
        double a1 = olde1/olds1;
        double newE1 = r1*a1/(1-a1);
        wrapped.setE1(newE1);
        wrapped.setS1(r1+newE1);
    }

    public void setT01(double t01){
        wrapped.setT01(t01);
    }

    public void setT10(double t10){
        wrapped.setT10(t10);
    }

    public void resetParameters(double a0, double r0, double a1, double r1, double t01, double t10){
        if (a0==0 || a1==0){
            MesquiteMessage.warnProgrammer("Either a0 or a1 == 0, wrapped model unchanged.");
            return;
        }
        double e0=r0*a0/(1-a0);
        double s0=r0+e0;
        double e1=r1*a1/(1-a1);
        double s1=r1+e1;
        wrapped.resetParameters(e0,s0,e1,s1,t01,t10);
    }

}
