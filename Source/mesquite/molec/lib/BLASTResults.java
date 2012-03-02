package mesquite.molec.lib;

import java.util.Iterator;
import java.util.List;

import org.apache.hivemind.util.PropertyUtils;
import org.dom4j.*;

import mesquite.categ.lib.*;
import mesquite.lib.*;

public class BLASTResults {
	protected double[] eValue;
	protected double[] bitScore ;
	protected String[] taxonomy;
	protected String[] definition;
	protected String[] accession;
	protected String[] ID;
	protected int[] frame;
	protected String[] sequence;
	protected boolean[] reversed;
	int maxHits = 1;

	public BLASTResults (int maxHits) {
		this.maxHits = maxHits;
		initialize();
	}
	public void initialize() {
		eValue = new double[maxHits];
		bitScore = new double[maxHits];
		taxonomy = new String[maxHits];
		definition = new String[maxHits];
		accession = new String[maxHits];
		frame = new int[maxHits];
		ID = new String[maxHits];
		reversed = new boolean[maxHits];
		zeroArrays();
	}
	public void zeroArrays() {
		for (int i=0; i<maxHits; i++) {
			eValue[i]= -1.0;
			bitScore[i] = 0.0;
			taxonomy[i] = "";
			definition[i] = "";
			accession[i] = "";
			frame[i] = 0;
			reversed[i] = false;
			ID[i] = "";
		}
	}
	public double geteValue(int index) {
		return eValue[index];
	}
	public void seteValue(double eValue, int index) {
		this.eValue[index] = eValue;
	}
	public double getBitScore(int index) {
		return bitScore[index];
	}
	public void setBitScore(double bitScore, int index) {
		this.bitScore[index] = bitScore;
	}
	public boolean getReversed(int index) {
		return reversed[index];
	}
	public void setReversed(boolean reversed, int index) {
		this.reversed[index] = reversed;
	}
	public String getTaxonomy(int index) {
		return taxonomy[index];
	}
	public void setTaxonomy(String taxonomy, int index) {
		this.taxonomy[index] = taxonomy;
	}
	public int getFrame(int index) {
		return frame[index];
	}
	public void setFrame(int frame, int index) {
		this.frame[index] = frame;
	}
	public String getDefinition(int index) {
		return definition[index];
	}
	public void setDefinition(String definition, int index) {
		this.definition[index] = definition;
	}
	public String getAccession(int index) {
		return accession[index];
	}
	public void setAccession(String accession, int index) {
		this.accession[index] = accession;
	}
	public String[] getAccessions() {
		return accession;
	}
	public String getID(int index) {
		return ID[index];
	}
	public void setID(String ID, int index) {
		this.ID[index] = ID;
	}
	public String[] getIDs() {
		return ID;
	}
	public String reversedToString() {
		String s = "";
		for (int i=0; i<maxHits && i<reversed.length; i++) {
			if (reversed[i]) 
				s+= " - ";
			else 
				s+= " + ";
		}
		return s;
	}


	public String getSequence(int index) {
		return sequence[index];
	}
	public void setSequence(String sequence, int index) {
		this.sequence[index] = sequence;
	}

	public String toString(int numHits) {
		StringBuffer sb = new StringBuffer();
		sb.append("   Top hits\n\tAccession\t[eValue]\tDefinition): \n");
		for (int i=0; i<maxHits && i<numHits && i<accession.length; i++) {
			if (StringUtil.notEmpty(accession[i])){
				/*				if (reversed[i])
					sb.append("\t-");
				else 
					sb.append("\t+");
				 */				sb.append("\t"+ accession[i] + "\t[" + eValue[i]+ "]\t" + definition[i]+"\n");
			}
		}
		return sb.toString();
	}


	/*.................................................................................................................*/
	public boolean acceptableHit(int hitCount, double bitScore, double eValue) {
		return hitCount<=maxHits;
	}

	/*.................................................................................................................*/
	public  void setIDFromDefinition(String separator, int index){
		String s="";
		for (int i=0; i<maxHits && i<ID.length; i++) {
			if (StringUtil.notEmpty(definition[i])){
				s=StringUtil.getItem(definition[i],"|", index);
				if (StringUtil.notEmpty(s))
					ID[i] = s;
			}
		}

	}
	/*.................................................................................................................*/
	public boolean hitsSatisfyMatches (String[] matchInDefinitions, int minNumToMatch, int maxNumToMatch, boolean noOthers, boolean onlyMatchOnce) {  
		if (minNumToMatch==0 || matchInDefinitions==null)
			return true;
		boolean[] alreadyMatched = new boolean[matchInDefinitions.length];
		for (int j=0; j<alreadyMatched.length; j++){
			alreadyMatched[j]=false;
		}

		int count = 0;
		for (int i=0; i<maxHits && i<definition.length; i++) {  // go through the hits
			if (StringUtil.notEmpty(definition[i])) {
				boolean foundMatch = false;
				for (int j=0; j<matchInDefinitions.length && !foundMatch; j++){ 
					if (StringUtil.notEmpty(definition[i])&&StringUtil.notEmpty(matchInDefinitions[j])) {
						int index = StringUtil.indexOfIgnoreCase(definition[i], matchInDefinitions[j]);
						if (index>=0){
							if (onlyMatchOnce && alreadyMatched[j])
								return false;
							alreadyMatched[j]=true;
							count++;
							foundMatch=true;
						}
					}
				}
				if (!foundMatch) {  // here is something that is not in the match list
					return false;
				}
			}
		}
		if (count>=minNumToMatch && count <= maxNumToMatch)
			return true;
		return false;
	}

	/*.................................................................................................................*/
	public  void setIDFromDefinition(){
		String s="";
		for (int i=0; i<maxHits && i<ID.length; i++) {
			if (StringUtil.notEmpty(definition[i])){
				ID[i] = definition[i];
			}
		}

	}
	/*.................................................................................................................*/
	public  void setIDFromElement(String separator, int index){
		String s="";
		for (int i=0; i<maxHits && i<ID.length; i++) {
			if (StringUtil.notEmpty(ID[i])){
				s=StringUtil.getItem(ID[i],"|", index);
				if (StringUtil.notEmpty(s))
					ID[i] = s;
			}
		}

	}
	/*.................................................................................................................*/
	public  void setAccessionFromDefinition(String separator, int index){
		String s="";
		for (int i=0; i<maxHits && i<accession.length; i++) {
			if (StringUtil.notEmpty(definition[i])){
				s=StringUtil.getItem(definition[i],"|", index);
				if (StringUtil.notEmpty(s))
					accession[i] = s;
			}
		}

	}
	/*.................................................................................................................*/
	public  void setAccessionsFromIDs(boolean nucleotides){
		accession = NCBIUtil.getGenBankAccessionFromID(ID, nucleotides, null, false);
	}

	/*.................................................................................................................*/
	public  boolean processResultsFromBLAST(String response, boolean storeSequences, double eValueCutoff){
		if (accession==null)
			return false;
		zeroArrays();
		Element blastOutputElement = XMLUtil.getRootXMLElementFromString("BlastOutput",response);
		if (blastOutputElement==null)
			return false;
		int hitCount = 0;
		Element blastIterationsElement = blastOutputElement.element("BlastOutput_iterations");
		if (blastIterationsElement!=null) {
			Element IterationsElement = blastIterationsElement.element("Iteration");
			if (IterationsElement!=null) {
				Element IterationHitElement = IterationsElement.element("Iteration_hits");
				if (IterationHitElement!=null) {
					List hitList = IterationHitElement.elements("Hit");
					for (Iterator iter = hitList.iterator(); iter.hasNext() && hitCount<maxHits;) {   // this is going through all of the hits
						Element hitElement = (Element) iter.next();
						if (hitElement!=null) {

							String s = hitElement.elementText("Hit_def");
							setDefinition(s, hitCount);
							//	Debugg.println("Hit_def: " + s);

							s = hitElement.elementText("Hit_accession");
							setAccession(s, hitCount);

							s = hitElement.elementText("Hit_id");
							//	Debugg.println("Hit_id: " + s);
							//s=StringUtil.getItem(s,"|", 2);
							if (StringUtil.notEmpty(s))
								setID(s, hitCount);

							Element hithsps = hitElement.element("Hit_hsps");
							if (hithsps!=null) {
								Element Hsp = hithsps.element("Hsp");
								if (Hsp!=null) {
									String eValue = Hsp.elementText("Hsp_evalue");
									double eValueDouble = MesquiteDouble.fromString(eValue);
									if (eValueCutoff< 0.0 || eValueDouble<=eValueCutoff) {
										seteValue(eValueDouble, hitCount);
										setBitScore(MesquiteDouble.fromString(Hsp.elementText("Hsp_bit-score")), hitCount);
										setFrame(MesquiteInteger.fromString(Hsp.elementText("Hsp_hit-frame")), hitCount);

										int queryFrame = MesquiteInteger.fromString(Hsp.elementText("Hsp_query-frame"));
										setReversed(queryFrame<0, hitCount);

										if (storeSequences)
											setSequence(Hsp.elementText("Hsp_hseq"), hitCount);
									} else if (eValueCutoff>=0.0 && eValueDouble>eValueCutoff) {
										setDefinition("", hitCount);
										setAccession("", hitCount);
										setID("", hitCount);
										setFrame(0, hitCount);
										hitCount--;
									}
								}
							}
							hitCount++;
						}

					}
					return true;

				}
			}
		}
		return false;
	}

	//	Alternatively, You can look at the query start/end and subject start/end coordinates. If your query aligns to reverse of subject, then the subject end 
	//coordinate will be smaller than the start coordinate. For example, if SeqA aligns to reverse of SeqB, you might see position 50-100 of SeqA 
	// aligning to position 200-150 of SeqB.

}
