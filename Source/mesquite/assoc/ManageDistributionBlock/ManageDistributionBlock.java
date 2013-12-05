/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.assoc.ManageDistributionBlock;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.assoc.lib.*;

/* ======================================================================== 
Manage's Distribution blocks in NEXUS files  */
public class ManageDistributionBlock extends MesquiteModule {
	private AssociationsManager assocTask;
	private TaxaManager taxaTask;
	private TreesManager treesTask;
	
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		taxaTask = (TaxaManager)findElementManager(Taxa.class);
		assocTask = (AssociationsManager)findElementManager(TaxaAssociation.class);
		treesTask = (TreesManager)findElementManager(TreeVector.class);
		return treesTask!=null  && taxaTask!=null && assocTask!=null;
	}
	
	public Class getDutyClass(){
		return ManageDistributionBlock.class;
	}
	/*.................................................................................................................*/
	public NexusBlockTest getNexusBlockTest(){ return new DistributionBlockTest();}
	/*.................................................................................................................*
			BEGIN DISTRIBUTION;
				TITLE = 'Interleukine-1';
				NTAX = 14;
				RANGE
					'Bovine IL-1a': Bovine,
					'Pig IL-1a': Pig,
					'Human IL-1a': Human,
					'Rabbit IL-1a': Rabbit,
					'Rat IL-1a': Rat,
					'Mouse IL-1a': Mouse,
					'Bovine IL-b': Bovine,
					'Sheep IL-1b': Sheep,
					'Mouse IL-1b': Mouse,
					'Human IL-1b': Human,
					'Human IL-1bm': Human,
					'Human IL-1ra': Human,
					'Mouse IL-1ra': Mouse,
					'Rat IL-1ra': Rat
					;
				TREE * T1= ((('Rabbit IL-1a',('Human IL-1a',('Bovine IL-1a','Pig IL-1a'))),('Rat IL-1a','Mouse IL-1a')),((('Bovine IL-b','Sheep IL-1b'),('Mouse IL-1b',('Human IL-1b','Human IL-1bm'))),('Human IL-1ra',('Mouse IL-1ra','Rat IL-1ra'))));
			ENDBLOCK;
	/*.................................................................................................................*/
	public NexusBlock readNexusBlock(MesquiteFile file, String name, FileBlock block, StringBuffer blockComments, String fileReadingArguments){
	//need to register Taxa block officially as NexusBlock
		TaxaAssociation association=null;
		Parser commandParser = new Parser();
		commandParser.setString(block.toString());
		MesquiteInteger startCharC = new MesquiteInteger(0);
		String title= "Association from DISTRIBUTION";
		String commandString;
		Taxa hostTaxa= getProject().getTaxa(file, 0); //get last read TAXA as base
		Taxa associateTaxa = null;
		TreeVector trees = null;
		int numTaxa=0;
		NexusBlock b = null; //this will be for association block
		logln("Reading DISTRIBUTION block " + title);
		while (!commandParser.blankByCurrentWhitespace(commandString=commandParser.getNextCommand(startCharC))) {
			String commandName = parser.getFirstToken(commandString);
			if (commandName.equalsIgnoreCase("DIMENSIONS")) { //TODO: not a very safe way to parse!!!
			}
			else if (commandName.equalsIgnoreCase("TITLE")) {
				title = parser.getTokenNumber(3);
			}
			else if (commandName.equalsIgnoreCase("NTAX")) {
				//CREATE new contained taxa block here
				numTaxa = MesquiteInteger.fromString(parser.getTokenNumber(3));
				associateTaxa = taxaTask.makeNewTaxa(title, numTaxa, false);
				associateTaxa.addToFile(file, getProject(), taxaTask);
				//make new Assoc block
				association = new TaxaAssociation();
				association.setTaxa(hostTaxa, 0);
				association.setTaxa(associateTaxa, 1);
				association.setName(title);
				association.addToFile(file, getProject(), assocTask);
				b = assocTask.elementAdded(association);
			}
			else if (commandName.equalsIgnoreCase("RANGE")) {
				if (hostTaxa!=null && associateTaxa !=null){
					//Here read taxa and their containing taxa one after another
					boolean done = false;
					String associateName = parser.getNextToken();
					int count = 0;
					while (!done){
						if (StringUtil.blank(associateName) || ";".equals(associateName))
							done = true;
						else {
							int whichAssociate =associateTaxa.whichTaxonNumber(associateName);
							if (whichAssociate<0) {
								associateTaxa.setTaxonName(count, associateName);
								whichAssociate = count++;
							}
							parser.getNextToken(); //eating :
							String hostName = parser.getNextToken();
							int whichHost = hostTaxa.whichTaxonNumber(hostName);
							if (whichHost<0) {
								String s =("Illegal taxon name in DISTRIBUTION block: " + hostName + " for taxa block " + hostTaxa.getName());
									if (file ==null)
										MesquiteMessage.warnProgrammer(s);
									else
										file.setOpenAsUntitled(s);
							}
							if (whichHost != -1 && whichAssociate != -1 && hostTaxa.getTaxon(whichHost)!=null && associateTaxa.getTaxon(whichAssociate)!=null) {
								association.setAssociation(hostTaxa.getTaxon(whichHost), associateTaxa.getTaxon(whichAssociate), true);
							}
							parser.getNextToken(); //eating ,
							associateName = parser.getNextToken();
						}
					}
				}
			}
			else if (commandName.equalsIgnoreCase("TREE")) {
				//here read tree description if contained taxa not null
				//read until ( and send to make tree
					if (associateTaxa!=null){
						if (trees == null){
							trees = new TreeVector( associateTaxa);
							trees.setTaxa(associateTaxa);
							NexusBlock t =trees.addToFile(file, getProject(), treesTask);
						}
						String treeDescription;
						String treeName;
						treeName=parser.getNextToken();
						if (treeName.equals("*"))
							treeName=parser.getNextToken();
						parser.getNextToken(); //eat up "equals"
						treeDescription=commandString.substring(parser.getPosition(), commandString.length());
						MesquiteTree thisTree =new MesquiteTree(associateTaxa);
						/*
						String commentString = comment.getValue();
						if (!StringUtil.blank(commentString)) {
							thisTree.setAnnotation(commentString.substring(1, commentString.length()));
						}
						*/
						thisTree.setTreeVector(trees);
						trees.addElement(thisTree, false);
						thisTree.readTree(treeDescription);
						//warnReticulations
						thisTree.setName(treeName);
					}
			}
			else if (!(commandName.equalsIgnoreCase("BEGIN") || commandName.equalsIgnoreCase("END")  || commandName.equalsIgnoreCase("ENDBLOCK"))) {
					readUnrecognizedCommand(file,b, name, block, commandName, commandString, blockComments, null);
			}
		}
		return b;
	}

	/*.................................................................................................................*/
    	 public String getName() {
		return "Read DISTRIBUTION blocks";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Coordinates the reading of a DISTRIBUTION block in NEXUS file." ;
   	 }
   	 
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
}
	
	
/* ======================================================================== */
class DistributionBlockTest extends NexusBlockTest  {
	public DistributionBlockTest () {
	}
	public  boolean readsWritesBlock(String blockName, FileBlock block){ //returns whether or not can deal with block
		return blockName.equalsIgnoreCase("DISTRIBUTION");
	}
}


