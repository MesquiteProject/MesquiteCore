/* Mesquite source code.  Copyright 1997-2008 W. Maddison and D. Maddison.
Version 2.5, June 2008.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.SearchTreeToolAssistant; 

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.trees.lib.*;

import java.lang.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;


	/* ======================================================================== */
	public class SearchTreeToolAssistant extends TreeDisplayAssistantI {
		TreeSwapper swapTask;
		NumberForTree numberTask;
		MesquiteString swapTaskName;
		MesquiteCommand swapC;
		MesquiteString numberTaskName;
		MesquiteCommand numberC;
		MesquiteBoolean smallerIsBetter = new MesquiteBoolean(true);
		MesquiteBoolean liveUpdates = new MesquiteBoolean(true);
		RandomBetween rng = new RandomBetween(System.currentTimeMillis());
		MesquiteSubmenuSpec treeSwapperSubmenu;
		MesquiteSubmenuSpec numberTaskSubmenu;

		public Vector extras;
		public String getFunctionIconPath(){
			return getPath() + "searchTree.gif";
		}
		/*.................................................................................................................*/
		public boolean startJob(String arguments, Object condition, boolean hiredByName){
			setUseMenubar(false); //menu available by touching oning button
			addCheckMenuItem(null, "Minimize Criterion", makeCommand("smallerIsBetter",  this), smallerIsBetter);
			addCheckMenuItem(null, "Live Updates", makeCommand("liveUpdates",  this), liveUpdates);
			swapTaskName = new MesquiteString();

			
			// TODO: have better protection here.
			
			MesquiteCommand command = makeCommand("saveLastNumberToFile",  this);		
			swapC = makeCommand("setSwapTask",  this);
			if (numModulesAvailable(TreeSwapper.class)>1) {
				 treeSwapperSubmenu = addSubmenu(null, "Tree Swapper", swapC, TreeSwapper.class);
			}
			
			numberC = makeCommand("setNumberTask",  this);
			if (numModulesAvailable(NumberForTree.class)>1) {
				numberTaskSubmenu = addSubmenu(null, "Criterion", numberC, NumberForTree.class);
			}
			
			extras = new Vector();
			return true;
		} 
		
		/*.................................................................................................................*/
		public boolean checkEmployeesAreHired(){
			if (swapTask==null) {
				swapTask = (TreeSwapper)hireEmployee(TreeSwapper.class, "Tree Rearranger");		
				if (swapTask==null)
					return sorry(getName() + " couldn't search because no tree rearranging module was obtained");
				swapTaskName.setValue(swapTask.getName());
				swapTask.setHiringCommand(swapC);
				treeSwapperSubmenu.setSelected(swapTaskName);
			}

			if (numberTask==null){
				numberTask = (NumberForTree)hireEmployee(NumberForTree.class, "Criterion used to judge trees");
				if (numberTask == null)
					return sorry(getName() + " couldn't search because no calculator module obtained.");
				numberTaskName = new MesquiteString(numberTask.getName());
				numberC = makeCommand("setNumberTask",  this);
				numberTask.setHiringCommand(numberC);
				numberTaskSubmenu.setSelected(numberTaskName);
			}

			return true;
		}
		/*.................................................................................................................*/
		public Object doCommand(String commandName, String arguments, CommandChecker checker) {
			if (checker.compare(this.getClass(), "Sets the objection function that calculates a value for each tree.", "[name of module calculating value]", commandName, "setNumberTask")) {
				NumberForTree temp = (NumberForTree)replaceEmployee(NumberForTree.class, arguments, "Statistic to calculate for tree", numberTask);
				if (temp !=null){
					numberTask = temp;
					numberTask.setHiringCommand(numberC);
					//numberTask.initialize(tree, charStates);
					if (numberTaskName==null)
						numberTaskName = new MesquiteString(numberTask.getName());
					else
						numberTaskName.setValue(numberTask.getName());
					parametersChanged();
					return numberTask;
				}
			} else if (checker.compare(this.getClass(), "Sets the method of rearrangement of a tree.", "[name of module rearranging tree]", commandName, "setSwapTask")) {
				TreeSwapper temp = (TreeSwapper)replaceEmployee(TreeSwapper.class, arguments, "Rearranger of tree", swapTask);
				if (temp !=null){
					swapTask = temp;
					swapTask.setHiringCommand(swapC);
					//numberTask.initialize(tree, charStates);
					swapTaskName.setValue(swapTask.getName());
					parametersChanged();
					return swapTask;
				}
			} else if (checker.compare(this.getClass(), "Sets whether the search considers tree with smaller values better", "[on off]", commandName, "smallerIsBetter")) {
				boolean current = smallerIsBetter.getValue();
				smallerIsBetter.toggleValue(parser.getFirstToken(arguments));
			}
			else if (checker.compare(this.getClass(), "Sets whether tree rearrangements are visible as they happen and whether or not other things that depend upon the tree (e.g., charts, legends) are recalculated as rearrangements happen.", "[on off]", commandName, "liveUpdates")) {
				boolean current = liveUpdates.getValue();
				liveUpdates.toggleValue(parser.getFirstToken(arguments));
			}
			else
				return  super.doCommand(commandName, arguments, checker);
			return null;
		}
		/*.................................................................................................................*/
		public TreeSwapper getTreeSwapper() {
			return swapTask;
		}
		/*.................................................................................................................*/
		public NumberForTree getNumberTask() {
				return numberTask;
		}
		/*.................................................................................................................*/
		public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
			SearchTreeToolExtra newPj = new SearchTreeToolExtra(this, treeDisplay);
			extras.addElement(newPj);
			return newPj;
		}
		/*.................................................................................................................*/
		public String getName() {
			return "Search within clade";
		}

		/*.................................................................................................................*/
		/** returns an explanation of what the module does.*/
		public String getExplanation() {
			return "Supplies a tool for tree windows that searches for a better branch arrangement within clade touched." ;
		}
		public boolean isSubstantive(){
			return false;
		}   	 
		public boolean isPrerelease(){
			return true;
		}   	 
		public boolean getSmallerIsBetter(){
			return smallerIsBetter.getValue();
		}
		public boolean getLiveUpdates(){
			return liveUpdates.getValue();
		}
		/*.................................................................................................................*/
		/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
		 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
		 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
		public int getVersionOfFirstRelease(){
			return 201;  
		}

	}

	/* ======================================================================== */
	class SearchTreeToolExtra extends TreeDisplayExtra implements Commandable  {
		TreeTool searchTreeTool;
		SearchTreeToolAssistant ownerModule;
		MesquiteCommand searchTreeCommand;
		AdjustableTree tree = null;
		RandomBetween rng = new RandomBetween(System.currentTimeMillis());
		TreeOptimizer treeOptimizer;

		public SearchTreeToolExtra (SearchTreeToolAssistant ownerModule, TreeDisplay treeDisplay) {
			super(ownerModule, treeDisplay);
			this.ownerModule = ownerModule;
			searchTreeCommand = MesquiteModule.makeCommand("searchTree",  this);
			searchTreeTool = new TreeTool(this, "randomlyRotate", ownerModule.getPath(),"searchTree.gif", 4,14,"Search within clade", "This tool is used to search for a better branch arrangement within the clade touched.");
			searchTreeTool.setTouchedCommand(searchTreeCommand);
			if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
				((MesquiteWindow)ownerModule.containerOfModule()).addTool(searchTreeTool);
				searchTreeTool.setPopUpOwner(ownerModule);
			}
			treeOptimizer =new TreeOptimizer(ownerModule,  ownerModule.getNumberTask(),ownerModule.getTreeSwapper());
		}
		/*.................................................................................................................*/
		public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		}

		/*.................................................................................................................*/
		public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
			drawOnTree(tree, drawnRoot, g);
		}
		/*.................................................................................................................*/
		public   void setTree(Tree tree) {
			if (tree instanceof AdjustableTree)
				this.tree = (AdjustableTree)tree;
			else this.tree =null;
		}
		MesquiteInteger pos = new MesquiteInteger();
		/*.................................................................................................................*/
		public Object doCommand(String commandName, String arguments, CommandChecker checker) { 

			if (checker.compare(this.getClass(), "Searches for rearrangements in the clade that yield a better tree as judged by the objective function values.", "[branch number]", commandName, "searchTree")) {
				if (!ownerModule.checkEmployeesAreHired())
					return null;
				
				pos.setValue(0);
				
				int branchFound= MesquiteInteger.fromString(arguments,pos);
				MesquiteString resultString = new MesquiteString();
				treeOptimizer.setSwapTask(ownerModule.getTreeSwapper());
				treeOptimizer.setNumberTask(ownerModule.getNumberTask());
				treeOptimizer.setBiggerIsBetter(!ownerModule.getSmallerIsBetter());
				treeOptimizer.setLiveUpdates(ownerModule.getLiveUpdates());
				treeOptimizer.setNotify(true);
				treeOptimizer.searchForBetterTree(tree, branchFound, rng, resultString);

				//	TreeSearchUtil.searchForBetterTree(ownerModule,  tree,  branchFound, ownerModule.getTreeSwapper(),  ownerModule.getNumberTask(),  rng,  resultString,  ownerModule.getSmallerIsBetter(),  ownerModule.getLiveUpdates(),  true);
			//	TreeSearchUtil.searchForBetterTree(ownerModule,  tree,  branchFound, 30000,false,false,ownerModule.getTreeSwapper(),  ownerModule.getNumberTask(),  rng,  resultString,  ownerModule.getSmallerIsBetter(),  ownerModule.getLiveUpdates(),  true,true,false);

			}
			return null;
		}
		public void turnOff() {
			ownerModule.extras.removeElement(this);
			super.turnOff();
		}
		

	}


