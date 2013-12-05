// DistanceMatrixUtils.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.distance;

import pal.io.*;
import pal.misc.*;

import java.io.*;
import java.util.*;


/**
 * Auxillary functions for distance matrices<p> 
 *
 * @version $Id: DistanceMatrixUtils.java,v 1.5 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Alexei Drummond
 */
public class DistanceMatrixUtils implements Serializable {
		
	/** 
	 * compute squared distance to second distance matrix.
	 * If both matrices have the same size it is assumed that the order of the taxa
	 * is identical. 
	 */
	public static double squaredDistance(DistanceMatrix mat1, DistanceMatrix mat2, boolean weighted) {

		boolean aliasNeeded = false;
		if (mat1.getSize() != mat2.getSize())
		{
			aliasNeeded = true;
		}
		
		int[] alias = null;
		
		if (aliasNeeded) {
			if (mat1.getSize() > mat2.getSize()) {
				//swap so mat1 is the smaller of the two
				DistanceMatrix temp = mat2;
				mat2 = mat1;
				mat1 = temp;
			}
			alias = new int[mat1.getSize()];
			for (int i = 0; i < alias.length; i++) {
				alias[i] = mat2.whichIdNumber(mat1.getIdentifier(i).getName());
			}
		} else {
			alias = new int[mat1.getSize()];
			for (int i = 0; i < alias.length; i++) {
				alias[i] = i;
			}
		}
			
		
		double sum = 0;
		int ai;
		for (int i = 0; i < mat1.numSeqs-1; i++)
		{
			ai = alias[i];	
		
			for (int j = i+1; j < mat1.numSeqs; j++)
			{
				double diff = mat1.distance[i][j] - mat2.distance[ai][alias[j]];
				double weight;
				if (weighted)
				{
					// Fitch-Margoliash weight
					// (variances proportional to distances)
					weight = 1.0/(mat1.distance[i][j]*mat2.distance[ai][alias[j]]);
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

	/**
	 * Returns a distance matrix with the specified taxa removed.
	 */
	public static DistanceMatrix minus(DistanceMatrix parent, int taxaToRemove) {
	
		int size = parent.getIdCount() - 1;
	
		double[][] distances = new double[size][size];
		Identifier[] ids = new Identifier[size];
		int counti = 0, countj = 0;
		for (int i = 0; i < size; i++) {
			if (counti == taxaToRemove) {
				counti += 1;
			}
			ids[i] = parent.getIdentifier(counti);
		
			countj = 0;
			for (int j = 0; j < size; j++) {
				if (countj == taxaToRemove) {
					countj += 1;
				}
				distances[i][j] = parent.distance[counti][countj];
				countj += 1;
			}
			counti += 1;
		}
	
		DistanceMatrix smaller = new DistanceMatrix(distances, new SimpleIdGroup(ids));

		return smaller;
	}
}

