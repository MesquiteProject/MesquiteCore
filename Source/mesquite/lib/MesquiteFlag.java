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

import java.awt.Color;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import mesquite.categ.lib.CategoricalState;
import mesquite.lib.ui.ColorDistribution;

/* ======================================================================== */
/**To hold a flag on an object, including a name, and explanation, a colour, an integer (for bit flags), and a double value (initially built for nodes and trees).
 */
public class MesquiteFlag implements Listable, Nameable {
	int bits = 0;  //bit 0 is off/on
	String name = null;
	String hexColor = null;
	Color color = null;
	String explanation = null;
	double doubleValue = MesquiteDouble.unassigned;
	boolean atNode = false; //versus at branch
	
	public MesquiteFlag(){
	}
	public MesquiteFlag(int bits, String name, String hexColor, String explanation, double doubleValue){
		this.bits = bits;
		this.name = name;
		this.hexColor = hexColor;
		this.color = ColorDistribution.colorFromHex(hexColor);
		this.explanation = explanation;
		this.doubleValue = doubleValue;
	}
	public MesquiteFlag(MesquiteFlag other){
		this.bits = other.bits;
		this.name = other.name;
		this.hexColor = other.hexColor;
		this.color = ColorDistribution.colorFromHex(hexColor);
		this.explanation = other.explanation;
		this.doubleValue = other.doubleValue;
		this.atNode = other.atNode;
	}
	public MesquiteFlag(int bits, String name, String hexColor){
		this.bits = bits;
		this.name = name;
		this.hexColor = hexColor;
		this.color = ColorDistribution.colorFromHex(hexColor);
	}
	public boolean showBitSet(){
		return CategoricalState.isElement(bits, 0) ;
	}
	public boolean getAtNode(){
		return atNode;
	}
	public void setAtNode(boolean s){
		atNode = s;
	}
	public Color getColor(){
		return color;
	}
	public void setName(String s){
		name = s;
	}
	public String getName(){
		return name;
	}
	public void setBits(int s){
		bits = s;
	}
	public int getBits(){
		return bits;
	}
	public String toString(){ //includes the name, e.g. "flag.TreeShrink.1"
		String s = "";
		s += " bits: " + bits;
		if (hexColor != null)
			s +="; color: " + hexColor;
		if (doubleValue != MesquiteDouble.unassigned)
			s += "; value: " +  MesquiteDouble.toString(doubleValue);
		if (explanation != null)
			s += "; explanation: " +  explanation;
		if (atNode)
			s +="; atNode: " + hexColor;
	s = name + " = " + ParseUtil.tokenize(s);
		return s;
	}

	public String writeDescription(){ //includes the name, e.g. "flag.TreeShrink.1"
		String s = "";
		s += " b: " + bits;
		if (hexColor != null)
			s +="/ c: " + hexColor;
		if (doubleValue != MesquiteDouble.unassigned)
			s += "/ v: " +  MesquiteDouble.toString(doubleValue);
		if (explanation != null)
			s += "/ e: " +  explanation;
		if (atNode)
			s +="/ a: " + atNode;
		s = name + " = " + ParseUtil.tokenize(s);
		return s;
	}
	public void readDescription(String desc){ //does not read the name
		deassign();
		Parser parser = new Parser(desc);

			StringTokenizer t = new StringTokenizer(desc, "/");
			String tok = null;
			int count = t.countTokens();
			try {
				while (t.hasMoreTokens()) {
					tok = t.nextToken();
					parser.setString(tok);
					String command = parser.getFirstToken();
					parser.getNextToken(); // :
					if ("b".equalsIgnoreCase(command)){ //bits (integer)
						bits = MesquiteInteger.fromString(parser);
					}
					else if ("c".equalsIgnoreCase(command)){//color (hex color string)
						hexColor = parser.getNextToken();
						color = ColorDistribution.colorFromHex(hexColor);
				}
					else if ("e".equalsIgnoreCase(command)) //explanation (string)
						explanation = parser.getNextToken();
					else if ("v".equalsIgnoreCase(command)) //double value (double)
						doubleValue = MesquiteDouble.fromString(parser);
					else if ("a".equalsIgnoreCase(command)){ //at node (boolean)
						atNode = MesquiteBoolean.fromTrueFalseString(parser.getNextToken());
				}
					
				}
			} 
			catch (NoSuchElementException e) {
			}
	}

	public void deassign(){
		bits = 0;
		name = null;
		hexColor = null;
		color = null;
		explanation = null;
		doubleValue = MesquiteDouble.unassigned;
		atNode = false;
	}
}


