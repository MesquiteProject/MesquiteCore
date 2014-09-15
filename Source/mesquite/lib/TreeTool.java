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
import mesquite.lib.duties.*;

/* ��������������������������� commands ������������������������������� */
/* includes commands,  buttons, miniscrolls
/* ======================================================================== */
	/** This subclass of MesquiteTool is used in Tree windows for manipulating the tree, etc.  Each tool
	represents a cursor in the tool palette.  Each has MesquiteCommands attached.<p>
	When a cursor is chosen, its corresponding MesquiteTool is set as
	the current tool.  The tool When a mousedown etc. event occurs on the tree, the tool is passed the tree and the mouse click 
	parameters -- the branch first touched, the branch dropped upon, etc.  If the tool has a touchedCommand, or droppedCommand,
	or touchedTaxonCommand, then this command is executed when the tool is informed of the event.  As argument to the command,
	the branch number(s) are passed in a string.
	*/
public class TreeTool extends TaxaTool {


	public TreeTool (Object initiator,  String name, String imageDirectoryPath, String imageFileName, int hotX, int hotY, String fullDescription, String explanation) {
		super(initiator, name, imageDirectoryPath, imageFileName, hotX, hotY, fullDescription, explanation);
	}


	public void branchTouched (int N, int x, int y, Tree tree, int modifiers) {
		if (touchedCommand!=null) {
			touchedCommand.doItMainThread(Integer.toString(N) + " " + Integer.toString(x)+ " " + Integer.toString(y) + " " + MesquiteEvent.modifiersToString(modifiers), CommandChecker.getQueryModeString("Tool", touchedCommand, this), false, false);  
		}
	}
	public void branchTransferred (int fromN, int toN, Tree tree, int modifiers) {
		if (transferredCommand!=null)
			transferredCommand.doItMainThread(Integer.toString(fromN) + " " + Integer.toString(toN) + " " + MesquiteEvent.modifiersToString(modifiers), CommandChecker.getQueryModeString("Tool", transferredCommand, this), false, false);  
	}
	public void branchDropped (int N, int x, int y, Tree tree, int modifiers) {
		if (droppedCommand!=null)
			droppedCommand.doItMainThread(Integer.toString(N) + " " + Integer.toString(x)+ " " + Integer.toString(y) + " " + MesquiteEvent.modifiersToString(modifiers), CommandChecker.getQueryModeString("Tool", droppedCommand, this), false, false);  
	}


	public void taxonTouched (int M, Tree tree, int modifiers) {
		taxonTouched(M,modifiers);
	}
	public void taxonMoveOver (int M, Tree tree, int modifiers) {
		taxonMoveOver(M,modifiers);
	}
	public void taxonMouseUp (int M, int x, int y, Tree tree, int modifiers) {
		taxonMouseUp(M,x, y, modifiers);
	}
	public boolean fieldTouched (int x, int y, Tree tree, int modifiers) {
		return fieldTouched(x,y,modifiers);
	}
	public boolean fieldMouseUp (int x, int y, Tree tree, int modifiers) {
		return fieldMouseUp(x,y,modifiers);
	}
	public boolean moved (int x, int y,Tree tree, int modifiers) {
		return moved(x,y,modifiers);
	}
	public void branchDragged (int N, int x, int y, Tree tree, int modifiers) {
		if (draggedCommand!=null)
			draggedCommand.doItMainThread(Integer.toString(N) + " " + Integer.toString(x)+ " " + Integer.toString(y) + " " + MesquiteEvent.modifiersToString(modifiers), CommandChecker.getQueryModeString("Tool", draggedCommand, this), false, false);  
	}


}

