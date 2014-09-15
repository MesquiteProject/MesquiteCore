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
	public MesquiteExternalProcess() {
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
	public void kill () {
		proc.destroy();
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
		if (proc==null)
			return false;
		try {
			proc.exitValue();
		} catch (IllegalThreadStateException e) {
			return true;
		}
		return false;
	}

	/*.................................................................................................................*/
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
}
