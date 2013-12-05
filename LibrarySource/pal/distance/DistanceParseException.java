// DistanceParseException.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.distance;


/**
 * exception thrown by ReadDistanceMatrix
 *
 * @author Korbinian Strimmer
 */
public class DistanceParseException extends Exception
{
	public DistanceParseException() {}

	public DistanceParseException(String msg)
	{
		super(msg);
	}
}

