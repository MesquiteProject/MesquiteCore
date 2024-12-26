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
import java.awt.image.*;
import java.awt.event.*;


/*===============================================*/
/** A panel containing and image */
public class ImagePanel extends Panel implements MouseListener  {
	Image image;
	Image imagePressed;
	ImagePanelListener imageListener;
	 String imageName;
	 boolean mouseIsDown = false;
	 int forcedImageHeight=0;
	 int forcedImageWidth = 0;
	
	public ImagePanel (Image image, Image imagePressed, String imageName, ImagePanelListener imageListener) {
		super();
		this.image = image;
		this.imagePressed = imagePressed;
		this.imageListener = imageListener;
		this.imageName = imageName;
		setVisible(true);
		setSize(getWidth(), getHeight());
		addMouseListener(this);
	}
	public ImagePanel (Image image) {
		super();
		this.image = image;
		setVisible(true);
		setSize(getWidth(), getHeight());
	}
	/*.................................................................................................................*/
	public void paint(Graphics g) {
		if (mouseIsDown && imagePressed !=null)
			g.drawImage(imagePressed,0,0,(ImageObserver)this);
		else
			g.drawImage(image,0,0,(ImageObserver)this);
	}
	/*.................................................................................................................*/
	public void setForcedImageHeight(int height) {
		forcedImageHeight = height;
	}
	/*.................................................................................................................*/
	public void setForcedImageWidth(int width) {
		forcedImageWidth = width;
	}
	/*.................................................................................................................*/
	public Dimension getPreferredSize() {
		return new Dimension(getWidth(), getHeight());
	}
	/*.................................................................................................................*/
	public Dimension getMinimumSize() {
		return new Dimension(getWidth(), getHeight());
	}
	/*.................................................................................................................*/
	public int getWidth() {
		if (image!=null) {
			if (forcedImageWidth==0)
				return image.getWidth((ImageObserver)this);
			else
				return forcedImageWidth;
		}
		else
			return 0;
	}
	/*.................................................................................................................*/
	public int getHeight() {
		if (image!=null) {
			if (forcedImageHeight==0)
				return image.getHeight((ImageObserver)this);
			else
				return forcedImageHeight;
		}
		else
			return 0;
	}
	/*.................................................................................................................*/
	public Image getImage(){
		return image;
	}
	/*.................................................................................................................*/
	public void mouseClicked(MouseEvent e) { }
	/*.................................................................................................................*/
	public void mouseEntered(MouseEvent e) { }
	/*.................................................................................................................*/
	public void mouseExited(MouseEvent e) { } 
	/*.................................................................................................................*/
	public void mousePressed(MouseEvent e) {
		mouseIsDown = true;
		if (imagePressed!=null) {
			Graphics g = this.getGraphics();
			if (g!=null) {
				g.drawImage(imagePressed,0,0,(ImageObserver)this);
			}
		}
	} 
	/*.................................................................................................................*/
	public void mouseReleased(MouseEvent e) {
		mouseIsDown = false;
		if (imagePressed!=null) {
			Graphics g = this.getGraphics();
			if (g!=null) 
				g.drawImage(image,0,0,(ImageObserver)this);
		}
		if (imageListener!=null)
			imageListener.mouseOnImage(imageName);
	}
}


