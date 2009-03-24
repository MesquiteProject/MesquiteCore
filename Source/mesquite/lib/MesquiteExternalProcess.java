package mesquite.lib;

import java.io.*;



public class MesquiteExternalProcess  {
	OutputStream inputToProcess;
	OutputStreamWriter inputStreamsWriter;
	BufferedWriter inputBufferedWriter;
	StreamGobbler errorGobbler;
	StreamGobbler outputGobbler;
	Process proc;

	public MesquiteExternalProcess(Process proc) {
		this.proc = proc;
		errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
		outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");
		errorGobbler.start();
		outputGobbler.start();

	}
	/*.................................................................................................................*/
	public Process getProcess() {
		return proc;
	}
	/*.................................................................................................................*/

	public void setProcess(Process proc) {
		this.proc = proc;
	}
	/*.................................................................................................................*/

	public void dispose() {
		try {
			if (inputBufferedWriter!=null)
				inputBufferedWriter.close();
		}
		catch (Exception e) {
		}
	}
	/*.................................................................................................................*/

	public boolean processRunning() {
		try {
			proc.exitValue();
		} catch (IllegalThreadStateException e) {
			return true;
		}
		return false;
	}

	/*.................................................................................................................*/
	public void sendStringToProcess(String s) {
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
}
