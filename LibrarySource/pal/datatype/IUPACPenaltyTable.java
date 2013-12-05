// IUPACPenaltyTable.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.datatype;

/**
 * Implements a table of transition penalties for a DNA states 
 * and IUPAC ambiguous states. A mismatch is normalized to a
 * penalty of 1.0 <br>
 * Used for alignment scoring. 
 *
 * @version $Id: IUPACPenaltyTable.java,v 1.3 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Alexei Drummond
 */
 
public class IUPACPenaltyTable implements TransitionPenaltyTable {

	private static final double hlf = 0.5;
	private static final double tth = 2.0 / 3.0;
	private static final double fsx = 5.0 / 6.0;
	private static final double thq = 3.0 / 4.0;
	private static final double sni = 7.0 / 9.0;
 
	public double[][] IUPACPenalties = { 
	// A	C    G    T     K    M    R    S    W    Y      B    D    H    V     N   -	
	{  0,   1,   1,   1,    1, hlf, hlf,   1, hlf,   1,     1, tth, tth, tth,  thq,	1}, // A
	{  1,   0,   1,   1,    1, hlf,   1, hlf,   1, hlf,   tth,   1, tth, tth,  thq,	1}, // C
	{  1,   1,   0,   1,  hlf,   1, hlf, hlf,   1,   1,   tth, tth,   1, tth,  thq,	1}, // G
	{  1,   1,   1,   0,  hlf,   1,   1,   1, hlf, hlf,   tth, tth, tth,   1,  thq,	1}, // T
	
	{  1,   1, hlf, hlf,  hlf,   1, thq, thq, thq, thq,   tth, tth, fsx, fsx,  thq,  1}, // K
	{hlf, hlf,   1,   1,    1, hlf, thq, thq, thq, thq,   fsx, fsx, tth, tth,  thq,  1}, // M
	{hlf,   1, hlf,   1,  thq, thq, hlf, thq, thq,   1,   fsx, tth, fsx, tth,  thq,  1}, // R
	{  1, hlf, hlf,   1,  thq, thq, thq, hlf,   1, thq,   tth, fsx, fsx, tth,  thq,  1}, // S
	{hlf,   1,   1, hlf,  thq, thq, thq,   1, hlf, thq,   fsx, tth, tth, fsx,  thq,  1}, // W
	{  1, hlf,   1, hlf,  thq, thq,   1, thq, thq, hlf,   tth, fsx, tth, fsx,  thq,  1}, // Y
	
	{  1, tth, tth, tth,  tth, fsx, fsx, tth, fsx, tth,   tth, sni, sni, sni,  thq,  1}, // B
	{tth,   1, tth, tth,  tth, fsx, tth, fsx, tth, fsx,   sni, tth, sni, sni,  thq,  1}, // D
	{tth, tth,   1, tth,  fsx, tth, fsx, fsx, tth, tth,   sni, sni, tth, sni,  thq,  1}, // H
	{tth, tth, tth,   1,  fsx, tth, tth, tth, fsx, fsx,   sni, sni, sni, tth,  thq,  1}, // V
	
	{thq, thq, thq, thq,  thq, thq, thq, thq, thq, thq,   thq, thq, thq, thq,  thq,  1}, // N
	
	{  1,   1,   1,   1,    1,   1,   1,   1,   1,   1,     1,   1,   1,   1,    1,  0}  // -
	};
	
	public static final String[] all = {"A", "C", "G", "U", "K", "M", "R", "S", "W", "Y", "B", "D", "H", "V", "N", "-"};

	private DataType dataType = new IUPACNucleotides();

	public final double penalty(int a, int b) {
   
		return IUPACPenalties[a][b];
	}

	public final DataType getDataType() {
		return dataType;
	}
}

