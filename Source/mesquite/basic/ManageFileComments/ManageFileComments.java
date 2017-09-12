/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.basic.ManageFileComments;

import java.util.*;
import java.io.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class ManageFileComments extends FileInit {
	boolean turnedOn=false;
	Vector windows;
	MesquiteSubmenuSpec mss = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		windows = new Vector();
		return true;
  	 }
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return false;
   	 }
 	/** A method called immediately after the file has been read in.*/
 	public void projectEstablished() {
		mss = getFileCoordinator().addSubmenu(MesquiteTrunk.editMenu, "Edit Comment", makeCommand("editFileComment", this), getProject().getFiles());
		super.projectEstablished();
 	}
  	 
	/*.................................................................................................................*
 	/** A method called immediately after the file has been read in or completely set up (if a new file).*
 	public void fileReadIn(MesquiteFile f) {
 		NexusBlock[] blocks = getProject().getNexusBlocks(FileCommentsBlock.class, f);
		if (blocks==null || blocks.length == 0){
			FileCommentsBlock ab = new FileCommentsBlock(f, this);
			addNEXUSBlock(ab);
		}
		
	}
	/*.................................................................................................................*/
 	public void fileAboutToBeWritten(MesquiteFile f) {
 		MesquiteTextWindow w = findWindow(f);
 		if (w!=null){
 			f.setAnnotation(w.getText(), false);
 		}
		
	}
	/*................................................................................................................*/
	public NexusBlockTest getNexusBlockTest(){ return new FileCommentBlockTest();}
	/*.................................................................................................................*/
	public NexusCommandTest getNexusCommandTest(){ 
		return new FileNoteNexusCommandTest();
	}

	/*.................................................................................................................*/
	public String getNexusCommands(MesquiteFile file, String blockName){ 
		if (blockName.equalsIgnoreCase("NOTES")) {
			String annot = file.getAnnotation();
			if (StringUtil.blank(annot))
				return null;
			else
				return "\tTEXT  FILE TEXT = " + StringUtil.tokenize(file.getAnnotation()) + ";" + StringUtil.lineEnding();
		}
		return null;
	}
  	 MesquiteInteger pos = new MesquiteInteger();
	/*...................................................................................................................*/
	public boolean readNexusCommand(MesquiteFile file, NexusBlock nBlock, String blockName, String command, MesquiteString comment){ 
		if (blockName.equalsIgnoreCase("NOTES")) {
			boolean fuse = parser.hasFileReadingArgument(file.fileReadingArguments, "fuseTaxaCharBlocks");
			if (fuse)
				return true;
			MesquiteProject project = file.getProject();
			String commandName = parser.getFirstToken(command);
			if (commandName.equalsIgnoreCase("TEXT") && "FILE".equalsIgnoreCase(parser.getNextToken()) && "TEXT".equalsIgnoreCase(parser.getNextToken()) ) {
				parser.getNextToken(); //eating up "="
				file.setAnnotation(parser.getNextToken(), false);
				return true;
			}
		}
		return false;
	}
	/*.................................................................................................................*/
	public NexusBlock readNexusBlock(MesquiteFile file, String name, FileBlock block, StringBuffer blockComments, String fileReadingArguments){
		Parser commandParser = new Parser();
		commandParser.setString(block.toString());
		MesquiteInteger startCharC = new MesquiteInteger(0);
		
		String s =commandParser.getNextCommand(startCharC);
		s =parser.getFirstToken(commandParser.getNextCommand(startCharC));
		if (getModuleWindow()!=null)
			((MesquiteTextWindow)getModuleWindow()).setText(s);
		FileCommentsBlock commentBlock = new FileCommentsBlock(file, this);
		
		//TODO: if file element should attach to file
		// is this project comment or not?
		//what if file comment comes in via non-block -- need to add FileCommentsBlock
		file.setAnnotation(s, false);
		turnedOn = true;
		return commentBlock; 
	}
	MesquiteTextWindow findWindow(MesquiteFile f){
		for (int i=0; i<windows.size(); i++){
			MesquiteTextWindow w = (MesquiteTextWindow)windows.elementAt(i);
			if (w.getCurrentObject() == f)
				return w;
		}
		return null;
	}
	MesquiteFile getFile(String argument){
		int i = MesquiteInteger.fromString(argument);
		if (!MesquiteInteger.isCombinable(i))
			return null;
		if (i<0 || i>= getProject().getNumberLinkedFiles())
			return null;
		return (MesquiteFile)getProject().getFiles().elementAt(i);
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
	   	Snapshot temp = new Snapshot();
	   	boolean found = false;
   	 	for (int i = 0; i< windows.size(); i++){
   	 		MesquiteWindow w = (MesquiteWindow)windows.elementAt(i);
   	 		
	   	 	if (w!=null && w.isVisible()) {
	   	 		int whichFile = getProject().getFileNumber(((MesquiteFile)w.getCurrentObject()));
		   	 	temp.addLine("editFileComment " + whichFile);
		  	 	Snapshot fromWindow = w.getSnapshot(file);
	  	 	
				temp.addLine("tell It");
				temp.incorporate(fromWindow, true);
				temp.addLine("showWindow");
				temp.addLine("endTell");
		 	 	found = true;
	 	 	}
 	 	}
 	 	if (found)
 	 		return temp;
 	 	else
 	 		return null;
  	 }
  	 
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the file comment", "[file comment string]", commandName, "setFileComment")){
			String comment =parser.getFirstToken(arguments);
			MesquiteFile f = getFile(parser.getNextToken());
			if (f!=null)
				f.setAnnotation(comment, false);
		}
		else if (checker.compare(this.getClass(), "Brings up window in which to display and edit file comments", null, commandName, "editFileComment")){
			MesquiteFile f = getFile(arguments);
			if (f==null)
				return null;
			MesquiteTextWindow window = findWindow(f);
			if (window == null) {
				window = new MesquiteTextWindow(this, "Comment on file \"" + f.getName() + "\"", true); //infobar
				window.setCurrentObject(f);
				setModuleWindow(window);
				window.setEditable(true);
				window.setWindowSize(300,300);
				windows.addElement(window);
			}
			if (window != null){
				window.setText(f.getAnnotation());
				resetAllMenuBars();
			}
			if (!MesquiteThread.isScripting())
				window.setVisible(true);
			return window;
			
		}
		else if (checker.compare(this.getClass(), "Makes window in which to display and edit file comments", null, commandName, "makeWindow")){
			MesquiteFile f = getProject().getHomeFile();
			if (f==null)
				return null;
			MesquiteTextWindow window = findWindow(f);
			if (window == null) {
				window = new MesquiteTextWindow(this, "Comment on file \"" + f.getName() + "\"", false); //infobar
				window.setCurrentObject(f);
				window.setWindowSize(300,300);
				windows.addElement(window);
				window.setText(f.getAnnotation());
				window.setEditable(true);
			}
			return window;
			
		}
		else if (checker.compare(this.getClass(), "Brings up window in which to display and edit file comments", null, commandName, "showWindow")){
			MesquiteFile f = getProject().getHomeFile();
			if (f==null)
				return null;
			MesquiteTextWindow window = findWindow(f);
			if (window != null) {
				window.setText(f.getAnnotation());
				window.setVisible(true);
				resetAllMenuBars();
			}
			return window;
			
		}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
  	/*public String getComments() {
  		if (getModuleWindow()!=null)
  			return ((MesquiteTextWindow)getModuleWindow()).getText();
  		else if (commentBlock!=null)
  			return commentBlock.getText();
  		else
  			return null;
  	}
  	*/
  	 public void windowGoAway(MesquiteWindow whichWindow) {
  		 if (whichWindow == null)
  			 return;
		Object obj = whichWindow.getCurrentObject();
		if (obj instanceof MesquiteFile){
			MesquiteFile f = (MesquiteFile)obj;
			if (f!=null)
	  	 		f.setAnnotation(((MesquiteTextWindow)whichWindow).getText(), false);
  	 	}
  	 	whichWindow.hide();
  	 	//don't dispose since may be recalled.
  	 	
  	 }
  	 public void endJob(){
			for (int i=0; i<windows.size(); i++){
				windowGoAway((MesquiteTextWindow)windows.elementAt(i));
				((MesquiteTextWindow)windows.elementAt(i)).dispose();
			}
		    getFileCoordinator().deleteMenuItem(mss);
		    		 
		super.endJob();
	
  	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "File Comment";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages the main file comment in a NEXUS file." ;
   	 }
}
/* ======================================================================== */
class FileCommentsBlock extends NexusBlock {  
	public FileCommentsBlock(MesquiteFile f, ManageFileComments mb){
		super(f,mb);
	}
	public void written() {
	}
	public boolean contains(FileElement e) {
		return false;
	}
	public boolean mustBeAfter(NexusBlock block){ //� 13 Dec 01
		return false;
	}
	public String getBlockName(){
		return "FILECOMMENT";
	}
	public String getName(){
		return "FileComments block";
	}
	public String getNEXUSBlock(){
		return null;  //this block is read but not written!!!!!  It was used temporarily.
	}
}
/* ======================================================================== */
/** An object of this kind can be returned by getNexusCommandTest that will be stored in the modulesinfo vector and used
to search for modules that can read a particular command in a particular block.  (Much as the NexusBlockObject.)*/
class FileNoteNexusCommandTest extends NexusCommandTest  {
	MesquiteInteger pos = new MesquiteInteger();
	/**returns whether or not the module can deal with command*/
	public boolean readsWritesCommand(String blockName, String commandName, String command){
		boolean b = (blockName.equalsIgnoreCase("NOTES")  && commandName.equalsIgnoreCase("TEXT"));
		if (b){
			pos.setValue(0);
			String firstToken = ParseUtil.getTokenNumber(command,  pos, 2);
			if (!("FILE".equalsIgnoreCase(firstToken)))
				return false;
			return true;
		}
		return false;
	} 
}
/* ======================================================================== */
class FileCommentBlockTest extends NexusBlockTest  {
	public FileCommentBlockTest () {
	}
	public  boolean readsWritesBlock(String blockName, FileBlock block){ //returns whether or not can deal with block
		return blockName.equalsIgnoreCase("FileComment");
	}
}


