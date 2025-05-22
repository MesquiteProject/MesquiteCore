/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.basic.ManageArchivedTaxonNames;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.TaxaStringsSet;
import mesquite.lib.ui.MesquiteSubmenuSpec;

public class ManageArchivedTaxonNames extends SpecsSetManager {
	final static String listOfAlternativeNameSetsName = "List of Alternative Name Sets";
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}


	/*.................................................................................................................*/
	/** A method called immediately after the file has been read in.*/
	public void projectEstablished() {
		MesquiteSubmenuSpec mmis = getFileCoordinator().addSubmenu(MesquiteTrunk.treesMenu,listOfAlternativeNameSetsName, makeCommand("showAlternativeNamesList",  this), (ListableVector)getProject().taxas);
		mmis.setOwnerModuleID(getID());
		mmis.setBehaviorIfNoChoice(MesquiteSubmenuSpec.ONEMENUITEM_ZERODISABLE);
		super.projectEstablished();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			MesquiteModule e=(MesquiteModule)getEmployeeVector().elementAt(i);
			if (e instanceof ManagerAssistant && (e.getModuleWindow()!=null) && e.getModuleWindow().isVisible() && e.getName().equals(listOfAlternativeNameSetsName)) {
				Object o = e.doCommand("getTaxa", null, CommandChecker.defaultChecker);

				if (o !=null && o instanceof Taxa) {
					//int wh =getProject().getTaxaReference((Taxa)o);
					temp.addLine("showAlternativeNamesList " + getProject().getTaxaReferenceExternal((Taxa)o), e); 
				}
				else
					temp.addLine("showAlternativeNamesList ", e); 
			}
		}
		return temp;
	}
	public void elementsReordered(ListableVector v){
	}
	/*.................................................................................................................*/
	public MesquiteModule showElement(FileElement e){
		//TODO:
		if (e != null)
			alert("Sorry, the " + e.getTypeName() + "  cannot be shown by this means yet.");
		return null;
	}
	public NexusBlock elementAdded(FileElement e){
		return null;
	}
	/*.................................................................................................................*/
	/** A method called immediately after the file has been read in or completely set up (if a new file).*/
	public void fileReadIn(MesquiteFile f) {
		/* this is for old files that used the SUPPLEMENTAL storage of a single archive.  Converted to SpecsSet as per 2. 0 */
		NameReference anr = NameReference.getNameReference("ArchivedTaxonNames");
		ListableVector taxas = getProject().getTaxas();
		for (int i = 0; i< taxas.size(); i++){
			Taxa taxa = (Taxa)taxas.elementAt(i);
			TaxaStringsSet part = null;
			boolean changed = false;
			taxa.prepareSpecsSetVector(TaxaStringsSet.class, "Alternative Names");
			for (int it = 0; it< taxa.getNumTaxa(); it++){
				Object n = taxa.getAssociatedObject(anr, it);
				if (part == null && n != null){
						part= new TaxaStringsSet("Alternative Naming from Archived", taxa.getNumTaxa(), taxa);
						part.setTypeName("Alternative Names");
						part.addToFile(taxa.getFile(), getProject(), findElementManager(TaxaStringsSet.class));
						taxa.setCurrentSpecsSet(part, TaxaStringsSet.class);
						changed = true;
				}
				if (n != null)
					taxa.setAssociatedObject(anr, it, null);
				if (part != null)
					part.setProperty(n, it);
			}
			if (changed)
				taxa.notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));
		}

	}



	public void elementDisposed(FileElement e){
		//nothing needs doing since separate reference not stored locally
	}
	public Class getElementClass(){
		return TaxaStringsSet.class;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Shows lists of the alternative name sets", null, commandName, "showAlternativeNamesList")) {
			if (StringUtil.blank(arguments)) {
				for (int i = 0; i< getProject().getNumberTaxas(checker.getFile()); i++) {
					showSpecsSets(getProject().getTaxa(checker.getFile(), i), listOfAlternativeNameSetsName);
				}
			}
			else {
				Taxa t = getProject().getTaxa(checker.getFile(), parser.getFirstToken(arguments));
				if (t!=null ) {
					return showSpecsSets(t, listOfAlternativeNameSetsName);
				}
			}
//			alert("Sorry, there are no taxa partitions");
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	String nexusStringForSpecsSet(TaxaStringsSet taxaSet, Taxa taxa, MesquiteFile file, boolean isCurrent){
		String s= "";
		boolean found = false;
		if (taxaSet!=null && (taxaSet.getFile()==file || (taxaSet.getFile()==null && taxa.getFile()==file))) {
			String sT = "";
			for (int ic=0; ic<taxa.getNumTaxa(); ic++) {
				if (StringUtil.blank((String)taxaSet.getProperty(ic))) 
					sT += " _";
				else {
					sT += " " + StringUtil.tokenize((String)taxaSet.getProperty(ic));
					found = true;
				}
			}
			if (found) {
				s+= "\tALTTAXNAMES " ;
				if (isCurrent)
					s += "* ";
				s+= StringUtil.tokenize(taxaSet.getName()) + " ";
				if (file.getProject().getNumberTaxas()>1)
					s+= " (TAXA = " +  StringUtil.tokenize(taxa.getName()) + ")";
				s+= " = "+  sT + ";" + StringUtil.lineEnding();
			}
		}
		return s;
	}
	/*.................................................................................................................*/
	public String getNexusCommands(MesquiteFile file, String blockName){ 
		if (blockName.equalsIgnoreCase("NOTES")) {
			String s= "";
			for (int ids = 0; ids<file.getProject().getNumberTaxas(); ids++) {

				Taxa taxa =  file.getProject().getTaxa(ids);
				if (taxa.getFile() == file){
					int numSets = taxa.getNumSpecsSets(TaxaStringsSet.class);
					SpecsSetVector ssv = taxa.getSpecSetsVector(TaxaStringsSet.class);
					if (ssv!=null){
						TaxaStringsSet ms = (TaxaStringsSet)taxa.getCurrentSpecsSet(TaxaStringsSet.class);
						if (ms!=null && (ms.getNexusBlockStored()==null || blockName.equalsIgnoreCase(ms.getNexusBlockStored()))) {
							ms.setNexusBlockStored(blockName);
							ms.setName("UNTITLED");
							s += nexusStringForSpecsSet(ms, taxa, file, true);
						}


						for (int ims = 0; ims<numSets; ims++) {
							s += nexusStringForSpecsSet((TaxaStringsSet)taxa.getSpecsSet(ims, TaxaStringsSet.class), taxa, file, false);
						}
					}
				}

			}
			return s;
		}
		return null;
	}
	int findWhichTaxon(int it, boolean fuse, Taxa taxa){
		if (!fuse)
			return it;
		IntegerArray oi = (IntegerArray)taxa.getAttachment("originalIndicesDupRead", IntegerArray.class);
		if (oi == null) //don't have translation; don't trust and indicate whichTaxon -1
			return MesquiteInteger.unassigned;
		int w = oi.getValue(it);
		if (MesquiteInteger.isCombinable(w))
			return w;
		return MesquiteInteger.unassigned;
	}
	/*.................................................................................................................*/
	public boolean readNexusCommand(MesquiteFile file, NexusBlock nBlock, String blockName, String command, MesquiteString comment, String fileReadingArguments){ 
		if (blockName.equalsIgnoreCase("NOTES")) {
			MesquiteInteger startCharT = new MesquiteInteger(0);

			String commandName = ParseUtil.getToken(command, startCharT);
			if (commandName.equalsIgnoreCase("ALTTAXNAMES")) {
				boolean fuse = parser.hasFileReadingArgument(file.fileReadingArguments, "fuseTaxaCharBlocks");
				
				String token = ParseUtil.getToken(command, startCharT);
				boolean isDefault = false;
				if ("*".equals(token)) {
					token = ParseUtil.getToken(command, startCharT);
					isDefault = true;
				}
				String nameOfTypeset = StringUtil.deTokenize(token); // name of typeset 
				token = ParseUtil.getToken(command, startCharT);
				String paradigmString = null;
				Taxa taxa = null;
				if (token.equalsIgnoreCase("(")) {
					token = ParseUtil.getToken(command, startCharT); //TAXA  //TODO: check to see what parameter is being set!
					token = ParseUtil.getToken(command, startCharT); //=
					token = (ParseUtil.getToken(command, startCharT)); // name of data
					taxa = file.getProject().getTaxaLastFirst(token);
					token = ParseUtil.getToken(command, startCharT); //)
					token = ParseUtil.getToken(command, startCharT);  //=
				}
				else if (file.getProject().getNumberTaxas(file)>0){
					taxa = file.getProject().getTaxa(file, 0);
				}
				else  
					taxa = file.getProject().getTaxa(0);

				if (taxa == null)
					return false;

				if (token.equals("="))
					token = ParseUtil.getToken(command, startCharT); 

				TaxaStringsSet taxaSet= new TaxaStringsSet(nameOfTypeset, taxa.getNumTaxa(), taxa);
				taxaSet.setTypeName("Alternative Names");
				taxaSet.setNexusBlockStored(blockName);

				int lastChar = -1;
				boolean join = false;
				int i = -1;
				while (!token.equals(";") && token.length()>0) {
					i++;
					int it = findWhichTaxon(i, fuse, taxa);
					if (token.equals(" ")) {
						taxaSet.setProperty("", it);
					}
					else {
						taxaSet.setProperty(token, it);
					}
					token = ParseUtil.getToken(command, startCharT); 
				}

				if (!fuse && isDefault) {
					if (!"UNTITLED".equals(taxaSet.getName())) {
						taxa.storeSpecsSet(taxaSet, TaxaStringsSet.class);
					}
					taxaSet.addToFile(file, getProject(), this);
					taxa.setCurrentSpecsSet(taxaSet, TaxaStringsSet.class);
				}
				else {
					taxa.storeSpecsSet(taxaSet, TaxaStringsSet.class);
					taxaSet.addToFile(file, getProject(), this);
				}
				return true;
			}
		}
		return false;
	}
	/*.................................................................................................................*/
	public NexusCommandTest getNexusCommandTest(){ 
		return new TSetNexusCommandTest();
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Manage Alternative Taxon Names";
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Manages (including NEXUS read/write) alternative taxon names." ;
	}
	/*.................................................................................................................*/

}

class TSetNexusCommandTest  extends NexusCommandTest{
	public boolean readsWritesCommand(String blockName, String commandName, String command){  //returns whether or not can deal with command
		return ((blockName.equalsIgnoreCase("NOTES")) && (commandName.equalsIgnoreCase("ALTTAXNAMES")));
	}
}


