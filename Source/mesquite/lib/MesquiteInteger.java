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

import mesquite.consensus.lib.Bipartition;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.QueryDialogs;
import mesquite.lib.ui.TwoIntegersDialog;

/*Last documented:  August 1999 */

/* ======================================================================== */
/**This int wrapper class is used to be able to pass integers by reference and have the
	original change as needed*/
public class MesquiteInteger implements Listable{
	public static final int unassigned = Integer.MIN_VALUE+1;
	public static final int impossible = Integer.MAX_VALUE - 1;
	public static final int finite = Integer.MAX_VALUE-3;
	public static final int infinite = Integer.MAX_VALUE;
	public static final int negInfinite = Integer.MIN_VALUE;
	public static final int inapplicable = Integer.MAX_VALUE -2;
	private int value;
	public static long totalCreated = 0;
	
	public MesquiteInteger (String name, int value) {
		this.name = name;
		this.value=value;
		totalCreated++;
	}
	public MesquiteInteger(int value) {
		this.value=value;
		totalCreated++;
	}
	public MesquiteInteger(long value) { //for backwards compatibility connected with long string positions
		this.value=(int)value;
		totalCreated++;
	}
	public MesquiteInteger() {
		this.value=unassigned;
		totalCreated++;
	}
	/*--------------------------------GET/SET--------------------------*/
	/** Returns value as an integer */
	public int getValue() {
		return value;
	}
	/** Sets value */
	public void setValue(int value) {
		this.value=value;
	}
	/** Sets value */
	public void setValue(long value) {//for backwards compatibility connected with long string positions
		this.value=(int)value;
	}
	/** Sets value */
	public void setValue(String s) {
		this.value=fromString(s);
	}
	/** Adds one to value */
	public void increment() {
		value++;
	}
	/** Adds one to value */
	public void increment(int incrementValue) {
		value+=incrementValue;
	}
	/** Subtracts one from value */
	public void decrement() {
		value--;
	}
	/** Subtracts one from value */
	public void decrement(int decrementValue) {
		value-= decrementValue;
	}
	/** Sets value to unassigned */
	public void setToUnassigned() {
		value=unassigned;
	}
	/*--------------------------------QUERY--------------------------*/
	/** Returns whether value is unassigned */
	public boolean isUnassigned() {
		return (value==unassigned);
	}
	/** Returns whether value is unassigned */
	public static boolean isUnassigned(int i) {
		return (i==unassigned);
	}
	/** Returns whether value is infinite */
	public boolean isInfinite() {
		return (value==infinite || value == negInfinite);
	}
	/** Returns whether value is infinite */
	public static boolean isInfinite(int i) {
		return (i==infinite || i == negInfinite);
	}
	/** Returns whether value is finite */
	public boolean isFinite() {
		return (value==finite || isCombinable());
	}
	/** Returns whether value is finite */
	public static boolean isFinite(int i) {
		return (i==finite || isCombinable(i));
	}
	/** Returns whether value is impossible */
	public boolean isImpossible() {
		return (value==impossible);
	}
	/** Returns whether value is impossible */
	public static boolean isImpossible(int i) {
		return (i==impossible);
	}
	/** Returns whether characteristic is inapplicable */
	public boolean isInapplicable() {
		return (value==inapplicable);
	}
	/** Returns whether characteristic is inapplicable */
	public static boolean isInapplicable(int i) {
		return (i==inapplicable);
	}
	/** Returns whether value is a regular number (NOT unassigned, infinite, inapplicable, impossible) */
	public boolean isCombinable() {
		return (value!=unassigned && value!=infinite && value!=impossible&& value!=inapplicable && value != finite);
	}
	/** Returns whether value is a regular number (NOT unassigned, infinite, inapplicable, impossible) */
	public static boolean isCombinable(int i) {
		return (i!=unassigned && i!=infinite && i!=impossible&& i!=inapplicable && i != finite);
	}
	/** Returns whether value is a regular number (NOT unassigned, infinite, inapplicable, impossible) and greater than zero */
	public static boolean isPositive(int i) {
		return isCombinable(i) && i>0;
	}
	/** Returns whether value is a regular number (NOT unassigned, infinite, inapplicable, impossible) and non-negative */
	public static boolean isNonNegative(int i) {
		return isCombinable(i) && i>=0;
	}
	/*--------------------------------INEQUALITIES--------------------------*/

	/** Returns true if value 1 is less than value 2. */
	public static boolean lessThan(int value1, int value2) {
		if (value1 == infinite) 
			return false;
		else if (value2 == infinite) 
			return true;
		else if (value1 == negInfinite) 
			return true;
		else if (value2 == negInfinite) 
			return false;
		else if (!isCombinable(value2))
			return isCombinable(value1);
		else if (!isCombinable(value1))
			return false;
		else 
			return (value1<value2);
	}
	/** Returns true if value 1 is greater than value 2. */
	public static boolean greaterThan(int value1, int value2) {
		if (value1 == infinite) 
			return true;
		else if (value2 == infinite) 
			return false;
		else if (value1 == negInfinite) 
			return false;
		else if (value2 == negInfinite) 
			return true;
		else if (!isCombinable(value2)) //TODO: think about these rules
			return isCombinable(value1);
		else if (!isCombinable(value1))
			return false;
		else 
			return (value1>value2);
	}
	/*-------------------------------- firstIsGreaterThan --------------------------*/
	/** Returns 1 if the first integer is greater than the second, -1 if the second is greater than the first, 0 otherwise.  Accounts for infinite, unassigned, etc. */
	public static int whichIsGreater(int a, int b) {
		if (a == unassigned || a == inapplicable || a == negInfinite || a == impossible || a == finite) {
			if (b==unassigned || b == inapplicable || b== negInfinite || b == impossible || b == finite)
				return 0;
			else
				return -1;    //a is bad, b is combinable
		}
		
		else if (b==unassigned || b == inapplicable || b== negInfinite || b == impossible || b == finite)
			return 1;  
		else if (a==infinite)
			return 1;
		else if (b==infinite)
			return -1;
		else if (a>b)
			return 1;
		else if (b>a)
			return -1;
		else return 0;
	}
	/*--------------------------------CONTAINS--------------------------*/
	/** Returns true iff the integer a is contained within the bounds of b1 through b2, inclusive. */
	public static boolean contains(int a, int b1, int b2) {
		if (!isCombinable(a) || !isCombinable(b1)||!isCombinable(b2))
			return false;
		if (b1<=b2)
			if (a>=b1 && a<=b2)
				return true;
		if (b1>=b2)
			if (a<=b1 && a>=b2)
				return true;
		return false;
	}

		/*--------------------------------MIN-MAX--------------------------*/
	/** Returns maximum of two integers.  Accounts for infinite, unassigned, etc. */
	public static int maximum(int a, int b) {
		if (a == unassigned || a == inapplicable || a == negInfinite || a == impossible || a == finite) {
			if (b==unassigned || b == inapplicable || b== negInfinite || b == impossible || b == finite)
				return unassigned;
			else
				return b;
		}
		
		else if (b==unassigned || b == inapplicable || b== negInfinite || b == impossible || b == finite)
			return a;
		else if (a==infinite)
			return a;
		else if (b==infinite)
			return b;
		else if (a>b)
			return a;
		else
			return b;
	}
	/** Returns minimum of two integers.  Accounts for infinite, unassigned, etc. */
	public static int minimum(int a, int b) {
		if (a == unassigned || a == inapplicable || a == infinite || a == impossible || a == finite){
			if (b==unassigned || b == inapplicable || b== infinite || b == impossible || b == finite)
				return unassigned;
			else
				return b;
		}
		else if (b==unassigned || b == inapplicable || b== infinite || b == impossible || b == finite)
			return a;
		else if (a==negInfinite)
			return a;
		else if (b==negInfinite)
			return b;
		else if (a<b)
			return a;
		else
			return b;
	}
	/** Returns maximum of three integers.  Accounts for infinite, unassigned, etc. */
	public static int maximum(int a, int b, int c) {
		return maximum(a,maximum(b,c));
	}
	/** Returns maximum of four integers.  Accounts for infinite, unassigned, etc. */
	public static int maximum(int a, int b, int c, int d) {
		return maximum(a,maximum(b,maximum(c,d)));
	}
	/** Returns minimum of three integers.  Accounts for infinite, unassigned, etc. */
	public static int minimum(int a, int b, int c) {
		return minimum(a,minimum(b,c));
	}
	/** Returns minimum of four integers.  Accounts for infinite, unassigned, etc. */
	public static int minimum(int a, int b, int c, int d) {
		return minimum(a,minimum(b,minimum(c,d)));
	}
	
	/*--------------------------------Arithmetics--------------------------*/
	/** Adds to current value.  Considers infinite, unassigned etc. */
	public void add(int toAdd) {
		value = add(toAdd, value);
	}
	/** Subtracts from current value.  Considers infinite, unassigned etc. */
	public void subtract(int toSubtract) {
		value = subtract(value, toSubtract);
	}
	/** Adds two ints.  Considers infinite, unassigned etc. */
	public static int add(int value1, int value2) {
		if (value1 == infinite){
			if (value2== negInfinite)
				return 0;
			else
				return infinite;
		}
		else if (value2 == infinite){
			if (value1== negInfinite)
				return 0;
			else
				return infinite;
		}
		else if (value1 == negInfinite)
			return negInfinite;
		else if (value2 == negInfinite)
			return negInfinite;
		else if (value1 == finite || value2 == finite)
			return finite;
		else if (isCombinable(value1)) {
			if (isCombinable(value2))
				return value1 + value2;
			else
				return value1;
		}
		else
			return value2;
	}
	/** Subtracts value2 from value 1.  Considers infinite, unassigned etc. */
	public static int subtract(int value1, int value2) {
		if (value2 == negInfinite){
			if (value1== negInfinite)
				return 0;
			else
				return infinite;
		}
		else if (value2 == infinite){
			if (value1== infinite)
				return 0;
			else
				return negInfinite;
		}
		else if (value1 == infinite)
			return infinite;
		else if (value1 == negInfinite)
			return negInfinite;
		else if (isCombinable(value2)) {
			if (isCombinable(value1))
				return value1 - value2;
			else if (value2 == finite)
				return value2;
			else
				return -value2;
		}
		else
			return value1;
	}
	/** Multiplies two ints.  Considers infinite, unassigned etc. */
	public static int multiply(int value1, int value2) {
		if (value1 == 0 || value2==0)
			return 0;
		else if (isCombinable(value1)){
			if (isCombinable(value2))
				return value1*value2;
			else {  //value 2 not combinable
				if (value2 == infinite){
					if (value1<0)
						return negInfinite;
					else 
						return infinite;
				}
				else if (value2== negInfinite) {
					if (value1<0)
						return infinite;
					else 
						return negInfinite;
				}
				else
					return unassigned;
			}
		}
		else {//value 1 not combinable
			if (isCombinable(value2)){
				if (value1 == infinite){
					if (value2<0)
						return negInfinite;
					else 
						return infinite;
				}
				else if (value1== negInfinite) {
					if (value2<0)
						return infinite;
					else 
						return negInfinite;
				}
				else
					return unassigned;
			}
			else { //neither combinable
				if (value1 == infinite && value2 == infinite)
					return infinite;
				else if (value1 == infinite && value2 == negInfinite)
					return negInfinite;
				else if (value1 == negInfinite && value2 == infinite)
					return negInfinite;
				else if (value1 == negInfinite && value2 == negInfinite)
					return infinite;
				else 
					return unassigned;
			}
		}
		
	}
	/** Divides first int by second int.  Considers infinite, unassigned etc. */
	public static double divide(int value1, int value2) {
		if (value2==0)
			return MesquiteDouble.infinite;
		else if (value1 == 0)
			return 0;
		else if (isCombinable(value1)){
			if (isCombinable(value2))
				return ((double)value1)/value2;
			else {  //value 2 not combinable
				if (value2 == infinite || value2== negInfinite)
					return 0;
				else
					return MesquiteDouble.unassigned;
			}
		}
		else {//value 1 not combinable
			if (isCombinable(value2)){
				return MesquiteDouble.unassigned;
			}
			else { //neither combinable
				if (value2 == infinite || value2== negInfinite)
					return 0;
				else
					return MesquiteDouble.unassigned;
			}
		}
	}
	/*----------------------------------------------------------*/
	public static boolean isDivisibleBy(int value, int diviser) {
		if (diviser == 0) 
			return false;
		int fraction = (int)java.lang.Math.round(value/diviser);
		return (fraction*diviser==value);
	}
	/*----------------------------------------------------------*/
	public static int roundUp(double value) {
		return (int)java.lang.Math.round(value+0.4999999999999);
	}
	/*--------------------------------STRINGS--------------------------*/
	String name = null;
	/** Returns string version of this value.  Returns "unassigned" etc. if needed. (for Listable interface)*/
	public String getName(){
		if (name == null)
		return toString();
		else return name;
	}
	/** Returns string version of this value.  Returns "unassigned" etc. if needed*/
	public String toString() {
		return toString(value);
	}
	/** Returns string version of value.  Returns "unassigned" etc. if needed*/
	public static String toString(int v) {
		if (v==unassigned)
			return "?"; //changed from "unassigned" June 02
		else if (v==infinite)
			return "infinite";
		else if (v==negInfinite)
			return "neg.infinite";  
		else if (v==impossible)
			return "impossible";  
		else if (v==inapplicable)
			return "inapplicable"; 
		else if (v==finite)
			return "finite"; 
		else {
			try {
				return Integer.toString(v);
			}
			catch (NumberFormatException ne){
				return "impossible";
			}
		}
	}

	/** Returns string version of this value, with leading zeros as needed.  Returns "unassigned" etc. if needed*/
	public static String toStringDigitsSpecified(int v, int digits) {
		if (v==unassigned)
			return "?"; 
		else if (v==infinite)
			return "infinite";
		else if (v==negInfinite)
			return "neg.infinite";  
		else if (v==impossible)
			return "impossible";  
		else if (v==inapplicable)
			return "inapplicable"; 
		else if (v==finite)
			return "finite"; 
		else {
			try {
				String s = "";
				boolean negative = (v<0);
				if (negative)
					s = Integer.toString(-v);
				else
					s = Integer.toString(v);
				if (s.length()<digits)
					for (int i=0; i<digits-s.length(); i++)
						s="0"+s;
				if (negative) 
					s="-"+s;
				return s;
			}
			catch (NumberFormatException ne){
				return "impossible";
			}
		}
	}

	public static boolean isNumber(String s) {
		if (StringUtil.blank(s))
			return false;
		try {
			int value = Integer.parseInt(s);
			return true;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}
	
	/** DOCUMENT */
	public static int orderOfMagnitude(int x) {
		if (!isCombinable(x))
			return MesquiteInteger.impossible;
		int current = 1;
		for (int i = 0; ; i++) {
			if (x<current)
				return i;
			try {
				current *=10;
			}
			catch (Exception e) {
				return i;
			}
		}
	}

	/** DOCUMENT */
	public static int getDigit(int x, int digit) {
		if (!isCombinable(x))
			return MesquiteInteger.impossible;
		int baseNumber = 1;
		for (int i=1; i<=digit; i++)
			baseNumber*=10;
		int leftOver = x % baseNumber;  // now need to strip off everything to the right
		if (digit == 1) return leftOver;
		baseNumber /= 10;
		leftOver /= baseNumber;
		return leftOver;
	}

	/** DOCUMENT */
	public static int power(int x, int power) {
		if (!isCombinable(x))
			return MesquiteInteger.impossible;
		if (power==0)
			return 1;
		int value = x;
		if (power>0) {
			for (int i=2; i<=power; i++)
				value=value*x;
		} else
			for (int i=2; i<=-power; i++)
				value= value/x;
		return value;
	}

	public static void logHorizIntegerList(int max){
		int orderOfMag = MesquiteInteger.orderOfMagnitude(max);
		StringBuffer sb = new StringBuffer();
		
		for (int OOM = orderOfMag-1; OOM>=0; OOM--) {
			int count = 1;
			int baseNumber = power(10,OOM);
			for (int i=0; i<max; i++){
				if (baseNumber>0 && (i+1) % baseNumber==0) {
					if ((i+1)/baseNumber>0) {
						sb.append("" + getDigit(i+1,OOM+1));
					}
					else
						sb.append(" ");
				}
				else if (baseNumber==0)
					sb.append(getDigit(i+1,1));
				else sb.append(" ");
				count++;
			}
			sb.append("\n");
		}
		MesquiteMessage.println(sb.toString());
	}

	/** Returns integer parsed from given string.  Assumes entire string is what should be parsed. */
	public static int fromString(String s) { 
		return fromString(s, false);
	}
	
	/** Returns integer parsed from given string; warns about NumberFormatException if boolean is true,
	   using warningMessage.
	  Assumes entire string is what should be parsed. */
	public static int fromString(String s, String warningMessage, boolean warnQuiet, boolean warnProminent) {
		if (StringUtil.blank(s))
			return impossible;
		try {
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e) {
			if (s !=null) {
				if (s.equalsIgnoreCase("unassigned") || s.equalsIgnoreCase("estimate") ||s.equalsIgnoreCase("?"))
					return unassigned;
				else if (s.equalsIgnoreCase("infinite") || s.equalsIgnoreCase("i"))
					return infinite;
				else if (s.equalsIgnoreCase("inapplicable"))
					return inapplicable;
				else if (s.equalsIgnoreCase("neg.Infinite"))
					return negInfinite;
				else if (s.equalsIgnoreCase("impossible"))
					return impossible;
				else if (s.equalsIgnoreCase("finite"))
					return finite;
			}
			if (warnQuiet)
				MesquiteMessage.println(warningMessage);
			else if (warnProminent)
				MesquiteTrunk.mesquiteTrunk.alert(warningMessage);
			return impossible;
		}
	}
	/** Returns integer parsed from given string; warns about NumberFormatException if boolean is true using the
	message "NumberFormatException in parsing string "<s>". Assumes entire string is what should be parsed. */
	public static int fromString(String s, boolean warn) {
		
		return fromString(s,"NumberFormatException in parsing string \"" + s + "\"",warn, false);
	}
	/** Returns integer parsed from given string, starting at string position given.
	Assumes only first token from pos is to be parsed. */
	public static int fromString(Parser parser) {
		String f =parser.getNextToken(); 
		if ("-".equals(f))
			return -fromString(parser.getNextToken(), false); 
		else
			return fromString(f, false); 
	}
	/** Returns integer parsed from given string, starting at string position given.
	Assumes only first token from pos is to be parsed. */
	public static int fromString(String s, MesquiteInteger pos) {
		String f =ParseUtil.getToken(s,pos); 
		if ("-".equals(f))
			return -fromString(ParseUtil.getToken(s,pos), false); 
		else
			return fromString(f, false); 
	}
	/** Returns integer parsed from first token of given string, and sets value of pos
	to character at end of this token. */
	public static int fromFirstToken(String s, MesquiteInteger pos) {
		pos.setValue(0);
		return fromString(s,pos);
	}
	/*--------------------------------QUERY DIALOGS--------------------------*/
	/** Presents dialog querying user for two integers */
	public static void queryTwoIntegers(MesquiteWindow parent, String title, String label1, String label2, MesquiteBoolean answer, MesquiteInteger num1, MesquiteInteger num2,int min1,int max1,int min2, int max2, String helpString) {
		 TwoIntegersDialog tid = new TwoIntegersDialog(parent, title, label1, label2, answer, num1, num2,min1,max1,min2,max2, helpString);
		 tid.dispose();
		 
	}
	/** Presents dialog querying user for an integers, with no check for minimum and maximum */
	public static int queryInteger(MesquiteWindow parent, String title, String message, int current) {
		MesquiteInteger io = new MesquiteInteger(current);
		//IntegerDialog id = new IntegerDialog(parent, title, message, io);
		//	id.dispose();
		QueryDialogs.queryInteger(parent, title, message, true, io);
		return io.getValue();
	}
	/** Presents dialog querying user for an integers, with a check for minimum and maximum */
	public static int queryInteger(MesquiteWindow parent, String title, String message, int current, int minimum, int maximum, boolean allowCancel) {
		return queryInteger( parent,  title,  message, "",  current,  minimum,  maximum,  allowCancel);
	}
	/** Presents dialog querying user for an integers, with a check for minimum and maximum */
	public static int queryInteger(MesquiteWindow parent, String title, String message, String secondaryMessage, String help, int current, int minimum, int maximum, boolean allowCancel) {
		if (MesquiteInteger.isCombinable(maximum) && current>maximum)
			current=maximum;
		else if (MesquiteInteger.isCombinable(minimum) && current<minimum)
			current=minimum;
		MesquiteInteger io = new MesquiteInteger(current);
		boolean done=false;
		if (StringUtil.blank(help)) {
			help = "<h3>" + StringUtil.protectForXML(title) + "</h3>Please enter a whole number (integer).  <p>The initial value is " + MesquiteInteger.toString(current) + ". ";
			if (MesquiteInteger.isCombinable(minimum))
				help += "The minimum value permitted is " + MesquiteInteger.toString(minimum)+ ". ";
			if (MesquiteInteger.isCombinable(maximum))
				help += "The maximum value permitted is " + MesquiteInteger.toString(maximum)+ ". ";
		}
		while (!done) {
			//IntegerDialog id = new IntegerDialog(parent, title, message, io);
			//id.dispose();
			QueryDialogs.queryInteger(parent, title, message, secondaryMessage, help, allowCancel, io);
			if (!io.isCombinable() || ((!MesquiteInteger.isCombinable(maximum) || io.getValue()<=maximum) && (!MesquiteInteger.isCombinable(minimum) ||  io.getValue()>=minimum)))
				done=true;
			else {
				if (maximum==infinite)
					MesquiteModule.mesquiteTrunk.alert("Value must be " + Integer.toString(minimum) + " or more");
				else
					MesquiteModule.mesquiteTrunk.alert("Value must be between " + Integer.toString(minimum) + " and " + Integer.toString(maximum));
				io.setValue(current);
			}
		}
		return io.getValue();
	}
	/** Presents dialog querying user for an integers, with a check for minimum and maximum */
	public static int queryInteger(MesquiteWindow parent, String title, String message, String help, int current, int minimum, int maximum, boolean allowCancel) {
		return queryInteger(parent, title, message, null, help, current, minimum, maximum, allowCancel);
	}
	/** Presents dialog querying user for an integers, with a check for minimum and maximum */
	public static int queryInteger(MesquiteWindow parent, String title, String message, int current, int minimum, int maximum) {
		return queryInteger(parent, title, message, current, minimum, maximum, true);
	}
	/** Presents dialog querying user for an integers, with a check for minimum and maximum */
	public static int queryInteger(MesquiteWindow parent, String title, String message, String secondaryMessage, String help, int current, int minimum, int maximum) {
		return queryInteger(parent, title, message, secondaryMessage, help, current, minimum, maximum, true);
	}
	/** Presents dialog querying user for an integers, with a check for minimum and maximum */
	public static int queryInteger(MesquiteWindow parent, String title, String message, String help, int current, int minimum, int maximum) {
		return queryInteger(parent, title, message, help, current, minimum, maximum, true);
	}
}

