/* 
 * Mesquite source code.  Copyright 1997-2009 W. Maddison and
 * D. Maddison.  
 *
 * Version 2.6, January 2009. 
 *
 * Contribution by Hilmar Lapp (hlapp at gmx.net), using code from
 * MesquiteRunner.
 *
 * Disclaimer: The Mesquite source code is lengthy and we are few.
 * There are no doubt inefficiencies and goofs in this code.  The
 * commenting leaves much to be desired. Please approach this source
 * code with the spirit of helping out.  Perhaps with your help we can
 * be more than a few, and make Mesquite better. Mesquite is
 * distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY.
 * 
 * Mesquite's web site is http://mesquiteproject.org. This source code
 * and its compiled class files are free and modifiable under the
 * terms of GNU Lesser General Public License.
 * (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.rLink.common;

import mesquite.lib.MesquiteDouble;
import mesquite.lib.Taxa;
import mesquite.lib.characters.CharacterData;
import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.DNAState;
import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.CategoricalState;
import mesquite.cont.lib.ContinuousData;
import mesquite.cont.lib.ContinuousState;

public class RCharacterData {

    private CharacterData charData;

    public RCharacterData(CharacterData data) {
        this.charData = data;
    }

    public String[] asStringMatrix() {
        if (charData instanceof ContinuousData) {
            throw new IllegalArgumentException(
                "data is continuous, use asDoubleMatrix instead");
        }
        StateAsString stateConv = null;
        if (charData instanceof DNAData) {
            stateConv = new StateAsString() {
                    public String asString(long state) {
                        return DNAState.toString(state, false);
                    }
                };
        } else {
            stateConv = new StateAsString() {
                    public String asString(long state) {
                        return CategoricalState.toString(state, false);
                    }
                };
        }
        int numChars = charData.getNumChars();
        int numTaxa = charData.getNumTaxa();
        String[] matrix = new String[numTaxa * numChars];
        CategoricalData data = (CategoricalData) charData;
        for (int it = 0; it < numTaxa; it++) {
            for (int ic = 0; ic < numChars; ic++) {
                long state = data.getState(ic, it);
                matrix[it * numChars + ic] = stateConv.asString(state);
            }
        }
        return matrix;
    }

    public double[] asDoubleMatrix() {
        ContinuousData data = (ContinuousData) charData;
        int numChars = data.getNumChars();
        int numTaxa = data.getNumTaxa();
        double[] matrix = new double[numTaxa * numChars];
        for (int it = 0; it < numTaxa; it++) {
            for (int ic = 0; ic < numChars; ic++) {
                double state = data.getState(ic, it, 0);
                if (!MesquiteDouble.isCombinable(state))
                    state = Double.NaN;
                matrix[it * numChars + ic] = state;
            }
        }
        return matrix;
    }

    public String[] getColumnNames() {
        int numChars = charData.getNumChars();
        String[] names = new String[numChars];
        for (int ic = 0; ic < numChars; ic++) {
            names[ic] = charData.getCharacterName(ic);
        }
        return names;
    }

    public String[] getRowNames() {
        Taxa taxa = charData.getTaxa();
        int numTaxa = taxa.getNumTaxa();
        String[] names = new String[numTaxa];
        for (int ic = 0; ic < numTaxa; ic++) {
            names[ic] = taxa.getTaxonName(ic);
        }
        return names;
    }

    public interface StateAsString {
        public String asString(long state);
    }
}