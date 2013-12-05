package JSci.maths.statistics;

/**
* The GeometricDistribution class provides an object for encapsulating geometric distributions.
* @version 0.2
* @author Mark Hale
*/
public final class GeometricDistribution extends ProbabilityDistribution {
        private double success;
        private double failure;

        /**
        * Constructs a geometric distribution.
        * @param prob the probability of success.
        */
        public GeometricDistribution(double prob) {
                if(prob<0.0 || prob>1.0)
                        throw new OutOfRangeException("The probability should be between 0.0 and 1.0.");
                success=prob;
                failure=1.0-prob;
        }
        /**
        * Returns the success parameter.
        */
        public double getSuccessParameter() {
                return success;
        }
        /**
        * Returns the mean.
        */
        public double getMean() {
                return 1.0/success;
        }
        /**
        * Returns the variance.
        */
        public double getVariance() {
                return failure/(success*success);
        }
        /**
        * Probability density function of a geometric distribution.
        * P(X) = p (1-p)<sup>X-1</sup>.
        * @param X should be integer-valued.
        * @return the probability that a stochastic variable x has the value X, i.e. P(x=X).
        */
        public double probability(double X) {
                checkRange(X,0.0,Double.MAX_VALUE);
                return success*Math.pow(failure,X-1);
        }
        /**
        * Cumulative geometric distribution function.
        * @param X should be integer-valued.
	* @return the probability that a stochastic variable x is less then X, i.e. P(x&lt;X).
        */
        public double cumulative(double X) {
                checkRange(X,0.0,Double.MAX_VALUE);
                return 1.0-Math.pow(failure,X);
        }
        /**
	* Inverse of the cumulative geometric distribution function.
        * @return the value X for which P(x&lt;X).
        */
        public double inverse(double probability) {
                checkRange(probability);
                return Math.log(1.0-probability)/Math.log(failure);
        }
}


