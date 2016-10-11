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

import java.text.*;
import java.util.*;

import mesquite.lib.characters.CharacterStates;
import mesquite.lib.characters.CodonPositionsSet;

/* ======================================================================== */
/*Vector of listable objects along with a simple, currently stupid system to display the list*/
public class ListableVector extends FileElement implements StringLister, Commandable, MesquiteListener {
	Vector vec;
	protected boolean notifyOfChanges = true;
	public static int totalCreated = 0;
	public static int totalDisposed = 0;
	public static int totalFinalized = 0;
	public static long totalElementsAdded = 0;
	MesquiteListWindow listWindow;
	public ListableVector() {
		super(0);
		totalCreated++;
		vec = new Vector();
	}
	public ListableVector(int initCapacity) {
		super(0);
		totalCreated++;
		vec = new Vector(initCapacity);
	}
	public String toHTMLStringDescription(){
		if (size() == 0)
			return "";
		String sH = "";
		
		for (int i=0; i<size(); i++){
			Listable e = elementAt(i);
			

			if (e instanceof HTMLDescribable){
				String sT =  ((HTMLDescribable)e).toHTMLStringDescription();
				if (!StringUtil.blank(sT))
					sH += sT;
				else
					sH += "<li>" + e.getName() + "</li>";
			}
			else 
				sH += "<li>" + e.getName() + "</li>";

				
		}
	//	if (StringUtil.blank(s))
	//		return "";
		return "<li>" + getTypeName() + " (" + size() + " units)<ul>" + sH + "</ul>" + super.toHTMLStringDescription() + "</li>";
	}
	boolean wasDisposed = false;
	public void dispose(boolean disposeElements){
		

		wasDisposed = true;
		if (disposeElements){
			for (int i = 0; i<size(); i++){
				Listable listable = elementAt(i);
				if (listable instanceof Disposable)
					((Disposable)listable).dispose();
		}
		}
		removeAllListeners();
		removeAllElements(false);
		if ((getClass() == TreeVector.class) || (getClass() == SpecsSetVector.class) || (getClass() == TaxaGroupVector.class))
			//||  (getClass() == CharactersGroupVector.class) && (getClass() != ModelVector.class))
			totalDisposed++;
		super.dispose();
	}
	public void dispose(){
		dispose(false);
	}
	public void finalize() throws Throwable {
		totalFinalized++;
		super.finalize();
	}
	
	public String getTypeName(){
		return "Listable Vector";
	}
	public Listable getElement(String name){
		if (name==null)
			return null;
		for (int i=0; i<size(); i++) {
			Listable obj = elementAt(i);
			if (name.equals(obj.getName()))
				return obj;
		}
		return null;
	}
	
	public Vector getElements(String name){
		if (name==null)
			return null;
		Vector list = new Vector();
		for (int i=0; i<size(); i++) {
			Listable obj = elementAt(i);
			if (name.equals(obj.getName()))
				list.addElement(obj);
		}
		return list;
	}
	public Listable getElementIgnoreCase(String name){
		if (name==null)
			return null;
		for (int i=0; i<size(); i++) {
			Listable obj = elementAt(i);
			if (name.equalsIgnoreCase(obj.getName()))
				return obj;
		}
		return null;
	}
	public String[] getStringArrayList(){
		String[] temp = new String[size()];
		for (int i=0; i<size(); i++) {
			Object obj = elementAt(i);
			if (obj instanceof SpecialListName)
				temp[i]= ((SpecialListName)obj).getListName();
			else
				temp[i]= ((Listable)obj).getName();
		}
		return temp;
	}
	public String getList(){
		String temp = "";
		for (int i=0; i<size(); i++) {
			Object obj = elementAt(i);
			if (obj instanceof SpecialListName)
				temp += ((SpecialListName)obj).getListName() + StringUtil.lineEnding();
			else
				temp += ((Listable)obj).getName() + StringUtil.lineEnding();
		}
		return temp;
	}
	public String nameOfElementAt(int i){
		Object obj = elementAt(i);
		if (obj == null)
			return null;
		if (obj instanceof SpecialListName)
			return ((SpecialListName)obj).getListName();
		else
			return ((Listable)obj).getName();
	}
	public Listable[] getElementArray(){
		Listable[] temp =  new Listable[size()];
		for (int i=0; i<size(); i++)
			temp[i] = (Listable)elementAt(i);
		return temp;
	}
	public String getUniqueName(String base){
		return getUniqueName(base, "");
	}
	public String getUniqueName(String base, String separator){
		if (size()==0)
			return base;
		else {
			int count =1;
			while (true){
				
				String candidate;
				if (count==1)
					candidate = base;
				else
					candidate = base + separator + count;
				if (getElementIgnoreCase(candidate)==null)
					return candidate;
				count++;
			}
		}
	}
	public boolean uniqueName(String name){
		if (size()==0 || name==null)
			return false;
		else {
			int count=0;
			for (int i=0; i<size(); i++)
				if (name.equals(((Listable)elementAt(i)).getName())){
					count++;
					if (count>1)
						return false;
				}
			return count==1;
		}
	}
	public boolean nameAlreadyInList(String name, int dontCountItem){
		if (size()==0 || name==null)
			return false;
		else {
			for (int i=0; i<size(); i++)
				if (i!=dontCountItem && name.equals(((Listable)elementAt(i)).getName())){
					return true;
				}
			return false;
		}
	}
	public boolean nameAlreadyInList(String name){
		return nameAlreadyInList (name, -1);
	}
	public boolean allNamesUnique(){
		if (size()==0)
			return true;
		else {
			String s;
			Listable listable;
			for (int i=0; i<size(); i++) {
				s = ((Listable)elementAt(i)).getName();
				if (s!=null)
					for (int j=i+1; j<size(); j++) {
						listable = (Listable)elementAt(j);
						if (listable!=null)
							if (s.equals(listable.getName()))
								return false;
					}
			}
			return true;
		}
	}
	public static String[] getStrings(Listable[] objects){
		if (objects==null)
			return null;
		else {
			String[] s = new String[objects.length];
			for (int i=0; i<objects.length; i++) {
				if ( objects[i]!=null)
					s[i] = objects[i].getName();
			}
			return s;
		}
	}
	public Listable[] getListables(){
		Listable[] temp = new Listable[size()];
		for (int i=0; i<size(); i++)
			temp[i]= (Listable)elementAt(i);
		return temp;
	}
	public String[] getStrings(){
		String[] temp = new String[size()];
		for (int i=0; i<size(); i++)
			temp[i]= ((Listable)elementAt(i)).getName();
		return temp;
	}
	public static String toString(Listable[] names){
		if (names==null)
			return null;
		String s = "[ ";
		for (int i=0; i<names.length; i++)
			if (names[i] !=null)
				s += " \"" + names[i].getName() + "\" ";
		return s + " ]";
	}
	/*-------------ListableVector----------------*/
	public void addElements(ListableVector objects, boolean notify) {
		if (objects == null)
			return;
		for (int i = 0; i< objects.size(); i++)
			addElement(objects.elementAt(i), false);
		numElements = vec.size();
		if (notify)
			notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
	}
	/*-------------ListableVector----------------*/
	private void addElement(Listable obj) {
		int code = 0;
		try {
			if (vec.indexOf(obj)>=0){
				if (MesquiteTrunk.debugMode)
					MesquiteMessage.println("adding object to ListableVector that is already present " + obj.getName() + " " + obj.getClass());
				return;
			}
			code = 0;
			totalElementsAdded++;
			addParts(size(), 1);
			code = 1;
			vec.addElement(obj);
			numElements = vec.size();
			code = 2;
		}
		catch(OutOfMemoryError e){
			MesquiteMessage.println("OutOfMemoryError in ListableVector addElement (code " + code + ").  See file memory.txt in the Mesquite_Folder.");
		}
	}
	/*-------------ListableVector----------------*/
	public void addElement(Listable obj, boolean notify) {
		if (obj==null) {
			MesquiteMessage.warnProgrammer("null object added to ListableVector " + this.getClass().toString());
			MesquiteMessage.printStackTrace();
			return;
		}
		addElement(obj);
		if (listWindow!=null)
			listWindow.table.setNumRows(listWindow.table.numRowsTotal+1);
		if (notify)
			notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
	}
	
	/*-------------ListableVector----------------*/
	public void insertElementAt(Listable obj, int index, boolean notify) {
		if (obj==null) {
			MesquiteMessage.warnProgrammer("null object inserted to ListableVector " + this.getClass().toString());
			MesquiteMessage.printStackTrace();
			return;
		}
		addParts(index-1, 1);
		vec.insertElementAt(obj, index);
		numElements = vec.size();
		if (listWindow!=null)
			listWindow.table.setNumRows(listWindow.table.numRowsTotal-1);
		if (notify)
			notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
	}
	/*-------------ListableVector----------------*/
	public void replaceElements(ListableVector newV, boolean notify) {
		if (newV!=null) {
			removeAllElements(false);
			//vec.setCapacity(newV.size());
			for (int i=0; i<newV.size(); i++)
				addElement((Listable)newV.elementAt(i));
			numElements = vec.size();
			if (listWindow!=null)
				listWindow.table.setNumRows(newV.size());
			if (notify)
				notifyListeners(this, new Notification(MesquiteListener.PARTS_CHANGED));
		}
	}
	/*-------------ListableVector----------------*/
	public void replaceElement(Listable old, Listable replacement, boolean notify) {
		if (replacement!=null && old !=null && indexOf(old)>=0) {
			int index = indexOf(old);
			removeElement(old, false);
			vec.insertElementAt(replacement, index);
			if (notify)
				notifyListeners(this, new Notification(MesquiteListener.PARTS_CHANGED));
		}
	}
	/*-------------ListableVector----------------*/
	public void removeElement(Listable obj, boolean notify) {
		if (obj==null)
			return;
		if (vec.indexOf(obj)>=0)
			deleteParts(vec.indexOf(obj), 1);
		vec.removeElement(obj);
		numElements = vec.size();
		if (vec.indexOf(obj)>=0)
			MesquiteMessage.warnProgrammer("object removed from listableVector but other copy remains " + this.getClass().toString() + "  " + obj);
		if (notify)
			notifyListeners(this, new Notification(MesquiteListener.PARTS_DELETED));
	}
	/*-------------ListableVector----------------*/
	public void removeElementAt(int i, boolean notify) {
		removeElement(elementAt(i), notify);
	}
	
	/*-------------ListableVector----------------*/
	public void removeAllElements(boolean notify){
		vec.removeAllElements();
		numElements = vec.size();
		if (notify)
			notifyListeners(this, new Notification(MesquiteListener.PARTS_DELETED));
	}
	/*...........................................................*/
	public boolean swapParts(int first, int second) {
		if (first<0 || first>=size() || second<0 || second>=size()) 
			return false;
		mesquite.lib.duties.ElementManager m = null;
		if (elementAt(first) instanceof FileElement)
			m = ((FileElement)elementAt(first)).getManager();
		
		Object objFirst = vec.elementAt(first);
		Object objSecond = vec.elementAt(second);
		vec.setElementAt(objSecond, first);
		vec.setElementAt(objFirst, second);
		if (m != null)
			m.elementsReordered(this);
		return super.swapParts(first, second);
		
	}
	/*...........................................................*/
	public boolean moveParts(int starting, int num, int justAfter) {
		if (num<=0 || starting>=size() || (justAfter>=starting && justAfter<=starting+num-1)) 
			return false;
		if (justAfter>=size())
			justAfter = size()-1;
		if (justAfter<0)
			justAfter = -1;
		mesquite.lib.duties.ElementManager m = null;
		if (elementAt(0) instanceof FileElement)
			m = ((FileElement)elementAt(0)).getManager();
		
		Vector newValues = new Vector(size());
		if (starting>justAfter){
			for (int i=0; i<=justAfter; i++)
				newValues.addElement(elementAt(i));
			
			for (int i=starting; i<=starting+num-1; i++)
				newValues.addElement(elementAt(i));
			for (int i=justAfter+1; i<=starting-1; i++)
				newValues.addElement(elementAt(i));
			for (int i=starting+num; i<size(); i++)
				newValues.addElement(elementAt(i));
		}
		else {
			for (int i=0; i<=starting-1; i++)
				newValues.addElement(elementAt(i));
			
			for (int i=starting+num; i<=justAfter; i++)
				newValues.addElement(elementAt(i));
			for (int i=starting; i<=starting+num-1; i++)
				newValues.addElement(elementAt(i));
			for (int i=justAfter+1; i<size(); i++)
				newValues.addElement(elementAt(i));
		}
		vec.removeAllElements();
		for (int i=0; i<newValues.size(); i++)
			vec.addElement(newValues.elementAt(i));
		numElements = vec.size();
		if (m != null)
			m.elementsReordered(this);
		boolean moved = super.moveParts(starting, num, justAfter);
		if (false)
			notifyListeners(this, new Notification(MesquiteListener.PARTS_MOVED));
		return moved;
	}
 	/*.................................................................................................................*/
	/** notifies listeners that element has been disposed*/
	public void notifyListenersOfDisposed(Object disp){ //{ ��� need in file element removeal to have all in one (dispose & remove) thjat calls this
		if (listeners!=null) {
			Enumeration e = listeners.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				MesquiteListener listener = (MesquiteListener)obj;
	 			listener.disposing(disp);
			}
		}
	}
	int numElements = 0;
	public int size() {
		if (vec != null)
			numElements =  vec.size();
		return numElements;
	}
	/** returns number of elements whose classes are subclasses of c */
	public int size(Class c) {
		if (c==null)
			return 0;
		int count =0;
		Enumeration e = vec.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (c.isAssignableFrom(obj.getClass()))
				count ++;
		}
		return count;
	}
	public Listable elementAt(int i) {
		if (i<0 || i>=size())
			return null;
		return (Listable)vec.elementAt(i);
	}
	public Listable elementWithName(String name){
		if (name==null)
			return null;
		for (int i=0; i<size(); i++) {
			Listable obj = elementAt(i);
			if (name.equals(obj.getName()))
				return obj;
		}
		return null;
	}
	public Enumeration elements() {
		return vec.elements();
	}
	public int indexOfClass(Class c){
		if (c==null)
			return -1;
		for (int i=0; i<size(); i++) {
			Object obj = vec.elementAt(i);
			if (obj != null && obj.getClass() == c)
				return i;
		}
		return -1;
	}
	public Listable[] elementsOfClass(Class c){
		if (c==null)
			return null;
		int count = 0;
		for (int i=0; i<size(); i++) {
			Object obj = vec.elementAt(i);
			if (obj != null && obj.getClass() == c)
				count++;
		}
		if (count == 0)
			return null;
		Listable[] list = new Listable[count];
		count = 0;
		for (int i=0; i<size(); i++) {
			Object obj = vec.elementAt(i);
			if (obj != null && obj.getClass() == c) {
				list[count]= (Listable)obj;
				count++;
			}
		}
		return list;
	}
	/*Returns any elements of interface OwnedByModule which claim to have module as owner */
	public Listable[] myElements(MesquiteModule module){
		if (module==null)
			return null;
		int count = 0;
		for (int i=0; i<size(); i++) {
			Object obj = vec.elementAt(i);
			if (obj != null && obj instanceof OwnedByModule && ((OwnedByModule)obj).getOwnerModule() == module)
				count++;
		}
		if (count == 0)
			return null;
		Listable[] list = new Listable[count];
		count = 0;
		for (int i=0; i<size(); i++) {
			Object obj = vec.elementAt(i);
			if (obj != null && obj instanceof OwnedByModule && ((OwnedByModule)obj).getOwnerModule() == module) {
				list[count]= (Listable)obj;
				count++;
			}
		}
		return list;
	}
	public int indexOf(Object obj){
		if (obj==null)
			return -1;
		return vec.indexOf(obj);
	}
	public static int indexOf(Listable[] list, Object obj){
		if (obj==null || list == null)
			return -1;
		for (int i=0; i<list.length; i++)
			if (obj == list[i])
				return i;
		return -1;
	}
	public static int indexOf(Listable[] list, String name){
		if (list == null)
			return -1;
		for (int i=0; i<list.length; i++) {
			String iName = list[i].getName();
			if (list[i]!=null && (iName==name || (iName != null && iName.equals(name))))
				return i;
		}
		return -1;
	}
	public static int indexOfByName(Vector v, Object target){
		if (target == null || v == null)
			return -1;
		String name = null;
		if (target instanceof String)
			name = (String)target;
		else if (target instanceof Listable)
			name = ((Listable)target).getName();
			else 
			return -1;
		if (name == null)
			return -1;
		for (int i=0; i<v.size(); i++) {
			Object obj = v.elementAt(i);
			if (obj instanceof Listable)
				
				if (name.equals(((Listable)obj).getName()))
					return i;
		}
		return -1;
	}
	public  int indexOfByName(String name){
		if (name==null)
			return -1;
		for (int i=0; i<size(); i++) {
			Object obj = elementAt(i);
			if (name.equals(((Listable)obj).getName()))
				return i;
		}
		return -1;
	}
	public int indexOfByNameIgnoreCase(String name){
		if (name==null)
			return -1;
		for (int i=0; i<size(); i++) {
			Object obj = elementAt(i);
			if (name.equalsIgnoreCase(((Listable)obj).getName()))
				return i;
		}
		return -1;
	}
	public void showList(String title){
		listWindow = new MesquiteListWindow(title, MesquiteTrunk.mesquiteTrunk, this, false);
	}
	public void showList(String title, MesquiteModule ownerModule){
		listWindow = new MesquiteListWindow(title, ownerModule, this, false);
	}
	static int endSequenceByThree(Listable[] listArray1, Object obj1,NumberArray numberArray2, int number2, int ic){
		int previousThird = ic;
		for (int ik = ic+1; ik< listArray1.length&& ik< numberArray2.getNumParts(); ik++){
			Object thisObj1 = listArray1[ik];
			int num2= numberArray2.getInt(ik);
			if (thisObj1 == obj1 && number2==num2) {
				//is a match; if not modulus 3 on from ic then return previousThird 
				if ((ik-ic) % 3 !=0)
					return previousThird;
				else
					previousThird = ik;
			}
			else {
				//is not a match; if modulus 3 on from ic then return previousThird 
				if ((ik-ic) % 3 ==0)
					return previousThird;
			}
		
		}
		return previousThird;
	}
	static int endSequenceByThree(Listable[] listArray, Object obj, int ic){
		int previousThird = ic;
		for (int ik = ic+1; ik< listArray.length; ik++){
			Object thisObj = listArray[ik];
			if (thisObj == obj) {
				//is a match; if not modulus 3 on from ic then return previousThird 
				if ((ik-ic) % 3 !=0)
					return previousThird;
				else
					previousThird = ik;
			}
			else {
				//is not a match; if modulus 3 on from ic then return previousThird 
				if ((ik-ic) % 3 ==0)
					return previousThird;
			}
		
		}
		return previousThird;
	}
	/** returns a string listing the elements of the array that are the passed object.  In the format
	of NEXUS character, taxa lists (e.g., "1- 3 6 201-455".  The offset is what the first element is to be numbered
	(e.g., 0 or 1)  */
	public static  String getListOfMatches(Listable[] listArray, Object obj, int offset) {
		return getListOfMatches(listArray, obj, offset, true);
	}
	/** returns a string listing the elements of the array that are the passed object.  In the format
	of NEXUS character, taxa lists (e.g., "1- 3 6 201-455".  The offset is what the first element is to be numbered
	(e.g., 0 or 1)  */
	public static  String getListOfMatches(Listable[] listArray, Object obj, int offset, boolean doByThirds) {
		return  getListOfMatches(listArray,  obj,  offset,  doByThirds, "");
	}
	/** returns a string listing the elements of the array that are the passed object.  In the format
	of NEXUS character, taxa lists (e.g., "1- 3 6 201-455".  The offset is what the first element is to be numbered
	(e.g., 0 or 1)  */
	public static  String getListOfMatches(Listable[] listArray, Object obj, int offset, boolean doByThirds, String separator) {
		int continuing = 0;
		String s="";
		boolean found=false;
		boolean writeSeparator=false;
		int lastWritten = -1;
		for (int i=0; i<listArray.length; i++) {
			if (listArray[i]==obj) {
				found=true;
				if (continuing == 0) {//first instance
					//first, check to see if there is a series of thirds....
					int lastThird = 0;
					if (doByThirds)
						lastThird = endSequenceByThree(listArray, obj, i);
					//if so, then go the series of thirds 
					if (doByThirds && lastThird != i){
						if (writeSeparator) {
							s+=separator;
							writeSeparator=false;
						}
						s += " " + CharacterStates.toExternal(i) + " - " +  CharacterStates.toExternal(lastThird) + "\\3";
						writeSeparator=true;
						i = lastThird;
					}
					else { //otherwise write as normal*/
						if (writeSeparator) {
							s+=separator;
							writeSeparator=false;
						}
						s += " " + (i + offset);
						lastWritten = i;
						continuing = 1;
					}
				}
				else if (continuing == 1) {  //second instance
					s += " - ";
					continuing = 2;
				}
			}
			else if (continuing >0) {  // we've already seen at least one
				if (lastWritten != i-1) {
					if (writeSeparator) {
						s+=separator;
						writeSeparator=false;
					}
					s += " " + (i-1 + offset);
					writeSeparator=true;
					lastWritten = i-1;
				}
				else {
					writeSeparator=true;
					lastWritten = -1;
				}
				continuing = 0;
			}
		}
		if (continuing>1){
			if (writeSeparator) {
				s+=separator;
				writeSeparator=false;
			}
			s += " " + (listArray.length-1 + offset);
		}
		if (found)
			return s;
		else
			return null;
	}
	/*...........................................................*/
	/** returns a string listing the elements of the array that are the passed object.  In the format
	of NEXUS character, taxa lists (e.g., "1- 3 6 201-455".  The offset is what the first element is to be numbered
	(e.g., 0 or 1)  */
	public static  String getListOfMatches(Listable[] listArray1, Object obj1, NumberArray numberArray2, int number2, int offset, boolean doByThirds) {
		int continuing = 0;
		String s="";
		boolean found=false;
		int lastWritten = -1;
		for (int i=0; i<listArray1.length && i<numberArray2.getNumParts(); i++) {
			if (listArray1[i]==obj1 && numberArray2.getInt(i)==number2) {
				found=true;
				if (continuing == 0) {//first instance
				//first, check to see if there is a series of thirds....
				 int lastThird = 0;
				 if (doByThirds)
					 lastThird = endSequenceByThree(listArray1, obj1,numberArray2, number2, i);
				//if so, then go the series of thirds 
				if (doByThirds && lastThird != i){
					s += " " + CharacterStates.toExternal(i) + " - " +  CharacterStates.toExternal(lastThird) + "\\3";
					i = lastThird;
				}
				else { //otherwise write as normal*/
					s += " " + (i + offset);
					lastWritten = i;
					continuing = 1;
				}
				}
				else if (continuing == 1) {
					s += " - ";
					continuing = 2;
				}
			}
			else if (continuing >0) {
				if (lastWritten != i-1) {
					s += " " + (i-1 + offset);
					lastWritten = i-1;
				}
				else
					lastWritten = -1;
				continuing = 0;
			}
		}
		if (continuing>1)
			s += " " + (listArray1.length-1 + offset);   //TODO:  what about listArray2???
		if (found)
			return s;
		else
			return null;
	}

	/*...........................................................*/
 	static Collator collator;
	static {
		collator = Collator.getInstance();
	}

	/*...........................................................*/
	public static void sort(int[] array){
		if (array==null || array.length<=1)
			return;
		
		for (int i=1; i<array.length; i++) {
			for (int j= i-1; j>=0 && array[j]>array[j+1]; j--) {
				int temp = array[j];
				array[j] = array[j+1];
				array[j+1]=temp;
			}
		}
		
	}
	public static void sort(Listable[] array){
 		if (array==null)
 			return;
 		boolean done = false;
		for (int i=1; i<array.length; i++) {
			for (int j= i-1; j>=0 && array[j]!=null && array[j+1] !=null && collator.compare(array[j].getName(), array[j+1].getName())>0; j--) {
						Listable temp = array[j];
						array[j] = array[j+1];
						array[j+1]=temp;
					
				
				
			}
		}
 	}
 	boolean distributeCommands = false;
	/*.................................................................................................................*/
	/** Note that command "distributeCommands" causes subsequent commands to be sent
	 on to any Commandables among the elements, until the next "endDistributeCommands" command */
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Deletes elements", "[start][end]", commandName, "deleteElements")) {
    	 		MesquiteInteger pos = new MesquiteInteger(0);
    	 		int first = MesquiteInteger.fromString(arguments, pos);
    	 		int last = MesquiteInteger.fromString(arguments, pos);
    	 		if (MesquiteInteger.isCombinable(first) && MesquiteInteger.isCombinable(last)) {
    	 			for (int i = last; i>=first ; i--)
    	 				removeElementAt(i, false);
				notifyListeners(this, new Notification(MesquiteListener.PARTS_DELETED));
			}
    	 			
 		}
 		else 	if (checker.compare(this.getClass(), "Turns off the distibution of commands to elements", null, commandName, "endDistributeCommands")) {
			distributeCommands = false;
		}
		else if (distributeCommands){
			for (int i=0; i<size(); i++) {
				Object obj = elementAt(i);
				if (obj instanceof Commandable) {
					((Commandable)obj).doCommand(commandName, arguments, checker);
				}
			}
		}
		else if (checker.compare(this.getClass(), "Turns on the distibution of commands to elements", null, commandName, "distributeCommands")) {
			distributeCommands = true;
		}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/** passes which object changed, along with optional Notification object with details (e.g., code number (type of change) and integers (e.g. which character))*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification); 
		if (indexOf(obj)>=0 && code != MesquiteListener.LOCK_CHANGED && code != MesquiteListener.SELECTION_CHANGED && code != MesquiteListener.ANNOTATION_CHANGED && code != MesquiteListener.ANNOTATION_ADDED && code != MesquiteListener.ANNOTATION_DELETED)
			notifyListeners(this, new Notification(MesquiteListener.ELEMENT_CHANGED));
	}
	/** passes which object was disposed*/
	public void disposing(Object obj){
	}
	/** Asks whether it's ok to delete the object as far as the listener is concerned (e.g., is it in use?)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;
	}
}

