package mesquite.chromaseq.lib;

import mesquite.lib.*;

/* ======================================================================== */
public class PrimerList { 
	String [] primerNames;
	String token;
	String [] fragmentNames;
	boolean [] forward;
	String fragName;
	int numPrimers;
	
	public PrimerList(String primerList) {
		readTabbedPrimerFile(primerList);
	}
	
	public void readXMLPrimerFile(String primerList) {  // this does not work; just started to build this.
		String oneFragment;
		int numPrimers = 0;
		String tempList = primerList;

		Parser parser = new Parser();
		Parser subParser = new Parser();
		parser.setString(primerList);
		if (!parser.isXMLDocument(false))   // check if XML
			return;
		if (!parser.resetToMesquiteTagContents())   // check if has mesquite tag, and reset to this if so
			return;
		MesquiteString nextTag = new MesquiteString();
		String tagContent = parser.getNextXMLTaggedContent(nextTag);
		
		while (!StringUtil.blank(tagContent)) {
			if ("primers".equalsIgnoreCase(nextTag.getValue())) {  //make sure it has the right root tag
				subParser.setString(tagContent);
				String subTagContent = subParser.getNextXMLTaggedContent(nextTag);
				while (!StringUtil.blank(subTagContent)) {
					if ("version".equalsIgnoreCase(nextTag.getValue())) {
						int version = MesquiteInteger.fromString(subTagContent);
						boolean acceptableVersion = (1==version);
						if (acceptableVersion) {
							parser.setString(tagContent);
							tagContent = parser.getNextXMLTaggedContent(nextTag);
							int count = -1;
							while (!StringUtil.blank(tagContent)) {
								if ("fragment".equalsIgnoreCase(nextTag.getValue()) || "gene".equalsIgnoreCase(nextTag.getValue())) {
									count++;
									subParser.setString(tagContent);
									subTagContent = subParser.getNextXMLTaggedContent(nextTag);
									while (!StringUtil.blank(subTagContent)) {
										if ("primer".equalsIgnoreCase(nextTag.getValue())) {
											primerNames[count] = StringUtil.cleanXMLEscapeCharacters(subTagContent);
										}
										else if ("forward".equalsIgnoreCase(nextTag.getValue())) {
											primerNames[count] = StringUtil.cleanXMLEscapeCharacters(subTagContent);
										}
										else if ("reverse".equalsIgnoreCase(nextTag.getValue())) {
										}
										subTagContent = subParser.getNextXMLTaggedContent(nextTag);
									}
							}
								tagContent = subParser.getNextXMLTaggedContent(nextTag);
							}
							
						}
						else
							return;
					}
					subTagContent = subParser.getNextXMLTaggedContent(nextTag);
				}
			}
			tagContent = parser.getNextXMLTaggedContent(nextTag);
		}
			
			
		
	}

	public void readTabbedPrimerFile(String primerList) {
		String oneFragment;
		int numPrimers = 0;
		String tempList = primerList;

		while (!StringUtil.blank(tempList) && tempList.length() > 10) {
			oneFragment = tempList.substring(0,tempList.indexOf(";"));
			numPrimers += getNumPrimers(oneFragment);
			tempList = tempList.substring(tempList.indexOf(";")+1, tempList.length());
			tempList.trim();
		}
		primerNames = new String[numPrimers];
		fragmentNames = new String[numPrimers];
		forward = new boolean[numPrimers];
		
		int count = -1;
		while (!StringUtil.blank(primerList) && primerList.length() > 10 && count < numPrimers) {
			oneFragment = primerList.substring(0,primerList.indexOf(";"));
			
			Parser parser = new Parser(oneFragment);
			fragName = parser.getNextToken(); 
			if (parser.getNextToken().equalsIgnoreCase("Forward")) {
				token = parser.getNextToken();
				while (!StringUtil.blank(token) && !token.equalsIgnoreCase("Reverse")) {
					count ++;
					primerNames[count] = token.trim();
					fragmentNames[count] = fragName;
					forward[count] = true;
					token = parser.getNextToken();
				}
				if (token.equalsIgnoreCase("Reverse")) {
					token = parser.getNextToken();
					while (!StringUtil.blank(token)) {
						count ++;
						primerNames[count] = token.trim();
						fragmentNames[count] = fragName;
						forward[count] = false;
						token = parser.getNextToken();
					}
				}
			} else if (parser.getNextToken().equalsIgnoreCase("Reverse")) {
				token = parser.getNextToken();
				while (!StringUtil.blank(token) && !token.equalsIgnoreCase("Forward")) {
					count ++;
					primerNames[count] = token.trim();
					fragmentNames[count] = fragName;
					forward[count] = false;
					token = parser.getNextToken();
				}
				if (token.equalsIgnoreCase("Forward")) {
					//count --;
					token = parser.getNextToken();
					while (!StringUtil.blank(token)) {
						count ++;
						primerNames[count] = token.trim();
						fragmentNames[count] = fragName;
						forward[count] = true;
						token = parser.getNextToken();
					}
				}
			}
			primerList = primerList.substring(primerList.indexOf(";")+1, primerList.length());
			primerList.trim();
			//count ++;
		}
	}

	/*.................................................................................................................*/
	public String getFragmentName(String primerName, MesquiteString stLouisString) {
		if (!StringUtil.blank(primerName)) {
			for (int i=0; i<primerNames.length; i++) {
				if (primerName.trim().equalsIgnoreCase(primerNames[i])) {
					if (forward[i])
						stLouisString.setValue("b.");
					else
						stLouisString.setValue("g.");
					return fragmentNames[i];
				}
			}
		}
		return "";
	}
	/*.................................................................................................................*/
	int getNumPrimers(String oneFragment) {
		int count = 0;
		Parser parser = new Parser(oneFragment);
		fragName = parser.getNextToken(); 
		String token;
		if (parser.getNextToken().equalsIgnoreCase("Forward")) {
			token = parser.getNextToken();
			while (!StringUtil.blank(token) && !token.equalsIgnoreCase("Reverse")) {
				count ++;
				token = parser.getNextToken();
			}
			if (token.equalsIgnoreCase("Reverse")) {
				token = parser.getNextToken();
				while (!StringUtil.blank(token)) {
					count ++;
					token = parser.getNextToken();
				}
			}
		}
		else if (parser.getNextToken().equalsIgnoreCase("Reverse")) {
			token = parser.getNextToken();
			while (!StringUtil.blank(token) && !token.equalsIgnoreCase("Forward")){
				count ++;
				token = parser.getNextToken();
			}
			if (token.equalsIgnoreCase("Forward")) {
				token = parser.getNextToken();
				while (!StringUtil.blank(token)) {
					count ++;
					token = parser.getNextToken();
				}
			}
		}
		return count;
	}
}


