/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 



Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/


package mesquite.lists.MarkTaxonInList;
/*~~  */

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;

import mesquite.lists.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.ManagerAssistant;
import mesquite.lib.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class MarkTaxonInList extends TaxonListAssistant {
	Taxa taxa;
	MesquiteTable table=null;
	String markName;
	String markCode;
	NameReference nameReference;
	boolean startWithNew = true;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		markName = "+";
		markCode = codeFromName(markName);
		nameReference = NameReference.getNameReference(markCode);
			
		addMenuItem("Set Name of Mark...", makeCommand("setName", this));
		addMenuItem("Mark Selected", makeCommand("markSelected", this));
		addMenuItem("Unmark Selected", makeCommand("unmarkSelected", this));
		addMenuSeparator();
		addMenuItem("New Mark...", makeCommand("newMark", this));
		addMenuItem("Select Other Mark...", makeCommand("selectMark", this));
		addMenuItem("Delete Mark...", makeCommand("deleteMark", this));
		return true;
	}

	public boolean canHireMoreThanOnce(){
		return true;
	}
	/*.................................................................................................................*/
	public String getExplanationForRow(int ic){
		if (taxa!=null ) {

			if (isMarked(ic))
				return "This taxon is MARKED.  To mark a taxon, touch on this cell with the arrow cursor";
			else
				return "This taxon is UNMARKED.  To mark a taxon, touch on this cell with the arrow cursor";
		}
		return null;
	}

	String codeFromName(String name){
		return "mark*." + name;
	}
	String nameFromCode(String code){
		if (code == null)
			return "";
		if (code.indexOf("mark*.")==0)
			return code.substring(6, code.length());
		return code;
	}
	boolean isMarked(int ic){
		boolean c = taxa.getAssociatedBit(nameReference, ic);
		if (!c && taxa.getWhichAssociatedBits(nameReference) == null){  //need to reset!  Mark has been deleted
			markName = "+";
			markCode = codeFromName(markName);
			nameReference = NameReference.getNameReference(markCode);
		}

		return c;
	}
	void mark(int ic, boolean toMark){
		taxa.setAssociatedBit(nameReference,  ic, toMark);
	}
	/*
	boolean isMarked(int ic){
		long c = taxa.getAssociatedLong(nameReference, ic);

		return c== 1;
	}
	void mark(int ic, boolean toMark){
		if (toMark)
			taxa.setAssociatedLong(nameReference,  ic, 1);
		else
			taxa.setAssociatedLong(nameReference,  ic, 0);
	}
	 */
	/** Gets background color for cell for row ic.  Override it if you want to change the color from the default. */
	public Color getBackgroundColorOfCell(int it, boolean selected){
		if (isMarked(it))
			return ColorDistribution.lightGreen;
		else
			return super.getBackgroundColorOfCell(it, selected);
	}
	/*.................................................................................................................*/
	public boolean arrowTouchInRow(Graphics g, int ic, int x, int y, boolean doubleClick, int modifiers){
		mark (ic, !isMarked(ic));
		if (table != null)
			table.repaintAll();
		return true;
	}

	/*.................................................................................................................*/
	public void setTableAndTaxa(MesquiteTable table, Taxa taxa){
		this.taxa = taxa;
		this.table = table;
		if (MesquiteThread.isScripting())
			return;
		int count = 0;
		for (int i=0; i<taxa.getNumberAssociatedBits(); i++){
			String name = taxa.getAssociatedBits(i).getNameReference().getValue();
			if (!name.equalsIgnoreCase("selected") && name.startsWith("mark*."))
				count++;
		}
		if (count > 0){  //there are some available
			if (AlertDialog.query(containerOfModule(), "New Mark?", "Do you want to define a new mark, or use an existing one?", "New", "Existing")){
				doCommand("setName", null, CommandChecker.defaultChecker);
				return;
			}
		}
		
		Bits bits = taxa.getWhichAssociatedBits(nameReference);  //see if there is already one linked here; if so, just rename;
		if (bits == null){  //choosing which mark to show
			selectMark(true, false, true, "show");

		}

	}

	String selectMark(boolean queryIfNone, boolean queryIfOne, boolean setMark, String verb){
		//select a bits and extract its name, otherwise use +/-
		int count = 0;
		String candidate = null;
		for (int i=0; i<taxa.getNumberAssociatedBits(); i++){
			String name = taxa.getAssociatedBits(i).getNameReference().getValue();
			if (!name.equalsIgnoreCase("selected") && name.startsWith("mark*.")){
				count++;
				candidate = name;
			}
		}
		if (count > 1 || (count == 1 && queryIfOne)){

			String[] bitsNames = new String[count];
			count = 0;
			for (int i=0; i<taxa.getNumberAssociatedBits(); i++){
				String name = taxa.getAssociatedBits(i).getNameReference().getValue();
				if (!name.equalsIgnoreCase("selected") && name.startsWith("mark*."))
					bitsNames[count++] = nameFromCode(name);
			}
			int ib = ListDialog.queryList(containerOfModule(), "Which mark?", "Select which mark to " + verb, null, bitsNames, 0);
			if (ib>=0 && ib< bitsNames.length){
				candidate = bitsNames[ib];
				if (setMark){
					markName = candidate;
					markCode = codeFromName(markName);
					nameReference = NameReference.getNameReference(markCode);
				}
				return codeFromName(candidate);
			}
		}
		else if (count ==1) {
			if (setMark){
				markName = nameFromCode(candidate);
				markCode = candidate;
				nameReference = NameReference.getNameReference(markCode);
			}
			return markCode;
		}
		else if (count == 0 && queryIfNone) 
			doCommand("setName", null, CommandChecker.defaultChecker);
		else
			discreetAlert("There are no marks available");
		return null;
	}
	/*.................................................................................................................*/
	public void dispose() {
		super.dispose();
		if (taxa!=null)
			taxa.removeListener(this);
	}


	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();

		temp.addLine("setName " + ParseUtil.tokenize(markCode)); 

		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the name of the mark", null, commandName, "setName")) {
			String code = parser.getFirstToken(arguments);
			if (code == null){
				if (!MesquiteThread.isScripting()){
					String name = MesquiteString.queryString(containerOfModule(), "Name of mark", "What is the name of this mark?", markName);
					if (name != null){
						markCode = codeFromName(name);
						markName = name;
						Bits bits = taxa.getWhichAssociatedBits(nameReference);  //see if there is already one linked here; if so, just rename;
						nameReference = NameReference.getNameReference(markCode);
						if (bits != null)
							bits.setNameReference(nameReference);
						else
							taxa.makeAssociatedBits(markCode);
						if (table != null)
							table.repaintAll();
					}
				}
				return null;
			}
			markCode = code;
			markName = nameFromCode(code);
			nameReference = NameReference.getNameReference(markCode);
		}
		else if (checker.compare(this.getClass(), "Select Mark", null, commandName, "selectMark")) {
			selectMark(false, true, true, "show");
			if (table != null)
				table.repaintAll();
		}
		else if (checker.compare(this.getClass(), "Delete Mark", null, commandName, "deleteMark")) {
			String code = selectMark(false, true, false, "delete");
			if (code == null)
				return null;
			taxa.removeAssociatedBits(NameReference.getNameReference(code));  //see if there is already one linked here; if so, just rename;
			if (code.equals(markCode))
				selectMark(false, true, true, "show");
			if (table != null)
				table.repaintAll();
		}
		else if (checker.compare(this.getClass(), "Make New Mark", null, commandName, "newMark")) {
			if (!MesquiteThread.isScripting()){
				String name = MesquiteString.queryString(containerOfModule(), "Name of new mark", "What is the name of the new mark?", "+");
				if (name != null){
					markCode = codeFromName(name);
					markName = name;
					nameReference = taxa.makeAssociatedBits(markCode);
					if (table != null)
						table.repaintAll();
				}
			}
		}
		else if (checker.compare(this.getClass(), "Marks selected taxa", null, commandName, "markSelected")) {
			if (taxa != null && table != null){
				for (int i = 0; i< taxa.getNumTaxa(); i++){
					if (taxa.getSelected(i) || table.isRowSelected(i))
						mark(i, true);
				}
			}
			else if (taxa != null){
				for (int i = 0; i< taxa.getNumTaxa(); i++){
					if (taxa.getSelected(i))
						mark(i, true);
				}
			}
			
			if (table != null)
				table.repaintAll();
		}
		else if (checker.compare(this.getClass(), "Unmarks selected taxa", null, commandName, "unmarkSelected")) {
			if (taxa != null && table != null){
				for (int i = 0; i< taxa.getNumTaxa(); i++){
					if (taxa.getSelected(i) || table.isRowSelected(i))
						mark(i, false);
				}
			}
			else if (taxa != null){
				for (int i = 0; i< taxa.getNumTaxa(); i++){
					if (taxa.getSelected(i))
						mark(i, false);
				}
			}
			
			if (table != null)
				table.repaintAll();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	public String getTitle() {
		return "Mark";
	}
	public String getStringForTaxon(int ic){

		if (taxa!=null) {
			if (isMarked(ic))
				return markName;
			else
				return "-";
		}
		return "-";
	}
	/*...............................................................................................................*/
	/** returns whether or not a cell of table is editable.*/
	public boolean isCellEditable(int row){
		return false;
	}
	/*...............................................................................................................*/
	public boolean useString(int ic){
		return true;
	}

	public String getWidestString(){
		if (markName.length()> 5)
			return markName;
		return "88888";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Mark Taxa";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 275;  
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Permits user to mark taxa in the List of Taxa with a specific name." ;
	}
}

