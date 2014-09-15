/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

package mesquite.lib;

import java.awt.*;
import java.util.*;
import java.io.*;

/* ======================================================================== */
/** Holds the name.*/

public class MesquiteMacro implements Listable, Explainable {
	String name;
	String explanation;
	String path; 
	int preferredMenu =0;
	
	public final static int UNSPECIFIED = 0;
	public final static int ANALYSIS = 1;

	MesquiteModuleInfo moduleInfo;
	boolean auto = false;
	
	public MesquiteMacro (String name, String explanation, String path, MesquiteModuleInfo mmi) {  
		this.name = name; 
 		this.explanation =explanation;
 		this.path = path;
		moduleInfo = mmi;
	}
	/** returns info for the owner module */
	public MesquiteModuleInfo getModuleInfo() {
		return moduleInfo;
	}
	/** sets preferred menu in which macro is to appear */
	public void setPreferredMenu(int a) {
		if (MesquiteInteger.isCombinable(a))
			preferredMenu = a;
	}
	/** returns preferred menu in which macro is to appear */
	public int getPreferredMenu() {
		return preferredMenu;
	}
	/** sets whether macro was made by autosave */
	public void setAutoSave(boolean a) {
		auto = a;
	}
	/** returns whether macro was made by autosave */
	public boolean isAutoSave() {
		return auto;
	}
	/** returns name of macro */
	public String getName() {
		return name;
	}
	/** sets name of macro */
	public void setName(String n) {
		name = n;
	}
	/** sets explanation of macro */
	public void setExplanation(String n) {
		explanation = n;
	}
 	/** returns an explanation of what the macro does*/
 	public String getExplanation() { 
 		if (moduleInfo==null)
 			return explanation;
 		else if (auto)
 			return explanation + "\n\nIt was built automatically at user request, to instruct the module \"" + moduleInfo.getNameForMenuItem() + "\"";
 		else 
 			return explanation + "\n\nInstructs module \"" + moduleInfo.getNameForMenuItem() + "\"";
   	}
 	public String getRawExplanation() { 
 			return explanation;
   	}
 	/** returns path*/
 	public String getPath() { 
 		return path;
   	}
 	/** returns object that lists autosaved macros*/
 	public static StringLister getAutoSavedLister() { 
 		return new AutoSavedLister();
   	}
   	
   	public static MesquiteMacro findMacro(String name){
   		if (StringUtil.blank(name)) {
   			return (MesquiteMacro)ListDialog.queryList(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Macro", "Select Macro", MesquiteString.helpString, new AutoSavedLister().getListables(), 0);
   		}
		for (int i = 0; i<MesquiteTrunk.mesquiteTrunk.mesquiteModulesInfoVector.size(); i++){
			MesquiteModuleInfo mmi = (MesquiteModuleInfo)MesquiteTrunk.mesquiteTrunk.mesquiteModulesInfoVector.elementAt(i);
			if (mmi.getMacros()!=null && mmi.getMacros().size()>0){
				Vector mForM = mmi.getMacros();
				for (int j = 0; j<mForM.size(); j++){
					MesquiteMacro mmr = (MesquiteMacro)mForM.elementAt(j);
					if (name.equals(mmr.getName()))
						return mmr;
				}
			}
		}
		return null;
   	}
   	public static void saveMacro (MesquiteModule target, String defaultMacroName, int preferredMenu, String macroCommands){
			MesquiteBoolean answer = new MesquiteBoolean(false);
			MesquiteString name = new MesquiteString(defaultMacroName);
			MesquiteString explanation = new MesquiteString("");
			MesquiteString.queryTwoStrings(target.containerOfModule(), "Save Macro", "Name of macro (to appear as menu item)", "Explanation of macro",  answer, name, explanation,true);
			if (!answer.getValue())
				return;
			if (StringUtil.blank(name.getValue()))
				name.setValue(defaultMacroName);
			if (StringUtil.blank(explanation.getValue()))
				explanation.setValue("(no explanation supplied)");
			String firstLine = "telling " +   target.getClass().getName()  + "  " + ParseUtil.tokenize(name.getValue()) + "   " +  ParseUtil.tokenize(explanation.getValue()) + "   " +  preferredMenu  + ";" + StringUtil.lineEnding();

			String base = MesquiteModule.prefsDirectory+ MesquiteFile.fileSeparator +"macros";
			if (!MesquiteFile.fileExists(base)) {
				File f = new File(base);
				f.mkdir();
			}
			String candidate = base + "/macro1";
			int count = 2;
			while (MesquiteFile.fileExists(candidate)){
				candidate = base + "/macro" + (count++);
			}
			MesquiteModuleInfo mmi = target.getModuleInfo();
			MesquiteMacro macro = new MesquiteMacro(name.getValue(), explanation.getValue(), candidate, mmi);
			macro.setAutoSave(true);
			macro.setPreferredMenu(preferredMenu);
			MesquiteFile.putFileContents(candidate, firstLine + macroCommands, true); 
			mmi.addMacro(macro);
			if (target !=null) {
				if (target.nameMatches("BasicTreeWindowMaker"))
					target.alert("The macro saved may appear in Macros For Tree Window submenu of the Tree menu of the Tree Window or elsewhere, such as the Macros submenus of the Window or Analysis menu");
				else
					target.alert("The macro saved may appear in the Macros submenus of the Window or Analysis menus.");
			}
			MesquiteTrunk.mesquiteTrunk.resetAllMenuBars();
   	 }

}

class AutoSavedLister implements StringLister{
	/*.................................................................................................................*/
	public String[] getStrings(){
	
		ListableVector macros = new ListableVector();
		for (int i = 0; i<MesquiteTrunk.mesquiteTrunk.mesquiteModulesInfoVector.size(); i++){
			MesquiteModuleInfo mmi = (MesquiteModuleInfo)MesquiteTrunk.mesquiteTrunk.mesquiteModulesInfoVector.elementAt(i);
			if (mmi.getMacros()!=null && mmi.getMacros().size()>0){
				Vector mForM = mmi.getMacros();
				for (int j = 0; j<mForM.size(); j++){
					MesquiteMacro mmr = (MesquiteMacro)mForM.elementAt(j);
					if (mmr.isAutoSave())
						macros.addElement(mmr, false);
				}
			}
		}
		if (macros.size() == 0)
			return null;
		return macros.getStrings();
	}
	/*.................................................................................................................*/
	public ListableVector getListables(){
	
		ListableVector macros = new ListableVector();
		for (int i = 0; i<MesquiteTrunk.mesquiteTrunk.mesquiteModulesInfoVector.size(); i++){
			MesquiteModuleInfo mmi = (MesquiteModuleInfo)MesquiteTrunk.mesquiteTrunk.mesquiteModulesInfoVector.elementAt(i);
			if (mmi.getMacros()!=null && mmi.getMacros().size()>0){
				Vector mForM = mmi.getMacros();
				for (int j = 0; j<mForM.size(); j++){
					MesquiteMacro mmr = (MesquiteMacro)mForM.elementAt(j);
					if (mmr.isAutoSave())
						macros.addElement(mmr, false);
				}
			}
		}
		if (macros.size() == 0)
			return null;
		return macros;
	}
}

