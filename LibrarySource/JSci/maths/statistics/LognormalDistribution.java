package JSci.maths.statistics;

/**
* The LognormalDistribution class provides an object for encapsulating lognormal distributions.
* @version 0.1
* @author Mark Hale
*/
public final class LognormalDistribution extends ProbabilityDistribution {
        private NormalDistribution normal;

        /**
        * Constructs a standard lognormal distribution.
        */
        public LognormalDistribution() {
                this(0.0,1.0);
        }
        /**
        * Constructs a lognormal distribution.
        * @param mu the mu parameter.
        * @param sigma the sigma parameter.
        */
        public LognormalDistribution(double mu,double sigma) {
                normal=new NormalDistribution(mu,sigma*sigma);
        }
        /**
        * Returns the mu parameter.
        */
        public double getMuParameter() {
                return normal.getMean();
        }
        /**
        * Returns the sigma parameter.
        */
        public double getSigmaParameter() {
                return Math.sqrt(normal.getVariance());
        }
        /**
        * Probability density function of a lognormal distribution.
        * @return the probability that a stochastic variable x has the value X, i.e. P(x=X).
        */
        public double probability(double X) {
                checkRange(X,0.0,Double.MAX_VALUE);
                return normal.probability(Math.log(X))/X;
        }
        /**
        * Cumulative lognormal distribution function.
	* @return the probability that a stochastic variable x is less then X, i.e. P(x&lt;X).
        */
        public double cumulative(double X) {
                checkRange(X,0.0,Double.MAX_VALUE);
                return normal.cumulative(Math.log(X));
        }
        /**
	* Inverse of the cumulative lognormal distribution function.
        * @return the value X for which P(x&lt;X).
        */
        public double inverse(double probability) {
                return Math.exp(normal.inverse(probability));
        }
}


