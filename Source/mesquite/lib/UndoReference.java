package mesquite.lib;

public class UndoReference {
	
	Undoer undoer;
	MesquiteModule responsibleModule;
	
	public UndoReference() {
	}

	public UndoReference(Undoer undoer, MesquiteModule responsibleModule) {
		this.undoer = undoer;
		this.responsibleModule = responsibleModule;
	}

	public MesquiteModule getResponsibleModule() {
		return responsibleModule;
	}

	public void setResponsibleModule(MesquiteModule responsibleModule) {
		this.responsibleModule = responsibleModule;
	}

	public Undoer getUndoer() {
		return undoer;
	}

	public void setUndoer(Undoer undoer) {
		this.undoer = undoer;
	}
	
	

}


