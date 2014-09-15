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
/**Module that displays a file.*/

public abstract class TextDisplayer extends MesquiteModule  {
   	 public Class getDutyClass() {
   	 	return TextDisplayer.class;
   	 }
	
 	public String getDutyName() {
 		return "Text Displayer";
   	 } 
	/** displays contents of given local file.*/
	public abstract void showFile(String pathName, int maxCharacters, boolean goAwayable);
	/** displays contents of given local file.*/
	public abstract void showFile(String pathName, int maxCharacters, boolean goAwayable, int fontSize, boolean monospaced);
	
	/** displays contents of given file.*/
	public abstract void showFile(MesquiteFile file, int maxCharacters, boolean goAwayable);
	/** displays contents of given file.*/
	public abstract void showFile(MesquiteFile file, int maxCharacters, boolean goAwayable, int fontSize, boolean monospaced);
	/** displays a string.*/
	public abstract void showText(String s, String title, boolean goAwayable);
	/** sets as separate normal window (w = 0), set as tile (w = 1), or popped out as separate window (w=2).  Default Popped Out.  Must be called before first call to showFile or showText.*/
	public abstract void setPoppedOut(int w);
	/** sets whether text is to wrap at right margin.  Must be called before first call to showFile or showText.*/
	public abstract void setWrap(boolean w);
	/** sets whether allows pasting into window.  Must be called before first call to showFile or showText.*/
	public abstract void setAllowPaste(boolean paste);
   	public boolean isSubstantive(){
   		return false;  
   	}
}


