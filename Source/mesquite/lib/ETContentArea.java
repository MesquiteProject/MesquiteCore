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
import mesquite.lib.duties.*;
/* ======================================================================== */
/** The ContentArea specifically to show the tree of EmployeeModules.*/
class ETContentArea extends ContentArea {
	MesquiteModule focalModule;
	HPanel browser;
	public ETContentArea(MesquiteModule focalModule){
		super(null);
		mainPanel.setLayout(new CardLayout());
		
		this.focalModule = focalModule;
		if (focalModule!=null){
			browser = focalModule.getBrowserPanel();
			if (browser !=null){
				browser.setTitle("Employees of \"" + focalModule.getName() + "\"");
				add(browser, "browser");
				browser.setVisible(true);
				browser.setBackground(ColorDistribution.lightYellow);
				browser.setRootNode(focalModule);
				browser.renew();
			}
		}
	}
	/** Gets the browser panel used to show the tree of employee modules */
	public HPanel getBrowser(){
		return browser;
	}
	public void setSize(int w, int h){
		super.setSize(w,h);
		if (browser !=null)
			browser.setSize(w,h);
	}
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x, y, w,h);
		if (browser !=null)
			browser.setSize(w,h);
	}

	public void dispose(){
		focalModule=null;
		if (browser !=null)
			browser.dispose();
	}
}

