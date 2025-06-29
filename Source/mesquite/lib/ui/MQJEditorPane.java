/* Mesquite source code.  Copyright 2001 and onward, D. Maddison and W. Maddison. 


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

import javax.swing.JEditorPane;

//workaround for crashes on OS X && Linux  [Search for MQLINUX]
public class MQJEditorPane extends JEditorPane implements MQComponent{
	public MQJEditorPane(String a, String b){
		super(a, b);
		helper = new MQComponentHelper(this);
	}
	
	Dimension minSize = null;
	public void setMinSize(int w, int h){
		minSize = new Dimension(w, h);
	}
	public Dimension getMinimumSize(){
		if (minSize!= null)
			return minSize;
		return super.getMinimumSize();
	}
	public boolean getScrollableTracksViewportWidth() {
		try {
			return super.getScrollableTracksViewportWidth();
		}
		catch(Exception e){
			System.err.println("Throwable in " + getClass() + ": " + e);
		}
		return true;
	}
	public boolean getScrollableTracksViewportHeight() {
		try {
			return super.getScrollableTracksViewportHeight();
		}
		catch(Exception e){
			System.err.println("Throwable in " + getClass() + ": " + e);
	}

		return true;
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
		Dimension sD = super.getPreferredSize();
		if (minSize!= null){
			if (sD.width<minSize.width)
				sD.width = minSize.width;
			if (sD.height<minSize.height)
				sD.height = minSize.height;
			
		}
		return sD;
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


