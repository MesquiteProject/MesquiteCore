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
	public FileParser(){
		parser = new Parser();
	}
	public void setFileBlock(FileBlock block) {
		parser.setBuffer(block.toMesquiteStringBuffer());
	}

	public String getNextToken() {
		return parser.getNextToken();
	}
	public String getNextCommand() {
		return parser.getNextCommand();
	}
	public String getNextCommandNameWithoutConsuming() {
		return parser.getNextCommandNameWithoutConsuming();
	}
	public String getPieceOfLine(int len) {
		return parser.getPieceOfLine(len);
	}
	public boolean blankByCurrentWhitespace(String s) {
		return parser.blankByCurrentWhitespace(s);
	}

	/* gets the local parser and ensures that it has at least len characters or until the end of the command */
	public Parser getLocalParser(int len) {
		return parser;
	}

	public void setLineEndingsDark(boolean b) {
		parser.setLineEndingsDark(b);
	}

	public void consumeNextIfSemicolon() {
		long currentPos = parser.getPosition();
		String token = parser.getNextCommand();
		if (token == null || !token.equals(";"))
			parser.setPosition(currentPos);
	}
}


