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

import mesquite.categ.lib.*;
import mesquite.molec.lib.*;

public class GenCodeEchino extends GeneticCode {

	public void setCode() {
		setStandardCode();
		setCode(A,A,A, ProteinData.ASN);
		setCode(A,G,A, ProteinData.SER);
		setCode(A,G,G, ProteinData.SER);
		setCode(U,G,A, ProteinData.TRP);
	}

	public String getName() {
		return "The Echinoderm and Flatworm Mitochondrial Code";
	}

	public static String getShortName (){
		return "Echinoderm Mitochondrial";
	}

	public String getNEXUSName(){
		return "mtdna.echino";
	}
	public int getNCBITranslationTableNumber(){
		return 9;
	}

}
