/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib;

import java.awt.*;
import java.text.*;



/* ======================================================================== */
/** An interface so that objects know some other object has changed (see Taxa)*/
public interface MesquiteListener {
	/** Constant for listener notify system.  Unknown change occured to object. */
	public final static int UNKNOWN = MesquiteInteger.unassigned;
	/** Names of parts have changed*/
	public final static int NAMES_CHANGED = -1;
	/** Selection of parts has changed*/
	public final static int SELECTION_CHANGED= 128;
	/** Parts have been added*/
	public final static int PARTS_ADDED=-2;   
	/** Parts have been deleted*/
	public final static int PARTS_DELETED=-3;
	/** Parts have been moved*/
	public final static int PARTS_MOVED=-4;
	/** Parts have been added, deleted, moved, or changed in some other serious way*/
	public final static int PARTS_CHANGED=-5; 
	/** Parts have been added, deleted, moved, or changed in some other serious way*/
	public final static int PARTS_SWAPPED=-17; 
	/** An element of a vector has changed */
	public final static int ELEMENT_CHANGED=-6;
	/** Branch lengths of tree have been changed*/
	public final static int BRANCHLENGTHS_CHANGED = 111; 
	/** Branches of tree have been rearranged*/
	public final static int BRANCHES_REARRANGED = 112; 
	/** One or more items in a source have been added terminally*/
	public final static int ITEMS_ADDED = 113; 
	/** One or more items in a source have been added terminally*/
	public final static int NUM_ITEMS_CHANGED = 114; 
	/** The value of the object has been changed*/
	public final static int VALUE_CHANGED = -7; 
	/** The taxa block on which this object is based was substituted for another*/
	public final static int TAXA_SUBSTITUTED = -8; 
	/** The annotations of the object has been changed*/
	public final static int ANNOTATION_CHANGED = -10; 
	/** The associated values of the Associable has been changed*/
	public final static int ASSOCIATED_CHANGED = -11; 
	/** The object has been destroyed (not used within Mesquite as of March 05)*/
	public final static int OBJECT_DESTROYED = -12; 
	/** An annotation of the object has been added*/
	public final static int ANNOTATION_ADDED = -13; 
	/** An annotation of the object has been added*/
	public final static int ANNOTATION_DELETED = -14; 
	/** An editable text field has been edited*/
	public final static int TEXTFIELD_EDITED = -15; 
	/** New results available*/
	public final static int NEW_RESULTS = -16; 
	/** Entries in a data matrix have been changed*/
	public final static int DATA_CHANGED = -18; 
	/** The main object (e.g. tree block) has been deleted*/
	public final static int BLOCK_DELETED=-19;
	/** The main object (e.g. data matrix) has been locked or unlocked from editing*/
	public final static int LOCK_CHANGED=-20;

	/** Param of the tree or taxa drawing have been changed in such a way that its size may have changed.*/
	public final static int TREE_DRAWING_SIZING_CHANGED = 800; 
	public final static int TAXA_DRAWING_SIZING_CHANGED = 801; 

	
	
	/** Subcode values specifying how much of the data has changed*/
	public final static int SINGLE_CELL = 900; 
	public final static int SINGLE_CHARACTER = 901; 
	public final static int SINGLE_TAXON = 902; 
	public final static int CELL_BLOCK = 903; 
	/** Subcode value specifying that the change is only a substitution of state in a cell, not a change from inapplicable or unassigned to or from a valid state*/
	public final static int CELL_SUBSTITUTION = 904; 
	/** Subcode value specifying that the change is only a shifting of all cells in each taxon, from first to last applicable, with no addition or deletion of characters*/
	public final static int ALL_CELLS_ONLY_SHIFTED = 905; 
	public final static int TAXA_CHANGED = 906;

	
	

	/** A command was cancelled (used by CommandRecord)*/
	public final static int COMMAND_CANCELLED = -9;
	/** passes which object changed, along with optional Notification object with details (e.g., code number (type of change) and integers (e.g. which character))*/
	public void changed(Object caller, Object obj, Notification notification); 
	/** passes which object was disposed*/
	public void disposing(Object obj);
	/** Asks whether it's ok to delete the object as far as the listener is concerned (e.g., is it in use?)*/
	public boolean okToDispose(Object obj, int queryUser);
}


