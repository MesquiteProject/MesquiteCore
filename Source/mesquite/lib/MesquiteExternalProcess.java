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

import java.io.*;

import org.apache.commons.io.input.*;




public class MesquiteExternalProcess  {
	OutputStream inputToProcess;
	OutputStreamWriter inputStreamsWriter;
	BufferedWriter inputBufferedWriter;
	OutputFileTailer errorReader;
	OutputFileTailer outputReader;
	Process proc;
	String directoryPath;
	String outputFilePath;
	String errorFilePath;
	MesquiteInteger errorCode;


	public MesquiteExternalProcess(Process proc) {
		this.proc = proc;
	}
	public MesquiteExternalProcess() {
	}
	/*.................................................................................................................*/
	public Process getProcess() {
		return proc;
	}
	/*.................................................................................................................*/
	public int getErrorCode() {
		if (errorCode!=null)
			return errorCode.getValue();
		return ProcessUtil.NOERROR;
	}
	/*.................................................................................................................*/

	public void restart(String directoryPath, String outputFilePath, String errorFilePath) {
		this.directoryPath = directoryPath;
		this.outputFilePath = outputFilePath;
		this.errorFilePath = errorFilePath;
		errorCode = new MesquiteInteger(ProcessUtil.NOERROR);

	}
	/*.................................................................................................................*/

	public void start(String directoryPath, String outputFilePath, String errorFilePath, String...command) {
		this.directoryPath = directoryPath;
		this.outputFilePath = outputFilePath;
		this.errorFilePath = errorFilePath;
		errorCode = new MesquiteInteger(ProcessUtil.NOERROR);
		this.proc = ProcessUtil.startProcess(errorCode, directoryPath,  outputFilePath,  errorFilePath, command);

	}


	/*.................................................................................................................*/

	public void setProcess(Process proc) {
		this.proc = proc;
	}
	/*.................................................................................................................*/
	public void kill () {
		if (proc!=null) {
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
		if (proc==null)
			return false;
		try {
			proc.exitValue();
		} catch (IllegalThreadStateException e) {
			return true;
		}
		return false;
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



