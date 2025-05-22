/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.align.SimpleBlockMover; 


import java.awt.*;
import mesquite.align.lib.*;
import mesquite.categ.lib.CategoricalData;
import mesquite.lib.CommandChecker;
import mesquite.lib.CommandRecord;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteModule;
import mesquite.lib.Notification;
import mesquite.lib.Snapshot;
import mesquite.lib.ui.MesquiteWindow;


/* ======================================================================== */
public class SimpleBlockMover extends BlockMover {
	MesquiteBoolean selectedBlock;
	boolean defaultSelectedBlock  =false;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		boolean superStart = super.startJob(arguments, condition, hiredByName);
		if (!superStart)
			return false;
		selectedBlock = new MesquiteBoolean(defaultSelectedBlock);
		//addCheckMenuItem(null, "Move Selected Block", makeCommand("toggleSelectedBlock",  this), selectedBlock);
		return true;
	}
	/*.................................................................................................................*
 	 public Snapshot getSnapshot(MesquiteFile file) {
 	 	Snapshot temp = new Snapshot();
		if (selectedBlock.getValue()!=defaultSelectedBlock)
			temp.addLine("toggleSelectedBlock " + selectedBlock.toOffOnString());
 	 	return temp;
 	 }
   	/*.................................................................................................................*
	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
	  if (checker.compare(this.getClass(), "Toggles whether the selected block is moved", "[on; off]", commandName, "toggleSelectedBlock")) {
	 		boolean current = selectedBlock.getValue();
	 		selectedBlock.toggleValue(parser.getFirstToken(arguments));
	 	}
	 	else
	 		return  super.doCommand(commandName, arguments, checker);
	 	return null;
	 }
	 
		/*.................................................................................................................*/
   	 public boolean wholeSelectedBlock(){
   	 	return selectedBlock.getValue();
   	 }
 	protected void adjustSelection() {
		table.deselectAll();
	}
	/*.................................................................................................................*/
   	 public  String getToolName(){
		return "blockMover";
   	 }
 	/*.................................................................................................................*/
   	 public void setOptionTools(){
    	 	moveTool.setOptionImageFileName("Hand.gif", 8,8);
    	 	moveTool.setOptionEdgeCursor("Hand.gif", 8,8);
  	 }
  	/*.................................................................................................................*/
   	 public boolean moveWholeSequence() {
   		return getOptionDown();
   	 }
  	/*.................................................................................................................*/
 	public boolean wholeSequenceToLeft(){
 		return moveWholeSequence();
 	}
 	/*.................................................................................................................*/
 	public boolean wholeSequenceToRight(){
 		return moveWholeSequence();
 	}
	/*.................................................................................................................*/
   	 public  String getCellToolImageFileName(){
		return "BlockMover.gif";
   	 }
	/*.................................................................................................................*/
   	 public  Point getCellToolHotSpot(){
		return new Point (8,8);
   	 }
	/*.................................................................................................................*/
   	 public  String getSplitToolImageFileName(){
		return "splitBlock.gif";
   	 }
	/*.................................................................................................................*/
   	 public  Point getSplitToolHotSpot(){
		return new Point (8,8);
   	 }
	/*.................................................................................................................*/
   	 public  String getExplanationForTool(){
		return "This tool moves blocks of sequences for manual alignment.";
   	 }
	/*.................................................................................................................*/
   	 public  String getFullDescriptionForTool(){
		return "Move Blocks";
   	 }
 	/*.................................................................................................................*/
  	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
  	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
  	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
     	public int getVersionOfFirstRelease(){
     		return -100;  
     	}
    	/*.................................................................................................................*/
      	 public boolean isPrerelease(){
      	 	return false;
      	 }
 	/*.................................................................................................................*/
   	 public boolean allowSplits(){
  		return !moveWholeSequence();
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Move block";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Allows one to manually blocks in a sequence, and split the blocks." ;
   	 }
   	 
}


