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
import java.io.*;
import java.util.Iterator;


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
	
	
	/*.................................................................................................................*
	/** Returns the number of downloads of the latest release*
	public int getNumReleaseDownloads(){
		if (StringUtil.notEmpty(getGitHubReleaseURL())) {
			try {
				GitHub github = GitHub.connect();
				if (github!=null) {
					GHRepository repo = github.getRepository(getGitHubRepository());
					if (repo!=null) {
						PagedIterable pagedIterable = repo.listReleases();
						java.util.List releases = (java.util.List) pagedIterable.asList();
						for (Iterator iter = releases.iterator(); iter.hasNext();) {
							GHRelease nextRelease = (GHRelease) iter.next();
						}

					}
				}
				 
			}
			catch (IOException ioe) {
			}
		}
		return 0;
	}
	
	/*.................................................................................................................*/
	
	public final static int NOREPOSITORY = 0;
	public final static int GITHUBREPOSITORY = 1;
	
	/*.................................................................................................................*/
	/** Returns the kind of repository*/
	public int getRepositoryKind(){
		return NOREPOSITORY;
	}
	/** Returns the URL for the release package*/
	public String getReleaseURL(){
		return "";
	}
	/*.................................................................................................................*/
	/** Returns the tag for the release tag*/
	public String getReleaseTag(){
		return "";
	}
	/*.................................................................................................................*/
	/** Returns the repository path on the repository server*/
	public String getRepositoryPath(){
		return "";
	}
	/*.................................................................................................................*/
	/** Returns the repository URL*/
	public String getRepositoryFullURL(){
		return "";
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
	/** Returns the  integer version of the MesquiteCore version  that this package requires to function*/
	public int getMinimumMesquiteVersionRequiredInt(){
		return 100;  //set to correct number just before release
	}
	/*.................................................................................................................*/
	/** Returns the String version of the MesquiteCore version number that this package requires to function*/
	public String getMinimumMesquiteVersionRequired(){
		return "1.00";  //set to correct number just before release
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


