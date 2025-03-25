/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib.tree;

import java.awt.Font;

import mesquite.lib.Associable;
import mesquite.lib.Debugg;
import mesquite.lib.Listable;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteLong;
import mesquite.lib.NameReference;
import mesquite.lib.Nameable;
import mesquite.lib.Notification;
import mesquite.lib.Parser;
import mesquite.lib.StringUtil;

/* ======================================================================== */
public class DisplayableTreeProperty extends TreeProperty  {
	public boolean showing = false;
	public boolean showName = true;
	public boolean centered = false;
	public boolean whiteEdges = false;
	public boolean showOnTerminals = true;
	public boolean vertical = false;
	public boolean showIfUnassigned = true;
	public boolean percentage = false;
	public double thresholdValueToShow = MesquiteDouble.unassigned;
	public int digits = 4;
	public int yOffset = 0;  //NOTE: this is in RIGHT orientation. It gets translated as the orientation shifts
	public int xOffset = 0;//NOTE: this is in RIGHT orientation. It gets translated as the orientation shifts
	public int fontSize = 12;
	public int color = 0; //standard Mesquite colors in ColorDistribution
	
	//this records the display preferences of tree properties
	public static ListableVector treePropertyDisplayPreferences = new ListableVector();

	//==== The storage points for tree properties are: ====
	// TreeProperty.treePropertiesSettingsVector: static, records settings in Mesquite_Folder/settings/trees/BranchPropertiesInit regarding branch properties (e.g. default kinds, betweenness)
	// DisplayableTreeProperty.treePropertyDisplayPreferences: static, records the display preferences of tree properties
	// MesquiteProject.knownTreeProperties: instance, the properties known by the project. For interface; not saved to file.
	// The module BranchPropertiesInit is the primary manager
	
	public boolean inCurrentTree = false;  //depends on current tree; used by NodePropertyDisplayControl and Node Properties List system

	
	public DisplayableTreeProperty(String name,int kind){
		super(name, kind);
		DisplayableTreeProperty prefRecord = (DisplayableTreeProperty)findInList(treePropertyDisplayPreferences, nRef, kind);
		if (prefRecord != null)
			cloneFrom(prefRecord);
		TreeProperty tp = (TreeProperty)findInList(treePropertiesSettingsVector, nRef, kind);
		if (tp != null)
			belongsToBranch = tp.belongsToBranch;
		else
			belongsToBranch = preferredBetweenness(name);
	}
	
	
	
	/*-------------------------------------*/
	public void cloneFrom(DisplayableTreeProperty other){
		showing = other.showing;
		showName = other.showName;
		centered = other.centered;
		whiteEdges = other.whiteEdges;
		showOnTerminals = other.showOnTerminals;
		vertical = other.vertical;
		showIfUnassigned = other.showIfUnassigned;
		percentage = other.percentage;
		thresholdValueToShow = other.thresholdValueToShow;
		digits = other.digits;
		yOffset = other.yOffset;
		xOffset = other.xOffset;
		fontSize = other.fontSize;
		color = other.color;
	}
	
	/*-------------------------------------*/
	public void setBooleans(Parser parser){//parser already with string and set to right position
		// sequence: showName, centered, whiteEdges, showOnTerminals, showIfUnassigned, percentage, vertical, showing
		//use "x" to ignore
		String b;
		if (StringUtil.notEmpty(b = parser.getNextToken()) && !"x".equals(b))
			showName = MesquiteBoolean.fromTrueFalseString(b);
		if (StringUtil.notEmpty(b = parser.getNextToken()) && !"x".equals(b))
			centered = MesquiteBoolean.fromTrueFalseString(b);
		if (StringUtil.notEmpty(b = parser.getNextToken()) && !"x".equals(b))
			whiteEdges = MesquiteBoolean.fromTrueFalseString(b);
		if (StringUtil.notEmpty(b = parser.getNextToken()) && !"x".equals(b))
			showOnTerminals = MesquiteBoolean.fromTrueFalseString(b);
		if (StringUtil.notEmpty(b = parser.getNextToken()) && !"x".equals(b))
			showIfUnassigned = MesquiteBoolean.fromTrueFalseString(b);
		if (StringUtil.notEmpty(b = parser.getNextToken()) && !"x".equals(b))
			percentage = MesquiteBoolean.fromTrueFalseString(b);
		if (StringUtil.notEmpty(b = parser.getNextToken()) && !"x".equals(b))
			vertical = MesquiteBoolean.fromTrueFalseString(b);
		if (StringUtil.notEmpty(b = parser.getNextToken()) && !"x".equals(b))
			showing = MesquiteBoolean.fromTrueFalseString(b);
	}
	/*-------------------------------------*/
	public String getBooleansString(){ 
		return " " + showName + " " + centered + " " + whiteEdges + " " + showOnTerminals
				+ " " + showIfUnassigned + " " + percentage + " " + vertical + " " + showing;
	}
	/*-------------------------------------*/
	public void setNumbers(Parser parser){//parser already with string and set to right position
		// sequence: fontSize, xOffset, yOffset, digits, color, thresholdValueToShow
		String b;
		int num;
		if (MesquiteInteger.isCombinable(num = MesquiteInteger.fromString(parser)))
			fontSize = num;
		if (MesquiteInteger.isCombinable(num = MesquiteInteger.fromString(parser)))
			xOffset = num;
		if (MesquiteInteger.isCombinable(num = MesquiteInteger.fromString(parser)))
			yOffset = num;
		if (yOffset == 9)
			Debugg.printStackTrace();
		if (MesquiteInteger.isCombinable(num = MesquiteInteger.fromString(parser)))
			digits = num;
		if (MesquiteInteger.isCombinable(num = MesquiteInteger.fromString(parser)))
			color = num;
		double dd = MesquiteDouble.fromString(parser);
		if (MesquiteDouble.isCombinable(dd) || MesquiteDouble.isUnassigned(dd))
			thresholdValueToShow = dd;
	}
		
	/*-------------------------------------*/
	public String getNumbersString(){
		return " " + MesquiteInteger.toString(fontSize) + " " + MesquiteInteger.toString(xOffset) 
				+ " " + MesquiteInteger.toString(yOffset) + " " + MesquiteInteger.toString(digits)
				+ " " + MesquiteInteger.toString(color)
				+ " " + MesquiteDouble.toString(thresholdValueToShow); 
	}
	
	/*-------------------------------------*/
	public static void mergeIntoPreferences(ListableVector propertyList){
		for (int i = 0; i< propertyList.size(); i++){
			DisplayableTreeProperty property = (DisplayableTreeProperty)propertyList.elementAt(i);
			DisplayableTreeProperty prefRecord = (DisplayableTreeProperty)findInList(treePropertyDisplayPreferences, property.getNameReference(), property.kind);
			if (prefRecord == null) {
				prefRecord = new DisplayableTreeProperty(property.getName(), property.kind);
				treePropertyDisplayPreferences.addElement(prefRecord, false);
			}
			prefRecord.cloneFrom(property);
		}
		treePropertyDisplayPreferences.notifyListeners(DisplayableTreeProperty.class, new Notification(MesquiteListener.PARTS_ADDED));
	}
	
	/*-------------------------------------*/
	Font baseFont = null;
	Font font = null;
	int lastFontSizeUsed = 0;
	public Font getFont(Font baseFont){
		if (this.baseFont != baseFont || font == null || lastFontSizeUsed != fontSize){
			this.baseFont = baseFont;
			lastFontSizeUsed = fontSize;
			font = new Font(baseFont.getName(),baseFont.getStyle(), fontSize);
		}
		return font;
	}
	/*-------------------------------------*/
	public String getStringAtNode(Tree tree, int node){
		return getStringAtNode(tree, node, false, false);
	}
	/*-------------------------------------*/
	public String getStringAtNode(Tree tree, int node, boolean showNamesRegardless, boolean showIfUnassignedRegardless){
		return getStringAtNode(tree, node, showNamesRegardless, false, showIfUnassignedRegardless);
	}
	/*-------------------------------------*/
	public String getStringAtNode(Tree tree, int node, boolean showNamesRegardless, boolean hideNamesRegardless, boolean showIfUnassignedRegardless){
		String nodeString = "";
		if (!tree.nodeIsInternal(node) &&  !showOnTerminals)
			return nodeString;
		if (!hideNamesRegardless && (showName || showNamesRegardless))
			nodeString += getName() + ": ";
		if (kind == Associable.BUILTIN){
			if (MesquiteTree.nodeLabelNameRef.equals(getNameReference())){
				String d= tree.getNodeLabel(node);
					if (d == null){
						if ((showIfUnassigned || showIfUnassignedRegardless) && showName)
							nodeString += "[none]";
						else
							nodeString = "";
					}
					else
						nodeString += d;
			}
			else if (MesquiteTree.branchLengthNameRef.equals(getNameReference())){
				double d = tree.getBranchLength(node);
				if (MesquiteDouble.isCombinable(d) || (showIfUnassigned || showIfUnassignedRegardless))
					nodeString += MesquiteDouble.toStringDigitsSpecified(tree.getBranchLength(node), digits);
				else
					nodeString = "";
			}
		}
		else if (kind == Associable.BITS){
			nodeString += MesquiteBoolean.toTrueFalseString(tree.getAssociatedBit(getNameReference(), node));
		}
		else if (kind == Associable.LONGS){
			long d = tree.getAssociatedLong(getNameReference(), node);
			if (MesquiteLong.isCombinable(d) || (showIfUnassigned || showIfUnassignedRegardless))
				nodeString += MesquiteLong.toString(d);
			else
				nodeString = "";
		}
		else if (kind == Associable.DOUBLES){
			double d = tree.getAssociatedDouble(getNameReference(), node);
			if (MesquiteDouble.isCombinable(d)){
				if (percentage)
					d = d*100;
				nodeString += MesquiteDouble.toStringDigitsSpecified(d, digits);
			}
			else if ((showIfUnassigned || showIfUnassignedRegardless)){
				nodeString += MesquiteDouble.toStringDigitsSpecified(d, digits);
			}
			else
				nodeString = "";
		}
		else if (kind == Associable.STRINGS){
			String d = tree.getAssociatedString(getNameReference(), node);
			if (d == null){
				if ((showIfUnassigned || showIfUnassignedRegardless))
					nodeString += "–";
				else
					nodeString = "";
			}
			else
				nodeString += d;
		}
		else if (kind == Associable.OBJECTS){
			Object d = tree.getAssociatedObject(getNameReference(), node);
			if (d == null){
				if ((showIfUnassigned || showIfUnassignedRegardless))
					nodeString += "–";
				else
					nodeString = "";
			}
			else 
				nodeString += d;
		}
		return nodeString;
	}
}

