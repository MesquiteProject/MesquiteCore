/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.ornamental.ManageAttachedNotes;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.characters.*;
import mesquite.categ.lib.*;

/* ======================================================================== 
 *new in 1.02*  */
public class ManageAttachedNotes extends FileInit /*implements ElementManager*/ {
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

	private void writeToFile(MesquiteFile file,  MesquiteString pending, StringBuffer context, String s){
		if (pending != null && !pending.isBlank()){
			file.writeLine(pending.toString());
			pending.setValue("");
		}
		if (context != null && context.length()>0){
			file.writeLine(context.toString());
			context.setLength(0);
		}
		file.writeLine(s);
	}
	/*.................................................................................................................*/
	public boolean writeNexusCommands(MesquiteFile file, String blockName, MesquiteString pending){ 
		boolean found = false;
		if (blockName.equalsIgnoreCase("NOTES")) {
			StringBuffer s = new StringBuffer(100);
			StringBuffer tokSB = new StringBuffer(100);
			String eL =";" + StringUtil.lineEnding();
			NameReference ref = NameReference.getNameReference("notes");
			MesquiteProject project = file.getProject();
			StringBuffer context = new StringBuffer();
			for (int i=0; i<project.getNumberTaxas(); i++){
				Taxa taxa = getProject().getTaxa(i);
				if (taxa.getFile() == file) {
					if (project.getNumberTaxas()>1) //note shift in 1. 06 to "current matrix and taxa" to avoid having to repeat in each note
						context.append("\tTAXA = " +  StringUtil.tokenize(taxa.getName(), null, tokSB) + eL);
					ObjectArray taxNotes = taxa.getWhichAssociatedObject(ref);
					for (int it = 0; it<taxa.getNumTaxa(); it++){
						if (taxNotes != null){
							Object obj = taxNotes.getValue(it);
							if (obj!=null && obj instanceof AttachedNotesVector){
								AttachedNotesVector notes = (AttachedNotesVector)obj;
								for (int iim=0; iim<notes.getNumNotes(); iim++){
									AttachedNote im = notes.getAttachedNote(iim);
									if (im.anythingNEXUStoWrite()){
										s.append('\t');
										s.append(AttachedNote.ANNOTATION);
										s.append(' ');
										s.append(AttachedNote.TAXON);
										s.append(" = ");
										s.append(Integer.toString(it+1));
										s.append(' ');
										im.getNexusString(s);
										s.append(eL);
										writeToFile(file, pending, context, s.toString());
										s.setLength(0);
										found = true;
									}

								}
							}
						}
					}
				}
				context.setLength(0);
			}
			for (int i=0; i<project.getNumberCharMatrices(); i++){
				CharacterData data = getProject().getCharacterMatrix(i);
				if (data.getFile() == file && data.getWritable()){
					if (project.getNumberCharMatrices()>1) {
						if (project.getNumberTaxas()>1) //note shift in 1. 06 to "current matrix and taxa" to avoid having to repeat in each note
							context.append("\tCHARACTERS = " +  StringUtil.tokenize(data.getName(), null, tokSB) +" TAXA = " +  StringUtil.tokenize(data.getTaxa().getName(), null, tokSB) + eL);
						else  //note shift in 1. 06 to "current matrix and taxa" to avoid having to repeat in each note
							context.append("\tCHARACTERS = " +  StringUtil.tokenize(data.getName(), null, tokSB) + eL);
					}
					ObjectArray charImages = data.getWhichAssociatedObject(ref);
					if (charImages !=null){
						for (int ic = 0; ic<data.getNumChars(); ic++){
							Object obj = charImages.getValue(ic);
							if (obj!=null && obj instanceof AttachedNotesVector){
								AttachedNotesVector notes = (AttachedNotesVector)obj;
								for (int iim=0; iim<notes.getNumNotes(); iim++){
									AttachedNote im = notes.getAttachedNote(iim);
									if (im.anythingNEXUStoWrite()){
										s.append('\t');
										s.append(AttachedNote.ANNOTATION);
										s.append(' ');
										s.append(AttachedNote.CHARACTER);
										s.append(" = ");
										s.append(Integer.toString(ic+1));
										s.append(' ');
										im.getNexusString(s);
										s.append(eL);
										writeToFile(file, pending, context, s.toString());
										s.setLength(0);
										found = true;
									}
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
										if (im.anythingNEXUStoWrite()){
											s.append('\t');
											s.append(AttachedNote.ANNOTATION);
											s.append(' ');
											s.append(AttachedNote.TAXON);
											s.append(" = ");
											s.append(Integer.toString(it+1));
											s.append(' ');
											s.append(AttachedNote.CHARACTER);
											s.append(" = ");
											s.append(Integer.toString(ic+1));
											s.append(' ');
											im.getNexusString(s);
											s.append(eL);
											writeToFile(file, pending, context, s.toString());
											s.setLength(0);
											found = true;
										}
									}
								}
							}
						}
					}
					if (data instanceof CategoricalData){
						CategoricalData cData = (CategoricalData)data;
						for (int ic = 0; ic<cData.getNumChars(); ic++){
							for (int st = 0; st<=CategoricalState.maxCategoricalState; st++){
								AttachedNotesVector notes = cData.getStateAnnotationsVector(ic, st);
								if (notes != null)
									for (int iim=0; iim<notes.getNumNotes(); iim++){
										AttachedNote im = notes.getAttachedNote(iim);
										if (im.anythingNEXUStoWrite()){
											s.append('\t');
											s.append(AttachedNote.ANNOTATION);
											s.append(' ');
											s.append(AttachedNote.STATE);
											s.append(" = ");
											s.append(Integer.toString(st));
											s.append(' ');
											s.append(AttachedNote.CHARACTER);
											s.append(" = ");
											s.append(Integer.toString(ic+1));
											s.append(' ');
											im.getNexusString(s);
											s.append(eL);
											writeToFile(file, pending, context, s.toString());
											s.setLength(0);
											found = true;
										}
									}

							}
						}
					}

				}
			}
		}
		return found;
	}
	private AttachedNote getNote(int ic, int it, int st, CharacterData data, Taxa taxa, AttachedNote n){
		if (MesquiteInteger.isCombinable(it) || MesquiteInteger.isCombinable(ic)) {
			//figure out if this is for character, cell, or taxon
			if (MesquiteInteger.isCombinable(ic) && ic>=0){ // character or cell or state
				if (MesquiteInteger.isCombinable(st) && st>=0) { //state
					if (data == null)
						return null; 
					CategoricalData cData = (CategoricalData)data;
					AttachedNotesVector aim = cData.getStateAnnotationsVector(ic, st);
					if (aim == null) {
						aim = new AttachedNotesVector(data);
						try {
						cData.setStateAnnotationsVector(ic, st, aim);
						}
						catch (ArrayIndexOutOfBoundsException e){
							MesquiteMessage.println("ERROR: attempt to set state annotation for character " + (ic+1) + " of " + data.getNumChars() + "; state " + st);
						}
					}
					AttachedNote hL = new AttachedNote();
					hL.setAuthor((Author)null);
					aim.addNote(hL, true);
					return hL;
				}
				else  if (MesquiteInteger.isCombinable(it) && it>=0) { //cell
					if (data == null)
						return null; 
					NameReference notesNameRef = NameReference.getNameReference("notes");

					AttachedNotesVector aim = (AttachedNotesVector)data.getCellObject(notesNameRef, ic, it);
					if (aim == null)
						aim = new AttachedNotesVector(data);
					AttachedNote hL = new AttachedNote();
					hL.setAuthor((Author)null);
					data.setCellObject(notesNameRef, ic, it, aim);
					data.setCellObjectDisplay(ic, it);
					aim.addNote(hL, true);
					return hL;
				}
				else { //whole character
					NameReference notesNameRef = data.makeAssociatedObjects("notes");

					AttachedNotesVector aim =(AttachedNotesVector)data.getAssociatedObject(notesNameRef, ic);
					if (aim == null)
						aim = new AttachedNotesVector(data);
					AttachedNote hL = new AttachedNote();
					hL.setAuthor((Author)null);
					data.setAssociatedObject(notesNameRef, ic, aim);
					aim.addNote(hL, true);
					return hL;
				}
			}
			else { //taxon
				if (taxa == null)
					return null;
				NameReference notesNameRef = taxa.makeAssociatedObjects("notes");

				AttachedNotesVector aim = (AttachedNotesVector)taxa.getAssociatedObject(notesNameRef, it);
				if (aim == null)
					aim = new AttachedNotesVector(taxa);
				AttachedNote hL = new AttachedNote();
				hL.setAuthor((Author)null);
				taxa.setAssociatedObject(notesNameRef, it, aim);
				aim.addNote(hL, true);
				return hL;
			}
		}
		return null;

	}
	/*
	writeNexusblock in annot block
	readNexusBlock don't use block.toString
	use abbreviated
	 *
	Author findAuthor(String authorCode){
		if (authorCode == null)
			return null;
		if (MesquiteModule.author != null && authorCode.equals(MesquiteModule.author.getCode()))
			return MesquiteModule.author;
		if (getProject().numAuthors()>0){
			ListableVector v = getProject().getAuthors();
			for (int i = 0; i< v.size(); i++){
				Author a = (Author)v.elementAt(i);
				if (a.getCode()!= null && authorCode.equals(a.getCode()))
					return a;
			}
		}
		return null;
	}
	 */
	long ancient = 500000000000L;
	boolean oldWarnedM = false;
	boolean oldWarned = false;
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

			if (!commandName.equalsIgnoreCase("ANNOTATION") && !commandName.equalsIgnoreCase(AttachedNote.ANNOTATION))
				return false;

			String token = ParseUtil.getToken(command, startCharT); 
			String dummy;
			AttachedNote note = null;
			long dateMod = -1;
			boolean dateCreatedSet = false;
			int taxonNumber=MesquiteInteger.unassigned;
			Taxa taxa = nBlock.getDefaultTaxa();
			int charNumber=MesquiteInteger.unassigned;
			int stateNumber = MesquiteInteger.unassigned;
			CharacterData data = nBlock.getDefaultCharacters();
			while (!StringUtil.blank(token) && !token.equals(";")) {
				if (token.equalsIgnoreCase(AttachedNote.TAXON) || token.equalsIgnoreCase("TAXON")) {
					dummy =ParseUtil.getToken(command, startCharT); // =
					String whichItem = (ParseUtil.getToken(command, startCharT)); // name of taxon/etc
					taxonNumber = MesquiteInteger.fromString(whichItem);
					if (MesquiteInteger.isCombinable(taxonNumber))
						taxonNumber--; //to convert to internal
				}
				else if (token.equalsIgnoreCase(AttachedNote.TAXA) || token.equalsIgnoreCase("TAXA")) {
					dummy =ParseUtil.getToken(command, startCharT); // =
					String taxaTitle = (ParseUtil.getToken(command, startCharT));
					taxa = getProject().getTaxaLastFirst(taxaTitle);
					if (taxa == null) {
						taxa = getProject().getTaxa(0);
					}
				}
				else if (token.equalsIgnoreCase(AttachedNote.CHARACTER) || token.equalsIgnoreCase("CHARACTER")) {
					dummy =ParseUtil.getToken(command, startCharT); // =
					String whichItem = (ParseUtil.getToken(command, startCharT)); // name of taxon/etc
					charNumber = MesquiteInteger.fromString(whichItem);
					if (MesquiteInteger.isCombinable(charNumber))
						charNumber--; //to convert to internal
				}
				else if (token.equalsIgnoreCase(AttachedNote.STATE)) {
					dummy =ParseUtil.getToken(command, startCharT); // =
					String whichItem = (ParseUtil.getToken(command, startCharT)); // name of taxon/etc
					stateNumber = MesquiteInteger.fromString(whichItem);
				}
				else if (token.equalsIgnoreCase(AttachedNote.CHARACTERS) || token.equalsIgnoreCase("CHARACTERS")) {
					dummy =ParseUtil.getToken(command, startCharT); // =
					String matrixName = (ParseUtil.getToken(command, startCharT));
					//logln("   for taxa " + taxaTitle);
					data = getProject().getCharacterMatrixByReference(file, matrixName);
					if (data == null) {
						data = getProject().getCharacterMatrix(0);
					}
				}
				else if (token.equalsIgnoreCase(AttachedNote.COMMENT) || token.equalsIgnoreCase("COMMENT")) {
					dummy =ParseUtil.getToken(command, startCharT); // =
					String commentString = (ParseUtil.getToken(command, startCharT));
					if (note == null)
						note = getNote(charNumber, taxonNumber, stateNumber, data, taxa, note);
					if (note != null)
						note.setComment(commentString, false);
					else
						return true; //returns true without saving object so that the note is deleted from the file
				}
				else if (token.equalsIgnoreCase(AttachedNote.REFERENCE) || token.equalsIgnoreCase("REFERENCE")) {
					dummy =ParseUtil.getToken(command, startCharT); // =
					String ref = (ParseUtil.getToken(command, startCharT));
					if (note == null)
						note = getNote(charNumber, taxonNumber, stateNumber, data, taxa, note);
					if (note != null)
						note.setReference(ref, false);
					else
						return true; //returns true without saving object so that the note is deleted from the file
				}
				else if (token.equalsIgnoreCase(AttachedNote.TEXTFIELD) || token.equalsIgnoreCase("TEXTFIELD")) {
					if (note == null)
						note = getNote(charNumber, taxonNumber, stateNumber, data, taxa, note);
					if (note == null)
						return true; //returns true without saving object so that the note is deleted from the file
					dummy =ParseUtil.getToken(command, startCharT); // =
					if (dummy == null || !(";".equals(dummy))) {
						dummy =ParseUtil.getToken(command, startCharT); // (
						if (dummy == null || !(";".equals(dummy))) {
							String fieldName = (ParseUtil.getToken(command, startCharT));
							boolean parensDone = false;
							if (fieldName == null || !(";".equals(fieldName))) {
								if ("REFERENCE".equalsIgnoreCase(fieldName) || AttachedNote.REFERENCE.equalsIgnoreCase(fieldName)) {
									String ref = (ParseUtil.getToken(command, startCharT));
									if (")".equals(ref) || ";".equals(ref))
										parensDone = true;
									else
										note.setReference(ref, false);
								}
								else if (AttachedNote.COMMENT.equalsIgnoreCase(fieldName) || "COMMENT".equalsIgnoreCase(fieldName)){
									String commentString = (ParseUtil.getToken(command, startCharT));
									if (")".equals(commentString) || ";".equals(commentString))
										parensDone = true;
									else
										note.setComment(commentString, false);
								}
								else { 
									//generic field
									String field = (ParseUtil.getToken(command, startCharT));

									if (")".equals(field) || ";".equals(field))
										parensDone = true;
									else
										note.setTextField(fieldName, field, false);
								}
							}
							else
								MesquiteMessage.warnProgrammer("Warning: textfield with name null or ; in attached note. Command:\n\t"+ command);
							if (!parensDone)
								dummy =ParseUtil.getToken(command, startCharT); // )

						}
					}
				}
				else if (token.equalsIgnoreCase(AttachedNote.AUTHOR) || token.equalsIgnoreCase("AUTHOR")) {
					dummy =ParseUtil.getToken(command, startCharT); // =
					String author = (ParseUtil.getToken(command, startCharT));
					if (note == null)
						note = getNote(charNumber, taxonNumber, stateNumber, data, taxa, note);
					if (note != null)
						note.setAuthor(author);
					else
						return true; //returns true without saving object so that the note is deleted from the file
				}
				else if (token.equalsIgnoreCase(AttachedNote.AUTHORCODE) || token.equalsIgnoreCase("AUTHORCODE")) {
					dummy =ParseUtil.getToken(command, startCharT); // =
					String authorCode = (ParseUtil.getToken(command, startCharT));
					Author author = getProject().findAuthor(authorCode, false);
					if (note == null)
						note = getNote(charNumber, taxonNumber, stateNumber, data, taxa, note);
					if (note != null) {
						if (author == null)
							note.setAuthorCode(authorCode);
						else
							note.setAuthor(author);
					}
					else
						return true; //returns true without saving object so that the note is deleted from the file
				}
				else if (token.equalsIgnoreCase(AttachedNote.DATEMOD) || token.equalsIgnoreCase("DATEMOD")) {
					dummy =ParseUtil.getToken(command, startCharT); // =
					String s = (ParseUtil.getToken(command, startCharT));
					if (note == null)
						note = getNote(charNumber, taxonNumber, stateNumber, data, taxa, note);
					if (note != null) {
						note.setDateModified(s);
						dateMod = note.getDateModifiedInMillis(); //this is done because subsequent calls will modify the note, and so this needs to be reset
						if (dateMod < ancient) {
							if (!oldWarnedM)
								file.setOpenAsUntitled("Warning: an annotation's date modified appears to be incorrect (too old): " + s + " (" + dateMod + ").  The date will be reset to be unspecified.  This warning will not be given again, but may apply to more annotations.");
							note.setDateModified(0L);
							dateMod = 0L;
							oldWarnedM = true;
						}
					}
					else {
						return true; //returns true without saving object so that the note is deleted from the file
					}
				}
				else if (token.equalsIgnoreCase("DATECREATED")) { //OLD STYLE string; not easily parsible; read as string directly
					dummy =ParseUtil.getToken(command, startCharT); // =
					String s = (ParseUtil.getToken(command, startCharT));
					if (note == null)
						note = getNote(charNumber, taxonNumber, stateNumber, data, taxa, note);
					if (note != null) {
						note.setDateCreatedString(s);
						dateCreatedSet = true;
					}
					else
						return true; //returns true without saving object so that the note is deleted from the file
				}
				else if (token.equalsIgnoreCase(AttachedNote.DATECREATED)) {
					dummy =ParseUtil.getToken(command, startCharT); // =
					String s = (ParseUtil.getToken(command, startCharT));
					if (note == null)
						note = getNote(charNumber, taxonNumber, stateNumber, data, taxa, note);
					if (note != null) {
						long dc = NexusBlock.getTimeFromNEXUS(s);
						if (dc > ancient){
							note.setDateCreated(dc);
							dateCreatedSet = true;
						}
						else {
							if (!oldWarned)
								file.setOpenAsUntitled("Warning: an annotation's date created appears to be incorrect (too old): " + s + " (" + dc + ").  The date will be reset to be unspecified.  This warning will not be given again, but may apply to more annotations.");
							oldWarned = true;
						}
					}
					else
						return true; //returns true without saving object so that the note is deleted from the file
				}
				else if (token.equalsIgnoreCase(AttachedNote.ID)) {
					dummy =ParseUtil.getToken(command, startCharT); // =
					String s = (ParseUtil.getToken(command, startCharT));
					if (note == null)
						note = getNote(charNumber, taxonNumber, stateNumber, data, taxa, note);
					if (note != null) {
						note.setID(s);
					}
					else
						return true; //returns true without saving object so that the note is deleted from the file
				}
				else if (token.equalsIgnoreCase(AttachedNote.IMAGELOC) || token.equalsIgnoreCase("IMAGELOC")) {
					if (taxa!=null){
						dummy =ParseUtil.getToken(command, startCharT); // 
						String pathName = ParseUtil.getToken(command, startCharT);
						if (pathName != null && pathName.indexOf(":\\")>=0 && MesquiteFile.fileExists(pathName)){ //to fix files written (by bug) with absolute paths in Windows
							pathName = MesquiteFile.decomposePath(getProject().getHomeDirectoryName(), pathName);
						}
						if (note == null)
							note = getNote(charNumber, taxonNumber, stateNumber, data, taxa, note);
						if (note != null){
							if (!StringUtil.blank(pathName)) 
								note.setImagePath(pathName, MesquiteFile.composePath(getProject().getHomeDirectoryName(),  pathName), false);
						}
						else
							return true; //returns true without saving object so that the note is deleted from the file
					}
				}
				else if (token.equalsIgnoreCase(AttachedNote.IMAGELABEL) || token.equalsIgnoreCase("IMAGELABEL")) {
					dummy =ParseUtil.getToken(command, startCharT); // =
					dummy =ParseUtil.getToken(command, startCharT); // (
					if (note == null)
						note = getNote(charNumber, taxonNumber, stateNumber, data, taxa, note);
					if (note == null)
						return true; //returns true without saving object so that the note is deleted from the file
					ImageLabel label = new ImageLabel();
					note.addLabel(label);
					String t = null;
					while (!endOfSubcommand(t = ParseUtil.getToken(command, startCharT))) {
						if (t.equalsIgnoreCase(AttachedNote.TEXT) || t.equalsIgnoreCase("TEXT")){
							dummy =ParseUtil.getToken(command, startCharT); // =
							label.setText(ParseUtil.getToken(command, startCharT));
						}
						else if (t.equalsIgnoreCase(AttachedNote.FONT) || t.equalsIgnoreCase("FONT")){
							dummy =ParseUtil.getToken(command, startCharT); // =
							label.setFontName( ParseUtil.getToken(command, startCharT));
						}
						else if (t.equalsIgnoreCase(AttachedNote.COLORNUMBER) || t.equalsIgnoreCase("COLORNUMBER")){
							dummy =ParseUtil.getToken(command, startCharT); // =
							label.setFontColor(MesquiteInteger.fromString(command, startCharT));
						}
						else if (t.equalsIgnoreCase(AttachedNote.COLOR) || t.equalsIgnoreCase("COLOR")){
							dummy =ParseUtil.getToken(command, startCharT); // =
							label.setFontColor(ParseUtil.getToken(command, startCharT));
						}
						else if (t.equalsIgnoreCase(AttachedNote.SIZE) || t.equalsIgnoreCase("SIZE")){
							dummy =ParseUtil.getToken(command, startCharT); // =
							label.setFontSize(MesquiteInteger.fromString(command, startCharT));
						}
						else if (t.equalsIgnoreCase("X")){
							dummy =ParseUtil.getToken(command, startCharT); // =
							label.setX(MesquiteInteger.fromString(command, startCharT));
						}
						else if (t.equalsIgnoreCase("Y")){
							dummy =ParseUtil.getToken(command, startCharT); // =
							label.setY(MesquiteInteger.fromString(command, startCharT));
						}
						else if (t.equalsIgnoreCase(AttachedNote.POINTERX) || t.equalsIgnoreCase("POINTERX")){
							dummy =ParseUtil.getToken(command, startCharT); // =
							label.setPointerX(MesquiteInteger.fromString(command, startCharT));
						}
						else if (t.equalsIgnoreCase(AttachedNote.POINTERY) || t.equalsIgnoreCase("POINTERY")){
							dummy =ParseUtil.getToken(command, startCharT); // =
							label.setPointerY(MesquiteInteger.fromString(command, startCharT));
						}
						else if (t.equalsIgnoreCase(AttachedNote.SHOWPOINTER) || t.equalsIgnoreCase("SHOWPOINTER")){
							dummy =ParseUtil.getToken(command, startCharT); // =
							String d =ParseUtil.getToken(command, startCharT); // token
							label.setShowPointer("true".equalsIgnoreCase(d));

						}
						else if (t.equalsIgnoreCase(AttachedNote.FIXEDTOIMAGE) || t.equalsIgnoreCase("FixedToImage")){
							dummy =ParseUtil.getToken(command, startCharT); // =
							String d =ParseUtil.getToken(command, startCharT); // token
							label.setFixedToImage("true".equalsIgnoreCase(d));

						}
						else if (t.equalsIgnoreCase(AttachedNote.WIDTH) || t.equalsIgnoreCase("WIDTH")){ 
							dummy =ParseUtil.getToken(command, startCharT); // =
							label.setWidth(MesquiteInteger.fromString(command, startCharT));
						}
						else { // save for forward compatibility in label.setExtraSubcommands()
							dummy =ParseUtil.getToken(command, startCharT); // =
							dummy =ParseUtil.getToken(command, startCharT); // token
							label.addExtraSubcommand(t + " = " + ParseUtil.tokenize(dummy) );
						}

					}
				}
				else {  //save for forward compatibility in note.addExtraSubcommand()
					if (note == null)
						note = getNote(charNumber, taxonNumber, stateNumber, data, taxa, note);
					if (note == null)
						return true; //returns true without saving object so that the note is deleted from the file
					String sE =ParseUtil.getToken(command, startCharT) + " "; // =
					dummy = ParseUtil.getToken(command, startCharT) + " "; // token or ?
					sE += dummy;
					if ("(".equals(dummy)){
						String t = null;
						while (!endOfSubcommand(t = ParseUtil.getToken(command, startCharT))) {
							sE += t + " ";
							sE +=ParseUtil.getToken(command, startCharT) + " "; //token
						}
						sE += t + " "; //)
					}
					note.addExtraSubcommand(sE, false);

				}
				if (note != null){
					if (dateMod>0)
						note.setDateModified(dateMod);
					if (!dateCreatedSet)
						note.setDateCreated(0);
				}

				token = ParseUtil.getToken(command, startCharT); 
			}
			return true;

		}
		return false;
	}
	boolean endOfSubcommand(String t){
		if (t == null)
			return true;
		if (")".equals(t))
			return true;
		return false;
	}
	/*.................................................................................................................*/
	public NexusCommandTest getNexusCommandTest(){ 
		return new ANCT();
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Manage Attached Notes";
	}

	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Manages (including NEXUS read/write) notes attached to taxa, characters and cells of matrices." ;
	}
	/*.................................................................................................................*/

}

class ANCT  extends NexusCommandTest{
	public boolean readsWritesCommand(String blockName, String commandName, String command){  //returns whether or not can deal with command
		return (blockName.equalsIgnoreCase("NOTES") && (commandName.equalsIgnoreCase("ANNOTATION") ||  commandName.equalsIgnoreCase(AttachedNote.ANNOTATION)));
	}
}


