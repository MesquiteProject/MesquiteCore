/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)

modified 26 July 01: protected against NullPointerException if null images in paint
 */
package mesquite.lib.simplicity;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;

import mesquite.lib.Commandable;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteModule;
import mesquite.lib.ui.ColorTheme;
import mesquite.lib.ui.MesquiteCheckMenuItem;
import mesquite.lib.ui.MesquiteImage;
import mesquite.lib.ui.MesquiteMenuItem;
import mesquite.lib.ui.MesquitePopup;
import mesquite.lib.ui.MesquiteTool;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.MousePanel;

public class SimplicityStrip extends MousePanel implements Commandable {
	Font smallFont = new Font("SanSerif", Font.PLAIN, 10);
	Font boldFont = new Font("SanSerif", Font.BOLD, 10);
	MesquiteWindow window;
	MesquitePopup popup=null;
	Image editing, power, simple;
	boolean showText = true;
	public SimplicityStrip(MesquiteWindow window, boolean showText) {
		super();
		this.window = window;
		this.showText = showText;
		setLayout(null);
		setFont(smallFont);
		setBackground(ColorTheme.getInterfaceBackground());
		setCursor(Cursor.getDefaultCursor());
		power = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "simplification/power.gif");  
		simple = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "simplification/simple.gif");  
		editing = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "notesTool.gif");  
	}

	public void dispose(){
		if (popup!=null)
			remove(popup);
		super.dispose();
	}
	public void setVisible(boolean vis){
		if (InterfaceManager.locked && vis)
			return;
		super.setVisible(vis);
	}
	/*.................................................................................................................*/
	void redoMenu() {
		if (popup==null)
			popup = new MesquitePopup(this);
		popup.removeAll();
		if (InterfaceManager.isEditingMode()){
			MesquiteCheckMenuItem editItem=new MesquiteCheckMenuItem("Turn OFF Interface Editing", null, new MesquiteCommand("offEdit", InterfaceManager.simplicityModule), null, null);
			editItem.set(InterfaceManager.isEditingMode());
			editItem.setHideable(false);
			popup.add(editItem);		
		}
		else {
			MesquiteCheckMenuItem fullItem=new MesquiteCheckMenuItem("Full Interface", null, new MesquiteCommand("full", InterfaceManager.simplicityModule), null, null);
		fullItem.set(!InterfaceManager.isSimpleMode() && !InterfaceManager.isEditingMode());
		popup.add(fullItem);
		MesquiteCheckMenuItem simpleItem=new MesquiteCheckMenuItem("Simple Interface", null, new MesquiteCommand("simple", InterfaceManager.simplicityModule), null, null);
		simpleItem.set(InterfaceManager.isSimpleMode() && !InterfaceManager.isEditingMode());
		popup.add(simpleItem);
		}
		popup.add(new MenuItem("-"));
		Menu lsm = new Menu("Load Simplification");
		popup.add(lsm);
		InterfaceManager.addSettingsMenuItems(lsm, "load", true);
		MesquiteMenuItem gs;
		popup.add(gs = new MesquiteMenuItem("Go to Simplification Control Panel...", null, new MesquiteCommand("showWindow", InterfaceManager.simplicityModule), null));
		gs.setHideable(false);

		/*
		 * MesquiteCheckMenuItem editItem=new MesquiteCheckMenuItem("Edit Simple Interface", null, new MesquiteCommand("edit", this), null, null);
		editItem.setState(InterfaceManager.isEditingMode());
		popup.add(editItem);
		popup.add(new MenuItem("-"));
		popup.add(new MesquiteMenuItem("Instructions...", null, new MesquiteCommand("showInstructions", InterfaceManager.simplicityModule), null));
		popup.add(new MesquiteMenuItem("Show/Hide Packages...", null, new MesquiteCommand("showWindow", InterfaceManager.simplicityModule), null));
		popup.add(new MenuItem("-"));
		*/
		//InterfaceManager.getLoadSaveMenuItems(popup);
		add(popup);
	}
	/*.................................................................................................................*/
	public void paint (Graphics g) {
		if (MesquiteWindow.checkDoomed(this))
			return;
		int left = 18;
		//g.drawRect(0, 0, getWidth()-1, getHeight()-1);
		if (!InterfaceManager.isEditingMode() && !InterfaceManager.isSimpleMode() && showText){
			g.drawImage(power, 0, 0, this);
				g.drawString("Full Interface", left, 13);
		}
		else if (!InterfaceManager.isEditingMode() && InterfaceManager.isSimpleMode()){
			g.drawImage(simple, 0, 0, this);
			if (showText)
				g.drawString("Simple Interface", left, 13);
		}
		else if (InterfaceManager.isEditingMode()){
			g.setColor(Color.cyan);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(Color.black);
			g.drawImage(editing, 0, 0, this);
			g.setFont(boldFont);
			g.drawString("EDITING INTERFACE", left, 13);
			g.setFont(smallFont);
		}

		MesquiteWindow.uncheckDoomed(this);
	}

	/*.................................................................................................................*/
	public void mouseDown(int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (InterfaceManager.locked)
			return;
		if (!InterfaceManager.isEditingMode() && !InterfaceManager.isSimpleMode())
			return;
		if (MesquiteWindow.checkDoomed(this))
			return;
			redoMenu();
		popup.show(this, 0,20);
		MesquiteWindow.uncheckDoomed(this);
	}
}

