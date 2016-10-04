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
public class Object2DArray implements Listable {
	Object[][] values;
	NameReference name=null;
	public Object2DArray(int numC, int numT){
		values = new Object[numC][numT];
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
			if (values[i]!=null)
				for (int j=0; j<values[i].length;j++)
					values[i][j] =  null;
	}
	/*...........................................................*/
	public void setValue(int column, int row, Object value) {
		if (column >=0 && column < values.length && values[column]!=null && row>=0 && row<values[column].length)
			values[column][row]= value;
	}
	public Object getValue(int column, int row){
		if (column >=0 && column < values.length && values[column]!=null && row>=0 && row<values[column].length)
			return values[column][row];
		else
			return null;
	}
	/*...........................................................*/
	public Object[][] getMatrix() {
		return values;
	}
	/*...........................................................*/
	public void setMatrix(Object[][] m) {
		values = m;
	}
	/*...........................................................*/
	public static void moveColumns(Object[][] d, int starting, int num, int justAfter) {
		if (num<=0 || d==null || starting>=d.length || (justAfter>=starting && justAfter<=starting+num-1)) //starting???
			return;
		if (justAfter>=d.length)
			justAfter = d.length-1;
		if (justAfter<0)
			justAfter = -1;
		Object[][] newValues = new Object[d.length][];
		if (starting>justAfter){
			int count =0;
			for (int i=0; i<=justAfter; i++)
				newValues[count++]=d[i];
			for (int i=starting; i<=starting+num-1; i++)
				newValues[count++]=d[i];
			for (int i=justAfter+1; i<=starting-1; i++)
				newValues[count++]=d[i];
			for (int i=starting+num; i<d.length; i++)
				newValues[count++]=d[i];
		}
		else {   // (starting<=justAfter)
			int count =0;
			for (int i=0; i<=starting-1; i++)
				newValues[count++]=d[i];
			for (int i=starting+num; i<=justAfter; i++)
				newValues[count++]=d[i];
			for (int i=starting; i<=starting+num-1; i++)
				newValues[count++]=d[i];
			for (int i=justAfter+1; i<d.length; i++)
				newValues[count++]=d[i];
		}
		for (int i=0; i<d.length; i++)
			d[i]=newValues[i];
	}
	/*...........................................................*/
	public static void swapColumns(Object[][] d, int first, int second) {
		if (first<0 || d==null || first>=d.length || second<0 || second>=d.length) 
			return;
		Object[] temp = d[first];
		d[first]=d[second];
		d[second] = temp;
	}
	/*...........................................................*/
	public static void swapCell(Object[][] d, int first, int second, int k) {
		if (k<0)
			return;
		if (first<0 || d==null || first>=d.length || second<0 || second>=d.length) 
			return;
		if (d[first] == null || k>= d[first].length || d[second] == null || k>= d[second].length)
			return;
		Object temp = d[first][k];
		d[first][k]=d[second][k];
		d[second][k] = temp;
	}
	/*...........................................................*/
	public static void moveRows(Object[][] d, int starting, int num, int justAfter) {
		if (num<=0 || d==null || d.length == 0)
			return;
		int numRows = d[0].length;
		if (starting>=numRows || (justAfter>=starting && justAfter<=starting+num-1)) //starting???
			return;
		if (justAfter>=numRows)
			justAfter = numRows-1;
		if (justAfter<0)
			justAfter = -1;
		Object[] newValues = new Object[numRows];
		for (int column = 0; column<d.length; column++){
			if (starting>justAfter){
				int count =0;
				for (int i=0; i<=justAfter; i++)
					newValues[count++]=d[column][i];
				
				for (int i=starting; i<=starting+num-1; i++)
					newValues[count++]=d[column][i];
				for (int i=justAfter+1; i<=starting-1; i++)
					newValues[count++]=d[column][i];
				for (int i=starting+num; i<numRows; i++)
					newValues[count++]=d[column][i];
			}
			else {
				int count =0;
				for (int i=0; i<=starting-1; i++)
					newValues[count++]=d[column][i];
				
				for (int i=starting+num; i<=justAfter; i++)
					newValues[count++]=d[column][i];
				for (int i=starting; i<=starting+num-1; i++)
					newValues[count++]=d[column][i];
				for (int i=justAfter+1; i<numRows; i++)
					newValues[count++]=d[column][i];
			}
			for (int i=0; i<numRows; i++)
				d[column][i]=newValues[i];
		}
	}
	public static Object[][] addRows(Object[][] d, int starting, int num) {
		if (num==0 || d == null || d.length == 0)
			return d;
		
		for (int column = 0; column<d.length; column++){
			d[column]=ObjectArray.addParts(d[column], starting, num);
		}
		return d;
	}
	public static Object[][] deleteRows(Object[][] d, int starting, int num) {
		if (num==0 || d == null || d.length == 0)
			return d;
		for (int column = 0; column<d.length; column++){
			d[column]=ObjectArray.deleteParts(d[column], starting, num);
		}
		return d;
	}
	/*...........................................................*/
}

