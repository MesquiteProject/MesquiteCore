/* Mesquite source code.  Copyright 1997-2002 W. Maddison & D. Maddison. 
Version 0.992.  September 2002.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

package mesquite.collab.lib; 

import mesquite.lib.*;
import java.awt.*;

public class DWImagePanel extends MousePanel{
	Image image = null;
	protected Image[][] images; //images[vertical][horizontal]
	protected int[] whichImageHoriz;
	protected StringInABox[][] comments;
	protected StringInABox[][] locations;
	//public int WHICHIMAGE = 0;

	int buffer = 8;
	protected MesquiteString comment = new MesquiteString();
	protected MesquiteString location = new MesquiteString();
	StringInABox error;
	protected Font font = new Font("SanSerif", Font.PLAIN, 12);
	public MesquiteBoolean showLocation = new MesquiteBoolean(false);
	
	public DWImagePanel (){
		setBackground(Color.white);
		error = new StringInABox("error", font, getBounds().width-8);
	}
	public void prepareMemory(int[] numImages){
   		images = new Image[numImages.length][];
   		comments = new StringInABox[numImages.length][];
   		locations = new StringInABox[numImages.length][];
   		whichImageHoriz = new int[numImages.length];
   		for (int i = 0; i<numImages.length; i++){
   			whichImageHoriz[i] = 0;
	   		images[i] = new Image[numImages[i]];
	   		comments[i] = new StringInABox[numImages[i]];
	   		locations[i] = new StringInABox[numImages[i]];
   		}
	}
	public void paint(Graphics g){
   		if (images!= null && images.length>0) {
   			int totalComments = 0;
   			for (int i=0; i< images.length; i++) {
   				if (comments[i][whichImageHoriz[i]]!= null) {
   					totalComments += comments[i][whichImageHoriz[i]].getHeight();
   				}
    				if (locations[i][whichImageHoriz[i]]!= null && showLocation.getValue()) {
   					totalComments += locations[i][whichImageHoriz[i]].getHeight();
   				}
  			}
   			int heightPer = (getBounds().height-totalComments)/images.length;
   			int top = 0;
	   		for(int i=0; i<images.length; i++) {
	   			
	   			if (images[i][whichImageHoriz[i]] == null || images[i][whichImageHoriz[i]].getWidth(this) <= 0){
	   				Color c = g.getColor();
	   				g.setColor(Color.blue);
	   				error.setWidth(getBounds().width - 24);
	   				String s = "Image file not obtained.  The address may be misspecified in the index, or there may be communication problems.  Also, please check the Images Base Address menu item.";
   				 	s += "  Location of image: " + locations[i][whichImageHoriz[i]].getString();
	   				error.setString(s);
	   				error.draw(g, 12, top+4);
	   				g.drawRect(10, top, getBounds().width -20, heightPer-buffer);
	   				g.setColor(c);
					comments[i][whichImageHoriz[i]].draw(g, 4, top+heightPer-buffer);
					top += comments[i][whichImageHoriz[i]].getHeight();
    					if (showLocation.getValue()) {
						locations[i][whichImageHoriz[i]].draw(g, 4, top+heightPer-buffer);
						top += locations[i][whichImageHoriz[i]].getHeight();
    					}
	   			}
	   			else {
	   				
					Rectangle r = MesquiteImage.drawImageWithinRect(g, images[i][whichImageHoriz[i]],  0, top, getBounds().width,  heightPer-buffer, this);
					if (r == null)
						comments[i][whichImageHoriz[i]].draw(g, 4, top+heightPer-buffer);
					else {
						comments[i][whichImageHoriz[i]].draw(g, 4, r.y +r.height);
		   				if (images[i][whichImageHoriz[i]].getWidth(this) * images[i][whichImageHoriz[i]].getHeight(this)>4000000){
			   				Color c = g.getColor();
			   				g.setColor(Color.red);
			   				error.setWidth(r.width);
			   				String s = "Image file very large (with many pixels).  If the image does not appear quickly, you may need to use a smaller image.  Location: " + locations[i][whichImageHoriz[i]].getString();
			   				error.setString(s);
			   				error.draw(g, r.x, r.y);
			   				g.setColor(c);
							MesquiteImage.drawImageWithinRect(g, images[i][whichImageHoriz[i]],  0, top, getBounds().width,  heightPer-buffer, this);
		   				}
					}
					top += comments[i][whichImageHoriz[i]].getHeight();
    					if (showLocation.getValue()) {
    						Color c = g.getColor();
    						g.setColor(Color.blue);
						if (r == null)
							locations[i][whichImageHoriz[i]].draw(g, 4, top+heightPer-buffer);
						else 
							locations[i][whichImageHoriz[i]].draw(g, 4, r.y + comments[i][whichImageHoriz[i]].getHeight() +r.height);
						g.setColor(c);
						top += locations[i][whichImageHoriz[i]].getHeight();
    					}
				}
				top += heightPer;
			}
		}
	}
	
	protected int findImage(int y){
   		if (images!= null && images.length>0){
   			int h = getBounds().height/images.length;
	   		for(int i=0; i<images.length; i++) {
				if (y> i*h && y< (i+1)*h)
					return i;
			}
		}
		return -1;
	}
	
}