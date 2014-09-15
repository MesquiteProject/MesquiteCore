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

/** a thermometer panel (see class ProgressIndicator)*/
public class ProgressPanel extends MesquitePanel implements MouseListener {
	public ThermoPanel progressBar;
	public ProgressPanel progressPanel;
	ProgressWindow pWindow;
	long currentValue = 0;
	long totalValue = 0;
	long textRefreshInterval = 500;
	public static int visibleProgWindows=0;
	int defaultDialogHeight = 130;
	int defaultDialogWidth = 380;
	int dialogHeight = defaultDialogHeight;
	int dialogWidth = defaultDialogWidth;
	int detailsExtraHeight = 220;
	int barHeight = 20;
	int barFromSide = 20;
	int textFromBottom = 8;
	boolean showDetails = false;
	int barTop = (dialogHeight-barHeight)/2-5;
	ProgressIndicator progressIndicator;
	String buttonName = "Stop";
	String progressString = "";
	public boolean buttonWasHit = false;
	private boolean hidden = false;
	private boolean disposed = false;
	Button stop;
	Panel buttons;
	Font textFont;
	public long doomTicks = 0;
	StringInABox stringInABox2, stringInABox3;
	boolean inWizard = false;

	int buttonTop=dialogHeight - barHeight - 22; //barHeight +barTop+8;
	public ProgressPanel (ProgressIndicator progressIndicator, ProgressWindow pWindow, String title, String initialMessage, long total, String buttonName) {
		setLayout(null);
		this.progressIndicator=progressIndicator;
		this.pWindow = pWindow;
		progressBar = new ThermoPanel();
		progressBar.setProgressIndicator(progressIndicator);
		progressBar.setLocation(barFromSide,barTop);
		progressBar.setSize(dialogWidth-20*2,barHeight);
		add(progressBar);
		if (progressIndicator != null){
			dialogWidth = progressIndicator.width;
			dialogHeight = progressIndicator.height;
		}
		if (showDetails)
			dialogHeight += detailsExtraHeight;
		setBackground(ColorTheme.getInterfaceBackgroundPale());
		this.buttonName =buttonName;
		progressString = initialMessage ;


		if (total==0)
			progressBar.setBackground(ColorDistribution.straw);
		//	progressBar.setBackground(Color.magenta);
		progressBar.setVisible(true);
		setTotalValue(total);
		progressBar.repaint();

		//addMouseListener(this);

		setSize(dialogWidth, dialogHeight);
		StringBuffer seco = new StringBuffer("");
		StringBuffer tert = new StringBuffer("");
		if (progressIndicator != null){
			seco = progressIndicator.t.secondaryMessage;
			tert = progressIndicator.t.tertiaryMessage;
		}
		stringInABox2 = new StringInABox(seco, getFont(), dialogWidth-20);
		stringInABox3 = new StringInABox(tert, getFont(), dialogWidth-20);
		textFont = new Font ("SanSerif", Font.PLAIN, 10);
		if (buttonName !=null){
			buttons = new Panel();
			Font f2 = new Font ("Dialog", Font.PLAIN, 12);
			buttons.add("East", stop = new Button(buttonName));
			//stop.setBackground(Color.white);
			stop.setFont(f2);
			stop.setVisible(true);//����
			stop.requestFocusInWindow();
			add(buttons);
			buttons.setLocation(0,buttonTop);
			buttons.setSize(dialogWidth, barHeight + 16); //dialogHeight - buttonTop);
			//buttons.setBackground(Color.yellow);
			//container.add("South", buttons);
			if (progressIndicator != null)
				buttons.setVisible(!(progressIndicator.buttonMode == ProgressIndicator.OFFER_KILL_THREAD && progressIndicator.ownerThread != MainThread.mainThread));
			stop.addMouseListener(this);
		}
		resetSizes();
	}
	void resetSizes(){
		progressBar.setLocation(barFromSide,barTop);
		progressBar.setSize(dialogWidth-20*2,barHeight);
		progressBar.setVisible(true);
		progressBar.repaint();
		if (buttons != null){
			buttons.setLocation(0,buttonTop);
			buttons.setSize(dialogWidth, barHeight + 16); //dialogHeight - buttonTop);
		}
	}
	/*
	public void setVisible(boolean vis){
		super.setVisible(vis);
		if (vis){
			progressBar.requestFocusInWindow();
		}
	}*/
	public void windowClosing(){
		if (buttonName!=null)
			buttonHit(buttonName, stop);
	}
	public void setStopButtonName(String buttonName){
		if (stop ==null)
			return;
		stop.setLabel(buttonName);
		if (buttons != null){
			buttons.invalidate();
			buttons.validate();
			buttons.doLayout();
		}
	}
	public String getStopButtonName(){
		if (stop ==null)
			return "Stop";
		return stop.getLabel();
	}
	/*.................................................................................................................*/
	public void buttonHit( String buttonLabel, Button button){
		if (progressIndicator != null && progressIndicator.buttonMode <ProgressIndicator.OFFER_KILL_THREAD) {
			setText("");
			buttonWasHit = true;
		}
	}
	public boolean checkForMouseInDetailTriangle (int mouseX, int mouseY){
		int y = progressBar.getBounds().y + progressBar.getBounds().height;
		int x = 10;
		if (mouseX >= x && mouseX< x + 16 && mouseY >= y && mouseY< y + 16){
			showDetails = !showDetails;
			resetShowDetails();
			return true;
		}
		return false;
	}	
	public void mouseClicked(MouseEvent e){
		//	if (e.getComponent() == this){
		//		checkForMouseInDetailTriangle(e.getX(), e.getY());
		//	}
	}
	private void resetShowDetails(){
		if (showDetails)
			dialogHeight = defaultDialogHeight + detailsExtraHeight;
		else
			dialogHeight = defaultDialogHeight;
		pWindow.dialogWidth = dialogWidth;
		pWindow.dialogHeight = dialogHeight;
		setSize(dialogWidth, dialogHeight);
		pWindow.setSize(dialogWidth, dialogHeight);
		int buttonTop=dialogHeight - barHeight - 22; //barHeight +barTop+8;
		if (buttons != null)
			buttons.setLocation(0,buttonTop);
	}

	public void mouseEntered(MouseEvent e){
	}
	public void mouseExited(MouseEvent e){
	}
	public void mousePressed(MouseEvent e){
		if (e.getComponent() == this){
			if (checkForMouseInDetailTriangle(e.getX(), e.getY()))
				e.consume();
		}
	}

	public void mouseReleased(MouseEvent e){
		if (e.getComponent() instanceof Button) {
			if (progressIndicator == null)
				return;
			if (progressIndicator.buttonMode== ProgressIndicator.OFFER_CONTINUE){
				if (!AlertDialog.query(this, "Stop?", progressIndicator.offerContinueString, "Continue", "Stop", 1)){
					buttonHit(((Button)e.getComponent()).getLabel(), (Button)e.getComponent());
					hide();
					MesquiteThread.doomIndicator(this);
				}
			}
			else if (progressIndicator.buttonMode<ProgressIndicator.OFFER_KILL_THREAD) {
				buttonHit(((Button)e.getComponent()).getLabel(), (Button)e.getComponent());
				if (progressIndicator.buttonMode== ProgressIndicator.FLAG_AND_HIDE){
					hide();
					MesquiteThread.doomIndicator(this);
				}
			}
			else if (progressIndicator.buttonMode == ProgressIndicator.OFFER_KILL_THREAD) {
				if (progressIndicator.getOwnerThread()!=null) {
					if (progressIndicator.getOwnerThread() == MainThread.mainThread && MainThread.mainThread != Thread.currentThread()){
						if (AlertDialog.query(this, "Cancel?", "Are you sure you want to cancel the command?  Cancelling in this way will stop the command immediately regardless of the consequences.  Various errors may occur, you may lose unsaved data, and your calculations may be incomplete or incorrect.  It is recommended that you cancel only when you absolutely need to.", "Continue", "Cancel Command", 1))
							return;


						hide();
						MesquiteDialog.cleanUpWizard();
						MainThread.mainThread.doCleanUp();
						MesquiteThread.doomIndicator(this);
						if (MainThread.getCurrentlyExecuting()!=null)
							MainThread.getCurrentlyExecuting().interrupted = true;
						
						try {
							MainThread.mainThread.interrupt();
						}
						catch (Exception ex){
							MesquiteMessage.println("Exception cancelling command");
							MesquiteMessage.printStackTrace(ex);
						}
						if (MainThread.getCurrentlyExecuting()!=null)
							MesquiteTrunk.mesquiteTrunk.alert("Command stopped: " + MainThread.getCurrentlyExecuting().getListName() + ".  Because this command was stopped by an emergency cancel, it is possible that some calculations or operations were partially completed, and MESQUITE MAY NOW BE UNSTABLE.  IT IS HIGHLY RECOMMENDED THAT YOU RESTART MESQUITE.  If you need to save your file, is best to use \"Save As\"."); 
						else 
							MesquiteTrunk.mesquiteTrunk.alert("Command stopped.  Because this command was stopped by an emergency cancel, it is possible that some calculations or operations were partially completed, and MESQUITE MAY NOW BE UNSTABLE.  IT IS HIGHLY RECOMMENDED THAT YOU RESTART MESQUITE.  If you need to save your file, is best to use \"Save As\"."); 
						MainThread.emergencyCancelled = true;
						MainThread.mainThread = new MainThread();
						MainThread.mainThread.start();
					}
				}
			}
			else if (progressIndicator.buttonMode == ProgressIndicator.OFFER_FLAG_OR_KILL_THREAD) {
				int response = AlertDialog.query(this, "Stop?", "Do you want to stop the file reading gracefully, after the next commands are processed, or do you want to cancel command processing immediately?  The latter is not recommended except in cases of emergency, as it will leave the file still open but with portions missing or analyses incomplete.", "Continue", "Stop Gracefully", "Emergency Cancel", 2);

				if (response == 0)
					return;

				if (response == 1){
					buttonHit(((Button)e.getComponent()).getLabel(), (Button)e.getComponent());
					hide();
					MesquiteThread.doomIndicator(this);
				}
				else if (progressIndicator.getOwnerThread()  != Thread.currentThread()){
					if (progressIndicator.getScriptingFile() !=null){
						progressIndicator.getScriptingFile().closeReading(); //needed so that thread can later be interrupted
					}

					if (progressIndicator.getOwnerThread() == MainThread.mainThread ){
						hide();
						MesquiteThread.doomIndicator(this);
						if (MainThread.getCurrentlyExecuting()!=null)
							MainThread.getCurrentlyExecuting().interrupted = true;
						try {
							MainThread.mainThread.interrupt();
						}
						catch (Exception ex){
							MesquiteMessage.println("Exception cancelling command");
							MesquiteMessage.printStackTrace(ex);
						}
						if (MainThread.getCurrentlyExecuting()!=null)
							MesquiteTrunk.mesquiteTrunk.alert("Command stopped: " + MainThread.getCurrentlyExecuting().getListName() + ".  Because this command was stopped by an emergency cancel, it is possible that some calculations or operations were partially completed, and MESQUITE MAY NOW BE UNSTABLE.  IT IS HIGHLY RECOMMENDED THAT YOU RESTART MESQUITE.  If you need to save your file, is best to use \"Save As\"."); 
						else
							MesquiteTrunk.mesquiteTrunk.alert("Command stopped.  Because this command was stopped by an emergency cancel, it is possible that some calculations or operations were partially completed, and MESQUITE MAY NOW BE UNSTABLE.  IT IS HIGHLY RECOMMENDED THAT YOU RESTART MESQUITE.  If you need to save your file, is best to use \"Save As\"."); 
						MainThread.emergencyCancelled = true;
						MainThread.mainThread = new MainThread();
						MainThread.mainThread.start();
					}
					else {
						hide();
						MesquiteThread.doomIndicator(this);
						progressIndicator.getOwnerThread().interrupt();  //currently this doesn't work on file read, perhaps because of open stream
					}
				}
			}
			else if (progressIndicator.buttonMode == ProgressIndicator.OFFER_CONTINUE_FORCEQUIT) {
				if (!AlertDialog.query(this, "Force Quit?", "Do you want to continue or force quit Mesquite? If you Force Quit, changes may be lost", "Continue", "Force Quit"))
					MesquiteTrunk.mesquiteTrunk.exit(true, 0);

			}
		}
	}

	/*.................................................................................................................*/
	int secondaryOffset = 20;
	int tertiaryOffset = secondaryOffset+40;
	/*.................................................................................................................*/
	public void paint(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;

		int y = progressBar.getBounds().y + progressBar.getBounds().height;
		int h = progressBar.getBounds().x;

		g.setFont(textFont);
		/*	if (inWizard){

				g.setColor(getBackground());

			g.fillRect(h-15,barTop-textFromBottom-30,dialogWidth, tertiaryOffset+secondaryOffset);
		}
		 */
		g.setColor(Color.black);


		g.drawString(progressString, h-10,  barTop-textFromBottom);
		if (progressIndicator != null && (progressIndicator.t.secondaryMessage.length()>0 || progressIndicator.t.tertiaryMessage.length()>0)){
			g.drawString("Details", h+8,  y + secondaryOffset -8);
			g.drawLine(h,  y + secondaryOffset, dialogWidth - 20, y + secondaryOffset);
			if (showDetails){
				g.drawImage(InfoBar.triangleImageDown, h-10, y, this);
				if (progressIndicator.t.secondaryMessage.length()>0){
					stringInABox2.setStringAndFont(progressIndicator.t.secondaryMessage, g.getFont());
					stringInABox2.draw(g, h-10,  y + secondaryOffset);
				}
				if (progressIndicator.t.tertiaryMessage.length()>0){
					stringInABox3.setStringAndFont(progressIndicator.t.tertiaryMessage, g.getFont());
					stringInABox3.draw(g, h-10,  y + tertiaryOffset);
				}
			}
			else {
				g.drawImage(InfoBar.triangleImage, h-10, y, this);
			}
		}

		MesquiteWindow.uncheckDoomed(this);
	}
	/*.................................................................................................................*/
	private void drawProgressText () {
		try {

			Graphics g=getGraphics();
			if (g != null) {
				update(g);
				g.dispose();
			}
		}
		catch (NullPointerException e){
			return;
		}
	}
	/*.................................................................................................................*/
	private void drawSecondaryMessage () {
		if (!showDetails || MesquiteWindow.checkDoomed(this))
			return;

		Graphics g=getGraphics();
		if (g != null) {
			g.setFont(textFont);
			g.setColor(getBackground());
			int y = progressBar.getBounds().y + progressBar.getBounds().height;
			g.fillRect(5,y + secondaryOffset -1,dialogWidth, tertiaryOffset-secondaryOffset);
			g.setColor(Color.black);
			if (progressIndicator  != null && progressIndicator.t.secondaryMessage.length()>0){
				//g.drawString("Details", 28,  y + secondaryOffset -8);
				g.drawLine(20,  y + secondaryOffset, dialogWidth - 20, y + secondaryOffset);
				g.drawImage(InfoBar.triangleImageDown, 10, y, this);
				stringInABox2.setStringAndFont(progressIndicator.t.secondaryMessage, g.getFont());
				stringInABox2.draw(g, 10,  y + secondaryOffset);
			}
			g.dispose();
		}
		MesquiteWindow.uncheckDoomed(this);
	}
	/*.................................................................................................................*/
	private void drawTertiaryMessage () {
		if (!showDetails || MesquiteWindow.checkDoomed(this))
			return;
		Graphics g=getGraphics();
		if (g != null) {
			g.setFont(textFont);
			g.setColor(getBackground());
			int y = progressBar.getBounds().y + progressBar.getBounds().height;
			g.fillRect(5,y + tertiaryOffset-1,dialogWidth, dialogHeight);
			g.setColor(Color.black);
			//g.drawString(progressString, 10,  barTop-textFromBottom);
			if (progressIndicator  != null && progressIndicator.t.tertiaryMessage.length()>0){
				//g.drawString("Details", 28,  y + secondaryOffset -8);
				g.drawLine(20,  y + secondaryOffset, dialogWidth - 20, y + secondaryOffset);
				g.drawImage(InfoBar.triangleImageDown, 10, y, this);
				stringInABox3.setStringAndFont(progressIndicator.t.tertiaryMessage, g.getFont());
				stringInABox3.draw(g, 10,  y + tertiaryOffset);
			}
			g.dispose();
		}
		MesquiteWindow.uncheckDoomed(this);
	}
	/*.................................................................................................................*/
	public void setAbort(){
		buttonWasHit = true;
	}

	/*.................................................................................................................*/
	public long getTextRefreshInterval(){
		return textRefreshInterval;
	}
	/*.................................................................................................................*/
	public void setTextRefreshInterval(long interval){
		textRefreshInterval = interval;
	}
	long lastText = 0;
	/*.................................................................................................................*/
	public void setText (String s, boolean bringToFront) {
		setText(s, bringToFront, false);
	}
	/*.................................................................................................................*/
	public void setText (String s, boolean bringToFront, boolean immediately) {
		if (buttonWasHit && progressIndicator  != null && progressIndicator.buttonMode == 1)
			return;
		progressString = s;
		long c = System.currentTimeMillis();
		if (immediately || c-lastText > textRefreshInterval) {
			lastText = c;
			drawProgressText();
		}
		if (bringToFront && MesquiteWindow.numNonWizardDialogs<=1)
			setVisible(true);
	}
	long lastSecondary = 0;
	/*.................................................................................................................*/
	public void setSecondaryMessage (String s) {
		if (buttonWasHit && progressIndicator  != null && progressIndicator.buttonMode == 1)
			return;
		long c = System.currentTimeMillis();
		if (c-lastSecondary > textRefreshInterval) {
			lastSecondary = c;
			drawSecondaryMessage();
		}
	}
	/*.................................................................................................................*/
	public void setTertiaryMessage (String s) {
		if (buttonWasHit && progressIndicator  != null && progressIndicator.buttonMode == 1)
			return;
		drawTertiaryMessage();
	}
	/*.................................................................................................................*/
	public void spin () {
		currentValue = 0;
		totalValue = 0;
		progressBar.setTime(totalValue, currentValue);
		Graphics g = progressBar.getGraphics();
		if (g !=null) {
			progressBar.spin(g);
			g.dispose();
		}
		else
			progressBar.repaint();
	}
	/*.................................................................................................................*/
	public void setText (String s) {
		setText(s, false);  //default changed here so that doesn't bring to front by default
	}
	/*.................................................................................................................*/
	public void setCurrentValue (long current) {
		if (buttonWasHit && progressIndicator  != null && progressIndicator.buttonMode == 1)
			return;
		currentValue = current;
		progressBar.setTime(totalValue, currentValue);
		if (inWizard)
			resetSizes();
		progressBar.repaint();
		//	if (inWizard)
		//		repaint();
		//setVisible(true);
	}
	/*.................................................................................................................*/
	public long getCurrentValue () {
		return currentValue;
	}
	/*.................................................................................................................*/
	public void setCurrentAndText (long current, String s) {
		if (buttonWasHit && progressIndicator  != null && progressIndicator.buttonMode == 1)
			return;
		progressString = s;
		currentValue = current;
		drawProgressText();
		progressBar.setTime(totalValue, currentValue);
		if (inWizard)
			resetSizes();
		progressBar.repaint();
		//	if (inWizard)
		//		repaint();
		//setVisible(true);
	}
	/*.................................................................................................................*/
	public void setTotalValue (long total) {
		if (buttonWasHit && progressIndicator  != null && progressIndicator.buttonMode == 1)
			return;
		totalValue = total;
		progressBar.setTime(totalValue, currentValue);
	}
	/*.................................................................................................................*/
	public long getTotalValue () {
		return totalValue;
	}
}


