/*
 * EthOntos - a tool for comparative methods using ontologies
 * Copyright 2004-2005 Peter E. Midford
 * 
 * Created on Aug 8, 2006
 * Last updated on Aug 8, 2006
 * 
 */
package mesquite.diverse.lib;

public interface DESpeciationSystemCateg extends DESystem {
    
    double getSRate(int state);
    double getERate(int state);

}
