/* Mesquite (package mesquite.ornamental).  Copyright 1997-2011 W. Maddison and D. Maddison. 
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.tol.TaxonOnWebDB;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.tol.lib.*;

/* ======================================================================== */
public class TaxonOnWebDB extends TreeDisplayAssistantI {
	public Vector extras;
	TaxonOnWebServer URLTask;
	TaxonOnWebDBToolExtra extra;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		extras = new Vector();
		if (arguments ==null)
			URLTask = (TaxonOnWebServer)hireNamedEmployee(TaxonOnWebServer.class, "#ToLURLServer");
		else {
			URLTask = (TaxonOnWebServer)hireNamedEmployee(TaxonOnWebServer.class, arguments);
			if (URLTask == null)
				URLTask = (TaxonOnWebServer)hireEmployee(TaxonOnWebServer.class, "Taxon on Web URL Provider");
		}
		if (URLTask == null) {
			return sorry(getName() + " couldn't start because providing of server URL could not be obtained.");
		}
		
		loadPreferences();

		return true;
	} 
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		TaxonOnWebDBToolExtra newPj = new TaxonOnWebDBToolExtra(this, treeDisplay);
		extras.addElement(newPj);
		extra = newPj;
		return newPj;
	}
	/*.................................................................................................................*/
	public TaxonOnWebServer getURLTask() {
		return URLTask;
	}
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "urlTask", "#" + URLTask.getClass().getName());
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("urlTask".equalsIgnoreCase(tag)) {
			String URLTaskName = StringUtil.cleanXMLEscapeCharacters(content);
			URLTask = (TaxonOnWebServer)hireNamedEmployee(TaxonOnWebServer.class, URLTaskName);
		}
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 

		 if (checker.compare(this.getClass(), "Set the URL task", "[module name]", commandName, "setURLTask")) {
			 TaxonOnWebServer temp=  (TaxonOnWebServer)replaceEmployee(TaxonOnWebServer.class, arguments, "URL Provider", URLTask);
			if (temp!=null)
				URLTask= temp;
			if (extra!=null && extra.getTreeTool()!=null)
				extra.getTreeTool().setEnabled(URLTask!=null);
			storePreferences();

			return URLTask;
		}
		else if (checker.compare(this.getClass(), "Present the popup menu to select options for search web tool", null, commandName, "taxonOnWebToolOptions")) {
			if (extra==null || extra.getTreeTool()==null)
				return null;
			MesquiteButton button = extra.getTreeTool().getButton();
			if (button!=null){
				MesquiteInteger io = new MesquiteInteger(0);
				int x= MesquiteInteger.fromString(arguments, io); //getting x and y from arguments
				int y= MesquiteInteger.fromString(arguments, io);
				MesquitePopup popup = new MesquitePopup(button);

				Listable[] moduleList = MesquiteTrunk.mesquiteModulesInfoVector.getModulesOfDuty(TaxonOnWebServer.class, null, this);
				for (int i=0; i<moduleList.length; i++) {
					MesquiteModuleInfo m = (MesquiteModuleInfo)moduleList[i];
					MesquiteCommand mc = makeCommand("setURLTask", this);
					mc.setDefaultArguments(StringUtil.tokenize(m.getName()));
					MesquiteCheckMenuItem mItem = new MesquiteCheckMenuItem(m.getName(), this, mc, null, null);
					if (URLTask!=null)
						mItem.set(URLTask.getName().equalsIgnoreCase(m.getName()));
					popup.add(mItem);
				}
				popup.showPopup(x,y+6);
			}

		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Get Taxon's tree from a web site";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Supplies a tool for tree windows that gets tree for taxon touched from a web site." ;
	}
	public boolean isSubstantive(){
		return false;
	}   	 
}

/* ======================================================================== */
class TaxonOnWebDBToolExtra extends TreeDisplayExtra implements Commandable  {
	TreeTool taxonOnWebTool;
	TaxonOnWebDB taxonOnWebModule;
	MesquiteCommand taxonCommand;
	public TaxonOnWebDBToolExtra (TaxonOnWebDB ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		taxonOnWebModule = ownerModule;
		taxonCommand = MesquiteModule.makeCommand("goToTaxonOnWeb",  this);
		enableTool();
	}
	public void enableTool(){
		if (taxonOnWebTool == null){
			taxonOnWebTool = new TreeTool(this, "goToTaxonOnWeb", ownerModule.getPath(), "TaxonOnWeb.gif", 4,0,"Go to Taxon on Web Site", "This tool searches for the taxon touched on a web site."); //; hold down shift to enter a URL
			taxonOnWebTool.setTouchedTaxonCommand(taxonCommand);
			taxonOnWebTool.setPopUpOwner(taxonOnWebModule);
			taxonOnWebTool.setOptionsCommand(MesquiteModule.makeCommand("taxonOnWebToolOptions", taxonOnWebModule));

			if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
				((MesquiteWindow)ownerModule.containerOfModule()).addTool(taxonOnWebTool);
			}
		}
	}
	/*.................................................................................................................*/
	public   TreeTool getTreeTool(){
		return taxonOnWebTool;
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
	}


	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public void goToPage(String arguments) {   // only do this if has page!
		Tree tree = treeDisplay.getTree();
		int M = MesquiteInteger.fromFirstToken(arguments, pos);
		if (M<0 || !MesquiteInteger.isCombinable(M) || M>=tree.getTaxa().getNumTaxa())
			return;
		String s = "";
		if (taxonOnWebModule.getURLTask()!=null)
			s = taxonOnWebModule.getURLTask().getURL(tree.getTaxa().getTaxonName(M));
		if (!StringUtil.blank(s))
			MesquiteModule.showWebPage(s, true);
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 

		if (checker.compare(this.getClass(), "Gets tree for that taxon from ToLweb.org", "[taxon number][modifiers]", commandName, "goToTaxonOnWeb")) {
			goToPage(arguments);
		}
		return null;
	}
	public void turnOff() {
		taxonOnWebModule.extras.removeElement(this);
		super.turnOff();
	}
}


