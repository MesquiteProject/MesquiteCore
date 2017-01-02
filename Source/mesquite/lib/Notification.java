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



/* ======================================================================== */
/** An object that contains information about a notification event (e.g., call to notifyListeners or parametersChanged)*/
public class Notification implements Identifiable {
	int code = MesquiteListener.UNKNOWN;
	int[] parameters = null;
	int[] subcodes = null;
	long id;
	long nn = -1;
	static long numCreated = 0;
	UndoReference undoReference = null;
	Class objectClass = null;
	
	public Notification(int code, int[] parameters){
		this();
		this.code = code;
		this.parameters = parameters;
	}
	public Notification(int code){
		this();
		this.code = code;
	}
	public Notification(int code,  UndoReference undoReference){
		this();
		this.code = code;
		this.undoReference = undoReference;
	}
	public Notification(int code,  int[] parameters, UndoReference undoReference){
		this();
		this.code = code;
		this.undoReference = undoReference;
		this.parameters = parameters;
	}
	
	public String toString(){
		return "Notification: " + getID() + " code = " + code + " parameters " + IntegerArray.toString(parameters);
	}
	public Notification(){
		id = numCreated++;
	}
	public long getID(){
		return id;
	}
	public Notification setNotificationNumber(long nn){
		this.nn = nn;
		return this;
	}
	public long getNotificationNumber(){
		if (nn<0)
			return id;
		else
			return nn;
	}
	public void dispose() {
		if (undoReference!=null)
			undoReference.dispose();
	}
	public int getCode(){
		return code;
	}
	public int[] getParameters(){
		return parameters;
	}
	public Class getObjectClass(){
		return objectClass;
	}
	public void setObjectClass(Class c){
		this.objectClass=c;
	}
	public int[] getSubcodes(){
		return subcodes;
	}
	public void setSubcodes(int[] subcodes){
		this.subcodes=subcodes;
	}
	public boolean subcodesContains(int subcode){
		if (subcodes!=null) {
			for (int i=0; i<subcodes.length; i++)
				if (subcodes[i]==subcode)
					return true;
		}
		return false;
	}
	
	public static long getNotificationNumber(Notification n){
		if (n==null)
			return -1;
		else
			return n.getNotificationNumber();
	}
	public static int getCode(Notification n){
		if (n==null)
			return MesquiteListener.UNKNOWN;
		else
			return n.getCode();
	}
	public static int[] getParameters(Notification n){
		if (n==null)
			return null;
		else
			return n.getParameters();
	}
	public static int[] getSubcodes(Notification n){
		if (n==null)
			return null;
		else
			return n.getSubcodes();
	}
	public static boolean appearsCosmetic(Notification n){  
		if (n==null)
			return false;
		else
			return n.getCode() == MesquiteListener.LOCK_CHANGED  ||  n.getCode() == MesquiteListener.ANNOTATION_CHANGED  ||  n.getCode() == MesquiteListener.ANNOTATION_ADDED  || n.getCode() == MesquiteListener.ANNOTATION_DELETED  ||n.getCode() == MesquiteListener.NAMES_CHANGED  ;
	}
	public static boolean appearsCosmeticOrSelection(Notification n){
		if (n==null)
			return false;
		else
			return n.getCode() == MesquiteListener.SELECTION_CHANGED  || n.getCode() == MesquiteListener.LOCK_CHANGED  ||  n.getCode() == MesquiteListener.ANNOTATION_CHANGED  ||  n.getCode() == MesquiteListener.ANNOTATION_ADDED  || n.getCode() == MesquiteListener.ANNOTATION_DELETED  ||n.getCode() == MesquiteListener.NAMES_CHANGED  ;
	}
	public UndoReference getUndoReference() {
		return undoReference;
	}
	public void setUndoReference(UndoReference undoReference) {
		this.undoReference = undoReference;
	}
	public static UndoReference getUndoReference(Notification n){
		if (n==null)
			return null;
		else
			return n.getUndoReference();
	}

}


