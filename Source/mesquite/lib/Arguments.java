/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib;

import java.awt.*;
import java.util.*;

/* еееееееееееееееееееееееееее commands еееееееееееееееееееееееееееееее */
/* includes commands,  buttons, miniscrolls */

public class Arguments {
	private Vector parameters;
	private Vector values;
	MesquiteInteger pos = new MesquiteInteger();
	
	//currently handles only simple arguments of form "token1 token 2 token3 = value3 token 4 = value4"
	public Arguments(Parser parser, boolean hyphenIsNegative) { 
		parameters = new Vector();
		values = new Vector();
		String token = parser.getNextToken();
		
		while (!StringUtil.blank(token) && !";".equalsIgnoreCase(token)){
			if (hyphenIsNegative && "-".equalsIgnoreCase(token)) {
				parameters.addElement(handleNegativeNumber(parser));
				token = parser.getNextToken();
				values.addElement("");
			}
			else {
				parameters.addElement(token);
				token = parser.getNextToken();
				if (StringUtil.blank(token) || ";".equalsIgnoreCase(token))
					values.addElement("");
				else if ("=".equalsIgnoreCase(token)) {
					token = parser.getNextToken();
					if (hyphenIsNegative && "-".equalsIgnoreCase(token))
						values.addElement(handleNegativeNumber(parser));
					else
						values.addElement(convertNull(token));
					token = parser.getNextToken();
				}
				else
					values.addElement("");
			}
				
		}
	}
	public String toString() {
		String s = "";
		for (int i=0; i<parameters.size(); i++) {
			s += "parameter [" + parameters.elementAt(i) + "]";
			if (i<values.size() && !StringUtil.blank((String)values.elementAt(i)))
				s += "  values [" + values.elementAt(i) + "]\n";
			else
				s += "\n";
		}
		return s;
	}
	private String handleNegativeNumber(Parser parser){
		pos.setValue(parser.getPosition());
		String s = parser.getString();
		double i  = MesquiteDouble.fromString(s, pos);
		parser.setPosition(pos.getValue());
		return MesquiteDouble.toString(-i);
	}
	private String convertNull(String n){
		if (n == null)
			return "";
		else
			return n;
	}
	
	public int getNumArguments(){
		if (parameters == null )
			return 0;
		return parameters.size();
	}
	public boolean parameterExists(String arg){
		if (parameters == null || arg == null)
			return false;
		for (int i=0; i<parameters.size(); i++) {
			if (arg.equalsIgnoreCase((String)parameters.elementAt(i)))
				return true;
		}
		return false;
	}
	public String getParameterValue(String arg){
		if (parameters == null || arg == null)
			return null;
		for (int i=0; i<parameters.size(); i++) {
			if (arg.equalsIgnoreCase((String)parameters.elementAt(i))) {
				if (values == null || i>= values.size())
					return null;
				return (String)values.elementAt(i);
			}
		}
		return null;
	}
	public int getParameterValueAsInt(String arg){
		if (parameters == null || arg == null)
			return MesquiteInteger.unassigned;
		for (int i=0; i<parameters.size(); i++) {
			if (arg.equalsIgnoreCase((String)parameters.elementAt(i))) {
				if (values == null || i>= values.size())
					return MesquiteInteger.unassigned;
				String s = (String)values.elementAt(i);
				return MesquiteInteger.fromString(s);
			}
		}
		return MesquiteInteger.unassigned;
	}
	public String getParameter(int i){
		if (parameters == null || i>= parameters.size())
			return null;
		return (String)parameters.elementAt(i);
	}
	public String getValue(int i){
		if (values == null || i>= values.size())
			return null;
		return (String)values.elementAt(i);
	}
}

