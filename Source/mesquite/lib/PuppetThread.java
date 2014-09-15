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
import mesquite.lib.duties.*;

/* ��������������������������� commands ������������������������������� */
/* includes commands,  buttons, miniscrolls


/* ======================================================================== */
/** A class that serves as a separate Thread on which commands can be sent. */
public class PuppetThread extends MesquiteThread {
	Object baseObject;
	String commands;
	NexusBlock nblock;
	Puppeteer puppeteer;
	String endString;
	boolean skip;
	MesquiteInteger pos;
	public PuppetThread (Puppeteer puppeteer, Object object, String commands, MesquiteInteger pos, String endString, boolean skip, NexusBlock nblock, boolean scripting) {
		super();
		this.baseObject = object;
		this.commands = commands;
		this.nblock = nblock;
		this.puppeteer = puppeteer;
		this.endString = endString;
		this.skip = skip;
		this.pos = pos;
		setCommandRecord(new CommandRecord(scripting));
		spontaneousIndicator = false;
		setCurrent(1);
	}
	public String getCurrentCommandName(){
		return null;
	}
	public String getCurrentCommandExplanation(){
		return null;
	}
	/** run the commands that were stored in the PuppetThread in its constructor. */
	public void run() {
			MesquiteModule.incrementMenuResetSuppression();
			puppeteer.execute(baseObject, commands, pos, endString, skip, nblock, null);
			baseObject = null;
			commands = null;
			MesquiteModule.decrementMenuResetSuppression();
			threadGoodbye();
	}

}

