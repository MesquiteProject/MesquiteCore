/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

package mesquite.basic.ManageAuthors;
/*~~  */

import java.util.*;
import java.awt.*;
import java.io.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

public class ManageAuthors extends FileInit {
	int numBlocks =0;
	MesquiteMenuItemSpec addAuthMMI;
	String noAuthorWarningNew = "The Author for this account and machine has not yet been set, but you are requesting an AUTHORS block." +
	" You should now go to the Set Author... menu item in the Defaults submenu of the File menu to set an author name." + 
	" For the code, please indicate a short code unique in your collaborative group.  If you do not set an author name, the AUTHORS block might not be written";
	String noAuthorWarning = "The Author for this account and machine has not yet been set, but this file contains an AUTHORS block." +
	" If you are going to edit and save this file, you are strongly urged to go to the Set Author... menu item in the Defaults submenu of the File menu to set an author name." + 
	" For the code, please indicate a short code unique in your collaborative group.";
	public Class getDutyClass(){
		return ManageAuthors.class;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {

		addAuthMMI = getFileCoordinator().addMenuItem(MesquiteTrunk.editMenu, "Add AUTHORS Block to File", new MesquiteCommand("addAuthorsBlock", this));
		addAuthMMI.setEnabled(false);
		MesquiteTrunk.resetMenuItemEnabling();
//		getFileCoordinator().addMenuItem(MesquiteTrunk.editMenu, "Add Last Author to Matrix Names", new MesquiteCommand("addAuthorNameToMatrices", this));
		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("addAuthorNameToMatrices");
 	 	return temp;
  	 }
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Adds name of author of scripting file to data matrix names", null, commandName, "addAuthorNameToMatrices")) {
			MesquiteFile f=  checker.getFile();
			if (f!= null &&f.getPreviousSaver() != null && f != getProject().getHomeFile()){
				int numMatrices = getProject().getNumberCharMatrices(f);
				for (int i= 0; i< numMatrices; i++) {
					CharacterData data = getProject().getCharacterMatrix(f, i);
					if (data.getLastModifiedAuthor() != null) {
						data.setName(data.getName() + " (from " + data.getLastModifiedAuthor() + ")");
						MesquiteWindow.resetAllTitles();
					}	
				}
			}
		}
		else 	if (checker.compare(this.getClass(), "Adds an authors block to the file", null, commandName, "addAuthorsBlock")) {
			MesquiteFile f=  checker.getFile();
			if (f == null)
				f = getProject().getHomeFile();
			AuthorsBlock ab = new AuthorsBlock(f, this);
			numBlocks++;
			addNEXUSBlock(ab);
			addAuthMMI.setEnabled(false);
			MesquiteTrunk.resetMenuItemEnabling();
			if (MesquiteModule.author.hasDefaultSettings() && !MesquiteThread.isScripting())
				discreetAlert( noAuthorWarningNew);
			if (MesquiteModule.author != null && !MesquiteModule.author.hasDefaultSettings()){

				if (f.getProject().numAuthors() == 0) {
					ListableVector authors = f.getProject().getAuthors();
					Author a = new Author();
					a.setName(MesquiteModule.author.getName());
					a.setCode(MesquiteModule.author.getCode());
					a.setCurrent(true);
					authors.addElement(a, false);
				}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	/** A method called immediately after the file has been read in or completely set up (if a new file).*/
	public void fileReadIn(MesquiteFile f) {
		if (f== null || f.getProject() == null)
			return;
		NexusBlock[] bs = getProject().getNexusBlocks(AuthorsBlock.class, f); 
		addAuthMMI.setEnabled((bs == null || bs.length ==0) && !Author.addAuthorBlockByDefault);
		if ((bs == null || bs.length ==0) && Author.addAuthorBlockByDefault){
			AuthorsBlock ab = new AuthorsBlock(f, this);
			numBlocks++;
			addNEXUSBlock(ab);  
		}
		MesquiteTrunk.resetMenuItemEnabling();
		if (MesquiteModule.author != null && !MesquiteModule.author.hasDefaultSettings()){

			if (f.getProject().numAuthors() == 0) {
				ListableVector authors = f.getProject().getAuthors();
				Author a = new Author();
				a.setName(MesquiteModule.author.getName());
				a.setCode(MesquiteModule.author.getCode());
				a.setCurrent(true);
				authors.addElement(a, false);
			}
		}
	}
	/*.................................................................................................................*/
	/** A method called immediately before a file is to be saved.*/
	public void fileAboutToBeWritten(MesquiteFile f) {
		NexusBlock[] bs = getProject().getNexusBlocks(AuthorsBlock.class, f); 
		if ((bs == null || bs.length ==0) && Author.addAuthorBlockByDefault){
			AuthorsBlock ab = new AuthorsBlock(f, this);
			numBlocks++;
			addNEXUSBlock(ab);  
		}
	}
	/*.................................................................................................................*/
	Author findAuthor(Author author){
		if (author == null)
			return null;
		if (getProject().numAuthors()>0){
			ListableVector v = getProject().getAuthors();
			for (int i = 0; i< v.size(); i++){
				Author a = (Author)v.elementAt(i);
				if (a.getCode()!= null && a.getCode().equals(author.getCode()) && a.getName()!= null && a.getName().equals(author.getName()))
					return a;
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	public NexusBlockTest getNexusBlockTest(){ return new AuthorsBlockTest();}
	/*.................................................................................................................*/
	public NexusBlock readNexusBlock(MesquiteFile file, String name, FileBlock block, StringBuffer blockComments, String fileReadingArguments){

		String commandString;
		NexusBlock b=new AuthorsBlock(file, this);
		ListableVector v = getProject().getAuthors();
		MesquiteString comment = new MesquiteString();
		boolean found = false;
		for (int ia = 0; ia< v.size(); ia++){
			Author au = (Author)v.elementAt(ia);
			if (au == MesquiteModule.author) {
				found = true;
			}
		}
		if (!found)
			v.addElement(MesquiteModule.author, false);
		if (MesquiteModule.author.hasDefaultSettings() && !MesquiteThread.isScripting())
			discreetAlert( noAuthorWarning);
		while (!StringUtil.blank(commandString = block.getNextFileCommand(comment))) {
			String commandName = parser.getFirstToken(commandString);

			if (commandName.equalsIgnoreCase("AUTHOR")) {
				String token = null;

				Author a = new Author();
				while (!StringUtil.blank(token = parser.getNextToken())){
					if ("NAME".equalsIgnoreCase(token)){
						parser.getNextToken(); //=
						a.setName(parser.getNextToken());

					}
					else if ("CODE".equalsIgnoreCase(token)){
						parser.getNextToken(); //=
						a.setCode(parser.getNextToken());
					}
					else if ("LASTSAVER".equalsIgnoreCase(token)){
						if (!a.hasDefaultSettings())
							file.setPreviousSaver(a);
					}
				}
				if (findAuthor(a) == null) //not found; add
					v.addElement(a, false);

			}
		}
		return b;
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Manage AUTHORS blocks";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Manages AUTHORS block in NEXUS file." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}
}



/* ======================================================================== */
class AuthorsBlockTest extends NexusBlockTest  {
	public AuthorsBlockTest () {
	}
	public  boolean readsWritesBlock(String blockName, FileBlock block){ //returns whether or not can deal with block
		return blockName != null && blockName.equalsIgnoreCase("AUTHORS");
	}
}

/* ======================================================================== */
class AuthorsBlock extends NexusBlock {

	public AuthorsBlock(MesquiteFile f, MesquiteModule mb){
		super(f, mb);
	}
	public boolean contains(FileElement e) {
		return false;  
	}

	public void written() {
	}
	public String getName(){
		return "AUTHORS block";
	}
	public boolean mustBeAfter(NexusBlock block){
		return false;
	}
	public String getBlockName(){
		return "AUTHORS";
	}
	public String getNEXUSBlock(){
		String contents = "";
		MesquiteProject proj = getFile().getProject();
		ListableVector v = proj.getAuthors();
		for (int i = 0; i<v.size(); i++){
			Author author =  (Author)v.elementAt(i);
			if (!(author.isCurrent() && "Anonymous".equalsIgnoreCase(author.getName()))) {
				contents += "AUTHOR  NAME = " + ParseUtil.tokenize(author.getName()) + " CODE = " + ParseUtil.tokenize(author.getCode());
				if (author.isCurrent())
					contents += " LASTSAVER";
				contents += ";" +  StringUtil.lineEnding();
			}
			//else	MesquiteMessage.warnProgrammer("Warning: there may be a problem with Author management, for an attempt is being made to write Anonymous to the AUTHORS block.  Please report this problem immediately."); 


		}
		String unrec = getUnrecognizedCommands();
		if (StringUtil.blank(contents) && StringUtil.blank(unrec))
			return null;
		String blocks="BEGIN AUTHORS;" + StringUtil.lineEnding();
		blocks += contents;
		if (!StringUtil.blank(unrec)) {
			blocks += StringUtil.lineEnding()+ unrec + StringUtil.lineEnding();
		}
		blocks += "END;" + StringUtil.lineEnding();
		return blocks;
	}
}

