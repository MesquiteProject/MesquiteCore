/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.distance.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.duties.*;
import mesquite.lib.*;


/* ======================================================================== */
/** A distance matrix for taxa.*/
public abstract class TaxaDistance {
	Taxa taxa;
	protected Bits taxonBits;
	public TaxaDistance(Taxa taxa){
		this.taxa = taxa;
	}
	public Taxa getTaxa(){
		return taxa;
	}
	public int getNumTaxa(){
		if (taxa==null)
			return 0;
		else
			return taxa.getNumTaxa();
	}
	public abstract double getDistance(int taxon1, int taxon2);
	public abstract boolean isSymmetrical();

	/** This method provides a mechanism, if overridden, to present the user with a dialog box to choose options
	 */
	public boolean getDistanceOptions() {
		return true;
	}

	/** This method should return the array of distances between the taxa contained in Taxa taxa.  
	 * Override this if you want a different method of calculation
	 */
	public double[][] getMatrix() {
		
		double[][] distances= new double[getNumTaxa()][getNumTaxa()];
		for (int taxon1 = 0; taxon1<getNumTaxa(); taxon1++) 
			for (int taxon2=0; taxon2<getNumTaxa(); taxon2++) {
				distances[taxon1][taxon2] = getDistance(taxon1,taxon2);
			}
		return distances;
	}
	
	public double getAverageDistance() {
		double totalDistance = 0.0;
		int count = 0;
		for (int taxon1 = 0; taxon1<getNumTaxa(); taxon1++) 
			for (int taxon2=0; taxon2<getNumTaxa(); taxon2++) {
				if (taxon1<taxon2 && (taxonBits==null || (taxonBits.isBitOn(taxon1) && taxonBits.isBitOn(taxon2)))) {
					double distance = getDistance(taxon1,taxon2);
					if (MesquiteDouble.isCombinable(distance)) {
						totalDistance+= distance;
						count++;
					}
				}
			}
		if (count==0)
			return 0.0;
		return totalDistance/count;
	}
	
	public double getMaximumDistance() {
		double maximumDistance = 0.0;
		int count = 0;
		for (int taxon1 = 0; taxon1<getNumTaxa(); taxon1++) 
			for (int taxon2=0; taxon2<getNumTaxa(); taxon2++) {
				if (taxon1<taxon2 && (taxonBits==null || (taxonBits.isBitOn(taxon1) && taxonBits.isBitOn(taxon2)))) {
					double distance = getDistance(taxon1,taxon2);
					if (MesquiteDouble.isCombinable(distance)) {
						if (distance>maximumDistance)
							maximumDistance= distance;
					}
				}
			}
		return maximumDistance;
	}
	
	public double getMinimumDistance() {
		double minimumDistance = 1.0;
		int count = 0;
		for (int taxon1 = 0; taxon1<getNumTaxa(); taxon1++) 
			for (int taxon2=0; taxon2<getNumTaxa(); taxon2++) {
				if (taxon1<taxon2 && (taxonBits==null || (taxonBits.isBitOn(taxon1) && taxonBits.isBitOn(taxon2)))) {
					double distance = getDistance(taxon1,taxon2);
					if (MesquiteDouble.isCombinable(distance)) {
						if (distance<minimumDistance)
							minimumDistance= distance;
					}
				}
			}
		return minimumDistance;
	}
	
	public double getNthDistance(int n, boolean longest) {
		if (n<1) return 0.0;
		int count = 0;
		for (int taxon1 = 0; taxon1<getNumTaxa(); taxon1++) 
			for (int taxon2=0; taxon2<getNumTaxa(); taxon2++) {
				if (taxon1<taxon2 && (taxonBits==null || (taxonBits.isBitOn(taxon1) && taxonBits.isBitOn(taxon2)))) {
					double distance = getDistance(taxon1,taxon2);
					if (MesquiteDouble.isCombinable(distance)) {
						count++;
					}
				}
			}
		double[] distances = new double[count];
		count=0;
		for (int taxon1 = 0; taxon1<getNumTaxa(); taxon1++) 
			for (int taxon2=0; taxon2<getNumTaxa(); taxon2++) {
				if (taxon1<taxon2 && (taxonBits==null || (taxonBits.isBitOn(taxon1) && taxonBits.isBitOn(taxon2)))) {
					double distance = getDistance(taxon1,taxon2);
					if (MesquiteDouble.isCombinable(distance)) {
						distances[count]=distance;
						count++;
					}
				}
			}
		DoubleArray.sort(distances);
		if (longest)
			return distances[distances.length-n];
		else
			return distances[n-1];
		
	}

	public String getDistanceString(boolean ordered) {
		int count = 0;
		for (int taxon1 = 0; taxon1<getNumTaxa(); taxon1++) 
			for (int taxon2=0; taxon2<getNumTaxa(); taxon2++) {
				if (taxon1<taxon2 && (taxonBits==null || (taxonBits.isBitOn(taxon1) && taxonBits.isBitOn(taxon2)))) {
					double distance = getDistance(taxon1,taxon2);
					if (MesquiteDouble.isCombinable(distance)) {
						count++;
					}
				}
			}
		double[] distances = new double[count];
		count=0;
		for (int taxon1 = 0; taxon1<getNumTaxa(); taxon1++) 
			for (int taxon2=0; taxon2<getNumTaxa(); taxon2++) {
				if (taxon1<taxon2 && (taxonBits==null || (taxonBits.isBitOn(taxon1) && taxonBits.isBitOn(taxon2)))) {
					double distance = getDistance(taxon1,taxon2);
					if (MesquiteDouble.isCombinable(distance)) {
						distances[count]=distance;
						count++;
					}
				}
			}
		if (ordered)
			DoubleArray.sort(distances);
		StringBuffer sb = new StringBuffer();
		for (int i=0;i<distances.length; i++) {
			sb.append(distances[i]+" ");
		}
		return sb.toString();
	}


	public void distancesToLog(){
		MesquiteTrunk.mesquiteTrunk.logln("Sorry, this feature isn't enabled yet for this type of distance.");

	}

	public String getName(){
		return "Unnamed Distance";
	}

}



