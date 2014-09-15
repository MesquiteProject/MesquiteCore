/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.ornamental.ManageHyperlinks;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class ManageHyperlinks extends FileInit /*implements ElementManager*/ {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
	}
	
	
	public boolean isSubstantive(){
		return false;
	}   	 
	public NexusBlock elementAdded(FileElement e){
		return null;
	}
	public void elementDisposed(FileElement e){
		//nothing needs doing since separate reference not stored locally
	}
	public Class getElementClass(){
		return null;
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public String getNexusCommands(MesquiteFile file, String blockName){ 
		if (blockName.equalsIgnoreCase("NOTES")) {
			String s ="";
			boolean found = false;
			MesquiteProject project = file.getProject();
			for (int i=0; i<project.getNumberTaxas(); i++){
				Taxa taxa = getProject().getTaxa(i);
				if (taxa.getFile() == file) {
					for (int it = 0; it<taxa.getNumTaxa(); it++){
						Object obj = taxa.getAssociatedObject(linkNameRef, it);
						if (obj!=null && obj instanceof String){
							s += "\tHYPERLINK TAXA = " + StringUtil.tokenize(taxa.getName()) + " TAXON = " + it + " URL = " + StringUtil.tokenize((String)obj) + ";" + StringUtil.lineEnding();
							found = true;
						}
					}
					Clades clades = taxa.getClades();
					for (int ic = 0; ic<clades.getNumClades(); ic++) {
						Clade clade = clades.getClade(ic);
						if (clade.getLink()!=null) {
							s += "HYPERLINK TAXA = " + StringUtil.tokenize(taxa.getName()) + " CLADE = " + StringUtil.tokenize(clade.getName()) + " URL = " + StringUtil.tokenize(clade.getLink()) + ";" + StringUtil.lineEnding();
							found = true;
						}
					}
				}   
			}
			for (int i=0; i<project.getNumberCharMatrices(); i++){
				CharacterData data = getProject().getCharacterMatrix(i);
				if (data.getFile() == file  && data.getWritable()){
					Object2DArray hyperLinks = data.getWhichCellObjects(NameReference.getNameReference("hyperlink"));
					if (hyperLinks !=null){
						for (int it = 0; it<data.getNumTaxa(); it++){
							for (int ic = 0; ic<data.getNumChars(); ic++){
								Object obj = hyperLinks.getValue(ic, it);
								if (obj!=null && obj instanceof NexusWritable){
									s += "\tCELLHYPERLINK TAXA = " + StringUtil.tokenize(data.getTaxa().getName()) + " CHARACTERS = " + StringUtil.tokenize(data.getName()) + " TAXON = " + it  + " CHARACTER = " + ic + " URL = " + StringUtil.tokenize(((NexusWritable)obj).getNexusString()) + ";" + StringUtil.lineEnding();
									found = true;
								}
							}
						}
					}
				}
			}
			if (found)
				return s;
			else
				return null;
		}
		return null;
	}
	NameReference linkNameRef = NameReference.getNameReference("hyperlink");
	/*.................................................................................................................*/
	public boolean readNexusCommand(MesquiteFile file, NexusBlock nBlock, String blockName, String command, MesquiteString comment){ 
		if (blockName.equalsIgnoreCase("NOTES")) {
			boolean fuse = parser.hasFileReadingArgument(file.fileReadingArguments, "fuseTaxaCharBlocks");
			if (fuse)
				return true;
			MesquiteProject project = file.getProject();
			MesquiteInteger startCharT = new MesquiteInteger(0);
			String commandName = ParseUtil.getToken(command, startCharT);
			int code = 0;
			if (commandName.equalsIgnoreCase("HYPERLINK"))
				code = 1;
			else if (commandName.equalsIgnoreCase("CELLHYPERLINK"))
				code = 2;
			if (code>0) {
				String token = ParseUtil.getToken(command, startCharT); 
				String dummy;
				String pathName = "";
				int taxonNumber=-1;
				int charNumber=-1;
				CharacterData data = null;
				if (getProject().getNumberCharMatrices()>0)
					data = getProject().getCharacterMatrix(0);
				Clade clade = null;
				Taxa	taxa = getProject().getTaxa(0);
				while (!StringUtil.blank(token) && !token.equals(";")) {
					if (token.equalsIgnoreCase("TAXON")) {
						dummy =ParseUtil.getToken(command, startCharT); // =
						String whichItem = (ParseUtil.getToken(command, startCharT)); // name of taxon/etc
						taxonNumber = MesquiteInteger.fromString(whichItem);
					}
					else if (token.equalsIgnoreCase("CHARACTER")) {
						dummy =ParseUtil.getToken(command, startCharT); // =
						String whichItem = (ParseUtil.getToken(command, startCharT)); // name of taxon/etc
						charNumber = MesquiteInteger.fromString(whichItem);
					}
					else if (token.equalsIgnoreCase("CLADE")) {
						dummy =ParseUtil.getToken(command, startCharT); // =
						if (taxa!=null){
							Clades clades = taxa.getClades();
							String whichItem = (ParseUtil.getToken(command, startCharT)); // name of taxon/etc
							if ((clade = clades.findClade(whichItem))==null)
								clade = clades.addClade(whichItem);
						}
					}
					else if (token.equalsIgnoreCase("TAXA")) {
						dummy =ParseUtil.getToken(command, startCharT); // =
						String taxaTitle = (ParseUtil.getToken(command, startCharT));
						//logln("   for taxa " + taxaTitle);
						taxa = getProject().getTaxaLastFirst(taxaTitle);
						if (taxa == null) {
							taxa = getProject().getTaxa(0);
						}
					}
					else if (token.equalsIgnoreCase("CHARACTERS")) {
						dummy =ParseUtil.getToken(command, startCharT); // =
						String matrixName = (ParseUtil.getToken(command, startCharT));
						//logln("   for taxa " + taxaTitle);
						data = getProject().getCharacterMatrixByReference(file, matrixName);
						if (data == null) {
							data = getProject().getCharacterMatrix(0);
						}
					}
					else if (token.equalsIgnoreCase("URL")) { //todo: this should be done by TaxonLink
						dummy =ParseUtil.getToken(command, startCharT); // =
						pathName = ParseUtil.getToken(command, startCharT);
						if (taxonNumber>-1) {
							if (taxa!=null){
								if (code == 2){
									if (data !=null && charNumber>-1){
										Object2DArray objs = data.getOrMakeCellObjects(NameReference.getNameReference("hyperlink"));
										objs.setValue(charNumber, taxonNumber, new Hyperlink(pathName));
										data.setCellObjectDisplay(charNumber, taxonNumber);
									}
								}
								else {
									if (taxa.getWhichAssociatedObject(linkNameRef)==null)
										taxa.makeAssociatedObjects("hyperlink");
									taxa.setAssociatedObject(linkNameRef, taxonNumber, pathName);
								}
							}
						}
						else if (clade != null){
							clade.setLink(pathName);
						}

						
					}
					token = ParseUtil.getToken(command, startCharT); 
				}
				return true;
						
			}
		}
		return false;
	}
	/*.................................................................................................................*/
	public NexusCommandTest getNexusCommandTest(){ 
		return new HyperlinkNexusCommandTest();
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Manage hyperlinks";
   	 }
   	 
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages (including NEXUS read/write) hyperlinks." ;
   	 }
	/*.................................................................................................................*/
   	 
}

class HyperlinkNexusCommandTest  extends NexusCommandTest{
	public boolean readsWritesCommand(String blockName, String commandName, String command){  //returns whether or not can deal with command
		return (blockName.equalsIgnoreCase("NOTES") && (commandName.equalsIgnoreCase("HYPERLINK") || commandName.equalsIgnoreCase("CELLHYPERLINK")));
	}
}


