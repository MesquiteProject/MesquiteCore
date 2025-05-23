/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.tree;


import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteInteger;

/* ======================================================================== */
/**This class is used by TreeDisplayExtras to give requests for extra room on the borders, especially for vertical or horizontal trees*/
public class TreeDisplayRequests {  
	/*These requests do NOT sum; the tree drawer will try to ensure there is at least this much outside the 
	 * bounds of the left and right tips, the highest tip, and the root node
	
		left, top, right, bottom borders are measured in pixels and in actually screen orientation, not relative to the root-tip direction of the tree
		
		tipsFieldDistance is measured in pixels, and in tree orientation. Used, e.g. in Character State Boxes
		
		extraDepthAtRoot is measured in tree length units, and are rootward regardless of screen orientation
	 * */
	public int leftBorder = 0; //in pixels
	public int topBorder = 0; //in pixels
	public int rightBorder = 0; //in pixels
	public int bottomBorder = 0; //in pixels
	public int tipsFieldDistance = 0; //in pixels
	public double extraDepthAtRoot = 0; //in branch length dimensions
	
	public int tipsFieldBase = 0;
	static int separation = 4;
	
	public TreeDisplayRequests(){
	}
	public TreeDisplayRequests(int left, int top, int right, int bottom, int tipsDistance, double rootward){
		leftBorder = left; 
		topBorder = top; 
		rightBorder = right; 
		bottomBorder = bottom; 
		tipsFieldDistance = tipsDistance;
		extraDepthAtRoot = rootward; 
	}
	
	//This is called ONLY by the treeDisplay to accumulate the overall border and to set the bases of each if the fields. 
	//Thus, the only object in which it is called is the overall request in treeDisplay.
	public void mergeFrom(TreeDisplayRequests other){
		leftBorder = MesquiteInteger.maximum(leftBorder, other.leftBorder);
		topBorder = MesquiteInteger.maximum(topBorder, other.topBorder);
		rightBorder = MesquiteInteger.maximum(rightBorder, other.rightBorder);
		bottomBorder = MesquiteInteger.maximum(bottomBorder, other.bottomBorder);
		other.tipsFieldBase = tipsFieldBase + separation; //telling the other where its field starts
		if (other.tipsFieldDistance>0)
			tipsFieldBase += separation + other.tipsFieldDistance;  //accumulating the total field width
		tipsFieldDistance = tipsFieldBase;  //accumulating the total field width
		extraDepthAtRoot = MesquiteDouble.maximum(extraDepthAtRoot, other.extraDepthAtRoot);
	}
	
	public static boolean equal(TreeDisplayRequests a, TreeDisplayRequests b){
		if (a == b)
			return true;
		if (a == null || b == null)
			return false;
		return (a.leftBorder == b.leftBorder && a.topBorder == b. topBorder && a.rightBorder == b.rightBorder && a.bottomBorder == b.bottomBorder && a.extraDepthAtRoot == b.extraDepthAtRoot);
	}
}



