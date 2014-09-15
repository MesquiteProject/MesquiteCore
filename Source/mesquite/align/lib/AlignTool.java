/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.align.lib;

import java.awt.*;
import java.awt.event.*;
import mesquite.lib.table.*;
import mesquite.lib.*;

/* ======================================================================== */
	/** This subclass of TableTool is used in data windows for aligning data; adds an edgeCursor for between cells.
	*/
public class AlignTool extends TableTool {
	MesquiteCursor edgeCursor=null;
	MesquiteCursor optionEdgeCursor=null;
	
	
	public AlignTool (Object initiator, String name, String imageDirectoryPath, String imageFileName, int hotX, int hotY, String extraImageFileName, int extraHotX, int extraHotY, String fullDescription, String explanation, MesquiteCommand touchedCommand, MesquiteCommand dragCommand, MesquiteCommand droppedCommand) {
		super(initiator, name, imageDirectoryPath, imageFileName, hotX, hotY, fullDescription, explanation, touchedCommand, dragCommand, droppedCommand);
		this.initiator = initiator;
		this.name = name;
		
		edgeCursor = new MesquiteCursor(initiator, name, imageDirectoryPath, extraImageFileName, extraHotX, extraHotY);
	}
	public void setOptionEdgeCursor(String extraImageFileName, int extraHotX, int extraHotY) {
		optionEdgeCursor = new MesquiteCursor(initiator, name, imageDirectoryPath, extraImageFileName, extraHotX, extraHotY);
	}
	public void cursorInCell(int modifiers, int column, int row, int regionInCellH, int regionInCellV, EditorPanel panel){
		if ((regionInCellH<20 ||  regionInCellH>80) && edgeCursor!=null)  {
			setCurrentStandardCursor(edgeCursor);
			if (optionEdgeCursor!=null)
				setCurrentOptionCursor(optionEdgeCursor);
		}
		else {
			setCurrentStandardCursor(null);
			if (optionCursor!=null)
				setCurrentOptionCursor(optionCursor);
		}
	}
	
}

