package JSci.maths.statistics;

import JSci.maths.SpecialMath;

/**
* The TDistribution class provides an object for encapsulating student's t-distributions.
* @version 1.0
* @author Jaco van Kooten
*/
public final class TDistribution extends ProbabilityDistribution {
        private int dgrFreedom;
        private double logPdfFreedom;

        /**
        * Constructor for student's t-distribution.
        * @param r degrees of freedom.
        */
        public TDistribution(int r) {
                if(r<=0)
                        throw new OutOfRangeException("The degrees of freedom must be greater than zero.");
                dgrFreedom=r;
                logPdfFreedom=-SpecialMath.logBeta(0.5*dgrFreedom,0.5)-0.5*Math.log(dgrFreedom);
        }
        /**
        * Returns the degrees of freedom.
        */
        public int getDegreesOfFreedom() {
                return dgrFreedom;
        }
        /**
        * Probability density function of a student's t-distribution.
        * @return the probability that a stochastic variable x has the value X, i.e. P(x=X).
        */
        public double probability(double X) {
                double logPdf=logPdfFreedom;
                logPdf-=(0.5*(dgrFreedom+1))*Math.log(1.0+(X*X)/dgrFreedom);
                return Math.exp(logPdf);
        }
        /**
        * Cumulative student's t-distribution function.
	* @return the probability that a stochastic variable x is less then X, i.e. P(x&lt;X).
        */
        public double cumulative(double X) {
                double A=0.5*SpecialMath.incompleteBeta((dgrFreedom)/(dgrFreedom+X*X),0.5*dgrFreedom,0.5);
                return X>0 ? 1-A : A;
        }
        /**
	* Inverse of the cumulative student's t-distribution function.
        * @return the value X for which P(x&lt;X).
        */
        public double inverse(double probability) {
                checkRange(probability);
                if(probability==0.0)
                        return -Double.MAX_VALUE;
                if(probability==1.0)
                        return Double.MAX_VALUE;
                if(probability==0.5)
                        return 0.0;
                return findRoot(probability, 0.0, -0.5*Double.MAX_VALUE, 0.5*Double.MAX_VALUE);
        }
}

