/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.basic.RawNexusBlockEditor;

import java.util.*;
import java.io.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class RawNexusBlockEditor extends EditRawNexusBlock {
	NexusBlockEditableRaw currentlyEdited = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
  	 }
  	 
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	if (getModuleWindow()!=null && getModuleWindow().isVisible()) {
	   	 	Snapshot temp = new Snapshot();
			temp.addLine("getWindow");
	  	 	Snapshot fromWindow = getModuleWindow().getSnapshot(file);
			temp.addLine("tell It");
			temp.incorporate(fromWindow, true);
			temp.addLine("endTell");
			temp.addLine("showWindow");
	 	 	return temp;
 	 	}
 	 	else return null;
  	 }
  	
  	 public void windowGoAway(MesquiteWindow whichWindow) {
 		if (whichWindow == null)
			return;
		if (currentlyEdited!=null) {
  	 		currentlyEdited.setText( ((MesquiteTextWindow)getModuleWindow()).getText());
  	 	}
  	 	currentlyEdited = null;
  	 	getModuleWindow().hide();
  	 	//window.dispose();
  	 }
	public void recordBlock(NexusBlockEditableRaw block){

		if (block == currentlyEdited && currentlyEdited!=null && getModuleWindow()!=null){
  	 		currentlyEdited.setText( ((MesquiteTextWindow)getModuleWindow()).getText());
  	 	}
	}
   	public NexusBlockEditableRaw getCurrentBlock(){
   		return currentlyEdited;
   	}
	
	public void editNexusBlock(NexusBlockEditableRaw block, boolean recordCurrent){
			if (block==null)
				return;
			if (getModuleWindow()==null) {
				setModuleWindow( new MesquiteTextWindow(this, block.getBlockName() + " Block: " + block.getName(), true)); //infobar
				getModuleWindow().setWindowSize(300,300);
		 		resetContainingMenuBar();
				resetAllWindowsMenus();
			}
			else {
				//if getModuleWindow() already open, then store contents and switch contents
				if (getModuleWindow().isVisible() && recordCurrent && currentlyEdited!=null) {
					currentlyEdited.setText(((MesquiteTextWindow)getModuleWindow()).getText());
				}
				getModuleWindow().setTitle(block.getName());
			}
			currentlyEdited=block;
			((MesquiteTextWindow)getModuleWindow()).setText(currentlyEdited.getText());
			((MesquiteTextWindow)getModuleWindow()).setEditable(true);
			//getModuleWindow().setVisible(true); TODO: pass scripting
	}
	public String getCurrentContents(){
		if (getModuleWindow()!=null && getModuleWindow().isVisible())
  	 		return ((MesquiteTextWindow)getModuleWindow()).getText();
  	 	return null;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Raw Nexus block editor";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Edits blocks in a NEXUS file." ;
   	 }
}

