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

public class GenCodeVertMito extends GeneticCode {

	public void setCode() {
		setStandardCode();
		setCode(A,G,A, ProteinData.TER);
		setCode(A,G,G, ProteinData.TER);
		setCode(A,U,A, ProteinData.MET);
		setCode(U,G,A, ProteinData.TRP);
	}

	public String getName() {
		return "The Vertebrate Mitochondrial Genetic Code";
	}

	public static String getShortName (){
		return "Vertebrate Mitochondrial";
	}

	public String getNEXUSName(){
		return "mtdna.mam";
	}

	public int getNCBITranslationTableNumber(){
		return 2;
	}

}
