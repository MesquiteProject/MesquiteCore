/* Mesquite source code.  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib;

import javax.swing.JEditorPane;
import java.awt.*;

//workaround for crashes on OS X
public class MesqJEditorPane extends JEditorPane{
	public MesqJEditorPane(String a, String b){
		super(a, b);
	}
	public Dimension getPreferredSize(){
		try {
			return super.getPreferredSize();
		}
		catch(Exception e){
		}
		return new Dimension(300, 600);
	}
	public boolean getScrollableTracksViewportWidth() {
		try {
			return super.getScrollableTracksViewportWidth();
		}
		catch(Exception e){
		}
		return true;
	}
	public boolean getScrollableTracksViewportHeight() {
		try {
			return super.getScrollableTracksViewportHeight();
		}
		catch(Exception e){
		}

		return true;
	}

}


