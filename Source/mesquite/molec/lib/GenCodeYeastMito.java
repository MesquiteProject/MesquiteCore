/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


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

public class GenCodeYeastMito extends GeneticCode {

	public void setCode() {
		setStandardCode();
		setCode(A,U,A, ProteinData.MET);
		setCode(C,U,A, ProteinData.THR);
		setCode(C,U,C, ProteinData.THR);
		setCode(C,U,G, ProteinData.THR);
		setCode(C,U,U, ProteinData.THR);
		setCode(U,G,A, ProteinData.TRP);

		setCode(C,G,A, ProteinData.ABSENT);
		setCode(C,G,C, ProteinData.ABSENT);
	}

	public String getName() {
		return "The Yeast Mitochondrial Genetic Code";
	}

	public static String getShortName (){
		return "Yeast Mitochondrial";
	}

	public String getNEXUSName(){
		return "mtdna.yeast";
	}

	public int getNCBITranslationTableNumber(){
		return 3;
	}

}
