/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genomic.ASTRALFromGeneTrees;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.ImageObserver;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.distance.lib.*;
import mesquite.externalCommunication.lib.AppChooser;
import mesquite.io.lib.IOUtil;

/* ======================================================================== */
public class ASTRALLiaison {  
	boolean useBuiltInIfAvailable = false;
	String builtinVersion;
	String astralPath = ""; 
	String alternativeManualPath ="";
	MesquiteModule ownerModule;
	boolean usingASTRAL_III = false;
	
	public ASTRALLiaison(MesquiteModule ownerModule) {
		this.ownerModule = ownerModule;
	}
	
	public boolean usingASTRAL_III() {
		return usingASTRAL_III;
	}
	public String getASTRALPath() {
		return astralPath;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		return temp;
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("alternativeManualPath".equalsIgnoreCase(tag)) 
			alternativeManualPath = content;
		else if ("useBuiltInIfAvailable".equalsIgnoreCase(tag)) 
			useBuiltInIfAvailable = MesquiteBoolean.fromTrueFalseString(content);
		else if ("usingAstral_III".equalsIgnoreCase(tag)) 
			usingASTRAL_III = MesquiteBoolean.fromTrueFalseString(content);
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		if (!StringUtil.blank(alternativeManualPath))
			StringUtil.appendXMLTag(buffer, 2, "alternativeManualPath", alternativeManualPath);  
		StringUtil.appendXMLTag(buffer, 2, "useBuiltInIfAvailable", useBuiltInIfAvailable);  
		StringUtil.appendXMLTag(buffer, 2, "usingASTRAL_III", usingASTRAL_III);  
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public void queryLocalOptions () {
		if (queryOptions())
			ownerModule.storePreferences();
	}
	/*.................................................................................................................*/
	SingleLineTextField programPathField =  null;
	SingleLineTextField manualOptionsField =  null;
	Checkbox ua3 = null;
	public boolean queryOptions() {
		if (!ownerModule.okToInteractWithUser(ownerModule.CAN_PROCEED_ANYWAY, "Querying Options")) 
			return true;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(ownerModule.containerOfModule(),  "Options for ASTRAL-IV",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()


		AppChooser appChooser = new AppChooser("ASTRAL", "ASTRAL", useBuiltInIfAvailable, alternativeManualPath);
		appChooser.addToDialog(dialog);
		dialog.addHorizontalLine(1);
		ua3 = dialog.addCheckBox("Using ASTRAL-III", usingASTRAL_III);

		dialog.addLargeOrSmallTextLabel("This is a very early draft of ASTRAL communication. Eventually, there were will options for running ASTRAL. "
				+" Also, there will be a choice to either use existing gene trees, or to infer them now, before sending them to ASTRAL. "
				+"For now, it will use a block of gene trees already in the file.");


		/*programPathField = dialog.addTextField("Path to trimAl:", trimAlPath, 40);
		Button programBrowseButton = dialog.addAListenedButton("Browse...",null, this);
		programBrowseButton.setActionCommand("programBrowse");
		 */
		dialog.addBlankLine();

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			astralPath = appChooser.getPathToUse();
			alternativeManualPath = appChooser.getManualPath(); //for preference writing
			useBuiltInIfAvailable = appChooser.useBuiltInExecutable(); //for preference writing
			builtinVersion = appChooser.getVersion(); //for informing user; only if built-in
			usingASTRAL_III = ua3.getState();
			ownerModule.storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
   /** TreeBlockFillers should override this if they want special commands to be sent to a tree window if a tree window is created after they are used. */
 	 public String getExtraTreeWindowCommands (boolean finalTree, long treeBlockID){
  		String extras = "";
  		extras += "getOwnerModule; tell it; ";
  		extras += "getEmployee #mesquite.ornamental.DrawTreeAssocDoubles.DrawTreeAssocDoubles; tell It; setOn on; toggleShow q1; toggleShow q2; toggleShow q3; endTell; ";
  		extras += "getTreeDrawCoordinator #mesquite.trees.BasicTreeDrawCoordinator.BasicTreeDrawCoordinator; tell It;";
  		extras +="getEmployee #mesquite.trees.BasicDrawTaxonNames.BasicDrawTaxonNames; tell It; toggleNodeLabels off; endTell;";
  		extras += "setTreeDrawer  #mesquite.trees.SquareLineTree.SquareLineTree; tell It; setEdgeWidth 6; endTell;";
 		extras += "endTell;";
  		extras += "endTell;";
 		return extras;
	 }
	
	/*.................................................................................................................*/
	NameReference widthNameReference = NameReference.getNameReference("width");  //instead, possibly use drawWMult
	NameReference 	colorNameRef = NameReference.getNameReference("color");
	NameReference 	palenessRef = NameReference.getNameReference("drawPale");
	NameReference 	q1Ref = NameReference.getNameReference("q1");
	NameReference 	q2Ref = NameReference.getNameReference("q2");
	NameReference 	q3Ref = NameReference.getNameReference("q3");
	NameReference 	colorRGBNameRef = NameReference.getNameReference("colorRGB");
	Parser parser = new Parser();
	MesquiteInteger pos = new MesquiteInteger(0);

	double getParameter(String s, String tag) {
		parser.setAllowComments(false);
		parser.setString(s);
		String token = parser.getFirstToken(); // [
		token = parser.getNextToken();  // param name
		while (!StringUtil.blank(token)) {
			if (tag.equalsIgnoreCase(token)) {
				parser.getNextToken(); // =
				return MesquiteDouble.fromString(parser); // value of parameter
			}
			parser.getNextToken(); // =
			parser.getNextToken(); // value
			parser.getNextToken(); // ,
			token = parser.getNextToken(); // ,
		}
		return MesquiteDouble.unassigned;
	}
	public   void extractInfoAtNode(MesquiteTree tree, int node) {
		if (tree.nodeIsInternal(node)) {
			String label = tree.getNodeLabel(node);
			double q1= getParameter(label, "q1");
			double q2= getParameter(label, "q2");
			double q3= getParameter(label, "q3");
			if (MesquiteDouble.isCombinable(q1)){

				double R = 1.5; //imbalance factor of q2, q3 to deserve colour
				//NOTE: Details here are temporary! Set green/blue/black, but then e.g. brDrWdMult for branch draw with multiplier, and drawPale to brighten the base colour
				double secondary = q2;
				if (q3>q2)
					secondary = q3;
				long colorNum =0;
				String colorRGB =null;
				if (q2>q3*R && q2>=0.10) {
					colorNum = 11;  //green 11 5 red
					colorRGB = "0 200 50";  //0,200,50
				}
				else	if (q3 > q2*R && q3 >=0.10) {
					colorNum = 14; // blue
					colorRGB = "0 0 255";
				}
				double palenessMultiplier = (q1-secondary)+0.1;
				if (palenessMultiplier>1.0)
					palenessMultiplier=1.0;
				tree.setAssociatedDouble(palenessRef, node, palenessMultiplier, true);
				tree.setAssociatedDouble(q1Ref, node, q1, true);
				tree.setAssociatedDouble(q2Ref, node, q2, true);
				tree.setAssociatedDouble(q3Ref, node, q3, true);
				tree.setAssociatedLong(colorNameRef, node, colorNum, true);
				tree.setAssociatedObject(colorRGBNameRef, node, colorRGB, true);
				tree.setNodeLabel("[q1=" + MesquiteDouble.toStringDigitsSpecified(q1, 3) + "; q2=" + MesquiteDouble.toStringDigitsSpecified(q2, 3) + "; q3=" + MesquiteDouble.toStringDigitsSpecified(q3, 3) + "]", node);
			}
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			extractInfoAtNode(tree, d);
	}

	//NOTE: Here, attach info via other tags, e.g. brDrWdMult for branch draw with multiplier, and brDrPaleness
	MesquiteTree extractASTRALInfo(MesquiteTree tree) {
/* below is not really needed, but establishes sequence of storage for ? cursor */
		if (tree.getWhichAssociatedDouble(q1Ref)==null)
			tree.makeAssociatedDoubles(q1Ref.getValue());
		if (tree.getWhichAssociatedDouble(q2Ref)==null)
			tree.makeAssociatedDoubles(q2Ref.getValue());
		if (tree.getWhichAssociatedDouble(q3Ref)==null)
			tree.makeAssociatedDoubles(q3Ref.getValue());
		if (tree.getWhichAssociatedLong(colorNameRef)==null)
			tree.makeAssociatedLongs(colorNameRef.getValue());
		if (tree.getWhichAssociatedDouble(palenessRef)==null)
			tree.makeAssociatedDoubles(palenessRef.getValue());

		extractInfoAtNode(tree, tree.getRoot());
		return tree;
	}
	
}


