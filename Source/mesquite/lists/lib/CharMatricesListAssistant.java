/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lists.lib;

import java.awt.Color;
import java.awt.Graphics;

import mesquite.lib.CommandChecker;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.NumberForMatrix;
import mesquite.lib.ui.MesquiteCMenuItemSpec;
import mesquite.lib.ui.MesquiteMenuSpec;



/* ======================================================================== */
public abstract class CharMatricesListAssistant extends ListAssistant  {
	protected ListableVector datas =null;

   	 public Class getDutyClass() {
   	 	return CharMatricesListAssistant.class;
   	 }
 	public String getDutyName() {
 		return "Character matrices list assistant";
   	 }

	 public String[] getDefaultModule() {
	 	 	return new String[] {"#NumForCharMatrixList"};
	 	 }
	
		/*======================================*
		 //DEFAULTASSISTANTS
	MesquiteCMenuItemSpec setAsDefaultMMI;
	MesquiteBoolean setAsDefaultColumn = new MesquiteBoolean(false);
	public boolean superStartJobAfter(String arguments, Object condition, boolean hiredByName){
		
		if (isDefaultable())
			setAsDefaultMMI = super.addCheckMenuItem(null, "Set as default column", new MesquiteCommand("setAsDefault", this), setAsDefaultColumn);
		return super.superStartJobAfter(arguments, condition, hiredByName);
	 }
	
	
	
	   public Object doCommand(String commandName, String arguments, CommandChecker checker) {
	
		if (checker.compare(this.getClass(), "Sets column as default", "[on or off]", commandName, "setAsDefault")) {
			setAsDefaultColumn.toggleValue(parser.getFirstToken(arguments));
			System.err.println("@ oops");
			//tell list window to use as default
			// call its method makeMeDefault(this). 
			//List window will maintain vector of default modules, and each time set save script to set up, saved and recovered via prefs
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*======================================*/
	
	public void drawInCell(int ic, Graphics g, int x, int y,  int w, int h, boolean selected){
	}
	
	/** Gets text color for cell for row ic.  Override it if you want to change the color from the default. */
	public Color getTextColorOfCell(int ic, boolean selected){
		if (datas ==null || ic<0 || ic>= datas.size())
			return null;
		CharacterData data =((CharacterData)datas.elementAt(ic));
		if (data!=null && !data.isUserVisible())
			return Color.gray;
		return null;
	}

}

