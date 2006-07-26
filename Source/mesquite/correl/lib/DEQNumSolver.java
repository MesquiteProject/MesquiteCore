package mesquite.correl.lib;

import java.util.Vector;


public interface DEQNumSolver {

	public double[] integrate(double x0, double[]y0, double h, double xend, DESystem ds,double[] results);

	// Version that saves the intermediate points on the interval - 'answer' is results.lastElement()
	public Vector integrate(double x0, double[]y0, double h, double xend, DESystem ds,Vector results,boolean saveResults);
	
}
