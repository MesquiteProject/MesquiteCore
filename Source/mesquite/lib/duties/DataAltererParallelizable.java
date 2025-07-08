/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */package mesquite.lib.duties;

/*It is parallelizable if it:
-- uses only saved instructions (relies entirely on preferences, or saves a snapshot)
-- does not rely on the MesquiteTable passed to it in alterData
-- is careful not to put up dialogs except after asking okToInteractWithUsers
 */
 public interface DataAltererParallelizable   {
  	 
}

 /*list of ones (as of July 2025) not yet made parallelizable
These would be difficult or inappropriate to make parallelizable:
            - [ ] AlignDNAToProtein
            - [ ] AddCharsFromSource
            - [ ] TrimByTerminalLength
            - [ ] SplitSequenceBlocksIntoSeparateTaxa
            - [ ] ImposeIndelPattern
            - [ ] Fill
            - [ ] SubtractSequence
            - [ ] ShiftOtherToMatch
            - [ ] RecodeCateg
             - [ ] ShuffleStates
  
These would not bee too be difficult to make parallelizable:
             - [ ] RandomFillWithMissing
            - [ ] RandomFillCateg
            - [ ] GapsToFromMissing
            - [ ] MoveDataToOtherEnd
            - [ ] Many continuous alterers that ask for multipliers, or noise value, etc.
            
*/