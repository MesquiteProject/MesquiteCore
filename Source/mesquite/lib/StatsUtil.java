package mesquite.lib;

import java.util.Arrays;


public class StatsUtil {


	/**
	 * Implements Silverman's Rule of Thumb for bandwidth selection.
	 */
	private static double calculateStandardDeviation(double[] data) {
		double mean = Arrays.stream(data).average().orElse(0.0);
		double sumSq = 0;
		for (double x : data) sumSq += Math.pow(x - mean, 2);
		return Math.sqrt(sumSq / (data.length - 1));
	}
/*
	private static double calculateIQR(double[] data) {
		double q1 = getPercentile(data, 25);
		double q3 = getPercentile(data, 75);
		return q3 - q1;
	}

	private static double getPercentile(double[] sortedData, double percentile) {
		int index = (int) Math.ceil(percentile / 100.0 * sortedData.length);
		return sortedData[Math.max(0, index - 1)];
	}
*/

	private static double calculateIQR(double[] data) {
		double[] sorted = data.clone();
		Arrays.sort(sorted);
		double q1 = sorted[(int) (sorted.length * 0.25)];
		double q3 = sorted[(int) (sorted.length * 0.75)];
		return q3 - q1;
	}


	public static double calculateSilvermanBandwidth(double[] data) {
		int n = data.length;
		double std = calculateStandardDeviation(data);
		double iqr = calculateIQR(data);

		// Silverman's adaptive rule: min(std, iqr/1.34)
		double a = Math.min(std, iqr / 1.34);
		return 0.9 * a * Math.pow(n, -0.2);
	}

	/**
	 * Standard Gaussian Kernel Density Estimation at point x.
	 */

	public static double calculateKDE(double x, double[] data, double h) {
		double sum = 0;
		for (double xi : data) {
			double u = (x - xi) / h;
			sum += gaussianKernel(u);
		}
		return sum / (data.length * h);
	}

	private static double gaussianKernel(double u) {
		return (1.0 / Math.sqrt(2 * Math.PI)) * Math.exp(-0.5 * u * u);
	}

	/* ................................................................................................................. *
	public static boolean[] getOutliersUsingKDE(MesquiteModule ownerModule, double[] xData, double alpha) {
        double[] data = {10, 12, 11, 13, 12, 11, 100}; // 100 is a clear outlier
        Arrays.sort(data);

        double h = calculateSilvermanBandwidth(data);
        ownerModule.logln("Calculated Bandwidth (h): " + h);

       double outlierThreshold = 1.0-alpha;

        for (double x : data) {
            double density = calculateKDE(x, data, h);
            ownerModule.logln("x: " + x +", density: " + density);

            ownerModule.logln("    OUTLIER!");
        }

		return null;
	}
	/* ................................................................................................................. */




	/**
	 * Calculates the CDF at a specific point using KDE with Silverman's Rule.
	 * @param data The input sample data.
	 * @param x The point to evaluate.
	 * @return The estimated cumulative probability.
	 */
	public static double estimateCDF(double[] data, double h, double x) {
		int n = data.length;
		if (n == 0) return 0.0;

		double sum = 0;
		for (double xi : data) {
			sum += standardNormalCDF((x - xi) / h);
		}

		return sum / n;
	}

	/**
	 * Standard Normal CDF approximation (phi function).
	 */
	private static double standardNormalCDF(double z) {
		// Error function based approximation for standard normal CDF
		return 0.5 * (1.0 + erf(z / Math.sqrt(2.0)));
	}

	/**
	 * Approximation of the Error Function (erf).
	 */
	private static double erf(double z) {
		double t = 1.0 / (1.0 + 0.5 * Math.abs(z));
		// Abramowitz and Stegun formula 7.1.26
		double ans = 1 - t * Math.exp(-z * z - 1.26551223 +
				t * (1.00002368 +
						t * (0.37409196 +
								t * (0.09678418 +
										t * (-0.18628806 +
												t * (0.27886807 +
														t * (-1.13520398 +
																t * (1.48851587 +
																		t * (-0.82215223 +
																				t * 0.17087277)))))))));
		return z >= 0 ? ans : -ans;
	}

	public static boolean[] getOutliersUsingKDE(double[] xData, double alpha) {
		boolean[] outliers = new boolean[xData.length];
		for (int i=0; i<outliers.length; i++)
			outliers[i]=false;
		double h = calculateSilvermanBandwidth(xData);

		double outlierThreshold = 1.0-alpha;
		int count = 0;
		for (double x : xData) {
			double cdfValue = estimateCDF(xData, h, x);
			if (cdfValue>outlierThreshold && count<outliers.length)
				outliers[count]=true;
			count++;
		}

		return null;
	}


}
