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

import java.awt.Cursor;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import mesquite.lib.system.SystemUtil;

/* ��������������������������� commands ������������������������������� */
/* includes commands,  buttons, miniscrolls
/* ======================================================================== */
/* scrollbar for tree */
public abstract class MesquiteScrollbar extends MQScrollbar implements AdjustmentListener {
	public MesquiteScrollbar ( int orientation, int value, int visible, int min, int max){
		super(orientation, value, visible, min, max);
		addAdjustmentListener(this);
		SystemUtil.setFocusable(this, false);
		setCursor(Cursor.getDefaultCursor());
	}
	public void adjustmentValueChanged(AdjustmentEvent e) {
		//Event queue
		if (processDuringAdjustment() || !e.getValueIsAdjusting())
			scrollTouched();
	}
	public abstract void scrollTouched();
	
	public boolean processDuringAdjustment() {
		return true;
	}


}


