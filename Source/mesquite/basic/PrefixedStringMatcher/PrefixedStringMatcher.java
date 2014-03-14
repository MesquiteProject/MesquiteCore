package mesquite.basic.PrefixedStringMatcher;

import mesquite.lib.*;
import mesquite.lib.duties.*;

public class PrefixedStringMatcher extends MesquiteStringMatcher {
	String prefix = "DNA";
	int charAfterPrefix = 4;

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	public boolean stringsMatch(String s1, String s2) {
		if (StringUtil.blank(prefix)) 
			return false;
		int location1 = s1.indexOf(prefix);
		int location2 = s2.indexOf(prefix);
		if (location1<0 || location2 < 0)
			return false;
		String nextChars1 = null;
		String nextChars2 = null;
		try {
			nextChars1 = s1.substring(location1+prefix.length(),location1+prefix.length()+charAfterPrefix);
			nextChars2 = s2.substring(location2+prefix.length(),location2+prefix.length()+charAfterPrefix);
		}
		catch (Exception e) {
			return false;
		}
		//Debugg.println("nextChars1: " + nextChars1 + ",   nextChars2: " + nextChars2);
		if (StringUtil.blank(nextChars1) || StringUtil.blank(nextChars2))
			return false;
		return nextChars1.equalsIgnoreCase(nextChars2);
	}


	public Class getDutyClass() {
		return MesquiteStringMatcher.class;
	}

	public String getName() {
		return "Prefixed String Matcher";
	}

}
