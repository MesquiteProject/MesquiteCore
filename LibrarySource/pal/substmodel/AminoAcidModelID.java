// AminoAcidModelID.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.substmodel;

import java.io.Serializable;

/**
 * interface for IDs of amino acid models
 *
 * @version $Id: AminoAcidModelID.java,v 1.2 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 */
public interface AminoAcidModelID extends Serializable
{
	//
	// Public stuff
	//

	int DAYHOFF = 0;
	int JTT = 1;
	int MTREV24 = 2;
	int BLOSUM62 = 3;
	int VT = 4;
	int WAG = 5;
	int CPREV = 6;
	
	int MODELCOUNT = 7;

}

