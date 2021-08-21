/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trunk;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
/*======================================================================== */
public class ProjectTreeWindow extends MesquiteWindow implements SystemWindow {
	HPanel  browser;
	MesquiteModule  ownerModule;
	HelpSearchStrip searchStrip;
	int searchHeight = 20;
	public ProjectTreeWindow (MesquiteModule ownerModule, BrowseHierarchy drawTask) {
		super(ownerModule, false);
		setWindowSize(300,300);
		setWindowLocation(4,4, false);
		this.ownerModule = ownerModule;
		setFont(new Font ("SanSerif", Font.PLAIN, 10));

//		getGraphicsArea().setLayout(new BorderLayout());
		//getGraphicsArea().setBackground(Color.cyan);
		if (drawTask!=null){
			browser = drawTask.makeHierarchyPanel();
			browser.setTitle(null);
			addToWindow(browser);
			
			browser.setSize(getWidth(), getHeight() - searchHeight);
			browser.setLocation(0, 0);
			browser.showTypes(true);
			browser.setBackground(Color.white);
			//browser.setBackground(ColorDistribution.projectLight[getColorScheme()]);
			//checking for memory leaks 
			browser.setRootNode(MesquiteTrunk.mesquiteTrunk.getProjectList());
			searchStrip = new HelpSearchStrip(this, false);
			addToWindow(searchStrip);
			searchStrip.setBounds(4, getHeight()-searchHeight, getWidth()-4, searchHeight);
			searchStrip.setVisible(true);
			browser.setVisible(true);
			setShowAnnotation(true);
			incrementAnnotationArea();
			setShowExplanation(true);
			incrementExplanationArea();
			setExplanation("Configuration of modules loaded: " + MesquiteTrunk.mesquiteTrunk.getConfiguration());
		}
		resetTitle();
	}
	public boolean showInfoTabs(){
		return false;
	}
	public boolean permitViewMode(){
		return false;
	}
	public void repaintSearchStrip(){
		searchStrip.repaint();
	}
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree blocks, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		if (compactWindows)
			setTitle("Projects and Files"); 
		else
			setTitle("Mesquite Projects and Files"); 

	}
	/** Returns menu location for item to bring the window to the for (0 = custom or don't show; 1 = system area of Windows menu; 2 = after system area of Windows menu)*/
	public int getShowMenuLocation(){
		return 1;
	}
	public void contentsChanged() {
		setExplanation("Configuration of modules loaded: " + MesquiteTrunk.mesquiteTrunk.getConfiguration());
		super.contentsChanged();
	}
	public void disposeReferences() {
		if (browser!=null)
			browser.disposeReferences();
	}
	public void renew() {
		if (browser!=null)
			browser.renew();
		setExplanation("Configuration of modules loaded: " + MesquiteTrunk.mesquiteTrunk.getConfiguration());
	}
	/*.................................................................................................................*/
	public void windowResized() {
		super.windowResized();
	   	if (MesquiteWindow.checkDoomed(this))
	   		return;
		setExplanation("Configuration of modules loaded: " + MesquiteTrunk.mesquiteTrunk.getConfiguration());
		if (browser!=null) {
			browser.setSize(getWidth(), getHeight()-  searchHeight);
			browser.setLocation(0, 0);
		}
		if (searchStrip != null) {
			searchStrip.setLocation(4, getHeight()-searchHeight);
			searchStrip.setSize(getWidth()-4, searchHeight);
		}
		MesquiteWindow.uncheckDoomed(this);
	}
}


