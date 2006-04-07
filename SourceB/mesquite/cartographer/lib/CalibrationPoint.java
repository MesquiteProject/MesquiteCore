package mesquite.cartographer.lib;

import java.awt.Color;
import java.awt.Graphics;

import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.StringInABox;


/*=======================================================================*/
public class CalibrationPoint {
	MesquiteNumber longitude = new MesquiteNumber(0.0);
	MesquiteNumber latitude = new MesquiteNumber(0.0);
	MesquiteNumber x = new MesquiteNumber(0.0);
	MesquiteNumber y = new MesquiteNumber(0.0);
	MesquiteNumber projectedX = new MesquiteNumber();
	MesquiteNumber projectedY = new MesquiteNumber();
	MesquiteNumber expectedScreenX = new MesquiteNumber();
	MesquiteNumber expectedScreenY = new MesquiteNumber();
	final static int crossSize = 5;
	MapProjection projectionTask;
	final static int near = 5;
	
	public CalibrationPoint(MapProjection projectionTask, MesquiteNumber longitude, MesquiteNumber latitude, int x, int y) {
		this.longitude.setValue(longitude);
		this.latitude.setValue(latitude);
		this.x.setValue(x);
		this.y.setValue(y);
		this.projectionTask = projectionTask;
	}
	/*.................................................................................................................*/
   	 public MesquiteNumber getLongitude(){
   	 	return longitude;
   	 }
	/*.................................................................................................................*/
   	 public MesquiteNumber getLatitude(){
   	 	return latitude;
   	 }
	/*.................................................................................................................*/
   	 public boolean projectedXLegal(){
   	 	return projectedX.isCombinable();
   	 }
	/*.................................................................................................................*/
   	 public boolean projectedYLegal(){
   	 	return projectedY.isCombinable();
   	 }
	/*.................................................................................................................*/
   	 public MesquiteNumber getProjectedX(){
   	 	return projectedX;
   	 }
	/*.................................................................................................................*/
   	 public MesquiteNumber getProjectedY(){
   	 	return projectedY;
   	 }
	/*.................................................................................................................*/
   	 public MesquiteNumber getX(){
   	 	return x;
   	 }
	/*.................................................................................................................*/
   	 public MesquiteNumber getY(){
   	 	return y;
   	 }
	/*.................................................................................................................*/
   	 public boolean nearPoint(int touchX, int touchY){
   	 	return ((Math.abs(x.getIntValue() - touchX) < near) && (Math.abs(y.getIntValue()-touchY)<near));
   	 }
	/*.................................................................................................................*/
   	 public void setExpectedScreenValues(){
		projectionTask.convertLongLatsToScreenCoordinates(longitude.getDoubleValue(), latitude.getDoubleValue(), expectedScreenX, expectedScreenY);
	}
	/*.................................................................................................................*/
   	 public int calibrationMismatch (int margin){
   	 	if (!expectedScreenX.isCombinable() || !expectedScreenY.isCombinable())
   	 		return MesquiteInteger.impossible;
   	 	if (!x.isCombinable() || !y.isCombinable())
   	 		return MesquiteInteger.impossible;
   	 	int mismatchX =  Math.abs(expectedScreenX.getIntValue()-margin-x.getIntValue());
   	 	int mismatchY = Math.abs(expectedScreenY.getIntValue()-margin-y.getIntValue());
//Debugg.println("mismatchX: " + mismatchX + ", mismatchY: " + mismatchY);   	 	
		if (!MesquiteInteger.isCombinable(mismatchX) || !MesquiteInteger.isCombinable(mismatchY))
			return MesquiteInteger.impossible;
		else
			return (mismatchX + mismatchY);
	}
	/*.................................................................................................................*/
   	 public void setProjectedValues(){
		projectionTask.convertToUnscaledProjectionCoordinates(longitude.getDoubleValue(), latitude.getDoubleValue(), projectedX, projectedY);
		
	}
	/*.................................................................................................................*/
   	 public String toString(){
   	 	return "'" + longitude.toString()+ "' '" + latitude.toString()+ "' " +  x.toString()+ " " + y.toString() ;
   	 }
	/*.................................................................................................................*/
	public void drawPoint(Graphics g, int margin) {
		g.setColor(Color.cyan);
		int X = x.getIntValue()+margin;
		int Y = y.getIntValue()+margin;
		g.drawLine(X-crossSize,Y-1,X+crossSize,Y-1);
		g.drawLine(X-1,Y-crossSize,X-1,Y+crossSize);
		g.drawLine(X-crossSize,Y+1,X+crossSize,Y+1);
		g.drawLine(X+1,Y-crossSize,X+1,Y+crossSize);
		g.setColor(Color.blue);
		g.drawLine(X-crossSize,Y,X+crossSize,Y);
		g.drawLine(X,Y-crossSize,X,Y+crossSize);
		g.setColor(Color.cyan);
		StringInABox.drawStringIfNotBlank(g, "("+latitude+", " + longitude + ")", X+6,Y-5);
		g.setColor(Color.blue);
		StringInABox.drawStringIfNotBlank(g, "("+latitude+", " + longitude + ")", X+5,Y-6);

	}
}




