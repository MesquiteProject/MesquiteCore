/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison. 
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.tol.lib;

import java.awt.Graphics;

import mesquite.lib.CommandChecker;
import mesquite.lib.Commandable;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteWindow;
import mesquite.lib.NameReference;
import mesquite.lib.ParseUtil;
import mesquite.lib.Puppeteer;
import mesquite.lib.Taxa;
import mesquite.lib.Tree;
import mesquite.lib.TreeDisplay;
import mesquite.lib.TreeDisplayExtra;
import mesquite.lib.TreeTool;
import mesquite.tol.lib.*;

public abstract class BaseSearchToLToolTaxonExtra  extends TreeDisplayExtra implements Commandable  {
		TreeTool tolTool;
		BaseSearchToLTaxon taxonToLModule;
		MesquiteCommand taxonCommand;
		NameReference tolLeavesNameRef = NameReference.getNameReference("ToLLeaves");
		NameReference tolHasChildrenNameRef = NameReference.getNameReference("ToLHasChildren");
		public BaseSearchToLToolTaxonExtra (BaseSearchToLTaxon ownerModule, TreeDisplay treeDisplay) {
			super(ownerModule, treeDisplay);
			taxonToLModule = ownerModule;
			taxonCommand = MesquiteModule.makeCommand("goToToLTaxon",  this);
		}
		public void enableTool(){
			if (tolTool == null){
				tolTool = new TreeTool(this, getToolScriptName(), ownerModule.getPath(), "ToL.gif", 4,0,getToolName(), getToolExplanation()); //; hold down shift to enter a URL
				tolTool.setTouchedTaxonCommand(taxonCommand);

				if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
					((MesquiteWindow)ownerModule.containerOfModule()).addTool(tolTool);
				}
			}
		}
		/*.................................................................................................................*/
		public abstract String getToolName() ;
		/*.................................................................................................................*/
		public abstract String getToolScriptName() ;
		/*.................................................................................................................*/
		public abstract String getToolExplanation() ;
		/*.................................................................................................................*/
		public abstract String getBaseURL() ;
		/*.................................................................................................................*/
		public abstract String getGetToLTreeModuleName() ;
		/*.................................................................................................................*/
		public abstract String getBaseURLForUser() ;
		/*.................................................................................................................*/
		public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		}

		/*.................................................................................................................*/
		public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
			drawOnTree(tree, drawnRoot, g);
		}
		/*.................................................................................................................*/
		public   void setTree(Tree tree) {
		}
		MesquiteInteger pos = new MesquiteInteger();
		/*.................................................................................................................*/
		public boolean hasDescendants(Taxa taxa, int taxonNumber){
			boolean children = true;
			MesquiteBoolean n = (MesquiteBoolean)taxa.getAssociatedObject(tolHasChildrenNameRef, taxonNumber);
			if (n !=null)
				children=n.getValue();
			return children;   // test if it is a leaf etc. 
		}
		/*.................................................................................................................*/
		public void goToToLPage(String arguments) {   // only do this if has page!
			Tree tree = treeDisplay.getTree();
			int M = MesquiteInteger.fromFirstToken(arguments, pos);
			if (M<0 || !MesquiteInteger.isCombinable(M) || M>=tree.getTaxa().getNumTaxa())
				return;
			String openName = ParseUtil.tokenize(tree.getTaxa().getTaxonName(M));
			String s = "http://" +getBaseURL() + "/" + openName;
			MesquiteModule.showWebPage(s, true);
		}
		/*.................................................................................................................*/
		public Object doCommand(String commandName, String arguments, CommandChecker checker) { 

			if (checker.compare(this.getClass(), "Gets tree for that taxon from "+getBaseURLForUser(), "[taxon number][modifiers]", commandName, "goToToLTaxon")) {
				Tree tree = treeDisplay.getTree();
				int M = MesquiteInteger.fromFirstToken(arguments, pos);
				if (M<0 || !MesquiteInteger.isCombinable(M) || M>=tree.getTaxa().getNumTaxa())
					return null;
				if (!hasDescendants(tree.getTaxa(), M)) 
					return null;
				while (ownerModule.getProject().developing)
					;
				String openName = ParseUtil.tokenize(tree.getTaxa().getTaxonName(M));
				String commands = "newThread; getProjectID; Integer.id *It; tell Mesquite; getWindowAutoShow; String.was *It; windowAutoShow off; closeProjectByID *Integer.id; openGeneral #" + getGetToLTreeModuleName() +" " + openName;
				commands +=  "; ifNotExists It;  showAbout; endIf; windowAutoShow *String.was; endTell;";

				//String commands = "newThread; getProjectID; Integer.id *It; tell Mesquite; getWindowAutoShow; String.was *It; windowAutoShow off; closeProjectByID *Integer.id; " + openCommand;
				//commands +=  StringUtil.tokenize(MesquiteFile.composePath(getProject().getHomeDirectoryName(), nextProjectName)) + "; ifNotExists It;  debug; showAbout; endIf; windowAutoShow *String.was; endTell;";


				Puppeteer p = new Puppeteer(ownerModule);
				MesquiteInteger pos = new MesquiteInteger(0);
				p.execute(ownerModule.getFileCoordinator(), commands, pos, "", false);
				ownerModule.iQuit();
			}
			return null;
		}
		public void turnOff() {
			taxonToLModule.extras.removeElement(this);
			super.turnOff();
		}
	}
