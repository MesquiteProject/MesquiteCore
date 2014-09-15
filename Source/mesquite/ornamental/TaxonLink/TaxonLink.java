/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.ornamental.TaxonLink;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class TaxonLink extends TreeDisplayAssistantI {
	public Vector extras;
	public String getFunctionIconPath(){
		return getPath() + "link.gif";
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		extras = new Vector();
		return true;
	} 
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		TaxonLinkToolExtra newPj = new TaxonLinkToolExtra(this, treeDisplay);
		extras.addElement(newPj);
		return newPj;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Taxon Link";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Supplies a tool for tree windows that jumps to hypertext links for taxa." ;
	}
	public boolean isSubstantive(){
		return false;
	}   	 
}

/* ======================================================================== */
class TaxonLinkToolExtra extends TreeDisplayExtra implements Commandable  {
	TreeTool taxonLinkTool;
	TaxonLink taxonLinkModule;
	MesquiteLabel message;
	MesquiteCommand taxonCommand, cladeCommand;
	public TaxonLinkToolExtra (TaxonLink ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		taxonLinkModule = ownerModule;
		taxonLinkTool = new TreeTool(this, "taxonLink", ownerModule.getPath(), "link.gif", 4,0,"Go to link", "This tool follows a hypertext link to another file or to a web page.  Hold down Control when using to set the link to a local file; hold down shift to enter a URL.  If filename ends in .htm or .html or contains http:// or https://, it is opened in a web browser; otherwise it is opened in Mesquite."); //; hold down shift to enter a URL
		taxonCommand = MesquiteModule.makeCommand("goToLinkedTaxon",  this);
		cladeCommand = MesquiteModule.makeCommand("goToLinkedClade",  this);
		taxonLinkTool.setTouchedTaxonCommand(taxonCommand);
		taxonLinkTool.setTouchedCommand(cladeCommand);
		if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
			((MesquiteWindow)ownerModule.containerOfModule()).addTool(taxonLinkTool);
		}
		message = new MesquiteLabel(null, 0);
		message.setSize(4,4);
		message.setColor(Color.yellow);
		message.setVisible(false);
		treeDisplay.addPanelPlease(message);
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
	}

	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
	}
	NameReference taxonlinkRef = NameReference.getNameReference("hyperlink");
	/** Returns true if this extra wants the taxon to have its name underlined */
	public boolean getTaxonUnderlined(Taxon taxon){
		Taxa taxa = taxon.getTaxa();
		int m = taxa.whichTaxonNumber(taxon);
		return (taxa.getAssociatedObject(taxonlinkRef, m) !=null);
		//return (taxon.getLink()!=null);
	}
	/** Returns the color the extra wants the taxon name colored.*/
	public Color getTaxonColor(Taxon taxon){
		if (getTaxonUnderlined(taxon))
			return Color.blue;
		else
			return null;
	}
	/** Returns true if this extra wants the clade to have its label underlined */
	public boolean getCladeLabelUnderlined(String label, int N){
		Clade c = treeDisplay.getTree().getTaxa().getClades().findClade(label);
		return (c!=null && c.getLink()!=null);
	}
	/** Returns the color the extra wants the clade label colored.*/
	public Color getCladeLabelColor(String label, int N){
		Clade c = treeDisplay.getTree().getTaxa().getClades().findClade(label);
		if (c!=null && c.getLink()!=null)
			return Color.blue;
		else
			return null;
	}

	private void showLink(String link){
		MesquiteWindow f = ownerModule.containerOfModule();
		if (f !=null && f instanceof MesquiteWindow){
			MesquiteWindow w = (MesquiteWindow)f;
			if (link == null)
				w.setAnnotation(null, null);
			else
				w.setAnnotation("Link to: " + link, "");
		}
	}

	NameReference linkNameRef = NameReference.getNameReference("hyperlink");
	boolean shown = false;
	/**to inform TreeDisplayExtra that cursor has just entered name of terminal taxon M*/
	public void cursorEnterTaxon(Tree tree, int M, Graphics g){
		if (taxonLinkTool.getInUse()){
			String link = (String)tree.getTaxa().getAssociatedObject(linkNameRef, M);
			if (link!=null) {

				showLink(link);
				shown = true;

				int tM = tree.nodeOfTaxonNumber(M);
				//message.setLocation(treeDisplay.getTreeDrawing().x[tM], treeDisplay.getTreeDrawing().y[tM]);
				message.setLocation(0,0);
				message.setText("Link to: " + link);
				message.setVisible(true);
				message.setCommand(taxonCommand);
				message.setArguments(Integer.toString(M));
				/**/
			}
		}
	}
	/**to inform TreeDisplayExtra that cursor has just exited name of terminal taxon M*/
	public void cursorExitTaxon(Tree tree, int M, Graphics g){
		if (shown) {
			showLink(null);
			message.setVisible(false);
		}
		shown = false;
	}
	/**to inform TreeDisplayExtra that cursor has just entered branch N*/
	public void cursorEnterBranch(Tree tree, int N, Graphics g){
		if (taxonLinkTool.getInUse()){
			String label = tree.getNodeLabel(N);
			if (!StringUtil.blank(label)) {
				Clade c = tree.getTaxa().getClades().findClade(label);
				if (c!=null) {
					String link = c.getLink();
					if (link!=null) {
						showLink(link);
						shown = true;

						//message.setLocation(treeDisplay.getTreeDrawing().x[N], treeDisplay.getTreeDrawing().y[N]);
						message.setLocation(0,0);
						message.setText("Link to: " + link);
						message.setVisible(true);
						message.setCommand(cladeCommand);
						message.setArguments(Integer.toString(N));
						/**/
					}
				}
			}
		}
	}
	/**to inform TreeDisplayExtra that cursor has just exited branch N*/
	public void cursorExitBranch(Tree tree, int N, Graphics g){
		if (shown) {
			showLink(null);
			message.setVisible(false);
			//message.setSize(4,4);
		}
		shown = false;
	}

	public String chooseLink(int from){
		if (from == 1){
			MesquiteString dir = new MesquiteString();
			MesquiteString f = new MesquiteString();
			String path = MesquiteFile.openFileDialog("Choose file to which to link", dir, f); // (if ends in .nex, link will take user to that file in Mesquite; if ends in .htm or .html, will take user to web browser)
			String d = dir.getValue();
			if (!StringUtil.blank(d) && !StringUtil.blank(f.getValue())) {
				if (!d.endsWith("/")) 
					d += "/";
				boolean success;
				if (ownerModule.getProject().getHomeDirectoryName().equalsIgnoreCase(d)){
					return f.getValue();
				}
				else {
					return path;
				}
			}
		}
		else if (from == 2) {
			return MesquiteString.queryString(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "URL to which to link", "URL to which to link:", "");
		}
		return null;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 

		if (checker.compare(this.getClass(), "Goes to the file or web page linked for the taxon", "[taxon number][modifiers]", commandName, "goToLinkedTaxon")) {
			Tree tree = treeDisplay.getTree();
			int M = MesquiteInteger.fromFirstToken(arguments, pos);
			if (M<0 || !MesquiteInteger.isCombinable(M) || M>=tree.getTaxa().getNumTaxa())
				return null;
			if (arguments.indexOf("shift")>=0) {  //url
				String link = (String)tree.getTaxa().getAssociatedObject(linkNameRef, M);
				if (link == null)
					link = "";
				String url = MesquiteString.queryShortString(ownerModule.containerOfModule(), "URL", "URL to which to link taxon", link);
				if (StringUtil.blank(url))
					return null;
				Taxa taxa = tree.getTaxa();
				if (taxa.getWhichAssociatedObject(linkNameRef)==null)
					taxa.makeAssociatedObjects("hyperlink");
				taxa.setAssociatedObject(linkNameRef, M, url);
				ownerModule.outputInvalid();
				return null;
			}
			else if (arguments.indexOf("control")>=0) {  //file
				String chosen = chooseLink(1);
				if (chosen==null)
					return null;
				Taxa taxa = tree.getTaxa();
				if (taxa.getWhichAssociatedObject(linkNameRef)==null)
					taxa.makeAssociatedObjects("hyperlink");
				taxa.setAssociatedObject(linkNameRef, M, MesquiteFile.decomposePath(ownerModule.getProject().getHomeDirectoryName(), chosen));
				ownerModule.outputInvalid();
				return null;
			}
			else {
				String link = (String)tree.getTaxa().getAssociatedObject(linkNameRef, M);
				if (link!=null) {
					if (StringUtil.startsWithIgnoreCase(link, "http:") || StringUtil.startsWithIgnoreCase(link, "https:") || link.endsWith(".html") || link.endsWith(".htm") || link.endsWith(".HTML") || link.endsWith(".HTM")) {  //assumed to be web page
						if (StringUtil.startsWithIgnoreCase(link, "http")) 
							taxonLinkModule.showWebPage(link, false);
						else
							taxonLinkModule.showWebPage(MesquiteFile.composePath(taxonLinkModule.getProject().getHomeDirectoryName(),  link), false);
					}
					else {
						ownerModule.logln("Jumping to " + link);
						String openCommand = "openFile ";
						if (StringUtil.startsWithIgnoreCase(link, "http")|| !taxonLinkModule.getProject().getHomeFile().isLocal()) {
							openCommand = "openGeneral  Open_URL ";
						}

						String commands = "newThread; getProjectID; Integer.id *It; tell Mesquite; closeProjectByID *Integer.id; " + openCommand;
						commands +=  StringUtil.tokenize(MesquiteFile.composePath(taxonLinkModule.getProject().getHomeDirectoryName(),link)) + "; endTell;";
						Puppeteer p = new Puppeteer(taxonLinkModule);
						MesquiteInteger pos = new MesquiteInteger(0);
						p.execute(taxonLinkModule.getFileCoordinator(), commands, pos, "", false);
						taxonLinkModule.fireEmployee(taxonLinkModule);
					}
				}
				else if (!MesquiteThread.isScripting()) {
					ownerModule.alert("There is no link associated with that taxon.  If you want to attach a link, hold down the control key while you touch the taxon to select a local file, shift key to enter a URL.  If filename ends in .nex, it is opened in Mesquite; otherwise it is opened in a web browser.");
				}
			}
		}
		else if (checker.compare(this.getClass(), "Goes to the file or web page linked for the clade", "[node number][modifiers]", commandName, "goToLinkedClade")) {
			Tree tree = treeDisplay.getTree();
			int edit = 0;
			int M = MesquiteInteger.fromFirstToken(arguments, pos);
			if (M<0 || !MesquiteInteger.isCombinable(M))
				return null;
			String label = tree.getNodeLabel(M);
			Clade c = tree.getTaxa().getClades().findClade(label);
			if (arguments.indexOf("shift")>=0) {  //url
				if (StringUtil.blank(label)) {
					if (!MesquiteThread.isScripting()) 
						ownerModule.alert("To establish a link on an internal branch, you must first assign a label or name to the branch (e.g., using the Name Nodes tool)");

					return null;
				}
				if (c==null)
					return null;
				String link = c.getLink();
				if (link == null)
					link = "";
				String url = MesquiteString.queryShortString(ownerModule.containerOfModule(), "URL", "URL to which to link branch", link);
				if (url==null)
					return null;
				c.setLink(url);
				ownerModule.outputInvalid();
				return null;
			}
			else if (arguments.indexOf("control")>=0) {  //file
				if (StringUtil.blank(label)) {
					if (!MesquiteThread.isScripting()) 
						ownerModule.alert("To establish a link on an internal branch, you must first assign a label or name to the branch (e.g., using the Name Nodes tool)");
					return null;
				}
				if (c==null)
					return null;
				String chosen = chooseLink(1);
				if (chosen==null)
					return null;
				c.setLink(chosen);
				ownerModule.outputInvalid();
				return null;
			}
			else {
				if (c==null)
					return null;
				String link = c.getLink();
				if (link!=null) {
					if (!link.endsWith(".nex")) {  //assumed to be web page
						if (StringUtil.startsWithIgnoreCase(link, "http")) 
							taxonLinkModule.showWebPage(link, false);
						else
							taxonLinkModule.showWebPage(MesquiteFile.composePath(taxonLinkModule.getProject().getHomeDirectoryName(),  link), false);
					}
					else {
						String openCommand = "openFile ";
						if (StringUtil.startsWithIgnoreCase(link, "http")|| !taxonLinkModule.getProject().getHomeFile().isLocal()) {
							openCommand = "openURL ";
						}

						String commands = "newThread; getProjectID; Integer.id *It; tell Mesquite; closeProjectByID *Integer.id; " + openCommand;

						commands +=  StringUtil.tokenize(MesquiteFile.composePath(taxonLinkModule.getProject().getHomeDirectoryName(),link)) + "; endTell;";
						Puppeteer p = new Puppeteer(taxonLinkModule);
						MesquiteInteger pos = new MesquiteInteger(0);
						p.execute(taxonLinkModule.getFileCoordinator(), commands, pos, "", false);
						taxonLinkModule.fireEmployee(taxonLinkModule);
					}
				}
				else if (!MesquiteThread.isScripting()) {
					ownerModule.alert("There is no link associated with that branch.  To establish a link on an internal branch, hold down the control key while you touch the branch to select a local file, shift key to enter a URL.  If filename ends in .nex, it is opened in Mesquite; otherwise it is opened in a web browser.");
				}
			}
		}
		return null;
	}
	public void turnOff() {
		if (treeDisplay != null && message !=null)
			treeDisplay.removePanelPlease(message);
		taxonLinkModule.extras.removeElement(this);
		super.turnOff();
	}
}



