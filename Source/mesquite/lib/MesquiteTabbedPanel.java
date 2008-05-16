package mesquite.lib;

import java.awt.Component;
import java.awt.GridLayout;

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
