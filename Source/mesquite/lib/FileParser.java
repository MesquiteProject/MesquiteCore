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
	long currentPosInBlock = 0;
	long currentPosLocal = 0;
	FileBlock block;
	public static final boolean READ_MATRIX_DIRECT_FROM_FILE = false;
	public static boolean verbose = false;
	public FileParser(){
		parser = new Parser();
	}
	public void setFileBlock(FileBlock block) {
		this.block = block;
		if (!READ_MATRIX_DIRECT_FROM_FILE)
			parser.setBuffer(block.toMesquiteStringBuffer());
	}

	private void checkAndRefreshParser() {
	/*	if (parser.atEnd()) {
			block.getNextLine(parser.getBuffer());
			Debugg.println("~.......[" + parser.getBuffer() + "] " + block.getFilePosition());
			parser.setPosition(0L);
		}*/
		long prevFilePos = block.getFilePosition();
		boolean needToGetMore = parser.atEnd();
		while (needToGetMore) {  //Debugg.println: have backup in case file ends!!!
			block.getNextLine(parser.getBuffer());
			parser.setPosition(0L);
			if (verbose) Debugg.println("~.......[" + parser.getBuffer() + "] " + parser.atEnd() + " prev " + prevFilePos + " current " + block.getFilePosition());
			needToGetMore = parser.atEnd() && !block.atEOF();
			prevFilePos = block.getFilePosition();
		}
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
				c = StringUtil.stripTrailingWhitespace(c);
				if (c.endsWith(";"))
					commandDone = true;
			}
			commandDone = commandDone || atEOF();
		}
		if (verbose) Debugg.println("~~~gNC [" + c + "]");
		return c;
	}
	public String getNextCommandNameWithoutConsuming() {
		checkAndRefreshParser();
		String s = parser.getNextCommandNameWithoutConsuming();
		if (verbose) Debugg.println("~~~gNCNWC [" + s + "]");
		return s;
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

/*
 * 

/* Like Parser, but based on a file, for direct reading. Temporarily merely an intermediary to Parser *
public class FileParser {
	//MesquiteStringBuffer buffer;
//	long currentPosInBuffer = 0;
	Parser localBufferParser;
	FileBlock block;
	public FileParser(){
		//buffer = new MesquiteStringBuffer();
		localBufferParser = new Parser();
	}
	public void setFileBlock(FileBlock block) {
		this.block = block;
	}

	public String getNextToken() {
		if (!localBufferParser.blank() && !localBufferParser.atEnd()) // if used up buffer then get next line
			return localBufferParser.getNextToken();
		//return parser.getNextToken();
		return block.getNextToken();
	}
	public String getNextCommand() {
		if (!localBufferParser.blank() && !localBufferParser.atEnd()) // if used up buffer then get next line
			return localBufferParser.getNextCommand();
		return block.getNextFileCommand(null);
	}
	public String getNextCommandNameWithoutConsuming() {
		String name = "";
		if (!localBufferParser.blank() && !localBufferParser.atEnd()) // if used up buffer then get next line
			name= localBufferParser.getNextCommandNameWithoutConsuming();
		Debugg.println("======GNCNWC-FP -[" + name + "]");
		if (StringUtil.blank(name))
			return block.getNextCommandNameWithoutConsuming();
		return name;
	}
	
	public boolean blankByCurrentWhitespace(String s) {
		return localBufferParser.blankByCurrentWhitespace(s);
	}

	// gets the local parser with  until the end of the line
	public Parser getParserAtCurrentPosition() {
		if (localBufferParser.blank() || localBufferParser.atEnd()) { // if used up buffer then get next line
			block.getNextLine(localBufferParser.getBuffer());
			localBufferParser.setPosition(0);
		}
		return localBufferParser;
	}

	public void setLineEndingsDark(boolean b) {
		localBufferParser.setLineEndingsDark(b);
	}

	public void consumeNextIfSemicolon() {
		if (!localBufferParser.blank() && !localBufferParser.atEnd()) { // if used up buffer then get next line
			block.getNextLine(localBufferParser.getBuffer());
			localBufferParser.setPosition(0);
		}
		long currentPos = localBufferParser.getPosition();
		String token = localBufferParser.getNextCommand();
		if (token == null || !token.equals(";"))
			localBufferParser.setPosition(currentPos);
	}
}

*/



