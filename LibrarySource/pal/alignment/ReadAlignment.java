// ReadAlignment.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.alignment;

import pal.io.*;
import pal.misc.*;

import java.io.*;
import java.util.*;


/**
 * reads aligned sequence data from plain text files.<p>
 *
 * recognizes PHYLIP 3.4 INTERLEAVED,
 *              PHYLIP SEQUENTIAL,
 *              CLUSTAL and derived formats.<p>
 *
 * Other features:
 * - the dot as "copy character" is recognized, 
 * - all base characters are capitalized,
 * - automatic data type estimation
 * - determination of corresponding base frequencies.
 *
 * @version $Id: ReadAlignment.java,v 1.4 2001/07/13 14:39:12 korbinian Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class ReadAlignment extends AbstractAlignment
{
	//
	// Public stuff
	//

	/** read from stream */
	public ReadAlignment(PushbackReader input)
		throws AlignmentParseException, IOException
 	{
		readFile(input);	
	}
 
	/** read from file */
	public ReadAlignment(String file)
		throws AlignmentParseException, IOException
	{
		PushbackReader input = InputSource.openFile(file);
		readFile(input);
		input.close();		
	}


	// Implementation of abstract Alignment method

	/** sequence alignment at (sequence, site) */
	public char getData(int seq, int site)
	{
		return data[seq][site];
	}


	//
	// Private stuff
	//
   
	private int lineLength;
	private Vector names, seqs, sites;
	private FormattedInput fi = FormattedInput.getInstance();

	// Raw sequence alignment [sequence][site]
	private char[][] data = null;


	private void readFile(PushbackReader in)
		throws AlignmentParseException, IOException
	{
		// Check for CLUSTAL style alignment
	
		int c;
		String id = "CLUSTAL";
		boolean isClustal = true;
		
		for (int i = 0; i < id.length(); i++)
		{
			c = fi.readNextChar(in);
			if (c != id.charAt(i) && isClustal)
			{
				isClustal = false;
				in.unread(c);
				break;
			}
		}
		
		if (isClustal)
		{
			fi.nextLine(in);
			readCLUSTALW(in);
		}
		else
		{
			readPHYLIP(in);
		}
		
		// Capitalize
		for (int i = 0; i < numSeqs; i++)
		{
			for (int j = 0; j < numSites; j++)
			{
				data[i][j] = Character.toUpperCase(data[i][j]);
			}
		}
		
		// Estimate data type
		guessDataType();
		
		// Determine frequencies
		AlignmentUtils.estimateFrequencies(this);
	}

 	// Read alignment (in CLUSTAL W format)
	private void readCLUSTALW(PushbackReader in)
		throws AlignmentParseException
	{	
		int EOF = -1;
		int c, seq = 0, pos = 0;
		
		try
		{
			// Find start block
			c = fi.readNextChar(in);
			in.unread(c);
			
			names = new Vector();
			seqs = new Vector();
			
			// Reading first data block 
			c = in.read();
			while (c != ' ' && c != '\n' && c != '\r')
			{
				in.unread(c);
				names.addElement(fi.readLabel(in, 10));			
				readSeqLineC(in, seq, pos);
				seq++;
				c = in.read();
			}
			in.unread(c);
			
			
			// Skip CLUSTAL W status line
			fi.nextLine(in);
			
			pos += lineLength;
			numSeqs = seq;
				
			// Go to next block
			c = fi.readNextChar(in);
			in.unread(c);

			// Reading remaining blocks
			while (c != EOF)
			{
				for (seq = 0; seq < numSeqs; seq++)
				{
					// goto next blank
					do
					{
						c = in.read();
					}
					while (c != ' ');

					readSeqLineC(in, seq, pos);
				}
				
				// Skip CLUSTAL W status line
				fi.nextLine(in);
				
				pos += lineLength;

				// Go to next block
				c = fi.readNextChar(in);
				in.unread(c);
			}
			
			numSites = pos;
			
			// Copy to array
			idGroup = new SimpleIdGroup(numSeqs);
			data = new char[numSeqs][numSites];
			for (int i = 0; i < numSeqs; i++)
			{
				idGroup.setIdentifier(i,
						      new Identifier((String)names.elementAt(i)));
			}
			for (int i = 0; i < numSeqs; i++)
			{
				for (int j = 0; j < numSites; j++)
				{
					data[i][j] =
						((Character)
							((Vector) seqs.elementAt(i)
						).elementAt(j)).charValue();
				}
			}
			
			// Help garbage collector
			names = null;
			for (int i = 0; i < numSeqs; i++)
			{
				((Vector) seqs.elementAt(i)).removeAllElements();
			}
			seqs = null;
		}
			catch (IOException e)
			{
				throw new AlignmentParseException("IO error after pos. " + (pos + 1) + ", seq. " + (seq + 1));
  			}		
	}
	

	private void readSeqLineC(PushbackReader in, int s, int pos)
		throws IOException, AlignmentParseException
	{
		int c;

		if (pos == 0)
		{
			sites = new Vector();
			seqs.addElement(sites);
		}
		else
		{
			sites = (Vector) seqs.elementAt(s);
		}

		if (s == 0)
		{
			String thisLine = fi.readLine(in, false);
			lineLength = thisLine.length();
			
			for (int i = 0; i < lineLength; i++)
			{
				c = thisLine.charAt(i);
				if (c == '.')
				{
					throw new AlignmentParseException("Copy character (.) in first sequence not allowed (pos. "
					+ (i + pos + 1) + ")");
				}
				sites.addElement(new Character((char) c));
			}
		}
		else
		{		
			for (int i = 0; i < lineLength; i++)
			{
				c = fi.readNextChar(in);
				if (c == '.')
				{
					c = ((Character)
							((Vector) seqs.elementAt(0)
						).elementAt(pos + i)).charValue();
				}
				sites.addElement(new Character((char) c));
			}
			fi.nextLine(in);
		}	
	}
	
 	// Read alignment (in PHYLIP 3.4 INTERLEAVED or PHYLIP SEQUENTIAL format)
	private void readPHYLIP(PushbackReader in)
		throws AlignmentParseException
	{
		int c, pos = 0, seq = 0;
	
		try
		{
			// Parse PHYLIP header line
			numSeqs = fi.readInt(in);
			numSites = fi.readInt(in);
   
			// Reserve memory
			idGroup = new SimpleIdGroup(numSeqs);
			data = new char[numSeqs][numSites];


			// Determine whether sequences are in INTERLEAVED
			// or in sequential format
			String header = fi.readLine(in, false);
			
			boolean interleaved = true;

			if (header.length() > 0)
			{
				if (header.charAt(0) == 'S')
				{
					interleaved = false;
				}
			}
			
			if (interleaved) // PHYLIP INTERLEAVED
			{
				//System.out.println("PHYLIP INTERLEAVED");
			
			
				// Reading data
				while (pos < numSites)
				{			
					// Go to next block
					c = fi.readNextChar(in);
					in.unread(c);
				
					for (seq = 0; seq < numSeqs; seq++)
					{
						readSeqLineP(in, seq, pos, numSites);
					}
					pos += lineLength;
				}
			}
			else // PHYLIP SEQUENTIAL
			{
				//System.out.println("PHYLIP SEQUENTIAL");
			
				for (seq = 0; seq < numSeqs; seq++)
				{
					// Go to next block
					c = fi.readNextChar(in);
					in.unread(c);
				
					// Read label
					idGroup.setIdentifier(seq, new Identifier(fi.readLabel(in, 10)));
				
					// Read sequences
					for (pos = 0; pos < numSites; pos++)
					{
						data[seq][pos] = (char) fi.readNextChar(in);
					
						if (data[0][pos] == '.')
						{
							if (seq == 0)
							{
								throw new AlignmentParseException(
								"Copy character (.) in first sequence not allowed (pos. "
								+ (pos + 1) + ")");
							}
							else
							{
								data[seq][pos] = data[0][pos];
							}
						}
					}
				}				
			}
		}
			catch (IOException e)
			{
				throw new AlignmentParseException("IO error after pos. " + (pos + 1) + ", seq. " + (seq + 1));
 			}
 	}

	private void readSeqLineP(PushbackReader in, int s, int pos, int maxPos)
		throws IOException, AlignmentParseException
	{
		if (pos == 0)
		{
			idGroup.setIdentifier(s, new Identifier(fi.readLabel(in, 10)));			
		}
		
		if (s == 0)
		{
			String thisLine = fi.readLine(in, false);
			
			if (thisLine.length() > maxPos - pos)
			{
				lineLength = maxPos - pos;
			}
			else
			{
				lineLength = thisLine.length();
			}
			
			for (int i = 0; i < lineLength; i++)
			{
				data[0][pos + i] = thisLine.charAt(i);
				if (data[0][pos + i] == '.')
				{
					throw new AlignmentParseException("Copy character (.) in first sequence not allowed (pos. "
					+ (i + pos + 1) + ")");
				}
			}
		}
		else
		{
			for (int i = 0; i < lineLength; i++)
			{
				data[s][pos + i] = (char) fi.readNextChar(in);
				if (data[s][pos + i] == '.')
				{
					data[s][pos + i] = data[0][pos + i];
				}
			}
			fi.nextLine(in);
		}	
	}
}

