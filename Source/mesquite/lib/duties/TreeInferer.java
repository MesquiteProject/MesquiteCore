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
package mesquite.lib.duties;

import mesquite.lib.*;
import mesquite.trees.lib.*;


/* ======================================================================== */
/**Supplies trees (compare to OneTreeSource), for instance from a file or simulated.  Most modules
are subclasses of the subclass TreeSource*/

public abstract class TreeInferer extends TreeBlockFiller  {
	Listened listened;
	TWindowMaker tWindowMaker;
	public Class getDutyClass() {
		return TreeInferer.class;
	}
	public String getDutyName() {
		return "Tree Inferer";
	}
	public String[] getDefaultModule() {
		return null;
	}
	public boolean canGiveIntermediateResults(){
		return false;
	}
	public Tree getLatestTree(Taxa taxa, MesquiteNumber score, MesquiteString titleForWindow){
		if (score != null)
			score.setToUnassigned();
		return null;
	}
	public void registerListener(MesquiteListener listener){
		if (listened == null)
			listened = new Listened();
		listened.addListener(listener);
	}
	public void deregisterListener(MesquiteListener listener){
		if (listened == null)
			return;
		listened.removeListener(listener);
	}
	protected void newResultsAvailable(TaxaSelectionSet outgroupSet){
		MesquiteString title = new MesquiteString();
		Tree tree = getLatestTree(null, null, title);
		if (tree instanceof AdjustableTree) {
			((AdjustableTree)tree).standardize(outgroupSet, false);
		}
		showIntermediatesWindow();
		if (tree != null && tWindowMaker != null){ 
			tWindowMaker.setTree(tree);
			MesquiteWindow w = tWindowMaker.getModuleWindow();
			if (title.isBlank() && w != null && w instanceof SimpleTreeWindow)
				((SimpleTreeWindow)w).setWindowTitle(title.getValue());
			String commands = getExtraIntermediateTreeWindowCommands();
			if (!StringUtil.blank(commands)) {
				if (w != null){
					Puppeteer p = new Puppeteer(this);
					p.execute(w, commands, new MesquiteInteger(0), "", false);
				}
			}


		}
		if (listened != null)
			listened.notifyListeners(this, new Notification(MesquiteListener.NEW_RESULTS));
	}
	public void showIntermediatesWindow(){
		if (tWindowMaker == null) {
			tWindowMaker = (TWindowMaker)hireNamedEmployee(TWindowMaker.class, "#ObedientTreeWindow");
			String commands = getExtraTreeWindowCommands();
			MesquiteWindow w = tWindowMaker.getModuleWindow();
			
			if (w != null){
				if (w instanceof SimpleTreeWindow)
					((SimpleTreeWindow)w).setWindowTitle("Most Recent Tree");
				Puppeteer p = new Puppeteer(this);
				p.execute(w, commands, new MesquiteInteger(0), "end;", false);
			}
		}
		if (tWindowMaker != null)
			tWindowMaker.setWindowVisible(true);
		
	}
}


