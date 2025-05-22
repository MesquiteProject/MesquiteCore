package mesquite.lib.taxa;


public interface TaxonFilterer {
	
	public boolean filterTaxon(mesquite.lib.characters.CharacterData data, int it);

}
