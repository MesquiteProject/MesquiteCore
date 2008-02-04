/* Mesquite source code.  Copyright 1997-2007 W. Maddison and D. Maddison.
Version 2.01, December 2007.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib;

import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;

public class UndoReference {
	
	Undoer undoer;
	MesquiteModule responsibleModule;
	
	public UndoReference() {
	}
	
	public UndoReference(CharacterData data, MesquiteModule responsibleModule) {
		UndoInstructions undoInstructions = data.getUndoInstructionsAllData();
		undoInstructions.setNewData(data);
		setUndoer(undoInstructions);
		setResponsibleModule(responsibleModule);
	}

	public UndoReference(CharacterData data, MesquiteModule responsibleModule, int icStart, int icEnd, int itStart, int itEnd) {
		UndoInstructions undoInstructions = new UndoInstructions (UndoInstructions.DATABLOCK, data, data, icStart,icEnd, itStart, itEnd);
		//undoInstructions.setNewData(data);
		setUndoer(undoInstructions);
		setResponsibleModule(responsibleModule);
	}


	public UndoReference(Undoer undoer, MesquiteModule responsibleModule) {
		this.undoer = undoer;
		this.responsibleModule = responsibleModule;
	}

	public MesquiteModule getResponsibleModule() {
		return responsibleModule;
	}

	public void setResponsibleModule(MesquiteModule responsibleModule) {
		this.responsibleModule = responsibleModule;
	}

	public Undoer getUndoer() {
		return undoer;
	}

	public void setUndoer(Undoer undoer) {
		this.undoer = undoer;
	}
	
	

}


