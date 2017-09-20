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
import java.net.*;

/* ======================================================================== */
/** Holds information re a Mesquite module. Since modules are not instantiated until
hired as employees, they cannot be queried directly for basic information.  Thus, each is instantiated
temporarily on Mesquite startup, queried for some basic information, which is stored in a MesquiteModulesInfo
object.  These objects are stored in the mesquiteTrunk's ModuleInfoVector object.<p>
This approach is somewhat of a remnant of
MacClade proto4; a more Java-natural approach might be to have getName, getAuthors, etc. be static methods, that way
there is no need to instantiate the module.*/

public class MesquiteModuleInfo implements Listable, CompatibilityChecker, FunctionExplainable, Prioritizable {
	String name;
	String moduleURL;
	boolean URLinPackageIntroDirectory;
	String htmlExplanation;
	String nameForMenuItem;
	String authors;
	String version;
	int versionInt;
	String explanation;
	String functionIconPath;
	String keywords;
	String dutyName;
	String[] defaultForSuper;
	String manualPath;
	Class mbClass;
	Class dutyClass;
	Class hireSubchoice;
	Class[] dontHireSubchoice;
	String directoryPath;
	String splashURL;
	String packageName;
	String packageCitation;
	String packageAuthors;
	String packageVersion;
	String packageDateReleased;
	int packageMesquiteVersionOfFirstRelease;
	int packageVersionInt;
	int packageBuildNumber;
	boolean builtInPackage;
	String packageURL;
	String homePhoneNumber;
	Vector commands;
	Vector menus;
	Vector explanations;
	Vector subfunctions;
	boolean userChooseable = true;
	boolean searchableAsModule;
	boolean splashExists;
	boolean isPrimaryChoice = false;
	boolean substantive = true;
	boolean sCitation = false;
	boolean prerelease = true;
	boolean isPackageIntro = false;
	boolean hideable = true;
	boolean loadModule = true;
	int versionOfFirstRelease = 0;
	boolean def = false;
	int numStarts = 0;
	NexusBlockTest nexusBlockTest;
	NexusCommandTest nexusCommandTest;
	CompatibilityTest compatibilityTest;
	/** Associated macro records.*/
	Vector macros; 
	Vector employeeNeedsVector;  //a vector of registered employee needs, used for documentation/searches of how to do analyses, but perhaps eventually also used in hiring
	
	public MesquiteModuleInfo (Class c, MesquiteModule mb, CommandChecker checker, String directoryPath) {  
		checker =CommandChecker.accumulate(mb, checker);
		mb.moduleInfo = this;
 		this.manualPath = mb.getManualPath();
  		if (directoryPath!=null)
  			this.directoryPath = new String(directoryPath); //must come first so subsequent can call module's path method
		this.splashExists = (mb instanceof PackageIntroInterface && (( PackageIntroInterface)mb).hasSplash());
		if (mb instanceof PackageIntroInterface)
			this.splashURL = (( PackageIntroInterface)mb).getSplashURL();
 		this.commands = checker.getAccumulatedCommands();
 		this.explanations = checker.getAccumulatedExplanations();
 		this.menus =checker.getAccumulatedMenus();
		this.mbClass = c;
		isPrimaryChoice = mb.requestPrimaryChoice();
		this.hireSubchoice = mb.getHireSubchoice();
		this.dontHireSubchoice = mb.getDontHireSubchoice();
		this.name = mb.getName(); //�
		this.nameForMenuItem = mb.getNameForMenuItem(); //�
 		this.authors =mb.getAuthors();//�
 		this.version =mb.getVersion();//�
 		this.loadModule = mb.loadModule();
 		
 		this.versionOfFirstRelease = mb.getVersionOfFirstRelease();
 		this.explanation =mb.getExplanation();//�
 		this.sCitation = mb.showCitation();
 		this.userChooseable =mb.getUserChooseable();//�
 		this.substantive = mb.isSubstantive();
 		this.prerelease = mb.isPrerelease();
 		this.dutyClass =mb.getDutyClass();//�
 		this.dutyName =mb.getDutyName();//�
 		this.defaultForSuper = mb.getDefaultModule();
 		this.keywords = mb.getKeywords();
 		this.versionInt = mb.getVersionInt();
		if (mb instanceof PackageIntroInterface){
			this.packageCitation = (( PackageIntroInterface)mb).getPackageCitation();
			this.packageName =  (( PackageIntroInterface)mb).getPackageName();
			this.packageAuthors = (( PackageIntroInterface)mb).getPackageAuthors();
			this.packageVersion = (( PackageIntroInterface)mb).getPackageVersion();
			this.packageDateReleased = (( PackageIntroInterface)mb).getPackageDateReleased();
			this.builtInPackage = (( PackageIntroInterface)mb).isBuiltInPackage();
			this.packageMesquiteVersionOfFirstRelease = (( PackageIntroInterface)mb).getVersionOfFirstRelease();
			this.packageVersionInt = (( PackageIntroInterface)mb).getPackageVersionInt();
			this.packageBuildNumber = (( PackageIntroInterface)mb).getPackageBuildNumber();
			this.packageURL = (( PackageIntroInterface)mb).getPackageURL();
			isPackageIntro = true;
			hideable = (( PackageIntroInterface)mb).getHideable();
			if ((( PackageIntroInterface)mb).getMinimumMesquiteVersionRequiredInt()> MesquiteTrunk.mesquiteTrunk.getVersionInt())
				MesquiteTrunk.mesquiteTrunk.discreetAlert("The package " + packageName 
						+ " requires a newer version of Mesquite (at least version " + (( PackageIntroInterface)mb).getMinimumMesquiteVersionRequired() + "). The package might not function properly and may cause crashes.");
 		}
  		mb.mesquiteStartup();
		this.nexusBlockTest = mb.getNexusBlockTest();//�
 		this.nexusCommandTest = mb.getNexusCommandTest();//�
		this.compatibilityTest = mb.getCompatibilityTest();
		this.searchableAsModule = mb.getSearchableAsModule();
		try{
			mb.getEmployeeNeeds();
		}
		catch (java.lang.NoClassDefFoundError e){
		}
		this.functionIconPath = mb.getFunctionIconPath();
		this.moduleURL = mb.getURLString();
		this.URLinPackageIntroDirectory = mb.URLinPackageIntro();
		this.htmlExplanation = mb.getHTMLExplanation();
		this.employeeNeedsVector = mb.getEmployeeNeedsVector();
		this.homePhoneNumber = mb.getHomePhoneNumber();
		String localPhoneBookPath = directoryPath + "phoneBook.txt";
		if (MesquiteFile.fileExists(localPhoneBookPath)) {
			String phoneNumber = MesquiteFile.getFileContentsAsString(localPhoneBookPath);
			if (StringUtil.notEmpty(phoneNumber)) {
				this.homePhoneNumber = phoneNumber;
			}
		}
		if (employeeNeedsVector != null)
			for (int i = 0; i< employeeNeedsVector.size(); i++){
				EmployeeNeed need = (EmployeeNeed)employeeNeedsVector.elementAt(i);
				need.setRequestor(this);
			}
		mb.getSubfunctions();
		this.subfunctions = mb.getSubfunctionsVector();
	//timer3.end();
	}
	/** returns name of Module */
	public String getName() {
		return name;
	}
	/** returns HTML Explanation of Module */
	public String getHTMLExplanation() {
		return htmlExplanation;
	}
	/** returns URL of Module */
	public String getURLString() {
		return moduleURL;
	}
	/** Returns whether or not the URL for this module is a relative reference from the PackageIntro directory */
	public boolean URLinPackageIntro(){
		return URLinPackageIntroDirectory;
	}

	/*.................................................................................................................*/
	/** returns the URL of the notices file for this module so that it can phone home and check for messages */
	public String  getHomePhoneNumber(){ 
		return homePhoneNumber;
	}


	/** returns whether name of Module matches passed String.  First checks to see if name matches directly (e.g., "Tree Window"); then checks to see if matches at whole package path
	(e.g. mesquite.minimal.BasicTreeWindowMaker.BasicTreeWindowMaker); then checks to see if matches at immediate classname (e.g., BasicTreeWindowMaker)*/
	public boolean nameMatches(String s) {
		if (s==null)
			return false;
		if (s.equalsIgnoreCase(name))
			return true;
		if (s.equals(getModuleClass().getName()))
			return true;
		if (s.length()>0 && s.charAt(0)=='#')
			s = s.substring(1, s.length());
		if (s.equals(getShortClassName()))
			return true;
		s = StringUtil.getLastItem(s, ".");
		if (s!=null && s.equals(getShortClassName()))
			return true;
		return false;
	}
	public Vector getEmployeeNeedsVector(){
		return employeeNeedsVector;
	}
	public boolean hasNeeds(){
		return employeeNeedsVector != null && employeeNeedsVector.size()>0;
	}
	public boolean getSearchableAsModule(){
		return searchableAsModule;
	}
	public Vector getSubfunctionsVector(){
		return subfunctions;
	}
	public String getKeywords(){
		return keywords;
	}
	public boolean isDefault(){
		return def;
	}
	public void setAsDefault(boolean d){
		def = d;
	}
	/** Returns information about a package of modules*/
 	public boolean getHideable(){
 		return hideable;
 	}
	/** Returns information about a package of modules*/
 	public boolean getIsPackageIntro(){
 		return isPackageIntro;
 	}
	/** Returns information about a package of modules*/
 	public String getPackageCitation(){
 		return packageCitation;
 	}
 	public String getPackageAuthors(){
 		return packageAuthors;
 	}
 	public String getPackageVersion(){
 		return packageVersion;
 	}
	public int getVersionOfFirstRelease(){
 		return versionOfFirstRelease;
 	}
	public String getPackageDateReleased(){
 		return packageDateReleased;
 	}
	public boolean loadModule(){
 		return loadModule;
 	}

	public boolean isBuiltInPackage(){
 		return builtInPackage;
 	}
	public int getMesquiteVersionOfFirstPackageRelease(){
 		return packageMesquiteVersionOfFirstRelease;
 	}
 	
 	public int getPackageVersionInt(){
 		return packageVersionInt;
 	}
 	public int getPackageBuildNumber(){
 		return packageBuildNumber;
 	}
 	public String getPackageURL(){
 		return packageURL;
 	}
 	public boolean showCitation(){
 		return sCitation;
 	}
	public void addMacro(MesquiteMacro macro){
		if (macros == null)
			macros = new Vector();
		macros.addElement(macro);
	}
	public void removeMacro(MesquiteMacro macro){
		if (macros != null)
			macros.removeElement(macro);
	}
	public Vector getMacros(){
		return macros;
	}
	public int getNumStarts() {
		return numStarts;
	}
	public void setNumStarts(int numStarts) {
		this.numStarts = numStarts;
	}
	public void incrementNumStarts() {
		 numStarts++;
	}

	
	/** returns name to be used in menu item to hire the module.  By default this is the same as the name of the module, but can be different if the
	module overrides its own getNameForMenuItem method.*/
	public String getNameForMenuItem() {
		return nameForMenuItem;
	}
	/** returns the vector of command descriptions accumulated during startup */
	public Vector getMenus () {
		return menus;
	}
	/** returns the vector of command descriptions accumulated during startup */
	public Vector getCommands () {
		return commands;
	}
	/** returns the vector of command descriptions accumulated during startup */
	public Vector getExplanations () {
		return explanations;
	}
	/** returns whether it is a module that appears in lists for the user to choose as an option (otherwise, it can be chosen only directly by other modules, or by CommandRecord.nr */
	public boolean getUserChooseable(){
		return userChooseable;
	}
 	/** Returns the default module for the superclass (duty class)*/
	public String[] getDefaultModule () {
		return defaultForSuper;
	}
 	/** Returns the authors string of the module*/
 	public String getAuthors() { 
 		return authors;
   	}

 	/** returns the version number string of the module*/
 	public String getVersion() { 
 		return version;
   	}
 	/** returns the version number of the module as an integer*/
 	public int getVersionInt() { 
 		return versionInt;
   	}
 	/** returns whether the module is marked as substantive*/
 	public boolean isSubstantive(){
 		return substantive;
 	}
 	/** returns whether the module is marked as release*/
 	public boolean isPrerelease(){
 		return prerelease;
 	}

 	/** returns an explanation of what the module does*/
 	public String getExplanation() { 
 		return explanation;
   	}
 	public String getFunctionIconPath(){
 		return functionIconPath;
 	}
 	/** returns path to manual*/
 	public String getManualPath() { 
 		return manualPath;
   	}
 	/** returns path to directory of module*/
 	public String getDirectoryPath() { 
 		return directoryPath;
   	}
 	/** returns path to package of module*/
 	public String getPackagePath() { 
 		String s= StringUtil.getAllButLastItem(directoryPath, MesquiteFile.fileSeparator);
 		s= StringUtil.getAllButLastItem(s, MesquiteFile.fileSeparator);
		if (s==null)
			return null;
		else
			return s + MesquiteFile.fileSeparator;
   	}

 	/** returns the class of the module*/
 	public Class getModuleClass() { 
 		return mbClass;
   	}
 	/** returns the complete String name of the class of the module (e.g., mesquite.minimal.BasicTreeWindowMaker.BasicTreeWindowMaker)*/
 	public String getClassName() { 
 		return mbClass.getName();
   	}
	/*.................................................................................................................*/
	/** Returns the name of the module introducing package of modules (e.g., "#aParsimonyIntro")*/
 	public MesquiteModuleInfo getPackageIntroModule(){
 		String packageName = StringUtil.getAllButLastItem(StringUtil.getAllButLastItem(getClassName(), "."), ".");  //getting package string
 		return MesquiteTrunk.mesquiteModulesInfoVector.findModule(packageName, PackageIntroInterface.class);
 	}

 	/** returns the immediate classnameof the module (e.g., BasicTreeWindowMaker)*/
 	public String getShortClassName() { 
 		return MesquiteModule.getShortClassName(mbClass);
   	}
 	/** Returns the name of the duty performed (usually defined in duty defining superclass)*/
 	public String getDutyName() {
 		return dutyName;
   	}
 	/** returns the duty class*/
 	public Class getDutyClass() { 
 		return dutyClass;
   	}
   	public NexusBlockTest getNexusBlockTest(){
   		return nexusBlockTest;
   	}
   	
 	/** returns the class of the employee desired*/
 	public Class getHireSubchoice() { 
 		return hireSubchoice;
   	}
 	/** returns the classes that the employee shouldn't represent*/
 	public Class[] getDontHireSubchoice() { 
 		return dontHireSubchoice;
   	}
	/** Returns whether module is compatible with given object*/
 	public boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer) {
 		return isCompatible(obj, project, prospectiveEmployer, null);
 	}
 	/** Returns whether module is compatible with given object*/
 	public boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer, MesquiteString report) {
 		if (compatibilityTest==null) {
 			return true;
 		}
 		else {
 			boolean c = compatibilityTest.isCompatible(obj, project, prospectiveEmployer, report);
 			return c;
 		}
   	}
	/** returns whether this module is one of the primary that appears in first-level choices */
   	public boolean isPrimary(Class dutyClass){
		if (isPrimaryChoice)
			return true;
		String[] defaults = MesquiteTrunk.mesquiteModulesInfoVector.getDutyDefaults(dutyClass);
		if (defaults == null)
			return false;
		for (int i = 0; i<defaults.length; i++)
			if (nameMatches(defaults[i]))
				return true;
   		return false;  
   	}
   	
	/** returns whether this module is one of the primary that appears in first-level choices; for Prioritizable interface */
	public boolean isFirstPriority(Class c){
		return isPrimary(c);
	}

 	/** Returns whether module is instance of particular duty-defining class*/
	public boolean doesDuty (Class dutyClass) {
		if (dutyClass == null)
			return false;
		if (dutyClass == MesquiteModule.class)
			return true;
		if (mbClass!=null) {
			if (dutyClass.isInterface()){
				if (dutyClass.isAssignableFrom(mbClass))
					return true;
			}
			
			for (Class superC = mbClass; superC != MesquiteModule.class; superC = superC.getSuperclass())
				if (superC == dutyClass)
					return true;
			return false;
		}
		else
			return false;
	}
 	/** Returns whether module is instance of particular duty-defining class*/
	public boolean doesADuty (Class[] dutyClasses) {
		if (dutyClasses == null)
			return false;
		for (int i=0; i<dutyClasses.length; i++){
			if (doesDuty(dutyClasses[i]))
					return true;
		}
			return false;
	}
	public String getPackageName(){
		return packageName;
	}
 	/** Returns whether module has splash screen*/
	public boolean hasSplash () {
		return splashExists;
	}
	public String getSplashURL(){
		return splashURL;
	}
	public void explainSplash(){
		if (!StringUtil.blank(splashURL)) {
			String s;
			if (packageName!=null)
				s= "This banner indicates that the package \"" + packageName +"\" is currently installed.  Would you like to see the package's introductory web page?";
			else
				s= "This banner refers to a package that is currently installed.  Would you like to see the package's introductory web page?";
			if (AlertDialog.query(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Installed Package", s, "Web Page", "No"))
				MesquiteTrunk.mesquiteTrunk.showWebPage(splashURL, false);
		} 
		else if (packageName!=null)
			MesquiteTrunk.mesquiteTrunk.alert("This banner indicates that the package \"" + packageName +"\" is currently installed");
	}
}

