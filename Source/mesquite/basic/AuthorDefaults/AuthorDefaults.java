/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


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
		addMenuItemToDefaults( "Set Author...", makeCommand("setAuthor",  this));
		addCheckMenuItemToDefaults(null, "Record Authors by Default", makeCommand("recordAuthors",  this), authorBlockDefault);
		return true;
	}
	public void endJob(){
		super.endJob();
		storePreferences();
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
	public void processSingleXMLPreference (String tag, String content) {
		if ("authorBlockDefault".equalsIgnoreCase(tag)){
			authorBlockDefault.setValue(content);
			Author.addAuthorBlockByDefault = authorBlockDefault.getValue();

		}
		else if ("authorName".equalsIgnoreCase(tag)){
			MesquiteModule.author.setName(content);
		}
		else if ("authorCode".equalsIgnoreCase(tag)){
			MesquiteModule.author.setCode(content);
		}
	}
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "authorBlockDefault", authorBlockDefault);  
		StringUtil.appendXMLTag(buffer, 2, "authorName", MesquiteModule.author.getName());
		StringUtil.appendXMLTag(buffer, 2, "authorCode", MesquiteModule.author.getCode());    
		return buffer.toString();
	}
	/*.................................................................................................................*
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
	/**/



	/*.................................................................................................................*/
	Author findAuthorIdentical(String name, String code){
		if (code == null || name == null)
			return null;
		Projects p = MesquiteTrunk.getProjectList();
		for (int i=0; i<p.getNumProjects(); i++){
			MesquiteProject proj = p.getProject(i);
			if (proj.numAuthors()>0){
				ListableVector v = proj.getAuthors();
				for (int k = 0; k< v.size(); k++){
					Author a = (Author)v.elementAt(k);
					if (a.getCode()!= null && a.getCode().equals(code) && a.getName()!= null && a.getName().equals(name))
						return a;
				}
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	Author findAuthorByCode(String code){
		if (code == null )
			return null;
		Projects p = MesquiteTrunk.getProjectList();
		for (int i=0; i<p.getNumProjects(); i++){
			MesquiteProject proj = p.getProject(i);
			if (proj.numAuthors()>0){
				ListableVector v = proj.getAuthors();
				for (int k = 0; k< v.size(); k++){
					Author a = (Author)v.elementAt(k);
					if (a.getCode()!= null && a.getCode().equals(code))
						return a;
				}
			}
		}
		return null;
	}

	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(getClass(), "Sets the author for this account and machine", null, commandName, "setAuthor")) {
			MesquiteBoolean answer = new MesquiteBoolean(false);
			MesquiteString resp1 = new MesquiteString(MesquiteModule.author.getName());
			MesquiteString resp2 = new MesquiteString(MesquiteModule.author.getCode());
			MesquiteString.queryTwoStrings(containerOfModule(), "Set Author", "Author", "Author code (do not use a number!)", answer, resp1, resp2, false);
			if (answer.getValue() && (!MesquiteModule.author.getName().equals(resp1.getValue()) || !MesquiteModule.author.getCode().equals(resp2.getValue()))){
				//there's be a change
				//first, is it a known author?
				Author a = findAuthorIdentical(resp1.getValue(), resp2.getValue());
				if (a == null){
					a = findAuthorByCode(resp2.getValue());  //exists; just need to change names
					if (a !=null){
						a.setName(resp1.getValue());
						Projects p = MesquiteTrunk.mesquiteTrunk.getProjectList();
						for (int i=0; i<p.getNumProjects(); i++){
							MesquiteProject proj = p.getProject(i);
							ListableVector v = proj.getAuthors();
							v.notifyListeners(this, new Notification(MesquiteListener.PARTS_CHANGED));
						}
						return null;
					}
				}
				if (a == null){  //no, so make a new author
					a = new Author();
					a.setName(resp1.getValue());
					a.setCode(resp2.getValue());
					Projects p = MesquiteTrunk.mesquiteTrunk.getProjectList();
					for (int i=0; i<p.getNumProjects(); i++){
						MesquiteProject proj = p.getProject(i);
						ListableVector v = proj.getAuthors();
						v.addElement(a, false);
					}
				}

				MesquiteModule.author.setCurrent(false);
				a.setCurrent(true);
				MesquiteModule.author = a;
				storePreferences();
				setCurrentAllProjects();
				
			}
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
			v.notifyListeners(this, new Notification(MesquiteListener.PARTS_CHANGED));
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
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}
}