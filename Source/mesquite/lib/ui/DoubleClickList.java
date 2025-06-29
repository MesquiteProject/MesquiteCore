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
import java.awt.List;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Method;

import javax.accessibility.AccessibleContext;

import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteTrunk;


/*===============================================*/
/** An list that records if it has been double-clicked */
public class DoubleClickList extends List implements MouseListener {
	boolean doubleClick=false;
	DoubleClickListener obj;
	boolean forceSize = true;
	boolean acceptsDoubleClicks = !MesquiteTrunk.isMacOSXAfterJaguarRunning33();
	
	/*.................................................................................................................*/
	public DoubleClickList (int numRows, boolean multipleMode) {
		super(numRows, multipleMode);
		addMouseListener(this);
	}
	/*.................................................................................................................*/
	public DoubleClickList (int numRows) {
		super(numRows);
		addMouseListener(this);
	}
	/*.................................................................................................................*/
	public DoubleClickList () {
		super();
		addMouseListener(this);
	}
	

	/*...............................................................................................................*/
	public void setEnableDoubleClicks(boolean enabled) {
		if (MesquiteTrunk.isMacOSXAfterJaguarRunning33())
			acceptsDoubleClicks= false;
		else
			acceptsDoubleClicks= enabled;
	}
	/*...............................................................................................................*/
	public boolean getAcceptsDoubleClicks() {
		return acceptsDoubleClicks;
	}
	/*...............................................................................................................*/
	public boolean getDoubleClick() {
		return doubleClick;
	}
	/*...............................................................................................................*/
	public void setDoubleClickListener(DoubleClickListener obj) {
		this.obj = obj;
	}
	/*...............................................................................................................*/
	int minWidth = 360;
	public void setMinimumWidth(int width) {
		this.minWidth = width;
	}
	/*...............................................................................................................*/
	public Dimension getPreferredSize() {
		if (forceSize) {
			int width = (int)super.getPreferredSize().width;
			return new Dimension(MesquiteInteger.maximum(width,minWidth), (int)super.getPreferredSize().height);
		}
		else
			return super.getPreferredSize();
		
	}
	
	/*...............................................................................................................*/
	Method findSelectAllMethod(AccessibleContext context){
		Method[] methods = context.getClass().getMethods();
		for (int q = 0; q<methods.length; q++) {
			if (methods[q].getName().equalsIgnoreCase("selectAllAccessibleSelection"))
				return methods[q];
		}
		return null;
	}
	/*...............................................................................................................*/
	//this doesn't seem to work :-(
	public boolean selectAll(){
		AccessibleContext context = getAccessibleContext();
		try {
			Method method = findSelectAllMethod(context);
			if (method != null)
				method.invoke(context);
			return true;
		}
		catch(Exception e){
			System.err.println("Exception in DoubleClickList: " + e);
		}
		return false;
	}

	/*...............................................................................................................*/
	public void setForceSize(boolean forceSize) {
		this.forceSize = forceSize;
	}
 	/*...............................................................................................................*/
	public void  mousePressed(MouseEvent e)   {
		if (e.getClickCount()>1 && acceptsDoubleClicks) {
			doubleClick=true;
			if (obj!=null)
				obj.doubleClicked(this);
		}
		else {
			doubleClick=false;
		}
	}
	/*...............................................................................................................*/
	 public void mouseClicked(MouseEvent e) {}
	/*...............................................................................................................*/
	 public void mouseReleased(MouseEvent e) {}
	/*...............................................................................................................*/
	 public void mouseEntered(MouseEvent e) {}
	/*...............................................................................................................*/
	 public void mouseExited(MouseEvent e) {}
	/**/

}

