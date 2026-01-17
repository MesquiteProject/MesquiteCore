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

import java.math.BigDecimal;

/*Last documented:  August 1999 */
/* ======================================================================== */
public class Binomial {

	int exponent = 0;
	double mantissa = 0.0;
	
	public static double power (double p, int r) {
		if (r==0)
			return 1;
		double result = p;
		for (int i=2; i<=r; i++)
			result *= p;
		return result;
	}
	
	/*
	public static BigDecimal probabilityBigDecimal(int n, int heads, double p){ 
		if (heads>n)
			return new BigDecimal("0.0");
		else if (heads == n){
			BigDecimal bd = new BigDecimal(p);
			return bd.pow(n);
		}
		else if (heads == 0){
			BigDecimal bd = new BigDecimal(1.0-p);
			return bd.pow(n);
		}
		else {
			BigDecimal bd = new BigDecimal("1.0");
			BigDecimal pp = new BigDecimal(p);
			BigDecimal ppm1 = new BigDecimal(1.0-p);
			for (int i= 1; i<= heads; i++){
				bd = bd.multiply(pp);
				BigDecimal ni1 = new BigDecimal(n-i+1);
				bd = bd.multiply(ni1);
				BigDecimal ii = new BigDecimal(i);
				bd = bd.divide(ii);
				//c *= (n-i+1)*p/i;
			}
			for (int i= 1; i<= n- heads; i++){
				bd = bd.multiply(ppm1);
				//c *= (1.0-p);
			}
			return bd;
		}
	}
	*/
	
	public static MesquiteBigDecimal probabilityMBD(int n, int heads, double p, MesquiteBigDecimal result) {
		if (result == null)
			result = new MesquiteBigDecimal(0, 0);
		if (heads>n){
			result.setValue(0,0);
		}
		else if (heads == n){
			result.setValue(p, 0);
			result.power(n);
	//		System.err.println("  power " + (p)  + " to power " + n + " " + result.getDoubleValue());
		}
		else if (heads == 0){
			result.setValue(1.0-p, 0);
			result.power(n);
		//	System.err.println("  power " + (1.0-p)  + " to power " + n + " " + result.getDoubleValue());
		}
		else {
			result.setValue(1.0, 0);
			for (int i= 1; i<= heads; i++){
				result.multiply(p);
				result.multiply(n-i+1);
				result.divide(i);
			}
			for (int i= 1; i<= n- heads; i++){
				result.multiply(1.0-p);
				//c *= (1.0-p);
			}
		}
		//System.err.println("  NEW=== " + n + " " + heads + " " + result.getDoubleValue());
		return result;
	}
	public static double probability(int n, int heads, double p) {
		double c = 0;
		if (n>10000) {
			MesquiteMessage.println("Error: binomial probability can't be calculated for n greater than 10000");
			c = 0;
		}
		if (heads>n)
			c= 0;
		else if (heads == n)
			c= power(p, n);
		else if (heads == 0)
			c = power((1-p), n);
		else {
			c = 1;
			for (int i= 1; i<= heads; i++)
				c *= (n-i+1)*p/i;
			for (int i= 1; i<= n- heads; i++)
				c *= (1.0-p);
		}
		//System.err.println("  OLD=== " + n + " " + heads + " " + c);
		return c;
	}
	public static double bestTail(int n, int heads, double p) {
		if (n>10000) {
			MesquiteMessage.println("Error: binomial probability can't be calculated for n greater than 10000");
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
	
	/*public static BigDecimal rightTailBigDecimal(int n, int heads, double p) {
		BigDecimal bd =  new BigDecimal("0.0");
		for (int i = heads; i<=n; i++){
			bd = bd.add(probabilityBigDecimal(n, i, p));
		}
		return bd;
	}
	*/
	public static MesquiteBigDecimal rightTailMBD(int n, int heads, double p, boolean approx) {
		MesquiteBigDecimal sum = new MesquiteBigDecimal(0, 0);
		MesquiteBigDecimal prob = new MesquiteBigDecimal(0, 0);
		MesquiteBigDecimal approxTester = new MesquiteBigDecimal(0, 0);
		boolean stop = false;
		for (int i = heads; i<=n && !stop; i++){
			prob = probabilityMBD(n, i, p, prob);
			if (approx && i-heads>3){
				approxTester.setValue(prob);
				approxTester.divide(sum);
				if (approxTester.lessThan(0.01, 0))
					stop = true;
			}
			sum.add(prob);
		}
		return sum;
	}
	public static double rightTail(int n, int heads, double p) {
		if (n>10000) {
			 MesquiteMessage.println("Error: binomial probability can't be calculated for n greater than 10000");
			return 0;
		}
		double c=0;
		for (int i = heads; i<=n; i++){
			c += probability(n, i, p);
		}
		return c;
	}
	public static double leftTail(int n, int heads, double p) {
		if (n>10000) {
			MesquiteMessage.println("Error: binomial probability can't be calculated for n greater than 10000");
			return 0;
		}
		double c=0;
		for (int i = 0; i<=heads; i++){
			c += probability(n, i, p);
		}
		return c;
	}
}


