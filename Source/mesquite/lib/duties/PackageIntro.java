/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib.duties;

import java.awt.*;
import mesquite.lib.*;


/* ======================================================================== */
/**Serves to introduce a package*/

public abstract class PackageIntro extends MesquiteModule implements PackageIntroInterface  {

	public Class getDutyClass() {
		return PackageIntro.class;
	}
	public String getDutyName() {
		return "Introduces a package";
	}

	/*.................................................................................................................*/
	/** Returns whether there is a splash screen banner and possibly also URL*/
	public boolean hasSplash(){
		return false;
	}
	/*.................................................................................................................*/
	/** Returns the URL of document shown when splash screen icon touched. By default, returns path to module's manual*/
	public String getSplashURL(){
		String splashP =getPath()+"index.html";
		if (MesquiteFile.fileExists(splashP))
			return splashP;
		splashP =getPath()+"splash.html";
		if (MesquiteFile.fileExists(splashP))
			return splashP;
		String s = getManualPath();
		if (StringUtil.notEmpty(s))
			return s;
		s = getPackageURL();
		if (StringUtil.notEmpty(s))
			return s;
		return s;
	}
	public boolean getSearchableAsModule(){
		return false;
	}
	/*.................................................................................................................*/
	/** Returns whether hideable*/
	public boolean getHideable(){
		return true;
	}
	/*.................................................................................................................*/
	/** Returns whether package is built-in (comes with default install of Mesquite)*/
	public boolean isBuiltInPackage(){
		return false;
	}
	/*.................................................................................................................*/
	/** Returns citation for a package of modules*/
	public String getPackageCitation(){
		return null;
	}
	/*.................................................................................................................*/
	/** Returns version for a package of modules*/
	public String getPackageVersion(){
		return null;
	}
	/*.................................................................................................................*/
	/** Returns version for a package of modules as an integer*/
	public int getPackageVersionInt(){
		return 0;
	}
	/*.................................................................................................................*/
	/** Returns build number for a package of modules as an integer*/
	public int getPackageBuildNumber(){
		return 0;
	}
	/*.................................................................................................................*/
	/** Returns authors for a package of modules*/
	public String getPackageAuthors(){
		return null;
	}
	public String getPackageDateReleased(){
		return null;
	}
	/*.................................................................................................................*/
	/** Returns the URL for the web page about the package*/
	public String getPackageURL(){
		return null;  
	}
	/*.................................................................................................................*/
	/** Returns the integer version of Mesquite for which this package was first released*/
	public int getVersionOfFirstRelease(){
		return 0;  
	}

	public boolean isSubstantive(){
		return false;  
	}

}


