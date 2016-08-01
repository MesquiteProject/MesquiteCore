/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)

Modified 27 July 01: getID output as well in response to message for objects
 */
package mesquite.lib;

import java.awt.*;
import mesquite.lib.duties.*;
import java.util.*;
import java.io.*;

/* ��������������������������� commands ������������������������������� */
/* includes commands,  buttons, miniscrolls


/* ======================================================================== */
/**An object that handles scripting.  Puppeteers are the main contollers of scripts (saved in Mesquite blocks of NEXUS files, or used to clone windows, or 
in macros).  It creates its own thread to pass commands.*/
public class Puppeteer  {
	MesquiteProject project;
	MesquiteModule ownerModule;
	Random rng = new Random();
	int levelCounter = 0;
	Parser parser = new Parser();
	private Parser parser2 = new Parser();
	public Puppeteer (MesquiteModule ownerModule) {
		this.ownerModule = ownerModule;
		project = ownerModule.getProject();
		parser2.setPunctuationString("(){}:,;-+<>=\\/\''\"");
		parser.setPunctuationString("(){}:,;-+<>=\\/\''\"");
	}
	/* -------------------------------------*/
	/** Calls up a dialog box in which the user can enter a script to be sent to the passed object. */
	public void dialogScript(Object baseObject, MesquiteWindow f, String objectToCommand){
		if (f==null)
			f = MesquiteTrunk.mesquiteTrunk.containerOfModule();
		String commands = MesquiteString.queryMultiLineString(f, "Script", "Script by which to command " + objectToCommand, "", 8, false, true);
		if (!StringUtil.blank(commands))  {
			MesquiteInteger pos = new MesquiteInteger(0);
			MesquiteWindow.tickClock("Executing script");
			MesquiteModule.incrementMenuResetSuppression();	
			CommandRecord prevR = MesquiteThread.getCurrentCommandRecord();
			MesquiteThread.setCurrentCommandRecord(new CommandRecord(true));
			sendCommands(baseObject, commands, pos, "", false, null,  CommandChecker.defaultChecker);
			MesquiteThread.setCurrentCommandRecord(prevR);
			MesquiteModule.decrementMenuResetSuppression();	
			MesquiteWindow.hideClock();
		}
	}
	/* -------------------------------------*/
	/** Execute the passed string of commands (currently at string location 'pos') until the String "endString' is reached, skipping commands if 
	the skip flag is set.  The NexusBlock is passed currently only so that the commands may assign it a title */
	public void execute(Object baseObject, String commands, MesquiteInteger pos, String endString, boolean skip, NexusBlock nblock, MesquiteFile file){ 
		MesquiteModule.incrementMenuResetSuppression();	
		if (file == null){
			MesquiteWindow.tickClock("Executing script");
			sendCommands(baseObject, commands, pos, endString, skip,nblock, CommandChecker.defaultChecker);
			MesquiteWindow.hideClock();
		}
		else {
			CommandChecker checker = new CommandChecker();
			checker.setFile(file);
			MesquiteWindow.tickClock("Executing script");
			sendCommands(baseObject, commands, pos, endString, skip,nblock, checker);
			MesquiteWindow.hideClock();
		} 
		MesquiteModule.decrementMenuResetSuppression();	
	}

	/* -------------------------------------*/
	/** Execute the passed string of commands (currently at string location 'pos') until the String "endString' is reached, skipping commands if 
	the skip flag is set.  The NexusBlock is passed currently only so that the commands may assign it a title */
	public Object executeWithResult(Object baseObject, String commands, MesquiteInteger pos, String endString, boolean skip, NexusBlock nblock, MesquiteFile file){ 
		Object result = null;
		MesquiteModule.incrementMenuResetSuppression();	
		if (file == null){
			MesquiteWindow.tickClock("Executing script");
			result = sendCommands(baseObject, commands, pos, endString, skip,nblock, CommandChecker.defaultChecker);
			MesquiteWindow.hideClock();
		}
		else {
			CommandChecker checker = new CommandChecker();
			checker.setFile(file);
			MesquiteWindow.tickClock("Executing script");
			result = sendCommands(baseObject, commands, pos, endString, skip,nblock, checker);
			MesquiteWindow.hideClock();
		} 
		MesquiteModule.decrementMenuResetSuppression();	
		return result;
	}
	/* -------------------------------------*/
	/** Execute the passed string of commands (currently at string location 'pos') until the String "endString' is reached, skipping commands if 
	the skip flag is set. */
	public void execute(Object baseObject, String commands, MesquiteInteger pos, String endString, boolean skip){
		execute(baseObject, commands, pos, endString, skip,null, null); 
	}
	/* -------------------------------------*/
	/** Apply macro file to given commandable*/
	public void applyMacroFile(String fileName, Commandable obj){
		if (obj == null)
			return;
		String tellingCommand = MesquiteFile.getFileFirstContents(fileName);
		MesquiteInteger macroPos = new MesquiteInteger(0);
		String com = ParseUtil.getFirstToken(tellingCommand, macroPos);
		Commandable toCommand = obj;
		if ("telling".equalsIgnoreCase(com)){
			String cName = ParseUtil.getToken(tellingCommand, macroPos);
			if (cName.equalsIgnoreCase("FileCoordinator"))
				toCommand = ownerModule.getFileCoordinator();
			else if (cName.equalsIgnoreCase("Mesquite"))
				toCommand = MesquiteTrunk.mesquiteTrunk;
		}

		String macroFile = MesquiteFile.getFileContentsAsString(fileName);
		if (StringUtil.blank(macroFile))
			return;
		macroPos.setValue(0);
		MesquiteModule.incrementMenuResetSuppression(); // seems to hang for some macros involving drawing trees (may be thread deadlock issue
		CommandRecord mr = MesquiteThread.getCurrentCommandRecord();
		MesquiteThread.setCurrentCommandRecord(CommandRecord.macroRecord);
		execute(toCommand, macroFile, macroPos, "", false, null, null); 
		MesquiteThread.setCurrentCommandRecord(mr);
		MesquiteModule.decrementMenuResetSuppression(); //seems to hang for some macros involving drawing trees (may be thread deadlock issue
	}
	/*--------------------------------------*/
	/** Gets the second part of the string, the token after the '.' */
	private String getSecondWord(String s) {
		MesquiteInteger period = new MesquiteInteger(s.indexOf(".") + 1);
		return ParseUtil.getToken(s, period);
	}
	/*--------------------------------------*/
	/* The following sections deal with the variable types for strings, objects, integers and numbers.
	There are local variables for each of these to hold the variables the script creates.*/
	/*-----------------STRINGS ---------------------*/
	private int arrayLengths = 128;
	private String[] strings = new String[arrayLengths];
	private String[] stringNames = new String[arrayLengths];
	private int numStrings = 0;

	/*--------------------------------------*/
	/** returns the String stored in the variable with the given name */
	private String findString(String name) {
		int item = StringArray.indexOfIgnoreCase(stringNames, name);
		if (item>=0 && item < strings.length)
			return strings[item];
		else
			return null;
	}
	/*-------------------OBJECTS-------------------*/
	private Object[] objects = new Object[arrayLengths];
	private String[] objectNames = new String[arrayLengths];
	private int numObjects = 0;
	/*--------------------------------------*/
	/** returns the Object stored in the variable with the given name */
	private Object findObject(String name) {
		int item = StringArray.indexOfIgnoreCase(objectNames, name);
		if (item>=0 && item < objects.length)
			return objects[item];
		else
			return null;
	}
	/*--------------------INTEGERS ------------------*/
	private MesquiteInteger[] integers = new MesquiteInteger[arrayLengths];
	private String[] integerNames = new String[arrayLengths];
	private int numIntegers = 0;
	/*--------------------------------------*/
	/** returns the MesquiteInteger stored in the variable with the given name */
	private MesquiteInteger findInteger(String name) {
		int item = StringArray.indexOfIgnoreCase(integerNames, name);
		if (item>=0 && item < integers.length)
			return integers[item];
		else
			return null;
	}
	/*------------------NUMBERS--------------------*/
	private MesquiteNumber[] numbers = new MesquiteNumber[arrayLengths];
	private String[] numberNames = new String[arrayLengths];
	private int numNumbers = 0;
	/*--------------------------------------*/
	/** returns the MesquiteNumber stored in the variable with the given name */
	private MesquiteNumber findNumber(String name) {
		int item = StringArray.indexOfIgnoreCase(numberNames, name);
		if (item>=0 && item < numbers.length)
			return numbers[item];
		else
			return null;
	}
	/*====================================*/
	/** Translates an argument that is intended to be a string but which may be some indirect reference into a string */
	private String argumentToString(String rawArguments, Object result) {
		if (ParseUtil.firstDarkChar(rawArguments)=='\'')
			return rawArguments; //ParseUtil.getToken(rawArguments, pos);
		String processed = "";
		String name;
		boolean first = true;
		parser2.setString(rawArguments);
		while (!StringUtil.blank(name = parser2.getNextToken())) {
			if (!first)
				processed += " ";
			first = false;
			if (name.length() == 1 && name.charAt(0)=='*') //this is needed because system was written to have * attached to next word, but in nexus file could be unattached
				name+=parser2.getNextToken();

			if (name.startsWith("*")) { //key character to indicate non-literal; i.e., look for substituting object
				String st = name.substring(name.lastIndexOf("*")+1, name.length());
				if (st.equalsIgnoreCase("it")) {
					if (result !=null) {
						if (result instanceof String)
							processed += (String)result;
						else if (result instanceof MesquiteInteger)
							processed += result.toString();
						else if ( result instanceof Listable)
							processed +=((Listable)result).getName();
						if (result instanceof Identifiable)
							processed += " (id " +  ((Identifiable)result).getID() + ")";
					}
				}
				else {
					if (StringUtil.startsWithIgnoreCase(st,"Integer")) {
						MesquiteInteger mi = findInteger(getSecondWord(st));
						if (mi!=null)
							processed += mi.toString();
					}
					else if (StringUtil.startsWithIgnoreCase(st,"Number")) {
						MesquiteNumber mi = findNumber(getSecondWord(st));
						if (mi!=null)
							processed += mi.toString();
					}
					else if (StringUtil.startsWithIgnoreCase(st,"Object")) {
						Object obj = findObject(getSecondWord(st));
						if (obj != null && obj instanceof Listable)
							processed +=((Listable)obj).getName();
						if (obj != null && obj  instanceof Identifiable)
							processed += " (id " +  ((Identifiable)obj).getID() + ")";
					}
					else if (StringUtil.startsWithIgnoreCase(st,"String")) {
						String s0 = findString(getSecondWord(st));
						if (s0!=null)
							processed += s0;
					}

				}
			}
			else {
				processed += StringUtil.tokenize(name); //StringUtil.deTokenize(name);
			}
		}
		return processed;

	}
	/*--------------------------------------*/
	/** Translates an argument that is intended to be a number but which may be some indirect reference into a number */
	private void argumentToNumber(String name, Object result, MesquiteNumber theNum) {
		if (name.startsWith("*")) { //key character to indicate non-literal; i.e., look for substituting object
			String st = name.substring(name.lastIndexOf("*")+1, name.length());
			if (st.equalsIgnoreCase("it")) {
				if (result !=null) {
					if (result instanceof String)
						theNum.setValue((String)result);
					else if (result instanceof MesquiteNumber)
						theNum.setValue((MesquiteNumber)result);
					else
						theNum.setToUnassigned();
				}
			}
			else {
				if (StringUtil.startsWithIgnoreCase(st,"Integer")) {
					MesquiteInteger mi = findInteger(getSecondWord(st));
					if (mi!=null)
						theNum.setValue(mi.getValue());
				}
				else if (StringUtil.startsWithIgnoreCase(st,"Number")) {
					MesquiteNumber mi = findNumber(getSecondWord(st));
					if (mi!=null)
						theNum.setValue(mi);
				}
				else if (StringUtil.startsWithIgnoreCase(st,"Object")) {
					Object obj = findObject(getSecondWord(st));
					if (obj != null && obj instanceof MesquiteInteger)
						theNum.setValue(((MesquiteInteger)obj).getValue());
					else if (obj != null && obj instanceof MesquiteNumber)
						theNum.setValue(((MesquiteNumber)obj));
				}
				else if (StringUtil.startsWithIgnoreCase(st,"String")) {
					String s0 = findString(getSecondWord(st));
					if (s0!=null)
						theNum.setValue(s0);
				}

			}
		}
		else
			theNum.setValue(name);
	}
	/*--------------------------------------*/
	/** Translates an argument that is intended to be an integer but which may be some indirect reference into an integer */
	private int argumentToInteger(String name, Object result) {
		if (name.startsWith("*")) { //key character to indicate non-literal; i.e., look for substituting object
			String st = name.substring(name.lastIndexOf("*")+1, name.length());
			if (st.equalsIgnoreCase("it")) {
				if (result !=null) {
					if (result instanceof String)
						return MesquiteInteger.fromString((String)result);
					else if (result instanceof MesquiteInteger)
						return ((MesquiteInteger)result).getValue();
					else
						return 0;
				}
			}
			else {
				if (StringUtil.startsWithIgnoreCase(st,"Integer")) {
					MesquiteInteger mi = findInteger(getSecondWord(st));
					if (mi!=null)
						return mi.getValue();
				}
				else if (StringUtil.startsWithIgnoreCase(st,"Object")) {
					Object obj = findObject(getSecondWord(st));
					if (obj != null && obj instanceof MesquiteInteger)
						return ((MesquiteInteger)obj).getValue();
				}
				else if (StringUtil.startsWithIgnoreCase(st,"String")) {
					String s0 = findString(getSecondWord(st));
					if (s0!=null)
						return MesquiteInteger.fromString(s0);
				}

			}
			return 0;
		}
		else return MesquiteInteger.fromString(name);
	}
	/*--------------------------------------*/
	private boolean debugging = false; 
	private boolean logOnly = false;
	private MesquiteTimer timer;
	private boolean showTime = false;
	/*................................................................................................................*/
	private MesquiteInteger pagingPos = new MesquiteInteger(0);
	private String pagingBlock;
	private boolean pagingSkip;
	private NexusBlock pagingNB;
	private MesquiteModule pagedModule;
	/** This is for the paging system, which may be defunct. */
	protected void returningPage(MesquiteModule pagedModule){
		if (this.pagedModule == pagedModule){
			MesquiteInteger stringPos = new MesquiteInteger(pagingPos.getValue());
			sendCommands(pagedModule, pagingBlock, stringPos, "endPaging;", pagingSkip, pagingNB, CommandChecker.defaultChecker);
		}
	}
	/*--------------------------------------*/
	private boolean stampToLog = true;
	/** Stamp the string to the System console and perhaps also the log */
	private void stamp(String s){
		if (logOnly)
			MesquiteFile.writeToLog(s+ StringUtil.lineEnding());
		else {
			System.out.println(s);
			if (stampToLog)
				ownerModule.logln(s);
		}
	}
	/*--------------------------------------*/
	/** Stamp the time since the last call for a time stamp */
	private void stampTimeSinceLast() {
		stamp("        TIME " + timer.timeSinceLast() + "  of  " + timer.timeSinceVeryStart());
	}
	//this is needed because system was written to have * attached to next word, but in nexus file could be unattached
	private String nextTokenCompressAsterisk(Parser parser){
		String s = parser.getNextToken();
		if (s !=null && s.startsWith("*")) {
			String n = parser.getNextToken();
			if (n!=null)
				return s+=n;
			else
				return s;
		}
		else
			return s;
	}
	/* -------------------------------------*/
	/** Sends commands to the object, but on a new thread. */
	public void sendCommandsOnNewThread(Object baseObject, String commands, MesquiteInteger pos, String endString, boolean skip, NexusBlock nblock, boolean scripting){
		PuppetThread puppetThread = new PuppetThread(this, baseObject, commands, pos, endString, skip, nblock, scripting);
		puppetThread.start();
	}
	String[] defunctModules = new String[]{"#mesquite.ornamental.CellPictures.CellPictures", "#mesquite.collab.aaManageImageIndices.aaManageImageIndices", "#mesquite.collab.DataImages.DataImages"};
	boolean defunctModule(String moduleName){
		if (StringArray.indexOf(defunctModules, moduleName) >= 0)
			return true;
		return false;
	}
	/*.................................................................................................................*/
	private String getEmployeeNotFoundWarning(String moduleName){
		if (moduleName == null)
			return "A script has failed to execute properly, possibly because it expected certain modules not available in the current configuration of Mesquite.  Details: A command (\" getEmployee\") did not result in the return of an object as expected.  The script's results may not be as expected.\nMore details are written into Mesquite's log file."; 
		if (defunctModule(moduleName))
			return null;
		String message = "A script has failed to execute properly, as it refers to a module \"" + StringUtil.getLastItem(moduleName, ".") + "\", that either did not start or was unavailable because the module is not installed or activated.";
		boolean compoundClassName = (moduleName.charAt(0) == '#' && moduleName.indexOf(".")>=0);
		if (moduleName.charAt(0) == '#' && moduleName.indexOf(".")>=0) { //compound class name
			String useName = moduleName.substring(1, moduleName.length());
			String s = StringUtil.getAllButLastItem(useName, ".");
			while (s!=null && StringUtil.characterCount(s, '.')>1) {
				s = StringUtil.getAllButLastItem(s, ".");
			}
			if (s!=null && StringUtil.characterCount(s, '.')==1){
				//s is now package name; find package and diagnose
				MesquitePackageRecord mpr = MesquitePackageRecord.findPackage(s);
				if (mpr == null)
					message += "\nThe package in which this module resides (" + s + ") appears not to be installed.";
				else if (mpr.loaded)
					message += "\nThe package in which this module resides (" + mpr.getName() + ") is installed and activated (loaded).  Perhaps the script has an error, or the module failed to start, or the package has changed.";
				else 
					message += "\nThe package in which this module resides (" + mpr.getName() + ") is installed but is not activated (loaded).  To use this module, change the activation/deactivation status of the package using the menu items in the File menu, and restart Mesquite.";
			}

		}
		else { 
			String useName = moduleName;
			if (moduleName.charAt(0) == '#')
				useName = moduleName.substring(1, moduleName.length());
			if (!MesquitePackageRecord.allPackagesActivated())
				message += "\nSome packages of modules appear to be installed but not activated (loaded). Check and perhaps change the activation/deactivation status of packages using the menu items in the File menu.";
		}
		return message;
	}
	private Object cancel(MesquiteInteger stringPos, String block){
		stringPos.setValue(block.length()); 
		MesquiteTrunk.mesquiteTrunk.alert("Script cancelled.");
		return null;
	}
	private void showCommands(Object mb, String arguments, boolean showExpl){

		if (mb != null || mb instanceof Commandable){
			String objectName;
			if (mb instanceof Listable) {
				if (mb instanceof MesquiteWindow)
					objectName = "Window: " + ((Listable)mb).getName();
				else
					objectName = ((Listable)mb).getName();
			}
			else
				objectName = mb.getClass().getName();
			if (";".equals(arguments))
				arguments = null;
			boolean single = !StringUtil.blank(arguments);
			CommandChecker cc = new CommandChecker();
			cc.setSparseMode();
			CommandChecker.accumulate((Commandable)mb, cc);
			Vector v = cc.getAccumulatedCommands();
			Vector ve = cc.getAccumulatedExplanations();
			if (v !=null || v.size() == 0) {
				if (!single)
					MesquiteTrunk.mesquiteTrunk.logln("Commands of " + objectName);
				for (int i = 0; i< v.size(); i++) {
					Object obj = v.elementAt(i);

					if (obj instanceof String) {
						String sCM = (String)obj;
						if (StringUtil.blank(arguments) || arguments.equalsIgnoreCase(sCM)) {
							Object objE = null;
							if ((showExpl || single) && i < ve.size())
								objE = ve.elementAt(i);
							if (objE !=null)
								sCM += "  - " + objE;
							if (single || showExpl)
								MesquiteTrunk.mesquiteTrunk.logln(sCM);
							else
								MesquiteTrunk.mesquiteTrunk.log("  " + sCM);
						}

					}
				}
				MesquiteTrunk.mesquiteTrunk.logln("");
			}
			else
				MesquiteTrunk.mesquiteTrunk.logln(" No Commands are available for " + objectName);
		}
	}
	private MesquiteProject getProject(Object mb, NexusBlock nb, Object result){
		if (nb !=null) {
			MesquiteFile f = nb.getFile();
			if (f !=null) {
				return f.getProject();
			}
		}
		if (CommandRecord.getScriptingFileS() !=null){
			return CommandRecord.getScriptingFileS().getProject();
		}
		if (mb != null && mb instanceof MesquiteModule  && ((MesquiteModule)mb).getProject() != null){
			return ((MesquiteModule)mb).getProject();
		}
		if (result != null && result instanceof MesquiteModule  && ((MesquiteModule)result).getProject() != null){
			return ((MesquiteModule)result).getProject();
		}
		if (mb != null && mb instanceof MesquiteProject){
			return  (MesquiteProject)mb;
		}
		if (mb != null && mb instanceof MesquiteFile){
			return  ((MesquiteFile)mb).getProject();
		}
		return null;
	}
	private String writingDirectory(){
		if (project == null){
			if (MesquiteTrunk.getProjectList().getNumProjects()==1)
				return MesquiteTrunk.getProjectList().getProject(0).getHomeDirectoryName();
			return "";
		}
		String s = project.getHomeDirectoryName();
		return s;
	}
	/*--------------------------------------------------------------------------------------------------------*/
	/* This assumes string position is just afer a control block start; it will go until corresponding endString */
	public Object sendCommands(Object mb, String block, MesquiteInteger stringPos, String endString, boolean skip, NexusBlock nb, CommandChecker checker) {
		int level = levelCounter++;
		Parser parser = new Parser();
		Parser commandParser = new Parser();
		commandParser.setString(block);
		String s;
		Object cm = null;
		Object result = null;
		boolean fineDebugging = false;
		String previousCommand = "";
		String previousArguments = "";
		String previousCommandName = "";
		String rememberedArguments = null;
		while (!StringUtil.blank(s=commandParser.getNextCommand(stringPos)) && !s.equalsIgnoreCase(endString)) {

			if (debugging) {
				if (mb == null)
					stamp("<NULL OBJECT>" + s);
				else
					stamp("<" +  mb.getClass().getName() + ">" + s);
			}
			if (fineDebugging) stamp("-sc- (" + level + ")1");
			String commandName = parser.getFirstToken(s);
			//MesquiteWindow.tickClock("Executing: " + commandName);
			CommandRecord.tick("Executing " + commandName);
			if (fineDebugging) stamp("-sc- (" + level + ")2");
			if (checker.compare(null, "Send the commands to follow (until endTell) to the indicated object", "[Object to which commands are to be sent]", commandName, "tell")) {
				if (fineDebugging) stamp("-sc- (" + level + ")7");
				String argument = nextTokenCompressAsterisk(parser);
				Object who = null;
				if (fineDebugging) stamp("-sc- (" + level + ")8");
				if (argument.equalsIgnoreCase("It"))
					who = result;
				else if (argument.equalsIgnoreCase("Mesquite"))
					who = MesquiteTrunk.mesquiteTrunk;
				else if (argument.equalsIgnoreCase("ProjectCoordinator")) {
					if (mb instanceof FileCoordinator)
						who = mb;
					else if (result instanceof FileCoordinator)
						who = result;
					else if (mb instanceof MesquiteModule &&  ((MesquiteModule)mb).getFileCoordinator()!=null){
						who = ((MesquiteModule)mb).getFileCoordinator();
					}
					else {
						who = getProject(mb, nb, result);
					}
				}
				else
					who = findObject(getSecondWord(argument));

				if (fineDebugging) stamp("-sc- (" + level + ")9");
				if (who==null && !skip) {
					String message ="Object of tell command is null (previous command: \"" + previousCommand + "\" to object " + mb + ")";
					if (debugging)
						stamp(message);
					else 
						ownerModule.logln("A script has failed to execute properly, possibly because it expected certain modules not available in the current configuration of Mesquite.  Details: " + message);
					CommandRecord comRec = MesquiteThread.getCurrentCommandRecord();
					if (comRec != null){
						if (!comRec.getErrorFound()) {
							if ("getEmployee".equalsIgnoreCase(previousCommandName)){ //the common case in which a better error can be given informing user about unloaded packages
								MesquiteTrunk.mesquiteTrunk.logln(getEmployeeNotFoundWarning(previousArguments));
							}
							else
								MesquiteTrunk.mesquiteTrunk.discreetAlert("A script has failed to execute properly, possibly because it expected certain modules not available in the current configuration of Mesquite.  Details: A command (\"" + previousCommand + "\") did not result in the return of an object as expected.  The script's results may not be as expected.\nMore details are written into Mesquite's log file."); 
						}
						comRec.setObjectToldNullWarning();
						comRec.setErrorFound();
					}
				}
				if (debugging) {
					if (skip) 
						stamp("  [skipping]");
					else if (who==null)
						stamp("  [will pass over commands]");
					else if (who instanceof Listable)
						stamp("   ----- about to send commands to " + "  ---[" + ((Listable)who).getName() + "]");
					else
						stamp("   ----- about to send commands to " + "  ---[" + who + "]");
				}
				if (fineDebugging) stamp("-sc- (" + level + ")10");
				sendCommands(who, block, stringPos, "endTell;", skip, nb, checker);
				if (fineDebugging) stamp("-sc- (" + level + ")11");
			} 
			//else if (checker.compare(null, "End this level of the script", null, commandName, "endTell")) {
			else if (checker.compare(null, "End this level of the script", null, commandName, "endTell")) {
				if (fineDebugging) stamp("-sc- (" + level + ")3a1");
				stringPos.setValue(stringPos.getValue()-8); //reset to before the endTell, since this should only get here because of an exitTell, and need to save the endTell for appropriate level
				return null;
			}
			else if (checker.compare(null, "Exit out of this current level of the script, jumping to 'endTell'", null, commandName, "exitTell")) {
				if (fineDebugging) stamp("-sc- (" + level + ")3");
				sendCommands(mb, block, stringPos, null, true, nb, checker);
			}
			else if (checker.compare(null, "Toggles suppression of all progress indicators on this thread", null, commandName, "toggleSuppressAllProgressIndicatorsCurrentThread")) {
				if (fineDebugging) stamp("-sc- (" + level + ")3#toggleSuppressAllProgressIndicatorsCurrentThread");
				MesquiteThread.setSuppressAllProgressIndicatorsCurrentThread(!MesquiteThread.getSuppressAllProgressIndicators(Thread.currentThread()));
			}
			else if (checker.compare(null, "Shows the log window", null, commandName, "showLogWindow")) {
				if (fineDebugging) stamp("-sc- (" + level + ")3#showLogWindow");
				MesquiteTrunk.showLogWindow();
			}
			else if (checker.compare(null, "Writes to log & console a list of the commands available for the currently commanded object", null, commandName, "sc")) {
				if (fineDebugging) stamp("-sc- (" + level + ")3#");
				showCommands(mb, nextTokenCompressAsterisk(parser), false);
			}
			else if (checker.compare(null, "Writes to log & console a list of the commands available for the currently commanded object", null, commandName, "?")) {
				if (fineDebugging) stamp("-sc- (" + level + ")3#?");
				showCommands(mb, nextTokenCompressAsterisk(parser), false);
			}
			else if (checker.compare(null, "Writes to log & console a list of the commands available for the currently commanded object", null, commandName, "??")) {
				if (fineDebugging) stamp("-sc- (" + level + ")3#?");
				showCommands(mb, nextTokenCompressAsterisk(parser), true);
			}
			else if (checker.compare(null, "Stop this script", null, commandName, "stop")) {
				if (fineDebugging) stamp("-sc- (" + level + ")3a");
				stringPos.setValue(block.length()); 
				skip = true;
				return null;
			}
			else if (skip && !checker.getAccumulateMode()) {
				if (debugging) stamp("  [skipping]");
			}	
			/*=== Commands above this are processed even if the "skip" flag is on; commands below this are skipped if the flag is on ====*/

			/* ----------------------- CONTROL FLOW -----------------------*/
			else if (checker.compare(null, "The start of a FOR loop (end is 'endFor')", "[An integer or reference to an integer, indicating the number of times the loop is to be cycled]", commandName, "for")) {
				int startRepeatPos = stringPos.getValue();
				String argument = nextTokenCompressAsterisk(parser);
				MesquiteInteger theInt = findInteger(getSecondWord(argument));
				int repetitions = 0;
				if (theInt==null)
					repetitions = MesquiteInteger.fromString(argument);
				else 
					repetitions = theInt.getValue();

				if (debugging)
					stamp("    @FOR loop with " + repetitions + " repetitions (skip " + skip + ")");
				while (repetitions>0) {
					stringPos.setValue(startRepeatPos);
					sendCommands(mb, block, stringPos, "endFor;", skip, nb, checker);
					if (skip)
						repetitions = 0; //end immediately
					else
						repetitions --;
				}

			}
			else if (checker.compare(null, "The start of a WHILE loop (end is 'endWhile')", "[An integer or reference to an integer; loop is cycled until that integer equals zero]", commandName, "while")) {
				int startRepeatPos = stringPos.getValue();
				String argument = nextTokenCompressAsterisk(parser);
				MesquiteInteger theInt = findInteger(getSecondWord(argument));
				if (debugging) 
					stamp("           @WHILE with argument: " + argument + " value: " + theInt);
				if (theInt == null || skip)
					sendCommands(mb, block, stringPos, "endWhile;", true, nb, checker);
				else {
					while (theInt.getValue()!=0) {
						stringPos.setValue(startRepeatPos);
						sendCommands(mb, block, stringPos, "endWhile;", skip, nb, checker);
					}
				}
			}
			else if (checker.compare(null, "The start of an IF block (end is 'endIf')", "[An integer or reference to an integer; block is entered if integer is non-zero]", commandName, "if")) {
				String argument = nextTokenCompressAsterisk(parser);
				MesquiteInteger theInt = findInteger(getSecondWord(argument));
				if (debugging) 
					stamp("           @IF with argument: " + argument + " value: " + theInt);
				sendCommands(mb, block, stringPos, "endIf;", skip || (theInt==null) || theInt.getValue() == 0, nb, checker);
			}
			else if (checker.compare(null, "The start of an IFNOT block (end is 'endIf')", "[An integer or reference to an integer; block is entered if integer is zero]", commandName, "ifNot")) {
				String argument = nextTokenCompressAsterisk(parser);
				MesquiteInteger theInt = findInteger(getSecondWord(argument));
				if (debugging) 
					stamp("           @IFNOT with argument: " + argument + " value: " + theInt);
				sendCommands(mb, block, stringPos, "endIf;", skip || (theInt==null) || theInt.getValue() != 0, nb, checker);
			}
			else if (checker.compare(null, "The start of an IFEXISTS block (end is 'endIf')", "[A reference to an object; block is entered if object is non-null]", commandName, "ifExists")) {
				Object who = null;
				String argument = nextTokenCompressAsterisk(parser);
				if (argument!=null){
					if (argument.equalsIgnoreCase("It"))
						who = result;
					else if (argument.equalsIgnoreCase("Mesquite"))
						who = MesquiteTrunk.mesquiteTrunk;
					else
						who = findObject(getSecondWord(argument));
				}
				if (debugging) 
					stamp("           @IFEXISTS with argument: " + argument + " who: " + who);
				sendCommands(mb, block, stringPos, "endIf;", skip || (who==null), nb, checker);

			}
			else if (checker.compare(null, "The start of an IFCOMBINABLE block (end is 'endIf')", "[A reference to a number object; block is entered if object is combinable]", commandName, "ifCombinable")) {
				Object who = null;
				String argument = nextTokenCompressAsterisk(parser);
				MesquiteInteger theInt = findInteger(getSecondWord(argument));
				if (debugging) 
					stamp("           @IFCOMBINABLE with argument: " + argument + " value: " + theInt);
				sendCommands(mb, block, stringPos, "endIf;", skip || (theInt==null) || !theInt.isCombinable(), nb, checker);
			}
			else if (checker.compare(null, "The start of an IFNOTCOMBINABLE block (end is 'endIf')", "[A reference to a number object; block is entered if object is not combinable]", commandName, "ifNotCombinable")) {
				Object who = null;
				String argument = nextTokenCompressAsterisk(parser);
				MesquiteInteger theInt = findInteger(getSecondWord(argument));
				if (debugging) 
					stamp("           @IFNOTCOMBINABLE with argument: " + argument + " value: " + theInt);
				sendCommands(mb, block, stringPos, "endIf;", skip || ((theInt!=null) && theInt.isCombinable()), nb, checker);
			}
			else if (checker.compare(null, "The start of an IFNOTEXISTS block (end is 'endIf')", "[A reference to an object; block is entered if object is null]", commandName, "ifNotExists")) {
				Object who = null;
				String argument = nextTokenCompressAsterisk(parser);
				if (argument!=null){
					if (argument.equalsIgnoreCase("It"))
						who = result;
					else if (argument.equalsIgnoreCase("Mesquite"))
						who = MesquiteTrunk.mesquiteTrunk;
					else
						who = findObject(getSecondWord(argument));
				}
				if (debugging) 
					stamp("           @IFNOTEXISTS with argument: " + argument + " who: " + who);
				sendCommands(mb, block, stringPos, "endIf;", skip || (who!=null), nb, checker);
			}

			/* ----------------------- VARIABLES -----------------------*/
			else if (checker.compareStart(null, "Defines or recalls an integer variable", "To be immediately followed by name of integer, as part of the same token  [integer, or reference to integer variable, or 'random']", commandName, "Integer.")) {
				if (fineDebugging) stamp("-sc- (" + level + ")14");
				MesquiteInteger theInt = findInteger(getSecondWord(commandName));
				String argument = nextTokenCompressAsterisk(parser);
				boolean doAdd = false;
				boolean doSubtract = false;
				if (argument.equals("-")){
					doSubtract = true;
					argument = nextTokenCompressAsterisk(parser);
				}
				else if (argument.startsWith("-")) {
					doSubtract = true;
					argument = argument.substring(argument.indexOf("+")+1, argument.length());
				}
				else if (argument.equals("+")){
					doAdd = true;
					argument = nextTokenCompressAsterisk(parser);
				}
				else if (argument.startsWith("+")) {
					doAdd = true;
					argument = argument.substring(argument.indexOf("+")+1, argument.length());
				}
				int value;
				if (argument !=null && argument.equalsIgnoreCase("random")){
					value = Math.abs(rng.nextInt());
				}
				else
					value =argumentToInteger(argument, result);

				if (fineDebugging) stamp("-sc- (" + level + ")15");
				if (debugging) 
					stamp("           argument: " + argument + " value of integer: " + value);
				if (theInt ==null) {
					integers[numIntegers] = new MesquiteInteger(value);
					integerNames[numIntegers] = getSecondWord(commandName);
					numIntegers++;
				}
				else {
					if (doAdd)
						theInt.add(value);
					else if (doSubtract)
						theInt.subtract(value);
					else
						theInt.setValue(value);
				}
				if (fineDebugging) stamp("-sc- (" + level + ")16");
			}
			else if (checker.compareStart(null, "Defines or recalls a numerical variable (integer or not)", "To be immediately followed by name of number, as part of the same token [integer, or reference to integer variable, or 'random' (double between 0 and 1); if token is preceded by '+', value will be added to existing]", commandName, "Number.")) {
				if (fineDebugging) stamp("-sc- (" + level + ")17");
				MesquiteNumber theNum = findNumber(getSecondWord(commandName));
				String argument = nextTokenCompressAsterisk(parser);
				boolean doAdd = false;
				boolean doSubtract = false;
				if (argument.equals("-")){
					doSubtract = true;
					argument = nextTokenCompressAsterisk(parser);
				}
				else if (argument.startsWith("-")) {
					doSubtract = true;
					argument = argument.substring(argument.indexOf("+")+1, argument.length());
				}
				else if (argument.equals("+")){
					doAdd = true;
					argument = nextTokenCompressAsterisk(parser);
				}
				else if (argument.startsWith("+")) {
					doAdd = true;
					argument = argument.substring(argument.indexOf("+")+1, argument.length());
				}
				if (theNum ==null) {
					theNum =  new MesquiteNumber();
					numbers[numNumbers] = theNum;
					numberNames[numNumbers] = getSecondWord(commandName);
					numNumbers++;
				}
				if (fineDebugging) stamp("-sc- (" + level + ")18");
				if (argument !=null && argument.equalsIgnoreCase("random")) {
					if (doAdd)
						theNum.add(Math.abs(rng.nextDouble()));
					else if (doSubtract)
						theNum.subtract(Math.abs(rng.nextDouble()));
					else
						theNum.setValue(Math.abs(rng.nextDouble()));
				}
				else {
					if (doAdd) {
						MesquiteNumber toAdd = new MesquiteNumber(0);
						argumentToNumber(argument, result, toAdd);
						theNum.add(toAdd);
					}
					else if (doSubtract) {
						MesquiteNumber toSub = new MesquiteNumber(0);
						argumentToNumber(argument, result, toSub);
						theNum.subtract(toSub);
					}
					else
						argumentToNumber(argument, result, theNum);
				}
				if (debugging) 
					stamp("           value of number: " + theNum.toString());
				if (fineDebugging) stamp("-sc- (" + level + ")19");
			}
			else if (checker.compareStart(null, "Decrements by one the named integer variable, or by a value other than one if that number follows the first token.", "To be immediately followed by the name of the variable, as part of the same token", commandName, "decrement.")) {
				MesquiteInteger theInt = findInteger(getSecondWord(commandName));
				String amountString = nextTokenCompressAsterisk(parser);
				MesquiteInteger amount = new MesquiteInteger();
				amount.setValue(amountString);
				if (theInt !=null) 
					if (amount.isCombinable()) {
						theInt.decrement(amount.getValue());
					}
					else
						theInt.decrement();
				if (debugging) 
					stamp("           decremented to: " + theInt);
			}
			else if (checker.compareStart(null, "Increments by one the named integer variable, or by a value other than one if that number follows the first token", "To be immediately followed by the name of the variable, as part of the same token", commandName, "increment.")) {
				MesquiteInteger theInt = findInteger(getSecondWord(commandName));

				String amountString = nextTokenCompressAsterisk(parser);
				MesquiteInteger amount = new MesquiteInteger();
				amount.setValue(amountString);
				if (theInt !=null) 
					if (amount.isCombinable()) {
						theInt.increment(amount.getValue());
					}
					else
						theInt.increment();
				if (debugging) 
					stamp("           incremented to: " + theInt);
			}
			else if (checker.compareStart(null, "Defines or recalls a variable containing an Object", "To be immediately followed by the name of the variable, as part of the same token  [A reference to an object]", commandName, "Object.")) {
				//Object theObj = findObject(getSecondWord(commandName));
				int whichObject = StringArray.indexOfIgnoreCase(objectNames, getSecondWord(commandName));
				if (whichObject<0) {
					objects[numObjects] = result;
					objectNames[numObjects] = getSecondWord(commandName);
					numObjects++;
				}
				else
					objects[whichObject] = result;
			}
			else if (checker.compareStart(null, "Defines or recalls a String variable", "To be immediately followed by the name of the variable, as part of the same token  [A string or a reference to a string]", commandName, "String.")) {
				if (fineDebugging) stamp("-sc- (" + level + ")25");
				int itemNum  = StringArray.indexOfIgnoreCase(stringNames, getSecondWord(commandName));
				String argument = nextTokenCompressAsterisk(parser);
				String value =null;
				boolean concat = false;
				if (argument.equalsIgnoreCase("*It")) {
					if (result != null) {
						if (result instanceof WithStringDetails)
							value = ((WithStringDetails)result).toStringWithDetails();
						else
							value = result.toString();
					}
				}
				else {
					if (argument.equals("+")){
						concat = true;
						argument = nextTokenCompressAsterisk(parser);
					}
					else if (argument.startsWith("+")) {
						concat = true;
						argument = argument.substring(argument.indexOf("+")+1, argument.length());
					}
					if (argument.startsWith("*")) {
						String parg = argument.substring(argument.indexOf("+")+1, argument.length());
						argument = argumentToString(parg, result);

					}

					if (fineDebugging) stamp("-sc- (" + level + ")26");
					if ((argument.equals(";") || StringUtil.blank(argument)) && result !=null && result instanceof String)
						value = (String)result;
					else {
						value = argument.substring(0, argument.length());
					}
				}

				if (itemNum<0) {
					itemNum = numStrings;
					strings[numStrings] = value;
					stringNames[numStrings] = getSecondWord(commandName);
					numStrings++;
				}
				else {
					if (concat && strings[itemNum]!=null)
						strings[itemNum] += value;
					else
						strings[itemNum] = value;
				}
				if (debugging)
					stamp("       String number " + itemNum + "defined or modified to \"" + strings[itemNum] + "\"");
				if (fineDebugging) stamp("-sc- (" + level + ")27");
			}
			else if (checker.compareStart(null, "Tokenizes a String variable", "To be immediately followed by the name of the variable, as part of the same token  [A string or a reference to a string]", commandName, "tokenize.")) {
				int itemNum  = StringArray.indexOfIgnoreCase(stringNames, getSecondWord(commandName));

				if (itemNum>=0 && itemNum< strings.length)  {
					if (strings[itemNum]!=null)
						strings[itemNum] = ParseUtil.tokenize(strings[itemNum] );
					if (debugging)
						stamp("       String number " + itemNum + "tokenized to \"" + strings[itemNum] + "\"");
					if (fineDebugging) stamp("-sc- (" + level + ")27at");
				}
			}
			else if (checker.compare(null, "Queries the user for a String", "[Message to be shown to user (as a quoted token)]", commandName, "queryString")) {
				if (fineDebugging) stamp("-sc- (" + level + ")30");
				String argument = nextTokenCompressAsterisk(parser);
				String r = MesquiteString.queryString(MesquiteTrunk.mesquiteTrunk.containerOfModule(), argument, argument, "");
				result = r;
				if (r==null || r.equals("")) {
					return cancel(stringPos, block);
				}
				if (fineDebugging) stamp("-sc- (" + level + ")31");
			}
			else if (checker.compare(null, "Queries the user for an integer value", "[Message to be shown to user (as a quoted token)]", commandName, "queryInteger")) {
				if (fineDebugging) stamp("-sc- (" + level + ")32");
				String argument = nextTokenCompressAsterisk(parser);
				int i = MesquiteInteger.queryInteger(MesquiteTrunk.mesquiteTrunk.containerOfModule(), argument, argument, 0);
				if (MesquiteInteger.isCombinable(i))
					result = new MesquiteInteger(i);
				else {
					return cancel(stringPos, block);
				}
				if (fineDebugging) stamp("-sc- (" + level + ")33");
			}
			else if (checker.compare(null, "Queries the user for a numerical value", "[Message to be shown to user (as a quoted token)]", commandName, "queryNumber")) {
				if (fineDebugging) stamp("-sc- (" + level + ")34");
				String argument = nextTokenCompressAsterisk(parser);
				MesquiteNumber m = new MesquiteNumber(0);
				MesquiteNumber r = MesquiteNumber.queryNumber(ownerModule.containerOfModule(), argument, argument, m);
				if (r.isCombinable())
					result = r;
				else {
					return cancel(stringPos, block);
				}
				if (fineDebugging) stamp("-sc- (" + level + ")35");
			}
			else if (checker.compare(null, "Queries the user with a list from which an item is to be chosen; returned is the integer position in the list", "[reference to ListableVector object] [Message to be shown to user (as a quoted token)]", commandName, "queryListNumber")) {
				String argument = nextTokenCompressAsterisk(parser);
				Object who = null;
				if (argument.equalsIgnoreCase("It"))
					who = result;
				else if (argument.equalsIgnoreCase("Mesquite"))
					who = MesquiteTrunk.mesquiteTrunk;
				else
					who = findObject(getSecondWord(argument));
				if (who!=null && who instanceof ListableVector){
					String queryMessage = nextTokenCompressAsterisk(parser);
					Listable r = ListDialog.queryList(ownerModule.containerOfModule(), "Select", queryMessage, MesquiteString.helpString, (ListableVector)who, 0);
					if (r!=null) {
						int i = ((ListableVector)who).indexOf(r);
						result = new MesquiteInteger(i);
					}
					else {
						return cancel(stringPos, block);
					}
				}
			}
			else if (checker.compare(null, "Queries the user with a list from which an item is to be chosen; returned is the object in the list", "[reference to ListableVector object] [Message to be shown to user (as a quoted token)]", commandName, "queryList")) {
				String argument = nextTokenCompressAsterisk(parser);
				Object who = null;
				if (argument.equalsIgnoreCase("It"))
					who = result;
				else if (argument.equalsIgnoreCase("Mesquite"))
					who = MesquiteTrunk.mesquiteTrunk;
				else
					who = findObject(getSecondWord(argument));
				if (who!=null && who instanceof ListableVector){
					String queryMessage = nextTokenCompressAsterisk(parser);
					result = ListDialog.queryList(ownerModule.containerOfModule(), "Select", queryMessage,MesquiteString.helpString,  (ListableVector)who, 0);
					if (result==null) {
						return cancel(stringPos, block);
					}
				}
			}
			else if (checker.compare(null, "Queries the user to choose a file; returned is the tokenized path to the file", "[Message to be shown to user (as a quoted token)]", commandName, "queryFile")) {
				String queryMessage = nextTokenCompressAsterisk(parser);
				MainThread.incrementSuppressWaitWindow();
				MesquiteFileDialog fdlg= new MesquiteFileDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), queryMessage, FileDialog.LOAD);
				fdlg.setBackground(ColorTheme.getInterfaceBackground());
				fdlg.setVisible(true);
				String tempFileName=fdlg.getFile();
				String tempDirectoryName=fdlg.getDirectory();
				// fdlg.dispose();
				MainThread.decrementSuppressWaitWindow();
				if (!StringUtil.blank(tempFileName) && !StringUtil.blank(tempDirectoryName)) {
					String r= tempDirectoryName + tempFileName;
					result = ParseUtil.tokenize(r);
					if (r==null || r.equals("")) {
						return cancel(stringPos, block);
					}

				}
			}
			else if (checker.compare(null, "Queries the user to choose a file name by which to save a file; returned is the tokenized path to the file", "[Message to be shown to user (as a quoted token)]", commandName, "queryFileSave")) {
				String queryMessage = nextTokenCompressAsterisk(parser);
				MainThread.incrementSuppressWaitWindow();
				MesquiteFileDialog fdlg= new MesquiteFileDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), queryMessage, FileDialog.SAVE);
				fdlg.setBackground(ColorTheme.getInterfaceBackground());
				fdlg.setVisible(true);
				String tempFileName=fdlg.getFile();
				String tempDirectoryName=fdlg.getDirectory();
				// fdlg.dispose();
				MainThread.decrementSuppressWaitWindow();
				if (!StringUtil.blank(tempFileName) && !StringUtil.blank(tempDirectoryName)) {
					String r= tempDirectoryName + tempFileName;
					result = ParseUtil.tokenize(r);
					if (r==null || r.equals("")) {
						return cancel(stringPos, block);
					}

				}
			}
			else if (checker.compare(null, "Queries the user about a yes/no choice; returned is 0 (no) or 1 (yes)", "[Message to be shown to user (as a quoted token)] [String for No] [String for Yes]", commandName, "queryYesNo")) {
				String queryMessage = nextTokenCompressAsterisk(parser);
				String no = nextTokenCompressAsterisk(parser);
				String yes = nextTokenCompressAsterisk(parser);

				boolean yesChosen = AlertDialog.query(ownerModule.containerOfModule(), "Query", queryMessage,yes, no);
				MesquiteInteger mi = new MesquiteInteger();
				if (yesChosen)
					mi.setValue(1);
				else
					mi.setValue(0);
				result = mi;

			}
			/* other ideas: 
	queryGetFile;  // querys user and puts result into It
	queryPutFile;  // querys user and puts result into It
	queryDirectory;  // querys user and puts result into It
	queryObject <ListableVector>;  // querys user and puts result into It
			 */
			else if (checker.compare(null, "Appends string to a file", "[filename] [an object or variable that can be converted to a string]", commandName, "appendMessageToFile")) {  
				String argument = nextTokenCompressAsterisk(parser); //fileName
				String fileName = null;
				if (argument!=null && argument.startsWith("*"))
					fileName = findString(getSecondWord(argument));
				else
					fileName = argument;
				String whatToWrite = argumentToString(nextTokenCompressAsterisk(parser), result); //whatToWrite
				
				MesquiteFile.appendFileContents(MesquiteFile.composePath(writingDirectory(), fileName), whatToWrite, false);
			}
			else if (checker.compare(null, "Appends literal string to a file", "[filename] [a string or a reference to a string]", commandName, "appendLiteralToFile")) {  
				String argument = nextTokenCompressAsterisk(parser); //fileName
				String fileName = null;
				if (argument!=null && argument.startsWith("*"))
					fileName = findString(getSecondWord(argument));
				else
					fileName = argument;
				String whatToWrite =nextTokenCompressAsterisk(parser); //whatToWrite
				MesquiteFile.appendFileContents(MesquiteFile.composePath(writingDirectory(),fileName), whatToWrite, false);
			}
			else if (checker.compare(null, "Saves string to a file", "[filename] [an object or variable that can be converted to a string]", commandName, "saveMessageToFile")) {  
				String argument = nextTokenCompressAsterisk(parser); //fileName
				String fileName = null;
				if (argument!=null && argument.startsWith("*"))
					fileName = findString(getSecondWord(argument));
				else
					fileName = argument;
				String whatToWrite = argumentToString(nextTokenCompressAsterisk(parser), result); //whatToWrite
				MesquiteFile.putFileContents(MesquiteFile.composePath(writingDirectory(),fileName), whatToWrite, false);
			}
			else if (checker.compare(null, "Appends an end line character/characters to a file", "[filename]", commandName, "appendReturnToFile")) {  
				String argument = nextTokenCompressAsterisk(parser); //fileName
				String fileName = null;
				if (argument!=null && argument.startsWith("*"))
					fileName = findString(getSecondWord(argument));
				else
					fileName = argument;
				MesquiteFile.appendFileContents(MesquiteFile.composePath(writingDirectory(), fileName), ""+ StringUtil.lineEnding(), false);
			}
			else if (checker.compare(null, "Appends an tab to a file", "[filename]", commandName, "appendTabToFile")) {  
				String argument = nextTokenCompressAsterisk(parser); //fileName
				String fileName = null;
				if (argument!=null && argument.startsWith("*"))
					fileName = findString(getSecondWord(argument));
				else
					fileName = argument;
				MesquiteFile.appendFileContents(MesquiteFile.composePath(writingDirectory(), fileName), "\t", false);
			}
			else if (checker.compare(null, "Requests that file (if any) containing script being executed be closed after reading", null, commandName, "closeFileAfterRead")) {  
				if (nb !=null && nb.getFile() != null)
					nb.getFile().setCloseAfterReading(true);
			}
			else if (checker.compare(null, "Sets the arguments for the following command", "[args]", commandName, "rememberArguments")) {  
				String argument = nextTokenCompressAsterisk(parser); 
				rememberedArguments = null;
				if (argument!=null && argument.startsWith("*"))
					rememberedArguments = findString(getSecondWord(argument));
				else
					rememberedArguments = argument;
			}
			else if (checker.compare(null, "Makes a new directory", "[path]", commandName, "mkdir")) {  
				String argument = nextTokenCompressAsterisk(parser); //fileName
				String path = null;
				if (argument!=null && argument.startsWith("*"))
					path = findString(getSecondWord(argument));
				else
					path = argument;
				File f = new File(MesquiteFile.composePath(writingDirectory(), path));
				f.mkdir();
			}
			else if (checker.compare(null, "For subsequent commands at this level in the script, execute on a new Thread", null, commandName, "newThread")) {
				if (fineDebugging) stamp("-sc- (" + level + ")4");
				if (debugging) {
					stamp("   STARTING NEW THREAD  ");

				}
				boolean scripting = MesquiteThread.isScripting();
				if (fineDebugging) stamp("-sc- (" + level + ")5");
				
				sendCommandsOnNewThread(mb, block, stringPos, endString, skip, nb, scripting);
				if (fineDebugging) stamp("-sc- (" + level + ")6");
				return null;
			}
			else if (commandName.equalsIgnoreCase("TITLE") && mb instanceof FileCoordinator) {
				if (fineDebugging) stamp("-sc- (" + level + ")12");
				if (nb !=null)
					nb.setName(nextTokenCompressAsterisk(parser));
				if (fineDebugging) stamp("-sc- (" + level + ")13");
			}

			else if (checker.compare(null, "Writes the current 'It' to the System console", null, commandName, "writeIt")) {  //writes current "It" to console
				if (result!=null)
					stamp(" [ It: " + result.toString() + "]");
				else
					stamp("[ It is null ]");
			}
			else if (checker.compare(null, "Displays a message in the system console", "The message to be displayed (a single token; use quotes ' if needed)", commandName, "message")) {
				if (fineDebugging) stamp("-sc- (" + level + ")20");
				String argument = nextTokenCompressAsterisk(parser);
				stamp(argumentToString(argument, result));
			}
			else if (checker.compare(null, "Displays a message in the system console", "The message to be displayed (a single token; use quotes ' if needed)", commandName, "messageLiteral")) {
				if (fineDebugging) stamp("-sc- (" + level + ")20");
				String argument = nextTokenCompressAsterisk(parser);
				stamp(argument);
			}
			else if (checker.compare(null, "Displays a message in an alert dialog for the user", "The message to be displayed (a single token; use quotes ' if needed)", commandName, "alert")) {
				if (fineDebugging) stamp("-sc- (" + level + ")21");
				String argument = nextTokenCompressAsterisk(parser);
				ownerModule.alert(argumentToString(argument, result));
			}
			else if (checker.compare(null, "Displays a message in the system console indicating the current object being commanded", null, commandName, "showObjectCommanded")) {
				if (fineDebugging) stamp("-sc- (" + level + ")22");
				stamp("     object commanded: " + mb);
				result = mb;
			}
			else if (checker.compare(null, "Used at the start of macros, to indicate the Class to which the macro is designed to direct its commands", "[short (packageless) name of Class]", commandName, "telling")) {
				if (fineDebugging) stamp("-sc- (" + level + ")23");
				String argument = nextTokenCompressAsterisk(parser);
				if (mb == null) {
					System.out.println("commands for object of class " + argument + " given to null object");
					skip = true;
				}
				else {

					if (!MesquiteModule.nameIsInstanceOf(argument, mb.getClass())) { //TODO: should allow subclasses also
						System.out.println("commands for object of class " + argument + " given to object of inappropriate class " + MesquiteModule.getShortClassName(mb.getClass()));
						skip = true;
					}
				}
				if (fineDebugging) stamp("-sc- (" + level + ")24");
			}
			else if (checker.compare(null, "Toggles debug mode -- in debug mode, a record of commands and results returned is written to the System console", null, commandName, "debug")) {
				if (fineDebugging) stamp("-sc- (" + level + ")28");
				debugging = !debugging;
			}
			else if (checker.compare(null, "Toggles logonlydebug mode -- in logonlydebug mode, a record of commands and results returned is written to the log file", null, commandName, "logonlydebug")) {
				if (fineDebugging) stamp("-sc- (" + level + ")28");
				debugging = !debugging;
				logOnly = !logOnly;
			}
			else if (checker.compare(null, "Returns System.currentTimeMillis as a string", null, commandName, "millis")) {
				result = Long.toString(System.currentTimeMillis());
			}
			else if (checker.compare(null, "Toggles timing mode -- in timing mode, the time required to complete each command is written to the System console", null, commandName, "time")) {
				showTime = !showTime;
				if (showTime) {
					timer = new MesquiteTimer();
					timer.start();
				}
			}
			else if (checker.compare(null, "Stamps the time to the log", null, commandName, "stampVersion")) {
				MesquiteTrunk.mesquiteTrunk.logln("Mesquite version " + MesquiteModule.getMesquiteVersion() + MesquiteModule.getBuildVersion());
			}
			else if (checker.compare(null, "Stamps the time to the log", null, commandName, "stampTime")) {
				long t = System.currentTimeMillis();
				Date dnow = new Date(t);
				MesquiteTrunk.mesquiteTrunk.logln( dnow.toString());
			}
			else if (checker.compare(null, "Toggles whether the messages (for instance, if the debug toggle is on) are echoed into log" , null, commandName, "messagesToLog")) {
				if (fineDebugging) stamp("-sc- (" + level + ")28");
				stampToLog = !stampToLog;
			}
			/*else if (checker.compare(null, "Toggles whether the hiring path of modules is written to the System console (for debugging)", null, commandName, "toggleShowHiringPath")) {
				if (fineDebugging) stamp("-sc- (" + level + ")28a");
				MesquiteModule.showHiringPath = !MesquiteModule.showHiringPath;
			}*/
			else if (checker.compare(null, "Sleeps", "[integer of how long this thread is to sleep]", commandName, "sleep")) {
				String argument = nextTokenCompressAsterisk(parser);
				int howLong = MesquiteInteger.fromString(argument);
				try {Thread.sleep(howLong);}
				catch(InterruptedException e){}
			}
			else if (checker.compare(null, "call the Runtime garbage collector", null, commandName, "gc")) {
				if (fineDebugging) stamp("-sc- (" + level + ")29");
				Runtime rtm = Runtime.getRuntime();
				rtm.gc();
			}
			else if (checker.compare(null, "may be defunct", null, commandName, "paging")) {
				if (mb instanceof MesquiteModule){
					String argument = nextTokenCompressAsterisk(parser);
					pagingPos.setValue(stringPos.getValue());
					pagingBlock = block;
					pagingSkip = skip;
					pagingNB = nb;
					pagedModule = (MesquiteModule)mb;
					//	((MesquiteModule)mb).removePaging(this);
					sendCommands(null, block, stringPos, "endPaging;", true, nb, checker);
					((MesquiteModule)mb).pageModule(ownerModule, "persistent".equalsIgnoreCase(argument)); //persistent if says so
				}
				else
					sendCommands(null, block, stringPos, "endPaging;", true, nb, checker);
			} 
			else if (checker.compare(null, "may be defunct", null, commandName, "removePaging")) {
				if (mb instanceof MesquiteModule)
					((MesquiteModule)mb).removePaging(ownerModule);
			} 
			else if (!skip && !checker.getAccumulateMode()) {
				if (fineDebugging) stamp("-sc- (" + level + ")100");
				String commandArguments;

				if (rememberedArguments !=null)
					commandArguments = rememberedArguments;
				else if (StringUtil.blank(s) || parser.getPosition()>= s.length()) 
					commandArguments = null;
				else {
					commandArguments = s.substring(parser.getPosition(), s.length()-1);
					commandArguments = argumentToString(commandArguments, result);
				}
				previousArguments = commandArguments;
				if (fineDebugging) stamp("-sc- (" + level + ")101");
				if (mb !=null && mb instanceof Commandable) {
					if (fineDebugging) stamp("-sc- (" + level + ")102");
					if (mb instanceof Logger) {
						String logString;
						if (mb instanceof Listable)
							logString = ((Listable)mb).getName() + ">" + s;
						else
							logString = "[" + mb.getClass().getName() + "]"  + ">" + s;
						//((Logger)mb).logln(logString);
					}
					//else
					//	MesquiteModule.mesquiteTrunk.logln(">" + s);
					if (fineDebugging) stamp("-sc- (" + level + ")103");

					try{
						result = ((Commandable)mb).doCommand(commandName, commandArguments,checker);  //cm = mb.doCommand(commandName, commandArguments);
						//CommandRecord.tick("Command complete");
						CommandRecord.tick(commandName + " complete");
					}
					catch (Exception e){ 
						result = null;
						CommandRecord comRec = MesquiteThread.getCurrentCommandRecord();
						if (comRec != null)
							comRec.setErrorFound();
						MesquiteFile.throwableToLog(this, e);
						String message = "Command being executed when Exception was thrown: " + commandName + "[arguments: " + commandArguments + "], commanding ";
						if (mb instanceof Listable)
							message += " the object \"" + ((Listable)mb).getName() + "\" of class " + mb.getClass();
						else
							message += " an object of class " + mb.getClass();
						MesquiteProject proj = getProject(mb, nb, result);

						if (proj !=null && proj.isDoomed){
							MesquiteTrunk.mesquiteTrunk.logln("Execution thrown in script " + message);
						}
						else {
							MesquiteTrunk.mesquiteTrunk.exceptionAlert(e,  "There has been a problem executing a script; a Java Exception was thrown.  " + message + ". This may be the result of an old, incompatible module or script being used.  (ERROR: " + e.getMessage() + ")");
							MesquiteMessage.printStackTrace(e);
						}
						MesquiteTrunk.zeroMenuResetSuppression(); //EXCEPTION HANDLER
						MesquiteTrunk.resetAllMenuBars();

					}
					catch (Error e){
						if (e instanceof OutOfMemoryError)
							MesquiteMessage.println("OutofMemoryError.  See file startingMesquiteAndMemoryAllocation.txt in the Mesquite_Folder for information on how to increase memory allocated to Mesquite.");
						result = null;
						MesquiteFile.throwableToLog(this, e);
						CommandRecord comRec = MesquiteThread.getCurrentCommandRecord();
						if (comRec != null)
							comRec.setErrorFound();
						MesquiteDialog.closeWizard();
						String message = "Command being executed when Error was thrown: " + commandName + "[arguments: " + commandArguments + "], commanding ";
						if (mb instanceof Listable)
							message += " the object \"" + ((Listable)mb).getName() + "\" of class " + mb.getClass();
						else
							message += " an object of class " + mb.getClass();
						MesquiteProject proj = getProject(mb, nb, result);
						if (proj !=null && proj.isDoomed){
							MesquiteTrunk.mesquiteTrunk.logln("Execution thrown in script " + message);
						}
						else {
							if (!(e instanceof OutOfMemoryError))
								MesquiteTrunk.mesquiteTrunk.exceptionAlert(e,  "There has been a problem executing a script; a Java Error was thrown.  " + message + ". This may be the result of an old, incompatible module or script being used.  (ERROR: " + e.getMessage() + ")");
							MesquiteMessage.printStackTrace(e);
						}
						MesquiteTrunk.zeroMenuResetSuppression(); //EXCEPTION HANDLER
						MesquiteTrunk.resetAllMenuBars();

					}
					rememberedArguments= null;

					if (fineDebugging) stamp("-sc- (" + level + ")104");
					if (debugging) {
						if (result == null)
							stamp(s + "  ---{null was returned}");
						else if (result instanceof MesquiteInteger)
							stamp(s + "  ---{returned: " + ((MesquiteInteger)result).toString() + "}");
						else if (result instanceof Listable)
							stamp(s + "  ---{returned: " + ((Listable)result).getName() + "}");
						else
							stamp(s + "  ---{returned  " + result + "}");
					}
					if (fineDebugging) stamp("-sc- (" + level + ")105");
				}
			}
			if (fineDebugging) stamp("-sc- (" + level + ")106");
			if (showTime)
				stampTimeSinceLast();
			if (fineDebugging) stamp("-sc- (" + level + ")107");
			previousCommand = s;
			previousCommandName = commandName;
		}
		return result;
	}
	public void finalize () throws Throwable{
		for (int i=0; i<numbers.length; i++)
			numbers[i]=null;
		for (int i=0; i<integers.length; i++)
			integers[i]=null;
		for (int i=0; i<objects.length; i++)
			objects[i]=null;
		for (int i=0; i<strings.length; i++)
			strings[i]=null;
		//	pagingNB = null;
		//	pagedModule = null;
		super.finalize();
	}
}

