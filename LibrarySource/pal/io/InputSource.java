// InputSource.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.io;

import java.io.*;


/**
 * convenience class to open input streams
 * linked to files, stdin, and strings
 *
 * @version $Id: InputSource.java,v 1.4 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 */
public class InputSource extends PushbackReader
{
	//
	// Public stuff
	//

	/**
	 * open file for reading
	 *
	 * @param name file name
	 *
	 * @return input stream
	 */
	public static InputSource openFile(String name)
		throws FileNotFoundException
	{
		return new InputSource(
			new BufferedReader(
			new FileReader(name)));
	}

	/**
	 * open standard input
	 *
	 * @return input stream
	 */			
	public static InputSource openStdIn()
	{
		return
			new InputSource(
			new BufferedReader(
			new InputStreamReader(System.in)));
	}

	/**
	 * "open" string for reading
	 *
	 * @param input string serving as source
	 *
	 * @return input stream
	 */
	public static InputSource openString(String input)
	{
		return new InputSource(new StringReader(input));
	}
	
	
	//
	// Private stuff
	//
	
	
	// Private constructor

	private InputSource(Reader in)
	{
		super(in);
	}
}

