/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.pairwise.PairwiseComparison;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.pairwise.lib.*;

/* ======================================================================== */
public class PairwiseComparison extends TreeDisplayAssistantMA {
	public String getName() {
		return "Pairwise Comparison";
	}
	
	/*  BUGS
	 * cut taxa from tree; labels remain
	 * 
	 * 
	 * 
	 * 
	 * *
	 */
	
	
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Performs pairwise comparison character correlation tests. Phylogenetically independent pairs are chosen, and the states of two binary characters are compared to see if they are correlated among these pairs." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(PairMaker.class, "Pairwise Comparisons needs to choose which method is used for selecting pairs.",
		"You can request the method for selecting pairs when Pairwise Comparisons starts, or later under the Pair Selector submenu.");
		//e.setAsEntryPoint(true);
		EmployeeNeed e2 = registerEmployeeNeed(CharSourceCoordObed.class, "Pairwise Comparisons needs to a sources of categorical characters on which to examine correlation.",
		"You can request the source of characters (both independent and dependent) when Pairwise Comparisons starts, or later under the Character Source submenu.");
		//e.setAsEntryPoint(true);
	}
	/*.................................................................................................................*/
	Vector pairDisplayers;
	PairMaker pairSelectorTask;
	public Taxa currentTaxa=null;
	public int currentCharA=0;
	public int currentCharB=1;
	int currentPairing = 0;
	public MesquiteBoolean showStates;
	int initialOffsetX = MesquiteInteger.unassigned;
	int initialOffsetY = MesquiteInteger.unassigned;
	public CharSourceCoordObed characterSourceTaskA, characterSourceTaskB;
	MesquiteString pairSelectorName;
	MesquiteCommand pstC;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		showStates = new MesquiteBoolean(true);
		pairDisplayers = new Vector();
		pairSelectorTask= (PairMaker)hireEmployee(PairMaker.class, "Method to select pairs");
		if (pairSelectorTask == null)
			return sorry(getName() + " couldn't start because no pair selector module obtained.");
		pairSelectorName = new MesquiteString(pairSelectorTask.getName());
		pstC = makeCommand("setPairSelector",  this);
		pairSelectorTask.setHiringCommand(pstC);
		if (numModulesAvailable(PairMaker.class)>1){
			MesquiteSubmenuSpec msPM = addSubmenu(null, "Pair selector", pstC, PairMaker.class);
			msPM.setSelected(pairSelectorName);
		}
		characterSourceTaskA = (CharSourceCoordObed)hireEmployee(CharSourceCoordObed.class, "Source of independent (first) character");
		if (characterSourceTaskA == null)
			return sorry(getName() + " couldn't start because no source of characters was obtained.");
		characterSourceTaskB = (CharSourceCoordObed)hireEmployee(CharSourceCoordObed.class,"Source of dependent (second) character");
		if (characterSourceTaskB == null)
			return sorry(getName() + " couldn't start because no source of characters was obtained");
		makeMenu("Pairs");
		addMenuItem( "Next Character (Indep.)", makeCommand("nextCharacterA",  this));
		addMenuItem( "Previous Character (Indep.)", makeCommand("previousCharacterA",  this));
		addMenuItem( "Next Character (Dep.)", makeCommand("nextCharacterB",  this));
		addMenuItem( "Previous Character (Dep.)", makeCommand("previousCharacterB",  this));
		addMenuSeparator();
		addMenuItem( "Next Pairing", makeCommand("nextPairing",  this));
		addCheckMenuItem(null, "Show states", MesquiteModule.makeCommand("showStates",  this), showStates);
		addMenuItem( "Close Pairwise Comparison", makeCommand("closeShowPairs",  this));
		addMenuSeparator();
		resetContainingMenuBar();
		return true;
	}

	/*.................................................................................................................*/
	/** Generated by an employee who quit.  The MesquiteModule should act accordingly. */
	public void employeeQuit(MesquiteModule employee) {
		if (employee == characterSourceTaskA || employee == characterSourceTaskB)  // character source quit and none rehired automatically
			iQuit();
	}
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		PairwiseDisplayer newDisplayer = new PairwiseDisplayer(this, treeDisplay, 0);
		currentTaxa=treeDisplay.getTaxa();
		characterSourceTaskA.initialize(currentTaxa);
		characterSourceTaskB.initialize(currentTaxa);
		characterSourceTaskA.getNumberOfCharacters(currentTaxa);
		characterSourceTaskB.getNumberOfCharacters(currentTaxa);
		newDisplayer.initiate();
		pairDisplayers.addElement(newDisplayer);
		return newDisplayer;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("requireCalculate"); 
		temp.addLine("showStates " + showStates.toOffOnString()); 
		temp.addLine("setPairSelector " , pairSelectorTask);
		temp.addLine("getCharacterSourceA " , characterSourceTaskA);
		temp.addLine("getCharacterSourceB " , characterSourceTaskB);
		temp.addLine("setCharacterA " + CharacterStates.toExternal(currentCharA));
		temp.addLine("setCharacterB " + CharacterStates.toExternal(currentCharB));
		temp.addLine("setPairing " + (currentPairing+1) );
		PairwiseDisplayer tco = (PairwiseDisplayer)pairDisplayers.elementAt(0);
		if (tco!=null && tco.legend!=null) {
			temp.addLine("setInitialOffsetX " + tco.legend.getOffsetX()); //Should go operator by operator!!!
			temp.addLine("setInitialOffsetY " + tco.legend.getOffsetY());
		}
		temp.addLine("calculate");
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	boolean requireCalculate = false;
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Goes to next pairing", null, commandName, "nextPairing")) {
			nextPairingAllOperators();
		}
		else if (checker.compare(this.getClass(), "Sets the initial horizontal offset of the legend", "[offset]", commandName, "setInitialOffsetX")) {
			int offset= MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(offset)) {
				initialOffsetX = offset;

			}
		}
		else if (checker.compare(this.getClass(),  "Sets the initial vertical offset of the legend", "[offset]", commandName, "setInitialOffsetY")) {
			int offset= MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(offset)) {
				initialOffsetY = offset;
			}
		}
		else if (checker.compare(this.getClass(), "Sets which pairing is shown", "[pairing number]", commandName, "setPairing")) {
			int index = MesquiteInteger.fromFirstToken(arguments, pos); //pairing numbers are 0 based internally, 1 based externally
			if (MesquiteInteger.isCombinable(index)) {
				if (index == 0) //to accomodate old scripts
					index = 1;
				currentPairing = index;
				setPairingAllOperators(index-1);
				if (MesquiteThread.isScripting())
					resetAllOperators();
			}
		}
		/*else if (checker.compare(this.getClass(), nullxxx, null, commandName, "numPairings")) {
			numPairingsAllOperators();
    	 	}
		 */
		else if (checker.compare(this.getClass(), "Sets whether or not the states used as the basis for the pairing and tests are shown at the tips of the tree", "[on or off]", commandName, "showStates")) {
			showStates.toggleValue(parser.getFirstToken(arguments));
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets the module used to select pairs of taxa", "[name of module]", commandName, "setPairSelector")) {
			PairMaker temp =  (PairMaker)replaceEmployee(PairMaker.class, arguments, "Method to choose pairs", pairSelectorTask);
			if (temp!=null) {
				pairSelectorTask=  temp;
				pairSelectorTask.setHiringCommand(pstC);
				pairSelectorName.setValue(pairSelectorTask.getName());
				if (!MesquiteThread.isScripting()){
					resetAllOperators();
					parametersChanged();
				}
			}
			return pairSelectorTask;
		}
		else if (checker.compare(this.getClass(), "Sets module supplying characters", "[name of module]", commandName, "setCharacterSourceA")) {//temporary, for data files using old system without coordinators
			return characterSourceTaskA.doCommand(commandName, arguments, checker);
		}
		else if (checker.compare(this.getClass(), "Returns module supplying characters A", null, commandName, "getCharacterSourceA")) {
			return characterSourceTaskA;
		}
		else if (checker.compare(this.getClass(), "Sets module supplying characters", "[name of module]", commandName, "setCharacterSourceB")) {//temporary, for data files using old system without coordinators
			return characterSourceTaskB.doCommand(commandName, arguments, checker);
		}
		else if (checker.compare(this.getClass(), "Returns module supplying characters B", null, commandName, "getCharacterSourceB")) {
			return characterSourceTaskB;
		}
		else if (checker.compare(this.getClass(), "Goes to next independent variable", null, commandName, "nextCharacterA")) {
			if (currentCharA >= characterSourceTaskA.getNumberOfCharacters(currentTaxa)-1)
				currentCharA=0;
			else
				currentCharA++;
			setPairingAllOperators(0);
			if (!MesquiteThread.isScripting())
				resetAllOperators();
		}
		else if (checker.compare(this.getClass(), "Goes to previous independent variable", null, commandName, "previousCharacterA")) {
			if (currentCharA<=0)
				currentCharA=characterSourceTaskA.getNumberOfCharacters(currentTaxa)-1;
			else
				currentCharA--;
			setPairingAllOperators(0);
			if (!MesquiteThread.isScripting())
				resetAllOperators();
		}
		else if (checker.compare(this.getClass(), "Sets which character is used as independent variable", null, commandName, "setCharacterA")) {
			int ic = CharacterStates.toInternal(MesquiteInteger.fromFirstToken(arguments, pos));
			if ((ic>=0) && (ic<=characterSourceTaskA.getNumberOfCharacters(currentTaxa)-1)) {
				currentCharA = ic;
				setPairingAllOperators(0);
				if (!MesquiteThread.isScripting())
					resetAllOperators();
			}
		}
		else if (checker.compare(this.getClass(), "Goes to next dependent variable", null, commandName, "nextCharacterB")) {
			if (currentCharB >= characterSourceTaskB.getNumberOfCharacters(currentTaxa)-1)
				currentCharB=0;
			else
				currentCharB++;
			setPairingAllOperators(0);
			if (!MesquiteThread.isScripting())
				resetAllOperators();
		}
		else if (checker.compare(this.getClass(), "Goes to previous dependent variable", null, commandName, "previousCharacterB")) {
			if (currentCharB<=0)
				currentCharB=characterSourceTaskB.getNumberOfCharacters(currentTaxa)-1;
			else
				currentCharB--;
			setPairingAllOperators(0);
			if (!MesquiteThread.isScripting())
				resetAllOperators();
		}
		else if (checker.compare(this.getClass(), "Sets which character is used as dependent variable", null, commandName, "setCharacterB")) {
			int ic = CharacterStates.toInternal(MesquiteInteger.fromFirstToken(arguments, pos));
			if ((ic>=0) && (ic<=characterSourceTaskB.getNumberOfCharacters(currentTaxa)-1)) { //TODO: should use external char numbers
				currentCharB = ic;
				setPairingAllOperators(0);
				if (!MesquiteThread.isScripting())
					resetAllOperators();
			}
		}    	 	
		else if (checker.compare(this.getClass(), "For scripting", null, commandName, "requireCalculate")) {
			requireCalculate = true;
		}
		else if (checker.compare(this.getClass(), "Requests calculations", null, commandName, "calculate")) {
			requireCalculate = false;
			resetAllOperators();
		}
		else if (checker.compare(this.getClass(), "Turn off pairwise comparison", null, commandName, "closeShowPairs")) {
			iQuit();
			resetContainingMenuBar();
		}
		return null;
	}
	/*.................................................................................................................*/
	long lastID = -1;
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (MesquiteThread.isScripting())
			return;
		if (notification != null){
			if (lastID == notification.getID())
				return;
			lastID = notification.getID();
		}
		resetAllOperators();
		outputInvalid();
	}
	/*.................................................................................................................*/
	public void resetAllOperators() {
		Enumeration e = pairDisplayers.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof PairwiseDisplayer) {
				PairwiseDisplayer tCO = (PairwiseDisplayer)obj;
				tCO.initiate();
				tCO.reset();
				currentPairing = tCO.getPairing();
			}
		}
	}
	public String purposeOfEmployee(MesquiteModule employee){
		if (employee == characterSourceTaskA)
			return "for independent character for pairwise comparison";
		else if (employee == characterSourceTaskB)
			return "for dependent character for pairwise comparison";
		else
			return "for pairwise comparison";
	}
	public String nameForWritableResults(){
		
		return "Pairwise Comparisons";
	}
	
	public boolean suppliesWritableResults(){
		return true;
	}
	public Object getWritableResults(){
		String results = "";
		Enumeration e = pairDisplayers.elements();
		
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof PairwiseDisplayer) {
				PairwiseDisplayer tCO = (PairwiseDisplayer)obj;
				if (results.length() > 0)
					 results += "\t";
				results += tCO.getWritableText(); 
			}
		}
		
		return results;
	}
	/*.................................................................................................................*/
	public void setPairingAllOperators(int index) {
		Enumeration e = pairDisplayers.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof PairwiseDisplayer) {
				PairwiseDisplayer tCO = (PairwiseDisplayer)obj;
				tCO.setPairing(index);
				currentPairing = tCO.getPairing();
			}
		}
	}
	/*.................................................................................................................*/
	public void nextPairingAllOperators() {
		Enumeration e = pairDisplayers.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof PairwiseDisplayer) {
				PairwiseDisplayer tCO = (PairwiseDisplayer)obj;
				tCO.nextPairing();
				currentPairing = tCO.getPairing();
			}
		}
	}

	/*.................................................................................................................
 	public void numPairingsAllOperators() {
		Enumeration e = pairDisplayers.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof PairwiseDisplayer) {
				PairwiseDisplayer tCO = (PairwiseDisplayer)obj;
	 			//System.out.println(tCO.numPairings());
	 		}
		}
	}

 	/*.................................................................................................................*/
	public void endJob() {
		Enumeration e = pairDisplayers.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof PairwiseDisplayer) {
				PairwiseDisplayer tCO = (PairwiseDisplayer)obj;
				tCO.turnOff();
			}
		}
		pairDisplayers.removeAllElements();
		super.endJob();
	}
	public boolean isPrerelease(){
		return false;
	} 
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}

}

/*Assumes binary & dichotomous.  On way down, store downstates and allstatesInClade.  On way up, pass forward Path objects and state sought.  Each Path object should store
two taxa which it should decide once it gets to tips.  A new Path object is formed each time an open clade is created by a lack of committed path sought
rising from below.  At each internal node must be stored the current choice at that internal node and the list of choices possible,
 so that all possible choices can be cycled through.  A choice is whether to continue Path up right or left if one is coming from below, or, if none
 is coming up, whether a path can be started (which it always can be 
 ======================================================================== */
class PairwiseDisplayer extends TreeDisplayDrawnExtra {
	public TaxaPairing pairing;
	TaxaPairerChars pairer;
	public ShowPairsLegend legend;
	private double pMax = 0;
	private double pMin = 1;
	public CharacterDistribution observedStatesA, observedStatesB;
	private int numPairings;
	private int numCharsA, numCharsB;
	Color[] colors;
	LabelsAtNodes labelsAtNodes;
	private int[] cat;
	PairwiseComparison pairModule;
	static final int POSITIVE = 1;
	static final int NEGATIVE = 0;
	static final int NEUTRAL = 2;
	static final int REMAINDER = 3;

	public PairwiseDisplayer (PairwiseComparison ownerModule, TreeDisplay treeDisplay, int numTaxa) {
		super(ownerModule, treeDisplay);
		pairModule = ownerModule;
		colors = new Color[64];
	}
	public void initiate() {
		if (legend!=null)
			legend.setMessage("Calculating...");
		pairer =(TaxaPairerChars) pairModule.pairSelectorTask.getPairer();
		cat = new int[4];
		setPairing(0);
		pairer.init();
		pMax = 0;
		pMin = 1;
		for  (int j=0; j<4; j++) cat[j]=0;
		treeDisplay.pleaseUpdate(false);
		numCharsA = pairModule.characterSourceTaskA.getNumberOfCharacters(pairModule.currentTaxa);
		numCharsB = pairModule.characterSourceTaskB.getNumberOfCharacters(pairModule.currentTaxa);
		if (legend!=null) {
			legend.adjustCharacterAScroll(pairModule.currentCharA, numCharsA);
			legend.adjustCharacterBScroll(pairModule.currentCharB, numCharsB);
		}
		//reset();
	}

	public String textForLegend(){
		return getWritableText();
	}
	
	public String getWritableText(){
		return getTextVersion();
	}
	
	public String textAtNode(Tree tree, int node){
		if (pairing == null)
			return " pairing null ";
		if (pairing.getNumPairs() == 0)
			return " no pairs ";
		TaxaPath path = pairing.findPath(node);
		if (path == null)
			return "";
		return describePathContrast(path);
	}

	/*.................................................................................................................*/
	void calculatePRange (TaxaPairerChars pairer) {
		Tree tree = treeDisplay.getTree();
		int currentPairing = pairer.getCurrentPairingNumber();
		int nP = pairer.getNumPairings(tree);
		double max,  min;
		TaxaPairing pairing = pairer.getFirstPairing(tree); 
		long[][] freqs = new long[100][2]; // first is characterizaiton, second is frequency
		for (int i = 0; i<100; i++) {
			freqs[i][0]=0;
			freqs[i][1]=0;
		}
		calculateCategories(pairing, tree);
		storeCats(cat, freqs);
		max = Binomial.bestTail(cat[1] + cat[0], cat[1], 0.5);
		min = max;
		for (int i = 1; i< nP; i++) {

			pairing = pairer.getNextPairing(tree); 
			if (i % 5000 ==0) MesquiteMessage.println("Pairings so far surveyed for p value: " + i);
			if (i % 100 ==0) CommandRecord.tick("Pairings surveyed for p value: " + i);
			//	if (i % 20000 ==0) MesquiteMessage.printStackTrace("Pairings so far surveyed for p value: " + i);
			calculateCategories(pairing, tree);
			storeCats(cat, freqs);
			double p =Binomial.bestTail(cat[1] + cat[0], cat[1], 0.5);
			if (p>max)
				max = p;
			else if (p<min)
				min = p;
		}
		//	writeFreqs(freqs);
		pMax = max;
		pMin = min;
		pairing = pairer.getPairing(tree, currentPairing);
		if (legend != null)
			legend.repaint();
	}
	/*.................................................................................................................*/
	private void storeCats(int[] cat, long[][] freqs) {
		long ispec = cat[0] + cat[1]*10 + cat[2]*100 + cat[3]*1000;
		boolean found = false;
		for (int i=0; i<100 && !found; i++) {
			if (freqs[i][0]== ispec) {
				found= true;
				freqs[i][1]++;
			}
			else if  (freqs[i][0]== 0) {
				freqs[i][0]= ispec;
				freqs[i][1]= 1;
				found= true;
			}
		}
	}
	/*.................................................................................................................*/
	private void writeFreqs(long[][] freqs) {
		boolean found = false;
		for (int i=0; i<100 && !found; i++) {
			if (freqs[i][0]== 0) {
				found= true;
			}
			else {
				System.out.println(" ispec " + freqs[i][0] + " num " + freqs[i][1]);
			}
		}
	}
	/*.................................................................................................................*/
	private void drawTerminalStuff(int N, Tree tree, Graphics g) {
		if (tree == null || g == null || treeDisplay == null || treeDisplay.getTreeDrawing() == null || observedStatesA == null || observedStatesB == null)
			return;
		for (int d = tree.firstDaughterOfNode(N); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			drawTerminalStuff(d, tree, g);
		if (tree.nodeIsTerminal(N)) {
			int t = tree.taxonNumberOfNode(N);

			MesquiteLabel c = (MesquiteLabel)labelsAtNodes.getPanel(t);
			if (c != null){
				if (pairModule.showStates.getValue()) {
					int nodeX = (int)treeDisplay.getTreeDrawing().x[N];  // integer nodeloc approximation
					int nodeY = (int)treeDisplay.getTreeDrawing().y[N]; // integer nodeloc approximation
					//	c.setText( observedStatesA.toString(t, " ") + "/" + observedStatesB.toString(t, " ")); 
					c.setText("\2" + observedStatesA.toString(t, " "));  //\2=boldface
					c.addLine(observedStatesB.toString(t, " ")); 
					c.setLocation(nodeX, nodeY);
					c.setVisible(true);

					c.repaint();
				}
				else
					c.setVisible(false);
			}
		}
		g.setColor(Color.black);
	}
	/*.................................................................................................................*/
	private void calculateCategories(TaxaPairing pairing, Tree tree) {
		if (tree == null || pairing == null || cat == null || observedStatesA == null || observedStatesB == null)
			return;
		for  (int j=0; j<4; j++)
			cat[j]=0;
		int numTaxa = tree.getNumTaxa();
		int numPaths = pairing.getNumPairs();
		for (int i=0; i<numPaths; i++) {
			TaxaPath path =pairing.getPath(i);
			if (path == null)
				return;
			int tax1 =path.gettaxon1();
			int tax2 = path.gettaxon2();

			if (observedStatesA.firstIsGreater(tax1, tax2)) {
				if (observedStatesB.firstIsGreater(tax1, tax2))
					cat[POSITIVE]++;
				else if (observedStatesB.firstIsGreater(tax2, tax1))
					cat[NEGATIVE]++;
				else 
					cat[NEUTRAL]++;
			}
			else if (observedStatesA.firstIsGreater(tax2, tax1)) {
				if (observedStatesB.firstIsGreater(tax1, tax2))
					cat[NEGATIVE]++;
				else if (observedStatesB.firstIsGreater(tax2, tax1))
					cat[POSITIVE]++;
				else 
					cat[NEUTRAL]++;
			}
			else {
				cat[REMAINDER]++;
			}

		}
	}
	
	private String describePath(TaxaPath path){
		if (observedStatesA == null)
			return "";
		Taxa taxa = observedStatesA.getTaxa();
		
		return taxa.getTaxonName(path.gettaxon1()) + " vs. "  + taxa.getTaxonName(path.gettaxon2()) + "; MRCA " + path.getBase();
	}
	private String describeContrast(int tax1, int tax2){
		return "X = "+ observedStatesA.toString(tax1, "") + " vs. " + observedStatesA.toString(tax2, "") + "; Y = "+ observedStatesB.toString(tax1, "") + " vs. " + observedStatesB.toString(tax2, "");
	}
	/*.................................................................................................................*/
	private String describePathContrast(TaxaPath path) {
		if (path == null)
			return "Remainder " + describePath(path);
		if (observedStatesA == null || observedStatesB== null)
			return "";
		int tax1 =path.gettaxon1();
		int tax2 = path.gettaxon2();

		if (observedStatesA.firstIsGreater(tax1, tax2)) {
			if (observedStatesB.firstIsGreater(tax1, tax2))
				return "Positive: "+ describeContrast(tax1, tax2) + " for " + describePath(path);
			else if (observedStatesB.firstIsGreater(tax2, tax1))
				return "Negative: "+ describeContrast(tax1, tax2) + " for " + describePath(path);
			else 
				return "Neutral: "+ describeContrast(tax1, tax2) + " for " + describePath(path);
		}
		else if (observedStatesA.firstIsGreater(tax2, tax1)) {
			if (observedStatesB.firstIsGreater(tax1, tax2))
				return "Negative: "+ describeContrast(tax1, tax2) + " for " + describePath(path);
			else if (observedStatesB.firstIsGreater(tax2, tax1))
				return "Positive: "+ describeContrast(tax1, tax2) + " for " + describePath(path);
			else 
				return "Neutral: "+ describeContrast(tax1, tax2) + " for " + describePath(path);
		}
		else {
			return "Remainder: "+ describeContrast(tax1, tax2) + " for " + describePath(path);
		}
	}
	/*.................................................................................................................*/
	private int categoryOfPath(TaxaPath path) {
		if (path == null)
			return REMAINDER;
		int tax1 =path.gettaxon1();
		int tax2 = path.gettaxon2();

		if (observedStatesA.firstIsGreater(tax1, tax2)) {
			if (observedStatesB.firstIsGreater(tax1, tax2))
				return POSITIVE;
			else if (observedStatesB.firstIsGreater(tax2, tax1))
				return NEGATIVE;
			else 
				return NEUTRAL;
		}
		else if (observedStatesA.firstIsGreater(tax2, tax1)) {
			if (observedStatesB.firstIsGreater(tax1, tax2))
				return NEGATIVE;
			else if (observedStatesB.firstIsGreater(tax2, tax1))
				return POSITIVE;
			else 
				return NEUTRAL;
		}
		else {
			return REMAINDER;
		}
	}
	int dp = 0;
	/*.................................................................................................................*/
	private void drawPairs(TreeDisplay treeDisplay, Tree tree, int drawnRoot, Graphics g) {
		int numTaxa = tree.getNumTaxa();
		int numPaths = pairing.getNumPairs();
		for (int i=0; i<numPaths; i++) {
			TaxaPath path =pairing.getPath(i);
			int category = categoryOfPath(path);
			int t1 = tree.nodeOfTaxonNumber(path.gettaxon1());
			int t2 =  tree.nodeOfTaxonNumber(path.gettaxon2());
			if (category == POSITIVE)
				g.setColor(Color.green);
			else if (category == NEGATIVE)
				g.setColor(Color.red);
			else if (category == NEUTRAL)
				g.setColor(ColorDistribution.lightBlue);
			else
				g.setColor(Color.blue);
			//g.setColor(new Color(Color.HSBtoRGB((float)(i * 1.0 /numTaxa),(float)1.0,(float)1.0)));
			int thisNode = t1;
			while (tree.nodeExists(thisNode) && thisNode!= path.getBase() && thisNode!=drawnRoot) {
				treeDisplay.getTreeDrawing().fillBranch(tree, thisNode, g);
				thisNode=tree.motherOfNode(thisNode);
			}
			thisNode = t2;
			while (tree.nodeExists(thisNode) && thisNode!= path.getBase() && thisNode!=drawnRoot) {
				treeDisplay.getTreeDrawing().fillBranch(tree, thisNode, g);
				thisNode=tree.motherOfNode(thisNode);
			}
		}
	}
	String textVersion = "";
	public String setResults(int[] results, TaxaPairing pairing, double pMin, double pMax) {
		if(pairing == null){
			String s = "Calculation not done";
			textVersion = s;
			return s;
		}
		else if (pairing.getCalculationNotDone()){
			String s = "Calculation not done\n" + pairer.getWarningMessage();
			textVersion = s;
			return s;
		}
		
		int numP = pairing.getNumPairs(); 
		int currentPairing = pairer.getCurrentPairingNumber();
		String s = "Pairing " + (currentPairing+1) +" of " + numPairings;
		if (pairModule.pairSelectorTask.limitReached(numPairings))
			s += " (There may be more pairings; the limit of number of pairings allowed was reached.)";
		if (!StringUtil.blank(pairer.getWarningMessage()))
			s += "\n" + pairer.getWarningMessage();
		s +="\nSelector: " + pairModule.pairSelectorTask.getName() + "\n" + numP + " pairs\n";
		s += "   Positive " + results[1] + "\n   Negative " + results[0] + "\n   Neutral " + results[2] + "\n   Remainder " + results[3];
		s += "\n best tail p=" + MesquiteDouble.toString(Binomial.bestTail(results[0] + results[1], results[1], 0.5));
		if (pMax>0 && pMin>0) {
			s += "\nRange (" + numPairings + " pairings):";
			s += "\n" + MesquiteDouble.toString(pMin) + " - " + MesquiteDouble.toString(pMax);
		}
		
		textVersion = "Pairing\t" + (currentPairing+1) +"\tof\t" + numPairings + "\t";
		if (pairModule.pairSelectorTask.limitReached(numPairings))
			textVersion += " (There may be more pairings; the limit of number of pairings allowed was reached.) ";
		textVersion += " " + pairer.getWarningMessage();
		textVersion +=" Selector: " + pairModule.pairSelectorTask.getName() + "\t" + numP + "\tpairs. ";
		textVersion += "   Positive\t" + results[1] + "\tNegative\t" + results[0] + "\tNeutral\t" + results[2] + "\tRemainder\t" + results[3];
		textVersion += "\tbest tail p=\t" + MesquiteDouble.toString(Binomial.bestTail(results[0] + results[1], results[1], 0.5));
		if (pMax>0 && pMin>0) {
			textVersion += "\tRange (" + numPairings + " pairings):";
			textVersion += "\t" + MesquiteDouble.toString(pMin) + "\t" + MesquiteDouble.toString(pMax);
		}
		else {
			textVersion += "\t \t  \t ";
		}
		return s;
	}	
	String getTextVersion(){
		return textVersion;
	}
	
	public void nextPairing() {
		if (pairer == null)
			return;
		if (pairer.getCurrentPairingNumber()<= numPairings) {
			pairing = pairer.getNextPairing(treeDisplay.getTree()); 
			if (legend !=null)
				legend.adjustPairingScroll(pairer.getCurrentPairingNumber(), numPairings);
			calculateCategories(pairing, treeDisplay.getTree());
			//legend.setResults(cat, pairing, new TwoCharPairing(tree, observedStatesA, observedStatesB));
			String s = setResults(cat, pairing, pMin, pMax);
			if (legend != null)
				legend.setMessage(s);
		}
		if (treeDisplay!=null)
			treeDisplay.pleaseUpdate(false);
	}
	public void setPairing(int index) {
		if (pairer == null)
			return;
		if (index< numPairings && index >= 0) {
			pairing = pairer.getPairing(treeDisplay.getTree(), index); 
			if (legend !=null)
				legend.adjustPairingScroll(pairer.getCurrentPairingNumber(), numPairings);
			calculateCategories(pairing, treeDisplay.getTree());
			//legend.setResults(cat, pairing, new TwoCharPairing(tree, observedStatesA, observedStatesB));
			String s = setResults(cat, pairing, pMin, pMax);
			if (legend != null)
				legend.setMessage(s);
		}
		if (treeDisplay!=null)
			treeDisplay.pleaseUpdate(false);
	}
	public int getPairing() {
		if (pairer == null)
			return 0;
		return pairer.getCurrentPairingNumber();
	}

	public void reset(){
		if (pairer == null)
			return;
		observedStatesA = pairModule.characterSourceTaskA.getCharacter(pairModule.currentTaxa, pairModule.currentCharA);
		observedStatesB = pairModule.characterSourceTaskB.getCharacter(pairModule.currentTaxa, pairModule.currentCharB);
		pairer.setCharacters(observedStatesA, observedStatesB);
		pairing = pairer.getFirstPairing(treeDisplay.getTree());

		numPairings = pairer.getNumPairings(treeDisplay.getTree());
		calculatePRange(pairer);
		if (legend !=null)
			legend.adjustPairingScroll(pairer.getCurrentPairingNumber(), numPairings);
		calculateCategories(pairing, treeDisplay.getTree());
		//legend.setResults(cat, pairing, new TwoCharPairing(tree, observedStatesA, observedStatesB));
		String s = setResults(cat, pairing, pMin, pMax);
		if (legend != null)
			legend.setMessage(s);
		treeDisplay.pleaseUpdate(false);
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		boolean toShow = false;
		if (legend == null) {
			legend = new ShowPairsLegend(pairModule, this);
			legend.adjustCharacterAScroll(pairModule.currentCharA, numCharsA);
			legend.adjustCharacterBScroll(pairModule.currentCharB, numCharsB);
			legend.setVisible(false);
			addPanelPlease(legend);
			toShow = true;
		}
		legend.adjustPairingScroll(pairer.getCurrentPairingNumber(), numPairings);  //dont do this without protection for loop
		if (labelsAtNodes==null) {
			labelsAtNodes = new LabelsAtNodes(ownerModule, tree.getNumTaxa(), treeDisplay);
		}
		else if (labelsAtNodes.getNumNodes()!=tree.getNumTaxa() ) {
			labelsAtNodes.resetNumNodes(tree.getNumTaxa());
		}
		if (pairing !=null) {
			drawTerminalStuff(drawnRoot, tree, g);
			drawPairs(treeDisplay, tree, drawnRoot, g);
			//	calculateCategories(pairing, tree);
			//legend.setResults(cat, pairing, new TwoCharPairing(tree, observedStatesA, observedStatesB));
			String s = setResults(cat, pairing, pMin, pMax);
			if (legend != null)
				legend.setMessage(s);

			legend.adjustLocation(); //dont do this without protection for loop
			if (toShow || !legend.isVisible())
				legend.setVisible(true);
			legend.repaint();  // dont do this without protection for loop
		}
	}
	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		reset();
	}

	public void turnOff() {
		super.turnOff();
		if (labelsAtNodes!=null)
			labelsAtNodes.dispose();
		if (treeDisplay!=null && legend!=null)
			removePanelPlease(legend);
	}
}


/* ======================================================================== */
class ShowPairsLegend extends TreeDisplayLegend {
	private PairwiseComparison ownerModule;
	public MiniScroll pairingScroll = null;
	public MiniScroll characterAScroll = null;
	public MiniScroll characterBScroll = null;
	private PairwiseDisplayer pD;
	private static final int defaultLegendWidth=180;
	private static final int defaultLegendHeight=340;

	private int oldNumPairings = 0;
	private int oldCurrentPairing = -1;
	private int oldCharA=-1;
	private int oldNumCharsA=0;
	private int oldCharB=-1;
	private int oldNumCharsB=0;
	private boolean resizingLegend=false;
	private TextArea pairingMessageBox;
	private TextArea AMessageBox;
	private TextArea BMessageBox;
	private static int scrollStart = 24;
	public ShowPairsLegend(PairwiseComparison ownerModule, PairwiseDisplayer pD) {
		super(pD.treeDisplay,defaultLegendWidth, defaultLegendHeight);
		this.pD = pD;
		this.ownerModule = ownerModule;
		//setBackground(ColorDistribution.light);
		setLayout(null);

		setOffsetX(ownerModule.initialOffsetX);
		setOffsetY(ownerModule.initialOffsetY);
		setSize(legendWidth, legendHeight);

		characterAScroll = new MiniScroll(MesquiteModule.makeCommand("setCharacterA",  ownerModule), false, 1, 1, 1, "character");
		add(characterAScroll);
		//characterAScroll.setBackground(Color.cyan);
		characterAScroll.setLocation(2,scrollStart);
		characterAScroll.setColor(Color.blue);
		AMessageBox = new TextArea("", 1, 1, TextArea.SCROLLBARS_NONE);
		AMessageBox.setVisible(false);
		AMessageBox.setBackground(Color.white);
		AMessageBox.setEditable(false);
		add(AMessageBox);
		AMessageBox.setBounds(4,characterAScroll.getBounds().y+characterAScroll.getBounds().height+4,legendWidth-8, 32);

		characterBScroll = new MiniScroll(MesquiteModule.makeCommand("setCharacterB",  ownerModule), false, 1, 1, 1, "character");
		add(characterBScroll);
		//characterBScroll.setBackground(Color.cyan);
		characterBScroll.setLocation(2,AMessageBox.getBounds().y+AMessageBox.getBounds().height+4);
		characterBScroll.setColor(Color.blue);
		BMessageBox = new TextArea("", 1, 1, TextArea.SCROLLBARS_NONE);
		BMessageBox.setEditable(false);
		BMessageBox.setVisible(false);
		BMessageBox.setBackground(Color.white);
		add(BMessageBox);
		BMessageBox.setBounds(4,characterBScroll.getBounds().y+characterBScroll.getBounds().height+4,legendWidth-8, 32);

		pairingScroll = new MiniScroll(MesquiteModule.makeCommand("setPairing",  ownerModule), false, 1,1,1,"");
		add(pairingScroll);
		//pairingScroll.setBackground(Color.cyan);
		pairingScroll.setLocation(2,BMessageBox.getBounds().y+BMessageBox.getBounds().height+4); 
		pairingScroll.setColor(Color.blue);
		pairingMessageBox = new TextArea("", 1, 1, TextArea.SCROLLBARS_NONE);
		pairingMessageBox.setVisible(false);
		pairingMessageBox.setEditable(false);
		pairingMessageBox.setBackground(Color.white);
		add(pairingMessageBox);
		pairingMessageBox.setBounds(4,pairingScroll.getBounds().y+pairingScroll.getBounds().height+4,legendWidth-8, legendHeight-(pairingScroll.getBounds().y+pairingScroll.getBounds().height+6));
	}

	public void adjustCharacterAScroll(int charA, int numCharsA) {
		if (characterAScroll == null) {
			characterAScroll = new MiniScroll(MesquiteModule.makeCommand("setCharacterA",  ownerModule), false, CharacterStates.toExternal(0), CharacterStates.toExternal(charA), CharacterStates.toExternal(numCharsA-1),"character");
			add(characterAScroll);
			//characterAScroll.setBackground(Color.cyan);
			characterAScroll.setLocation(2,4);
			characterAScroll.setColor(Color.blue);
			repaint();
			oldCharA = charA;
			oldNumCharsA = numCharsA;
		}
		else {
			characterAScroll.setVisible(true);
			if (oldNumCharsA != numCharsA) {
				characterAScroll.setMaximumValue(CharacterStates.toExternal(numCharsA -1));
				oldNumCharsA = numCharsA;
			}
			if (oldCharA != charA) {
				characterAScroll.setCurrentValue(CharacterStates.toExternal(charA));
				oldCharA = charA;
			}
			repaint();
		}
	}
	public void adjustCharacterBScroll(int charB, int numCharsB) {
		if (characterBScroll == null) {
			characterBScroll = new MiniScroll( MesquiteModule.makeCommand("setCharacterB",  ownerModule), false, CharacterStates.toExternal(0), CharacterStates.toExternal(charB), CharacterStates.toExternal(numCharsB-1),"character");
			add(characterBScroll);
			//characterBScroll.setBackground(Color.cyan);
			characterBScroll.setLocation(2,characterAScroll.getBounds().y+characterAScroll.getBounds().height+4);
			characterBScroll.setColor(Color.blue);
			repaint();
			oldCharB = charB;
			oldNumCharsB = numCharsB;
		}
		else {
			characterBScroll.setVisible(true);
			if (oldNumCharsB != numCharsB) {
				characterBScroll.setMaximumValue(CharacterStates.toExternal(numCharsB -1));
				oldNumCharsB = numCharsB;
			}
			if (oldCharB != charB) {
				characterBScroll.setCurrentValue(CharacterStates.toExternal(charB));
				oldCharB = charB;
			}
			repaint();
		}
	}
	public void adjustPairingScroll(int currentPairing, int numPairings) {
		//currentPairing is in internal (0 based); convert to external where needed
		if (pairingScroll == null) {
			pairingScroll = new MiniScroll( MesquiteModule.makeCommand("setPairing",  ownerModule), false, 1, currentPairing+1, numPairings,"");
			add(pairingScroll);
			//pairingScroll.setBackground(Color.cyan);
			pairingScroll.setLocation(2,characterBScroll.getBounds().y+characterBScroll.getBounds().height+4); 
			pairingScroll.setColor(Color.blue);
			repaint();
			oldNumPairings = numPairings;
			oldCurrentPairing = currentPairing;
		}
		else {
			pairingScroll.setVisible(true);
			if (oldNumPairings != numPairings) {
				pairingScroll.setMaximumValue(numPairings);
				oldNumPairings = numPairings;
			}
			if (oldCurrentPairing != currentPairing) {
				pairingScroll.setCurrentValue(currentPairing+1);
				oldCurrentPairing = currentPairing;
			}
			repaint();
		}
		pairingMessageBox.setBounds(4,pairingScroll.getBounds().y+pairingScroll.getBounds().height+4,legendWidth-8, legendHeight-(pairingScroll.getBounds().y+pairingScroll.getBounds().height+6));
	}

	public void setVisible(boolean b) {
		super.setVisible(b);
		if (pairingScroll !=null)
			pairingScroll.setVisible(b);
		if (pairingMessageBox != null)
			pairingMessageBox.setVisible(b);
		if (characterAScroll !=null)
			characterAScroll.setVisible(b);
		if (AMessageBox != null)
			AMessageBox.setVisible(b);
		if (characterBScroll !=null)
			characterBScroll.setVisible(b);
		if (BMessageBox != null)
			BMessageBox.setVisible(b);
	}
	public void setLocation(int x, int y){
		super.setLocation(x, y);
	}
	public void paint(Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		Color c = g.getColor();
		g.setColor(Color.blue);
		g.drawString("Pairwise comparisons", 2, scrollStart-8);
		g.drawRect(0,0, getBounds().width-1, getBounds().height -1);
		Rectangle rect = pairingScroll.getBounds();
		g.drawString("Pairing", rect.x+ rect.width, rect.y + rect.height-4);
		rect = characterAScroll.getBounds();
		g.drawString("Independent", rect.x+ rect.width, rect.y + rect.height-4);
		rect = characterBScroll.getBounds();
		g.drawString("Dependent", rect.x+ rect.width, rect.y + rect.height-4);
		if (c!=null) g.setColor(c);
		String A = ownerModule.characterSourceTaskA.getNameAndParameters();
		String B = ownerModule.characterSourceTaskB.getNameAndParameters();
		if (A !=null && !A.equals(AMessageBox.getText()))
			AMessageBox.setText(A);
		if (B !=null && !B.equals(BMessageBox.getText()))
			BMessageBox.setText(B);
		MesquiteWindow.uncheckDoomed(this);
	}

	String getTextVersion(){
		return pD.getTextVersion();
	}
	public void setMessage(String s) {
		if (s==null || s.equals("")) {
			pairingMessageBox.setText("");
		}
		else {
			pairingMessageBox.setText(s);
		}
		pairingMessageBox.repaint();
	}
}


