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


/* ��������������������������� parse util ������������������������������� */
/* this probably should move to a NEXUS library
/* ======================================================================== */
/** A set of string parsing methods.  Should be renamed StringParser or NEXUSParser.*/
public class ParseUtil extends StringUtil {
//TODO: these should be protected against string overruns

	private static String getQuoted(String line, MesquiteInteger startChar) {
		if (line==null)
			return null;
		StringBuffer temp = new StringBuffer(line.length());
		char c;
		boolean done = false;
		while (!done) {
			int where = startChar.getValue();
			if (where<line.length()) {
				c= line.charAt(where);
				if (c==defaultQuote){
					int np = where + 1;
					if (np<line.length()) {
						char nxt= line.charAt(np);
						if (nxt==defaultQuote) {
							temp.append(defaultQuote);
							startChar.increment();  //skip to \'
							startChar.increment(); //skip to next
						}
						else 
							done = true;
					}
					else done = true;
				}
				else if (c=='^'){
					int np = where + 1;
					if (np<line.length()) {
						char nxt= line.charAt(np);
						if (nxt=='n') {
							temp.append('\n');
							startChar.increment();  //skip to \n
							startChar.increment(); //skip to next
						}
						else if (nxt=='r') {
							temp.append('\r');
							startChar.increment();  //skip to \n
							startChar.increment(); //skip to next
						}
						else
							startChar.increment();  //skip to next
					}
					else done = true;
				}
				else {
					temp.append(c);
					startChar.increment();
				}
			}
			else
				done = true;
			
		}
		startChar.increment();
		return temp.toString();
	}
	/*.........................................................................................................*/
	/*  doesn't replace '' by ' */
	static String getQuotedUnaltered(String line, MesquiteInteger startChar) {
		if (line==null)
			return null;
		StringBuffer temp = new StringBuffer(line.length());
		char c;
		boolean done = false;
		while (!done) {
			int where = startChar.getValue();
			if (where<line.length()) {
				c= line.charAt(where);
				if (c==defaultQuote){
					int np = where + 1;
					if (np<line.length()) {
						char nxt= line.charAt(np);
						if (nxt==defaultQuote) {
							temp.append('\'');
							temp.append('\'');
							startChar.increment();  //skip to \'
							startChar.increment(); //skip to next
						}
						else 
							done = true;
					}
					else done = true;
				}
				else {
					temp.append(c);
					startChar.increment();
				}
			}
			else
				done = true;
			
		}
		startChar.increment();
		return temp.toString();
	}
	/*.........................................................................................................*/
	/** returns remainder of line starting at startChar; including square bracket comments*/
	public static String getRemaining(String line, MesquiteInteger startChar) {
		if (line == null)
			return null;
		else
			return line.substring(startChar.getValue(), line.length());
	}

	/*............................................  ....................................................*/
	/** returns first token from string; sets pos to end of token; excluding square bracket comments*/
	public static String getFirstToken(String line, MesquiteInteger pos) {
		if (pos == null)
			pos = new MesquiteInteger(0);
		else
			pos.setValue(0);
		return getToken(line, pos);
	}
	/*............................................  ....................................................*/
	/** returns token from line starting at startChar; excluding square bracket comments*/
	public static String getToken(String line, MesquiteInteger startChar) {
		return getToken(line, startChar, (String)null, (String)null);
	}
	/*.................................................................................................................*/
	public static char charOnDeck(String line, MesquiteInteger pos, int ahead) {
		int posTemp = pos.getValue() +ahead;
		if (posTemp>=line.length()) {
			return 0;
		}
		return line.charAt(posTemp);
	}
	/*............................................  ....................................................*/
	/** returns token from line starting at startChar; excluding square bracket comments*/
	public static String getToken(String line, MesquiteInteger startChar, String whitespaceString, String punctuationString) {
		return getToken(line, startChar, whitespaceString, punctuationString, true);
	}
	/*............................................  ....................................................*/
	/** returns token from line starting at startChar; excluding square bracket comments*/
	public static String getToken(String line, MesquiteInteger startChar, String whitespaceString, String punctuationString, boolean replaceUnderscoresWithBlanks) {
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		 if (startChar.getValue() >= line.length())
			return null;
		StringBuffer temp = new StringBuffer(100); //better to pass from outside
		char c;
		try {
			boolean continu = true;
			while (whitespace(c=getNextChar(line, startChar), whitespaceString))
				;
			if (punctuation(c, punctuationString)) {
				continu = false;
				if (c == defaultQuote) {
					temp.setLength(0);
					temp.append(getQuoted(line, startChar));
				}
				else if (c == '-') {
					char cod = charOnDeck(line, startChar, 1);
					char cod2 = charOnDeck(line, startChar, 2);
					if (("0123456789".indexOf(cod)>=0)||(cod=='.' && ("0123456789".indexOf(cod2)>=0))) {
						temp.append(c);
						c=getNextChar(line, startChar);
						continu = true;
					}
					else {
						temp.append(c);
					}
				}
				else
					temp.append(c);
			}
			if (continu) {
				while ((!whitespace(c, whitespaceString) && !punctuation(c, punctuationString)) && c!=0) {
					if (c=='_' && replaceUnderscoresWithBlanks)
						c = ' ';
					temp.append(c);
					c=getNextChar(line, startChar);
				}
				if (punctuation(c, punctuationString)) {
					startChar.decrement();
				}
			}
		}
		catch (StringIndexOutOfBoundsException e) {
			System.out.println("string bounds exceeded 0");
			startChar.setValue(line.length());
			}
		return temp.toString();
	}
	/*.........................................................................................................*/
	/** returns token from line starting at startChar; excluding square bracket comments*
	public static String getUnalteredToken(String line, MesquiteInteger startChar, boolean includeWhitespace) {
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		 if (startChar.getValue() >= line.length())
			return null;
		StringBuffer temp = new StringBuffer(100); //better to pass from outside
		char c;
		try {
			boolean continu = true;
			while (whitespace(c=getNextChar(line, startChar), null))
				if (includeWhitespace)
					temp.append(c);
			if (punctuation(c, null)) {
				continu = false;
				if (c == defaultQuote) {
					temp.setLength(0);
					temp.append(defaultQuote);
					temp.append(getQuotedUnaltered(line, startChar));
					temp.append(defaultQuote);
				}
				else if (c == '-') {
					char cod = charOnDeck(line, startChar, 1);
					char cod2 = charOnDeck(line, startChar, 2);
					if (("0123456789".indexOf(cod)>=0)||(cod=='.' && ("0123456789".indexOf(cod2)>=0))) {
						temp.append(c);
						c=getNextChar(line, startChar);
						continu = true;
					}
					else {
						temp.setLength(0);
						temp.append(c);
					}
				}
				else {
					temp.setLength(0);
					temp.append(c);
				}
			}
			if (continu) {
				while ((!whitespace(c, null) && !punctuation(c, null)) && c!=0) {
					temp.append(c);
					c=getNextChar(line, startChar);
				}
				if (punctuation(c, null)) {
					startChar.decrement();
				}
			}
		}
		catch (StringIndexOutOfBoundsException e) {
			System.out.println("string bounds exceeded 0");
			startChar.setValue(line.length());
			}
		if (blank(temp)) 
			return null;
		return temp.toString();
	}
	/*.........................................................................................................*/
	/** returns token from line starting at startChar; keeps track of pending square bracket closure, etc.*
	public static String getUnalteredToken(String line, MesquiteInteger startChar, boolean includeWhitespace, MesquiteInteger pendingBrackets, StringBuffer comment) {
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		 if (startChar.getValue() >= line.length())
			return null;
		StringBuffer temp = new StringBuffer(100); //better to pass from outside
		char c;
		try {
			boolean continu = true;
			while (whitespace(c=getNextChar(line, startChar, pendingBrackets, comment), null))  //skipping whitespace in front
				if (includeWhitespace)
					temp.append(c);
			if (punctuation(c, null)) {
				continu = false;
				if (c == defaultQuote) {
					temp.setLength(0);
					temp.append(defaultQuote);
					temp.append(getQuotedUnaltered(line, startChar));
					temp.append(defaultQuote);
				}
				else if (c == '-') {
					char cod = charOnDeck(line, startChar, 1);
					char cod2 = charOnDeck(line, startChar, 2);
					if (("0123456789".indexOf(cod)>=0)||(cod=='.' && ("0123456789".indexOf(cod2)>=0))) {
						temp.append(c);
						c=getNextChar(line, startChar, pendingBrackets, comment);
						continu = true;
					}
					else {
						temp.setLength(0);
						temp.append(c);
					}
				}
				else {
					temp.setLength(0);
					temp.append(c);
				}
			}
			if (continu) {
				while ((!whitespace(c, null) && !punctuation(c, null)) && c !=0) {
					temp.append(c);
					c=getNextChar(line, startChar, pendingBrackets, comment);
				}
				if (punctuation(c, null)) {
					startChar.decrement();
				}
			}
		}
		catch (StringIndexOutOfBoundsException e) {
			System.out.println("string bounds exceeded 1");
			startChar.setValue(line.length());
			}
		if (blank(temp)) 
			return null;
		return temp.toString();
	}
	/*.................................................................................................................*/
	/** returns token from line starting at startChar; keeps track of pending square bracket closure, etc.*
	public static String getToken(String line, MesquiteInteger startChar, MesquiteInteger pendingBrackets, StringBuffer comment) {
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		 if (startChar.getValue() >= line.length())
			return null;
		StringBuffer temp = new StringBuffer(100); //better to pass from outside
		char c;
		try {
			boolean continu = true;
			while (whitespace(c=getNextChar(line, startChar, pendingBrackets, comment), null))  //skipping whitespace in front
				;
			if (punctuation(c, null)) {
				continu = false;
				if (c == defaultQuote) {
					temp.setLength(0);
					temp.append(getQuoted(line, startChar));
				}
				else if (c == '-') {
					char cod = charOnDeck(line, startChar, 1);
					char cod2 = charOnDeck(line, startChar, 2);
					if (("0123456789".indexOf(cod)>=0)||(cod=='.' && ("0123456789".indexOf(cod2)>=0))) {
						temp.append(c);
						c=getNextChar(line, startChar, pendingBrackets, comment);
						continu = true;
					}
					else {
						temp.append(c);
					}
				}
				else
					temp.append(c);
			}
			if (continu) {
				while ((!whitespace(c, null) && !punctuation(c, null)) && c !=0) {
					if (c=='_')
						c = ' ';
					temp.append(c);
					c=getNextChar(line, startChar, pendingBrackets, comment);
				}
				if (punctuation(c, null)) {
					startChar.decrement();
				}
			}
		}
		catch (StringIndexOutOfBoundsException e) {
			System.out.println("string bounds exceeded 1");
			startChar.setValue(line.length());
			}
		return temp.toString();
	}
	/*.................................................................................................................*/
	public static boolean darkBeginsWithIgnoreCase(String s, String find){
		//cycle through s until darkspace hit; 
		//then go in parallel through s and find to see if the characters are equivalent 
		
		if (s==null || find == null)
			return false;
		int sIndex = 0;
		while (sIndex < s.length() && whitespace(s.charAt(sIndex), null))
			sIndex++;
		if (sIndex == s.length())
			return false;
		int fIndex = 0;
		while (sIndex < s.length() && fIndex<find.length()) {
			if (Character.toUpperCase(s.charAt(sIndex))!=Character.toUpperCase(find.charAt(fIndex)))
				return false;
			sIndex++;
			fIndex++;
		}
		if (fIndex== find.length()) //got to end with not problem
			return true;
		return false;
		
	}
	/*.................................................................................................................*/
	public static boolean darkBeginsWithIgnoreCase(StringBuffer s, String find){
		//cycle through s until darkspace hit; 
		//then go in parallel through s and find to see if the characters are equivalent 
		
		if (s==null || find == null)
			return false;
		int sIndex = 0;
		while (sIndex < s.length() && whitespace(s.charAt(sIndex), null))
			sIndex++;
		if (sIndex == s.length())
			return false;
		int fIndex = 0;
		while (sIndex < s.length() && fIndex<find.length()) {
			if (Character.toUpperCase(s.charAt(sIndex))!=Character.toUpperCase(find.charAt(fIndex)))
				return false;
			sIndex++;
			fIndex++;
		}
		if (fIndex== find.length()) //got to end with not problem
			return true;
		return false;
		
	}
	/*.................................................................................................................*
	static char getNextChar(String s, MesquiteInteger current, MesquiteInteger pendingBrackets, StringBuffer comment) {
		int pos = current.getValue();
		if (pos>=s.length()) {
			return 0;
		}
		char c = s.charAt(pos);
		pos++;
		int debt = pendingBrackets.getValue();
		
		boolean hadBrack = false;
		if (openingSquareBracket(c)) {
			if (debt>0)
				if (comment!=null) comment.append(c);
			debt++;
		}
		else if (closingSquareBracket(c)) {
			if (debt>0){
				debt--;
				if (debt>0)
					if (comment!=null) comment.append(c);
			}
			c=0;
		}
		else if (debt>0)
			if (comment!=null) comment.append(c);
		while  (debt>0 && pos< s.length()) {
			c=s.charAt(pos);
			hadBrack=true;
			if (openingSquareBracket(c)) {
				if (debt>0)
					if (comment!=null) comment.append(c);
				debt++;
			}
			else if (closingSquareBracket(c)) {
				if (debt>0){
					debt--;
					if (debt>0)
						if (comment!=null) comment.append(c);
				}
				c=0;
			}
			else if (debt>0)
				if (comment!=null) comment.append(c);
			pos++;
		}
		pendingBrackets.setValue(debt);
		current.setValue(pos);
		if (pos>s.length())
			c=0;
		else if (hadBrack) {
			return getNextChar(s, current,pendingBrackets, comment);
		}
		return c;
	}
	
	/*.................................................................................................................*/
	static char getNextChar(String s, MesquiteInteger current) {
		int pos = current.getValue();
		if (pos>=s.length()) {
			return 0;
		}
		char c = s.charAt(pos);
		pos++;
		int debt = 0;
		boolean hadBrack = false;
		if (openingSquareBracket(c))
			debt++;
		while  (debt>0 && pos< s.length()) {
			c=s.charAt(pos);
			hadBrack = true;
			if (openingSquareBracket(c)) {
				debt++;
			}
			else if (closingSquareBracket(c)) {
				if (debt>0)
					debt--;
				c=0;
			}
			pos++;
		}
		current.setValue(pos);
		if (pos>s.length())
			c=0;
		else if (hadBrack) {
			return getNextChar(s, current);
		}
		return c;
	}
	/*.................................................................................................................*/
	public static String getTokenNumber(String s, MesquiteInteger startChar, int tokenNumber) {
			if (startChar==null)
				startChar= new MesquiteInteger(0);
			else
				startChar.setValue(0);
			String token=null;
			for (int i=1; i<= tokenNumber; i++)
				token = getToken(s, startChar);
			return token;
	}
	/*.................................................................................................................*/
	public static String getTokens(String s, MesquiteInteger startChar, int first, int last) {
			if (startChar==null)
				startChar= new MesquiteInteger(0);
			else
				startChar.setValue(0);
			String token=null;
			for (int i=1; i<= first; i++)
				token = getToken(s, startChar);
			String tokens = null;
			if (token !=null) {
				tokens = tokenize(token);
				for (int i=first+1; i<= last; i++)
					tokens += tokenize(getToken(s, startChar));
			}
			return tokens;
	}
	/*.................................................................................................................*/
	public static String[][] getSubcommands(String s, MesquiteInteger startChar) {
			if (startChar==null)
				startChar= new MesquiteInteger(0);
			int startValue = startChar.getValue();
			String token= getToken(s, startChar); //subcommandName
			int count =0;
			boolean scan = false;
			while (token !=null && !token.equals(";")) {
				token = getToken(s, startChar); //=
					
				if (token !=null && !token.equals(";")) {
					token = getToken(s, startChar); //subcommandParameters
					if (token != null && token.equals("-")){
						scan = true;
						int current = startChar.getValue();
						String nextToken = getToken(s, startChar); //rest of negative number?
						try {
							double d = Double.valueOf(nextToken).doubleValue();
							token = token +  nextToken; //rest of negative number
						}
						catch (NumberFormatException e) {
							startChar.setValue(current);  // was just lone -, so back up for next token
						}
					}
					count++;
				}
				
				token = getToken(s, startChar); //subcommandName
			}
			if (count==0)
				return null;
			String[][] strings = new String[2][count];
			startChar.setValue(startValue);
			for (int i=0; i< count; i++) {
				strings[0][i] = getToken(s, startChar); //subcommandName
				token = getToken(s, startChar); //=
				token = getToken(s, startChar); //subcommandParameters
				if (token != null && token.equals("-")){
					int current = startChar.getValue();
					String nextToken = getToken(s, startChar); //rest of negative number?
					try {
						double d = Double.valueOf(nextToken).doubleValue();
						token = token +  nextToken; //rest of negative number
					}
					catch (NumberFormatException e) {
						startChar.setValue(current);  // was just lone -, so back up for next token
					}
					//token = token + getToken(s, startChar); //rest of negative number
				}
				strings[1][i] = token; 
			}
			
			return strings;
	}
	/*.................................................................................................................*/
	public static void skipComment(String line, MesquiteInteger startChar) {
		try {
			int index = startChar.getValue();
			char c;
			while ((c = line.charAt(index)) != ']') {
				if (c=='[') {
					startChar.setValue(index);
					skipComment(line, startChar);
					index=startChar.getValue()-1;
				} 
				index++;
			}
			index++;
			startChar.setValue(index);
		}
		catch (StringIndexOutOfBoundsException e) {}
	}
	/*.................................................................................................................*/
	public static void skipToDarkspace(String line, MesquiteInteger startChar) {
		try {
			int index = startChar.getValue();
			while (whitespace(line.charAt(index), null))
				index++;
			startChar.setValue(index);
		}
		catch (StringIndexOutOfBoundsException e) {
			startChar.setValue(line.length());
		}
	}
	/*.................................................................................................................*/
	public static char nextDarkChar(String line, MesquiteInteger startChar) {
		try {
			int index = startChar.getValue();
			while (whitespace(line.charAt(index), null))
				index++;
			char dark =line.charAt(index);
			startChar.setValue(++index);
			if (whitespace(dark, null))
				return '\0';
			else
				return dark;
		}
		catch (StringIndexOutOfBoundsException e) {
			startChar.setValue(line.length());
		}
		return '\0';
	}
	/*.................................................................................................................*/
	public static char firstDarkChar(String line) {
		try {
			int index = 0;
			while (whitespace(line.charAt(index), null))
				index++;
			return line.charAt(index);
		}
		catch (StringIndexOutOfBoundsException e) {}
		return '\0';
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
}


