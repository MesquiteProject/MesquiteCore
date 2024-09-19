/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.basic.ManageLabelsBlock;
/*~~  */

import java.util.*;
import java.awt.*;
import java.io.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/** Manages the labels block in a nexus file*/
public class ManageLabelsBlock extends FileInit {
	int numBlocks = 0;
	public Class getDutyClass(){
		return ManageLabelsBlock.class;
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
 		NexusBlock[] bs = getProject().getNexusBlocks(LabelsBlock.class, f); //added Dec 01
		if (bs == null || bs.length ==0){ 
			LabelsBlock ab = new LabelsBlock(f, this);
			numBlocks++;
			addNEXUSBlock(ab);
		}
		
	}
	/*.................................................................................................................*/
	public NexusBlockTest getNexusBlockTest(){ return new LabelsBlockTest();}
	/*.................................................................................................................*/
	public NexusBlock readNexusBlock(MesquiteFile file, String name, FileBlock block, StringBuffer blockComments, String fileReadingArguments){
		Parser commandParser = new Parser();
		commandParser.setString(block.toString());
		MesquiteLong startCharC = new MesquiteLong(0);
		String s;
 		NexusBlock[] bs = getProject().getNexusBlocks(LabelsBlock.class, file); //added Sept 2011
 		LabelsBlock sB ;
		if (bs == null || bs.length ==0)
		 sB= new LabelsBlock(file, this); //TODO: should this store the LABELS??
		else
			sB = (LabelsBlock)bs[0];
		int numChars=0;
		while (!StringUtil.blank(s=commandParser.getNextCommand(startCharC))) {
			String commandName = parser.getFirstToken(s);
			if (!commandName.equalsIgnoreCase("BEGIN") && !commandName.equalsIgnoreCase("END") && !commandName.equalsIgnoreCase("ENDBLOCK"))
				if (commandName.equalsIgnoreCase("LINK")){
					sB.processLinkCTCommand( s, getProject(), parser);
				}
				else
					readUnrecognizedCommand(file, sB, "LABELS", block, commandName, s, blockComments, null);
		}
			numBlocks++;
		return sB;
	}
/*
	private boolean findReaderForCommand (MesquiteFile mf, String blockName, String commandName, String command, MesquiteString comment, LabelsBlock sB) {
		Enumeration enumeration=mf.getProject().getCoordinatorModule().getEmployeeVector().elements(); // WHY ARE ONLY EMPLOYEES OF FILE COORDINATOR USED????
		MesquiteModule employeeModule;
		MesquiteModuleInfo mbi;
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			employeeModule = (MesquiteModule)obj;
			mbi = employeeModule.getModuleInfo();
			if (mbi==null)
				MesquiteMessage.println("no employees of ownerModule!!!");
			else if (mbi.nexusCommandTest!=null) {
				if (mbi.nexusCommandTest.readsWritesCommand(blockName, commandName, command)) {
					if (employeeModule.readNexusCommand(mf, blockName, command, comment))
						return true;
				}
			}
		}
		sB.storeUnrecognizedCommand(command);
		return false;
	}
	/*.................................................................................................................*/
	public String getLabelsBlock(MesquiteFile file, LabelsBlock sB){
		String contents = employeesGetCommands(getProject().ownerModule, file);
		if (sB != null) contents += sB.getUnrecognizedCommands() + StringUtil.lineEnding();
		if (StringUtil.blank(contents))
			return null;
		String blocks="BEGIN LABELS;" + StringUtil.lineEnding()+ contents+ "END;" + StringUtil.lineEnding();
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
			String command =employee.getNexusCommands(mf, "LABELS");
			if (!StringUtil.blank(command))
				commands+=command + StringUtil.lineEnding();
			commands+=employeesGetCommands(employee, mf);
		}
		return commands;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Manage LABELS blocks";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages LABELS block in NEXUS file." ;
   	 }
}
	
/* ======================================================================== */
class LabelsBlock extends NexusBlock {
	public LabelsBlock(MesquiteFile f, ManageLabelsBlock mb){
		super(f,mb);
	}
	public void written() {
		MesquiteMessage.warnProgrammer("written labels");
	}
	public boolean mustBeAfter(NexusBlock block){
		if (block==null)
			return false;
		return (block.getBlockName().equalsIgnoreCase("TAXA") ||  block.getBlockName().equalsIgnoreCase("CHARACTERS"));
		
	}
	public String getBlockName(){
		return "LABELS";
	}
	public boolean contains(FileElement e) {
		return false;
	}
	public String getName(){
		return "LABELS block";
	}
	public String getNEXUSBlock(){
		return ((ManageLabelsBlock)getManager()).getLabelsBlock(getFile(), this);
	}
}

	
/* ======================================================================== */
class LabelsBlockTest extends NexusBlockTest  {
	public LabelsBlockTest () {
	}
	public  boolean readsWritesBlock(String blockName, FileBlock block){ //returns whether or not can deal with block
		return blockName.equalsIgnoreCase("LABELS");
	}
}


