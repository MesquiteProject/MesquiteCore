package mesquite.schmidt.ConsensusBranchLengths;

import mesquite.lib.*;
import mesquite.lib.duties.*;

public class ConsensusBranchLengths extends BranchLengthsAlterer
	{TreeSource treeSourceTask;
	 double[][] sampledBranchLengths;
	 int[] numTreesWithBranch;
	 double[] meanBranchLengths;
	 double[] stdDevBranchLengths;
	 int numTrees;
	 int numNodes = 0;
	 Taxa taxa;
	 
	 public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName)
		{return true;
		}

	 private void countNodes(AdjustableTree tree, int node)
	 	{for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter))
			{countNodes(tree, daughter);
			}
		 numNodes++; 
	 	}
	 
	 private void setLengths(AdjustableTree tree, int node, CommandRecord commandRec)
	 	{for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter))
			{setLengths(tree, daughter, commandRec);
			}
		 for (int i = 0; i < numTrees; i++)
		 	{Tree currentTree = treeSourceTask.getTree(taxa, i, commandRec);
		 	 if (currentTree.isClade(tree.getTerminalTaxaAsBits(node)))
		 	 	{int nodeInCurrentTree = currentTree.mrca(tree.getTerminalTaxaAsBits(node));
		 		 //logln(String.valueOf(node));
		 		 //logln(String.valueOf(numTreesWithBranch[node]));
		 	 	 //logln(String.valueOf(nodeInCurrentTree));
		 	 	 sampledBranchLengths[node][numTreesWithBranch[node]] = currentTree.getBranchLength(nodeInCurrentTree);
		 		 //sampledBranchLengths[0][0] = 0;
		 	 	 //numTreesWithBranch[node] = 0;
		 	 	 //sampledBranchLengths[node][numTreesWithBranch[node]] = 0.1;
		 	 	 numTreesWithBranch[node]++;
		 	 	 //logln(String.valueOf(meanBranchLengths[node]));
		 	 	 //logln(String.valueOf(sampledBranchLengths[node][i]));
		 	 	}
		 	}
	 	}
	 
	 public boolean transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify, CommandRecord commandRec)
	 	{if (tree instanceof MesquiteTree)
	 		{taxa = tree.getTaxa();
	 		
	 		 treeSourceTask = (TreeSource)hireEmployee(commandRec, TreeSource.class, "Source of trees with branch lengths"); 		
	 			if (treeSourceTask == null)
	 				{return sorry(commandRec, getName() + " couldn't start because no source of trees was obtained.");
	 				} 
	 			
	 		 numTrees = treeSourceTask.getNumberOfTrees(taxa, commandRec);
	 		 countNodes(tree, 2);
	 		 double[][] sampledBranchLengths = new double[numNodes][numTrees];
	 		 int[] numTreesWithBranch = new int[numNodes];
	 		 double[] meanBranchLengths = new double[numNodes];
	 		 double[] stdDevBranchLengths = new double[numNodes];
	 		 
	 		 for (int i = 0; i < numNodes; i++)
	 		 	{for (int j = 0; j < numTrees; j++)
	 		 		{sampledBranchLengths[i][j] = 0;
	 		 		}
	 		 	 numTreesWithBranch[i] = 0;
	 		 	 meanBranchLengths[i] = 0;
	 		 	 stdDevBranchLengths[i] = 0;
	 		 	}
	 		 
	 		 //logln(Integer.toString(numNodes));
	 		 
	 		 setLengths(tree, 2, commandRec);
	 		 
	 		 if (notify && tree instanceof Listened) ((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.BRANCHLENGTHS_CHANGED), commandRec);
			 	{return true;
			 	}
	 		}
	 	 return false;	
	 	}
	
	 public boolean requestPrimaryChoice()
	 	{return true;  
	 	}
	 
	 public String getName()
	 	{return "Consensus Branch Lengths";
	 	}

	 public String getExplanation()
	 	{return "Sets branch lengths to be the means from a sample of trees." ;
	 	}
	 
	}
