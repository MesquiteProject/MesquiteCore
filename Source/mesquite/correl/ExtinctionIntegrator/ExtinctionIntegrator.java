package mesquite.correl.ExtinctionIntegrator;


import mesquite.correl.IntegLikeCateg.IntegLikeCateg;
import mesquite.correl.lib.DEQNumSolver;
import mesquite.correl.lib.DESystem;
import mesquite.correl.lib.RK4Solver;
import mesquite.correl.lib.CladeExtinctionModel;
import mesquite.lib.CommandRecord;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.Tree;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.lib.duties.NumberForCharAndTree;

public class ExtinctionIntegrator extends NumberForCharAndTree {
	
	DEQNumSolver solver;
	DESystem speciesModel;
	IntegLikeCateg calcTask;

	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
 		calcTask = (IntegLikeCateg)hireEmployee(commandRec, IntegLikeCateg.class, "Integrating Likelihood");
		if (calcTask == null)
			return sorry(commandRec, getName() + " couldn't start because no integrating likelihood calculator module obtained.");

		solver = new RK4Solver();
		speciesModel = new CladeExtinctionModel(0.001, 0.001, 0.005, 0.001, 0.01, 0.01);
		return true;
	}

	public void initialize(Tree tree, CharacterDistribution charStates1, CommandRecord commandRec) {
		// TODO Auto-generated method stub

	}

	public void calculateNumber(Tree tree, CharacterDistribution charStates, MesquiteNumber result, MesquiteString resultString, CommandRecord commandRec) {
		if (result == null)
			return;
		result.setToUnassigned();
		if (tree == null || charStates == null)
			return;
		calcTask.calculateLogProbability(tree, speciesModel, solver, charStates, resultString, result, commandRec);

	}


	public String getName() {
		// TODO Auto-generated method stub
		return "Speciation/Extinction Integrator";
	}
	
	public String getAuthors() {
		return "Peter E. Midford, Sarah P. Otto & Wayne P. Maddison";
	}

	public String getVersion() {
		return "0.1";
	}

	public String getExplanation(){
		return "Calculates likelihoods using a speciation/extinction model";
	}

	public boolean isPrerelease(){
		return true;
	}


}
