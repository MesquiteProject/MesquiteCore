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
import mesquite.lib.simplicity.InterfaceManager;
/* ======================================================================== */
/** A panel at the bottom of windows in which explanations and footnotes can be displayed and edited.*/
public class ExplanationArea extends MousePanel implements TextListener, MesquiteListener, FocusListener {
	private ExplTextArea explTextArea;
	ExplanationControl control;
	static final int grabberHeight = 0;
	Annotatable annotatable = null;
	MesquiteWindow window;
	int suppressNotify = 0;
	int controlWidth = 36;
	static final int increment = 20;
	static final int minimumHeightExplanation = 30;
	static final int minimumHeightAnnotation = 20;
	boolean focusSuppressed = false;
	boolean hasFocus = false;
	int fontIncrement = 0;
	public static Image plusImage, minusImage, minusOffImage;
	boolean isAnnotation = false;
	public ExplanationArea (MesquiteWindow window, boolean isAnnotation) {
		super();

		setLayout(null);
		this.isAnnotation = isAnnotation;
		this.window = window;


		explTextArea = new ExplTextArea("", 2, 8, TextArea.SCROLLBARS_VERTICAL_ONLY, this); 
		add(explTextArea);
		explTextArea.setBounds(controlWidth, 0, getBounds().width-controlWidth, getBounds().height - grabberHeight);
		explTextArea.setVisible(true);
		explTextArea.addFocusListener(this);


		control = new ExplanationControl(this);
		controlWidth= control.getMinimumWidth();
		add(control);
		control.setBounds(0, 0, controlWidth, getBounds().height);

		control.setVisible(true);
		control.repaint(); //attempt to deal with bug in OS X 10.2
		explTextArea.addTextListener(this);

		control.setBackground(ColorTheme.getInterfaceBackground());
		requestFocusInWindow();
	}
	public void dispose(){
		if (control !=null)
			control.dispose();
		annotatable = null;
		super.dispose();
	}
	public TextArea getTextArea(){
		return explTextArea;
	}
	String name;
	public void setName(String name){  //for Debugging 
		this.name = name;
	}
	public void focusGained(FocusEvent e){
		if (e.getSource() == this)
			hasFocus = true;
	}
	public void focusLost(FocusEvent e){
		if (e.getSource() == this)
			hasFocus = false;
	}
	long lastTick = -1;
	boolean showClock = false;
	String explanationSet = "";
	String tickString = null;
	public void tickClock(String tickString){
		if (tickString == null)
			tickString = "";
		this.tickString = tickString;

		control.showClock = true;
		if (lastTick < 0 || (lastTick > 0 && System.currentTimeMillis() - lastTick > 200)){

			if (explTextArea != null)
				explTextArea.setText(tickString);

			if (lastTick < 0)
				control.clockCount = 0;
			else
				control.clockCount++;
			control.repaint();
			lastTick = System.currentTimeMillis();
		}

	}
	public void hideClock(){
		this.tickString = null;
		control.showClock = false;

		if (explTextArea != null){
			if (annotatable == null)
				explTextArea.setText(explanationSet);
			else
				explTextArea.setText(annotatable.getAnnotation());

		}

		lastTick = -1;
		control.repaint();
	}
	void resetFont(){
		Font currentFont = window.getCurrentFont();
		if (currentFont != null){
			Font fontToSet = new Font (currentFont.getName(), currentFont.getStyle(), currentFont.getSize() + fontIncrement);
			setFont(fontToSet);
			repaint();
		}
	}
	public void paint (Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		g.setColor(Color.black);
		int middle = getBounds().width/2;
		int grabberTop = getBounds().height - grabberHeight;
		if (lastTouched>=0)
			g.fillRect(middle-10, grabberTop-8,20, 20);
		g.drawLine(0, 0,getBounds().width, 0);
		g.setColor(Color.darkGray);
		g.drawLine(middle-8, grabberTop,middle + 8, grabberTop);
		g.drawLine(middle-8, grabberTop + 2,middle + 8, grabberTop + 2);
		//g.drawLine(middle-8, 2,middle + 8, 2);
		MesquiteWindow.uncheckDoomed(this);
	}
	public void setBackground(Color c){
		explTextArea.setBackground(c);  
		super.setBackground(c);
	}
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x,y,w,h);
		explTextArea.setBounds(controlWidth, 0, w-controlWidth, h - grabberHeight);
		control.setBounds(0, 0, controlWidth, h);
	}
	public void setSize(int w, int h){
		super.setSize(w,h);
		explTextArea.setSize(w-controlWidth, h - grabberHeight);
		control.setSize(controlWidth, h);
	}
	public boolean getIsAnnotation(){
		return isAnnotation;
	}
	public boolean isMinimumHeight(){
		if (!isAnnotation)
			return (window.explanationHeight <=  minimumHeightExplanation);
		else 
			return (window.annotationHeight <=  minimumHeightAnnotation);
	}
	public void plus(){
		if (!isAnnotation){
			int willBe = window.explanationHeight + increment;
			if (willBe < minimumHeightExplanation)
				willBe = minimumHeightExplanation;
			window.setShowExplanation(true, willBe);
		}
		else {
			int willBe = window.annotationHeight + increment;
			if (willBe < minimumHeightAnnotation)
				willBe = minimumHeightAnnotation;
			window.setShowAnnotation(true, willBe);
		}
	}
	public void minus(){
		if (!isAnnotation){
			int willBe = window.explanationHeight - increment;
			if (willBe < minimumHeightExplanation)
				willBe = minimumHeightExplanation;
			window.setShowExplanation(true, willBe);
		}
		else {
			int willBe = window.annotationHeight - increment;
			if (willBe < minimumHeightAnnotation)
				willBe = minimumHeightAnnotation;
			window.setShowAnnotation(true, willBe);
		}
	}
	public void textValueChanged(TextEvent e){
		if (explTextArea !=null && annotatable != null) {
			if (suppressNotify>0){
				suppressNotify--;
			}
			else {

				String s = explTextArea.getText();
				int start = explTextArea.getSelectionStart();
				int end = explTextArea.getSelectionEnd();
				if ("".equals(s))
					s = null;
				annotatable.setAnnotation(s, suppressNotify == 0);
				explTextArea.setSelectionStart(start);
				explTextArea.setSelectionEnd(end);
			}

		}
	}
	public void setFocusSuppression(boolean suppress){
		this.focusSuppressed = suppress;
		if (suppress && explTextArea.isEditable()){
			explTextArea.setEditable(false);
			control.setEditable(false);
		}
	}
	public boolean getFocusSuppression(){
		return focusSuppressed;
	}
	public void setExplanation(String text){
		if (text == null)  
			text = "";
		annotatable = null;
		String current = explTextArea.getText();
		explanationSet = text;
		if (text!=null && current!=null && !text.equals(current)){
			explTextArea.setText(text);
		}
		if (explTextArea.isEditable()){
			explTextArea.setEditable(false);
			control.setEditable(false);
		}

	}

	public void setExplanation(Annotatable annotatable){
		String current = explTextArea.getText();
		if (annotatable != this.annotatable){
			if (this.annotatable!=null && this.annotatable instanceof Listened)
				((Listened)this.annotatable).removeListener(this);
			this.annotatable = annotatable;
			if (annotatable !=null && annotatable instanceof Listened)
				((Listened)annotatable).addListener(this);
		}

		if (annotatable == null) {
			setExplanation("");
			return;
		}

		String newText = annotatable.getAnnotation();
		if (newText == null)
			newText = "";
		if (current!=null && !newText.equals(current))
			explTextArea.setText(newText);
		if (explTextArea.isEditable() != (isAnnotation && !focusSuppressed)){
			explTextArea.setEditable(isAnnotation && !focusSuppressed);
			control.setEditable(isAnnotation && !focusSuppressed);
		}
		/*
		if (current!=null && !newText.equals(current))
			debuggingPanel.setText(newText);
		if (debuggingPanel.isEditable() != (isAnnotation && !focusSuppressed)){
			debuggingPanel.setEditable(isAnnotation && !focusSuppressed);
			control.setEditable(isAnnotation && !focusSuppressed);
		}
		 */

	}
	public Annotatable getAnnotatable(){
		return annotatable;
	}
	/*.................................................................................................................*/
	/** passes which object changed, along with optional code number (type of change) and integers (e.g. which character)*/
	public void changed(Object caller, Object obj, Notification notification){
		if (obj == annotatable && Notification.getCode(notification) == MesquiteListener.ANNOTATION_CHANGED){
			suppressNotify++;
			if (explTextArea != null && annotatable != null)
				explTextArea.setText(annotatable.getAnnotation());
			/*	if (debuggingPanel != null && annotatable != null)
				debuggingPanel.setText(annotatable.getAnnotation());
			 */
		}
	}
	/** passes which object was disposed*/
	public void disposing(Object obj){
	}
	/** Asks whether it's ok to delete the object as far as the listener is concerned (e.g., is it in use?)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;
	}
	public String getExplanation(){
		return explTextArea.getText();
	}

	int lastTouched = -1;
	/*.................................................................................................................*/
	public void mouseDown(int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		int middle = getBounds().width/2;
		if (x>middle-8 && x<middle+8 && y>getBounds().height - grabberHeight-8){
			lastTouched = y;
			repaint();
		}
		MesquiteWindow.uncheckDoomed(this);
	}
	/*.................................................................................................................*/
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		if (lastTouched>=0) {
			if (window.explanationHeight - (lastTouched-y) > 24)
				window.setShowExplanation(true, window.explanationHeight- (lastTouched-y));
			else
				window.setShowExplanation(true, 24);
			repaint();
		}
		lastTouched = -1;
		MesquiteWindow.uncheckDoomed(this);
	}
}

class ExplanationControl extends MousePanel {
	ExplanationArea area;
	static int buttonHeight = 13;
	static int buttonWidth = 16;
	static int buttonSidePadding = 3;
	static int buttonMiddlePadding = 2;
	boolean showClock = false;
	int clockCount=0;
	int roundClock = 16;
	int clockRadius = 8;
	double clockMult = Math.PI *2.0/ roundClock;
	//int plusTop = 4;
	//int minusTop = 20;
	boolean editable = false;
	public ExplanationControl (ExplanationArea area){
		this.area = area;
	}
	public void dispose(){
		super.dispose();
	}
	public void paint(Graphics g){
		if (MesquiteWindow.checkDoomed(this))
			return;
		g.setColor(Color.black);
		g.drawRect(0,0,getBounds().width-1, getBounds().height -1);
		if (editable){
			g.setColor(Color.green);
			g.drawRect(1,1,getBounds().width-3, getBounds().height -3);
			g.drawRect(2,2,getBounds().width-5, getBounds().height -5);
		}
		if (showClock){
			clockRadius = 6;
			if (clockCount > roundClock)
				clockCount = 0;
			g.setColor(Color.white);
			g.fillOval(minusLeft(), minusTop(), clockRadius+clockRadius, clockRadius+clockRadius);
			g.setColor(Color.black);
			g.drawOval(minusLeft(), minusTop(), clockRadius+clockRadius, clockRadius+clockRadius);
			g.drawOval(minusLeft()+1, minusTop()+1, clockRadius+clockRadius-2, clockRadius+clockRadius-2);
			g.fillArc(minusLeft()+1, minusTop()+1, clockRadius+clockRadius-2, clockRadius+clockRadius-2, 360 -  clockCount*360/roundClock, 30);
		}
		else {
			if (ExplanationArea.plusImage !=null)
				g.drawImage(ExplanationArea.plusImage,plusLeft(),plusTop(), this);
			if (!area.isMinimumHeight()) {
				if (ExplanationArea.minusImage!=null)
					g.drawImage(ExplanationArea.minusImage,minusLeft(),minusTop(), this);
			}
			else {
				if (ExplanationArea.minusOffImage!=null)
					g.drawImage(ExplanationArea.minusOffImage,minusLeft(),minusTop(), this);
			}
		}
		MesquiteWindow.uncheckDoomed(this);
	}
	/*.................................................................................................................*/
	public static int getMinimumWidth(){
		return (buttonSidePadding+buttonWidth)*2; //buttonSidePadding*2+buttonWidth*2+buttonMiddlePadding;
	}
	/*.................................................................................................................*/
	public int minusLeft(){
		return buttonSidePadding;
	}
	/*.................................................................................................................*/
	public int plusLeft(){
		return buttonSidePadding+(buttonWidth); //buttonSidePadding+buttonWidth+buttonMiddlePadding;
	}
	/*.................................................................................................................*/
	public int plusTop(){
		return buttonSidePadding; //getBounds().height - buttonHeight-buttonSidePadding;
	}
	/*.................................................................................................................*/
	public int minusTop(){
		return buttonSidePadding; //getBounds().height - buttonHeight-buttonSidePadding;
	}
	/*.................................................................................................................*/
	public boolean inPlusButton(int x, int y) {
		return (x>=plusLeft() && x<plusLeft()+buttonWidth && y>=plusTop() &&  y<=plusTop() + buttonHeight);
	}
	/*.................................................................................................................*/
	public boolean inMinusButton(int x, int y) {
		return  (x>=minusLeft() && x<minusLeft()+buttonWidth && y>=minusTop() &&  y<=minusTop() + buttonHeight);
	}
	private void font(int inc){
		area.fontIncrement += inc;
		area.resetFont();
	}
	/*.................................................................................................................*/
	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
		String s="";
		String name="";
		if (area.getIsAnnotation())
			name ="the annotation area above";
		else
			name="this explanation area";
		if (area.tickString != null){
			s = area.tickString;
		}
		else if (inPlusButton(x,y)) {
			s += "This button will increase the height of "+name+".   (With modifier keys or right click, will increase font size.)";
		}
		else if (inMinusButton(x,y)) {
			s += "This button will decrease the height of "+name+". ";
			if (area.isMinimumHeight())
				s+= "It is disabled as the area is already at its minimum height.";
			s += "  (With modifier keys or right click, will decrease font size.)";	
		}
		MesquiteWindow.windowOfItem(this).setExplanation(s);
		super.mouseMoved(modifiers,x,y,tool);
	}
	/*.................................................................................................................*/
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		if (inPlusButton(x,y)) {
			if (MesquiteEvent.commandOrControlKeyDown(modifiers) || MesquiteEvent.optionKeyDown(modifiers))
				font(1);
			else
				area.plus();
		}
		else if (inMinusButton(x,y)) {
			if (MesquiteEvent.commandOrControlKeyDown(modifiers) || MesquiteEvent.optionKeyDown(modifiers))
				font(-1);
			else
				area.minus();
		}
		MesquiteWindow.uncheckDoomed(this);
	}
	public void setEditable(boolean b){
		boolean changed = (b!=editable);
		repaint();
		this.editable = b;
		if (changed)
			repaint();
	}
}

class ExplTextArea extends TextArea {
	ExplanationArea explArea;
	public ExplTextArea(String text, int rows,  int columns, int scrollbars, ExplanationArea explArea){
		super(text, rows, columns, scrollbars);
		setSelectionStart(0);
		setSelectionEnd(0);
		this.explArea = explArea;
	}
	public void gotFocus(){
		if (explArea.getFocusSuppression()){
			explArea.window.requestFocus();
		}
		else
			explArea.hasFocus = true;
	}

	public void setText(String t){  // and possibly others
		try {
			if (MesquiteTrunk.isMacOSX()){  //this had been a workaround to bug in OS X Snow Leopard, but it slowed alignment too much
				setSelectionStart(0);
				setSelectionEnd(0);
			}
			super.setText(t);
			if (MesquiteTrunk.isMacOSX()){
				setSelectionStart(0);
				setSelectionEnd(0);
			}
		}
		catch (Throwable e){
			//This is to catch ClassCastExceptions on Linux deep in java 1.8 code
		}
	}

	public void processFocusEvent(FocusEvent e) {
		super.processFocusEvent(e);
		if (e.getID() == FocusEvent.FOCUS_GAINED && explArea.getFocusSuppression())
			explArea.window.graphicsRequestFocus();
		else if (e.getID() == FocusEvent.FOCUS_GAINED)
			explArea.hasFocus = true;
		if (e.getID() == FocusEvent.FOCUS_LOST)
			explArea.hasFocus = false;
	}
}
