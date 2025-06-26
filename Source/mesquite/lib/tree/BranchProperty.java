/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib.tree;

import mesquite.lib.Associable;
import mesquite.lib.ListableVector;
import mesquite.lib.NameReference;
import mesquite.lib.PropertyRecord;

/* ======================================================================== */
/*A specialized version of PropertyRecord for trees, especially for tree branches/nodes*/
public class BranchProperty extends PropertyRecord  {

	protected boolean belongsToBranch = true; //This is settable only from the settings managed by BranchPropertiesInit.
	public static final String branchNodeExplanation = "Some properties for branches/nodes imply a direction of time (polarized), others do not. Unpolarized properties, like branch length, stay the same however you reroot the tree. Those are labelled as being attached to the branch itself."
			+" Polarized properties, like node height, assume that time proceeded forward along the branch toward the node. Those are labelled as \"Polarized node\". "
			+"\n\nThis is important when the tree is rerooted. A branch property that is unpolarized may appear to flip to a different node on rerooting, "
			+" while a property assigned to a \"polarized node\" will appear to flip to a different branch on rerooting. Also, rerooting may generate a contradiction if it turns polarized properties upside-down."
			+"\n\nFor instance, branch length, color, and clade confidence measures should belong to the branch, but others like heights and node labels should belong the polarized node.";

	
	/*This vector records settings in Mesquite_Folder/settings/trees/BranchPropertiesInit regarding branch properties.
	 * This is read by BranchPropertiesInit, and cannot be changed at runtime. It cannot be overridden either.*/
	public static ListableVector branchPropertiesSettingsVector = new ListableVector(); 

	//The storage points for branch properties are:
	// BranchProperty.branchPropertiesSettingsVector: static, records settings in Mesquite_Folder/settings/trees/BranchPropertiesInit regarding branch properties (e.g. default kinds, betweenness)
	// DisplayableBranchProperty.branchPropertyDisplayPreferences: static, records the display preferences of branch properties
	// MesquiteProject.knownBranchProperties: instance, the properties known by the project. For interface; not saved to file.
	// The module BranchPropertiesInit is the primary manager

	//as of 4.0, this isn't instantiated at this level, but only as DisplayableBranchProperty
	public BranchProperty(String name,int kind){
		super(name, kind);
	}
	//as of 4.0, this isn't instantiated at this level, but only as DisplayableBranchProperty
	public BranchProperty(NameReference nr,int kind){
		super(nr, kind);
	}
	public static int preferredKind(String name){
		PropertyRecord[] props = findInBranchPropertySettings(name);
		if (props == null || props.length==0 || props.length>1)
			return -1;
		return props[0].kind;
	}
	/*-------------------------------------*/
	//Associables assume not between, except MesquiteTree, which assumes between as default (therefore overridden in BranchProperty)
	public static boolean preferredBetweenness(String name){
		PropertyRecord[] props = findInBranchPropertySettings(name);
		if (props == null || props.length==0 || props.length>1)
			return true;
		if (props[0] instanceof BranchProperty)
			return ((BranchProperty)props[0]).belongsToBranch;
		return true;
	}
	/*-------------------------------------*/
	public boolean getBelongsToBranch(){ //in Associable, this is referred to as betweenness
		return belongsToBranch;
	}

	/*-------------------------------------*/
	//for internal use only; not for UI
	public void setBelongsToBranch(boolean belongsToBranch){
		this.belongsToBranch = belongsToBranch;
	}
	/*-------------------------------------*/
	//returns true if succeeds. To be used by UI if a change is requested
	public boolean setBelongsToBranch(boolean belongsToBranch, boolean recordIfNew){
		if (kind == Associable.BUILTIN || BranchProperty.findInBranchPropertySettings(nRef, kind) != null)
			return false;
		if (this.belongsToBranch != belongsToBranch){
			//Record this?
		}
		this.belongsToBranch = belongsToBranch;
		return true;
	}
	/*-------------------------------------*/
	public static BranchProperty findInBranchPropertySettings(NameReference nr, int kind){
		ListableVector pList = branchPropertiesSettingsVector;
		for (int i=0; i<pList.size(); i++){
			BranchProperty mi = (BranchProperty)pList.elementAt(i);
			if (mi.getNameReference().equals(nr) && mi.kind ==kind)
				return mi;
		}
		return null;
	}

	/*-------------------------------------*/
	public static BranchProperty[] findInBranchPropertySettings(String name){
		ListableVector pList = branchPropertiesSettingsVector;
		int count = 0;
		for (int i=0; i<pList.size(); i++){
			PropertyRecord mi = (PropertyRecord)pList.elementAt(i);
			if (mi.getNameReference().equalsString(name))
				count++;
		}
		if (count == 0)
			return null;
		BranchProperty[] props = new BranchProperty[count];
		count = 0;
		for (int i=0; i<pList.size(); i++){
			BranchProperty mi = (BranchProperty)pList.elementAt(i);
			if (mi.getNameReference().equalsString(name))
				props[count++] = mi;
		}
		return props;
	}
	
	/*-------------------------------------*/
	public void addToKnownBranchPropertiesIfNeeded(MesquiteTree tree){
		if (tree != null && tree.getTaxa() != null && tree.getTaxa().getProject()!= null)
			addIfNotInList(tree.getTaxa().getProject().knownBranchProperties, this);// add p to project.knownBranchProperties if not there by name
	}

	/*-------------------------------------*/
	public String toString(){
		String s = super.toString();
		if (belongsToBranch)
			s += " branch";
		else
			s += " node";
		return s;
	}

}

