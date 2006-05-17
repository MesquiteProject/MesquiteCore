package JSci.maths.statistics;

import JSci.maths.SpecialMath;

/**
* The BetaDistribution class provides an object for encapsulating beta distributions.
* @version 1.0
* @author Jaco van Kooten
*/
public final class BetaDistribution extends ProbabilityDistribution {
        private double p,q;

        /**
        * Constructs a beta distribution.
        * @param dgrP degrees of freedom p.
        * @param dgrQ degrees of freedom q.
        */
        public BetaDistribution(double dgrP,double dgrQ) {
                if(dgrP<=0 || dgrQ<=0)
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
        * Probability density function of a beta distribution.
        * @return the probability that a stochastic variable x has the value X, i.e. P(x=X).
        */
        public double probability(double X) {
                checkRange(X);
                if(X==0.0 || X==1.0)
                        return 0.0;
                return Math.exp(-SpecialMath.logBeta(p,q)+(p-1.0)*Math.log(X)+(q-1.0)*Math.log(1.0-X));
        }
        /**
        * Cumulative beta distribution function.
	* @return the probability that a stochastic variable x is less then X, i.e. P(x&lt;X).
        */
        public double cumulative(double X) {
                checkRange(X);
                return SpecialMath.incompleteBeta(X,p,q);
        }
        /**
	* Inverse of the cumulative beta distribution function.
        * @return the value X for which P(x&lt;X).
        */
        public double inverse(double probability) {
                checkRange(probability);
                if(probability==0.0)
                        return 0.0;
                if(probability==1.0)
                        return 1.0;
                return findRoot(probability, 0.5, 0.0, 1.0);
        }
}

