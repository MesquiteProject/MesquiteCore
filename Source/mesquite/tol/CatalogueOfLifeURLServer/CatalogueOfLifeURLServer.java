package mesquite.tol.CatalogueOfLifeURLServer;


	import mesquite.lib.*;
import mesquite.tol.lib.TaxonOnWebServer;

	public class CatalogueOfLifeURLServer extends TaxonOnWebServer {

		public String getURL(String taxonName) {
			String openName = StringUtil.encodeForURL(StringUtil.replace(taxonName,' ', '+'));
			return "http://www.catalogueoflife.org/annual-checklist/search_results.php?search_string=" + openName + "&match_whole_words=on";
		
		}

		public String getName() {
			return "Catalogue of Life";
		}

		public String getExplanation() {
			return "Provides a URL to a taxon in the Catalogue of Life";
		}

	}
