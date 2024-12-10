/* Mesquite source code.  Copyright 2024 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib;


/* Like Parser, but based on a file, for direct reading. Temporarily merely an intermediary to Parser */
public class MatrixFileParser {
	Parser parser;
	MesquiteStringBuffer tempBuffer;
	FileBlock block;
	public static final boolean READ_MATRIX_DIRECT_FROM_FILE = false;
	public static boolean verbose = false;

	public MatrixFileParser(FileBlock block){
		parser = new Parser();
		this.block = block;
		tempBuffer = new MesquiteStringBuffer();
		if (!READ_MATRIX_DIRECT_FROM_FILE)
			parser.setBuffer(block.toMesquiteStringBuffer());
		parser.setWhitespaceString(StringUtil.defaultWhitespace);   //fixing to default whitespace
	}
	/*  ............................................................................  */
	public long getFilePosition() {
		return block.getFilePosition();
	}
	/*  ............................................................................  */
	public boolean atEOF() {
		return block.atEOF();
	}
	/*  ............................................................................  */
	private String getNextLineFromFile() {
		block.getNextLine(tempBuffer);
		String s = tempBuffer.toString();
		s = StringUtil.stripLeadingWhitespace(s, parser.getWhitespaceString());
		s = StringUtil.stripTrailingWhitespace(s, parser.getWhitespaceString());
		return s;
	}

	/*  ............................................................................  */
	/* Called when tokens etc. are requested, to make sure the parser hasn't exhausted. If so, get another line from the file until one with darkspace is found*/
	private void checkAndRefreshParser() {
		if (!READ_MATRIX_DIRECT_FROM_FILE)
			return;
		boolean needToGetMore = parser.atEnd();
		while (needToGetMore && !block.atEOF()) { 
			String s = getNextLineFromFile();
			if (StringUtil.blank(s, parser.getWhitespaceString()))  //keep going until next dark space
				needToGetMore = true;
			else {
				parser.setString(s + "\n"); //the \n is to mimic the linefeed that would have been in the buffer if brought in from the file as a block
				parser.setPosition(0L);
				needToGetMore = false;
			}
		}
	}

	/*  -------------------------------------------------------------  */
	/* gets the local parser, including at least current in the file, at current position */
	public Parser getParserAtCurrentPosition() {
		if (!READ_MATRIX_DIRECT_FROM_FILE)
			return parser;
		checkAndRefreshParser();
		return parser;
	}
	/*  -------------------------------------------------------------  */
	/* Get the next (darkspace) token*/
	public String getNextToken() {
		return getNextToken(true);
	}
	/*  -------------------------------------------------------------  */
	/* Get the next (darkspace) token*/
	public String getNextToken(boolean forceStripWhite) {
		if (!READ_MATRIX_DIRECT_FROM_FILE) {
			String s = parser.getNextToken();
			if (verbose) Debugg.println("~~~gNT [" + s + "]");
			return s;
		}
		String s = "";
		boolean tokenDone = false;
		while (!tokenDone && !atEOF()) {
			checkAndRefreshParser();  //make sure the parser isn't at its end
			s = parser.getNextToken(); //this could fail if the parser hits its end, so we need to see if we actually got something
			if (forceStripWhite) {
				s = StringUtil.stripTrailingWhitespace(s, parser.getWhitespaceString());
				s = StringUtil.stripLeadingWhitespace(s, parser.getWhitespaceString());
			}
			if (StringUtil.blank(s, parser.getWhitespaceString())) {
				s = getNextLineFromFile(); //get more from the file in hopes it's what we need, and go back and try more
				parser.getBuffer().append(" " + s);
			}
			else
				tokenDone = true;
		}
		if (s != null && s.equals("") && atEOF())
			s = null;
		if (verbose) Debugg.println("~~~gNT [" + s + "] [" + parser.getBuffer() + "]");

		return s;
	}

	/*  -------------------------------------------------------------  */
	/* Get the next command up to and including :;"*/
	public String getNextCommand() {
		return getNextCommand(null);
	}

	public String getNextCommand(MesquiteLong posJustBeforeCommand) {
		if (posJustBeforeCommand != null)
			posJustBeforeCommand.setValue(parser.getPosition());
		if (!READ_MATRIX_DIRECT_FROM_FILE) {
			String s= parser.getNextCommand();
			if (verbose) Debugg.println("~~~gNC [" + s + "]");
			return s;
		}
		String c = "";
		boolean commandDone = false;
		while (!commandDone && !atEOF()) {
			checkAndRefreshParser();
			if (posJustBeforeCommand != null)
				posJustBeforeCommand.setValue(parser.getPosition());
			String s = parser.getNextCommand();  //Need to know if command is finished!!!!
			if (s!= null) {
				c += " " + s;
				c = StringUtil.stripTrailingWhitespace(c, parser.getWhitespaceString());
				if (c.endsWith(";"))
					commandDone = true;
			}
		}
		if (c != null && c.equals("") && atEOF())
			c = null;
		if (verbose) Debugg.println("~~~gNC [" + c + "]");
		return c;
	}

	/*  -------------------------------------------------------------  */
	public String getNextCommandNameWithoutConsuming() { 
		if (!READ_MATRIX_DIRECT_FROM_FILE) {
			String s = parser.getNextCommandNameWithoutConsuming();
			if (verbose) Debugg.println("~~~gNCNameWC [" + s + "]");
			return s;
		}
		checkAndRefreshParser();
		boolean commandNameDone = false;
		String s = "";
		while (!atEOF() && !commandNameDone) {
			s = parser.getNextCommandNameWithoutConsuming(); //this could fail if the parser hits its end, so we need to see if we actually got something
			if (verbose) Debugg.println("*****************[[[" + s + "]]]*********");
			s = StringUtil.stripTrailingWhitespace(s, parser.getWhitespaceString());  //not sure if needed, but let's be clean
			s = StringUtil.stripLeadingWhitespace(s, parser.getWhitespaceString());
			if (StringUtil.blank(s, parser.getWhitespaceString())) {
				String c = getNextLineFromFile();
				parser.getBuffer().append(" " + c);
			}
			else
				commandNameDone = true;
		}
		if (s != null && s.equals("") && atEOF())
			s = null;
		if (verbose) Debugg.println("~~~gNCNameWC [" + s + "]");
		return s;
	}

	/*  -------------------------------------------------------------  */
	public String getPieceOfLine(int len) {
		if (!READ_MATRIX_DIRECT_FROM_FILE) {
			String s= parser.getPieceOfLine(len);
			if (verbose) Debugg.println("~~~gPOL [" + s + "]");
			return s;
		}
		checkAndRefreshParser();
		String s = parser.getPieceOfLine(len);
		if (verbose) Debugg.println("~~~gPOL [" + s + "]");
		return s;
	}

	/*  -------------------------------------------------------------  */
	MesquiteLong posBefore = new MesquiteLong();
	public void consumeNextIfSemicolon() {
		long currentPos = parser.getPosition();
		if (verbose) Debugg.println("~~~CNISstart " + currentPos); // + " buffer [" + parser.getBuffer() + "]");
		String token = getNextCommand(posBefore);
		if (verbose) Debugg.println("~~~CNISmid " +  parser.getPosition()); // + " buffer [" + parser.getBuffer() + "]");
		token = StringUtil.stripTrailingWhitespace(token);  //not sure if needed, but let's be clean
		token = StringUtil.stripLeadingWhitespace(token);
		if (token == null || !token.equals(";"))
			parser.setPosition(posBefore.getValue());
		if (verbose) Debugg.println("~~~CNIS [" + token + "]");
	}

	/*  -------------------------------------------------------------  */
	public boolean blankByCurrentWhitespace(String s) {
		return parser.blankByCurrentWhitespace(s);
	}
	/*  -------------------------------------------------------------  */
	public void setLineEndingsDark(boolean b) {
		parser.setLineEndingsDark(b);
	}

}




