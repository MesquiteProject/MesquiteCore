/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.distance.UPGMA;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.distance.lib.*;

/* ======================================================================== */
//eventually have DistanceTree as treeblock filler, which hires TreeClusterer and TaxaDistanceSource
public class UPGMA extends TreeClusterer {

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
  		return true;
  	 }
	/*.................................................................................................................*/
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Taxa taxa){
   		//
   	}
   	
   	public double getDistanceBetweenClusters(double[][] distanceMatrix, int[] clusterI, int[] clusterJ){
   		if (clusterI == null || clusterJ == null || distanceMatrix == null)
   			return MesquiteDouble.unassigned;
  		double d = 0;
   		int count = 0;
   		for (int i=0; i<clusterI.length; i++)
   			for (int j=0; j<clusterJ.length; j++) {
   				double dist = distanceMatrix[clusterI[i]][clusterJ[j]];
   				if (MesquiteDouble.isCombinable(dist)) {
   					d += dist;
   					count++;
   				}
   			}
   					
   		if (count == 0)
   			return MesquiteDouble.impossible;
   		return d/count;
   	}
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "UPGMA";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Supplies trees obtained from UPGMA clustering.";
   	 }
   	 
}


