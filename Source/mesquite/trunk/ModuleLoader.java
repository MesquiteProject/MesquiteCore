/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trunk;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import mesquite.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
/*======================================================================== */
public class ModuleLoader {
	Mesquite mesquite;
	int directoryTotal = 0;
	ListableVector configurations;
	boolean[] packagesFound;
	Parser parser;
	String configurationString;
	boolean verboseStartup = false;
	public ModuleLoader(Mesquite mesquite){
		this.mesquite = mesquite;
		parser = new Parser();
	}
	
MesquiteTimer loadTimer, fileTimer, listTimer,instantiateTime,compTime,mmiTime,otherTime, classTime;
	/*.................................................................................................................*/
	public void init(String configFile, ListableVector configurations, boolean useMinimal) {
	this.configurations = configurations;
		mesquite.mesquiteModulesInfoVector = new ModulesInfoVector();
		MesquiteModuleInfo mBI = new MesquiteModuleInfo(Mesquite.class, mesquite, new CommandChecker(), mesquite.getPath());
		mesquite.mesquiteModulesInfoVector.addElement(mBI, false);
		mesquite.mesquiteModulesInfoVector.recordDuty(mesquite);
		loadTimer= new MesquiteTimer();
		fileTimer= new MesquiteTimer();
		listTimer= new MesquiteTimer();
		instantiateTime= new MesquiteTimer();
		compTime= new MesquiteTimer();
		mmiTime= new MesquiteTimer();
		otherTime= new MesquiteTimer();
		classTime = new MesquiteTimer();
		verboseStartup = MesquiteFile.fileExists(MesquiteModule.getRootPath() + "verbose");
		if (MesquiteTrunk.isApplet()) {
			System.out.println("Error: attempt to use applet as application");
		}
		else {
			StringArray targetDirectories;

			
			MesquiteTimer countTimer = new MesquiteTimer();
			MesquiteTimer timer = new MesquiteTimer();
			/*--------------------------------------*/
			String[] configs = null;
			if (useMinimal){
				configs = new String[2];
				configs[0] = "Minimal";
				configs[1] = "mesquite.minimal";
			}
			else if (configFile != null && !("all".equalsIgnoreCase(configFile))){
				String configPath =configFile; //MesquiteModule.getRootPath() + "mesquite" + MesquiteFile.fileSeparator + "configs" + MesquiteFile.fileSeparator + configFile;
				if (MesquiteFile.fileExists(configPath)) {
					configs = MesquiteFile.getFileContentsAsStrings(configPath);
					mesquite.logln("Preferences request startup configuration file.  Mesquite will start with a selected set of modules.");
				}
			}
			String path =MesquiteModule.getRootPath() + "mesquite" + MesquiteFile.fileSeparator + "minimal" + MesquiteFile.fileSeparator + "BasicFileCoordinator";
			File f = new File(path+ MesquiteFile.fileSeparator + "BasicFileCoordinator.class");  //Modules/
			numDirectoriesCurrent = 0;
			if (!MesquiteInteger.isCombinable(mesquite.numDirectories))
				mesquite.numDirectories = 0;
			if (mesquite.numDirectories==0)
				directoryTotal = 10000;
			else
				directoryTotal =  mesquite.numDirectories;
			showMessage(true, "Looking for modules", directoryTotal, 0);
			loadMesquiteModuleClassFiles(f, path, "mesquite.minimal.BasicFileCoordinator." , "BasicFileCoordinator.class");
			mesquite.logln("Modules loading from directory " + MesquiteModule.getRootPath() + "mesquite/");
			
			StringBuffer report =  new StringBuffer(5000);
			MesquiteModule.mesquiteTrunk.logln(report.toString());
		//timer.start();
			if (configs!=null){
				targetDirectories = new StringArray(configs.length);
				packagesFound = new boolean[configs.length];
				for (int i=0; i<configs.length; i++) packagesFound[i]=false;
				mesquite.setConfiguration(parser.getFirstToken(configs[0]));
				mesquite.log("Using module set configuration file: Only modules in the module set \"" + mesquite.getConfiguration() + "\" are being loaded.  Packages loaded: ");
				configurationString = "Configuration: " + mesquite.getConfiguration();
				targetDirectories.setValue(0, "mesquite.minimal");
				for (int i=1; i< configs.length; i++) { //first String is title & explanation
					targetDirectories.setValue(i, configs[i]);
				}
				getModules("mesquite", MesquiteModule.getRootPath() + "mesquite", "", 0, targetDirectories, true, false);  //do the directories in config
				int count = 0;
				String notFound = "";
				for (int i=0; i<configs.length; i++)
					if (!packagesFound[i] && !("mesquite.minimal".equalsIgnoreCase(targetDirectories.getValue(i)))){
						count++;
						notFound += "   " + targetDirectories.getValue(i) + StringUtil.lineEnding();
					}
				if (count>0)
					mesquite.alert("Some packages requested in the configuration file were not found.  These are:\n" + notFound);
				
				/* added by Paul Lewis */
				String classPathsFileMF = null; 
				if (MesquiteFile.fileExists(MesquiteModule.getRootPath() + "classpaths.xml")){
					classPathsFileMF = MesquiteFile.getFileContentsAsString(MesquiteModule.getRootPath() + "classpaths.xml");
					addModulesAtPaths(MesquiteModule.getRootPath(), classPathsFileMF);
				}
				
			}
			else {
				int numStandard = MesquiteTrunk.standardPackages.length; 
				int numStandardExtra = MesquiteTrunk.standardExtras.length; 
				packagesFound = new boolean[numStandard + numStandardExtra +6];
				targetDirectories = new StringArray(numStandard);
				for (int i=0; i<numStandard; i++)
					targetDirectories.setValue(i, "mesquite." + MesquiteTrunk.standardPackages[i]); //getting standard mesquite directories
				getModules("mesquite", MesquiteModule.getRootPath() + "mesquite", "", 0, targetDirectories, true, true);  //first, only do the target directories
				targetDirectories = new StringArray(numStandardExtra);
				for (int i=0; i<numStandardExtra; i++)
					targetDirectories.setValue(i, "mesquite." + MesquiteTrunk.standardExtras[i]); //getting standard mesquite directories
				getModules("mesquite", MesquiteModule.getRootPath() + "mesquite", "", 0, targetDirectories, true, true);  //first, only do the target directories

				targetDirectories = new StringArray(numStandard+ numStandardExtra+ 6);
				for (int i=0; i<numStandard; i++)
					targetDirectories.setValue(i, "mesquite." + MesquiteTrunk.standardPackages[i]); //getting standard mesquite directories
				for (int i=0; i<numStandardExtra; i++)
					targetDirectories.setValue(numStandard + i, "mesquite." + MesquiteTrunk.standardExtras[i]); //getting standard mesquite directories
				targetDirectories.setValue(numStandard + numStandardExtra, "mesquite.lib.duties");//TODO: avoid duties in all contexts!
				targetDirectories.setValue(numStandard + numStandardExtra+1, "mesquite.lib");//TODO: avoid lib in all contexts!
				targetDirectories.setValue(numStandard + numStandardExtra+2, "mesquite.documentation");//TODO: avoid documentation in all contexts!
				targetDirectories.setValue(numStandard + numStandardExtra+3, "mesquite.docs");//TODO: avoid docs in all contexts!
				targetDirectories.setValue(numStandard + numStandardExtra+4, "mesquite.macros");//TODO: avoid macros in all contexts!
				targetDirectories.setValue(numStandard + numStandardExtra+5, "mesquite.configs");  //TODO: avoid configs in all contexts!
				getModules("mesquite", MesquiteModule.getRootPath() + "mesquite", "", 0, targetDirectories, false, true); //next, add to the target directories and do everything but them
				try {
					ClassPathHacker.addFile(System.getProperty("user.home") + MesquiteFile.fileSeparator  + "Mesquite_Support_Files" + MesquiteFile.fileSeparator  + "classes");
					getModules("mesquite", System.getProperty("user.home") + MesquiteFile.fileSeparator  + "Mesquite_Support_Files" + MesquiteFile.fileSeparator  + "classes" + MesquiteFile.fileSeparator + "mesquite", "", 0, null, false, true);  //do the directories in config
					ClassPathHacker.addFile(MesquiteModule.getRootPath() +  "additionalMesquiteModules" );
					getModules("mesquite", MesquiteModule.getRootPath() +  "additionalMesquiteModules" + MesquiteFile.fileSeparator + "mesquite", "", 0, null, false, true);  //do the directories in config
					
					String classPathsFileMF = null; 
					if (MesquiteFile.fileExists(MesquiteModule.getRootPath() + "classpaths.xml")){
						classPathsFileMF = MesquiteFile.getFileContentsAsString(MesquiteModule.getRootPath() + "classpaths.xml");

						addModulesAtPaths(MesquiteModule.getRootPath(), classPathsFileMF);
					
					}
					if (MesquiteFile.fileExists(System.getProperty("user.home") + MesquiteFile.fileSeparator  + "Mesquite_Support_Files" + MesquiteFile.fileSeparator  + "classpaths.xml")){
						classPathsFileMF = MesquiteFile.getFileContentsAsString(System.getProperty("user.home") + MesquiteFile.fileSeparator  + "Mesquite_Support_Files" + MesquiteFile.fileSeparator  + "classpaths.xml");
						addModulesAtPaths(System.getProperty("user.home") + MesquiteFile.fileSeparator  + "Mesquite_Support_Files" + MesquiteFile.fileSeparator , classPathsFileMF);
					}
				}
				catch(java.io.IOException e){ 
					System.out.println("IOE in loading extra classes in Mesquite_Support_Files");
				}
				catch(Throwable e){  //to permit function under Java 1.1
				}
				}
			mesquite.mesquiteModulesInfoVector.filterAllDutyDefaults();
			mesquite.mesquiteModulesInfoVector.accumulateAllVersions();
			//timer.end();
			//CommandChecker checker = new CommandChecker();
			//checker.composeDocumentation();
			
			// get special macros from user prefs directory
			loadMacros(MesquiteModule.prefsDirectory+ MesquiteFile.fileSeparator +"macros", true);
			loadConfigs(MesquiteModule.prefsDirectory+ MesquiteFile.fileSeparator +"configs", true);

			hideMessage(true);
			hideMessage(false);
			mesquite.numDirectories = numDirectoriesCurrent;
			mesquite.logln("");
		}
	}
	
						
	void addModulesAtPaths(String relativeTo, String xmlPathsFileContents){
		if (xmlPathsFileContents == null)return;

		Parser parser = new Parser();
		parser.setString(xmlPathsFileContents);
		if (!parser.isXMLDocument(false))   // check if XML
			return;
		if (!parser.resetToMesquiteTagContents())   // check if has mesquite tag
			return;

		MesquiteString nextTag = new MesquiteString();
		String tagContent = parser.getNextXMLTaggedContent(nextTag);
		while (!StringUtil.blank(tagContent) || !StringUtil.blank(nextTag.getValue())) {
				if (!StringUtil.blank(tagContent) && "classpath".equalsIgnoreCase(nextTag.getValue())) {
					try{
						String path = MesquiteFile.composePath(relativeTo, tagContent) ; //here you pass the ith thing in the list
						DirectInit.loadJarsInDirectories(path, mesquite.jarFilesLoaded);
						ClassPathHacker.addFile(path);	

						mesquite.logln("\n\nAdditional modules loaded from " + path);
						getModules("mesquite", path+ MesquiteFile.fileSeparator  + "mesquite", "", 0, null, false, true);  //do the directories in config
					}
					catch(IOException e){
					}
				}
				tagContent = parser.getNextXMLTaggedContent(nextTag);
			}

			
	}

	
	
	
	int directoryNumber = 0;
	int modulesLoaded = 0;
	int numDirectoriesCurrent = 0;
	void checkIfModulesChanged(String path, String fileName, StringArray dontLook, StringBuffer report){ //path has no slash at the end of it
			String filePathName;
			if (StringUtil.blank(fileName))
				filePathName = path;  //
			else
				filePathName = path+ MesquiteFile.fileSeparator + fileName;
			if (fileName!=null && fileName.endsWith(".class")) {
				report.append(filePathName + StringUtil.lineEnding());
				return;
			}
			File f = new File(filePathName);  //
			if (!f.exists())
				return;
			else if (f.isDirectory()){  // is a directory; hence look inside at each item
				report.append(fileName + "===========" + StringUtil.lineEnding());
				String[] modulesList = f.list();
				mesquite.numDirectories++;
				for (int i=0; i<modulesList.length; i++)
					if (modulesList[i]!=null && !modulesList[i].endsWith(".class") && !avoidedDirectory(modulesList[i]) && (dontLook==null || dontLook.indexOf(modulesList[i])<0)) {
						checkIfModulesChanged(filePathName, modulesList[i], null, report);
					}
			}
			else {
				report.append(filePathName + StringUtil.lineEnding());
			}
	}
	boolean avoidedDirectory(String name){ //added 11 Mar 02
		return ("macros".equalsIgnoreCase(name) || "documentation".equalsIgnoreCase(name) || "docs".equalsIgnoreCase(name)  || "lib".equalsIgnoreCase(name)  || "duties".equalsIgnoreCase(name)  || "configs".equalsIgnoreCase(name));
	}
	void countModules(String path, String fileName, StringArray dontLook){ //path has no slash at the end of it
			String filePathName;
			if (StringUtil.blank(fileName))
				filePathName = path;  //
			else
				filePathName = path+ MesquiteFile.fileSeparator + fileName;
			if (fileName!=null && fileName.endsWith(".class"))
				return;
			File f = new File(filePathName);  //
			if (!f.exists())
				return;
			else if (f.isDirectory()){  // is a directory; hence look inside at each item
				String[] modulesList = f.list();
				mesquite.numDirectories++;
				if (mesquite.numDirectories % 10 == 0)
					showMessage(true, "Counting modules (approximate): " + mesquite.numDirectories, 0, 0);
				for (int i=0; i<modulesList.length; i++)
					if (modulesList[i]!=null && !modulesList[i].endsWith(".class") && !avoidedDirectory(modulesList[i]) && (dontLook==null || dontLook.indexOf(modulesList[i])<0)) {
						countModules(filePathName, modulesList[i], null);
					}
			}
	}
	/*.................................................................................................................*/
	/** Put a message in the lower left corner of the About window; integers are for thermometer use; send unassingned for no thermometer  */
	public  void showMessage(boolean upper, String s){
		if (mesquite.about!=null) {
			ThermoPanel mp = null;
			if (upper)
				mp = mesquite.about.upperMessagePanel;
			//else
			//	mp = mesquite.about.aboutMessagePanel;
			if (mp!=null){
				if (!mp.isVisible())
					mp.setVisible(true);
				if (s!=null && !s.equals(mp.getText()))
					mp.setText(s);
			}
			
		}
	}
	/*.................................................................................................................*/
	/** Put a message in the lower left corner of the About window; integers are for thermometer use; send unassingned for no thermometer  */
	public  void showMessage(boolean upper, String s, int total, int current){
		if (mesquite.about!=null) {
			ThermoPanel mp = null;
			if (upper)
				mp = mesquite.about.upperMessagePanel;
			//else
			//	mp = mesquite.about.aboutMessagePanel;
			if (mp!=null){
				if (!mp.isVisible())
					mp.setVisible(true);
				if (s!=null && !s.equals(mp.getText()))
					mp.setText(s);
				mp.setTime(total, current);
			}
			
		}
	}
	/*.................................................................................................................*/
	/** Put a message in the lower left corner of the About window; integers are for thermometer use; send unassingned for no thermometer  */
	public  void showMessage(boolean upper, int current){
		if (mesquite.about!=null) {
			ThermoPanel mp = null;
			if (upper)
				mp = mesquite.about.upperMessagePanel;
			//else
			//	mp = mesquite.about.aboutMessagePanel;
			if (mp!=null){
				mp.setTime(MesquiteLong.unassigned, current);
			}
			
		}
	}
	/*.................................................................................................................*/
	/** Remove message in the lower left corner of the About window  */
	public  void hideMessage (boolean upper){
		if (mesquite.about!=null) {
			ThermoPanel mp = null;
			if (upper)
				mp = mesquite.about.upperMessagePanel;
			//else
			//	mp = mesquite.about.aboutMessagePanel;
			if (mp!=null){
				mp.setVisible(false);
			}
			
		}
	}
	String extension(String name){
		if (StringUtil.blank(name))
			return "";
		else {
			int period = name.lastIndexOf('.');
			if (period <0)
				return "";
			return name.substring(period, name.length());
		}
	}
	String[] indent = {" ", "    ", "        ", "            ", "                ", "                   ", "                        "};
	void getModules(String packageName, String path, String fileName, int level, StringArray targetDirectories, boolean targetOn, boolean loadingAll){ //path has no slash at the end of it
			String filePathName;
			if (StringUtil.blank(fileName))
				filePathName = path;  //
			else
				filePathName = path+ MesquiteFile.fileSeparator + fileName;
			level++;  //increment depth into directory structure
			if (verboseStartup) MesquiteMessage.println(">level " + level + " " + filePathName);
			if (fileName!=null && (".class").equalsIgnoreCase(extension(fileName))) {  //this is a class file, therefore try to load it
				String cName = fileName.substring(0, fileName.lastIndexOf('.'));
				if (path.indexOf(cName)<0) //for it to be a module, name of class must appear in directory structure (e.g., name of immediately containing folder)
					return;
				File f = new File(filePathName);  //
				if (!f.exists())
					return;
				if (!"BasicFileCoordinator.class".equalsIgnoreCase(fileName)) { //is a single file; see if it's a class file; file coordinator has already been loaded
					loadMesquiteModuleClassFiles(f, path, packageName, fileName);
					if (verboseStartup){
						if (targetOn)
							MesquiteMessage.println("+loading " + fileName);
						else 
							MesquiteMessage.println("-loading " + fileName);
					}
				}
			}
			else {
				File f = new File(filePathName);  //
				if (verboseStartup) MesquiteMessage.println("    file  " + f + " exists? " + f.exists());
				if (!f.exists())
					return;
				else if (f.isDirectory()){  // is a directory; hence look inside at each item
					if (level ==2 ) {
						loadPackageExplanation(filePathName, fileName, true);
					}
					numDirectoriesCurrent++;
					String[] modulesList = f.list();
				//	if (MesquiteTrunk.isJava2DAvailable()){
						try{
						Arrays.sort(modulesList);
					}
					catch (Throwable e){//to permit function under Java 1.1 (Arrays.sort is not defined)
					}
					int numItems = modulesList.length;
					if (verboseStartup) MesquiteMessage.println("    into directory with  " + numItems + " items" );
					if (level ==2) {
						showMessage(true, "Loading from directory: " + fileName, directoryTotal, ++directoryNumber);
						mesquite.log(" " + fileName);
						if (MesquiteFile.fileOrDirectoryExists(filePathName + MesquiteFile.fileSeparator + "jars")){
							StringBuffer buffer =new StringBuffer();
							buffer.append("\n");
							DirectInit.loadJars(filePathName + MesquiteFile.fileSeparator + "jars", buffer);
							mesquite.logln(buffer.toString());
						}
					}
					else
						showMessage(true, ++directoryNumber);
					boolean macrosFound = false;
					for (int i=0; i<modulesList.length; i++) {
						if (modulesList[i]==null )
							;
						else if (!avoidedDirectory(modulesList[i])){
							String pathFM = packageName + fileName + "."+modulesList[i];
							if (targetDirectories !=null){
								int targetNumber = targetDirectories.indexOf(pathFM);
								if (targetNumber>=0 && targetNumber<packagesFound.length)
									packagesFound[targetNumber] = true;
							}
							if ((targetDirectories==null || (targetDirectories.indexOf(pathFM)<0 && !targetOn) || (targetDirectories.indexOf(pathFM)>=0 && targetOn))) {
								getModules(packageName + fileName + ".", filePathName, modulesList[i], level, null, targetOn, loadingAll);
							}
							else if (level == 1) {
								String notDonePath = filePathName+ MesquiteFile.fileSeparator + modulesList[i];
								File notDoneFile = new File(notDonePath);
								if (notDoneFile.exists() && notDoneFile.isDirectory() && !loadingAll) { //if loading all will catch later
									loadConfigs(filePathName+ MesquiteFile.fileSeparator + modulesList[i]+ MesquiteFile.fileSeparator + "configs", false);
									//loadMacros(filePathName+ MesquiteFile.fileSeparator + modulesList[i]+ MesquiteFile.fileSeparator + "macros");
									loadPackageExplanation(notDonePath, modulesList[i], false);
								}
							}
							else if (targetDirectories.indexOf(packageName + fileName + "."+modulesList[i])<0 && targetOn){
								mesquite.logln("Not loading package \"" + pathFM + "\" because not included in current configuration list");
							}
						}
						else if ("macros".equalsIgnoreCase(modulesList[i])){
							macrosFound = true;
						}
						else if ("configs".equalsIgnoreCase(modulesList[i]) && (!loadingAll || targetOn)){
							loadConfigs(filePathName+ MesquiteFile.fileSeparator + "configs", false);
						}
					}
					if (macrosFound)
						loadMacros(filePathName+ MesquiteFile.fileSeparator + "macros", false);
				}
			}
	}
	private void loadPackageExplanation(String path, String packageName, boolean loaded){
			MesquitePackageRecord pRec = new MesquitePackageRecord();
			pRec.setStored(0, packageName);
			pRec.setLoaded(loaded);
			mesquite.packages.addElement(pRec, false);
			String ePath = path + MesquiteFile.fileSeparator + "explanation.txt";
			if (MesquiteFile.fileExists(ePath)){
				String[] contents = MesquiteFile.getFileContentsAsStrings(ePath);
				if (contents!=null && contents.length>0){
					pRec.setStored(1,contents[0]);
					if (contents.length>1){
						String s = "";
						for (int i=1; i<contents.length; i++)
							s += contents[i] + "\n";
						pRec.setStored(2, s);
					}
				}
			}
							
	}

	private void loadConfigs(String path, boolean userDefined){
		if (configurations == null)
			return;
		File configDir = new File(path);
		if (configDir.exists() && configDir.isDirectory()) {
					String[] configsList = configDir.list();
					for (int i=0; i<configsList.length; i++) {
						if (configsList[i]==null )
							;
						else {
							String cPath = path + MesquiteFile.fileSeparator + configsList[i];
							File cFile = new File(cPath);
							if (cFile.exists() && !cFile.isDirectory() && configsList[i].endsWith("config")) {
								String firstLine = MesquiteFile.getFileFirstContents(cPath);
								ConfigFileRecord cfr = new ConfigFileRecord(userDefined);
								cfr.cStored[0] = cPath;
								cfr.cStored[1] = parser.getFirstToken(firstLine);
								cfr.cStored[2]= parser.getNextToken();
								configurations.addElement(cfr, false);
							}
						}
					}
		}
	}
	private void loadMacros(String path, boolean auto){  
		MesquiteInteger io = new MesquiteInteger(0);
		File macrosDirectory = new File(path);  
		if (macrosDirectory.exists() && macrosDirectory.isDirectory()) {
			String[] v = macrosDirectory.list();
			for (int i=0; i<v.length; i++) {
				String fullPath = path + MesquiteFile.fileSeparator + v[i];
				File cFile = new File(fullPath);
				if (cFile.exists() && !cFile.isDirectory()) {
					String firstLine = MesquiteFile.getFileFirstContents(fullPath);
					parser.getFirstToken(firstLine);  //"telling"
					String target  = parser.getNextToken();
					String name  = parser.getNextToken();
					String explanation  = parser.getNextToken();
					int preferredMenu  = MesquiteInteger.fromString(parser.getNextToken());
					if (explanation== null || ";".equals(explanation))
						explanation = "";
					if (name == null || ";".equals(name))
						name = v[i];
					MesquiteModuleInfo mmi = mesquite.mesquiteModulesInfoVector.findModule(MesquiteModule.class, "#" + target);
					if (mmi !=null) {
						MesquiteMacro cfr = new MesquiteMacro(name, explanation, fullPath, mmi);
						cfr.setAutoSave(auto);
						if (MesquiteInteger.isCombinable(preferredMenu))
								cfr.setPreferredMenu(preferredMenu);
						mmi.addMacro(cfr);
					}
				}
			}
		}
	}
	/*.................................................................................................................*/
	/** Returns whether module compatible with this version of Mesquite.*/
   	 private String checkModuleForCompatibility(Class original){
   	 	if (!MesquiteModule.checkMethodsAtStartup)
   	 		return null;
   	 	Class s = original;
   	 	while (s!=MesquiteModule.class && s!=Object.class) {
   	 		s = s.getSuperclass();
   	 		try {
   	 			String m = checkCompatibility(s, original);
   	 			if (m!=null)
   	 				return m;
   	 		}
   	 		catch (Exception e){
   	 			return "Exception (" + e + ") checking class " + original.getName() + " against superclass " + s.getName();
   	 		}
   	 		catch (Error e){
   	 			if (e instanceof ThreadDeath)
   	 				throw e;
   	 			else
   	 				return "Error (" + e + ") checking class " + original.getName() + " against superclass " + s.getName();
   	 		}
   	 	}
   	 	return null;
   	 	
   	 	//check all superclasses of this until arrive at MesquiteModule, for each checking that abstract methods of superclass are accurately represented in this
   	 }
   	 private String checkCompatibility(Class superClass, Class target){
   	 	//check that abstract methods of superclass are accurately represented in target
   	 	Method[] methods = superClass.getMethods();
   	 	if (methods==null)
   	 		return null;
   	 	for (int i=0; i<methods.length; i++) {
   	 		if (Modifier.isAbstract(methods[i].getModifiers())) {
   	 			//check to find if method matching signature found in target
   	 			try {
   	 				Method inTarget = target.getMethod(methods[i].getName(), methods[i].getParameterTypes());
   	 				if (inTarget==null){
   	 					return "Method " + methods[i].getName() + " which should occur in class " + target.getName() + " is absent ";
 	 				}
   	 				else if (Modifier.isAbstract(inTarget.getModifiers())){
   	 					return "Method " + methods[i].getName() + " which should occur in class " + target.getName() + " is abstract ";
 	 				}
   	 			}
	   	 		catch (Exception e){
   	 				return "Exception (" + e + ") checking method " + methods[i].getName() + " of superclass " + superClass.getName() + " against target class " + target.getName();
	   	 		}
	   	 		catch (Error e){
   	 				if (e instanceof ThreadDeath)
   	 					throw e;
   	 				else
   	 					return "Error (" + e + ") checking method " + methods[i].getName() + " of superclass " + superClass.getName() + " against target class " + target.getName();
	   	 		}
   	 			
   	 		}
   	 	}
   	 	return null;
   	 }
   	 static boolean warnedError = false;
   	 CommandChecker moduleChecker = new CommandChecker();
	/*.................................................................................................................*/
	public void loadMesquiteModuleClassFiles (File thisFile, String pathname, String packageName, String filename) {
		String classname=StringUtil.getAllButLastItem(filename, ".");
		String directoryName=StringUtil.getLastItem(pathname, MesquiteFile.fileSeparator);
		if (classname==null || !classname.equalsIgnoreCase(directoryName)) //NOTE: module must be in directory of same name!!!
			return;
		String pathSlash = pathname + MesquiteFile.fileSeparator;
		String lastTried = null;
		if (thisFile.isFile()) {
			try {
				Class c = null;
				//note: as of  21 april 2000 this simpler "Class.forName" was used instead of the more complex local ClassLoader
				lastTried = packageName + classname;
//classTime.start();
				c= Class.forName(packageName + classname);
//classTime.end();
				if (lastTried.equals("mesquite.Mesquite")){
				}
				else if (c == null)  {
					mesquite.logln("NULL returned by module class loader");
				}
				else {
//instantiateTime.start();
					MesquiteModule mb = mesquite.instantiateModule(c);
					if (mb!=null && mb instanceof MesquiteModule) {
						if (mb.isPrerelease() && mb.isSubstantive() && mb.loadModule()){
							
							MesquiteModule.mesquiteTrunk.substantivePrereleasesFound();
						}
						String message = checkModuleForCompatibility(c);
						if (message == null && mb.compatibleWithSystem() && mb.loadModule()) {
							MesquiteModuleInfo mBI = new MesquiteModuleInfo(c, mb, moduleChecker, pathSlash);
							if (!mb.getName().equals("Mesquite") && mesquite.mesquiteModulesInfoVector.nameAlreadyInList(mb.getName()))
								MesquiteTrunk.mesquiteTrunk.alert("Two modules have the same name (" + mb.getName() + ").  This may make one of the modules unavailable for use. (Module class: " + mb.getClass().getName() +
										").\n\nThis problem can arise if a module has been moved, and you update your copy of Mesquite on a Windows machine by replacing folders without deleting the previous folder, or if you are programming and you haven't updated all projects.");
							mesquite.mesquiteModulesInfoVector.addElement(mBI, false);
								mesquite.mesquiteModulesInfoVector.recordDuty(mb);
							mBI.setAsDefault(mesquite.mesquiteModulesInfoVector.isDefault(mb));
							MesquiteTrunk.mesquiteTrunk.addSplash(mBI);
							showMessage(false, configurationString);
							if (mb.getExpectedPath() !=null){
								File n = new File(mb.getExpectedPath());
								if (!n.exists())
									MesquiteMessage.warnProgrammer("...\n**************\nThe module " +mb.getName() + " (" + mb.getClass().getName() + ") expects a file or directory at " + mb.getExpectedPath() + " but it was not found. \n**************\n ...");
							}
							modulesLoaded++;
							//mesquite.logln("Loading: " + mb.getName(), MesquiteLong.unassigned, MesquiteLong.unassigned);
						}
						else if (message !=null)
							MesquiteTrunk.mesquiteTrunk.alert("Incompatible module found: " + mb.getName() + ". The module may be out of date and no longer compatible with the current version of the Mesquite system.   Error message: " + message);
						//else 
						//	MesquiteTrunk.mesquiteTrunk.alert("Incompatible module found: " + mb.getName() + ". The module may be out of date and no longer compatible with the current Java VM, the operating system. or the current version of the Mesquite system. ");
						EmployerEmployee.totalDisposed++;
						mb = null;
					}
					c = null;
				}
			}
			catch (ClassNotFoundException e){
				mesquite.logln("\n\nClassNotFoundException while loading: " + lastTried);
				MesquiteFile.throwableToLog(this, e);
				warnMissing(lastTried, e);
			}
			catch (NoClassDefFoundError e){
				mesquite.logln("\n\nNoClassDefFoundError while loading: " + lastTried);
				MesquiteFile.throwableToLog(this, e);
				warnMissing(lastTried, e);
			}
			catch (NoSuchMethodError e){
				mesquite.logln("\n\nNoSuchMethodError while loading: " + lastTried);
				MesquiteFile.throwableToLog(this, e);
				warnIncompatible(lastTried, e);
			}
			catch (AbstractMethodError e){
				mesquite.logln("\n\nAbstractMethodError while loading: " + lastTried);
				MesquiteFile.throwableToLog(this, e);
				warnIncompatible(lastTried, e);
			}
			catch (Exception e){
				mesquite.logln("\n\nException while loading: " + lastTried + "   exception: " + e.getClass());
				MesquiteFile.throwableToLog(this, e);
			}
			catch (Error e){
				mesquite.logln("\n\nError while loading: " + lastTried + "   error: " + e.getClass());
				MesquiteFile.throwableToLog(this, e);
   	 			throw e;
			}
		}
	}
	void warnIncompatible(String lastTried, Throwable e){
		if (!warnedError)
			mesquite.discreetAlert("Error while loading "  + lastTried + ".  This probably means that you have installed a package that is old or otherwise incompatible with this version of Mesquite.  Please ensure that any extra packages you have installed are up to date.  \n\nDetails: " +  e);
		warnedError = true;
	}
	void warnMissing(String lastTried, Throwable e){
		if (!warnedError)
			mesquite.discreetAlert("Error while loading "  + lastTried + ".    It appears that a component of Mesquite or a required library is missing.  We recommend that you install Mesquite again. \n\nDetails: " +  e);
		warnedError = true;
	}
}



