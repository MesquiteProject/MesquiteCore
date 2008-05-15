package mesquite.lib;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;


public class MesquiteTabbedPane extends JTabbedPane {
	MesquiteTabbedPanel panel;
	public MesquiteTabbedPane(MesquiteTabbedPanel panel){
		super();
		this.panel = panel;
	}
	int count = 0;
	public void setSelectedIndex(int i){
		JPanel p = panel.getTabPanel(i);
		if (p != null)
			p.setVisible(false);
		super.setSelectedIndex(i);
	}
}
