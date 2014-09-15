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
import java.awt.image.*;
import mesquite.lib.duties.*;

import java.util.*;

/* ======================================================================== */
/** The vector of information (MesquiteModuleInfo objects) about available modules.*/
public class ModulesInfoVector extends ListableVector {
	Vector dutyClasses;
	Vector dutyDefaultsSourceClass; //duty class of module that supplied dutyDefault;  
	Vector dutyDefaults;
	public ModulesInfoVector  () {
		super(50);
		notifyOfChanges = false;
		dutyClasses = new Vector(100);
		dutyDefaults = new Vector(100);
		dutyDefaultsSourceClass = new Vector(100);
	}

	public void dispose(){
		removeAllElements(false);
		dutyClasses.removeAllElements(); 
		dutyDefaults.removeAllElements(); 
		dutyDefaultsSourceClass.removeAllElements(); 
	}
	public Vector getDutyList(){//hackathon
		Vector list = new Vector();
		for (int d = 0; d < dutyClasses.size(); d++) {
			Class duty = (Class) dutyClasses.elementAt(d);

			int num = size();
			MesquiteModuleInfo mbi;
			boolean first = true;
			for (int i = 0; i < num; i++) {
				mbi = (MesquiteModuleInfo) elementAt(i);
				if (mbi.getDutyClass() == duty) {
					if (first) {
						list.addElement(mbi.getDutyName() + " (Class name " + mbi.getDutyClass().getName() + ")");
						first = false;
					}
				}

			}
		}
		return list;
	}
	public Vector whoUsesMe(MesquiteModuleInfo module){
		
		Vector v = new Vector();
		if (MesquiteTrunk.class.isAssignableFrom(module.getClass())) //no other modules use the trunk!
			return v;
		Class target = module.getModuleClass();
		/*
		 * for (int i = 0; i< dutyDefaults.size(); i++){
			MesquiteModuleInfo mm = (MesquiteModuleInfo)dutyDefaults.elementAt(i);
			if (mm != null){
				Vector needs = mm.getEmployeeNeedsVector();
				if (needs != null){
					for (int k = 0; k< needs.size(); k++){
						EmployeeNeed need = (EmployeeNeed)needs.elementAt(k);
						if (need.getDutyClass().isAssignableFrom(target))
							v.addElement(mm, false);  //should also store which use this is!  (i.e. store the particular need)

					}
				}
			}
		}
		 */
		int numHighPriority = 0;
		int numMediumPriority = 0;
		MesquiteModuleInfo mm = null;
		MesquiteInteger lastIndex =  new MesquiteInteger(-1);
		MesquiteInteger category = new MesquiteInteger(0);
		while ((mm = (MesquiteModuleInfo)MesquiteTrunk.mesquiteModulesInfoVector.getNextModule(lastIndex, category))!=null){
			if (mm != null && mm != module){
				Vector needs = mm.getEmployeeNeedsVector();
				if (needs != null){
					for (int k = 0; k< needs.size(); k++){
						EmployeeNeed need = (EmployeeNeed)needs.elementAt(k);
						if (need.getDutyClass().isAssignableFrom(target) && v.indexOf(need) < 0) {
							if (need.getPriority() >= 2){
								v.insertElementAt(need, numHighPriority);
								numHighPriority++;
							}
							else if (need.getPriority() ==1 ){
								v.insertElementAt(need, numHighPriority + numMediumPriority);
								numMediumPriority++;
							}
							else
								v.addElement(need);  //should also store which use this is!  (i.e. store the particular need)
						}

					}
				}
			}
		}
		if (v.size() == 0)
			return null;
		return v;
	}

	public boolean isDefault(MesquiteModule mb){
		String[] defaults = mb.getDefaultModule();
		return (whichDefault(defaults, mb.getModuleInfo())>=0);
	}
	//returns all modules in sequence, starting first with defaults, then with ones with primary choice, then remaining
	public MesquiteModuleInfo getNextModule(MesquiteInteger lastIndex, MesquiteInteger category){
		if (lastIndex == null || category == null)
			return null;
		if (category.getValue() == 0){ // defaults
			int i = lastIndex.getValue()+1;
			while (i < size()){
				MesquiteModuleInfo mmi = (MesquiteModuleInfo)elementAt(i);
				if (mmi.isDefault()){
					lastIndex.setValue(i);
					return mmi;
				}
				i++;
			}
			category.setValue(1); //failed; go to next category
			lastIndex.setValue(-1);
		}
		if (category.getValue() == 1){ // primary choice
			int i = lastIndex.getValue()+1;
			while (i < size()){
				MesquiteModuleInfo mmi = (MesquiteModuleInfo)elementAt(i);
				if (!mmi.isDefault() && mmi.isPrimaryChoice){
					lastIndex.setValue(i);
					return mmi;
				}
				i++;
			}
			category.setValue(2); //failed; go to next category
			lastIndex.setValue(-1);
		}
		if (category.getValue() == 2){ // remaining
			int i = lastIndex.getValue()+1;
			while (i < size()){
				MesquiteModuleInfo mmi = (MesquiteModuleInfo)elementAt(i);
				if (!mmi.isDefault() && !mmi.isPrimaryChoice){
					lastIndex.setValue(i);
					return mmi;
				}
				i++;
			}
			category.setValue(3); //failed; go to next category
			lastIndex.setValue(-1);
		}
		return null;
	}
	private String[] filterDefaults(String[] candidates, Class dutyClass){ //making sure the listed defaults are all valid
		if (candidates == null)
			return null;
		if (dutyClass == null)
			return null;
		int count = 0;
		for (int i=0; i<candidates.length; i++){
			MesquiteModuleInfo mbi = findModule(dutyClass, candidates[i]);
			if (mbi != null)
				count++;
		}
		if (count == candidates.length)
			return candidates;
		String[] result = new String[count];
		count = 0;
		for (int i=0; i<candidates.length; i++){
			MesquiteModuleInfo mbi = findModule(dutyClass, candidates[i]);
			if (mbi != null) {
				result[count++] = candidates[i];
			}
		}
		return result;
	}
	public void filterAllDutyDefaults(){//making sure the listed defaults are all valid
		for (int i=0; i<dutyDefaults.size(); i++){
			dutyDefaults.setElementAt(filterDefaults((String[])dutyDefaults.elementAt(i), (Class)dutyDefaultsSourceClass.elementAt(i)), i); 
		}
	}
	private boolean inStandardPackages(MesquiteModuleInfo mmi){
		//String packageName = StringUtil.getAllButLastItem(StringUtil.getAllButLastItem(getClass().getName(), "."), ".");  //getting package string
		for (int i=0; i<MesquiteTrunk.standardPackages.length; i++)
			if (mmi.getModuleClass().getName().startsWith("mesquite." + MesquiteTrunk.standardPackages[i]))
				return true;
		for (int i=0; i<MesquiteTrunk.standardExtras.length; i++)
			if (mmi.getModuleClass().getName().startsWith("mesquite." + MesquiteTrunk.standardExtras[i]))
				return true;
		return false;

	}
	public void accumulateAllVersions(){//making sure the listed defaults are all valid
		for (int i=0; i<size(); i++){
			MesquiteModuleInfo mmi = (MesquiteModuleInfo)elementAt(i);
			if (mmi.doesDuty(PackageIntroInterface.class)){
				if (!StringUtil.blank(mmi.getPackageVersion())){
					mmi.version = mmi.getPackageVersion();
				}
			}
			MesquiteModuleInfo pim = mmi.getPackageIntroModule();
			if (pim!=null && !StringUtil.blank(pim.getPackageVersion())) {
				mmi.version =  pim.getPackageVersion();
				mmi.packageName = pim.packageName;
			}
			
			//if part of standard
			if (inStandardPackages(mmi)) {
				mmi.version =  MesquiteTrunk.mesquiteTrunk.getVersion();
				mmi.packageName = "Mesquite standard packages";
			}
		}
	}
	public void recordDuty(MesquiteModule mb){
		if (mb==null) return;
		/*Class dutyClass = mb.getDutyClass();
		if (dutyClasses.indexOf(dutyClass)<0){
			dutyClasses.addElement(dutyClass);
			dutyDefaults.addElement(mb.getDefaultModule());
		}*/
		boolean isDefault = false;
		Class c = mb.getDutyClass();
		String[] defaults = mb.getDefaultModule();
		while (c!=MesquiteModule.class){
			int loc = dutyClasses.indexOf(c);
			if (loc<0){ //not found; simply added
				dutyClasses.addElement(c);
				dutyDefaults.addElement(defaults); 
				dutyDefaultsSourceClass.addElement(mb.getDutyClass()); 
			}
			else {
				Class prevSource = (Class)dutyDefaultsSourceClass.elementAt(loc);
				if (mb.getDutyClass().isAssignableFrom(prevSource) && mb.getDutyClass() != prevSource ) { 
					// this module is of duty class that is superclass to duty class of module that had supplied these entries; use this instead
					dutyDefaults.setElementAt(defaults, loc); 
					dutyDefaultsSourceClass.setElementAt(mb.getDutyClass(), loc); 
				}
			}
			c = c.getSuperclass();
		}
	}
	public String getDutyName(Class dutyClass){
		int num = size();
		for (int i=0; i<num; i++){
			MesquiteModuleInfo mbi = (MesquiteModuleInfo)elementAt(i);
			if (mbi.getDutyClass() == dutyClass)
				return mbi.getDutyName();
		}
		for (int i=0; i<num; i++){
			MesquiteModuleInfo mbi = (MesquiteModuleInfo)elementAt(i);
			if (dutyClass.isAssignableFrom(mbi.getDutyClass()))
				return mbi.getDutyName();
		}
		return null;
	}
	public String[] getDutyDefaults(Class dutyClass){
		Class c = dutyClass;
		while (c!=MesquiteModule.class && c!=null){
			for (int i=0; i<dutyClasses.size(); i++){
				if (dutyClasses.elementAt(i) == c)
					return (String[])dutyDefaults.elementAt(i);
			}
			c = c.getSuperclass();
		}
		return null;
	}

	/** Dumps list of available modules to System.out.*/
	public void dumpModuleList () {
		int num = size();
		System.out.println("=====Modules List=====");
		MesquiteModuleInfo mbi;
		for (int i=0; i<num; i++){
			mbi = (MesquiteModuleInfo)elementAt(i);
			System.out.println(mbi.getName() + "  duty class " + mbi.getDutyClass());
		}
		System.out.println("=================");
	}
	/** Return a String array listing all modules that subclass the passed duty class.  *
	public Listable[] getModuleNamesOfDuty(Class dutyClass, Object condition, MesquiteModule prospectiveEmployer) {
		int num = size();
		MesquiteModuleInfo mbi=null;
		int count=0;
		while ((mbi = findNextModule(dutyClass, mbi))!=null) {
		//todo: could check for compatibility here as in menus
			if (mbi.doesDuty(dutyClass) && mbi.getUserChooseable()){
				count++;
			}
		}
		if (count==0)
			return null;
		else {
			Listable[] names = new Listable[count];
			int imod=0;
			mbi=null;
			while ((mbi = findNextModule(dutyClass, mbi))!=null) {
			//todo: could check for compatibility here as in menus
				if (mbi.doesDuty(dutyClass) && mbi.getUserChooseable()){
					names[imod]=mbi;
					imod++;
				}
			}
			return names;
		}
	}
	/** Return a String array listing all modules that subclass the passed duty class.  */
	public Listable[] getModulesOfDuty(Class dutyClass, Object condition, MesquiteModule prospectiveEmployer) {
		return getModulesOfDuty(dutyClass, condition, prospectiveEmployer, null);
	}
	
	/** Return a String array listing all modules that subclass the passed duty class.  */
	public Listable[] getModulesOfDuty(Class dutyClass, Object condition, MesquiteModule prospectiveEmployer, StringBuffer compatibilityReport) {
		int num = size();
		MesquiteModuleInfo mbi=null;
		MesquiteProject proj = null;
		if (prospectiveEmployer !=null)
			proj = prospectiveEmployer.getProject();
		int count=0;
		while ((mbi = findNextModule(dutyClass, mbi))!=null) {
			//todo: could check for compatibility here as in menus
			if (mbi.doesDuty(dutyClass) && mbi.getUserChooseable() && (/*condition==null || */mbi.isCompatible(condition, proj, prospectiveEmployer))){
				count++;
			}
		}
		if (count==0)
			return null;
		else {
			Listable[] infos = new Listable[count];
			int imod=0;
			mbi=null;
			MesquiteString report = new MesquiteString();
			while ((mbi = findNextModule(dutyClass, mbi))!=null) {
				//todo: could check for compatibility here as in menus
				report.setValue("");
				if (mbi.doesDuty(dutyClass) && mbi.getUserChooseable()&&  (/*condition==null || */mbi.isCompatible(condition, proj, prospectiveEmployer, report))){
					infos[imod]=mbi;
					imod++;
				}
				if (!report.isBlank() && compatibilityReport != null)
					compatibilityReport.append("  " + mbi.getName() + " is not an available choice because " + report);
			}
			return infos;
		}
	}
	/*........................................................................*/
	/** Returns module information for first module found that is instance of dutyClass and has given name.
	Returns null if none found.*/
	public MesquiteModuleInfo findModule (Class dutyClass, String name) {
		if (StringUtil.blank(name))
			return null;
		int num = size();
		MesquiteModuleInfo mbi;

		boolean className = (name.charAt(0) == '#');
		String useName;
		if (className) {
			useName = name.substring(1, name.length());
		}
		else
			useName = name;

		for (int i=0; i<num; i++){
			mbi = (MesquiteModuleInfo)elementAt(i);
			if (mbi.doesDuty(dutyClass) && mbi.nameMatches(useName)) {
				return mbi;
			}
		}
		return null;
	}
	/*........................................................................*/
	/** Returns module information for first module found that is instance of dutyClass within the given package (e.g. "mesquite.parsimony").
	Returns null if none found.*/
	public MesquiteModuleInfo findModule (String packageName, Class dutyClass) {
		if (StringUtil.blank(packageName))
			return null;
		int num = size();
		MesquiteModuleInfo mbi;


		for (int i=0; i<num; i++){
			mbi = (MesquiteModuleInfo)elementAt(i);
			String packageMBI = StringUtil.getAllButLastItem(StringUtil.getAllButLastItem(mbi.getModuleClass().getName(), "."), ".");
			boolean correctPackage = packageName.equals(packageMBI);
			if (mbi.doesDuty(dutyClass) && correctPackage) {
				return mbi;
			}
		}
		return null;
	}
	/*........................................................................*/
	/** returns mbi's index among the default modules named in the string array; if not present, returns -1 */ 
	private int whichDefault(String[] defaults, MesquiteModuleInfo mbi){ //finds out if mbi is in defaults list
		if (defaults==null || defaults.length==0)
			return -1;
		for (int i=0; i<defaults.length; i++) {
			if (defaults[i]==null)
				;
			else if (mbi.getName().equalsIgnoreCase(defaults[i]))
				return i;
			else if (defaults[i].equalsIgnoreCase("#"+ mbi.getShortClassName()))
				return i;
			else if (defaults[i].equalsIgnoreCase(mbi.getShortClassName()))
				return i;
		}
		return -1;
	}
	/*........................................................................*/
	/** Returns module information for first module found that is instance of dutyClass.
	Returns null if none found.*/
	public MesquiteModuleInfo findModule (Class dutyClass) {
		return findModule(dutyClass, null, null, null);
	}
	/*........................................................................*/
	/** Returns module information for first module found that is instance of dutyClass and satisfies given condition.
	Returns null if none found.*/
	public MesquiteModuleInfo findModule(Class dutyClass, Object condition, MesquiteProject project, EmployerEmployee prospectiveEmployer) {
		int num = size();
		// first, see if there is an available compatible default module
		MesquiteModuleInfo mbi = findNextDefaultModule(dutyClass, -1, condition, project, prospectiveEmployer);
		if (mbi!=null)
			return mbi;

		// Next, look among all of the modules to choose the first compatible one
		for (int i=0; i<num; i++){
			mbi = (MesquiteModuleInfo)elementAt(i);
			if (mbi.doesDuty(dutyClass)   && mbi.isCompatible(condition, project, prospectiveEmployer)){ //&& mbi.getUserChooseable()
				return mbi;
			}
		}
		return null;
	} 
	/*........................................................................*/
	/** Returns module information for next default module found, after the previousTH default, that is instance of dutyClass and satisfies given condition.
	Returns null if none found.*/
	public MesquiteModuleInfo findNextDefaultModule (Class dutyClass, int previous, Object condition, MesquiteProject project, EmployerEmployee prospectiveEmployer) {
		int num = size();
		MesquiteModuleInfo mbi;
		String[] defaultModules = null;
		boolean dutyClassFound = false;
		// first, finding defaults
		defaultModules =getDutyDefaults(dutyClass);
		if (defaultModules==null || defaultModules.length == 0 || previous >= defaultModules.length-1)
			return null;
		if (previous<0)
			previous = -1;
		for (int i=previous+1; i<defaultModules.length; i++){

			mbi = findModule(dutyClass, defaultModules[i]);
			if (mbi !=null && mbi.doesDuty(dutyClass)  && mbi.isCompatible(condition, project, prospectiveEmployer)){ //&& mbi.getUserChooseable()
				return mbi;
			}
		}
		return null;
	}
	/*........................................................................*/
	/** Returns module information for next module found, after previousModule, that is instance of dutyClass.
	Returns null if none found.*/
	public MesquiteModuleInfo findNextModule (Class dutyClass, MesquiteModuleInfo previousModule) {
		return findNextModule(dutyClass, previousModule, null, null, null);
	}
	/*........................................................................*/
	/** Returns module information for next module found, after previousModule, that is instance of dutyClass and satisfies given condition.
	Returns null if none found.*/
	public MesquiteModuleInfo findNextModule (Class dutyClass, MesquiteModuleInfo previousModule, Object condition, MesquiteProject project, EmployerEmployee prospectiveEmployer) {
		if (previousModule==null) {// no previous; find first module
			return findModule(dutyClass, condition, project, prospectiveEmployer);
		}

		boolean chooseNext = false;
		boolean defaultsExist = false;
		int num = size();
		MesquiteModuleInfo mbi;
		//first, check to see if there are any more defaults that could be chosen
		String[] defaultModules =getDutyDefaults(dutyClass); 
		if (defaultModules!=null &&  defaultModules.length>=1) { //check if there is a subsequent default
			defaultsExist = true;
			if (previousModule == null){
				MesquiteModuleInfo fmbi =findNextDefaultModule(dutyClass, -1, condition, project, prospectiveEmployer);
				if (fmbi!=null)
					return fmbi;
			}
			else {
				int where = whichDefault(defaultModules, previousModule);


				if (where >= 0 ) { // is a default, thus go to next
					if (where == defaultModules.length-1){ //previous is exactly last default; thus go to non-defaults
						chooseNext = true;
					}
					else if (where<defaultModules.length-1){ 
						MesquiteModuleInfo fmbi =findNextDefaultModule(dutyClass, where, condition, project, prospectiveEmployer);
						if (fmbi!=null)
							return fmbi;
					}
				}
			}
		}
		else
			defaultsExist = false;

		for (int i=0; i<num; i++){ //next, go through non-defaults
			mbi = (MesquiteModuleInfo)elementAt(i); //If there are defaults, don't choose if is default (otherwise defaults will be chosen more than once
			if (chooseNext) {
				if (mbi.doesDuty(dutyClass)){
					if ((!defaultsExist || whichDefault(defaultModules, mbi)<0)  && mbi.isCompatible(condition, project, prospectiveEmployer)) { //&& mbi.getUserChooseable()
						return mbi;
					}
				}
			}
			else {
				if (mbi.mbClass == previousModule.getModuleClass()) {
					chooseNext = true;
				}
			}
		}
		return null;
	}
}


