package mesquite.thinkTrees.aThinkTreesIntro;

import mesquite.lib.duties.PackageIntro;

public class aThinkTreesIntro extends PackageIntro {

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
  	 }
  	 public Class getDutyClass(){
  	 	return aThinkTreesIntro.class;
  	 }
 	/*.................................................................................................................*/
	 public String getExplanation() {
	return "ThinkTrees is a package of Mesquite modules providing tools and exercises to encourage \"tree thinking\".";
	 }
   
	/*.................................................................................................................*/
    	 public String getName() {
		return "ThinkTrees Package";
   	 }
	/*.................................................................................................................*/
	/** Returns the name of the package of modules (e.g., "Basic Mesquite Package", "Rhetenor")*/
 	public String getPackageName(){
 		return "ThinkTrees Package";
 	}
	/*.................................................................................................................*
 	public String getPackageCitation(){
 		return "Maddison, D.R., & W.P. Maddison.  2014.  ThinkTrees.  A package of Mesquite modules for teaching phylogenetics. " + getPackageVersion() + ".";
 	}
	/*.................................................................................................................*/
	/** Returns whether there is a splash banner*/
	public boolean hasSplash(){
 		return false; 
	}
	/*.................................................................................................................*/
	/** Returns version for a package of modules*/
	public String getPackageVersion(){
		return "0.3";
	}
	/*.................................................................................................................*/
	/** Returns version for a package of modules as an integer*/
	public int getPackageVersionInt(){
		return 300;
	}
	/*.................................................................................................................*/
	/** Returns build number for a package of modules as an integer*/
	public int getPackageBuildNumber(){
		return 10;
	}
/* release history:
 
 	0.3, build 10, 18 August 2014
	
	 */


	public String getPackageDateReleased(){
		return "18 August 2014";
	}

}
