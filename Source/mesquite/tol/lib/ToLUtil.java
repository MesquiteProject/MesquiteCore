package mesquite.tol.lib;

import java.util.Iterator;
import java.util.List;

import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteTree;
import mesquite.lib.Taxa;

import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Element;
import org.tolweb.treegrow.main.XMLConstants;

public class ToLUtil {

	/*--------------------------*/
	public static int countTerminals(Element element, String spacer) {
		boolean isNode = isNode(element);
		List children = element.getContent();
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
	static boolean isLeaf(Element element){
		if (!isNode(element))
			return false;
		Attribute leafAttribute = element.getAttribute("LEAF");
		try { 
			return leafAttribute.getIntValue() == 1; 
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
		return XMLConstants.NAME.equalsIgnoreCase(element.getName());
	}
	/*--------------------------*/
	public static int getTerminals(Element element, String[] names, boolean[] leaves, MesquiteString termName, MesquiteInteger c) {
		boolean isNode = isNode(element);
		boolean isName = "Name".equalsIgnoreCase(element.getName());
		List children = element.getContent();
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
					terms += getTerminals((Element) o, names, leaves,termName, c);
			}
		}
		if (isNode && terms == 0) {

			names[c.getValue()] =  new String(termName.getValue()); //element.getAttributeValue("NAME");
			if (isLeaf(element))
				leaves[c.getValue()] = true;
			else
				leaves[c.getValue()] = false;
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
			List children = element.getContent();
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
