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
	/** This subclass of MesquiteTool is used in Taxon windows for manipulating the taxa, etc.  Each tool
	represents a cursor in the tool palette.  Each has MesquiteCommands attached.<p>
	When a cursor is chosen, its corresponding MesquiteTool is set as
	the current tool.  The tool When a mousedown etc. event occurs on the taxa, the tool is passed the taxon and the mouse click 
	parameters -- the branch first touched, the branch dropped upon, etc.  If the tool has a touchedCommand, or droppedCommand,
	or touchedTaxonCommand, then this command is executed when the tool is informed of the event.  As argument to the command,
	the branch number(s) are passed in a string.
	*/
public class TaxaTool extends MesquiteTool {
	protected MesquiteCommand touchedCommand;
	protected MesquiteCommand droppedCommand;
	protected MesquiteCommand transferredCommand;
	protected MesquiteCommand draggedCommand;
	protected MesquiteCommand touchedTaxonCommand;
	protected MesquiteCommand moveOverTaxonCommand;
	protected MesquiteCommand draggedTaxonCommand;

	protected MesquiteCommand mouseUpTaxonCommand;
	protected MesquiteCommand touchedFieldCommand;
	protected MesquiteCommand mouseUpFieldCommand;
	protected MesquiteCommand movedCommand;
	
	protected boolean ignoreTaxa = false;


	public TaxaTool (Object initiator,  String name, String imageDirectoryPath, String imageFileName, int hotX, int hotY, String fullDescription, String explanation) {
		super(initiator, name, imageDirectoryPath, imageFileName, hotX, hotY, fullDescription, explanation);
	}

	public void dispose(){ 
		if (touchedCommand!=null) 
			touchedCommand.dispose();
		else if (droppedCommand!=null) 
			droppedCommand.dispose();
		else if (transferredCommand!=null) 
			transferredCommand.dispose();
		else if (draggedCommand!=null) 
			draggedCommand.dispose();
		else if (touchedTaxonCommand!=null) 
			touchedTaxonCommand.dispose();
		else if (moveOverTaxonCommand!=null) 
			moveOverTaxonCommand.dispose();
		else if (draggedTaxonCommand!=null)
			draggedTaxonCommand.dispose();
		else if (mouseUpTaxonCommand!=null)
			mouseUpTaxonCommand.dispose();
		else if (touchedFieldCommand!=null) 
			touchedFieldCommand.dispose();
		else if (mouseUpFieldCommand!=null) 
			mouseUpFieldCommand.dispose();
		else if (movedCommand != null)
			movedCommand.dispose();
		touchedCommand=null;
		droppedCommand=null;
		transferredCommand=null;
		draggedCommand=null;
		touchedTaxonCommand=null;
		moveOverTaxonCommand = null;
		mouseUpTaxonCommand=null;
		touchedFieldCommand = null;
		mouseUpFieldCommand = null;
		draggedTaxonCommand = null;
		movedCommand = null;
		super.dispose();
	}
	public boolean getIgnoreTaxa() {
		return (ignoreTaxa);
	}
	public void setIgnoreTaxa(boolean ignoreTaxa) {
		this.ignoreTaxa = ignoreTaxa;
	}
	public boolean informTransfer() {
		return (transferredCommand!=null);
	}
	public boolean informDrop() {
		return (droppedCommand!=null);
	}
	public boolean informTouch() {
		return (touchedCommand!=null);
	}
	public boolean informDrag() {
		return (draggedCommand!=null);
	}
	public boolean informMove() {
		return (movedCommand!=null);
	}
	public boolean informDragTaxon(){
		return (draggedTaxonCommand!=null);
	}
	public boolean informTouchField() {
		return (touchedFieldCommand!=null);
	}
	public boolean informMouseUpTaxon() {
		return (mouseUpTaxonCommand!=null);
	}
	public boolean informMouseUpField() {
		return (mouseUpFieldCommand!=null);
	}
	public void setTransferredCommand(MesquiteCommand transferredCommand){
		this.transferredCommand = transferredCommand;
	}
	public void setTouchedCommand(MesquiteCommand touchedCommand){
		this.touchedCommand = touchedCommand;
	}
	public void setDraggedCommand(MesquiteCommand draggedCommand){
		this.draggedCommand = draggedCommand;
		if (draggedCommand != null)
			draggedCommand.setSuppressLogging(true);
	}
	public void setDroppedCommand(MesquiteCommand droppedCommand){
		this.droppedCommand = droppedCommand;
	}
	public void setTouchedTaxonCommand(MesquiteCommand touchedTaxonCommand){
		this.touchedTaxonCommand = touchedTaxonCommand;
	}
	public void setMoveOverTaxonCommand(MesquiteCommand moveOverTaxonCommand){
		this.moveOverTaxonCommand = moveOverTaxonCommand;
		if (moveOverTaxonCommand != null)
			moveOverTaxonCommand.setSuppressLogging(true);
	}
	public void setMouseUpTaxonCommand(MesquiteCommand mouseUpTaxonCommand){
		this.mouseUpTaxonCommand = mouseUpTaxonCommand;
	}
	public void setDraggedTaxonCommand(MesquiteCommand draggedTaxonCommand){
		this.draggedTaxonCommand = draggedTaxonCommand;
		if (draggedTaxonCommand != null)
			draggedTaxonCommand.setSuppressLogging(true);
	}
	public void setTouchedFieldCommand(MesquiteCommand touchedFieldCommand){
		this.touchedFieldCommand = touchedFieldCommand;
	}
	public void setMouseUpFieldCommand(MesquiteCommand mouseUpFieldCommand){
		this.mouseUpFieldCommand = mouseUpFieldCommand;
	}
	public void setMovedCommand(MesquiteCommand movedCommand){
		this.movedCommand = movedCommand;
		if (movedCommand != null)
			movedCommand.setSuppressLogging(true);
	}

	public void taxonMoveOver (int M, int modifiers) {
		if (moveOverTaxonCommand!=null)
			moveOverTaxonCommand.doItMainThread(Integer.toString(M) + " "  + MesquiteEvent.modifiersToString(modifiers), CommandChecker.getQueryModeString("Tool", moveOverTaxonCommand, this), false, false);  
	}

	public void taxonTouched (int M, int modifiers) {
		if (touchedTaxonCommand!=null)
			touchedTaxonCommand.doItMainThread(Integer.toString(M) + " "  + MesquiteEvent.modifiersToString(modifiers), CommandChecker.getQueryModeString("Tool", touchedTaxonCommand, this), false, false);  
	}
	
	
	
	public void taxonMouseUp (int M, int x, int y, int modifiers) {
		if (mouseUpTaxonCommand!=null)
			mouseUpTaxonCommand.doItMainThread(Integer.toString(M) + " "  + Integer.toString(x)+ " " + Integer.toString(y) + " "  + MesquiteEvent.modifiersToString(modifiers), CommandChecker.getQueryModeString("Tool", mouseUpTaxonCommand, this), false, false);  
	}

	public void taxonDragged (int M, int x, int y, int modifiers) {
		if (draggedTaxonCommand!=null)
			draggedTaxonCommand.doItMainThread(Integer.toString(M) + " "  + Integer.toString(x)+ " " + Integer.toString(y) + " " +MesquiteEvent.modifiersToString(modifiers), CommandChecker.getQueryModeString("Tool", draggedTaxonCommand, this), false, false);  
	}	
	
	
	public boolean fieldTouched (int x, int y, int modifiers) {
		if (touchedFieldCommand!=null) { 
			touchedFieldCommand.doItMainThread(Integer.toString(x)+ " " + Integer.toString(y) + " " + MesquiteEvent.modifiersToString(modifiers), CommandChecker.getQueryModeString("Tool", touchedFieldCommand, this), false, false);  
			return true;
		}
		else
			return false;
	}
	public boolean fieldMouseUp (int x, int y, int modifiers) {
		if (mouseUpFieldCommand!=null) { 
			mouseUpFieldCommand.doItMainThread(Integer.toString(x)+ " " + Integer.toString(y) + " " + MesquiteEvent.modifiersToString(modifiers), CommandChecker.getQueryModeString("Tool", mouseUpFieldCommand, this), false, false);  
			return true;
		}
		else
			return false;
	}
	public boolean moved (int x, int y, int modifiers) {
		if (movedCommand!=null) {
			movedCommand.doItMainThread(Integer.toString(x)+ " " + Integer.toString(y) + " " + MesquiteEvent.modifiersToString(modifiers), CommandChecker.getQueryModeString("Tool", movedCommand, this), false, false);  
			return true;
		}
		else
			return false;
	}
}

