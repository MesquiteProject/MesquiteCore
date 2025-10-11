/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.ZoL;
/*~~  */

import java.util.Random;

import mesquite.lib.CommandChecker;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModuleInfo;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.NameReference;
import mesquite.lib.StringArray;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.MesquiteInit;
import mesquite.lib.duties.PackageIntro;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.TaxonNamer;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.TreeUtil;
import mesquite.lib.tree.TreeVector;

/* ======================================================================== */
public class ZoL extends MesquiteInit {
	MesquiteTree tree = null;
	int startNode = 0;
	int currentNode = 0;
	int endNode = 0;
	Random random = new Random();
	Taxa taxa;
	int moves = 0;
	boolean mailboxOpen = false;
	MesquiteInteger pos = new MesquiteInteger();
	boolean initiated = false;
	String synapomorphy = null;
	boolean synapomorphyFound = false;
	boolean finished = false;
	TreeVector trees = null;
	int whichTree = 0;
	int level = 1;
	
	/* IDEAS
	 * 
	 * Allow restart level (and go back to number of moves?)
	 * 
	 * If you say get/pick/take at start, say "you can't do that"
	 * If you say get/pick/take at node, say something else.
	 * 
	 * If ancestral node is named, use that name (in addition to stating the descendants)
	 * 
	 * Task: in the theme of ecology/interactions, to carry a lineage to another lineage, e.g.
		-- purple bacteria to MRCA eukaryotes to make mitochondrion; 
		-- cyanobacteria to MRCA green plants to make chloroplast;
		-- hymenoptera to flowering plant for pollination
		-- alga to fungus to make lichen
		-- protist parasite or nematode into insect
	 * 
	 * Task: to give luck/opportunity to lineage to evolve new trait:
	    -- seed
	    -- trait that allowed land plants (following which, tetrapods could evolve?)
	    -- coming on land for tetrapods
	  *
	  * Such tasks can be protrayred all as you acting as a bit of luck.
	  *
	  * Possible gameplay: 
	  * -- some regions of the tree are unavailable until teh conditions for their emergence are met, e.g.
	  * 	eukaryotes aren't available until mitochondrion arrives; green plants need chloroplast; land plants need luck;
	  *    tetrapods need land plants and luck; etc.
	  * -- above tasks could be chained, e.g. once mitochondrion arrives, allows euk to diversify, 
	  * 	and then the next task given takes you into the eukaryotes.
	  * 
	  */
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		String settingsdir = getInstallationSettingsPath();
		MesquiteFile file = new MesquiteFile(settingsdir, "tree.phy");
		if (!file.fileExists())
			return false;
		taxa = new Taxa(0);
		trees = TreeUtil.readNewickTreeFile (file, null, taxa, true, false, null, null, null);
		tree = (MesquiteTree)trees.elementAt(0);
		if (tree == null)
			return false;
		return true;
	}
	public String  getVersion(){ 
		return "1.0";
	}
	void start(){
		tree = (MesquiteTree)trees.elementAt(whichTree);
		finished = false;
		synapomorphyFound = false;
		startNode = tree.randomTerminalInClade(tree.getRoot(), random, true);
		endNode = tree.randomTerminalInClade(tree.getRoot(), random, true);
		synapomorphy = randomSynapomorphy();
		while (endNode == startNode)
			endNode = tree.randomTerminalInClade(tree.getRoot(), random, true);
		currentNode = startNode;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		println("");
		if (finished)
			commandName = "reset";
		if (checker.compare(this.getClass(), "", "", commandName, "reset")) {
			level = 1;
			MesquiteInteger pos = new MesquiteInteger();
			int seed = MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(seed))
				random.setSeed(seed);
			whichTree = 0;
			moves = 0;
			start();
			if (tree == null){
				MesquiteMessage.println("Sorry, ZoL: The Great Phylogenetic Adventure is closed due to lack of a tree.");
				return null;
			}
			if (tree.numberOfTerminalsInClade(tree.getRoot()) <2){
				MesquiteMessage.println("Sorry, the tree is too small for an adventure.");
				return null;
			}
			mailboxOpen = false;
			initiated = true;
			
			
			println("\n\n************************************************");
			println("ZoL: The Great Phylogenetic Adventure!");
			println("Level 1");
			println("");
			println("You are standing in an open field.\nIn front of you, to your surprise, is an organism.\nYou recognize that it is a " + taxonNameOfNode(startNode)+ ".");
			println("\nThere is a small mailbox here.");
			println("");
			println("----------------------------------------");
			return this;
		}
		else if (checker.compare(this.getClass(), "", "", commandName, "open")) {
			if (currentNode != startNode){
				println("\n------------------------------");
				println("There is nothing to open here.");
				println("------------------------------");
				return null;
			}

			println("\n------------------------------");
			if (mailboxOpen)
				println("The mailbox is already open. There is a leaflet inside, which says:");
			else 
				println("You have opened the mailbox. There is a leaflet inside, which says:");
			mailboxOpen = true;
			println("\n##################################\nWELCOME TO ZoL!\nv. " + getVersion() + "\n");
			println("ZoL is a game of adventure, phylogenetic knowledge, and occasional confusion. "
					+"In it you will explore one of the most amazing histories ever reconstructed by mortals. No computer should be without ZoL.");
			println("\nYour goal is to get to the taxon: " + taxonNameOfNode(endNode));
			if (StringUtil.notEmpty(synapomorphy))
				println("And to pick up the following synapomorphy along the way: " + synapomorphy);
			//println("\n");
			println("\nYou are currently at the taxon: " + taxonNameOfNode(startNode) +".\n");
			listCommands();
			println("\nGood luck!");
			println("##################################\n\n");
		}
		else if (checker.compare(this.getClass(), "", "", commandName, "where")) {
			moves++;
			printSeparatorLine("\n");			
			where();
			printSeparatorLine("");			
		}
		else if (checker.compare(this.getClass(), "", "", commandName, "commands")) {
			printSeparatorLine("");			
			listCommands();
			printSeparatorLine("");			
		}
		else if (checker.compare(this.getClass(), "", "", commandName, "ancestor") || checker.compare(this.getClass(), "", "", commandName, "anc")) {
			moves++;
			printSeparatorLine("\n");			
			if (currentNode == tree.getRoot()){
				println("Sorry, you can't go any further into the ancestry");
				where();
				return this;
			}
			currentNode = tree.motherOfNode(currentNode);
			where();
			printSeparatorLine("");			
		}
		else if (checker.compare(this.getClass(), "", "", commandName, "go") || checker.compare(this.getClass(), "", "", commandName, "g")) {
			moves++;
			printSeparatorLine("\n");			
			int[] d = tree.daughtersOfNode(currentNode);
			if (d == null){
				println("Sorry, you can use the go command only at an ancestral node");
				printSeparatorLine("");			
				return this;
			}
			int g = MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(g) && g>=1 && g<= d.length){
				currentNode = d[g-1];
				where();
				printSeparatorLine("");			
				return this;

			}
			println("Sorry, to go to a descendant, you have to enter the number corresponding to the descendant. Type where to re-list the descendants.");
			printSeparatorLine("");			

		}
		else if (checker.compare(this.getClass(), "", "", commandName, "goal")) {
			println("Your goal is to get to the taxon: " + taxonNameOfNode(endNode));
			if (StringUtil.notEmpty(synapomorphy)){
				if (!synapomorphyFound)
					println(" and to pick up the following synapomorphy along the way: " + synapomorphy);
				else
					println("You have already found the synapomorphy: " + synapomorphy);
			}
		}
		else if (checker.compare(this.getClass(), "", "", commandName, "xyzzy")) {
			println("Well, ZoL hopes that felt good, because it didn't help you to your goals.");
		}
		else if (currentNode == startNode && ("pick".equalsIgnoreCase(commandName) || "take".equalsIgnoreCase(commandName) || "get".equalsIgnoreCase(commandName))){
			println("Sorry, you can't do that.");
		
		}
		else if (!initiated)
			return  super.doCommand(commandName, arguments, checker);
		else {
			printSeparatorLine("\n");			
			println("Sorry, that is not a command that ZoL understands.\n");
			listCommands();

			printSeparatorLine("\n");			
		}
		return this;
	}

	NameReference synsRef = NameReference.getNameReference("synapomorphies");
	NameReference synRef = NameReference.getNameReference("synapomorphy");
	String randomSynapomorphy(){
		for (int i = 0; i<200; i++){
			int node = tree.randomNodeInClade(tree.getRoot(), random);
			Object syns = tree.getAssociatedObject(synsRef, node);
			if (syns == null)
				syns  = tree.getAssociatedObject(synRef, node);
			if (syns != null && syns instanceof StringArray){
				StringArray sA = (StringArray)syns;
				if (sA.getSize()>0){
					int which = random.nextInt(sA.getSize());
					String s = sA.getValue(which);
					if (!StringUtil.blank(s))
						return s;
				}
			}
		}
		return null;
	}
	boolean synapomorphyAtNode(int node){
		if (StringUtil.blank(synapomorphy))
			return false;
		Object syns = tree.getAssociatedObject(synsRef, node);
		if (syns == null)
			syns  = tree.getAssociatedObject(synRef, node);
		if (syns != null && syns instanceof StringArray){
			StringArray sA = (StringArray)syns;
			for (int i= 0; i<sA.getSize(); i++){
				String s = sA.getValue(i);
				if (!StringUtil.blank(s) && s.equals(synapomorphy))
					return true;
			}
		}
		return false;
	}
	String taxonNameOfNode(int node){
		int t = tree.taxonNumberOfNode(node);
		return taxa.getTaxonName(t);
	}
	void listCommands(){
		println("The following commands are available to you:");
		println("   ancestor (or anc) — goes to the immediate ancestral node.");
		println("   go X (or g X) — where X is a number, goes to the descendant number X.");
		println("   where — says where you are at, including a list of descendants if you're at an ancestor.");
		println("   goal — says the taxon you are trying to get to.");
		println("   reset — start again.");
		println("   commands — list commands.");
	}
	void where(){
		if (!synapomorphyFound && synapomorphyAtNode(currentNode)){
			println("*****Congratulations!***** You have found the synapomorphy: " + synapomorphy + "!");
			println("");
			synapomorphyFound = true;
		}
		int[] d = tree.daughtersOfNode(currentNode);
		if (d == null || d.length == 0) {
			println("You are at the taxon " +taxonNameOfNode(currentNode)+ ".");
			if (currentNode == endNode) {
				if (!synapomorphyFound && StringUtil.notEmpty(synapomorphy)){
					println("Congratulations! You have arrived to you destination. However, you have not yet found the synapomorphy: " + synapomorphy);
				}
				else {println("\n!!!@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@!!!");
				println("CONGRATULATIONS! You have arrived to your destination.");
				if (StringUtil.notEmpty(synapomorphy))
					println("You have also found the synapomorphy!");
				
				whichTree++;
				if (whichTree>=trees.size()){
					println("Your journey is over. There are no more levels. " + moves());
					finished = true;
					println("!!!@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@!!!");
					return;
				}
				println("!!!@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@!!!");
				level++;
				
				println("\n\nYour journey continues to the next level...");
				println("Level " + level + "\n");

				start();
				println("You blink, and suddenly find yourself standing beside a " +taxonNameOfNode(currentNode)+ ".\n");
				println("Your goal is now to get to the taxon " + taxonNameOfNode(endNode));
				if (StringUtil.notEmpty(synapomorphy)){
						println(" and to pick up the following synapomorphy along the way: " + synapomorphy);
				}
				println("");
				println(moves());
				
				//("@ reset with whichTree+1 if there are more
				
				
				
				}
			}
			else {
				println("You are currently not at the taxon you are seeking. You are at a tip of the tree and you can't go further, so you'll need to go back down through the ancestry.");
				println("Remember, your goal is to get to the taxon " + taxonNameOfNode(endNode));
				if (StringUtil.notEmpty(synapomorphy)){
					if (!synapomorphyFound)
						println(" and to pick up the following synapomorphy along the way: " + synapomorphy);
					else
						println("You have already found the synapomorphy: " + synapomorphy);
				}
				println(moves());
			}
		}
		else {
			println("You are at an ancestral node. These are its subclades:");
			for (int i = 0; i< d.length; i++){
				int randomTip = tree.randomTerminalInClade(d[i], random, true);
				println(" " + (i+1) + " — a subclade that includes the taxon " + taxonNameOfNode(randomTip));
			}
			String punc = ".";
			if (StringUtil.notEmpty(synapomorphy)&& !synapomorphyFound)
				punc = ",";
			println("\nRemember, your goal is to get to the taxon " + taxonNameOfNode(endNode) + punc);
			if (StringUtil.notEmpty(synapomorphy)){
				if (!synapomorphyFound)
					println("and to pick up the following synapomorphy along the way: " + synapomorphy +".\n");
				else
					println("You have already found the synapomorphy: " + synapomorphy);
			}
			println(moves());
		}
	}
	String moves(){
		if (moves == 1)
			return "You have made 1 move.";
		return "You have made " + moves + " moves.";
	}
	void println(String s){
		MesquiteMessage.println(s);
	}
	void printSeparatorLine(String s){
		println(s + "--------------------------------------");
	}
	/*.................................................................................................................*/
	public String getName() {
		return "ZoL";
	}
	/*.................................................................................................................*/


}

