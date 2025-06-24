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
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;

import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteMessage;

/* ��������������������������� commands ������������������������������� */
/* includes commands,  buttons, miniscrolls*/
/* ======================================================================== */
/** A tool (i.e. cursor) that can be active and used on items on the screen.*/
public class MesquiteCursor {
	protected String  name; 
	protected String imageDirectoryPath;
	protected String imageFileName;
	protected Cursor cursor;
	Point hotSpot;
	Image cursorImage = null;
	
	public MesquiteCursor (Object initiator, String name, String imageDirectoryPath, String imageFileName, int hotX, int hotY) {
		if (initiator !=null)
			this.name = initiator.getClass().getName() + "." + name;
		else
			this.name = name;
		this.imageDirectoryPath = imageDirectoryPath;
		this.imageFileName = imageFileName;
		hotSpot = new Point(hotX, hotY);
	}
	
	public void setCursorImage(Image i){
		cursorImage = i;
	}
	public Image getCursorImage(){
		return cursorImage;
	}
	public String getName(){
		return name;
	}
	public void setHotSpot(int x, int y){
		hotSpot.x = x;
		hotSpot.y = y;
	}
	public Point getHotSpot(){
		return hotSpot;
	}
	/*.................................................................................................................*/
	public Cursor getCursor(){  
		if (MesquiteFile.fileExists(getImagePath())){
			Image im = getCursorImage();
			if (im == null){
				Dimension best = Toolkit.getDefaultToolkit().getBestCursorSize(16, 16);
				if ((best.width>16 || best.height>16) && MesquiteFile.fileExists(getSizedImagePath(best.width))){
					im = MesquiteImage.getImage(getSizedImagePath(best.width));
					if (im == null)
						im = MesquiteImage.getImage(getImagePath());
				}
				else 
					im = MesquiteImage.getImage(getImagePath());
				setCursorImage(im);
			}
			if (!MesquiteImage.waitForImageToLoad(im)) {
				MesquiteMessage.println("Note: image of cursor of tool not obtained: " + getName() + "  " + getImagePath());
				return Cursor.getDefaultCursor();
			}
			
			Cursor c = Toolkit.getDefaultToolkit().createCustomCursor(im, getHotSpot(), getName());
			return c;
		}
		return Cursor.getDefaultCursor();
	}
	public String getImagePath(){
		return imageDirectoryPath + imageFileName;
	}
	public void setImageFileName(String name){
		imageFileName = name;
		cursorImage = null; // to force reloading
	}
	/*.................................................................................................................*/
	public String getSizedImagePath(int s){
		return imageDirectoryPath + s + imageFileName;
	}
	
}
