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
import java.text.*;


/* ======================================================================== */
public class ObjectArray implements Listable {
	Object[] values;
	NameReference name=null;
	public ObjectArray(int num){
		values = new Object[num];
		for (int i=0; i<num; i++)
			values[i] =  null;
	}
	//used for Associables that might need to record whether reference is to part or in between part
	//intially for MesquiteTree to know if info applies to node or branch ancestral to node
	boolean between = false;
	public void setBetweenness(boolean b){
		between = true;  
	}
	public boolean isBetween(){
		return between;
	}
	/*...........................................................*/
	public String getName(){
		if (name!=null)
			return name.getValue();
		else
			return "";
	}
	/*...........................................................*/
	public void setNameReference(NameReference nr){
		name = nr;
	}
	/*...........................................................*/
	public NameReference getNameReference(){
		return name;
	}
	public void zeroArray(){
		for (int i=0; i<values.length; i++)
			values[i] =  null;
	}
	/*...........................................................*/
	public void copyTo(ObjectArray d){
		if (d==null || d.values.length!=values.length)
			return;
		for (int i=0; i<values.length; i++) {
			d.values[i] =  values[i];
		}
	}
	public void setValue(int index, Object value) {
		if (index >=0 && index < values.length)
			values[index] = value;
	}
	public Object getValue(int index){
		if (index <0 || index >= values.length)
			return null;
		else
			return values[index];
	}
	/*...........................................................*/
	public static int indexOf(Object[] values, Object match){
		if (values==null)
			return -1;
		for (int i=0; i<values.length; i++)
			if (values[i]== match)
				return i;
		return -1;
	}
	/*...........................................................*/
	public int indexOf(Object match){
		if (values==null)
			return -1;
		for (int i=0; i<values.length; i++)
			if (values[i]== match)
				return i;
		return -1;
	}
	/*...........................................................*/
	public int getSize() {
		return values.length;
	}
	/*...........................................................*/
	public void resetSize(int newNum) {
		if (newNum == getSize())
			return;
		Object[] newValues = new Object[newNum];
		for (int i=0; i<getSize() && i<newNum; i++)
			newValues[i]=values[i];
		if (newNum>getSize())
			for (int i=getSize(); i<newNum; i++)
				newValues[i]=null;
		values=newValues;
	}
	/*...........................................................*/
	public boolean addParts(int starting, int num){
		if (num<=0)
			return false;
		values = addParts(values, starting, num);
		return true;
	}
	/*...........................................................*/
	public boolean deleteParts(int starting, int num){

		if (num==0)
			return false;
		if (starting>values.length || starting<0) 
			return false;
		values = deleteParts(values, starting, num);
		return true;
	}
	/*...........................................................*/
	public static Object[] addParts(Object[] values, int starting, int num){
		if (num<=0 || values == null)
			return values;
		if (starting<0) 
			starting = -1;
		if (starting>values.length) 
			starting = values.length-1;
		int newNumParts = values.length+num;
		Object[] newValues = new Object[newNumParts];
		for (int i=0; i<= starting; i++) {
			newValues[i] = values[i];
		}
		for (int i=0; i<num ; i++) {
			newValues[starting + i + 1] = null;
		}
		for (int i=0; i<values.length-starting-1; i++) {
			newValues[i +starting+num+1] = values[starting + i+1];
		}
		return newValues;
	}
	/*...........................................................*/
	public static Object[] deleteParts(Object[] values, int starting, int num){
		if (num<=0 || values == null)
			return values;
		if (num+starting>values.length)
			num = values.length-starting;
		int newNumParts = values.length-num;
		Object[] newValues = new Object[newNumParts];
		
		for (int i=0; i<starting; i++) {
			newValues[i] = values[i];
		}
		for (int i=starting+num; i<values.length; i++) {
			newValues[i-num ] = values[i];
		}
		return newValues;
	}
	/*...........................................................*/
	public static void swapParts(Object[] d, int first, int second) {
		if (d == null || first<0 || first>=d.length || second<0 || second>=d.length) 
			return;
		Object temp = d[first];
		d[first] = d[second];
		d[second] = temp;
	}
	/*...........................................................*/
	public boolean swapParts(int first, int second) {
		if (first<0 || first>=values.length || second<0 || second>=values.length) 
			return false;
		Object temp = values[first];
		values[first] = values[second];
		values[second] = temp;
		return true;
	}
	/*...........................................................*/
	public boolean moveParts(int starting, int num, int justAfter){
		if (num<=0)
			return false;
		if (starting<0)
			return false;
		else if (starting>values.length)
			return false;

		Object[] newValues = new Object [values.length];
		
		if (starting>justAfter){
			int count =0;
			for (int i=0; i<=justAfter; i++)
				newValues[count++]=values[i];
			
			for (int i=starting; i<=starting+num-1; i++)
				newValues[count++]=values[i];
			for (int i=justAfter+1; i<=starting-1; i++)
				newValues[count++]=values[i];
			for (int i=starting+num; i<values.length; i++)
				newValues[count++]=values[i];
		}
		else {
			int count =0;
			for (int i=0; i<=starting-1; i++)
				newValues[count++]=values[i];
			
			for (int i=starting+num; i<=justAfter; i++)
				newValues[count++]=values[i];
			for (int i=starting; i<=starting+num-1; i++)
				newValues[count++]=values[i];
			for (int i=justAfter+1; i<values.length; i++)
				newValues[count++]=values[i];
		}
		for (int i=0; i<values.length; i++)
			values[i]=newValues[i];
		return true;
	}
	/*...........................................................*/
}

