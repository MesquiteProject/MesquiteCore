/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib.tree;

import java.awt.*;

import java.math.*;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.Element;

import mesquite.lib.*;

/* ��������������������������� tree stuff ������������������������������� */
/* ======================================================================== */
/** A record of the rules for translating a Newick string of a different dialect to Mesquite's standard */
public class NewickDialect implements Listable {
	String humanName;
	String internalName;
	String punctuation;
	String whitespace;

	/*String[2] arrays with replacements to be made using String.replace. 
	NOTE: Mesquite will have separated NEXUS punctuation via spaces, e.g. 
	[@aa/bb/cc= zyx] will have become [@ aa / bb / cc = zyx], 
	so a common replacement is to get rid of the spaces and add single quotes to tokenize */
	Vector replacements = new Vector(); 

	/*String[3] arrays with [0] name of property, [1] what datatype it's supposed to be (double, long, string) 
	 * and [2] whether it is to be attached to branch or node (branch, node). 
	 * The datatype might be ignored except for double.*/
	Vector properties = new Vector();

	public NewickDialect (){
	}
	public NewickDialect (String dialectFilePath){
		processDialectFile(dialectFilePath);
	}

	public boolean isReady(){
		return humanName != null && internalName != null;
	}
	public String getName(){
		return internalName;
	}
	public void setName(String internalName){
		this.internalName = internalName;
	}

	public String getHumanName(){
		return humanName;
	}
	public void setHumanName(String humanName){
		this.humanName = humanName;
	}
	public String getPunctuation(){
		return punctuation;
	}
	public void setPunctuation(String punctuation){
		this.punctuation = punctuation;
	}
	public String getWhitespace(){
		return whitespace;
	}
	public void setWhitespace(String whitespace){
		this.whitespace = punctuation;
	}
	public String translate(String treeDescription){
		if (treeDescription == null)
			return null;
		for (int i = 0; i<replacements.size(); i++){
			String[] r = (String[])replacements.elementAt(i);
			if ("all".equalsIgnoreCase(r[2]))
				treeDescription = treeDescription.replace(r[0], r[1]);
			else if ("first".equalsIgnoreCase(r[2])){
				int first = treeDescription.indexOf(r[0]);
				treeDescription = treeDescription.substring(0,first)+r[1]+treeDescription.substring(first);
			}
			else if ("last".equalsIgnoreCase(r[2])){
				int last = treeDescription.lastIndexOf(r[0]);
				treeDescription = treeDescription.substring(0,last)+r[1]+treeDescription.substring(last);
			}
		}
		return treeDescription;
	}

	public void addReplacement(String[] repl){
		if (repl != null && repl.length >= 3)
			replacements.addElement(repl);
	}
	public Vector getReplacementsVector(){
		return replacements;
	}

	public void addProperty(String[] p){
		if (p != null && p.length >=3)
			properties.addElement(p);
	}

	// returns whether the dialect knows the property
	public boolean isPropertyKnown(String name){
		if (name == null)
			return false;
		for (int i = 0; i<properties.size(); i++){
			String[] prop = (String[])properties.elementAt(i);
			if (name.equalsIgnoreCase(prop[0]))
				return true;
		}
		return false;
	}
	// returns double, long, String, doublearray, stringarray, object
	public String getPropertyKind(String name){
		if (name == null)
			return null;
		for (int i = 0; i<properties.size(); i++){
			String[] prop = (String[])properties.elementAt(i);
			if (name.equalsIgnoreCase(prop[0]))
				return prop[1];
		}
		return null;
	}
	// 0 not found; 1 branch; 2 node
	public int getPropertyWhere(String name){
		if (name == null)
			return 0;
		for (int i = 0; i<properties.size(); i++){
			String[] prop = (String[])properties.elementAt(i);
			if (name.equalsIgnoreCase(prop[0])){
				if ("branch".equalsIgnoreCase(prop[2]))
					return 1;
				else if ("node".equalsIgnoreCase(prop[2]))
					return 2;
				else 
					return 3; //what is default???
			}
		}
		return 0;
	}
	public boolean processDialectFile(String dialectFilePath){
		String s = MesquiteFile.getFileContentsAsString(dialectFilePath); 
		Document doc = XMLUtil.getDocumentFromString(s);
		if (doc==null) {
			if (StringUtil.blank(s))
				MesquiteMessage.println("WARNING: dialect xml file is empty, at " + dialectFilePath);
			else 
				MesquiteMessage.println("WARNING: dialect xml file is improperly formatted, at " + dialectFilePath);
			return false;
		}
		Element rootElement = doc.getRootElement();
		List element = rootElement.elements();
		for (Iterator iter = element.iterator(); iter.hasNext();) {   // this is going through all of the notices
			Element item = (Element) iter.next();
			if ("humanName".equalsIgnoreCase(item.getName()))
				humanName = item.getText();
			else if ("internalName".equalsIgnoreCase(item.getName()))
				internalName = item.getText();
			else if ("whitespace".equalsIgnoreCase(item.getName())){
				whitespace = item.getText();
			}
			else if ("punctuation".equalsIgnoreCase(item.getName()))
				punctuation = item.getText();
			else if ("replacement".equalsIgnoreCase(item.getName())){
				List parts = item.elements();
				String before = null;
				String after = null;
				String where = "all";
				for (Iterator iterP = parts.iterator(); iterP.hasNext();) {
					Element part = (Element) iterP.next();

					if ("before".equalsIgnoreCase(part.getName()))
						before = part.getText();
					else if ("after".equalsIgnoreCase(part.getName()))
						after = part.getText();
					else if ("where".equalsIgnoreCase(part.getName()))
						where = part.getText();
				}
				if (before != null && after != null)
					replacements.addElement(new String[]{before, after,where});
			}
			else if ("property".equalsIgnoreCase(item.getName())){
				List parts = item.elements();
				String name = null;
				String kind = null;
				String where = null;
				for (Iterator iterP = parts.iterator(); iterP.hasNext();) {
					Element part = (Element) iterP.next();

					if ("name".equalsIgnoreCase(part.getName()))
						name = part.getText();
					else if ("kind".equalsIgnoreCase(part.getName()))
						kind = part.getText();
					else if ("where".equalsIgnoreCase(part.getName()))
						where = part.getText();
				}
				if (name != null && (kind != null || where != null))
					properties.addElement(new String[]{name, kind, where});
			}

		}
		return true;
	}
	
	public String toString(){
		String s = "Dialect " + internalName + " for " + humanName + '\n';
		
		if (punctuation == null)
			s += "\tpunctuation null\n";
		else
		s += "\tpunctuation " + punctuation + '\n';
		if (whitespace == null)
			s += "\twhitespace null\n";
		else
			s += "\twhitespace \"" + whitespace + "\"\n";
		if (replacements.size()>0){
			s += "\treplacements"  + '\n';
			for (int i = 0; i<replacements.size(); i++){
				String[] r = (String[])replacements.elementAt(i);
				s += "\t\t" + r[2] + " \"" + r[0] + "\" to \"" +  r[1] + "\"\n";
			}
		}
		if (properties.size()>0){
			s += "\tproperties"  + '\n';
			for (int i = 0; i<properties.size(); i++){
				String[] r = (String[])properties.elementAt(i);
				s += "\t\t" + r[0] + " " + r[1] + " " +  r[2] + "\n";
			}
		}
		return s;
	}
}



