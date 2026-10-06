package mesquite.lib.duties;

import mesquite.lib.MesquiteModule;
import mesquite.lib.StringUtil;


/* ======================================================================== */
/**Superclass of file interpreting modules (e.g., NEXUS file reader/writer).  Different subclasses are expected
to read different data file formats.  Example module: "Interpret NEXUS files" (class InterpretNexus).  Example of use:
see BasicFileCoordinator.*/

/** a subclass for determining whether two strings match*/
public abstract class StringMatcher extends MesquiteModule  {
	public static StringMatcher simplificationMatcher;
	static {simplificationMatcher = new SimplificationStringMatcher();}
	/** returns true if the options are already specified.*/
	public  boolean optionsSpecified(){
		return false;
	}
	/** returns true if the options are set and accepted.*/
	public  boolean queryOptions(){
		return true;
	}
	/** returns whether two strings are considered equal.*/
	public abstract boolean stringsMatch(String s1, String s2);

	public boolean useDefaultMatching() {
		return false;
	}


}

class SimplificationStringMatcher extends StringMatcher {
	public boolean stringsMatch(String s1, String s2){
		if (s1 == null && s2 == null)
			return true;
		if (s1 == null || s2 == null)
			return false;
		if (s1.equals(s2))
			return true;
		if (s1.equals(StringUtil.simplifyIfNeededForOutput(s2, true, false)))
			return true;
		if (s1.equals(StringUtil.simplifyIfNeededForOutput(s2, true, true)))
			return true;
		if (s2.equals(StringUtil.simplifyIfNeededForOutput(s1, true, false)))
			return true;
		if (s2.equals(StringUtil.simplifyIfNeededForOutput(s1, true, true)))
			return true;
		if (s1.equals(StringUtil.simplifyCutBlanksUnderscores(s2)))
			return true;
		if (s2.equals(StringUtil.simplifyCutBlanksUnderscores(s1)))
			return true;

		return false;
	}

	//not being used as module; Kludge to use in Taxa.whichTaxonNumber
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return false;
	}

	public Class getDutyClass() {
		return null;
	}

	public String getName() {
		return null;
	}
}

