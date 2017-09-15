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
public class ExternalProcessManager implements Commandable  {
	static int sleepTime = 50;
	Process proc;
	String directoryPath;
	String programCommand;
	String programOptions;
	String name;
	String[] outputFilePaths; //reconnect
	String stdOutFilePath, stdErrFilePath;
	public String stdOutFileName = ShellScriptRunner.stOutFileName;
	public String stdErrFileName = ShellScriptRunner.stErrorFileName;
	OutputFileProcessor outputFileProcessor; //reconnect
	StdOutWatcher stdOutWatcher; //reconnect
	ShellScriptWatcher watcher; //reconnect
	boolean visibleTerminal;
	long[] lastModified;
	MesquiteModule ownerModule;
	MesquiteExternalProcess externalProcess;
	long stdOutLastModified = 0;
	long stdErrLastModified = 0;
	boolean badExitCode = false;

	
	public ExternalProcessManager(MesquiteModule ownerModule, String directoryPath, String programCommand, String programOptions, String name, String[] outputFilePaths, OutputFileProcessor outputFileProcessor, ShellScriptWatcher watcher, boolean visibleTerminal){
		this.directoryPath=directoryPath;
		this.name = name;
		this.outputFilePaths = outputFilePaths;
		this.outputFileProcessor = outputFileProcessor;
		this.ownerModule = ownerModule;
		this.programCommand = programCommand;
		this.programOptions = programOptions;
		stdOutFilePath = MesquiteFile.getDirectoryPathFromFilePath(directoryPath) + MesquiteFile.fileSeparator + stdOutFileName;
		stdErrFilePath = MesquiteFile.getDirectoryPathFromFilePath(directoryPath) + MesquiteFile.fileSeparator + stdErrFileName;
		this.watcher = watcher;
		this.visibleTerminal = visibleTerminal;
	}
	public ExternalProcessManager(MesquiteModule ownerModule){  //to be used for reconnecting
	}

	public boolean exitCodeIsBad() {
		return badExitCode;
	}
	public void setOutputProcessor(OutputFileProcessor outputFileProcessor){
		this.outputFileProcessor = outputFileProcessor;
	}
	public void setWatcher(ShellScriptWatcher watcher){
		this.watcher = watcher;
	}
	public void setStdOutWatcher(StdOutWatcher stdOutWatcher){
		this.stdOutWatcher = stdOutWatcher;
	}
	public String getStdOutFilePath() {
		return stdOutFilePath;
	}
	public void setStdOutFileName(String stdOutFileName) {
		this.stdOutFileName = stdOutFileName;
		stdOutFilePath = MesquiteFile.getDirectoryPathFromFilePath(directoryPath) + MesquiteFile.fileSeparator + stdOutFileName;
	}
	public String getStdErrFilePath() {
		return stdErrFilePath;
	}
	public void setStdErrFileName(String stdErrFileName) {
		this.stdErrFileName = stdErrFileName;
		stdErrFilePath = MesquiteFile.getDirectoryPathFromFilePath(directoryPath) + MesquiteFile.fileSeparator + stdErrFileName;
	}


	public static boolean isAlive(Process proc) {
		if (MesquiteTrunk.isJavaGreaterThanOrEqualTo(1.8)) 
			return proc.isAlive();
	try {
			proc.exitValue();
			return false;
		} catch (Exception e) {
			return true;
		}
		
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setDirectoryPath " + ParseUtil.tokenize(directoryPath));
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
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the directory path", "[file path]", commandName, "setDirectoryPath")) {
			directoryPath = parser.getFirstToken(arguments);
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
	public boolean isVisibleTerminal() {
		return visibleTerminal;
	}
	public void setVisibleTerminal(boolean visibleTerminal) {
		this.visibleTerminal = visibleTerminal;
	}
	/*.................................................................................................................*/
	public String getStdErr() {
		return MesquiteFile.getFileContentsAsStringNoWarn(stdErrFilePath);
	}
	/*.................................................................................................................*/
	public String getStdOut() {
		return MesquiteFile.getFileContentsAsStringNoWarn(stdOutFilePath);
	}
	
	OutputTextListener textListener;
	
	public  void setOutputTextListener(OutputTextListener textListener){
		this.textListener= textListener;
	}

	/*.................................................................................................................*/
	public long getStdErrLastModified() {
		File file = new File(stdErrFilePath);
		if (file==null)
			return 0;
		return file.lastModified();
	}
	/*.................................................................................................................*/
	public long getStdOutLastModified() {
		File file = new File(stdOutFilePath);
		if (file==null)
			return 0;
		return file.lastModified();
	}

	/*.................................................................................................................*/
	public void resetLastModified(int i){
		if (i>=0 && i<lastModified.length)
			lastModified[i]=0;
	}
	/*.................................................................................................................*/
	public void stopExecution(){
		if (externalProcess!=null)
			externalProcess.kill();
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
	public static String[] getStringArray(String string1, String...strings) {
		if (StringUtil.blank(string1))
			return null;
		String[] array;
		if (strings==null || strings.length==0) {
			array = new String[1];
			array[0]=string1;
		} else {
			array = new String[strings.length+1];
			array[0]=string1;
			int count=0;
			for (String s : strings) {
				count++;
				array[count]=s;
			}
		}
		return array;

	}
	/*.................................................................................................................*/
	public static String[] getStringArrayWithSplitting(String string1, String string2) {
		if (StringUtil.blank(string1))
			return null;
		String[] array;
		string2=StringUtil.stripBoundingWhitespace(string2);
		if (StringUtil.blank(string2)) {
			array = new String[1];
			array[0]=string1;
		} else {
			Parser parser = new Parser(string2);
			parser.setPunctuationString("");
			int total = parser.getNumberOfTokens();
			array = new String[total+1];
			array[0]=string1;
			String token = parser.getFirstToken();
			int count=0;
			while (StringUtil.notEmpty(token)) {
				count++;
				array[count]=token;
				token = parser.getNextToken();
			}
		}
		return array;

	}
	/*.................................................................................................................*/
	/** executes a shell script at "scriptPath".  If runningFilePath is not blank and not null, then Mesquite will create a file there that will
	 * serve as a flag to Mesquite that the script is running.   */
	public boolean executeInShell(){
		if (MesquiteTrunk.isJavaVersionLessThan(1.7))
			return false;
		proc = null;
		externalProcess = new MesquiteExternalProcess();
		externalProcess.start(directoryPath, stdOutFilePath, stdErrFilePath, getStringArrayWithSplitting(programCommand, programOptions));
		proc = externalProcess.getProcess();
		return proc!=null;
	}
	/*.................................................................................................................*/
	public boolean runStillGoing() {
		return true;
	}
	/*.................................................................................................................*/
	public boolean goodExitValue(int exitValue, boolean warnIfBad) {
		if (exitValue!=0) {
			ownerModule.logln("Process exit value: " +exitValue);
		}
		return exitValue==0;
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
	/** monitors the run.   */
	public boolean monitorAndCleanUpShell(ProgressIndicator progressIndicator){
		lastModified=null;
		boolean stillGoing = true;
		if (outputFilePaths!=null) {
			lastModified = new long[outputFilePaths.length];
			LongArray.deassignArray(lastModified);
		}

		while (runStillGoing() && stillGoing){

			if (watcher!=null && watcher.fatalErrorDetected()) {
				return false;
			}
			//if (stdOutModified()) {  // check to see if it should be stdOut or stdErr
			//	textListener.setOutputText(getStdOut());
			//}

			processOutputFiles();
			if (stdOutWatcher !=null) 
				stdOutWatcher.currentStdOutText(getStdOut());

			try {
				Thread.sleep(sleepTime);
				//externalProcessManager.flushStandardOutputsReaders();
			}
			catch (InterruptedException e){
				MesquiteMessage.notifyProgrammer("InterruptedException in shell script executed by " + name);
				return false;
			}
			stillGoing = watcher == null || watcher.continueShellProcess(proc);
			if (proc!=null && !isAlive(proc)) {
				stillGoing=false;
				boolean goodValue = goodExitValue(proc.exitValue(), true);
				if (!goodValue && !ownerModule.isDoomed() && !watcher.userAborted()) {
					String message = name + " quit, possibly because of an error ("+proc.exitValue()+"). Please examine StandardOutputFile and StandardErrorFile in the analysis directory for information.";
					AlertWithLinkToDirectory alert = new AlertWithLinkToDirectory(ownerModule.containerOfModule(),"Error in executing "+name, message, directoryPath);
				}
				badExitCode = !goodValue;
				return goodValue;
			}
			if (progressIndicator!=null){
				progressIndicator.spin();
				if (progressIndicator.isAborted()){
					externalProcess.kill();
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
