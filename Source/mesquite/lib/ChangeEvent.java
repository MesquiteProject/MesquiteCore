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
import java.util.*;



public class ChangeEvent {
	Author author;
	long time;
	String change;
	ChangeAuthority authority;
	
	public ChangeEvent(Author author, long time, String change){
		this.author = author;
		this.time = time;
		this.change = change;
	}
	public ChangeEvent cloneEvent(){
		ChangeEvent ce = new ChangeEvent(author, time, new String(change));
		ce.authority = authority;
		return ce;
	}
	public void exchangeData(ChangeEvent other){
		Author tempA = author;
		author = other.author;
		other.author = tempA;
		long tempT = time;
		time = other.time;
		other.time = tempT;
		String tempC = change;
		change = other.change;
		other.change = tempC;
		ChangeAuthority tempU = authority;
		authority = other.authority;
		other.authority = tempU;
	}
	public boolean equals(ChangeEvent other){
		return Author.authorsEqual(author, other.author) && time == other.time && StringUtil.stringsEqual(change, other.change);
	}
	public void setAuthority(ChangeAuthority authority){
		this.authority = authority;
	}
	public ChangeAuthority getAuthority(){
		return authority;
	}
	
	public String getChange(){
		return change;
	}
	public Author getAuthor(){
		return author;
	}
	public long getTime(){
		return time;
	}
	public String toString(){
		StringBuffer sb = new StringBuffer(1000);
		sb.append( "Changed to: " + change);
		sb.append(" by author: " );
		if (!StringUtil.blank(author.getName()))
			sb.append(author.getName());
		else
			sb.append(author.getCode());
		sb.append( " at time " + new Date(time));
		return sb.toString();
	}
}

