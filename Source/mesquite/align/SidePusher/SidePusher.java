/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.align.SidePusher; 

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.align.lib.*;
import mesquite.categ.lib.*;

/* ======================================================================== */
public class SidePusher extends BlockMover {
	public void getSubfunctions(){
		registerSubfunction(new FunctionExplanation("Grab right/left", "Grabs right or left", null, getPath() + getCellToolImageFileName()));
		registerSubfunction(new FunctionExplanation("Split block right/left", "Splits block left or right", null, getPath() + getSplitToolImageFileName()));
		super.getSubfunctions();
	}
	/*.................................................................................................................*/
   	 public  String getToolName(){
		return "push";
   	 }
	/*.................................................................................................................*/
   	 public  String getCellToolImageFileName(){
		return "rightGrab.gif";
   	 }
	/*.................................................................................................................*/
   	 public  Point getCellToolHotSpot(){
		return new Point (1,8);
   	 }
	/*.................................................................................................................*/
   	 public  String getSplitToolImageFileName(){
		return "splitBlockRight.gif";
   	 }
	/*.................................................................................................................*/
   	 public  Point getSplitToolHotSpot(){
		return new Point (8,8);
   	 }
	/*.................................................................................................................*/
   	 public  String getExplanationForTool(){
		return "This tool pushes the sequences for manual alignment.";
   	 }
	/*.................................................................................................................*/
   	 public  String getFullDescriptionForTool(){
		return "Push Sequence";
   	 }
	/*.................................................................................................................*/
   	 public boolean allowSplits(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Side Pusher";
   	 }
	/*.................................................................................................................*/
   	 public boolean canMoveRight(){
   	 	return !getOptionDown();
   	 }
	/*.................................................................................................................*/
   	 public boolean canMoveLeft(){
   	 	return getOptionDown();
   	 }
	/*.................................................................................................................*/
   	 public boolean wholeSequenceToLeft(){
   	 	return getOptionDown();
   	 }
	/*.................................................................................................................*/
   	 public boolean wholeSequenceToRight(){
   	 	return !getOptionDown();
   	 }
   	/*.................................................................................................................*/
  	 public boolean isPrerelease(){
  	 	return false;
  	 }
	   /*.................................................................................................................*/
   	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
   	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
   	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
      	public int getVersionOfFirstRelease(){
      		return -100;  
    }
	/*.................................................................................................................*/
   	 public void setOptionTools(){
    	 	moveTool.setOptionImageFileName("leftGrab.gif", 15,8);
    	 	moveTool.setOptionEdgeCursor("SplitBlockLeft.gif", 8,8);
  	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Pushes all sequences on one side." ;
   	 }
   	 
}


