/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.distance.SingleLinkage;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.distance.lib.*;

/* ======================================================================== */
//eventually have DistanceTree as treeblock filler, which hires TreeClusterer and TaxaDistanceSource
public class SingleLinkage extends TreeClusterer {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
  		return true;
  	 }
	/*.................................................................................................................*/
   	public double getDistanceBetweenClusters(double[][] distanceMatrix, int[] clusterI, int[] clusterJ){
   		if (clusterI == null || clusterJ == null || distanceMatrix == null)
   			return MesquiteDouble.unassigned;
   		double d = MesquiteDouble.unassigned;
   		for (int i=0; i<clusterI.length; i++)
   			for (int j=0; j<clusterJ.length; j++) {
   				if (distanceMatrix != null && clusterI[i] < distanceMatrix.length && clusterJ[j]< distanceMatrix[clusterI[i]].length){
   					double dist = distanceMatrix[clusterI[i]][clusterJ[j]];
				if (MesquiteDouble.lessThan(dist, d, 0))
					d = dist;
   				}
   			}
   					
   		return d;
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Single Linkage";
   	 }
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Supplies trees obtained from Single Linkage clustering.";
   	 }
   	 
}


