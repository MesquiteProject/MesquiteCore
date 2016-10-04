/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.MirrorTreeWindowMaker;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class MirrorTreeWindowMaker extends TreeWindowAssistantN implements TreeDisplayActive {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(DrawTreeCoordinator.class, getName() + "  needs a module to coordinate tree drawing.",
		"This is arranged automatically");
		EmployeeNeed e2 = registerEmployeeNeed(TreeDisplayAssistantA.class, getName() + "  uses modules to add analyses and graphics to a tree graphic.",
		"Supplementary analyses and graphics are available in the Left Side and Right Side submenus.");
		
	}
	/*.................................................................................................................*/
	public DrawTreeCoordinator treeDrawCoordTask;
	MirrorTreeWindow mirrorTreeWindow;
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		treeDrawCoordTask= (DrawTreeCoordinator)hireEmployee(DrawTreeCoordinator.class, null);
		if (treeDrawCoordTask == null)
			return sorry(getName() + " couldn't start because no tree draw coordinating module obtained.");
 		makeMenu("Mirror");
 		return true;
  	 }
  	 
  	 public void employeeQuit(MesquiteModule m){
  	 	if (m == treeDrawCoordTask)
  	 		iQuit();
  	 }
	/*.................................................................................................................*/
 	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (source instanceof DrawTreeCoordinator){ //ignores since this should have directly called to update tree display
			return;
		}
		if (mirrorTreeWindow != null)
			mirrorTreeWindow.refresh();
		super.employeeParametersChanged(employee, source, notification);
	}
	/*.................................................................................................................*/
	 public boolean isPrerelease(){
	 	return false;
	 }
	 
	/*.................................................................................................................*/
	/** Query module as to whether conditions are such that it will have to quit soon -- e.g. if its taxa block has been doomed.  The tree window, data window, 
	etc. override this to return true if their object is doomed. This is useful in case MesquiteListener disposing method is not called for an employer before one of its
	employees discovers that it needs to quit.  If the employer is going to quit anyway,there is no use to use auto rehire for the quit employee.*/
	public boolean quittingConditions(){
		return (mirrorTreeWindow.taxa.isDoomed());
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		if (mirrorTreeWindow==null){
	 		mirrorTreeWindow= new MirrorTreeWindow( this, treeDrawCoordTask, tree.getTaxa());
	 		setModuleWindow(mirrorTreeWindow);
	 		if (!MesquiteThread.isScripting())
	 			mirrorTreeWindow.setVisible(true);
	 		resetContainingMenuBar();
	 		employer.resetContainingMenuBar();
			resetAllWindowsMenus();
	 		mirrorTreeWindow.sizeDisplays();
		}
		mirrorTreeWindow.setTree(tree);
	}
	/*.................................................................................................................*/
 	public void windowGoAway(MesquiteWindow whichWindow) {
		if (whichWindow == null)
			return;
		whichWindow.hide();
		whichWindow.dispose();
		iQuit();
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
  	 	if (mirrorTreeWindow ==null)
  	 		return null;
  	 	Snapshot fromWindow = mirrorTreeWindow.getSnapshot(file);
  	 	if (fromWindow == null || fromWindow.getNumLines() ==0)
  	 		return null;
   	 	Snapshot sn = new Snapshot();
		sn.addLine("getWindow");
		sn.addLine("tell It");
		sn.incorporate(fromWindow, true);
		sn.addLine("endTell");
		sn.addLine("getTreeDrawCoordinator", treeDrawCoordTask);
    	 	sn.addLine("showWindow");
		
  	 	return sn;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Returns the tree draw coordinating module", null, commandName, "getTreeDrawCoordinator")) {
    	 		return treeDrawCoordTask;
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		}		   
	/*.................................................................................................................*/
   	 public boolean mouseDownInTreeDisplay(int modifiers, int x, int y, TreeDisplay treeDisplay, Graphics g) {
   		if (!treeDisplay.getTree().isLocked())
   			return mirrorTreeWindow.ScanTouch(treeDisplay, g, x, y, modifiers);
   		 return false;
   	 }

	/*.................................................................................................................*/
   	 public boolean mouseUpInTreeDisplay(int modifiers, int x, int y, TreeDisplay treeDisplay, Graphics g) {
   		 return true;
   	 }
   	 
	/*.................................................................................................................*/
   	 public boolean mouseMoveInTreeDisplay(int modifiers, int x, int y, TreeDisplay treeDisplay, Graphics g) {
   		if (!treeDisplay.getTree().isLocked())
   			mirrorTreeWindow.ScanFlash(treeDisplay, g, x, y, modifiers);
   		 return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean mouseDragInTreeDisplay(int modifiers, int x, int y, TreeDisplay treeDisplay, Graphics g) {
   		 return true;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Mirror Tree Window";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Displays a single tree (the same as in a tree window) twice, in mirror image." ;
   	 }
   	 
   	 
}
	
/* ======================================================================== */
class MirrorTreeWindow extends MesquiteWindow implements Commandable  {
	public TreeDisplay[] treeDisplays;
	public DrawTreeCoordinator treeDrawCoordTask;
	int totalWidth;
	int totalHeight;
	int firstTree=0;
	public MessagePanel messagePanel;
	Taxa taxa;
	MirrorExtra extra;
	public int highlightedBranch=0;
	String defaultExplanation;
	
	public MirrorTreeWindow ( MirrorTreeWindowMaker ownerModule, DrawTreeCoordinator treeDrawCoordTask, Taxa taxa){
		super(ownerModule, true); //infobar
  		this.treeDrawCoordTask = treeDrawCoordTask;
      		setWindowSize(500,400);
		setShowExplanation(true);
		defaultExplanation = "This window shows the same tree as seen in ";
		MesquiteWindow eW = ownerModule.getEmployer().getModuleWindow();
		if (eW !=null)
			defaultExplanation += eW.getTitle();
		else
			defaultExplanation += "a Tree Window";
		setExplanation(defaultExplanation);
		this.ownerModule = ownerModule;
		//setLayout( null );
		setBackground(Color.white);
		messagePanel=new MessagePanel(getColorScheme());
		addToWindow(messagePanel);
		messagePanel.setVisible(true);
		this.taxa = taxa; //taxa = ownerModule.getProject().getTaxa(0); //reset in setTree if not correct taxa block
		ownerModule.addSubmenu(null, "Left Side", MesquiteModule.makeCommand("newAssistantLeft",  this), TreeDisplayAssistantA.class);
		ownerModule.addSubmenu(null, "Right Side", MesquiteModule.makeCommand("newAssistantRight",  this), TreeDisplayAssistantA.class);
		treeDisplays =treeDrawCoordTask.createTreeDisplays(2, taxa, this);
		addToWindow(treeDisplays[0]);
		treeDisplays[0].setOrientation(TreeDisplay.RIGHT);
		treeDisplays[0].setAllowReorientation(false);
		treeDisplays[0].setTipsMargin(40);
		treeDisplays[0].setTaxonNameBuffer(8); ///8
		treeDisplays[0].setTaxonNameDistance(14); ///absent
		treeDisplays[1].setOrientation(TreeDisplay.LEFT);
		treeDisplays[1].setAllowReorientation(false);
		treeDisplays[1].suppressNames = true;
		treeDisplays[1].setTipsMargin(18); //14
		treeDisplays[1].setTaxonNameBuffer(20); //absent
		treeDisplays[1].setTaxonNameDistance(14); ///absent
		extra = new MirrorExtra(ownerModule, treeDisplays[0], this);
		treeDisplays[0].addExtra(extra);
		treeDisplays[0].centerNames = true;
		//treeDisplays[1].setBackground(Color.cyan);
		//treeDisplays[0].setBackground(Color.yellow);
		addToWindow(treeDisplays[1]);
		sizeDisplays();
		addAssistantsDI(ownerModule);
		resetTitle();
	}
	protected void addAssistantsDI(MesquiteModule ownerModule){
		ownerModule.hireAllEmployees(TreeDisplayAssistantDI.class);
		Enumeration e = ownerModule.getEmployeeVector().elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			if (obj instanceof TreeDisplayAssistantDI) {
				TreeDisplayAssistant tda = (TreeDisplayAssistant)obj;
				treeDrawCoordTask.addAssistantTask(tda);
				TreeDisplayExtra tce = tda.createTreeDisplayExtra(treeDisplays[0]);
				if (tce!=null) 
					treeDisplays[0].addExtra(tce);
				TreeDisplayExtra tce2 = tda.createTreeDisplayExtra(treeDisplays[1]);
				if (tce2!=null) 
					treeDisplays[1].addExtra(tce2);
	 		}
		}
	}
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree blocks, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		setTitle("Mirror Tree"); 
	}
	/*.................................................................................................................*/
  	 public int getNumSnapshotLines(MesquiteFile file) {
  	 	int tot = 0;
		for (int i = 0; i<ownerModule.getNumberOfEmployees(); i++) {
			Object e=ownerModule.getEmployeeVector().elementAt(i);
			if (e instanceof TreeDisplayAssistantA) {
				tot++;
			}
		}
  	 	return tot;
  	 }
  	 public void setWindowSize(int w, int h){
  		 super.setWindowSize(w, h);
 		//sizeDisplays();
  	 }
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
  	 	temp.incorporate(super.getSnapshot(file), false);
		ListableVector extrasLeft = treeDisplays[0].getExtras();
		if (extrasLeft!=null) {
			Enumeration enumeration=extrasLeft.elements();
			while (enumeration.hasMoreElements()){
				TreeDisplayExtra tde = (TreeDisplayExtra)enumeration.nextElement();
				MesquiteModule mb = tde.getOwnerModule();
				if (mb instanceof TreeDisplayAssistantA) {
					temp.addLine("newAssistantLeft", mb);
				}
			}
		}
		
		ListableVector extrasRight = treeDisplays[1].getExtras();
		if (extrasRight!=null) {
			Enumeration enumeration=extrasRight.elements();
			while (enumeration.hasMoreElements()){
				TreeDisplayExtra tde = (TreeDisplayExtra)enumeration.nextElement();
				MesquiteModule mb = tde.getOwnerModule();
				if (mb instanceof TreeDisplayAssistantA) {
					temp.addLine("newAssistantRight", mb);
				}
			}
		}
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Hires a new tree display assistant (A) for the left-hand tree", "[name of module]", commandName, "newAssistantLeft")) {
    	 		TreeDisplayAssistantA tda= (TreeDisplayAssistantA)ownerModule.hireNamedEmployee(TreeDisplayAssistantA.class, arguments);
			if (tda!=null){
				treeDrawCoordTask.addAssistantTask(tda);
				
				TreeDisplayExtra tce = tda.createTreeDisplayExtra(treeDisplays[0]);
				tce.setTree(treeDisplays[0].getTree());
				treeDisplays[0].addExtra(tce);
				treeDisplays[0].repaint();
				return tda;
			}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Hires a new tree display assistant (A) for the right-hand tree", "[name of module]", commandName, "newAssistantRight")) {
    	 		TreeDisplayAssistantA tda= (TreeDisplayAssistantA)ownerModule.hireNamedEmployee(TreeDisplayAssistantA.class, arguments);
			if (tda!=null){
				treeDrawCoordTask.addAssistantTask(tda);
				
				TreeDisplayExtra tce = tda.createTreeDisplayExtra(treeDisplays[1]);
				tce.setTree(treeDisplays[1].getTree());
				treeDisplays[1].addExtra(tce);
				treeDisplays[1].repaint();
				return tda;
			}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	return null;
    	 }
    	public void refresh(){
		treeDisplays[0].pleaseUpdate(true);
		treeDisplays[1].pleaseUpdate(true);
    	}
	/*.................................................................................................................*/
	public void sizeDisplays(){
		totalWidth = getWidth();
		totalHeight = getHeight() - 16;
		if (treeDisplays == null || treeDisplays.length ==0)
			return;
		int leftTreeEdge = totalWidth/2 + treeDisplays[0].getTipsMargin()/2 -  treeDisplays[1].getTipsMargin()/2;
		treeDisplays[0].setLocation(0,0);
		treeDisplays[0].setSize(leftTreeEdge,totalHeight);
		treeDisplays[0].setFieldSize(leftTreeEdge,totalHeight);
		treeDisplays[1].setLocation(leftTreeEdge, 0);
		treeDisplays[1].setSize( totalWidth  - leftTreeEdge,totalHeight);
		treeDisplays[1].setFieldSize(totalWidth  - leftTreeEdge,totalHeight);
		resetDisplay(treeDisplays[0]);
		resetDisplay(treeDisplays[1]);
		messagePanel.setSize(totalWidth, 16);
		messagePanel.setLocation(0, totalHeight);
	}
	void resetDisplay(TreeDisplay treeDisplay){
		treeDisplay.setVisRect(new Rectangle(0, 0, treeDisplay.getWidth(), treeDisplay.getHeight()));
		Component[] cc = treeDisplay.getComponents();
		if (cc!=null && cc.length>0)
			for (int i=0; i<cc.length; i++) {
				if (cc[i] instanceof Legend){ //make sure legends are in bounds
					//adjustLocation
					Legend legend = (Legend)cc[i];
				
						legend.setConstrainingRectangle(treeDisplay.getBounds()); //treeDisplay.getBounds()
				
					legend.adjustLocation();
				}
			}
	}
	/*.................................................................................................................*/
	public void setTree(Tree newTree){
		if (treeDisplays[0].getTree()!=null)
			treeDisplays[0].getTree().dispose();
		if (treeDisplays[1].getTree()!=null)
			treeDisplays[1].getTree().dispose();
		Tree tree;
		if (newTree!=null) {

			/*if (newTree.getTaxa() != taxa) {
				taxa = newTree.getTaxa();
				treeDisplays[0].setTaxa(taxa);
				treeDisplays[1].setTaxa(taxa);
			}*/
			tree = newTree.cloneTree();
			treeDisplays[0].setTree(tree);
			treeDisplays[1].setTree(tree);
			treeDisplays[0].suppressDrawing(false);
			treeDisplays[0].setVisible(true);
			treeDisplays[0].repaint();
			treeDisplays[0].setTreeAllExtras(tree);
			treeDisplays[1].suppressDrawing(false);
			treeDisplays[1].setVisible(true);
			treeDisplays[1].repaint();
			treeDisplays[1].setTreeAllExtras(tree);
			MesquiteModule employer = ownerModule.getEmployer();
			if (employer instanceof TreeWindowMaker && employer.getModuleWindow()!=null)
				messagePanel.setMessage(tree.getName() + " in " + employer.getModuleWindow().getName());
			else
				messagePanel.setMessage(tree.getName());  
		}
		else messagePanel.setMessage("Error: source tree is null");  
		messagePanel.repaint();
	}
	/*_________________________________________________*/
	
	public   void InvertBranchOld(TreeDisplay treeDisplay, Graphics g, int N) {
		highlightedBranch=N;
		treeDisplay.getTreeDrawing().fillBranchInverted(treeDisplay.getTree(), N, g);
	   }
	   
	/*_________________________________________________*/
	public   void RevertBranchOld(TreeDisplay treeDisplay, Graphics g, int N) {
		highlightedBranch=0;
		treeDisplay.getTreeDrawing().fillBranchInverted(treeDisplay.getTree(), N, g);
	   }
	/*_________________________________________________*/
	
	public   void HighlightBranch(TreeDisplay treeDisplay, Graphics g, int N) {
		highlightedBranch=N;
		treeDisplay.getTreeDrawing().highlightBranch(treeDisplay.getTree(), N, g);
	   }
	   
	/*_________________________________________________*/
	public   void UnhighlightBranch(TreeDisplay treeDisplay, Graphics g, int N) {
		highlightedBranch=0;
		treeDisplay.getTreeDrawing().unhighlightBranch(treeDisplay.getTree(), N, g);
	   }
	/*_________________________________________________*/
	public   void ScanFlash(TreeDisplay treeDisplay, Graphics g, int x, int y, int modifiers) {
		setExplanation(defaultExplanation);
		if (treeDisplay == null || treeDrawCoordTask == null || treeDrawCoordTask.getNamesTask() == null || treeDisplay.getTreeDrawing()==null)
			return;
		Tree tree = treeDisplay.getTree();
		int drawnRoot = treeDisplay.getTreeDrawing().getDrawnRoot(); //TODO: remember drawnRoot!!!
		if (!tree.nodeExists(drawnRoot))
			drawnRoot = tree.getRoot();
		MesquiteDouble fraction = new MesquiteDouble();
		int branchFound =treeDisplay.getTreeDrawing().findBranch(tree,  drawnRoot, x, y, fraction);
		if (highlightedBranch != 0) {
			if (branchFound==0) {
				notifyExtrasOfBranchExit(treeDisplay, g, highlightedBranch);
				UnhighlightBranch(treeDisplay, g, highlightedBranch);
			}
			else if (branchFound!=highlightedBranch)  {
				notifyExtrasOfBranchExit(treeDisplay, g, highlightedBranch);
				UnhighlightBranch(treeDisplay, g, highlightedBranch); 
				notifyExtrasOfBranchEnter(treeDisplay, g, branchFound);
				HighlightBranch(treeDisplay, g, branchFound);
			}
		}
		else if (branchFound!=0) {
			notifyExtrasOfBranchEnter(treeDisplay, g, branchFound);
			HighlightBranch(treeDisplay, g, branchFound); 
		}

	}
	/*_________________________________________________*/
	public   boolean ScanTouch(TreeDisplay treeDisplay, Graphics g, int x, int y, int modifiers) {
		setExplanation(defaultExplanation);
		if (treeDisplay == null || treeDrawCoordTask == null || treeDrawCoordTask.getNamesTask() == null || treeDisplay.getTreeDrawing()==null)
			return false;
		Tree tree = treeDisplay.getTree();
		int drawnRoot = treeDisplay.getTreeDrawing().getDrawnRoot();
		if (!tree.nodeExists(drawnRoot))
			drawnRoot = tree.getRoot();
		MesquiteDouble fraction= new MesquiteDouble();
		int branchFound =treeDisplay.getTreeDrawing().findBranch(tree,  drawnRoot, x, y, fraction);
	        if (branchFound!=0) {
			if (highlightedBranch != 0) {
				notifyExtrasOfBranchExit(treeDisplay, g, highlightedBranch);
				UnhighlightBranch(treeDisplay, g, highlightedBranch);
			}
	   		notifyExtrasOfBranchTouch(treeDisplay, g, branchFound);
	   		return true;
       		}
 		return false;
	   }
	/*................................................................................................*/
	private void notifyExtrasOfBranchTouch(TreeDisplay treeDisplay,Graphics g, int N) {
		if (treeDisplay.getExtras()!=null) {
			Enumeration e = treeDisplay.getExtras().elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof TreeDisplayExtra) {
					TreeDisplayExtra tce = (TreeDisplayExtra)obj;
		 			tce.cursorTouchBranch(treeDisplay.getTree(), N, g);
		 		}
			}
		}
	}
	/*................................................................................................*/
	public void notifyExtrasOfBranchEnter(TreeDisplay treeDisplay, Graphics g, int N) {
		if (treeDisplay.getExtras()!=null) {
			Enumeration e = treeDisplay.getExtras().elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof TreeDisplayExtra) {
					TreeDisplayExtra tce = (TreeDisplayExtra)obj;
		 			tce.cursorEnterBranch(treeDisplay.getTree(), N, g);
		 		}
			}
		}
	}
	/*................................................................................................*/
	public void notifyExtrasOfBranchExit(TreeDisplay treeDisplay,Graphics g, int N) {
		if (treeDisplay.getExtras()!=null) {
			Enumeration e = treeDisplay.getExtras().elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				if (obj instanceof TreeDisplayExtra) {
					TreeDisplayExtra tce = (TreeDisplayExtra)obj;
		 			tce.cursorExitBranch(treeDisplay.getTree(), N, g);
		 		}
			}
		}
	}
	/*.................................................................................................................*/
	 public void paintContents(Graphics g) {
		if (treeDisplays==null){
			MesquiteMessage.warnProgrammer("Oh no, tree displays are null");
		}
      		else {
			sizeDisplays();
		}
		
	}
		/*.................................................................................................................*/
		/**
		* @author Peter Midford
		*/
		public void windowToPDF(MesquitePDFFile pdfFile, int fitToPage) {
			try{
				// These windows don't currently support text mode, so no need to check infoBar (which doesn't seem to be around.
			if (pdfFile != null) {
				Graphics g = pdfFile.getPDFGraphicsForComponent(this.getOuterContentsArea(),null);
				if (g == null || treeDisplays == null || treeDisplays[0] == null || treeDisplays[1]==null)
					return;
				sizeDisplays();
				treeDisplays[0].print(g);
				g.translate((int)treeDisplays[1].getLocation().getX(),(int)treeDisplays[1].getLocation().getY());
				treeDisplays[1].print(g);
				pdfFile.end();
			}
			}
			catch (NullPointerException e){  //seems to be an issue...
			}
		}
		/**
		* @author Peter Midford
		*/
		public String getPrintToPDFMenuItemName() {
			return "Save Mirror Tree Window as PDF...";
		}
	public void dispose(){
		for (int itree=0; itree<2; itree++) {
				if (treeDisplays[itree]!=null){
					if (treeDisplays[itree].getTree()!=null)
						treeDisplays[itree].getTree().dispose();
					treeDisplays[itree].dispose();
				}
		}
		super.dispose();
	}
}

/* ======================================================================== */
class MirrorExtra extends TreeDisplayExtra {
	MirrorTreeWindow treeWindow;
	public MirrorExtra (MesquiteModule ownerModule, TreeDisplay treeDisplay, MirrorTreeWindow treeWindow) {
		super(ownerModule, treeDisplay);
		this.treeWindow = treeWindow;
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		treeWindow.sizeDisplays();
	}
	public   void setTree(Tree tree) {
	}
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
	}
}


