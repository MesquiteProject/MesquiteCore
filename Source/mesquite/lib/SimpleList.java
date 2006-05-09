package mesquite.lib;

import java.awt.ScrollPane;

public class SimpleList extends ScrollPane {
	Listable listable;
	MousePanel homePanel;
	
	public SimpleList(Listable listable, MousePanel homePanel) {
		this.listable = listable;
		this.homePanel = homePanel;
	}
	
	

}
