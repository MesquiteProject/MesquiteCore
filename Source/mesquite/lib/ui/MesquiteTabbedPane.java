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
import java.awt.Graphics;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;


public class MesquiteTabbedPane extends JTabbedPane implements MQComponent {
	MesquiteTabbedPanel panel;
	public MesquiteTabbedPane(MesquiteTabbedPanel panel){
		super();
		this.panel = panel;
	}
	
	int count = 0;
	
	public void setSelectedIndex2(int i){
	
		 int current = getSelectedIndex();
		if (current>=0) {
			JPanel p2 = panel.getTabPanel(current);
			if (p2 != null)
				p2.setVisible(false);
		}

		super.setSelectedIndex(i);


		JPanel p = panel.getTabPanel(i);
		//	setVisible(true);
		if (p != null) {
			p.setVisible(true);
			Graphics g = p.getGraphics();
			if (g!=null)
				g.setClip(null);
			p.invalidate();
		}
		invalidate();
		//	try {Thread.sleep(20);} catch (Exception e) {}
	//	super.setSelectedIndex(i);
	}

	//###########################################################
	/*################################################################
	 *  The following overrides were built to avoid the frequent StackOverflowErrors on Linux Java post-1.8, 
	 *  but were extended in part to other OSs. See also others satisfying MQComponent interface.
	 */		
	MQComponentHelper helper = new MQComponentHelper(this);
	public MQComponentHelper getHelper(){
		return helper;
	}
	public void superValidate(){
		super.validate();
	}
	public void superSetBounds(int x, int y, int w, int h){
		super.setBounds(x,y,w,h);
	}
	public void superSetFont (Font f){
	super.setFont(f);
	}
	public void superSetSize (int w, int h){
		super.setSize(w,h);
	}
	public void superSetLocation (int x, int y){
		super.setLocation(x,y);
	}
	public Dimension superGetPreferredSize(){
		return super.getPreferredSize();
	}
	public void superLayout(){
		super.layout();
	}
	public void superInvalidate(){
		super.invalidate();
	}
	/* - - - - - - */
	public void invalidate (){
		if (helper == null)
			superInvalidate();
		else
			helper.invalidate();
	}

	public void setFont (Font f){
		if (helper == null)
			superSetFont(f);
		else
			helper.setFont(f);
	}
	public void setSize (int w, int h){
		if (helper == null)
			superSetSize(w,h);
		else
			helper.setSize(w, h);
	}
	public void setLocation (int x, int y){
		if (helper == null)
			superSetLocation(x, y);
		else
			helper.setLocation(x,y);
	}
	public Dimension getPreferredSize() {
		if (helper == null)
			return superGetPreferredSize();
		else
			return helper.getPreferredSize();
	}
	public void layout(){
		if (helper == null)
			superLayout();
		else
			helper.layout();
	}
	public void validate(){
		if (helper == null)
			superValidate();
		else
			helper.validate();
	}
	public void setBounds(int x, int y, int w, int h){
		if (helper == null)
			superSetBounds(x,y,w,h);
		else
			helper.setBounds(x,y,w,h);
	}
	/*###########################################################*/
	//###########################################################


}
