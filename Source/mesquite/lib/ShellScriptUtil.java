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

import java.util.*;
import java.io.*;


/* TODO: 
 * make a MesquiteExternalProcess that extends Process and stores things like 
 * 		OutputStream inputToProcess = proc.getOutputStream();
		OutputStreamWriter inputStreamsWriter = new OutputStreamWriter(inputToProcess);
	- all of the isWindows, StringUtil.lineEnding() needs to use line endings etc. from the computer running the process, not the local computer.

 * */

/* ======================================================================== */
public class ShellScriptUtil  {
	static int sleepTime = 50;
	public static int recoveryDelay = 0;
	

	/*.................................................................................................................*/
	public static String protectForShellScript(String s) {  //Is this only used for paths???!!!!!  See StringUtil.protectForWindows.
		if (MesquiteTrunk.isWindows())
			return StringUtil.protectFilePathForWindows(s);
		else
			return StringUtil.protectFilePathForUnix(s);
	}
	/*.................................................................................................................*/
	public static String getWriteStringAsFile(String path, String contents) {
		if (MesquiteTrunk.isWindows())
			return "echo \"" + contents + "\" > " + StringUtil.protectFilePathForWindows(path) + StringUtil.lineEnding();
		else
			return "echo \"" + contents + "\" > " + StringUtil.protectFilePathForUnix(path) + StringUtil.lineEnding();
	}

	/*.................................................................................................................*/
	public static String getAppendStringAsFile(String path, String contents) {
		if (MesquiteTrunk.isWindows())
			return "echo \"" + contents + "\" >> " + StringUtil.protectFilePathForWindows(path) + StringUtil.lineEnding();
		else
			return "echo \"" + contents + "\" >> " + StringUtil.protectFilePathForUnix(path) + StringUtil.lineEnding();
	}

	/*.................................................................................................................*
    	String getAliasCommand(String alias, String expansion){
    		return "alias " +StringUtil.protectForUnix(alias) + " " + StringUtil.protectForUnix(expansion) +StringUtil.lineEnding();
    	}
    	/*.................................................................................................................*/
	public static String getLinkCommand(String path, String aliasPath){
		if (MesquiteTrunk.isWindows())
			return "shortcut -f -t " +StringUtil.protectFilePathForWindows(path) + " -n " + StringUtil.protectFilePathForUnix(aliasPath) +StringUtil.lineEnding();
		else
			return "ln  " +StringUtil.protectFilePathForUnix(path) + " " + StringUtil.protectFilePathForUnix(aliasPath) +StringUtil.lineEnding();
	}
	/*.................................................................................................................*/
	public static String getChangeDirectoryCommand(String directory){
		String directoryString;
		if (MesquiteTrunk.isWindows()) {
			directoryString = "/d "+StringUtil.protectFilePathForWindows(directory);
		} else {
			directoryString = StringUtil.protectFilePathForUnix(directory);
		}
		return "cd " + directoryString +StringUtil.lineEnding();
	}
	/*.................................................................................................................*/
	public static String getRemoveCommand(String filePath){
		if (MesquiteTrunk.isWindows())
			return "del " + StringUtil.protectFilePathForWindows(filePath) +StringUtil.lineEnding();
		else
			return "rm -f " + StringUtil.protectFilePathForUnix(filePath) +StringUtil.lineEnding();
	}
	
	/** This returns whether or not an exit command for shell scripts is available
	 * in the OS.  This is used to close terminal windows by user choice.  However, because visible terminals are
	 * only used in Windows and Mac, and because the Mac's Terminal can't easily be closed,
	 * this only returns true for Windows. 
	 */
	public static boolean exitCommandIsAvailableAndUseful(){
		if (MesquiteTrunk.isWindows())
			return true;
		return false;
	}
	
	/** This returns the exit command that might be used to quit a visible terminal window from within itself. 
	 */
	public static String getExitCommand(){
		if (MesquiteTrunk.isMacOSX()){
			return "osascript -e 'quit app \"Terminal\"'";  // doesn't fully work as will prompt user
		}
		else
			return "exit ";
	}
	/*.................................................................................................................*/
	public static String getSetFileTypeCommand(String filePath){
		if (MesquiteTrunk.isMacOSX())
			return "/Developer/Tools/setFile -t TEXT " + StringUtil.protectFilePathForUnix(filePath) +StringUtil.lineEnding();
		else
			return "";
	}
	/*.................................................................................................................*/
	public static String getOpenDirectoryCommand(String directoryPath){
		if (MesquiteTrunk.isMacOSX())
			return "open " + StringUtil.protectFilePathForUnix(directoryPath) +StringUtil.lineEnding();
		else
			return "";
	}
	/*.................................................................................................................*/
	public static void sendToProcessInput (Process proc, String input) {
		
		OutputStream inputToProcess = proc.getOutputStream();
		OutputStreamWriter inputStreamsWriter = new OutputStreamWriter(inputToProcess);
		BufferedWriter inputBufferedWriter = new BufferedWriter(inputStreamsWriter);
		try {
			try {
				inputBufferedWriter.write(input);
			} finally {
				inputBufferedWriter.flush();
			//	inputBufferedWriter.close();
			} 
		} catch (Exception e) {
		}
	}
	/*.................................................................................................................*
	public static MesquiteExternalProcess startExternalProcess(String executablePath){ 	
		Process proc=null;
		try {
			if (MesquiteTrunk.isMacOSX()|| MesquiteTrunk.isLinux()) {
				// remove double slashes or things won't execute properly
				executablePath = executablePath.replaceAll("//", "/");
				proc = Runtime.getRuntime().exec(executablePath);
			} else {
			}
		}  catch (IOException e) {
			MesquiteMessage.println("Script execution failed.");
			return null;
		}
		if (proc != null) {
			return new MesquiteExternalProcess(proc);
		}
		return null;
	}


	/*.................................................................................................................*/
	@Deprecated
	public  static Process executeScript(String scriptPath){ 
		return executeScript(scriptPath, true);
	}
	/*.................................................................................................................*/
	public static Process executeScript(String scriptPath, boolean visibleTerminal){ 
		Process proc;
		try {
			String[] pathArray = null;
			if (MesquiteTrunk.isMacOSX()){
				if (visibleTerminal) {
					pathArray = new String[] {"open",  "-a","/Applications/Utilities/Terminal.app",  scriptPath};
				}
				else {
					scriptPath = scriptPath.replaceAll("//", "/");
					pathArray = new String[] {scriptPath};
				}
				proc = Runtime.getRuntime().exec(pathArray);
			}
			else if (MesquiteTrunk.isLinux()) {
				// remove double slashes or things won't execute properly
				scriptPath = scriptPath.replaceAll("//", "/");
				pathArray = new String[] {scriptPath};
				proc = Runtime.getRuntime().exec(pathArray);
			} else {  // Windows
				scriptPath = "\"" + scriptPath + "\"";
				if (visibleTerminal)
					proc = Runtime.getRuntime().exec("cmd /c start \"\" " + scriptPath);
				else {
					pathArray = new String[] {"cmd", "/c", scriptPath};
					proc = Runtime.getRuntime().exec(pathArray);
				}
			}
		}  catch (IOException e) {
			MesquiteMessage.println("Script execution failed. " + e.getMessage());
			return null;
		}
		if (proc != null) { /*
			StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
			StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");
			errorGobbler.start();
			outputGobbler.start();
		 */
		}
		return proc;
	}
	/*.................................................................................................................*/
	public static boolean setScriptFileToBeExecutable(String scriptPath) throws IOException {
		Process proc;
		try {
			//Original implementation (permission change was not complete before script execution attempted)
			//if (!MesquiteTrunk.isWindows())
			//	Runtime.getRuntime().exec(new String[] {"chmod", "+x", scriptPath } );
			if(!MesquiteTrunk.isWindows()){
				proc = Runtime.getRuntime().exec(new String[] {"chmod", "+x", scriptPath } );
				try{// waitFor() so thread waits for permission change to complete before trying to run the script
					proc.waitFor();
				} catch (InterruptedException e){
					MesquiteMessage.println("Thread interrupted while waiting for change in ownership.");
					return false;
				}
			}
		}
		catch (IOException e) {
			MesquiteMessage.println("Script cannot be set to be executable.");
			return false;
		}
		return true;
	}
	/*.................................................................................................................*/
	public static String getDefaultRunningFilePath(){
		Random rng = new Random(System.currentTimeMillis());
		String runningFilePath = MesquiteModule.getTempDirectoryPath()  + MesquiteFile.fileSeparator + "running" + MesquiteFile.massageStringToFilePathSafe(MesquiteTrunk.getUniqueIDBase() + Math.abs(rng.nextInt()));
		return runningFilePath;
	}
	/*.................................................................................................................*/
	public static String getOutputFileCopyPath(String path) {
		if (path==null)
			return null;
		else
			return path + "2";
	}
	/*.................................................................................................................*/
	public static void processOutputFiles(OutputFileProcessor outputFileProcessor, String[]outputFilePaths, long[] lastModified){
		if (outputFileProcessor!=null && outputFilePaths!=null && lastModified !=null) {
			String[] paths = outputFileProcessor.modifyOutputPaths(outputFilePaths);
			for (int i=0; i<paths.length && i<lastModified.length; i++) {
				File file = new File(paths[i]);
				long lastMod = file.lastModified();
				if (!MesquiteLong.isCombinable(lastModified[i])|| lastMod>lastModified[i]){
					outputFileProcessor.processOutputFile(paths, i);
					lastModified[i] = lastMod;
				}
			}
		}
	}

	/*.................................................................................................................*/
	/** executes a shell script at "scriptPath".  If runningFilePath is not blank and not null, then Mesquite will create a file there that will
	 * serve as a flag to Mesquite that the script is running.   */
	public static boolean executeAndWaitForShell(String scriptPath, String runningFilePath, String runningFileMessage, boolean appendRemoveCommand, String name, String[] outputFilePaths, OutputFileProcessor outputFileProcessor, ShellScriptWatcher watcher, boolean visibleTerminal){
		Process proc = null;
		long[] lastModified=null;
		boolean stillGoing = true;
		if (outputFilePaths!=null) {
			lastModified = new long[outputFilePaths.length];
			LongArray.deassignArray(lastModified);
		}
		try{
			ShellScriptUtil.setScriptFileToBeExecutable(scriptPath);
			if (!StringUtil.blank(runningFilePath)) {
				if (StringUtil.blank(runningFileMessage))
					MesquiteFile.putFileContents(runningFilePath, "Script running...", true);
				else
					MesquiteFile.putFileContents(runningFilePath, runningFileMessage, true);
				if (appendRemoveCommand && MesquiteFile.fileExists(runningFilePath))
					MesquiteFile.appendFileContents(scriptPath, StringUtil.lineEnding() + ShellScriptUtil.getRemoveCommand(runningFilePath), true);  //append remove command to guarantee that the runningFile is deleted
				//+StringUtil.lineEnding()+ShellScriptUtil.getExitCommand()
			}
			proc = ShellScriptUtil.executeScript(scriptPath, visibleTerminal);

			if (proc==null) {
				MesquiteMessage.notifyProgrammer("Process is null in shell script executed by " + name);
				return false;
			}
			else if (!StringUtil.blank(runningFilePath))   // is file at runningFilePath; watch for its disappearance
				while (MesquiteFile.fileExists(runningFilePath) && stillGoing){
					processOutputFiles (outputFileProcessor, outputFilePaths, lastModified);
					try {
						Thread.sleep(sleepTime);
					}
					catch (InterruptedException e){
						MesquiteMessage.notifyProgrammer("InterruptedException in shell script executed by " + name);
						return false;
					}
					stillGoing = watcher == null || watcher.continueShellProcess(proc);
				}
		}
		catch (IOException e){
			MesquiteMessage.warnProgrammer("IOException in shell script executed by " + name);
			return false;
		}
		
		try {  
			Thread.sleep(recoveryDelay * 1000);
		}
		catch (InterruptedException e){
		}
		
		if (outputFileProcessor!=null)
			outputFileProcessor.processCompletedOutputFiles(outputFilePaths);
		return true;
	}

	/*.................................................................................................................*/
	/** executes a shell script at "scriptPath".  If runningFilePath is not blank and not null, then Mesquite will create a file there that will
	 * serve as a flag to Mesquite that the script is running.   */
	public static boolean executeAndWaitForShell(String scriptPath, String runningFilePath, String runningFileMessage, boolean appendRemoveCommand, String name){
		return executeAndWaitForShell( scriptPath,  runningFilePath,  runningFileMessage,  appendRemoveCommand,  name, null, null, null, true);
	}
	/*.................................................................................................................*/
	public static boolean executeAndWaitForShell(String scriptPath, String name){
		String runningFilePath = null;
		if (!StringUtil.blank(scriptPath))
			runningFilePath=getDefaultRunningFilePath();
		return executeAndWaitForShell(scriptPath, runningFilePath, null, true, name);
	}


	/*.................................................................................................................*/
	public static boolean executeLogAndWaitForShell(String scriptPath, String name, String[] outputFilePaths, OutputFileProcessor outputFileProcessor, ShellScriptWatcher watcher){
		String runningFilePath = null;
		if (!StringUtil.blank(scriptPath))
			runningFilePath=getDefaultRunningFilePath();
		return executeAndWaitForShell(scriptPath, runningFilePath, null, true, name, outputFilePaths, outputFileProcessor, watcher, true);
	}



}

