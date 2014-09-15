/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.ManageAssumptionsBlock;
/*~~  */

import java.util.*;
import java.awt.*;
import java.io.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/** Manages the ASSUMPTIONS block of a NEXUS file.*/
public class ManageAssumptionsBlock extends FileInit {
	int numBlocks =0;
	public Class getDutyClass(){
		return ManageAssumptionsBlock.class;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
  	 }
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
 	/** A method called immediately after the file has been read in or completely set up (if a new file).*/
 	public void fileReadIn(MesquiteFile f) {
 		NexusBlock[] bs = getProject().getNexusBlocks(AssumptionsBlock.class, f); //added Dec 01
		if (bs == null || bs.length ==0){ 
			AssumptionsBlock ab = new AssumptionsBlock(f, this);
			numBlocks++;
			addNEXUSBlock(ab);
		}
		
	}
	/*.................................................................................................................*/
	public NexusBlockTest getNexusBlockTest(){ return new AssumptionsBlockTest();}
	/*.................................................................................................................*/
	public NexusBlock readNexusBlock(MesquiteFile file, String name, FileBlock block, StringBuffer blockComments, String fileReadingArguments){
		int c = 0;
		String s;
		AssumptionsBlock ab;
 		NexusBlock[] bs = getProject().getNexusBlocks(AssumptionsBlock.class, file); //added Sept 2011
		if (bs == null || bs.length ==0)
			ab = new AssumptionsBlock(file, this);
		else
			ab = (AssumptionsBlock)bs[0];
		MesquiteString comment = new MesquiteString();
		while (!StringUtil.blank(s=block.getNextFileCommand(comment))) {
			String commandName = parser.getFirstToken(s);
			if (commandName != null && !commandName.equalsIgnoreCase("BEGIN") && !commandName.equalsIgnoreCase("END") && !commandName.equalsIgnoreCase("ENDBLOCK")) {
				if (commandName.equalsIgnoreCase("LINK")){
					ab.processLinkCTCommand( s, getProject(), parser);
				}
				else if (commandName.equalsIgnoreCase("Options")){
					ab.setOptionsCommand(s);
				}
				else 
					readUnrecognizedCommand(file, ab, "ASSUMPTIONS", block, commandName, s, blockComments, comment);
			}
		}
		numBlocks++;
		return ab; 
	}

	/*.................................................................................................................*/
	public String employeesGetCommands(MesquiteModule module, MesquiteFile mf) {
		Enumeration enumeration=module.getEmployeeVector().elements();
		MesquiteModule employee;
		String commands="";
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			employee = (MesquiteModule)obj;
			String command =employee.getNexusCommands(mf, "ASSUMPTIONS");
			if (!StringUtil.blank(command))
				commands+="\t"+command + StringUtil.lineEnding();
			commands+=employeesGetCommands(employee, mf);
		}
		return commands;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Manage ASSUMPTIONS blocks";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages ASSUMPTIONS block in NEXUS file." ;
   	 }
}
	
	
	
class AssumptionsBlock extends NexusBlock {
	String options = null;
	public AssumptionsBlock(MesquiteFile f, MesquiteModule mb){
		super(f, mb);
	}
	public boolean contains(FileElement e) {
		return false;  //always false; needs to use other means to determine if need to create block
	}
	public void written() {
		MesquiteMessage.warnProgrammer("Assumptions clean");
	}
	public void setOptionsCommand(String opt){
		options = opt;
	}
	public String getName(){
		return "ASSUMPTIONS block";
	}
	public boolean mustBeAfter(NexusBlock block){
		if (block==null)
			return false;
		return (block.getBlockName().equalsIgnoreCase("TAXA") ||  block.getBlockName().equalsIgnoreCase("CHARACTERS") ||  block.getBlockName().equalsIgnoreCase("SETS") ||  block.getBlockName().equalsIgnoreCase("LABELS"));
		
	}
	public String getBlockName(){
		return "ASSUMPTIONS";
	}
	public String getNEXUSBlock(){

		String contents = ((ManageAssumptionsBlock)getManager()).employeesGetCommands(getManager().getFileCoordinator(), getFile());
		String unrec = getUnrecognizedCommands();
		if (StringUtil.blank(contents) && StringUtil.blank(unrec))
			return null;
		String blocks="BEGIN ASSUMPTIONS;" + StringUtil.lineEnding();
		if (options!=null)
			blocks += options + StringUtil.lineEnding();
		blocks += contents;
		if (!StringUtil.blank(unrec)) {
			blocks += StringUtil.lineEnding()+ unrec + StringUtil.lineEnding();
		}
		blocks += "END;" + StringUtil.lineEnding();
		return blocks;
	}
}/* ======================================================================== */
class AssumptionsBlockTest extends NexusBlockTest  {
	public AssumptionsBlockTest () {
	}
	public  boolean readsWritesBlock(String blockName, FileBlock block){ //returns whether or not can deal with block
		return blockName.equalsIgnoreCase("ASSUMPTIONS");
	}
}


