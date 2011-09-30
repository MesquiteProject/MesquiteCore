/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison. 
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.tol.lib;

import java.util.Iterator;
import java.util.List;

import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteTree;
import mesquite.lib.Taxa;

import org.dom4j.*;

public class ToLUtil {

	/*--------------------------*/
	public static int countTerminals(Element element, String spacer) {
		boolean isNode = isNode(element);
		List children = element.content();
		Iterator iterator = children.iterator();
		int terms = 0;
		while (iterator.hasNext()) {
			Object o = iterator.next();
			if (o instanceof Element) {
				Element e = (Element)o;
				if (isContinuable(e))
					terms += countTerminals(e, spacer + "   ");
			}
		}
		if (isNode && terms == 0) {
			return 1;
		}
		else  {
			return terms;
		}

	}  
	
	/* Node Attributes
	 *       <NODE EXTINCT="0" ID="8882" CONFIDENCE="0" PHYLESIS="0" LEAF="0" HASPAGE="1" ITALICIZENAME="0" INCOMPLETESUBGROUPS="0" SHOWAUTHORITY="0" SHOWAUTHORITYCONTAINING="0" IS_NEW_COMBINATION="0" COMBINATION_DATE="null">

	 * 
	 */
	  /*--------------------------*/
	public static boolean isLeaf(Element element){
		if (!isNode(element))
			return false;
		Attribute leafAttribute = element.attribute("LEAF");
		try { 
			return leafAttribute.getValue().equals("1");
		}
		catch (Exception e) {
			return false;
		}
	}
	  /*--------------------------*/
	public static boolean hasChildren(Element element){
		if (!isNode(element))
			return false;
		Attribute childcountAttribute = element.attribute("CHILDCOUNT");
		try { 
			return MesquiteInteger.fromString(childcountAttribute.getValue()) > 0; 
		}
		catch (Exception e) {
			return false;
		}
	}
	/*--------------------------*/
	static boolean isNode(Element element){
		return "Node".equalsIgnoreCase(element.getName());
	}
	static boolean isAncestor(Element element){
		return "Ancestor".equalsIgnoreCase(element.getName()) || "Ancestors".equalsIgnoreCase(element.getName()) || "Ancestors_INFO".equalsIgnoreCase(element.getName());
	}
	static boolean isContinuable(Element element){
		return "TREE".equalsIgnoreCase(element.getName())|| "NAME".equalsIgnoreCase(element.getName()) || "NODES".equalsIgnoreCase(element.getName()) || "NODE".equalsIgnoreCase(element.getName());
	}
	static boolean isName(Element element) {
		//return XMLConstants.NAME.equalsIgnoreCase(element.getName());
		return "Name".equalsIgnoreCase(element.getName());
	}
	/*--------------------------*/
	public static int getTerminals(Element element, String[] names, boolean[] leaves, boolean[] hasChildren, MesquiteString termName, MesquiteInteger c) {
		boolean isNode = isNode(element);
		boolean isName = "Name".equalsIgnoreCase(element.getName());
		List children = element.content();
		Iterator iterator = children.iterator();
		int terms = 0;
		while (iterator.hasNext()) {
			Object o = iterator.next();
			if (isName){
				if (o instanceof CDATA) {
					termName.setValue(((CDATA)o).getText());
				}
			}
			else if (o instanceof Element) {
				Element e = (Element)o;
				if (isContinuable(e))
					terms += getTerminals((Element) o, names, leaves,hasChildren, termName, c);
			}
		}
		if (isNode && terms == 0) {

			names[c.getValue()] =  new String(termName.getValue()); //element.getAttributeValue("NAME");
			if (isLeaf(element))
				leaves[c.getValue()] = true;
			else
				leaves[c.getValue()] = false;
			if (hasChildren(element))
				hasChildren[c.getValue()] = true;
			else
				hasChildren[c.getValue()] = false;
			c.increment();
			return 1;
		}
		else 
			return terms;

	}  
	/*--------------------------*/
	public static int getTerminalsWithAuthors(Element element, String[] names, String[] authors, boolean[] leaves, boolean[] hasChildren, MesquiteString termName,MesquiteString authorName, MesquiteInteger c) {
		boolean isNode = isNode(element);
		boolean isName = "Name".equalsIgnoreCase(element.getName());
		boolean isAuthor = "Authority".equalsIgnoreCase(element.getName());
		List children = element.content();
		Iterator iterator = children.iterator();
		int terms = 0;
		while (iterator.hasNext()) {
			Object o = iterator.next();
			if (isName){
				if (o instanceof CDATA) {
					termName.setValue(((CDATA)o).getText());
				}
			}
			else if (isAuthor){
				if (o instanceof CDATA) {
					authorName.setValue(((CDATA)o).getText());
				}
			}
			else if (o instanceof Element) {
				Element e = (Element)o;
				if (isContinuable(e))
					terms += getTerminals((Element) o, names, leaves,hasChildren, termName, c);
			}
		}
		if (isNode && terms == 0) {

			names[c.getValue()] =  new String(termName.getValue()); //element.getAttributeValue("NAME");
			authors[c.getValue()] =  new String(authorName.getValue()); //element.getAttributeValue("NAME");
			if (isLeaf(element))
				leaves[c.getValue()] = true;
			else
				leaves[c.getValue()] = false;
			if (hasChildren(element))
				hasChildren[c.getValue()] = true;
			else
				hasChildren[c.getValue()] = false;
			c.increment();
			return 1;
		}
		else 
			return terms;

	}  

	public static void buildTree(boolean isRoot, Element element, MesquiteTree tree, int node, String[] names, MesquiteInteger c) {
		if (ToLUtil.countTerminals(element, "  ") == 1 && ToLUtil.isNode(element)) {
			tree.setTaxonNumber(node, c.getValue(), false); //StringArray.indexOf(names, element.getAttributeValue("NAME")), false);
			Taxa taxa = tree.getTaxa();
			c.increment();
		}
		else {
			List children = element.content();
			Iterator iterator = children.iterator();
			while (iterator.hasNext()) {
				Object o = iterator.next();
				if (o instanceof Element) {
					Element e = (Element)o;
					if (ToLUtil.isNode(e)){ // is a node
						if (isRoot){
							isRoot = false;
							buildTree(isRoot,(Element) o, tree, node, names, c);
						}
						else
							buildTree(isRoot,(Element) o, tree, tree.sproutDaughter(node, false), names, c);
					}
					else if (ToLUtil.isContinuable(e))
						buildTree(isRoot,(Element) o, tree, node, names, c);
				}
			}
		}

	}

}
