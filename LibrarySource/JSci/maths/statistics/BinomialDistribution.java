package JSci.maths.statistics;

import JSci.maths.ExtraMath;

/**
* The BinomialDistribution class provides an object for encapsulating binomial distributions.
* @version 0.1
* @author Mark Hale
*/
public final class BinomialDistribution extends ProbabilityDistribution {
        private int n;
        private double p;

        /**
        * Constructs a binomial distribution.
        * @param trials the number of trials.
        * @param prob the probability.
        */
        public BinomialDistribution(int trials,double prob) {
                if(trials<=0)
                        throw new OutOfRangeException("The number of trials should be (strictly) positive.");
                n=trials;
                if(prob<0.0 || prob>1.0)
                        throw new OutOfRangeException("The probability should be between 0 and 1.");
                p=prob;
        }
        /**
        * Returns the number of trials.
        */
        public int getTrialsParameter() {
                return n;
        }
        /**
        * Returns the probability.
        */
        public double getProbabilityParameter() {
                return p;
        }
        /**
        * Returns the mean.
        */
        public double getMean() {
                return n*p;
        }
        /**
        * Returns the variance.
        */
        public double getVariance() {
                return n*p*(1.0-p);
        }
        /**
        * Probability density function of a binomial distribution.
        * @param X should be integer-valued.
        * @return the probability that a stochastic variable x has the value X, i.e. P(x=X).
        */
        public double probability(double X) {
                checkRange(X,0.0,n);
                return ExtraMath.binomial(n,X)*Math.pow(p,X)*Math.pow(1.0-p,n-X);
        }
        /**
        * Cumulative binomial distribution function.
        * @param X should be integer-valued.
	* @return the probability that a stochastic variable x is less then X, i.e. P(x&lt;X).
        */
        public double cumulative(double X) {
                checkRange(X,0.0,n);
                double sum=0.0;
                for(double i=0.0;i<=X;i++)
                        sum+=probability(i);
                return sum;
        }
        /**
	* Inverse of the cumulative binomial distribution function.
        * @return the value X for which P(x&lt;X).
        */
        public double inverse(double probability) {
                checkRange(probability);
                return Math.floor(findRoot(probability,n/2.0,0.0,n));
        }
}


