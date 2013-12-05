// FormattedInput.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.io;

import java.io.*;
import java.text.*;
import java.util.*;


/**
 * tools to simplify formatted input from an input stream
 *
 * @version $Id: FormattedInput.java,v 1.6 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 */
public class FormattedInput implements Serializable
{
	//
	// Public stuff
	//

	/**
	 * create new instance of this object
	 * (note that there is no public constructor
	 * because this class is a singleton!)
	 */
	public static FormattedInput getInstance()
	{
		if (singleton == null)
		{
			singleton = new FormattedInput();
		}
		
		return singleton;
	}
	
	/**
	 * go to the beginning of the next line.
         * Recognized line terminators:
	 * Unix: \n, DOS: \r\n, Macintosh: \r
	 *
	 * @param in input stream
	 */
	public void nextLine(PushbackReader in)
		throws IOException
	{
		readLine(in, false);
	}
	
	/**
	 * read a whole line
	 *
	 * @param in              input stream
	 * @param keepWhiteSpace  keep or drop white space
	 *
	 * @return string with content of line
	 */
	public String readLine(PushbackReader in, boolean keepWhiteSpace)
		throws IOException
	{
		StringBuffer buffer = new StringBuffer();

		int EOF = -1;
		int c;
		
		c = in.read();
		while (c != EOF && c != '\n' && c != '\r')
		{
			if (!isWhite(c) || keepWhiteSpace)
			{
				buffer.append((char) c);
			}
			c = in.read();
		}
		
		if (c == '\r')
		{
			c = in.read();
			if (c != '\n')
			{
				in.unread(c);
			}
		}
	
		return buffer.toString();
	}

	/**
	 * go to first non-whitespace character
	 *
	 * @param in input stream
	 *
	 * @return character or EOF
	 */
	public int skipWhiteSpace(PushbackReader in)
		throws IOException
	{
		int EOF = -1;
		int c;
		
		// search for first non-whitespace character
		do
		{
			c = in.read();
		}
		while (c != EOF && isWhite(c));
				
		return c;
	}

	/**
	 * read next character from stream
	 * (EOF does not count as character but will throw exception)
	 *
	 * @param input input stream
	 *
	 * @return character
	 */
	public int readNextChar(PushbackReader input)
		throws IOException
	{
		int EOF = -1;
		
		int c = skipWhiteSpace(input);
		
		if (c == EOF)
		{
			new IOException("End of file/stream");
		}
		
		return c;
	}

	/**
	 * read word from stream
	 *
	 * @param input stream
	 *
	 * @return word read from stream
	 */
	public String readWord(PushbackReader in)
		throws IOException
	{
		StringBuffer buffer = new StringBuffer();
		
		int EOF = -1;
		int c;
		
		c = skipWhiteSpace(in);
		
		// search for last non-whitespace character
		while (c != EOF && !isWhite(c))
		{
			buffer.append((char) c);
			c = in.read();
		}
		
		if (c != EOF)
		{
			in.unread(c);
		}
		
		return buffer.toString();
	}

	/**
	 * read sequence label from stream
	 *
	 * A sequence label is not allowed to contain
	 * whitespace and either of :,;()[]{}.  Note
	 * that newline/cr is NOT counted as white space!!
	 *
	 * @param in input stream
	 * @param maxLength maximum allowed length of label
	 *        (if negative any length is permitted)
	 *
	 * @return label
	 */
	public String readLabel(PushbackReader in, int maxLength)
		throws IOException
	{
		StringBuffer buffer = new StringBuffer();
		
		int EOF = -1;
		int c;
		int len = 0;
		
		
		c = skipWhiteSpace(in);
				
		// search for last label character
		while (c != EOF && buffer.length() != maxLength &&
			!(
			(isWhite(c) && c != '\n' && c != '\r') ||
			c == ':' || c == ',' || c == ';' ||
			c == '(' || c == ')' ||
			c == '[' || c == ']' ||
			c == '{' || c == '}'))
		{
			// read over newline/cr
			if (c != '\n' && c != '\r') buffer.append((char) c);
			c = in.read();
		}
		
		if (c != EOF)
		{
			in.unread(c);
		}
		
		return buffer.toString();
	}

	/*
	 * read next number from stream
	 *
	 * @param in input stream
	 * @param ignoreNewlineCR  ignore newline/cr as separator
	 *
	 * @return number (as string)
	 */
	public String readNumber(PushbackReader in, boolean ignoreNewlineCR)
		throws IOException
	{
		StringBuffer buffer = new StringBuffer();
		
		int EOF = -1;
		int c;
		
		// search for first number character
		do
		{
			c = in.read();
		}
		while (c != EOF &&
			!(c == '-' || c == '.' || Character.isDigit((char) c)));
		
		// search for last number character
		while (c != EOF &&
			(c == '-' || c == '.' || c == 'e'
			|| c == 'E' || Character.isDigit((char) c))
			|| (isNewlineCR(c) && ignoreNewlineCR) )
		{
			if (!(isNewlineCR(c) && ignoreNewlineCR))
				buffer.append((char) c);
			c = in.read();
		}
		
		if (c != EOF)
		{
			in.unread(c);
		}
				
		return buffer.toString();
	}

	/**
	 * read next number from stream and convert it to a double
	 * (newline/cr are treated as separators)
	 *
	 * @param in input stream
	 *
	 * @return double
	 */
	public double readDouble(PushbackReader in)
		throws IOException, NumberFormatException
	{
		return readDouble(in, false);
	}

	/**
	 * read next number from stream and convert it to a double
	 *
	 * @param in input stream
	 * @param ignoreNewlineCR  ignore newline/cr as separator
	 *
	 * @return double
	 */
	public double readDouble(PushbackReader in, boolean ignoreNewlineCR)
		throws IOException, NumberFormatException
	{
		String w = readNumber(in, ignoreNewlineCR);
		if (w.length() == 0)
		{
			throw new IOException("End of file/stream");
		}
		
		return Double.valueOf(w).doubleValue();
	}


	/**
	 * read next number from stream and convert it to a int
	 * (newline/cr are treated as separators)
	 *
	 * @param in input stream
	 *
	 * @return integer
	 */
	public int readInt(PushbackReader in)
		throws IOException, NumberFormatException
	{
		return readInt(in, false);
	}

	/**
	 * read next number from stream and convert it to a int
	 *
	 * @param in input stream
	 * @param ignoreNewlineCR  ignore newline/cr as separator
	 *
	 * @return integer
	 */
	public int readInt(PushbackReader in, boolean ignoreNewlineCR)
		throws IOException, NumberFormatException
	{
		String w = readNumber(in, ignoreNewlineCR);
		if (w.length() == 0)
		{
			throw new IOException("End of file/stream");
		}
		
		return Integer.valueOf(w).intValue();
	}


	//
	// Private stuff
	//
	
	// private constructor
	private FormattedInput()
	{
		// Just to prevent a public constructor
	}
	
	private static FormattedInput singleton;
	
	private static boolean isWhite(int c)
	{
		return Character.isWhitespace((char) c);
	}
	
	private static boolean isNewlineCR(int c)
	{
		if (c == '\n' || c == 'r')
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}

