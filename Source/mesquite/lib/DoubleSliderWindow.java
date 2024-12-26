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
/* ======================================================================== */
public class DoubleSliderWindow extends MesquiteWindow {
	TextField tF1,tF2;
	Label label1, label2;
	MiniSliderWithText slider1, slider2;
	MesquiteMenuItemSpec minItem, maxItem;
	GridBagLayout gridBag;
	GridBagConstraints constraints;
	String title ="Slider";
	static int insetFromWindow = 40;
	static int minWindowWidth = 360;
	boolean allowEstimation=false;
	protected Panel sliderArea;

	public DoubleSliderWindow(MesquiteModule module, String title, String name1, MesquiteCommand command1, double initialValue1, double min1, double max1,  double minSweet1, double maxSweet1, String name2, MesquiteCommand command2, double initialValue2, double min2, double max2, double minSweet2, double maxSweet2) {
		super(module, true);
		setVisible(false);
		setPreferredPopoutWidth(300);
		setWindowSize(minWindowWidth, 240);

		this.title = title;

		//Panel contents = getGraphicsArea();
		sliderArea = new Panel();
		addToWindow(sliderArea);
		sliderArea.setVisible(true);
		sliderArea.setBounds(0, 0, minWindowWidth, 220);
	        gridBag = new GridBagLayout();
	        constraints = new GridBagConstraints();
	        constraints.gridx=1;
	       	constraints.gridy = 1;
	        constraints.weightx=1;
	        constraints.weighty=1;
	        constraints.ipadx=1;
	        constraints.ipady=1;
	        constraints.insets=new Insets(0,8,0,8);
	        constraints.fill=GridBagConstraints.BOTH;
	     	constraints.anchor=GridBagConstraints.CENTER;
		sliderArea.setLayout(gridBag);
		
		label1 = new Label();
   	 	if (!StringUtil.blank(name1))
   	 		label1.setText(name1);
		sliderArea.add(label1,constraints);

		slider1 = new MiniSliderWithText(command1, initialValue1, min1, max1, minSweet1, maxSweet1);
	       	constraints.gridy ++;
		sliderArea.add(slider1,constraints);
		slider1.setWidth(getBounds().width-insetFromWindow);
		slider1.setVisible(true);

		label2 = new Label();
   	 	if (!StringUtil.blank(name2))
   	 		label2.setText(name2);
		constraints.gridy ++;
		sliderArea.add(label2,constraints);

		slider2 = new MiniSliderWithText(command2, initialValue2, min2, max2, minSweet2, maxSweet2);
		constraints.gridy ++;
		sliderArea.add(slider2, constraints);
		slider2.setWidth(getBounds().width-insetFromWindow);
		slider2.setVisible(true);
		//ok.setBackground(Color.white);
		/* *
		Panel buttons = new Panel();
		buttons.setBackground(Color.green);
		sliderArea.add(buttons);
		Checkbox ok;
		buttons.add("South", ok = new Checkbox("Flat Prior", false));
		Font df = new Font("Dialog", Font.PLAIN, 12);
		ok.setFont(df);
	/**/
	
		//setVisible(true);  Windows should set themselves visible!!!!
		resetTitle();
		slider1.getEnterButton().setEnabled(false);
		slider2.getEnterButton().setEnabled(false);
	}
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree lists, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		setTitle(title);
	}
	
	/*.................................................................................................................*/
  	 public void useExponentialScale(boolean exp) {
  	 	slider1.useExponentialScale(exp);
  	 	slider2.useExponentialScale(exp);
  	 }
	/*.................................................................................................................*/
  	 public void setAllowEstimation(boolean allow) {
  	 	allowEstimation = allow;
  	 	if (slider1!=null) 
  	 		slider1.setAllowEstimation(allow);
	 	if (slider2!=null) 
  	 		slider2.setAllowEstimation(allow);
  	 }
	/*.................................................................................................................*/
  	 public Dimension getMinimumSize() {
  	 	return new Dimension(minWindowWidth,getWindowHeight());
  	 }
	/*.................................................................................................................*/
  	 public Dimension getMaximumSize() {
  	 	return new Dimension(super.getMaximumSize().width, getWindowHeight());  // forces height to not change
  	 }
	/*.................................................................................................................*/
	public void windowResized(){
		super.windowResized();
		int toFitWidth = getBounds().width-insetFromWindow;
		if (toFitWidth > 400)
			toFitWidth = 400;
		if (sliderArea != null)
			sliderArea.setBounds(0, 0, toFitWidth, getBounds().height);
		if (slider1!= null)
			slider1.setWidth(toFitWidth);
		if (slider2!= null)
			slider2.setWidth(toFitWidth);
	}
	/*.................................................................................................................*/
	public void setColor(Color c){
		setBackground(c);
		label1.setBackground(c);
		label2.setBackground(c);
		Panel contents = getGraphicsArea();
		contents.setBackground(c);
		repaintAll();
	}
	public void setText(int which, String s) {
		if (which==1){
			label1.setText(s);
			label1.repaint();
		}
		else if (which ==2){
			label2.setText(s);
			label2.repaint();
		}
		repaint();
	}
	public String getText(int which) {
		if (which==1)
			return label1.getText();
		else if (which ==2)
			return label2.getText();
		return null;
	}
	
	public void setValue(int which, double d) {
		if (which==1){
			slider1.setCurrentValue(d);
			slider1.repaint();
		}
		else if (which ==2){
			slider2.setCurrentValue(d);
			slider2.repaint();
		}
		repaint();
	}
	public double getCurrentValue(int which) {
		if (which==1)
			return slider1.getCurrentValue().getDoubleValue();
		else if (which ==2)
			return slider2.getCurrentValue().getDoubleValue();
		return 0;
	}
	public void dispose(){
		//getOwnerModule().deleteMenuItem(minItem);
		//getOwnerModule().deleteMenuItem(maxItem);
		super.dispose();
	}
	/*=============*/
	public void buttonHit(String label, Button button) {
		if (label.equalsIgnoreCase("Close")) {
			getOwnerModule().windowGoAway(this);
		}
	}
}


