package mesquite.basic.BasicStringMatcher;

import mesquite.lib.*;
import mesquite.lib.duties.*;

public class BasicStringMatcher extends StringMatcher {
	 

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	public boolean stringsMatch(String s1, String s2) {
		return false;
	}

	public Class getDutyClass() {
		return StringMatcher.class;
	}

	public String getName() {
		return "Default String Matcher";
	}

	public String getExplanation() {
		return "Uses the default string-matching mechanism of the module that is doing the calling.";
	}

	public boolean useDefaultMatching() {
		return true;
	}

}
