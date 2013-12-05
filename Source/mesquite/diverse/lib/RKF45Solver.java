/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.diverse.lib;

import java.util.Vector;

import mesquite.lib.MesquiteDouble;


public class RKF45Solver implements DEQNumSolver {

    DESystem mySystem;
    double[] nextY;
    double minh;
    double maxh;
    MesquiteDouble step = new MesquiteDouble();
    
    final static double fourthRootOneHalf = Math.exp(Math.log(0.5)/4);
    final static double tol = 0.000001;

    /**
     * Solves the system specified by ds over the interval [x0,xend] with initial condition y0 using RKF45
     * @arg x0 starting point
     * @arg y0 starting values
     * @arg step size
     * @arg xend
     * @arg ds system of equations to solve
     * @arg results saves intermediate values, last element is (estimated) y at xend
     */
    public double[] integrate(double x0, double[] y0, double h, double xend, DESystem ds, double[] results){
        mySystem = ds;
        minh = 0.1*h;
        maxh = 10*h;
        step.setValue(h);
        if (results == null || results.length != y0.length)
            results = new double[y0.length];
        nextY = results;
        for(int i=0;i<nextY.length;i++)
            nextY[i]=y0[i];
        if (x0 >= xend)
            return null;
        double x = x0;
        while(x < xend){
            oneStep(x,nextY,step);
            if (step.getValue()<minh)
                step.setValue(minh);
            if (step.getValue()>maxh)
                step.setValue(maxh);                        
            x += step.getValue();
        }
        return results;
    }

    /**
     * Solves the system specified by ds over the interval [x0,xend] with initial condition y0 using RKF45
     * @arg x0 starting point
     * @arg y0 starting values
     * @arg step size
     * @arg xend
     * @arg ds system of equations to solve
     * @arg results saves intermediate values, last element is (estimated) y at xend
     */    
    public Vector integrate(double x0, double[] y0, double h, double xend, DESystem ds,Vector results,boolean saveResults) {
        mySystem = ds;
        minh = 0.1*h;
        maxh = 10*h;
        step.setValue(h);
        double[] lastResult = null;
        if (results == null && saveResults)
            results = new Vector();
        if (results != null)
            results.clear();
        if (nextY == null || nextY.length != y0.length)
            nextY = new double[y0.length];
        for(int i=0;i<nextY.length;i++)
            nextY[i]=y0[i];
        if (x0 >= xend)
            return null;
        double x = x0;
        while(x<xend){
            double oldStep = step.getValue();
            if ((x+oldStep) > xend){
                oldStep = xend-x;
                step.setValue(oldStep);
            }
            oneStep(x,nextY,step);
            x += oldStep;
            if (step.getValue()<minh)
                step.setValue(minh);
            if (step.getValue()>maxh)
                step.setValue(maxh);                                        
            if (saveResults && results != null)
                results.add(nextY.clone());
            else
                lastResult = nextY;
        }        
        if (!saveResults){
            if (results == null)
                results = new Vector(1);
            results.add(lastResult.clone());
        }
        return results;
    }

    
    
    private double[] k1 = null;
    private double[] k2 = null;
    private double[] k3 = null;
    private double[] k4 = null;
    private double[] k5 = null;
    private double[] k6 = null;
    private double[] yk1 = null;
    private double[] yk2 = null;
    private double[] yk3 = null;
    private double[] yk4 = null;
    private double[] yk5 = null;
    private double[] z = null;
    private double[] err2 = null;
    
    private double[] oneStep(double x, double [] y, MesquiteDouble h){
        int ylen = y.length;
        double hval = h.getValue();
        double quarter_h = 0.25*hval;
        double threeEights_h = 0.375*hval;
        double half_h = 0.5*hval;
        double twelveThirteenths_h = 12.0*hval/13;
        if (k1 == null || k1.length != ylen){
            k1 = new double[ylen];
            k2 = new double[ylen];
            k3 = new double[ylen];
            k4 = new double[ylen];
            k5 = new double[ylen];
            k6 = new double[ylen];
            yk1 = new double[ylen];
            yk2 = new double[ylen];
            yk3 = new double[ylen];
            yk4 = new double[ylen];
            yk5 = new double[ylen];
            z = new double[ylen];
            err2 = new double[ylen];
        }
        if (y.length == 0 || k1.length == 0)
            System.out.println("oops");
        k1 = mySystem.calculateDerivative(x,y,k1);
        for(int i=0;i<ylen;i++){
            k1[i] *= hval;
            yk1[i]=y[i]+k1[i]*0.25;
        }
        k2 = mySystem.calculateDerivative(x+quarter_h,yk1,k2);
        for(int i=0;i<ylen;i++){
            k2[i] *= hval;
            yk2[i]=y[i]+k1[i]*3.0/32.0+k2[i]*9.0/32.0;
        }
        k3 = mySystem.calculateDerivative(x+threeEights_h,yk2,k3);
        for(int i=0;i<ylen;i++){
            k3[i] *= hval;
            yk3[i]=y[i]+k1[i]*1932.0/2197.0 - k2[i]*7200.0/2197.0 + k3[i]*7296.0/2197.0;
        }
        k4 = mySystem.calculateDerivative(x+twelveThirteenths_h,yk3,k4);
        for(int i=0;i<ylen;i++){
            k4[i] *= hval;
            yk4[i]= y[i]+k1[i]*(439.0/216.0)-k2[i]*8.0 + k3[i]*(3680.0/513.0) - k4[i]*(845.0/4104.0);
        }
        k5 = mySystem.calculateDerivative(x+hval,yk4,k5);
        for(int i=0;i<ylen;i++){
            k5[i] *= hval;
            yk5[i] = y[i] -k1[i]*(8.0/27.0) + k2[i]*2 - k3[i]*(3544.0/2565.0) + k4[i]*(1859.0/4104.0) - k5[i]*(11.0/40.0);            
        }
        k6 = mySystem.calculateDerivative(x+half_h,yk5,k6);
        for(int i=0;i<ylen;i++)
            k6[i] *= hval;
        double s=MesquiteDouble.infinite;
        for(int i=0;i<nextY.length;i++){
            nextY[i] = y[i] + (25.0/216.0)*k1[i]+(1408.0/2565.0)*k3[i]+(2197.0/4104.0)*k4[i]-(1.0/5.0)*k5[i];
            z[i] = y[i] + (16.0/135.0)*k1[i] + (6656.0/12825.0)*k3[i] + (28561.0/56430.0)*k4[i] - (9.0/50.0)*k5[i] + (2.0/55.0)*k6[i];
            err2[i] = Math.abs((1.0/360.0)*k1[i] + (-128.0/4275.0)*k3[i] + (-2197.0/75240.0)*k4[i] + (1.0/50.0)*k5[i] + (2.0/55.0)*k6[i]);
            double part5a = (y[i] + (16.0/135.0)*k1[i] + (6656.0/12825.0)*k3[i] + (28561.0/56430.0)*k4[i] - (9.0/50.0)*k5[i] + (2.0/55.0)*k6[i]);
            double part5b = (y[i] + (25.0/216.0)*k1[i]+(1408.0/2565.0)*k3[i]+(2197.0/4104.0)*k4[i]-(1.0/5.0)*k5[i]);
            double part5c = part5a-part5b;
            double part5 = (y[i] + (16.0/135.0)*k1[i] + (6656.0/12825.0)*k3[i] + (28561.0/56430.0)*k4[i] - (9.0/50.0)*k5[i] + (2.0/55.0)*k6[i]) - (y[i] + (25.0/216.0)*k1[i]+(1408.0/2565.0)*k3[i]+(2197.0/4104.0)*k4[i]-(1.0/5.0)*k5[i]);
            double myerr = z[i]-nextY[i];
            if (myerr != 0.0){
                double si = fourthRootOneHalf*Math.exp(0.25*Math.log(((tol*hval)/Math.abs(part5c))));
                if (si < s)
                    s = si;
            }
        }
        if (s != MesquiteDouble.infinite)
            h.setValue(hval*s);
        return nextY;
    }

    





}
