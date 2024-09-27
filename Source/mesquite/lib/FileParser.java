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
public class FileParser {
	Parser parser;
	MesquiteStringBuffer tempBuffer;
	FileBlock block;
	public static final boolean READ_MATRIX_DIRECT_FROM_FILE = true;
	public static boolean verbose = false;
	public FileParser(){
		parser = new Parser();
		parser.setWhitespaceString(StringUtil.defaultWhitespace);   //fixing to default whitespace
	}
	public void setFileBlock(FileBlock block) {
		this.block = block;
		tempBuffer = new MesquiteStringBuffer();
		if (!READ_MATRIX_DIRECT_FROM_FILE)
			parser.setBuffer(block.toMesquiteStringBuffer());
	}

	private void checkAndRefreshParser() {
		if (!READ_MATRIX_DIRECT_FROM_FILE)
			return;
		long prevFilePos = block.getFilePosition();
		boolean needToGetMore = parser.atEnd();
		while (needToGetMore) { 
				block.getNextLine(parser.getBuffer());
				parser.setPosition(0L);
			if (verbose) Debugg.println("~.......[" + parser.getBuffer() + "] " + parser.atEnd() + " prev " + prevFilePos + " current " + block.getFilePosition());
			needToGetMore = (parser.blank() || parser.atEnd()) && !block.atEOF();
			prevFilePos = block.getFilePosition();
		}
	}
	private void appendNextLineToParser() {
				block.getNextLine(tempBuffer);
				String s = tempBuffer.toString();
				s = StringUtil.stripLeadingWhitespace(s, parser.getWhitespaceString());
				s = StringUtil.stripTrailingWhitespace(s, parser.getWhitespaceString());
				parser.getBuffer().append(" " + s);
	}
	public long getFilePosition() {
		return block.getFilePosition();
	}
	public boolean atEOF() {
		return block.atEOF();
	}
	public String getNextToken() {
		checkAndRefreshParser();
		String s = parser.getNextToken();
		if (verbose) Debugg.println("~~~gNT [" + s + "]");
		return s;
	}
	public String getNextCommand() {
		String c = "";
		boolean commandDone = false;
		while (!commandDone) {
			checkAndRefreshParser();
			String s = parser.getNextCommand();  //Need to know if command is finished!!!!
			if (s!= null) {
				c += " " + s;
				c = StringUtil.stripTrailingWhitespace(c, parser.getWhitespaceString());
				if (c.endsWith(";"))
					commandDone = true;
			}
			commandDone = commandDone || atEOF();
		}
		if (verbose) Debugg.println("~~~gNC [" + c + "]");
		return c;
	}
	public String getNextCommandNameWithoutConsuming() { 
		if (!READ_MATRIX_DIRECT_FROM_FILE)
			return parser.getNextCommandNameWithoutConsuming();
		checkAndRefreshParser();
		String c = "";
		while (!atEOF()) {
			String s = parser.getNextCommandNameWithoutConsuming();
			//Debugg.println("*****************[[[" + parser.getLineEndingsDark() + "]]]*********");
			s = StringUtil.stripTrailingWhitespace(s, parser.getWhitespaceString());
			s = StringUtil.stripLeadingWhitespace(s, parser.getWhitespaceString());
			if (StringUtil.blank(s, parser.getWhitespaceString()))
				appendNextLineToParser();
			else {
				if (verbose) Debugg.println("~~~gNCNameWC [" + c + "]");
				return s;
			}
		}
		return null;
	}
	public String getPieceOfLine(int len) {
		checkAndRefreshParser();
		String s = parser.getPieceOfLine(len);
		if (verbose) Debugg.println("~~~gPOL [" + s + "]");
		return s;
	}
	public boolean blankByCurrentWhitespace(String s) {
		return parser.blankByCurrentWhitespace(s);
	}

	/* gets the local parser, including at least current in the file, at current position */
	public Parser getParserAtCurrentPosition() {
		checkAndRefreshParser();
		return parser;
	}

	public void setLineEndingsDark(boolean b) {
		parser.setLineEndingsDark(b);
	}

	public void consumeNextIfSemicolon() {
		long currentPos = parser.getPosition();
		String token = getNextCommand();
		if (token == null || !token.equals(";"))
			parser.setPosition(currentPos);
		if (verbose) Debugg.println("~~~CNIS [" + token + "]");
	}
}




