/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.basic.ManageTaxaSets;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/** Manages TAXSETs (not Taxa blocks; see ManageTaxa), including reading the NEXUS command for TAXSETs */
public class ManageTaxaSets extends SpecsSetManager {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(mesquite.lists.TaxonSetList.TaxonSetList.class, getName() + "  uses an assistant to display a list window.",
		"The assistant is arranged automatically");
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
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
	public void elementDisposed(FileElement e){
		//nothing needs doing since separate reference not stored locally
	}
	public Class getElementClass(){
		return TaxaSelectionSet.class;
	}
	/*.................................................................................................................*/
 	/** A method called immediately after the file has been read in.*/
 	public void projectEstablished() {
		MesquiteSubmenuSpec mmis = getFileCoordinator().addSubmenu(MesquiteTrunk.treesMenu,"List of Taxon Sets", makeCommand("showSets",  this), getProject().taxas);
		mmis.setBehaviorIfNoChoice(MesquiteSubmenuSpec.ONEMENUITEM_ZERODISABLE);
		mmis.setOwnerModuleID(getID());
		super.projectEstablished();
 	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			MesquiteModule e=(MesquiteModule)getEmployeeVector().elementAt(i);
			if (e instanceof ManagerAssistant && (e.getModuleWindow()!=null) && e.getModuleWindow().isVisible() && e.getName().equals("List of Taxon Sets")) {
				Object o = e.doCommand("getTaxa", null, CommandChecker.defaultChecker);
				
				if (o !=null && o instanceof Taxa) {
					//int wh =getProject().getTaxaReference((Taxa)o);
  	 				temp.addLine("showSets " + getProject().getTaxaReferenceExternal((Taxa)o), e); 
  	 			}
  	 			else
  	 				temp.addLine("showSets ", e); 
  	 		}
		}
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Shows lists of the taxon sets (TAXSETS)", null, commandName, "showSets")) {
    	 		if (StringUtil.blank(arguments)) {
	    	 		for (int i = 0; i< getProject().getNumberTaxas(checker.getFile()); i++) {
	    	 			showSpecsSets(getProject().getTaxa(checker.getFile(), i), "List of Taxon Sets");
				}
    	 		}
    	 		else {
    	 			Taxa t = getProject().getTaxa(checker.getFile(), parser.getFirstToken(arguments));
 	 			if (t!=null ) {
 	 				return showSpecsSets(t, "List of Taxon Sets");
 	 			}
    	 		}
//    	 			alert("Sorry, there are no taxon sets");
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
   
	/*.................................................................................................................*/
	String nexusStringForSpecsSet(TaxaSelectionSet taxaSet, Taxa taxa, MesquiteFile file, boolean isCurrent){
			String s= "";
			if (taxaSet!=null && (taxaSet.getFile()==file || (taxaSet.getFile()==null && taxa.getFile()==file))) {
				String sT = "";
				int continuing = 0;
				int lastWritten = -1;
				for (int ic=0; ic<taxa.getNumTaxa(); ic++) {
					if (taxaSet.isBitOn(ic)) {
						if (continuing == 0) {
							sT += " " + Taxon.toExternal(ic);
							lastWritten = ic;
							continuing = 1;
						}
						else if (continuing == 1) {
							sT += " - ";
							continuing = 2;
						}
					}
					else if (continuing>0) {
						if (lastWritten != ic-1) {
							sT += " " + Taxon.toExternal(ic-1);
							lastWritten = ic-1;
						}
						else
							lastWritten = -1;
						continuing = 0;
					}

				}
				if (continuing>1)
					sT += " " + Taxon.toExternal(taxa.getNumTaxa()-1);
				if (!StringUtil.blank(sT)) {
					s+= "\tTAXSET " ;
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
		if (blockName.equalsIgnoreCase("SETS")) {
			String s= "";
	 		for (int ids = 0; ids<file.getProject().getNumberTaxas(); ids++) {

				Taxa taxa =  file.getProject().getTaxa(ids);
				if (taxa.getFile() == file){
					int numSets = taxa.getNumSpecsSets(TaxaSelectionSet.class);
					SpecsSetVector ssv = taxa.getSpecSetsVector(TaxaSelectionSet.class);
					if (ssv!=null){
						TaxaSelectionSet ms = (TaxaSelectionSet)taxa.getCurrentSpecsSet(TaxaSelectionSet.class);
						if (ms!=null && (ms.getNexusBlockStored()==null || blockName.equalsIgnoreCase(ms.getNexusBlockStored()))) {
							ms.setNexusBlockStored(blockName);
							ms.setName("UNTITLED");
							s += nexusStringForSpecsSet(ms, taxa, file, true);
						}
						
							
						for (int ims = 0; ims<numSets; ims++) {
							s += nexusStringForSpecsSet((TaxaSelectionSet)taxa.getSpecsSet(ims, TaxaSelectionSet.class), taxa, file, false);
						}
					}
				}

			}
			return s;
		}
		return null;
	}
	/*.................................................................................................................*/
	public boolean readNexusCommand(MesquiteFile file, NexusBlock nBlock, String blockName, String command, MesquiteString comment){ 
		if (blockName.equalsIgnoreCase("SETS") || blockName.equalsIgnoreCase("ASSUMPTIONS")) {
			MesquiteInteger startCharT = new MesquiteInteger(0);

			String commandName = ParseUtil.getToken(command, startCharT);
			if (commandName.equalsIgnoreCase("TAXASET") || commandName.equalsIgnoreCase("TAXSET")) {
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
				else if (file.getProject().getNumberTaxas()>0)
					taxa = file.getProject().getTaxa(file, 0);
				else  
					taxa = file.getProject().getTaxa(0);

				if (taxa == null)
					return false;
				
				if (token.equals("="))
					token = ParseUtil.getToken(command, startCharT); 
				//TaxaGroup defaultProperty =  null;
	
				//TaxaGroup lastCM =new TaxaGroup();
				//lastCM.setName(token);
				
				//lastCM.setGroupNumber(MesquiteInteger.fromString(token));
		 		TaxaSelectionSet taxaSet= new TaxaSelectionSet(nameOfTypeset, taxa.getNumTaxa(), taxa);
				taxaSet.setNexusBlockStored(blockName);
				
				int lastChar = -1;
				boolean join = false;
				while (!token.equals(";") && token.length()>0) {
					if (token.equals("-")) {
						if (lastChar!=-1)
							join = true;
					}
					else if (token!=null) {
						if (token != null && token.equals("."))
							token = Integer.toString(taxa.getNumTaxa());
					
						if (token.startsWith("-")) {
							if (lastChar!=-1)
								join = true;
							token = token.substring(1, token.length());
						}
						int whichChar = Taxon.toInternal(MesquiteInteger.fromString(token, false));
						if (!MesquiteInteger.isCombinable(whichChar)) {
							whichChar = taxa.whichTaxonNumber(token);
						}
						if (MesquiteInteger.isCombinable(whichChar) && whichChar>=0) {
							if (join) {
								for (int j = lastChar; j<= whichChar; j++)
									taxaSet.setSelected(j, true);
								lastChar = -1;
								join = false;
							}
							else {
								taxaSet.setSelected(whichChar, true);
								lastChar = whichChar;
							}
						}
					}
					token = ParseUtil.getToken(command, startCharT); 
				}
				
				if (isDefault) {
					if (!"UNTITLED".equals(taxaSet.getName())) {
			 			taxa.storeSpecsSet(taxaSet, TaxaSelectionSet.class);
			 		}
			 		taxaSet.addToFile(file, getProject(), this);
					taxa.setCurrentSpecsSet(taxaSet, TaxaSelectionSet.class);
				}
				else {
		 			taxa.storeSpecsSet(taxaSet, TaxaSelectionSet.class);
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
		return "Manage taxon sets";
   	 }
   	 
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages (including NEXUS read/write) taxon sets." ;
   	 }
	/*.................................................................................................................*/
   	 
}

class TSetNexusCommandTest  extends NexusCommandTest{
	public boolean readsWritesCommand(String blockName, String commandName, String command){  //returns whether or not can deal with command
		return ((blockName.equalsIgnoreCase("SETS") || blockName.equalsIgnoreCase("ASSUMPTIONS")) && (commandName.equalsIgnoreCase("TAXASET") || commandName.equalsIgnoreCase("TAXSET")));
	}
}


