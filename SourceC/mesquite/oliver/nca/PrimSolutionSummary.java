package mesquite.oliver.nca;

import mesquite.lib.Taxon;

/** An object to hold individual elements of the solution summary table of Prim's (1957) minimum spanning network.*/
public class PrimSolutionSummary {
	private Taxon TaxonA, TaxonB;
	private int distance; 
	
	public Taxon getTaxonA(){
		return TaxonA;
	}
	public void setTaxonA(Taxon newTaxonA){
		this.TaxonA = newTaxonA;
	}
	public Taxon getTaxonB(){
		return TaxonB;
	}
	public void setTaxonB(Taxon newTaxonB){
		this.TaxonB = newTaxonB;
	}
	public int getDistance(){
		return distance;
	}
	public void setDistance(int newDistance){
		this.distance = newDistance;
	}
}
