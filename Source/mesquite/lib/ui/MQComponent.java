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

import java.awt.Dimension;
import java.awt.Font;

import mesquite.lib.MesquiteTrunk;

/* �������������������� */
/*  [Search for MQLINUX] -- Intermediary class for workaround of StackOverflowError in Linux JDK 11 - 23 (at least!). 
 * These classes intercept validate and resize components on another thread in hopes of avoiding stack overflow error */
/* ======================================================================== */
public interface MQComponent {

	public static boolean verboseTW = false;

	public void superValidate();
	public void superInvalidate();
	public void superSetBounds(int x, int y, int w, int h);
	public void superSetFont (Font f);
	public void superSetSize (int w, int h);
	public void superSetLocation (int x, int y);
	public Dimension superGetPreferredSize();
	public void superLayout();
	public MQComponentHelper getHelper();
	
	/*
	Thread touchingFont = null;
	Thread touchingSize = null;
	Thread touchingLocation = null;
	Thread touchingDimension = null;
	Thread touchingLayout = null;
	boolean validating = false;
	Thread touchingValidate = null;
	Thread touchingPValidate = null;
	Thread touchingBounds = null;
	Thread touchingPSBounds = null;
	*/
}