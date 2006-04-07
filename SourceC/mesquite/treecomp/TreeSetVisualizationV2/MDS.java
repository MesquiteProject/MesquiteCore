/*
 * This software is part of the Tree Set Visualization module for Mesquite,
 * written by Jeff Klingner, Fred Clarke, and Denise Edwards.
 *
 * Copyright (c) 2002 by the University of Texas
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose without fee is hereby granted under the GNU Lesser General 
 * Public License, as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version, 
 * provided that this entire notice is included in all copies of any 
 * software which are or include a copy or modification of this software
 * and in all copies of the supporting documentation for such software.
 *
 * THIS SOFTWARE IS BEING PROVIDED "AS IS", WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTY.  IN PARTICULAR, NEITHER THE AUTHORS NOR THE UNIVERSITY OF TEXAS
 * AT AUSTIN MAKE ANY REPRESENTATION OR WARRANTY OF ANY KIND CONCERNING THE 
 * MERCHANTABILITY OF THIS SOFTWARE OR ITS FITNESS FOR ANY PARTICULAR PURPOSE.
 * IN NO CASE WILL THESE PARTIES BE LIABLE FOR ANY SPECIAL, INCIDENTAL, 
 * CONSEQUENTIAL, OR OTHER DAMAGES THAT MAY RESULT FROM USE OF THIS SOFTWARE.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
	Last change:  DE   15 Apr 2003    2:35 pm
 */

package mesquite.treecomp.TreeSetVisualizationV2;

/* This file isolates everything involved with MDS, primarily to keep file size
 * down.  It includes classes for MDS and its supporting data structures: the
 * difference matrix and the point.
 */

/**
 * Implements Multi-Dimensional Scaling.  some methods are
 * synchronized because an mds object can be affected by two threads at once: by
 * the MDSThread doing the calculations, and by the user interface code responding
 * to user requests that the points be scrambled or changing the step size or the
 * sampling policy.
 */
class MDS {

	/** The goal distances for the dimensional scaling. ("big D") */
	private SampledDiffMatrix targetDistances;
	/** Number of nodes (points) for the dimensional scaling. */
	private int n_nodes;
	/** Dimensionality of the Euclidian space into which the nodes will be embedded. */
	private int n_dims;
	/** Current locations of the embedded points. */
	private MDSPoint[] nodes;
	/** The "nudge" for each point that will improve the embedding. */
	private MDSPoint[] changes;
	/** A scratch variable used in doOneIteration */
	private MDSPoint d;
	/** Global state needed by the xgvis version of mds */
	private float stress, stress_dd, stress_dx, stress_xx;
	/** How agressively we persue an embedding. The value of this is very important and can be adjusted by the user. */
	private float stepSize;

	// Embedding re-centering constants
	/** how far the average point location must drift from the origin before re-centering is triggered */
	protected static final float CENTERING_THRESHOLD = 1e5f; // aribtrary
	/** how many mds iterations occure between each re-centering check */
	protected static final int CENTERING_CHECK_PERIOD = 15; // translational drifting shouldn't really be a problem
	
	private MDSPoint nudgeVector;
	private float nudgeVectorLength;
	private static final float NUDGE_VECTOR_SIZE = 0.0001f;
		

	/**
	 * Constructor for the MDS object
	 *
	 * @param targetDistances  Difference Matrix containing the goal of the embedding
	 * @param n_dims            Number of dimensions to perform the embedding in
	 */
	public MDS(SampledDiffMatrix targetDistances, int n_dims, float stepSize) {
		this.targetDistances = targetDistances;
		this.n_dims = n_dims;
		this.stepSize = stepSize;
		n_nodes = targetDistances.getNumberOfItems();
		// allocate memory for data structures
	    changes = new MDSPoint[n_nodes];
		nodes = new MDSPoint[n_nodes];
		for (int i = 0; i < n_nodes; i++) {
			changes[i] = new MDSPoint(n_dims);
			nodes[i] = new MDSPoint(n_dims);
		}
		d = new MDSPoint(n_dims);
		randomize_nodes(); 
		
		nudgeVector = new MDSPoint(n_dims);
		nudgeVector.zero();
		nudgeVector.setComponent(0,NUDGE_VECTOR_SIZE);
		nudgeVectorLength = nudgeVector.magnitude();
	}
	
	public void resetNumberOfItems(int newNumberOfItems) {
		n_nodes = newNumberOfItems;
		changes = new MDSPoint[n_nodes];
		nodes = new MDSPoint[n_nodes];
		for (int i = 0; i < n_nodes; i++) {
			changes[i] = new MDSPoint(n_dims);
			nodes[i] = new MDSPoint(n_dims);
		}
		randomize_nodes();
	}


	/**
	 * Sets the step size. The caller is trusted to make sure this is a sane value.
	 *
	 * @param newStepSize  The new stepSize value
	 */
	public synchronized void setStepSize(float newStepSize) {
		stepSize = newStepSize;
	}


	/**
	 * Gets the current positions of all the points
	 *
	 * @return   The current embedding
	 */
	public synchronized MDSPoint[] getEmbedding() {
		return nodes;
	}

	/**
	 * Gets the stress of the current MDS configuration
	 *
	 * @return    The stress of the current configuration
	 */
	public synchronized float getStress() {
		return stress;
	}


	/**
	 * Gets the current step size.  Called in response to user action to mannually adjust
	 * the step size, because the step size is not kept as state anywhere else.
	 *
	 * @return   The current step size
	 */
	public synchronized float getStepSize() {
		return stepSize;
	}


	/**
	 * Recenters the points at the origin. Only changes things if the points
	 * are more than CENTERING_THRESHOLD off center.
	 */
	public synchronized void center_embedding() {
		float[] node_means = new float[n_dims];
		// First, compute the "average point" that is the center of the embedding.
		for (int i = 0; i < n_nodes; i++) {
			for (int j = 0; j < n_dims; j++) {
				node_means[j] += nodes[i].getComponent(j);
			}
		}
		for (int j = 0; j < n_dims; j++) {
			node_means[j] /= n_nodes;
		}

		// Second, subtract the "average point" from every node to recenter them at the origin.
		MDSPoint p = new MDSPoint(node_means);
		if (p.magnitude() > CENTERING_THRESHOLD) {
			for (int i = 0; i < n_nodes; i++) {
				nodes[i].subtract(p);
			}
			System.out.println("MDS recentered it's embedding.");
		}
	}


	/** Perform one iteration of MDS */
	public synchronized void doOneIteration() {
		boolean sampling = targetDistances.getSampling();
		//System.out.println("MDS did an iteration. sampling = " + sampling);
		float resid;
		float d_length;
		float stress_diff_accum = 0;
		float stress_norm_accum = 0;

		// First, zero out the change vectors
		for (int i = 0; i < n_nodes; ++i) {
			changes[i].zero();
		}

		// Compute how much each point is pushed or pulled on by each other point
		for (int i = 0; i < n_nodes; ++i) {
			for (int j = 0; j < i; ++j) {
				if (!sampling || targetDistances.diffInSample(i,j)    // short circuiting of the || operator
							  || targetDistances.diffInSample(j,i)) { // avoids unneccesary calls to diffInSample
					d.setToDifference(nodes[j],nodes[i]);
					d_length = d.magnitude();
					// If two points are on top of each other, we need to pick an arbitrary direction to nudge them apart.
					if (d_length == 0) {
						d.add(nudgeVector);
						d_length = nudgeVectorLength;
					}
					resid = d_length - targetDistances.getElement(i, j);
					d.scale(resid/d_length);
					if (!sampling || targetDistances.diffInSample(i,j)) {
						changes[i].add(d); // accumulate the changes for point i
					}
					if (!sampling || targetDistances.diffInSample(j,i)) {
						changes[j].subtract(d); // similarly for j, in the opposite direction
					}
					// accumulate sums for stress calculations
					stress_diff_accum += resid * resid;
					stress_norm_accum += d_length * d_length;
				}
			}
		}
		
		// Finally, apply the changes
		for (int i = 0; i < n_nodes; ++i) {
			changes[i].scale(stepSize);
			nodes[i].add(changes[i]);
		}

		/* Compute stress (this is a normalized stress, called Kruskal-1) */
		if (stress_norm_accum > 0) {
			stress = (float) Math.sqrt(stress_diff_accum/stress_norm_accum);
		} else {
			System.out.println("Stress calculation problem");
		}
		//System.out.println("normalized stress = " + stress);
	}


    /** Compute the current stress */
	public synchronized void compute_stress() {
		float resid;
		float d_length;
		float stress_diff_accum = 0;
		float stress_norm_accum = 0;

		for (int i = 0; i < n_nodes; ++i) {
			for (int j = 0; j < i; ++j) {
				d.setToDifference(nodes[j],nodes[i]);
				d_length = d.magnitude();
				resid = d_length - targetDistances.getElement(i, j);

				// accumulate sums for stress calculations
				stress_diff_accum += resid * resid;
				stress_norm_accum += d_length * d_length;
			}
		}

		// Compute stress (this is a normalized stress, called Kruskal-1)
		if (stress_norm_accum > 0) {
			stress = (float) Math.sqrt(stress_diff_accum/stress_norm_accum);
		} else {
			System.out.println("Stress calculation problem");
		}
	}


	/** Randomizes the embedding. Gives every coordinate of every point a random value between -1 and 1. */
	public synchronized void randomize_nodes() {
		java.util.Random random_source = new java.util.Random();
		for (int i = 0; i < n_nodes; i++) {
			for (int j = 0; j < n_dims; j++) {
				nodes[i].setComponent(j, random_source.nextFloat());
			}
		}
	}
}


/** A simple difference matrix, implemented as a 2D triangular array */
class DiffMatrix {
	
	/** The triangular array to hold differences between items */
	private float[][] diffs;

	/**
	 * Constructor taking the number of items. Initializes a matrix with all
	 * differences set to negative one
	 *
	 * @param n_items  Number of items among which the matirx will hold differences
	 */
	public DiffMatrix(int n_items) {
		resetNumberOfItems(n_items);
	}
	
	public void resetNumberOfItems(int newNumberOfItems) {
		if (newNumberOfItems <= 0) { // There must be at least one item.
			newNumberOfItems = 1;
		}

		diffs = new float[newNumberOfItems][];
		for (int i = 0; i < newNumberOfItems; ++i) {
			// In a triangular matrix, the ith row has i elements, plus the diagonal of zeros
			diffs[i] = new float[i+1];
			for (int j = 0; j < i; ++j) {
				diffs[i][j] = -1; //uninitialized flag value
			}
			diffs[i][i] = 0; // reflexive entry. d(i,i) == 0 for all i
		}
	}


	/**
	 * Initializes the matrix with user-supplied differences.  May be used when the differences
	 * are read in from a file rather that computed.
	 *
	 * @param diffs  Initial differences. Must be at least triangular. (An
	 *      ArrayIndexOutOfBounds exception will be thrown if it is not.)
	 *      Rectangular input here is OK.
	 */
	public DiffMatrix(float[][] diffs) {
		this.diffs = new float[diffs.length][];
		for (int i = 0; i < diffs.length; ++i) {
			this.diffs[i] = new float[diffs[i].length];
			System.arraycopy(diffs[i], 0, this.diffs[i], 0, diffs[i].length);
		}
	}


	/**
	 * Sets one difference in the matrix. The indices can be specified in any
	 * order. For speed, bounds checking on i and j is not done.  You cannot
	 * change the value of the reflexive differences, which are always zero.
	 *
	 * @param i  Index to the first element
	 * @param j  Index to the second element
	 * @param x  New difference between elements i and j
	 */
	public void setElement(int i, int j, float x) {
		if (i == j
			// bounds checking
			// || i >= n_items || i < 0
			// || j >= n_items || j < 0
			) {
		}  // Do nothing
		else if (i > j) {
			diffs[i][j] = x;
		} else { // i < j
			diffs[j][i] = x;
		}
	}


	/**
	 * Returns one difference in the matrix The indices can be specified in any
	 * order.  For speed, bounds checking on i and j is not done.
	 *
	 * @param i  Index to the first element
	 * @param j  Index to the second element
	 * @return   The current difference between i and j
	 */
	public final float getElement(int i, int j) {
		// bounds checking
		// if (   i >= n_items || i < 0
		//	   || j >= n_items || j < 0 ) {
		//	return -1;
		//}	else
		if (i > j) {
			return diffs[i][j];
		} else { // i <= j
			return diffs[j][i];
		}
	}


	/**
	 * Gets the number of items in the difference matrix
	 *
	 * @return   Number of items in the matrix
	 */
	public int getNumberOfItems() {
		return diffs.length;
	}
}


/**
 * A Difference Matrix that supports sampling (only some subset of the
 * distances are necessarily maintained.)
 */
class SampledDiffMatrix extends DiffMatrix {

	/** True if we are sampling a set of points from the overall set of n points.
	 *  This means that if point p is in the sample, then for all other points x,
	 *  (x,p) is active.  Note that (p,x) is not necesarily also active.
	 */
	private boolean samplingByPoint;
	/** true if we are sampling some set of differences in the matrix.  Not all the
	 *  differences for one particular point are necessarily in the sample.
	 *  samplingByPoint and samplingByDiff are never both true.
	 */
	private boolean samplingByDiff;

	/** A bit vector to keep track of which points are in the sample (used for sampling by point) */
	private boolean[] pointSample;
	/** a 2D bit vertor to keep track of which ordered point pairs are in the sample (used for sampling by difference) */
	private boolean[][] diffSample;


	/** constructor to creates a diff matrix with sampling turned off initially */
	public SampledDiffMatrix(int n_items) {
		super(n_items);
		disableSampling();
		pointSample = null;
		diffSample = null;
	}
	
	public void resetNumberOfItems(int newNumberOfItems) {
		super.resetNumberOfItems(newNumberOfItems);
		disableSampling();
		pointSample = null;
		diffSample = null;
	}

	/** Returns true iff sampling by point is enabled */
	public boolean getSamplingByPoint() {
		return samplingByPoint;
	}

	/** returns true iff sampliny by difference is enabled */
	public boolean getSamplingByDiff() {
		return samplingByDiff;
	}

	/** returns true iff any kind of sampling is enabled */
	public boolean getSampling() {
		return (samplingByPoint || samplingByDiff);
	}

	/** turns off all sampling */
	public void disableSampling() {
		samplingByPoint = false;
		samplingByDiff = false;
	}

	/** Returns the current sample size.  This is the number of points if
	 * sampling by point (0..n) or the number of differences if sampling by
	 * diffs (0..n^2).  Returns zero if sampling is not enabled.
	 */
	public int getSampleSize() {
		int sampleSize = 0;
		if (samplingByPoint) {
			for (int i=0; i < pointSample.length; ++i) {
				if (pointSample[i]) {
					++sampleSize;
				}
			}
		} else if (samplingByDiff) {
			for (int i=0; i < diffSample.length; ++i) {
				for (int j=0; j < diffSample.length; ++j) {
					if (diffSample[i][j]) {
						++sampleSize;
					}
				}
			}
		}
		return sampleSize;
	}

	/** Chooses sampleSize random points from the n possibilities.
	 *  Note that if point p is in the sample, then for all other points x,
	 *  (x,p) is active and that (p,x) is not necesarily therefore also active.
	 */
	public void sampleByPoint(int sampleSize) {
		samplingByPoint = true;
		samplingByDiff = false;

		int n = getNumberOfItems();

		// create the sample array if it doesn't already exist.
		if (pointSample == null) {
			pointSample = new boolean[n];
		}

		// First wipe the sample by removing every point from it.
		for (int i = 0; i < n; ++i) {
			pointSample[i] = false;
		}

		java.util.Random randomSource = new java.util.Random(); // seed here for repeatability
		int currentRandomChoice;
		int k;
		for (int i = 0; i < sampleSize; ++i) {
			// loop invariant: of the n total points, i have been chosen and
			// (n - i) are available for the next random selection.)
			currentRandomChoice = randomSource.nextInt(n - i);
			// find the (currentRandomChoice+1)th number in the overall set that
			// hasn't already been chosen.
			k = 0;
			while (pointSample[k]) {++k;} // skip initial trues
			for (int j = 0; j < currentRandomChoice; j++) {
				// loop invariant: we have passed over j falses; sample[k] = false
				++k; // pass over the (j+1)th false
				while (pointSample[k]) {++k;} // skip trues
			}
			pointSample[k] = true; // k is now the index of the (currenRandomChoice+1)th false
		}
	}

	/** Chooses sampleSize different differences at random from the n^2 possibilities.
	 *  Note that d(x,y) is considered to be independent of d(y,x) for this sampling
	 *  even thought d(x,y) always equals d(y,x)
	 */
	public void sampleByDiff(int sampleSize) {
		samplingByPoint = false;
		samplingByDiff = true;

		int n = getNumberOfItems();

		// create the sample array if it doesn't already exist.
		if (diffSample == null) {
			diffSample = new boolean[n][n];
		}

		// First wipe the sample by removing every difference from it.
		for (int i = 0; i < n; ++i) {
			for (int j = 0; j < n; ++j) {
				diffSample[i][j] = false;
			}
		}

		java.util.Random randomSource = new java.util.Random(); // seed here for repeatability
		int currentRandomChoice;
		int k;
		// We will think of the the 2D array of booleans as one long linear array, so that
		// we can step through it with the same logic that was used for sampleByPoint().
		// k is the index into this big unrolled 1D boolean array
		for (int i = 0; i < sampleSize; ++i) {
			// loop invariant: of the n^2 total differences, i have been chosen
			// and (n^2 - i) are available for the next random selection.)
			currentRandomChoice = randomSource.nextInt(n * n - i);
			// find the (currentRandomChoice+1)th number in the overall set that
			// hasn't already been chosen.
			k = 0;
			while (diffSample[k/n][k%n]) {++k;} // skip initial trues
			for (int j = 0; j < currentRandomChoice; j++) {
				// loop invariant: we have passed over j falses; sample[k] = false
				++k; // pass over the (j+1)th false
				while (diffSample[k/n][k%n]) {++k;} // skip trues
			}
			diffSample[k/n][k%n] = true; // k is now the index of the (currenRandomChoice+1)th false
		}
	}

	/** Checks whether or not a point is in the sample.  Returns false if sampling by point is not enabled. */
	public final boolean pointInSample(int n) {
		// relies on short-circuiting of the && operator
		return (samplingByPoint && (n >= 0 && n < getNumberOfItems()) && pointSample[n]);
	}

	/** Checks whether or not a specified difference (order of points matters)
	 * is currently in the sample.  Returns false if sampling is not enabled.
	 * If samplingByPoint, diffInSample(x,p) is true if p is in the sample.
	 * Because this method is called in the inner loop of MDS, it does not
	 * check the bounds of x and y for the sake of speed.  Be careful.
	 */
	public final boolean diffInSample(int x, int y) {
		// relies on short-circuiting of the && and || operators
		//return ((samplingByPoint && pointSample[y]) || (samplingbyDiff && diffSample[x][y]));
		// An if-then-else construction seems to run faster than the big expression on the previous line.
		if (samplingByPoint) {
			return pointSample[y];
		} else if (samplingByDiff) {
			return diffSample[x][y];
		} else {
			return false;
		}
	}
}



/**
 * Mathematical points/vectors of arbitrary dimensionality.
 */
class MDSPoint {
	
	/** Used in the default (no parameter) constructor */
	private final static int DEFAULT_DIMENSIONS = 2;

	/** Number of dimensions in the point (2D, 3D, etc.) */
	private int dimensionality;
	
	/** One value for each dimentions (e.g. x, y, and z components of a vector) */
	private float[] components;
	
	/** A universal loop index used to avoid declaring local variables.  (Local
	 *  variables can prevent the inlining of these fuctions, which are called
	 *  from the inner loop of the MDS iteration method.) */
	private int i;


	/**
	 * Constructor for the MDSPoint object using a component list.
	 *
	 * @param values  Array of component values (one value for each dimension of
	 *      the point) The size of this array specifies the number of dimensions
	 */
	public MDSPoint(float[] values) {
		dimensionality = values.length;
		components = new float[dimensionality];
		System.arraycopy(values, 0, components, 0, values.length);
	}


	/**
	 * Constructor for the MDSPoint object using a dimensionality specification.
	 *
	 * @param ndims  Number of dimensions for the point. The point defaults to the origin.
	 */
	public MDSPoint(int ndims) {
		if (ndims < 1) {
			/* Illegal number of dimensions.  Use the default number instead. */
			ndims = DEFAULT_DIMENSIONS;
		}
		dimensionality = ndims;
		components = new float[ndims];
		for (i = 0; i < ndims; ++i) {
			components[i] = 0;
		}
	}


	/**
	 * Copy constructor for the MDSPoint object
	 *
	 * @param p  Source point
	 */
	public MDSPoint(MDSPoint p) {
		this(p.components);
	}


	/**
	 * Default Constructor for the MDSPoint object. Makes a point at the origin
	 * with DEFAULT_DIMENSIONS dimensions.
	 */
	public MDSPoint() {
		this(DEFAULT_DIMENSIONS);
	}


	/**
	 * Sets one of the component values for a point
	 *
	 * @param c          Which component you want to change?
	 * @param new_value  new value for that component. For efficiency, this
	 *      commonly-called method does not bounds-check c, so make sure that c is
	 *      non-negative and less than the number of dimensions.
	 */
	public void setComponent(int c, float new_value) {
		components[c] = new_value;
	}


	/**
	 * Gets a component of the point.
	 *
	 * @param c  Which component do you want?
	 * @return   the value of component c. For efficiency, this commonly-called
	 *      method does not bounds-check c, so make sure that c is non-negative and
	 *      less than the number of dimensions.
	 */
	public float getComponent(int c) {
		return components[c];
	}


	/**
	 * Gets the dimensionality of the point
	 *
	 * @return   The number of dimensions
	 */
	public int getDimensionality() {
		return dimensionality;
	}


	/** Zeros out a point. Coordinates become the origin. */
	public final void zero() {
		for (i = 0; i < dimensionality; ++i) {
			components[i] = 0;
		}
	}


	/**
	 * Returns a new vector that is the vector difference between two points
	 *
	 * @param p  Vector to be subtracted from this
	 * @return   this - p
	 */
	public MDSPoint difference(MDSPoint p) {
		MDSPoint d = new MDSPoint(dimensionality);
		for (i = 0; i < dimensionality; ++i) {
			d.components[i] = this.components[i] - p.components[i];
		}
		return d;
	}

	/** Slightly less useful than difference(), but much faster, because it
	 *  doesn't need to make a new MDSPoint.
	 */
	public final void setToDifference(MDSPoint p1, MDSPoint p2) {
		for (i = 0 ; i < dimensionality; ++i) {
			components[i] = p1.components[i] - p2.components[i];
		}
	}


	/**
	 * Does vector addition. The vector sum (this + p) is assigned to this.
	 *
	 * @param p  addend; added to this.
	 */
	public final void add(MDSPoint p) {
		for (i = 0; i < dimensionality; ++i) {
			components[i] += p.components[i];
		}
	}


	/**
	 * Does vector subtaction. Modifies this by assigning the vector difference
	 * (this-p) to this.
	 *
	 * @param p  subtend; subtracted from this
	 */
	public final void subtract(MDSPoint p) {
		for (i = 0; i < dimensionality; ++i) {
			components[i] -= p.components[i];
		}
	}


	/**
	 * Treats the point as a vector and computes its magnitude
	 *
	 * @return   the scalar length of the vector specified by this Point
	 */
	public final float magnitude() {
		float accum = 0;
		for (i = 0; i < dimensionality; ++i) {
			accum += components[i] * components[i];
		}
		return (float) Math.sqrt(accum);
	}


	/**
	 * Scales this point by the factor s. For example, the point (1,-2) scaled by -3
	 * becomes (-3,6).
	 *
	 * @param s  the scaling factor
	 */
	public final void scale(float s) {
		for (i = 0; i < dimensionality; ++i) {
			components[i] *= s;
		}
	}


	/**
	 * Treats this point as a vector and normalizes it to length 1 by dividing it
	 * by its length.  The zero vector is left unchanged.
	 */
	public void normalize() {
		float length = this.magnitude();
		if (length > 0) { // You can't normalize the zero vector.
			for (i = 0; i < dimensionality; ++i) {
				components[i] /= length;
			}
		}
	}
}
