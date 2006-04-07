package mesquite.oliver.lib;

import mesquite.lib.CommandRecord;
import mesquite.lib.*;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.*;
import mesquite.assoc.lib.*;

public abstract class NumberForMatrixAndAssociation extends NumberForMatrix {
	TaxaAssociation association;
	AssociationSource associationTask;

	public String getName() {
		return "Number for Matrix and Association";
	}
	
	public abstract void initialize(MCharactersDistribution data, TaxaAssociation association, CommandRecord commandRec);

	public abstract void calculateNumber(MCharactersDistribution data, TaxaAssociation association, MesquiteNumber result, MesquiteString resultString, CommandRecord commandRec);

	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		associationTask = (AssociationSource)hireEmployee(commandRec, AssociationSource.class, "Source of taxon associations");
		if (associationTask == null)
			return sorry(commandRec, getName() + " couldn't start because no source of taxon associations obtained.");
		return true;
  	 }
	
	public TaxaAssociation getAssociation (MCharactersDistribution data, CommandRecord commandRec){
		if (data != null){
			Taxa containedTaxa = data.getTaxa();
			if (association == null && associationTask != null){
				return association = associationTask.getCurrentAssociation(containedTaxa, commandRec);
			}
			else return null;
		}else return null;	
	}
	/*===== For NumberForMatrix interface ======*/
	public void initialize(MCharactersDistribution data, CommandRecord commandRec) {
		initialize(data, association, commandRec);
	}
	public void calculateNumber(MCharactersDistribution data, MesquiteNumber result, MesquiteString resultString, CommandRecord commandRec) {
		if (result==null || data == null)
			return;
		if(association == null)
			association = getAssociation(data, commandRec);
		calculateNumber(data, association, result, resultString, commandRec);
	}
	
	/*===== For NumberForItem interface ======*/
	public void initialize(Object object1, Object object2, CommandRecord commandRec){
		if (object1 instanceof MCharactersDistribution) 
			initialize((MCharactersDistribution)object1, (TaxaAssociation)object2, commandRec);
	}
	public  void calculateNumber(Object object1, Object object2, MesquiteNumber result, MesquiteString resultString, CommandRecord commandRec){
		if (result==null)
			return;
		if (object1 instanceof MCharactersDistribution && object1 != null) {
				if(association == null)
					association = getAssociation((MCharactersDistribution)object1, commandRec);
				calculateNumber((MCharactersDistribution)object1, association, result, resultString, commandRec);
		}
	}
	
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification, CommandRecord commandRec) {
		if (employee==associationTask) {
			association = null;
			parametersChanged(notification, commandRec);
		}
	}
	

	

}
