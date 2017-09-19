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

import mesquite.lib.duties.*;

/* TODO: 
 * make a MesquiteExternalProcess that extends Process and stores things like 
 * 		OutputStream inputToProcess = proc.getOutputStream();
		OutputStreamWriter inputStreamsWriter = new OutputStreamWriter(inputToProcess);

 * */

/* ======================================================================== */
public class ShellScriptRunner implements Commandable  {
	static int sleepTime = 50;
	Process proc;
	String scriptPath;
	String runningFileMessage;
	boolean appendRemoveCommand;
	String name;
	String runningFilePath; //reconnect
	String[] outputFilePaths; //reconnect
	String stdOutFilePath, stdErrFilePath;
	public static String stOutFileName = "StandardOutputFile";
	public static String stErrorFileName = "StandardErrorFile";
	OutputFileProcessor outputFileProcessor; //reconnect
	ShellScriptWatcher watcher; //reconnect
	boolean visibleTerminal;
	long[] lastModified;
	MesquiteExternalProcess externalProcessManager;
	long stdOutLastModified = 0;
	long stdErrLastModified = 0;
	boolean aborted = false;

	
	public ShellScriptRunner(String scriptPath, String runningFilePath, String runningFileMessage, boolean appendRemoveCommand, String name, String[] outputFilePaths, OutputFileProcessor outputFileProcessor, ShellScriptWatcher watcher, boolean visibleTerminal){
		this.scriptPath=scriptPath;
		this.runningFilePath=runningFilePath;
		if (runningFilePath == null && !StringUtil.blank(scriptPath))
			this.runningFilePath=ShellScriptUtil.getDefaultRunningFilePath();

		this.runningFileMessage=runningFileMessage;
		this.appendRemoveCommand =appendRemoveCommand;
		this.name = name;
		this.outputFilePaths = outputFilePaths;
		this.outputFileProcessor = outputFileProcessor;
		setOutErrFilePaths();
		this.watcher = watcher;
		this.visibleTerminal = visibleTerminal;
		
	}
	public ShellScriptRunner(){  //to be used for reconnecting; note that stdOutFilePath via setRunningFilePath command stored in snapshot
	}
	public void setOutputProcessor(OutputFileProcessor outputFileProcessor){
		this.outputFileProcessor = outputFileProcessor;
	}
	public void setWatcher(ShellScriptWatcher watcher){
		this.watcher = watcher;
	}
	public void setOutErrFilePaths(){
		stdOutFilePath = MesquiteFile.getDirectoryPathFromFilePath(runningFilePath) + MesquiteFile.fileSeparator + stOutFileName;
		stdErrFilePath = MesquiteFile.getDirectoryPathFromFilePath(runningFilePath) + MesquiteFile.fileSeparator + stErrorFileName;
	}
	public static boolean localScriptRunsCanDisplayTerminalWindow(){
		return !MesquiteTrunk.isLinux();
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setRunningFilePath " + ParseUtil.tokenize(runningFilePath));
		if (outputFilePaths != null){
			String files = " ";
			for (int i = 0; i< outputFilePaths.length; i++){
				files += " " + ParseUtil.tokenize(outputFilePaths[i]);
			}
			temp.addLine("setOutputFilePaths " + files);
		}
		return temp;
	}
	Parser parser = new Parser();
	boolean reconnectToExternal = false;

	/*.................................................................................................................*/
	public void pleaseReconnectToExternalProcess() {
		reconnectToExternal=true;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the running file path", "[file path]", commandName, "setRunningFilePath")) {
			runningFilePath = parser.getFirstToken(arguments);
			setOutErrFilePaths();
			if (reconnectToExternal) {
				reconnectToExternalProcess();
				reconnectToExternal = false;
			}
		}
		else if (checker.compare(this.getClass(), "Sets the output file paths", "[file paths]", commandName, "setOutputFilePaths")) {
			int num = parser.getNumberOfTokens(arguments);
			outputFilePaths = new String[num];
			if (num >0)
				outputFilePaths[0] = parser.getFirstToken();
			for (int i=1; i<num; i++)
				outputFilePaths[i] = parser.getNextToken();
		}
		return null;
	}	
	OutputTextListener textListener;
	
	public  void setOutputTextListener(OutputTextListener textListener){
		this.textListener= textListener;
	}

	public boolean isVisibleTerminal() {
		return visibleTerminal;
	}
	public void setVisibleTerminal(boolean visibleTerminal) {
		this.visibleTerminal = visibleTerminal;
	}
	/*.................................................................................................................*/
	public String getStdErr() {
		if (externalProcessManager!=null)
			return externalProcessManager.getStdErrContents();
		return "";
	}
	/*.................................................................................................................*/
	public String getStdOut() {
		if (externalProcessManager!=null)
			return externalProcessManager.getStdOutContents();
		return "";
	}

	/*.................................................................................................................*/
	public void resetLastModified(int i){
		if (i>=0 && i<lastModified.length)
			lastModified[i]=0;
	}
	/*.................................................................................................................*/
	public long getStdErrLastModified() {
		File file = new File(stdErrFilePath);
		if (file!=null)
			return 0;
		return file.lastModified();
	}
	/*.................................................................................................................*/
	public long getStdOutLastModified() {
		File file = new File(stdOutFilePath);
		if (file!=null)
			return 0;
		return file.lastModified();
	}

	/*.................................................................................................................*/
	public void processOutputFiles(){
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
	public void reconnectToExternalProcess() {
		externalProcessManager = new MesquiteExternalProcess();
		setOutErrFilePaths();
		File outputFile = new File(stdOutFilePath);  // note this and stErrorFilePath are always within the scriptPath directory
		File errorFile = new File(stdErrFilePath);
		externalProcessManager.startFileTailers(outputFile, errorFile);   

	}
	/*.................................................................................................................*/
	/** Executes a shell script at "scriptPath".  If runningFilePath is not blank and not null, then Mesquite will create a file there that will
	 * serve as a flag to Mesquite that the script is running.   */
	public boolean executeInShell(){
		proc = null;
		try{
			ShellScriptUtil.setScriptFileToBeExecutable(scriptPath);
			if (!StringUtil.blank(runningFilePath)) {
				if (StringUtil.blank(runningFileMessage))
					MesquiteFile.putFileContents(runningFilePath, "Script running...", true);
				else
					MesquiteFile.putFileContents(runningFilePath, runningFileMessage, true);
				if (appendRemoveCommand && MesquiteFile.fileExists(runningFilePath))
					MesquiteFile.appendFileContents(scriptPath, StringUtil.lineEnding() + ShellScriptUtil.getRemoveCommand(runningFilePath), true);  //append remove command to guarantee that the runningFile is deleted
			}
			proc = ShellScriptUtil.executeScript(scriptPath, visibleTerminal);  
			externalProcessManager = new MesquiteExternalProcess(proc);
			File outputFile = new File(stdOutFilePath);  // note this and stErrorFilePath are always within the scriptPath directory
			File errorFile = new File(stdErrFilePath);
			externalProcessManager.startFileTailers(outputFile, errorFile);   
				
		}
		catch (IOException e){
			MesquiteMessage.warnProgrammer("IOException in shell script executed by " + name);
			return false;
		}
		return true;
	}
	/*.................................................................................................................*/
	public void stopExecution(){
		if (externalProcessManager!=null)
			externalProcessManager.kill();
		aborted = true;

	}
	/*.................................................................................................................*/
	public boolean stdOutModified(){
		long lastModified = stdOutLastModified;
		stdOutLastModified= getStdOutLastModified();
		return stdOutLastModified!=lastModified;
	}
	/*.................................................................................................................*/
	public boolean stdErrModified(){
		long lastModified = stdErrLastModified;
		stdErrLastModified= getStdErrLastModified();
		return stdErrLastModified!=lastModified;
	}


	/*.................................................................................................................*/
	public boolean runStillGoing() {
		return (StringUtil.notEmpty(runningFilePath) && MesquiteFile.fileExists(runningFilePath));
	}
	/*.................................................................................................................*/
	/** monitors the run.   */
	public boolean monitorAndCleanUpShell(ProgressIndicator progressIndicator){
		lastModified=null;
		boolean stillGoing = true;
		if (outputFilePaths!=null) {
			lastModified = new long[outputFilePaths.length];
			LongArray.deassignArray(lastModified);
		}

		while (runStillGoing() && stillGoing){
			if (aborted)
				return false;
			if (watcher!=null && watcher.fatalErrorDetected()) {
				return false;
			}
			processOutputFiles();
			//if (stdOutModified()) {
			//	textListener.setOutputText(getStdOut());
			//}
			try {
				Thread.sleep(sleepTime);
				//externalProcessManager.flushStandardOutputsReaders();
			}
			catch (InterruptedException e){
				MesquiteMessage.notifyProgrammer("InterruptedException in shell script executed by " + name);
				return false;
			}
			stillGoing = watcher == null || watcher.continueShellProcess(proc);
			if (progressIndicator!=null){
				progressIndicator.spin();
				if (progressIndicator.isAborted()){
					externalProcessManager.kill();
					aborted = true;
					return false;  //TODO: destroy process
				}
			}
			if (watcher!=null && watcher.fatalErrorDetected()) {
				return false;
			}
		}
		try {  
			Thread.sleep(ShellScriptUtil.recoveryDelay * 1000);
		}
		catch (InterruptedException e){
		}

		if (outputFileProcessor!=null)
			outputFileProcessor.processCompletedOutputFiles(outputFilePaths);
		return true;
	}

	/*.................................................................................................................*/
	/** monitors the run.   */
	public boolean monitorAndCleanUpShell(){
		
		return monitorAndCleanUpShell(null);
	}



}
