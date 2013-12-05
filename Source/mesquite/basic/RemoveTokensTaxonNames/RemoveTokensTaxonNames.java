/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.basic.RemoveTokensTaxonNames;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.JLabel;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class RemoveTokensTaxonNames extends TaxonNameAlterer implements KeyListener {
	int numStart = 0;
	int numEnd = 0;
	IntegerField startTokensField = null;
	IntegerField endTokensField = null;
	JLabel currentName, alteredName;
	int firstTaxon = 0;
	Taxa currentTaxa = null;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}	

	/*.................................................................................................................*/
	public void checkFields() {
		if (currentTaxa==null)
			return;
		String name = currentTaxa.getName(firstTaxon);
		MesquiteString newName = new MesquiteString();
		numStart = startTokensField.getValue();
		numEnd = endTokensField.getValue();
		boolean nameChanged = getNewName(currentTaxa, firstTaxon, newName);
		if (nameChanged)
			name = newName.getValue();
		alteredName.setText("Modified Name: " + name);	
		alteredName.repaint();
	}
	/*.................................................................................................................*/
	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
		checkFields();
	}

	public void keyTyped(KeyEvent e) {
		checkFields();
	}

	/*.................................................................................................................*/
	public boolean getOptions(Taxa taxa, int firstSelected){
		if (MesquiteThread.isScripting())
			return true;
		if (MesquiteInteger.isCombinable(firstSelected))
			firstTaxon = firstSelected;
		currentTaxa = taxa;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Remove tokens",  buttonPressed);
		queryDialog.addLabel("Remove Tokens", Label.CENTER);
		startTokensField = queryDialog.addIntegerField("Number of tokens to remove from start", numStart, 4,0,1000);
		startTokensField.addKeyListener(this);
		endTokensField = queryDialog.addIntegerField("Number of tokens to remove from end", numEnd, 4,0,1000);
		endTokensField.addKeyListener(this);
		queryDialog.addHorizontalLine(2);
		queryDialog.addLabel("Preview of first taxon name to be changed:");
		currentName = queryDialog.addLabel("");	
		alteredName = queryDialog.addLabel("");	
		String name = currentTaxa.getName(firstTaxon);
		currentName.setText("Current Name: " + name);		
		checkFields();
		queryDialog.addHorizontalLine(2);
		queryDialog.addLargeOrSmallTextLabel("Hint: before doing this, you may want to archive current names using Archive Taxon Names under Taxon Utilities in the Character Matrix Editor or the List of Taxa window" );
		queryDialog.completeAndShowDialog(true);

		boolean ok = (queryDialog.query()==0);

		if (ok) {
			numStart = startTokensField.getValue();
			numEnd = endTokensField.getValue();
		}

		queryDialog.dispose();

		return ok;
	}
	/*.................................................................................................................*/
	/** Called to alter the taxon name in a single cell.  If you use the alterContentOfCells method of this class, 
   	then you must supply a real method for this, not just this stub. */
	public boolean getNewName(Taxa taxa, int it, MesquiteString ms){
		if (ms==null)
			return false;
		boolean nameChanged = false;
		String name = taxa.getTaxonName(it);
		if (name!=null){
			Parser parser = new Parser();
			//	parser.setWhitespaceString("|");
			parser.addToDefaultPunctuationString("|");
			parser.setString(name);

			for (int i=0; i<numEnd; i++) {
				String token = parser.getLastToken();
				if (token != null){
					int pos = name.indexOf(token);
					if (pos>=0) {
						name = name.substring(0, pos);
						nameChanged = true;
					}
				}
				parser.setString(name);
			}
			for (int i=0; i<numStart; i++) {
				String token = parser.getFirstToken();
				if (token != null){
					int pos = name.indexOf(token);
					if (pos>=0) {
						name = name.substring(pos+token.length());
						nameChanged = true;
					}
				}
				parser.setString(name);
			}
			if (nameChanged){
				name = name.trim();
				ms.setValue(name);
			}

		}
		return nameChanged;
	}
	/*.................................................................................................................*/
	/** Called to alter the taxon name in a single cell.  If you use the alterContentOfCells method of this class, 
   	then you must supply a real method for this, not just this stub. */
	public boolean alterName(Taxa taxa, int it){
		MesquiteString newName = new MesquiteString();
		boolean nameChanged = getNewName(taxa, it, newName);
		if (nameChanged)
			taxa.setTaxonName(it, newName.getValue(), false);
		return nameChanged;
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Remove Tokens...";
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Remove Tokens";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Removes tokens from start or end of the taxon name.";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 273;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}

}

