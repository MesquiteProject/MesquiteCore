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
import mesquite.lib.duties.*;

/* ��������������������������� commands ������������������������������� */
/* includes commands,  buttons, miniscrolls

/* ======================================================================== */
/** A miniature slider used to set the value of a MesquiteNumber.  It provides
an incrementing and decrementing arrow, and a text field to show current value. */
public class MiniSlider extends MousePanel implements Explainable, ImageOwner {
	double currentValue, minValue, maxValue;
	double minSweetValue, maxSweetValue;
	ValueToPixel valueToPixel=null;
	//SliderButton slideButton;
	int widthSet = 0;
	public int totalWidth = 0;  
	public int totalHeight=0; 
	boolean sliderIsMoving = false;
	Color bg = Color.white;
	boolean horizontal = true;
	int insetEdge = 4;
	MesquiteCommand command;
	Color color = null;
	MiniSliderWithText textToNotify = null;
	static Image enterVertical, enterHorizontal;
	private Image enter;
	int touchOffset = 0;
	MesquiteTimer timer=null;
	//boolean enforceMin, enforceMax;
	static {
		enterVertical = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "sliderVert.gif");
		enterHorizontal = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "sliderHoriz.gif");
	}
	public MiniSlider (MesquiteCommand command, boolean horizontal, double currentValue, double minValue, double maxValue, double minSweetValue, double maxSweetValue) {
		setCursor(Cursor.getDefaultCursor());
		this.currentValue = currentValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.minSweetValue = minSweetValue;
		this.maxSweetValue = maxSweetValue;
		this.command=command;
		this.horizontal = horizontal;
		
		setLayout(null);
		//add(slideButton = new SliderButton(this, horizontal));
		//adjustSlider();
		//setBackground(Color.green);
		if (horizontal) 
			enter = enterVertical;
		else 
			enter = enterHorizontal;
//		this.enforceMin = enforceMin;
//		this.enforceMax = enforceMax;
		valueToPixel = new LinearValueToPixel(minValue,maxValue, minSweetValue, maxSweetValue, sliderRangeInPixels());
	}

	/*.................................................................................................................*/
	 public boolean getSliderIsMoving() {
	 	return sliderIsMoving;
  	 }
	/*.................................................................................................................*/
	 public void useExponentialScale(boolean exp) {         // not yet functioning
  	 //	valueToPixel.dispose();   
/*
  	 	if (exp)
  	 		valueToPixel = new LogValueToPixel(minValue,maxValue, minSweetValue, maxSweetValue, sliderRangeInPixels());
  	 	else
  	 		valueToPixel = new LinearValueToPixel(minValue,maxValue, minSweetValue, maxSweetValue, sliderRangeInPixels());
  	 	repaint();
*/
  	 }
	public void  setColor(Color c){
		color = c;
	}
	public void setRangeInPixels(int w){
		if (horizontal)
			setSize(w, 16);
		else
			setSize(16, w);
		if (valueToPixel!= null)
			valueToPixel.setTotalPixels(sliderRangeInPixels());
		//adjustSlider();
	}
	int sliderRangeInPixels(){
		if (horizontal)
			return getBounds().width -8; //-8 for width of image
		else
			return getBounds().height -8;
	}
	double percentPosition(){
		if (MesquiteDouble.isCombinable(currentValue) && (maxValue-minValue)!=0) {
			valueToPixel.setTotalPixels(sliderRangeInPixels());
			return valueToPixel.getPixelPercent(currentValue);
		}
		else
			return 0;
	}
	int sliderPositionInPixels(){
		double pp = percentPosition();
		return (int)( percentPosition()*sliderRangeInPixels());
	}
	public void setTextToNotify(MiniSliderWithText t){
		textToNotify = t;
	}
	public void paint(Graphics g) { //^^^
		if (g instanceof PrintGraphics)
			return;
	   	if (MesquiteWindow.checkDoomed(this))
	   		return;
		//g.setColor(getBackground());
		//g.fillRect(0,0,getBounds().width,getBounds().height);
		if (color!=null) {
			g.setColor(color);
    			g.fillRect(insetEdge, insetEdge, getBounds().width-insetEdge-insetEdge,getBounds().height-insetEdge-insetEdge);
		}
		if (valueToPixel.hasStartingNonSweet()) {
			g.setColor(Color.lightGray);
    			g.fillRect(insetEdge, insetEdge,valueToPixel.getStartSweetPixels(),getBounds().height-insetEdge-insetEdge);
		}
		if (valueToPixel.hasEndingNonSweet()) {
			g.setColor(Color.lightGray);
    			g.fillRect(insetEdge+valueToPixel.getEndSweetPixels()+1, insetEdge,valueToPixel.getPostSweetPixels(),getBounds().height-insetEdge-insetEdge);
		}
		g.setColor(Color.black);
    		g.drawRect(insetEdge, insetEdge, getBounds().width-insetEdge-insetEdge,getBounds().height-insetEdge-insetEdge);
		if (horizontal)
			g.drawImage(enter,sliderPositionInPixels(), 0, this);
		else
			g.drawImage(enter,0, sliderPositionInPixels(), this);
		/*if (sliderIsMoving){
			if (horizontal)
				g.drawString(MesquiteDouble.toString(currentValue), 4, getBounds().height-insetEdge-insetEdge);
			else
				g.drawString(MesquiteDouble.toString(currentValue), 24, 4);
		}*/
		MesquiteWindow.uncheckDoomed(this);
	}
	public void printAll(Graphics g) { 
	}
	public void paintComponents(Graphics g) { 
		if (g instanceof PrintGraphics)
			return;
		else
			super.paintComponents(g);
	}
	public void printComponents(Graphics g) { 
	}
	public void print(Graphics g) { 
	}
	private boolean checkBackground(){
		if (getParent() !=null && getBackground()!=null && !getBackground().equals(getParent().getBackground())) {
			bg =getParent().getBackground();
			setBackground(bg);
			return true;
		}
		else return false;
	}
	public void setVisible(boolean b) {
		if (b)
			checkBackground();
		super.setVisible(b);
		repaint();
	}
	public void setMaximumValue (double i) { 
		if (i!=maxValue) {
			maxValue=i;
			valueToPixel.setMaxValue(maxValue);
			if (MesquiteDouble.isCombinable(currentValue) && (currentValue>maxValue))
				setCurrentValue(maxValue);
			else
				setCurrentValue(currentValue); //this ensures draw correctly
			repaint();
		}
	}
	public void setMinimumValue (double i) { 
		if (i!=minValue) {
			minValue=i;
			valueToPixel.setMinValue(minValue);
			if (MesquiteDouble.isCombinable(currentValue) && (currentValue<minValue))
				setCurrentValue(minValue);
			else
				setCurrentValue(currentValue); //this ensures draw correctly
			repaint();
		}
	}
	public void setMaximumSweetValue (double i) { 
		if (i!=maxSweetValue) {
			maxSweetValue=i;
			valueToPixel.setMaxSweetValue(i);
			repaint();
		}
	}
	public void setMinimumSweetValue (double i) { 
		if (i!=minSweetValue) {
			minSweetValue=i;
			valueToPixel.setMinSweetValue(i);
			repaint();
		}
	}
	public void setCurrentValue (double i) {  
		if (MesquiteDouble.isUnassigned(i)) {
			currentValue = i;
			return;
		}
		if (!MesquiteDouble.isCombinable(i))
			return;
		if (i<=maxValue && (i>=minValue) && i!=currentValue) {
			currentValue=i;
		}
		repaint();
		//adjustSlider();
	}

	public MesquiteNumber getMaximumValue () { 
			return  new MesquiteNumber(maxValue);
	}
	public MesquiteNumber getMinimumValue () { 
			return  new MesquiteNumber(minValue);
	}
	public MesquiteNumber getCurrentValue () {  
			return  new MesquiteNumber(currentValue);
	}
	public void sliderAt(int x) { 
		if (MesquiteWindow.getQueryMode(this)) {
			MesquiteWindow.respondToQueryMode("Mini slider", command, this);
			return;
		}
		double i;
		valueToPixel.setTotalPixels(sliderRangeInPixels());
		i = valueToPixel.getValue(x);
				
		if (command!=null) {
			if (i>maxValue)
				i=maxValue;
			if (i<minValue)
				i=minValue;
			currentValue=i;
			if (textToNotify!=null)
				textToNotify.setCurrentNoSliderUpdate(i);
			command.doItMainThread(MesquiteDouble.toString(currentValue), CommandChecker.getQueryModeString("Mini slider", command, this), false, false); //false, false added 17 Nov 01
		}
	}
	public String getExplanation(){ //TODO: this should use string passed in constructor
		return "This is a slider control";
	}
	public String getImagePath(){ //TODO: this should use path to standard image
		return MesquiteModule.getRootImageDirectoryPath() + "sliderVert.gif";
	}
	
  	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
  		sliderIsMoving = true;
		if (!horizontal) {
			touchOffset = y - sliderPositionInPixels(); //get position of touch relative to  slider
		}
		else {
			touchOffset = x - sliderPositionInPixels();
		}
		repaint();
	}
   	public void mouseDrag(int modifiers, int x, int y, MesquiteTool tool) {
   		if (timer==null) {
   			timer = new MesquiteTimer();
   			timer.start();
   		}
   		if (timer==null || timer.timeSinceLast()>20)  // only check this if it has been 20 milliseconds
   			if (!horizontal){
   				int newLoc = (y-touchOffset); //this is only for vertical
   				if (newLoc>=0 && newLoc< sliderRangeInPixels()) {
   					sliderAt(newLoc);
   					repaint();
   				}
   			}
   			else {
   				int newLoc =  (x-touchOffset); //this is only for horizontal
   				if (newLoc>=0 && newLoc< sliderRangeInPixels()) {
   					sliderAt(newLoc);
   					repaint();
   				}
   			}
	}
   	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		//miniSlider.acceptText();
		sliderIsMoving = false;
	}
}
/*
class SliderButton extends MousePanel {
	public MiniSlider miniSlider;
	public static Image enterVertical, enterHorizontal;
	private Image enter;
	private boolean firsttime=true;
	int touched = 0;
	private boolean horizontal = true;
	static {
		enterVertical = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "sliderVert.gif");
		enterHorizontal = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "sliderHoriz.gif");
	}
	public SliderButton (MiniSlider miniSlider, boolean horizontal) {
		this.miniSlider = miniSlider;
		this.horizontal = horizontal;
		if (horizontal) {
			enter = enterVertical;
			setBounds(0,0,8,16);
		}
		else {
			enter = enterHorizontal;
			setBounds(0,0,16,8);
		}
	}
	public SliderButton (MiniSlider miniSlider) {
		this(miniSlider, true);
	}
	
	public void paint (Graphics g) {
	   	if (MesquiteWindow.checkDoomed(this))
	   		return;
		g.drawImage(enter,0,0,this);
		MesquiteWindow.uncheckDoomed(this);
	}
	
  	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (!horizontal)
			touched = y;
		else
			touched = x;
		repaint();
	}
   	public void mouseDrag(int modifiers, int x, int y, MesquiteTool tool) {
		if (!horizontal){
			int newLoc = getLocation().y + (y-touched); //this is only for vertical
			if (newLoc>=0 && newLoc< miniSlider.sliderRangeInPixels()) {
				miniSlider.sliderAt(newLoc);
				setLocation(getLocation().x, newLoc);
			}
		}
		else {
			int newLoc = getLocation().x + (x-touched); //this is only for horizontal
			if (newLoc>=0 && newLoc< miniSlider.sliderRangeInPixels()) {
				miniSlider.sliderAt(newLoc);
				setLocation(newLoc, getLocation().y);
			}
		}
	}
   	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		//miniSlider.acceptText();
	}
}
*/

