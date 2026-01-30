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

/* ======================================================================== */
/* a very approximate version of BigDecimal, for speed and memory*/
public class MesquiteBigDecimal {

	int exponent = 0;
	double mantissa = 0.0;

	public MesquiteBigDecimal(double mantissa, int exponent ){
		this.exponent= exponent;
		this.mantissa= mantissa;
		adjust();
	}

	public void setValue(double mantissa, int exponent ){
		this.exponent= exponent;
		this.mantissa= mantissa;
		adjust();
	}
	public void setValueLog10(double log10 ){
		boolean neg = (log10<0);
		if (neg)
			log10 = -log10;
		exponent = (int)log10;
		double mL = log10 - exponent;
		if (neg){
			mantissa= Math.pow(10, -mL);
			exponent= -exponent;
		}
		else
			mantissa= Math.pow(10, mL);
		adjust();
	}
	public void setValue(double mantissa){
		this.exponent= 0;
		this.mantissa= mantissa;
		adjust();
	}
	public void setValue(MesquiteBigDecimal mbd){
		if (mbd == null)
			return;
		setValue(mbd.getMantissa(), mbd.getExponent());
	}
	private void adjust(){
		if (mantissa == 0){
			exponent = 0;
		}
		else if (mantissa<0.001){
			double log10M = Math.log10(mantissa);
			int factor = (int)log10M;
			mantissa *= Math.pow(10, -factor);
			exponent += factor;
		}
		else if (mantissa>1000){
			double log10M = Math.log10(mantissa);
			int factor = (int)log10M;
			mantissa = mantissa/Math.pow(10, factor);
			exponent += factor;
		}
	}
	public void power (int r) {
		if (r==0)
			return;
		double origM = mantissa;
		int origE = exponent;
		for (int i=2; i<=r; i++)
			multiply(origM, origE);
	}

	public void multiply(MesquiteBigDecimal mbd){
		if (mbd == null)
			return;
		multiply(mbd.getMantissa(), mbd.getExponent());
	}
	public void multiply(double m, int e){
		exponent =exponent + e;
		mantissa = mantissa * m;
		adjust();
	}

	public void divide(MesquiteBigDecimal mbd){
		if (mbd == null)
			return;
		divide(mbd.getMantissa(), mbd.getExponent());
	}
	public void divide(double m, int e){
		if (m == 0)
			System.err.println("MesquiteBigDecimal divide by zero");
		exponent =exponent - e;
		mantissa = mantissa / m;
		adjust();
	}
	public void multiply(double m){
		mantissa = mantissa * m;
		adjust();
	}

	public void divide(double m){
		mantissa = mantissa / m;
		adjust();
	}
	public void add(MesquiteBigDecimal mbd){
		if (mbd == null)
			return;
		add(mbd.getMantissa(), mbd.getExponent());
	}

	public void add(double m, int e){
		if (e != exponent){
			int f = e-exponent;
			m *= Math.pow(10, f);
			//System.err.println("    f " + f + " m " );

		}
		mantissa += m;
		adjust();
		//	System.err.println("    @SUM= " + Double.toString(mantissa) + " X 10^" + exponent);
	}

	public double getLog10(){
		if (mantissa == 0)
			System.err.println("MesquiteBigDecimal log10 of zero");

		return exponent + Math.log10(mantissa);
	}
	public double getMantissa(){
		return mantissa;
	}
	public int getExponent(){
		return exponent;
	}
	public double getDoubleValue(){
		return mantissa*Math.pow(10, exponent);
	}
	public boolean lessThan(double m, int e){
		if (e != exponent){ // make exponents the same
			int f = e-exponent;
			m *= Math.pow(10, f);
		}
		if (mantissa==0) {
			return m > 0;
		}
		if (m == 0)
			return mantissa < 0;
		if (mantissa<0 && m>0 || mantissa<0 && m>0)
			return true;
		if (m<0 && mantissa>0 || m<0 && mantissa>0)
			return false;
		if (m>0) // positives, so 
			return mantissa < m;
		//negatives so smaller absolute number is bigger
		return mantissa > m;

	}
	public boolean greaterThan(double m, int e){
		if (e != exponent){ // make exponents the same
			int f = e-exponent;
			m *= Math.pow(10, f);
		}
		if (mantissa==0) {
			return m < 0;
		}
		if (m == 0)
			return mantissa > 0;
			if (mantissa>0 && m<0 || mantissa>0 && m<0)
				return true;
			if (m>0 && mantissa<0 || m>0 && mantissa<0)
				return false;
			if (m>0) // positives, so 
				return mantissa > m;
				//negatives so smaller absolute number is bigger
				return mantissa < m;
	}
	public String toString(){
		return Double.toString(mantissa) + " X 10^" + exponent + " (log10: " +getLog10() + ")";
	}
}


