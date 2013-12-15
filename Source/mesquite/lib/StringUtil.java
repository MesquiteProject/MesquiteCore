/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
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

import java.util.Date;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;


/* ======================================================================== */
/** A set of static methods for Strings, independent of NEXUS parsing rules*/
public class StringUtil {
	static String lineSeparator;
	static {
		/*
		if (MesquiteTrunk.isMacOSXJaguar())
			lineSeparator = "\r";
		else */
		lineSeparator = System.getProperty("line.separator"); //"\n"
	}

	public static FontMetrics defaultFontMetrics = null; 
	// standard line ends:  UNIX: LF (\n),   Mac: CR (\r),   DOS:  CR LF (\r\n)
	public static final String defaultLineEnd = "\r\n";
	public static final String defaultWhitespace = " \t\n\r\f\b" + (char)0;
	public static final String defaultPunctuation = "(){}:,;-+<>=\\*/\''\"[]"; //Note: & isn't punctuation by NEXUS format //[] added Nov 03
	public static final String defaultPunctuationPlusLB = defaultPunctuation + "\n\r"; //Note: & isn't punctuation by NEXUS format //[] added Nov 03
	public static final String xmlPunctuation = "(){}:,;+<>=\\*/\''\"[]$%#^";
	public static final String allPunctuation = "(){}:,;+<>=\\*/\''\"[]$%#^&@!-|";
	public static final char defaultQuote = '\''; 
	public static final String digits = "0123456789";
	public static final char argumentMarker = '@'; 

	/*.................................................................................................................*
	public static String getUniqueObjectID(){
		return "Mesquite"+ MesquiteTrunk.mesquiteTrunk.getVersion() + URL+startupTimeMillis;
	}
	/*.................................................................................................................*/
	public static String getDateDayOnly() {
		long startupTime = System.currentTimeMillis();
		Date dnow = new Date(startupTime);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		return sdf.format(dnow);
	}
	/*.................................................................................................................*/
	public static String getDateTime(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMMMM yyyy,  HH:mm z");
		return sdf.format(date);
	}
	/*.................................................................................................................*/
	public static String getDateTime() {
		long startupTime = System.currentTimeMillis();
		Date dnow = new Date(startupTime);
		return getDateTime(dnow);
	}
	/*.................................................................................................................*/
	public static String getOutputFileStamp(MesquiteModule m) {
		StringBuffer sb = new StringBuffer();
		sb.append("Mesquite version " + m.getMesquiteVersion() + m.getBuildVersion() + "\n");
		sb.append(StringUtil.getDateTime());
		return sb.toString();
	}

	/*.................................................................................................................*/
	public static String secondsToHHMMSS(int sec) {

		int hours = sec / 3600;
		int remainder = sec % 3600;
		int minutes = remainder / 60;
		int seconds = remainder % 60;

		return ( (hours < 10 ? "0" : "") + hours + ":" + (minutes < 10 ? "0" : "") + minutes + ":" + (seconds< 10 ? "0" : "") + seconds );

	}
	/*.................................................................................................................*/
	public static boolean charAtIsASCIIValue(String s, int index, int asciiValue){
		if (index<0 || index >=s.length())
			return false;
		return s.charAt(index) == (char)asciiValue;
	}
	/*.................................................................................................................*/
	public static boolean charAtIsTab(String s, int index){
		return charAtIsASCIIValue(s,index,9);
	}
	public static boolean firstStringIsGreaterThan(String one, String two){
		Collator collator = Collator.getInstance();
		return collator.compare(one, two) > 0;
	}
	/*.................................................................................................................*/
	public static String lineEnding(){
		return lineSeparator;
	}
	public static boolean getIsNumeric(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static void highlightString(Graphics g, String s, int x, int y, Color fore, Color back){
		if (s==null)
			return;
		Color c = g.getColor();
		if (back!=null)
			g.setColor(back);
		boolean done = false;
		if (MesquiteTrunk.isMacOSXLeopard()){
			int w = StringUtil.getStringDrawLength(g, s);
			int h = StringUtil.getTextLineHeight(g);
			FontMetrics fm = getFontMetrics(g);
			if (fm!=null) {
				int ascent = fm.getAscent();
				int height = fm.getHeight();
				g.fillRect(x-1, y-ascent-1, w+2, height+2);
			}
			done = true;
		}
		if (!done) {
			//g.drawString(s, x,y+1);
			//g.drawString(s, x,y-1);
			//g.drawString(s, x+1,y);
			//g.drawString(s, x-1,y);
			g.drawString(s, x+1,y+1);
			g.drawString(s, x-1,y+1);
			g.drawString(s, x+1,y-1);
			g.drawString(s, x-1,y-1);
		}
		g.setColor(fore);
		g.drawString(s, x,y);
		if (c!=null) g.setColor(c);
	}

	/*.................................................................................................................*/
	public static String wrap(String original, int width){
		String s = "";
		for (int i=0; i<=original.length()/width; i++) {
			if ((i+1)*width>original.length())
				s += original.substring(i*width, original.length()) + lineEnding();
			else
				s += original.substring(i*width, (i+1)*width) + lineEnding();
		}
		return s;
	}

	/*.................................................................................................................*/
	/** truncates string if bigger than given size*/
	public static String truncateIfLonger(String line, int max) {
		if (line==null)
			return null;
		if (line.length()<=max)
			return line;
		return line.substring(0, max);
	}
	/*.................................................................................................................*/
	/** removes piece from string*/
	public static String removePiece(String line, int start, int end) {
		if (line==null)
			return null;
		if (start<0)
			start =0;
		if (end>=line.length())
			end = line.length()-1;
		if (start>0 && end < line.length()-1)
			return line.substring(0, start-1) + line.substring(end+1, line.length());
		if (start==0 && end < line.length()-1)
			return line.substring(end+1, line.length());
		if (start>0 && end == line.length()-1)
			return line.substring(0, start-1);
		return "";
	}
	/*.................................................................................................................*/
	/** replaces all instance of the old character by the nnew character*/
	public static String delete(String line, char old) {
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		else if (line.indexOf(old)<0)
			return line;
		else {
			StringBuffer sb = new StringBuffer("");
			int len = line.length();
			for (int i=0; i<len; i++)
				if (line.charAt(i)!=old)
					sb.append(line.charAt(i));
			return sb.toString();
		}
	}
	/*.................................................................................................................*/
	/** replaces all instance of the old character by the nnew character*/
	public static String replace(String line, char old, char nnew) {
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		else if (line.indexOf(old)<0)
			return line;
		else {
			StringBuffer sb = new StringBuffer(line);
			int len = sb.length();
			for (int i=0; i<len; i++)
				if (sb.charAt(i)==old)
					sb.setCharAt(i, nnew);
			return sb.toString();
		}
	}
	/*.................................................................................................................*/
	public static int characterCount(String s,char c){
		if (s==null || s.length() == 0)
			return 0;
		int count = 0;
		for (int i=0; i<s.length(); i++)
			if (c == s.charAt(i))
				count++;
		return count;
	}
	/*.................................................................................................................*/
	public static String replace(String s,String from,String to){
		if (s == null || from == null)
			return null;
		if (to!=null && from.equals(to))
			return s;
		String newString = s;
		int indexCounter = 0;
		int pos = s.indexOf(from);
		while (pos>=indexCounter) {
			if (to == null) {
				newString = newString.substring(0, pos)+newString.substring(pos+from.length(), newString.length());
				indexCounter = pos;
			}
			else {
				newString = newString.substring(0, pos)+to+newString.substring(pos+from.length(), newString.length());
				indexCounter = pos +to.length();
			}
			pos = newString.indexOf(from, indexCounter);
		}
		return newString;
	}
	/*.................................................................................................................*/
	public static String stripStuttered(String s, String target){
		if (s == null || target == null)
			return null;
		String stuttered = target + target;
		String newString = s;
		int indexCounter = 0;
		int pos = s.indexOf(stuttered);
		while (pos>=indexCounter) {
			newString = newString.substring(0, pos)+newString.substring(pos+stuttered.length(), newString.length());
			indexCounter = pos;
			pos = newString.indexOf(stuttered, indexCounter);
		}
		return newString;
	}
	/*.................................................................................................................*/
	public static String stripRedundantLineSeparators(String s){
		if (s == null)
			return null;
		String newString = stripStuttered(s, "\r\n");
		newString = stripStuttered(newString, "\n");
		newString = stripStuttered(newString, "\r");
		return newString;
	}
	/*.................................................................................................................*/
	/** returns all text between startBoundary through endBoundary*/
	public static String getFirstSubString(String line,String startText, String endText)
	{
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		else if (startText ==null ||  endText ==null || line.indexOf(startText)<0 || line.indexOf(endText)<0)
			return "";
		else {
			int placeStart = line.indexOf(startText)+startText.length();
			int placeEnd = line.indexOf(endText,placeStart);  // find ending portion
			if (placeEnd<0)
				return line;
			StringBuffer sb = new StringBuffer(placeEnd-placeStart+1);
			for (int i=placeStart; i<placeEnd; i++)
				sb.append(line.charAt(i));
			return sb.toString();
		}
	}
	public static boolean foundIgnoreCase(String compared, String target){
		if (compared == null || target == null)
			return false;
		int tlength = target.length();
		int clength = compared.length();
		if (tlength>clength) //can't be contained
			return false;
		for (int i=0; i<=clength-tlength; i++) {
			if (compared.regionMatches(true, i, target, 0, tlength))
				return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	/** replaces first instance of the all text in startOld through endOld inclusive by the new substring*/
	public static String replaceFirst(String line,String startOld, String endOld,String nnew)
	{
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		else if (startOld ==null ||  endOld ==null || line.indexOf(startOld)<0 || line.indexOf(endOld)<0)
			return line;
		else {
			int place = line.indexOf(startOld);
			int placeEnd = line.indexOf(endOld,place+startOld.length());  // find ending portion
			if (placeEnd<0)
				return line;
			int len = line.length();
			StringBuffer sb = new StringBuffer(line.length());
			for (int i=0; i<place; i++)
				sb.append(line.charAt(i));
			if (nnew!=null)
				for (int i=0; i<nnew.length(); i++)
					sb.append(nnew.charAt(i));
			for (int i=placeEnd+endOld.length(); i<len; i++)
				sb.append(line.charAt(i));
			return sb.toString();
		}
	}
	/*.................................................................................................................*/
	/** replaces first instance of the old substring by the nnew substring*/
	public static String replaceFirst(String line, String old, String nnew) {
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		else if (old ==null || line.indexOf(old)<0)
			return line;
		else {
			int place = line.indexOf(old);
			int len = line.length();
			StringBuffer sb = new StringBuffer(line.length());
			for (int i=0; i<place; i++)
				sb.append(line.charAt(i));
			if (nnew!=null)
				for (int i=0; i<nnew.length(); i++)
					sb.append(nnew.charAt(i));
			for (int i=place+old.length(); i<len; i++)
				sb.append(line.charAt(i));
			return sb.toString();
		}
	}
	/*.................................................................................................................*/
	/** returns the first item in a string, separated into parts by the separator*/
	public static String getFirstItem(String line, String separator) {
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		String sep = defaultWhitespace;
		if (separator!=null)
			sep = separator;
		int posStart = 0;
		StringBuffer sb = new StringBuffer(line.length());
		boolean startSaving = false;
		char c;
		for (int i=0; i<line.length(); i++) {
			c = line.charAt(i);
			if (sep.indexOf(c)<0)  //this character is not whitespace
				startSaving = true;
			if (startSaving) {
				if (sep.indexOf(c)>=0)  //this character is whitespace
					break;
				sb.append(line.charAt(i));
			}
		}
		return sb.toString();
	}
	/*.................................................................................................................*/
	/** returns the last item in a string, separated into parts by the separator*/
	public static String getLastItem(String line, String separator) {
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		return line.substring(line.lastIndexOf(separator)+1, line.length());
	}
	/*.................................................................................................................*/
	/** returns the last item in a string, separated into parts by whichever is last of the two separators*/
	public static String getLastItem(String line, String separator1, String separator2) {
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		int last = MesquiteInteger.maximum(line.lastIndexOf(separator1), line.lastIndexOf(separator2));
		return line.substring(last+1, line.length());
	}
	/*.................................................................................................................*/
	/** returns the  item number "index" in a string, separated into parts by the separator.  NOT 0-based*/
	public static String getItem(String line, String separator, int index) {
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		if (index==1)
			return getFirstItem(line, separator);
		String s = line;
		for (int i=1; i<index; i++){
			s=s.substring(s.indexOf(separator)+1);
			if (StringUtil.blank(s))
				return null;
		}
		if (s.indexOf(separator)>=0)
			return s.substring(0,s.indexOf(separator));
		return s;
	}
	/*.................................................................................................................*/
	/** returns the  item in a string, JUST after beforeString up until afterString */
	public static String getPiece(String line, String beforeString, String afterString) {
		if (line==null || beforeString==null)
			return null;
		else if (line.equals(""))
			return "";
		String s = line;
		if (s.indexOf(beforeString)<0)
			return "";
		s=s.substring(s.indexOf(beforeString)+beforeString.length());
		if (s.indexOf(afterString)>=0)
			return s.substring(0,s.indexOf(afterString));
		return s;
	}

	/*.................................................................................................................*/
	/** returns everything in front of the last item in a string, separated into parts by the separator*/
	public static String getAllButLastItem(String line, String separator) {
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		else if (line.lastIndexOf(separator) <0)
			return line;
		return line.substring(0, line.lastIndexOf(separator));
	}
	/*.................................................................................................................*/
	/** returns everything in front of the last item in a string, separated into parts by whichever is last of the two separators*/
	public static String getAllButLastItem(String line, String separator1, String separator2) {
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		int last = MesquiteInteger.maximum(line.lastIndexOf(separator1), line.lastIndexOf(separator2));
		if (last <0 )
			return line;
		return line.substring(0, last);
	}
	/*.................................................................................................................*/
	/** returns everything in a string AFTER a particular substring; returns empty string if substring not present*/
	public static String getAllAfterSubString(String line, String subString) {
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		int loc=line.indexOf(subString);
		if (loc<0)
			return "";
		loc += subString.length();
		return line.substring(loc);
	}
	/*.................................................................................................................*/
	/** returns the reverse of a String*/
	public static String reverse(String s) {
		if (s==null)
			return null;
		StringBuffer sb = new StringBuffer(s);
		return sb.reverse().toString();
	}
	/*.................................................................................................................*/
	public static byte[] pascalByteArrayFromString(String s){
		byte[] nameBytes = s.getBytes();
		byte[] pstrName = new byte[nameBytes.length + 1];
		System.arraycopy(nameBytes, 0, pstrName, 1, nameBytes.length);
		pstrName[0] = (byte)nameBytes.length;
		return pstrName;
	}
	/*.................................................................................................................*/
	public static String stringFromPascalByteArray(byte[] nameBytes){
		byte[] pstrName = new byte[nameBytes.length - 1];
		System.arraycopy(nameBytes, 1, pstrName, 0, nameBytes.length-1);
		String s = new String(pstrName);
		return s;
	}
	/*...............................................................................................................*/
	private static FontMetrics getFontMetrics(Component c){ //built because of crashes in Linux & Windows
		if (c==null)
			return null;
		try {
			FontMetrics fm = c.getFontMetrics(c.getFont());
			return fm;
		}
		catch (Exception e){
		}
		return null;
	}
	private static FontMetrics getFontMetrics(Graphics g){ //built because of crashes in Linux & Windows
		if (g==null)
			return null;
		try {
			FontMetrics fm = g.getFontMetrics();
			return fm;
		}
		catch (Exception e){
		}
		return null;
	}
	/*...............................................................................................................*/

	public static int getStringVertPosition(Graphics g, int top, int height, MesquiteBoolean overflow) {
		FontMetrics fm = getFontMetrics(g);
		return getStringVertPosition(fm, top, height, overflow);
	}
	public static int getStringVertPosition(FontMetrics fm, int top, int height, MesquiteBoolean overflow) {
		if (fm!=null) {
			if (overflow != null)
				overflow.setValue(height<fm.getAscent()+fm.getDescent());
			return top+height-(height-fm.getAscent()-fm.getDescent())/2-fm.getDescent();
		}
		else
			return top+height-MesquiteString.riseOffset;
	}
	/*...............................................................................................................*/
	public static int getStringCenterPosition(String s, Graphics g, int left, int width, MesquiteBoolean overflow){
		FontMetrics fm = getFontMetrics(g);
		return getStringCenterPosition(s, fm, left, width, overflow);
	}
	/*...............................................................................................................*/
	public static int getStringCenterPosition(String s, FontMetrics fm, int left, int width, MesquiteBoolean overflow){
		if (fm!=null) {
			if (overflow != null)
				overflow.setValue(width<fm.stringWidth(s));
			return left+(width-fm.stringWidth(s))/2;
		}
		else
			return left+2;
	}
	/*...............................................................................................................*/
	public static int getStringDrawLength(Graphics g, String s){
		if (g!=null) {
			FontMetrics fm = getFontMetrics(g);
			if (fm!=null) 
				return fm.stringWidth(s);
		}
		if (defaultFontMetrics!=null) 
			return defaultFontMetrics.stringWidth(s); //added Dec 2001
		return 0;
	}
	/*...............................................................................................................*/
	public static int getStringDrawLength(Component component, String s){
		if (s == null)
			return 0;
		if (component!=null) {
			FontMetrics fm = getFontMetrics(component);
			if (fm== null)
				fm = defaultFontMetrics; //added Dec 2001
			if (fm!=null) {
				int sum = 0;
				for (int i=0; i<s.length(); i++)
					sum += fm.charWidth(s.charAt(i));
				return sum; //this is done instead of stringWidth because of apparent bug when stringWidth > 32767, possibly only on Mac
			}
		}
		return 0;
	}
	/*...............................................................................................................*/
	public static int getTextLineHeight(Graphics g){
		if (g!=null) {
			FontMetrics fm = getFontMetrics(g);
			if (fm!=null) 
				return fm.getHeight();
		}
		return 0;
	}
	/*...............................................................................................................*/
	public static int getMaxCharWidth(Component component){
		if (component!=null) {
			FontMetrics fm = getFontMetrics(component);
			if (fm!=null) 
				return fm.getMaxAdvance();
		}
		return 0;
	}
	/*...............................................................................................................*/
	public static int getTextLineHeight(Component component){
		if (component!=null) {
			FontMetrics fm = getFontMetrics(component);
			if (fm!=null) 
				return fm.getHeight();
		}
		return 0;
	}
	/*.................................................................................................................*/
	public static boolean titled(String line) {
		if (blank(line))
			return false;
		else if (line.equalsIgnoreCase("untitled"))
			return false;
		else
			return true;
	}
	public static String deTokenize(String token) {
		if (token == null)
			return null;
		if (token.indexOf('_')>=0) {
			StringBuffer sb = new StringBuffer(token);
			for (int i=0; i<token.length(); i++)
				if (sb.charAt(i) == '_')
					sb.setCharAt(i, ' ');
			return sb.toString();
		}
		else
			return token;
	}
	/*.................................................................................................................*/
	//TODO: quote should not be punctuation, and should be treated separately!!!!
	private static String quote(String token, StringBuffer sb) {
		if (token == null)
			return "";
		if (token.indexOf(defaultQuote)>=0) {
			if (sb == null)
				sb = new StringBuffer(token);
			else {
				sb.setLength(0);
				sb.append(token);
			}
			int extra = 0;
			for (int i=0; i<token.length()+extra; i++) {
				if (sb.charAt(i) == defaultQuote) {
					sb.insert(i, defaultQuote);
					extra ++;
					i++;
				}
				else if (sb.charAt(i) == '\n' || sb.charAt(i) == '\r') {  //to reduce all to the same (^r was not recognized in Windows)
					sb.setCharAt(i, '^');
					sb.insert(i+1, 'n');
					extra ++;
					i++;
				}
				/*	else if (sb.charAt(i) == '\r') {
					sb.setCharAt(i, '^');
					sb.insert(i+1, 'r');
					extra ++;
					i++;
				}
				 */
			}
			return "'" + sb.toString() + defaultQuote;
		}
		else if (token.indexOf('\n')>=0 || token.indexOf('\r')>=0) {
			if (sb == null)
				sb = new StringBuffer(token);
			else {
				sb.setLength(0);
				sb.append(token);
			}
			int extra = 0;
			for (int i=0; i<token.length() + extra; i++)
				if (sb.charAt(i) == '\n' || sb.charAt(i) == '\r') {//to reduce all to the same (^r was not recognized in Windows)
					sb.setCharAt(i, '^');
					sb.insert(i+1, 'n');
					i++;
					extra ++;
				}
			/*else if (sb.charAt(i) == '\r') {
					sb.setCharAt(i, '^');
					sb.insert(i+1, 'r');
					i++;
					extra ++;
				}*/
			return "'" + sb.toString() + defaultQuote;
		}
		else 
			return  "'" + token + defaultQuote;
	}
	/*.................................................................................................................*/
	public static String tokenize(String token, String punctuationString, StringBuffer sb) {
		if (token == null)
			return "";
		if (hasPunctuation(token,punctuationString) || token.indexOf("_")>=0 || hasSpecial(token))  //28Feb05: added "_" so that this forced quoted token to preserve the underscore
			return quote(token, sb);  
		if (sb == null)
			sb = new StringBuffer(token);
		else {
			sb.setLength(0);
			sb.append(token);
		}
		int extra = 0;
		for (int i=0; i<token.length()+extra; i++)
			if (sb.charAt(i) == ' ')
				sb.setCharAt(i, '_');
			else if (sb.charAt(i) == '\n' || sb.charAt(i) == '\r') {//to reduce all to the same (^r was not recognized in Windows)
				sb.setCharAt(i, '^');
				sb.insert(i+1, 'n');
				extra ++;
				i++;
			}
		/*	else if (sb.charAt(i) == '\r'){
				sb.setCharAt(i, '^');
				sb.insert(i+1, 'r');
				extra ++;
				i++;
			}*/
		return sb.toString();
	}
	/*.................................................................................................................*/
	public static String tokenize(String token, String punctuationString) {
		return tokenize(token, punctuationString, null);
	}
	/*.................................................................................................................*/
	public static String tokenize(String token) {
		return tokenize(token, null,null);
	}
	/*.................................................................................................................*/
	public static int getNumMatchingChars(String token, char c) {
		if (token==null) return 0;
		int numMatches = 0;
		for (int i=0; i<token.length(); i++) {
			if (token.charAt(i)==c) 
				numMatches++;
		}
		return numMatches;
	}
	/*.................................................................................................................*/
	public static String punctuationToUnderline(String token) {
		if (token==null)
			return null;
		else if (token.equals(""))
			return "";
		StringBuffer sb = new StringBuffer(token);
		for (int i = 0; i<token.length(); i++) {
			if (allPunctuation.indexOf(sb.charAt(i))>=0)
				sb.setCharAt(i,'_');
		}
		return sb.toString();
	}
	/*.................................................................................................................*/
	public static String underlineToBlanks(String token) {
		if (token==null)
			return null;
		else if (token.equals(""))
			return "";
		StringBuffer sb = new StringBuffer(token);
		for (int i = 0; i<token.length(); i++) {
			if (sb.charAt(i)=='_')
				sb.setCharAt(i,' ');
		}
		return sb.toString();
	}
	/*.................................................................................................................*/
	public static String blanksToUnderline(String token, String blanks) {
		if (token==null)
			return null;
		else if (token.equals(""))
			return "";
		StringBuffer sb = new StringBuffer(token);
		for (int i = 0; i<token.length(); i++) {
			if (blanks==null) {
				if (defaultWhitespace.indexOf(sb.charAt(i))>=0)
					sb.setCharAt(i,'_');
			}
			else if (blanks.indexOf(sb.charAt(i))>=0)
				sb.setCharAt(i,'_');
		}
		return sb.toString();
	}
	/*.................................................................................................................*/
	public static String blanksToUnderline(String token) {
		return blanksToUnderline(token, null);
	}
	/*.................................................................................................................*/
	public static String removeLastCharacterIfMatch(String token, char c) {
		String timmedString = token.trim();
		if (token == null)
			return "";
		String trimmedString = token.trim();
		int charPos = trimmedString.lastIndexOf((int)c) ;  
		if (charPos == trimmedString.length()-1)   // then the last none-whitespace is the character c
			return token.substring(0,token.lastIndexOf((int)c)-1);
		else
			return token;
	}
	/*.................................................................................................................*/
	public static String removeCharacters(String token, String characters) {
		if (token == null)
			return "";
		if (characters == null || characters.length()<1)
			return token;
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<token.length(); i++) {
			char c = token.charAt(i);
			if (characters.indexOf(c)<0)
				sb.append(c);
		}
		return sb.toString();
	}
	/*.................................................................................................................*/
	public static String stripWhitespace(String token) { 
		if (token == null)
			return "";
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<token.length(); i++) {
			char c = token.charAt(i);
			if (defaultWhitespace.indexOf(c)<0){
				sb.append(c);
			}
		}
		return sb.toString();
	}
	/*.................................................................................................................*/
	public static String stripLeadingWhitespace(String token) { //added 22 Dec 01
		if (token == null)
			return "";
		int firstDark = -1;
		for (int i=0; i<token.length(); i++) {
			char c = token.charAt(i);
			if (defaultWhitespace.indexOf(c)<0){
				firstDark = i;
				break;
			}
		}
		if (firstDark == -1)
			return "";
		else
			return token.substring(firstDark, token.length());
	}
	/*.................................................................................................................*/
	public static String stripTrailingWhitespace(String token) {
		if (token == null)
			return "";
		int firstDark = -1;
		for (int i=token.length()-1;  i>=0; i--) {
			char c = token.charAt(i);
			if (defaultWhitespace.indexOf(c)<0){
				firstDark = i;
				break;
			}
		}
		if (firstDark == -1)
			return "";
		else
			return token.substring(0, firstDark+1);
	}
	/*.................................................................................................................*/
	public static String stripTrailingWhitespaceAndPunctuation(String token) {
		if (token == null)
			return "";
		int firstDark = -1;
		for (int i=token.length()-1;  i>=0; i--) {
			char c = token.charAt(i);
			if (!(defaultWhitespace.indexOf(c)>=0 ||  defaultPunctuation.indexOf(c)>=0)){  // not (whitespace or punctuation)
				firstDark = i;
				break;
			}
		}
		if (firstDark == -1)
			return "";
		else
			return token.substring(0, firstDark+1);
	}
	/*.................................................................................................................*/
	public static String stripBoundingWhitespace(String token) {
		String s = stripLeadingWhitespace(token);
		return stripTrailingWhitespace(s);
	}
	/*.................................................................................................................*/
	/** Removes all new line characters from the string. */
	public static String removeNewLines(String line) {
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		else if (line.indexOf("\r")<0 && line.indexOf("\n")<0)
			return line;
		else {   // there's at least one \r or \n
			line = removeLastCharacterIfMatch(line,'\n');  // remove trailing \n
			line = removeLastCharacterIfMatch(line,'\r');    // remove trailing \r
			if (line.indexOf("\r")<0 && line.indexOf("\n")<0)  // check to see if there are any left
				return line;
			StringBuffer sb = new StringBuffer(line);
			int len = sb.length();
			for (int i=0; i<len; i++)
				if (standardLineEndCharacter(sb.charAt(i)))
					sb.setCharAt(i, ' ');
			line = sb.toString().trim();
			return line;
		}
	}

	/*.................................................................................................................*/
	public static boolean hasPunctuation(String token, String punctuationString) {
		if (token == null)
			return false;
		for (int i=0; i<token.length(); i++)
			if (punctuation (token.charAt(i), punctuationString))
				return true;
		return false;
	}
	/*.................................................................................................................*/
	public static boolean hasPunctuation(String token) {
		return hasPunctuation(token, null);
	}
	/*.................................................................................................................*/
	public static boolean hasSpecial(String token) {
		if (token == null)
			return false;
		for (int i=0; i<token.length(); i++) {
			char c = token.charAt(i);
			if ('^'==c || '\n'==c || '\t'==c || '\r'==c)
				return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	protected static boolean openingSquareBracket(char c) {
		return (c=='[');
	}
	/*.................................................................................................................*/
	protected static boolean closingSquareBracket(char c) {
		return (c==']');
	}
	/*.................................................................................................................*
	public static int numTokens(String s, boolean useNEXUSRules) {
		if (blank(s))
			return 0;
		int num=0;
		int pos=0;
		if (useNEXUSRules) {
			MesquiteMessage.warnProgrammer("numTokens doesn't support NEXUS rules yet!!!");
		}
		else 
			try {
				while (pos<s.length()) {
					if (!whitespace(s.charAt(pos),defaultWhitespace)) {// we are at start of a new token
						num++;
						pos++;
						while (!whitespace(s.charAt(pos),defaultWhitespace))  // go through until we hit whitespace again
							pos++;
					}
					pos++;
				}
			}
			catch (Exception e) {
			}
		return num;
	}
	/*.................................................................................................................*/
	public static boolean whitespace(char c, String whitespaceString) {
		// in the future, we might want to add  if (c<0) return true;
		if (c<0)
			return true;
		if (c==0)
			return false;  


		if (whitespaceString!=null)
			return whitespaceString.indexOf(c)>=0;
			return defaultWhitespace.indexOf(c)>=0;
	}
	/*.................................................................................................................*/
	public static boolean punctuation(char c, String punctuationString) {
		if (c==0)
			return false;
		else if (punctuationString!=null)
			return punctuationString.indexOf(c)>=0;
			else  //NOTE:  * is assumed not to be punctuation!!!!!!!!!!!!!!!  at least, if this changes, search for it (ManageMesquiteBlock)
				return defaultPunctuation.indexOf(c)>=0;
	}
	/*.................................................................................................................*/
	public static boolean punctuationOrQuote(char c, String punctuationString, char quotechar) {
		if (c==0)
			return false;
		else if (c==quotechar)
			return true;
		else if (punctuationString!=null)
			return punctuationString.indexOf(c)>=0;
			else  //NOTE:  * is assumed not to be punctuation!!!!!!!!!!!!!!!  at least, if this changes, search for it (ManageMesquiteBlock)
				return defaultPunctuation.indexOf(c)>=0;
	}
	/*.................................................................................................................*/
	/** Takes a string containing NEXUS tokens and converts these to a String[], ignoring punctuation if asked */
	public static String[] tokenListToStrings(String s, boolean skipPunctuation){
		int count=0;
		Parser parser = new Parser(s);
		String token = parser.getNextToken();
		while (!StringUtil.blank(token)) {
			if (!skipPunctuation || defaultPunctuation.indexOf(token)<0)
				count++;
			token = parser.getNextToken();
		}
		String[] strings =new String[count];
		parser.setPosition(0);
		count=0;
		token = parser.getNextToken();
		while (!StringUtil.blank(token)) {
			if (!skipPunctuation || defaultPunctuation.indexOf(token)<0) {
				strings[count] = token;
				count++;
			}
			token = parser.getNextToken();
		}
		return strings;
	}
	/*-----------------------------------------------------------*/
	public static String[] delimitedTokensToStrings(String line, char delimiter){
		if (delimiter == '\t')
			return tabDelimitedTokensToStrings(line);
		String[] result;

		StringTokenizer t = new StringTokenizer(line, "" + delimiter);
		String tok = null;
		int count = t.countTokens();
		result = new String[count];
		tok = null;
		count = 0;
		try{
			while (t.hasMoreTokens()){
				tok = t.nextToken();
				if (tok == null)
					tok = "";
				else
					tok = stripBoundingWhitespace(tok);
				result[count] = tok;
				count++;
			}
		}
		catch (NoSuchElementException e){
		}
		return result;
	}
	/*-----------------------------------------------------------*/
	public static String[] tabDelimitedTokensToStrings(String line){
		String[] result;
		if (line.indexOf("\t\t")>=0){
			StringBuffer sb = new StringBuffer(line);
			boolean prev = false;
			for (int i=sb.length()-1; i>=0; i--){
				if (sb.charAt(i) == '\t'){
					if (prev){
						sb.insert(i+1, ' ');
					}
					prev = true;
				}
				else
					prev = false;

			}
			line = sb.toString();
		}

		StringTokenizer t = new StringTokenizer(line, "\t");
		String tok = null;
		int count = t.countTokens();
		result = new String[count];
		tok = null;
		count = 0;
		try{
			while (t.hasMoreTokens()){
				tok = t.nextToken();
				if (tok == null)
					tok = "";
				result[count] = tok;
				count++;
			}
		}
		catch (NoSuchElementException e){
		}
		return result;
	}
	/*.................................................................................................................*/
	public static String encodeForURL(String s){
		if (s==null) return null;
		StringBuffer buffer = new StringBuffer(s.length()*2);
		for (int i=0; i<s.length(); i++) {
			if (Character.isSpaceChar(s.charAt(i))) 
				buffer.append("%20");
			else if (s.charAt(i)=='>')
				buffer.append("%3E");
			else if (s.charAt(i)=='<')
				buffer.append("%3C");
			else if (s.charAt(i)=='"')
				buffer.append("%22");
			else if (s.charAt(i)=='#')
				buffer.append("%23");
			else if (s.charAt(i)=='%')
				buffer.append("%25");
			else if (s.charAt(i)=='{')
				buffer.append("%7B");
			else if (s.charAt(i)=='}')
				buffer.append("%7D");
			else if (s.charAt(i)=='|')
				buffer.append("%7C");
			else if (s.charAt(i)=='\\')
				buffer.append("%5C");
			else if (s.charAt(i)=='^')
				buffer.append("%5E");
			else if (s.charAt(i)=='~')
				buffer.append("%7E");
			else if (s.charAt(i)=='[')
				buffer.append("%5B");
			else if (s.charAt(i)==']')
				buffer.append("%5D");
			else if (s.charAt(i)=='`')
				buffer.append("%60");
			else
				buffer.append(s.charAt(i));
		}
		return buffer.toString();
	}

	public static String protectForWindows(String s) {
		String returnVal = protectForUnix(s, false);
		return "\"" + returnVal + "\"";
	}

	public static String protectForUnix(String s, boolean escapeSpaces) {
		if (s==null) return null;
		StringBuffer buffer = new StringBuffer(s.length()*2);
		for (int i=0; i<s.length(); i++) {
			if (escapeSpaces && Character.isSpaceChar(s.charAt(i)))
				buffer.append("\\ ");
			else if (s.charAt(i)=='&')
				buffer.append("\\&");
			else if (s.charAt(i)=='!')
				buffer.append("\\!");
			else if (s.charAt(i)=='[')
				buffer.append("\\[");
			else if (s.charAt(i)==']')
				buffer.append("\\]");
			else if (s.charAt(i)=='"')
				buffer.append("\\\"");
			else if (s.charAt(i)=='(')
				buffer.append("\\(");
			else if (s.charAt(i)==')')
				buffer.append("\\)");
			else if (s.charAt(i)=='|')
				buffer.append("\\|");
			else if (s.charAt(i)=='\'')
				buffer.append("\\'");
			else if (s.charAt(i)=='/')
				buffer.append("\\/");
			else if (s.charAt(i)=='“')
				buffer.append("\\\"");
			else if (s.charAt(i)=='”')
				buffer.append("\\\"");
			else if (stripAccent(s.charAt(i))!= 0)
				buffer.append(stripAccent(s.charAt(i)));
			else
				buffer.append(s.charAt(i));
		}
		return buffer.toString();  		 
	}
	private static char stripAccent(char a){
		if ((int)a==65533)
			return '_';
		if (a=='é')
			return 'e';
		else if (a=='á')
			return 'a';
		else if (a=='ó')
			return 'o';
		else if (a=='ú')
			return 'u';
		else if (a=='í')
			return 'i';
		else if (a=='É')
			return 'E';
		else if (a=='Á')
			return 'A';
		else if (a=='Í')
			return 'I';
		else if (a=='Ó')
			return 'O';
		else if (a=='Ú')
			return 'U';

		else if (a=='è')
			return 'e';
		else if (a=='à')
			return 'a';
		else if (a=='ò')
			return 'o';
		else if (a=='ù')
			return 'u';
		else if (a=='ì')
			return 'i';
		else if (a=='È')
			return 'E';
		else if (a=='À')
			return 'A';
		else if (a=='Ì')
			return 'I';
		else if (a=='Ò')
			return 'O';
		else if (a=='Ù')
			return 'U';

		else if (a=='ë')
			return 'e';
		else if (a=='ä')
			return 'a';
		else if (a=='ö')
			return 'o';
		else if (a=='ü')
			return 'u';
		else if (a=='ï')
			return 'i';
		else if (a=='Ë')
			return 'E';
		else if (a=='Ä')
			return 'A';
		else if (a=='Ï')
			return 'I';
		else if (a=='Ö')
			return 'O';
		else if (a=='Ü')
			return 'U';

		else if (a=='ø')
			return 'o';

		else if (a=='Ø')
			return 'Ø';

		else if (a=='ñ')
			return 'n';
		else if (a=='Ñ')
			return 'N';
		else if (a=='ç')
			return 'C';
		else if (a=='Ç')
			return 'C';
		return 0;
	}
	/*.................................................................................................................*/
	public static String protectForUnix(String s){
		return protectForUnix(s, true);
	}


	/* For a description of how to write simple, one-level deep XML preferences, search for "XMLPreferencesDocumentation" in MesquiteModule */
	/*.................................................................................................................*/
	public static String protectForXML(String s) {
		//TODO: finish this !!
		if (blank(s))
			return s;
		StringBuffer buffer = new StringBuffer(s.length()*2);
		for (int i=0; i<s.length(); i++) {
			if (s.charAt(i)=='&')
				buffer.append("&amp;");
			else if (s.charAt(i)=='<')
				buffer.append("&lt;");
			else if (s.charAt(i)=='>')
				buffer.append("&gt;");
			else if (s.charAt(i)=='\'')
				buffer.append("&apos;");
			else if (s.charAt(i)=='"')
				buffer.append("&quot;");
			else
				buffer.append(s.charAt(i));
		}
		return buffer.toString();  		 
	}
	/*.................................................................................................................*/
	public static String cleanXMLEscapeCharacters(String s) {
		//TODO: finish this !!
		if (s==null)
			return "";
		else if (blank(s))
			return s;
		String newString = replace(s,"&amp;","&");
		newString = replace(newString,"&lt;","<");
		newString = replace(newString,"&gt;",">");
		newString = replace(newString,"&apos;","'");
		newString = replace(newString,"&quot;","\"");
		return newString;  		 
	}

	/*.................................................................................................................*/
	public static void appendStartOfXMLFile(StringBuffer sb){
		sb.append("<?xml version=\"1.0\"?>\n");
	}
	/*.................................................................................................................*/
	public static void appendStartXMLTag(StringBuffer sb, int numTabs, String tag, String attributeName, String attribute, boolean addLineFeed){
		if (sb!=null) {
			for (int i=1; i<=numTabs; i++){
				sb.append("\t");
			}
			sb.append("<");
			sb.append(tag);
			if (StringUtil.notEmpty(attributeName) && StringUtil.notEmpty(attribute)){
				sb.append(" " + attributeName +"=\"");
				sb.append(attribute);
				sb.append("\"");
			}
			sb.append(">");
			if (addLineFeed)
				sb.append("\n");
		}
	}
	/*.................................................................................................................*/
	public static void appendStartXMLTag(StringBuffer sb, int numTabs, String tag, boolean addLineFeed){
		appendStartXMLTag(sb,numTabs,tag, null, null, false);
	}
	/*.................................................................................................................*/
	public static void appendStartXMLTag(StringBuffer sb, int numTabs, String tag){
		appendStartXMLTag(sb,numTabs,tag,false);
	}
	/*.................................................................................................................*/
	public static void appendStartXMLTag(StringBuffer sb, int numTabs, String tag, String attributeName, String attribute){
		appendStartXMLTag(sb,numTabs,tag,attributeName, attribute, false);
	}
	/*.................................................................................................................*/
	public static void appendEndXMLTag(StringBuffer sb, int numTabs, String tag){
		if (sb!=null) {
			for (int i=1; i<=numTabs; i++){
				sb.append("\t");
			}
			sb.append("</");
			sb.append(tag);
			sb.append(">\n"); 
		}
	}
	/*.................................................................................................................*/
	public static void appendEndXMLTag(StringBuffer sb, String tag){
		if (sb!=null) {
			sb.append("</");
			sb.append(tag);
			sb.append(">\n"); 
		}
	}
	/*.................................................................................................................*/
	public static void appendXMLTag(StringBuffer sb, int numTabs, String tag, String content){
		if (sb!=null) {
			appendStartXMLTag(sb,numTabs,tag);
			if (content!=null)
				sb.append(StringUtil.protectForXML(content));
			appendEndXMLTag(sb,tag);
		}
	}
	/*.................................................................................................................*/
	public static void appendXMLTag(StringBuffer sb, int numTabs, String tag, String flavor, String content){
		if (sb!=null) {
			appendStartXMLTag(sb,numTabs,tag, XMLUtil.FLAVOR, StringUtil.protectForXML(flavor));
			if (content!=null)
				sb.append(StringUtil.protectForXML(content));
			appendEndXMLTag(sb,tag);
		}
	}
	/*.................................................................................................................*/
	public static void appendXMLTag(StringBuffer sb, int numTabs, String tag, String attributeName, String attribute, String content){
		if (sb!=null) {
			appendStartXMLTag(sb,numTabs,tag, attributeName, StringUtil.protectForXML(attribute));
			if (content!=null)
				sb.append(StringUtil.protectForXML(content));
			appendEndXMLTag(sb,tag);
		}
	}
	/*.................................................................................................................*/
	public static void appendXMLTag(StringBuffer sb, int numTabs, String tag, boolean value){
		if (sb!=null) {
			appendStartXMLTag(sb,numTabs,tag);
			sb.append(MesquiteBoolean.toTrueFalseString(value));
			appendEndXMLTag(sb,tag);
		}
	}
	/*.................................................................................................................*/
	public static void appendXMLTag(StringBuffer sb, int numTabs, String tag, MesquiteModule module){
		if (sb!=null) {
			appendStartXMLTag(sb,numTabs,tag);
			if (module!=null)
				sb.append(StringUtil.protectForXML(module.getClassName()));
			appendEndXMLTag(sb,tag);
		}
	}
	/*.................................................................................................................*/
	public static void appendXMLTag(StringBuffer sb, int numTabs, String tag, MesquiteBoolean value){
		if (sb!=null && value!=null) {
			appendStartXMLTag(sb,numTabs,tag);
			sb.append(MesquiteBoolean.toTrueFalseString(value.getValue()));
			appendEndXMLTag(sb,tag);
		}
	}
	/*.................................................................................................................*/
	public static void appendXMLTag(StringBuffer sb, int numTabs, String tag, double value){
		if (sb!=null&& MesquiteDouble.isCombinable(value)) {
			appendStartXMLTag(sb,numTabs,tag);
			sb.append(MesquiteDouble.toString(value));
			appendEndXMLTag(sb,tag);
		}
	}
	/*.................................................................................................................*/
	public static void appendXMLTag(StringBuffer sb, int numTabs, String tag, int value){
		if (sb!=null && MesquiteInteger.isCombinable(value)) {
			appendStartXMLTag(sb,numTabs,tag);
			sb.append(MesquiteInteger.toString(value));
			appendEndXMLTag(sb,tag);
		}
	}
	/*.................................................................................................................*/
	public static void appendXMLTag(StringBuffer sb, int numTabs, String tag, MesquiteInteger value){
		if (sb!=null && value!=null && value.isCombinable()) {
			appendStartXMLTag(sb,numTabs,tag);
			sb.append(value.toString());
			appendEndXMLTag(sb,tag);
		}
	}
	/*.................................................................................................................*/
	public static String deletePunctuationAndSpaces(String token) {
		if (token==null)
			return null;
		else if (token.equals(""))
			return "";
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i<token.length(); i++) {
			if (allPunctuation.indexOf(token.charAt(i))<0 && token.charAt(i) != ' ' && token.charAt(i) != '?' && token.charAt(i) != '.')
				sb.append(token.charAt(i));
		}
		return sb.toString();
	}
	/*.................................................................................................................*/
	public static String cleanseStringOfFancyChars(String s, boolean onlyAlphaNumeric, boolean alphaNumericAndUnderscore){
		if (s==null)
			return null;
		StringBuffer buffer = new StringBuffer(s.length()*2);
		for (int i=0; i<s.length(); i++) {
			if (onlyAlphaNumeric) {
				if ((s.charAt(i)>='0'&& s.charAt(i)<='9')||(s.charAt(i)>='a'&& s.charAt(i)<='z')||(s.charAt(i)>='A'&& s.charAt(i)<='Z'))
					buffer.append(s.charAt(i));
				else if (stripAccent(s.charAt(i))!= 0)
					buffer.append(stripAccent(s.charAt(i)));
			}
			else if (alphaNumericAndUnderscore) {
				if ((s.charAt(i)>='0'&& s.charAt(i)<='9')||(s.charAt(i)>='a'&& s.charAt(i)<='z')||(s.charAt(i)>='A'&& s.charAt(i)<='Z'))
					buffer.append(s.charAt(i));
				else if (stripAccent(s.charAt(i))!= 0)
					buffer.append(stripAccent(s.charAt(i)));
				else 
					buffer.append('_');
			}
			else if (s.charAt(i)=='“')
				buffer.append("\"");
			else if (s.charAt(i)=='”')
				buffer.append("\"");
			else if (stripAccent(s.charAt(i))!= 0)
				buffer.append(stripAccent(s.charAt(i)));
			else
				buffer.append(s.charAt(i));
		}
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public static String cleanseStringOfFancyChars(String s){
		return cleanseStringOfFancyChars(s,false, false);
	}
	/*.................................................................................................................*/
	public static String simplifyIfNeededForOutput(String s, boolean simplify){
		return simplifyIfNeededForOutput(s,simplify, false);
	}
	/*.................................................................................................................*/
	public static String simplifyIfNeededForOutput(String s, boolean simplify, boolean forMesquiteUse){
		String token = s;
		if (simplify) {
			token = cleanseStringOfFancyChars(token,false,true);
			if (forMesquiteUse)
				token = underlineToBlanks(token);
		}
		else
			token = tokenize(token);
		return token;
	}
	/*.................................................................................................................*/
	public static String cleansePath(String s){
		return cleanseStringOfFancyChars(s);
	}
	/*.................................................................................................................*/
	public boolean lineEndCharacter(char c) {
		return (c=='\r' || c=='\n');
	}
	/*.................................................................................................................*/
	public static boolean standardLineEndCharacter(char c) {
		return (c=='\r' || c=='\n');
	}
	/*.................................................................................................................*/
	public static boolean isPunctuationOrWhitespace(char c) {
		if (allPunctuation.indexOf(c)>=0)
			return true;
		if (defaultWhitespace.indexOf(c)>=0)
			return true;
		return false;
	}
	/*.................................................................................................................*/
	public static int indexOf(String a, String b, boolean caseSensitive, boolean wholeWord) {
		if (a == null || b == null)
			return -1;
		if (!caseSensitive) {
			a = a.toLowerCase();
			b = b.toLowerCase();
		}
		if (!wholeWord)
			return a.indexOf(b);
		else {
			if (a==b)
				return 0;
			int pos = 0;
			while (pos>=0) {
				pos = a.indexOf(b,pos);
				if (pos>=0) {
					if (pos==0){ // is at start of string
						if (b.length()<a.length()){  // there is more beyond the search string
							if (isPunctuationOrWhitespace(a.charAt(b.length())))
								return pos;
						}
					} else {
						boolean punctBefore = isPunctuationOrWhitespace(a.charAt(pos-1));
						if (punctBefore) {
							if (pos+b.length()<a.length()){  // there is more beyond the search string
								if (isPunctuationOrWhitespace(a.charAt(pos+b.length())))
									return pos;
							} else  // it is at the end of the string;
								return pos;
						}
					}
				} else
					return -1;
				if (pos==a.lastIndexOf(b))
					return -1;
				pos++;
			}
			return -1;
		}
	}
	/*.................................................................................................................*/
	public static int indexOfIgnoreCase(String a, String b) {
		if (a == null || b == null)
			return -1;
		a = a.toLowerCase();
		b = b.toLowerCase();
		return a.indexOf(b);
	}
	/*.................................................................................................................*/
	public static boolean containsIgnoreCase(String a, String b) {
		return indexOfIgnoreCase(a,b)>=0;
	}
	/*.................................................................................................................*/
	public static boolean stringsEqual(String a, String b) {
		if (a ==b)
			return true;
		if (a == null || b == null) 
			return false;
		return (a.equals(b));
	}
	/*.................................................................................................................*/
	public static boolean stringsEqualIgnoreCase(String a, String b) {
		if (a ==b)
			return true;
		if (a == null || b == null) 
			return false;
		return (a.equalsIgnoreCase(b));
	}
	/*.................................................................................................................*/
	public static boolean stringsEqualIgnoreCaseIgnoreBlanksUnderlines(String a, String b) {
		if (a ==b)
			return true;
		if (a == null || b == null) 
			return false;
		if (a.equalsIgnoreCase(b))
			return true;
		if (blanksToUnderline(a).equalsIgnoreCase(blanksToUnderline(b)))
			return true;
		return false;
	}
	/*.................................................................................................................*/
	public static boolean startsWithIgnoreCase(String a, String b) {
		if (a ==b)
			return true;
		if (a == null || b == null) 
			return false;
		if (b.length()> a.length())
			return false;
		for (int i = 0; i< b.length(); i++){
			char ca = Character.toLowerCase(a.charAt(i));
			char cb = Character.toLowerCase(b.charAt(i));
			if (ca != cb)
				return false;
		}
		return true;
	}
	/*.................................................................................................................*/
	public static String blankIfNull(String line) {
		if (line==null)
			return "";
		else 
			return line;
	}
	/*.................................................................................................................*/
	public static boolean blank(String line) {
		if (line==null)
			return true;
		else if (line.length()==0)
			return true;
		else {
			for (int i=0; i<line.length(); i++) {
				char c = line.charAt(i);
				if (c > 0 && !whitespace(c, null))
					return false;
			}
			return true;
		}
	}
	/*.................................................................................................................*/
	public static boolean notEmpty(String line) {
		return !blank(line);
	}
	/*.................................................................................................................*/
	public static boolean blankDebugg(String line) {
		if (line==null)
			return true;
		else if (line.length()==0)
			return true;
		else {
			for (int i=0; i<line.length(); i++) {
				char c = line.charAt(i);
				if (c > 0 && !whitespace(c, null) )
					return false;
			}
			return true;
		}
	}
	/*.................................................................................................................*/
	public static boolean blank(String line, String temporaryWhiteSpace) {
		if (temporaryWhiteSpace == null)
			return blank(line);
		if (line==null)
			return true;
		else if (line.length()==0)
			return true;
		else {
			for (int i=0; i<line.length(); i++) {
				if ((!whitespace(line.charAt(i), null)) && (!(temporaryWhiteSpace.indexOf(line.charAt(i))>=0))) {
					return false;
				}
			}
			return true;
		}
	}
	/*.................................................................................................................*/
	public static boolean blank(StringBuffer line) {
		if (line==null)
			return true;
		else if (line.length()==0)
			return true;
		else {
			for (int i=0; i<line.length(); i++) {
				char c = line.charAt(i);
				if (c >0 && !whitespace(c, null))
					return false;
			}
			return true;
		}
	}
	/*.................................................................................................................*/
	public static boolean blank(FileBlock line) {
		if (line==null)
			return true;
		else 
			return line.isEmpty();
	}

	/*.................................................................................................................*/
	public static void append(StringBuffer recipient, StringBuffer donor) {
		if (recipient==null || donor == null || donor.length() == 0)
			return;
		for (int i=0; i<donor.length(); i++)
			recipient.append(donor.charAt(i));
	}

}


