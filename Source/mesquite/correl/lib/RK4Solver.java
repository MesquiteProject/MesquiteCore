package mesquite.correl.lib;

import java.util.Vector;


public class RK4Solver implements DEQNumSolver {

	DESystem mySystem;
	double[] nextY;

	public double[] integrate(double x0, double[] y0, double h, double xend, DESystem ds, double[] results){
		mySystem = ds;
		if (results == null || results.length != y0.length)
			results = new double[y0.length];
		nextY = results;
		for(int i=0;i<nextY.length;i++)
			nextY[i]=y0[i];
		if (h>0) {   //most common case
			if (x0 >= xend)
				return null;
			double x = x0;
			while(x < xend){
				step(x,nextY,h);
				x += h;
			}
		}
		else {    // might be useful for seeding non-self starting methods
			if (x0 <= xend)
				return null;
			double x = x0;
			while (x> xend){
				step(x,nextY,-h);
				x -= h;
			}
		}
		return results;
	}

	
	public Vector integrate(double x0, double[] y0, double h, double xend, DESystem ds,Vector results) {
		mySystem = ds;
		if (results == null)
			results = new Vector();
		if (nextY == null || nextY.length != y0.length)
			nextY = new double[y0.length];
		for(int i=0;i<nextY.length;i++)
			nextY[i]=y0[i];
		if (h>0) {   //most common case
			if (x0 >= xend)
				return null;
			double x = x0;
			while(x < xend){
				step(x,nextY,h);
				results.add(nextY.clone());
				x += h;
			}
		}
		else {    // might be useful for seeding non-self starting methods
			if (x0 <= xend)
				return null;
			double x = x0;
			while (x> xend){
				
			}
		}
		return results;
	}

	
	
	double[] k1;
	double[] k2;
	double[] k3;
	double[] k4;
	double[] yk1;
	double[] yk2;
	double[] yk3;

	double[] step(double x, double [] y, double h){
		if (k1 == null || k1.length != y.length){
			k1 = new double[y.length];
			k2 = new double[y.length];
			k3 = new double[y.length];
			k4 = new double[y.length];
			yk1 = new double[y.length];
			yk2 = new double[y.length];
			yk3 = new double[y.length];
		}
		k1 = mySystem.calculateDerivative(x,y);
		for(int i=0;i<yk1.length;i++)
			yk1[i]=y[i]+k1[i]*0.5*h;
		k2 = mySystem.calculateDerivative(x+0.5*h,yk1);
		for(int i=0;i<yk2.length;i++)
			yk2[i]=y[i]+k2[i]*0.5*h;
		k3 = mySystem.calculateDerivative(x+0.5*h,yk2);
		for(int i=0;i<yk3.length;i++)
			yk3[i]=y[i]+k3[i]*h;
		k4 = mySystem.calculateDerivative(x+h,yk3);
		for(int i=0;i<nextY.length;i++)
			nextY[i] = y[i] + (h/6)*(k1[i]+2*k2[i]+2*k3[i]+k4[i]);
		return nextY;
	}

	





}
