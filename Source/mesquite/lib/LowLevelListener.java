package mesquite.lib;

/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison.
Version 2.6, January 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */


/* ======================================================================== */
public interface LowLevelListener {
	//change cell of matrix, code VALUE_CHANGED, i1 = ic, i2 = it, i3 not used
	//add characters to CharacterData or taxa to Taxa, code PARTS_ADDED, i1 = justAfter, i2 = num
	//delete characters in CharacterData or taxa in Taxa, code PARTS_DELETED, i1 = starting, i2 = num
	//move characters in CharacterData or taxa in Taxa, code PARTS_MOVED, i1=starting, i2= num, i3 = justAfter
	//swap characters in CharacterData or taxa in Taxa, code PARTS_SWAPPED, i1= first, i2 = second
	public void llChange(Object obj, int code, int i1, int i2, int i3);
}

