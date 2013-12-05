/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
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
public class SliderWindow extends MesquiteWindow {
	//TextField tF;
	Label label;
	GridBagLayout gridBag;
	GridBagConstraints constraints;
	MiniSliderWithText sliderWithText;
	MesquiteMenuItemSpec minItem, maxItem;
	boolean allowEstimation=false;
	static int insetFromWindow = 40;
	static int minWindowWidth = 200;
	String title ="Slider";
	double minValue, maxValue;
	double minSweetValue, maxSweetValue;
	Panel contents;
	public SliderWindow(MesquiteModule module, String title, String name, MesquiteCommand command, double initialValue, double min, double max, double minSweet, double maxSweet) {
		super(module, true);
		setWindowSize(minWindowWidth, 100);
		minItem = module.addMenuItem("Slider Core Minimum...", module.makeCommand("setSweetMin", this));
		maxItem = module.addMenuItem("Slider Core Maximum...", module.makeCommand("setSweetMax", this));
		this.title = title;
		contents = getGraphicsArea();
		//contents.setLayout(new BorderLayout());
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

		contents.setLayout(gridBag);

		label = new Label();
		if (!StringUtil.blank(name))
			label.setText(name);
		//label.setBackground(Color.blue);
		contents.add(label,constraints);

		constraints.weighty=2;
		constraints.gridy = 2;
		sliderWithText = new MiniSliderWithText(command, initialValue, min, max, minSweet, maxSweet);
		minValue = min;
		maxValue = max;
		minSweetValue = minSweet;
		maxSweetValue = maxSweet;
		//sliderWithText.setLayout(new BorderLayout());
		contents.add(sliderWithText,constraints);
		//	contents.setBackground(Color.red);
		//	contents.add("Center", sliderWithText);
		int toFitWidth = getBounds().width-insetFromWindow;
		if (toFitWidth > 400)
			toFitWidth = 400;
		sliderWithText.setWidth(toFitWidth);
		sliderWithText.setVisible(true);
		//setVisible(true);  Windows should not set themselves visible!
		resetTitle();
		sliderWithText.getEnterButton().setEnabled(false);
	}
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree lists, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		setTitle(title);
	}

	/*.................................................................................................................*/
	public void windowResized(){
		super.windowResized();
		if (sliderWithText!= null) {
			int toFitWidth = getBounds().width-insetFromWindow;
			if (toFitWidth > 400)
				toFitWidth = 400;
			sliderWithText.setWidth(toFitWidth);

		}
		if (contents != null){
			contents.invalidate();
			contents.validate();
		}
	}
	/*.................................................................................................................*/
	public void useExponentialScale(boolean exp) {
		sliderWithText.useExponentialScale(exp);
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
	public void setAllowEstimation(boolean allow) {
		allowEstimation = allow;
		if (sliderWithText!=null)
			sliderWithText.setAllowEstimation(allow);
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setSweetMin " + sliderWithText.getMinimumSweetValue());
		temp.addLine("setSweetMax " + sliderWithText.getMaximumSweetValue());
		temp.incorporate(super.getSnapshot(file), false);
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the lower boundary for the core area of the slider, over which values change slowly", "[minimum value]", commandName, "setSweetMin")) {
			MesquiteInteger io = new MesquiteInteger(0);
			double x= MesquiteDouble.fromString(arguments, io);
			if (!MesquiteDouble.isCombinable(x)){
				x = MesquiteDouble.queryDouble(MesquiteWindow.windowOfItem(this), "Slider Core Minimum...", "Set lower boundary for slider's core:", sliderWithText.getMinimumSweetValue().getDoubleValue());
			}
			if (MesquiteDouble.isCombinable(x)) 
				if (x>=maxSweetValue)
					MesquiteMessage.notifyUser("Lower boundary cannot be more than the upper boundary.");
				else if (x>minValue || minValue==MesquiteDouble.negInfinite){
					sliderWithText.setMinimumSweetValue(x);
					sliderWithText.repaint();
				}
		}
		else if (checker.compare(this.getClass(), "Sets the upper boundary for the core area of the slider, over which values change slowly", "[maximum value]", commandName, "setSweetMax")) {
			MesquiteInteger io = new MesquiteInteger(0);
			double x= MesquiteDouble.fromString(arguments, io);
			if (!MesquiteDouble.isCombinable(x)){
				x = MesquiteDouble.queryDouble(MesquiteWindow.windowOfItem(this), "Slider Core Maximum...", "Set upper boundary for slider's core:", sliderWithText.getMaximumSweetValue().getDoubleValue());
			}
			if (MesquiteDouble.isCombinable(x)) 
				if (x<=minSweetValue)
					MesquiteMessage.notifyUser("Upper boundary cannot be less than the lower boundary.");
				else if (x<=maxValue || maxValue==MesquiteDouble.infinite){
					sliderWithText.setMaximumSweetValue(x);
					sliderWithText.repaint();
				}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public void setColor(Color c){
		setBackground(c);
		label.setBackground(c);
		Panel contents = getGraphicsArea();
		contents.setBackground(c);
		repaintAll();
	}
	public MiniSliderWithText getSlider() {
		return sliderWithText;
	}
	public void setText(String s) {
		label.setText(s);
		label.repaint();
		repaint();
	}
	public String getText() {
		return label.getText();
	}

	public void dispose(){
		getOwnerModule().deleteMenuItem(minItem);
		getOwnerModule().deleteMenuItem(maxItem);
		super.dispose();
	}
	/*=============*/
	public void buttonHit(String label, Button button) {
		if (label.equalsIgnoreCase("Close")) {
			getOwnerModule().windowGoAway(this);
		}
	}
}


