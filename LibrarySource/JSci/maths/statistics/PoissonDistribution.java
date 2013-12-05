package JSci.maths.statistics;

import JSci.maths.ExtraMath;

/**
* The PoissonDistribution class provides an object for encapsulating Poisson distributions.
* @version 0.2
* @author Mark Hale
*/
public final class PoissonDistribution extends ProbabilityDistribution {
        private double lambda;

        /**
        * Constructs a Poisson distribution.
        * @param interval the interval.
        */
        public PoissonDistribution(double interval) {
                if(interval<=0.0)
                        throw new OutOfRangeException("The interval should be (strictly) positive.");
                lambda=interval;
        }
        /**
        * Returns the interval parameter.
        */
        public double getIntervalParameter() {
                return lambda;
        }
        /**
        * Returns the mean.
        */
        public double getMean() {
                return lambda;
        }
        /**
        * Returns the variance.
        */
        public double getVariance() {
                return lambda;
        }
        /**
        * Probability density function of a Poisson distribution.
        * P(X) = <img border=0 alt="lambda" src="../doc-files/lambda.gif"><sup>X</sup>e<sup>-<img border=0 alt="lambda" src="../doc-files/lambda.gif"></sup>/X!.
        * @param X should be integer-valued.
        * @return the probability that a stochastic variable x has the value X, i.e. P(x=X).
        */
        public double probability(double X) {
                checkRange(X,0.0,Double.MAX_VALUE);
                return Math.exp(X*Math.log(lambda)-lambda-ExtraMath.logFactorial(X));
        }
        /**
        * Cumulative Poisson distribution function.
        * @param X should be integer-valued.
	* @return the probability that a stochastic variable x is less then X, i.e. P(x&lt;X).
        */
        public double cumulative(double X) {
                checkRange(X,0.0,Double.MAX_VALUE);
                double sum=0.0;
                for(double i=0.0;i<=X;i++)
                        sum+=probability(i);
                return sum;
        }
        /**
	* Inverse of the cumulative Poisson distribution function.
        * @return the value X for which P(x&lt;X).
        */
        public double inverse(double probability) {
                checkRange(probability);
                return Math.floor(findRoot(probability,lambda,0.0,Double.MAX_VALUE));
        }
}


