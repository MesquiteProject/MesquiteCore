/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.AssignGeneticCode; 

import java.io.*;

import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class AssignGeneticCode extends DataWindowAssistantI {
	MesquiteTable table;
	CharacterData data;
	//MesquiteSubmenuSpec mss= null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem("Genetic Code...", makeCommand("showListWithCode",  this));
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		this.data = data;
	}

	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyDNAData();
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	private void showList(CharacterData data){
		if (data == null && !(data instanceof DNAData))
			return;
		CharactersManager manageCharacters = (CharactersManager)findElementManager(CharacterData.class);

	if (manageCharacters == null)
		return;
		MesquiteModule list = manageCharacters.getListOfCharactersModule(data, true);
		if (list == null) 
			return;
		//here ask each list imployee if they have genetic codes shown; otherwise finish script
		EmployeeVector e = list.getEmployeeVector();
		if (e != null){
			for (int i = 0; i< e.size(); i++)
				if (e.elementAt(i) instanceof mesquite.molec.CharListGenCodeModels.CharListGenCodeModels) //is being shown
					return;
		}
		Puppeteer p = new Puppeteer(this);
		MesquiteInteger pos = new MesquiteInteger(0);

		String commands =  "getWindow; tell It; newAssistant  #mesquite.molec.CharListGenCodeModels.CharListGenCodeModels; endTell;";

		pos.setValue(0);
		p.execute(list, commands, pos, "", false);
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Shows the genetic code assignments", null, commandName, "showListWithCode")) {
			if (table!=null && data !=null){
				showList(data);
			}
		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Show List with Genetic Codes";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Shows the List of Character window with the genetic codes column on." ;
	}

}


