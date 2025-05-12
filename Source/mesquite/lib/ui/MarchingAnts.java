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

import java.awt.*;
import java.util.*;

public class MarchingAnts {
	AntMarcher ants;
	protected int x;
	protected int y;
	protected int width;
	protected int height;
	Timer tmr;
	protected Graphics g;
	public MarchingAnts() {
	}
	public MarchingAnts(Graphics g, int x, int y, int width, int height) {
		this.x=x; 
		this.y=y;
		this.width=width;
		this.height = height;
		this.g = g;
		startAnts();
	}
	public void startAnts() {
		ants = new AntMarcher(g, this);
		tmr = new Timer();
		tmr.schedule(ants, 0,20);
	}
	public void resize (int x, int y, int width, int height){
		if (ants!=null)
			ants.drawAnts();  //draws old size just to get rid of it
		this.x=x; 
		this.y=y;
		this.width=width;
		this.height = height;
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public void cancel() {
		if (ants!=null) 
			ants.cancel();
	}
	public void setLineWidth(float lineWidth){
		if (ants!=null)
			ants.setLineWidth(lineWidth);
	}

}

class AntMarcher extends TimerTask {
	float dashPhase = 0;
	Graphics2D g2;
	MarchingAnts c = null;
	float lineWidth = 1.5f;
	
	public AntMarcher (Graphics g, MarchingAnts c) {
		this.c=c;
		if (g instanceof Graphics2D) {
			g2 = (Graphics2D)g;
			drawAnts();
		}
		else
			cancel();
	}
	public void setLineWidth(float lineWidth){
		this.lineWidth = lineWidth;
	}
	public boolean cancel() {
		drawAnts();
		return super.cancel();
	}
	public void drawAnts() {
		if (c==null)
			return;
		g2.setPaint(Color.black);
		GraphicsUtil.setSafeXORMode(g2);
		//	Stroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0.0f, new float[]{4, 2, 6, 2},dashPhase);
		Stroke stroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0.0f, new float[]{4, 4},dashPhase);
		g2.setStroke(stroke);
		g2.drawRect(c.getX(),c.getY(),c.getWidth(),c.getHeight());
		GraphicsUtil.setSafePaintMode(g2); 
	}
	public  void run() {
		if (c==null)
			return;
		//g2.fillRect(c.getX(),c.getY(),c.getWidth(),c.getHeight());

		drawAnts();
		dashPhase++;
		drawAnts();

	}
}