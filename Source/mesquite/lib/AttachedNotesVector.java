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

/* ======================================================================== */
public class AttachedNotesVector {
	private Vector notes;
	int currentNote = 0;
	Listened owner;
	
	public AttachedNotesVector(Listened owner){
		this.owner = owner;
		notes = new Vector();
	}
	public void notifyOwner(int event){
		if (owner !=null)
			owner.notifyListeners(this, new Notification(event));
	}
	
	public static final int NOTCONTAINED = 3;
	public static final int SUBSET = 2;
	public static final int SUPERSET = 1;
	public static final int EQUAL = 0;
	public static final int EQUALBUTMODIFIED = 4;
	
	public int compareNotesByDescent(AttachedNotesVector other){
		boolean thisIsSubset = firstFoundInSecond(this, other);
		boolean thisIsSuperset = firstFoundInSecond(other, this);
		if (thisIsSubset && thisIsSuperset) {  //same annotation IDs
			if (firstFoundInSecond(this, other, false, false) &&  firstFoundInSecond(other, this, false, false))
				return EQUAL;
			else
				return EQUALBUTMODIFIED;
		}
		if (thisIsSubset)
			return SUBSET;
		if (thisIsSuperset)
			return SUPERSET;
		return NOTCONTAINED;
	}
	private boolean firstFoundInSecond(AttachedNotesVector u, AttachedNotesVector v){
		if (u == null)
			return false;
		for (int i= 0; i< u.getNumNotes(); i++){
			AttachedNote note = u.getAttachedNote(i);
			if (sameNoteFound(note, v) == null)
				return false;
		}
		return true;
	}
	private AttachedNote sameNoteFound(AttachedNote e, AttachedNotesVector v){
		if (v == null)
			return null;
		for (int j= 0; j< v.getNumNotes();j++){
			AttachedNote otherE = v.getAttachedNote(j);
			if (e.descendant(otherE))
				return otherE;
		}
		return null;
	}
	public int compareNotesByContent(AttachedNotesVector other, boolean excludeDateCreated, boolean excludeDateModified){
		boolean thisIsSubset = firstFoundInSecond(this, other, excludeDateCreated, excludeDateModified);
		boolean thisIsSuperset = firstFoundInSecond(other, this, excludeDateCreated, excludeDateModified);
		if (thisIsSubset && thisIsSuperset)
			return EQUAL;
		if (thisIsSubset)
			return SUBSET;
		if (thisIsSuperset)
			return SUPERSET;
		return NOTCONTAINED;
	}
	private boolean firstFoundInSecond(AttachedNotesVector u, AttachedNotesVector v, boolean excludeDateCreated, boolean excludeDateModified){
		if (u == null)
			return false;
		for (int i= 0; i< u.getNumNotes(); i++){
			AttachedNote note = u.getAttachedNote(i);
			if (!matchingNoteFound(note, v, excludeDateCreated, excludeDateModified))
				return false;
		}
		return true;
	}
	private boolean matchingNoteFound(AttachedNote e, AttachedNotesVector v, boolean excludeDateCreated, boolean excludeDateModified){
		if (v == null)
			return false;
		for (int j= 0; j< v.getNumNotes();j++){
			AttachedNote otherE = v.getAttachedNote(j);
			if (e.sameContents(otherE, excludeDateCreated, excludeDateModified))
				return true;
		}
		return false;
	}
	public AttachedNotesVector cloneVector(){
		return cloneVector(owner);
	}
	public AttachedNotesVector cloneVector(Listened owner){
		AttachedNotesVector ch = new AttachedNotesVector(owner);
		for (int i= 0; i< getNumNotes(); i++){
			AttachedNote ce = getAttachedNote(i);
			ch.addNote(ce.cloneNote(), false);
		}
		return ch;
	}
	public void concatenate(AttachedNotesVector ch){
		if (ch == null)
			return;
		for (int i= 0; i< ch.getNumNotes(); i++){
			AttachedNote ce = ch.getAttachedNote(i);
			addNote(ce.cloneNote(), false);
		}
	}
	public void incorporateByContent(AttachedNotesVector v){
		if (v == null)
			return;
		for (int i= 0; i< v.getNumNotes(); i++){
			AttachedNote ce = v.getAttachedNote(i);
			if (!matchingNoteFound(ce, this, true, true))
				addNote(ce.cloneNote(), false);
		}
	}
	public void incorporateByDescent(AttachedNotesVector v, boolean useLastModified){
		if (v == null)
			return;
		for (int i= 0; i< v.getNumNotes(); i++){
			AttachedNote ce = v.getAttachedNote(i);
			AttachedNote foundHere = sameNoteFound(ce, this);
			if (foundHere==null)
				addNote(ce.cloneNote(), false);
			else if (!foundHere.equals(ce)){
				if (useLastModified){
					if (ce.getDateModifiedInMillis() > foundHere.getDateModifiedInMillis()) {
						foundHere.setToClone(ce);
					}
				}
				else
					foundHere.setToClone(ce);
			}
			
		}
	}
	public void describeDifferencesByDescent(AttachedNotesVector v, boolean useLastModified, StringBuffer sb){
		if (sb == null || v == null)
			return;
		for (int i= 0; i< v.getNumNotes(); i++){
			AttachedNote ce = v.getAttachedNote(i);
			AttachedNote foundHere = sameNoteFound(ce, this);
			if (foundHere==null)
				sb.append("Note not present: " + ce);
			else if (!foundHere.equals(ce)){
				foundHere.describeDifferences(ce, sb);
			}
			
		}
	}
	public int addNote(AttachedNote s, boolean notify){
		s.setVector(this);
		notes.addElement(s);
		if (notify) notifyOwner(MesquiteListener.ANNOTATION_ADDED);
		return notes.size()-1;
	}
	public int addNoteFirst(AttachedNote s, boolean notify){
		s.setVector(this);
		notes.insertElementAt(s, 0);
		if (notify) notifyOwner(MesquiteListener.ANNOTATION_ADDED);
		return notes.size()-1;
	}
	public AttachedNote getAttachedNote(int i){
		if (i>=0 && i< notes.size())
			return (AttachedNote)notes.elementAt(i);
		return null;
	}
	public int getNumNotes(){
		return notes.size();
	}
	public void deleteNote(int i){
		if (i>=0 && i< notes.size()) {
			notes.removeElementAt(i);
			if (currentNote>= i)
				currentNote--;
			if (currentNote>= notes.size())
				currentNote = notes.size()-1;
			if (currentNote<0)
				currentNote = 0;
			notifyOwner(MesquiteListener.ANNOTATION_DELETED);
		}
	}
	public void deleteAllNotes(){
			notes.removeAllElements();
			notifyOwner(MesquiteListener.ANNOTATION_DELETED);
	}
	/*
	public int getCurrent(){
		return currentNote;
	}
	public void setCurrent(int i){
		if (!MesquiteInteger.isCombinable(i))
			return;
		currentNote = i;
		if (currentNote>= notes.size())
			currentNote = notes.size()-1;
		if (currentNote<0)
			currentNote = 0;
	}
	public AttachedNote getCurrentAttachedNote(){
		if (currentNote < 0 || currentNote>=notes.size())
			return null;
		AttachedNote aim = (AttachedNote)notes.elementAt(currentNote);
		return aim;
	}
	public void setCurrentComment(String s){
		if (currentNote < 0 || currentNote>=notes.size())
			return;
		AttachedNote aim = (AttachedNote)notes.elementAt(currentNote);
		aim.setComment(s);
	}
	public void setCurrentAuthor(String s){
		if (currentNote < 0 || currentNote>=notes.size())
			return;
		AttachedNote aim = (AttachedNote)notes.elementAt(currentNote);
		aim.setAuthor(s);
	}
	*/
	public String getImagePath(int i){
		if (i < 0 || i>=notes.size())
			return null;
		AttachedNote aim = (AttachedNote)notes.elementAt(i);
		
		return aim.getImagePath();
	}
	public Image getImage(int i){
		if (i < 0 || i>=notes.size())
			return null;
		AttachedNote aim = (AttachedNote)notes.elementAt(i);
		return aim.getImage();
	}
	public String getComment(int i){
		if (i < 0 || i>=notes.size())
			return null;
		AttachedNote aim = (AttachedNote)notes.elementAt(i);
		return aim.getComment();
	}
	public String getReference(int i){
		if (i < 0 || i>=notes.size())
			return null;
		AttachedNote aim = (AttachedNote)notes.elementAt(i);
		return aim.getReference();
	}
	
	public String toString(){
		String s = "";
		for (int i=0; i<notes.size(); i++){
			AttachedNote aim = (AttachedNote)notes.elementAt(i);
			s += "  " + aim.getComment() + " " + aim.getDateModified();
		}
		return s;
	}
	
}
	


