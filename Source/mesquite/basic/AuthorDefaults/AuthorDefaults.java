/* Mesquite source code.  Copyright 1997-2007 W. Maddison and D. Maddison.
Version 2.0, September 2007.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.basic.AuthorDefaults;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class AuthorDefaults extends DefaultsAssistant {
	MesquiteBoolean authorBlockDefault;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		authorBlockDefault = new MesquiteBoolean(Author.addAuthorBlockByDefault);
		loadPreferences();
		addMenuItem( "Set Author...", makeCommand("setAuthor",  this));
 		addCheckMenuItem(null, "Record Authors by Default", makeCommand("recordAuthors",  this), authorBlockDefault);
		return true;
  	 }
	public void processPreferencesFromFile (String[] prefs) {
		if (prefs!=null && prefs.length>0) {
				if ("&".equals(prefs[0])){
					if (prefs.length > 1){
						authorBlockDefault.setValue("recordAuthors".equalsIgnoreCase(prefs[1]));
						if (prefs.length >2){
							MesquiteModule.author.setName(prefs[2]);
							if (prefs.length>3)
								MesquiteModule.author.setCode(prefs[3]);
		    	 			
						}
				}
				}
				else if ("-*".equals(prefs[0]))
					;
				else {
					MesquiteModule.author.setName(prefs[0]);
					if (prefs.length>1)
						MesquiteModule.author.setCode(prefs[1]);
    	 			}
		}
	}
	public void endJob(){
		super.endJob();
		storePreferences();
	}
	/*.................................................................................................................*/
	public String[] preparePreferencesForFile () {
		String authorsRecDef = "recordAuthors";
		if (!Author.addAuthorBlockByDefault)
			authorsRecDef = "dontrecordAuthorsbyDefault";
		if (!StringUtil.blank(MesquiteModule.author.getName())) {
			if (MesquiteModule.author.getCode() != null) 
				return (new String[] {"&", authorsRecDef, MesquiteModule.author.getName(), MesquiteModule.author.getCode()});
			else
				return (new String[] {"&", authorsRecDef, MesquiteModule.author.getName()});
		}
		else
			return (new String[] {"&", authorsRecDef});
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(MesquiteWindow.class, "Sets the author for this account and machine", null, commandName, "setAuthor")) {
    	 		MesquiteBoolean answer = new MesquiteBoolean(false);
    	 		MesquiteString resp1 = new MesquiteString(MesquiteModule.author.getName());
    	 		MesquiteString resp2 = new MesquiteString(MesquiteModule.author.getCode());
			MesquiteString.queryTwoStrings(containerOfModule(), "Set Author", "Author", "Author code (do not use a number!)", answer, resp1, resp2, false);
			if (answer.getValue()){
				MesquiteModule.author.setName(resp1.getValue());
				MesquiteModule.author.setCode(resp2.getValue());
			}
			storePreferences();
			setCurrentAllProjects();
			return null;

    	 	}
    		else	if (checker.compare(this.getClass(), "Sets default to record authors", null, commandName, "recordAuthors")) {
    			authorBlockDefault.toggleValue(parser.getFirstToken(arguments));
    			Author.addAuthorBlockByDefault = authorBlockDefault.getValue();
    	 		storePreferences();
    	 		return null;
    		}
   	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
   	 }
  	 
  	 protected void setCurrentAllProjects(){
  	 //go through all projects changing current author or adding current author
  	 	Projects p = MesquiteTrunk.mesquiteTrunk.getProjectList();
  	 	for (int i=0; i<p.getNumProjects(); i++){
  	 		MesquiteProject proj = p.getProject(i);
  	 		ListableVector v = proj.getAuthors();
  	 		boolean found = false;
  	 		for (int ia = 0; ia< v.size(); ia++){
  	 			Author au = (Author)v.elementAt(ia);
 	 			if (au.isCurrent()) {
  	 				au.setName(MesquiteModule.author.getName());
  	 				au.setCode(MesquiteModule.author.getCode());
  	 				found = true;
  	 			}
  	 		}
  	 		if (!found){
				Author a = new Author();
				a.setName(MesquiteModule.author.getName());
				a.setCode(MesquiteModule.author.getCode());
				a.setCurrent(true);
				v.addElement(a, true);
  	 		}
  	 	}
  	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Set Author";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Sets the author for this machine and account.";
   	 }
}