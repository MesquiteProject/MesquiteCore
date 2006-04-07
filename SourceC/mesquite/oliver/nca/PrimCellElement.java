package mesquite.oliver.nca;

import mesquite.lib.*;

/** An element of a matrix to implement Prim's (1957) method of constructing a minimum spanning network*/
public class PrimCellElement {
	/**The taxon currently referenced by the cell*/
	private Taxon refTaxon;
	private int distance; 
	
	public Taxon getTaxon(){
		return refTaxon;
	}
	public void setTaxon(Taxon newRefTaxon){
		this.refTaxon = newRefTaxon;
	}
	public int getDistance(){
		return distance;
	}
	public void setDistance(int newDistance){
		this.distance = newDistance;
	}
}
