/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
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
	
	public void distancesToLog(){
		MesquiteTrunk.mesquiteTrunk.logln("Sorry, this feature isn't enabled yet for this type of distance.");

	}

	public String getName(){
		return "Unnamed Distance";
	}

}



