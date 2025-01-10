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

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import mesquite.lib.Debugg;
import mesquite.lib.MesquiteTrunk;

import java.awt.*;


public class MesquiteTabbedPane extends JTabbedPane implements MQComponent {
	MesquiteTabbedPanel panel;
	public MesquiteTabbedPane(MesquiteTabbedPanel panel){
		super();
		this.panel = panel;
	}
	
	int count = 0;
	
	public void setSelectedIndex2(int i){
	
		 int current = getSelectedIndex();
		if (current>=0) {
			JPanel p2 = panel.getTabPanel(current);
			if (p2 != null)
				p2.setVisible(false);
		}

		super.setSelectedIndex(i);


		JPanel p = panel.getTabPanel(i);
		//	setVisible(true);
		if (p != null) {
			p.setVisible(true);
			Graphics g = p.getGraphics();
			if (g!=null)
				g.setClip(null);
			p.invalidate();
		}
		invalidate();
		//	try {Thread.sleep(20);} catch (Exception e) {}
	//	super.setSelectedIndex(i);
	}

	/*layout -------------------------*/
	public void layout(){
		if (MesquiteTrunk.isLinux()) {
			try {
				super.layout();
			}
			catch (StackOverflowError e) {
				System.out.println("Yet another StackOverflowError on  linux");
			}
		}
		else {
			super.layout();
		}
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
		try {
			super.validate();
		}
		catch (StackOverflowError e) {
			System.out.println("Yet another StackOverflowError on  linux");
			
		}
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
