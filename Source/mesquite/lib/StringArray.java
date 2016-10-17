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


/* ======================================================================== */
public class StringArray implements StringLister, Listable {
	String[] values;
	int maxFilled = -1;
	NameReference name=null;
	public StringArray(int num){	
		values = new String[num];
		for (int i=0; i<num; i++)
			values[i] =  null;
	}
	/*...........................................................*/
	public String[] getStrings(){
		return values;
	}
	/*...........................................................*/
	public String[] getFilledStrings(){
		int count=0;
		for (int i=0; i<getFilledSize(); i++)
			if (values[i]!=null)
				count++;
		String[] strings = new String[count];
		count=0;
		for (int i=0; i<getFilledSize(); i++)
			if (values[i]!=null) {
				strings[count] = values[i];
				count++;
			}
		return strings;
	}
	/*...........................................................*/
	public static boolean allBlank(String[] array){
		if (array==null || array.length==0)
			return true;
		for (int i=0; i<array.length; i++) {
			if (StringUtil.notEmpty(array[i]))
				return false;
		}
		return true;
	}
	/*...........................................................*/
	public static void sort(String[] array){
		if (array==null || array.length<=1)
			return;
		MesquiteCollator coll = new MesquiteCollator();
		for (int i=1; i<array.length; i++) {
			for (int j= i-1; j>=0 && coll.compare(array[j],array[j+1])>0; j--) {
				String temp = array[j];
				array[j] = array[j+1];
				array[j+1]=temp;
			}
		}
		
	}
	/*...........................................................*/
	public String getValue(int index){
		if (index <0 || index >= values.length)
			return null;
		else
			return values[index];
	}
	/*...........................................................*/
	public void setValue(int index, String value) {
		if (index >=0 && index < values.length) {
			values[index] = value;
			if (index>maxFilled)
				maxFilled = index;
		}
	}
	/*...........................................................*/
	public void deassignArray(){
		for (int i=0; i<values.length; i++)
			values[i] =  null;
		maxFilled = -1;
	}

	/*...........................................................*/
	public static int lastIndexOfIgnoreCase(String[]s, String match){
		if (match == null || s==null)
			return -1;
		for (int i=s.length-1; i>=0; i--)
			if (match.equalsIgnoreCase(s[i]))
				return i;
		return -1;
	}
	/*...........................................................*/
	public static int lastIndexOf(String[]s, String match){
		if (match == null || s==null)
			return -1;
		for (int i=s.length-1; i>=0; i--)
			if (match.equals(s[i]))
				return i;
		return -1;
	}
	/*...........................................................*/
	public static int indexOfIgnoreCase(String[]s, String match){
		if (match == null || s==null)
			return -1;
		for (int i=0; i<s.length; i++)
			if (match.equalsIgnoreCase(s[i]))
				return i;
		return -1;
	}
	/*...........................................................*/
	public int indexOfIgnoreCase(String match){
		if (match == null)
			return -1;
		for (int i=0; i<values.length; i++)
			if (match.equalsIgnoreCase(values[i]))
				return i;
		return -1;
	}
	/*...........................................................*/
	public static int indexOf(String[]s, String match){
		if (match == null || s==null)
			return -1;
		for (int i=0; i<s.length; i++)
			if (match.equals(s[i]))
				return i;
		return -1;
	}
	/*...........................................................*/
	public static int indexOfIgnoreCase(java.util.Vector v, String match){
		if (match == null)
			return -1;
		for (int i=0; i<v.size(); i++){
			String string = (String)v.elementAt(i);
			if (match.equalsIgnoreCase(string))
				return i;
		}
		return -1;
	}
	/*...........................................................*/
	public static int indexOf(java.util.Vector v, String match){
		if (match == null)
			return -1;
		for (int i=0; i<v.size(); i++){
			String string = (String)v.elementAt(i);
			if (match.equals(string))
				return i;
		}
		return -1;
	}
	/*...........................................................*/
	public static int indexOfIgnoreCase(String[][] s, int firstIndex, String match){
		if (match == null || s==null || s.length == 0 || firstIndex>=s.length || firstIndex<0)
			return -1;
		for (int i=0; i<s[firstIndex].length; i++)
			if (match.equalsIgnoreCase(s[firstIndex][i]))
				return i;
		return -1;
	}
	/*...........................................................*/
	public int indexOf(String match){
		if (match == null)
			return -1;
		for (int i=0; i<values.length; i++)
			if (match.equals(values[i]))
				return i;
		return -1;
	}
	/*...........................................................*/
	public boolean exists(String s){
		return indexOf(s)>=0;
	}

	/*...........................................................*/
	public boolean fillNextUnassigned(String v){
		for (int i=0; i<values.length; i++)
			if (values[i]== null) {
				values[i] = v;
				if (i>maxFilled)
					maxFilled = i;
				return true;
			}
		return false;
	}
	/*...........................................................*/
	public boolean addAndFillNextUnassigned(String v){
		if (!fillNextUnassigned(v)) {
			resetSize(getSize()+10);
			return fillNextUnassigned(v);
		}
		return true;
	}
	/*...........................................................*/
	public int getSize() {
		return values.length;
	}
	
	/** Concatenates the two String arrays together */
	public static String[] concatenate(String[] d1, String[] d2) {
		if (d1==null && d2==null)
			return null;
		int newNumParts = 0;
		int d1Length = 0;
		int d2Length = 0;
		if (d2==null) {
			newNumParts = d1.length;
			d1Length = d1.length;
		}
		else if (d1==null) {
			newNumParts = d2.length;
			d2Length = d2.length;
		}
		else {
			newNumParts = d1.length+d2.length;
			d1Length = d1.length;
			d2Length = d2.length;
		}
		if (newNumParts==0)
			return null;
		
		String[] newValues = new String[newNumParts];
		
		
		if (d1!=null)
			for (int i=0; i< d1Length; i++) {
				newValues[i] = d1[i];
			}
		if (d2!=null)
			for (int i=0; i< d2Length; i++) {
				newValues[i+d1Length] = d2[i];
			}
		return newValues;
	}
	/*...........................................................*/
	public void addParts(int starting, int num) {
		values=addParts(values, starting, num);
	}
	/*...........................................................*/
	public static String[] addParts(String[] d, int starting, int num) {
		if (num<=0 || d == null)
			return d;
		if (starting<0) 
			starting = -1;
		if (starting>d.length) 
			starting = d.length-1;
		
		int newNumParts = d.length+num;
		String[] newValues = new String[newNumParts];
		
		for (int i=0; i<= starting; i++) {
			newValues[i] = d[i];
		}
		for (int i=0; i<num ; i++) {
			newValues[starting + i + 1] = null;
		}
		for (int i=0; i<d.length-starting-1; i++) {
			newValues[i +starting+num+1] = d[starting + i+1];
		}
		return newValues;
	}
	/*...........................................................*/
	public static String[] addToStart(String[] d, String s) {
		String[] sa = addParts(d,-1,1);
		sa[0]=s;
		return sa;
	}
	/*...........................................................*/
	public void deleteParts(int starting, int num) {
		if (num<=0 || starting<0 || starting>=getSize())
			return;
		values = deleteParts(values, starting, num);
	}
	/*...........................................................*/
	public static String[] deleteParts(String[] d, int starting, int num) {
		if (d==null || num<=0 || starting<0 || starting>=d.length)
			return d;
		if (num+starting>d.length)
			num = d.length-starting;

		int newNum =d.length - num;
		String[] newValues = new String[newNum];
		for (int i=0; i<starting; i++)
			newValues[i]=d[i];
		for (int i=starting+num; i<d.length; i++)
			newValues[i-num]=d[i];
		return newValues;
	}
	/*...........................................................*/
	public void moveParts(int starting, int num, int justAfter) {
		moveParts(values, starting, num, justAfter);
	}
	/*...........................................................*/
	public static void swapParts(String[] d, int first, int second) {
		if (d == null || first<0 || first>=d.length || second<0 || second>=d.length) 
			return;
		String temp = d[first];
		d[first] = d[second];
		d[second] = temp;
	}
	/*...........................................................*/
	public static void swapCell(String[][] d, int first, int second, int k) {
		if (k<0)
			return;
		if (first<0 || d==null || first>=d.length || second<0 || second>=d.length) 
			return;
		if (d[first] == null || k>= d[first].length || d[second] == null || k>= d[second].length)
			return;
		String temp = d[first][k];
		d[first][k]=d[second][k];
		d[second][k] = temp;
	}
	/*...........................................................*/
	public static void swapColumns(String[][] d, int first, int second) {
		if (first<0 || d==null || first>=d.length || second<0 || second>=d.length) 
			return;
		String[] temp = d[first];
		d[first]=d[second];
		d[second] = temp;
	}
	/*...........................................................*/
	public static void moveParts(String[] d, int starting, int num, int justAfter) {
		if (num<=0 || d==null || starting>=d.length || (justAfter>=starting && justAfter<=starting+num-1)) //starting???
			return;
		if (justAfter>=d.length)
			justAfter = d.length-1;
		if (justAfter<0)
			justAfter = -1;
		String[] newValues = new String[d.length];
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
		else {
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
	/** assumes String[columns][rows] */
	public static void moveRows(String[][] d, int starting, int num, int justAfter) {
		if (num<=0 || d==null || d.length == 0)
			return;
		int numRows = d[0].length;
		if (starting>=numRows || (justAfter>=starting && justAfter<=starting+num-1)) //starting???
			return;
		if (justAfter>=numRows)
			justAfter = numRows-1;
		if (justAfter<0)
			justAfter = -1;
		String[] newValues = new String[numRows];
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
	public static String[][] addRows(String[][] d, int starting, int num) {
		if (num==0 || d == null || d.length == 0)
			return d;
		for (int column = 0; column<d.length; column++){
			d[column]=addParts(d[column], starting, num);
		}
		return d;
	}
	public static String[][] deleteRows(String[][] d, int starting, int num) {
		if (num==0 || d == null || d.length == 0)
			return d;
		for (int column = 0; column<d.length; column++){
			d[column]=deleteParts(d[column], starting, num);
		}
		return d;
	}
	/*...........................................................*/
	public static void moveColumns(String[][] d, int starting, int num, int justAfter) {
		if (num<=0 || d==null || starting>=d.length || (justAfter>=starting && justAfter<=starting+num-1)) //starting???
			return;
		if (justAfter>=d.length)
			justAfter = d.length-1;
		if (justAfter<0)
			justAfter = -1;
		String[][] newValues = new String[d.length][];
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
	public void resetSize(int newNum) {
		if (newNum == getSize())
			return;
		String[] newValues = new String[newNum];
		for (int i=0; i<getSize() && i<newNum; i++)
			newValues[i]=values[i];
		values=newValues;
	}
	/*...........................................................*/
	public int getFilledSize() {
		return maxFilled+1;
	}
	/*...........................................................*/
	public String getName(){
		if (name!=null)
			return name.getValue();
		else
			return "";
	}
	/*...........................................................*/
	public void setName(String nr){
		name = NameReference.getNameReference(nr);
	}
		/*...........................................................*/
	public void setNameReference(NameReference nr){
		name = nr;
	}
	/*...........................................................*/
	public NameReference getNameReference(){
		return name;
	}
	/*...........................................................*/
	public static boolean equals(String[] s, String[] t){
		if (s==null && t == null)
			return true;
		if (s==null || t == null)
			return false;
		if (s.length != t.length)
			return false;
		for (int i=0; i<s.length; i++) {
			if (s[i] != null && t[i]!=null) {
				if (!s[i].equals(t[i]))
					return false;
			}
			else if (s[i]!=t[i])
				return false;
		}
		return true;
	}
	/*...........................................................*/
	public String toString(){
		if (values==null)
			return "null";
		String r = "";
		for (int i=0; i<values.length; i++)
			r += "[" +values[i] + "] ";
		return r;
	}
	/*...........................................................*/
	public static String toString(String[]s){
		if (s==null)
			return "null";
		String r = "";
		for (int i=0; i<s.length; i++)
			r += "[" +s[i] + "] ";
		return r;
	}
	/*...........................................................*/
	public static String toString(String[][] s){
		if (s==null)
			return "null";
		String r = "";
		for (int i=0; i<s.length; i++) {
			r += "[";
			for (int j=0;j<s[i].length; j++)
				r += "(" +s[i][j] + ") ";
			r += "]\n";
		}
		return r;
	}
}


