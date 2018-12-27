/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.ColorTaxonByNumDataInMatrices;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class ColorTaxonByNumDataInMatrices extends TaxonNameStyler {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return 360;  
	}

	private int getNumMatricesWithDataForTaxon(Taxa taxa, int it){
		int totMatrices =  getProject().getNumberCharMatrices(taxa);
		int numMatrices = 0;
		if (totMatrices >0){
			for (int im = 0; im < totMatrices; im++){
				CharacterData data = getProject().getCharacterMatrix(taxa, im);
				if (data.hasDataForTaxon(it)){
					numMatrices++;
				}
			}
		}
		return numMatrices;
	}
	/*.................................................................................................................*/
	MesquiteColorTable colorTable = MesquiteColorTable.DEFAULTGRAYTABLE.cloneColorTable();

	public Color getTaxonNameColor(Taxa taxa, int it){
		int totMatrices =  getProject().getNumberCharMatrices(taxa);
		if (totMatrices >0){
			int numMatrices = getNumMatricesWithDataForTaxon(taxa, it);
			return colorTable.getColor(totMatrices+1, numMatrices);
		}
		return null;
	}
	public boolean getTaxonNameBoldness(Taxa taxa, int it){
		int totMatrices =  getProject().getNumberCharMatrices(taxa);
		if (totMatrices >0){
			int numMatrices = getNumMatricesWithDataForTaxon(taxa, it);
			return totMatrices ==numMatrices;
		}
		return false;
	}
	public String getObjectComment(Object obj){
		Taxon taxon = ((Taxon)obj);
		Taxa taxa = taxon.getTaxa();
		int it = taxon.getNumber();
		String s = "";
		int totMatrices =  getProject().getNumberCharMatrices(taxa);
		int matricesWithData = 0;
		if (totMatrices >0){
			for (int im = 0; im < totMatrices; im++){
				CharacterData data = getProject().getCharacterMatrix(taxa, im);
				if (data.hasDataForTaxon(it)){
					matricesWithData++;
					s = s + "  —  " + data.getName();
				}
			}
		}
		
		if (matricesWithData==1)
			return "Taxon has data in this matrix " + s;
		else if (matricesWithData >1) {
			if (s.length()<300)
				return "Taxon has data in these " + matricesWithData + " matrices: " + s;
			else
				return "Taxon has data in " + matricesWithData + " matrices";
		}
		else
			return "Taxon has data in no matrices";
			
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Emphasize Taxa With Data in More Matrices";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Supplies the colors and boldness proportional to the number of matrices for which the taxon has data." ;
	}
}
