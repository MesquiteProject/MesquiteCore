// Identifier.java
//
// (c) 1999-2000 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.misc;

import java.io.*;
import pal.util.Comparable;

/**
 * An identifier for some sampled data. This will most often be 
 * for example, the accession number of a DNA sequence, or the
 * taxonomic name that the sequence represents, et cetera.
 *
 * @version $Id: Identifier.java,v 1.3 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Alexei Drummond
 */


public class Identifier implements Serializable, 
				   pal.util.Comparable, Nameable { 
	
    private String name = null;

    public static Identifier ANONYMOUS = new Identifier("");

    public Identifier() {}
    
    public Identifier(String name) {
	setName(name);
    }
    
    public String toString() {
	return getName();
    }
    
    // implements Comparable interface

    public int compareTo(Object c) {
	
	return getName().compareTo(((Identifier)c).getName());
    }

    public boolean equals(Object c) {
	
	if (c instanceof Identifier) {
	    return getName().equals(((Identifier)c).getName());
	} else return false;
    }
     
    // implements Nameable interface

    public String getName() {
	return name;
    }
    
    public void setName(String s) {
	name = s;
    }
    
}


