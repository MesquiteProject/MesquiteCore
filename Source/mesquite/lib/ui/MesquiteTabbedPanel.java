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

public class MesquiteTabbedPanel extends JPanel implements MQComponent  {
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
		JPanel panel = new JPanel();
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
	/*getPreferredSize -------------------------*/
    public Dimension getPreferredSize() {
		if (MesquiteTrunk.isLinux()) {
			try {
				return super.getPreferredSize();
			}
			catch (StackOverflowError e) {
				System.err.println("Yet another StackOverflowError on  linux");
			}
		}
		try {
			return super.getPreferredSize();
		}
		catch (Exception e) {
			System.err.println("Exception in " + getClass() + " (" + e.getClass() + ")"); //Debugg.println if (MesquiteTrunk.debugMode) 
		}
		return new Dimension(400, 400);
	}
	/*validate -------------------------*/
	boolean validating = false;
	public void validate(){
		if (MesquiteTrunk.isLinux() && MesquiteTrunk.linuxGWAThread!=null)
			MesquiteTrunk.linuxGWAThread.validateRequested(this);
		else {
			if (validating)
				Debugg.printStackTrace("Double validating " + this);
			validating = true;
			super.validate();
			validating = false;
		}
	}
	public void pleaseValidate(){
		if (validating)
			Debugg.printStackTrace("Double validating (PV) " + this);
		validating = true;
		super.validate();
		validating = false;
	}

	
	/*setBounds -------------------------*/
	//This is currently bypassed (see linxuGWAThread) and may not be needed; left here in case further testing shows this protection is needed also. See ExplTextArea also
	public void setBounds(int x, int y, int w, int h){
		if (MesquiteTrunk.isLinux() && MesquiteTrunk.linuxGWAThread!=null)
			MesquiteTrunk.linuxGWAThread.setBoundsRequested(this, x, y, w, h);
		else
			super.setBounds(x, y, w, h);
	}
	public void pleaseSetBounds(int x, int y, int w, int h){
		super.setBounds(x, y, w, h);
	}
	/*s----- -------------------------*/

}
