/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.FlagByCompareOther;
/*~~  */




import mesquite.categ.lib.CategoricalData;
import mesquite.lib.Bits;
import mesquite.lib.CommandChecker;
import mesquite.lib.Listable;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteString;
import mesquite.lib.Notification;
import mesquite.lib.Snapshot;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterState;
import mesquite.lib.characters.MatrixFlags;
import mesquite.lib.duties.MatrixFlagger;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.ListDialog;

/* ======================================================================== */
public class FlagByCompareOther extends MatrixFlagger {

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem("Choose Comparison Matrix...", new MesquiteCommand("setOther", this));
		return true;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		if (oData!=null)
			temp.addLine("setOther " + getProject().getCharMatrixReferenceExternal(oData));
		return temp;
	}

	public void endJob(){
		if (oData != null)
			oData.removeListener(this);
		super.endJob();
	}

	CharacterData currentData = null;
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the comparison matrix", "[name of block]", commandName, "setOther")) {
			String dataReference =parser.getFirstToken(arguments);
			CharacterData d = getProject().getCharacterMatrixByReference(checker.getFile(), null, null, dataReference, true);
			if (d == null)
				d = getProject().getCharacterMatrixByReference(checker.getFile(), null, null, dataReference);
			if (d==null)
				d = chooseOther(currentData);
			if (d!= null && oData != null && oData != d){
				oData.removeListener(this);
				oData = d;
				oData.addListener(this);
				parametersChanged();
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	/** passes which object changed, along with optional integer (e.g. for character) (from MesquiteListener interface)*/
	public void changed(Object caller, Object obj, Notification notification){
		parametersChanged();
	}

	CharacterData chooseOther(CharacterData data){
		if (data == null)
			return null;
		Taxa taxa = data.getTaxa();
		int numSets = getProject().getNumberCharMatricesVisible(taxa);
		int numSetsDiff = numSets;
		for (int i = 0; i<numSets; i++) {
			CharacterData pData =getProject().getCharacterMatrixVisible(taxa, i);
			if (pData== data)
				numSetsDiff--;
			else if (pData.getClass() != data.getClass())
				numSetsDiff--;
		}
		if (numSetsDiff<=0) {
			alert("Sorry, there are no other compatible data matrices available for comparison.  If the other matrix is in another file, open the file as a linked file before attempting to compare.");
			return null;
		}
		else {
			Listable[] matrices = new Listable[numSetsDiff];
			int count=0;
			for (int i = 0; i<numSets; i++) {
				CharacterData pData =getProject().getCharacterMatrixVisible(taxa, i);
				if (pData!= data && (pData.getClass() == data.getClass())) {
					matrices[count]=pData;
					count++;
				}
			}
			boolean differenceFound=false;
			oData = (CharacterData)ListDialog.queryList(containerOfModule(), "Compare with", "Compare data matrix with:", MesquiteString.helpString,matrices, 0);
			if (oData==null)
				return null;
			oData.addListener(this);
		}
		return oData;
	}
	CharacterData oData = null;

	/*======================================================*/
	public MatrixFlags flagMatrix(CharacterData data, MatrixFlags flags) {
		if (data!=null && data.getNumChars()>0 && data instanceof CategoricalData){
			if (flags == null)
				flags = new MatrixFlags(data);
			else 
				flags.reset(data);
			currentData = data;
			if (oData == null){
				oData = chooseOther(data);
				if (oData == null)
					return flags;
			}
			log("Comparing this matrix " + data.getName() + " with other matrix " + oData.getName()+ ".");
			boolean diffNumChars = false;
			if (oData.getNumChars() != data.getNumChars()){
				log(" — Difference in number of characters: this matrix has " + data.getNumChars() + "; other has " + oData.getNumChars()+ ".");
				diffNumChars = true;
			}
			boolean[][] cellFlags = flags.getCellFlags();
			CharacterState cs1 = null;
			CharacterState cs2 = null;
			long count=0;
			for (int it = 0; it<data.getNumTaxa() && it<oData.getNumTaxa(); it++){
				for (int ic = 0; ic<data.getNumChars() && ic<oData.getNumChars(); ic++){
					cs1 = data.getCharacterState(cs1, ic, it);
					cs2 = oData.getCharacterState(cs2, ic, it);
					if (!cs1.equals(cs2, false, true)) {
						cellFlags[ic][it] = true;
						count++;
					}
				}
			}
			int moreInThis = data.getNumChars()-oData.getNumChars();

			if (moreInThis>0){
				//flag parts of this matrix that other doesn't have
				Bits charFlags = flags.getCharacterFlags();
				for (int ic = oData.getNumChars(); ic<data.getNumChars(); ic++)
					charFlags.setBit(ic, true);
			}

			if (count == 0){
				if (diffNumChars){
					if (moreInThis>0)
						logln(" No differences found among the cells examined, but this matrix has " + moreInThis + "more characters than the other.");
					else
						logln(" No differences found among the cells examined, but the other matrix has " + (-moreInThis) + "more characters than this.");
				}
				else
					logln(" No differences found between matrices.");

			}
			else {
				log(" " + count);
				logln(" cells of matrix found different between matrices.");
				if (diffNumChars){
					if (moreInThis>0)
						logln(" In addition, this matrix has " + moreInThis + " more characters than the other.");
					else
						logln(" In addition, the other matrix has " + (-moreInThis) + "more characters than this.");
				}
			}

		}

		return flags;

	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;  
	}

	/*.................................................................................................................*/
	public boolean showCitation(){
		return false;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Compare to other matrix";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Flags sites differing from other matrix." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}


}



