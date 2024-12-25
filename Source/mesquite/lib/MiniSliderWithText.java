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

/* ��������������������������� commands ������������������������������� */
/* includes commands,  buttons, miniscrolls

/* ======================================================================== */
/** A miniature slider used to set the value of a MesquiteNumber.  It provides
an incrementing and decrementing arrow, and a text field to show current value. */
public class MiniSliderWithText extends MQPanel implements MiniControl, Explainable, ImageOwner, ActionListener, TextListener {
	double currentValue, minValue, maxValue;
	boolean adjustingSizes = true;
	boolean allowEstimation=false;
	String currentText="";
	TextField tf;
	TextField dummy;
	EnterButton enterButton;
	MiniSlider slider;
	private int textBoxWidth = 34;
	private int textBoxHeight = 18;
	int widthSet = 0;
	public int totalWidth = 0;  
	public int totalHeight=0; 
	private int oldTextBoxWidth = 1;
	private int oldTextBoxHeight = 1;
	Color bg = Color.white;
	int edge = 4;
	int edgeRight = 8;
//	boolean enforceMin, enforceMax;
	double minSweetValue, maxSweetValue;

	MesquiteCommand command;
	int enterWidth = 8;
	// be passed max and min values  
	
	public MiniSliderWithText (MesquiteCommand command, double currentValue, double minValue, double maxValue, double minSweetValue, double maxSweetValue) {
		this.currentValue = currentValue;
		this.minSweetValue = minSweetValue;
		this.maxSweetValue = maxSweetValue;
		this.command=command;
		this.minValue = minValue;
		this.maxValue = maxValue;
		initValues();
	}
	/*.................................................................................................................*/
  	 public void useExponentialScale(boolean exp) {
  	 	slider.useExponentialScale(exp);
  	 }
	/*.................................................................................................................*/
  	 public void setText(String t) {
  	 	tf.setText(t);
  	 	currentText = t;
  	 }
	public void setWidth(int w){
		Point loc = getLocation();
		widthSet = w;
		calcTextBoxSize(getGraphics());
		setTotalSize();
		//slider.setLocation(10, textBoxHeight);
		slider.setRangeInPixels(widthSet);
		//valueToPixel.setTotalPixels(slider.sliderRangeInPixels());
	//setLocation(loc);
	}
	/*.................................................................................................................*/
  	 public void setAllowEstimation(boolean allow) {
  	 	allowEstimation = allow;
  	 }
	/*.................................................................................................................*/
  	 public Dimension getPreferredSize() {
  	 	return new Dimension(totalWidth, totalHeight);
  	 }
	/*.................................................................................................................*/
  	 public EnterButton getEnterButton() {
  	 	return enterButton;
  	 }
	/*.................................................................................................................*/
  	 public void setTotalSize() {
		if (textBoxWidth  + EnterButton.MIN_DIMENSION+2 >widthSet)
			totalWidth = textBoxWidth + EnterButton.MIN_DIMENSION+2;
		else
			totalWidth = widthSet  + EnterButton.MIN_DIMENSION+2;
		totalHeight = textBoxHeight + 24;
		setSize(totalWidth, totalHeight);
  	 }
	/*.................................................................................................................*/
	private void initValues(){
		adjustingSizes = true;
		calcTextBoxSize(getGraphics());
		setTotalSize();
		setLayout(null);
		add(slider = new MiniSlider(command, true, currentValue, minValue, maxValue, minSweetValue, maxSweetValue));
		slider.setLocation(10, textBoxHeight);
		slider.setTextToNotify(this);
		slider.setRangeInPixels(widthSet);
		slider.setColor(Color.white);
		slider.setVisible(true);
		add(enterButton = new EnterButton(this, true));
		enterButton.setEnabled(false);
		currentText = MesquiteDouble.toString(currentValue);
		add(tf = new TextField("888888", 2));
			tf.setText(currentText);
		
		tf.addActionListener(this);
		tf.addTextListener(this);
		tf.setVisible(false);
		enterButton.setVisible(false);
		tf.setSize(1, 1);
		tf.setBackground(Color.white);
	/*	add(dummy = new TextField(""));
		dummy.setEditable(false);
		dummy.setSize(0,0);
		dummy.setBackground(bg);
		dummy.setLocation(0,0);
	*/

		recalcPositions(true);
		setBackground(bg);
		adjustingSizes = false;
		//paint();
	}
	private void calcTextBoxSize(Graphics g){
		if (g!=null) {
			Font f = g.getFont();
			FontMetrics fm = g.getFontMetrics(f);
			int sw1 = MesquiteInteger.maximum(fm.stringWidth("888888888888888888"), fm.stringWidth(tf.getText()));  // width of text in text field
			int sw = MesquiteInteger.maximum(sw1, widthSet);   // check if user-specified width is bigger
			int sh = fm.getMaxAscent()+fm.getMaxDescent();
			
			textBoxWidth = sw1 + MesquiteModule.textEdgeCompensationWidth; //34
			textBoxHeight =  sh + MesquiteModule.textEdgeCompensationHeight; //18
			if (tf!= null) {
				if (Math.abs(tf.getBounds().width -  textBoxWidth)>2 || Math.abs(tf.getBounds().height - textBoxHeight)>0){
					tf.setBounds(0, 0, textBoxWidth, textBoxHeight);
				}
				if (enterButton != null) {
					if (Math.abs(enterButton.getLocation().x - ( tf.getBounds().x + tf.getBounds().width + 4))>2 || enterButton.getLocation().y != 2)
						enterButton.setLocation(tf.getBounds().x + tf.getBounds().width + 4, 2);
					enterButton.repaint();
				}
				if (slider != null && (slider.getLocation().x != 10 || Math.abs(slider.getLocation().y - ( tf.getBounds().y+tf.getBounds().height+4))>2))
					slider.setLocation(10, tf.getBounds().y+tf.getBounds().height+4);
			}
		}
	}
	private boolean recalcPositions(boolean doRepaint){
		Graphics g = getGraphics();
		if (g != null){
			adjustingSizes = true;
			calcTextBoxSize(g);
			setTotalSize();
			if (doRepaint)
				repaint();
			if (getParent()!= null)
				getParent().repaint();
			adjustingSizes = false;
			g.dispose();
		}
		return true;
	}
	public void paint(Graphics g) { 
		//recalcPositions(g, false);
	}
	public void repaint() { 
		recalcPositions( false);
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
			//setBackground(Color.green);
			if (tf!=null)
				tf.setBackground(Color.white);
			if (enterButton!=null)
				enterButton.setBackground(bg);
			return true;
		}
		else return false;
	}
	public void setVisible(boolean b) {
		if (b)
			checkBackground();
		super.setVisible(b);
		tf.setVisible(b);
		enterButton.setVisible(b);
		repaint();
	}
	public void setColor(Color c) {
		tf.setForeground(c);
	}
	public MiniSlider getSlider() {  
		return slider;
	}
	public void setMaximumValue (double i) { 
		if (i!=maxValue) {
			maxValue=i;
			slider.setMaximumValue(i);
			if (MesquiteDouble.isCombinable(currentValue) && (currentValue>maxValue))
				setCurrentValue(maxValue);
			else
				setCurrentValue(currentValue); //this ensures draw correctly
		}
	}
	public void setMinimumValue (double i) { 
		if (i!=minValue) {
			minValue=i;
			slider.setMinimumValue(i);
			if (MesquiteDouble.isCombinable(currentValue) && (currentValue<minValue))
				setCurrentValue(minValue);
			else
				setCurrentValue(currentValue); //this ensures draw correctly
		}
	}
	public void setMaximumSweetValue (double i) { 
		if (i!=maxSweetValue) {
			maxSweetValue=i;
			slider.setMaximumSweetValue(i);
		}
	}
	public void setMinimumSweetValue (double i) { 
		if (i!=minSweetValue) {
			minSweetValue=i;
			slider.setMinimumSweetValue(i);
		}
	}
	public void setCurrentNoSliderUpdate (double i) {  
		if (!MesquiteDouble.isCombinable(i))
			return;
		if ((i<=maxValue) && (i>=minValue) && i!=currentValue) {
			currentValue=i;
		}
		tf.setText(MesquiteDouble.toString(currentValue));
	}
	public void setCurrentValue (double i) {  
		if (!MesquiteDouble.isCombinable(i))
			return;
		if ((i<=maxValue) && (i>=minValue) && i!=currentValue) {
			currentValue=i;
		}
		slider.setCurrentValue(i);
		tf.setText(MesquiteDouble.toString(currentValue));
		enterButton.setEnabled(false);
	}
	public MesquiteNumber getMaximumValue () { 
			return  new MesquiteNumber(maxValue);
	}
	public MesquiteNumber getMinimumValue () { 
			return  new MesquiteNumber(minValue);
	}
	public MesquiteNumber getMaximumSweetValue () { 
			return  new MesquiteNumber(maxSweetValue);
	}
	public MesquiteNumber getMinimumSweetValue () { 
			return  new MesquiteNumber(minSweetValue);
	}
	public MesquiteNumber getCurrentValue () {  
			return  new MesquiteNumber(currentValue);
	}
	public boolean textValid(String s){
		double d = MesquiteDouble.fromString(s);
		if (MesquiteString.explicitlyUnassigned(s) && allowEstimation)
			return true;
		if (!MesquiteDouble.isCombinable(d))
			return false;
		if (d>maxValue) {
			MesquiteTrunk.mesquiteTrunk.alert("That value (" + d + ") is above the maximum currently allowed (" + maxValue + ")");
			return false;
		}
		else if (d<minValue) {
			MesquiteTrunk.mesquiteTrunk.alert("That value (" + d + ") is below the minimum currently allowed (" + minValue + ")");
			return false;
		}
		return true;
	}
	 public void textValueChanged(TextEvent e) {
	 	String s = tf.getText();
	 	double d = MesquiteDouble.fromString(s);
	 	if (!slider.getSliderIsMoving() && (d!=currentValue)) {
	 		enterButton.setEnabled(textValid(s));
	 	}
	 	else
	 		enterButton.setEnabled(false);
	}
	public void acceptText(){
		if (MesquiteWindow.getQueryMode(this)) {
			MesquiteWindow.respondToQueryMode("Mini slider", command, this);
			return;
		}
		String s = tf.getText();
		if (MesquiteString.explicitlyUnassigned(s) && allowEstimation){
			currentValue=MesquiteDouble.unassigned;
			slider.setCurrentValue(currentValue);
			command.doItMainThread(s, CommandChecker.getQueryModeString("Mini slider", command, this), this);
			return;
		}
		if (!textValid(s))
			return;
		currentText=s;
		double d = MesquiteDouble.fromString(s);
		setCurrentValue(d);
		//enterButton.repaint();
		command.doItMainThread(MesquiteDouble.toString(d), CommandChecker.getQueryModeString("Mini slider", command, this), this);
		enterButton.setEnabled(false);
	}
	public void actionPerformed(ActionEvent e){
		//Event queue
			acceptText();
	}
	public String getExplanation(){ //TODO: this should use string passed in constructor
		return "This is a number control with text entry and slider";
	}
	public String getImagePath(){ //TODO: this should use path to standard image
		return null;
	}
	
}



