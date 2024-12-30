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

import java.awt.*;

/*===============================================*/
/** A field for displaying uneditable text. */
public class MesquiteTextCanvas extends MQTextArea  {
	String message;
	int width = 0;
	int totalLength;
	int totalHeight;
	int lineHeight;
	int numReturns=0;
	Font font;
	/*.................................................................................................................*/
	public MesquiteTextCanvas (int width, Font font, String message, int scrollbars) {
		super(message, 6, 40, TextArea.SCROLLBARS_NONE);
		this.message = message;
		numReturns = StringUtil.getNumMatchingChars(message, '\n');
		this.width = width;
		if (font == null)
			font = ExtensibleDialog.defaultBigFont;
		this.font = font;
		setFont(font);
		reCalcRows(width);
		setEditable(false);
		disableEvents(AWTEvent.MOUSE_EVENT_MASK+AWTEvent.MOUSE_MOTION_EVENT_MASK);
	}
	/*.................................................................................................................*/
	public void reCalcRows (int width) {
		totalLength = StringUtil.getStringDrawLength(this,message);
		lineHeight = StringUtil.getTextLineHeight(this);
		int estimatedRows;
		if (width == 12)
			estimatedRows = 2+numReturns; //added 13 Dec 01
		else
			estimatedRows = totalLength / (width-12) +2+numReturns;
		if (estimatedRows <= 0)
			estimatedRows = 3;
		else if (estimatedRows >20)
			estimatedRows = 20;
		estimatedRows++;  //and one for the pot
		setRows(estimatedRows);
	}
	/*.................................................................................................................*/
	public void setCanvasRows (int width) {
		Font font = getFont();
		if (font==null)
			setFont(new Font("Dialog", Font.PLAIN, 12));
		reCalcRows(width);
		if (getBackground() != getParent().getBackground())
			setBackground(getParent().getBackground());
	}

	public static MesquiteTextCanvas getTextCanvas(int width, Font font, String message){
		MesquiteTextCanvas tc = new MesquiteTextCanvas(width, font, message, TextArea.SCROLLBARS_NONE);
		if (tc.getRows()>50){
			return new MesquiteTextCanvas(width, font, message, TextArea.SCROLLBARS_VERTICAL_ONLY);
		}
		else
			return tc;
	}
	public void paint(Graphics g){
		if (getBackground() != getParent().getBackground())
			setBackground(getParent().getBackground());
		super.paint(g);
	}


}
