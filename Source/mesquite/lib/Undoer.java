package mesquite.lib;

import mesquite.lib.*;

public interface Undoer  {

	public Undoer undo();
	
	public void setNewState (Object newState);

	
}
