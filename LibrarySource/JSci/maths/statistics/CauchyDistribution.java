package JSci.maths.statistics;

/**
* The CauchyDistribution class provides an object for encapsulating Cauchy distributions.
* @version 0.2
* @author Mark Hale
*/
public final class CauchyDistribution extends ProbabilityDistribution {
        private double alpha;
        private double gamma;

        /**
        * Constructs the standard Cauchy distribution.
        */
        public CauchyDistribution() {
                this(0.0,1.0);
        }
        /**
        * Constructs a Cauchy distribution.
        * @param location the location parameter.
        * @param scale the scale parameter.
        */
        public CauchyDistribution(double location,double scale) {
                if(scale<0.0)
                        throw new OutOfRangeException("The scale parameter should be positive.");
                alpha=location;
                gamma=scale;
        }
        /**
        * Returns the location parameter.
        */
        public double getLocationParameter() {
                return alpha;
        }
        /**
        * Returns the scale parameter.
        */
        public double getScaleParameter() {
                return gamma;
        }
        /**
        * Probability density function of a Cauchy distribution.
        * P(X) = <img border=0 alt="Gamma" src="../doc-files/ugamma.gif">/(<img border=0 alt="pi" src="../doc-files/pi.gif">(<img border=0 alt="Gamma" src="../doc-files/ugamma.gif"><sup>2</sup>+(X-<img border=0 alt="alpha" src="../doc-files/alpha.gif">)<sup>2</sup>)).
        * @return the probability that a stochastic variable x has the value X, i.e. P(x=X).
        */
        public double probability(double X) {
                final double y=X-alpha;
                return gamma/(Math.PI*(gamma*gamma+y*y));
        }
        /**
        * Cumulative Cauchy distribution function.
	* @return the probability that a stochastic variable x is less then X, i.e. P(x&lt;X).
        */
        public double cumulative(double X) {
                return 0.5+Math.atan((X-alpha)/gamma)/Math.PI;
        }
        /**
	* Inverse of the cumulative Cauchy distribution function.
        * @return the value X for which P(x&lt;X).
        */
        public double inverse(double probability) {
                checkRange(probability);
                return alpha-gamma/Math.tan(Math.PI*probability);
        }
}

