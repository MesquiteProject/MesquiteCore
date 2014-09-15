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

import java.awt.*;
import java.text.*;



public class FunctionExplanation implements Listable, FunctionExplainable {
	String  name;
	String explanation;
	String iconPath;
	String keywords;
	String URL =null;
	boolean URLinPackageIntroDirectory = false;
	public FunctionExplanation(String name, String explanation, String keywords, String iconPath, String URL, boolean URLinPackageIntroDirectory){
		this.name = name;
		this.explanation = explanation;
		this.iconPath = iconPath;
		this.keywords = keywords;
		this.URL = URL;
		this.URLinPackageIntroDirectory = URLinPackageIntroDirectory;
	}
	public FunctionExplanation(String name, String explanation, String keywords, String iconPath){
		this.name = name;
		this.explanation = explanation;
		this.iconPath = iconPath;
		this.keywords = keywords;
	}
	public String getName(){
		return name;
	}
	public String getURLString(){
		return URL;
	}
 	/** Returns whether or not the URL for this module is a relative reference from the PackageIntro directory */
	public boolean URLinPackageIntro(){
		return URLinPackageIntroDirectory;
	}
	public String getExplanation(){
		return explanation;
	}
	public String getHTMLExplanation(){
		return explanation;
	}
 	public String getFunctionIconPath(){  //path to icon explaining function, e.g. a tool
 		return iconPath;
 	}
 	public String getKeywords(){
 		return keywords;
 	}
}

