/* Mesquite source code.  Copyright 1997 and onward, W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.coalesce.lib;

import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMessage;


/* ======================================================================== */
/** .*/
public class CoalescedNode {
	public CoalescedNode firstDaughter=null;
	public CoalescedNode nextSister=null;
	public CoalescedNode mother=null;
	public int name=0;
	public int when=0;
	public int location = MesquiteInteger.unassigned;  //can be used to indicate in which population it occurs, for instance
	public boolean open = true;
	
	public boolean isTerminal() {
		return firstDaughter==null;
	}
	public static CoalescedNode findOpen(CoalescedNode[] nodes){
			/*return new CoalescedNode();*/
	
		if (nodes==null || nodes.length==0)
			return null;
		for (int i=0; i<nodes.length; i++)
			if (nodes[i].open){
				nodes[i].open = false;
				return nodes[i];
			}
		return null;
	}
	public void reinitialize() {
		firstDaughter=null;
		nextSister=null;
		mother=null;
		name=0;
		when=0;
		open = true;
		location = MesquiteInteger.unassigned;  
	}
	public int countTerminalTaxa() {
		if (isTerminal())
			return 1;
		else {
			int sum = 0;
			for (CoalescedNode daughter = firstDaughter; daughter !=null; daughter = daughter.nextSister)
				sum += daughter.countTerminalTaxa();
			return sum;
		}
	}
	
	public static CoalescedNode[] findNodes (CoalescedNode[] x) {
		CoalescedNode[] result = new CoalescedNode[countNumber(x)];
		int count = 0;
		for (int i=0; i<x.length; i++)
			if (x[i]!=null)
				result[count++] = x[i];
		return result;
	}
	/*.................................................................................................................*/
	public static int countNumber (CoalescedNode[] x) {
		int num=0;
		for (int i=0; i<x.length; i++)
			if (x[i]!=null)
				num++;
		return num;
	}
	/*.................................................................................................................*/
	public static CoalescedNode[] concatenate(CoalescedNode[] a, CoalescedNode[] b){
		if (a == null)
			return b;
		else if (b == null)
			return a;
		else {
			CoalescedNode[] result = new CoalescedNode[a.length + b.length];
			int i;
			for (i=0; i<a.length; i++)
				result[i]=a[i];
			for (i=0;i<b.length;i++)
				result[i+a.length] = b[i];
			return result;
		}
	}
	/*.................................................................................................................*/
	public static String writeCoalescedNodes(CoalescedNode[] c){
		if (c == null)
			return "null";
		String s = "";
		for (int i = 0; i<c.length; i++) {
			if (c[i] != null)
				s += " " +  c[i].name;
			else
				s += " " +  null;
		}
		return s;
	}
	/*.................................................................................................................*/
	public static CoalescedNode[] cloneNodes(CoalescedNode[] c){
		if (c == null)
			return null;
		CoalescedNode[] result = new CoalescedNode[c.length];
		for (int i = 0; i<result.length; i++) {
			result[i]=c[i];
		}
		return result;
	}
	/*.................................................................................................................*/
	public static CoalescedNode findNode (CoalescedNode[] x) {
		for (int i=0; i<x.length; i++)
			if (x[i]!=null)
				return x[i];
		return null;
	}
	/*.................................................................................................................*/
	public String cladeToString() {
		if (isTerminal()){  //terminal; return just  name
			String n = String.valueOf(name);
			String loc = "";
			if (MesquiteInteger.isCombinable(location))
				loc = "<location =" + location + ">";
			if (mother==null) {
				MesquiteMessage.warnProgrammer("Warning: mother null in terminal cladeToString in CoalescedNode");
				return n + loc;//TODO: branch lengths all wrong
			}
			else 
				return n + ":" + (mother.when-when) + " " + loc;//TODO: branch lengths all wrong
				
		}
		else {  //internal; return parentheses and daughter clades
			String s = "(";
			boolean first = true;
			for (CoalescedNode daughter = firstDaughter; daughter !=null; daughter = daughter.nextSister) {
				if (!first)
					s += ",";
				first = false;
				s += daughter.cladeToString();
			}
			s += ")";
			if (mother!=null)
				s += ":" + (mother.when-when); //to show branch lengths
			if (MesquiteInteger.isCombinable(location))
				s += "<location =" + location + ">";
			return s;
		}
	}
}





