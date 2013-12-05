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

/*==========================  Mesquite Basic Class Library    ==========================*/
/*===  the basic classes used by the trunk of Mesquite and available to the modules

/* ======================================================================== */
/** A specset with numerical information.*/
public abstract class NumSpecsSet extends SpecsSet  {
	NumberArray nums;
	public NumSpecsSet(String name, int numParts, MesquiteNumber defaultValue){
		super(name, numParts);
		nums = new NumberArray(numParts);
		if (defaultValue!=null)
			for (int i=0; i<numParts; i++)
				nums.setValue(i, defaultValue);
	}
	public String getTypeName(){
		return "Number specificaton set";
	}
	public NumberArray getNumberArray(){
		return nums;
	}
	public boolean equals(int index, MesquiteNumber n) {
		return nums.equals(index, n);
	}
 	/*.................................................................................................................*/
 	/** Sets the value for part "part" to be the same as that at part "otherPart" in the incoming specsSet*/
	public void equalizeSpecs(SpecsSet other, int otherPart, int part){
		if (other instanceof NumSpecsSet){
			NumSpecsSet nss = (NumSpecsSet)other;
			if (nss.isInt()){
				int n = nss.getInt(otherPart);
				setValue(part, n);
			}
			else if (nss.isLong()){
				long n = nss.getLong(otherPart);
				setValue(part, n);
			}
			else if (nss.isDouble()){
				double n = nss.getDouble(otherPart);
				setValue(part, n);
			}
		}
	}
 	/*.................................................................................................................*/
	/** Returns whether or not number type is "int". */
	public boolean isInt(){
		return nums.getValueClass() == NumberArray.INT;
	}
 	/*.................................................................................................................*/
	/** Returns whether or not number type is "long". */
	public boolean isLong(){
		return nums.getValueClass() == NumberArray.LONG;
	}
 	/*.................................................................................................................*/
	/** Returns whether or not number type is "double". */
	public boolean isDouble(){
		return nums.getValueClass() == NumberArray.DOUBLE;
	}
 	/*.................................................................................................................*/
	/** Returns whether part ic is unassigned. */
	public boolean isUnassigned(int ic){
		return nums.isUnassigned(ic);
	}
 	/*.................................................................................................................*/
	/** Returns int value of part ic. */
	public int getInt(int ic){
		return nums.getInt(ic);
	}
 	/*.................................................................................................................*/
	/** Returns long value of part ic. */
	public long getLong(int ic){
		return nums.getLong(ic);
	}
 	/*.................................................................................................................*/
	/** Returns double value of part ic. */
	public double getDouble(int ic){
		return nums.getDouble(ic);
	}
	/** Places the value of part ic into the passed MesquiteNumber*/
	public void placeValue(int ic, MesquiteNumber n){
		if (n==null)
			return;
		nums.placeValue(ic, n);
	}
	/** Set value of part ic */
	public void setValue(int ic, MesquiteNumber n){
		setDirty(true);
		nums.setValue(ic, n);
	}
	/** Set value of part ic */
	public void setValue(int ic, int n){
		setDirty(true);
		nums.setValue(ic, n);
	}
	/** Set value of part ic */
	public void setValue(int ic, long n){
		setDirty(true);
		nums.setValue(ic, n);
	}
	/** Set value of part ic */
	public void setValue(int ic, double n){
		setDirty(true);
		nums.setValue(ic, n);
	}
	/** Deassigns all values */
	public void deassignAll(){
		setDirty(true);
		nums.deassignArray();
	}
	/** Zeros all values */
	public void zeroAll(){
		setDirty(true);
		nums.zeroArray();
	}
 	/*.................................................................................................................*/
	/** Returns a string representing the number assigned to part ic. */
	public String toString(int ic){
		return nums.toString(ic);
	}
 	/*.................................................................................................................*/
	/** Add num parts just after "starting" (filling with default values)  */
  	public boolean addParts(int starting, int num){  
		setDirty(true);
 		nums.addParts(starting, num); //TODO: set default (for inclusion, set to on!!!)
 		if (defaultValue != null)
 			for (int i = starting+1; i<=starting+num; i++)
 				nums.setValue(i, defaultValue);
		numParts = nums.getSize();
		return true;
	}
  	
	/*.................................................................................................................*/
	/** Sets default value, e.g. "1" for weights  */
	MesquiteNumber defaultValue;
  	public void setDefaultValue(MesquiteNumber def){
  		defaultValue = def;
  	}
	/*.................................................................................................................*/
	/** Delete num parts from and including "starting"  */
	public boolean deleteParts(int starting, int num){ 
		setDirty(true);
 		nums.deleteParts(starting, num);
		numParts = nums.getSize();
		return true;
	}
	/*.................................................................................................................*/
 	/** */
	public boolean moveParts(int starting, int num, int justAfter){  
		setDirty(true);
 		nums.moveParts(starting, num, justAfter);
		return true;
	}
	/*.................................................................................................................*/
 	/** */
	public boolean swapParts(int first, int second){  
		setDirty(true);
 		nums.swapParts(first, second);
		return true;
	}
}

