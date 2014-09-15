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

public class ColorPickerPanel extends Panel implements MouseListener {
	int hues = 50;
	int saturations = 50;
	int brightnesses = 50;
	double brightness = 1.0;
	Color[][] colors;
	float[] colorLoc = null;
	CurrentColorPanel indicator;
	Colorable dialog;
	private Color currentColor = Color.white;
	int currentI = 0;
	int currentJ = 0;
	int boxSizeX;
	int boxSizeY;
	int offsetX;
	int offsetY;
	int touchedX = -1;
	int touchedY = -1;
	int indicatorX = -1;
	int indicatorY = -1;
	int colorEdge = 50;
	public ColorPickerPanel(Colorable d, Color initColor, int divisions){
		this.dialog = d;
		addMouseListener(this);
		if (divisions>0) {
			hues = divisions;
			saturations = divisions;
		}
		colors = new Color[hues][saturations];
		for (int i=0; i<hues-1; i++){
			for (int j=0; j<saturations; j++){
				colors[i][j] = new Color(Color.HSBtoRGB((float)(i*1.0/hues),(float)(j*1.0/saturations),(float)brightness));
			}
		}
		/**/
		for (int j=0; j<saturations; j++){
			float f = (float)(j*1.0/saturations);
			colors[hues-1][j] = new Color(f, f, f);
		}
		/**/
	  	add(indicator = new CurrentColorPanel(this));
		setSize(200,200);
		int w =  getBounds().width - colorEdge;
		int h = getBounds().height;
		 boxSizeX = w/hues;
		 boxSizeY = h/saturations;
		indicator.setBounds(-50,-50, boxSizeX+2, boxSizeY+2);
	  	setInitialColor(initColor);
	}
	public void setInitialColor(Color current){
		if (current == null)
			return;
		currentColor = current;
		brightness = Color.RGBtoHSB(current.getRed(), current.getGreen(), current.getBlue(), null)[2];
			for (int ki=0; ki<hues-1; ki++){
  				for (int kj=0; kj<saturations; kj++){
  					colors[ki][kj] = new Color(Color.HSBtoRGB((float)(ki*1.0/hues),(float)(kj*1.0/saturations),(float)brightness));
  				}
  			}

		checkBounds();
		setColorFromXY(indicatorX, indicatorY);
		/*float hu = colorLoc[0];
		float sa = colorLoc[1];
		indicatorX = (int)(offsetX + hu*hues*boxSizeX-1);
		indicatorY = (int)(offsetY+ sa*saturations*boxSizeY-1);
		indicator.setLocation(indicatorX,indicatorY);
		*/
		indicator.repaint();
	}
	
	private void checkBounds(){
		int red = currentColor.getRed();
		int green = currentColor.getGreen();
		int blue = currentColor.getBlue();
		int w =  getBounds().width - colorEdge;
		int h = getBounds().height;
		 boxSizeX = w/hues;
		 boxSizeY = h/saturations;
		 offsetX = (w-hues*boxSizeX)/2;
		 offsetY = (h-saturations*boxSizeY)/2;
		if (red == green && red == blue){
			indicatorX = (int)(offsetX + (hues-1)*boxSizeX-1);
			indicatorY = (int)(offsetY+ (1.0*red)/255*saturations*boxSizeY-1);
		}
		else {
			colorLoc =  Color.RGBtoHSB(red, green, blue, null);
			if (colorLoc==null)
				return;
			float hu = colorLoc[0];
			float sa = colorLoc[1];
			brightness = colorLoc[2];
			indicatorX = (int)(offsetX + hu*hues*boxSizeX-1);
			indicatorY = (int)(offsetY+ sa*saturations*boxSizeY-1);
		}
		Rectangle d = indicator.getBounds();
		if (d.x != indicatorX || d.y != indicatorY || d.width != boxSizeX+2 || d.height != boxSizeY+2)
			indicator.setBounds(indicatorX,indicatorY, boxSizeX+2, boxSizeY+2);
	}
	public Dimension getPreferredSize() {
		return new Dimension(400, 200);
	}
	public void paint(Graphics g){
		g.setClip(0, 0, getBounds().width, getBounds().height);
		checkBounds();
		/* boxSizeX = w/hues;
		 boxSizeY = h/saturations;
		*/
		int w =  getBounds().width - colorEdge;
		int h = getBounds().height;
		 offsetX = (w-hues*boxSizeX)/2;
		 offsetY = (h-saturations*boxSizeY)/2;
		for (int x = 0; x < hues ; x++){
			for (int y = 0; y<saturations; y++){
				g.setColor(colors[x][y]);
				g.fillRect(offsetX + x*boxSizeX,offsetY+ y*boxSizeY,boxSizeX,boxSizeY);
			}
		}
		
		/*
		g.setColor(currentColor);
		g.fillRect(w,0,colorEdge,h);
		*/
		float[] f = new float[3];
		Color.RGBtoHSB(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), f);
		
		for (int b = 0; b < brightnesses ; b++){
			float br = (float)(1.0-(b*1.0/brightnesses));
			g.setColor(new Color(Color.HSBtoRGB(f[0],f[1],br)));
				g.fillRect(w, b*h/brightnesses,colorEdge,(h/brightnesses));
		}

  		
  		
  		g.setColor(Color.black);
		g.drawRect(w,0,colorEdge-1,h-1);
		g.drawRect(offsetX, offsetY, hues*boxSizeX-1, saturations*boxSizeY-1);

		g.drawRect(w + colorEdge/2-1, 0, 2, h-1);
		g.setColor(Color.white);
		g.drawLine(w + colorEdge/2, 0, w + colorEdge/2, h-1);
		g.setColor(Color.black);
		g.fillRoundRect(w + colorEdge/2-6, h-(int)(h * brightness + 4), 12, 8, 4, 4);
		g.setColor(Color.white);
		g.drawRoundRect(w + colorEdge/2-6, h-(int)(h * brightness + 4), 12, 8, 4, 4);
		//g.drawRect(indicatorX, indicatorY, boxSizeX, boxSizeY);
		/*
		indicator.setSize(boxSizeX+2, boxSizeY+2);
		
		if (touchedX<0 && touchedY<0 && colorLoc!=null){
			float hu = colorLoc[0];
			float sa = colorLoc[1];
			indicatorX = (int)(offsetX + hu*hues*boxSizeX-1);
			indicatorY = (int)(offsetY+ sa*saturations*boxSizeY-1);
			indicator.setLocation((int)(offsetX + hu*hues*boxSizeX-1),(int)(offsetY+ sa*saturations*boxSizeY-1));
		}
		*/
		
	}
	private void setColorFromXY(int x, int y){
		if (boxSizeX>0 && boxSizeY>0){
	  		int i =(x-offsetX)/boxSizeX;
	  		int j =(y-offsetY)/boxSizeY;
	  		if (i>=0 && i<hues && j >=0 && j<saturations) {
	  			currentColor = colors[i][j];
	  			currentI = i;
	  			currentJ = j;
				checkBounds();
				/*float hu = colorLoc[0];
				float sa = colorLoc[1];
				indicatorX = (int)(offsetX + hu*hues*boxSizeX-1);
				indicatorY = (int)(offsetY+ sa*saturations*boxSizeY-1);
	  			*/
	  			//dialog.setColor(colors[i][j]);
	  			touchedX = x;
	  			touchedY = y;
				//indicator.setLocation(indicatorX, indicatorY);
				indicator.repaint();
	  		}
	  		else if (j >=0 && j<saturations && i >= hues){
	  			brightness = (saturations - j)*1.0/saturations;
	  			for (int ki=0; ki<hues-1; ki++){
	  				for (int kj=0; kj<saturations; kj++){
	  					colors[ki][kj] = new Color(Color.HSBtoRGB((float)(ki*1.0/hues),(float)(kj*1.0/saturations),(float)brightness));
	  				}
	  			}
	  			currentColor = colors[currentI][currentJ];
	  			repaint();
	  			indicator.repaint();
	  		}
  		}
	}
   	public void mouseClicked(MouseEvent e) {
  		int x = e.getX();
  		int y = e.getY();
		setColorFromXY(x, y);
  	}

   	public void mouseEntered(MouseEvent e) {
   	}
   	public void mouseExited(MouseEvent e) {
   	}
   	public void mousePressed(MouseEvent e) {
   	}
   	public void mouseReleased(MouseEvent e) {
   	}
   	
   	public Color getColor(){
   		return currentColor;
   	}
}

class CurrentColorPanel extends Panel {
	ColorPickerPanel colorPanel;
	public CurrentColorPanel(ColorPickerPanel cp){
		colorPanel = cp;
	}
	public void paint(Graphics g){
		g.setColor(colorPanel.getColor());
		int w = getBounds().width;
		int h = getBounds().height;
		g.fillRect(0,0, w-1, h -1);
		g.setColor(Color.black);
		g.drawRect(0,0, w-1, h -1);
	}
}



