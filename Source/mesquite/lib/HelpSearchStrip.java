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
package mesquite.lib;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
/* ======================================================================== */

public class HelpSearchStrip extends MousePanel implements Commandable {
	TextArea searchBox;
	int searchBoxAndX, searchBoxAndY;
	public static final int searchAND = 0;
	public static final int searchOR = 1;
	public static final int searchDATA = 2;
	public static int searchMODE = searchAND;
	static Image andImage, orImage, dataImage;
	boolean searchDataOnly = false;
	Font smallFont = new Font("SanSerif", Font.PLAIN, 10);
	MesquiteWindow window;
	static String[] searchModeExplanation;
	MesquiteCommand searchCommand = MesquiteTrunk.makeCommand("searchKeywords", this);
	static {
		searchModeExplanation = new String[]{"Search Mesquite features; match all terms", "Search Mesquite features; match at least one term", "Search Data in window; match string"};
	}
	public HelpSearchStrip(MesquiteWindow window, boolean data) {
		super();
		this.window = window;
		setLayout(null);
		searchDataOnly = data;
		searchBox = new TextArea("", 1, 8, TextArea.SCROLLBARS_NONE);
		add(searchBox);
		searchBox.setBounds(0, 1, 130, 14);
		searchBox.setVisible(true);
		searchBox.setFont(smallFont);
		searchBox.addKeyListener(new KIListener(this));
		if (andImage==null && MesquiteModule.getRootPath()!=null){ //done here instead of static in case root path not yet defined when static run
			andImage=  MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "and.gif");  
			orImage=  MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "or.gif");  
			dataImage=  MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "dataSearch.gif");  
		}
		setFont(smallFont);
		setBackground(ColorTheme.getInterfaceBackground());
		setCursor(Cursor.getDefaultCursor());
	}
	public void setText(String t){
		searchBox.setText(t);
	}
	public void search(){
		enterPressed();
	}
	void enterPressed(){
		String string = searchBox.getText();
		if (!StringUtil.blank(string)) {
			string = StringUtil.stripTrailingWhitespace(string);
			string = StringUtil.replace(string, '"', '\'');
			searchCommand.doItMainThread(ParseUtil.tokenize(string), null, false, false);
		}
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Searches for keywords", "[keywords]", commandName, "searchKeywords")) {
			String string = new Parser().getFirstToken(arguments);
			if (!StringUtil.blank(string)) {
				string = StringUtil.stripTrailingWhitespace(string);
				searchBox.setText(string);
				searchBox.selectAll();
				if (searchDataOnly || searchMODE == searchDATA)
					MesquiteTrunk.mesquiteTrunk.searchData(string, window);
				else
					MesquiteTrunk.mesquiteTrunk.searchKeyword(string, false);
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public void paint (Graphics g) {
		if (searchDataOnly)
			return;
		if (MesquiteWindow.checkDoomed(this))
			return;

		Rectangle searchBoxRect = searchBox.getBounds();
		searchBoxAndX = searchBoxRect.x+searchBoxRect.width+2;
		searchBoxAndY = searchBoxRect.y + 2;
		if (searchMODE != searchDATA)
			g.setColor(ColorDistribution.brown);
		else
			g.setColor(ColorDistribution.violetBlue);
		g.fillRect(searchBoxAndX-2, searchBoxRect.y, 12, searchBoxRect.height);
		g.setColor(Color.black);
		if (searchMODE == searchAND){
			g.drawImage(andImage, searchBoxAndX, searchBoxAndY, this);
			g.drawString("Search Features", searchBoxAndX+12, searchBoxAndY+10);
		}
		else if (searchMODE == searchOR) {
			g.drawImage(orImage, searchBoxAndX, searchBoxAndY, this);
			g.drawString("Search Features", searchBoxAndX+12, searchBoxAndY+10);
		}
		else if (searchMODE == searchDATA){
			g.drawImage(dataImage, searchBoxAndX, searchBoxAndY, this);
			g.drawString("Search Data", searchBoxAndX+12, searchBoxAndY+10);
	}
		
	for (int i=0; i<2000; i+=15)
			g.drawString(Integer.toString(i), i, i);
		MesquiteWindow.uncheckDoomed(this);
	}
	int count = 0;
	/*.................................................................................................................*/
	public void mouseDown(int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		if (searchDataOnly)
			return;
		if (MesquiteWindow.checkDoomed(this))
			return;
		if (x> searchBoxAndX && x< searchBoxAndX+12  && y > searchBoxAndY && y< searchBoxAndY +12) {
				searchMODE++;
				searchMODE = searchMODE % 3;
				window.setExplanation(searchModeExplanation[searchMODE]);
				MesquiteTrunk.mesquiteTrunk.storePreferences();
				MesquiteWindow.repaintAllSearchStrips();
			}
		else if (x> searchBoxAndX + 12 && x< searchBoxAndX+50  && y > searchBoxAndY && y< searchBoxAndY +12) {
			enterPressed();
		}
		
		
		MesquiteWindow.uncheckDoomed(this);
	}
	public void mouseMoved(int modifiers, int x, int y, MesquiteTool tool) {
		super.mouseMoved(modifiers, x, y, tool);
		if (searchDataOnly)
			return;
		window.setExplanation(searchModeExplanation[searchMODE]);
	}
	public void mouseEntered(int modifiers, int x, int y, MesquiteTool tool) {
		super.mouseEntered(modifiers, x, y, tool);
		if (searchDataOnly)
			return;
		window.setExplanation(searchModeExplanation[searchMODE]);
	}
}

class KIListener extends KeyAdapter {
	HelpSearchStrip infoBar;
	public KIListener(HelpSearchStrip bar){
		infoBar = bar;
	}
	public void keyPressed(KeyEvent e){
		//Event queue
		super.keyPressed(e);
		if (e.getKeyCode()== KeyEvent.VK_ENTER) {
			infoBar.enterPressed();
		}
	}
}


