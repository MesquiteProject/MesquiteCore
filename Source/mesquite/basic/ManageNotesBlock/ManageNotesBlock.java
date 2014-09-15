/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.basic.ManageNotesBlock;
/*~~  */

import java.util.*;
import java.awt.*;
import java.io.*;

import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;

/** Manages the NOTES block of a NEXUS file */
public class ManageNotesBlock extends FileInit {
	Vector blocks;
	int numBlocks =0;
	public Class getDutyClass(){
		return ManageNotesBlock.class;
	}
	/*.................................................................................................................*/
	public NexusBlockTest getNexusBlockTest(){ return new NotesBlockTest();}
	/*.................................................................................................................*/

	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
	/** A method called immediately after the file has been read in or completely set up (if a new file).*/
	public void fileReadIn(MesquiteFile f) {
		NexusBlock[] bs = getProject().getNexusBlocks(NotesBlock.class, f); //added Dec 01
		if (bs == null || bs.length ==0){ 
			NotesBlock ab = new NotesBlock(f, this);
			numBlocks++;
			addNEXUSBlock(ab);
		}

	}

	private NotesBlock makeBlock(MesquiteFile file) {
		NexusBlock[] bs = getProject().getNexusBlocks(NotesBlock.class, file);//added Sept 2011
		NotesBlock b;
		if (bs == null || bs.length == 0){
			b = new NotesBlock(file, this);
			b.setName("Notes Block");
			numBlocks++;
			if (blocks==null)
				blocks = new Vector();
			blocks.addElement(b);
		}
		else
			b = (NotesBlock)bs[0];
		return b;
	}
	/*
Begin Notes;
	Text [Taxon=taxon-set] [Character=character-set]
			[State=state-set] [Tree=tree-set]
			Source={Inline|File|Resource} 
			Text=text or source descriptor;
	Picture [Taxon=taxon-set] [CHARACTER=character-set]
			[State=state-set] [Tree=tree-set] 
			[Format={PICT|TIFF|EPS|JPEG|GIF}] 
			[Encode={None|UUEncode|BinHex}]
			Source={Inline|File|Resource} 
			Picture=picture-or-source-descriptor;
End;
	/*.................................................................................................................*/

	public NexusBlock readNexusBlock(MesquiteFile file, String name, FileBlock block, StringBuffer blockComments, String fileReadingArguments){
		NotesBlock b = makeBlock(file);
		int c = 0;
		String s;
		boolean nbw = file.notesBugWarn;
		MesquiteString comment = new MesquiteString();
		file.notesBugVector = new Vector();
		while (!StringUtil.blank(s=block.getNextFileCommand(comment))) {
			String commandName = parser.getFirstToken(s);
			CommandRecord.tick("Reading command " + commandName);

			if (commandName != null && !commandName.equalsIgnoreCase("BEGIN") && !commandName.equalsIgnoreCase("END") && !commandName.equalsIgnoreCase("ENDBLOCK")){
				if (commandName.equalsIgnoreCase("LINK")){
					b.processLinkCTCommand( s, getProject(), parser);
				}
				else
					readUnrecognizedCommand(file, b, "NOTES", block, commandName, s, blockComments, comment);
			}
		}
		if (!nbw && file.notesBugWarn){
			alert("A NOTES block in a linked file has overridden some notes assigned previously.  "
					+"It is possible that this is due to a bug in versions 1.0-1.02 of Mesquite which wrote duplicate NOTES blocks in any linked files. " 
					+ "If the linked files were disconnected from the main data file, they may have retained a relictual NOTES block that could later override the more current version. "
					+ "Into the log will be written the characters and taxa for which notes were overridden.  Please check to see that the footnotes for these are correct. "
					+ "If the notes appear incorrect, then we suggest you close these files WITHOUT SAVING, and contact "
					+ "Wayne Maddison at wmaddisn@interchange.ubc.ca for assistance.");
			logln("");

			for (int i = 0; i < file.notesBugVector.size(); i++)
				logln("Warning: Overridden note for " + (String)file.notesBugVector.elementAt(i));
			logln("");
			file.notesBugVector.removeAllElements();
		}
		return b;
	}
	/*.................................................................................................................*
	public String employeesGetCommands(MesquiteModule module, MesquiteFile mf) {
		Enumeration enumeration=module.getEmployeeVector().elements();
		MesquiteModule employee;
		String commands="";
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			employee = (MesquiteModule)obj;
			String command =employee.getNexusCommands(mf, "NOTES");
			if (!StringUtil.blank(command))
				commands+=command + StringUtil.lineEnding();
			commands+=employeesGetCommands(employee, mf);
		}
		return commands;
	}
	/*.................................................................................................................*/
	boolean employeesWriteCommands(MesquiteModule module, MesquiteFile mf, MesquiteString pending) {
		Enumeration enumeration=module.getEmployeeVector().elements();
		MesquiteModule employee;
		boolean written = false;
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			employee = (MesquiteModule)obj;
			written = employee.writeNexusCommands(mf, "NOTES", pending) || written;
			employeesWriteCommands(employee, mf, pending);
		}
		return written;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Manage NOTES blocks";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Manages footnotes and pictures attached to taxa, characters and data points (including read/write NOTES block in NEXUS file)." ;
	}
}

class NotesBlock extends NexusBlock {
	String blockContents = "";
	public NotesBlock(MesquiteFile f, MesquiteModule mb){
		super(f, mb);
	}
	public void written() {
		MesquiteMessage.warnProgrammer("written Notes");
	}
	public boolean mustBeAfter(NexusBlock block){
		if (block==null)
			return false;
		return (block.getBlockName().equalsIgnoreCase("TAXA") ||  block.getBlockName().equalsIgnoreCase("TREES") ||block.getBlockName().equalsIgnoreCase("CHARACTERS"));

	}
	public String getBlockName(){
		return "NOTES";
	}
	public boolean contains(FileElement e) {
		return false;
	}
	public void setText(String contents) {
		blockContents = contents;
	}
	public String getText() {
		return blockContents;
	}
	public String getName(){
		return "NOTES block";
	}
	/** Writes the NEXUS block into the file  Block must override either this or getNEXUSBlock to write to file*/
	public void writeNEXUSBlock(MesquiteFile file, ProgressIndicator progIndicator){
		MesquiteTrunk.mesquiteTrunk.logln("      Writing " + getName());
		MesquiteString pending = new MesquiteString("BEGIN NOTES;" + StringUtil.lineEnding());
		boolean written = ((ManageNotesBlock)getManager()).employeesWriteCommands(getManager().getFileCoordinator(), file, pending);

		String unrec =getUnrecognizedCommands();
		if (!StringUtil.blank(unrec)){
			if (!pending.isBlank()){
				file.writeLine(pending.toString());

			}
			file.writeLine(unrec + StringUtil.lineEnding() + "END;" + StringUtil.lineEnding());

		}
		else if (written)
			file.writeLine("END;" + StringUtil.lineEnding());
	}
	/*   *
	public String getNEXUSBlock(){
		String contents = ((ManageNotesBlock)getManager()).employeesGetCommands(getManager().getFileCoordinator(), getFile());
		String unrec =getUnrecognizedCommands();
		if (StringUtil.blank(contents) && StringUtil.blank(unrec))
			return null;
		String blocks="BEGIN NOTES;" + StringUtil.lineEnding()+ contents + StringUtil.lineEnding()+ unrec + StringUtil.lineEnding() + "END;" + StringUtil.lineEnding();
		return blocks;
	}
	//remember to store unrecognixed commands*/
}
/* ======================================================================== */
class NotesBlockTest extends NexusBlockTest  {
	public NotesBlockTest () {
	}
	public  boolean readsWritesBlock(String blockName, FileBlock block){ //returns whether or not can deal with block
		return blockName.equalsIgnoreCase("NOTES");
	}
}


