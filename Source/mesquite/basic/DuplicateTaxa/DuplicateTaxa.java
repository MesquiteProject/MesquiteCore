/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.basic.DuplicateTaxa;

import java.util.*;
import java.awt.*;

import mesquite.lib.characters.*;
import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;


/* ======================================================================== */
public class DuplicateTaxa extends TaxonUtility {
	
		/*.................................................................................................................*/
		public boolean startJob(String arguments, Object condition, boolean hiredByName){
			return true;
		}
		
		/*.................................................................................................................*/
		/** if returns true, then requests to remain on even after operateOnTaxa is called.  Default is false*/
		public boolean pleaseLeaveMeOn(){
			return false;
		}
		/*.................................................................................................................*/
		/** Called to operate on the taxa in the block.  Returns true if taxa altered*/
		public  boolean operateOnTaxa(Taxa taxa){
			int numSelected = taxa.numberSelected();
			if (numSelected<1){
				discreetAlert( "You need to select at least one taxon to be duplicated.");
				return false;
			}
			boolean[] selected = new boolean[taxa.getNumTaxa()];

			for (int it = 0; it<taxa.getNumTaxa(); it++) {
				selected[it] = taxa.getSelected(it);
			}
			
			
			for (int it = taxa.getNumTaxa()-1; it>=0; it--) {
				if (selected[it]) {
					String s = taxa.getTaxonName(it);
					boolean success = taxa.addTaxa(it, 1, true);
					String provisional = s + " (duplicate)";
					int num = 2;
					while (taxa.whichTaxonNumber(provisional)>=0){
						provisional = s + " (duplicate " + num + ")";
						num++;
					}
					int itDup = it+1;
					taxa.setTaxonName(itDup, provisional);
					
					int numMatrices = getProject().getNumberCharMatrices(taxa);
					for (int iM = 0; iM < numMatrices; iM++){
						CharacterData data = getProject().getCharacterMatrix(taxa, iM);
						CharacterState cs2 = null;
						data.incrementSuppressHistoryStamp();
						for (int ic = 0; ic<data.getNumChars(); ic++){
								cs2 = data.getCharacterState(cs2, ic, it);
								if (cs2 !=null) 
									data.setState(ic, itDup, cs2);
						}
						data.decrementSuppressHistoryStamp();
						data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
					}
				}
			}
			return true;
		}
		/*.................................................................................................................*/
		public String getName() {
			return "Duplicate Taxa";
		}
		/*.................................................................................................................*/
		public String getNameForMenuItem() {
			return "Duplicate Taxa";
		}

		/*.................................................................................................................*/
		public String getExplanation() {
			return "Duplicates selected taxa and their character states.";
		}
		/*.................................................................................................................*/
		/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
		 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
		 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
		public int getVersionOfFirstRelease(){
			return 300;  
		}
		/*.................................................................................................................*/
		public boolean isPrerelease(){
			return false;
		}

	}









