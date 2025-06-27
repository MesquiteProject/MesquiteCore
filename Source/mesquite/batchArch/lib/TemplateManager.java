/* Mesquite source code.  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.batchArch.lib; 

import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteString;
import mesquite.lib.ObjectContainer;
import mesquite.lib.duties.FileInit;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.Tree;
import mesquite.lib.ui.ExtensibleDialog;

/* ========== */
public abstract class TemplateManager extends FileInit {
	
	public Class getDutyClass(){
		return TemplateManager.class;
	}
	/*.................................................................................................................*/
	public abstract TemplateRecord chooseTemplate(Taxa taxa);
	public abstract TemplateRecord getTemplate(String name);
	public abstract boolean getTreeHasBeenChosen();
	public abstract ExtensibleDialog getChooseTemplateDLOG(Taxa taxa, String title, ObjectContainer oC, MesquiteInteger buttonPressed, boolean includeMatrices);
	
	public abstract MesquiteString getFileSpecific(String name);
	public abstract Tree getTree();

}
	

