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

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class MesquiteTabbedPanel extends JPanel  {
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
	public void addPanel(String title, boolean setAsAddPanel){
		JPanel panel = new JPanel();
		tabbedPane.addTab(title, panel);
		//panel.add(new Checkbox("testing panel " + numPanels));
		panel.setVisible(false);
		if (setAsAddPanel && dialog!=null)
			dialog.setAddJPanel(panel);
		numPanels++;
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
}
