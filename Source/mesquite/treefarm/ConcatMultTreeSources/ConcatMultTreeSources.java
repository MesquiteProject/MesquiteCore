/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.ConcatMultTreeSources;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.treefarm.lib.*;

/* ======================================================================== */
public class ConcatMultTreeSources extends TreeSource {
	public String getName() {
		return "Concatenate Multiple Tree Sources";
	}
	public String getNameForMenuItem() {
		return "Concatenate Multiple Tree Sources...";
	}
	public String getExplanation() {
		return "Concatenates multiple tree sources to yield a tree source supplying trees from each source.  The trees are marked with values to indicate the source.";
	}

	public int getVersionOfFirstRelease(){
		return 272;  
	}
	public boolean isPrerelease(){
		return false;  
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		super.getEmployeeNeeds();
		EmployeeNeed e = registerEmployeeNeed(TreeSource.class, getName() + "  needs a method to modify trees.",
		"The method to modify trees can be chosen initially or in the Transformer of Trees submenu");
	}
	Vector sources = new Vector();
	Vector assignedNumbers = new Vector();
	Vector isAssigned = new Vector();
	Taxa taxa;
	int defaultNumberOfItems = 100;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (!MesquiteThread.isScripting()){
			TreeSource treeSourceA = (TreeSource)hireEmployee(TreeSource.class, "First source of trees to be concatenated");
			if (treeSourceA == null) {
				return sorry(getName() + " couldn't start because no source of trees was obtained.");
			}
			sources.addElement(treeSourceA);
			assignedNumbers.addElement(new MesquiteInteger());
			isAssigned.addElement(new MesquiteBoolean(false));
			TreeSource treeSourceB = (TreeSource)hireEmployee(TreeSource.class, "Second source of trees to be concatenated");
			if (treeSourceB == null) {
				return sorry(getName() + " couldn't start because a second source of trees was not obtained.");
			}
			sources.addElement(treeSourceB);
			assignedNumbers.addElement(new MesquiteInteger());
			isAssigned.addElement(new MesquiteBoolean(false));
			TreeSource source = null;
			int i = 3;
			do {
				acceptEmployeeHireCancel(true);
				source = (TreeSource)hireEmployee(TreeSource.class, "Next source of trees to be concatenated (#" + i + ").  Hit Cancel to indicate no more sources needed.");
				acceptEmployeeHireCancel(false);
				if (source != null) {
					sources.addElement(source);
					assignedNumbers.addElement(new MesquiteInteger());
					isAssigned.addElement(new MesquiteBoolean(false));
				}
				
				i++;
			}
			while (source != null);
		}
		return true;
	}
	/** Returns the purpose for which the employee was hired (e.g., "to reconstruct ancestral states" or "for X axis").*/
	public String purposeOfEmployee(MesquiteModule employee) {
		int w = sources.indexOf(employee);
		if (w>=0)
			return "Source of trees #"  + (w+1); 
		else
			return  "for " + getName();
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}

	/*.................................................................................................................*/
	boolean isNumberAssigned(TreeSource source){
		int which = sources.indexOf(source);
		if (which >=0 && which < isAssigned.size())
			return ((MesquiteBoolean)isAssigned.elementAt(which)).getValue();
		return false;
	}
	void setIsNumberAssigned(int i, boolean assigned){
		if (i >=0 && i < isAssigned.size())
			((MesquiteBoolean)isAssigned.elementAt(i)).setValue(assigned);
	}
	void setIsNumberAssigned(TreeSource source, boolean assigned){
		int which = sources.indexOf(source);
		setIsNumberAssigned(which, assigned);
	}
	int getNumberAssigned(int i){
		if (i >=0 && i < assignedNumbers.size())
			return ((MesquiteInteger)assignedNumbers.elementAt(i)).getValue();
		return MesquiteInteger.unassigned;
	}
	void setNumberAssigned(int i, int num){
		if (i >=0 && i < assignedNumbers.size())
			((MesquiteInteger)assignedNumbers.elementAt(i)).setValue(num);
	}
	void setNumberAssigned(TreeSource source,  int num){
		int which = sources.indexOf(source);
		setNumberAssigned(which, num);
	}
	/*.................................................................................................................*/
	MesquiteColorTable colorTable;
  	public void prepareItemColors(Taxa taxa){
  		colorTable = new MesquiteColorTable();
  		colorTable.setMode(MesquiteColorTable.COLORS_NO_BW);
   	}
  	
   	public Color getItemColor(Taxa taxa, int itree){
   		int whichSource = findSource(taxa, itree);
   		if (whichSource <0 || sources == null || colorTable == null)
   			return Color.lightGray;
   		else
   			return colorTable.getColor(sources.size(), whichSource);

   	}
   	private int findSource(Taxa taxa, int itree){
		setPreferredTaxa(taxa);
		Tree t = null;
		int sum = 0;
		for (int i= 0; i<sources.size() && t == null; i++){
			int nta = getNumberAssigned(i);
			TreeSource source = (TreeSource)sources.elementAt(i);
			if (nta != MesquiteInteger.finite) {
				if (itree <nta + sum)
					return i;
				sum += nta;
			}
			else {
				t = source.getTree(taxa, itree);

				if (t == null){
					nta = source.getNumberOfTrees(taxa, true);
					if (!MesquiteInteger.isCombinable(nta))
						return -1;
					if (itree  < nta + sum)
						return i;
					sum += nta;
				}
			}
		}
		return -1;
 	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = super.getSnapshot(file);
		for (int i = 0; i<sources.size(); i++){
			temp.addLine("addTreeSource ", (TreeSource)sources.elementAt(i)); 
			if (isNumberAssigned((TreeSource)sources.elementAt(i)))
				temp.addLine("assignNumTrees " + i + "  " + getNumberAssigned(i)); 
		}
		return temp;
	}
	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger(0);

	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Adds a source of trees", "[name of module]", commandName, "addTreeSource")) {
			TreeSource temp = (TreeSource)hireNamedEmployee(TreeSource.class, arguments, null);
			if (temp !=null){
				sources.addElement(temp);
				assignedNumbers.addElement(new MesquiteInteger());
				isAssigned.addElement(new MesquiteBoolean(false));
				return temp;
			}
		}
		else if (checker.compare(this.getClass(), "Sets the number of trees for the first source", "[number of trees]", commandName, "assignNumTrees")) {
			int which = MesquiteInteger.fromFirstToken(arguments, pos);
			int newNum = MesquiteInteger.fromString(arguments, pos);
			if (MesquiteInteger.isCombinable(which) && MesquiteInteger.isCombinable(newNum) && newNum>0 ) {
				setNumberAssigned(which, newNum);
				setIsNumberAssigned(which, true);
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	void resetSource(TreeSource source, Taxa taxa, boolean queryPlease){
		int numItems = source.getNumberOfTrees(taxa, true); //this can't be infinite; if so then ask for number
		if (!MesquiteInteger.isCombinable(numItems) && MesquiteInteger.finite != numItems) { //not specified; need to set
			if (!isNumberAssigned(source) || queryPlease) {
				int numTreesAssigned = defaultNumberOfItems;
				if (!MesquiteThread.isScripting()) {
					numTreesAssigned = MesquiteInteger.queryInteger(containerOfModule(), "Number of Trees", "Number of trees from source #" + (sources.indexOf(source)+1) + " [" + source.getNameAndParameters() + "]", numTreesAssigned);
					if (!MesquiteInteger.isCombinable(numTreesAssigned)) 
						numTreesAssigned = defaultNumberOfItems;
				}
				setNumberAssigned(source, numTreesAssigned);
			}
			setIsNumberAssigned(source, true);
		}
		else {
			setNumberAssigned(source, numItems);
			setIsNumberAssigned(source, false);

		}
	}
	/*.................................................................................................................*/
	public void resetTreeSource( Taxa taxa, boolean queryPlease){
		for (int i = 0; i<sources.size(); i++)
			resetSource((TreeSource)sources.elementAt(i), taxa, queryPlease);
	}
	/*.................................................................................................................*/
	public void setPreferredTaxa(Taxa taxa) {
		this.taxa = taxa;
		for (int i = 0; i<sources.size(); i++)
			((TreeSource)sources.elementAt(i)).setPreferredTaxa(taxa);
	}
	/*.................................................................................................................*/
	public void initialize(Taxa taxa) {
		setPreferredTaxa(taxa);
		for (int i = 0; i<sources.size(); i++)
			((TreeSource)sources.elementAt(i)).initialize(taxa);
	}

	/*.................................................................................................................*/
	public Tree getTree(Taxa taxa, int itree) {
		setPreferredTaxa(taxa);
		int whichSource = 0;
		Tree t = null;
		int sum = 0;
		for (int i= 0; i<sources.size() && t == null; i++){
			int nta = getNumberAssigned(i);
			TreeSource source = (TreeSource)sources.elementAt(i);
			if (nta != MesquiteInteger.finite) {
				if (itree <nta + sum){
					t = source.getTree(taxa, itree-sum);
					whichSource = i;
				}
				sum += nta;
			}
			else {
				t = source.getTree(taxa, itree);

				if (t == null){
					nta = source.getNumberOfTrees(taxa, true);
					if (!MesquiteInteger.isCombinable(nta))
						return null;
					setNumberAssigned(source, nta);
					setIsNumberAssigned(source, true);
					if (itree  < nta + sum){
						t = source.getTree(taxa, itree - sum);
						whichSource = i;
					}
					sum += nta;
				}
			}
		}
		if (t == null)
			return null;
		MesquiteTree tree = new MesquiteTree(taxa);
		if (t instanceof MesquiteTree)
			tree.setToClone((MesquiteTree)t);
		else 
			tree.setToCloneFormOnly(t);
		tree.attach(new MesquiteLong("Which Source",  whichSource));
		return tree;
	}
	/*.................................................................................................................*/
	public int getNumberOfTrees(Taxa taxa) {
		setPreferredTaxa(taxa);
		resetTreeSource(taxa, false);
		int sum = 0;
		for (int i = 0; i<sources.size(); i++){
			int nta = getNumberAssigned(i);
			if (nta == MesquiteInteger.finite)
				return MesquiteInteger.finite;
			sum += nta;
		}
		return sum;
	}
	/*.................................................................................................................*/
	public String getTreeNameString(Taxa taxa, int itree) {
		Tree t = getTree(taxa, itree);
		if (t == null)
			return "NO TREE";
		return t.getName();
	}
	/*.................................................................................................................*/
	public String getParameters() {
		String s = "Trees from sources: ";
		for (int i = 0; i<sources.size(); i++){
			if (i != 0)
				s += " &";
			s += " " + ((TreeSource)sources.elementAt(i)).getNameAndParameters();
		}
		return s;
	}
	/*.................................................................................................................*/
}

