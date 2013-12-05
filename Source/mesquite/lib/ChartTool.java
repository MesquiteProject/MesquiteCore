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
package mesquite.lib;

import java.awt.*;
import mesquite.lib.duties.*;

/* еееееееееееееееееееееееееее commands еееееееееееееееееееееееееееееее */
/* includes commands,  buttons, miniscrolls
/* ======================================================================== */
	/** This subclass of MesquiteTool is used in charts.
	*/
public class ChartTool extends MesquiteTool {
	MesquiteCommand touchedCommand;
	MesquiteCommand droppedCommand;
	
	public ChartTool (Object initiator, String name, String imageDirectoryPath, String imageFileName, int hotX, int hotY, String fullDescription, String explanation, MesquiteCommand touchedCommand, MesquiteCommand droppedCommand) {
		super(initiator, name, imageDirectoryPath,  imageFileName, hotX, hotY, fullDescription, explanation);
		this.touchedCommand = touchedCommand;
		this.droppedCommand = droppedCommand;
	}
	
	public void setTouchedCommand(MesquiteCommand touchedCommand){
		this.touchedCommand = touchedCommand;
	}
	public void setDroppedCommand(MesquiteCommand droppedCommand){
		this.droppedCommand = droppedCommand;
	}
	public void dispose(){ 
		if (touchedCommand!=null) 
			touchedCommand.dispose();
		else if (droppedCommand!=null) 
			droppedCommand.dispose();
		touchedCommand=null;
		droppedCommand=null;
		super.dispose();
	}
	
	public void pointTouched (int point, int x, int y, int modifiers) {
		if (touchedCommand!=null)
			touchedCommand.doItMainThread(Integer.toString(point) + " " + x + " " +  y + " " + MesquiteEvent.modifiersToString(modifiers), CommandChecker.getQueryModeString("Tool", touchedCommand, this), false, false);  
	}
	public void pointDropped (int point, int x, int y, int modifiers) {
		if (droppedCommand!=null)
			droppedCommand.doItMainThread(Integer.toString(point) + " " + x + " " +  y + " " + MesquiteEvent.modifiersToString(modifiers), CommandChecker.getQueryModeString("Tool", droppedCommand, this), false, false);  
	}
	}

