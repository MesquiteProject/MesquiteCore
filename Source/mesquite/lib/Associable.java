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

import java.awt.Color;
import java.util.Vector;

import mesquite.lib.tree.DisplayableBranchProperty;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.ui.ColorDistribution;

/*.................................................................................................................*/
/** A class that contains serially repeated parts, to each of which may be attached information.
For example, a Tree contains many nodes, and information might be attached to each node.  A Taxa contains taxons,
a CharacterData contains characters, and so on.  The purpose of this class is to coordinate
this attached ("associated") information.  Subclasses include FileElement (and thus CharacterData
and Taxa) and Tree.  */
public abstract class Associable extends Attachable implements Commandable, Annotatable, Selectionable {
	public static final int BUILTIN = 0;
	public static final int BITS = 1;
	public static final int DOUBLES = 2;
	public static final int LONGS = 3;
	public static final int STRINGS = 4;
	public static final int OBJECTS = 5;

	protected Vector bits;
	protected Vector longs;
	protected Vector doubles;
	protected Vector strings;
	protected Vector objects;
	int[] defaultOrder;
	int[] currentOrder;
	int[] previousOrder;
	Bits justAdded;
	protected int numParts;
	boolean dirty = false;
	protected boolean rememberDefaultOrder = false;
	public static int totalCreatedA=0;
	public static int totalDisposedA=0;
	public static int totalFinalizedA=0;


	/** A number recording the version number.  With every change of the object, the version number is incremented. Used now for trees and taxa.*/
	protected long versionNumber = 0;

	protected String comment = null;

	/** The bit field indicating which parts are selected currently.  This is instantiated only if needed.*/
	protected Bits selected;

	/** The object array storing comments (e.g. footnotes) for each of the parts.  This is instantiated only if needed.*/
	protected ObjectArray comments;

	public Associable (int numParts){
		super();
		totalCreatedA++;
		this.numParts = numParts;
		bits = new Vector();
		longs = new Vector();
		doubles = new Vector();
		strings = new Vector();
		objects = new Vector();
	}
	public Associable (){
		super();
		totalCreatedA++;
		//TODO: if comes this way, need to make vectors later when needed?
	}
	public void finalize() throws Throwable {
		totalFinalizedA++;
		super.finalize();
	}
	/** Disposes this associable */
	public void dispose(){
		totalDisposedA++;
		if (bits!=null)
			bits.removeAllElements();
		if (longs!=null)
			longs.removeAllElements();
		if (doubles!=null)
			doubles.removeAllElements();
		if (objects!=null)
			objects.removeAllElements();
		if (strings!=null)
			strings.removeAllElements();
		bits = null;
		longs = null;
		doubles = null;
		objects = null;
		strings = null;
		defaultOrder = null;
		numParts = 0;
		super.dispose();
	}
	public String toHTMLStringDescription(){
		String sT = super.toHTMLStringDescription();
		if (bits == null && longs == null && doubles == null && objects == null && strings == null)
			return sT;
		String s = "";
		if (bits!=null) {
			for (int i=0; i<bits.size(); i++) {
				Listable b = (Listable)bits.elementAt(i);
				s += "<li>Bit array: " + b.getName()+ "</li>";	
			}
		}
		if (longs!=null) {
			for (int i=0; i<longs.size(); i++) {
				Object obj = longs.elementAt(i);
				Listable b = (Listable)longs.elementAt(i);
				s += "<li>Long array: " + b.getName()+  "</li>";	
			}
		}
		if (doubles!=null){
			for (int i=0; i<doubles.size(); i++) {
				Listable b = (Listable)doubles.elementAt(i);
				s += "<li>Double array: " + b.getName()+  "</li>";	
			}
		}
		if (strings!=null){
			for (int i=0; i<strings.size(); i++) {
				Listable b = (Listable)strings.elementAt(i);
				s += "<li>String array: " + b.getName()+  "</li>";	
			}
		}
		if (objects!=null) {
			for (int i=0; i<objects.size(); i++) {
				Object obj = objects.elementAt(i);
				if (obj instanceof Listable){
					Listable b = (Listable)obj;
					s += "<li>Object: " + b.getName()+ "</li>";	
				}
				else if (obj instanceof String){
					s += "<li>String:  " + obj+ " </li>";	
				}
				else if (obj instanceof StringArray){
					s += "<li>StringArray as ObjectArray: </li>";	
				}
				else if (obj instanceof String[]){
					s += "<li>String[]: </li>";	
				}
				else
					s += "<li>Object of class " + obj.getClass().getName()+ "</li>";	
			}
		}
		if (StringUtil.blank(s))
			return sT;
		sT += "<li>Associated: <ul>" + s + "</ul></li>";
		return sT;
	}

public ListableVector getAssociatesOfKind(int kind){
	ListableVector v = new ListableVector();
	if (kind == Associable.BITS){
		if (bits!=null) {
			for (int i=0; i<bits.size(); i++) {
				v.addElement((Listable)bits.elementAt(i), false);
			}
		}
	}
	else if (kind == Associable.LONGS){
		if (longs!=null) {
			for (int i=0; i<longs.size(); i++) {
				Object obj = longs.elementAt(i);
				Listable b = (Listable)longs.elementAt(i);
				v.addElement(b, false);
			}
		}
	}
	else if (kind == Associable.DOUBLES){
		if (doubles!=null) {
			for (int i=0; i<doubles.size(); i++) {
				Object obj = doubles.elementAt(i);
				Listable b = (Listable)doubles.elementAt(i);
				v.addElement(b, false);
			}
		}
	}
	else if (kind == Associable.STRINGS){
		if (strings!=null) {
			for (int i=0; i<strings.size(); i++) {
				Object obj = strings.elementAt(i);
				Listable b = (Listable)strings.elementAt(i);
				v.addElement(b, false);
			}
		}
	}
	else if (kind == Associable.OBJECTS){
		if (objects!=null) {
			for (int i=0; i<objects.size(); i++) {
				Object obj = objects.elementAt(i);
				Listable b = (Listable)objects.elementAt(i);
				v.addElement(b, false);
			}
		}
	}
	return v;
}

	public String getTextVersionAssociates(String nameOfPart){
		if (bits == null && longs == null && doubles == null && objects == null && strings == null)
			return "";
		String s = "";
		for (int i=0; i<numParts; i++){
			s += nameOfPart + " " + (i+1) + ": " + toString(i) + "\n";
		}
		return s;
	}

	public String toString(int part){
		String s = "";
		String add = "";
		if (bits == null && longs == null && doubles == null && objects == null && strings == null)
			return s;
		if (bits!=null) {
			for (int i=0; i<bits.size(); i++) {
				Bits b = (Bits)bits.elementAt(i);
				s += add + "Bit \"" + b.getName()+ "\": ";	
				if (((Bits)b).isBitOn(part))
					s += " ON" ;
				else
					s += " OFF";
				add = "; ";
			}
		}
		if (longs!=null) {
			for (int i=0; i<longs.size(); i++) {
				Object obj = longs.elementAt(i);
				LongArray b = (LongArray)longs.elementAt(i);
				s += add + "" + b.getName()+ ": " + MesquiteLong.toString(b.getValue(part));	
				add = "; ";
			}
		}
		if (doubles!=null){
			for (int i=0; i<doubles.size(); i++) {
				DoubleArray b = (DoubleArray)doubles.elementAt(i);
				s += add + "" + b.getName()+ ": " + MesquiteDouble.toString(b.getValue(part));	
				add = "; ";
			}
		}
		if (strings!=null){
			for (int i=0; i<strings.size(); i++) {
				StringArray b = (StringArray)strings.elementAt(i);
				s += add + "" + b.getName()+ ": " + b.getValue(part);	
				add = "; ";
			}
		}
		if (objects!=null) {
			for (int i=0; i<objects.size(); i++) {
				boolean changeAdd = true;
				Object obja = objects.elementAt(i);
				if (obja instanceof ObjectArray){
					Object obj = ((ObjectArray)obja).getValue(part);
					if (obj instanceof MesquiteString){
						MesquiteString b = (MesquiteString)obj;
						s += add + "" + b.getName()+ ": " + b.getValue();	
					}
					else if (obj instanceof Listable){
						Listable b = (Listable)obj;
						s += add + "Object \"" + b.getName()+ "\"" + "  " + obj.getClass().getName();	
					}
					else if (obj instanceof String){
						s += add + ((ObjectArray)obja).getName() + ": " + obj;	
					}
					else if (obj != null)
						s += add +  "Object of class " + obj.getClass().getName();	
					else
						changeAdd = false;
				}
				else if (obja instanceof MesquiteString){
					MesquiteString b = (MesquiteString)obja;
					s += add + "" + b.getName()+ ": " + b.getValue();	
				}
				else if (obja instanceof Listable){
					Listable b = (Listable)obja;
					s += add + "Object \"" + b.getName()+ "\"" + "  " + obja.getClass().getName();	
				}
				else if (obja instanceof String){
					s += add + "String: " + obja + add;	
				}
				else 
					s += add + "Object of class " + obja.getClass().getName();	
				if (changeAdd)
					add = "; ";
			}
		}
		return s;
	}
	
	//makes the associated info of a part of this equal to that of otherPart of other associable
	public void equalizeParts(Associable other, int otherPart, int part){
		if (other==null || part >= getNumberOfParts() || otherPart >= other.getNumberOfParts())
			return;
		if (other.bits!=null)
			for (int i=0; i<other.bits.size(); i++) {
				Bits b1 = (Bits)other.bits.elementAt(i);
				NameReference nr = makeAssociatedBits(b1.getNameReference().getValue());
				Bits b = getAssociatedBits(nr);
				b.setBit(part, b1.isBitOn(otherPart));
			}
		if (other.longs!=null)
			for (int i=0; i<other.longs.size(); i++) {
				LongArray b1 = (LongArray)other.longs.elementAt(i);
				NameReference nr = makeAssociatedLongs(b1.getNameReference().getValue());
				LongArray b = getAssociatedLongs(nr);
				b.setValue(part, b1.getValue(otherPart));
			}
		if (other.doubles!=null)
			for (int i=0; i<other.doubles.size(); i++) {
				DoubleArray b1 = (DoubleArray)other.doubles.elementAt(i);
				NameReference nr = makeAssociatedDoubles(b1.getNameReference().getValue());
				DoubleArray b = getAssociatedDoubles(nr);
				b.setValue(part, b1.getValue(otherPart));
			}
		if (other.strings!=null)
			for (int i=0; i<other.strings.size(); i++) {
				StringArray b1 = (StringArray)other.strings.elementAt(i);
				NameReference nr = makeAssociatedStrings(b1.getNameReference().getValue());
				StringArray b = getAssociatedStrings(nr);
				b.setValue(part, b1.getValue(otherPart));
			}
		if (other.objects!=null)
			for (int i=0; i<other.objects.size(); i++) {

				ObjectArray b1 = (ObjectArray)other.objects.elementAt(i);
				NameReference nr = makeAssociatedObjects(b1.getNameReference().getValue());
				ObjectArray b = getAssociatedObjects(nr);
				b.setValue(part, b1.getValue(otherPart));
			}
	}
	public void setJustAdded(int part, boolean b){
		if (part<0 || part>=numParts)
			return;
		if (justAdded == null || justAdded.getSize()!=numParts) {
			justAdded = new Bits(numParts);
		}
		justAdded.setBit(part,b);
	}
	public boolean getJustAdded(int part) {
		if (part<0 || part>=numParts || justAdded==null)
			return false;
		return justAdded.isBitOn(part);
	}
	public void resetJustAdded(){
		justAdded=null;
	}
	public void deleteJustAdded(){
		if (justAdded != null) {
			for (int i=numParts-1; i>0; i--)
				if (justAdded.isBitOn(i))
					deleteParts(i,1);
		}
	}
	protected void recordDefaultOrder(){
		rememberDefaultOrder = true;
		if (defaultOrder == null)
			defaultOrder = new int[numParts];
		for (int i=0; i<numParts; i++)
			defaultOrder[i] = i;
	}
	public void recordCurrentOrder(){
		if (currentOrder == null)
			currentOrder = new int[numParts];
		for (int i=0; i<numParts; i++)
			currentOrder[i] = i;
	}
	public void recordPreviousOrder(){
		if (previousOrder == null)
			previousOrder = new int[numParts];
		for (int i=0; i<numParts; i++)
			previousOrder[i] = i;
	}
	public void copyCurrentToPreviousOrder(){
		if (previousOrder == null || currentOrder==null)
			return;
		for (int i=0; i<previousOrder.length && i<currentOrder.length; i++)
			previousOrder[i] = currentOrder[i];
	}
	/*-----------------------------------------*/
	/** Returns version number of Associable.  Version number should be incremented with each change.*/
	public long getVersionNumber(){
		return versionNumber;
	}
	
	protected void incrementVersion(int code, boolean notify){
		versionNumber++;
		setDirty(true);
	}

	public int getNumberOfParts() {
		return numParts;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		boolean saveSnapOrd = rememberDefaultOrder && !orderMonotonic();
		boolean saveSnapSel = anySelected();
		boolean saveSnapAttach = anyAttachments();
		String list = null;
		if (saveSnapSel){
			list = selected.getListOfBitsOn(1);
			saveSnapSel = !StringUtil.blank(list);
		}

		if (saveSnapSel || saveSnapOrd || saveSnapAttach) {
			Snapshot temp = new Snapshot();
			if (saveSnapSel)
				temp.addLine("setSelected " + list);
			if (saveSnapOrd)
				temp.addLine("setDefaultOrder " + getDefaultOrderNEXUS());
			temp.addLine("attachments "+ "<" + writeAttachments() +">");
			return temp;
		}
		return null;
	}
	/*-----------------------------------------*/
	protected MesquiteInteger pos = new MesquiteInteger(0);
	/** For Commandable interface.  TODO:  have setDouble and pass part; have getDouble and pass part.  Likewise for long, etc. */
	public Object doCommand(String commandName, String arguments, CommandChecker checker){
		if (checker.compare(this.getClass(), "Sets the value of an associated double", "[name of double][value]", commandName, "setDoubleOfSelected")) {
			String name = ParseUtil.getFirstToken(arguments, pos);
			if (StringUtil.blank(name))
				return null;
			double bL = MesquiteDouble.fromString(arguments, pos);
			if (MesquiteDouble.isCombinable(bL)|| MesquiteDouble.isUnassigned(bL)){
				NameReference nr = NameReference.getNameReference(name);
				boolean c = false;
				for (int i = 0; i<numParts; i++)
					if (getSelected(i)) {
						setAssociatedDouble(nr, i, bL);
						c = true;
					}
				if (c)
					notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
			}
		}
		else if (checker.compare(this.getClass(), "Stores attachments", null, commandName, "attachments")) {
			MesquiteInteger pos = new MesquiteInteger(0);
			ParseUtil.getToken(arguments, pos);
			readAttachments(arguments, pos);
		}
		else if (checker.compare(this.getClass(), "Sets what parts are selected", "[list of parts]", commandName, "setSelected")) {
			int lastPart = -1;
			boolean join = false;
			MesquiteInteger pos = new MesquiteInteger(0);
			String token =  ParseUtil.getToken(arguments, pos); 
			boolean some = false;
			if (selected!=null)
				selected.clearAllBits();
			while (!StringUtil.blank(token) && !token.equals(";")) {
				if (token.equals(",") || token.equals(";")){
				}
				else	if (token.equals("-")) {
					if (lastPart!=-1)
						join = true;
				}
				else {
					int whichPart = MesquiteInteger.fromString(token, false);
					if (MesquiteInteger.isCombinable(whichPart)) {
						whichPart--; //to reset to 0 based
						if (join) {
							for (int j = lastPart; j<=whichPart; j ++)
								setSelected(j, true);
							join = false;
							lastPart = -1;
						}
						else {
							lastPart = whichPart;
							setSelected(whichPart, true);
							some = true;
						}
					}
				}
				token = ParseUtil.getToken(arguments, pos); 
			}

			if (some && !MesquiteThread.isScripting())
				notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
		}
		else if (checker.compare(this.getClass(), "Sets default order of parts", "[list of positions]", commandName, "setDefaultOrder")) {
			MesquiteInteger pos = new MesquiteInteger(0);
			String token =  ParseUtil.getToken(arguments, pos); 
			int part = -1;
			while (!StringUtil.blank(token) && !token.equals(";")) {
				int position = MesquiteInteger.fromString(token, false);
				part++;
				if (MesquiteInteger.isCombinable(position)) {
					setDefaultPosition(part, position);
				}
				token = ParseUtil.getToken(arguments, pos); 
			}

		}
		return null;
	}
	/** Sets the comment (e.g., footnote) of this element */
	public void setAnnotation(String e, boolean notify){
		comment = e;
		if (notify)
			notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));
	}
	/** Returns the comment (e.g., footnote) of this element */
	public String getAnnotation(){
		return comment;
	}
	/*.................................................................................................................*/
	/** Sets the element as dirty (changed since last file save) or not */
	public void setDirty(boolean d){

		if (d == true){
			versionNumber++;
		}
		dirty = d;
	}
	/*.................................................................................................................*/
	/** Returns whether the element is dirty (changed since last file save) or not */
	public boolean getDirty(){
		return dirty;
	}
	/*-----------------------------------------*/
	/** Returns true if part is within bounds.*/
	protected  boolean inBounds(int part) {
		return part>=0 && part<numParts; 
	}

	public boolean hasAnyAssociates(){
		if (bits != null && bits.size()>0)
			return true;
		if (longs != null && longs.size()>0)
			return true;
		if (doubles != null && doubles.size()>0)
			return true;
		if (strings != null && strings.size()>0)
			return true;
		if (objects != null && objects.size()>0)
			return true;
		return false;
	}
	public String listAssociates(){ //changed May 02
		String s = " Associates of ";
		if (this instanceof Listable)
			s =  getName() + "\n";
		else
			s = "object of class " + getClass().getName() + "\n";
		if (bits!=null) {
			s += "Bits " + '\n';
			for (int i=0; i<bits.size(); i++) {
				Listable b = (Listable)bits.elementAt(i);
				s += "   " + b.getName()+ " (is between? " + ((Bits)b).isBetween() + ")\n";	
			}
		}
		if (longs!=null) {
			s += "Longs " + '\n';
			for (int i=0; i<longs.size(); i++) {
				Object obj = longs.elementAt(i);
				Listable b = (Listable)longs.elementAt(i);
				s += "   " + b.getName()+ " (is between? " + ((LongArray)b).isBetween() + ")\n";	
			}
		}
		if (doubles!=null){
			s += "Doubles " + '\n';
			for (int i=0; i<doubles.size(); i++) {
				Listable b = (Listable)doubles.elementAt(i);
				s += "   " + b.getName()+ " (is between? " + ((DoubleArray)b).isBetween() + ")\n";	
			}
		}
		if (strings!=null){
			s += "Strings " + '\n';
			for (int i=0; i<strings.size(); i++) {
				Listable b = (Listable)strings.elementAt(i);
				s += "   " + b.getName()+ " (is between? " + ((StringArray)b).isBetween() + ")\n";	
			}
		}

		if (objects!=null) {
			s += "Objects (" + objects.size() + ")\n";
			for (int i=0; i<objects.size(); i++) {
				Object obj = objects.elementAt(i);
				if (obj instanceof DoubleArray){
					s += "  doubleArray:  " + ((DoubleArray)obj).getName()+ "\n";	
				}
				else if (obj instanceof String){
					s += "  string:  " + obj+ "\n";	
				}
				else if (obj instanceof String[]){
					s += "  strings:  ";
					String[] st = (String[])obj;
					for (int k = 0; k<st.length; k++)
						s += " [" + st[k] + "]";
					s += "\n";	
				}
				else if (obj instanceof ObjectArray){
					s += "  ObjectArray:  " + ((ObjectArray)obj).getName()+ " (is between? " + ((ObjectArray)obj).isBetween() + ")\n";	
				}
				else if (obj instanceof StringArray){
					s += "  StringArray as ObjectArray:  " + ((StringArray)obj).getName()+ "\n";	
				}	
				else if (obj instanceof Listable){
					Listable b = (Listable)obj;
					s += "   " + b.getName()+ " " + b.getClass() + "\n";	
				}
				else
					s += "   Object of class " + obj.getClass().getName()+ "\n";	
			}
		}
		return s;
	}

	public String[] getAssociatesNames(){ 
		int total = getNumberAssociatedBits() +getNumberAssociatedLongs() + getNumberAssociatedDoubles() + getNumberAssociatedStrings() + getNumberAssociatedObjects();
		if (total == 0)
			return null;
		String[] names = new String[total];
		int count = 0;
		if (bits!=null) {
			for (int i=0; i<bits.size(); i++) {
				Listable b = (Listable)bits.elementAt(i);
				names[count++] = b.getName();
			}
		}
		if (longs!=null) {
			for (int i=0; i<longs.size(); i++) {
				Object obj = longs.elementAt(i);
				Listable b = (Listable)longs.elementAt(i);
				names[count++] = b.getName();
			}
		}
		if (doubles!=null){
			for (int i=0; i<doubles.size(); i++) {
				Listable b = (Listable)doubles.elementAt(i);
				names[count++] = b.getName();
			}
		}
		if (strings!=null){
			for (int i=0; i<strings.size(); i++) {
				Listable b = (Listable)strings.elementAt(i);
				names[count++] = b.getName();
			}
		}
		if (objects!=null) {
			for (int i=0; i<objects.size(); i++) {
				Listable b = (Listable)objects.elementAt(i);
				names[count++] = b.getName();
			}
		}
		return names;
	}

	/*-----------------------------------------*/
	public void renameAssociated(DisplayableBranchProperty property, String newName, boolean notify){
		Nameable d = null;
		if (property.kind == Associable.BITS)
			d = getAssociatedBits(property.getNameReference());
		else if (property.kind == Associable.DOUBLES)
			d = getAssociatedDoubles(property.getNameReference());
		else if (property.kind == Associable.LONGS)
			d =  getAssociatedLongs(property.getNameReference());
		else if (property.kind == Associable.STRINGS)
			d =  getAssociatedStrings(property.getNameReference());
		else if (property.kind == Associable.OBJECTS)
			d =  getAssociatedObjects(property.getNameReference());
		if (d != null){
			d.setName(newName);
			property.setName(newName);
			if (notify)
				notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
		}
	}
	public boolean isPropertyAssociated(PropertyRecord property){
		if (property.kind == Associable.BUILTIN)
			return this instanceof MesquiteTree && (property.getNameReference().equals(MesquiteTree.branchLengthNameRef) || property.getNameReference().equals(MesquiteTree.nodeLabelNameRef));
		if (property.kind == Associable.BITS)
			return getAssociatedBits(property.getNameReference())!= null;
		if (property.kind == Associable.DOUBLES)
			return getAssociatedDoubles(property.getNameReference())!= null;
		if (property.kind == Associable.LONGS)
			return getAssociatedLongs(property.getNameReference())!= null;
		if (property.kind == Associable.STRINGS)
			return getAssociatedStrings(property.getNameReference())!= null;
		if (property.kind == Associable.OBJECTS)
			return getAssociatedObjects(property.getNameReference())!= null;
		return false;
	}
	public boolean propertyIsBetween(PropertyRecord property){
		if (property.kind == Associable.BUILTIN){
			if (this instanceof MesquiteTree){
				if (property.getNameReference().equals(MesquiteTree.branchLengthNameRef))
					return true;
				if ( property.getNameReference().equals(MesquiteTree.nodeLabelNameRef))
					return false;
			}
		}
		if (property.kind == Associable.BITS){
			Bits d = getAssociatedBits(property.getNameReference());
			if (d!= null)
				return d.isBetween();
		}
		if (property.kind == Associable.DOUBLES){
			DoubleArray d = getAssociatedDoubles(property.getNameReference());
			if (d!= null)
				return d.isBetween();
		}
		if (property.kind == Associable.LONGS){
			LongArray d = getAssociatedLongs(property.getNameReference());
			if (d!= null)
				return d.isBetween();
		}
		if (property.kind == Associable.STRINGS){
			StringArray d = getAssociatedStrings(property.getNameReference());
			if (d!= null)
				return d.isBetween();
		}
		if (property.kind == Associable.OBJECTS){
			ObjectArray d = getAssociatedObjects(property.getNameReference());
			if (d!= null)
				return d.isBetween();
		}
		return false;
	}
	public void setPropertyIsBetween(PropertyRecord property, boolean isBetween){
		if (property.kind == Associable.BITS){
			Bits d = getAssociatedBits(property.getNameReference());
			if (d!= null)
				d.setBetweenness(isBetween);
		}
		if (property.kind == Associable.DOUBLES){
			DoubleArray d = getAssociatedDoubles(property.getNameReference());
			if (d!= null)
				d.setBetweenness(isBetween);
		}
		if (property.kind == Associable.LONGS){
			LongArray d = getAssociatedLongs(property.getNameReference());
			if (d!= null)
				d.setBetweenness(isBetween);
		}
		if (property.kind == Associable.STRINGS){
			StringArray d = getAssociatedStrings(property.getNameReference());
			if (d!= null)
				d.setBetweenness(isBetween);
		}
		if (property.kind == Associable.OBJECTS){
			ObjectArray d = getAssociatedObjects(property.getNameReference());
			if (d!= null)
				d.setBetweenness(isBetween);
		}
	}

	public void getAssociatesWithBetweenness(ListableVector v, int node, boolean target){
		if (v == null)
			return;
		if (bits!=null)
			for (int i=0; i<bits.size(); i++) {
				Bits b = (Bits)bits.elementAt(i);
				if (b.isBetween() == target && b.isBitOn(node))
					v.addElement(b, false);
			}
		if (longs!=null)
			for (int i=0; i<longs.size(); i++) {
				LongArray b = (LongArray)longs.elementAt(i);
				if (b.isBetween() == target && MesquiteLong.isCombinable(b.getValue(node)))
					v.addElement(b, false);
			}
		if (doubles!=null)
			for (int i=0; i<doubles.size(); i++) {
				DoubleArray b = (DoubleArray)doubles.elementAt(i);
				if (b.isBetween() == target && MesquiteDouble.isCombinable(b.getValue(node)))
					v.addElement(b, false);
			}
		if (strings!=null)
			for (int i=0; i<strings.size(); i++) {
				StringArray b = (StringArray)strings.elementAt(i);
				if (b.isBetween() == target && !StringUtil.blank(b.getValue(node)))
					v.addElement(b, false);
			}
		if (objects!=null)
			for (int i=0; i<objects.size(); i++) {
				ObjectArray b = (ObjectArray)objects.elementAt(i);
				if (b.isBetween() == target && b.getValue(node) != null)
					v.addElement(b, false);
			}
	}
	public boolean anyAssociatesWithBetweenness(int node, boolean target){
		if (bits!=null)
			for (int i=0; i<bits.size(); i++) {
				Bits b = (Bits)bits.elementAt(i);
				if (b.isBetween() == target && b.isBitOn(node))
					return true;
			}
		if (longs!=null)
			for (int i=0; i<longs.size(); i++) {
				LongArray b = (LongArray)longs.elementAt(i);
				if (b.isBetween() == target && MesquiteLong.isCombinable(b.getValue(node)))
					return true;
			}
		if (doubles!=null)
			for (int i=0; i<doubles.size(); i++) {
				DoubleArray b = (DoubleArray)doubles.elementAt(i);
				if (b.isBetween() == target && MesquiteDouble.isCombinable(b.getValue(node)))
					return true;
			}
		if (strings!=null)
			for (int i=0; i<strings.size(); i++) {
				StringArray b = (StringArray)strings.elementAt(i);
				if (b.isBetween() == target && !StringUtil.blank(b.getValue(node)))
					return true;
			}
		if (objects!=null)
			for (int i=0; i<objects.size(); i++) {
				ObjectArray b = (ObjectArray)objects.elementAt(i);
				if (b.isBetween() == target && b.getValue(node) != null)
					return true;
			}
		return false;
	}
	public boolean anyAssociatesWithBetweenness(boolean target){
		if (bits!=null)
			for (int i=0; i<bits.size(); i++) {
				Bits b = (Bits)bits.elementAt(i);
				if (b.isBetween() == target)
					return true;
			}
		if (longs!=null)
			for (int i=0; i<longs.size(); i++) {
				LongArray b = (LongArray)longs.elementAt(i);
				if (b.isBetween() == target)
					return true;
			}
		if (doubles!=null)
			for (int i=0; i<doubles.size(); i++) {
				DoubleArray b = (DoubleArray)doubles.elementAt(i);
				if (b.isBetween() == target)
					return true;
			}
		if (strings!=null)
			for (int i=0; i<strings.size(); i++) {
				StringArray b = (StringArray)strings.elementAt(i);
				if (b.isBetween() == target)
					return true;
			}
		if (objects!=null)
			for (int i=0; i<objects.size(); i++) {
				ObjectArray b = (ObjectArray)objects.elementAt(i);
				if (b.isBetween() == target)
					return true;
			}
		return false;
	}

	public void deassignAllColor(){
		deassignAllAssociatedStrings(ColorDistribution.colorRGBNameReference);
	}
	public void setColor(int node, String hex){
		setAssociatedString(ColorDistribution.colorRGBNameReference, node, hex);
	}
	public void setColor(int node, Color c){
		String hex = ColorDistribution.hexFromColor(c);
		setAssociatedString(ColorDistribution.colorRGBNameReference, node, hex);
	}
	public void setColor(int node, int standardColorNumber){
		String hex = ColorDistribution.hexFromColor(standardColorNumber);
		setAssociatedString(ColorDistribution.colorRGBNameReference, node, hex);
	}
	public Color getColor(int node){
		Object c = getAssociatedString(ColorDistribution.colorRGBNameReference, node);
		if (c instanceof String){
			return ColorDistribution.colorFromHex((String)c);
		}
		return null;
	}
	public String getColorAsHexString(int node){
		Object c = getAssociatedString(ColorDistribution.colorRGBNameReference, node);
		if (c instanceof String){
			return (String)c;
		}
		return null;
	}
	public String writeAssociated(int node, boolean associatedUseComments){
		String s = null;
		if (associatedUseComments)
			s = "["  + Parser.substantiveCommentMark;
		else
			s = "<";
		boolean first = true;
		boolean biton = false;
		if (bits!=null)
			for (int i=0; i<bits.size(); i++) {
				Bits b = (Bits)bits.elementAt(i);
				if (b.isBitOn(node)) {
					if (!first)
						s += ", ";
					first = false;
					s+= StringUtil.tokenize(b.getName()) + " = on ";
					biton = true;
				}
			}
		if (longs!=null)
			for (int i=0; i<longs.size(); i++) {
				LongArray b = (LongArray)longs.elementAt(i);

				if (b.getValue(node)!=MesquiteLong.unassigned) {
					if (!first)
						s += ", ";
					first = false;
					if ("color".equals(b.getName())) {  //special treatment to convert old to new
						s+= StringUtil.tokenize("!color") + " = " + ColorDistribution.hexFromColor(b.getValue(node)) + " ";
					}
					else {
						s+= StringUtil.tokenize(b.getName()) + " = " + MesquiteLong.toString(b.getValue(node)) + " ";
					}
				}
			}
		if (doubles!=null)
			for (int i=0; i<doubles.size(); i++) {
				DoubleArray b = (DoubleArray)doubles.elementAt(i);
				if (b.getValue(node)!=MesquiteDouble.unassigned){
					if (!first)
						s += ", ";
					first = false;
					s+= StringUtil.tokenize(b.getName()) + " = " + MesquiteDouble.toString(b.getValue(node)) + " ";
				}
			}
		if (strings!=null)
			for (int i=0; i<strings.size(); i++) {
				StringArray b = (StringArray)strings.elementAt(i);
				String sNode = b.getValue(node);
				if (sNode!=null && sNode.length()>0){
					if (!first)
						s += ", ";
					first = false;
					s+= StringUtil.tokenize(b.getName()) + " = " + ParseUtil.tokenize(sNode) + " ";
				}
			}
		if (objects!=null)
			for (int i=0; i<objects.size(); i++) {
				ObjectArray b = (ObjectArray)objects.elementAt(i);
				Object obj = b.getValue(node);
				if (obj!=null && (obj instanceof Listable || obj instanceof String)){
					if (!first)
						s += ", ";
					first = false;
					if (obj instanceof DoubleArray){
						DoubleArray ddoubles = (DoubleArray)obj;
						s+= StringUtil.tokenize(b.getName()) + " = {";
						boolean firstD = true;
						for (int k = 0; k<ddoubles.getSize(); k++){
							if (!firstD)
								s += ", ";
							firstD = false;
							s += MesquiteDouble.toString(ddoubles.getValue(k));
						}
						s+=  "} ";
					}
					else if (obj instanceof StringArray){
						StringArray sstrings = (StringArray)obj;
						s+= StringUtil.tokenize(b.getName()) + " = {";
						boolean firstD = true;
						for (int k = 0; k<sstrings.getSize(); k++){
							if (!firstD)
								s += ", ";
							firstD = false;
							s += ParseUtil.tokenize(sstrings.getValue(k));
						}
						s+=  "} ";
					}
					else if (obj instanceof String){
						MesquiteMessage.printStackTrace("Associable: writing string in objectarray!");
						s+= StringUtil.tokenize(b.getName()) + " = " + ParseUtil.tokenize((String)obj) + " ";
					}
					else {
						MesquiteMessage.warnProgrammer("Warning: Saving of objects of type " + obj.getClass() +" in Associables not yet working!");
						if (MesquiteTrunk.developmentMode)
							MesquiteMessage.printStackTrace();
					}
				}
			}
		if ((associatedUseComments && s.equals("[" + Parser.substantiveCommentMark)) || (!associatedUseComments && s.equals("<")))
			return "";
		else if (associatedUseComments)
			return s+ "]";
		else
			return s + ">";
	}
	/* --#########################################----*/
	public void readAssociated(String assocString, int node, MesquiteInteger pos){
		readAssociated(assocString,node,pos, (String)null, (String)null, false);

	}
	public void readAssociated(String assocString, int node, MesquiteInteger pos, String whitespace, String punctuation){
		readAssociated(assocString, node, pos, whitespace, punctuation, false);
	}

	boolean reportReading = false;
	/* Primarily from trees; the punctuation in comments may follow Newick rules */
	public void readAssociated(String assocString, int node, MesquiteInteger pos, String whitespace, String punctuation, boolean forceNumberToDouble){
		if (pos==null || node>numParts || node<0 || StringUtil.blank(assocString))
			return;
		String key=ParseUtil.getToken(assocString, pos, whitespace, punctuation);
		if (reportReading) Debugg.println("!!!" + pos.getValue() + " ~~~~~~~~~~~~readAssociated at " + pos.getValue() + "  " + assocString);
		while (!">".equals(key)) {
			if (StringUtil.blank(key))
				return;
			if (reportReading) Debugg.println("!!!~~KEY " + key);
			if (reportReading) Debugg.println("     @~~preferred kind " + PropertyRecord.preferredKind(key));
			String eq = ParseUtil.getToken(assocString, pos, whitespace, punctuation); //eating up equals
			if (reportReading) Debugg.println("     !!!~~equal " + eq);
			int oldPos = pos.getValue();
			String value = ParseUtil.getToken(assocString, pos, whitespace, punctuation); //finding value
			value=StringUtil.removeFirstCharacterIfMatch(value, '\'');
			value=StringUtil.removeLastCharacterIfMatch(value, '\'');
			//if (whitespace != null && whitespace.length() == 0){
			value = StringUtil.stripLeadingWhitespace(value);
			value = StringUtil.stripTrailingWhitespace(value);
			if (reportReading) Debugg.println("     !!!~~[value] [" + value + "]");
			//}
			if (StringUtil.blank(value))
				return;
			if (key.equals("color")){ //special case; reading old color possibly
				int oldColor = MesquiteInteger.fromString(value);
				if (value.length()<=2 && oldColor>=0 && oldColor<20) //old color; convert
					value =ColorDistribution.hexFromColor(oldColor);
				setAssociatedString(ColorDistribution.colorRGBNameReference, node, value);
			}
			else if (key.equalsIgnoreCase("setBetweenLong")) { //note this is not for the node, but for the tree. This is to read an old Mesquite 3 convention
				/* disabled as that is now controlled otherwise
				 NameReference nRef = NameReference.getNameReference(value);
				LongArray b = getAssociatedLongs(nRef);
				if (b != null)
					b.setBetweenness(true);
					*/
			}
			else if (key.equalsIgnoreCase("setBetweenDouble")) {//note this is not for the node, but for the tree. This is to read an old Mesquite 3 convention
				/* disabled as that is now controlled otherwise
			NameReference nRef = NameReference.getNameReference(value);
				DoubleArray b = getAssociatedDoubles(nRef);
				if (b != null)
					b.setBetweenness(true);
					*/
			}
			else if (key.equalsIgnoreCase("setBetweenObject")) {//note this is not for the node, but for the tree. This is to read an old Mesquite 3 convention
				/* disabled as that is now controlled otherwise
				NameReference nRef = NameReference.getNameReference(value);
				ObjectArray b = getAssociatedObjects(nRef);
				if (b != null)
					b.setBetweenness(true);
					*/
			}
			else if (key.equalsIgnoreCase("triangled")) { //note this is not for the node, but for the tree. This is to read an old Mesquite 3 convention
				NameReference nr = makeAssociatedBits("collapsed");
				Bits bb = getAssociatedBits(nr);
				bb.setBit(node, true);
			}
			else if (value.equalsIgnoreCase("on")) {
				NameReference nr = makeAssociatedBits(key);
				Bits bb = getAssociatedBits(nr);
				bb.setBit(node, true);
			}
			else if (value.equalsIgnoreCase("off")) {
				NameReference nr = makeAssociatedBits(key);
				Bits bb = getAssociatedBits(nr);
				bb.setBit(node, false);
			}
			else if (value.indexOf("string:") == 0) { //treat as String 

				NameReference nr = makeAssociatedStrings(key);
				StringArray bb = getAssociatedStrings(nr);
				bb.setValue(node, value.substring(7, value.length()));
			}
			else if (value.indexOf("strings") == 0) { //treat as String[] 

				NameReference nr = makeAssociatedObjects(key);
				ObjectArray bb = getAssociatedObjects(nr);		
				//	bb.setValue(node, value.substring(7, value.length()));
			}
			else if (value.indexOf("{") == 0) { //treat as Objects (DoubleArray or StringArray) bounded by {}; e.g., added to read BEAST results
				int pPos = pos.getValue();
				double vn = MesquiteDouble.fromString(assocString, pos);
				if (reportReading) Debugg.println("    {}~~ vn " + vn);
				if (MesquiteDouble.isCombinable(vn)){ //Objects: DoubleArrays
					pos.setValue(pPos);
					DoubleArray values = new DoubleArray(1);
					String s="";
					int count = 1;
					while (!"}".equals(s)) {
						double v = MesquiteDouble.fromString(assocString, pos);
						if (reportReading) Debugg.println("    v~~ " + v);
						values.resetSize(count);
						values.setValue(count-1, v);
						count++;
						s=ParseUtil.getToken(assocString, pos, whitespace, punctuation); //comma or }
						if (reportReading) Debugg.println("    s{}~~ " + s);

					}

					NameReference nr = makeAssociatedObjects(key);
					ObjectArray bb = getAssociatedObjects(nr);
					bb.setValue(node, values);
				}
				else { //Objects: StringArrays 
					pos.setValue(pPos);
					StringArray values = new StringArray(1);
					String s="";
					int count = 1;
					while (!"}".equals(s)) {
						s=ParseUtil.getToken(assocString, pos, whitespace, punctuation); 
						values.resetSize(count);
						values.setValue(count-1, s);
						count++;
						s=ParseUtil.getToken(assocString, pos, whitespace, punctuation); //comma or }
						if (reportReading) Debugg.println("    s{}~~ " + s);
					}

					NameReference nr = makeAssociatedObjects(key);
					ObjectArray bb = getAssociatedObjects(nr);
					bb.setValue(node, values);
				}
			}
			else if ((PropertyRecord.preferredKind(key)== Associable.DOUBLES) || (forceNumberToDouble && MesquiteNumber.isNumber(value)) || ((value.indexOf(".")>=0) && MesquiteDouble.interpretableAsDouble(assocString, pos, oldPos))) { //treat as double 
				if (reportReading) Debugg.println("    ~~to double " + value);
				NameReference nrEx= NameReference.getNameReference(key);   // fixed in 3.01
				DoubleArray bb = getAssociatedDoubles(nrEx);       //Finding doubles if they exist
				if (bb == null) {
					//Making doubles to be filled
					NameReference nr = makeAssociatedDoubles(key);
					bb = getAssociatedDoubles(nr);

					//but first check to see if there are longs.  If so, and if doubles hadn't existed before, then transfer
					NameReference nrExL= NameReference.getNameReference(key);
					LongArray longs = getAssociatedLongs(nrExL);
					if (longs != null){
						//There is an array of longs of the same name.  It's therefore assumed that they should all be upgraded to doubles!
						longs.copyTo(bb);
						removeAssociatedLongs(nrExL);   //delete longs as no longer needed
					}

				}
				pos.setValue(oldPos);
				bb.setValue(node, MesquiteDouble.fromString(assocString, pos));
			}
			//at this point there are just two alternatives left that are recognized: a string, and an integer
			//first check to see if it could be a number
			else if ("0123456789-+".indexOf(value.charAt(0))<0 || MesquiteLong.fromString(value) == MesquiteLong.impossible) {  //doesn't start as number or starts as number but not interpretable as long
				if (reportReading) Debugg.println("    ~~to string " + value);
				if (reportReading) Debugg.println("          ~~\"0123456789-+\".indexOf(value.charAt(0))<0 " + ("0123456789-+".indexOf(value.charAt(0))<0));
				if (reportReading) Debugg.println("          ~~MesquiteLong.fromString(value) == MesquiteLong.impossible " + (MesquiteLong.fromString(value) == MesquiteLong.impossible));
				NameReference nr = makeAssociatedStrings(key);
				StringArray bb = getAssociatedStrings(nr);
				bb.setValue(node, value);
			}
			else {  //treat as long, unless (fixed in 3.01) same name exists as DoubleArray in which case put there
				if (reportReading) Debugg.println("    {}~~to long or double " + value );
				NameReference nrEx= NameReference.getNameReference(key);   // 
				DoubleArray bbd = getAssociatedDoubles(nrEx);       //Finding doubles if they exist; if so, use as doubles instead!!!!!
				if (bbd != null){
					pos.setValue(oldPos);
					bbd.setValue(node, MesquiteInteger.fromString(assocString, pos));
				} 
				else {
					NameReference nr = makeAssociatedLongs(key);
					LongArray bb = getAssociatedLongs(nr);
					//pos.setValue(oldPos);
					bb.setValue(node, MesquiteInteger.fromString(value)); //need to get it this way to keep it moving
				}
			}
			key=ParseUtil.getToken(assocString, pos, whitespace, punctuation);
			if (",".equals(key)) //eating up "," separating subcommands
				key=ParseUtil.getToken(assocString, pos, whitespace, punctuation);
		}
	}

	/* -----------------------------------------------------------------------------------------*/
	public void setAssociateds(Associable a){
		if (a==null)
			return;
		setNumberOfParts(a.getNumberOfParts());
		bits.removeAllElements();
		longs.removeAllElements();
		strings.removeAllElements();
		doubles.removeAllElements();
		objects.removeAllElements();
		if (a.bits!=null)
			for (int i=0; i<a.bits.size(); i++) {
				Bits b1 = (Bits)a.bits.elementAt(i);
				NameReference nr = makeAssociatedBits(b1.getNameReference().getValue());
				Bits b = getAssociatedBits(nr);
				b1.copyBits(b);
				b.setBetweenness(b1.isBetween());
			}
		if (a.longs!=null)
			for (int i=0; i<a.longs.size(); i++) {
				LongArray b1 = (LongArray)a.longs.elementAt(i);
				NameReference nr = makeAssociatedLongs(b1.getNameReference().getValue());
				LongArray b = getAssociatedLongs(nr);
				b1.copyTo(b);
				b.setBetweenness(b1.isBetween());
			}
		if (a.doubles!=null)
			for (int i=0; i<a.doubles.size(); i++) {
				DoubleArray b1 = (DoubleArray)a.doubles.elementAt(i);
				NameReference nr = makeAssociatedDoubles(b1.getNameReference().getValue());
				DoubleArray b = getAssociatedDoubles(nr);
				b1.copyTo(b);
				b.setBetweenness(b1.isBetween());
			}
		if (a.strings!=null)
			for (int i=0; i<a.strings.size(); i++) {
				StringArray b1 = (StringArray)a.strings.elementAt(i);
				NameReference nr = makeAssociatedStrings(b1.getNameReference().getValue());
				StringArray b = getAssociatedStrings(nr);
				b1.copyTo(b);
				b.setBetweenness(b1.isBetween());
			}
		if (a.objects!=null)
			for (int i=0; i<a.objects.size(); i++) {

				ObjectArray b1 = (ObjectArray)a.objects.elementAt(i);
				NameReference nr = makeAssociatedObjects(b1.getNameReference().getValue());
				ObjectArray b = getAssociatedObjects(nr);
				b1.copyTo(b);
				b.setBetweenness(b1.isBetween());
			}
	}
	public void transferAssociated(int fromNode, int toNode){
		if (bits!=null)
			for (int i=0; i<bits.size(); i++) {
				Bits b = (Bits)bits.elementAt(i);
				b.setBit(toNode, b.isBitOn(fromNode));
			}
		if (longs!=null)
			for (int i=0; i<longs.size(); i++) {
				LongArray b = (LongArray)longs.elementAt(i);
				b.setValue(toNode, b.getValue(fromNode));
			}
		if (doubles!=null)
			for (int i=0; i<doubles.size(); i++) {
				DoubleArray b = (DoubleArray)doubles.elementAt(i);
				b.setValue(toNode, b.getValue(fromNode));
			}
		if (strings!=null)
			for (int i=0; i<strings.size(); i++) {
				StringArray b = (StringArray)strings.elementAt(i);
				b.setValue(toNode, b.getValue(fromNode));
			}
		if (objects!=null)
			for (int i=0; i<objects.size(); i++) {
				ObjectArray b = (ObjectArray)objects.elementAt(i);
				b.setValue(toNode, b.getValue(fromNode));
			}
		incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
	}

	public void exchangeAssociated(int node1, int  node2){
		if (bits!=null)
			for (int i=0; i<bits.size(); i++) {
				Bits b = (Bits)bits.elementAt(i);
				boolean n1 = b.isBitOn(node1);
				b.setBit(node1, b.isBitOn(node2));
				b.setBit(node2, n1);
			}
		if (longs!=null)
			for (int i=0; i<longs.size(); i++) {
				LongArray b = (LongArray)longs.elementAt(i);
				long n1 = b.getValue(node1);
				b.setValue(node1, b.getValue(node2));
				b.setValue(node2, n1);
			}
		if (doubles!=null)
			for (int i=0; i<doubles.size(); i++) {
				DoubleArray b = (DoubleArray)doubles.elementAt(i);
				double n1 = b.getValue(node1);
				b.setValue(node1, b.getValue(node2));
				b.setValue(node2, n1);
			}
		if (strings!=null)
			for (int i=0; i<strings.size(); i++) {
				StringArray b = (StringArray)strings.elementAt(i);
				String n1 = b.getValue(node1);
				b.setValue(node1, b.getValue(node2));
				b.setValue(node2, n1);
			}
		if (objects!=null)
			for (int i=0; i<objects.size(); i++) {
				ObjectArray b = (ObjectArray)objects.elementAt(i);
				Object n1 = b.getValue(node1);
				b.setValue(node1, b.getValue(node2));
				b.setValue(node2, n1);
			}
	}
	public void deassignAssociated(int node){
		if (bits!=null)
			for (int i=0; i<bits.size(); i++) {
				Bits b = (Bits)bits.elementAt(i);
				b.clearBit(node);	
			}
		if (longs!=null)
			for (int i=0; i<longs.size(); i++) {
				LongArray b = (LongArray)longs.elementAt(i);
				b.setValue(node, MesquiteLong.unassigned);
			}
		if (doubles!=null)
			for (int i=0; i<doubles.size(); i++) {
				DoubleArray b = (DoubleArray)doubles.elementAt(i);
				b.setValue(node, MesquiteDouble.unassigned);
			}
		if (strings!=null)
			for (int i=0; i<strings.size(); i++) {
				StringArray b = (StringArray)strings.elementAt(i);
				b.setValue(node, null);
			}
		if (objects!=null)
			for (int i=0; i<objects.size(); i++) {
				ObjectArray b = (ObjectArray)objects.elementAt(i);
				b.setValue(node, null);
			}
	}
	public void deassignAssociated(){ //removeAllAssociated deleteAllAssociated
		if (bits!=null)
			bits.removeAllElements();
		if (longs!=null)
			longs.removeAllElements();
		if (doubles!=null)
			doubles.removeAllElements();
		if (strings!=null)
			strings.removeAllElements();
		if (objects!=null)
			objects.removeAllElements();
	}
	/*--============================================================================-*/
	/** Set the number of parts to given number.  THIS MUST BE CALLED whenever the number of
	parts (characters, nodes, etc.) changes.*/
	public void setNumberOfParts(int num){
		//TODO: adjust all associated information for newNum
		if (numParts==num)
			return;
		numParts = num;
		if (bits!=null) {
			for (int i=0; i< bits.size(); i++) {
				Bits b = (Bits)bits.elementAt(i);
				b.resetSize(numParts);
			}
		}
		if (longs!=null) {
			for (int i=0; i< longs.size(); i++) {
				LongArray b = (LongArray)longs.elementAt(i);
				b.resetSize(numParts);
			}
		}
		if (doubles!=null)
			for (int i=0; i< doubles.size(); i++) {
				DoubleArray b = (DoubleArray)doubles.elementAt(i);
				b.resetSize(numParts);
			}
		if (strings!=null) {
			for (int i=0; i< strings.size(); i++) {
				StringArray b = (StringArray)strings.elementAt(i);
				b.resetSize(numParts);
			}
		}
		if (objects!=null)
			for (int i=0; i< objects.size(); i++) {
				ObjectArray b = (ObjectArray)objects.elementAt(i);
				b.resetSize(numParts);
			}
		incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
	}
	/*-----------------------------------------*/
	private int[] addToOrder(int[] order, int starting, int num) {
		//if adding to end, then put after maximum
		//if adding to middle, then give new parts their native numbers and shift any above their range upward, even if currently in front of new parts
		if (defaultOrder==null) {
			int[] newValues = new int[numParts+num];
			for (int i=0; i<numParts+num; i++) { 
				newValues[i]=i;
			}
			return newValues;
		}

		boolean addToEnd = false;
		int maxValue = numParts-1;
		if (starting == numParts) {
			starting = numParts-1;
			addToEnd = true;
			maxValue = IntegerArray.maximum(defaultOrder);
		}
		int newNum = numParts + num;
		int[] newValues = new int[newNum];
		//if before point of insertion, just copy default order
		for (int i=0; i<=starting; i++) { 
			newValues[i]=defaultOrder[i];
			if (!addToEnd && newValues[i]>starting) {
				newValues[i] += num+1;
			}
		}
		//for inserted ones, give new numbers bigger than previous
		for (int i=0; i<num ; i++) {
			if (addToEnd)
				newValues[starting + i + 1]= ++maxValue;
			else
				newValues[starting + i + 1]= starting + i + 1;
		}
		//after insertion, give new numbers bigger than previous
		for (int i=0; i<numParts-starting-1; i++) {
			if (starting+i+1>=defaultOrder.length)
				newValues[i +starting+num+1]=starting + i + 1;
			else
				newValues[i +starting+num+1]=defaultOrder[starting + i + 1];
			if (!addToEnd && newValues[i +starting+num+1]>=starting) {
				newValues[i +starting+num+1] += num;
			}
		}
		return newValues;
	}
	/*-----------------------------------------*/
	public String report(){
		String r = "class " + getClass().getName() + "\n";
		r += " numParts " + numParts + "\n";
		r += " defaultOrder " + defaultOrder + "\n";
		r += " currentOrder " + currentOrder + "\n";
		r += " previousOrder " + previousOrder + "\n";
		return r;
	}
	/*-----------------------------------------*/
	public static long totalPartsAdded = 0;
	public boolean addParts(int starting, int num){
		if (num==0)
			return false;
		try {
			totalPartsAdded+=num;
			if (starting<0) starting = -1;
			if (starting>numParts) starting = numParts;
			if (bits!=null) {
				for (int i=0; i< bits.size(); i++) {
					Bits b = (Bits)bits.elementAt(i);
					b.addParts(starting, num);
				}
			}
			if (longs!=null) {
				for (int i=0; i< longs.size(); i++) {
					LongArray b = (LongArray)longs.elementAt(i);
					b.addParts(starting, num, MesquiteLong.unassigned);
				}
			}
			if (doubles!=null)
				for (int i=0; i< doubles.size(); i++) {
					DoubleArray b = (DoubleArray)doubles.elementAt(i);
					b.addParts(starting, num);
				}
			if (strings!=null)
				for (int i=0; i< strings.size(); i++) {
					StringArray b = (StringArray)strings.elementAt(i);
					b.addParts(starting, num);
				}
			if (objects!=null)
				for (int i=0; i< objects.size(); i++) {
					ObjectArray b = (ObjectArray)objects.elementAt(i);
					b.addParts(starting, num); //comments handled here
				}
			if (defaultOrder != null){
				defaultOrder = addToOrder(defaultOrder,starting,num);
			}
			if (currentOrder != null){
				currentOrder = addToOrder(currentOrder,starting,num);
			}
			if (previousOrder != null){
				previousOrder = addToOrder(previousOrder,starting,num);
			}
			if (justAdded!=null) {
				justAdded.addParts(starting, num);
			}
			numParts = numParts+num;
			for (int i=0;i<num; i++)
				setJustAdded(starting+i+1, true);
		}
		catch (Exception e){
		}

		incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
		return true;
	}
	/*-----------------------------------------*/
	public boolean deleteParts(int starting, int num){

		if (num==0)
			return false;
		if (starting>numParts || starting<0) 
			return false;

		if (bits!=null) {
			for (int i=0; i< bits.size(); i++) {
				Bits b = (Bits)bits.elementAt(i);
				b.deleteParts(starting, num);
			}
		}
		if (longs!=null) {
			for (int i=0; i< longs.size(); i++) {
				LongArray b = (LongArray)longs.elementAt(i);
				b.deleteParts(starting, num);
			}
		}
		if (doubles!=null)
			for (int i=0; i< doubles.size(); i++) {
				DoubleArray b = (DoubleArray)doubles.elementAt(i);
				b.deleteParts(starting, num);
			}
		if (strings!=null)
			for (int i=0; i< strings.size(); i++) {
				StringArray b = (StringArray)strings.elementAt(i);
				b.deleteParts(starting, num);
			}
		if (objects!=null)
			for (int i=0; i< objects.size(); i++) {
				ObjectArray b = (ObjectArray)objects.elementAt(i);
				b.deleteParts(starting, num); //comments handled here
			}
		if (defaultOrder != null){
			defaultOrder = IntegerArray.deleteParts(defaultOrder, starting, num);
		}
		if (currentOrder != null){
			currentOrder = IntegerArray.deleteParts(currentOrder, starting, num);
		}
		if (previousOrder != null){
			previousOrder = IntegerArray.deleteParts(previousOrder, starting, num);
		}
		numParts = numParts-num;
		incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
		return true;
	}
	/** Deletes parts flagged for deletion in Bits*/
	protected boolean deletePartsFlagged(Bits toDelete){ 

		if (bits!=null) {
			for (int i=0; i< bits.size(); i++) {
				Bits b = (Bits)bits.elementAt(i);
				b.deletePartsFlagged(toDelete);
			}
		}
		if (longs!=null) {
			for (int i=0; i< longs.size(); i++) {
				LongArray b = (LongArray)longs.elementAt(i);
				b.deletePartsFlagged(toDelete);
			}
		}
		if (doubles!=null)
			for (int i=0; i< doubles.size(); i++) {
				DoubleArray b = (DoubleArray)doubles.elementAt(i);
				b.deletePartsFlagged(toDelete);
			}
		if (strings!=null)
			for (int i=0; i< strings.size(); i++) {
				StringArray b = (StringArray)strings.elementAt(i);
				b.deletePartsFlagged(toDelete);
			}
		if (objects!=null)
			for (int i=0; i< objects.size(); i++) {
				ObjectArray b = (ObjectArray)objects.elementAt(i);
				b.deletePartsFlagged(toDelete);
			}
		if (defaultOrder != null){
			defaultOrder = IntegerArray.deletePartsFlagged(defaultOrder, toDelete);
		}
		if (currentOrder != null){
			currentOrder = IntegerArray.deletePartsFlagged(currentOrder, toDelete);
		}
		if (previousOrder != null){
			previousOrder = IntegerArray.deletePartsFlagged(previousOrder, toDelete);
		}

		//figuring out how many deleted total to adjust numParts
		numParts = numParts-toDelete.numBitsOn();

		incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
		return true;
	}	
	/*-------------------------------------------------------*/
	/** Deletes parts by blocks.
	 * blocks[i][0] is start of block; blocks[i][1] is end of block
	 * Assumes that these blocks are in sequence, non-overlapping, etc!!! *
	protected boolean deletePartsBy Blocks(int[][] blocks){ 

		if (bits!=null) {
			for (int i=0; i< bits.size(); i++) {
				Bits b = (Bits)bits.elementAt(i);
				b.deletePartsBy Blocks(blocks);
			}
		}
		if (longs!=null) {
			for (int i=0; i< longs.size(); i++) {
				LongArray b = (LongArray)longs.elementAt(i);
				b.deletePartsBy Blocks(blocks);
			}
		}
		if (doubles!=null)
			for (int i=0; i< doubles.size(); i++) {
				DoubleArray b = (DoubleArray)doubles.elementAt(i);
				b.deletePartsBy Blocks(blocks);
			}
		if (objects!=null)
			for (int i=0; i< objects.size(); i++) {
				ObjectArray b = (ObjectArray)objects.elementAt(i);
				b.deletePartsBy Blocks(blocks);
			}
		if (defaultOrder != null){
			defaultOrder = IntegerArray.deletePartsBy Blocks(defaultOrder, blocks);
		}
		if (currentOrder != null){
			currentOrder = IntegerArray.deletePartsBy Blocks(currentOrder, blocks);
		}
		if (previousOrder != null){
			previousOrder = IntegerArray.deletePartsBy Blocks(previousOrder, blocks);
		}

		//figuring out how many deleted total to adjust numParts
		int shift = 0;
		for (int block = 0; block<blocks.length; block++) 
			shift += blocks[block][1]-blocks[block][0]+1;		
		numParts = numParts-shift;

		incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
		return true;
	}
	/*-----------------------------------------*/
	public boolean moveParts(int starting, int num, int justAfter){
		if (num==0)
			return false;
		if (starting>numParts || starting<0) 
			return false;
		if ((justAfter>=starting && justAfter<=starting+num-1))
			return false;

		if (bits!=null) {
			for (int i=0; i< bits.size(); i++) {
				Bits b = (Bits)bits.elementAt(i);
				b.moveParts(starting, num, justAfter);
			}
		}
		if (longs!=null) {
			for (int i=0; i< longs.size(); i++) {
				LongArray b = (LongArray)longs.elementAt(i);
				b.moveParts(starting, num, justAfter);
			}
		}
		if (doubles!=null)
			for (int i=0; i< doubles.size(); i++) {
				DoubleArray b = (DoubleArray)doubles.elementAt(i);
				b.moveParts(starting, num, justAfter);
			}
		if (strings!=null)
			for (int i=0; i< strings.size(); i++) {
				StringArray b = (StringArray)strings.elementAt(i);
				b.moveParts(starting, num, justAfter);
			}
		if (objects!=null)
			for (int i=0; i< objects.size(); i++) {
				ObjectArray b = (ObjectArray)objects.elementAt(i);
				b.moveParts(starting, num, justAfter); //comments handled here
			}
		if (defaultOrder != null){
			defaultOrder = IntegerArray.moveParts(defaultOrder, starting, num, justAfter);
		}
		if (currentOrder != null){
			currentOrder = IntegerArray.moveParts(currentOrder, starting, num, justAfter);
		}
		if (previousOrder != null){
			previousOrder = IntegerArray.moveParts(previousOrder, starting, num, justAfter);
		}


		incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
		return true;
	}
	public void restoreToPreviousOrder(){
		if (previousOrder == null)
			return;
		for (int current = 0; current<numParts; current++) {
			int i = 0;
			boolean found = false;
			while (i<numParts && !found) {
				if (previousOrder[i] == current) {// we've found the next one
					moveParts(i,1,current-1);
					found = true;
				}
				i++;
			}
		}
	}


	MesquiteTimer t3 = new MesquiteTimer();
	MesquiteTimer t4 = new MesquiteTimer();
	MesquiteTimer t5 = new MesquiteTimer();
	MesquiteTimer t6 = new MesquiteTimer();

	/*-----------------------------------------*
	public boolean swapParts(int first, int second){
		return swapParts(first, second, true);
	}
	 */
	public boolean swapParts(int first, int second, boolean notify){
		if (first>numParts || first<0) 
			return false;
		if (second>numParts || second<0) 
			return false;
		if (bits!=null) {
			for (int i=0; i< bits.size(); i++) {
				Bits b = (Bits)bits.elementAt(i);
				b.swapParts(first, second);
			}
		}
		if (longs!=null) {
			for (int i=0; i< longs.size(); i++) {
				LongArray b = (LongArray)longs.elementAt(i);
				b.swapParts(first, second);
			}
		}
		if (doubles!=null)
			for (int i=0; i< doubles.size(); i++) {
				DoubleArray b = (DoubleArray)doubles.elementAt(i);
				b.swapParts(first, second);
			}
		if (strings!=null)
			for (int i=0; i< strings.size(); i++) {
				StringArray b = (StringArray)strings.elementAt(i);
				b.swapParts(first, second);
			}
		if (objects!=null)
			for (int i=0; i< objects.size(); i++) {
				ObjectArray b = (ObjectArray)objects.elementAt(i);
				b.swapParts(first, second); //comments handled here
			}
		if (defaultOrder != null){
			IntegerArray.swapParts(defaultOrder, first, second);
		}
		if (currentOrder != null){
			IntegerArray.swapParts(currentOrder, first, second);
		}
		if (previousOrder != null){
			IntegerArray.swapParts(previousOrder, first, second);
		}

		incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
		return true;
	}
	/* ---------------------Default Order -----------------------*/
	public int getDefaultPosition(int part){
		if (!inBounds(part) || defaultOrder == null)
			return -1;
		return defaultOrder[part];
	}
	public void setDefaultPosition(int part, int position){
		if (!inBounds(part) || defaultOrder == null)
			return;
		defaultOrder[part] = position;
	}
	public void resetDefaultOrderToCurrent(){
		if (defaultOrder != null){
			for (int i=0; i<numParts; i++)
				defaultOrder[i] = i;
		}
	}

	private boolean orderMonotonic(){
		if (defaultOrder ==null)
			return true;
		for (int i=1; i<defaultOrder.length; i++){
			if (defaultOrder[i] <= defaultOrder[i-1])
				return false;
		}
		return true;
	}

	private String getDefaultOrderNEXUS(){
		StringBuffer sb = new StringBuffer(50);
		for (int i = 0; i< defaultOrder.length; i++) {
			sb.append(' ');
			sb.append(Integer.toString(defaultOrder[i]));
		}
		return sb.toString();
	}


	/* ---------------------COMMENTS -----------------------*/
	/*-----------------------------------------*/
	/** Sets the comment for a part */
	public void setAnnotation(int part, String comment) {
		if (!inBounds(part))
			return;
		if (comments==null) {
			NameReference sN = makeAssociatedObjects("comments");
			comments = getAssociatedObjects(sN);
		}
		comments.setValue(part, comment);
		setDirty(true);
	}
	/*-----------------------------------------*/
	/** Returns whether the part is selected */
	public String getAnnotation(int part) {
		if (!inBounds(part))
			return null;
		if (comments==null)
			return null;
		return (String)comments.getValue(part);
	}
	/*-----------------------------------------*/
	/** Returns whether there are any selected parts *
	public boolean anyComments() {
		if (comments!=null)
			return comments.anyBitsOn();
		else
			return false;
	}
	/* ---------------------SELECTION -----------------------*/
	/** Returns number of parts that can be selected*/
	public int getNumberOfSelectableParts(){
		return getNumberOfParts();
	}
	/*-----------------------------------------*/
	/** Sets whether or not the part is selected */
	public void setSelected(int part, boolean select) {
		if (!inBounds(part))
			return;
		if (selected==null) {
			NameReference sN = makeAssociatedBits("selected");
			selected = getAssociatedBits(sN);
		}
		if (select)
			selected.setBit(part);
		else
			selected.clearBit(part);
		setDirty(true);
	}
	/*-----------------------------------------*/
	/** Sets whether or not the part is selected */
	public void setSelected(int partStart, int partEnd, boolean select) {
		for (int i=partStart; i<=partEnd; i++)
			setSelected(i,select);
	}
	/*-----------------------------------------*/
	/** Sets whether or not the parts specified by the incoming Bits is selected */
	public void setSelected(Bits partBits, boolean select) {
		for (int i=0;  i<partBits.getSize(); i++) // this is depending upon setSelected to check if it is within bounds
			if (partBits.isBitOn(i))
				setSelected(i,select);
	}
	/*-----------------------------------------*/
	/** Returns whether the part is selected */
	public boolean getSelected(int part) {
		if (!inBounds(part))
			return false;
		if (selected==null)
			return false;
		return selected.isBitOn(part);
	}
	/*-----------------------------------------*/
	/** Returns whether the part is selected */
	public boolean isSelected(int part) {
		return getSelected(part);
	}
	/*-----------------------------------------*/
	/** Deselects all parts */
	public void deselectAll(){
		if (selected!=null)
			selected.clearAllBits();
		setDirty(true);
	}
	/*-----------------------------------------*/
	/** Selects all parts */
	public void selectAll(){
		if (selected==null) {
			NameReference sN = makeAssociatedBits("selected");
			selected = getAssociatedBits(sN);
		}
		if (selected!=null)
			selected.setAllBits();
		setDirty(true);
	}
	/*-----------------------------------------*/
	/** Returns index of first selected part */
	public int firstSelected() {
		if (!anySelected())
			return -1;
		for (int i = 0; i<getNumberOfParts(); i++) {
			if (selected.isBitOn(i))
				return i;
		}
		return -1;
	}
	/*-----------------------------------------*/
	/** Returns index of i'th selected part if any are selected, otherwise returns i.
	 * If there are not i parts selected (but some are), numParts is returned */
	public int selectedIndexToPartIndex(int i) {
		if (!anySelected())
			return i;
		int count = 0;
		for (int k = 0;  k<getNumberOfParts(); k++) {
			if (selected.isBitOn(k)){
				if (count == i)
					return k;
				count++;
			}
		}
		return getNumberOfParts();
	}
	/*-----------------------------------------*/
	/** Returns index of part that is the i'th selected, if any are selected, otherwise the i'th part.
	 * if i is not selected, then returns the previous selected index
	 *  If there are not i selected parts, numParts is returned */
	public int partIndexToSelectedIndex(int i) {
		if (!anySelected())
			return i;
		int count = 0;
		for (int k = 0;  k<getNumberOfParts(); k++) {
			if (i == k)
				return count;
			if (selected.isBitOn(k))
				count++;
		}
		return getNumberOfParts();
	}
	/*-----------------------------------------*/
	/** Returns index of next selected part if any are selected, otherwise the next part.
	 * If there are no remaining parts, numParts is returned */
	public int nextSelectedIfAny(int previous) {
		if (!anySelected())
			return previous +1;
		
		for (int i = previous+1; i<getNumberOfParts(); i++) {
			if (selected.isBitOn(i))
				return i;
		}
		return getNumberOfParts();
	}
	/*-----------------------------------------*/
	/** Returns index of next selected part if any are selected, otherwise the next part.
	 * If there are no remaining parts, numParts is returned */
	public int nextPart(int previous, boolean selectedOnly) {
		if (!selectedOnly || !anySelected())
			return previous +1;
		
		for (int i = previous+1; i<getNumberOfParts(); i++) {
			if (selected.isBitOn(i))
				return i;
		}
		return getNumberOfParts();
	}
	/*-----------------------------------------*/
	/** Returns index of last selected part */
	public int lastSelected() {
		if (!anySelected())
			return -1;
		for (int i = getNumberOfParts()-1; i>=0; i--) {
			if (selected.isBitOn(i)) {
				return i;

			}
		}
		return -1;
	}
	/*-----------------------------------------*/
	/** Returns selected directly */
	public Bits getSelectedBits() {
		return selected;
	}

	/*-----------------------------------------*/
	/** Returns whether there are selected parts that form a contiguous selection */
	public boolean contiguousSelection() {
		if (!anySelected())
			return false;
		int firstSelected = selected.nextBit(0, true);
		if (firstSelected<0)  // none is selected
			return false;
		int nextNotSelected = selected.nextBit(firstSelected+1,false);  // first one, after the block, that is not selected
		if (nextNotSelected>0) {
			return (selected.nextBit(nextNotSelected+1,true)==-1);  //no more have been found
		}
		return true;
	}
	/*-----------------------------------------*/
	/** Returns whether there are any selected parts */
	public boolean anySelected() {
		if (selected!=null)
			return selected.anyBitsOn();
		else
			return false;
	}
	/*-----------------------------------------*/
	/** Returns number of selected parts */
	public int numberSelected() {
		if (selected!=null)
			return selected.numBitsOn();
		else
			return 0;
	}
	/*-----------------------------------------*/
	/** returns number of parts that are selected or, if onlyCountSelected is false, all parts*/
	public int numberSelected(boolean onlyCountSelected) {
		if (onlyCountSelected)
			return numberSelected();
		else
			return getNumberOfParts();
	}
	/* ---------------------BITS------------------------*/
	public int getNumberAssociatedBits(){
		if (bits==null)
			return 0;
		else
			return bits.size();
	}
	public Bits getAssociatedBits(int index){
		if (bits!=null && index>=0 && index<bits.size()) {
			return (Bits)bits.elementAt(index);
		}
		return null;
	}
	public NameReference makeAssociatedBits(String name){
		NameReference nr = NameReference.getNameReference(name);
		Bits b = (getAssociatedBits(nr));
		if (b==null) {
			b = new Bits(numParts);
			b.setNameReference(nr);
			b.setBetweenness(PropertyRecord.preferredBetweenness(name));
			bits.addElement(b);
		}
		incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
		return nr;
	}
	public void removeAssociatedBits(NameReference nRef){
		boolean found = false;
		if (bits!=null && nRef!=null) {
			for (int i=0; i<bits.size() && !found; i++) {
				Bits b = (Bits)bits.elementAt(i);
				if (b !=null && nRef.equals(b.getNameReference())) {
					bits.removeElement(b);
					found = true;
				}
			}
		}
		incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
	}
	public Bits getAssociatedBits(NameReference nRef){
		if (bits!=null && nRef!=null) {
			for (int i=0; i<bits.size(); i++) {
				Bits b = (Bits)bits.elementAt(i);
				if (b !=null && nRef.equals(b.getNameReference())) {
					return b; 
				}
			}
		}
		return null;
	}
	public void clearAllAssociatedBits(NameReference nRef){
		boolean found = false;
		if (bits!=null && nRef!=null) {
			for (int i=0; i<bits.size() && !found; i++) {
				Bits b = (Bits)bits.elementAt(i);
				if (b !=null && nRef.equals(b.getNameReference())) {
					b.clearAllBits(); 
					found = true;
				}
			}
		}
		incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
	}
	public void setAssociatedBit(NameReference nRef, int index, boolean value){
		if (bits!=null && nRef!=null) {
			for (int i=0; i<bits.size(); i++) {
				Bits b = (Bits)bits.elementAt(i);
				if (b !=null && nRef.equals(b.getNameReference())) {
					if (value)
						b.setBit(index); 
					else
						b.clearBit(index);
					incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
					return;
				}
			}
			makeAssociatedBits(nRef.getValue());
			Bits b = getAssociatedBits(nRef);
			if (b==null)
				return;
			if (value)
				b.setBit(index);
			else
				b.clearBit(index);
			incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
		}
	}
	public boolean getAssociatedBit(NameReference nRef, int index){
		if (bits==null || nRef==null) {
			return false;
		}
		for (int i=0; i<bits.size(); i++) {
			Bits b = (Bits)bits.elementAt(i);
			if (b !=null && nRef.equals(b.getNameReference())) {
				return b.isBitOn(index); 
			}
		}
		return false;
	}
	/* ---------------------LONG------------------------*/
	public int getNumberAssociatedLongs(){
		if (longs==null)
			return 0;
		else
			return longs.size();
	}
	public LongArray getAssociatedLongs(int index){
		if (longs!=null && index>=0 && index<longs.size()) {
			return (LongArray)longs.elementAt(index);
		}
		return null;
	}
	public NameReference makeAssociatedLongs(String name){
		NameReference nr = NameReference.getNameReference(name);
		LongArray d = getAssociatedLongs(nr);
		if (d==null){
			d = new LongArray(numParts);
			d.setNameReference(nr);
			d.setBetweenness(PropertyRecord.preferredBetweenness(name));
			longs.addElement(d);
		}
		incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
		return nr;
	}
	public void removeAssociatedLongs(NameReference nRef){
		boolean found = false;
		if (longs!=null && nRef!=null) {
			for (int i=0; i<longs.size() && !found; i++) {
				LongArray b = (LongArray)longs.elementAt(i);
				if (b !=null && nRef.equals(b.getNameReference())) {
					longs.removeElement(b);
					found = true;
				}
			}
		}
		incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
	}
	public LongArray getAssociatedLongs(NameReference nRef){
		if (longs!=null && nRef!=null) {
			for (int i=0; i<longs.size(); i++) {
				LongArray b = (LongArray)longs.elementAt(i);
				if (b !=null && nRef.equals(b.getNameReference())) {
					return b; 
				}
			}
		}
		return null;
	}
	public void deassignAllAssociatedLongs(NameReference nRef){
		boolean found = false;
		if (longs!=null && nRef!=null) {
			for (int i=0; i<longs.size() && !found; i++) {
				LongArray b = (LongArray)longs.elementAt(i);
				if (b !=null && nRef.equals(b.getNameReference())) {
					b.deassignArray(); 
					found = true;
				}
			}
		}
		incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
	}
	public void zeroAllAssociatedLongs(NameReference nRef){
		boolean found = false;
		if (longs!=null && nRef!=null) {
			for (int i=0; i<longs.size() && !found; i++) {
				LongArray b = (LongArray)longs.elementAt(i);
				if (b !=null && nRef.equals(b.getNameReference())) {
					b.zeroArray(); 
					found = true;
				}
			}
		}
		incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
	}
	public void setAssociatedLong(NameReference nRef, int index, long value){
		if (longs!=null && nRef!=null) {
			for (int i=0; i<longs.size(); i++) {
				LongArray b = (LongArray)longs.elementAt(i);
				if (b !=null && nRef.equals(b.getNameReference())) {
					b.setValue(index, value); 
					incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
					return;
				}
			}
			makeAssociatedLongs(nRef.getValue());
			LongArray b = getAssociatedLongs(nRef);
			if (b==null)
				return;
			b.setValue(index, value);
			incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
		}
	}
	public long getAssociatedLong(NameReference nRef, int index){
		if (longs==null || nRef==null)
			return MesquiteLong.unassigned;
		for (int i=0; i<longs.size(); i++) {
			LongArray b = (LongArray)longs.elementAt(i);
			if (b !=null && nRef.equals(b.getNameReference())) {
				return b.getValue(index); 
			}
		}
		return MesquiteLong.unassigned;
	}
	/* ---------------------DOUBLE------------------------*/
	public int getNumberAssociatedDoubles(){
		if (doubles==null)
			return 0;
		else
			return doubles.size();
	}
	public DoubleArray getAssociatedDoubles(int index){
		if (doubles!=null && index>=0 && index<doubles.size()) {
			return (DoubleArray)doubles.elementAt(index);
		}
		return null;
	}
	public NameReference makeAssociatedDoubles(String name){
		NameReference nr = NameReference.getNameReference(name);
		DoubleArray d = getAssociatedDoubles(nr);
		if (d==null){
			d = new DoubleArray(numParts);
			d.setNameReference(nr);
			d.setBetweenness(PropertyRecord.preferredBetweenness(name));
			doubles.addElement(d);
		}
		incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
		return nr;
	}
	public void removeAssociatedDoubles(NameReference nRef){
		boolean found = false;
		if (doubles!=null && nRef!=null) {
			for (int i=0; i<doubles.size() && !found; i++) {
				DoubleArray b = (DoubleArray)doubles.elementAt(i);
				if (b !=null && nRef.equals(b.getNameReference())) {
					doubles.removeElement(b);
					found = true;
				}
			}
		}
		incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
	}
	public DoubleArray getAssociatedDoubles(NameReference nRef){
		if (doubles!=null && nRef!=null) {
			for (int i=0; i<doubles.size(); i++) {
				DoubleArray b = (DoubleArray)doubles.elementAt(i);
				if (b !=null && nRef.equals(b.getNameReference())) {
					return b; 
				}
			}
		}
		return null;
	}
	public void zeroAllAssociatedDoubles(NameReference nRef){
		boolean found = false;
		if (doubles!=null && nRef!=null) {
			for (int i=0; i<doubles.size() && !found; i++) {
				DoubleArray b = (DoubleArray)doubles.elementAt(i);
				if (b !=null && nRef.equals(b.getNameReference())) {
					b.zeroArray(); 
					found = true;
				}
			}
		}
		incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
	}
	public void setAssociatedDouble(NameReference nRef, int index, double value){
		if (doubles!=null && nRef!=null) {
			for (int i=0; i<doubles.size(); i++) {
				DoubleArray b = (DoubleArray)doubles.elementAt(i);
				if (b !=null && nRef.equals(b.getNameReference())) {
					b.setValue(index, value); 
					incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
					return;
				}
			}
			makeAssociatedDoubles(nRef.getValue());
			DoubleArray b = getAssociatedDoubles(nRef);
			if (b==null)
				return;
			b.setValue(index, value);
			incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
		}
	}
	public double getAssociatedDouble(NameReference nRef, int index){
		if (doubles==null || nRef==null)
			return MesquiteDouble.unassigned;
		for (int i=0; i<doubles.size(); i++) {
			DoubleArray b = (DoubleArray)doubles.elementAt(i);
			if (b !=null && nRef.equals(b.getNameReference())) {
				return b.getValue(index); 
			}
		}
		return MesquiteDouble.unassigned;
	}
	/* ---------------------STRING------------------------*/
	public int getNumberAssociatedStrings(){
		if (strings==null)
			return 0;
		else
			return strings.size();
	}
	public StringArray getAssociatedStrings(int index){
		if (strings!=null && index>=0 && index<strings.size()) {
			return (StringArray)strings.elementAt(index);
		}
		return null;
	}
	public NameReference makeAssociatedStrings(String name){
		NameReference nr = NameReference.getNameReference(name);
		StringArray d = getAssociatedStrings(nr);
		if (d==null){
			d = new StringArray(numParts);
			d.setNameReference(nr);
			d.setBetweenness(PropertyRecord.preferredBetweenness(name));
			strings.addElement(d);
		}
		incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
		return nr;
	}
	public void removeAssociatedStrings(NameReference nRef){
		boolean found = false;
		if (strings!=null && nRef!=null) {
			for (int i=0; i<strings.size() && !found; i++) {
				StringArray b = (StringArray)strings.elementAt(i);
				if (b !=null && nRef.equals(b.getNameReference())) {
					strings.removeElement(b);
					found = true;
				}
			}
		}
		incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
	}
	public StringArray getAssociatedStrings(NameReference nRef){
		if (strings!=null && nRef!=null) {
			for (int i=0; i<strings.size(); i++) {
				StringArray b = (StringArray)strings.elementAt(i);
				if (b !=null && nRef.equals(b.getNameReference())) {
					return b; 
				}
			}
		}
		return null;
	}
	public void deassignAllAssociatedStrings(NameReference nRef){
		boolean found = false;
		if (strings!=null && nRef!=null) {
			for (int i=0; i<strings.size() && !found; i++) {
				StringArray b = (StringArray)strings.elementAt(i);
				if (b !=null && nRef.equals(b.getNameReference())) {
					b.deassignArray(); 
					found = true;
				}
			}
		}
		incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
	}

	public void setAssociatedString(NameReference nRef, int index, String value){
		if (strings!=null && nRef!=null) {
			for (int i=0; i<strings.size(); i++) {
				StringArray b = (StringArray)strings.elementAt(i);
				if (b !=null && nRef.equals(b.getNameReference())) {
					b.setValue(index, value); 
					incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
					return;
				}
			}
			makeAssociatedStrings(nRef.getValue());
			StringArray b = getAssociatedStrings(nRef);
			if (b==null)
				return;
			b.setValue(index, value);
			incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
		}
	}
	public String getAssociatedString(NameReference nRef, int index){
		if (nRef==null)
			return null;
		if (strings != null)
			for (int i=0; i<strings.size(); i++) {
				StringArray b = (StringArray)strings.elementAt(i);
				if (b !=null && nRef.equals(b.getNameReference())) {
					return b.getValue(index); 
				}
			}

		//not found among strings. If a string had previously been placed as an object, need to check there. It would have already been warned about!
		if (assocStringObjectWarned){
			for (int i=0; i<objects.size(); i++) {
				ObjectArray b = (ObjectArray)objects.elementAt(i);
				if (b !=null && nRef.equals(b.getNameReference())) {
					Object bS = b.getValue(index); 
					if (bS instanceof String)
						return (String)bS;
					else
						return null;
				}
			}
		}


		return null;
	}
	/** Returns true iff there is at least one associate string of type nRef */
	public boolean anyAssociatedString(NameReference nRef){
		if (strings==null || nRef==null)
			return false;
		for (int i=0; i<strings.size(); i++) {
			StringArray b = (StringArray)strings.elementAt(i);
			if (b !=null && nRef.equals(b.getNameReference())) {
				return true; 
			}
		}
		return false;
	}

	/* ---------------------OBJECT------------------------*/
	public int getNumberAssociatedObjects(){
		if (objects==null)
			return 0;
		else
			return objects.size();
	}
	public ObjectArray getAssociatedObjects(int index){
		if (objects!=null && index>=0 && index<objects.size()) {
			return (ObjectArray)objects.elementAt(index);
		}
		return null;
	}
	public NameReference makeAssociatedObjects(String name){
		NameReference nr = NameReference.getNameReference(name);
		ObjectArray d = getAssociatedObjects(nr);
		if (d==null){
			d = new ObjectArray(numParts);
			d.setNameReference(nr);
			d.setBetweenness(PropertyRecord.preferredBetweenness(name));
			objects.addElement(d);
			incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
		}
		return nr;
	}
	public void removeAssociatedObjects(NameReference nRef){
		boolean found = false;
		if (objects!=null && nRef!=null) {
			for (int i=0; i<objects.size() && !found; i++) {
				ObjectArray b = (ObjectArray)objects.elementAt(i);
				if (b !=null && nRef.equals(b.getNameReference())) {
					objects.removeElement(b);
					found = true;
				}
			}
		}
		incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
	}
	public ObjectArray getAssociatedObjects(NameReference nRef){
		if (objects!=null && nRef!=null) {
			for (int i=0; i<objects.size(); i++) {
				ObjectArray b = (ObjectArray)objects.elementAt(i);
				if (b !=null && nRef.equals(b.getNameReference())) {
					return b; 
				}
			}
		}
		return null;
	}

	public void zeroAllAssociatedObjects(NameReference nRef){
		boolean found = false;
		if (objects!=null && nRef!=null) {
			for (int i=0; i<objects.size() && !found; i++) {
				ObjectArray b = (ObjectArray)objects.elementAt(i);
				if (b !=null && nRef.equals(b.getNameReference())) {
					b.zeroArray(); 
					found = true;
				}
			}
		}
		incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
	}

	static boolean assocStringObjectWarned = false;
	boolean assocStringObjectFound = false;
	
	public void setAssociatedObject(NameReference nRef, int index, Object value){
		if (value instanceof String){
			assocStringObjectFound = true;
			if (MesquiteTrunk.developmentMode && !assocStringObjectWarned){
				MesquiteMessage.println("String saved in Associable as object (a); Associable: " + getClass() + ". It will be saved instead as a string.");
				MesquiteMessage.printStackTrace("");
				assocStringObjectWarned = true;
			}
			setAssociatedString(nRef, index, (String)value);
			return;
		}

		if (value instanceof String && value != null && ((String)value).equals(""))  // a filter so a blank string is not saved
			value = null;
		if (objects!=null && nRef!=null) {
			for (int i=0; i<objects.size(); i++) {
				ObjectArray b = (ObjectArray)objects.elementAt(i);
				if (b !=null && nRef.equals(b.getNameReference())) {
					b.setValue(index, value); 
					incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
					return;
				}
			}
			makeAssociatedObjects(nRef.getValue());
			ObjectArray b = getAssociatedObjects(nRef);
			if (b==null)
				return;
			b.setValue(index, value); 
			incrementVersion(MesquiteListener.ASSOCIATED_CHANGED, false);
		}
	}
	public Object getAssociatedObject(NameReference nRef, int index){
		if (nRef==null)
			return null;
		if (objects != null)
			for (int i=0; i<objects.size(); i++) {
			ObjectArray b = (ObjectArray)objects.elementAt(i);
			if (b !=null && nRef.equals(b.getNameReference())) {
				if (b.getValue(index) instanceof String && MesquiteTrunk.developmentMode && !assocStringObjectWarned){
					MesquiteMessage.println("String found saved in Associable as object (b); Associable: " + getClass() + ".");
					MesquiteMessage.printStackTrace("");
					assocStringObjectWarned = true;
				}
				return b.getValue(index); 
			}
		}
		//Not found. Checking Strings in case it's an old style request
		if (strings != null)
			for (int i=0; i<strings.size(); i++) {
				StringArray b = (StringArray)strings.elementAt(i);
			if (b !=null && nRef.equals(b.getNameReference())) {
				if (MesquiteTrunk.developmentMode && !assocStringObjectWarned){
					MesquiteMessage.println("String found saved in Associable as object (c); Associable: " + getClass() + ".");
					MesquiteMessage.printStackTrace("");
					assocStringObjectWarned = true;
				}
				return b.getValue(index); 
			}
		}

		return null;
	}

	/** Returns true iff there is at least one associate object of type nRef */
	public boolean anyAssociatedObject(NameReference nRef){
		if (objects==null || nRef==null)
			return false;
		for (int i=0; i<objects.size(); i++) {
			ObjectArray b = (ObjectArray)objects.elementAt(i);
			if (b !=null && nRef.equals(b.getNameReference())) {
				return true; 
			}
		}
		return false;
	}
/* Disabled until this can be worked via settings also
	public void setAssociatedBitsBetweenness(NameReference nref, boolean between){
		Bits b = getAssociatedBits(nref);
		if (b != null)
			b.setBetweenness(between);
	}
	public void setAssociatedLongBetweenness(NameReference nref, boolean between){
		LongArray b = getAssociatedLongs(nref);
		if (b != null)
			b.setBetweenness(between);
	}
	public void setAssociatedDoubleBetweenness(NameReference nref, boolean between){
		DoubleArray b = getAssociatedDoubles(nref);
		if (b != null)
			b.setBetweenness(between);
	}
	public void setAssociatedStringBetweenness(NameReference nref, boolean between){
		StringArray b = getAssociatedStrings(nref);
		if (b != null)
			b.setBetweenness(between);
	}
	public void setAssociatedObjectBetweenness(NameReference nref, boolean between){
		ObjectArray b = getAssociatedObjects(nref);
		if (b != null)
			b.setBetweenness(between);
	}
*/
}




