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
package mesquite.lib;

import java.awt.*;
import mesquite.lib.duties.*;

/* еееееееееееееееееееееееееее commands еееееееееееееееееееееееееееееее */
/* includes commands,  buttons, miniscrolls

/* ======================================================================== */
/* ======================================================================== */
/** Interfact designed for modules so that they can claim to own or be associated with other Commandables.  When queried the module should
return an array of instantiated Commandables, from each of which can be accumulated its commands.  The purpose of this is for the automatic
accumulation of documentation of commands (see CommandChecker) to have access to Commandables other than the modules themselves.  Thus,
if a module uses the interface CommandableOwner and returns an array containing an instantiated copy of its special window type, the accumulator of
of command documentation can include the documentation for commands of that special window type within the documentation for the module itself.    */
public interface CommandableOwner {
	/** return an array of instantiated Commandables from which commands can be accumulated.  These Commandables will typically be
	temporary copies instantiated for this purpose alone, because this method gets called during Mesquite startup, before the modules have actually started up. */
 	public Commandable[] getCommandablesForAccumulation();
 }


