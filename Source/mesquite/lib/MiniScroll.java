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
/** A miniature scroller used in the tree window and trace character legends.  It provides
an incrementing and decrementing arrow, and a text field to show current value. */
public class MiniScroll extends MousePanel implements MiniControl, Explainable, ImageOwner, ActionListener, TextListener {
	public long currentValue, minValue, maxValue;
	ScrollTextField tf;
	TextField dummy;
	MiniScrollButton decrementButton, incrementButton;
	String itemName="";
	EnterButton enterButton;
	private int textBoxWidth = 34;
	private int textBoxHeight = 18;
	private int arrowHeight = 26;
	public int totalWidth = 0;  
	public int totalHeight=0; 
	private int oldTextBoxWidth = 1;
	private int oldTextBoxHeight = 1;
	Color bg = Color.white;

	public boolean stacked = false;
	public boolean horizontal = false;
	MesquiteCommand command;
	//int enterWidth = 8;
	// be passed max and min values  
	
	public MiniScroll (MesquiteCommand command, boolean stacked, int currentValue, int minValue, int maxValue, String itemName) {
		this(command, stacked, true, (long)currentValue,  (long)minValue,  (long)maxValue, itemName);
	}
	public MiniScroll (MesquiteCommand command, boolean stacked, boolean horizontal, int currentValue, int minValue, int maxValue, String itemName) {
		this(command, stacked, horizontal,  (long)currentValue,  (long)minValue,  (long)maxValue, itemName);
	}
	public MiniScroll (MesquiteCommand command, boolean stacked, long currentValue, long minValue, long maxValue, String itemName) {
		this(command, stacked, true, currentValue, minValue, maxValue, itemName);
	}
	
	public MiniScroll (MesquiteCommand command, boolean stacked, boolean horizontal,long currentValue, long minValue, long maxValue, String itemName) {
		this.currentValue = currentValue;
		this.itemName = itemName;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.stacked = stacked; 
		this.horizontal = horizontal; 
		this.command=command;
		if (!horizontal && !stacked){
			totalWidth = textBoxWidth + 10;
			totalHeight = textBoxHeight + 40 + EnterButton.MIN_DIMENSION+2;
		}
		else if (stacked) {
			totalWidth = textBoxWidth  + EnterButton.MIN_DIMENSION+2;
			totalHeight = textBoxHeight + 28;
		}
		else  {
			totalWidth = textBoxWidth + 40 + EnterButton.MIN_DIMENSION+2;
			totalHeight = textBoxHeight + 2;
		}
		setSize(totalWidth, totalHeight);
		setLayout(null);
		if (this.horizontal){
			add(decrementButton= new MiniScrollButton(this, MiniScrollButton.LEFT, itemName));
			add(incrementButton= new MiniScrollButton(this, MiniScrollButton.RIGHT, itemName));
		}
		else {
			add(decrementButton= new MiniScrollButton(this, MiniScrollButton.DOWN, itemName));
			add(incrementButton= new MiniScrollButton(this, MiniScrollButton.UP, itemName));
		}
		add(enterButton = new EnterButton(this, horizontal || stacked));
		add(tf = new ScrollTextField(this, Long.toString(currentValue), 2));
		tf.setVisible(false);
		tf.addActionListener(this);
		tf.addTextListener(this);
		tf.setSize(5, 5);
		tf.setLocation(5,5);

		add(dummy = new TextField(""));
		dummy.setEditable(false);
		dummy.setSize(0,0);
		dummy.setBackground(bg);
		dummy.setLocation(0,0);

		decrementButton.setVisible(false);
		incrementButton.setVisible(false);
		enterButton.setVisible(false);
		if (horizontal){
			if (stacked) {
				tf.setSize(1, 1);
				decrementButton.setLocation(2,arrowHeight);
				incrementButton.setLocation(totalWidth-20,arrowHeight);
			}
			else {
				tf.setSize( 1, 1);
				decrementButton.setLocation(0,2);
				tf.setLocation(decrementButton.getBounds().x + decrementButton.getBounds().width, 2);
				incrementButton.setLocation(tf.getBounds().x + tf.getBounds().width + EnterButton.MIN_DIMENSION+1,2);
			}
		}
		else {
			if (stacked) {
				tf.setSize(1, 1);
				decrementButton.setLocation(2,arrowHeight);
				incrementButton.setLocation(totalWidth-26,arrowHeight);
			}
			else {
				tf.setSize( 1, 1);
				incrementButton.setLocation(0,0);
				tf.setLocation(0, decrementButton.getBounds().height+6);
				decrementButton.setLocation(0, tf.getBounds().y + tf.getBounds().height + EnterButton.MIN_DIMENSION+1);
			}
		}

		tf.setBackground(bg);
		setBackground(bg);
		enterButton.setEnabled(false);
		decrementButton.setEnabled(currentValue>minValue);
		incrementButton.setEnabled(currentValue<maxValue);
	}
	public int getTotalHeight(){
		return totalHeight;
	}
	public int getTotalWidth(){
		return totalWidth;
	}
	Font oldFont = null;
	String text=null;
	String oldText=null;
	int sw = 0;
	int sh = 0;
	
	private boolean recalcPositions(Graphics g){
		Font f =  g.getFont();
		if (f != oldFont || !(StringUtil.stringsEqual(oldText, text))){
			FontMetrics fm = g.getFontMetrics(f);
			sw = MesquiteInteger.maximum(fm.stringWidth("888"), fm.stringWidth(tf.getText()));
			sh = fm.getMaxAscent()+fm.getMaxDescent();
			
		}
		oldText = text;
		oldFont = f;
		if (oldTextBoxWidth < sw + MesquiteModule.textEdgeCompensationWidth -1 || oldTextBoxHeight < sh + MesquiteModule.textEdgeCompensationHeight -1){
			textBoxWidth = sw + MesquiteModule.textEdgeCompensationWidth; //34
			textBoxHeight =  sh + MesquiteModule.textEdgeCompensationHeight; //18
			oldTextBoxWidth = textBoxWidth;
			oldTextBoxHeight = textBoxHeight;
			if (horizontal){
				if (stacked) {
					totalWidth = textBoxWidth +  EnterButton.MIN_DIMENSION+2;
					totalHeight = textBoxHeight + 20;
					setSize(totalWidth, totalHeight);
					tf.setSize(textBoxWidth, textBoxHeight);
					tf.setLocation(0, 0);
					decrementButton.setLocation(2,textBoxHeight+4);
					incrementButton.setLocation(totalWidth-20,textBoxHeight+4);
				}
				else  {
					totalWidth = textBoxWidth +36  +  EnterButton.MIN_DIMENSION+2;
					totalHeight = textBoxHeight+4;
					setSize(totalWidth, totalHeight);
					tf.setSize( textBoxWidth, textBoxHeight);
					decrementButton.setLocation(0,2);
					tf.setLocation(decrementButton.getBounds().x + decrementButton.getBounds().width + 1, 0);  // 1 added to x
					incrementButton.setLocation(tf.getBounds().x + tf.getBounds().width + EnterButton.MIN_DIMENSION+2,2);  //1 added to x
				}
				enterButton.setLocation(tf.getBounds().x + tf.getBounds().width+1, 2);
			}
			else {
				if (stacked) {
					totalWidth = textBoxWidth +  EnterButton.MIN_DIMENSION+2;
					totalHeight = textBoxHeight + 20;
					setSize(totalWidth, totalHeight);
					tf.setSize(textBoxWidth, textBoxHeight);
					tf.setLocation(0, 0);
					decrementButton.setLocation(2,textBoxHeight + 4);
					incrementButton.setLocation(totalWidth-22,textBoxHeight+4);	
					enterButton.setLocation(tf.getBounds().x + tf.getBounds().width+1, 2);
				}
				else  {
					totalWidth = textBoxWidth; //should be 0
					totalHeight = textBoxHeight + 36 +  EnterButton.MIN_DIMENSION+2;

					setSize(totalWidth, totalHeight);
					tf.setSize( textBoxWidth, textBoxHeight);
					incrementButton.setLocation(1,2);
					tf.setLocation(0, incrementButton.getBounds().height+3);
					decrementButton.setLocation(1, tf.getBounds().y + tf.getBounds().height + EnterButton.MIN_DIMENSION+2);
					enterButton.setLocation(1, tf.getBounds().y + tf.getBounds().height+1);
				}
			}
			incrementButton.repaint();
			decrementButton.repaint();
			enterButton.repaint();
			repaint();
			getParent().repaint();
			return true;
		}
		if (checkBackground()){
			repaint();
			return true;
		}
	/*	if (!getBackground().equals(getParent().getBackground())) {
			bg =getParent().getBackground();
			setBackground(bg);
			//setBackground(Color.green);
			tf.setBackground(bg);
			decrementButton.setBackground(bg);
			incrementButton.setBackground(bg);
			enterButton.setBackground(bg);
			repaint();
			return true;
		}*/
		return false;
	}
	
	
	public void repaint(){
		text = tf.getText();
		Graphics g = getGraphics();
		if (g!= null){
			recalcPositions(g);
			g.dispose();
		}
		super.repaint();
	}
	boolean neverCalculated = true;
	public void paint(Graphics g) { //^^^
		if (g instanceof PrintGraphics)
			return;
	   	if (MesquiteWindow.checkDoomed(this))
	   		return;
		/**/
		if (getParent()==null ) {
			MesquiteWindow.uncheckDoomed(this);
			return;
		}
		if (neverCalculated){
			if (recalcPositions(g)){
				MesquiteWindow.uncheckDoomed(this);
				neverCalculated = false;
				return;
			}
		}
		/**/	
		/* This stuff below seems unnecessary, but for some reason on MacOS 8.1/MRJ 2.0 it was leaving garbage around; likewise for components*/
		g.setColor(getBackground());
		g.fillRect(getBounds().x,getBounds().y,getBounds().width,getBounds().height);
		g.setColor(Color.black);
		//tf.repaint();
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
			//setBackground(Color.green);
			if (tf!=null)
				tf.setBackground(bg);
			if (decrementButton!=null)
				decrementButton.setBackground(bg);
			if (incrementButton!=null)
				incrementButton.setBackground(bg);
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
		decrementButton.setVisible(b);
		incrementButton.setVisible(b);
		enterButton.setVisible(b);
		repaint();
	}
	public void setColor(Color c) {
		tf.setForeground(c);
	}
	boolean enterLock = false;
	public void setEnterLock(boolean lock){
		enterLock = lock;
		if (!lock){
			boolean b = (currentValue<=maxValue && currentValue>=minValue);
			enterButton.setEnabled(b);
		}
		else
			enterButton.setEnabled(false);
	}
	public void setEnableEnter(boolean en){
		if (enterLock)
			return;
		enterButton.setEnabled(en);
	}
	public void setMaximumValue (int i) { 
		if (i!=maxValue) {
			maxValue=i;
			incrementButton.setEnabled(currentValue<maxValue);
		}
	}
	public void setMinimumValue (int i) { 
		if (i!=minValue) {
			minValue=i;
			decrementButton.setEnabled(currentValue>minValue);
		}
	}
	public void setMaximumValueLong (long i) { 
		if (i!=maxValue) {
			maxValue=i;
			incrementButton.setEnabled(currentValue<maxValue);
		}
	}
	public void setMinimumValueLong (long i) { 
		if (i!=minValue) {
			minValue=i;
			decrementButton.setEnabled(currentValue>minValue);
		}
	}

	public void setCurrentValue (int i) {  
		if (i<=maxValue && (i>=minValue)) {
			currentValue=i;
			tf.setText(Integer.toString((int)currentValue));
			decrementButton.setEnabled(currentValue>minValue);
			incrementButton.setEnabled(currentValue<maxValue);
		}
	}
	public int getCurrentValue () {  
		return (int)currentValue;
	}
	public void setCurrentValueLong (long i) {  
		if (i<=maxValue && (i>=minValue)) {
			currentValue=i;
			tf.setText(Long.toString(currentValue));
			decrementButton.setEnabled(currentValue>minValue);
			incrementButton.setEnabled(currentValue<maxValue);
		}
	}
	public long getCurrentValueLong () {  
		return currentValue;
	}

	public void acceptText(){
		String s = tf.getText();
		oldText = null;
		text = s;
		try {
			int value = MesquiteInteger.fromString(s);
			crement(value);
		}
		catch (NumberFormatException e){}
	}
	public void actionPerformed(ActionEvent e){
		//Event queue
			acceptText();
	}
	
	 public boolean isTextValid() {
	 	boolean b = false;
		String s = tf.getText();
		try {
			int value = MesquiteInteger.fromString(s);
			b = (value<=maxValue && value>=minValue);
		}
		catch (NumberFormatException e){}
		return b;
	}
	 public void textValueChanged(TextEvent e) {
		boolean b = false;
		String s = tf.getText();
		try {
			int value = MesquiteInteger.fromString(s);
			b = (value<=maxValue && value>=minValue && value!=currentValue);
		}
		catch (NumberFormatException ex){}
	 	enterButton.setEnabled(b && !enterLock);
	}


	public void crement (int i) {  
		if (MesquiteWindow.getQueryMode(this)) {
			MesquiteWindow.respondToQueryMode("Mini scroll", command, this);
			return;
		}
		if (i<=maxValue && (i>=minValue) && command!=null) {
			currentValue=i;
			command.doItMainThread(Long.toString(currentValue), CommandChecker.getQueryModeString("Mini scroll", command, this), this);
			tf.setText(Long.toString(currentValue));
			decrementButton.setEnabled(i>minValue);
			incrementButton.setEnabled(i<maxValue);
		}
	}
	public void increment () {  // have interface incrementable and pass object to this miniscroll so it can notify object
 		if (MesquiteWindow.getQueryMode(this)) {
			MesquiteWindow.respondToQueryMode("Mini scroll", command, this);
			return;
		}
		if (currentValue<maxValue && command!=null) {
			currentValue++;
			command.doItMainThread(Long.toString(currentValue), CommandChecker.getQueryModeString("Mini scroll", command, this), this);
			tf.setText(Long.toString(currentValue));
			enterButton.setEnabled(false);
			decrementButton.setEnabled(currentValue>minValue);
			incrementButton.setEnabled(currentValue<maxValue);
		}
	}
	public void decrement () {
		if (MesquiteWindow.getQueryMode(this)) {
			MesquiteWindow.respondToQueryMode("Mini scroll", command, this);
			return;
		}
		if (currentValue>minValue && command!=null) {
			currentValue--;
			tf.setText(Long.toString(currentValue));
			command.doItMainThread(Long.toString(currentValue), CommandChecker.getQueryModeString("Mini scroll", command, this), this);
			enterButton.setEnabled(false);
			decrementButton.setEnabled(currentValue>minValue);
			incrementButton.setEnabled(currentValue<maxValue);
		}
	}
	public String getExplanation(){ //TODO: this should use string passed in constructor
		return "This is a mini-scroll";
	}
	public String getImagePath(){ //TODO: this should use path to standard image
		if (stacked)
			return MesquiteModule.getRootPath() + "images" + MesquiteFile.fileSeparator + "miniscrollVert.gif"; 
		else
			return MesquiteModule.getRootPath() + "images" + MesquiteFile.fileSeparator + "miniscrollHoriz.gif"; 
	}
	
}




