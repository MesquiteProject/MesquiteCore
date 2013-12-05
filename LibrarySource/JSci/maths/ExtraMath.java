package JSci.maths;

/**
* The extra math library.
* Provides extra functions not in java.lang.Math class.
* This class cannot be subclassed or instantiated because all methods are static.
* @version 1.1
* @author Mark Hale
*/
public final class ExtraMath extends AbstractMath {
        private ExtraMath() {}

        /**
        * Returns sqrt(x<sup>2</sup>+y<sup>2</sup>).
        */
        public static double hypot(final double x,final double y) {
                final double xAbs=Math.abs(x);
                final double yAbs=Math.abs(y);
                if(xAbs==0.0 && yAbs==0.0)
                        return 0.0;
                else if(xAbs<yAbs)
                        return yAbs*Math.sqrt(1.0+(x/y)*(x/y));
                else
                        return xAbs*Math.sqrt(1.0+(y/x)*(y/x));
        }
        /**
        * Returns a<sup>b</sup>.
        */
        public static int pow(int a,int b) {
                for(int i=0;i<b;i++)
                        a*=a;
                return a;
        }
        /**
        * Returns the factorial.
        * (Wrapper for the gamma function).
        * @see SpecialMath#gamma
        * @param x a double.
        */
        public static double factorial(double x) {
                return SpecialMath.gamma(x+1.0);
        }
        /**
        * Returns the natural logarithm of the factorial.
        * (Wrapper for the log gamma function).
        * @see SpecialMath#logGamma
        * @param x a double.
        */
        public static double logFactorial(double x) {
                return SpecialMath.logGamma(x+1.0);
        }
        /**
        * Returns the binomial coefficient (n k).
        * @param n a double.
        * @param k a double.
        */
        public static double binomial(double n,double k) {
                return Math.exp(SpecialMath.logGamma(n+1.0)-SpecialMath.logGamma(k+1.0)-SpecialMath.logGamma(n-k+1.0));
        }
        /**
        * Returns the hyperbolic sine of a double.
        * @param x a double.
        */
        public static double sinh(double x) {
                return (Math.exp(x)-Math.exp(-x))/2.0;
        }
        /**
        * Returns the hyperbolic cosine of a double.
        * @param x a double.
        */
        public static double cosh(double x) {
                return (Math.exp(x)+Math.exp(-x))/2.0;
        }
        /**
        * Returns the hyperbolic tangent of a double.
        * @param x a double.
        */
        public static double tanh(double x) {
                return sinh(x)/cosh(x);
        }
        /**
        * Returns the arc hyperbolic sine of a double,
        * in the range of -<img border=0 alt="infinity" src="doc-files/infinity.gif"> through <img border=0 alt="infinity" src="doc-files/infinity.gif">.
        * @param x a double.
        */
        public static double asinh(double x) {
                return Math.log(x+Math.sqrt(x*x+1.0));
        }
        /**
        * Returns the arc hyperbolic cosine of a double,
        * in the range of 0.0 through <img border=0 alt="infinity" src="doc-files/infinity.gif">.
        * @param x a double.
        */
        public static double acosh(double x) {
                return Math.log(x+Math.sqrt(x*x-1.0));
        }
        /**
        * Returns the arc hyperbolic tangent of a double,
        * in the range of -<img border=0 alt="infinity" src="doc-files/infinity.gif"> through <img border=0 alt="infinity" src="doc-files/infinity.gif">.
        * @param x a double.
        */
        public static double atanh(double x) {
                return Math.log((1.0+x)/(1.0-x))/2.0;
        }
}


