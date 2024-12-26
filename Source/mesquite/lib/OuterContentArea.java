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
/** A container placed in a window to help ensure that insets don't cause problems on non-MacOS systems.
In turn it contains the infoBar and the InterContentArea.  When the window is resized, this container is in
charge of resizing the infoBar and InterContentArea, along with the principle components within the InterContentArea.*/
class OuterContentArea extends Panel {
	ContentArea[] graphics;
	ContentArea[] text;
	InterContentArea iC;
	MesquiteWindow ownerWindow;
	InfoBar infoBar;
	ExplanationArea explanationArea, annotationArea;
	public OuterContentArea (MesquiteWindow ownerWindow) {
		this.ownerWindow = ownerWindow;
		//addComponentListener(new OCACE(this));
		setLayout(null);
	}
	public void dispose(){
		ownerWindow=null;
		for (int i=0; i < graphics.length; i++) 
			graphics[i]=null;
		for (int i=0; i < text.length; i++) 
			text[i]=null;
		iC = null;
		infoBar=null;  
	}

	/*.................................................................................................................*/
	/** Registers the content areas so that they can be resized as needed */
	public void setContentsArea(ContentArea[] graphics, ContentArea[] text, InterContentArea iC){
		this.graphics=graphics;
		this.iC = iC;
		this.text = text;
	}
	/*.................................................................................................................*/
	/** Registers the InfoBar so it can be resized as needed */
	public void setInfoArea(InfoBar iB){
		this.infoBar=iB;
	}
	/*.................................................................................................................*/
	/** Registers the ExplanationArea so it can be resized as needed */
	public void setExplanationArea(ExplanationArea eA){
		this.explanationArea=eA;
	}
	/*.................................................................................................................*/
	/** Registers the AnnotationArea so it can be resized as needed */
	public void setAnnotationArea(ExplanationArea eA){
		this.annotationArea=eA;
	}
	/*.................................................................................................................*/
	/** Sets the bounds of this and the content areas within it */
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x,y,w,h);
//TODO: undone mar 2000		ownerWindow.containerSizeSet(w,h);  //compensate by difference in insets (if inset has gotten bigger, adjust to compensate
		try{
		if (ownerWindow==null)
			return;
		int contentHeight = h - ownerWindow.infoBarHeight - ownerWindow.explanationHeight - ownerWindow.annotationHeight;
		if (iC!=null)
			iC.setBounds(0, ownerWindow.infoBarHeight, w,  contentHeight);
		if (graphics!=null)
			for (int i=0; i<graphics.length; i++){
				if (graphics[i]!=null) {
					graphics[i].setBounds(0, 0, w, contentHeight);
					graphics[i].doLayout();
				}
			}
		if (text!=null)
			for (int i=0; i<text.length; i++){
				if (text[i]!=null)
					text[i].setBounds(0, 0, w, contentHeight);
			}
		if (infoBar!=null)
			infoBar.setBounds(0, 0, w,ownerWindow.infoBarHeight);
		if (explanationArea!=null)
			explanationArea.setBounds(0, ownerWindow.infoBarHeight + contentHeight + ownerWindow.annotationHeight, w, ownerWindow.explanationHeight);
		if (annotationArea!=null)
			annotationArea.setBounds(0, ownerWindow.infoBarHeight + contentHeight, w, ownerWindow.annotationHeight);
		doLayout(); //added 18Mar02
		}
		catch (NullPointerException e){ //may happen if in process of disposing window
		}
	}
	/*.................................................................................................................*/
	/** Sets the size of this and the content areas within it */
	public void setSize(int w, int h){
		super.setSize(w,h);
		try{
		if (ownerWindow==null)
			return;
		int contentHeight = h - ownerWindow.infoBarHeight - ownerWindow.explanationHeight -ownerWindow.annotationHeight;
		if (iC!=null)
			iC.setSize(w,contentHeight);
		if (graphics!=null)
			for (int i=0; i<graphics.length; i++){
				if (graphics[i]!=null) {
					graphics[i].setSize(w,contentHeight);
					graphics[i].doLayout();
				}
			}
		if (text!=null)
			for (int i=0; i<text.length; i++){
				if (text[i]!=null) {
					text[i].setSize(w,contentHeight);
					text[i].doLayout();
				}
			}
		if (infoBar!=null) {
			infoBar.setSize(w,ownerWindow.infoBarHeight);
			infoBar.doLayout();
		}
			if (explanationArea!=null)
				explanationArea.setSize(w, ownerWindow.explanationHeight);
			if (annotationArea!=null)
				annotationArea.setSize(w, ownerWindow.annotationHeight);
			doLayout(); //added 18Mar02
		}
		catch (NullPointerException e){
		}
	}
	
}

