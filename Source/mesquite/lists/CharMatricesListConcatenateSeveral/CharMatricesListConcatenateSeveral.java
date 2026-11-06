/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.CharMatricesListConcatenateSeveral;
/* created May 02 */

import java.awt.Checkbox;
import java.util.Vector;

import mesquite.lib.CommandChecker;
import mesquite.lib.IntegerField;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lists.lib.CharMatricesListProcessorUtility;
import mesquite.lists.lib.ListModule;
import mesquite.lists.CharMatricesListConcatenate.CharMatricesListConcatenate;

/* ======================================================================== */
public class CharMatricesListConcatenateSeveral extends mesquite.lists.CharMatricesListConcatenate.CharMatricesListConcatenate {
	int numParts = 1;
	int numMatricesPerPart = 100;
	/*.................................................................................................................*/
	public String getName() {
		return "Concatenate Matrices into Several Matrices";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Concatenate Matrices into Several Matrices...";
	}

	public String getExplanation() {
		return "Concatenates  matrices in List of Character Matrices window into several matrices.  Only those compatible with first selected are concatenated into it." ;
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference(String tag, String content) {
		if ("numParts".equalsIgnoreCase(tag))
			numParts = MesquiteInteger.fromString(content);
		super.processSingleXMLPreference(tag, content);
	}

	/*.................................................................................................................*/
	public String preparePreferencesForXML() {
		StringBuffer buffer = new StringBuffer(super.preparePreferencesForXML());
		StringUtil.appendXMLTag(buffer, 2, "numParts", numParts);
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = super.getSnapshot(file);
		temp.addLine("numParts "+ numParts);
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets number of concatenated matrices to create.", "[int]", commandName, "numParts")) {
			int temp = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			boolean changed=false;
			if (MesquiteInteger.isCombinable(temp)) {
				if (temp!=numParts) {
					changed=true;
					}
				numParts = temp;
				if (changed) {
					parametersChanged();
				}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	IntegerField numPartsField=null;
	/*.................................................................................................................*/
	public void addExtraOptions(ExtensibleDialog dialog) {
		numPartsField = dialog.addIntegerField("Number of concatenated matrices: ", numParts, 8, 1, MesquiteInteger.infinite);
	}
	/*.................................................................................................................*/
	public void processExtraOptions() {
		if (numPartsField!=null) {
			int temp = numPartsField.getValue();
			if (MesquiteInteger.isCombinable(temp))
				numParts=temp;
		}
	}

	
	/*.................................................................................................................*/
	
	public boolean concatenate (ListableVector datas, MesquiteInteger count, MesquiteInteger countFailed, MesquiteInteger deleted, MesquiteInteger created) {
		int numMatrices = datas.size();
		numMatricesPerPart = (int)(numMatrices/numParts);
		if (numMatrices % numParts>0)
			numMatricesPerPart++;
		if (numMatricesPerPart<1)
			numMatricesPerPart=1;
		if (numMatricesPerPart>numMatrices)
			numMatricesPerPart=numMatrices;
		boolean found = false;
		int part=1;
		for (int i=0; i<numMatrices; i+= numMatricesPerPart) {
			if (concatenateSome(datas, numMatrices, i, i+numMatricesPerPart-1, true, part, count, countFailed, deleted, created))
				found = true;
			part++;
		}
		return found;
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;  
	}
	public void endJob() {
		super.endJob();
	}

}

