/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib;

import java.awt.Font;

import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;

/* ======================================================================== */
/*represents the bits, longs, doubles, strings and objects belonging to parts of an Associable.
 *  Not permanently in an Associable, but for temporary use, e.g. for display in tree window, hence the graphics parameters*/
public class PropertyDisplayRecord implements Listable, Nameable  {
	private String name = null;
	private NameReference nRef;
	public int kind = -1;
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
	public boolean belongsToBranch = true; //Is this settable, or only from a prefs setting?
	
	public boolean inCurrentTree = false;  //depends on current tree; used by NodeAssociatesZ and Node Associates List system

	public static ListableVector preferenceRecords = new ListableVector();
	
	public PropertyDisplayRecord(String name,int kind){
		this.name = name;
		nRef = NameReference.getNameReference(name);
		this.kind = kind;
		PropertyDisplayRecord prefRecord = findInList(preferenceRecords, nRef, kind);
		if (prefRecord != null)
			cloneFrom(prefRecord);
		//see mesquite.trees.PropertyDisplayRecordInit for defaults setting and management
	}
	public void setName(String name){
		this.name = name;
		nRef = NameReference.getNameReference(name);
	}
	public String getName(){
		return name;
	}
	public NameReference getNameReference(){
		return nRef;
	}
	
	public void cloneFrom(PropertyDisplayRecord other){
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
		belongsToBranch = other.belongsToBranch;
	}
	
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
	public String getBooleansString(){ 
		return " " + showName + " " + centered + " " + whiteEdges + " " + showOnTerminals
				+ " " + showIfUnassigned + " " + percentage + " " + vertical + " " + showing;
	}
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
		if (MesquiteInteger.isCombinable(num = MesquiteInteger.fromString(parser)))
			digits = num;
		if (MesquiteInteger.isCombinable(num = MesquiteInteger.fromString(parser)))
			color = num;
		double dd = MesquiteDouble.fromString(parser);
		if (MesquiteDouble.isCombinable(dd) || MesquiteDouble.isUnassigned(dd))
			thresholdValueToShow = dd;
	}
		
	public String getNumbersString(){
		return " " + MesquiteInteger.toString(fontSize) + " " + MesquiteInteger.toString(xOffset) 
				+ " " + MesquiteInteger.toString(yOffset) + " " + MesquiteInteger.toString(digits)
				+ " " + MesquiteInteger.toString(color)
				+ " " + MesquiteDouble.toString(thresholdValueToShow); 
	}
	
	public static void mergeIntoPreferences(ListableVector propertyList){
		for (int i = 0; i< propertyList.size(); i++){
			PropertyDisplayRecord property = (PropertyDisplayRecord)propertyList.elementAt(i);
			PropertyDisplayRecord prefRecord = findInList(preferenceRecords, property.getNameReference(), property.kind);
			if (prefRecord == null) {
				prefRecord = new PropertyDisplayRecord(property.getName(), property.kind);
				preferenceRecords.addElement(prefRecord, false);
			}
			prefRecord.cloneFrom(property);
		}
		preferenceRecords.notifyListeners(PropertyDisplayRecord.class, new Notification(MesquiteListener.PARTS_ADDED));
	}
	
	
	public boolean equals(PropertyDisplayRecord other){  //style doesn't matter; just name and kind
		if (other ==null)
			return false;
		return nRef.equals(other.nRef) && kind == other.kind;
	}
	public static PropertyDisplayRecord findInList(ListableVector pList, NameReference nr, int kind){
		for (int i=0; i<pList.size(); i++){
			PropertyDisplayRecord mi = (PropertyDisplayRecord)pList.elementAt(i);
			if (mi.getNameReference().equals(nr) && mi.kind ==kind)
				return mi;
		}
		return null;
	}
	
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
	public String getStringAtNode(Tree tree, int node){
		return getStringAtNode(tree, node, false, false);
	}
	public String getStringAtNode(Tree tree, int node, boolean showNamesRegardless, boolean showIfUnassignedRegardless){
		return getStringAtNode(tree, node, showNamesRegardless, false, showIfUnassignedRegardless);
	}
	
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

