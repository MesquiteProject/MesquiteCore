/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.SelectCharacterRanges;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.RadioButtons;
import mesquite.lib.ui.SingleLineTextArea;

/* ======================================================================== */
public class SelectCharacterRanges extends CharacterSelector {
	boolean bracketFormat = true;
	String ranges = "";
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public void selectRange(CharacterData data, int icStart, int icEnd){
			for (int i=icStart; i<=icEnd; i++) {
					data.setSelected(i, true);
			}
   	}

	boolean queryOptions(){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Character Ranges to Select",  buttonPressed);
		queryDialog.addLabel("Character Ranges to Select", Label.CENTER);
		String help="Ranges should be specified using one of two formats:<br>";
		help += "(1) bracketed ranges, without hyphens, of the form \"[startOfRange1 endOfRange1] [startOfRange2 endOfRange2] [startOfRange3 endOfRange3]\". This is the format used in GBlocks output.";
		help += "<br>(2) hyphenated ranges, of the form \"startOfRange1 - endOfRange1  startOfRange2 - endOfRange2 startOfRange3 - endOfRange3\"";
		queryDialog.appendToHelpString(help);

		SingleLineTextArea rangeText = queryDialog.addSingleLineTextArea(ranges,5);

		queryDialog.addLabel("Format of ranges:", Label.CENTER);
		RadioButtons formatRadios = queryDialog.addRadioButtons (new String[]{"bracketed ranges", "hyphenated ranges"}, 0);

		queryDialog.completeAndShowDialog(true);
		boolean ok = (queryDialog.query()==0);

		if (ok) {
			ranges = rangeText.getText();
			bracketFormat = formatRadios.getValue() ==0;
		}
		queryDialog.dispose();   		 

		return ok;
	}

   	/** Called to select characters*/
   	public void selectCharacters(CharacterData data){
   		if (data!=null && data.getNumChars()>0){
   			if (!queryOptions() || StringUtil.blank(ranges))
   				return;
   			Parser parser = new Parser(ranges);
   			//parser.setPunctuationString("[]");
   			parser.setAllowComments(false);
   			String token = parser.getNextToken();
   			int icStart = -2;
   			int icEnd = -1;

   			if (bracketFormat) {
   				while (StringUtil.notEmpty(token)) {
   					if (token.equalsIgnoreCase("[")) {  // start of range
   						icStart = MesquiteInteger.fromString(parser);
   						if (MesquiteInteger.isCombinable(icStart)) {
   							icEnd = MesquiteInteger.fromString(parser);
   							if (MesquiteInteger.isCombinable(icEnd)) {
   								selectRange(data,icStart-1,icEnd-1);
   							} else
   								selectRange(data,icStart-1,icStart-1);
   						}
   					}
   					token = parser.getNextToken();
   					if (StringUtil.notEmpty(token)) {
   						if (token.equalsIgnoreCase("]")) 
   							token = parser.getNextToken();
   					}
   				}
   			} else {
   				parser.setHyphensArePartOfNumbers(false);
   				while (StringUtil.notEmpty(token)) {
   					if (token.equalsIgnoreCase("-") || token.equalsIgnoreCase("–")) {  // now get end of range
   	   						token = parser.getNextToken();
   							icEnd = MesquiteInteger.fromString(token);  //end of range
   	   						if (MesquiteInteger.isCombinable(icStart) && MesquiteInteger.isCombinable(icEnd)) 
   	   								selectRange(data,icStart-1,icEnd-1);
   					} else {  // it should be a normal number
   						icStart = MesquiteInteger.fromString(token);
   						if (MesquiteInteger.isCombinable(icStart))
   							selectRange(data,icStart-1,icStart-1);  // might as well select it right now in case we don't ever get an end of range value
   					}
   					token = parser.getNextToken();
   				}
  			}

   			data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
   		}
   	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 302;  
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Character Ranges...";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Selects characters that are within the ranges given in a text list of the form [start end] [start end]..." ;
   	 }
   	 
}


