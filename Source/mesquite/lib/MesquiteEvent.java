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
import java.awt.event.*;
import java.text.*;

/* ======================================================================== */
/**This class is used to look after events*/
public class MesquiteEvent {  //DRM

	public static int getModifiers(InputEvent e){
		int mod = e.getModifiers();
		if (e.isShiftDown())
			mod |= InputEvent.SHIFT_MASK;
		if (e.isAltDown())
			mod |= InputEvent.ALT_DOWN_MASK;
		if (e.isControlDown())
			mod |= InputEvent.CTRL_MASK;
		if (e.isMetaDown())
			mod |= InputEvent.META_MASK;
		return mod;
	}
	public static boolean shiftKeyDown(int modifiers) {
		return ((modifiers & InputEvent.SHIFT_MASK)!=0);
	}
	public static boolean optionKeyDown(int modifiers) {
		return ((modifiers & InputEvent.ALT_DOWN_MASK)!=0);
	}
	public static boolean controlKeyDown(int modifiers) {
		return ((modifiers & InputEvent.CTRL_MASK)!=0);
	}
	public static boolean rightClick(int modifiers) {
		return ((modifiers & InputEvent.BUTTON3_MASK)!=0) || ((modifiers & InputEvent.BUTTON3_DOWN_MASK)!=0);
	//	return ((modifiers & InputEvent.CTRL_MASK)!=0) || ((modifiers & InputEvent.BUTTON2_DOWN_MASK)!=0) || ((modifiers & Event.META_MASK)!=0);
	}
	public static boolean commandKeyDown(int modifiers) {
		return ((modifiers & InputEvent.META_MASK)!=0);
	}
	public static boolean commandOrControlKeyDown(int modifiers) {
		return ((modifiers & InputEvent.META_MASK)!=0 || (modifiers & InputEvent.CTRL_MASK)!=0);
	}
	public static String modifiersToString(int modifiers){
			String s = "";
			if (shiftKeyDown(modifiers))
				s+= " shift";
			if (controlKeyDown(modifiers))
				s+= " control";
			else if (optionKeyDown(modifiers)) //else is here because of discord between Sun and Apple styles & Sun's overloading BUTTON2 and ALT.  Rule in Mesquite is that control option always behaves like control.  Right click therefore varies
				s+= " option";
			if (commandKeyDown(modifiers))
				s+= " command";
			 return s;
	}
}



