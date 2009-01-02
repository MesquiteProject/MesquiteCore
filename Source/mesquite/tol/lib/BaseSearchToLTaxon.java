/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison. 
Version 2.6, January 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.tol.lib;

import java.util.Vector;

import mesquite.lib.CommandChecker;
import mesquite.lib.TreeDisplay;
import mesquite.lib.TreeDisplayExtra;
import mesquite.lib.duties.TreeDisplayAssistantI;
import mesquite.tol.lib.*;

public abstract class BaseSearchToLTaxon extends TreeDisplayAssistantI {
		public Vector extras;
		/*.................................................................................................................*/
		public boolean startJob(String arguments, Object condition, boolean hiredByName){
			extras = new Vector();
			return true;
		} 
		
		/*.................................................................................................................*/
		public abstract String getBaseURLForUser();


		/*.................................................................................................................*/
		public String getName() {
			return "Get Taxon's tree from "+getBaseURLForUser();
		}
		
		/*.................................................................................................................*/
		public Object doCommand(String commandName, String arguments, CommandChecker checker) { 

			if (checker.compare(this.getClass(), "Turns on tools", null, commandName, "enableTools")) {
				for (int i=0; i<extras.size(); i++){
					BaseSearchToLToolTaxonExtra e = (BaseSearchToLToolTaxonExtra)extras.elementAt(i);
					e.enableTool();
				}
			}
			else
				return  super.doCommand(commandName, arguments, checker);
			return null;
		}


		/*.................................................................................................................*/
		/** returns an explanation of what the module does.*/
		public String getExplanation() {
			return "Supplies a tool for tree windows that gets tree for taxon touched from "+getBaseURLForUser();
		}
		public boolean isSubstantive(){
			return false;
		}   	 
	}
