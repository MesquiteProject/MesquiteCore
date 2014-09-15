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
import java.awt.image.*;
import java.awt.event.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
/** A Text window that serves as a console. */
public class ConsoleWindow extends MesquiteTextWindow implements KeyListener {
	String typed = "";
	boolean consoleMode = true;
	ConsoleThread thread;
	public static boolean suppressConsoleInput = false;
	
	public ConsoleWindow(MesquiteModule module, String assignedTitle, boolean showInfoBar) {
		super(module, assignedTitle, showInfoBar); //infobar
		setBackground(Color.white);
		addKeyListener(this, (KeyListener)this, false);
		if (!suppressConsoleInput){
			thread = new ConsoleThread(module, MesquiteTrunk.mesquiteTrunk, false);
			thread.start();
		}
		else consoleMode = false;
	}
	
	public void setConsoleMode(boolean console){
		if (!suppressConsoleInput)
		consoleMode = console;
		
	}
	public boolean isConsoleMode(){
		return consoleMode;
	}
	private void write(String s){
		if (consoleMode){
			if (this instanceof LogWindow)
				MesquiteTrunk.mesquiteTrunk.log(s);
			else
				super.append(s);
		}
	}
	public void showPrompt(){
		if (consoleMode)
			thread.getCommunicator().showPrompt();
	}
	
	public void keyTyped(KeyEvent e){
		if (!consoleMode)
			return;
		if (!tA.hasFocus())
			return;
		int mod = MesquiteEvent.getModifiers(e);
		if (!MesquiteEvent.commandOrControlKeyDown(mod)) {
			if (e.getKeyChar()== '\n' || e.getKeyChar()== '\r' || e.getKeyCode()== KeyEvent.VK_ENTER) {
				thread.enterCommand(typed);
				tA.requestFocus();
				tA.requestFocusInWindow();
				typed = "";
			}
			else if ((new Character(e.getKeyChar()).hashCode())==8) {
				typed = typed.substring(0, typed.length()-1);
				consume(1);
				tA.requestFocus();
				tA.requestFocusInWindow();
			}
			else {
				write("" + e.getKeyChar());
				typed += e.getKeyChar();
			}
		}
		
	}
	
	public void keyPressed(KeyEvent e){
	}
	
	public void keyReleased(KeyEvent e){
	}
}



