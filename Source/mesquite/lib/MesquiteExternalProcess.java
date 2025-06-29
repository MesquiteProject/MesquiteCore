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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;






public class MesquiteExternalProcess  {
	OutputStream inputToProcess;
	OutputStreamWriter inputStreamsWriter;
	BufferedWriter inputBufferedWriter;
	OutputFileTailer errorReader;
	OutputFileTailer outputReader;
	Process proc =null;
	ProcessHandle procH = null;
	String directoryPath;
	String outputFilePath;
	String errorFilePath;
	MesquiteInteger errorCode;
	boolean keyProcessIsChildOfProcess = false;
	boolean diesOnClosing=true;
	boolean dontKill = false;


	public MesquiteExternalProcess(Process proc, boolean keyProcessIsChildOfProcess) {
		if (proc!=null)
			this.proc = proc;
		this.keyProcessIsChildOfProcess = keyProcessIsChildOfProcess;
	}
	public MesquiteExternalProcess(ProcessHandle procH, boolean keyProcessIsChildOfProcess) {
		if (procH!=null) {
			this.procH = procH;
		}
		this.keyProcessIsChildOfProcess = keyProcessIsChildOfProcess;
	}
	public MesquiteExternalProcess() {
	}
	/*.................................................................................................................*/
	public int getErrorCode() {
		if (errorCode!=null)
			return errorCode.getValue();
		return ProcessUtil.NOERROR;
	}
	public void setDontKill(boolean dontKill) {
		this.dontKill = dontKill;
	}

	/*.................................................................................................................*/

	public void restart(String directoryPath, String outputFilePath, String errorFilePath) {
		this.directoryPath = directoryPath;
		this.outputFilePath = outputFilePath;
		this.errorFilePath = errorFilePath;
		errorCode = new MesquiteInteger(ProcessUtil.NOERROR);

	}
	/*.................................................................................................................*/

	public void start(String directoryPath, String outputFilePath, String errorFilePath, String envVariableName, String envVariableValue, String...command) {
		this.directoryPath = directoryPath;
		this.outputFilePath = outputFilePath;
		this.errorFilePath = errorFilePath;
		errorCode = new MesquiteInteger(ProcessUtil.NOERROR);
		this.proc = ProcessUtil.startProcess(errorCode, directoryPath,  outputFilePath,  errorFilePath, envVariableName,  envVariableValue, command);
		if (this.proc!=null)
			this.procH = proc.toHandle();

	}
	/*.................................................................................................................*/

	public void start(String directoryPath, String outputFilePath, String errorFilePath, String...command) {
		start(directoryPath,  outputFilePath,  errorFilePath, null, null, command);

	}
	public boolean diesOnClosing() {
		return diesOnClosing;
	}
	public void setDiesOnClosing(boolean diesOnClosing) {
		this.diesOnClosing = diesOnClosing;
	}


	/*.................................................................................................................*/

	public void setProcessHandle(ProcessHandle procH) {
		this.procH = procH;
	}
	/*.................................................................................................................*/

	public ProcessHandle getProcessHandle() {
		return procH;
	}
	/*.................................................................................................................*/

	public void setProcess(Process proc) {
		this.proc = proc;
	}
	/*.................................................................................................................*/

	public Process getProcess() {
		return proc;
	}
	/*.................................................................................................................*/
	public void kill () {
		if (dontKill)
			return;
		if (proc!=null) {
			if (keyProcessIsChildOfProcess) {  // it's the child process that counts - presumably as script-based
				ProcessHandle childH = ShellScriptUtil.getChildProcess(proc);
				if (childH==null)  // protection added 1 Jan 2025
					return;
				childH.destroy();
				try {
					Thread.sleep(100);
					if (ExternalProcessManager.isAlive(childH))
						childH.destroyForcibly();
				} catch (Exception e) {
				}
			} else {   // not a childProcess
				try {
					InputStream errorStream = proc.getErrorStream();
					errorStream.close();
					OutputStream outputStream = proc.getOutputStream();
					outputStream.close();
					endFileTailers();

				} catch (IOException e) {
					MesquiteMessage.println("Couldn't close streams of process.");
				}

				proc.destroy();
				try {
					Thread.sleep(100);
					if (ExternalProcessManager.isAlive(proc))
						proc.destroyForcibly();
				} catch (Exception e) {
				}
			}
		} else if (procH!=null) {
			procH.destroy();
			try {
				Thread.sleep(100);
				if (ExternalProcessManager.isAlive(procH))
					procH.destroyForcibly();
			} catch (Exception e) {
			}
		}
		endFileTailers();
	}

	/*.................................................................................................................*/
	public int exitValue () {
		if (proc!=null)
			return proc.exitValue();
		return 0;
	}

	/*.................................................................................................................*/

	public void dispose() {
		endFileTailers();
	}
	/*.................................................................................................................*/

	public boolean processRunning() {
		if (proc!=null && procH==null)
			procH = proc.toHandle();
		if (procH==null)
			return false;
		try {
			return procH.isAlive();
		} catch (IllegalThreadStateException e) {
			return true;
		}
	}
	public void endFileTailers() {
		if (outputReader!=null)
			 outputReader.stop();
		if (errorReader!=null)
			 errorReader.stop();
	}


	public void startFileTailers(File outputFile, File errorFile) {
		errorReader = new OutputFileTailer(errorFile);
		outputReader = new OutputFileTailer(outputFile);
		errorReader.start();
		outputReader.start();
	}
	/*.................................................................................................................*/

	public String getStdErrContents() {
		if (errorReader!=null)
			return errorReader.getFileContents();
		return null;
	}
	public String getStdOutContents() {
		if (outputReader!=null)
			return outputReader.getFileContents();
		return null;
	}

}



