package mesquite.lists.lib;

import java.awt.Color;

import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteModule;
import mesquite.lib.StringUtil;
import mesquite.lib.taxa.TaxaGroup;
import mesquite.lib.ui.MesquiteSymbol;

public class TaxaListPartitionUtil {

	/*.................................................................................................................*/
	public  static TaxaGroup createNewTaxonGroup(MesquiteModule module, MesquiteFile file) {
		String n = "Untitled Group";
		if (file==null)
			return null;
		n = file.getFileElements().getUniqueName(n);
		GroupDialog d = new GroupDialog(module.getProject(),module.containerOfModule(), "New Taxon Group", n, Color.white, null, TaxaGroup.supportsSymbols());
		d.completeAndShowDialog();
		String name = d.getName();
		boolean ok = d.query()==0;
		Color c = d.getColor();
		MesquiteSymbol symbol = d.getSymbol();

		d.dispose();
		if (!ok)
			return null;
		//String name = MesquiteString.queryString(containerOfModule(), "New character group", "New character group label", "Untitled Group");
		if (StringUtil.blank(name))
			return null;
		TaxaGroup group = new TaxaGroup();
		group.setName(name);
		if (symbol!=null)
			group.setSymbol(symbol);
		group.addToFile(file, file.getProject(), null);
		if (c!=null) {
			group.setColor(c);
		}
		return group;
	}

}
