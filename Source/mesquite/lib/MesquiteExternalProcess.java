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



public class MesquiteExternalProcess  {
	OutputStream inputToProcess;
	//OutputStreamWriter inputStreamsWriter;
	//BufferedWriter inputBufferedWriter;
	//StandardOutputsStreamReader errorReader;
	//StandardOutputsStreamReader outputReader;
	//FileWriter outputWriter;
	//FileWriter errorWriter;
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
		return ShellScriptUtil.NOERROR;
	}
	/*.................................................................................................................*/

	public void start(String directoryPath, String outputFilePath, String errorFilePath, String...command) {
		this.directoryPath = directoryPath;
		this.outputFilePath = outputFilePath;
		this.errorFilePath = errorFilePath;
		errorCode = new MesquiteInteger(ShellScriptUtil.NOERROR);
		this.proc = ShellScriptUtil.startProcess(errorCode, directoryPath,  outputFilePath,  errorFilePath, command);
		
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
			} catch (IOException e) {
				MesquiteMessage.println("Couldn't close streams of process.");
			}
			proc.destroy();
			try {
				Thread.sleep(100);
				if (proc.isAlive())
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
/*		try {
			if (inputBufferedWriter!=null)
				inputBufferedWriter.close();
		}
		catch (Exception e) {
		}*/
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
	
	/*
	 * public void startStandardOutputsReaders(File outputFile, File errorFile) {
		try { 
			errorWriter = new FileWriter(errorFile);
			outputWriter = new FileWriter(outputFile);
		} 
		catch (FileNotFoundException e) {
			MesquiteMessage.warnProgrammer("Output file not found");
		}
		catch (IOException e) {
			MesquiteMessage.warnProgrammer("IOException");
		}
		
		errorReader = new StandardOutputsStreamReader(proc.getErrorStream(), errorWriter);
		outputReader = new StandardOutputsStreamReader(proc.getInputStream(),  outputWriter);
		if (errorReader!=null)
			errorReader.start();
		if (outputReader!=null)
			outputReader.start();
	
	}
			*/

	/*.................................................................................................................*

	public void flushStandardOutputsReaders() {
		if (fos!=null) {
			try { 
				fos.flush();
			} 
			catch (IOException e) {
				MesquiteMessage.warnProgrammer("IOException on standard output file.");
			}
		}
	}
	
	public void endStandardOutputsReaders() {
		if (fos!=null) {
			try { 
				fos.close();      
			} 
			catch (IOException e) {
				MesquiteMessage.warnProgrammer("IOException on standard output file.");
			}
		}
	}


	/*.................................................................................................................*
	public void sendStringToProcess(String s) {
		if (proc==null)
			return;
		if (inputToProcess==null)
			inputToProcess = proc.getOutputStream();
		if (inputToProcess!=null && inputStreamsWriter==null)
			inputStreamsWriter = new OutputStreamWriter(inputToProcess);
		if (inputToProcess==null || inputStreamsWriter==null)
			return;
		if (inputBufferedWriter==null)
			inputBufferedWriter = new BufferedWriter(inputStreamsWriter);
		if (inputBufferedWriter==null)
			return;
		try {
			try {
				inputBufferedWriter.write(s);
			} finally {
				inputBufferedWriter.flush();
				//	inputBufferedWriter.close();
			} 
		} catch (Exception e) {
		}

	}
	/*.................................................................................................................*/
}


class StandardOutputsStreamReader extends Thread {
	InputStream is;
	FileWriter os;


	StandardOutputsStreamReader(InputStream is, FileWriter os) {
		this.is = is;
		this.os = os;
	}
	StandardOutputsStreamReader(InputStream is) {
		this(is, null);
	}
	public void run() {
		try {
		/*	PrintWriter pw = null;
			if (os != null)
				pw = new PrintWriter(os);
*/
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line=null;
			while ( (line = br.readLine()) != null) {
				if (os != null) {
					os.write(line+StringUtil.lineEnding());
				}
			}
			if (os != null) {
				os.flush();
				os.close();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();  
		}
	}


}

