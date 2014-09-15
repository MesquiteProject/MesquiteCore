/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib;

import java.awt.*;
import java.util.*;

/*Last documented:  August 1999 */
/*==========================  Mesquite Basic Class Library    ==========================*/
/*===  the basic classes used by the trunk of Mesquite and available to the modules




/* ======================================================================== */
/** subclass of Random to return random integer between min and max inclusive.*/
public class RandomBetween extends Random {
	public long requests = 0;
	public static boolean askSeed;
	public RandomBetween() {
		super();
	}
	public RandomBetween(long seed) {
		super(seed);
	}
	/*.................................................................................................................*/
	/** Returns a random integer between min and max inclusive.*/
   	public int randomIntBetween(int min, int max) {
   		requests++;
   		int minUse=min, maxUse=max;   		
   		if (!MesquiteInteger.isCombinable(min))
   			minUse = 0;
   		if (!MesquiteInteger.isCombinable(min))
   			maxUse = minUse+1;
   		return ((int)(nextDouble()*(maxUse-minUse + 1))) + minUse;
   	}
	/*.................................................................................................................*/
	/** Returns a random double between min and max inclusive.*/
   	public double randomDoubleBetween(double min, double max) {
   		requests++;
   		double minUse=min, maxUse=max;   		
   		if (!MesquiteDouble.isCombinable(min))
   			minUse = 0.0;
   		if (!MesquiteDouble.isCombinable(min))
   			maxUse = minUse+1.0;
   		return ((nextDouble()*(max-minUse))) + minUse;
   	}
	/*.................................................................................................................*/
	/** Returns a random long between min and max inclusive.*/
   	public long randomLongBetween (long min, long max) {
   		requests++;
  		long minUse=min, maxUse=max;   		
   		if (!MesquiteLong.isCombinable(min))
   			minUse = 0;
   		if (!MesquiteLong.isCombinable(min))
   			maxUse = minUse+1;
   		return ((long)(nextDouble()*(max-minUse + 1))) + minUse;
   	}
	/*.................................................................................................................*/
	/** Returns a random double between min and max inclusive.*/
   	public double getDouble(double min, double max) {
   		requests++;
   		return randomDoubleBetween(min,max);
   	}
	/*.................................................................................................................*/
	/** Returns a random double between min and max inclusive.*/
   	public static double getDoubleStatic(double min, double max) {
   		RandomBetween rnb = new RandomBetween();
   		return rnb.randomDoubleBetween(min,max);
   	}
	/*.................................................................................................................*/
	/** Returns a random double between min and max inclusive.*/
   	public static long getLongStatic(long min, long max) {
   		RandomBetween rnb = new RandomBetween();
   		return rnb.randomLongBetween(min,max);
   	}
	/*.................................................................................................................*/
	/** Returns a random double between min and max inclusive.*/
   	public static int getIntStatic(int min, int max) {
   		RandomBetween rnb = new RandomBetween();
   		return rnb.randomIntBetween(min,max);
   	}
   	
	/*.................................................................................................................*/
	public void setSeedToValueAtIndex(int index, long originalSeed, MesquiteLong seed) {
		setSeed(originalSeed);
		long rnd = originalSeed;  
		for (int it = 0; it<= index; it++) 
			rnd =  nextInt();
		setSeed(rnd+1);
		if (seed!=null)
			seed.setValue(rnd + 1); 
	}
	
	/*.................................................................................................................*/
	public void addRequests(int index) {
		for (int it = 0; it< index; it++)  {
			nextDouble();
			requests++;
		}
	}


}


