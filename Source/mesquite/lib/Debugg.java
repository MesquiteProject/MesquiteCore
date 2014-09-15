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

import java.io.*;
 
/*=======================*/
/** A utility class with method to duplicate System.out.println, but can be searched for.  
 * Intended for temporary use.  Do not leave in released code. */
public class Debugg {
	public static int count = 0;
	public static void print(String s) {
		System.out.print(s);
		if (MesquiteTrunk.mesquiteTrunk != null)
		MesquiteTrunk.mesquiteTrunk.log(s);
	}
	public static void println(String s) {
		System.out.println(s);
		if (MesquiteTrunk.mesquiteTrunk != null)
			MesquiteTrunk.mesquiteTrunk.logln(s);
	}
	public static void printLogln(String s) {
		MesquiteMessage.warnUser(s);
	}
	public static void printHandle(Object obj) {
		if (obj == null)
			System.out.println(" request for handle of null object");
		/*else if (obj instanceof Listable)
			System.out.println("Handle: " + Integer.toHexString(obj.hashCode()<<3) + "  " + obj + "     " + ((Listable)obj).getName());*/
		else
			System.out.println("Handle: " + Integer.toHexString(obj.hashCode()<<3) + "  " + obj);
	}
	public static void printStackTrace(String message) {
		printLogln(message);
		printStackTrace();
	}
	public static void printStackTrace() {
		printLogln("An exception was generated intentionally to show a stack trace.  It is used for debugging purposes.");
		Exception e = new Exception();
		printStackTrace(e);
	}
	public static void printStackTrace(Throwable e) {
		if (e!=null){
			MesquiteFile.throwableToLog(null, e);
			
		}
	}
	public static void printStackTrace(Exception e) {
		if (e!=null){
			MesquiteFile.throwableToLog(null, e);
		}
	}
	public static void printStackTrace(Error e) {
		if (e!=null){
			MesquiteFile.throwableToLog(null, e);
		}
	}
}

