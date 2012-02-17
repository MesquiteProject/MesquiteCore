package mesquite.molec.lib;

public class BLASTResults {
	protected double eValue = -1.0;
	protected double bitScore = 0.0;
	protected String taxonomy = "";
	protected String definition = "";
	protected String accession = "";
	public double geteValue() {
		return eValue;
	}
	public void seteValue(double eValue) {
		this.eValue = eValue;
	}
	public double getBitScore() {
		return bitScore;
	}
	public void setBitScore(double bitScore) {
		this.bitScore = bitScore;
	}
	public String getTaxonomy() {
		return taxonomy;
	}
	public void setTaxonomy(String taxonomy) {
		this.taxonomy = taxonomy;
	}
	public String getDefinition() {
		return definition;
	}
	public void setDefinition(String definition) {
		this.definition = definition;
	}
	public String getAccession() {
		return accession;
	}
	public void setAccession(String accession) {
		this.accession = accession;
	}
	
}
