package JSci.maths.statistics;

import JSci.maths.SpecialMath;

/**
* The WeibullDistribution class provides an object for encapsulating Weibull distributions.
* @version 0.2
* @author Mark Hale
*/
public final class WeibullDistribution extends ProbabilityDistribution {
        private double shape;

        /**
        * Constructs a Weibull distribution.
        * @param sh the shape.
        */
        public WeibullDistribution(double sh) {
                if(sh<=0.0)
                        throw new OutOfRangeException("The shape parameter should be positive.");
                shape=sh;
        }
        /**
        * Returns the shape parameter.
        */
        public double getShapeParameter() {
                return shape;
        }
        /**
        * Returns the mean.
        */
        public double getMean() {
                return SpecialMath.gamma(1.0+1.0/shape);
        }
        /**
        * Returns the variance.
        */
        public double getVariance() {
                return SpecialMath.gamma(1.0+2.0/shape)-getMean()*getMean();
        }
        /**
        * Probability density function of a Weibull distribution.
        * P(X) = s X<sup>s-1</sup> exp(-X<sup>s</sup>).
        * @param X should be integer-valued.
        * @return the probability that a stochastic variable x has the value X, i.e. P(x=X).
        */
        public double probability(double X) {
                checkRange(X,0.0,Double.MAX_VALUE);
                final double XpowShape=Math.pow(X,shape);
                return shape*XpowShape/X*Math.exp(-XpowShape);
        }
        /**
        * Cumulative Weibull distribution function.
        * @param X should be integer-valued.
	* @return the probability that a stochastic variable x is less then X, i.e. P(x&lt;X).
        */
        public double cumulative(double X) {
                checkRange(X,0.0,Double.MAX_VALUE);
                return 1.0-Math.exp(-Math.pow(X,shape));
        }
        /**
	* Inverse of the cumulative Weibull distribution function.
        * @return the value X for which P(x&lt;X).
        */
        public double inverse(double probability) {
                checkRange(probability);
                return Math.pow(-Math.log(1.0-probability),1.0/shape);
        }
}


