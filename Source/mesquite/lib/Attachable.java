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

import mesquite.lib.ui.HTMLDescribable;

/*==========================  Mesquite Basic Class Library    ==========================*/
/*.................................................................................................................*/
/** A class to which objects can be attached.  May 2002*/
public abstract class Attachable extends Listened implements HTMLDescribable { 
	Vector attachments;
	//this should separately accept longs, doubles, & objects (e.g. strings) and be made more parallel to the associable system

	public Attachable (){
		super();
	}
	/** Disposes this attachable */
	public void dispose(){
		if (attachments!=null)
			attachments.removeAllElements();
		super.dispose();
	}

	public String toHTMLStringDescription(){
		if (!anyAttachments())
			return "";
		String s = "<li>Attachments: <ul>";
		if (attachments!=null){
			for (int i=0; i<attachments.size(); i++) {
				s += "<li>";
				Object obj = attachments.elementAt(i);
				if (obj instanceof Listable){
					if (obj instanceof MesquiteBoolean)
						s += "Boolean: ";
					else if (obj instanceof MesquiteLong)
						s += "Long: ";
					else if (obj instanceof MesquiteDouble)
						s += "Double: ";
					else if (obj instanceof MesquiteString)
						s += "String: ";
					Listable b = (Listable)obj;
					s += "   " + b.getName();	
				}
				else
					s += "Object of class " + obj.getClass().getName();	
				s += "</li>";
			}
		}
		s += "</ul></li>";
		return s;
	}
	public void attach(Object obj){
		if (obj == null)
			return;
		if (attachments==null)
			attachments = new Vector();
		attachments.addElement(obj);
	}
	public void attachIfUniqueName(Object obj){
		if (obj == null)
			return;
		if (attachments==null)
			attachments = new Vector();
		if (ListableVector.indexOfByName(attachments, obj)>=0)
			return;
		attachments.addElement(obj);
	}
	public void detach(Object obj){
		if (obj == null)
			return;
		if (attachments==null)
			return;
		attachments.removeElement(obj);
	}
	public void detachObjectOfName(String name){
		if (name == null || attachments==null)
			return;
		int index = ListableVector.indexOfByName(attachments, name);
		if (index <0)
			return;
		attachments.removeElementAt(index);
	}
	public void detachAllObjectsOfName(String name){
		if (name == null || attachments==null)
			return;
		int index=0;
		while (index>=0) {
			index = ListableVector.indexOfByName(attachments, name);
			if (index <0)
				return;
			attachments.removeElementAt(index);
		}
	}
	public void setAttachments(Attachable at){
		Vector atat = at.attachments;

		if (attachments !=null)
			attachments.removeAllElements();
		if (atat == null)
			return;
		for (int i=0; i<atat.size(); i++) {
			Object obj = atat.elementAt(i);
			if (obj instanceof MesquiteDouble){ //todo: add attachments of other sorts also
				MesquiteDouble b = (MesquiteDouble)obj;
				MesquiteDouble md = new MesquiteDouble(b.getValue());
				md.setName(b.getName());
				attach(md);
			}
			else if (obj instanceof MesquiteLong){ //todo: add attachments of other sorts also
				MesquiteLong b = (MesquiteLong)obj;
				MesquiteLong md = new MesquiteLong(b.getValue());
				md.setName(b.getName());
				attach(md);
			}
			else if (obj instanceof MesquiteString){ //todo: add attachments of other sorts also
				MesquiteString b = (MesquiteString)obj;
				MesquiteString md = new MesquiteString(b.getValue());
				md.setName(b.getName());
				attach(md);
			}
			else {
				MesquiteMessage.warnProgrammer("ERROR: setAttachments encountered attachment of unknown type.  This is not handled yet. " + obj.getClass() + "  " + obj);
			}
		}
	}
	public boolean anyAttachments(){
		return attachments != null && attachments.size()>0;
	}
	public Vector getAttachments(){
		return attachments;
	}
	public Object getAttachment(String name){
		return getAttachment(name, null);
	}
	public Object getAttachment(String name, Class c){
		if (attachments==null || name == null)
			return null;
		for (int i=0; i<attachments.size(); i++) {
			Object obj = attachments.elementAt(i);
			if (obj instanceof Listable && (c == null || c.isAssignableFrom(obj.getClass()))){
				Listable b = (Listable)obj;
				if (name.equalsIgnoreCase(b.getName()))
					return b;	
			}
		}
		return null;
	}
	public String listAttachments(){
		String s = " Attachments of ";
		if (this instanceof Listable)
			s =  ((Listable)this).getName() + "\n";
		else
			s = getClass() + "\n";
		if (attachments!=null){
			for (int i=0; i<attachments.size(); i++) {
				Object obj = attachments.elementAt(i);
				if (obj instanceof Listable){
					Listable b = (Listable)obj;
					s += "   " + b.getName()+ "\n";	
				}
				else
					s += "   Object of class " + obj.getClass().getName()+ "\n";	
			}
		}
		return s;
	}
	public String writeAttachments(){
		String s = "";
		boolean first = true;
		if (attachments!=null)
			for (int i=0; i<attachments.size(); i++) {
				Object obj = attachments.elementAt(i);
				if (obj instanceof Listable && ((Listable)obj).getName()!=null){
					if (obj instanceof MesquiteDouble){
						if (!first)
							s += ", ";
						first = false;
						s += ParseUtil.tokenize(((Listable)obj).getName()) + " = " + obj + " ";
					}
					else if (obj instanceof MesquiteLong){
						if (!first)
							s += ", ";
						first = false;
						s += ParseUtil.tokenize(((Listable)obj).getName()) + " = " + obj + " ";
					}
					else if (obj instanceof MesquiteBoolean){
						if (!first)
							s += ", ";
						first = false;
						s += ParseUtil.tokenize(((MesquiteBoolean)obj).getName()) + " = " + ((MesquiteBoolean)obj).toOffOnString() + " ";
					}
					else if (obj instanceof MesquiteString){
						if (!first)
							s += ", ";
						first = false;
						s += ParseUtil.tokenize(((MesquiteString)obj).getName()) + " = " + ParseUtil.tokenize(((MesquiteString)obj).getValue())+ " ";
					}
				}
			}
		if (StringUtil.blank(s))
			return null;
		return s;
	}
	/** Reading and writing of attachments is not fully supported.  Below are the methods for comment-saving, e.g. for trees.
	 * Other classes such as CharacterData may have their managing modules store the attachments as supplemental notes (see SUCM
	 * in ManageCharacters).
	 */
	boolean reportReading = false;
	public boolean readAttachment(String assocString, MesquiteInteger pos){
		//assumes already past "<", and we're about to read a key
		String key=ParseUtil.getToken(assocString, pos);
		if (StringUtil.blank(key))
			return false;
		int posBeforeEquals = pos.getValue();
		String tok = ParseUtil.getToken(assocString, pos); //eating up equals
		int posBeforeValue = pos.getValue();
		String value = ParseUtil.getToken(assocString, pos); //finding value
		if (StringUtil.blank(value))
			return false;
		if (reportReading) Debugg.println("!!!~~KEY " + key + " VALUE " + value + " pos " + pos.getValue());
		if (value.equalsIgnoreCase("on")) {
			MesquiteBoolean mb = new MesquiteBoolean(true);
			mb.setName(key);
			attachIfUniqueName(mb);
		}
		else if (value.equalsIgnoreCase("off")) {
			MesquiteBoolean mb = new MesquiteBoolean(false);
			mb.setName(key);
			attachIfUniqueName(mb);
		}
		else if (value.indexOf("string:") == 0) { //treat as String 
			MesquiteString mb = new MesquiteString();
			mb.setValue(value.substring(7, value.length()));
			mb.setName(key);
			attachIfUniqueName(mb);
		}
		else {
			
			pos.setValue(posBeforeValue);
			double d = MesquiteDouble.fromString(assocString, pos);
			if (reportReading) Debugg.println("  d " +  MesquiteDouble.toString(d) + " pos " + pos.getValue());

			if (!MesquiteDouble.isCombinable(d)){ //not a number; treat as string
				if (reportReading) Debugg.println("     -> string ");
				pos.setValue(posBeforeValue);
				ParseUtil.getToken(assocString, pos); //eating up value again
				MesquiteString mb = new MesquiteString();
				mb.setValue(value);
				mb.setName(key);
				attachIfUniqueName(mb);
			}
			else {
				if (value.indexOf(".")>=0) { //treat as double 
					//TODO:  there is a problem here; if some cases use ., others not, should be double; but will be treated as mixed
					if (reportReading) Debugg.println("     -> double ");
					MesquiteDouble mb = new MesquiteDouble();
					pos.setValue(posBeforeValue);
					mb.setValue(MesquiteDouble.fromString(assocString, pos));
					mb.setName(key);
					attachIfUniqueName(mb);
				}
				else {  //treat as long
					if (reportReading) Debugg.println("     -> long ");
					MesquiteLong mb = new MesquiteLong();
					pos.setValue(posBeforeValue);
					mb.setValue(MesquiteLong.fromString(assocString, pos));
				
					if (!mb.isCombinable())
						return false;
					mb.setName(key);
					attachIfUniqueName(mb);
				}
			}
		}
		return true;
	}

	public void readAttachments(String assocString, MesquiteInteger pos){
		//assumes already past "<";
		String s="<"; //just so it enters the loop
		while (!">".equals(s) && !"]".equals(s) && !";".equals(s) && StringUtil.notEmpty(s)) {
			int oldPos = pos.getValue();
			boolean success = readAttachment(assocString, pos);
			s=ParseUtil.getToken(assocString, pos);  //eating up "," separating subcommands, or hit the end
			if (reportReading) Debugg.println("  s " +  s + " pos " + pos.getValue());
		}
		if (StringUtil.blank(s))
			pos.setValue(assocString.length()-1);
		else if (s.equals(";"))
			pos.decrement();
		else if (!s.equals(">"))
			ParseUtil.getToken(assocString, pos); //eating up >
	}



}




