package mesquite.lib;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

class KeyAdapterToConsumeKeys extends KeyAdapter {
	boolean allowReturn = false;
	
	public KeyAdapterToConsumeKeys (boolean allowReturn){
		super();
		this.allowReturn=allowReturn;
	}
	public void keyPressed(KeyEvent e){
		if (e.getKeyCode()== KeyEvent.VK_ENTER && !allowReturn) {
			e.consume();
		}
		else {  
		}

	}
}

