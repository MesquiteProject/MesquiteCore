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
/** A class for parsing strings for NEXUS files and commands.*/
public class Parser extends StringUtil {
	StringBuffer buffer, buffer2;
	StringBuffer commandBuffer;
	String whitespaceString = null;
	String whitespaceStringSet = null;
	String punctuationString = null;
	String punctuationStringSet = null;
	String lineEndString = null;
	String lineEndStringSet = null;
	static char openCommentBracket = '[';
	static char closeCommentBracket = ']';
	public static boolean allowComments = true;
	boolean convertUnderscoresToBlanks = true;
	char quoteChar = defaultQuote;
	boolean lineEndingsDark = false;
	MesquiteInteger pos;
	boolean hyphensArePartOfNumbers = true;
	StringBuffer line = new StringBuffer(1000);
	char[][] charTranslationTable = new char[50][2];  //50 max
	
	public Parser(){
		pos = new MesquiteInteger(0);
		buffer = new StringBuffer(100); 
		buffer2 = new StringBuffer(100); 
		openCommentBracket = '[';
		closeCommentBracket = ']';
		allowComments = true;

	}
	public Parser(String line){
		this();
		setString(line);
	}
	/*-------------------*/
	//these methods allow the parser to be set to autotranslate particular characters on the fly
	private int whichTranslated(char c){
		for (int i=0; i<charTranslationTable.length; i++)
			if (c == charTranslationTable[i][0])
				return i;
		return -1;
	}
	private int firstEmptyTranslated(){
		for (int i=0; i<charTranslationTable.length; i++)
			if (0 == charTranslationTable[i][0])
				return i;
		return -1;
	}
	private boolean checkForTranslations(){
		for (int i=0; i<charTranslationTable.length; i++)
			if (0 != charTranslationTable[i][0])
				return true;
		return false;
	}
	boolean anyTranslations = false;
	public void setTranslatedCharacter(char fromChar, char toChar){
		int i = whichTranslated(fromChar);
		if (i>=0)
			charTranslationTable[i][1] = toChar;
		else {
			i = firstEmptyTranslated();
			charTranslationTable[i][0] = fromChar;
			charTranslationTable[i][1] = toChar;
		}
		anyTranslations = checkForTranslations();
	}
	public void clearTranslatedCharacter(char fromChar){
		int i = whichTranslated(fromChar);
		if (i>=0){
			charTranslationTable[i][0] = 0;
			charTranslationTable[i][1] = 0;
		}
		anyTranslations = checkForTranslations();
	}
	public char getTranslation(char fromChar){
		int i = whichTranslated(fromChar);
		if (i>=0){
			return charTranslationTable[i][1];
		}
		return fromChar;
	}
	public void setConvertUnderscoresToBlanks(boolean convertUnderscoresToBlanks) {
		this.convertUnderscoresToBlanks = convertUnderscoresToBlanks;
	}

	char lineCharAt(String line, int pos){
		if (line == null)
			return 0;
		if (pos>=line.length()) 
			return 0;
		char c = line.charAt(pos); 
		if (anyTranslations)
			return getTranslation(c);
		return c;
	}
	char lineCharAt(StringBuffer line, int pos){
		if (line == null)
			return 0;
		if (pos>=line.length()) 
			return 0;
		char c = line.charAt(pos); 
		if (anyTranslations)
			return getTranslation(c);
		return c;
	}
	/*-------------------*/
	public void setString(String s){
		if (s == null)
			s = "";
		line.setLength(0);
		line.append(s);
		pos.setValue(0);
	}
	/*this causes the passed buffer to be used.  Danger is that the Parser is later subject to this buffer's external chagnes.
	thus, call resetBufferToLocal afterward to make Parser behave normally */
	public void setBuffer(StringBuffer s){
		if (s == null)
			return;
		line = s;
		pos.setValue(0);
	}
	public void resetBufferToLocal(){
		line = new StringBuffer(1000);
	}
	public StringBuffer getBuffer(){
		return line;
	}
	public String getString(){
		if (line == null)
			return null;
		return line.toString();
	}
	public boolean blank(){
		if (line == null)
			return true;
		if (line.length() == 0)
			return true;
		for (int i=0; i< line.length(); i++)
			if (!whitespace(lineCharAt(line, i)))
				return false;
		return true;
	}
	public boolean atEnd(){
		return pos.getValue()>=line.length();
	}
	public int getPosition(){
		return pos.getValue();
	}
	public void setPosition(int p){
		pos.setValue(p);
	}
	public String getlineEndString(){
		return lineEndString;
	}
	public void setLineEndString(String s){
		if (s==null){
			lineEndString = defaultLineEnd;
			lineEndStringSet = null;
		}
		else {
			lineEndString = s;
			lineEndStringSet = s;
		}
	}
	/*.................................................................................................................*/
	public boolean lineEndCharacter(char c) {
		if (c<0)
			return true;
		if (c==0)
			return false;  
		if (lineEndString!=null)
			return lineEndString.indexOf(c)>=0;
			return defaultLineEnd.indexOf(c)>=0;
	}

	public String getWhitespaceString(){
		return whitespaceString;
	}
	public void setWhitespaceString(String s){
		if (s==null){
			whitespaceString = defaultWhitespace;
			whitespaceStringSet = null;
		}
		else {
			whitespaceString = s;
			whitespaceStringSet = s;
		}
		if (lineEndingsDark){
			if (whitespaceString ==null)
				whitespaceString = StringUtil.defaultWhitespace;
			whitespaceString = StringUtil.delete(whitespaceString, '\n');
			whitespaceString = StringUtil.delete(whitespaceString, '\r');
		}
	}
	public boolean getLineEndingsDark(){
		return lineEndingsDark;
	}
	public void setLineEndingsDark(boolean b){
		lineEndingsDark = b;
		if (lineEndingsDark){
			if (whitespaceString ==null)
				whitespaceString = StringUtil.defaultWhitespace;
			whitespaceString = StringUtil.delete(whitespaceString, '\n');
			whitespaceString = StringUtil.delete(whitespaceString, '\r');
			if (punctuationString == null)
				punctuationString = defaultPunctuationPlusLB;
			else {
				if (punctuationString.indexOf('\n')<0)
					punctuationString = punctuationString + '\n';
				if (punctuationString.indexOf('\r')<0)
					punctuationString = punctuationString + '\r';
			}
		}
		else {
			whitespaceString =  whitespaceStringSet;
			punctuationString = punctuationStringSet;
		}
	}
	public void addToDefaultPunctuationString(String s){
		if (s==null)
			return;
		punctuationString = defaultPunctuation+s;
		punctuationStringSet = defaultPunctuation+s;
		if (lineEndingsDark){
			if (punctuationString.indexOf('\n')<0)
				punctuationString = punctuationString + '\n';
			if (punctuationString.indexOf('\r')<0)
				punctuationString = punctuationString + '\r';
		}
	}

	public void setPunctuationString(String s){
		if (s==null){
			punctuationStringSet = null;
			if (lineEndingsDark)
				punctuationString = defaultPunctuationPlusLB;
			else
				punctuationString = defaultPunctuation;

		}
		else {
			punctuationString = s;
			punctuationStringSet = s;
			if (lineEndingsDark){
				if (punctuationString == null)
					punctuationString = defaultPunctuationPlusLB;
				else {
					if (punctuationString.indexOf('\n')<0)
						punctuationString = punctuationString + '\n';
					if (punctuationString.indexOf('\r')<0)
						punctuationString = punctuationString + '\r';
				}
			}
		}
	}
	public void setPunctuationStringRaw(String s){
		if (s==null){
			punctuationStringSet = null;
			punctuationString = null;
		}
		else {
			setPunctuationString(s);
		}
	}
	public String getPunctuationString (){
		return punctuationString;
	}
	private String getQuoted() {
		if (line==null)
			return null;
		buffer2.setLength(0);
		char c;
		boolean done = false;
		while (!done) {
			int where = pos.getValue();
			if (where<line.length()) {
				c= lineCharAt(line, where);
				if (c==quoteChar){
					int np = where + 1;
					if (np<line.length()) {
						char nxt= lineCharAt(line, np);
						if (nxt==quoteChar) {
							buffer2.append(quoteChar);
							pos.increment();  //skip to \'
							pos.increment(); //skip to next
						}
						else 
							done = true;
					}
					else done = true;
				}
				else if (c=='^'){
					int np = where + 1;
					if (np<line.length()) {
						char nxt= lineCharAt(line, np);
						if (nxt=='n') {
							buffer2.append('\n');
							pos.increment();  //skip to \n
							pos.increment(); //skip to next
						}
						else if (nxt=='r') { 
							buffer2.append('\n');  //translated for windows
							pos.increment();  //skip to \r
							pos.increment(); //skip to next
						}
						else {
							pos.increment(); //skip to next
						}
					}
					else done = true;
				}
				else {
					buffer2.append(c);
					pos.increment();
				}
			}
			else
				done = true;

		}
		pos.increment();
		return buffer2.toString();
	}
	/*.........................................................................................................*/
	/*  doesn't replace '' by ' */
	private StringBuffer getQuotedUnaltered(StringBuffer buf) {
		if (line==null)
			return null;
		buf.setLength(0);
		char c;
		boolean done = false;
		while (!done) {
			int where = pos.getValue();
			if (where<line.length()) {
				c= lineCharAt(line, where);
				if (c==quoteChar){
					int np = where + 1;
					if (np<line.length()) {
						char nxt= lineCharAt(line, np);
						if (nxt==quoteChar) {
							buf.append('\'');
							buf.append('\'');
							pos.increment();  //skip to \'
							pos.increment(); //skip to next
						}
						else 
							done = true;
					}
					else done = true;
				}
				else {
					buf.append(c);
					pos.increment();
				}
			}
			else
				done = true;

		}
		pos.increment();
		return buf;
	}
	/*.........................................................................................................*/
	/** returns remainder of line starting at pos; including square bracket comments*/
	public String getRemaining() {
		if (line == null)
			return null;
		else if (pos.getValue()<0 || line.length() == 0)
			return null;
		else
			return line.toString().substring(pos.getValue(), line.length());
	}

	public void setQuoteCharacter(char c){
		quoteChar = c;
	}
	public void setNoQuoteCharacter(){
		quoteChar = (char)0;
	}
	/*............................................  ....................................................*/
	/** if first character of string is a quote ('), takes to contents of the quote and returns them (calling getFirstToken).
	Otherwise returns the string*/
	public String getUnquotedToken() {
		if (line == null || line.length()==0 || lineCharAt(line, 0)!= quoteChar)
			return line.toString();
		else
			return getFirstToken(line.toString());
	}
	/*............................................  ....................................................*/
	/** returns first token from string; sets pos to end of token; excluding square bracket comments*/
	public String getFirstToken(StringBuffer s) {
		setBuffer(s);
		pos.setValue(0);
		return getNextToken();
	}
	/*............................................  ....................................................*/
	/** returns first token from string; sets pos to end of token; excluding square bracket comments*/
	public String getFirstToken(String line) {
		setString(line);
		pos.setValue(0);
		return getNextToken();
	}
	/*............................................  ....................................................*/
	/** returns first token from string; afterward sets pos to end of token; excluding square bracket comments*/
	public String getFirstToken() {
		pos.setValue(0);
		return getNextToken();
	}
	/*............................................  ....................................................*/
	/** returns first index of occurence of target token; useful to find if token is in string*/
	public int tokenIndexOfIgnoreCase(String line, String target) {
		if (target == null || line == null)
			return -1;
		setString(line);
		pos.setValue(0);
		String token = null;
		int count = 0;
		while ((token = getNextToken()) != null){
			if (target.equalsIgnoreCase(token))
				return count;
			count++;
		}
		return -1;
	}
	/*............................................  ....................................................*/
	/** returns first index of occurence of target token; useful to find if token is in string*/
	public int tokenIndex(String line, String target) {
		if (target == null || line == null)
			return -1;
		setString(line);
		pos.setValue(0);
		String token = null;
		int count = 0;
		while ((token = getNextToken()) != null){
			if (target.equals(token))
				return count;
			count++;
		}
		return -1;
	}
	/*............................................  ....................................................*/
	/** returns characters from pos to first instance of c*/
	public String getRemainingUntilChar(char stopChar) {
		return getRemainingUntilChar(stopChar,false);
	}
	/*............................................  ....................................................*/
	/** returns characters from pos to first instance of c*/
	public String getRemainingUntilChar(char stopChar, boolean skipPast) {
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		if (pos.getValue() >= line.length())
			return null;
		char c=getNextChar();
		buffer.setLength(0);
		while (c!=stopChar && pos.getValue()<line.length()) {  // or should this be <= line.length?
			buffer.append(c);
			c=getNextChar();
		}
		if (c!=stopChar)
			buffer.append(c);
		if (c==stopChar && !skipPast)
			setPosition(getPosition()-1);
		return buffer.toString();
	}
	/*............................................  ....................................................*/
	/** returns characters from pos to first instance of any character in stopChars */
	public String getRemainingUntilChars(String stopChars, boolean skipPast) {
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		if (pos.getValue() >= line.length())
			return null;
		char c=getNextChar();
		buffer.setLength(0);
		while (stopChars.indexOf(c)<0 && pos.getValue()<line.length()) {  // or should this be <= line.length?
			buffer.append(c);
			c=getNextChar();
		}
		if (stopChars.indexOf(c)<0)
			buffer.append(c);
		if (stopChars.indexOf(c)>=0 && !skipPast)
			setPosition(getPosition()-1);
		return buffer.toString();
	}
	/*............................................  ....................................................*/
	public boolean lineEndsAreDefaults() {
		return (lineEndString==null || defaultLineEnd.equals(lineEndString));
	}
	/*............................................  ....................................................*/
	/** returns characters from pos to first instance of \r or \n*/
	public String getRawNextLine() {
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		if (pos.getValue() >= line.length())
			return null;
		char c=getNextChar();
		buffer.setLength(0);

		while (!lineEndCharacter(c) && pos.getValue()<=line.length() && c!=(char)0) {  
			buffer.append(c);
			c=getNextChar();
		}
		if (c=='\r' && lineEndsAreDefaults()) {
			int pos = getPosition();
			c=getNextChar();
			if (c!='\n')   // if the next char is a newline, we want to go past it; otherwise, step back
				setPosition(pos);
		}
		return buffer.toString();
	}
	/*............................................  ....................................................*/
	/** returns n characters from pos */
	public String getPiece(int n) {
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		if (pos.getValue() >= line.length())
			return null;
		char c=getNextChar();
		buffer.setLength(0);
		
		int count = 0;
		while (count++< n && pos.getValue()<=line.length() && c!=(char)0) {  
			buffer.append(c);
			c=getNextChar();
		}
		return buffer.toString();
	}
	/*............................................  ....................................................*/
	/** returns n characters from pos */
	public String getPieceOfLine(int n) {
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		if (pos.getValue() >= line.length())
			return null;
		char c=getNextChar();
		buffer.setLength(0);
		
		int count = 0;
		while (count++< n && !lineEndCharacter(c) &&  pos.getValue()<=line.length() && c!=(char)0) {  
			buffer.append(c);
			c=getNextChar();
		}
		return buffer.toString();
	}
	/*............................................  ....................................................*/
	/** returns characters from pos to first instance of \r or \n*/
	public String getRawNextDarkLine() {
		String s = "";
		while (StringUtil.blank(s) && pos.getValue() <  line.length())
			s = getRawNextLine();
		return s;
	}
	/*............................................  ....................................................*/
	/** returns token from line starting at pos; excluding square bracket comments*/
	public String getLastToken() {
		String s = getFirstToken();
		String sLast = s;
		while (!StringUtil.blank(s)) {
			sLast = s;
			s = getNextToken();
		}
		return sLast;
	}
	/*............................................  ....................................................*/
	/** returns the position just before the last token */
	public int getPosBeforeLastToken() {
		String s = getFirstToken();
		int lastPos = 0;
		int prevPos=0;
		while (!StringUtil.blank(s)) {
			prevPos=lastPos;
			lastPos=pos.getValue();
			s = getNextToken();
		}
		return prevPos;
	}
	/*............................................  ....................................................*/
	/** returns token from line starting at pos; excluding square bracket comments*/
	public String getNextTrimmedToken() {
		String s = getNextToken();
		if (s!=null)
			return s.trim();
		return null;
	}
	/*............................................  ....................................................*/
	int startOfToken;
	public int getStartOfPreviousToken(){
		return startOfToken;
	}
	private void recordStartOfToken(){
		if (!MesquiteInteger.isCombinable(startOfToken))
			startOfToken = pos.getValue()-1;
	}
	/*............................................  ....................................................*/
	/** returns token from line starting at pos; excluding square bracket comments*/
	public String getNextToken() {
		startOfToken = MesquiteInteger.unassigned;
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		if (pos.getValue() >= line.length())
			return null;
		char c;
		buffer.setLength(0);
		startOfToken = MesquiteInteger.unassigned;
		try {
			boolean continu = true;
			while (whitespace(c=getNextChar(), whitespaceString))
				;
			if (punctuationOrQuote(c, punctuationString, quoteChar)) {
				continu = false;
				if (c == quoteChar) {
					buffer.setLength(0);
					recordStartOfToken();
					buffer.append(getQuoted());
				}
				else if (c == '-' && hyphensArePartOfNumbers) {
					char cod = charOnDeck(1);
					char cod2 = charOnDeck(2);
					if ((digits.indexOf(cod)>=0)||(cod=='.' && (digits.indexOf(cod2)>=0))) {
						recordStartOfToken();
						buffer.append(c);
						c=getNextChar();
						continu = true;
					}
					else {
						recordStartOfToken();
						buffer.append(c);
					}
				}
				else{
					recordStartOfToken();
					buffer.append(c);
				}
				if (lineEndCharacter(c)) {
					char r = charOnDeck(1);
					//if next character is also lineEndCharacter but of opposite sort (i.e. \n instead of \r) then eat it up
					if (r!=c && lineEndCharacter(r))
						pos.increment();
				}
			}
			if (continu) {
				while ((!whitespace(c, whitespaceString) && !punctuationOrQuote(c, punctuationString, quoteChar)) && c!=0) {
					if (c=='_' && convertUnderscoresToBlanks)
						c = ' ';
					else if (c=='^'){
						char nxt = getNextChar();
						if (nxt=='n') 
							c='\n';
						else if (nxt=='r') 
							c='\n'; //translated for windows
					}
					recordStartOfToken();
					buffer.append(c);
					c=getNextChar();
				}
				if (punctuationOrQuote(c, punctuationString, quoteChar)) {
					pos.decrement();
				}
			}
		}
		catch (StringIndexOutOfBoundsException e) {
			System.out.println("string bounds exceeded 0");
			startOfToken = MesquiteInteger.unassigned;
			pos.setValue(line.length());
		}
		return buffer.toString();
	}
	/*.........................................................................................................*/
	/** returns token from line starting at pos; excluding square bracket comments*/
	public String getUnalteredToken(boolean includeWhitespace) {
		startOfToken = MesquiteInteger.unassigned;
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		if (pos.getValue() >= line.length())
			return null;
		buffer.setLength(0);
		char c;
		try {
			boolean continu = true;
			while (whitespace(c=getNextChar(), whitespaceString))
				if (includeWhitespace){
					recordStartOfToken();
					buffer.append(c);
				}
			if (punctuationOrQuote(c, punctuationString, quoteChar)) {
				continu = false;
				if (c == quoteChar) {
					buffer.setLength(0);
					recordStartOfToken();
					buffer.append(quoteChar);
					buffer.append(getQuotedUnaltered(buffer2).toString());
					buffer.append(quoteChar);
				}
				else if (c == '-'  && hyphensArePartOfNumbers) {
					char cod = charOnDeck(1);
					char cod2 = charOnDeck(2);
					if ((digits.indexOf(cod)>=0)||(cod=='.' && (digits.indexOf(cod2)>=0))) {
						recordStartOfToken();
						buffer.append(c);
						c=getNextChar();
						continu = true;
					}
					else {
						buffer.setLength(0);
						recordStartOfToken();
						buffer.append(c);
					}
				}
				else {
					buffer.setLength(0);
					recordStartOfToken();
					buffer.append(c);
				}
				if (lineEndCharacter(c)) {
					char r = charOnDeck(1);
					//if next character is also lineEndCharacter but of opposite sort (i.e. \n instead of \r) then eat it up
					if (r!=c && lineEndCharacter(r))
						pos.increment();
				}
			}
			if (continu) {
				while ((!whitespace(c, whitespaceString) && !punctuationOrQuote(c, punctuationString, quoteChar)) && c!=0) {
					recordStartOfToken();
					buffer.append(c);
					c=getNextChar();
				}
				if (punctuationOrQuote(c, punctuationString, quoteChar)) {
					pos.decrement();
				}
			}
		}
		catch (StringIndexOutOfBoundsException e) {
			System.out.println("string bounds exceeded 0");
			pos.setValue(line.length());
			startOfToken = MesquiteInteger.unassigned;
		}
		if (blankByCurrentWhitespace(buffer)) 
			return null;
		return buffer.toString();
	}
	/*.........................................................................................................*/
	/** returns token from line starting at pos; keeps track of pending square bracket closure, etc.*/
	public String getUnalteredToken(boolean includeWhitespace, MesquiteInteger pendingBrackets, StringBuffer comment, MesquiteBoolean suppressComment) {
		startOfToken = MesquiteInteger.unassigned;
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		if (pos.getValue() >= line.length())
			return null;
		buffer.setLength(0);

		char c = 0;
		try {
			boolean continu = true;
			while (whitespace(c=getNextChar(pendingBrackets, comment, suppressComment), whitespaceString))  //skipping whitespace in front
				if (includeWhitespace){
					recordStartOfToken();
					buffer.append(c);
				}
			if (punctuationOrQuote(c, punctuationString, quoteChar)) {
				continu = false;
				if (c == quoteChar) {
					buffer.setLength(0);
					recordStartOfToken();
					buffer.append(quoteChar);
					buffer.append(getQuotedUnaltered(buffer2).toString());
					buffer.append(quoteChar);
				}
				else if (c == '-' && hyphensArePartOfNumbers) {
					char cod = charOnDeck(1);
					char cod2 = charOnDeck(2);
					if ((digits.indexOf(cod)>=0)||(cod=='.' && (digits.indexOf(cod2)>=0))) {
						recordStartOfToken();
						buffer.append(c);
						c=getNextChar(pendingBrackets, comment, suppressComment);
						continu = true;
					}
					else {
						buffer.setLength(0);
						recordStartOfToken();
						buffer.append(c);
					}
				}
				else {
					buffer.setLength(0);
					recordStartOfToken();
					buffer.append(c);
				}
				if (lineEndCharacter(c)) {
					char r = charOnDeck(1);
					//if next character is also lineEndCharacter but of opposite sort (i.e. \n instead of \r) then eat it up
					if (r!=c && lineEndCharacter(r)) {
						pos.increment();
					}
				}
			}
			if (continu) {
				while ((!whitespace(c, whitespaceString) && !punctuationOrQuote(c, punctuationString, quoteChar)) && c !=0) {
					recordStartOfToken();
					buffer.append(c);
					c=getNextChar(pendingBrackets, comment, suppressComment);
				}
				if (punctuationOrQuote(c, punctuationString, quoteChar)) {
					pos.decrement();
				}
			}
		}
		catch (StringIndexOutOfBoundsException e) {
			System.out.println("string bounds exceeded 1");
			startOfToken = MesquiteInteger.unassigned;
			pos.setValue(line.length());
		}
		if (blankByCurrentWhitespace(buffer)) 
			return null;
		return buffer.toString();
	}
	/*.........................................................................................................*/
	/** Fills StringBuffer with token from line starting at pos; keeps track of pending square bracket closure, etc.  Returns false if null string to be returned*/
	public boolean getUnalteredToken(StringBuffer token, boolean includeWhitespace, MesquiteInteger pendingBrackets, StringBuffer comment, MesquiteBoolean suppressComment) {
		startOfToken = MesquiteInteger.unassigned;
		if (token == null)
			return false;
		token.setLength(0);
		if (line==null)
			return false;
		if (pos.getValue() >= line.length())
			return false;
		if (line.equals(""))
			return true;

		char c = 0;
		try {
			boolean continu = true;
			while (whitespace(c=getNextChar(pendingBrackets, comment, suppressComment), whitespaceString))  //skipping whitespace in front
				if (includeWhitespace){
					recordStartOfToken();
					token.append(c);
				}
			if (punctuationOrQuote(c, punctuationString, quoteChar)) {
				continu = false;
				if (c == quoteChar) {
					token.setLength(0);
					recordStartOfToken();
					token.append(quoteChar);
					/*�*/StringUtil.append(token, getQuotedUnaltered(buffer2));
					token.append(quoteChar);
				}
				else if (c == '-' && hyphensArePartOfNumbers) {
					char cod = charOnDeck(1);
					char cod2 = charOnDeck(2);
					if ((digits.indexOf(cod)>=0)||(cod=='.' && (digits.indexOf(cod2)>=0))) {
						recordStartOfToken();
						token.append(c);
						c=getNextChar(pendingBrackets, comment, suppressComment);
						continu = true;
					}
					else {
						token.setLength(0);
						recordStartOfToken();
						token.append(c);
					}
				}
				else {
					token.setLength(0);
					recordStartOfToken();
					token.append(c);
				}
				if (lineEndCharacter(c)) {
					char r = charOnDeck(1);
					//if next character is also lineEndCharacter but of opposite sort (i.e. \n instead of \r) then eat it up
					if (r!=c && lineEndCharacter(r)) {
						pos.increment();
					}
				}
			}
			if (continu) {
				while ((!whitespace(c, whitespaceString) && !punctuationOrQuote(c, punctuationString, quoteChar)) && c !=0) {
					recordStartOfToken();
					token.append(c);
					c=getNextChar(pendingBrackets, comment, suppressComment);
				}
				if (punctuationOrQuote(c, punctuationString, quoteChar)) {
					pos.decrement();
				}
			}
		}
		catch (StringIndexOutOfBoundsException e) {
			System.out.println("string bounds exceeded 1");
			startOfToken = MesquiteInteger.unassigned;
			pos.setValue(line.length());
		}
		if (blankByCurrentWhitespace(token)) 
			return false;
		return true;
	}
	/*.................................................................................................................*/
	/** returns token from line starting at pos; keeps track of pending square bracket closure, etc.*/
	public String getToken(MesquiteInteger pendingBrackets, StringBuffer comment, MesquiteBoolean suppressComment) {
		startOfToken = MesquiteInteger.unassigned;
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		if (pos.getValue() >= line.length())
			return null;
		buffer.setLength(0);
		char c;
		try {
			boolean continu = true;
			while (whitespace(c=getNextChar(pendingBrackets, comment, suppressComment), whitespaceString))  //skipping whitespace in front
				;
			if (punctuationOrQuote(c, punctuationString, quoteChar)) {
				continu = false;
				if (c == quoteChar) {
					recordStartOfToken();
					buffer.append(getQuoted());
				}
				else if (c == '-' && hyphensArePartOfNumbers) {
					char cod = charOnDeck(1);
					char cod2 = charOnDeck(2);
					if ((digits.indexOf(cod)>=0)||(cod=='.' && (digits.indexOf(cod2)>=0))) {
						recordStartOfToken();
						buffer.append(c);
						c=getNextChar(pendingBrackets, comment, suppressComment);
						continu = true;
					}
					else {
						recordStartOfToken();
						buffer.append(c);
					}
				}
				else {
					recordStartOfToken();
					buffer.append(c);
				}
				if (lineEndCharacter(c)) {
					char r = charOnDeck(1);
					//if next character is also lineEndCharacter but of opposite sort (i.e. \n instead of \r) then eat it up
					if (r!=c && lineEndCharacter(r)) {
						pos.increment();
					}
				}
			}
			if (continu) {
				while ((!whitespace(c, whitespaceString) && !punctuationOrQuote(c, punctuationString, quoteChar)) && c !=0) {
					if (c=='_' && convertUnderscoresToBlanks)
						c = ' ';
					recordStartOfToken();
					buffer.append(c);
					c=getNextChar(pendingBrackets, comment, suppressComment);
				}
				if (punctuationOrQuote(c, punctuationString, quoteChar)) {
					pos.decrement();
				}
			}
		}
		catch (StringIndexOutOfBoundsException e) {
			System.out.println("string bounds exceeded 1");
			startOfToken = MesquiteInteger.unassigned;
			pos.setValue(line.length());
		}
		return buffer.toString();
	}
	/*............................................  ....................................................*/
	public boolean hasFileReadingArgument(String arguments, String target){
		String fRA = getFirstToken(arguments);
		while (!StringUtil.blank(fRA)) {
			if (fRA.equalsIgnoreCase(StringUtil.argumentMarker + target))
				return true;
			fRA = getNextToken();
		}
		return false;
	}
	/*............................................  ....................................................*/
	/** returns next XML starttag element name.  Returns attributes in MesquiteString attributes.  If it is an empty element (ending in /) or a processing instruction (bounded by ?) that too is returned in the arguments.*/
	//TODO: does this deal with escaped characters?
	public String getNextXMLTag(MesquiteInteger startPos, MesquiteString attributes, MesquiteBoolean isEmptyElement, MesquiteBoolean isProcessingInstruction, MesquiteBoolean isEndTag) {
		if (line==null)
			return null;
		else if (line.equals(""))
			return "";
		String s2 = line.toString();
		if (pos.getValue() >= line.length())
			return null;
		char c;
		boolean processedFirst = false;
		if (isEmptyElement!=null) 
			isEmptyElement.setValue(false);
		if (isProcessingInstruction!=null) 
			isProcessingInstruction.setValue(false);
		if (isEndTag!=null) 
			isEndTag.setValue(false);
		boolean isProcessing = false;
		boolean isEnd=false;
		buffer.setLength(0);
		StringBuffer attributesBuffer = new StringBuffer();
		boolean withinAttributes = false;
		try {
			while ((c=getNextChar())!='<')  //exit once we have found '<'
				if (pos.getValue()>=line.length()) {
					return "";
				}
			if (startPos!=null)
				startPos.setValue(pos.getValue()-1);
			while ((c=getNextChar())!='>' && pos.getValue()<line.length()){ //now we are in the tag
				if (!processedFirst){  //examine the first character in the tag
					processedFirst=true;
					if (isProcessingInstruction!=null) 
						isProcessingInstruction.setValue(c=='?');
					if (isEndTag!=null) 
						isEndTag.setValue(c=='/');
					isProcessing=c=='?';
					isEnd =c=='/';
					if (!isProcessing && !isEnd)
						buffer.append(c);
				}
				else if (c=='/' && isEmptyElement!=null) {
					isEmptyElement.setValue(true);
				}
				else if (!withinAttributes) {
					if (whitespace(c))
						withinAttributes = true;
					else 
						buffer.append(c);
				}
				attributesBuffer.append(c);
			}

		}
		catch (StringIndexOutOfBoundsException e) {
			System.out.println("string bounds exceeded 0");
			pos.setValue(line.length());
		}
		if (attributes!=null)
			attributes.setValue(attributesBuffer.toString());
		return buffer.toString();
	}
	/*............................................  ....................................................*/
	/** checks to see if the contained String from the attributes string of an XML tag has an attribute called attribute with a value called value */
	public static boolean attributesContains(String attributes, String attribute, String value) {
		Parser parser = new Parser(attributes);
		parser.setPunctuationString("=\"");
		String token = parser.getNextToken();
		while (!StringUtil.blank(token)) {
			if (attribute.equalsIgnoreCase(token)) {
				token = parser.getNextToken();  //=
				token = parser.getNextToken(); //"
				token = parser.getNextToken();
				if (value.equalsIgnoreCase(token))
					return true;
				token = parser.getNextToken(); //"
			}
			token = parser.getNextToken();
		} 
		return false;
	}
	/*............................................  ....................................................*/
	/** returns next tag in <>*/
	public String getNextXMLTag(MesquiteInteger startPos) {
		return getNextXMLTag(startPos,null,null,null,null);
	}
	/*............................................  ....................................................*/
	/** returns next tag in <>*/
	public String getNextXMLTag() {
		return getNextXMLTag(null,null,null,null,null);
	}
	/*............................................  ....................................................*/
	/** checks to see if the contained String is an XML document */
	public boolean isXMLDocument(boolean rememberPosition) {
		int oldPos = getPosition();
		setPosition(0);
		MesquiteBoolean isProcessingElement = new MesquiteBoolean(false);
		String tag = getNextXMLTag(null,null,null, isProcessingElement,null);   
		if (rememberPosition)
			setPosition(oldPos);
		return (isProcessingElement.getValue() && tag.startsWith("xml"));  
	}
	/*............................................  ....................................................*/
	/** returns token from line starting at pos; excluding square bracket comments*/
	public String getNextXMLTaggedContent(MesquiteString tag, MesquiteString attributes) {
		MesquiteBoolean isEmptyElement = new MesquiteBoolean(false);
		MesquiteBoolean isProcessingElement = new MesquiteBoolean(false);
		MesquiteBoolean isEndTag = new MesquiteBoolean(false);   //utilize this!!!
		String s = getNextXMLTag(null,attributes,isEmptyElement, isProcessingElement,isEndTag);  // get start tag
		tag.setValue(s);
		if (isEndTag.getValue() || isProcessingElement.getValue() || isEmptyElement.getValue())
			return null;
		String endTag = s;
		String nextTag;
		int startPos = pos.getValue();
		if (startPos>=line.length()) {
			return null;
		}

		MesquiteInteger endPos = new MesquiteInteger(startPos);
		try {
			while (!endTag.equalsIgnoreCase(nextTag=getNextXMLTag(endPos,null,null,null,isEndTag)) && !StringUtil.blank(nextTag)) {//get end tag
				if (!isEndTag.getValue())
					;  //complain
			}
		}
		catch (StringIndexOutOfBoundsException e) {
			System.out.println("string bounds exceeded 0");
			pos.setValue(line.length());
			endPos.setValue(line.length());
		}
		if (endPos.getValue()<=startPos)
			return null;
		else
			return line.substring(startPos,endPos.getValue());
	}
	/*............................................  ....................................................*/
	/** returns token from line starting at pos; excluding square bracket comments*/
	public String getNextXMLTaggedContent(MesquiteString tag) {
		return getNextXMLTaggedContent(tag,null);
	}
	/*............................................  ....................................................*/
	/** checks for tag, and resets parser to be for the contained text*/
	public boolean resetToXMLTagContents(String searchTag) {
		MesquiteString nextTag = new MesquiteString();
		String tagContent = getNextXMLTaggedContent(nextTag);
		while (!StringUtil.blank(nextTag.getValue())) {
			if (searchTag.equalsIgnoreCase(nextTag.getValue())) {
				setString(tagContent);  // reset this parser so that it is for the mesquite root tag contents
				return true;
			}
			tagContent = getNextXMLTaggedContent(nextTag);
		}
		return false;
	}
	/*............................................  ....................................................*/
	/** checks for root Mesquite tag, and resets parser to be for the contained text*/
	public boolean resetToMesquiteTagContents() {
		return resetToXMLTagContents("mesquite");
	}
	/*.................................................................................................................*/
	private boolean storeCharToComment(String c, int debt, StringBuffer comment, MesquiteBoolean suppressComment){
		if (debt >0 && comment!=null && !suppressComment.getValue()) {
			comment.append(c);
			return true;
		}
		else
			return false;
	}
	/*.................................................................................................................*/
	private boolean storeCharToComment(char c, int debt, StringBuffer comment, MesquiteBoolean suppressComment){
		if (debt >0 && comment!=null && !suppressComment.getValue()) {
			comment.append(c);
			return true;
		}
		else
			return false;
	} 
	/*.................................................................................................................*/
	public void setCharAt(char c, int position) {
		if (( position>0) && (position<line.length()))
			line.setCharAt(position-1, c); 
	}
	/*.................................................................................................................*/

	int pendingPComment = 0;
	/*.................................................................................................................*/
	char getNextChar(MesquiteInteger pendingBrackets, StringBuffer comment, MesquiteBoolean suppressComment) {
		int posTemp = pos.getValue();
		if (posTemp>=line.length()) {
			return 0;
		}
		char c = lineCharAt(line, posTemp);
		posTemp++;
		int debt = pendingBrackets.getValue();
		boolean hadBrack = false;
		boolean checkExclamation = false;
		if (openingCommentBracket(c)) {
			if (posTemp>= line.length()) //c was last character of line, therefore add space as workaround for line-ending "["
				line.append(' ');

			char next = 0;
			if (posTemp<line.length())
				next = lineCharAt(line, posTemp);
			if (debt==0 && (next == '%')) { 
				c = '<';
				posTemp++; //done to go past %
				line.setCharAt(posTemp-1, c); //done in case punctuation passed back will cause pos to decrement
				pendingPComment = 1;
			}
			else {
				if (!storeCharToComment(c, debt, comment, suppressComment))
					checkExclamation = true;
				if (comment !=null && comment.length()>0)
					comment.append(' ');
				debt++;
			}

		}
		else if (closingCommentBracket(c)) {
			if (comment!=null)
				comment.append("$1");
			if (debt==0 && pendingPComment >0) {
				c = '>';
				line.setCharAt(posTemp-1, c); //done in case punctuation passed back will cause pos to decrement
				pendingPComment = 0;
			}
			else {
				if (debt>0){
					debt--;
					storeCharToComment(c, debt, comment, suppressComment);
					if (debt ==0)
						suppressComment.setValue(false);
				}
				c=0;
			}
		}
		else 
			storeCharToComment(c, debt, comment, suppressComment);
		int count = 0;
		while  (debt>0 && posTemp< line.length()) {
			count++;
			c=lineCharAt(line, posTemp);
			if (!whitespace(c) && count == 1 && checkExclamation){
				if (checkExclamation && debt ==1)
					if (c != '!' && c!= '&')
						suppressComment.setValue(true);
				checkExclamation = false;
			}
			hadBrack=true;
			if (openingCommentBracket(c)) {
				if (posTemp+1== line.length()) //c was last character of line, therefore add space as workaround for line-ending "["
					line.append(' ');
				storeCharToComment(c, debt, comment, suppressComment);
				debt++;
			}
			else if (closingCommentBracket(c)) {
				if (comment!=null)
					comment.append("$2");
				if (debt>0){
					debt--;
					storeCharToComment(c, debt, comment, suppressComment);
					if (debt ==0)
						suppressComment.setValue(false);
				}
				c=0;
			}
			else storeCharToComment(c, debt, comment, suppressComment);
			posTemp++;
		}
		if (posTemp>=line.length()) {
			storeCharToComment(StringUtil.lineEnding(), debt, comment, suppressComment);
		}
		pendingBrackets.setValue(debt);
		pos.setValue(posTemp);
		if (posTemp>line.length())
			c=0;
		else if (hadBrack) {
			return getNextChar(pendingBrackets, comment, suppressComment);
		}
		return c;
	}

	/*.................................................................................................................*/
	public char charOnDeck(int ahead) {
		int posTemp = pos.getValue() +ahead;
		if (posTemp>=line.length()) {
			return 0;
		}
		return lineCharAt(line, posTemp);
	}
	/*.................................................................................................................*/
	public char getNextChar() {
		int posTemp = pos.getValue();
		if (posTemp>=line.length()) {
			return 0;
		}
		char c = lineCharAt(line, posTemp);
		posTemp++;
		int debt = 0;
		boolean hadBrack = false;
		if (openingCommentBracket(c)){
			if (posTemp>= line.length()) //%% c was last character of line, therefore add space as workaround for line-ending "["
				line.append(' ');
			debt++;
		}
		while  (debt>0 && posTemp< line.length()) {
			c=lineCharAt(line, posTemp);
			hadBrack = true;
			if (openingCommentBracket(c)) {
				if (posTemp+1>= line.length()) //%% c was last character of line, therefore add space as workaround for line-ending "["
					line.append(' ');
				debt++;
			}
			else if (closingCommentBracket(c)) {

				if (debt>0)
					debt--;
				c=0;
			}
			posTemp++;
		}
		pos.setValue(posTemp);
		if (posTemp>line.length())
			c=0;
		else if (hadBrack) {
			return getNextChar();
		}
		return c;
	}
	/*.................................................................................................................*/
	public String getTokenNumber(String s, int tokenNumber) {
		setString(s);
		return getTokenNumber(tokenNumber);
	}
	/*.................................................................................................................*/
	public String getTokenNumber(int tokenNumber) {
		pos.setValue(0);
		String token=null;
		for (int i=1; i<= tokenNumber; i++)
			token = getNextToken();
		return token;
	}
	/*.................................................................................................................*/
	public String getTokens(String s, int first, int last) {
		setString(s);
		return getTokens(first, last);
	}
	/*.................................................................................................................*/
	public String getTokens(int first, int last) {
		pos.setValue(0);
		String token=null;
		for (int i=1; i<= first; i++)
			token = getNextToken();
		String tokens = null;
		if (token !=null) {
			tokens = StringUtil.tokenize(token);
			for (int i=first+1; i<= last; i++)
				tokens += StringUtil.tokenize(getNextToken());
		}
		return tokens;
	}
	/*.................................................................................................................*/
	public int getNumberOfTokens(String s) {
		setString(s);
		return getNumberOfTokens();
	}
	/*.................................................................................................................*/
	public int getNumberOfTokens() {
		pos.setValue(0);
		return getNumberOfTokensRemaining();
	}
	/*.................................................................................................................*/
	public int getNumberOfTokensRemaining() {
		int oldPos = pos.getValue();
		String token=null;
		int count=0;
		do {
			token = getNextToken();
			if (token!=null)
				count++;
		} while (token!=null);
		pos.setValue(oldPos);
		return count;
	}
	/*.................................................................................................................*/
	/** returns in String the next "number" dark characters */
	public String getNextDarkChars(int number) {
		char c;
		StringBuffer b = new StringBuffer(number);
		for (int i = 1; i<=number && getPosition()<line.length(); i++) {
			c=nextDarkChar();
			b.append(c);
		}
		return b.toString();
	}
	/*.................................................................................................................*/
	public int getNumberOfSpecifiedCharRemaining(char theChar) {
		char c=' ';
		int count=0;
		do {
			c = nextDarkChar();
			if (theChar == c)
				count++;
		} while (getPosition()<line.length());
		return count;
	}
	/*.................................................................................................................*/
	public int getNumberOfSpecifiedChar(char theChar) {
		setPosition(0);
		return getNumberOfSpecifiedCharRemaining(theChar);
	}
	/*.................................................................................................................*/
	public int getNumberOfDarkChars() {
		char c=' ';
		int count=0;
		do {
			c = nextDarkChar();
			count++;
		} while (getPosition()<line.length());
		return count;
	}
	/*.................................................................................................................*/
	void skipComment() {
		try {
			int index = pos.getValue();
			char c;
			while ((c = lineCharAt(line, index)) != closeCommentBracket) {
				if (c==openCommentBracket && allowComments) {
					pos.setValue(index);
					skipComment();
					index=pos.getValue()-1;
				} 
				index++;
			}
			index++;
			pos.setValue(index);
		}
		catch (StringIndexOutOfBoundsException e) {}
	}
	/*.................................................................................................................*/
	void skipToDarkspace() {
		try {
			int index = pos.getValue();
			while (whitespace(lineCharAt(line, index), whitespaceString))
				index++;
			pos.setValue(index);
		}
		catch (StringIndexOutOfBoundsException e) {
			pos.setValue(line.length());
		}
	}
	/*.................................................................................................................*/
	public char nextDarkChar() {
		try {
			int index = pos.getValue();
			while (whitespace(lineCharAt(line, index), whitespaceString))
				index++;
			char dark =lineCharAt(line, index);
			pos.setValue(++index);
			if (whitespace(dark, whitespaceString))
				return '\0';
			else
				return dark;
		}
		catch (StringIndexOutOfBoundsException e) {
			pos.setValue(line.length());
		}
		return '\0';
	}
	/*.................................................................................................................*/
	public char firstDarkChar() {
		try {
			int index = 0;
			while (whitespace(lineCharAt(line, index), whitespaceString))
				index++;
			return lineCharAt(line, index);
		}
		catch (StringIndexOutOfBoundsException e) {}
		return '\0';
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public char getCharAt(int index) {
		if (line != null && index >=0 && index<line.length())
			return lineCharAt(line, index);
		return '\0';
	}
	/*.................................................................................................................*/
	public boolean whitespace(char c) {
		return whitespace(c, whitespaceString);
	}
	/*.................................................................................................................*/
	public boolean punctuation(char c) {
		return punctuation(c, punctuationString);
	}
	/*.................................................................................................................*/
	/** Returns the name of the next command in the string passed starting at the given character.  Resets pos afterward to start of command name. */
	public String getNextCommandName(MesquiteInteger p) {
		if (p!=null)
			pos.setValue(p.getValue());
		String token = getUnalteredToken(false);
		if (p!=null)
			pos.setValue(p.getValue());
		return token;
	}
	/*.................................................................................................................*/
	/** Returns the next command in the string passed starting at the given character */
	public String getNextCommand(MesquiteInteger p) {
		if (p!=null)
			pos.setValue(p.getValue());
		String token = getNextCommand();
		if (p!=null)
			p.setValue(pos.getValue());
		return token;
	}
	/*.................................................................................................................*/
	/** Returns the next command in the string passed starting at the given character */
	public String getNextCommand() {
		if (commandBuffer==null)
			commandBuffer = new StringBuffer(300);
		commandBuffer.setLength(0);
		if (line == null || pos.getValue()>=line.length())
			return null;
		String token = getUnalteredToken(false);
		if (!blankByCurrentWhitespace(token)) {
			commandBuffer.append(token);
			while (!blankByCurrentWhitespace(token) && !token.equals(";")) {
				token = getUnalteredToken( false);
				if (token != null && token.length() >0) {
					if (token.charAt(0)!=';') 
						commandBuffer.append(' ');
					commandBuffer.append(token);
				}
			}
		}
		return commandBuffer.toString();
	}
	/*.................................................................................................................*/
	public boolean blankByCurrentWhitespace(String line) {
		if (line==null)
			return true;
		else if (line.length()==0)
			return true;
		else {
			for (int i=0; i<line.length(); i++)
				if (!whitespace(lineCharAt(line, i)))
					return false;
			return true;
		}
	}
	/*.................................................................................................................*/
	public boolean blankByCurrentWhitespace(StringBuffer line) {
		if (line==null)
			return true;
		else if (line.length()==0)
			return true;
		else {
			try{
				for (int i=0; i<line.length(); i++)
					if (!whitespace(lineCharAt(line, i)))
						return false;
			}
			catch(StringIndexOutOfBoundsException e){ //here in case multithreading zaps line in progress
				return true;
			}return true;
		}
	}
	/*.................................................................................................................*/
	public  void setAllowComments(boolean b) {
		allowComments = b;
	}
	/*.................................................................................................................*/
	public static boolean allowComments() {
		return allowComments;
	}
	/*.................................................................................................................*/
	public  void setOpeningCommentBracket(char c) {
		openCommentBracket = c;
	}
	/*.................................................................................................................*/
	public  void setClosingCommentBracket(char c) {
		closeCommentBracket = c;
	}
	/*.................................................................................................................*/
	protected static boolean openingCommentBracket(char c) {
		return (c==openCommentBracket && allowComments);
	}
	/*.................................................................................................................*/
	protected static boolean closingCommentBracket(char c) {
		return (c==closeCommentBracket && allowComments);
	}
	public boolean getHyphensArePartOfNumbers() {
		return hyphensArePartOfNumbers;
	}
	public void setHyphensArePartOfNumbers(boolean hyphensArePartOfNumbers) {
		this.hyphensArePartOfNumbers = hyphensArePartOfNumbers;
	}
}

