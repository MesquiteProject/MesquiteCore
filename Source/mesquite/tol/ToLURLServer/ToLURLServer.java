/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison.
Version 2.7, August 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.tol.ToLURLServer;

import mesquite.lib.*;
import mesquite.tol.lib.TaxonOnWebServer;

public class ToLURLServer extends TaxonOnWebServer {

	public String getURL(String taxonName) {
		String openName = StringUtil.encodeForURL(taxonName);
		return "http://tolweb.org/" + openName;
	}

	public String getName() {
		return "Tree of Life Web Project";
	}
	
	public String getExplanation() {
		return "Provides a URL to a taxon on the Tree of Life Web Project";
	}


}
