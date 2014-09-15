/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.lib; 
/*~~  */

import java.util.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import java.awt.datatransfer.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;
import mesquite.charMatrices.lib.*;
import mesquite.charMatrices.BasicDataWindowMaker.*;

	
/* ======================================================================== */
public abstract class DataWindow extends TableWindow {
	MesquiteTable mesquiteTable;
	CharacterData data;
	BasicDataWindowMaker ownerModule;

	public DataWindow () {
	}
	public DataWindow (BasicDataWindowMaker ownerModule, CharacterData data) {
		super(ownerModule, true); //INFOBAR
		ownerModule.setModuleWindow(this);
		this.data = data;
		this.ownerModule = ownerModule;
	}
	/*.................................................................................................................*/
//    	 public MesquiteTable getTable() {
//		return mesquiteTable; 
//   	 }
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree blocks, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		String t;
		if (data != null && data.hasTitle()) {
			t = "Character Matrix \"" + data.getName() + "\"";
		}
		else {
			t = "Character Matrix";
		}
		setTitle(t);
	}
	/*.................................................................................................................*/
	public int numDataColumnNamesAssistants(){
		int num=0;
		for (int i = 0; i<ownerModule.getNumberOfEmployees(); i++) { 
			MesquiteModule e=(MesquiteModule)ownerModule.getEmployeeVector().elementAt(i);
			if (e instanceof DataColumnNamesAssistant) {
				num++;
			}
		}
		return num;
	}
	/*.................................................................................................................*/
	public DataColumnNamesAssistant getDataColumnNamesAssistant(int num){
		int count = 0;
		for (int i = 0; i<ownerModule.getNumberOfEmployees(); i++) { 
			MesquiteModule e=(MesquiteModule)ownerModule.getEmployeeVector().elementAt(i);
			if (e instanceof DataColumnNamesAssistant) {
				if (count>=num)
					return (DataColumnNamesAssistant)e;
				count++;
			}
		}
		return null;
	}
   	 
}



