/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.cont.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.duties.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;

/** Contains an array of geographic character states for one character */
public abstract class GeographicStates  extends ContinuousStates {
	
	public GeographicStates (Taxa taxa) {
		super(taxa);
	}
	/*..........................................ContinuousStates................*/
	/**returns blank CharacterState object */
	public CharacterState getCharacterState (){
		return new GeographicState();
	}
	/** Indicates the type of character stored */ 
	public Class getStateClass(){
		return GeographicState.class;
	}
	/**returns the corresponding CharacterData subclass*/
	public Class getCharacterDataClass (){
		return GeographicData.class;
	}
	/** returns the name of the type of data stored */
	public String getDataTypeName(){
		return "Geographic Data";
	}
	
}


