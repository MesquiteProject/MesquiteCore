// DistanceMatrix.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.distance;

import pal.io.*;
import pal.misc.*;

import java.io.*;
import java.text.*;
import java.util.*;


/**
 * storage for pairwise distance matrices.<p>
 *
 * features:
 * - printing in in PHYLIP format,
 * - computation of (weighted) squared distance to other distance matrix
 * - Fills in all of array...	
 *
 * @version $Id: DistanceMatrix.java,v 1.9 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class DistanceMatrix implements Serializable, IdGroup
{

	/**
	 * This class has a hard-coded serialVersionUID
	 * all fields should be maintained under the same name
	 * for backwards compatibility with serialized objects.
	 */
	static final long serialVersionUID = 4725925229860707633L;

	//
	// Public stuff
	//

	/** 
	 * number of sequences. Should be replaced by method that 
	 * looks directly at size of distance matrix.
	 */
	public int numSeqs = 0;

	/** sequence identifiers */
	public IdGroup idGroup;

 	/** distances [seq1][seq2] */
 	public double[][] distance = null;



	/** constructor */
	public DistanceMatrix()
	{
		format = FormattedOutput.getInstance();
	}

	/** constructor taking distances array and IdGroup */
	public DistanceMatrix(double[][] distance, IdGroup idGroup)
	{
		super();

		this.distance = distance;
		this.idGroup = idGroup;
		numSeqs = distance.length;
		format = FormattedOutput.getInstance();
	}

	/** 
	 * constructor that takes a distance matrix and clones the distances
	 * but uses the same idGroup.
	 */
	public DistanceMatrix(DistanceMatrix dm) {
		distance = new double[dm.getSize()][dm.getSize()];
		for (int i = 0; i < dm.getSize(); i++) {
			for (int j = 0; j < dm.getSize(); j++) {
				distance[i][j] = dm.distance[i][j];
			}
		}
		numSeqs = distance.length;
		format = dm.format;
		idGroup = dm.getIdGroup();
	}

	/**
	 * constructor that takes a distance matrix and clones the distances,
	 * of a the identifiers in idGroup.
	 */
	public DistanceMatrix(DistanceMatrix dm, IdGroup subset) {
		
		int index1, index2;
		
		distance = new double[subset.getIdCount()][subset.getIdCount()];
		for (int i = 0; i < distance.length; i++) {
			index1 = dm.whichIdNumber(subset.getIdentifier(i).getName());
				
			for (int j = 0; j < i; j++) {
				index2 = dm.whichIdNumber(subset.getIdentifier(j).getName());	
				distance[i][j] = dm.distance[index1][index2];
				distance[j][i] = distance[i][j];
			}
		}
		numSeqs = distance.length;
		format = dm.format;
		idGroup = subset;
	}

	/** print alignment (PHYLIP format) */
	public void printPHYLIP(PrintWriter out)
	{
 		// PHYLIP header line
		out.println("  " + numSeqs);

		for (int i = 0; i < numSeqs; i++)
		{
			format.displayLabel(out, 
				idGroup.getIdentifier(i).getName(), 10);
			out.print("      ");

			for (int j = 0; j < numSeqs; j++)
			{
				// Chunks of 6 blocks each
				if (j % 6 == 0 && j != 0)
				{
					out.println();
					out.print("                ");
				}

				out.print("  ");
				format.displayDecimal(out, distance[i][j], 5);
			}
			out.println();
		}
	}
	
	/** returns representation of this alignment as a string */
	public String toString() {
	
		StringWriter sw = new StringWriter();
		printPHYLIP(new PrintWriter(sw));
		
		return sw.toString();
	}
	
	/** compute squared distance to second distance matrix */
	public double squaredDistance(DistanceMatrix mat, boolean weighted)
	{
		double sum = 0;
		for (int i = 0; i < numSeqs-1; i++)
		{
			for (int j = i+1; j < numSeqs; j++)
			{
				double diff = distance[i][j] - mat.distance[i][j];
				double weight;
				if (weighted)
				{
					// Fitch-Margoliash weight
					// (variances proportional to distances)
					weight = 1.0/(distance[i][j]*distance[i][j]);
				}
				else
				{
					// Cavalli-Sforza-Edwards weight
					// (homogeneity of variances)
					weight = 1.0;
				}
				sum += weight*diff*diff;
			}
		}
		
		return 2.0*sum; // we counted only half the matrix
	}

	/** compute absolute distance to second distance matrix */
	public double absoluteDistance(DistanceMatrix mat)
	{   
		double sum = 0;
		for (int i = 0; i < numSeqs-1; i++)
		{
			for (int j = i+1; j < numSeqs; j++)
			{
				double diff = 
					Math.abs(distance[i][j] - mat.distance[i][j]);
				
				sum += diff;
			}
		}
		
		return 2.0*sum; // we counted only half the matrix
	}

	/**
	 * Returns the number of rows and columns that the distance matrix has.
	 */
	public int getSize() {
		return distance.length;
	}

	/**
	 * Returns the distances as a 2-dimensional array of doubles.
	 */
	public double[][] getDistances() {
		return distance;
	}

	/**
	 * Sets both upper and lower triangles.
	 */
	public void setDistance(int i, int j, double dist) {
		distance[i][j] = distance[j][i] = dist;
	}

	/**
	 * Adds a delta to both upper and lower triangle distances.
	 */
	public void addDistance(int i, int j, double delta) {
		distance[i][j] += delta;
		distance[j][i] += delta;
	}

	/**
	 * Returns the mean pairwise distance of this matrix
	 */
	public double meanDistance() {
		double dist = 0.0;
		int count = 0;
		for (int i = 0; i < distance.length; i++) {
			for (int j = 0; j < distance[i].length; j++) {
				if (i != j) {
					dist += distance[i][j];
					count += 1;
				}
			}
		}
		return dist / (double)count;
	}
	
	//IdGroup interface
	public Identifier getIdentifier(int i) {return idGroup.getIdentifier(i);}
	public void setIdentifier(int i, Identifier ident) { idGroup.setIdentifier(i, ident); }
	public int getIdCount() { return idGroup.getIdCount(); }
	public int whichIdNumber(String name) { return idGroup.whichIdNumber(name); }

	/**
	 * Return id group of this alignment. 
	 * @deprecated distance matrix now implements IdGroup
	 */
	public IdGroup getIdGroup() { return idGroup; }


	/**
	 * test whether this matrix is a symmetric distance matrix
	 *
	 */
	public boolean isSymmetric()
	{
		for (int i = 0; i < distance.length; i++)
		{
			if (distance[i][i] != 0) return false;
		}
		for (int i = 0; i < distance.length-1; i++)
		{
			for (int j = i+1; j < distance.length; j++)
			{
				if (distance[i][j] != distance[j][i]) return false;
			}
		}
		return true;
	}
	

	//
	// Private stuff
	//
	
	FormattedOutput format;
}

