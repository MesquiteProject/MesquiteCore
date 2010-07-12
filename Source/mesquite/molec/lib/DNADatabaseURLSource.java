/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison. 
Version 2.72, December 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.lib;

import java.util.Hashtable;

import mesquite.lib.*;

public abstract class DNADatabaseURLSource extends DatabaseURLSource {
	public static final int PRIMER_NAME = 1;
	public static final int SAMPLE_CODE = 2;
	public static final int EXTRACTION_CODE = 2;
	public static final int PCR_REACTION_CODE = 3;
	public static final int AUTHORIZATION_KEY = 4;
	public static final int GENE = 5;
	public static final int TAXON = 6;
	public static final int NAME = 7;
	public static final int EXTRACTION = 8;
	public static final int FASTA=9;
	public static final int COUNT=10;
	
	

	
	public static final int PRIMER_SERVICE = 1;
	public static final int SEQUENCE_NAME_SERVICE = 2;
	public static final int CONTRIBUTOR_SERVICE = 3;
	public static final int CHROMATOGRAM_SEARCH_SERVICE = 4;
	public static final int SEQUENCE_UPLOAD_SERVICE = 5;
	public static final int FASTA_UPLOAD_SERVICE = 6;
	public static final int CHROMATOGRAM_BATCH_CREATION_SERVICE = 7;
	public static final int CHROMATOGRAM_DOWNLOAD_SERVICE = 8;

	public static final int SEQUENCE_ELEMENT=1;
	
	public boolean includeSampleCodePrefixInSampleCode() {
		return false;
	}
	
	public abstract String getChromatogramDownloadURL(Hashtable args);

}
