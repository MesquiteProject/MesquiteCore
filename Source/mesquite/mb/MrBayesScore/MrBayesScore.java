package mesquite.mb.MrBayesScore;

import mesquite.lib.*;
import mesquite.lib.duties.*;

/* 
 * Walks a Mesquite tree and calculates the number of taxa in it.
 */

public class MrBayesScore extends NumberForTree {

    /* ................................................................................................................. */

    public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
        return true;
    }

 
    /* ................................................................................................................. */
      public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString, CommandRecord commandRec) {
        if (result == null || tree == null)
            return;
        result.setToUnassigned();
        if (tree instanceof Attachable){
        	Object obj = ((Attachable)tree).getAttachment("MrBayesScore");
        	if (obj == null){
        			if (resultString != null)
        				resultString.setValue("No MrBayes score is associated with this tree.  To obtain a score, use as tree source \"Trees from MrBayes\".");
        			return;
        	}
        	if (obj instanceof MesquiteDouble)
        			result.setValue(((MesquiteDouble)obj).getValue());
			else if (obj instanceof MesquiteNumber)
				result.setValue((MesquiteNumber)obj);
        }
       
        if (resultString != null) {
            resultString.setValue("MrBayes score : " + result.toString());
        }
    }

  	/*.................................................................................................................*/
   	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
   	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
   	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
      	public int getVersionOfFirstRelease(){
      		return 110;  
      	}
  /* ................................................................................................................. */
    /** Explains what the module does. */

    public String getExplanation() {
        return "Supplies posterior probability score from MrBayes";
    }

    /* ................................................................................................................. */
    /** Name of module */
    public String getName() {
        return "MrBayes Score";
    }
}
