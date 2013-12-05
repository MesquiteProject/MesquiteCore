// TreeParseException.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.tree;


/**
 * exception thrown by ReadTree
 *
 * @author Korbinian Strimmer
 */
public class TreeParseException extends Exception
{
	public TreeParseException() {}

	public TreeParseException(String msg)
	{
		super(msg);
	}
}

