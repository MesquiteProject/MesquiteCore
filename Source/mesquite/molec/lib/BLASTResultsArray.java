package mesquite.molec.lib;

public class BLASTResultsArray {
	public BLASTResults[] blastResultsArray;
	int numResults=1;
	
	public BLASTResultsArray (int numResults) {
		this.numResults = numResults;
		blastResultsArray= new BLASTResults[numResults];
	}

	public BLASTResults getResults(int i) {
		if (i>=0 && i<blastResultsArray.length)
			return blastResultsArray[i];
		return null;
	}
	public void setResults(int i, BLASTResults blastResults) {
		if (i>=0 && i<blastResultsArray.length)
			 blastResultsArray[i]=blastResults;
	}

}
