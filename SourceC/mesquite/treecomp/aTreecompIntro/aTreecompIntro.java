/* Mesquite module ~~ Copyright 1997-2000 W. & D. Maddison*/
package mesquite.treecomp.aTreecompIntro;
/*~~  */

import java.applet.*;
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
public class aTreecompIntro extends PackageIntro {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
 		return true;
  	 }
  	 public Class getDutyClass(){
  	 	return aTreecompIntro.class;
  	 }
	/*.................................................................................................................*/
    	 public String getExplanation() {
		return "Serves as an introduction to the tree comparison package for Mesquite.";
   	 }
   
	/*.................................................................................................................*/
    	 public String getName() {
		return "Tree Comparison Package Introduction";
   	 }
	/*.................................................................................................................*/
  	 public String getVersion() {
		return null;
   	 }
	/*.................................................................................................................*/
	/** Returns the name of the package of modules (e.g., "Basic Mesquite Package", "Rhetenor")*/
 	public String getPackageName(){
 		return "Tree Comparison Package";
 	}
	/*.................................................................................................................*/
	/** Returns citation for a package of modules*/
 	public String getPackageCitation(){
 		return "Maddison, W. 2001.  Tree comparison and tree shape package for Mesquite, version 0.5.";
 	}
	/*.................................................................................................................*/
	/** Returns whether there is a splash banner*/
	public boolean hasSplash(){
 		return false; 
	}
	/*.................................................................................................................*/
	/** Returns the URL of document shown when splash screen icon touched. By default, returns path to module's manual*/
	public String getSplashURL(){
		String splashP =getPath()+"splash.html";
		if (MesquiteFile.fileExists(splashP))
			return splashP;
		else
			return getManualPath();
	}
}
