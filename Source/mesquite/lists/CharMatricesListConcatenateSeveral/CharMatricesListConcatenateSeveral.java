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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import mesquite.lib.CommandChecker;
import mesquite.lib.IntegerField;
import mesquite.lib.ListableVector;
import mesquite.lib.LongField;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteLong;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.RadioButtons;
import mesquite.lists.lib.CharMatricesListProcessorUtility;
import mesquite.lists.lib.ListModule;
import mesquite.lists.CharMatricesListConcatenate.CharMatricesListConcatenate;

/* ======================================================================== */
public class CharMatricesListConcatenateSeveral extends mesquite.lists.CharMatricesListConcatenate.CharMatricesListConcatenate implements ItemListener {
	int numParts = 1;
	int numMatricesPerPart = 100;
	long matrixSizeTarget =20000;
	
	static int USESIZETARGET=0;
	static int USENUMPARTS=1;
	
	int concatStyle = USESIZETARGET;
	
	
	
	
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
		if ("matrixSizeTarget".equalsIgnoreCase(tag))
			matrixSizeTarget = MesquiteLong.fromString(content);
		if ("concatStyle".equalsIgnoreCase(tag))
			concatStyle = MesquiteInteger.fromString(content);
		super.processSingleXMLPreference(tag, content);
	}

	/*.................................................................................................................*/
	public String preparePreferencesForXML() {
		StringBuffer buffer = new StringBuffer(super.preparePreferencesForXML());
		StringUtil.appendXMLTag(buffer, 2, "numParts", numParts);
		StringUtil.appendXMLTag(buffer, 2, "matrixSizeTarget", matrixSizeTarget);
		StringUtil.appendXMLTag(buffer, 2, "concatStyle", concatStyle);
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = super.getSnapshot(file);
		temp.addLine("numParts "+ numParts);
		temp.addLine("matrixSizeTarget "+ matrixSizeTarget);
		temp.addLine("concatStyle "+ concatStyle);
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets length of concatenated matrices to create.", "[long]", commandName, "matrixSizeTarget")) {
			long temp = MesquiteLong.fromString(parser.getFirstToken(arguments));
			boolean changed=false;
			if (MesquiteLong.isCombinable(temp)) {
				if (temp!=matrixSizeTarget) {
					changed=true;
					}
				matrixSizeTarget = temp;
				if (changed) {
					parametersChanged();
				}
			}
		}
		else	if (checker.compare(this.getClass(), "Sets number of concatenated matrices to create.", "[int]", commandName, "numParts")) {
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
		else	if (checker.compare(this.getClass(), "Sets how to concatenate.", "[int]", commandName, "concatStyle")) {
			int temp = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			boolean changed=false;
			if (MesquiteInteger.isCombinable(temp)) {
				if (temp!=concatStyle) {
					changed=true;
					}
				concatStyle = temp;
				if (changed) {
					parametersChanged();
				}
			}
		}
			else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	
	/*.................................................................................................................*/
	public String getDialogTitle() {
		return "Options to Concatenate to Several Matrices";
	}

	IntegerField numPartsField=null;
	LongField matrixSizeTargetField=null;
	RadioButtons concatStyleButtons=null;
	/*.................................................................................................................*/
	public void addExtraOptions(ExtensibleDialog dialog) {
		concatStyleButtons = dialog.addRadioButtons (new String[] {"specify sequence length of each matrix", "specify number of matrices"}, concatStyle);
		matrixSizeTargetField = dialog.addLongField("Approximate sequence length of each matrix: ", matrixSizeTarget, 8);
		numPartsField = dialog.addIntegerField("Number of concatenated matrices: ", numParts, 8, 1, MesquiteInteger.infinite);
		concatStyleButtons.addItemListener(this);
		dialog.addHorizontalLine(1);
		checkEnabled(concatStyle);
	}
	/*.................................................................................................................*/
	public void processExtraOptions() {
		if (numPartsField!=null) {
			int temp = numPartsField.getValue();
			if (MesquiteInteger.isCombinable(temp))
				numParts=temp;
		}
		if (matrixSizeTargetField!=null) {
			long temp = matrixSizeTargetField.getValue();
			if (MesquiteLong.isCombinable(temp))
				matrixSizeTarget=temp;
		}
		if (concatStyleButtons!=null) {
			concatStyle=concatStyleButtons.getValue();
		}
	}
	/*.................................................................................................................*/

	public void checkEnabled(int concatStyle) {
		numPartsField.setEnabled(concatStyle==USENUMPARTS);
		matrixSizeTargetField.setEnabled(concatStyle==USESIZETARGET);
	}

	/*.................................................................................................................*/
	public void itemStateChanged(ItemEvent e) {
		if (concatStyleButtons.isAButton(e.getItemSelectable()))
			checkEnabled (concatStyleButtons.getValue());
	}

	/*.................................................................................................................*/

	public boolean concatenate (ListableVector datas, MesquiteInteger count, MesquiteInteger countFailed, MesquiteInteger deleted, MesquiteInteger created) {
		int numMatrices = datas.size();
		boolean found = false;
		int part=1;

		if (concatStyle==USENUMPARTS) {
			numMatricesPerPart = (int)(numMatrices/numParts);
			if (numMatrices % numParts>0)
				numMatricesPerPart++;
			if (numMatricesPerPart<1)
				numMatricesPerPart=1;
			if (numMatricesPerPart>numMatrices)
				numMatricesPerPart=numMatrices;
			for (int i=0; i<numMatrices; i+= numMatricesPerPart) {
				if (concatenateSome(datas, numMatrices, i, i+numMatricesPerPart-1, true, part, count, countFailed, deleted, created))
					found = true;
				part++;
			}
		} else {
			int firstData=0;
			long currentSize =0;
			for (int i=0; i<numMatrices; i++) {
				CharacterData data = (CharacterData)datas.elementAt(i);
				currentSize+= data.getNumChars();  // length of this next matrix added on
				if (currentSize>=matrixSizeTarget || i==numMatrices-1) {  // we've found enough
					if (concatenateSome(datas, numMatrices, firstData, i, true, part, count, countFailed, deleted, created))
						found = true;
					firstData=i+1;
					currentSize=0;
					part++;
				}
			}
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

