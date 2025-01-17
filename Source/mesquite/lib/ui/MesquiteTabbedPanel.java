/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.ui;

import java.awt.*;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import mesquite.lib.Debugg;
import mesquite.lib.MesquiteTrunk;

public class MesquiteTabbedPanel extends MQJPanel implements MQComponent  {
	MesquiteTabbedPane tabbedPane;
	int numPanels=0;
	ExtensibleDialog dialog;
	
	public MesquiteTabbedPanel (ExtensibleDialog dialog) {
		super(new GridLayout(1,1));
		this.dialog = dialog;
		tabbedPane = new MesquiteTabbedPane(this);
		add(tabbedPane);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	}
	public JPanel addPanel(String title, boolean setAsAddPanel){
		JPanel panel = new MQJPanel();
		tabbedPane.addTab(title, panel);
		//panel.add(new Checkbox("testing panel " + numPanels));
		panel.setVisible(false);
		if (setAsAddPanel && dialog!=null)
			dialog.setAddJPanel(panel);
		numPanels++;
		return panel;
	}
	public JPanel getTabPanel(int i) {
		Component c = tabbedPane.getComponentAt(i);
		if (c instanceof JPanel) 
			return (JPanel)c;
		return null;
		
	}
	public void cleanup(){
		setVisible(true);
		tabbedPane.setSelectedIndex(0);
		tabbedPane.doLayout();
		tabbedPane.validate();
		if (dialog!=null)
			dialog.pack();
	}
	/*################################
	 *  The following overrides were built to handle (hide) the frequent StackOverflowErrors on Linux Java post-1.8, 
	 *  but were extended in part to other OSs
	 */

	/*getPreferredSize -------------------------*/
	public Dimension getPreferredSize() {
		try {
			return super.getPreferredSize();
		}
		catch (Exception e) {
			if (MesquiteTrunk.developmentMode)
				System.err.println("Exception in " + getClass() + " (" + e.getClass() + ") (getPreferredSize)"); 
		}
		catch (Error e) {
			if (MesquiteTrunk.developmentMode)
				System.err.println("Error in " + getClass() + " (" + e.getClass() + ") (getPreferredSize)"); 
		}
		return new Dimension(400, 400);
	}
	/*layout -------------------------*/
	public void layout(){
		try {
			super.layout();
		}
		catch (Exception e) {
			if (MesquiteTrunk.developmentMode)
				System.err.println("Exception in " + getClass() + " (" + e.getClass() + ") (layout)"); 
		}
		catch (Error e) {
			if (MesquiteTrunk.developmentMode)
				System.err.println("Error in " + getClass() + " (" + e.getClass() + ") (layout)"); 
		}
	}
	/*validate -------------------------*/
	boolean validating = false;
	public void validate(){
		if (MesquiteTrunk.isLinux()) { //seems to help on linux to put on separate thread
			if (MesquiteTrunk.linuxGWAThread!=null)
				MesquiteTrunk.linuxGWAThread.validateRequested(this);
		}
		else {
			try {
				super.validate();
			}
			catch (Exception e) {
				if (MesquiteTrunk.developmentMode)
					System.err.println("Exception in " + getClass() + " (" + e.getClass() + ") (validate)"); 
			}
			catch (Error e) {
				if (MesquiteTrunk.developmentMode)
					System.err.println("Error in " + getClass() + " (" + e.getClass() + ") (validate)"); 
			}
		}
	}

	public void pleaseValidate(){ //this will only be called on linux
		if (validating && MesquiteTrunk.developmentMode)
			System.err.println("Double validating " + this);
		validating = true;
		try {
			super.validate();
		}
		catch (Exception e) {
			if (MesquiteTrunk.developmentMode)
				System.err.println("Exception in " + getClass() + " (" + e.getClass() + ") (pleaseValidate)"); 
		}
		catch (Error e) {
			if (MesquiteTrunk.developmentMode)
				System.err.println("Error in " + getClass() + " (" + e.getClass() + ") (pleaseValidate)"); 
		}
		validating = false;
	}


	/*setBounds -------------------------*/
	public void setBounds(int x, int y, int w, int h){
		//This is currently bypassed (see linxuGWAThread) and may not be needed; 
		if (MesquiteTrunk.isLinux() && MesquiteTrunk.linuxGWAThread!=null)
			MesquiteTrunk.linuxGWAThread.setBoundsRequested(this, x, y, w, h);
		else {
			try {
				super.setBounds(x, y, w, h);
			}
			catch (Exception e) {
				if (MesquiteTrunk.developmentMode)
					System.err.println("Exception in " + getClass() + " (" + e.getClass() + ") (setBounds)"); 
			}
			catch (Error e) {
				if (MesquiteTrunk.developmentMode)
					System.err.println("Error in " + getClass() + " (" + e.getClass() + ") (setBounds)"); 
			}
		}
	}
	public void pleaseSetBounds(int x, int y, int w, int h){ //this will only be called on linux
		try {
			super.setBounds(x, y, w, h);
		}
		catch (Exception e) {
			if (MesquiteTrunk.developmentMode)
				System.err.println("Exception in " + getClass() + " (" + e.getClass() + ") (pleaseSetBounds)"); 
		}
		catch (Error e) {
			if (MesquiteTrunk.developmentMode)
				System.err.println("Error in " + getClass() + " (" + e.getClass() + ") (pleaseSetBounds)"); 
		}
	}
	/*################################*/

}
