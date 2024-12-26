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
/* ======================================================================== */
public class TDLegendWithColors extends TreeDisplayLegend {
	private LegendHolder traceModule;
	private MesquiteString resultString;
	private static final int defaultLegendWidth=142;
	private static final int defaultLegendHeight=120;
	private TCMPanel messageBox;
	private TextArea specsBox;
	private boolean holding = false;
	final int scrollAreaHeight = 25;
	private int messageHeight = 22;
	final int defaultSpecsHeight = (34 + MesquiteModule.textEdgeCompensationHeight) * 1;
	private int specsHeight = defaultSpecsHeight;
	private int e = 4;
	private String title;
	private Color titleColor;
	ColorRecord[] colorRecords;
	
	public TDLegendWithColors(LegendHolder traceModule, TreeDisplay treeDisplay, MesquiteString resultString, String title, Color titleColor) {
		super(treeDisplay,defaultLegendWidth, defaultLegendHeight);
		setVisible(false);
		this.title = title;
		this.titleColor = titleColor;
		this.resultString = resultString;
		legendWidth=defaultLegendWidth;
		legendHeight=defaultLegendHeight;
		setOffsetX(traceModule.getInitialOffsetX());
		setOffsetY(traceModule.getInitialOffsetY());
		this.traceModule = traceModule;
		//setBackground(ColorDistribution.light);
		setLayout(null);
		setSize(legendWidth, legendHeight);

		specsBox = new TextArea(" ", 2, 2, TextArea.SCROLLBARS_NONE);
		specsBox.setEditable(false);
		if (traceModule.showLegend())// && traceModule.showReconstruct.getValue())
			specsBox.setVisible(false);
		specsBox.setBounds(1,scrollAreaHeight+2,legendWidth-2, specsHeight);
		add(specsBox);
		messageBox = new TCMPanel();
		messageBox.setBounds(2,legendHeight-messageHeight-4,legendWidth-6, messageHeight);
		messageBox.setText("\n");
		//messageBox.setColor(Color.pink);
		//messageBox.setBackground(Color.pink);
		add(messageBox);
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setVisible(boolean b) {
		super.setVisible(b);
		if (messageBox!=null)
			messageBox.setVisible(b);
		if (specsBox!=null)// && traceModule.showReconstruct.getValue())
			specsBox.setVisible(b);
	}
	
	public void setColorRecords(ColorRecord[] colors){
		int old = getNumBoxes();
		colorRecords = colors;
		if (old != getNumBoxes())
			reviseBounds();
	}
	
	public void refreshSpecsBox(){
		if (resultString!=null)
			specsBox.setText(resultString.getValue()); 
	}
	public void paint(Graphics g) {
		   	if (MesquiteWindow.checkDoomed(this))
		   		return;
			if (!holding) {
				g.setColor(Color.black);
				if (colorRecords!=null) {
					for (int ibox=0; ibox<colorRecords.length; ibox++) {
						if (colorRecords[ibox]!=null) {
							g.setColor(colorRecords[ibox].getColor());
							g.fillRect(4, ibox*16+scrollAreaHeight + specsHeight+ e, 20, 12);
							g.setColor(Color.black);
							g.drawRect(4, ibox*16+scrollAreaHeight + specsHeight + e, 20, 12);
							if (colorRecords[ibox].getString()!=null)
								g.drawString(colorRecords[ibox].getString(), 28, ibox*16 + specsHeight+scrollAreaHeight + 12 + e);
						}
					}
				}
				g.setColor(Color.cyan);
				g.drawRect(0, 0, legendWidth-1, legendHeight-1);
				g.fillRect(legendWidth-6, legendHeight-6, 6, 6);
				g.drawLine(0, scrollAreaHeight, legendWidth-1, scrollAreaHeight);
				
				g.setColor(titleColor);
				g.drawString(title, 4, 14);
				g.setColor(Color.black);
				if (resultString!=null  && resultString.getValue()!=null && !resultString.getValue().equals(specsBox.getText())){
					specsBox.setText(resultString.getValue()); 
				}
				if (specsBox.getBackground() != getBackground())
					specsBox.setBackground(getBackground());
			}
		MesquiteWindow.uncheckDoomed(this);
	}
	
	public void printAll(Graphics g) {
		g.setColor(Color.black);
		g.drawString(title, 4, 14);
		int QspecsHeight = 0;
		if (resultString!=null) {
			String info = resultString.getValue();
			StringInABox sib = new StringInABox(info, g.getFont(), legendWidth);
			sib.draw(g, 4, 16);
			QspecsHeight = sib.getHeight();
		}
		int lastBox = QspecsHeight + 20;
		//int QspecsHeight = 0;
		//int lastBox = scrollAreaHeight + QspecsHeight + 20 + 12;
		if (colorRecords!=null) {
			for (int ibox=0; ibox<colorRecords.length; ibox++) {
				if (colorRecords[ibox]!=null) {
					g.setColor(colorRecords[ibox].getColor());
					g.fillRect(4, ibox*16+ QspecsHeight + 20, 20, 12);
					g.setColor(Color.black);
					g.drawRect(4, ibox*16+ QspecsHeight + 20, 20, 12);
					if (colorRecords[ibox].getString()!=null)
						g.drawString(colorRecords[ibox].getString(),  28, ibox*16 + QspecsHeight + 32);
					lastBox =ibox*16 + QspecsHeight + 20 + 12;
				}
			}
		}
	}
	
	private int getNumBoxes(){
		if (colorRecords == null)
			return 0;
		return colorRecords.length;
	}
	
	public void legendResized(int widthChange, int heightChange){
		if ((specsHeight + heightChange)>= defaultSpecsHeight)
			specsHeight += heightChange;
		else
			specsHeight  = defaultSpecsHeight;
		checkComponentSizes();
	}
//int reviseBoundsCounter=0;
	public void reviseBounds(){
		checkComponentSizes();
		Point where = getLocation();
		Rectangle bounds = getBounds();
		if (bounds.width!=legendWidth || bounds.height!=legendHeight) //make sure a change is really needed
			setBounds(where.x,where.y,legendWidth, legendHeight);
	}
	public void checkComponentSizes(){
		specsBox.setBounds(1,scrollAreaHeight+2,legendWidth-2, specsHeight);
		specsBox.setVisible(true);
		messageHeight = messageBox.getHeightNeeded();
		if (messageHeight<20)
			messageHeight = 20;
		legendHeight=getNumBoxes()*16+scrollAreaHeight + specsHeight + e + messageHeight + 4;
		messageBox.setBounds(2,legendHeight-messageHeight-4,legendWidth-6, messageHeight);
	}
	
	public void setMessage(String s) {
		if (s==null || s.equals("")) {
			//messageBox.setBackground(ColorDistribution.light);
			messageBox.setText("\n");
			reviseBounds();
		}
		else {
			//messageBox.setBackground(Color.white);
			messageBox.setText(s);
			reviseBounds();
		}
	}
	public void onHold() {
		holding = true;
	}
	
	public void offHold() {
		holding = false;
	}
}

class TCMPanel extends Panel {
	String message = "";
	StringInABox box;
	public TCMPanel(){
		super();
		box =  new StringInABox("", null, getBounds().width);
	}
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x,y,w,h);
		box.setWidth(w);
	}
	public void setSize(int w, int h){
		super.setSize(w,h);
		box.setWidth(w);
	}
	public int getHeightNeeded(){
		return box.getHeight();
	}
	public void paint (Graphics g) {
	   	if (MesquiteWindow.checkDoomed(this))
	   		return;
		box.setFont(g.getFont());
		box.draw(g,0, 0);
		//g.drawString(message, 2, 10);
		MesquiteWindow.uncheckDoomed(this);
	}
	
	public void setText(String s) {
		if (!message.equals(s)) {
			message = s;
			box.setString(s);
			repaint();
		}
	}
}


