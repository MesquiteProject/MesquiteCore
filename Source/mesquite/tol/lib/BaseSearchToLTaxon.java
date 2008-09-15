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
