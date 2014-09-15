/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.basic.ManageSetsBlock;
/*~~  */

import java.util.*;
import java.awt.*;
import java.io.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/** Manages the SETS block of a NEXUS file.*/
public class ManageSetsBlock extends FileInit {
	int numBlocks =0;
	public Class getDutyClass(){
		return ManageSetsBlock.class;
	}
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
 		NexusBlock[] bs = getProject().getNexusBlocks(SETSBlock.class, f); //added Dec 01
		if (bs == null || bs.length ==0){ 
			SETSBlock ab = new SETSBlock(f, this);
			numBlocks++;
			addNEXUSBlock(ab);
		}
		
	}
	/*.................................................................................................................*/
	public NexusBlockTest getNexusBlockTest(){ return new SetsBlockTest();}
	/*.................................................................................................................*/
	public NexusBlock readNexusBlock(MesquiteFile file, String name, FileBlock block, StringBuffer blockComments, String fileReadingArguments){
		int c = 0;
		String s;
		NexusBlock[] bs = getProject().getNexusBlocks(SETSBlock.class, file);//added Sept 2011
		SETSBlock sB = null;
		if (bs == null || bs.length == 0)
			sB = new SETSBlock(file, this); //TODO: should this store the sets??
		else
			sB= (SETSBlock)bs[0];
		
		numBlocks++;
		int numChars=0;
		MesquiteString comment = new MesquiteString();
		while (!StringUtil.blank(s=block.getNextFileCommand(comment))) {
			String commandName = parser.getFirstToken(s);
			if (!commandName.equalsIgnoreCase("BEGIN") && !commandName.equalsIgnoreCase("END") && !commandName.equalsIgnoreCase("ENDBLOCK")){
				if (commandName.equalsIgnoreCase("LINK")){
					sB.processLinkCTCommand( s, getProject(), parser);
				}
				else
					readUnrecognizedCommand(file, sB, "SETS", block, commandName, s, blockComments, comment);
			}
		}
		return sB;
	}
	/*.................................................................................................................*/
	public String getSetsBlock(MesquiteFile file, SETSBlock sB){
		String contents = employeesGetCommands(getProject().ownerModule, file);
		String unrec = sB.getUnrecognizedCommands();
		if (StringUtil.blank(contents) && StringUtil.blank(unrec))
			return null;
		String blocks="BEGIN SETS;" + StringUtil.lineEnding()+ contents;
		if (!StringUtil.blank(unrec))
			blocks += StringUtil.lineEnding()+ unrec + StringUtil.lineEnding();
		blocks += "END;" + StringUtil.lineEnding();
		return blocks;
	}
	/*.................................................................................................................*/
	private String employeesGetCommands(MesquiteModule module, MesquiteFile mf) {
		Enumeration enumeration=module.getEmployeeVector().elements();
		MesquiteModule employee;
		String commands="";
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			employee = (MesquiteModule)obj;
			String command =employee.getNexusCommands(mf, "SETS");
			if (!StringUtil.blank(command))
				commands+=command + StringUtil.lineEnding();
			commands+=employeesGetCommands(employee, mf);
		}
		return commands;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Manage SETS blocks";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages character sets and other sets of various kinds (including read/write SETS block in NEXUS file)." ;
   	 }
}
	
/* ======================================================================== */
class SETSBlock extends NexusBlock {
	public SETSBlock(MesquiteFile f, ManageSetsBlock mb){
		super(f,mb);
	}
	public void written() {
	}
	public boolean mustBeAfter(NexusBlock block){
		if (block==null)
			return false;
		return (block.getBlockName().equalsIgnoreCase("TAXA") ||  block.getBlockName().equalsIgnoreCase("CHARACTERS") ||  block.getBlockName().equalsIgnoreCase("LABELS"));
		
	}
	public String getBlockName(){
		return "SETS";
	}
	public boolean contains(FileElement e) {
		return false;
	}
	public String getName(){
		return "SETS block";
	}
	public String getNEXUSBlock(){
		return ((ManageSetsBlock)getManager()).getSetsBlock(getFile(), this);
	}
}

	
/* ======================================================================== */
class SetsBlockTest extends NexusBlockTest  {
	public SetsBlockTest () {
	}
	public  boolean readsWritesBlock(String blockName, FileBlock block){ //returns whether or not can deal with block
		return blockName.equalsIgnoreCase("SETS");
	}
}


