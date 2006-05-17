package JSci.maths.statistics;

import JSci.maths.*;

/**
* The NormalDistribution class provides an object for encapsulating normal distributions.
* @version 1.1
* @author Jaco van Kooten
*/
public final class NormalDistribution extends ProbabilityDistribution implements NumericalConstants {
        private double mean,variance;
        private double pdfDenominator,cdfDenominator;

        /**
        * Constructs the standard normal distribution (zero mean and unity variance).
        */
        public NormalDistribution() {
                this(0.0,1.0);
        }
        /**
        * Constructs a normal distribution.
        * @param mu the mean.
        * @param var the variance.
        */
        public NormalDistribution(double mu,double var) {
                mean=mu;
                if(var<=0.0) 
                        throw new OutOfRangeException("The variance should be (strictly) positive.");
                variance=var;
                pdfDenominator=SQRT2PI*Math.sqrt(variance);
                cdfDenominator=SQRT2*Math.sqrt(variance);
        }
        /**
        * Constructs a normal distribution from a data set.
        * @param array a sample.
        * @author Mark Hale
        */
        public NormalDistribution(double array[]) {
                double sumX=array[0];
                double sumX2=array[0]*array[0];
                for(int i=1;i<array.length;i++) {
                        sumX+=array[i];
                        sumX2+=array[i]*array[i];
                }
                mean=sumX/array.length;
                variance=mean*mean-sumX2/(array.length*array.length);
                pdfDenominator=SQRT2PI*Math.sqrt(variance);
                cdfDenominator=SQRT2*Math.sqrt(variance);
        }
        /**
        * Returns the mean.
        */
        public double getMean() {
                return mean;
        }
        /**
        * Returns the variance.
        */
        public double getVariance() {
                return variance;
        }
        /**
        * Probability density function of a normal (Gaussian) distribution.
        * @return the probability that a stochastic variable x has the value X, i.e. P(x=X).
        */
        public double probability(double X) {
                return Math.exp(-(X-mean)*(X-mean)/(2*variance))/pdfDenominator;
        }
        /**
        * Cumulative normal distribution function.
	* @return the probability that a stochastic variable x is less then X, i.e. P(x&lt;X).
        */
        public double cumulative(double X) {
                return SpecialMath.complementaryError(-(X-mean)/cdfDenominator)/2;
        }
        /**
	* Inverse of the cumulative normal distribution function.
        * @return the value X for which P(x&lt;X).
        */
        public double inverse(double probability) {
                checkRange(probability);
                if(probability==0.0)
                        return -Double.MAX_VALUE;
                if(probability==1.0)
                        return Double.MAX_VALUE;
                if(probability==0.5)
                        return mean;
// To ensure numerical stability we need to rescale the distribution
                double meanSave=mean,varSave=variance;
                double pdfDSave=pdfDenominator,cdfDSave=cdfDenominator;
                mean=0.0;
                variance=1.0;
                pdfDenominator=Math.sqrt(TWO_PI);
                cdfDenominator=SQRT2;
                double X=findRoot(probability, 0.0, -100.0, 100.0);
// Scale back
                mean=meanSave;
                variance=varSave;
                pdfDenominator=pdfDSave;
                cdfDenominator=cdfDSave;
                return X*Math.sqrt(variance)+mean;
        }
}

