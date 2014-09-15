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

import java.awt.Button;
import mesquite.lib.*;

public class TextCanvasWithButtons {
	Button button = null;
	Button button2 = null;
	MesquiteTextCanvas textCanvas = null;

	public  TextCanvasWithButtons() {
	}

	public Button getButton() {
		return button;
	}

	public void setButton(Button button) {
		this.button = button;
	}

	public Button getButton2() {
		return button2;
	}

	public void setButton2(Button button2) {
		this.button2 = button2;
	}

	public MesquiteTextCanvas getTextCanvas() {
		return textCanvas;
	}

	public void setTextCanvas(MesquiteTextCanvas textCanvas) {
		this.textCanvas = textCanvas;
	}
	

}
