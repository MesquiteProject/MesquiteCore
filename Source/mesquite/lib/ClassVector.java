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
import java.util.*;

/* ======================================================================== */
/*To keep track of classes used*/
public class ClassVector extends ListableVector {
	public ClassVector(){
		super();
	}
	public void record(Class c){
		if (c==null)
			return;
		CRecord cr = (CRecord)elementWithName(c.getName());
		if (cr == null)
			addElement(new CRecord(c), false);
		else
			cr.increment();
	}
	public void recordWithTime(Class c){
		if (c==null)
			return;
		CRecord cr = (CRecord)elementWithName(c.getName());
		if (cr == null)
			addElement(cr = new CRecord(c), false);
		else
			cr.increment();
		cr.startTime();
	}
	public void stopTime(Class c){
		if (c==null)
			return;
		CRecord cr = (CRecord)elementWithName(c.getName());
		if (cr == null)
			return;
		cr.stopTime();
	}
	public String recordsToString(){
		Listable[] array = getElementArray(); //firstSort
		for (int i=1; i<array.length; i++) {
			for (int j= i-1; j>=0 && ((CRecord)array[j]).count>((CRecord)array[j+1]).count; j--) {
				Listable temp = array[j];
				array[j] = array[j+1];
				array[j+1]=temp;
			}
		}
		String s = "";
		for (int i=0; i<array.length; i++){
			CRecord cr = (CRecord)array[i];
			s += "   " + cr + "\n";
		}
		return s;
	}
}

class CRecord implements Listable{
	long count;
	long times = 0;
	String name;
	MesquiteTimer timer;
	public CRecord(Class c) {
		name = c.getName();
		count = 1;
	}
	public void increment(){	
		count++;
	}
	public void startTime(){
		if (timer == null)
			timer = new MesquiteTimer();
		times++;
		timer.start();
	}
	public void stopTime(){
		if (timer == null)
			timer = new MesquiteTimer();
		times--;
		timer.end();
	}
	public String toString(){
		if (timer == null)
			return name + "  " + count;
		else if (times !=0)
			return name + "  " + count + " (time: " + timer.getAccumulatedTime() + " ERROR: times = " + times + ")";
		else
			return name + "  " + count + " (time: " + timer.getAccumulatedTime() + ")";
	}
	public String getName(){
		return name;
	}
}

