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

import mesquite.lib.characters.CharacterModel;

/*==========================  Mesquite Basic Class Library    ==========================*/
/*===  the basic classes used by the trunk of Mesquite and available to the modules
/* ======================================================================== */
/** DOCUMENT.*/
public abstract class ObjectSpecsSet  extends SpecsSet {
	Object[] properties;
	Object defaultProperty;
	public ObjectSpecsSet (String name, int numParts, Object defaultProperty) {
		super(name, numParts);
		this.defaultProperty = defaultProperty;
		properties = getNewPropertyStorage(numParts);
		for (int i=0; i<numParts; i++) {
			properties[i] = defaultProperty;
		}
	}
	
	public String toString(int ic){
		return "" + getProperty(ic);
	}
 	/*.................................................................................................................*/
 	/** Sets model of character*/
	public void setProperty(Object property, int index) {
		setDirty(true);
		if (index<0)
			System.out.println("Error: attempt to set object of part-- " + index + " " + this);
		else if (index>=properties.length)
			System.out.println("Error: attempt to set object of part- " + index + " " + this);
		else
			properties[index]=property;
	}
 	/*.................................................................................................................*/
 	/** Sets the value for part "part" to be the same as that at part "otherPart" in the incoming specsSet*/
	public void equalizeSpecs(SpecsSet other, int otherPart, int part){
		if (other instanceof ObjectSpecsSet){
			setProperty(((ObjectSpecsSet)other).getProperty(otherPart), part);
		}
	}
 	/*.................................................................................................................*/
 	/** return array of character properties (can also get indivitually using getProperty)*/
	public Object[] getProperties() {
		return properties;
	}
 	/*.................................................................................................................*/
 	/** returns property for specified character*/
	public Object getProperty(int index) {
		if (index>= 0 && index< properties.length)
			return properties[index];
		else
			return null;
	}
 	/*.................................................................................................................*/
 	/** Gets default property specified for PropertySet*/
	public Object getDefaultProperty() {
		return defaultProperty;
	}
 	/*.................................................................................................................*/
 	/** sets default property*/
	public void setDefaultProperty(Object defaultProperty) {
		setDirty(true);
		this.defaultProperty = defaultProperty;
	}
	/*.................................................................................................................*/
 	/** Gets default model specified for ModelSet*/
	public Object getDefaultProperty(int ic) {
		return getDefaultProperty();
	}
 	/*.................................................................................................................*/
 	/** gets storage for set of properties*/
	public abstract Object[] getNewPropertyStorage(int numParts);
	/*.................................................................................................................*/
	/** Add num characters just after "starting" (filling with default values)  */
  	public boolean addParts(int starting, int num){  
  		if (num <=0)
  			return false;
  		if (getProject() != null && getProject().isDoomed)
  			return false;
		setDirty(true);
		if (starting<0)
			starting = -1;
		else if (starting>=getNumberOfParts())
			starting = getNumberOfParts()-1;
		int newNumParts = getNumberOfParts() + num;
		Object[] newProperties = getNewPropertyStorage(newNumParts);
		for (int i=0; i<=starting; i++) {
			newProperties[i] = properties[i];
		}
		for (int i=0; i<num; i++) {
			newProperties[starting + i + 1] = getDefaultProperty(starting);
		}
		for (int i=0; i<properties.length-starting-1; i++) {
			newProperties[i +starting+num+1] = properties[starting + i+1];
		}

		properties = newProperties;
		numParts = newNumParts;
		return true;
	}
	/*.................................................................................................................*/
	/** Delete num characters from and including "starting"  */
	public boolean deleteParts(int starting, int num){  
		if (num<=0 || starting<0 || starting>=getNumberOfParts())
			return false;
		setDirty(true);
		if (num+starting>getNumberOfParts())
			num = getNumberOfParts()-starting;
		int newNumParts = getNumberOfParts() - num;
		Object[] newProperties = getNewPropertyStorage(newNumParts);
		for (int i=0; i<starting; i++) {
			newProperties[i] = properties[i];
		}
		for (int i=starting+num; i<properties.length; i++) {
			newProperties[i-num ] = properties[i];
		}
		properties = newProperties;
		numParts = newNumParts;
		return true;
	}
	/*...........................................................*/
	public boolean swapParts( int first, int second) {
		if (first<0 || first>=properties.length || second<0 || second>=properties.length) 
			return false;
		setDirty(true);
		Object temp = properties[first];
		properties[first] = properties[second];
		properties[second] = temp;
		return true;
	}
	/*.................................................................................................................*/
 	/** */
	public boolean moveParts(int starting, int num, int justAfter){  
		if (num<=0)
			return false;
		if (starting<0)
			return false;
		else if (starting>getNumberOfParts())
			return false;
		setDirty(true);
		Object[] newValues = getNewPropertyStorage(getNumberOfParts());
		
		if (starting>justAfter){
			int count =0;
			for (int i=0; i<=justAfter; i++)
				newValues[count++]=properties[i];
			
			for (int i=starting; i<=starting+num-1; i++)
				newValues[count++]=properties[i];
			for (int i=justAfter+1; i<=starting-1; i++)
				newValues[count++]=properties[i];
			for (int i=starting+num; i<properties.length; i++)
				newValues[count++]=properties[i];
		}
		else {
			int count =0;
			for (int i=0; i<=starting-1; i++)
				newValues[count++]=properties[i];
			
			for (int i=starting+num; i<=justAfter; i++)
				newValues[count++]=properties[i];
			for (int i=starting; i<=starting+num-1; i++)
				newValues[count++]=properties[i];
			for (int i=justAfter+1; i<properties.length; i++)
				newValues[count++]=properties[i];
		}
		for (int i=0; i<properties.length; i++)
			properties[i]=newValues[i];
		return true;
	}
 	/*.................................................................................................................*/
 	public void disposing(Object obj){
 		
 		for (int i=0; i<properties.length; i++) {
 			if (properties[i] == obj)
 				properties[i] = getDefaultProperty();
 		}
 		
 	}

}



