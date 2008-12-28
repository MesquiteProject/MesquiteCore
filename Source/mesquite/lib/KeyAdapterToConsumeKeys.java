/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison.
Version 2.6, January 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
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

