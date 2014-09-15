/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.basic.BasicParametersExplorer;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
import mesquite.lib.table.TableTool;

public class BasicParametersExplorer extends ParametersExplorer  {
	String title = null;
	PEWindow npw;
	ParametersExplorable explorable;
	MesquiteParameter[] parameters;
//	int[] constraints;
	boolean[] selected;
	MesquiteParameter xParameter, yParameter;
	double[][] values;
	int numDivisions = 10;  //SHOULD be user settable
	boolean suspend = false;
	boolean abort = false;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {

		setModuleWindow(npw = new PEWindow(this));

		values = new double[numDivisions][numDivisions];

		if (!MesquiteThread.isScripting()){
			npw.setVisible(true);
		}
		
		makeMenu("Parameters");
		
		addMenuItem("Manage Parameters...", makeCommand("chooseParameters", this));
		addMenuItem("Recalculate", makeCommand("doCalcs", this));
		resetContainingMenuBar();
		resetAllWindowsMenus();
		return true;
	}
	
	/*.................................................................................................................*/
	boolean showDialog(){
		if (parameters == null)
			return false;
		
		ParametersDialog dlog = new ParametersDialog(containerOfModule(), "Parameters", "Parameters to be explored", parameters, selected, 2, 2, true);
		IntegerField numField = dlog.addIntegerField("Number of divisions", numDivisions, 4);
		dlog.completeAndShowDialog(true);

		boolean ok = (dlog.query()==0);

		if (ok) {
			dlog.acceptParameters();
			numDivisions = numField.getValue();
		}

		dlog.dispose();
		return ok;
	}
	/*.................................................................................................................*/
	public boolean setExplorable(ParametersExplorable explorable){
		this.explorable = explorable;
		askForParameters();
		if (!MesquiteThread.isScripting()){
			if (showDialog())
				doCalcs();
			else
				return false;
		}
		return true;
	}
	/*.................................................................................................................*/
	/** notifies this module that explorable's previous values are now invalid */
	public void explorableChanged(ParametersExplorable explorable){
		doCalcs();
	}
	/*.................................................................................................................*/
	/** notifies this module that explorable's list of parameters is now invalid */
	public void parameterSpecsChanged(ParametersExplorable explorable){
		askForParameters();
		doCalcs();
	}
	MesquiteParameter getParameterSelected(int which){
		int count = 0;
		for (int i=0; i<selected.length; i++)
			if (selected[i]){
				if (count == which)
					return parameters[i];
				count++;
			}
		return null;
	}
	int getWhichParameter(MesquiteParameter which){
		for (int i=0; i<parameters.length; i++)
			if (parameters[i] == which){

					return i;
			}
		return -1;
	}
	
	//TODO: needs to follow indirect constraints (a to b to c; therefore a to c)
	MesquiteParameter[] getConstrainedTo(int which){
		if (which > parameters.length)
			return null;
		MesquiteParameter target = parameters[which];
		int count = 0;
		for (int i = 0; i<parameters.length; i++)
			if (parameters[i].getConstrainedTo() == target)
				count++;
		if (count == 0)
			return null;
		MesquiteParameter[] constrained = new MesquiteParameter[count];
		count = 0;
		for (int i = 0; i<parameters.length; i++)
			if (parameters[i].getConstrainedTo() == target){
				constrained[count++] = parameters[i];
			}
		
		return constrained;
	}
	public void doCalcs(){
		if (explorable == null || parameters == null || suspend)
			return;
		npw.setBlank(true);
		if (values.length != numDivisions)
			values = new double[numDivisions][numDivisions];

		xParameter = getParameterSelected(0);
		MesquiteParameter[] constrainedToX = getConstrainedTo(getWhichParameter(xParameter));
		double xMin = xParameter.getMinimumSuggested();
		double xMax = xParameter.getMaximumSuggested();
		yParameter = getParameterSelected(1);
		MesquiteParameter[] constrainedToY = getConstrainedTo(getWhichParameter(yParameter));
	//TODO: not yet protected against stupid users: can ask to view two parameters but which are constrained
		double origX = xParameter.getValue();
		double origY = yParameter.getValue();
		double yMin = yParameter.getMinimumSuggested();
		double yMax = yParameter.getMaximumSuggested();
		Double2DArray.deassignArray(values);
		npw.setValues(values, xMin, xMax, yMin, yMax, numDivisions, xParameter, yParameter);
		long start = System.currentTimeMillis();
		for (int ix = 0; ix < numDivisions && !abort; ix++){
			double x = xMin + (xMax-xMin)*ix/(numDivisions-1);
			xParameter.setValue(x);
			if (constrainedToX != null)
				for (int i= 0; i< constrainedToX.length; i++)
					constrainedToX[i].setValue(x);
			for (int iy = 0; iy < numDivisions && !abort; iy++){
				double y = yMin + (yMax-yMin)*iy/(numDivisions-1);
				CommandRecord.tick("x: " + x + "; y: " + y);
				yParameter.setValue(y);
				if (constrainedToY != null)
					for (int i= 0; i< constrainedToY.length; i++)
						constrainedToY[i].setValue(y);
				values[ix][iy] = explorable.calculate(null);
				if (System.currentTimeMillis()-start>1000 && !abort){
					npw.setBlank(false);
					npw.paintPlease();
					start = System.currentTimeMillis();
			}
			}
		}
		npw.setBlank(false);
		xParameter.setValue(origX);
		yParameter.setValue(origY);
		if (constrainedToX != null)
			for (int i= 0; i< constrainedToX.length; i++)
				constrainedToX[i].setValue(origX);
		if (constrainedToY != null)
			for (int i= 0; i< constrainedToY.length; i++)
				constrainedToY[i].setValue(origY);
		npw.repaintPlease();
		explorable.restoreAfterExploration();
	}

	void askForParameters(){
		parameters = explorable.getExplorableParameters();
		if (parameters == null)
			return;
		if (selected == null || selected.length != parameters.length){
			selected = new boolean[parameters.length];
			if (selected.length > 0)
				selected[0] = true;
			if (selected.length > 1)
				selected[1] = true;
		}
/*		//TODO: constraints should be obtained from the explorable
		if (constraints == null || constraints.length != parameters.length){
			constraints = new int[parameters.length];
			for (int i = 0; i< constraints.length; i++)
				constraints[i] = -1;
		}
		*/
	}
	/*.................................................................................................................*/

	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		if (npw !=null){
			temp.addLine("suspend");
			temp.addLine("getWindow");
			temp.addLine("tell It");
			Snapshot fromWindow = npw.getSnapshot(file);
			temp.incorporate(fromWindow, true);
			temp.addLine("endTell");
			temp.addLine("showWindow");
			temp.addLine("resume");
		}
		else {
		}
		/*if (!StringUtil.blank(npw.getJumpExplanation()))
		   	 		temp.addLine("setExplanation " + StringUtil.tokenize(npw.getJumpExplanation()));
		 */
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {

		if (checker.compare(this.getClass(), "Shows the dialog choosing parameters", null, commandName, "chooseParameters")) {
			if (showDialog())
				doCalcs();
		}
		else if (checker.compare(this.getClass(), "Suspends calculations", null, commandName, "suspend")) {
			suspend = true;
		}
		else if (checker.compare(this.getClass(), "Resumes calculations", null, commandName, "resume")) {
			suspend = false;
			doCalcs();
		}
		else if (checker.compare(this.getClass(), "Does calculations", null, commandName, "doCalcs")) {
			doCalcs();
		}
		else if (checker.compare(this.getClass(), "Zooms in", null, commandName, "zoomIn")) {
	 		MesquiteInteger pos = new MesquiteInteger(0);
	 		MesquiteInteger.fromString(arguments,pos); //eating up point
  			int xPos= MesquiteInteger.fromString(arguments,pos);
   			int yPos= MesquiteInteger.fromString(arguments,pos);
   			
  			recentre(xPos, yPos, 0.5);
	}
		else if (checker.compare(this.getClass(), "Zooms out", null, commandName, "zoomOut")) {
	 		MesquiteInteger pos = new MesquiteInteger(0);
	 		MesquiteInteger.fromString(arguments,pos); //eating up point
   			int xPos= MesquiteInteger.fromString(arguments,pos);
   			int yPos= MesquiteInteger.fromString(arguments,pos);
  			recentre(xPos, yPos, 2);
	}
		else if (checker.compare(this.getClass(), "Centres", null, commandName, "centre")) {
	 		MesquiteInteger pos = new MesquiteInteger(0);
	 		MesquiteInteger.fromString(arguments,pos); //eating up point
   			int xPos= MesquiteInteger.fromString(arguments,pos);
   			int yPos= MesquiteInteger.fromString(arguments,pos);
   			recentre(xPos, yPos, 1);
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	void recentre(int xPos, int yPos, double zoomFactor){
			double touchedX =  (npw.findXValue(xPos));  //how many units to shift right (+ve) or left (-ve)
			double maxX = xParameter.getMaximumSuggested();
			double minX = xParameter.getMinimumSuggested();
 			double centreX = (maxX+minX)/2; 			

  			double newMinX = touchedX -(centreX-minX)*zoomFactor;
  			double newMaxX = touchedX +(maxX-centreX)*zoomFactor;
  			if (newMinX< xParameter.getMinimumAllowed()){
  	
  				double offset = xParameter.getMinimumAllowed()-newMinX;
  				newMinX = xParameter.getMinimumAllowed();
  				newMaxX += offset;
  				if (newMaxX > xParameter.getMaximumAllowed())
  					newMaxX = xParameter.getMaximumAllowed();
  			}
  			else if (newMaxX > xParameter.getMaximumAllowed()){
				double offset = newMaxX-xParameter.getMaximumAllowed();
				newMaxX = xParameter.getMaximumAllowed();
  				newMinX -= offset;
  				if (newMinX< xParameter.getMinimumAllowed())
  					newMinX = xParameter.getMinimumAllowed();
 			}
 			xParameter.setMinimumSuggested(newMinX);
 			xParameter.setMaximumSuggested(newMaxX);
 			
			double touchedY =  npw.findYValue(yPos);  //how many units to shift right (+ve) or left (-ve)
			double maxY = yParameter.getMaximumSuggested();
			double minY = yParameter.getMinimumSuggested();
 			double centreY = (maxY+minY)/2; 			
  						
  			double newMinY = touchedY -(centreY-minY)*zoomFactor;
  			double newMaxY = touchedY +(maxY-centreY)*zoomFactor;
  			if (newMinY< yParameter.getMinimumAllowed()){
  				double offset = yParameter.getMinimumAllowed()-newMinY;
  				newMinY = yParameter.getMinimumAllowed();
  				newMaxY += offset;
  				if (newMaxY > yParameter.getMaximumAllowed())
  					newMaxY = yParameter.getMaximumAllowed();
  			}
  			else if (newMaxY > yParameter.getMaximumAllowed()){
				double offset = newMaxY-yParameter.getMaximumAllowed();
				newMaxY = yParameter.getMaximumAllowed();
  				newMinY -= offset;
  				if (newMinY< yParameter.getMinimumAllowed())
  					newMinY = yParameter.getMinimumAllowed();
 			}
  			yParameter.setMinimumSuggested(newMinY);
 			yParameter.setMaximumSuggested(newMaxY);
			doCalcs();
	}
	public void shutDown(){
		if (!isDoomed() && npw!=null)
			windowGoAway(npw);
	}
	/*.................................................................................................................*/
	public void windowGoAway(MesquiteWindow whichWindow) {
		abort = true;
		whichWindow.hide();
		whichWindow.dispose();
		iQuit();
	}
	/*.................................................................................................................*/
	public void endJob() {
		abort = true;
		if (npw != null) {
			npw.hide();
			npw.dispose();
		}
		super.endJob();
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Parameters Explorer";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Provides a window to show values returned when parameter settings are varied";
	}
	public boolean isSubstantive(){
		return true;
	}
	public boolean isPrerelease(){
		return false;
	}

}

/* ======================================================================== */
class PEWindow extends MesquiteWindow {
	FieldPanel field;
	ChartTool zoomIn, zoomOut, centre, select;
	public PEWindow(BasicParametersExplorer module) {
		super(module, true);
		centre = new ChartTool (this, "centre",  module.getPath(), "centre.gif", 8, 8, "Centre", "Centers chart", MesquiteModule.makeCommand("centre", module), null);
		addTool(centre);
		zoomIn = new ChartTool (this, "zoomIn",  MesquiteModule.getRootImageDirectoryPath(), "zoomIn.gif", 4, 4, "Zoom In", "Zooms in and centers chart", MesquiteModule.makeCommand("zoomIn", module), null);
		addTool(zoomIn);
		zoomOut = new ChartTool (this, "zoomOut",  MesquiteModule.getRootImageDirectoryPath(), "zoomOut.gif", 4, 4, "Zoom Out", "Zooms out and centers chart", MesquiteModule.makeCommand("zoomOut", module), null);
		addTool(zoomOut);
		setCurrentTool(centre);
		setWindowSize(220, 260);
		setBackground(ColorDistribution.straw);
		field = new FieldPanel();
		addToWindow(field);
		field.setLocation(0, 0);
		field.setVisible(true);
		windowResized();
		setExplanation("This is a parameters explorer; colors indicate likelihoods, with whiter being better likelihoods");
		setShowExplanation(true);
		resetTitle();

	}
	void setBlank(boolean blank){
		field.blank = blank;
		if (blank)
			paintPlease();
	}
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree lists, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		setTitle("Parameters Explorer");
	}

	/*.................................................................................................................*/
	/** to be overridden by MesquiteWindows for a text version of their contents*/
	public String getTextContents() {
		return "";
	}
	/*int findXPlace(int graphicX){
		if (field == null)
			return -1;
		return field.findXPlace(graphicX);
	}
	int findYPlace(int graphicY){
		if (field == null)
			return -1;
		return field.findYPlace(graphicY);
	}
	*/
	double findXValue(int graphicX){
		if (field == null)
			return 0;
		return field.findXValue(graphicX);
	}
	double findYValue(int graphicY){
		if (field == null)
			return 0;
		return field.findYValue(graphicY);
	}

	void setValues(double[][] values, double xMin, double xMax, double yMin, double yMax, int numDivisions, MesquiteParameter xParam, MesquiteParameter yParam){


		field.setValues(values, xMin,  xMax,  yMin,  yMax,  numDivisions, xParam, yParam);
	}
	void repaintPlease(){
		field.repaint();
		repaint();
	}
	void paintPlease(){
		
				Graphics g = field.getGraphics();
				if (g != null){
					field.paint(g);
				g.dispose();
				}
			}

	public void windowResized(){
		if (field == null)
			return;
		field.setSize(getWidth(), getHeight());
	}

}

class FieldPanel extends MesquitePanel {
	double[][] values;
	double xMin; double xMax; double yMin; double yMax; int numDivisions;
	int border = 100;
	TextRotator textRotator;
	MesquiteParameter xParam, yParam;
	boolean blank = false;
	public FieldPanel (){
		textRotator = new TextRotator();
	}
	void setValues(double[][] values, double xMin, double xMax, double yMin, double yMax, int numDivisions, MesquiteParameter xParam, MesquiteParameter yParam){
		this.values = values;
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
		this.xParam = xParam;
		this.yParam = yParam;
		this.numDivisions = numDivisions;
	}
	/*int findXPlace(int graphicX){
		int w = getBounds().width-border;
		for (int ic = 0; ic<numDivisions; ic++){
			int pixelStartX = (ic*w)/numDivisions + border;
			if (pixelStartX>graphicX)
				return ic;
		
		}
		return numDivisions-1;
	}
	int findYPlace(int graphicY){
		int h = getBounds().height-border;
		for (int it = 0; it<numDivisions; it++){
			int pixelStartY = ((it+1)*h)/numDivisions - 6 + border;
			if (pixelStartY>graphicY)
				return it;
		}
		return numDivisions-1;
	}
	*/
	double findXValue(int graphicX){
		int w = getBounds().width-border;
		for (int ic = 1; ic<numDivisions; ic++){
			int pixelStartX = (ic*w)/numDivisions + border;
			if (pixelStartX>graphicX)
				return xMin + (xMax-xMin)*(ic-1)/(numDivisions-1);
		
		}
		return xMax;
	}
	double findYValue(int graphicY){
		int h = getBounds().height-border;
		for (int it = 1; it<numDivisions; it++){
			int pixelStartY = ((it)*h)/numDivisions - 6 + border;
			if (pixelStartY>graphicY)
				return yMin + (yMax-yMin)*(it-1)/(numDivisions-1);
		}
		return yMax;
	}
	StringInABox box = new StringInABox("", getFont(), border-16);
	public void paint(Graphics g){
		if (MesquiteWindow.checkDoomed(this))
			return;

		if (values == null || blank){
			MesquiteWindow.uncheckDoomed(this);
			return;
		}
		g.setColor(Color.black);
		int w = getBounds().width-border;
		int h = getBounds().height-border;
		box.setStringAndFont(new StringBuffer(xParam.getName()), getFont());
		box.draw(g, border-46, border-16, 0, border-10, this, false);
				
			//	textRotator.drawRotatedText(xParam.getName(), -1, g, this, border-12, border-16);
		for (int ic = 0; ic<numDivisions; ic++){
			double xValue = xMin + (xMax-xMin)*ic/(numDivisions-1);
			int pixelStartX = (ic*w)/numDivisions + border;

			textRotator.drawRotatedText(MesquiteDouble.toString(xValue), ic, g, this, pixelStartX, border-4);

		}
		box.setString(yParam.getName());
		box.draw(g, 4,  - 20 + border);
		
		//g.drawString(yParam.getName(), 4,  - 6 + border);
		for (int it = 0; it<numDivisions; it++){
			double yValue = yMin + (yMax-yMin)*it/(numDivisions-1);

			int pixelStartY = ((it+1)*h)/numDivisions - 6 + border;
			g.drawString(MesquiteDouble.toString(yValue), 10, pixelStartY);
		}


		double min = Double2DArray.minimum(values, true);
		double max = Double2DArray.maximum(values, true);
		if (MesquiteDouble.isUnassigned(min) || MesquiteDouble.isUnassigned(max)){
		}
		else {

			for (int ic = 0; ic<values.length; ic++)
				for (int it = 0; it<values[ic].length; it++){
					int pixelStartX = (ic*w)/values.length + border;
					int pixelEndX = ((ic+1)*w)/values.length + border;
					int pixelStartY = (it*h)/values[ic].length + border;
					int pixelEndY = ((it+1)*h)/values[ic].length + border;
					double v= values[ic][it];
					Color c = null;
					if (MesquiteDouble.isUnassigned(v))
						c = Color.gray;
					else if (!MesquiteDouble.isCombinable(v))
						c = Color.pink;
					else
						c = MesquiteColorTable.getGreenScale(values[ic][it],  min,  max, false);
					g.setColor(c);
					g.fillRect(pixelStartX, pixelStartY, pixelEndX-pixelStartX, pixelEndY-pixelStartY);
					g.setColor(Color.black);
					g.drawString(MesquiteDouble.toStringDigitsSpecified(values[ic][it], 4), pixelStartX+2, pixelEndY-4);
					if (MesquiteDouble.isCombinable(v) && (v-min)/(max - min) < 0.0001){
						g.setColor(Color.blue);
						g.drawRect(pixelStartX, pixelStartY, pixelEndX-pixelStartX-1, pixelEndY-pixelStartY-1);
					}
				}
		}


		MesquiteWindow.uncheckDoomed(this);
	}
	/*...............................................................................................................*/
	public void mouseDown(int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (!(tool instanceof ChartTool))
			return;
		((ChartTool)tool).pointTouched (-1, x, y, modifiers); 
	}
	/*_________________________________________________*/
	public void mouseDrag(int modifiers, int x, int y, MesquiteTool tool) {

	}
	/*_________________________________________________*/
	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		if (!(tool instanceof ChartTool))
			return;
		((ChartTool)tool).pointDropped (-1, x, y, modifiers); 
	}
}


