/* Mesquite source code.  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

import mesquite.lib.Listable;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTrunk;

/** a thermometer window (see class ProgressIndicator)*/
public class ProgressWindow extends Frame implements Listable, WindowListener, ComponentListener, MQComponent {
	public ProgressPanel progressPanel;
	long textRefreshInterval = 500;
	public static int visibleProgWindows=0;
	int defaultDialogHeight = 130;
	int defaultDialogWidth = 380;
	int dialogHeight = defaultDialogHeight;
	int dialogWidth = defaultDialogWidth;
	int detailsExtraHeight = 220;
	boolean showDetails = false;
	ProgressIndicator progressIndicator;
	private boolean hidden = false;
	private boolean disposed = false;
	Font textFont;
	public long doomTicks = 0;
	boolean inWizard = false;
	boolean useWizard = false;
	public static Vector allIndicators;
	static {
		allIndicators = new Vector();
	}
	public ProgressWindow (ProgressIndicator progressIndicator, String title, String initialMessage, long total, String buttonName) {
		super(title);
		setLayout(null);
		this.progressIndicator=progressIndicator;
		progressPanel = new ProgressPanel(progressIndicator, this, title,  initialMessage,  total,  buttonName);
		if (useWizard && MesquiteDialog.currentWizard != null){  //not used
			inWizard = true;
			MesquiteDialog.currentWizard.waitPanel.showWait = false;
			MesquiteDialog.currentWizard.waitPanel.repaint();
			MesquiteDialog.currentWizard.waitPanel.add(progressPanel);
			MesquiteDialog.currentWizard.pleaseRequestFocus();
		}
		else 
			add(progressPanel);
		if (MesquiteTrunk.isLinux())
			progressPanel.setLocation(0, 30);
		dialogWidth = progressIndicator.width;
		dialogHeight = progressIndicator.height;
		if (showDetails)
			dialogHeight += detailsExtraHeight;
	//	setBackground(ColorDistribution.paleGoldenRod); //.light[0]); //ggray
		setBackground(ColorTheme.getInterfaceBackgroundPale()); //.light[0]); //ggray


		setTotalValue(total);


		setSize(dialogWidth, dialogHeight);
		progressPanel.setSize(dialogWidth, dialogHeight);
		if (inWizard){
			progressPanel.setLocation(100, 100);
			//progressPanel.setBackground(Color.green);
			progressPanel.setVisible(true);
			MesquiteDialog.currentWizard.pleaseRequestFocus();
		}
		addWindowListener(this);

		this.setResizable(false);
		MesquiteWindow.centerWindowTile(this,visibleProgWindows);
	}
	public static void allIndicatorsToFront(){
		for (int i = 0; i< allIndicators.size(); i++){
			ProgressWindow w = (ProgressWindow)allIndicators.elementAt(i);
			w.toFront();
		}
	}
	public boolean isSpontaneous(){
		return progressIndicator.getIsFromWatcher();
	}
	public void show(){
		if (MesquiteWindow.suppressAllWindows)
			return;
		if (progressIndicator.dontStart)
			return;
		hidden = false;
		if (!inWizard)
			super.show();
		else {
			progressPanel.setVisible(true);
			MesquiteDialog.currentWizard.pleaseRequestFocus();
		}
		registerProgressWindow(true);

	}
	boolean countedInTotal = false;
	private void registerProgressWindow(boolean r){
		if (r){
			if (allIndicators.indexOf(this)<0){
			if (!inWizard)
				ProgressWindow.visibleProgWindows++;
				allIndicators.addElement(this);
			countedInTotal = true;
			}
		}
		else {
			if (allIndicators.indexOf(this)>=0){
				if (!inWizard)
			ProgressWindow.visibleProgWindows--;
			allIndicators.removeElement(this);
			countedInTotal = false;
			}
		}
	}
	public void setVisible(boolean vis){
		if (!MesquiteWindow.GUIavailable || MesquiteWindow.suppressAllWindows)
			return;
		if (vis && (MesquiteThread.getSuppressAllProgressIndicators(Thread.currentThread())) || MesquiteThread.getSuppressAllProgressIndicators(progressIndicator.ownerThread))
			return;


		if (vis)
			hidden = false;
		//if (vis && !isVisible())
		//	pack();
		registerProgressWindow(vis);
		if (!inWizard){
			super.setVisible(vis);
		}
		else if (!vis){
			progressPanel.setVisible(vis);
			MesquiteDialog.currentWizard.waitPanel.showWait = true;
		}
		else {
			progressPanel.setVisible(vis);
			MesquiteDialog.currentWizard.pleaseRequestFocus();
		}
	}
	public String getName(){
		return getTitle();// + " id " + getID();
	}
	public void hide(){
		if (!hidden){
			super.hide();
			if (inWizard){
				MesquiteDialog.currentWizard.waitPanel.showWait = true;
			}
			registerProgressWindow(false);

			hidden = true;
		}
	}

	public void dispose(){

		if (!disposed){
			removeAll();
			if (inWizard){
				progressPanel.removeAll();
				//progressPanel.setVisible(false);
			}
			disposed = true;
			super.dispose();
			registerProgressWindow(false);

		}


	}

	public void setStopButtonName(String buttonName){
		progressPanel.setStopButtonName(buttonName);
	}
	public String getStopButtonName(){
		return 		progressPanel.getStopButtonName();

	}
	
	/*.................................................................................................................*/
	public void componentResized(ComponentEvent e){
		if (getBounds().height !=dialogHeight || getBounds().width != dialogWidth){
			setSize(dialogWidth, dialogHeight);
			progressPanel.setSize(dialogWidth, dialogHeight);
		}
	}
	public void componentMoved(ComponentEvent e){
	}
	public void componentHidden(ComponentEvent e){
	}
	public void componentShown(ComponentEvent e){

		Toolkit.getDefaultToolkit().sync();
	}

	/*.................................................................................................................*/
	public void windowActivated(WindowEvent e) {
	}
	/*.................................................................................................................*/
	public void windowClosed(WindowEvent e) {
	}
	/*.................................................................................................................*/
	public void windowClosing(WindowEvent e) {
		progressPanel.windowClosing();
	}
	/*.................................................................................................................*/
	public void windowDeactivated(WindowEvent e) {
	}
	/*.................................................................................................................*/
	public void windowDeiconified(WindowEvent e) {
	}
	/*.................................................................................................................*/
	public void windowIconified(WindowEvent e) {
	}
	/*.................................................................................................................*/
	public void windowOpened(WindowEvent e) {
	}

	public void setButtonsVisible(boolean vis){
		if (progressPanel == null || progressPanel.buttons == null)
			return;
		progressPanel.buttons.setVisible(vis);
	}
	public boolean wasButtonHit(){
		return progressPanel.buttonWasHit;
	}
	/*.................................................................................................................*/
	public void setAbort(){
		progressPanel.setAbort();
	}

	/*.................................................................................................................*/
	public long getTextRefreshInterval(){
		return progressPanel.getTextRefreshInterval();
	}
	/*.................................................................................................................*/
	public void setTextRefreshInterval(long interval){
		progressPanel.setTextRefreshInterval(interval);
	}
	long lastText = 0;
	/*.................................................................................................................*/
	public void setText (String s) {
		setText(s, false);  //default changed here so that doesn't bring to front by default
	}
	/*.................................................................................................................*/
	public void setText (String s, boolean bringToFront) {
		progressPanel.setText(s, bringToFront);
	}
	/*.................................................................................................................*/
	public void setText (String s, boolean bringToFront, boolean immediately) {
		progressPanel.setText(s, bringToFront, immediately);
	}
	long lastSecondary = 0;
	/*.................................................................................................................*/
	public void setSecondaryMessage (String s) {
		progressPanel.setSecondaryMessage(s);
	}
	/*.................................................................................................................*/
	public void setTertiaryMessage (String s) {
		progressPanel.setTertiaryMessage(s);
	}
	/*.................................................................................................................*/
	public void spin () {
		progressPanel.spin();
	}
	/*.................................................................................................................*/
	public void setCurrentValue (long current) {
		progressPanel.setCurrentValue(current);
	}
	/*.................................................................................................................*/
	public long getCurrentValue () {
		return progressPanel.getCurrentValue();
	}
	/*.................................................................................................................*/
	public void setCurrentAndText (long current, String s) {
		progressPanel.setCurrentAndText(current, s);
	}
	/*.................................................................................................................*/
	public void setTotalValue (long total) {
		progressPanel.setTotalValue(total);
	}
	/*.................................................................................................................*/
	public long getTotalValue () {
		return progressPanel.getTotalValue();
	}
	
	//###########################################################
	/*################################################################
	 *  The following overrides were built to avoid the frequent StackOverflowErrors on Linux Java post-1.8, 
	 *  but were extended in part to other OSs. See also others satisfying MQComponent interface.
	 */		
	MQComponentHelper helper = new MQComponentHelper(this);
	public MQComponentHelper getHelper(){
		return helper;
	}
	public void superValidate(){
		super.validate();
	}
	public void superSetBounds(int x, int y, int w, int h){
		super.setBounds(x,y,w,h);
	}
	public void superSetFont (Font f){
	super.setFont(f);
	}
	public void superSetSize (int w, int h){
		super.setSize(w,h);
	}
	public void superSetLocation (int x, int y){
		super.setLocation(x,y);
	}
	public Dimension superGetPreferredSize(){
		return super.getPreferredSize();
	}
	public void superLayout(){
		super.layout();
	}
	public void superInvalidate(){
		super.invalidate();
	}
	/* - - - - - - */
	public void invalidate (){
		if (helper == null)
			superInvalidate();
		else
			helper.invalidate();
	}
	public void setFont (Font f){
		if (helper == null)
			superSetFont(f);
		else
			helper.setFont(f);
	}
	public void setSize (int w, int h){
		if (helper == null)
			superSetSize(w,h);
		else
			helper.setSize(w, h);
	}
	public void setLocation (int x, int y){
		if (helper == null)
			superSetLocation(x, y);
		else
			helper.setLocation(x,y);
	}
	public Dimension getPreferredSize() {
		if (helper == null)
			return superGetPreferredSize();
		else
			return helper.getPreferredSize();
	}
	public void layout(){
		if (helper == null)
			superLayout();
		else
			helper.layout();
	}
	public void validate(){
		if (helper == null)
			superValidate();
		else
			helper.validate();
	}
	public void setBounds(int x, int y, int w, int h){
		if (helper == null)
			superSetBounds(x,y,w,h);
		else
			helper.setBounds(x,y,w,h);
	}
	/*###########################################################*/
	//###########################################################
}


