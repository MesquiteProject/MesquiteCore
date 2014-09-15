/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.ManageCodonsBlock;
/*~~  */

import java.util.*;
import java.awt.*;
import java.io.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/** Manages the NEXUS block specifying codon position sets*/
public class ManageCodonsBlock extends FileInit {
	public Class getDutyClass(){
		return ManageCodonsBlock.class;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
  	 }
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
 	/** A method called immediately after the file has been read in or completely set up (if a new file).*/
 	public void fileReadIn(MesquiteFile f) {
 		NexusBlock[] bs = getProject().getNexusBlocks(CodonsBlock.class, f);
		if (bs == null || bs.length ==0){ 
			CodonsBlock ab = new CodonsBlock(f, this);
			addNEXUSBlock(ab);
		}
		
	}
	/*.................................................................................................................*/
	public NexusBlockTest getNexusBlockTest(){ return new CodonsBlockTest();}
	/*.................................................................................................................*/
	public NexusBlock readNexusBlock(MesquiteFile file, String name, FileBlock block, StringBuffer blockComments, String fileReadingArguments){
		Parser commandParser = new Parser();
		commandParser.setString(block.toString());
		MesquiteInteger startCharC = new MesquiteInteger(0);
		String s;
		CodonsBlock sB = new CodonsBlock(file, this); 
		int numChars=0;
		while (!StringUtil.blank(s=commandParser.getNextCommand(startCharC))) {
			String commandName = parser.getFirstToken(s);
			if (!commandName.equalsIgnoreCase("BEGIN") && !commandName.equalsIgnoreCase("END") && !commandName.equalsIgnoreCase("ENDBLOCK"))
				if (commandName.equalsIgnoreCase("LINK")){
					sB.processLinkCTCommand( s, getProject(), parser);
				}
				else
					readUnrecognizedCommand(file, sB, "CODONS", block, commandName, s, blockComments, null);
		}
		return sB;
	}
	/*.................................................................................................................*/
	public String getCodonsBlock(MesquiteFile file, CodonsBlock sB){
		String contents = employeesGetCommands(getProject().ownerModule, file);
		if (sB != null) contents += sB.getUnrecognizedCommands() + StringUtil.lineEnding();
		if (StringUtil.blank(contents))
			return null;
		String blocks="BEGIN CODONS;" + StringUtil.lineEnding()+ contents+ "END;" + StringUtil.lineEnding();
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
			String command =employee.getNexusCommands(mf, "CODONS");
			if (!StringUtil.blank(command))
				commands+=command + StringUtil.lineEnding();
			commands+=employeesGetCommands(employee, mf);
		}
		return commands;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Manage CODONS blocks";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages CODONS block in NEXUS file." ;
   	 }
}
	
/* ======================================================================== */
class CodonsBlock extends NexusBlock {
	public CodonsBlock(MesquiteFile f, ManageCodonsBlock mb){
		super(f,mb);
	}
	public void written() {
		MesquiteMessage.warnProgrammer("written codons");
	}
	public boolean mustBeAfter(NexusBlock block){
		if (block==null)
			return false;
		return (block.getBlockName().equalsIgnoreCase("TAXA") ||  block.getBlockName().equalsIgnoreCase("CHARACTERS"));
		
	}
	public String getBlockName(){
		return "CODONS";
	}
	public boolean contains(FileElement e) {
		return false;
	}
	public String getName(){
		return "CODONS block";
	}
	public String getNEXUSBlock(){
		return ((ManageCodonsBlock)getManager()).getCodonsBlock(getFile(), this);
	}
}

	
/* ======================================================================== */
class CodonsBlockTest extends NexusBlockTest  {
	public CodonsBlockTest () {
	}
	public  boolean readsWritesBlock(String blockName, FileBlock block){ //returns whether or not can deal with block
		return blockName.equalsIgnoreCase("CODONS");
	}
}


