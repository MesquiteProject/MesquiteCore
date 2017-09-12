/* Mesquite source code.  Copyright 2001 and onward, D. Maddison and W. Maddison. 


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
import java.awt.event.*;

/*
 things to do:  
- immediate update?
- dealing with double progress windows?


File reading:
- have verbose mode of file-reading whereby it sends titles of all nexus commands to log file?
- reports previous block
- 
 */



/*===============================================*/
/** presents a progress window*/
public class ProgressIndicator implements Abortable {
	ProgressWindowThread t;
	MesquiteThread ownerThread;
	Color barColor;
	Color barBackColor;
	static int defaultHeight = 130;
	static int defaultWidth = 300;
	int height = 130;
	int width = 300;
	static long totalCreated = 0;
	long id =0;
	int buttonMode = FLAG_AND_HIDE;
	String title;
	private boolean fromClockWatcher = false;
	public static final int NOSTOP = -1;
	public static final int FLAG = 0;
	public static final int FLAG_AND_HIDE = 1;
	public static final int OFFER_CONTINUE = 2;
	public static final int OFFER_KILL_THREAD = 3;
	public static final int OFFER_FLAG_OR_KILL_THREAD =4;
	public static final int OFFER_CONTINUE_FORCEQUIT = 5;

	public boolean dontStart = false;
	MesquiteTimer timer = null;

//	0 = posts flag that button hit; 1 = posts flag that button hit AND gets rid of dialog; 2 = aborts owner thread
	//boolean goneAway = false;
	public ProgressIndicator (MesquiteProject mp, String title, String initialMessage, long total, String buttonName) {
		if (mp!=null) {
			barColor= ColorTheme.getActiveDark();
			barBackColor= ColorTheme.getActiveLight();
		}
		else {
			barColor= ColorDistribution.spinDark;
			barBackColor = ColorDistribution.spinLight;
		}
		this.title = title;
		id = totalCreated++;
		t = new ProgressWindowThread(this,title, initialMessage, total, buttonName);
	}

	public ProgressIndicator (MesquiteProject mp, String title, String initialMessage, long total, boolean showStop) {
		this(mp, title, initialMessage, total, showStop ? "Stop" : null);
		setButtonMode(FLAG_AND_HIDE);
	}
	public ProgressIndicator (MesquiteProject mp, String title, long total, boolean showStop) {
		this(mp,title, "", total, showStop);
	}
	public ProgressIndicator (MesquiteProject mp, String title, long total) {
		this(mp,title, total, true);
	}
	/** If this constructor is used, then the progress indicator doesn't have a final value, and thus is just a "spinning" indicator */
	public ProgressIndicator (MesquiteProject mp, String title) {
		this(mp,title, 0, true);
	}
	public void toFront(){
		if (t != null && t.dlog != null)
			t.dlog.toFront();
	}

	public boolean getIsFromWatcher(){
		return fromClockWatcher;
	}
	public void setIsFromWatcher(boolean v){
		fromClockWatcher = v;
	}
	public long getID(){
		return id;
	}
	public void setOwnerThread(MesquiteThread th){
		ownerThread = th;
		if (t.dlog !=null)
			t.dlog.setButtonsVisible(!(buttonMode >= OFFER_KILL_THREAD && ownerThread != MainThread.mainThread));
	}
	public MesquiteThread getOwnerThread(){
		return ownerThread;
	}
	public void setSize(int w, int h){
		height = h;
		width = w;
	}
	public void setButtonMode(int a){ //0 = posts flag that button hit; 1 = posts flag that button hit AND gets rid of dialog; 2 = aborts owner thread
		buttonMode = a;
		if (t.dlog !=null)
			t.dlog.setButtonsVisible(buttonMode!= NOSTOP && !(buttonMode == OFFER_KILL_THREAD && ownerThread != MainThread.mainThread));

	}
	public int getButtonMode(){ //0 = posts flag that button hit; 1 = posts flag that button hit AND gets rid of dialog; 2 = aborts owner thread
		return buttonMode;			
	}
	public String getStopButtonName(){
		if (t.dlog !=null)
			return t.dlog.getStopButtonName();
		return "Stop";
	}
	public void setStopButtonName(String buttonName){
		if (t.dlog !=null)
			t.dlog.setStopButtonName(buttonName);

	}
	public void startTimer() {
		timer = new MesquiteTimer();
	}

	//assign
	public void start(){
		try {
			if (getOwnerThread() == null && Thread.currentThread() instanceof MesquiteThread)
				setOwnerThread((MesquiteThread)Thread.currentThread()); //by default the owner thread is the one that requests the window to start
			t.start();
		}
		catch(IllegalThreadStateException e){
		}
	}
	public void goAway(){
		if (timer!=null)
			if (title!= null)
				MesquiteMessage.println("Total time (for " + title + "): " + timer.timeSinceVeryStart());
			else 
				MesquiteMessage.println("Total time operation: " + timer.timeSinceVeryStart());
		t.stopDLOG();
		t.interrupt();
	}
	public ProgressWindow getProgressWindow(){
		if (t == null)
			return null;
		return t.dlog;
	}
	public boolean isVisible(){
		if (t == null)
			return false;
		if (t.dlog == null)
			return false;
		return t.dlog.isVisible();
	}
	/*.................................................................................................................*/
	public boolean isAborted () {
		if (t.dlog!=null) {
			boolean abort = t.dlog.wasButtonHit() && buttonMode > 0;
			return abort;
		}
			else
				return false;
	}
	/*.................................................................................................................*/
	public void spin(){
		if (t.dlog!=null)
			t.dlog.spin();
	}
	/*.................................................................................................................*/
	public Color getBarColor(){
		return barColor;
	}
	/*.................................................................................................................*/
	public Color getBarBackColor(){
		return barBackColor;
	}
	/*.................................................................................................................*/
	public void setAbort(){
		if (t.dlog!=null)
			t.dlog.setAbort();
	}
	String offerContinueString = "Are you sure you want to stop?";
	/*.................................................................................................................*/
	public void setOfferContinueMessageString(String ocs){
		offerContinueString = ocs;
	}

	/*.................................................................................................................*/
	public void setText (String s, boolean bringToFront, boolean immediately) {
		if (t.dlog!=null)
			t.dlog.setText(s, bringToFront, immediately);
		else
			t.setText (s);
	}
	/*.................................................................................................................*/
	public void setText (String s, boolean bringToFront) {
		if (t.dlog!=null)
			t.dlog.setText(s, bringToFront);
		else
			t.setText (s);
	}
	/*.................................................................................................................*/
	public void setText (String s) {
		t.setText (s);
	}
	/*.................................................................................................................*/
	public void setSecondaryMessage (String s) {
		t.setSecondaryMessage(s);
	}
	/*.................................................................................................................*/
	public void setTertiaryMessage (String s) {
		t.setTertiaryMessage(s);
	}
	/*.................................................................................................................*/
	public long getTextRefreshInterval(){
		return t.getTextRefreshInterval();
	}
	/*.................................................................................................................*/
	public void setTextRefreshInterval(long interval){
		t.setTextRefreshInterval(interval);
	}
	/*.................................................................................................................*/
	MesquiteFile scriptingFile;
	public void setScriptingFile(MesquiteFile file){
		scriptingFile = file;
	}
	public MesquiteFile getScriptingFile(){
		return scriptingFile;
	}
	/*.................................................................................................................*/
	public void setCurrentValue (long current) {
		if (t.dlog!=null)
			t.dlog.setCurrentValue(current);
	}
	/*.................................................................................................................*/
	public void increment () {
		if (t.dlog!=null){
			t.dlog.setCurrentValue(t.dlog.getCurrentValue() + 1);
		}
	}
	/*.................................................................................................................*/
	public long getCurrentValue () {
		if (t.dlog!=null)
			return t.dlog.getCurrentValue();
		return 0;
	}
	/*.................................................................................................................*/
	public void setTitle (String s) {
		if (t.dlog!=null)
			t.dlog.setTitle(s);
	}
	/*.................................................................................................................*/
	public void setCurrentAndText (long current, String s) {
		if (t.dlog!=null)
			t.dlog.setCurrentAndText(current, s);
	}
	/*.................................................................................................................*/
	public void setTotalValue (long total) {
		if (t.dlog!=null)
			t.dlog.setTotalValue(total);
	}
	/*.................................................................................................................*/
	public long getTotalValue () {
		if (t.dlog!=null)
			return t.dlog.getTotalValue();
		else 
			return 0;
	}

	public String toString(){
		String name = "";
		if (t.dlog!=null)
			name = t.dlog.getName();
		return "Progress indicator (" + name + "  id: " + getID()  + "; button mode: "+ getButtonMode()  + ")";
	}
}

/* ======================================================================== */
class ProgressWindowThread extends Thread {
	ProgressWindow dlog;
	String title,initialMessage;
	StringBuffer secondaryMessage = new StringBuffer();
	StringBuffer tertiaryMessage = new StringBuffer();
	long textRefreshInterval = 500;
	long total;
	String buttonName;
	boolean dontStart = false;
	ProgressIndicator progressIndicator=null;

	public ProgressWindowThread (ProgressIndicator progressIndicator, String title, String initialMessage, long total, String buttonName) {
		this.title = title;
		this.initialMessage = initialMessage;
		this.total = total;
		this.buttonName = buttonName;
		this.progressIndicator = progressIndicator;
		if (progressIndicator != null)
			progressIndicator.dontStart = dontStart;
	}
	void setText(String message){
		this.initialMessage = message;
		if (dlog !=null)
			dlog.setText(message);
	}
	void setText(String message, boolean immediately){
		this.initialMessage = message;
		if (dlog !=null)
			dlog.setText(message, false, immediately);
	}
	/*.................................................................................................................*/
	public long getTextRefreshInterval(){
		if (dlog==null)
			return textRefreshInterval;
		else
			return dlog.getTextRefreshInterval();
	}
	/*.................................................................................................................*/
	/** Allows one to set the interval, in milliseconds, between refreshes of the text in the progress indicator.  Default is 500. */
	public void setTextRefreshInterval(long interval){
		if (dlog!=null)
			dlog.setTextRefreshInterval(interval);
	}
	void setSecondaryMessage(String message){
		secondaryMessage.setLength(0);
		if (message!=null)
			secondaryMessage.append(message);
		if (dlog !=null)
			dlog.setSecondaryMessage(message);
	}
	void setTertiaryMessage(String message){
		tertiaryMessage.setLength(0);
		if (message!=null)
			tertiaryMessage.append(message);
		if (dlog !=null)
			dlog.setTertiaryMessage(message);
	}
	/** DOCUMENT */
	public void run() {
		if (!dontStart) {
			dlog = new ProgressWindow(progressIndicator, title, initialMessage, total, buttonName);
			if (!dontStart)
				dlog.setVisible(true); //TODO: if thread doesn't show until after file reading started, and alert appears, could be hidden under this, with STOP being only option
			if (dontStart) 
				dlog.hide();
			/*if (dlog.isVisible()){
				while (showDialog){

				}
			}*/
		}
	}
	public void stopDLOG() {
		dontStart = true;
		if (progressIndicator != null)
			progressIndicator.dontStart = dontStart;
		if (dlog!=null) {
			dlog.setVisible(false);


			if (MesquiteTrunk.isMacOSX() && !MesquiteTrunk.isMacOSXJaguar()){
				try {
					Thread.sleep(10); //attempt to workaround bug in 1.3.1 update 1 on OS X
				}
				catch(InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			MesquiteThread.doomIndicator(dlog);
		}
	}

	public void interrupt(){
		dontStart = true;
		if (progressIndicator != null)
			progressIndicator.dontStart = dontStart;
		if (dlog!=null) {
			dlog.hide();
			MesquiteThread.doomIndicator(dlog);
		}
		super.interrupt();
	}


}


