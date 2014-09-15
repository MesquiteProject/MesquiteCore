/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.coalesce.CoalescenceWMigration;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
//import mesquite.coalesce.lib.*;
import mesquite.assoc.lib.*;
/** ======================================================================== */
public class CoalescenceWMigration extends TreeSimulate {
	AssociationSource associationTask;
	int oldPopulationSize = 0;
	TaxaAssociation association;
	OneTreeSource oneTreeSourceTask;
	Tree speciesTree;
	RandomBetween rNG;
	int basePopulationSize  = 10000;
	double migrationRate = 0.0000;
	long initialSeed = System.currentTimeMillis(); //better rules for initial seed!!!!
	int migrationFestival = -1;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		//TODO: allow choices
		associationTask = (AssociationSource)hireEmployee(AssociationSource.class, "Source of taxon association");
		if (associationTask == null)
			return sorry(getName() + " couldn't start because no source of taxon associations obtained.");
		if (oneTreeSourceTask == null) {
			oneTreeSourceTask = (OneTreeSource)hireEmployee(OneTreeSource.class, "Source of containing tree");
		}
		rNG= new RandomBetween(initialSeed);
		addMenuItem("Coalescence Parameters...", makeCommand("showParamsDialog", this));
		if (!MesquiteThread.isScripting()){
			showDialog();
		}
		return true;
	}
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
		
	
	void showDialog(){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog checkBoxDialog = new ExtensibleDialog(containerOfModule(), "Coalescence With Migration",buttonPressed);
		checkBoxDialog.addLargeOrSmallTextLabel ("Parameters of coalescence simulation within species tree but with migration");
		IntegerField ur = checkBoxDialog.addIntegerField("Effective population size", basePopulationSize, 15);
		DoubleField mr = checkBoxDialog.addDoubleField("Migration probability per individual per generation", migrationRate, 15);
		checkBoxDialog.addHorizontalLine(2);
		checkBoxDialog.addLargeOrSmallTextLabel("If all migration is concentrated into a single burst a certain number of generations before the present, enter the point in time (number of generations before present) at which the burst occurred (enter -1 if not a burst but rather ongoing)");
		IntegerField mF = checkBoxDialog.addIntegerField("Generation of burst", migrationFestival, 15);
		//checkBoxDialog.addIntegerField("", 0, 15);

		checkBoxDialog.completeAndShowDialog(true);

		if (buttonPressed.getValue()==0) {
			int bp = ur.getValue();
			if (MesquiteInteger.isCombinable(bp))
				basePopulationSize = bp;
			double mrt = mr.getValue();
			if (MesquiteDouble.isCombinable(mrt))
				migrationRate = mrt;
			int mFS = mF.getValue();
			if (MesquiteInteger.isCombinable(bp))
				migrationFestival = mFS;
		}
		checkBoxDialog.dispose();

	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("suppress "); 
		temp.addLine("getAssociationTask ", associationTask); 
		temp.addLine("getTreeSource ", oneTreeSourceTask); 
		temp.addLine("setEffective " + basePopulationSize);
		temp.addLine("setMigrationRate " + migrationRate);
		if (migrationFestival>=0)
			temp.addLine("setMigrationMoment " + migrationFestival);
		temp.addLine("forgetAssociation "); 
		temp.addLine("desuppress "); 
	
		return temp;
	}
	/*---*/
boolean suppress = false;
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the effective population size for coalescence simulations", "[effective population size]", commandName, "setEffective")) {
			int eff = MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(eff))
				eff = MesquiteInteger.queryInteger(containerOfModule(), "Population Size", "Set Effective population size", basePopulationSize);
			if (!MesquiteInteger.isCombinable(eff))
				return null;
			if (basePopulationSize != eff) {
				basePopulationSize = eff;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			}
		}
		else if (checker.compare(this.getClass(), "Sets the moment at which migration occurs (-1 if not a single moment)", "[generations before present]", commandName, "setMigrationMoment")) {
			int eff = MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(eff))
				eff = MesquiteInteger.queryInteger(containerOfModule(), "Migration Moment", "Number of generations before present when all migration is concentrated (-1 if not concentrated)", migrationFestival);
			if (!MesquiteInteger.isCombinable(eff))
				return null;
			if (migrationFestival != eff) {
				migrationFestival = eff;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			}
		}
		else if (checker.compare(this.getClass(), "Sets the migration rate for coalescence simulations", "[probability of migration per individual per generation]", commandName, "setMigrationRate")) {
			double mr = MesquiteDouble.fromString(arguments, new MesquiteInteger(0));
			if (!MesquiteDouble.isCombinable(mr))
				mr = MesquiteDouble.queryDouble(containerOfModule(), "Migration Rate", "Set migration probability per individual per generation", migrationRate);
			if (!MesquiteDouble.isCombinable(mr))
				return null;
			if (migrationRate != mr) {
				migrationRate = mr;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			}
		}
		else if  (checker.compare(this.getClass(), "Sets coalescence parameters by GUI", null, commandName, "showParamsDialog")) {
			showDialog();
			parametersChanged(); 
		}
		else if  (checker.compare(this.getClass(), "Returns the species tree being used for Contained With Migration", null, commandName, "getSpeciesTree")) {
			return speciesTree;
		}
		else if  (checker.compare(this.getClass(), "Returns the containing tree context", null, commandName, "getTreeSource")) {
			return oneTreeSourceTask;
		}
		else if  (checker.compare(this.getClass(), "Returns the module supplying associations", null, commandName, "getAssociationTask")) {
			return associationTask;
		}
		else if  (checker.compare(this.getClass(), "Sets the current association to null", null, commandName, "forgetAssociation")) {
			association = null;
		}
		else if  (checker.compare(this.getClass(), "Suppresses calculations", null, commandName, "suppress")) {
			suppress = true;
		}
		else if  (checker.compare(this.getClass(), "Desuppresses calculations", null, commandName, "desuppress")) {
			suppress = false;
			if (!MesquiteThread.isScripting())
				parametersChanged(); 
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	private boolean check(Taxa taxa){
		if (taxa!=null) {
			if (association == null || (association.getTaxa(0)!= taxa && association.getTaxa(1)!= taxa)) {
				association = associationTask.getCurrentAssociation(taxa);
				if (association==null) {
					String s = "Association null in Contained With Migration (for taxa " + taxa.getName() + ")";
					if (MesquiteThread.isScripting())
						MesquiteMessage.warnProgrammer(s);
					else
						alert(s);
					return false;
				}
			}
			if (oneTreeSourceTask == null) {
				oneTreeSourceTask = (OneTreeSource)hireEmployee(OneTreeSource.class, "Source of containing tree");
				if (oneTreeSourceTask == null) {
					String s = "Source for containing tree null in Contained With Migration (for taxa " + taxa.getName() + ")";
					if (MesquiteThread.isScripting())
						MesquiteMessage.warnProgrammer(s);
					else
						alert(s);
					return false;
				}

			}
		}
		return true;
	}
	/*.................................................................................................................*/
	NameReference widthNameReference = NameReference.getNameReference("width");
	DoubleArray widths = null;
	double maxWidth = 0;
	/*.................................................................................................................*/
	/* returns Ne multiplication factor for a branch, based on width stored */;
	double branchNEAdjustment(int node){
		if (widths !=null) {
			double w = widths.getValue(node);
			if (MesquiteDouble.isCombinable(w) && w>=0) {
				return w;
			}
		}	
		return 1.0;
	}
	public void initialize(Taxa taxa){
		if (!check(taxa))
			return;
		if (oneTreeSourceTask!=null && association!=null)
			oneTreeSourceTask.initialize(association.getOtherTaxa(taxa));

	}
	/*.................................................................................................................*/
	boolean first = true;
	/*.................................................................................................................*/
	int numPopulationsFound(int[] populationsAtHeight){
		if (populationsAtHeight == null || populationsAtHeight.length == 0)
			return 0;
		int count = 0;
		for (int i=0; i<populationsAtHeight.length; i++){
			if (populationsAtHeight[i]>=0)
				count++;
		}
		return count;
	}
	int[] containing; //length of geneTree.getNumNodeSpaces();
	/*.................................................................................................................*/
	/*  coalesces the gene tree nodes contained within the populations. */
	private void coalesceAllPopulations (MesquiteTree speciesTree, MesquiteTree geneTree, Taxa geneTaxa) {
		if (suppress  || !speciesTree.hasBranchLengths())
			return;
		int numGenes = geneTree.getTaxa().getNumTaxa();

		double height = speciesTree.tallestPathAboveNode(speciesTree.getRoot(), 1.0);

		int[] populationsAtHeight = speciesTree.getNodesAtHeight(height, 1.0, null);

		//In case there is a slight error (e.g. roundoff) in tree heights, 1 from height until populations array has at least one node in it.  
		//Since branch lengths are measured in generations, subtracting 1 is just going generation by generation until the first branch pokes above water.
		while (numPopulationsFound(populationsAtHeight)==0 && height > 0) {
			height -= 1.0;
			populationsAtHeight = speciesTree.getNodesAtHeight(height, 1.0, populationsAtHeight);
		}
		double top = height;
		if (height <= 0)
			return;

		//Will store which nodes in gene tree had migration events
		/**/
		NameReference nr = geneTree.makeAssociatedObjects("MigrationEvents");
		ObjectArray migrated = geneTree.getWhichAssociatedObject(nr);
		migrated.resetSize(geneTree.getNumNodeSpaces());
		for (int i= 0; i< migrated.getSize(); i++){
			if (migrated.getValue(i) == null)
				migrated.setValue(i, new Vector());
			else
				((Vector)migrated.getValue(i)).removeAllElements();
		}


		int numPopulations = 0;
		for (int igene = 0; igene<containing.length; igene++)
			containing[igene] =-1;

		boolean[] justMigrated = new boolean[containing.length];
		for (int igene = 0; igene<justMigrated.length; igene++)
			justMigrated[igene] = false;


		//initialize containing tree to put all terminal gene nodes into populations
		for (int ipop = 0; ipop < populationsAtHeight.length; ipop++) {
			int containingNode = populationsAtHeight[ipop];
			if (containingNode>0){  //i.e., it's a legitimate node
				numPopulations++;
				int taxonNum = speciesTree.taxonNumberOfNode(containingNode);
				Taxon species =speciesTree.getTaxa().getTaxon(taxonNum);
				if (species !=null){
					Taxon[] containedGenes = association.getAssociates(species); //finding all the genes contained within this species/population
					if (containedGenes != null) { 
						for (int i=0; i<containedGenes.length; i++)  {
							int taxNum = geneTaxa.whichTaxonNumber(containedGenes[i]);
							int cN = geneTree.nodeOfTaxonNumber(taxNum);
							if (!geneTree.nodeExists(cN)) {  //error: a gene contained in the species is not represented in the gene tree
								MesquiteMessage.warnProgrammer("contained gene not in gene tree in module " + getName() + ": containedGenes[i] " + containedGenes[i] + " taxNum " + taxNum + " cN " + cN);
								MesquiteMessage.warnProgrammer("   " + geneTree.writeTree());
							}
							containing[cN] = containingNode;  //remember which species node contains this node in the gene tree

						}
					}
				}
			}
		}

		int[] numGenesInPop = new int[populationsAtHeight.length];
		for (int i=0; i<numGenesInPop.length; i++)
			numGenesInPop[i] = -1;
		int[] commonAncestors = new int[numGenes];
		int[] ancestors = new int[geneTree.getNumNodeSpaces()];
		for (int i=0; i<ancestors.length; i++)
			ancestors[i] = -1;
		int numLeft = numGenes;



		//Gene tree starts off as bush with all branch lengths 0

		long generation=0;
		//Now coalesce down
		while (numLeft > 1){  //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< GENERATIONS loop
			generation++; //about to make next generation

			//initialize how many genes are in each population
			for (int ipop = 0; ipop < numGenesInPop.length; ipop++)
				numGenesInPop[ipop] = 0;

			for (int igene = 0; igene<containing.length; igene++)
				if (containing[igene] >= 0) 
					if (geneTree.motherOfNode(igene) != geneTree.getRoot()){
						MesquiteMessage.warnProgrammer("current gene branch not daughter of root");
					}


			//lengthen gene tree branches to include this generation
			for (int igene = 0; igene<containing.length; igene++)
				if (containing[igene] >= 0) //gene is contained in some population  (???? are these just all of the daughters of the root of the gene tree
					//lengthen branch for this generation
					geneTree.setBranchLength(igene, geneTree.getBranchLength(igene) + 1.0, false);

			//do migration & count genes in population
			for (int igene = 0; igene<justMigrated.length; igene++)
				justMigrated[igene] = false;
			for (int ipop = 0; ipop < populationsAtHeight.length; ipop++) {  //=====================> migration loop start
				int containingNode = populationsAtHeight[ipop];
				if (containingNode>0){
					//cycle through all contained genes in this population
					for (int igene = 0; igene<containing.length; igene++){
						if (containing[igene] == containingNode){ //gene is contained in this pop

							//here decide whether to migrate
							if ((migrationFestival < 0 || migrationFestival == generation) && !justMigrated[igene] && migrationRate>0.0 && rNG.nextDouble() <migrationRate){ //not already migrated and random number chooses to migrate
								int choices = numPopulations-1;   //if there are 5 populations, then we have 4 to choose from
								if (choices > 0){
									int roll = rNG.randomIntBetween(0, choices-1);  //thus we can choose populations 0 through 3 inclusive
									int ich = 0;
									int chosen = -1;
									//go through populations to see which species node the choice corresponds to
									for (int ihome = 0; ihome < populationsAtHeight.length && chosen<0; ihome++){
										if (populationsAtHeight[ihome]>0 && populationsAtHeight[ihome] != containingNode) {  //has to be at this height and not the current population
											if (roll == ich)
												chosen = ihome;
											ich++;
										}
									}
									if (chosen>=0){ //move to population chosen

										Vector v = (Vector)migrated.getValue(igene);

										if (v == null){
											v = new Vector();
											migrated.setValue(igene, v);
										}
										int pos = (int)(generation); // - geneTree.tallestPathAboveNode(igene, 1.0));  //record gen below top of branch that this happened
										v.addElement(new Dimension(populationsAtHeight[chosen], pos));   
										
										justMigrated[igene] = true;
										containing[igene] = populationsAtHeight[chosen];
										numGenesInPop[chosen]++;//gene moves, gets credited to new population
									}
								}
							}

							else
								numGenesInPop[ipop]++;  //gene stays, gets credited to original population
						}
					}

				} 
			} //=============================================================> migration loop end

			numLeft = 0;

			//coalesce: choose ancestors
			for (int ipop = 0; ipop < populationsAtHeight.length; ipop++) { //=============> coalescing all population loop start
				if (numGenesInPop[ipop]>1){   //this population does have genes
					int containingNode = populationsAtHeight[ipop];  //the species tree node corresponding to this

					if (containingNode>0){  //a legitimate species tree node
						//intialize ancestors chosen
						for (int i=0; i<ancestors.length; i++)
							ancestors[i] = -1;

						//calculate Ne as base Ne times branch width
						int Ne = (int)( branchNEAdjustment(containingNode)*basePopulationSize);  

						//cycle through all contained genes in this population, having each choose an ancestor
						for (int igene = 0; igene<containing.length; igene++){
							if (containing[igene] == containingNode){ //gene is contained in this pop
								ancestors[igene] = rNG.randomIntBetween(0, Ne-1); 
							}
						}

						//cycle through all contained genes in this population, seeing who chose the same ancestor
						for (int igene = 0; igene<containing.length; igene++){
							if (containing[igene] == containingNode){ //gene is contained in this pop
								/* If ancestor chosen is -1, then skip (already dealt with)
								otherwise get list of all genes that choose the same ancestor, 
								make them coalesce on the gene tree, then set their chosen ancestors to -1.
								 */
								if (ancestors[igene] != -1) {
									numLeft++;
									double igeneLength = geneTree.getBranchLength(igene);
									getGenesWithCommonAncestors(ancestors, igene, containing, containingNode, commonAncestors);
									boolean firstCoalescence = true;
									for (int icommon =0; icommon<commonAncestors.length; icommon++){
										int other = commonAncestors[icommon];
										if (other != igene && other >=0){  //found gene to coalesce
											double otherLength = geneTree.getBranchLength(other);
											geneTree.moveBranch(other, igene, false);
											if (firstCoalescence){  //need to set up mother node
												int mother = geneTree.motherOfNode(igene);
												if (geneTree.nodeExists(mother)){
													geneTree.setBranchLength(mother, 0, false);
													containing[mother]=containingNode;
												}
											}
											else {  //need to collapse new node to make polytomy
												int mother = geneTree.motherOfNode(igene);
												if (geneTree.nodeExists(mother)){
													geneTree.collapseBranch(mother, false);
												}
											}
											geneTree.setBranchLength(igene, igeneLength, false);
											geneTree.setBranchLength(other, otherLength, false);
											firstCoalescence = false;
											ancestors[igene]=-1;
											ancestors[other]=-1;
											containing[igene] = -1;
											containing[other] = -1;
										}
									}
								}
							}
						}
					}
				}
				else
					numLeft += numGenesInPop[ipop];
			}//=============================================================> coalescing all population loop start
			height -= 1.0;
			populationsAtHeight = speciesTree.getNodesAtHeight(height, 1.0, populationsAtHeight);

			numPopulations = numPopulationsFound(populationsAtHeight);

			//if we've gone to root, need to set populations as consisting of just root
			if (numPopulations == 0) {
				numPopulations = 1;
				populationsAtHeight[0] = speciesTree.getRoot();
			}

			//if genes are found that are not in a current population because water level has been dropped, move the genes into their population's ancestor
			for (int igene = 0; igene<containing.length; igene++){
				if (containing[igene]>=0 && IntegerArray.indexOf(populationsAtHeight, containing[igene])<0){ //gene is not contained in any current pop
					containing[igene] = speciesTree.motherOfNode(containing[igene]);
					if (IntegerArray.indexOf(populationsAtHeight, containing[igene])<0){
						MesquiteMessage.warnProgrammer("Error in " + getName() + ": gene not in a populations;  igene " + igene + " containing[igene] " + containing[igene] + "  ROOT " + speciesTree.getRoot());
					}
					}

			}
		}  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> GENERATIONS loop

	}
	/*.................................................................................................................*/
	void getGenesWithCommonAncestors(int[] ancestors,  int igene, int[] containing, int containingNode,int[] commonAncestors){
		for (int i = 0; i < commonAncestors.length; i++)
			commonAncestors[i]=-1;
		int target = ancestors[igene];
		int count = 0;
		for (int i= 0; i<ancestors.length; i++){
			if (containing[i] == containingNode && ancestors[i] == target)
				commonAncestors[count++] = i;
		}
	}
	/*.................................................................................................................*/
	public int getNumberOfTrees(Taxa taxa) {
		return MesquiteInteger.infinite;
	}
	MesquiteTree savedBush;
	/*.................................................................................................................*/
	public Tree getSimulatedTree(Taxa taxa, Tree geneTree, int treeNumber, ObjectContainer extra, MesquiteLong seed) {
		if (!check(taxa))			return null;
		CommandRecord.tick("Coalescing with migration in tree " + treeNumber);
		if (association==null) {
			if (first)
				discreetAlert( "No association found for use by Contained With Migration; no tree could be made");
			first = false;
			return null;
		}
		rNG.setSeed(seed.getValue());
		speciesTree = oneTreeSourceTask.getTree(association.getOtherTaxa(taxa));
		if (speciesTree==null) {
			if (first)
				discreetAlert( "No species tree found for use by Contained With Migration.  Taxa blocks: " + taxa + " AND " + association.getOtherTaxa(taxa));
			first = false;
			return null;
		}
		widths = speciesTree.getWhichAssociatedDouble(widthNameReference);

		MesquiteTree t=null;
		if (geneTree==null || !(geneTree instanceof MesquiteTree))
			t = new MesquiteTree(taxa);
		else
			t = (MesquiteTree)geneTree;
		if (savedBush==null || savedBush.getTaxa()!=t.getTaxa()) {
			t.setToDefaultBush(t.getNumTaxa(), false);
			savedBush = t.cloneTree();
		}
		else {
			t.setToClone(savedBush);
		}
		if (containing ==null || containing.length!=t.getNumNodeSpaces())
			containing = new int[t.getNumNodeSpaces()];
		for (int i=0; i<containing.length; i++)
			containing[i] = 0;
		t.setAllBranchLengths(0.0, false);
		coalesceAllPopulations((MesquiteTree)speciesTree, t, taxa);
		seed.setValue(rNG.nextLong());
		return t;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Coalescence in Current Tree with Migration";
	}
	/*.................................................................................................................*/
	public String getVersion() {
		return null;
	}
	/*.................................................................................................................*/
	/** returns whether this module is a prerelease version.  This returns "TRUE" here, forcing modules to override to claim they are not prerelease */
   	public boolean isPrerelease(){
   		return false;
   	}

	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Generates tree by a simple coalescence model of a neutral gene with constant population size, within a current species tree from a Tree window or other tree context. "
		+"Branch lengths are assigned according to generation of coalescence.  The default population size is 10000. " 
		+"The species tree used is a current tree found in a Tree Window or other tree context. " 
		+"Migration is allowed; the default probability is 0.00001 per individual generation. " 
		+"Migration can be isolated to a particular generation, or spread throughout the depth of the species/population history." ;
	}
	/*.................................................................................................................*/
	/** returns current parameters, for logging etc..*/
	public String getParameters() {
		if (speciesTree!=null) {
			String s =  "Species tree: " + speciesTree.getName() + "; Ne: " + basePopulationSize + "; Migration rate: " + migrationRate;
			if (migrationRate> 0.0 && migrationFestival>=0)
				s += " (migration concentrated in generation " + migrationFestival + " back in past)";
			return s;
		}
		return "";
	}
}


