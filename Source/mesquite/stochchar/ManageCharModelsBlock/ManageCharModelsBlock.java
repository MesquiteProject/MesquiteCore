/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.stochchar.ManageCharModelsBlock;
/*~~  */

import java.util.*;
import java.awt.*;
import java.io.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== 
Manages the block with stochastic models of evolutoin */
public class ManageCharModelsBlock extends FileInit {
	int numBlocks =0;
	public Class getDutyClass(){
		return ManageCharModelsBlock.class;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
  	 }
  	 
  	 public boolean isPrerelease(){
  	 	return false;
  	 }
	/*.................................................................................................................*/
 	/** A method called immediately after the file has been read in or completely set up (if a new file).*/
 	public void fileReadIn(MesquiteFile f) {
 		NexusBlock[] bs = getProject().getNexusBlocks(ProbCharModelsBlock.class, f);
		if (bs == null || bs.length ==0){ 
			ProbCharModelsBlock ab = new ProbCharModelsBlock(f, this);
			numBlocks++;
			addNEXUSBlock(ab);
		}
		
	}
	/*.................................................................................................................*/
	public NexusBlockTest getNexusBlockTest(){ return new ProbCharModelsBlockTest();}
	/*.................................................................................................................*/
	public NexusBlock readNexusBlock(MesquiteFile file, String name, FileBlock block, StringBuffer blockComments, String fileReadingArguments){
		int c = 0;
		String s;
		ProbCharModelsBlock ab;
 		NexusBlock[] bs = getProject().getNexusBlocks(ProbCharModelsBlock.class, file);  //only one per file
 		if (bs == null || bs.length == 0)
 			ab = new ProbCharModelsBlock(file, this);
 		else
 			ab = (ProbCharModelsBlock)bs[0];
		MesquiteString comment = new MesquiteString();
		while (!StringUtil.blank(s=block.getNextFileCommand(comment))) {
			String commandName = parser.getFirstToken(s);
			if (!commandName.equalsIgnoreCase("BEGIN") && !commandName.equalsIgnoreCase("END") && !commandName.equalsIgnoreCase("ENDBLOCK"))
				readUnrecognizedCommand(file, ab, "MESQUITECHARMODELS", block, commandName, s, blockComments, comment);
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
			String command =employee.getNexusCommands(mf, "MESQUITECHARMODELS");
			if (!StringUtil.blank(command))
				commands+="\t"+command + StringUtil.lineEnding();
			commands+=employeesGetCommands(employee, mf);
		}
		return commands;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Manage MESQUITECHARMODELS blocks";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages MESQUITECHARMODELS block in NEXUS file." ;
   	 }
}
	
	
	
class ProbCharModelsBlock extends NexusBlock {
	public ProbCharModelsBlock(MesquiteFile f, MesquiteModule mb){
		super(f, mb);
	}
 	/*.................................................................................................................*/
 	/** gets the explanation of this matrix*/
	public String getExplanation() {
		return "This character models block belongs to the file \"" + getFile().getName() + "\"";

	}
	public boolean contains(FileElement e) {
		return false;  //always false; needs to use other means to determine if need to create block
	}
	public void written() {
		MesquiteMessage.warnProgrammer("probcharmodels block clean");
	}
	public String getName(){
		return "MESQUITECHARMODELS block";
	}
	public boolean mustBeAfter(NexusBlock block){
		if (block==null)
			return false;
		return (block.getBlockName().equalsIgnoreCase("TAXA") ||  block.getBlockName().equalsIgnoreCase("SETS") ||  block.getBlockName().equalsIgnoreCase("CHARACTERS") ||  block.getBlockName().equalsIgnoreCase("SETS") ||  block.getBlockName().equalsIgnoreCase("LABELS"));
		
	}
	public String getBlockName(){
		return "MESQUITECHARMODELS";
	}
	public String getNEXUSBlockContents(){
		return ((ManageCharModelsBlock)getManager()).employeesGetCommands(getManager().getFileCoordinator(), getFile());
	}
	public String getNEXUSBlock(){
		String contents = getNEXUSBlockContents();
		contents=contents.trim();
		String unrec = getUnrecognizedCommands();
		unrec = unrec.trim();
		if (StringUtil.blank(contents) && StringUtil.blank(unrec))
			return null;
		String blocks="BEGIN " + getBlockName() + ";" + StringUtil.lineEnding()+ "\t"+contents + StringUtil.lineEnding();
		if (!StringUtil.blank(unrec)) {
			blocks += StringUtil.lineEnding()+ "\t" + unrec + StringUtil.lineEnding();
		}
		blocks += "END;" + StringUtil.lineEnding();
		return blocks;
	}
}/* ======================================================================== */
class ProbCharModelsBlockTest extends NexusBlockTest  {
	public ProbCharModelsBlockTest () {
	}
	public  boolean readsWritesBlock(String blockName, FileBlock block){ //returns whether or not can deal with block
		return blockName.equalsIgnoreCase("MESQUITECHARMODELS");
	}
}


