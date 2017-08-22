/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.duties;

import java.awt.*;

import mesquite.lib.*;


/* ======================================================================== */
/**Supplies trees (compare to OneTreeSource), for instance from a file or simulated.  Most modules
are subclasses of the subclass TreeSource*/

public abstract class TreeBlockFiller extends MesquiteModule   {
   	 public Class getDutyClass() {
   	 	return TreeBlockFiller.class;
   	 }
 	public String getDutyName() {
 		return "Tree Block Filler";
   	 }
	 public String getFunctionIconPath(){
   		 return getRootImageDirectoryPath() + "functionIcons/treeSource.gif";
   	 }
   	 
	 public boolean permitSeparateThreadWhenFilling(){
		 return true;
	 }
   	 public String[] getDefaultModule() {
   	 	return new String[] {"#SimulateTree", "#RandomModifTree", "#StoredTrees"};
   	 }
   	 
    /** TreeBlockFillers should override this if they want special commands to be sent to a tree window if a tree window is created after they are used. */
  	 public String getExtraTreeWindowCommands (boolean finalTree){
   		 return "";
   	 }
  	 
     /** TreeBlockFillers should override this if they want special commands to be sent to a tree window if a tree window is created after they are used. */
  	 public String getExtraIntermediateTreeWindowCommands (){
   		 return "";
   	 }

   	 
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public abstract void initialize(Taxa taxa);

   	public Reconnectable getReconnectable(){
   		return null;
   	}
   	
   	 /** Returns whether there is a limited (e.g. stored trees) or unlimited (e.g., simulated trees) number of trees available.
   	 If this is a TreeSource, this method checks the getNumberOfTrees method.  Otherwise, the module should
   	 override it.*/
   	 public boolean hasLimitedTrees(Taxa taxa){
  		if (taxa == null)
  			return true;
  		if (this instanceof TreeSource) {
  			TreeSource ts = (TreeSource)this;
  			int numTrees = ts.getNumberOfTrees(taxa);
  			return (numTrees>=0 && MesquiteInteger.isFinite(numTrees));
  		}
  		else 
  			return false;
   	 }


   	 /** Fills the passed tree block with trees.  The parameter numberIfUnlimited indicates how many trees
   	 are to be filled if the source offers an unlimited number (e.g., if simulated trees).  This method by default calls
   	 the methods of TreeSource if this object is of the TreeSource subclass.  If this is not a TreeSource, 
   	 this method should be overridden*/
  	public void fillTreeBlock(TreeVector treeList, int numberIfUnlimited, boolean verbose){
  		if (treeList==null || abort)
  			return;
  		if (this instanceof TreeSource && !abort) {
  			Taxa taxa = treeList.getTaxa();
  			TreeSource ts = (TreeSource)this;
  			int numTrees = ts.getNumberOfTrees(taxa);
  			if (numTrees<=0 || !MesquiteInteger.isFinite(numTrees)) 
  				numTrees = numberIfUnlimited;
  			if (numTrees<=0 || !MesquiteInteger.isFinite(numTrees))
  				return;
  			Tree tree = ts.getTree(taxa, 0);
  			if (tree!=null) {
  				treeList.addElement(tree.cloneTree(), false);
  			}
			if (verbose) 
				logln("Trees about to be made by " + getName());
  			for(int i=1; i<numTrees && tree != null  && !isDoomed() && !abort; i++) {
  				tree = ts.getTree(taxa, i);
  				if (tree!=null)  {
  					CommandRecord.tick("Adding tree to trees block " + i);
  					treeList.addElement(tree.cloneTree(), false);
					if (i%100 == 0)
						logln("  trees added: " + i);
  				}
  			}
  			treeList.setName(getName());
			treeList.setAnnotation ("Parameters: "  + getParameters(), false);

  		}
  		abort = false;
  	}
  	
	 /** Fills the passed tree block with trees.  The parameter numberIfUnlimited indicates how many trees
	 are to be filled if the source offers an unlimited number (e.g., if simulated trees).  This method by default calls
	 the methods of TreeSource if this object is of the TreeSource subclass.  If this is not a TreeSource, 
	 this method should be overridden*/
	public void fillTreeBlock(TreeVector treeList, int numberIfUnlimited){
		fillTreeBlock(treeList,numberIfUnlimited, false);
	}


	public void retrieveTreeBlock(TreeVector treeList, int numberIfUnlimited){
		retrieveTreeBlock(treeList,numberIfUnlimited);
	}

	protected boolean abort = false;
	public void abortFilling(){
		abort = true;
	}
	


  }


	



