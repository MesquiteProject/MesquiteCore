/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charts.ShowPercentile;

import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Vector;

import mesquite.lib.CommandChecker;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.NumberArray;
import mesquite.lib.Snapshot;
import mesquite.lib.duties.HistogramAssistantA;
import mesquite.lib.ui.ChartBkgdExtra;
import mesquite.lib.ui.ChartExtra;
import mesquite.lib.ui.Charter;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.DoubleField;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.MesquiteChart;

public class ShowPercentile extends HistogramAssistantA implements ActionListener  {
	/*.................................................................................................................*/
 	public String getName(){
		return "Show Percentile";
	}
 	public String getExplanation() {
 		String s= "Calculates and shows percentile boundaries.  Finds the upper (right) or lower (left) tail of the distribution that contains the specified percentage of the distribution.";
 		return s + " If there is not a boundary that exactly matches the specified percentage, then the closest boundary that does not exceed the percentage is shown." ;
   	 }
	/*.................................................................................................................*/
	Vector extras;
	double percentile=0.05;
	boolean leftTail=true;
	boolean rightTail = true;

	ExtensibleDialog dialog;
	Choice colorChoice;
	Checkbox leftTailCheck;
	Checkbox rightTailCheck;
	Color color = Color.red;
	int colorNumber = 5;
	DoubleField df;
	MesquiteInteger buttonPressed;
	boolean towardMedian=true;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
   		extras = new Vector();
   		addMenuItem("Adjust Percentile Shown...", makeCommand("setPercentile",  this));
		if (MesquiteThread.isScripting())
			return true;
		return queryPercentile();
	}
	/*.................................................................................................................*/
	public boolean canHireMoreThanOnce(){
		return true;
	}
	/*.................................................................................................................*/
   	public boolean queryPercentile(){
		buttonPressed = new MesquiteInteger(1);
		dialog = new ExtensibleDialog(containerOfModule(), "Show Percentile",  buttonPressed);
		dialog.setAutoDispose(false);
		df = dialog.addDoubleField("Percentile boundary",percentile,20);
		
		colorChoice = dialog.addPopUpMenu("Line color: ", ColorDistribution.standardColorNames.getStrings(),colorNumber);
		
		leftTailCheck = dialog.addCheckBox("lower (left) tail", leftTail);
		dialog.suppressNewPanel();
		rightTailCheck = dialog.addCheckBox("upper (right) tail", rightTail);
		
		String helpString = "\"Show Percentile\" will draw lines marking the specified percentile.  It will mark the percentile at both tails.  ";
		helpString += " Thus, if you choose 0.05 as the percentile, it will draw a line marking the first 0.05 of the values, and the last 0.05 of the values.";
		dialog.appendToHelpString(helpString);
		
		dialog.setHelpURL(this,"",true);
		
		dialog.completeAndShowDialog(true,this);
		
		if (dialog!=null) {
			 if (buttonPressed.getValue()==0) {
		 		if (checkOptions()) {
		 			getOtherDialogValues();
		 		}
		 		else 
		 			buttonPressed.setValue(1);
			 }
			if (dialog!=null)
				dialog.dispose();
		}
		
		return  (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	public void getOtherDialogValues(){
		colorNumber = colorChoice.getSelectedIndex();
	 	color = ColorDistribution.getStandardColor( colorNumber);
	 	leftTail = leftTailCheck.getState();
		rightTail = rightTailCheck.getState();
		if (!leftTail && !rightTail) {
			MesquiteMessage.notifyUser("At least one tail must be shown; the upper (right) will be chosen");
			rightTail=true;
		}
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
  	 	String s = "setPercentile " + percentile + "  " + colorNumber; 
   	 	if (leftTail && rightTail)
   	 		s += " 2";
   	 	else if (rightTail)
   	 		s += " 1";
  	 	else 
   	 		s += " 0";
  	 	temp.addLine(s); 
  	 	return temp;
  	 }
  	 MesquiteInteger pos = new MesquiteInteger(0);
  	/*.................................................................................................................*/
  	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets the percentile", "[percentile, 0 to 1]", commandName, "setPercentile")) {
    	 		pos.setValue(0);
 			double s = MesquiteDouble.fromString(arguments, pos);
    	 		int bc = MesquiteInteger.fromString(arguments, pos); 
    	 		int tails = MesquiteInteger.fromString(arguments, pos); 
    	 		if (MesquiteInteger.isCombinable(bc)) {
    	 			colorNumber = bc;
	 			color = ColorDistribution.getStandardColor( colorNumber);
	 		}
	 		if (MesquiteInteger.isCombinable(tails)){
	 			leftTail=true;
	 			rightTail=true;
	 			if (tails==0)
	 				rightTail=false;
	 			else if (tails==1)
	 				leftTail=false;
	 		}
	 		boolean q = true;
	 		if (!MesquiteDouble.isCombinable(s) || s<0 || s>1)
 				q = queryPercentile();
 			else
 				percentile = s;
	 		if (q) {
				for (int i =0; i<extras.size(); i++){
					ShowPercentileExtra e = (ShowPercentileExtra)extras.elementAt(i);
					e.percentile = percentile;
					e.color = color;
					e.setShowLeftTail(leftTail);
					e.setShowRightTail(rightTail);
				}
	 			parametersChanged();
	 		}
    		}
   	 	else return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
   	public boolean checkOptions(){
   		double value = df.getValue();
   		if (!MesquiteDouble.isCombinable(value))
   			return false;
   		if (value<0.0000000001 || value>0.999999999)
   			return false;
   		percentile = value;
   		return true;
   	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}

	public ChartExtra createExtra(MesquiteChart chart){
		ShowPercentileExtra chartExtra = new ShowPercentileExtra(this, chart, percentile, color, leftTail, rightTail);
		extras.addElement(chartExtra);
		return chartExtra;
	}
	
	/*.................................................................................................................*/
	public String getNameForMenuItem(){
		return "Percentiles...";
		}
	/*.................................................................................................................*/
	public String getParameters(){
		return "Percentile shown: " + percentile;
	}
	/*.................................................................................................................*/
  	 public String getAuthors() {
		return "David R. Maddison & Wayne P. Maddison";
   	 }
	/*.................................................................................................................*/
  	 public boolean isPrerelease() {
		return false;
   	 }
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	 public  void actionPerformed(ActionEvent e) {
	 	if   (dialog.getDefaultOKLabel().equals(e.getActionCommand())) {
	 		if (checkOptions()) {
	 			getOtherDialogValues();
	 			buttonPressed.setValue(0);
	 			dialog.dispose();
	 			dialog=null;
	 		}
	 		else
	 			alert("Percentile value needs to be between 0.0 and 1.0");
		}
	 	else if   (dialog.getDefaultCancelLabel().equals(e.getActionCommand())) {
	 		buttonPressed.setValue(1);
			dialog.dispose();
		}
		else dialog.actionPerformed(e);
	 }
	 
	/*.................................................................................................................*/
 	public void endJob() {
		if (extras!=null) {
			Enumeration e = extras.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof ChartExtra) {
					ChartExtra tCO = (ChartExtra)obj;
		 			tCO.turnOff();
		 			
		 		}
			}
			extras.removeAllElements();
		}
 		super.endJob();
   	 }
}

/* =================================================================================*/
class ShowPercentileExtra extends ChartBkgdExtra {
	double percentile;  
	int totalValues=0;
	int numPercentiles=2;   // percentile 0 is left tail; percentile 1 is right tail
	double[] actualPercentile = new double[numPercentiles];
	boolean[] showTail = new boolean[numPercentiles];
	int[] numValues = new int[numPercentiles];   //
	MesquiteNumber[] percentileBoundary = new MesquiteNumber[numPercentiles];
	MesquiteNumber[] percentileBoundaryDrawn = new MesquiteNumber[numPercentiles];
	MesquiteBoolean[] includeEqualSign = new MesquiteBoolean[numPercentiles];
	Charter charter;
	Color color, useColor;
	Polygon upTriangle,rightTriangle;
	private boolean warnedOnce = false;
	int trisize = 5;
	boolean okToShowPercentile = true;
	
	public ShowPercentileExtra(MesquiteModule ownerModule, MesquiteChart chart, double percent, Color color, boolean leftTail, boolean rightTail){
		super(ownerModule, chart);
		upTriangle=new Polygon();
		upTriangle.xpoints = new int[4];
		upTriangle.ypoints = new int[4];
		upTriangle.npoints=0;
		upTriangle.addPoint(0, trisize);
		upTriangle.addPoint(trisize, 0);
		upTriangle.addPoint(trisize+trisize, trisize);
		upTriangle.addPoint(0, trisize);
		upTriangle.npoints=4;
		rightTriangle=new Polygon();
		rightTriangle.xpoints = new int[4];
		rightTriangle.ypoints = new int[4];
		rightTriangle.npoints=0;
		rightTriangle.addPoint(0, 0);
		rightTriangle.addPoint(trisize, trisize);
		rightTriangle.addPoint(0, trisize+trisize);
		rightTriangle.addPoint(0, 0);
		rightTriangle.npoints=4;
		percentile =percent;
		this.color = color;
		charter = chart.getCharter();
		for (int i=0; i<numPercentiles; i++) {
			percentileBoundary[i]=new MesquiteNumber(0);
			percentileBoundaryDrawn[i]=new MesquiteNumber(0);
			includeEqualSign[i] = new MesquiteBoolean(true);
		}
	
		showTail[0] = leftTail;
		showTail[1] = rightTail;
		

	}
	/*.................................................................................................................*/
	public boolean canDoShowPercentile(){
		boolean canDo = (chart.getOrientation() == 0 || charter.isNative());
		if (!canDo && !warnedOnce) {
			MesquiteTrunk.mesquiteTrunk.discreetAlert( "Show Percentile is not available with this orientation if items are grouped.");
			warnedOnce = true;
		}
		okToShowPercentile = canDo;
		return canDo;
	}
	/*.................................................................................................................*/
	public void setShowLeftTail (boolean show){
		showTail[0]=show;
	}
	/*.................................................................................................................*/
	public void setShowRightTail (boolean show){
		showTail[1]=show;
	}
	/*.................................................................................................................*/
	public double getPercentile (){
		return percentile;
	}
	/*.................................................................................................................*/
	public void setInitialBoundaryValues (int tail, int count, double numInPercentile, int point, int lastPoint, int i, NumberArray array, MesquiteNumber boundaryValue){
		if (count==numInPercentile) {
			array.placeValue(point,boundaryValue);  // we are at it, store this point
		}
		else {  // we've gone past it.
			if (count>1)
				array.placeValue(lastPoint,boundaryValue);  // as we have gone past it, now store the last point
			else 
				boundaryValue.setToUnassigned(); // we've gone past it just by getting to the first point; therefore, boundary is edge - indicate this by setting to unassigned.
			numValues[tail]=count-1;
		}
	}
	
	/*.................................................................................................................*/
	boolean pointValid(MesquiteChart chart, int point){
		if (chart ==null)
			return false;
		if (!okToShowPercentile)
			return false;
		return (chart.getXArray().isCombinable(point) && chart.getYArray().isCombinable(point));
	}
	/*.................................................................................................................*/
	/**Calculates the boundary for a particular percentile value*/
	public void placePercentileBoundary(double percentileValue, boolean left, MesquiteNumber mnDrawn, MesquiteNumber mn, MesquiteBoolean mb){
//		int numInPercentile = (int)java.lang.Math.round(percentileValue * totalValues-0.0000000001);  
//		int numInPercentile = MesquiteInteger.roundUp(percentileValue * totalValues);  
		double numInPercentile = percentileValue * totalValues;  
		
		MesquiteNumber previousValue = new MesquiteNumber();
		MesquiteNumber currentValue = new MesquiteNumber();
		MesquiteNumber nextValue = new MesquiteNumber();
		MesquiteNumber boundaryValue = new MesquiteNumber();
		MesquiteNumber boundaryValueDrawn = new MesquiteNumber();
		int count=1;   
		int lastDifferentCount=1;
		int lastDifferentPoint=MesquiteInteger.unassigned;
		NumberArray values = null;
		MesquiteNumber minimumDrawn = null;
		MesquiteNumber minimum = null;
		MesquiteNumber maximumDrawn = null;
		MesquiteNumber maximum = null;
		boolean boundaryIncludeEqualSign = true;
		int[] ord =  null;  // order of the points ordered by x coordinate.  returns integerarray.
		if (chart.getOrientation() == 0) {  //orientation stored in chart as 0 = items by values; 1 = values by items   (y by x)
			values = chart.getXArray();
			minimum = charter.leftmostXDrawn(chart);
			minimumDrawn = chart.getMinimumX();
			ord =  chart.getOrderByX();  // order of the points ordered by x coordinate.  returns integerarray.
			maximumDrawn = charter.rightmostXDrawn(chart);
			maximum = chart.getMaximumX();
		}
		else if (chart.getOrientation() == 1) {  //orientation stored in chart as 0 = items by values; 1 = values by items   (y by x)
			values = chart.getYArray();
			minimum = chart.getMinimumY();
			minimumDrawn = charter.bottommostYDrawn(chart);
			ord =  chart.getOrderByY();  // order of the points ordered by y coordinate.  returns integerarray.
			maximumDrawn = charter.topmostYDrawn(chart);
			maximum = chart.getMaximumY();
		}
		if (ord !=null){
			int point =0;
			int lastPoint = 0;  // this keeps note of the last point encountered
			count =1;  // this keeps count of the number of points we have gone through
			boolean boundaryFound = false;
// LEFT TAIL
			if (left) {
				for (int i= 0; i<chart.getNumPoints(); i++) {     
					point = ord[i];   // these ordering of the points is stored in ord
								// however, the value of "point" is not in order - that is, the values of ord[0], ord[1], ord[2] might be 23   184  99
					if (pointValid(chart, point)) {
						values.placeValue(point,currentValue); 
						if (!currentValue.equals(previousValue) && previousValue.isCombinable()) {// there is a change between the last point and this one
							lastDifferentPoint = lastPoint;
							lastDifferentCount = count-1;
						}
						if (!boundaryFound &&  (count >= numInPercentile)) {  // we've found the boundary element
								//now we have to figure out exactly what is the boundary value.
							setInitialBoundaryValues (0,count, numInPercentile, point, lastPoint, i, values, boundaryValue);
							boundaryFound = true;
							if (!boundaryValue.isCombinable()){  // at the left edge
								boundaryValue.setValue(minimum.getDoubleValue());  // set it to the minimumDrawn value
								boundaryValueDrawn.setValue(minimumDrawn.getDoubleValue());
							}
							else {
								if (i<chart.getNumPoints()-1) {  // we are not at the end
									int nextPoint=MesquiteInteger.unassigned;
									for (int j=i+1; j<chart.getNumPoints(); j++) {
										nextPoint = ord[j];
										if (pointValid(chart, nextPoint)) {
											values.placeValue(nextPoint,nextValue);
											break;
										}
									}
									if (nextValue.equals(boundaryValue)) {// then if we include this value in it, we also need to include too large a percentile, > value desired
																	// so we need to reset the value to that of the previous bin
										if (!MesquiteInteger.isCombinable(lastDifferentPoint)) {  // the previous bin is the left edge of the chart
											boundaryValue.setValue(minimum.getDoubleValue());
											boundaryValueDrawn.setValue(minimumDrawn.getDoubleValue());
											numValues[0]=0;
										}
										else {
											values.placeValue(lastDifferentPoint,boundaryValue); 
											numValues[0]=lastDifferentCount;
										}
									}
									if (!boundaryValueDrawn.isCombinable()){  //we haven't set this yet; 
										if (charter.moreThanOneValueInInterval(boundaryValue, chart)) // then we want to leave it where it is
											boundaryValueDrawn.setValue(boundaryValue.getDoubleValue());
										else if (chart.getOrientation() == 0) 
											boundaryValueDrawn.setValue(charter.rightmostXOfInterval(boundaryValue, chart));  // we want to set it to limits of bar
										else
											boundaryValueDrawn.setValue(charter.topmostYOfInterval(boundaryValue, chart));  // we want to set it to limits of bar
									}

								}
							}
							break;						
						}
						values.placeValue(point,previousValue);
						lastPoint = point;
						count++;
					}
				}
				if (!boundaryFound) {  // we didn't find it, thus needs to be end point (???)
					values.placeValue(lastPoint,previousValue);
					numValues[0]=count-1;
				}
				actualPercentile[0] = 1.0* (numValues[0])/totalValues;
			}
// RIGHT TAIL
			else {  
				for (int i= chart.getNumPoints()-1; i>=0; i--) {      // we start from the right edge of the chart, and cycle through the points going left
					point = ord[i];
					if (pointValid(chart, point)) {    // ok, we have a point with good values
						values.placeValue(point,currentValue); 
						if (!currentValue.equals(previousValue) && previousValue.isCombinable()) {// there is a change between the last point and this one
							lastDifferentPoint = lastPoint;   // store last point that was different so we can set the boundary there if needed
							lastDifferentCount = count-1;
						}
						if (!boundaryFound &&  (count >= numInPercentile)) {  // we've found the boundary element
							setInitialBoundaryValues (1,count, numInPercentile, point, lastPoint, i, values, boundaryValue);
							boundaryFound = true;
							if (!boundaryValue.isCombinable() && maximumDrawn!=null) {  // the boundary is at right edge of the chart
								boundaryValue.setValue(maximum.getDoubleValue());  
								boundaryValueDrawn.setValue(maximumDrawn.getDoubleValue());
							}
							else {
								if (i>0) {  // we are not at the end
									int nextPoint=MesquiteInteger.unassigned;
									for (int j=i-1; j>=0; j--) {   // we cycle through looking for the next value to the left, and place it in nextValue
										nextPoint = ord[j];
										if (pointValid(chart, nextPoint)) {
											values.placeValue(nextPoint,nextValue);
											break;
										}
									}
									if (nextValue.equals(boundaryValue)) {// then if we include this value in it, we also need to include too large a percentile, > value desired
																	// so we need to reset it to the previous bin
										if (!MesquiteInteger.isCombinable(lastDifferentPoint)) {  // we never set lastDifferentPoint; boundary is at end
											if (maximumDrawn!=null){
												boundaryValue.setValue(maximum.getDoubleValue());  // set it to the right edge
												boundaryValueDrawn.setValue(maximumDrawn.getDoubleValue());  // set it to the right edge
											}
											numValues[1]=0;
										}
										else {   // we did set lastDifferentPoint; that's were we move the boundary too
											values.placeValue(lastDifferentPoint,boundaryValue); 
											numValues[1]=lastDifferentCount;
										}
									}
									if (!boundaryValueDrawn.isCombinable()){  //we haven't set this yet; 
										if (charter.moreThanOneValueInInterval(boundaryValue, chart)) // then we want to leave it where it is
											boundaryValueDrawn.setValue(boundaryValue.getDoubleValue());
										else if (chart.getOrientation() == 0) 
											boundaryValueDrawn.setValue(charter.leftmostXOfInterval(boundaryValue, chart));  // we want to set it to limits of bar
										else
											boundaryValueDrawn.setValue(charter.bottommostYOfInterval(boundaryValue, chart));  // we want to set it to limits of bar
									}
								}
							}
							break;						
						}
						values.placeValue(point,previousValue);
						lastPoint = point;
						count++;
					}
				}
				if (!boundaryFound) {  // we didn't find it, thus needs to be end point (???)
					values.placeValue(lastPoint,previousValue);
					numValues[1]=count-1;
				}
				actualPercentile[1] = 1.0* (numValues[1])/totalValues;
			}
		}

		mnDrawn.setValue(boundaryValueDrawn);
		mn.setValue(boundaryValue);
		
		mb.setValue(boundaryIncludeEqualSign);
	}
	/*.................................................................................................................*/
	/**Do any calculations needed*/
	public boolean doCalculations(){
		if (chart == null)
			return false;
		if (!canDoShowPercentile())
			return false;
		for (int i=0; i<numPercentiles; i++) {
			percentileBoundary[i].setToUnassigned();  
			percentileBoundaryDrawn[i].setToUnassigned();  
			actualPercentile[i]=0.0;
			numValues[i]=0;
		}
			
		if (chart.getOrientation() == 0)
			totalValues = chart.getYTotal().getIntValue();
		else
			totalValues = chart.getNumPoints();
		if (chart.getCharter() != null){
			if (chart.getCharter().showingClumpSums())
				return false;
		}
		for (int i=0; i<numPercentiles; i++) {
			actualPercentile[i] = percentile;
			numValues[i]=(int)(percentile * totalValues);
		}

		if (showTail[0]) 
			placePercentileBoundary(percentile,true,percentileBoundaryDrawn[0], percentileBoundary[0], includeEqualSign[0]);///, percentileBoundaryNext[0]);
		if (showTail[1])
			placePercentileBoundary(percentile,false,percentileBoundaryDrawn[1], percentileBoundary[1], includeEqualSign[1]);///, percentileBoundaryNext[1]);
		boolean someSelected = chart.getSelected().anyBitsOn();
		if (someSelected)
			useColor = ColorDistribution.brighter(color, ColorDistribution.dimmingConstant*1.5);
		else
			useColor = color;
		return false;
	}
	
	/*.................................................................................................................*/
	/**draw on the chart*/
	public void drawOnChart(Graphics g){
		if (!okToShowPercentile)
			return;
		Color col = g.getColor();
		g.setColor(useColor);
		int x;
		int x2;
		int y;
		for (int i=0; i<numPercentiles; i++) {
			if (showTail[i]) {
				if (chart.getOrientation() == 0) {  //orientation stored in chart as 0 = items by values; 1 = values by items
					//drawing vertical line
					if (!percentileBoundaryDrawn[i].isCombinable()) {
						if (i==0) //left tail
							x = charter.xToPixel(charter.leftmostXDrawn(chart).getDoubleValue(),chart);
						else {
							x = charter.xToPixel(charter.rightmostXDrawn(chart).getDoubleValue(),chart);
						}
					}
					else {
						x = charter.xToPixel(percentileBoundaryDrawn[i].getDoubleValue(),chart);
						
					}
					g.drawLine(x, 0,x, chart.getFieldHeight());
					g.drawLine(x+1, 0,x+1, chart.getFieldHeight());
				}
				else {  
					//drawing horizontal line
					if (!percentileBoundaryDrawn[i].isCombinable()) 
						return;
					y = charter.yToPixel(percentileBoundaryDrawn[i].getDoubleValue(),chart);
					g.drawLine(0,y, chart.getFieldWidth(), y);
					g.drawLine(0,y+1, chart.getFieldWidth(), y+1);
				}
			}
		}
		g.setColor(col);
	}
	public void drawOnAxes(Graphics g, MesquiteChart chart){
		if (!okToShowPercentile)
			return;
		Color col = g.getColor();
		g.setColor(useColor);
		for (int i=0; i<numPercentiles; i++) {
			if (showTail[i] && percentileBoundaryDrawn[i].isCombinable()) {
				if (chart.getOrientation() == 0) {  //orientation stored in chart as 0 = items by values; 1 = values by items
					int xPixel = xPixelOnAxis(percentileBoundaryDrawn[i].getDoubleValue(), chart)-trisize;
					int yPixel = chart.getFieldHeight();
					upTriangle.translate(xPixel,yPixel);
					g.fillPolygon(upTriangle);
					upTriangle.translate(-xPixel,-yPixel);
				}
				else {  
					int xPixel = chart.getXAxisEdge()-trisize;
					int yPixel = yPixelOnAxis(percentileBoundaryDrawn[i].getDoubleValue(), chart)-trisize;
					rightTriangle.translate(xPixel,yPixel);
					g.fillPolygon(rightTriangle);
					rightTriangle.translate(-xPixel,-yPixel);
				}
			}
		}
		g.setColor(col);
		
	}
	/*.................................................................................................................*/
	/**print on the chart*/
	public void printOnChart(Graphics g){
		drawOnChart(g);
	}
	/*.................................................................................................................*/
	public String getTailNote(int i, boolean left){
		String s = " percentile boundary ";
		if (left) {
			if (chart.getOrientation() == 0)
				s += "(lower tail) <";
			else
				s += "(left tail) <";
			if (numValues[i]>0 && includeEqualSign[i].getValue())
				s += "=";
		}
		else {
			if (chart.getOrientation() == 0)
				s += "(upper tail) >";
			else
				s += "(right tail) >";
			if (numValues[i]>0 && includeEqualSign[i].getValue())
				s += "=";
		}
		s= "" + actualPercentile[i] + s +" " + percentileBoundary[i].getDoubleValue();
		if (actualPercentile[i]!=percentile) {
			s+="\n     " + actualPercentile[i] + " is the closest percentile to "+percentile + " that was found,";
			s+="\n     and";
		}
		else
			s+="\n     This";
		s+=" corresponds to " + numValues[i] + " replicate";
		if (numValues[i]>1 || numValues[i]==0)
			s+="s";
		s+=" out of "+totalValues + ".";
		return s;
	}
	/*.................................................................................................................*/
	public String writeOnChart(){
		if (!okToShowPercentile)
			return "";
		String s="";
		if (showTail[0])
			s+= getTailNote(0,true);
		if (showTail[1])
			s+="\n" + getTailNote(1,false);
		return s;
	}
	/*.................................................................................................................*/
	/**to inform ChartExtra that cursor has just entered point*/
	public void cursorEnterPoint(int point, int exactPoint, Graphics g){
		if (chart == null || charter == null)
			return;
		MesquiteNumber mn;
		if (chart.getOrientation() == 0) {  //orientation stored in chart as 0 = items by values; 1 = values by items
			mn = new MesquiteNumber();
			charter.pixelToX(exactPoint,chart, mn);
		}
		else {  
			mn = new MesquiteNumber();
			charter.pixelToY(exactPoint,chart, mn);
		}
	}
	/*.................................................................................................................*/
	/**to inform ChartExtra that cursor has just exited point*/
	public void cursorExitPoint(int point, int exactPoint, Graphics g){
	}
	/*.................................................................................................................*/
	/**to inform ChartExtra that cursor has just touched point*/
	public void cursorTouchPoint(int point, int exactPoint, Graphics g){
	}
}

