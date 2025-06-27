package mesquite.lists.lib;

import java.awt.Color;
import java.awt.Graphics;

import mesquite.lib.ListableVector;
import mesquite.lib.characters.CharacterData;

public class CharMatricesListWindow extends ListableVectorWindow {
	ListableVector datas;


	public CharMatricesListWindow (ListModule ownerModule) {
		super(ownerModule);
	}

	public ListableVector getDatas() {
		return datas;
	}

	public void setDatas(ListableVector datas) {
		this.datas = datas;
	}
	public void notifyRowDeletion(Object obj){ // row deletion notification happens through remove file element of project
	}

	/*...............................................................................................................*/
	public void setRowNameColor(Graphics g, int row){
		super.setRowNameColor(g,row);
		if (datas!=null) {
			CharacterData data =(CharacterData)datas.elementAt(row);
			if (data!=null && !data.isUserVisible())
				g.setColor(Color.gray);
		}
	}

}
