// OutputTarget.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.io;

import java.io.*;


/**
 * convenience class to create output streams linked to
 * files, stdout, and strings
 *
 * @version $Id: OutputTarget.java,v 1.3 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 */
public class OutputTarget extends PrintWriter
{
	//
	// Public stuff
	//

	/**
	 * open file for writing
	 *
	 * @param name file name
	 *
	 * @return output stream
	 */
	public static OutputTarget openFile(String name)
		throws IOException
	{
		return new OutputTarget(
			new PrintWriter(
			new BufferedWriter(
			new FileWriter(name))));
	}
	
	/**
	 * open standard out
	 *
	 * @return output stream
	 */		
	public static OutputTarget openStdOut()
	{
		return new OutputTarget(new PrintWriter(System.out));
	}

	/**
	 * "open" string to write into
	 *
	 * @return output stream
	 */
	public static OutputTarget openString()
	{
		StringWriter sw = new StringWriter();

		return new OutputTarget(new PrintWriter(sw), sw);
	}
	
	/**
	 * get string corresponding to current stream created by openString()
	 *
	 * @return string 
	 */
	public String getString()
	{
		if (stringWriter == null)
		{
			return "";
		}
		else
		{
			return stringWriter.toString();
		}
	}
	
	
	//
	// Private stuff
	//
	
	private StringWriter stringWriter;
	
	// Private constructor
	private OutputTarget(PrintWriter out)
	{
		super(out);
	}

	private OutputTarget(PrintWriter out, StringWriter sw)
	{
		super(out);
		stringWriter = sw;
	}
}

