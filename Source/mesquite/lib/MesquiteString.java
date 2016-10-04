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

/* ======================================================================== */
/**This String wrapper class is used to be able to pass Strings by reference and have the
	original change as needed*/
public class MesquiteString extends Listened implements Listable, Identifiable, Explainable {
	public static final int riseOffset = 5;
	private boolean bound = false;
	public static long totalCreated = 0;
	public static String helpString=null;
	private StringBuffer sb;
	private boolean isNull = true;
	private String name = null;
	long id;
	public MesquiteString() {
		super();
		id = totalCreated;
		totalCreated++;
		sb = new StringBuffer();
	}
	public MesquiteString(String value) {
		super();
		if (value == null)
			sb = new StringBuffer();
		else
			sb = new StringBuffer(value);
		isNull = value==null;
		id = totalCreated;
		totalCreated++;
	}
	public MesquiteString(String name, String value) {
		this(value);
		this.name = name;
	}	

	public long getID(){
		return id;
	}

	String referentID = null; //so that object whose name is recorded in MesquiteString can also be identified by an ID
	public String getReferentID(){
		return referentID;
	}
	public void setReferentID(String rID){
		referentID = rID;
	}
	public String getValue() {
		if (isNull)
			return null;
		return sb.toString();
	}
	public String toString() {
		if (isNull)
			return "null";
		return sb.toString();
	}
	public boolean isBlank() {
		if (isNull)
			return true;
		return StringUtil.blank(sb.toString());
	}
	public void setName(String s){
		name = s;
	}
	public String getName() {
		if (name == null)
			return getValue();
		else
			return name;
	}
	public String getExplanation(){
		return getValue();
	}
	public static boolean explicitlyUnassigned(String s){
		if (s==null)
			return false;
		return ("?".equals(s) || "unassigned".equalsIgnoreCase(s) || "estimate".equalsIgnoreCase(s));

	}

	public void setValue(String value) {
		setValue(value, true);
	}
	public void setValue(MesquiteString value) {
		if (value==null){
			sb.setLength(0);
			return;
		}
		setValue(value.toString(), true);
	}

	public void setValue(String value, boolean notify) {
		if (value==null){
			sb.setLength(0);
			if (!isNull){
				isNull = true;
				if (bound)
					MesquiteTrunk.resetCheckMenuItems(); //TODO: shouldn't this method be in MenuOwner???
				if (notify)
					notifyListeners(this, new Notification(MesquiteListener.VALUE_CHANGED));
			}
		}
		else {
			isNull = false;
			if (!value.equals(sb.toString())) {
				sb.setLength(0);
				sb.append(value);
				if (bound)
					MesquiteTrunk.resetCheckMenuItems(); //TODO: shouldn't this method be in MenuOwner???
				if (notify)
					notifyListeners(this, new Notification(MesquiteListener.VALUE_CHANGED));
			}
			else if (bound)
				MesquiteTrunk.resetCheckMenuItems(); //TODO: shouldn't this method be in MenuOwner???
		}
	}

	public void append(String value) {
		if (value!=null) {
			if (isNull)
				sb.setLength(0);
			isNull = false;
			sb.append(value);
			if (bound)
				MesquiteTrunk.resetCheckMenuItems(); //TODO: shouldn't this method be in MenuOwner???
			notifyListeners(this, new Notification(MesquiteListener.VALUE_CHANGED));
		}
	}
	public void prepend(String value) {
		if (value!=null) {
			if (isNull){
				sb.setLength(0);
				isNull = false;
				sb.append(value);
			}
			else
				sb.insert(0,value);
			if (bound)
				MesquiteTrunk.resetCheckMenuItems(); //TODO: shouldn't this method be in MenuOwner???
			notifyListeners(this, new Notification(MesquiteListener.VALUE_CHANGED));
		}
	}
	public void bindMenuItem() {
		bound = true;
	}
	public void releaseMenuItem() {
		bound = false;
	}
	public static String queryMultiLineString(MesquiteWindow parent, String title, String message, String current, int rows, boolean hasDefault, boolean useSmallText) { //TODO: use Frame passed
		MesquiteString io = new MesquiteString(current);
		QueryDialogs.queryMultiLineString(parent, title, message, io, rows, hasDefault, useSmallText);
		return io.getValue();
	}
	public static String queryString(MesquiteWindow parent, String title, String message, String current, int rows) { //TODO: use Frame passed
		MesquiteString io = new MesquiteString(current);
		QueryDialogs.queryString(parent, title, message, io, rows);
		return io.getValue();
	}
	public static String queryString(MesquiteWindow parent, String title, String message, String current) { //TODO: use Frame passed
		MesquiteString io = new MesquiteString(current);
		QueryDialogs.queryString(parent, title, message, io);
		return io.getValue();
	}
	public static String queryShortString(MesquiteWindow parent, String title, String message, String current, boolean hasDefault) { //TODO: use Frame passed
		MesquiteString io = new MesquiteString(current);
		QueryDialogs.queryShortString(parent, title, message, io, hasDefault);
		return io.getValue();
	}
	public static String queryShortString(MesquiteWindow parent, String title, String message, String current) { //TODO: use Frame passed
		MesquiteString io = new MesquiteString(current);
		QueryDialogs.queryShortString(parent, title, message, io);
		return io.getValue();
	}
	public static void queryTwoStrings(MesquiteWindow parent, String title, String label1, String label2, MesquiteBoolean answer, MesquiteString resp1, MesquiteString resp2, boolean secondLong) {
		new TwoStringsDialog(parent, title, label1, label2, answer, resp1, resp2,false, secondLong);
	}
	/*.................................................................................................................*/
}



