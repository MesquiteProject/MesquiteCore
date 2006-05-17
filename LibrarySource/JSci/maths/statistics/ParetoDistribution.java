package JSci.maths.statistics;

/**
* The ParetoDistribution class provides an object for encapsulating Pareto distributions.
* @version 0.2
* @author Mark Hale
*/
public final class ParetoDistribution extends ProbabilityDistribution {
        private double shape;
        private double scale;

        /**
        * Constructs a Pareto distribution.
        * @param sh the shape.
        * @param sc the scale.
        */
        public ParetoDistribution(double sh,double sc) {
                if(sh<0.0)
                        throw new OutOfRangeException("The shape parameter should be positive.");
                shape=sh;
                if(sc<0.0)
                        throw new OutOfRangeException("The scale paremeter should be positive.");
                scale=sc;
        }
        /**
        * Returns the shape parameter.
        */
        public double getShapeParameter() {
                return shape;
        }
        /**
        * Returns the scale parameter.
        */
        public double getScaleParameter() {
                return scale;
        }
        /**
        * Returns the mean.
        */
        public double getMean() {
                return shape*scale/(shape-1.0);
        }
        /**
        * Returns the variance.
        */
        public double getVariance() {
                return shape*scale*scale/((shape-2.0)*(shape-1.0)*(shape-1.0));
        }
        /**
        * Probability density function of a Pareto distribution.
        * P(X) = (a/X) (s/X)<sup>a</sup>.
        * @return the probability that a stochastic variable x has the value X, i.e. P(x=X).
        */
        public double probability(double X) {
                if(X<scale)
                        throw new OutOfRangeException("X should be greater than or equal to the scale.");
                return shape*Math.pow(scale/X,shape)/X;
        }
        /**
        * Cumulative Pareto distribution function.
	* @return the probability that a stochastic variable x is less then X, i.e. P(x&lt;X).
        */
        public double cumulative(double X) {
                if(X<scale)
                        throw new OutOfRangeException("X should be greater than or equal to the scale.");
                return 1.0-Math.pow(scale/X,shape);
        }
        /**
	* Inverse of the cumulative Pareto distribution function.
        * @return the value X for which P(x&lt;X).
        */
        public double inverse(double probability) {
                checkRange(probability);
                return scale/Math.pow(1.0-probability,1.0/shape);
        }
}

