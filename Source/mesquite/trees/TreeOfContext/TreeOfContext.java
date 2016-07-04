/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.TreeOfContext;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/** Supplies a "current" tree.  It does this by looking for currently open tree contexts -- for instance, a Tree Window showing a tree.  If the tree is 
for the appropriate block of taxa, this module can returns it.  It is used by simulators of character evolution and other calculations that depend upon a single "current tree".
There is a search strategy, prefering tree contexts that are among the employers of this module, then looking later to more distantly connected contexts. */
public class TreeOfContext extends OneTreeSource implements TreeContextListener {
	TreeContext context=null;
	TreeContext oldContext=null;
	Taxa oldTaxa = null;
	MesquiteTree rememberedDefaultTree = null;
	Tree lastTree;
	String contextID = null;
	Object condition = null;
	
	//TODO: problem here with snapshotting.  This module may depend on a module in the employee tree, hired after it.  Must make sure
	//context is hire before this module
  	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
  		this.condition = condition;
  		if (!MesquiteThread.isScripting()){
	  		context = (TreeContext)findEmployerWithDuty(TreeContext.class);
	  		if (context == null)
	  			context = (TreeContext)findNearestColleagueWithDuty(TreeContext.class);
	   		if (context == null){
	   			return sorry("Sorry, " + whatIsMyPurpose()+ " you need to have an open Tree Window to serve as a source of a current tree; no appropriate Tree Window could be found.  You may request a Tree Window from the Trees menu.  The Multi Tree window is not appropriate, as it does not show a single current tree."); //TODO: should alert user
	   		}
	   		}
   		context = null; // the check on context had been just to ensure that there was something
		addMenuItem("Display Tree (used by " + getEmployer().getName() +")", makeCommand("showContext", this));
  		return true;
  	}
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true;
   	 }
 	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
 	
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
   	 	//tree context will create variable announcing its moduleID;
   	 	//this module will request the name of the variable
  	 	if (context instanceof MesquiteModule){
   			String s =((MesquiteModule)context).getPermanentIDString();
			temp.addLine("setContextID " + s);  //for tree context
  	 	}	

   	 	//temp.addLine(" " , treeSourceTask);

 	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets the context identification code of this module (this is used internally in saving scripts to ensure users of the context reconnect to the correct tree context", "[code string]", commandName, "setContextID")) {
   
    	 		contextID = arguments;
    	 		if (!MesquiteThread.isScripting())
    	 			parametersChanged();
    	 	}
    	 	else if (checker.compare(this.getClass(), "Gets the current context", null, commandName, "getContext")) {
   			return context;
    	 	}
    	 	else if (checker.compare(this.getClass(), "Shows the current context", null, commandName, "showContext")) {
	  		if (context == null)
	  			alert("Sorry, there is no record of a current tree context.");
	  		else if (context instanceof Showable)
	  			((Showable)context).showMe();
	  		else
	  			alert("Sorry, the current tree in use is not in a location that can be shown.");
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
    	private boolean contextAppropriate(TreeContext context, Taxa taxa) {
  		if (context ==null)
  			return false;
  		if (context.getTree()==null)
  			return false;
  		if (context.getTree().getTaxa() == null)
  			return false;
		if (taxa == null)
  			return false;
  		return context.getTree().getTaxa().equals(taxa);
    	}
	/*.................................................................................................................*/
  	private TreeContext findContext(Taxa taxa){
  		if (contextAppropriate(context, taxa))
  			return context;
  		ListableVector contexts = findModulesWithDuty(TreeContext.class);
  		for (int i=0; i<contexts.size(); i++) {
  			TreeContext c = (TreeContext)contexts.elementAt(i);
  			if (contextAppropriate(c, taxa)) 
  				return c;
  		}
  		return null;
  	}
	/*.................................................................................................................*/
	/* rule: first, look for context among ancestor employees. If one found, use as default. If not, look for closest.  If more than one, allow
	user to choose.  If one, use.  If more than one, allow for user to reattach to other context*/
	/*.................................................................................................................*/
  	private TreeContext queryFindContext(Taxa taxa, String explanationForUser){
  		if (taxa==null)
  			return null;
  		
  		//first, find out if current context is OK
     		if (contextAppropriate(context, taxa)) {
  			return context;
  		}
  			
  		//First, gather info about all TreeContexts
 		ListableVector contexts = findModulesWithDuty(TreeContext.class);
 
 		if (contexts==null)
 			return null;
		for (int i = contexts.size()-1; i>=0; i--){
  			TreeContext c = (TreeContext)contexts.elementAt(i);
  			if (!contextAppropriate(c, taxa)) 
  				contexts.removeElement(c, false);
		}


 		TreeContext employerContext = null;
  		// otherwise, check among ancestors
  		if (condition == null || !(condition instanceof MesquiteBoolean && !((MesquiteBoolean)condition).getValue())){
  			MesquiteModule mb = getEmployer();
  		while (mb !=null && !(mb instanceof TreeContext))
  			mb = mb.getEmployer();
  		if (mb!=null && mb instanceof TreeContext && contextAppropriate((TreeContext)mb, taxa)) {
  			employerContext = (TreeContext)mb;
  		}
  		}
  		/**/
  			
  		if (contexts.size()==1) { //just one context found
  			TreeContext context = (TreeContext)contexts.elementAt(0);
  			if (!contextAppropriate(context, taxa))
  				return null;
  			if (!MesquiteThread.isScripting()) {
  				String s = "The current tree (for " + employer.getName() + ") will be obtained from ";
  				if (context instanceof MesquiteModule)
  					s+="the window " + ((MesquiteModule)context).containerOfModule().getName();
  				else if (context instanceof Listable)
  					s+= context.getName();
  				if (StringUtil.notEmpty(explanationForUser))
  					s+="\n "+explanationForUser;
				alert(s);
  			}
  			return context;
  			
  		}
  		else if (contexts.size()==0) //no contexts found
  			return null;
  		else if (MesquiteThread.isScripting()){ //if scripting use employer context or first one found
  			if (employerContext!=null)
	  			context = employerContext;
			else
	  			context = (TreeContext)contexts.elementAt(0);
  			return context;
  		}
  		else { //more than one found, not scripting
			if (employerContext!=null)
	  			context = employerContext;
			else {
	  			context = null;
	  			int i = 0;
	  			while (i<contexts.size() && context == null){
	  				EmployerEmployee tc = (EmployerEmployee)contexts.elementAt(i);
	  				if (!isEmployerOrHigher(tc)){
	  					context = (TreeContext)tc;
	  				}
	  				i++;
	  			}
	  			if (context == null)
		  			context = (TreeContext)contexts.elementAt(0);
			}
	  		if (context instanceof Showable)
	  			((Showable)context).showMe();
	  		String s = "The current tree (for " + employer.getName() + ") will be obtained from the window " + ((MesquiteModule)context).containerOfModule().getName();
	  		if (StringUtil.notEmpty(explanationForUser))
	  			s+="\n\n("+explanationForUser+")";
	  		s+= "\n\nIs this OK?";
			if (AlertDialog.query(containerOfModule(), "Query", s, "Yes", "No"))
	  			return context;
			
			context = null;


			Context[] cx = new Context[contexts.size()];
			for (int i=0; i<contexts.size(); i++){
	  			cx[i] = (Context)contexts.elementAt(i);
			}
  			int chosen = ListDialog.queryList(containerOfModule(), "Choose tree context", "Choose the tree context from which to obtain the current tree (for " + employer.getName() + ")", MesquiteString.helpString,cx, 0, true);
  			if (MesquiteInteger.isNonNegative(chosen)) {
  				context =  (TreeContext)contexts.elementAt(chosen);
  				return context;
  			}
  			else
  				return null;
  		}
  		
  		//CONTEXTNAME should be used here
  	}
	/*.................................................................................................................*/
  	public void setPreferredTaxa(Taxa taxa){
  	}
  	
  	public MesquiteModule getUltimateSource(){
  		if (context == null)
  			return null;
  		return context.getTreeSource();
  	}
	/*.................................................................................................................*/
	/** For TreeContextListener */
	public void treeChanged(Tree tree) { 
		parametersChanged(new Notification());
	}
	/*.................................................................................................................*/
	/** For TreeContextListener */
	public void disposing(TreeContext context) {
		if (this.context == context) {
			context.removeTreeContextListener(this);
			rememberedDefaultTree = null;
			lastTree = null;
			this.context = null;
			oldContext = null;
			parametersChanged();
		}
	}
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Taxa taxa){
   		if (taxa==null)
   			return;
   		context = queryFindContext(taxa, null);
   	}
  	 /** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress).  This version allows one to supply an explanation to the user*/
  	 public void initialize(Taxa taxa, String explanationForUser){
    		if (taxa==null)
       			return;
       		context = queryFindContext(taxa, explanationForUser);
  	 }

	public void broadCastAssignedID(MesquiteModule module, String assignedID){
  		if (contextID !=null && contextID.equals(assignedID))
  			parametersChanged();
	}
   	boolean warned = false;
	/*.................................................................................................................*/
	/* rule: first, look for context among ancestor employees. If one found, use as default. If not, look for closest.  If more than one, allow
	user to choose.  If one, use.  If more than one, allow for user to reattach to other context*/
   	public Tree getTree(Taxa taxa) {
   		return getTree(taxa, null);
   	}
	/*.................................................................................................................*/
	/* rule: first, look for context among ancestor employees. If one found, use as default. If not, look for closest.  If more than one, allow
	user to choose.  If one, use.  If more than one, allow for user to reattach to other context*/
   	public Tree getTree(Taxa taxa, String explanationForUser) {
   		if (taxa==null)
   			return null;
   		if (doomed)
   			return null;
   		
   		//if context is supplying tree with other Taxa, need to look for other contexts!
  		if (contextID !=null) {
  			/*try to find id'd context (looking through modules for their assignedIDString.
  			if found, set to context, and set contextID to null
  			if not, return null; */
  			MesquiteModule mb = getFileCoordinator().findEmployeeWithPermanentID(contextID);
  			if (mb == null) {
  				context= null;
  				return null;
  			}
  			else if (mb instanceof TreeContext) {
  				context = (TreeContext)mb;
  				contextID = null;
  			}
  		}
   		context = queryFindContext(taxa, explanationForUser);
 		if (context == null) {
			if (taxa != oldTaxa || rememberedDefaultTree == null)
				rememberedDefaultTree = taxa.getDefaultDichotomousTree(rememberedDefaultTree);
			oldTaxa = taxa;
	   		if (context instanceof MesquiteModule)
	   			deferBranchPriority((MesquiteModule)context);
	   		if (!MesquiteThread.isScripting() && !warned){
	   			alert("No suitable current tree was found (for " + employer.getName() + ").  A tree window needs to be open to supply a current tree.  There may be no tree window available for this set of taxa, or the tree in an available window may not be usable for this purpose.  This module may have to quit, and you may have to choose an alternative.");
	   			warned = true;
	   			iQuit();
	   			return null;
	   		}
	   		lastTree =rememberedDefaultTree;
			return rememberedDefaultTree;
		}
		else if (context != oldContext) {
			if (oldContext !=null)
				oldContext.removeTreeContextListener(this);
	   		context.addTreeContextListener(this);
	   		if (context instanceof MesquiteModule)
	   			deferBranchPriority((MesquiteModule)context);
	   	}
		oldContext = context;
   		Tree tree = context.getTree();
   		oldTaxa = taxa;
   		lastTree = tree;
		if (taxa == null) 
			return tree;
		else if (tree != null && taxa.equals(tree.getTaxa(), false)) 
			return tree;
		else {
   			lastTree = taxa.getDefaultTree();
	   		if (!MesquiteThread.isScripting() && !warned){
	   			alert("No suitable current tree was found (for " + employer.getName() + ").  A default tree will be used instead.");
	   			warned = true;
	   		}
			return lastTree;
		}
   	}
	/*.................................................................................................................*/
	public void endJob() {
		if (context!=null)
			context.removeTreeContextListener(this);
		super.endJob();
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Tree of context";
   	 }
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
  	 public String getParameters() {
		String s = null;
		if (context == null)
			s = "No tree context found; default trees used.";
		else
			s = "Tree(s) used from " + context.getContextName() + ".";  
		 if (lastTree!=null)
		 	s+= " Last tree used: " + lastTree.getName() +  "  [tree: " + lastTree.writeTree() + "] ";
		return s;
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Supplies a single tree from the nearest tree context (e.g., an available tree window).";
   	 }
   	 
}

