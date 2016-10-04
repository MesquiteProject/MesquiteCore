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

import java.util.*;
import java.io.*;
import mesquite.lib.*;
import mesquite.*;
import com.apple.eawt.*;



/* ======================================================================== */
public class EAWTHandler implements FileOpener {
	boolean waiting = false;
	Vector fileList;
	Mesquite mesquite;
	static boolean quitting = false;
	public static Vector openFileThreads = new Vector();
	public EAWTHandler (Mesquite mesquite) {
		this.mesquite = mesquite;
		fileList = new Vector();
	}
	
	public void register(){
		Application app = new Application();
	    	EAWTH eawtH = new EAWTH();
		app.addApplicationListener(eawtH);
	}
	
	public boolean isWaiting(){
		return waiting;
	}
	
	public void openFilesNow() {
		if (mesquite == null)
			return;
		mesquite.openFilesNowUsed = true;
		MesquiteModule.incrementMenuResetSuppression();
		waiting = false;
		while (fileList.size()>0) {
			Object obj = fileList.elementAt(0);
			fileList.removeElement(obj);
			String path = null;
			if (obj instanceof File){
				File f = (File)obj;
				path = f.getAbsolutePath();
			}
			else
				path = (String)obj;
			CommandRecord cr = new CommandRecord((CommandThread)null, false);
		//	cr.suppressDebugWarning = true;
			openFileThreads.addElement(Thread.currentThread());
			CommandRecord prevR = MesquiteThread.getCurrentCommandRecord();
			MesquiteThread.setCurrentCommandRecord(cr);
			MesquiteTrunk.mesquiteTrunk.openFile(path);
			MesquiteThread.setCurrentCommandRecord(prevR);
			openFileThreads.removeElement(Thread.currentThread());
		}
		MesquiteModule.decrementMenuResetSuppression();
	}
	class EAWTH implements ApplicationListener {
		public void handleAbout (ApplicationEvent e){
			if (((Mesquite)(MesquiteTrunk.mesquiteTrunk)).about!=null) {
				((Mesquite)(MesquiteTrunk.mesquiteTrunk)).about.setVisible(true);
				e.setHandled(true);
			}
		}
		public void handleReOpenApplication (ApplicationEvent e){
		}
		public void handleOpenApplication (ApplicationEvent e){
		}
		public void handleOpenFile (ApplicationEvent e){
			MesquiteModule.incrementMenuResetSuppression();
			if (((Mesquite)MesquiteTrunk.mesquiteTrunk).ready) {
				CommandRecord cr = new CommandRecord((CommandThread)null, false);
				CommandRecord prevR = MesquiteThread.getCurrentCommandRecord();
				MesquiteThread.setCurrentCommandRecord(cr);
				openFileThreads.addElement(Thread.currentThread());
			//	cr.suppressDebugWarning = true;
				MesquiteTrunk.mesquiteTrunk.openFile(e.getFilename());
				MesquiteThread.setCurrentCommandRecord(prevR);
				openFileThreads.removeElement(Thread.currentThread());
			}
			else {
				waiting = true;
				fileList.addElement(e.getFilename());

			}
			MesquiteModule.decrementMenuResetSuppression();
		}
		public void handlePreferences (ApplicationEvent e){
		}
		public void handlePrintFile (ApplicationEvent e){
		}
		public void handleQuit (ApplicationEvent e){
			if (quitting)
				return;
			if (MesquiteTrunk.attemptingToQuit)
				return;
			quitting = true;
			QT q = new QT();
			String MRJversion = System.getProperty("mrj.version");
			double d= MesquiteDouble.fromString(MRJversion);
			if (d>=3 && d<3.4)
				q.start();
			else
				q.run();
		}
	}
	class QT extends Thread {
		public void run(){
			if (mesquite == null)
				return;
			MesquiteTrunk.mesquiteTrunk.logln("About to Quit...");
			mesquite.doCommand("quit", null, CommandChecker.defaultChecker);
			quitting = false;
		}
	}
}

