/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.basic.ReplaceInElementNames;

import java.awt.Label;

import mesquite.lib.CommandChecker;
import mesquite.lib.Listable;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Nameable;
import mesquite.lib.NameableWithNotify;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.ListableNameAlterer;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.SingleLineTextField;

/* ======================================================================== 
 */
public class ReplaceInElementNames extends ListableNameAlterer {
	String searchText="";
	String replaceText = "";
//	MesquiteBoolean addToEnd = new MesquiteBoolean(true);
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	/*.................................................................................................................*/
   	public boolean getOptions(ListableVector datas, int firstSelected){
   		if (MesquiteThread.isScripting())
   			return true;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Replace in " + getElementNameSingular() + " names",  buttonPressed);
		queryDialog.addLabel("Replace in " + getElementNameSingular() + " names", Label.CENTER);
		SingleLineTextField searchField = queryDialog.addTextField("Search for:", searchText, 30, true);
		SingleLineTextField replaceField = queryDialog.addTextField("Replace with:", replaceText, 20, true);
		queryDialog.completeAndShowDialog(true);
			
		boolean ok = (queryDialog.query()==0);
		
		if (ok) {
			searchText = searchField.getText();
			replaceText = replaceField.getText();
		}
		
		queryDialog.dispose();

		return ok;
   	}
	/*.................................................................................................................*/
   	/** Called to alter the element name in a single cell.  If you use the alterContentOfCells method of this class, 
   	then you must supply a real method for this, not just this stub. */
   	public boolean alterName(ListableVector elements, int it){
   		boolean nameChanged = false;
   		Object obj = elements.elementAt(it);
   		if (obj == null || !(obj instanceof Nameable))
   			return false;
   		String name = ((Listable)obj).getName();

		if (name!=null){
			String s=StringUtil.replace(name,searchText,replaceText);
			if (obj instanceof NameableWithNotify)
				((NameableWithNotify)obj).setName( s, false);
			else
				((Nameable)obj).setName( s);
			nameChanged = !name.equals(s);
		}
		return nameChanged;
   	}
	
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Replaces text in names", "[text]", commandName, "replaceText")) {
	   	 		if (elements !=null){
	   	 			 searchText = parser.getFirstToken(arguments);
	   	 			 replaceText = parser.getFirstToken(arguments);
	   	 			alterElementNames(elements, table);
	   	 		}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
    	 
   /*.................................................................................................................*/
    	 public boolean requestPrimaryChoice(){
    		 return true;
    	 }

	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Replace in Name...";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Replace text in element names";
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Replaces text in names in list.";
   	 }
}


	


