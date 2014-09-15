/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib;

import java.awt.*;
import java.math.*;


/* ��������������������������� taxon pair ������������������������������� */
/* ======================================================================== */
/** A pair of taxa.*/
public class TaxonPair implements Listable  {
	private Taxa taxa = null;
	private Taxon[] pair= new Taxon[2];
	
	public TaxonPair(Taxa taxa, int t1, int t2){
		this.taxa = taxa;
		if (t1>=0 && t1<taxa.getNumTaxa())
			pair[0] = taxa.getTaxon(t1);
		if (t2>=0 && t2<taxa.getNumTaxa())
			pair[1] = taxa.getTaxon(t2);
	}
	public Taxa getTaxa() {
		return taxa;
	}
	public void setTaxon(Taxon taxon1, Taxon taxon2) {
		pair[0] = taxon1;
		pair[1] = taxon2;
	}
	public Taxon getTaxon(int i) {
		if (i>=0 && i<=1)
			return pair[i];
		else
			return null;
	}
	public String getName() {
		if (pair[0]!=null && pair[1]!=null)
			return pair[0].getName() + "/" + pair[1].getName();
		else
			return "";
	}
}


