/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.ornamental.ManagePictures;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.characters.*;

/* ======================================================================== */
public class ManagePictures extends FileInit /*implements ElementManager*/ {
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
	/*.................................................................................................................*
	public String getNexusCommands(MesquiteFile file, String blockName){ 
		if (blockName.equalsIgnoreCase("NOTES")) {
			String s ="";
			boolean found = false;
			NameReference ref = NameReference.getNameReference("image");
			MesquiteProject project = file.getProject();
			for (int i=0; i<project.getNumberTaxas(); i++){
				Taxa taxa = getProject().getTaxa(i);
				ObjectArray taxNotes = taxa.getWhichAssociatedObject(ref);
				for (int it = 0; it<taxa.getNumTaxa(); it++){
					String path = taxa.getTaxon(it).getIllustrationPath();
					if (path!=null){
						s += "\tPICTURE TAXA = " + StringUtil.tokenize(taxa.getName()) + " TAXON = " + (it+1) + " LOC = " + StringUtil.tokenize(MesquiteFile.decomposePath(getProject().getHomeDirectoryName(), path)) + ";" + StringUtil.lineEnding();
						found = true;
					}
					if (taxNotes != null){
						Object obj = taxNotes.getValue(it);
						if (obj!=null && obj instanceof AttachedNotesVector){
							AttachedNotesVector notes = (AttachedNotesVector)obj;
							for (int iim=0; iim<notes.getNumNotes(); iim++){
								AttachedNote im = notes.getAttachedNote(iim);
								s += "\tNOTE TAXA = " + StringUtil.tokenize(taxa.getName()) + " TAXON = " + (it+1)  + " " + im.getNexusString() + ";" + StringUtil.lineEnding();
								found = true;
							}
						}
					}
				}
			}
			for (int i=0; i<project.getNumberCharMatrices(); i++){
				CharacterData data = getProject().getCharacterMatrix(i);
				ObjectArray charImages = data.getWhichAssociatedObject(ref);
				if (charImages !=null){
					for (int ic = 0; ic<data.getNumChars(); ic++){
						Object obj = charImages.getValue(ic);
						if (obj!=null && obj instanceof AttachedNotesVector){
							AttachedNotesVector notes = (AttachedNotesVector)obj;
							for (int iim=0; iim<notes.getNumNotes(); iim++){
								AttachedNote im = notes.getAttachedNote(iim);
								s += "\tNOTE TAXA = " + StringUtil.tokenize(data.getTaxa().getName()) + " CHARACTERS = " + StringUtil.tokenize(data.getName()) + " CHARACTER = " + (ic+1) + " " + im.getNexusString() + ";" + StringUtil.lineEnding();
								found = true;
							}
						}
					}
				}
				Object2DArray cellImages = data.getWhichCellObjects(ref);
				if (cellImages !=null){
					for (int ic = 0; ic<data.getNumChars(); ic++){
						for (int it = 0; it<data.getNumTaxa(); it++){
							Object obj = cellImages.getValue(ic, it);
							if (obj!=null && obj instanceof AttachedNotesVector){
								AttachedNotesVector notes = (AttachedNotesVector)obj;
								for (int iim=0; iim<notes.getNumNotes(); iim++){
									AttachedNote im = notes.getAttachedNote(iim);
									s += "\tNOTE TAXA = " + StringUtil.tokenize(data.getTaxa().getName()) + " CHARACTERS = " + StringUtil.tokenize(data.getName()) + " TAXON = " + (it+1)  + " CHARACTER = " + (ic+1) + " " + im.getNexusString() + ";" + StringUtil.lineEnding();
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
	/*.................................................................................................................*/
	public boolean readNexusCommand(MesquiteFile file, NexusBlock nBlock, String blockName, String command, MesquiteString comment){ 
		if (blockName.equalsIgnoreCase("NOTES")) {
			boolean fuse = parser.hasFileReadingArgument(file.fileReadingArguments, "fuseTaxaCharBlocks");
			if (fuse)
				return true;
			MesquiteProject project = file.getProject();
			MesquiteInteger startCharT = new MesquiteInteger(0);
			String commandName = ParseUtil.getToken(command, startCharT);
			Taxon taxon = null;
			if (!commandName.equalsIgnoreCase("PICTURE"))
				return false;
			String token = ParseUtil.getToken(command, startCharT); 
			String dummy;
			String pathName = "";
			String commentString = null;
			int taxonNumber=MesquiteInteger.unassigned;
			Taxa taxa = getProject().getTaxa(0);
			int charNumber=MesquiteInteger.unassigned;
			CharacterData data = null;
			if (getProject().getNumberCharMatrices()>0)
				data = getProject().getCharacterMatrix(0);
			while (!StringUtil.blank(token) && !token.equals(";")) {
				if (token.equalsIgnoreCase("TAXON")) {
					dummy =ParseUtil.getToken(command, startCharT); // =
					String whichItem = (ParseUtil.getToken(command, startCharT)); // name of taxon/etc
					taxonNumber = MesquiteInteger.fromString(whichItem);
					if (MesquiteInteger.isCombinable(taxonNumber))
						taxonNumber--; //to convert to internal
				}
				else if (token.equalsIgnoreCase("TAXA")) {
					dummy =ParseUtil.getToken(command, startCharT); // =
					String taxaTitle = (ParseUtil.getToken(command, startCharT));
					taxa = getProject().getTaxaLastFirst(taxaTitle);
					if (taxa == null) {
						taxa = getProject().getTaxa(0);
					}
				}
				else if (token.equalsIgnoreCase("SOURCE")) {
					dummy =ParseUtil.getToken(command, startCharT); // =
					String source = (ParseUtil.getToken(command, startCharT));
					if (!("file".equalsIgnoreCase(source))) { //TODO:  what if it is "file"? why not deal with it?
						file.setOpenAsUntitled("A picture source (\"" + source + "\", in NOTES block) was not recognized.  Mesquite may be unable to read and use the picture.");
						if (taxon != null)
							taxon.setIllustration(null, pathName);
						return true; //returns true without saving object so that the note is deleted from the file
					}
				}
				else if (token.equalsIgnoreCase("CHARACTER")) {
					dummy =ParseUtil.getToken(command, startCharT); // =
					String whichItem = (ParseUtil.getToken(command, startCharT)); // name of taxon/etc
					charNumber = MesquiteInteger.fromString(whichItem);
					if (MesquiteInteger.isCombinable(charNumber))
						charNumber--; //to convert to internal
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
				else if (token.equalsIgnoreCase("COMMENT")) {
					dummy =ParseUtil.getToken(command, startCharT); // =
					commentString = (ParseUtil.getToken(command, startCharT));
				}
				else if (token.equalsIgnoreCase("PICTURE")) {
					if (taxa!=null){
						dummy =ParseUtil.getToken(command, startCharT); // =
						pathName = ParseUtil.getToken(command, startCharT);
						if ((MesquiteInteger.isCombinable(taxonNumber) || MesquiteInteger.isCombinable(charNumber)) && !StringUtil.blank(pathName)) {
							//figure out if this is for character, cell, or taxon
							if (MesquiteInteger.isCombinable(charNumber) && charNumber>=0){ // character or cell
								if (MesquiteInteger.isCombinable(taxonNumber) && taxonNumber>=0) { //cell
									if (data == null)
										return true; //returns true without saving object so that the note is deleted from the file
									NameReference imageNameRef = NameReference.getNameReference("notes");
						   	 		
									AttachedNotesVector aim = (AttachedNotesVector)data.getCellObject(imageNameRef, charNumber, taxonNumber);
									if (aim == null)
										aim = new AttachedNotesVector(data);
									AttachedNote hL = new AttachedNote();
						   	 		aim.addNote(hL, false);
						   	 		hL.setImagePath(pathName, MesquiteFile.composePath(getProject().getHomeDirectoryName(),  pathName), false);
						   	 		hL.setComment(commentString, false);
									data.setCellObject(imageNameRef, charNumber, taxonNumber, aim);
									data.setCellObjectDisplay(charNumber, taxonNumber);
								}
								else { //whole character
									NameReference imageNameRef = data.makeAssociatedObjects("notes");
						   	 		
									AttachedNotesVector aim =(AttachedNotesVector)data.getAssociatedObject(imageNameRef, charNumber);
									if (aim == null)
										aim = new AttachedNotesVector(data);
									AttachedNote hL = new AttachedNote();
						   	 		aim.addNote(hL, false);
						   	 		hL.setImagePath(pathName, MesquiteFile.composePath(getProject().getHomeDirectoryName(),  pathName), false);
						   	 		hL.setComment(commentString, false);
									data.setAssociatedObject(imageNameRef, charNumber, aim);
								}
							}
							else { //taxon
									NameReference imageNameRef = taxa.makeAssociatedObjects("notes");
						   	 		
									AttachedNotesVector aim = (AttachedNotesVector)taxa.getAssociatedObject(imageNameRef, taxonNumber);
									if (aim == null)
										aim = new AttachedNotesVector(taxa);
									AttachedNote hL = new AttachedNote();
						   	 		aim.addNote(hL, false);
						   	 		hL.setImagePath(pathName, MesquiteFile.composePath(getProject().getHomeDirectoryName(),  pathName), false);
						   	 		hL.setComment(commentString, false);
									taxa.setAssociatedObject(imageNameRef, taxonNumber, aim);
								
							}
						}
					}
				}
				token = ParseUtil.getToken(command, startCharT); 
			}
			return true;
		
		}
		return false;
	}
	/*.................................................................................................................*/
	public NexusCommandTest getNexusCommandTest(){ 
		return new MPCT();
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Manage pictures";
   	 }
   	 
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages (including NEXUS read/write) pictures." ;
   	 }
	/*.................................................................................................................*/
   	 
}

class MPCT  extends NexusCommandTest{
	public boolean readsWritesCommand(String blockName, String commandName, String command){  //returns whether or not can deal with command
		return (blockName.equalsIgnoreCase("NOTES") && (commandName.equalsIgnoreCase("PICTURE")));
	}
}


