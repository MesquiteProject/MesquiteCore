package JSci.maths.statistics;

import JSci.maths.SpecialMath;

/**
* The FDistribution class provides an object for encapsulating F-distributions.
* @version 1.0
* @author Jaco van Kooten
*/
public final class FDistribution extends ProbabilityDistribution {
        private double p,q;

        /**
        * Constructs an F-distribution.
        * @param dgrP degrees of freedom p.
        * @param dgrQ degrees of freedom q.
        */
        public FDistribution(double dgrP, double dgrQ) {
                if(dgrP<=0.0 || dgrQ<=0.0)
                        throw new OutOfRangeException("The degrees of freedom must be greater than zero.");
                p=dgrP;
                q=dgrQ;
        }

        /**
        * Returns the degrees of freedom p.
        */
        public double getDegreesOfFreedomP() {
                return p;
        }
        /**
        * Returns the degrees of freedom q.
        */
        public double getDegreesOfFreedomQ() {
                return q;
        }
        /**
        * Probability density function of an F-distribution.
        * @return the probability that a stochastic variable x has the value X, i.e. P(x=X).
        */
        public double probability(double X) {
// We make use of the fact that when x has an F-distibution then
// y = q/(q+p*x) has a beta distribution with parameters p/2 and q/2.
                checkRange(X,0.0,Double.MAX_VALUE);
                double y=q/(q+(p*X));
                BetaDistribution beta=new BetaDistribution(p/2.0,q/2.0);
                return beta.probability(y)*y*y*p/q;
        }
        /**
        * Cumulative F-distribution function.
	* @return the probability that a stochastic variable x is less then X, i.e. P(x&lt;X).
        */
        public double cumulative(double X) {
// We make use of the fact that when x has an F-distibution then
// y = q/(q+p*x) has a beta distribution with parameters p/2 and q/2.
                checkRange(X,0.0,Double.MAX_VALUE);
                return SpecialMath.incompleteBeta(1.0/(1.0+q/(p*X)),p/2.0,q/2.0);
        }
        /**
	* Inverse of the cumulative F-distribution function.
        * @return the value X for which P(x&lt;X).
        */
        public double inverse(double probability) {
// We make use of the fact that when x has an F-distibution then
// y = q/(q+p*x) has a beta distribution with parameters p/2 and q/2.
                checkRange(probability);
                if(probability==0.0)
                        return 0.0;
                if(probability==1.0)
                        return Double.MAX_VALUE;
                BetaDistribution beta=new BetaDistribution(p/2.0,q/2.0);
                double y=beta.inverse(1.0-probability);
                if(y<2.23e-308) //avoid overflow
                        return Double.MAX_VALUE;
                else
                        return (p/q)*(1.0/y-1.0);
        }
}


