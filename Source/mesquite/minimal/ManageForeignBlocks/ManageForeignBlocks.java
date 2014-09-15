/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.minimal.ManageForeignBlocks;
/*~~  */

import java.util.*;
import java.io.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/** Manages unrecognized blocks in NEXUS data files (storing for editing or later rewriting) */
public class ManageForeignBlocks extends MesquiteModule {
	public String getName() {
		return "Manage Foreign Blocks";
	}
	public String getExplanation() {
		return "Manages unrecognized blocks in a NEXUS file." ;
	}
	/*.................................................................................................................*/
	ListableVector foreignBlocks;
	ForeignBlock currentlyEdited = null;
	MesquiteMenuItemSpec editSubmenu;
	MesquiteMenuItemSpec newBlockMenu;
	EditRawNexusBlock editor;  //TODO: allow editor for each block
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		foreignBlocks = new ListableVector();
		return true;
	}
	/*.................................................................................................................*/
	/** A method called immediately after the file has been read in.*/
	public void projectEstablished() {
		MesquiteMenuItemSpec mmis = getFileCoordinator().addMenuItem(MesquiteTrunk.editMenu, "New Generic NEXUS block...", makeCommand("newBlock",  this));
		super.projectEstablished();
	}

	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	public Class getDutyClass(){
		return ManageForeignBlocks.class;
	}

	/*.................................................................................................................*/
	public NexusBlock readNexusBlock(MesquiteFile file, String name, FileBlock block, StringBuffer blockComments, String fileReadingArguments){
		if (editSubmenu == null)
			editSubmenu = getFileCoordinator().addSubmenu(MesquiteTrunk.editMenu, "Edit Foreign Block", makeCommand("editBlock", this), foreignBlocks);

		Parser commandParser = new Parser();
		String b = block.toString();

		commandParser.setString(b);
		MesquiteInteger startCharC = new MesquiteInteger(0);

		String s =commandParser.getNextCommand(startCharC);
		s =parser.getFirstToken(commandParser.getNextCommand(startCharC));

		ForeignBlock commentBlock =new ForeignBlock(file, this);
		commentBlock.setName(name);
		commentBlock.setBlockName(name);

		commentBlock.setText(b + StringUtil.lineEnding() + "END;");
		foreignBlocks.addElement(commentBlock, false);
		return commentBlock; 
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		if (editor!=null) {
			Snapshot temp = new Snapshot();
			temp.addLine("editBlock " + foreignBlocks.indexOf(editor.getCurrentBlock()), editor);
			return temp;
		}
		else return null;
	}
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Calls up a window in which to edit a NEXUS block foreign to the current modules of Mesquite", "[number of foreign block]", commandName, "editBlock")){
			//find which block first
			int which = MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(which))
				return null;
			ForeignBlock toBeEdited = (ForeignBlock)foreignBlocks.elementAt(which);
			if (toBeEdited == null)
				return null;
			if (editor==null)
				editor = (EditRawNexusBlock)hireEmployee(EditRawNexusBlock.class, "To edit NEXUS block");
			if (editor!=null) {
				editor.editNexusBlock(toBeEdited, true);
				toBeEdited.setEditor(editor);
				if (!MesquiteThread.isScripting() && editor.getModuleWindow()!=null)
					editor.getModuleWindow().setVisible(true);
			}
			return editor;
		}
		else if (checker.compare(this.getClass(), "Creates a new generic NEXUS block", null, commandName, "newBlock")) {
			//which file to add to?
			MesquiteFile file = getProject().chooseFile( "Select the file to which to add the new NEXUS block");
			if (file == null)
				return null;
			String typeName = MesquiteString.queryShortString(containerOfModule(), "Block type name", "Enter name of type of NEXUS block (e.g., DISTRIBUTION):", "UNKNOWN");
			if (StringUtil.blank(typeName))
				return null;
			ForeignBlock block =new ForeignBlock(file, this);
			block.setName(typeName);
			block.setBlockName(typeName);
			block.setText("BEGIN " + typeName + ";" + StringUtil.lineEnding() + "[contents of block here]" + StringUtil.lineEnding() + "END;" + StringUtil.lineEnding());
			foreignBlocks.addElement(block, false);
			addNEXUSBlock(block);
			if (editor==null)
				editor = (EditRawNexusBlock)hireEmployee(EditRawNexusBlock.class, "To edit NEXUS block");
			if (editor!=null) {
				editor.editNexusBlock(block, true);
				block.setEditor(editor);
				if (!MesquiteThread.isScripting() && editor.getModuleWindow()!=null)
					editor.getModuleWindow().setVisible(true);
			}
			if (editSubmenu == null)
				editSubmenu = getFileCoordinator().addSubmenu(MesquiteTrunk.editMenu, "Edit Foreign Block", makeCommand("editBlock", this), foreignBlocks);
			resetAllMenuBars();
			return editor;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}
}
/* ======================================================================== */
class ForeignBlock extends NexusBlockEditableRaw implements Explainable {  
	String blockContents = "";
	String blockName = "";
	ManageForeignBlocks ownerModule = null;
	public ForeignBlock(MesquiteFile f, ManageForeignBlocks mb){

		super(f,mb);
		ownerModule = mb;
	}
	public void written() {
		MesquiteMessage.warnProgrammer("written Foreign block");
	}
	public boolean contains(FileElement e){
		return false;
	}
	private int getBlockID(NexusBlock block){
		if ("MacClade".equalsIgnoreCase(getBlockName()))
			return 3;
		if ("PAUP".equalsIgnoreCase(getBlockName()))
			return 2;
		if ("MrBayes".equalsIgnoreCase(getBlockName()))
			return 1;
		return -1;

	}
	/** Returns true if this block must occur after the given block*/
	public boolean mustBeAfter(NexusBlock block){
		int mine = getBlockID(this);
		int theirs = getBlockID(block);
		if (mine < 0 || theirs <0) {
			return getID()>block.getID();
		}
		return mine>theirs;
	}
	public void setBlockName(String bName){

		blockName = bName;
	}
	/** Returns the offical NEXUS block name (e.g. "TAXA")*/
	public String getBlockName(){
		return blockName;
	}

	public void setText(String contents) {
		blockContents = contents;
	}
	public String getText() {
		return blockContents;
	}
	public String getName(){
		if (super.getName() == null)
			return  "Unrecognized block";
		else
			return super.getName() + " block";
	}

	public String getExplanation(){
		return "This is a foreign block not understood by Mesquite";
	}
	public String getNEXUSBlock(){
		if (getEditor()!=null)
			getEditor().recordBlock(this);
		if (blockContents==null)
			return null;
		else
			return blockContents;
	}
}
/* ======================================================================== 
class FileCommentBlockTest extends NexusBlockTest  {
	public FileCommentBlockTest () {
	}
	public  boolean readsWritesBlock(String blockName, FileBlock block){ //returns whether or not can deal with block
		return blockName.equalsIgnoreCase("FileComment");
	}
}

 */
