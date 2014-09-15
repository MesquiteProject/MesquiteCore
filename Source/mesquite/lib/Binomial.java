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

/*Last documented:  August 1999 */
/* ======================================================================== */
public class Binomial {
	public static double power (double p, int r) {
		if (r==0)
			return 1;
		double result = p;
		for (int i=2; i<=r; i++)
			result *= p;
		return result;
	}
	
	public static double probability(int n, int heads, double p) {
		if (n>1000) {
			MesquiteMessage.println("Error: binomial probability can't be calculated for n greater than 1000");
			return 0;
		}
		if (heads>n)
			return 0;
		else if (heads == n)
			return power(p, n);
		else if (heads == 0)
			return power((1-p), n);
		else {
			double c = 1;
			for (double i= 1; i<= heads; i++)
				c *= (n-i+1)*p/i;
			for (double i= 1; i<= n- heads; i++)
				c *= (1.0-p);
			return c;
		}
	}
	public static double bestTail(int n, int heads, double p) {
		if (n>1000) {
			MesquiteMessage.println("Error: binomial probability can't be calculated for n greater than 1000");
			return 0;
		}
		double c=0;
		int t;
		double q;
		if (heads< n-heads) {
			t = heads;
			q = p;
		}
		else {
			q = 1-p;
			t = n-heads;
		}
		for (int i = 0; i<=t; i++){
			c += probability(n, i, q);
		}
		return c;
	}
	public static double rightTail(int n, int heads, double p) {
		if (n>1000) {
			 MesquiteMessage.println("Error: binomial probability can't be calculated for n greater than 1000");
			return 0;
		}
		double c=0;
		for (int i = heads; i<=n; i++){
			c += probability(n, i, p);
		}
		return c;
	}
	public static double leftTail(int n, int heads, double p) {
		if (n>1000) {
			MesquiteMessage.println("Error: binomial probability can't be calculated for n greater than 1000");
			return 0;
		}
		double c=0;
		for (int i = 0; i<=heads; i++){
			c += probability(n, i, p);
		}
		return c;
	}
}


