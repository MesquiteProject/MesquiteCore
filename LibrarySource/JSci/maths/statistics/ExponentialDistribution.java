package JSci.maths.statistics;

/**
* The ExponentialDistribution class provides an object for encapsulating exponential distributions.
* @version 0.2
* @author Mark Hale
*/
public final class ExponentialDistribution extends ProbabilityDistribution {
        private double lambda;

        /**
        * Constructs the standard exponential distribution.
        */
        public ExponentialDistribution() {
                this(1.0);
        }
        /**
        * Constructs an exponential distribution.
        * @param decay the scale parameter.
        */
        public ExponentialDistribution(double decay) {
                if(decay<0.0)
                        throw new OutOfRangeException("The scale parameter should be positive.");
                lambda=decay;
        }
        /**
        * Constructs an exponential distribution from a data set.
        * @param array a sample.
        */
        public ExponentialDistribution(double array[]) {
                double sumX=array[0];
                for(int i=1;i<array.length;i++)
                        sumX+=array[i];
                lambda=sumX/array.length;
        }
        /**
        * Returns the scale parameter.
        */
        public double getScaleParameter() {
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
                return lambda*lambda;
        }
        /**
        * Probability density function of an exponential distribution.
        * P(X) = <img border=0 alt="lambda" src="../doc-files/lambda.gif">e<sup>-<img border=0 alt="lambda" src="../doc-files/lambda.gif">X</sup>.
        * @return the probability that a stochastic variable x has the value X, i.e. P(x=X).
        */
        public double probability(double X) {
                checkRange(X,0.0,Double.MAX_VALUE);
                return lambda*Math.exp(-lambda*X);
        }
        /**
        * Cumulative exponential distribution function.
	* @return the probability that a stochastic variable x is less then X, i.e. P(x&lt;X).
        */
        public double cumulative(double X) {
                checkRange(X,0.0,Double.MAX_VALUE);
                return 1.0-Math.exp(-lambda*X);
        }
        /**
	* Inverse of the cumulative exponential distribution function.
        * @return the value X for which P(x&lt;X).
        */
        public double inverse(double probability) {
                checkRange(probability);
                return -Math.log(1.0-probability)/lambda;
        }
}

