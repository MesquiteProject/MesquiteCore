/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib;

import java.awt.*;
import java.math.*;


public class TreeReference {
	long id, versionNumber, topologyVersion, branchLengthsVersion;
	public TreeReference(){
	}
	
	public long getID(){
		return id;
	}
	public long getVersionNumber(){
		return versionNumber;
	}
	public long getTopologyVersion(){
		return topologyVersion;
	}
	public long getBranchLengthsVersion(){
		return branchLengthsVersion;
	}
	public void setID(long n){
		id = n;
	}
	public void setVersionNumber(long n){
		versionNumber = n;
	}
	public void setTopologyVersion(long n){
		topologyVersion = n;
	}
	public void setBranchLengthsVersion(long n){
		branchLengthsVersion = n;
	}
	
	public String toString(){
		return ("Tree id " + id + "  version number: " + versionNumber + "  topology version: " + topologyVersion + "  branch lengths version: " + branchLengthsVersion);
	}
	/*-----------------------------------------*/
	/* Returns whether tree is the same id, topology, branch lengths and or full version */
 	public boolean equals(TreeReference tr){
 		if (tr==null)
 			return false;
 		if (tr.getID() != getID())
 			return false;
 		if (tr.getTopologyVersion() != getTopologyVersion())
 			return false;
 		if ((tr.getBranchLengthsVersion() != getBranchLengthsVersion()))
 			return false;
 		if ((tr.getVersionNumber() != getVersionNumber()))
 			return false;
 		return true;
 	}
}

