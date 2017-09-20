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

import java.awt.*;
import java.util.*;

import javax.swing.*;

/* =============================================== */
/** A dialog box */
public class MesquiteDialogParent extends JDialog implements Identifiable {
	LayoutManager layout;
	Component current;
	boolean enlargeOnly = false;
	MDPShowThread showThread;
	MDPHelperThread helperThread;
	boolean isWizard = false;
	WizardInfoPanel infoPanel;
	public static int infoWidth = 200;
	MesquiteDialog currentDialog;
	public static long totalCreated = 0;
	long id;
	public static long totalDisposed = 0;
	public static long totalFinalized  = 0;
	public WaitPanel waitPanel;
	Vector dialogsToAdd = new Vector();
	Vector dialogsToRemove = new Vector();
	boolean hiddenForCalculation = false;
	public MesquiteDialogParent(Frame f, String title, boolean b) {
		super(f, title, b);
		//setLocation(0, 0);
		id = totalCreated++;
		
		getContentPane().setLayout(layout = new CardLayout());
		setResizable(false);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setForeground(Color.black);
		showThread = new MDPShowThread(this);
		showThread.start();
		helperThread = new MDPHelperThread(this);
		helperThread.start();
		waitPanel = new WaitPanel(this);
		waitPanel.setBackground(ColorTheme.getInterfaceBackground());

		MesquiteWindow.centerWindow(this);
	}
	public Container getContentPane(){
		if (MesquiteWindow.headless)
			return new Panel();
		return super.getContentPane();
	}
	public long getID(){
		return id;
	}
	MesquiteModule initiatingModule;
	public MesquiteModule setInitiatingModuleIfNeeded(MesquiteModule w){
		if (initiatingModule == null)
			initiatingModule = w;
		return initiatingModule ;
	}
	public MesquiteModule getInitiatingModule(){
		return initiatingModule ;
	}
	/*-------------------------------------------------------*/
	public void finalize() throws Throwable {
		totalFinalized++;
		super.finalize();
	}
	public void pleaseDispose(){
		if (helperThread != null)
		helperThread.pleaseDispose = true;

	}
	public void pleaseShow(){
		if (showThread != null)
		showThread.pleaseShow = true;
	}
	public void pleaseBringToFront(){
		if (helperThread != null)
		helperThread.pleaseBringToFront = true;
	}
	public void toFront(){
		if (!hiddenForCalculation)
			super.toFront();
	}
	public void pleaseRequestFocus(){
		if (helperThread != null)
		helperThread.pleaseRequestFocus = true;
	}
	boolean alreadyDisposed = false;
	
	boolean inArray(Component[] components, Component c){
		for (int i=0; i< components.length; i++)
			if (c == components[i])
				return true;
		return false;
	}
	void hideAll(Component c){
		c.setVisible(false);
		if (c instanceof Container){
			Component[] components = ((Container)c).getComponents();
			for (int i=0; i<components.length; i++)
				hideAll(components[i]);
		}
	}
	public void removeDialog(MesquiteDialog d){
		dialogsToRemove.addElement(d);
		removeOldest();
		//dialogsToRemove.addElement(d);
		//helperThread.pleaseRemoveDialog = true;
		pleaseRequestFocus();
	}
	public void removeOldest(){
		if (dialogsToRemove.size()==0)
			return;
		MesquiteDialog dlog = (MesquiteDialog)dialogsToRemove.elementAt(0);
		dialogsToRemove.removeElement(dlog);
		Component c = dlog.outerContents;
		remove(c);
		Component[] components = getComponents();
		hideAll(c);
		if (c instanceof Container)
			((Container)c).removeAll();

//		components = getComponents();
		if (!inArray(components, waitPanel)){
			if (layout instanceof BorderLayout)
				super.getContentPane().add(waitPanel,BorderLayout.CENTER);  //LINE_END
			else
				super.getContentPane().add(waitPanel);
		}
		waitPanel.setVisible(true);
		current = waitPanel;
		currentDialog = null;
		resetInfo();
		resetSizes();
		doLayout();
		waitPanel.repaint();
	}
	public void dispose(){
		if (alreadyDisposed) {
			return;
		}
		alreadyDisposed = true;
		
		if (isWizard) 
			remove(infoPanel);
		if (this == MesquiteDialog.currentWizard)
			MesquiteDialog.currentWizard = null;
		removeAll();
		showThread.go = false;
		helperThread.go = false;
		super.setVisible(false);
		totalDisposed++;
		
		super.dispose();
	}
	public void setVisible(boolean b){
		if (alreadyDisposed)
			return;
		if (b)
			hiddenForCalculation = false;
		if (!b && this == MesquiteDialog.currentWizard)
			MesquiteDialog.currentWizard = null;
		if (b && isWizard)
			MesquiteDialog.currentWizard = this;
		if (b) {
			pleaseBringToFront();  //This is a workaround to the problem of a more recent dialog box dropping behind an older one.  PUts on different thread.
		super.setVisible(b);
		}
	}

	public void hideForCalculation(){
		if (alreadyDisposed)
			return;
		hiddenForCalculation = true;
		super.setVisible(false);
	}
	public Dimension getPreferredSize(boolean contentsOnly){
		if (isWizard){
			if (contentsOnly && currentDialog!= null && currentDialog.outerContents != null) {
				Dimension ps =  currentDialog.outerContents.getPreferredSize(true);
				return ps;
			}
			else
				return new Dimension(MesquiteDialog.wizardWidth, MesquiteDialog.wizardHeight);
		}
		return super.getPreferredSize();
	}
	public Dimension getPreferredSize(){
		if (isWizard)
			return new Dimension(MesquiteDialog.wizardWidth, MesquiteDialog.wizardHeight);
		return super.getPreferredSize();
	}
	void addNext(){
		if (dialogsToAdd.size()==0)
			return;
		MesquiteDialog dlog = (MesquiteDialog)dialogsToAdd.elementAt(0);
		dialogsToAdd.removeElement(dlog);
		Component c = dlog.outerContents;
		String s = Integer.toString(dlog.id);
		if (alreadyDisposed)
			return;
		if (layout instanceof BorderLayout) {
			super.remove(waitPanel);
			getContentPane().add(c,BorderLayout.CENTER); //LINE_END
		}
		else{
			getContentPane().add(c, s);
			//((CardLayout)layout).show(this, s);
		}
		if (current != null)
			remove(current);
		current = c;
		
		currentDialog = dlog;
		//	pleaseRequestFocus();

		//&*	c.requestFocusInWindow();
		resetSizes();

	}
	public void add(MesquiteDialog dlog){
		dialogsToAdd.addElement(dlog);
		addNext();
		//helperThread.pleaseAddDialog = true;

	}
	public MesquiteDialog getCurrentDialog(){
		return currentDialog;
	}
	public void setAsWizard(){

		getContentPane().setLayout(layout = new BorderLayout());

		if (infoPanel == null){
			infoPanel = new WizardInfoPanel(this);
			infoPanel.setSize(infoWidth, getBounds().height);
			infoPanel.setVisible(true);
			getContentPane().add(infoPanel, BorderLayout.WEST); //LINE_START
		}
		resetSizes();
		enlargeOnly = true;
		isWizard = true;
		if (isVisible())
			MesquiteDialog.currentWizard = this;
	}
	public boolean isWizard(){
		return isWizard;
	}
	void resetSizes(){
		if (alreadyDisposed)
			return;
		if (layout instanceof CardLayout){
			if (helperThread != null)
				helperThread.pleaseResetInfo = true;
			return;
		}
		Insets insets = getInsets();
		int netHeight = getBounds().height - insets.top - insets.bottom;
		int netWidth = getBounds().width - insets.left - insets.right;
		 if (infoPanel != null)
				infoPanel.setSize(infoWidth, netHeight);
			if (current != null) {
				current.setSize(netWidth - infoWidth, netHeight);
			}
			
		
		if (helperThread != null)
			helperThread.pleaseResetInfo = true;
	}
	void resetInfo(){
		if (alreadyDisposed)
			return;
		try {
			if (currentDialog != null && currentDialog instanceof ExtensibleDialog){
				String st = ((ExtensibleDialog)currentDialog).getHelpString();
				if (st != null && infoPanel != null)
					infoPanel.setText(st);
			}
			else 
				infoPanel.setText("<html><body bgcolor=\"#DEB887\"></body></html>");
		}
		catch (Throwable e){

		}
		try {
		if (current != null){
			current.doLayout();
			current.invalidate();
			current.validate();
		}
		}
		catch (Throwable e){

		}

	}
	public void pack(){
		if (alreadyDisposed)
			return;
		if (isWizard){
			resetSizes();
			super.pack();
			invalidate();
			validate();
			doLayout();
			MesquiteWindow.rpAll(current);
			resetSizes();
		}
		else
			super.pack();
	}

	public void setSize(Dimension d){
		if (alreadyDisposed)
			return;
		if (enlargeOnly){
			if (d.width < getWidth())
				d.width = getWidth();
			if (d.height < getHeight())
				d.height = getHeight();
		}
		super.setSize(d);
		resetSizes();

	}
	public void setSize(int w, int h){
		if (alreadyDisposed)
			return;
		if (enlargeOnly){
			if (w < getWidth())
				w = getWidth();
			if (h < getHeight())
				h = getHeight();
		}
		super.setSize(w, h);
		resetSizes();

	}
	public void setBounds(int x, int y, int w, int h){
		if (alreadyDisposed)
			return;
		if (enlargeOnly){
			if (w < getWidth())
				w = getWidth();
			if (h < getHeight())
				h = getHeight();
		}
//		if (isWizard && locOnceSet)
		super.setBounds(x, y, w, h);
		resetSizes();
	}
	public void setLocation(int x, int y){
		if (alreadyDisposed)
			return;
		super.setLocation(x, y);
	}
}

class MDPShowThread extends Thread {
	MesquiteDialogParent parent;
	boolean pleaseShow = false;
	int count = 0;
	boolean go;
	public MDPShowThread(MesquiteDialogParent parent){
		this.parent = parent;
	}
	public void run(){
		go = true;
		try{
			while (go  && !MesquiteTrunk.mesquiteTrunk.mesquiteExiting){
				Thread.sleep(50);
				if (pleaseShow){

					pleaseShow = false;
					//go = false;
					parent.setVisible(true);
				}
			}
		}
		catch (InterruptedException e){
			MesquiteFile.throwableToLog(null, e);
		}
	}
}

class MDPHelperThread extends Thread {
	MesquiteDialogParent parent;
	boolean pleaseDispose = false;
	boolean pleaseResetInfo = false;
	boolean pleaseBringToFront = false;
	boolean pleaseRequestFocus = false;
	boolean pleaseRemoveDialog = false;
	boolean pleaseAddDialog = false;
	boolean disposed = false;
	boolean go;
	int count = 0;
	public MDPHelperThread(MesquiteDialogParent parent){
		this.parent = parent;
	}
	public void run(){
		go = true;
		try{
			while (go && !MesquiteTrunk.mesquiteTrunk.mesquiteExiting){
				Thread.sleep(50);
				if (pleaseResetInfo){
					parent.resetInfo();
					pleaseResetInfo = false;
				}
				if (pleaseBringToFront){
					parent.toFront();
					pleaseBringToFront = false;
				}
				if (pleaseRequestFocus){
					parent.requestFocus();
					pleaseRequestFocus = false;
				}
				if (pleaseRemoveDialog){
					parent.removeOldest();
					pleaseRemoveDialog = false;
				}
				if (pleaseAddDialog){
					parent.addNext();
					pleaseAddDialog = false;
				}
				if (pleaseDispose){
					parent.dispose();
					disposed = true;
					pleaseDispose = false;
					go = false;
				}
			}
		}
		catch (InterruptedException e){
		}
		if (!disposed)
			parent.dispose();
	}
}

class WizardInfoPanel extends MesquitePanel {
	MesquiteDialogParent parent;
	JEditorPane text;
	public WizardInfoPanel (MesquiteDialogParent parent){
		this.parent = parent;
		setLayout(null);
		text = new MesqJEditorPane("text/html","<html><body bgcolor=\"#DEB887\"></body></html>");
		add(text);
		text.setVisible(true);
		setBackground(ColorDistribution.mesquiteBrown);
		//text.setBounds(0, 0, 50, 50);
	}
	public void setText(String s){
		try{
			text.setText("<html><body bgcolor=\"#DEB887\">" + s + "</body></html>");	
	 	}
		catch(Exception e){
			try{
				text.setText("<html><body bgcolor=\"#DEB887\"></body></html>");	
		 	}
			catch(Exception e2){
			}
		}
		
	}
	public void setSize(int w, int h){
		super.setSize(w, h);
		text.setBounds(4, 4, w-8, h-8);
	}
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x, y, w, h);
		text.setBounds(4, 4, w-8, h-8);
	}
	public Dimension getPreferredSize(){
		return new Dimension(parent.infoWidth, MesquiteDialog.wizardHeight);
	}

}
class WaitPanel extends MesquitePanel {
	MesquiteDialogParent parent;
//	public ProgressPanel progressPanel;
	public boolean showWait = true;
	public WaitPanel (MesquiteDialogParent parent){
		this.parent = parent;
		setLayout(null);
		//	progressPanel = new ProgressPanel(null, null, null, 0, null);
		//	add(progressPanel);
		//	progressPanel.setBounds(0,0,0,0);
	}
	public void paint(Graphics g){
		//	Component[] c = getComponents();
		if (showWait)
			g.drawString("Please Wait...", getBounds().width/2-40, getBounds().height/3);
	}
}

