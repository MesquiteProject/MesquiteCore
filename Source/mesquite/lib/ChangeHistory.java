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
import java.util.*;



/* ======================================================================== */
/**  */
public class ChangeHistory {
	Vector events;
	
	public ChangeHistory(){
		events = new Vector();
	}
	public ChangeEvent addEvent(Author author, long time, String change){
		ChangeEvent e = null;
		events.addElement(e = new ChangeEvent(author, time, change));
		sortByReverseTime();
		return e;
	}
	
	public void sortByReverseTime(){
		for (int i= 0; i< getNumEvents(); i++){
			ChangeEvent e = getEvent(i);
			for (int j= i-1; j>=0 && getEvent(j).time<getEvent(j+1).time; j--) {
				ChangeEvent tempJ = getEvent(j);
				ChangeEvent tempJ1 = getEvent(j+1);
				tempJ.exchangeData(tempJ1);				
			}
		}
	}
	public void addEvent(ChangeEvent e){
		events.addElement(e);
	}
	public static final int NOHISTORY = 4;
	public static final int NOTCONTAINED = 3;
	public static final int SUBSET = 2;
	public static final int SUPERSET = 1;
	public static final int EQUAL = 0;
	
	public int compareHistories(ChangeHistory other){
		boolean thisIsSubset = firstFoundInSecond(this, other);
		boolean thisIsSuperset = firstFoundInSecond(other, this);
		if (thisIsSubset && thisIsSuperset)
			return EQUAL;
		if (thisIsSubset)
			return SUBSET;
		if (thisIsSuperset)
			return SUPERSET;
		return NOTCONTAINED;
	}
	private boolean firstFoundInSecond(ChangeHistory u, ChangeHistory v){
		for (int i= 0; i< u.getNumEvents(); i++){
			ChangeEvent e = u.getEvent(i);
			if (!matchingEventFound(e, v))
				return false;
		}
		return true;
	}
	private boolean matchingEventFound(ChangeEvent e, ChangeHistory v){
		boolean matchFound = false;
		for (int j= 0; j< v.getNumEvents();j++){
			ChangeEvent otherE = v.getEvent(j);
			if (e.equals(otherE))
				matchFound = true;
		}
		return matchFound;
	}
	public ChangeHistory cloneHistory(){
		ChangeHistory ch = new ChangeHistory();
		for (int i= 0; i< getNumEvents(); i++){
			ChangeEvent ce = getEvent(i);
			ch.addEvent(ce.cloneEvent());
		}
		ch.sortByReverseTime();
		return ch;
	}
	public void incorporate(ChangeHistory v){
		for (int i= 0; i< v.getNumEvents(); i++){
			ChangeEvent ce = v.getEvent(i);
			if (!matchingEventFound(ce, this))
				addEvent(ce.cloneEvent());
		}
		sortByReverseTime();
	}
	public int getNumEvents(){
		return events.size();
	}
	public ChangeEvent getEvent(int i){
		if (i<0 || i>= events.size())
			return null;
		if (events.size()==0)
			return null;
		return (ChangeEvent)events.elementAt(i);
	}
	public long lastModified(){
		if (events.size()==0)
			return 0;
		long max = 0;
		for (int i=events.size()-1; i>=0;i--) {
			ChangeEvent ce = (ChangeEvent)events.elementAt(i);
			if (ce.time > max)
				max = ce.time;
		}
		return max;
	}
	public ChangeEvent getLastEvent(){
		if (events.size()==0)
			return null;
		long max = 0;
		ChangeEvent last = null;
		for (int i=events.size()-1; i>=0;i--) {
			ChangeEvent ce = (ChangeEvent)events.elementAt(i);
			if (ce.time > max) {
				max = ce.time;
				last = ce;
			}
		}
		return last;
	}
	public boolean timeExists(long time){
		if (events.size()==0)
			return false;
		for (int i=events.size()-1; i>=0;i--) {
			ChangeEvent ce = (ChangeEvent)events.elementAt(i);
			if (ce.time == time)
				return true;
		}
		return false;
	}
	public ChangeEvent updateEvent(Author author, long time, String change){
		if (events.size()==0)
			return null;
		for (int i=events.size()-1; i>=0;i--) {
			ChangeEvent ce = (ChangeEvent)events.elementAt(i);
			if (ce.time == time) {
				ce.change = change;
				return ce;
			}
		}
		sortByReverseTime();
		return null;
	}
	public String toString(){
		if (events.size()==0)
			return null;
		StringBuffer sb = new StringBuffer(1000);
		for (int i=0; i<events.size();i++) {
			ChangeEvent ce = (ChangeEvent)events.elementAt(i);
			sb.append("Author: " );
			if (!StringUtil.blank(ce.author.getName()))
				sb.append(ce.author.getName());
			else
				sb.append(ce.author.getCode());
			sb.append( "; Time " + new Date(ce.time) + " Changed to: " + ce.change);
			ChangeAuthority ca = ce.getAuthority();
			if (ca !=null) {
				sb.append("; Authority: ");
				sb.append(ca.toString());
			}
			sb.append('\n');
		}
		return sb.toString();
	}
	public String getNexusString(){
		if (events.size()==0)
			return null;
		StringBuffer sb = new StringBuffer(1000);
		getNexusString(sb);
		return sb.toString();
	}

	public void getNexusString(StringBuffer sb){
		if (events.size()==0 || sb == null)
			return;
		for (int i=0; i<events.size();i++) {
			ChangeEvent ce = (ChangeEvent)events.elementAt(i);
			sb.append("( ");
			sb.append(ce.author.getCode());
			sb.append(" " + ParseUtil.tokenize(NexusBlock.getNEXUSTime(ce.time)) + " " + ParseUtil.tokenize(ce.change));
			ChangeAuthority ca = ce.getAuthority();
			if (ca !=null) {
				sb.append(' ');
				sb.append(ca.getNexusString());
			}
			sb.append(" ) ");
		}
	}
}


