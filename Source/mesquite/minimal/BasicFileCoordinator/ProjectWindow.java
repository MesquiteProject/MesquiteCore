/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.minimal.BasicFileCoordinator;
/*~~  */

import java.io.File;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.event.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeVector;
import mesquite.lib.ui.ClosablePanel;
import mesquite.lib.ui.ClosablePanelContainer;
import mesquite.lib.ui.ColorTheme;
import mesquite.lib.ui.HelpSearchStrip;
import mesquite.lib.ui.ListDialog;
import mesquite.lib.ui.MQComponent;
import mesquite.lib.ui.MQComponentHelper;
import mesquite.lib.ui.MesquiteDialog;
import mesquite.lib.ui.MesquiteFrame;
import mesquite.lib.ui.MesquiteImage;
import mesquite.lib.ui.MesquiteMenuItem;
import mesquite.lib.ui.MesquiteTool;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.MousePanel;
import mesquite.lib.ui.StringInABox;
import mesquite.categ.lib.*;
import mesquite.cont.lib.*;
import mesquite.meristic.lib.*;
import mesquite.assoc.lib.*;

public class ProjectWindow extends MesquiteWindow implements MesquiteListener {
	MesquiteProject proj;
	boolean suppressed = false;
	BasicFileCoordinator bfc;
	ProjectPanel projPanel;
	ScrollPanel scrollPanel;
	int scrollHeight = 26; 
	int scrollWidth = 54;
	/*.................................................................................................................*/
	public ProjectWindow(FileCoordinator  ownerModule){
		super(ownerModule, false);
		bfc = (BasicFileCoordinator)ownerModule;
		proj = ownerModule.getProject();
		projPanel = new ProjectPanel(this, proj, bfc);
		scrollPanel = new ScrollPanel(projPanel);
		addToWindow(scrollPanel);
		addToWindow(projPanel);
		setBackground(ColorTheme.getExtInterfaceBackground());
		proj.addListener(this);

	}
	public int getDefaultTileLocation(){
		return MesquiteFrame.RESOURCES;
	}
	/*.................................................................................................................*/
	/** Gets the content width of the window (excluding the insets) */
	public int getContentsWidth(){
		///return	 (outerContents.getBounds().width);
		Insets insets = getInsets();
		return (getParentFrame().getBounds().width-insets.left - insets.right);
	}
	/*.................................................................................................................*/
	/** Gets basic snapshot for window, including size, location. */
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		MesquiteFrame f = getParentFrame();
		temp.addLine("suppress");
		temp.incorporate(super.getSnapshot(file), false);
		return temp;
	}
	/*.................................................................................................................*/
	/** Gets basic snapshot for window, including size, location. */
	public Snapshot getLateSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		MesquiteFrame f = getParentFrame();

		//find the uniqueID of the foremost window in each tile
		MesquiteWindow main = f.frontMostInLocation(MesquiteFrame.MAIN);
		MesquiteWindow pop = f.frontMostInLocation(MesquiteFrame.POPTILE);
		MesquiteWindow either = f.getFrontWindow();

		temp.addLine("setResourcesState " + f.getResourcesFullWindow() + " " + f.getResourcesClosedWhenMinimized() + " "  + f.getResourcesWidth());
		if (either != null){
			if (pop == null) //no pop just bring first
				addToFrontScriptForWindow(temp, main);
			else {
				temp.addLine("setPopoutState " + f.getPopoutWidth());
				if (either == pop){ //pop is frontmost; do main first then pop
					addToFrontScriptForWindow(temp, main);
					addToFrontScriptForWindow(temp, pop);
				}
				else { //main is frontmost; do pop first then main
					addToFrontScriptForWindow(temp, pop);
					addToFrontScriptForWindow(temp, main);
				}
			}
		}
		temp.addLine("desuppress");
		return temp;
	}

	void addToFrontScriptForWindow(Snapshot temp, MesquiteWindow w){
		if (w == null)
			return;

		temp.addLine("findWindow " + StringUtil.tokenize(w.getUniqueID()));
		temp.addLine("tell It");
		temp.addLine("\tsetAsFront");
		temp.addLine("endTell");
	}
	/*.................................................................................................................*/
	Parser parser = new Parser();
	/** Respond to commands sent to the window. */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(MesquiteWindow.class, "Sets the state of the resources panel of the window", null, commandName, "setResourcesState")) {
			MesquiteFrame f = getParentFrame();
			String rfwt = parser.getFirstToken(arguments);
			if (rfwt != null){
				boolean rfw = rfwt.equalsIgnoreCase("true");
				String rcwmt = parser.getNextToken();
				if (rcwmt != null){
					boolean rcwm = rcwmt.equalsIgnoreCase("true");
					String rwt = parser.getNextToken();
					int rw = MesquiteInteger.fromString(rwt);
					if (!MesquiteFrame.respectFileSpecificResourceWidth || !MesquiteInteger.isCombinable(rw))
						rw = f.getResourcesWidth();
					f.setResourcesState(rfw, rcwm, rw);

				}
			}
		}
		else if (checker.compare(MesquiteWindow.class, "Sets the width of the resources panel of the window", null, commandName, "setResourcesWidth")) {
			if (MesquiteFrame.respectFileSpecificResourceWidth)
				return null;
			MesquiteFrame f = getParentFrame();
			String rfwt = parser.getFirstToken(arguments);
			if (rfwt != null){
				int rw = MesquiteInteger.fromString(rfwt);
				if (MesquiteInteger.isCombinable(rw))
					f.setResourcesState( f.getResourcesFullWindow(), f.getResourcesClosedWhenMinimized(), rw);
			}

		}
		else if (checker.compare(MesquiteWindow.class, "Sets the state of the popout panel of the window", null, commandName, "setPopoutState")) {
			MesquiteFrame f = getParentFrame();
			String rfwt = parser.getFirstToken(arguments);
			if (rfwt != null){
				int rw = MesquiteInteger.fromString(rfwt);
				if (MesquiteInteger.isCombinable(rw)){
					f.requestPopoutWidth(rw);
				}
				String rcwmt = parser.getNextToken();
				if (rcwmt != null){
					int total = MesquiteInteger.fromString(rcwmt);
					if (MesquiteInteger.isCombinable(total)){
						//	f.setTotalMainPlusPopWidth(rfw, rcwm, rw);
					}
				}
			}
		}
		else if (checker.compare(MesquiteWindow.class, "Finds window with uniqueID", null, commandName, "findWindow")) {
			MesquiteFrame f = getParentFrame();
			MesquiteWindow w = f.findWindowByUniqueID(parser.getFirstToken(arguments));
			return w;

		}
		else if (checker.compare(MesquiteWindow.class, "Explain the incorporation options", null, commandName, "explainIncorporate")) {
			explainIncorporate();
		}
		else if (checker.compare(MesquiteWindow.class, "Suppress refreshing the panels", null, commandName, "suppress")) {
			proj.incrementProjectWindowSuppression();
		}
		else if (checker.compare(MesquiteWindow.class, "Desuppress refreshing the panels", null, commandName, "desuppress")) {
			proj.decrementProjectWindowSuppression();
		}
		else return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public boolean setAsFrontOnClickAnyContent(){
		return false;
	}
	public void explainIncorporate(){
		String html = "<html><body>";
		html += "<h2>Incorporating information from another file</h2>";
		html += "There are several ways to incorporate information from another file.  Follow this key to decide what is appropriate for your needs:";
		html += "<ol>";

		html += "<li>Do you want to incorporate taxa and/or matrices from another file into this project?  (If you want to incorporate trees only, see the other options below.)";
		html += "<ol>";
		html += "<li><img src=\"" + MesquiteFile.massageFilePathToURL(bfc.getPath()+"projectHTML" + MesquiteFile.fileSeparator + "fileLink.gif") + "\">&nbsp;Do you want the information incorporated to remain in the other file?  If so, then you want to use <b>Link File</b>. ";
		html +="If you do this and you want to save the files linked together, you should keep them in the same relative position on disk, so that when you later open the home file, it can find the linked file.</li><br>";
		html += "<li><img src=\"" + MesquiteFile.massageFilePathToURL(bfc.getPath()+"projectHTML" + MesquiteFile.fileSeparator + "fileIncludeBasic.gif") + "\">&nbsp;Do you want the information incorporated to be copied into the home file of this project?";
		html += " The other file will be read and then ignored; the other file will not remain linked and will not be re-written.";
		html += "<ol>";
		html += "<li><img src=\"" + MesquiteFile.massageFilePathToURL(bfc.getPath()+"projectHTML" + MesquiteFile.fileSeparator + "fileInclude.gif") + "\">&nbsp;&nbsp;Do you want to incorporate taxa, matrices and trees, but in such a way that in general keeps the taxa blocks and matrices separate from those currently in the project?";
		html += " Then use <b>Include File</b>.</li><br>";
		html += "<li><img src=\"" + MesquiteFile.massageFilePathToURL(bfc.getPath()+"projectHTML" + MesquiteFile.fileSeparator + "fileMergeTM.gif") + "\">&nbsp;&nbsp;Do you want to merge the taxa blocks and matrices from the other file into the taxa blocks and matrices currently in this project?";
		html += " Then use <b>Merge Taxa/Matrices</b>.  This is a special system that permits you to fuse taxa and matrices from other files into existing taxa blocks and matrices.";
		html += " This is useful for instance to add new sequences from another file into a DNA sequence matrix.</li>";
		html += "</ol>";
		html +="</li>";
		html += "</ol>";
		html +="</li>";

		html += "<li>Do you want to incorporate only trees from another file?";
		html += "<ol>";
		html += "<li>Do you want to incorporate the trees only temporarily in a calculation, and so as to save memory?";
		html += " If so then you can request one of the following two options as your Tree Source in the tree window or in various calculations:<ol>";
		html += "<li><b>Use Trees from Separate File</b>: this reads in the trees from the file one at a time, as needed, and therefore saves memory with large tree files.</li>";
		html += "<li><b>MrBayes Trees</b>: this is a special version of Use Trees from Separate File that can also read the associated .p file to recover tree scores.</li>";
		html += "</ol>To use either of these, choose it as your <b>Tree Source</b> whenever you are using a Tree Window, Chart or other calculation that uses trees.  These do not bring the trees into the project, and therefore the trees are available only for the tree window or calculation requested.";
		html +="</li><br>";
		html += "<li><img src=\"" + MesquiteFile.massageFilePathToURL(bfc.getPath()+"projectHTML" + MesquiteFile.fileSeparator + "fileLinkTrees.gif") + "\">&nbsp;&nbsp;Do you want the trees incorporated to remain in the other file?";
		html += " Then use <b>Link Trees</b>.  ";
		html +="If you do this and you want to save the files linked together, you should keep them in the same relative position on disk, so that when you later open the home file, it can find the linked file.</li><br>";
		html += "<li><img src=\"" + MesquiteFile.massageFilePathToURL(bfc.getPath()+"projectHTML" + MesquiteFile.fileSeparator + "fileIncludeTrees.gif") + "\">&nbsp;&nbsp;Do you want the trees to be copied into the home file of this project?";
		html += " Then use <b>Include Trees</b>.  With this option you can choose whether to include all or only some of the trees.";
		html += " By sampling only some of the trees, you can save memory.  The trees will be moved into the home file of this project, and will be saved there when you save the file.</li>";
		html += "</ol>";

		html += "</ol>";

		html += "</html></body>";
		bfc.alertHTML(bfc.containerOfModule().getParentFrame(), html, "Incorporating File", "Incorporating File", 820, 700);
	}
	/*  From ManageTrees
	 * 		MesquiteSubmenuSpec mss = getFileCoordinator().addSubmenu(MesquiteTrunk.treesMenu, "Import File with Trees");
		getFileCoordinator().addItemToSubmenu(MesquiteTrunk.treesMenu, mss, "Link Contents...", makeCommand("linkTreeFile",  this));
		getFileCoordinator().addItemToSubmenu(MesquiteTrunk.treesMenu, mss, "Include Contents...", makeCommand("includeTreeFile",  this));
		getFileCoordinator().addItemToSubmenu(MesquiteTrunk.treesMenu, mss, "Include Partial Contents...", makeCommand("includePartialTreeFile",  this));

	 */
	public void windowResized(){
		try {
			if (projPanel != null){
				projPanel.setBounds(0,0,getBounds().width, getBounds().height);
				scrollPanel.setBounds(0,getBounds().height-scrollHeight,getWidth(), scrollHeight);
			}
		}
		catch (Exception e){
		}
	}
	/*.................................................................................................................*/
	public void resetTitle(){
		if (ownerModule==null || ownerModule.getProject() == null)
			setTitle("Project");
		else if (ownerModule.getProject().hasName())
			setTitle("Project: " + ownerModule.getProject().getName());
		else
			setTitle(ownerModule.getProject().getName());
		repaint();
	}
	public boolean permitViewMode(){
		return false;
	}
	public void dispose(){
		try{
			projPanel.dispose();
			if (proj != null)
				proj.removeListener(this);
			proj = null;
			bfc = null;
			removeAll();
			projPanel = null;
			scrollPanel = null;
		}
		catch (Exception e){
		}
		super.dispose();
	}
	/** passes which object changed, along with optional Notification object with details (e.g., code number (type of change) and integers (e.g. which character))*/
	public void changed(Object caller, Object obj, Notification notification){
		if (obj == proj && !MesquiteThread.isScripting()&& Notification.getCode(notification)!= MesquiteListener.ELEMENT_CHANGED)
			projPanel.refresh();

	}
	public void setFootnote(String heading, String text){
		if (heading == null && text == null && MesquiteTrunk.debugMode){
			heading = "FileElement instantiations - finalizations " + (FileElement.totalCreated - FileElement.totalFinalized);
			text = "FileElement instantiations " + (FileElement.totalCreated);
		}
		projPanel.setFootnote(heading, text);
	}
	public void refresh(FileElement element){
		if (bfc.isDoomed() || bfc.getProject().refreshSuppression>0)
			return;
		BasicFileCoordinator.totalProjectPanelRefreshes++;
		projPanel.refresh(element);
	}
	public void refresh(){
		if (bfc.isDoomed() || bfc.getProject().refreshSuppression>0)
			return;
		BasicFileCoordinator.totalProjectPanelRefreshes++;
		projPanel.refresh();
	}
	public void refreshGraphics(){
		if (bfc.isDoomed())
			return;
		BasicFileCoordinator.totalProjectPanelRefreshes++;
		projPanel.refreshGraphics();
	}
	void suppress(){
		suppressed = true; 
		proj.incrementProjectWindowSuppression();
	}
	void resume(){  //BECAUSE OF decPWS below, should call this only if suppress had been called previously
		resume(true);
	}
	void resume(boolean dpws){  
		suppressed = false;
		if (dpws)
			proj.decrementProjectWindowSuppression();
		refresh();
	}
}

class ProjectPanel extends MousePanel implements ClosablePanelContainer{
	Vector elements = new Vector();
	MesquiteProject proj;
	BasicFileCoordinator bfc;
	ProjectWindow w;
	Image fileIm;
	ScrollPanel scrollPanel;
	NotesPanel notesPanel = null;
	public ProjectPanel(ProjectWindow w, MesquiteProject proj, BasicFileCoordinator bfc){ 
		super();
		this.w = w;
		this.bfc = bfc;
		this.proj = proj;
		setLayout(null);
		setBackground(ColorTheme.getExtInterfaceBackground());
		fileIm = 	MesquiteImage.getImage(bfc.getPath()+ "projectHTML" + MesquiteFile.fileSeparator + "fileSmall.gif");
		setSize(94, 500);

	}
	public void setFootnote(String heading, String text){
		if (notesPanel != null)
			notesPanel.setText(heading, text);
	}
	public void requestHeightChange(ClosablePanel panel){
		resetSizes(getBounds().width, getBounds().height);
	}
	public void refreshGraphics(){
		for (int e = 0; e< elements.size(); e++){
			ProjPanelPanel panel = ((ProjPanelPanel)elements.elementAt(e));
			panel.refreshGraphics();
		}
	}

	public void dispose(){
		for (int i = 0; i<elements.size(); i++){
			ClosablePanel panel = ((ClosablePanel)elements.elementAt(i));
			panel.dispose();
		}
		removeAll();
		w = null;
		bfc = null;
		proj = null;
		super.dispose();
	}
	public ClosablePanel getPrecedingPanel(ClosablePanel panel){
		return null;
	}
	public void explainProjectWindow(){
		String html = "<html><body>";   
		html += "<h3>Project with Home File \""  + StringUtil.protectForXML(proj.getHomeFileName()) + "\"</h3>";
		html += "In Directory: " + StringUtil.protectForXML(proj.getHomeDirectoryName()) + "<p>";
		html += "This panel summarizes your project, which may represent one or more files.  Listed are the files, and the objects within the project, such as taxa, character matrices, trees, models and others.";
		html += "<ul><li>You can see more details for each object by touching on the triangles (<img src=\"" + MesquiteFile.massageFilePathToURL(MesquiteModule.getRootPath() +"images" + MesquiteFile.fileSeparator + "infoBarTriangle.gif") + "\">) to open them";
		html += "</li>";

		html += "<li>If you touch on the object's name, you will get a menu allowing you to perform various actions, such as renaming or deleting the object.";
		html += "</li></ul>";
		html += "<p align=\"center\"><img  src=\"" + MesquiteFile.massageFilePathToURL(bfc.getPath()+"projectHTML" + MesquiteFile.fileSeparator + "projectPanel.jpg") + "\">";
		html += "</p></html></body>";
		bfc.alertHTML(bfc.containerOfModule().getParentFrame(), html, "Project", "Project", 500, 600);
	}
	void addExtraPanel(ProjPanelPanel p){
		elements.addElement(p);
		add(p);
		p.setSize(getWidth(), p.getHeight());
		resetSizes(getWidth(), getHeight());
		p.setVisible(true);
	}
	public void paint(Graphics g){

		super.paint(g);
		/*int vertical = 2;
		int w = getWidth();
		for (int i = 0; i<elements.size(); i++){
			ProjPanelPanel panel = ((ProjPanelPanel)elements.elementAt(i));
			int requestedlHeight = panel.getRequestedHeight(w);
			if (
				vertical += panel.requestSpacer();
			panel.setBounds(0, vertical, w, requestedlHeight);
			vertical += requestedlHeight;
		}*/
	}
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x,y,w,h);
		resetSizes(w, h);
	}
	public void setSize(int w, int h){
		super.setSize(w,h);
		resetSizes(w, h);
	}	

	public static void checkSizes(Component c, int offset){
		if (c==null)
			return;
		int thisOffset = c.getX() + offset;
		int extent = thisOffset + c.getWidth();
		if (c instanceof Container){
			Component[] cc = ((Container)c).getComponents();
			if (cc!=null && cc.length>0)
				for (int i=0; i<cc.length; i++)
					checkSizes(cc[i], thisOffset);
		}

	}

	boolean elementInBounds(int k, String typeName){
		if ( k<FileCoordinator.maxLinesOfAnyElementInPanel)
			return true;
		if (!FileCoordinator.maxLinesOfAnyElementInPanelQueried){ //setting hasn't yet been made
			int m = MesquiteInteger.queryInteger(w, "Maximum items of a kind in project panel", "Your file has more than " + FileCoordinator.maxLinesOfAnyElementInPanel + " " + typeName +
					". To save memory and time, Mesquite limits how many are shown in the project panel. If you'd like to change the limit, indicate a new maximum. You can always change this maximum " 
					+ " via the menu item File>Defaults>Maximum # Items in Project Panel...", FileCoordinator.maxLinesOfAnyElementInPanel);

			if (MesquiteInteger.isCombinable(m) && MesquiteInteger.isPositive(m)){
				FileCoordinator.maxLinesOfAnyElementInPanel = m;
				FileCoordinator.maxLinesOfAnyElementInPanelQueried = true;
			}
		}
		return k<FileCoordinator.maxLinesOfAnyElementInPanel;
	}
	//===============================================
	int count = 0;
	int sequenceUpToDate(){
		if (bfc.isDoomed())
			return 0;
		MesquiteProject proj = bfc.getProject();
		if (proj.refreshSuppression >0)
			return 0;
		int e = 0;
		if (e>= elements.size())
			return 11029;
		ProjPanelPanel ppanel = ((ProjPanelPanel)elements.elementAt(e));
		if (!(ppanel instanceof ProjectLabelPanel) || !ppanel.upToDate())
			return 222;
		((ProjectLabelPanel)ppanel).resetCommands();
		e++;
		Enumeration efi = bfc.getEmployeeVector().elements();
		while (efi.hasMoreElements()) {
			Object obj = efi.nextElement();
			if (obj instanceof FileInit){
				FileInit mbe = (FileInit)obj;
				ProjPanelPanel ppp = mbe.getProjectPanelPanel();
				if (ppp != null){
					ProjPanelPanel eppp = ((ProjPanelPanel)elements.elementAt(e));
					if (eppp.getOwnerModule() != mbe || !eppp.upToDate())
						return 45601;
					e++;
				}
			}
		}
		for (int i=0; i<proj.getNumberLinkedFiles(); i++){
			MesquiteFile mf = proj.getFile(i);
			if (e>= elements.size())
				return 1;
			ProjPanelPanel panel = ((ProjPanelPanel)elements.elementAt(e));
			if (!(panel instanceof FilePanel) || ((FilePanel)panel).mf != mf || !panel.upToDate())
				return 2;
			panel.resetTitle();
			panel.repaint();
			e++;
		}
		if (e>= elements.size())
			return 21;
		ProjPanelPanel fip;
		/*ProjPanelPanel fip = ((ProjPanelPanel)elements.elementAt(e));
		if (!(fip instanceof FileIncorporatePanel) || !fip.upToDate())
			return 22;
		e++;*/
		if (e>= elements.size())
			return 2001;
		fip = ((ProjPanelPanel)elements.elementAt(e));
		if (!(fip instanceof AddElementPanel) || !fip.upToDate())
			return 2001;
		e++;
		if (proj.taxas.size()>0){
			for (int i = 0; i< proj.taxas.size() && elementInBounds(i, "taxa blocks"); i++){
				Taxa t = (Taxa)proj.taxas.elementAt(i);
				if (e>= elements.size())
					return 3;
				ProjPanelPanel panel = ((ProjPanelPanel)elements.elementAt(e));
				if (!(panel instanceof ElementPanel) || ((ElementPanel)panel).element != t || !panel.upToDate()){
					return 4;
				}
				panel.resetTitle();
				panel.repaint();
				e++;
				if (proj.getNumberCharMatricesVisible(t)>0){
					e++; //for the title "Character Matrices"
					if (proj.getNumberCharMatricesVisible(t)<=FileCoordinator.maxLinesOfMatricesTreeBlocksSeparateInPanel)
						for (int k = 0; k<proj.getNumberCharMatricesVisible(t) && elementInBounds(k, "character matrices"); k++){   
							CharacterData data = proj.getCharacterMatrixVisible(t, k);
							if (data.isUserVisible()){
								if (e>= elements.size())
									return 5;
								panel = ((ProjPanelPanel)elements.elementAt(e));
								if (!(panel instanceof ElementPanel) || ((ElementPanel)panel).element != data || !panel.upToDate())
									return 6;
								panel.resetTitle();
								panel.repaint();
								e++;
							}
						}
				}
				if (proj.getNumberOfFileElements(TreeVector.class)>0){
					e++; //for the title "Tree Blocks"
					if (proj.getTreeVectors().size()<=FileCoordinator.maxLinesOfMatricesTreeBlocksSeparateInPanel)
						for (int k = 0; k<proj.getNumberOfFileElements(TreeVector.class) && elementInBounds(k, "tree blocks"); k++){
							TreeVector trees = (TreeVector)proj.getFileElement(TreeVector.class, k);
							if (e>= elements.size())
								return 7;
							panel = ((ProjPanelPanel)elements.elementAt(e));
							if (!(panel instanceof ElementPanel) || ((ElementPanel)panel).element != trees || !panel.upToDate())
								return 8;
							panel.resetTitle();
							panel.repaint();
							e++;
						}
				}

			}
			/*if (bfc.getProject().getCharacterModels().getNumNotBuiltIn()>0){
				if (e>= elements.size())
					return 9;
				ProjPanelPanel panel = ((ProjPanelPanel)elements.elementAt(e));
				if (!(panel instanceof CharModelsPanel) || !panel.upToDate())
					return 10;
				((CharModelsPanel)panel).refresh();
				e++;
			}*/
			ListableVector others = bfc.getProject().getOtherElements();
			if (others.size()>0){
				for (int i=0; i<others.size(); i++){
					FileElement f = (FileElement)others.elementAt(i);
					/*if (f instanceof TaxaGroupVector || f instanceof CharactersGroupVector){
						if (((ListableVector)f).size()>0){
							if (e>= elements.size())
								return 100;
							ProjPanelPanel panel = ((ProjPanelPanel)elements.elementAt(e));
							if (!(panel instanceof ElementPanel) || ((ElementPanel)panel).element != f || !panel.upToDate())
								return 101;
							((GroupsPanel)panel).refresh();
							e++;
						}
					}
					else */if (f instanceof TaxaAssociation){
						if (e>= elements.size())
							return 900;
						ProjPanelPanel panel = ((ProjPanelPanel)elements.elementAt(e));
						if (!(panel instanceof ElementPanel) || ((ElementPanel)panel).element != f || !panel.upToDate())
							return 901;
						panel.resetTitle();
						panel.repaint();
						e++;
					}
				}

			}
			if (e<elements.size())
				return 11;
			return 0;
		}
		else {
			/*if (bfc.getProject().getCharacterModels().getNumNotBuiltIn()>0){
				if (e>= elements.size())
					return 12;
				ProjPanelPanel panel = ((ProjPanelPanel)elements.elementAt(e));
				if (!(panel instanceof CharModelsPanel) || !panel.upToDate())
					return 13;
				((CharModelsPanel)panel).refresh();
				e++;
			}
			 */
			ListableVector others = bfc.getProject().getOtherElements();
			if (others.size()>0){
				for (int i=0; i<others.size(); i++){
					FileElement f = (FileElement)others.elementAt(i);
					/*if (f instanceof TaxaGroupVector || f instanceof CharactersGroupVector){
						if (((ListableVector)f).size()>0){
							if (e>= elements.size())
								return 102;
							ProjPanelPanel panel = ((ProjPanelPanel)elements.elementAt(e));
							if (!(panel instanceof ElementPanel) || ((ElementPanel)panel).element != f || !panel.upToDate())
								return 103;
							((GroupsPanel)panel).refresh();
							e++;
						}
					}
					else */if (f instanceof TaxaAssociation){
						if (e>= elements.size())
							return 902;
						ProjPanelPanel panel = ((ProjPanelPanel)elements.elementAt(e));
						if (!(panel instanceof ElementPanel) || ((ElementPanel)panel).element != f || !panel.upToDate())
							return 903;
						panel.resetTitle();
						panel.repaint();
						e++;
					}
				}

			}
			if (elements.size() != 0)
				return 14;
		}

		return 0;
	}
	public void refresh(FileElement e){
		for (int i = 0; i<elements.size(); i++){
			ClosablePanel panel = ((ClosablePanel)elements.elementAt(i));
			if (panel instanceof ElementPanel && ((ElementPanel)panel).element == e){
				((ElementPanel)panel).refreshIcon();
				panel.repaint();
			}
		}
	}
	//boolean fipOpen = false;
	//FileIncorporatePanel fip = null;
	public void refresh(){

		int sutd = sequenceUpToDate();  //integer passed to diagnose why not up to date, for debugging

		if (sutd==0){
			resetSizes();
			return;
		}
		//if (fip != null)
		//	fipOpen = fip.isOpen();
		for (int i = 0; i<elements.size(); i++){
			ClosablePanel panel = ((ClosablePanel)elements.elementAt(i));
			remove(panel);
			panel.dispose();
		}
		elements.removeAllElements();
		ElementPanel panel = null;
		MesquiteProject proj = bfc.getProject();

		addExtraPanel(panel = new ProjectLabelPanel(bfc, this, w,proj));
		Enumeration efi = bfc.getEmployeeVector().elements();
		while (efi.hasMoreElements()) {
			Object obj = efi.nextElement();
			if (obj instanceof FileInit){
				FileInit mbe = (FileInit)obj;
				ProjPanelPanel ppp = mbe.getProjectPanelPanel();
				if (ppp != null){
					ppp.setContainer(this);
					addExtraPanel(ppp);
				}
			}
		}

		panel.setLocation(0,0);
		for (int i=0; i<proj.getNumberLinkedFiles(); i++){
			MesquiteFile mf = proj.getFile(i);
			addExtraPanel(panel = new FilePanel(bfc, this, w,mf));
			panel.setSize(94, 10);
			panel.setLocation(0,0);
		}
		//addExtraPanel(fip = new FileIncorporatePanel(bfc,this,w));
		//fip.setOpen(fipOpen);
		panel.setLocation(0,0);
		addExtraPanel(panel = new AddElementPanel(bfc, this,w));
		panel.setOpen(false);
		panel.setLocation(0,0);
		if (proj.taxas.size()>0){
			for (int i = 0; i< proj.taxas.size() && elementInBounds(i, "taxa blocks"); i++){
				Taxa t = (Taxa)proj.taxas.elementAt(i);
				addExtraPanel(panel = new TaxaPanel(bfc, this, w, t));
				panel.setLocation(0,0);
				if (proj.getNumberCharMatricesVisible(t)>0){
					addExtraPanel(panel = new AbundanceMPanel(bfc, this, w, proj, t));
					if (proj.getNumberCharMatricesVisible(t)<=FileCoordinator.maxLinesOfMatricesTreeBlocksSeparateInPanel)
						for (int k = 0; k<proj.getNumberCharMatricesVisible(t) && elementInBounds(k, "character matrices"); k++){
							CharacterData data = proj.getCharacterMatrixVisible(t, k);
							if (data.isUserVisible()){
								if (data instanceof MolecularData)
									addExtraPanel(panel = new MolecMPanel(bfc, this, w,data));
								else if (data instanceof ContinuousData)
									addExtraPanel(panel = new ContMPanel(bfc, this, w, data));
								else if (data instanceof MeristicData)
									addExtraPanel(panel = new MeristicMPanel(bfc, this, w, data));
								else
									addExtraPanel(panel = new CategMPanel(bfc, this, w, data));
								panel.setLocation(0,0);
							}
						}
				}
				if (proj.getTreeVectors().size()>0){
					addExtraPanel(panel = new AbundanceTPanel(bfc, this, w, proj, t));
					if (proj.getTreeVectors().size()<=FileCoordinator.maxLinesOfMatricesTreeBlocksSeparateInPanel)
						for (int k = 0; k<proj.getNumberOfFileElements(TreeVector.class) && elementInBounds(k, "tree blocks"); k++){
							TreeVector trees = (TreeVector)proj.getFileElement(TreeVector.class, k);
							if (trees.getTaxa() == t){
								addExtraPanel(panel = new TreesRPanel(bfc, this, w, trees));
								panel.setLocation(0,0);
							}
						}
				}

			}
		}
		/*	if (bfc.getProject().getCharacterModels().getNumNotBuiltIn()>0){
			addExtraPanel(panel = new CharModelsPanel(bfc, this, w));
			panel.setLocation(0,0);
		}
		 */
		ListableVector others = bfc.getProject().getOtherElements();
		if (others.size()>0){
			for (int i=0; i<others.size(); i++){
				FileElement f = (FileElement)others.elementAt(i);
				/*if (f instanceof TaxaGroupVector || f instanceof CharactersGroupVector){
					if (((ListableVector)f).size()>0){
						addExtraPanel(panel = new GroupsPanel(bfc, this, w, (ListableVector)f));
						panel.setLocation(0,0);
					}
				}
				else */if (f instanceof TaxaAssociation){
					addExtraPanel(panel = new AssocPanel(bfc, this, w, f));
					panel.setLocation(0,0);
				}
			}

		}
		addExtraPanel(notesPanel = new NotesPanel(bfc, this, w));
		resetSizes();
	}
	int scrollOffset = 0;
	int lowestPanel = 0;
	int highestTaxaPanel = 0;
	void resetSizes(){
		resetSizes(getBounds().width, getBounds().height);
	}

	void resetSizes(int w, int h){
		if (bfc!=null && (bfc.isDoomed() ||  bfc.getProject().refreshSuppression>0))
			return;
		int max = getHeight();
		int vertical = 2;
		highestTaxaPanel = 0;
		for (int i = 0; i<elements.size(); i++){
			ProjPanelPanel panel = ((ProjPanelPanel)elements.elementAt(i));
			if (i<scrollOffset){
				panel.setBounds(0, 0, 0, 0);
			}
			else {
				int requestedlHeight = panel.getRequestedHeight(w);
				if(i>0)
					vertical += panel.requestSpacer();
				if (panel instanceof NotesPanel){
					requestedlHeight = max-vertical - 15;
				}
				if (vertical < max && vertical+requestedlHeight<max) {
					panel.setBounds(0, vertical, w, requestedlHeight);
					panel.setVisible(true);
				}
				else if (vertical < max && vertical+requestedlHeight>max){
					panel.setBounds(0, vertical, w, max-vertical);
					panel.setVisible(true);
				}
				else if (vertical > max && vertical+requestedlHeight>max){
					panel.setBounds(0, 0, 0, 0);
					panel.setVisible(false);
				}
				else {
					panel.setBounds(0, 0, 0, 0);
					panel.setVisible(false);
				}

				if (panel instanceof TaxaPanel && highestTaxaPanel == 0)
					highestTaxaPanel = vertical;
				if (requestedlHeight >=0)
					vertical += requestedlHeight;
				lowestPanel = vertical;
			}
		}
		if (scrollPanel != null)
			scrollPanel.repaint();
		MesquiteWindow.rpAll(this);
	}
	boolean canScrollUp(){
		if (scrollOffset==0 && lowestPanel < getHeight())
			return false;
		return true;
	}
	boolean canScrollDown(){
		if (scrollOffset == 0)
			return false;
		return true;
	}
	void scrollDown(){
		int was = scrollOffset;
		scrollOffset -= 1;
		if (scrollOffset <0)
			scrollOffset = 0;
		if (was != scrollOffset)
			resetSizes();
		if (scrollPanel!=null)
			scrollPanel.repaint();
	}
	void scrollUp(){
		if (scrollOffset==0 && lowestPanel < getHeight())
			return;
		int was = scrollOffset;
		scrollOffset += 1;
		if (scrollOffset > elements.size()-1)
			scrollOffset = elements.size()-1;
		if (was != scrollOffset)
			resetSizes();
		if (scrollPanel!=null)
			scrollPanel.repaint();
	}

}
/*======================================================================== */
class ScrollPanel extends MousePanel {
	ProjectPanel p;
	Image up, down, search; // query;
	int scrollLeft = 38;
	HelpSearchStrip searchStrip;
	int searchWidth = 130;
	int searchX = 20;
	int vOff = 4;

	public ScrollPanel(ProjectPanel p){
		super();
		this.p = p;
		p.scrollPanel = this;
		setLayout(null);
		searchStrip = new HelpSearchStrip(p.w, true);
		add(searchStrip);
		searchStrip.setSize(searchWidth, 15); 
		searchStrip.setLocation(searchX, vOff);
		searchStrip.setVisible(true);
		searchStrip.setText("");
		search = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath()+ "search.gif");
		up = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath()+ "uparrow.gif");
		down = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath()+ "downarrow.gif");
		//	query = MesquiteImage.getImage(p.bfc.getPath() + "projectHTML" + MesquiteFile.fileSeparator + "queryGray.gif");
		setBackground(ColorTheme.getExtInterfaceBackground());
	}
	public void paint(Graphics g){
		g.drawImage(search, 4, vOff, this);
		g.setColor(Color.darkGray);
		g.drawLine(0, 0, getWidth(), 0);
		if (!p.canScrollUp() && !p.canScrollDown())
			return;
		if (p.canScrollUp()){ 
			g.drawImage(up, searchX+searchWidth+4, vOff, this);
		}
		if (p.canScrollDown())
			g.drawImage(down, searchX+searchWidth+26, vOff, this);
	}
	/*public void setSize(int w, int h){
		super.setSize(w, h);
		searchStrip.setSize(w, 15);
	}
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x, y, w, h);
		searchStrip.setSize(w, 15);
	}*/
	public void mouseDown (int modifiers, int clickCount, long when, int x, int y, MesquiteTool tool) {
		//if modifiers include right click/control, then do dropdown menu
		if (y<=16 && x < 20) {
			searchStrip.search();
		}
		else if (y>=vOff && y<=vOff+26 && x>= searchX+searchWidth+4 && x < searchX+searchWidth + 22) {
			p.scrollUp();
		}
		else if (y>=vOff && y<=vOff+26 && x>= searchX+searchWidth+26 && x < searchX+searchWidth + 44) {
			p.scrollDown();
		}
	}


}
/*======================================================================== */
class AddElementPanel extends ElementPanel {

	public AddElementPanel(BasicFileCoordinator bfc, ClosablePanelContainer container,MesquiteWindow w){
		super(bfc, container, w, "Add...");
		setShowTriangle(false);
		setColors(ColorTheme.getExtInterfaceElement(), ColorTheme.getExtInterfaceElement(), ColorTheme.getExtInterfaceElement(), ColorTheme.getExtInterfaceTextMedium());
		addCommand(true, null, "New Taxa Block...", "New Taxa Block...",  new MesquiteCommand("newTaxa", (Commandable)bfc.findElementManager(Taxa.class)));
		addCommand(true, null, "New Character Matrix...", "New Character Matrix...",  new MesquiteCommand("newMatrix", (Commandable)bfc.findElementManager(CharacterData.class)));
		addCommand(true, null, "New Trees Block...", "New Trees Block...",  new MesquiteCommand("newTreeBlock", (Commandable)bfc.findElementManager(TreeVector.class)));
		addCommand(true, null, "-", "-",  null);
		addCommand(true, null, "-", "Read Other File",  null);
		addCommand(false, "queryGray.gif", null, "Explanation...",  new MesquiteCommand("explainIncorporate", bfc.getModuleWindow()));
		addCommand(false, "fileLink.gif", "Link\nFile", "Link File...", new MesquiteCommand("linkFile", bfc));
		addCommand(false, "fileInclude.gif", "Include\nFile", "Include File...", new MesquiteCommand("includeFile", bfc));
		MesquiteCommand c = new MesquiteCommand("newAssistant", bfc);
		c.setDefaultArguments("#mesquite.dmanager.FuseTaxaMatrices.FuseTaxaMatrices");
		addCommand(false, "fileMergeTM.gif", "Merge Taxa\n& Matrices", "Merge Taxa/Matrices...", c);
		ElementManager tm = bfc.findElementManager(TreeVector.class);
		addCommand(false, "fileLinkTrees.gif", "Link\nTrees", "Link Trees...", new MesquiteCommand("linkTreeFile", ((MesquiteModule)tm)));
		addCommand(false, "fileIncludeTrees.gif", "Include\nTrees", "Include Trees...", new MesquiteCommand("includeTreeFileAskPartial", ((MesquiteModule)tm)));
		addCommand(true, null, "-", "-",  null);
		addCommand(true, null, "New Linked File...", "New Linked File...",  bfc.getProject().getNewLinkFileCommand());
	}

}

/*======================================================================== *
class NewElementPanel extends ElementPanel {

	public NewElementPanel(BasicFileCoordinator bfc, ClosablePanelContainer container,MesquiteWindow w){
		super(bfc, container, w, "New...");
		setShowTriangle(false);
		setColors(ColorTheme.getExtInterfaceElement(), ColorTheme.getExtInterfaceElement(), ColorTheme.getExtInterfaceElement(), ColorTheme.getExtInterfaceTextMedium());
		addCommand(true, null, "New Taxa Block...", "New Taxa Block...",  new MesquiteCommand("newTaxa", (Commandable)bfc.findElementManager(Taxa.class)));
		addCommand(true, null, "New Character Matrix...", "New Character Matrix...",  new MesquiteCommand("newMatrix", (Commandable)bfc.findElementManager(CharacterData.class)));
		addCommand(true, null, "New Trees Block...", "New Trees Block...",  new MesquiteCommand("newTreeBlock", (Commandable)bfc.findElementManager(TreeVector.class)));
	}

}

/*======================================================================== *
class FileIncorporatePanel extends ElementPanel {

	public FileIncorporatePanel(BasicFileCoordinator bfc, ClosablePanelContainer container, MesquiteWindow w){
		super(bfc, container, w, "Read from File...");
		//	setColors(ColorDistribution.veryVeryVeryLightGray, ColorDistribution.veryVeryVeryLightGray, ColorDistribution.veryVeryVeryLightGray, Color.black);
		//	Color cLight = ColorTheme.getExtInterfaceBackground();
		//		Color cDark = ColorTheme.getExtInterfaceElement();
		//		setColors(cLight, cLight, cLight, cDark);
		setColors(ColorTheme.getExtInterfaceElement(), ColorTheme.getExtInterfaceElement(), ColorTheme.getExtInterfaceElement(), ColorTheme.getExtInterfaceTextMedium());
		addCommand(false, "queryGray.gif", null, "Explanation...",  new MesquiteCommand("explainIncorporate", bfc.getModuleWindow()));
		addCommand(false, "fileLink.gif", "Link\nFile", "Link File...", new MesquiteCommand("linkFile", bfc));
		addCommand(false, "fileInclude.gif", "Include\nFile", "Include File...", new MesquiteCommand("includeFile", bfc));
		MesquiteCommand c = new MesquiteCommand("newAssistant", bfc);
		c.setDefaultArguments("#mesquite.dmanager.FuseTaxaMatrices.FuseTaxaMatrices");
		addCommand(false, "fileMergeTM.gif", "Merge Taxa\n& Matrices", "Merge Taxa/Matrices...", c);
		ElementManager tm = bfc.findElementManager(TreeVector.class);
		addCommand(false, "fileLinkTrees.gif", "Link\nTrees", "Link Trees...", new MesquiteCommand("linkTreeFile", ((MesquiteModule)tm)));
		addCommand(false, "fileIncludeTrees.gif", "Include\nTrees", "Include Trees...", new MesquiteCommand("includeTreeFileAskPartial", ((MesquiteModule)tm)));
		setWholeOpen(true);
	}

}
/*======================================================================== */
class ProjectLabelPanel extends ElementPanel {
	MesquiteProject project;
	String[] names;
	public ProjectLabelPanel(BasicFileCoordinator bfc, ClosablePanelContainer container, MesquiteWindow w, MesquiteProject project){
		super(bfc, container, w, "Project");
		setShowTriangle(false);
		this.project = project;

		resetCommands();
	}

	public boolean getBold(){
		return true;
	}
	public String getFootnote(){
		if (project == null)
			return null;
		String text = "Project with home file " + project.getHomeFileName();
		int numTaxaBlocks = project.getNumberOfFileElements(Taxa.class);
		int numMatrices = project.getNumberCharMatrices();
		int numTreeBlocks = project.getNumberOfFileElements(TreeVector.class);
		if (numTaxaBlocks>0){
			text += ", with";
			if (numTaxaBlocks == 1)
				text += "\n  " + numTaxaBlocks + " taxa block";
			else if (numTaxaBlocks > 1)
				text += "\n  " + numTaxaBlocks + " taxa blocks";
			if (numMatrices==1)
				text += ";\n  " + numMatrices + " character matrix";
			else if (numMatrices>1)
				text += ";\n  " + numMatrices + " character matrices";
			if (numTreeBlocks==1)
				text += ";\n  "  + numTreeBlocks + " tree block";
			else if (numTreeBlocks>1)
				text += ";\n  "  + numTreeBlocks + " tree blocks";
		}
		return text;
	}
	protected String getMenuHeading(){
		if (project.hasName())
			return "Project: " + project.getName();
		else
			return project.getName();
	}
	public void resetCommands(){
		deleteAllCommands();
		addCommand(true, null, "Name of Project...", "Name of Project...", new MesquiteCommand("setName", project));
		int numProjects = MesquiteTrunk.getProjectList().getNumProjects();
		if (numProjects<2)
			return;
		names = new String[numProjects-1];
		addCommand(true, null, "-", "-", null);
		int k = 0;
		addCommand(true, null, "Other Projects:", "Other Projects:", null);
		for (int i = 0; i< numProjects; i++){
			MesquiteProject proj = MesquiteTrunk.getProjectList().getProject(i);
			if (proj != project && proj != null) {
				addCommand(true, null, proj.getHomeFileName(), proj.getHomeFileName(), new MesquiteCommand("allToFront", proj.getCoordinatorModule()));
				names[k++] = proj.getHomeFileName();
			}
		}
	}


}
/*======================================================================== */
class NotesPanel extends ProjPanelPanel {
	StringInABox textBox, headingBox;
	String text = null;
	String heading = null;
	public NotesPanel(BasicFileCoordinator bfc, ClosablePanelContainer container, MesquiteWindow w){
		super(bfc, container, w, "Notes", bfc);
		headingBox =  new StringInABox("", new Font("SansSerif", Font.BOLD, MesquiteFrame.resourcesFontSize), getWidth());
		textBox =  new StringInABox("", new Font("SansSerif", Font.PLAIN, MesquiteFrame.resourcesFontSize), getWidth());
		setText(null, null);
	}
	public int getRequestedHeight(int width){
		return 200;
	}
	void setText(String h, String t){
		text = t;
		heading = h;
		headingBox.setString(h);
		textBox.setString(t);
		repaint();
	}
	protected void resetSizes(int w, int h){
		if (textBox != null){
			headingBox.setWidth(w-24);
			textBox.setWidth(w-24);
		}
	}

	public void paint(Graphics g){
		if (text != null){
			g.setColor(Color.lightGray);
			g.fillRect(8, 12, getWidth()-16, 2);
			g.setColor(ColorTheme.getExtInterfaceTextMedium());
			if (heading != null){
				headingBox.draw(g, 16,16);

				textBox.draw(g, 16,headingBox.getHeight() + 16);
			}
			else
				textBox.draw(g, 16,16);

		}
	}
}

/*======================================================================== */
class FilePanel extends ElementPanel {
	MesquiteFile mf;
	public FilePanel(BasicFileCoordinator bfc, ClosablePanelContainer container, MesquiteWindow w, MesquiteFile mf){
		super(bfc, container, w,mf.getFileName());
		setShowTriangle(false);
		this.mf = mf;
		if (mf == bfc.getProject().getHomeFile())
			im = 	MesquiteImage.getImage(bfc.getPath()+ "projectHTML" + MesquiteFile.fileSeparator + "fileSmall.gif");
		else
			im = 	MesquiteImage.getImage(bfc.getPath()+ "projectHTML" + MesquiteFile.fileSeparator + "fileLinkedSmall.gif");
		//	Color cLight = ColorDistribution.projectLight[bfc.getProject().getProjectColor()];
		//	Color cDark = ColorDistribution.projectDark[bfc.getProject().getProjectColor()];
		//	setColors(cLight, cLight, cLight, cDark);
		//	setColors(ColorDistribution.veryVeryVeryLightGray, ColorDistribution.veryVeryVeryLightGray, ColorDistribution.veryVeryVeryLightGray, Color.black);
		setColors(ColorTheme.getExtInterfaceElement(), ColorTheme.getExtInterfaceElement(), ColorTheme.getExtInterfaceElement(), ColorTheme.getExtInterfaceTextMedium());
		if (mf.isLocal()){
			addCommand(true, null, "Show File Location on Disk", "Show File Location on Disk", new MesquiteCommand("show", this));
			addCommand(true, null, "-", "-", null);
			addCommand(true, null, "Save File", "Save File", new MesquiteCommand("save", mf));
		}
		addCommand(true, null, "Save File As...", "Save File As...", new MesquiteCommand("saveAs", mf));
		addCommand(true, null, "Close File", "Close File", new MesquiteCommand("close", mf));


	}
	public void resetTitle(){
		setTitle(mf.getFileName());
		repaint();
	}
	public String getFootnote(){
		if (mf == null || mf.getDirectoryName() == null)
			return null;
		return "Location of file: " + mf.getDirectoryName();
	}
	/*.................................................................................................................*/
	public String getElementTypeName(){ 
		return "File";
	}
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Shows file location on disk", null, commandName, "show")) {
			if (mf == null || mf.getDirectoryName() == null)
				return null;
			MesquiteFile.showDirectory(mf.getDirectoryName());
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

}

/*======================================================================== */
class TaxaPanel extends ElementPanel {

	public TaxaPanel(BasicFileCoordinator bfc, ClosablePanelContainer container, MesquiteWindow w, FileElement element){
		super(bfc, container, w, element);
		//	setColors(ColorDistribution.veryLightGray, ColorDistribution.veryVeryLightGray, Color.lightGray, Color.black);
		if (bfc.getProject().virginProject)
			setOpen(true);
		setColors(ColorTheme.getExtInterfaceElement(), ColorTheme.getExtInterfaceElement(), ColorTheme.getExtInterfaceElement(), ColorTheme.getExtInterfaceTextMedium());
		addCommand(false, "list.gif", "List &\nManage\nTaxa", "List & Manage Taxa", new MesquiteCommand("showMe", element));
		if (otherMatch()) {
			addCommand(true, null, "-", "-", null);
			addCommand(true, null, "(NOTE: Taxa block duplicated? Select for details...)", "(NOTE: Taxa block duplicated? Select for details...)", new MesquiteCommand("duplicatedInfo", this));  
			addCommand(true, null, "Copy Matrices to other Taxa Block", "Copy Matrices to other Taxa Block", new MesquiteCommand("transferMatrices", this));  
			addCommand(true, null, "Copy Trees to other Taxa Block", "Copy Trees to other Taxa Block", new MesquiteCommand("transferTrees", this)); 
			addCommand(true, null, "Merge Matrices and Trees with other Taxa Block", "Merge Matrices and Trees with other Taxa Block", new MesquiteCommand("mergeBlock", this)); 
			addCommand(true, null, "-", "-", null);
		}
		addCommand(false, "chart.gif", "Chart\nTaxa", "Chart Taxa", new MesquiteCommand("chart", this));
		addCommand(true, null, "Show New\nTree Window", "Show New Tree Window", new MesquiteCommand("showInitTreeWindow", this));
		addCommand(true, null, "-", "-", null);
		addCommand(true, null, "Rename Taxa Block", "Rename Taxa Block", new MesquiteCommand("renameMe", element));
		addCommand(true, null, "Delete Taxa Block", "Delete Taxa Block", new MesquiteCommand("deleteMe", element));
		addCommand(true, null, "-", "-", null);
		addCommand(true, null, "Edit Comment", "Edit Comment", new MesquiteCommand("editComment", element));

	}

	public String getTitleAddition(){
		int numTaxa = ((Taxa)element).getNumTaxa();
		String heading = " (" + numTaxa + " tax";
		if (numTaxa>1)
			heading += "a)";
		else
			heading += "on)";
		return heading;
	}


	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Shows an initial tree window", null, commandName, "showInitTreeWindow")) {
			//CommandRecord oldCommandRec = MesquiteThread.getCurrentCommandRecord();
			//CommandRecord scriptRec = new CommandRecord(true);
			//MesquiteThread.setCurrentCommandRecord(scriptRec);
			((BasicFileCoordinator)bfc).showInitTreeWindow((Taxa)element, true);
			//MesquiteThread.setCurrentCommandRecord(oldCommandRec);
		}
		else if (checker.compare(this.getClass(), "Shows an initial tree window", null, commandName, "duplicatedInfo")) {
			((BasicFileCoordinator)bfc).alert("This taxa block appears to be a duplicate of at least one other, because it has the same number of taxa and with the same names. " +
					"Calculations with this taxa block will not have access to matrices and trees of the other block, and vice versa. " + 
					"\n\nIf this was unintentional, you could choose the following menu items to transfer character matrices and trees to the other block. "+
					"\n\nTo avoid this problem in the future, when combining separate files, try using options other than Include or Link under Include & Merge.");
		}
		else if (checker.compare(this.getClass(), "Transfers matrices to other taxa block", null, commandName, "transferMatrices")) {
			Taxa taxa = (Taxa)element;
			Taxa other = chooseOther(taxa, "Choose taxa block to which to copy the matrices");
			if (other == null)
				return null;
			transferMatrices(taxa, other);
			projectWindow.projPanel.refresh();
		}
		else if (checker.compare(this.getClass(), "Transfers matrices to other taxa block", null, commandName, "transferTrees")) {
			Taxa taxa = (Taxa)element;
			Taxa other = chooseOther(taxa, "Choose taxa block to which to copy the trees");
			if (other == null)
				return null;
			transferTrees(taxa, other);
			projectWindow.projPanel.refresh();
		}
		else if (checker.compare(this.getClass(), "Transfers matrices and trees to other taxa block and then deletes this taxa block", null, commandName, "mergeBlock")) {
			Taxa taxa = (Taxa)element;
			Taxa other = chooseOther(taxa, "Choose taxa block into which to merge this one, copying this one's matrices and trees before deleting it.");
			if (other == null)
				return null;
			transferMatrices(taxa, other);
			transferTrees(taxa, other);
			taxa.deleteMe(false);
			projectWindow.projPanel.refresh();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	
	/* - - - - - - - - - - - - - - - - - - - - */
	void transferMatrices(Taxa taxa, Taxa other){
		MesquiteProject project = taxa.getProject();
		boolean reordered = taxa.matchOrderIfEqual(other);
		for (int iM = 0; iM < project.getNumberCharMatrices(taxa); iM++){
			CharacterData data = project.getCharacterMatrix(taxa, iM);
			CharacterData cloned = data.cloneData();
			cloned.addToFile(project.getHomeFile(),project, bfc.findElementManager(CharacterData.class));  
			data.copyMetadataTo(cloned);
			cloned.setTaxa(other, true);
			cloned.setName(data.getName());
			cloned.setAnnotation(data.getAnnotation(), false);
		}
		if (reordered)
			taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_MOVED));
	}
	/* - - - - - - - - - - - - - - - - - - - - */
	void transferTrees(Taxa taxa, Taxa other){
		MesquiteProject project = taxa.getProject();
		boolean reordered = taxa.matchOrderIfEqual(other);
		for (int iM = 0; iM < project.getNumberTreeVectors(taxa); iM++){
			TreeVector trees = project.getTreesByNumber(taxa, iM);
			TreeVector otherTrees = new TreeVector(other);
			otherTrees.setWriteWeights(trees.getWriteWeights());
			for (int i=0; i<trees.size(); i++){
				MesquiteTree t = (MesquiteTree)trees.elementAt(i);
				MesquiteTree cloned = t.cloneTree();
				cloned.setTaxa(other, true);
				otherTrees.addElement(cloned, false);

			}
			otherTrees.addToFile(project.getHomeFile(),project, bfc.findElementManager(TreeVector.class));  
			otherTrees.setName(trees.getName());
			otherTrees.setAnnotation(trees.getAnnotation(), false);
		}
		if (reordered)
			taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_MOVED));
	}	
	/* - - - - - - - - - - - - - - - - - - - - */
	Taxa chooseOther(Taxa taxa, String expl){
		MesquiteProject project = taxa.getProject();
		Taxa other = null;
		ListableVector v = new ListableVector();
		for (int iT = 0; iT < project.getNumberTaxas(); iT++){
			Taxa c = taxa.getProject().getTaxa(iT);
			if (c != taxa && c.equals(taxa, true, true))
				v.addElement(c, false);
		}
		if (v.size() == 0)
			return null;
		else if (v.size() == 1){
			other = (Taxa)v.elementAt(0);
		}
		else {
			Listable result = ListDialog.queryList(bfc.containerOfModule(), "Choose taxa block", expl, null, v, 0); 
			if (result == null)
				return null;
			other = (Taxa)result;
		}
		return other;
	}
	/* - - - - - - - - - - - - - - - - - - - - */
	boolean otherMatch(){
		if (element == null)
			return false;
		Taxa taxa = (Taxa)element;
		MesquiteProject project = taxa.getProject();
		for (int iT = 0; iT < project.getNumberTaxas(); iT++){
			Taxa c = taxa.getProject().getTaxa(iT);
			if (c != taxa && c.equals(taxa, true, true))
				return true;
		}
		return false;
	}
	
	/* - - - - - - - - - - - - - - - - - - - - */
	public String getIconFileName(){
		if (otherMatch())
			return "warning.gif";
		return null;
	}
	/*.................................................................................................................*/
	public String getElementTypeName(){ 
		return "Taxa Block";
	}
	public int requestSpacer(){
		return 16;
	}
	public void chart(){
		String mID = Long.toString(((FileElement)element).getID());
		MesquiteThread.addHint(new MesquiteString("TaxonValuesChart", mID));
		if (MesquiteDialog.useWizards)
			MesquiteThread.triggerWizard();
		((BasicFileCoordinator)bfc).showChartWizard("Taxa");
		if (MesquiteDialog.useWizards)
			MesquiteThread.detriggerWizard();
	}
	public String getNotes(){
		if(element == null)
			return "";
		return Integer.toString(((Taxa)element).getNumTaxa()) + " Taxa";
	}

}
/*======================================================================== */
class MElementPanel extends ElementPanel {
	CharacterData data = null;

	public MElementPanel(BasicFileCoordinator bfc, ClosablePanelContainer container, MesquiteWindow w, FileElement element){
		super(bfc, container,w, element);
		addCommand(false, getShowMatrixIconFileName(), "Show\nMatrix", "Show Matrix", new MesquiteCommand("showMe", element));
		addCommand(false, "list.gif", "List &\nManage\nCharacters", "List & Manage Characters", new MesquiteCommand("list", this));
		addCommand(false, "chart.gif", "Chart\nCharacters", "Chart Characters", new MesquiteCommand("chart", this));
		addCommand(true, null, "-", "-", null);
		addCommand(true, null, "Rename Matrix", "Rename Matrix", new MesquiteCommand("renameMe", element));
		addCommand(true, null, "Delete Matrix", "Delete Matrix", new MesquiteCommand("deleteMe", element));
		addCommand(true, null, "Duplicate Matrix", "Duplicate Matrix", new MesquiteCommand("duplicateMe", element));
		addCommand(true, null, "Export Matrix", "Export Matrix", new MesquiteCommand("exportMe", element));
		addCommand(true, null, "-", "-", null);
		addCommand(true, null, "Edit Comment", "Edit Comment", new MesquiteCommand("editComment", element));
		if (bfc.getProject().virginProject)
			setOpen(true);
		if (element instanceof CharacterData)
			data = (CharacterData)element;
		//addCommand(true, null, "ID " + element.getID(), "ID " + element.getID(), new MesquiteCommand("id", this));

	}
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Lists the characters", null, commandName, "list")) {
			((CharacterData)element).showList();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public String getTitleAddition(){
		int numChars = ((CharacterData)element).getNumChars();
		String heading = " (" + numChars + " character";
		if (numChars>1)
			heading += "s";
		heading += ")";

		return heading;
	}
	/*.................................................................................................................*/
	public String getElementTypeName(){ 
		return "Matrix";
	}
	public String getNotes(){
		if (element == null)
			return "";
		return Integer.toString(((CharacterData)element).getNumChars()) + " Characters";
	}
	public String getShowMatrixIconFileName(){ //for small 16 pixel icon at left of main bar
		return "matrixCateg.gif";
	}
	public void actUponDroppedFileContents(FileInterpreter fileInterpreter, String path) {
		if (fileInterpreter!=null) {
			Taxa taxa = data.getTaxa();
			((ReadFileFromString)fileInterpreter).readFileFromString( data,  taxa,  MesquiteFile.getFileContentsAsString(path), MesquiteFile.getFileNameFromFilePath(path), "");
			taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
			data.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED, null, null));
			data.notifyInLinked(new Notification(MesquiteListener.PARTS_ADDED, null, null));
			data.notifyListeners(this, new Notification(CharacterData.DATA_CHANGED, null, null));

		}

	}
	public FileInterpreter findFileInterpreter(String droppedContents, String fileName) {
		if (w!=null) {
			FileCoordinator fileCoord = w.getOwnerModule().getFileCoordinator();
			FileInterpreter fileInterpreter = fileCoord.findImporter(droppedContents, fileName,0, "",true, DNAState.class);  
			return fileInterpreter;
		} 
		return null;
	}

	/*.................................................................................................................*/
	public void processFilesDroppedOnPanel(List files) {
		int count = 0;
		FileInterpreter fileInterpreter =null;
		for (Iterator iter = files.iterator(); iter.hasNext();) {
			File nextFile = (File) iter.next();
			if (!askListenersToProcess(nextFile, true)) {
				if (count==0) {
					fileInterpreter = findFileInterpreter(MesquiteFile.getFileContentsAsString(nextFile.getAbsolutePath()), nextFile.getName());  
					if (fileInterpreter== null)
						return;
				}
				//system.out.println("next file dropped is: " + nextFile);
				MesquiteMessage.println("\n\nReading file " + nextFile.getName());
				CommandRecord.tick("\n\nReading file " + nextFile.getName());
				actUponDroppedFileContents(fileInterpreter, nextFile.getAbsolutePath());
				count++;
			}
		}
	}
	/*.................................................................................................................*/
	public void processFileStringDroppedOnPanel(String path) {
		String contents = MesquiteFile.getURLContentsAsString(path, -1);
		FileInterpreter fileInterpreter =null;
		fileInterpreter = findFileInterpreter(contents, "File Contents");  
		if (fileInterpreter!= null)
			actUponDroppedFileContents(fileInterpreter, path);

	}

	public void chart(){
		String mID = Long.toString(((FileElement)element).getID());
		String tID = Long.toString(((CharacterData)element).getTaxa().getID());
		MesquiteThread.addHint(new MesquiteString("CharacterValuesChart", tID));
		MesquiteThread.addHint(new MesquiteString("CharSrcCoordObed", "#StoredCharacters"));
		MesquiteThread.addHint(new MesquiteString("StoredCharacters", mID));
		if (MesquiteDialog.useWizards)
			MesquiteThread.triggerWizard();

		((BasicFileCoordinator)bfc).showChartWizard("Characters");
		if (MesquiteDialog.useWizards)
			MesquiteThread.detriggerWizard();
	}

}
/*======================================================================== */
class MeristicMPanel extends MElementPanel {

	public MeristicMPanel(BasicFileCoordinator bfc, ClosablePanelContainer container, MesquiteWindow w, FileElement element){
		super(bfc, container, w, element);
	}
	public String getShowMatrixIconFileName(){ //for small 16 pixel icon at left of main bar
		return "matrixMeristic.gif";
	}
}
/*======================================================================== */
class CategMPanel extends MElementPanel {

	public CategMPanel(BasicFileCoordinator bfc, ClosablePanelContainer container, MesquiteWindow w, FileElement element){
		super(bfc, container, w, element);
	}
	public String getShowMatrixIconFileName(){ //for small 16 pixel icon at left of main bar
		return "matrixCateg.gif";
	}
}
/*======================================================================== */
class MolecMPanel extends MElementPanel {

	public MolecMPanel(BasicFileCoordinator bfc, ClosablePanelContainer container,MesquiteWindow w,  FileElement element){
		super(bfc, container, w,element);

	}
	public String getShowMatrixIconFileName(){ //for small 16 pixel icon at left of main bar
		return "matrixMolec.gif";
	}
}
/*======================================================================== */
class ContMPanel extends MElementPanel {

	public ContMPanel(BasicFileCoordinator bfc, ClosablePanelContainer container, MesquiteWindow w, FileElement element){
		super(bfc, container, w, element);

	}
	public String getShowMatrixIconFileName(){ //for small 16 pixel icon at left of main bar
		return "matrixCont.gif";
	}
}
/*======================================================================== */
class AbundanceMPanel extends ElementPanel {
	MesquiteProject project = null;
	Taxa taxa = null;
	public AbundanceMPanel(BasicFileCoordinator bfc, ClosablePanelContainer container,MesquiteWindow w,  MesquiteProject project, Taxa taxa){
		super(bfc, container, w,project.getCharacterMatrices());
		addCommand(true, null, "View List", "View List", new MesquiteCommand("showMe", project.getCharacterMatrices()));
		this.taxa = taxa;
		this.project = project;
	}
	public String getTitle(){
		int n = project.getNumberCharMatricesVisible(taxa);
		if (n>1)
			return Integer.toString(n) + " Character Matrices";
		return "1 Character Matrix";

	}
}
/*======================================================================== */
class AbundanceTPanel extends ElementPanel {
	MesquiteProject project = null;
	Taxa taxa = null;
	public AbundanceTPanel(BasicFileCoordinator bfc, ClosablePanelContainer container,MesquiteWindow w,  MesquiteProject project, Taxa taxa){
		super(bfc, container, w,project.getTreeVectors());
		addCommand(true, null, "View List", "View List", new MesquiteCommand("showMe", project.getTreeVectors()));
		this.taxa = taxa;
		this.project = project;
	}
	public String getTitle(){
		int n = project.getNumberTreeVectors(taxa);
		if (n>1)
			return Integer.toString(n) + " Tree Blocks";
		return "Tree Block";
	}
}
/*======================================================================== */
class TreesRPanel extends ElementPanel {

	public TreesRPanel(BasicFileCoordinator bfc, ClosablePanelContainer container, MesquiteWindow w, FileElement element){
		super(bfc, container, w, element);

		addCommand(false, "treeView.gif", "View\nTrees", "View Trees ", new MesquiteCommand("showTreesInWindow", element));

		addCommand(false, null, "View\nConsensus", "View Consensus", new MesquiteCommand("showConsensusInWindow", element));
		addCommand(false, "trees.gif", "List &\nManage\nTrees", "List & Manage Trees", new MesquiteCommand("showMe", element));
		addCommand(false, "chart.gif", "Chart\nTrees", "Chart Trees", new MesquiteCommand("chart", this));
		addCommand(true, null, "-", "-", null);

		if (!StringUtil.blank(getAnalysisDirectoryPath(element)))
			addCommand(true, null, "Show Analysis Folder", "Show Analysis Folder", new MesquiteCommand("showAnalysisDirectory", this));

		addCommand(true, null, "Rename Trees Block", "Rename Trees Block", new MesquiteCommand("renameMe", element));
		addCommand(true, null, "Delete Trees Block", "Delete Trees Block", new MesquiteCommand("deleteMe", element));
		addCommand(true, null, "Duplicate Trees Block", "Duplicate Trees Block", new MesquiteCommand("duplicateMe", element));
		addCommand(true, null, "Export Trees Block", "Export Trees Block", new MesquiteCommand("exportMe", element));
		addCommand(true, null, "-", "-", null);
		addCommand(true, null, "Edit Comment", "Edit Comment", new MesquiteCommand("editComment", element));
		if (bfc.getProject().virginProject)
			setOpen(true);
		//	addCommand(true, null, "ID " + element.getID(), "ID " + element.getID(), new MesquiteCommand("id", this));

	}

	public String getFootnote(){
		String s = " Tree";
		int numTrees = ((TreeVector)element).size();
		if (numTrees != 1)
			s += "s";
		String f = super.getFootnote();
		if (f == null)
			f = "";
		return "" + numTrees + s + "\n" + f;
	}

	/*.................................................................................................................*/
	String getAnalysisDirectoryPath(FileElement element) {
		if (element instanceof TreeVector) {
			String annot = element.getAnnotation();
			if (annot != null) {
				String targetHeading = "Results stored in folder:";
				int i = StringUtil.indexOfIgnoreCase(annot, targetHeading);
				if (i>=0) {
					Parser parser = new Parser(annot);
					parser.setPosition(i + targetHeading.length());
					String path = parser.getRawNextLine();
					path = StringUtil.stripLeadingWhitespace(path);
					if (MesquiteFile.fileOrDirectoryExists(path)) {
						return path;
					}
				}
			}
		}
		return null;
	}
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(MesquiteWindow.class, "Shows analysis directory of element", null, commandName, "showAnalysisDirectory")) {
			String dir = getAnalysisDirectoryPath((FileElement)getElement());
			if (!StringUtil.blank(dir)) //show file location on  disk
				MesquiteFile.showDirectory(dir);
		}
		else return  super.doCommand(commandName, arguments, checker);
		return null;

	}
	/*.................................................................................................................*/
	public String getElementTypeName(){ 
		return "Trees Block";
	}
	public void chart(){
		String mID = Long.toString(((TreeVector)element).getID());
		String tID = Long.toString(((TreeVector)element).getTaxa().getID());
		MesquiteThread.addHint(new MesquiteString("TreeValuesChart", tID));
		MesquiteThread.addHint(new MesquiteString("TreeValuesChart", "#StoredTrees"));
		MesquiteThread.addHint(new MesquiteString("StoredTrees", mID));
		if (MesquiteDialog.useWizards)
			MesquiteThread.triggerWizard();
		((BasicFileCoordinator)bfc).showChartWizard("Trees");
		if (MesquiteDialog.useWizards)
			MesquiteThread.detriggerWizard();
	}
	public String getTitleAddition(){
		int numTrees = ((TreeVector)element).size();
		String heading = " (" + numTrees + " tree";
		if (numTrees>1)
			heading += "s";
		heading += ")";
		return heading;
	}

	public String getNotes(){
		if (element == null)
			return "";
		return Integer.toString(((TreeVector)element).size()) + " Trees";
	}
}
/*======================================================================== */
class AssocPanel extends ElementPanel {
	public AssocPanel(BasicFileCoordinator bfc, ClosablePanelContainer container, MesquiteWindow w, FileElement element){
		super(bfc, container, w,element);
		setTitle(element.getName());
		setColors(ColorTheme.getExtInterfaceElement(), ColorTheme.getExtInterfaceElement(), ColorTheme.getExtInterfaceElement(), ColorTheme.getExtInterfaceTextMedium());
		addCommand(true, null, "Edit Associaton", "Edit Associaton", new MesquiteCommand("showMe", element));
		addCommand(true, null, "-", "-", null);
		addCommand(true, null, "Rename Association", "Rename Association", new MesquiteCommand("renameMe", element));
		addCommand(true, null, "Delete Association", "Delete Association", new MesquiteCommand("deleteMe", element));
		//	addCommand(true, null, "ID " + element.getID(), "ID " + element.getID(), new MesquiteCommand("id", this));

	}
	public void resetTitle(){
		setTitle("Association: " + element.getName());
		repaint();
	}
	public int requestSpacer(){
		return 16;
	}
	/*.................................................................................................................*/
	public String getElementTypeName(){ 
		return "Taxa Assocation";
	}

	public String getNotes(){
		if (element == null)
			return "";
		return "Association between " + ((TaxaAssociation)element).getTaxa(0).getName() + " and " +  ((TaxaAssociation)element).getTaxa(1).getName();
	}
}
/*======================================================================== *
class CharModelsPanel extends ElementPanel {
	public CharModelsPanel(BasicFileCoordinator bfc, ClosablePanelContainer container,MesquiteWindow w){
		super(bfc, container, w,"Character Models");
		//	setColors(ColorDistribution.veryVeryLightGray, ColorDistribution.veryVeryVeryLightGray, Color.lightGray, Color.darkGray);
		setColors(ColorTheme.getExtInterfaceElement(), ColorTheme.getExtInterfaceElement(), ColorTheme.getExtInterfaceElement(), ColorTheme.getExtInterfaceTextMedium());
		MesquiteProject proj = bfc.getProject();
		proj.getCentralModelListener().addListener(this);
		refresh();
	}
	//PUT COMMANDS AT TOP????

	public void dispose(){
		if (bfc != null && !bfc.isDoomed())
			bfc.getProject().getCentralModelListener().removeListener(this);
		super.dispose();
	}
	public String getNotes(){
		return null;
	}
	public boolean upToDate(){
		int e = 0;
		MesquiteProject proj = bfc.getProject();
		for (int i=0; i<proj.getNumModels(); i++){
			CharacterModel m = proj.getCharacterModel(i);
			if (!m.isBuiltIn() && m.isUserVisible()){
				if (e>= subPanels.size())
					return false;
				ElementPanel panel = ((ElementPanel)subPanels.elementAt(e));
				if (panel.element != m)
					return false;
				panel.resetTitle();
				panel.repaint();
				e++;
			}
		}
		if (e<subPanels.size())
			return false;
		return true;
	}
	public void refresh(){
		if (upToDate()) {
			resetSizes(getWidth(), getHeight());
			repaintAllPanels();
			return;
		}
		for (int i = 0; i<subPanels.size(); i++){
			ClosablePanel panel = ((ClosablePanel)subPanels.elementAt(i));
			remove(panel);
			panel.dispose();
		}
		subPanels.removeAllElements();
		MesquiteProject proj = bfc.getProject();
		for (int i=0; i<proj.getNumModels(); i++){
			CharacterModel m = proj.getCharacterModel(i);
			if (!m.isBuiltIn() && m.isUserVisible()){
				CharModelPanel panel = null;
				addExtraPanel(panel = new CharModelPanel((BasicFileCoordinator)bfc, this, w,m), false);
				panel.setBounds(0, 0, 0, 0);
			}
		}
	}
	public int requestSpacer(){
		return 16;
	}

	public ClosablePanel getPrecedingPanel(ClosablePanel panel){
		return null;
	}
	public void changed(Object caller, Object obj, Notification notification){

		refresh();
	}
}
/*======================================================================== *
class GroupPanel extends ElementPanel {
	GroupLabel e;
	public GroupPanel(BasicFileCoordinator bfc, ClosablePanelContainer container, MesquiteWindow w, GroupLabel e){
		super(bfc, container, w, e);
		setShowTriangle(false);
		this.e = e;
		addCommand(true, null, "Edit Group", "Edit Group", new MesquiteCommand("showMe", e));
		addCommand(true, null, "Delete Group", "Delete Group", new MesquiteCommand("deleteMe", e));
		//	setColors(ColorDistribution.veryVeryVeryLightGray, e.getColor(), ColorDistribution.veryVeryVeryLightGray, Color.black);
	}
	public String getElementTypeName(){ 
		return "Group";
	}
	public void setColors(Color bgTopColor, Color bgBottomColor, Color barColor, Color textColor){
		setBackground(bgBottomColor);
		if (e != null)
			setBgTopColor(e.getColor());
		this.bgBottomColor = bgBottomColor;
		this.barColor = barColor;
	}
	public void setBgTopColor(Color bgTopColor){
		this.bgTopColor = bgTopColor;
		float[] hsb = new float[3];
		hsb[0]=hsb[1]=hsb[2]= 1;
		if (bgTopColor == null)
			this.textColor = Color.black;
		else {
			Color.RGBtoHSB(bgTopColor.getRed(), bgTopColor.getGreen(), bgTopColor.getBlue(), hsb);
			if (hsb[2]>0.5)
				this.textColor = Color.black;
			else
				this.textColor = Color.white;
		}
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (obj == element && element != null) {
			resetTitle();
			if (getBgTopColor() == null ||  !getBgTopColor().equals(((GroupLabel)element).getColor())){
				setBgTopColor(((GroupLabel)element).getColor());
			}
			repaint();
		}
	}

}
/*======================================================================== *
class GroupsPanel extends ElementPanel {
	MesquiteWindow w= null;
	public GroupsPanel(BasicFileCoordinator bfc, ClosablePanelContainer container,MesquiteWindow w,  ListableVector v){
		super(bfc, container, w, v);
		this.w=w;
		//	setColors(ColorDistribution.veryVeryLightGray, ColorDistribution.veryVeryVeryLightGray, Color.lightGray, Color.darkGray);
		setColors(ColorTheme.getExtInterfaceElement(), ColorTheme.getExtInterfaceElement(), ColorTheme.getExtInterfaceElement(), ColorTheme.getExtInterfaceTextMedium());
		refresh();
	}
	//PUT COMMANDS AT TOP????

	public boolean upToDate(){
		int e = 0;
		ListableVector v = (ListableVector)element;
		if (v==null) return false;
		for (int i=0; i<v.size(); i++){
			GroupLabel m = (GroupLabel)v.elementAt(i);
			if (e>= subPanels.size())
				return false;
			ElementPanel panel = ((ElementPanel)subPanels.elementAt(e));
			if (panel.element != m)
				return false;
			panel.resetTitle();
			if (panel.getBgTopColor() == null ||  !panel.getBgTopColor().equals(m.getColor())){
				panel.setBgTopColor(m.getColor());
			}
			panel.repaint();
			e++;

		}
		if (e<subPanels.size())
			return false;
		return true;
	}
	static int countt = 0;
	public void refresh(){
		if (upToDate()) {
			resetSizes(getWidth(), getHeight());
			repaintAllPanels();
			return;
		}
		for (int i = 0; i<subPanels.size(); i++){
			ClosablePanel panel = ((ClosablePanel)subPanels.elementAt(i));
			remove(panel);
			panel.dispose();
		}
		subPanels.removeAllElements();
		ListableVector v = (ListableVector)element;
		if (v!=null)
			for (int i=0; i<v.size(); i++){
				GroupLabel m = (GroupLabel)v.elementAt(i);

				ElementPanel panel = null;
				addExtraPanel(panel = new GroupPanel((BasicFileCoordinator)bfc, this, w,m), false);
				panel.setBounds(0, 0, 0, 0);
			}
	}
	public int requestSpacer(){
		return 12;
	}

	public ClosablePanel getPrecedingPanel(ClosablePanel panel){
		return null;
	}
	public void changed(Object caller, Object obj, Notification notification){

		refresh();
	}
}/*======================================================================== */

/*======================================================================== *
class CharModelPanel extends ElementPanel {

	public CharModelPanel(BasicFileCoordinator bfc, ClosablePanelContainer container, MesquiteWindow w, FileElement element){
		super(bfc, container, w,element);
		addCommand(true, null, "Edit Model", "Edit Model", new MesquiteCommand("editMe", element));
		addCommand(true, null, "Rename Model", "Rename Model", new MesquiteCommand("renameMe", element));
		addCommand(true, null, "Delete Model", "Delete Model", new MesquiteCommand("deleteMe", element));
		addCommand(true, null, "-", "-", null);
		addCommand(true, null, "Edit Comment", "Edit Comment", new MesquiteCommand("editComment", element));
		//	addCommand(true, null, "ID " + element.getID(), "ID " + element.getID(), new MesquiteCommand("id", this));

	}
	public String getElementTypeName(){ 
		if (element instanceof CharacterModel)
			return ((CharacterModel)element).getTypeName();
		return "Character Model";
	}

	public String getNotes(){
		return null; //applicability
	}
}
/*======================================================================== *
class SpecsSetVectorPanel extends ElementPanel {  
	private SpecsSetVector ssv;
	public SpecsSetVectorPanel(BasicFileCoordinator bfc, ClosablePanelContainer container, MesquiteWindow w, SpecsSetVector element, String name){
		super(bfc, container, w,name);
		ssv = (SpecsSetVector)element;
		currentHeight = MINHEIGHT + 20;
		for (int k=0; k<ssv.size(); k++){
			FileElement fe = (FileElement)ssv.elementAt(k);
			fe.addListener(this);
		}
		refresh();
	}
	public boolean upToDate(){
		int e = 0;
		if (ssv == null)
			return true;

		for (int k=0; k<ssv.size(); k++){
			if (e>= subPanels.size())
				return false;
			ElementPanel panel = ((ElementPanel)subPanels.elementAt(e));
			FileElement fe = (FileElement)ssv.elementAt(k);
			if (panel.element != fe)
				return false;

			if (!StringUtil.stringsEqual(panel.element.getName(), panel.getTitle())){
				panel.setTitle(panel.element.getName());
			}
			e++;
		}


		if (e<subPanels.size())
			return false;
		return true;
	}
	public void setColors(Color bgTopColor, Color bgBottomColor, Color barColor, Color textColor){
		super.setColors(bgTopColor, bgBottomColor, barColor, textColor);
		for (int i = 0; i<subPanels.size(); i++){
			ClosablePanel panel = ((ClosablePanel)subPanels.elementAt(i));
			panel.setColors(bgBottomColor, bgBottomColor, bgBottomColor, textColor);
		}
	}
	public void refresh(){
		if (upToDate()) {
			resetSizes(getWidth(), getHeight());
			repaintAllPanels();
			return;
		}
		for (int i = 0; i<subPanels.size(); i++){
			ClosablePanel panel = ((ClosablePanel)subPanels.elementAt(i));
			remove(panel);
			panel.dispose();
		}
		subPanels.removeAllElements();
		ElementPanel panel;
		for (int k=0; k<ssv.size(); k++){
			FileElement fe = (FileElement)ssv.elementAt(k);
			addExtraPanel(panel = new SpecsSetPanel((BasicFileCoordinator)bfc, this, w,fe), false);
			panel.setBounds(0, 0, 0, 0);
			panel.setColors(bgBottomColor, bgBottomColor, bgBottomColor, textColor);
		}


	}
	public void dispose(){
		if (ssv != null){
			for (int k=0; k<ssv.size(); k++){
				FileElement fe = (FileElement)ssv.elementAt(k);
				fe.removeListener(this);
			}

		}
		super.dispose();
	}

}
/*======================================================================== *
class SpecsSetPanel extends ElementPanel {
	SpecsSet s;
	public SpecsSetPanel(BasicFileCoordinator bfc, ClosablePanelContainer container, MesquiteWindow w,  FileElement element){
		super(bfc, container, w, element);
		s = (SpecsSet)element;
		addCommand(true, null, "Rename " + s.getTypeName(), "Rename " + s.getTypeName(), new MesquiteCommand("renameMe", element));
		addCommand(true, null, "Delete " + s.getTypeName(), "Delete " + s.getTypeName(), new MesquiteCommand("deleteMe", element));
		currentHeight = MINHEIGHT + 20;
	}
	public String getNotes(){
		if (s == null)
			return null;
		return s.getTypeName();
	}
	public String getElementTypeName(){ 
		if (element instanceof SpecsSet)
			return ((SpecsSet)element).getTypeName();
		return null;
	}

}

/*======================================================================== */
class ElementPanel extends ProjPanelPanel {
	Listable element;

	public ElementPanel(BasicFileCoordinator bfc, ClosablePanelContainer container, MesquiteWindow w, FileElement element){
		super(bfc, container, w, element.getName(), bfc);
		notes.setString(getNotes());
		this.element = element;
		if (element.getName() == null)
			setTitle(element.getClass().getName());
		setOpen(element.getResourcePanelIsOpen());
		if (element !=null)
			element.addListener(this);
		/*if (element != null){
			Vector vv = ((FileElement)element).getSpecSetsVectorVector();
			if (vv != null)
				for (int i=0; i<vv.size(); i++){
					SpecsSetVector v = (SpecsSetVector)vv.elementAt(i);
					v.addListener(this);
				}
		}*/
		refreshIcon();
		refresh();

	}
	public String getFootnoteHeading(){
		if (element instanceof FileElement){
			String an = ((FileElement)element).getAnnotation();
			if (StringUtil.blank(an))
				return null;
			String text = ((FileElement)element).getTypeName();
			if (element.getName() != null)
				text += ":  " + element.getName();
			return text;
		}
		return null;
	}
	public String getFootnote(){
		if (element instanceof FileElement){
			String an = ((FileElement)element).getAnnotation();
			return an;
		}
		return null;
	}
	public String getTitle(){
		String t = super.getTitle() + getTitleAddition();
		return t;
	}
	public String getIconFileName(){ //for small 16 pixel icon at left of main bar
		if (element != null && element instanceof FileElement){
			String s = ((FileElement)element).getIconFileName();
			return s;
		}
		return null;
	}

	public ElementPanel(FileCoordinator bfc, ClosablePanelContainer container, MesquiteWindow w, String name){
		super(bfc, container, w, name, bfc);
	}
	public void resetTitle(){
		if (element != null && element instanceof FileElement){
			String newTitle = "";
			if (element.getName() == null)
				newTitle = element.getClass().getName();
			else
				newTitle = element.getName();
			if (!StringUtil.stringsEqual(newTitle, getTitle())){
				setTitle(newTitle);
				repaint();
			}
		}
	}

	public String getTitleAddition(){
		return "";
	}
	/*.................................................................................................................*/
	public String getElementTypeName(){ 
		return null;
	}
	public boolean upToDate(){
		/*int e = 0;
		if (element != null && element instanceof FileElement){
			Vector vv = ((FileElement)element).getSpecSetsVectorVector();
			if (vv != null)
				for (int i=0; i<vv.size(); i++){
					if (e>= subPanels.size())
						return false;
					SpecsSetVector v = (SpecsSetVector)vv.elementAt(i);
					if (v.size()>0){
						ElementPanel panel = ((ElementPanel)subPanels.elementAt(e));
						if (panel.element != v)
							return false;
						e++;
					}
				}
		}
		if (e<subPanels.size())
			return false;*/
		return true;
	}
	public boolean checkSize(){
		ProjectWindow pw = (ProjectWindow)w;
		if (pw == null)
			return true;
		ProjectPanel pp = pw.projPanel;

		Component c = this;
		int x = 0;
		while (!(c instanceof ProjectPanel)){
			x += c.getX();
			c = c.getParent();
			if (c == null)
				return true;
		}
		if (pp == null)
			return true;
		int extent = x + getWidth();
		if (extent != pp.getWidth()){
			setSize(getWidth() - (extent - pp.getWidth()), getHeight());
			return false;
		}
		return true;
	}
	public void paint(Graphics g){
		if (MesquiteWindow.checkDoomed(this))
			return;
		if (!checkSize()){
			MesquiteWindow.uncheckDoomed(this);
			repaint();
			return;
		}
		super.paint(g);
		MesquiteWindow.uncheckDoomed(this);
	}
	public void refresh(){
		if (upToDate()) {
			resetSizes(getWidth(), getHeight());
			repaintAllPanels();
			return;
		}
		for (int i = 0; i<subPanels.size(); i++){
			ClosablePanel panel = ((ClosablePanel)subPanels.elementAt(i));
			remove(panel);
			panel.dispose();
		}
		subPanels.removeAllElements();
		/*if (element != null && element instanceof FileElement){
			Vector vv = ((FileElement)element).getSpecSetsVectorVector();
			if (vv != null){
				ElementPanel panel;
				for (int i=0; i<vv.size(); i++){
					SpecsSetVector v = (SpecsSetVector)vv.elementAt(i);
					if (v.size()>0){
						addExtraPanel(panel = new SpecsSetVectorPanel((BasicFileCoordinator)bfc, this,w, v, v.getTypeName()), false);
						panel.setBounds(0, 0, 0, 0);
						panel.setColors(bgBottomColor, bgBottomColor, bgBottomColor, textColor);
					}
				}
			}
		}*/
	}
	public Listable getElement(){
		return element;
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (obj == element && element != null) {
			setTitle(element.getName());
			notes.setString(getNotes());
			repaint();
		}
		else {
			refresh();
		}
	}


	protected String getMenuHeading(){
		String heading = getTitle();
		if (getElementTypeName() != null)
			heading = getElementTypeName() + ": " + heading;
		return heading;
	}

	public void dispose(){
		if (popup!=null)
			remove(popup);
		if (element != null && element instanceof FileElement){
			((FileElement)element).removeListener(this);
			((FileElement)element).setResourcePanelIsOpen(isOpen());
			/*Vector vv = ((FileElement)element).getSpecSetsVectorVector();
			if (vv != null){
				for (int i=0; i<vv.size(); i++){
					SpecsSetVector v = (SpecsSetVector)vv.elementAt(i);
					v.removeListener(this);
				}
			}*/
		}
		for (int i = 0; i<subPanels.size(); i++){
			ClosablePanel panel = ((ClosablePanel)subPanels.elementAt(i));
			panel.dispose();
		}
		super.dispose();
	}

}

