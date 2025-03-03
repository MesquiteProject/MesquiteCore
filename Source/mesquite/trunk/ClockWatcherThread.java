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

import java.awt.*;
import java.net.*;
import java.util.*;
import java.io.*;
import mesquite.lib.*;
import mesquite.*;
import mesquite.lib.duties.*;
import mesquite.lib.ui.MesquiteDialog;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.ProgressIndicator;

/* ======================================================================== */
public class ClockWatcherThread extends Thread {
	Mesquite mesquite;
	static final int sleep = 1500;
	static final int catnap = 50;
	static final int numBoutsPerSleep = 10;
	int sleepTime = sleep;
	long lastTick = 0;
	public ClockWatcherThread (Mesquite mesquite) {
		this.mesquite = mesquite;
		setPriority(Thread.MIN_PRIORITY);
	}

	void doDelayedRepaints(){
		//Repainting any window on special repaint queue
		for (int iw = MesquiteWindow.delayedRepaintQueue.size()-1;  iw>=0; iw--){
			MesquiteWindow w = (MesquiteWindow)MesquiteWindow.delayedRepaintQueue.elementAt(iw);
			if (System.currentTimeMillis()>w.drqTime){ 
				if (!w.disposed() && !w.disposing)
					w.repaintAll();
				MesquiteWindow.delayedRepaintQueue.remove(w); //done! remove
			}
		}
	}

	public void run() {
		long sleepCount = 0;
		boolean reportThreads = false;
		boolean sleptLong = false;
		//MesquiteTrunk.mesquiteTrunk.helpSearchManager.loadManual();
		while (!MesquiteTrunk.mesquiteTrunk.mesquiteExiting) { 
			boolean previousSleptLong = sleptLong;
			sleptLong = (sleepTime == sleep);
			if (previousSleptLong && !sleptLong) //just starting naps
				sleepCount = 0;
			else
				sleepCount++;

			//sleep is down in a series of bouts. This is a kludge to let some functions happen at each bout (delayed repaint) but others be after each sleep
			for (int iBout = 0; iBout<numBoutsPerSleep; iBout++){
				try {
					Thread.sleep(sleepTime/numBoutsPerSleep);  
					doDelayedRepaints();
				}
				catch (InterruptedException e){
					Thread.currentThread().interrupt();
				}
			}
			MesquiteTrunk.checkForResetCheckMenuItems();

			//Surveying windows to reset graphically after first shown (bugs in some macOS versions)
			if (sleptLong || sleepCount % (sleep/catnap) == 1) {
				MesquiteThread.surveyDoomedIndicators();
				if (MesquiteTrunk.isMacOSX())
					MesquiteThread.surveyNewWindows();
			}


			//Surveying threads for progressindicators, need to put up "command is executing"
			MesquiteThread[] mThreads = new MesquiteThread[MesquiteThread.threads.size()];
			try {
				for (int i=0; i<mThreads.length; i++)
					mThreads[i] = (MesquiteThread)MesquiteThread.threads.elementAt(i);
			}
			catch (Exception e){
			}

			try {
				if (MesquiteTrunk.startedFromFlex2) {
					String filesToOpenPath = System.getProperty("user.home") + MesquiteFile.fileSeparator + "Mesquite_Support_Files" + MesquiteFile.fileSeparator + MesquiteTrunk.encapsulatedPathOfExecutable+ MesquiteFile.fileSeparator + "filesToOpen.txt";

					if (MesquiteFile.fileExists(filesToOpenPath)) {

						String[] files = MesquiteFile.getFileContentsAsStrings(filesToOpenPath);

						MesquiteFile.deleteFile(filesToOpenPath);

						Thread.sleep(40);
						if (files != null) {
							for (int i=0; i<files.length; i++) {
								if (MesquiteTrunk.mesquiteTrunk.applicationHandler9 != null)
									MesquiteTrunk.mesquiteTrunk.applicationHandler9.handleOpenFile(files[i]);
								/* Old macOS (pre Java 9) handling of file opening. Disabling until someone complains
								 * else
									EAWTHandler.handleOpenFile(files[i]);*/
							}
						}
					}
				}
			}
			catch (InterruptedException e){
			}
			sleepTime = sleep;
			sleepCount++;
			for (int i=0; i<mThreads.length && mThreads[i]!=null; i++){  //go through current threads
				MesquiteThread thread = mThreads[i];
				thread.count++;
				long currentCommandSequence = thread.getCurrent();
				long previousCommandSequence = thread.getPrevious();
				if (currentCommandSequence != previousCommandSequence || previousCommandSequence<=0){ // || MesquiteWindow.numDialogs>0){
					thread.count = 0;
					thread.setPrevious(currentCommandSequence);
					ProgressIndicator pi = thread.getProgressIndicator();
					if (pi !=null && pi.getIsFromWatcher()) {
						pi.goAway();
						thread.setProgressIndicator(null);
					}
					thread.checkOften = false;

				}
				else {
					if ((thread.checkOften || sleptLong || sleepCount % (sleep/catnap) == 0) && (thread.count==thread.getPatience() || (thread.count> thread.getPatience() && thread.count % thread.getPatience() == 0))) {
						if (!thread.dead() && (MesquiteWindow.numNonWizardDialogs>0 || MesquiteDialog.responsePending)){
							thread.count =-1;
						}
						else if (!thread.dead() && MainThread.getShowWaitWindow()) {
							sleepTime = catnap;
							thread.checkOften = true;
							try {
								if (thread.getProgressIndicator() == null && thread.getSpontaneousIndicator()) {
									ProgressIndicator pi;

									thread.setProgressIndicator(pi = new ProgressIndicator(null, "Command is executing", "A command is executing.", 0, "Emergency Cancel")); //"Cancel Command");
									pi.setSecondaryMessage("Thread " + thread.getClass().getName() + " id " + thread.getID());
									pi.setIsFromWatcher(true);
									//pi.setSize(400, 220);
									pi.setButtonMode(ProgressIndicator.OFFER_KILL_THREAD);
									String commandNote = null;
									CommandRecord cr = thread.getCommandRecord();
									if (cr !=null)
										commandNote = cr.getProgressNote();
									if (!StringUtil.blank(commandNote))
										commandNote += "\n";
									else
										commandNote = "";
									pi.setSecondaryMessage(commandNote);
									pi.setTertiaryMessage(StringUtil.blankIfNull(thread.getCurrentCommandName()) + "\n" + StringUtil.blankIfNull(thread.getCurrentCommandExplanation()));
									pi.start();
								}
								if (thread.getProgressIndicator() !=null) {
									String commandNote = null;
									CommandRecord cr = thread.getCommandRecord();
									if (cr !=null){
										long tick = cr.getTicks();
										if (tick!=lastTick) {
											thread.getProgressIndicator().spin();
										}
										lastTick = tick;
										commandNote = cr.getProgressNote();
									}
									if (!StringUtil.blank(commandNote))
										commandNote += "\n";
									else
										commandNote = "";
									thread.getProgressIndicator().setSecondaryMessage(commandNote);
									thread.getProgressIndicator().setTertiaryMessage(StringUtil.blankIfNull(thread.getCurrentCommandName()) + "\n" + StringUtil.blankIfNull(thread.getCurrentCommandExplanation()));
									//thread.getProgressIndicator().spin();
									if (!thread.getProgressIndicator().isVisible()){
										thread.getProgressIndicator().getProgressWindow().setVisible(true);
									}
								}
							}
							catch (NullPointerException e){
							}
						}
					}
				}
			}
		}
	}


}



