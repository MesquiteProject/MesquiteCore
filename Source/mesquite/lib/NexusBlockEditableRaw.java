/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib;

import java.awt.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
/**Represents a block in a NEXUS file that is editable raw (e.g., a foreign block, or a Mesquite block.
 */
public abstract class NexusBlockEditableRaw extends NexusBlock {
	EditRawNexusBlock editor;
	public NexusBlockEditableRaw(MesquiteFile f, MesquiteModule manager){
		super(f, manager);
	}
	public void dispose(){
		editor = null;
		super.dispose();
	}
	/** DOCUMENT */
	public abstract String getText();

	/** DOCUMENT */
	public abstract void setText(String n);
	
	/** DOCUMENT THIS should only be called if it is an editable-raw nexus block*/
	public void setEditor(EditRawNexusBlock editor) {
		this.editor = editor;
	}
	/** DOCUMENT THIS should only be called if it is an editable-raw nexus block*/
	public EditRawNexusBlock getEditor() {
		return editor;
	}
}


