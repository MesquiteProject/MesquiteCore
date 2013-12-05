// FormattedOutput.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.io;

import java.io.*;
import java.text.*;
import java.util.*;
import java.math.*;

/**
 * tools to simplify formatted output to a stream
 *
 * @version $Id: FormattedOutput.java,v 1.14 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class FormattedOutput implements Serializable
{
	//
	// Public stuff
	//

	/**
	 * create instance of this class
	 * (note that there is no public constructor
	 * as this class is a singleton)
	 */
	public synchronized static FormattedOutput getInstance()
	{
		if (singleton == null)
		{
			singleton = new FormattedOutput();
		}

		return singleton;
	}

	/**
	 * print decimal number with a prespecified number
	 * of digits after the point
	 *
	 * @param out output stream
	 * @param number to be printed
	 * @param width number of fraction digits
	 *
	 * @return length of the string printed
	 */
	public int displayDecimal(PrintWriter out, double number, int width)
	{
		String s = getDecimalString(number, width);

		out.print(s);

		return s.length();
	}

	/**
	 * Returns a decimal string representation of a number with
	 * constrained width.
	 */
	public synchronized String getDecimalString(double number, int width) {
		nf.setMinimumFractionDigits(width);
		nf.setMaximumFractionDigits(width);

		return nf.format(number);
	}

	private static final double round(double number, int sf) {
		double decimals = Math.floor(Math.log(number) / Math.log(10.0));
		double power =  Math.pow(10, decimals - sf+1);
		number /= power;
		number = Math.round(number);
		number *= power;
		return number;
	}

	//public String getSFString(double number, int sf) {
	//	double decimals = Math.floor(Math.log(number) / Math.log(10.0));
	//	double power =  Math.pow(10, decimals - sf);
	//	number /= power;
	//	number = Math.round(number);
	//	number *= power;
	//
	//	return "" + number;
	//}
		
	/** Used by getSFString3() */
	private SFStringInfo getSFStringInfo(String buffer,int sf, boolean includeRightZeros, boolean previousCarry) {
		char[] b2 = new char[(includeRightZeros ? buffer.length() : Math.min(sf,buffer.length()))];
		boolean carry = (sf>=buffer.length() ? previousCarry : buffer.charAt(sf) >= '5');
		for(int i = b2.length-1 ; i >=0; i--) {
			if(i<sf) {
				b2[i] = buffer.charAt(i);
				if(carry) {
					if(b2[i] == '9') {
						b2[i] = '0';
					} else {
						b2[i]++;
          	carry = false;
					}
				}
			} else {
				carry = buffer.charAt(i)>='5';
				b2[i] = '0';
			}
		}
		return new SFStringInfo(new String(b2), carry);

	}

	/**
	 * An alternative version of getSFString which works on the actual string
	 * Returns a string representing the given number to the number
	 * of significant figures requested.
	 */
	 public String getSFString(double number, int sf) {
		String s = number+"";
		String preFullStop = null;
		String postFullStop = null;
		String postE = null;

		int fullStopIndex = s.indexOf('.');
		int eIndex = s.indexOf('e');
		if(eIndex<0) {
			eIndex = s.indexOf('E');
		}

		if(eIndex>=0) {
			postE = s.substring(eIndex+1);
		}
		eIndex = s.length();
		if(fullStopIndex>=0) {
			postFullStop = s.substring(fullStopIndex+1,eIndex);

		} else {
			fullStopIndex = eIndex;
		}

		preFullStop = s.substring(0,fullStopIndex);

		SFStringInfo postFSI = null;


		boolean postCarry = false;
		if(postFullStop!=null) {
			int postSF = sf - preFullStop.length();
			if(postSF>0) {
				postFSI =   getSFStringInfo(postFullStop, postSF, false,false);
				postCarry = postFSI.carry;
			} else {

				postCarry = postFullStop.charAt(0)>='5';
			}

		}
		SFStringInfo preFSI;
		preFSI = getSFStringInfo(preFullStop,sf,true,postCarry);
		if(preFSI.carry) {
			preFSI.string = "1"+preFSI.string;
		}
		String b;
		if(postFSI!=null) {
			b = preFSI.string+"."+postFSI.string;
		} else {
			b = preFSI.string;
		}
		if(postE != null) {
     			b+='e'+postE;
		}

		while (b.charAt(b.length() - 1) == '0') {
			b = b.substring(0, b.length()-1);
		}
		if (b.charAt(b.length() - 1) == '.') {
			b = b.substring(0, b.length()-1);
		}

		return b;
	}
	/**
	 * print label with a prespecified length
	 * (label will be shortened or spaces will introduced, if necessary)
	 *
	 * @param out output stream
	 * @param label label to be printed
	 * @param width desired length
	 */
	public void displayLabel(PrintWriter out, String label, int width)
	{
		int len = label.length();

		if (len == width)
		{
			// Print as is
			out.print(label);
		}
		else if (len < width)
		{
			// fill rest with spaces
			out.print(label);
			multiplePrint(out, ' ', width - len);
		}
		else
		{
			// Print first width characters
			for (int i = 0; i < width; i++)
			{
				out.print(label.charAt(i));
			}			
		}		
	}
	
	/**
	 * print integer, aligned to a reference number,
	 * (introducing space at the left side)
	 *
	 * @param out output stream
	 * @param num number to be printed
	 * @param maxNum reference number
	 */
	public void displayInteger(PrintWriter out, int num, int maxNum)
	{
		int lenNum = Integer.toString(num).length();
		int lenMaxNum = Integer.toString(maxNum).length();
		
		if (lenNum < lenMaxNum)
		{
			multiplePrint(out, ' ', lenMaxNum - lenNum);
		}
		out.print(num);
	}

	/**
	 * print whitespace of length of a string displaying a given integer
	 *
	 * @param output stream
	 * @param maxNum number
	 */
	public void displayIntegerWhite(PrintWriter out, int maxNum)
	{
		int lenMaxNum = Integer.toString(maxNum).length();
		
		multiplePrint(out, ' ', lenMaxNum);
	}

	/**
	 * repeatedly print a character
	 *
	 * @param out output stream
	 * @param c   character
	 * @param num number of repeats
	 */
	public void multiplePrint(PrintWriter out, char c, int num)
	{
		for (int i = 0; i < num; i++)
		{
			out.print(c);
		}
	}

	/**
	 * returns of string of a given length of a single character.
	 *
	 * @param size length of the string required
	 * @param c   character
	 */
	public static String space(int size, char c) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < size; i++) {
			sb.append(c);
		}		
		return new String(sb);
	}


	//
	// Private stuff
	//
	
	// private constructor
	private FormattedOutput()
	{
		nf = NumberFormat.getInstance(Locale.UK);
		nf.setGroupingUsed(false);
	}
	
	private static FormattedOutput singleton = null;
	
	private NumberFormat nf;
}

/**
 * helper class
 */
class SFStringInfo
{
	String string;
	boolean carry;

	public SFStringInfo(String s, boolean c)
	{
		this.string = s;
		this.carry = c;
	}
}

