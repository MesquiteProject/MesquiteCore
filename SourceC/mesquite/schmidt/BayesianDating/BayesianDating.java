package mesquite.schmidt.BayesianDating;

import java.io.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lists.lib.*;
import mesquite.categ.lib.*;
import mesquite.io.lib.*;

public class BayesianDating extends TaxonListUtility
	{TreeSource treeSourceTask;
	 int numTrees;
	 int numGenes;
	 int[] treeArray;
	 String[] fileNames;
	 String[] pathSeq;
	 String PAMLPath;
	 String MultiPath;
	 String scriptPath;
	 Taxa taxa;
	 Taxa newTaxa;
	 FileCoordinator coord;
	 
	 public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName)
	 	{return true;
	 	}
	 
	 public boolean operateOnTaxa(Taxa currentTaxa, CommandRecord commandRec)
	 	{taxa = currentTaxa;
		 newTaxa = taxa.cloneTaxa();
		 for (int it = 0; it < newTaxa.getNumTaxa(); it++)
			{newTaxa.setTaxonName(it, shortenTaxonName(newTaxa.getTaxonName(it), it, 10));
			}
		 
		 treeSourceTask = (TreeSource)hireEmployee(commandRec, TreeSource.class, "Source of trees for Bayesian dating"); 		
		 if (treeSourceTask == null)
			{return sorry(commandRec, getName() + " couldn't start because no source of trees was obtained.");
			}
  		 
		 numTrees = MesquiteInteger.queryInteger(containerOfModule(), "Number of Trees", "Number of trees to randomly draw from tree set", numTrees);
		 if (!MesquiteInteger.isCombinable(numTrees) || numTrees == 0)
		 	{return sorry(commandRec, getName() + " couldn't start because at least one tree must be analyzed.");
		 	}
		 
		 PAMLPath = MesquiteFile.chooseDirectory("Select the PAML Directory: ");
		 if (PAMLPath == null)
		 	{return sorry(commandRec, getName() + " couldn't start because PAML could not be found.");
		 	}
  		 
		 MultiPath = MesquiteFile.chooseDirectory("Select the Multidistribute Directory: ");
		 if (MultiPath == null)
			 {return sorry(commandRec, getName() + " couldn't start because Multidistribute could not be found.");
			 }
		 
		 coord = getFileCoordinator();
	
		 numGenes = getProject().getNumberCharMatrices(taxa);
		 fileNames = new String[numGenes];
		 pathSeq = new String[numGenes];
		 
		 for (int i = 0; i < numGenes; i++)
			 {CharacterData data = getProject().getCharacterMatrix(taxa, i);
			  fileNames[i] = MesquiteFile.massageStringToFilePathSafe(data.getName());
			  MolecularData molecularData = (MolecularData)data;
			  setupDatingAnalysis(i, molecularData, commandRec);
			 }
		 
		 treeArray = makeTreeArray(commandRec);		 
		 for (int i = 1; i < numTrees + 1; i++)
			 {saveTreeFile(i, treeSourceTask, commandRec);
			 }
		 
		 writeShellScript(commandRec);		 
		 runShellScript();
		 
		 return false;
	 	}
	
	 private boolean setupDatingAnalysis(int gene, MolecularData data, CommandRecord commandRec)
	 	{saveFastaFile(gene, data, commandRec);
		 
		 for (int i = 1; i < numTrees + 1; i++)
			 {writePAMLCTLFile(gene, i, commandRec);
			  writeHMMDatFile(gene, i, commandRec);
			 }
		 
		 return true;
	 	}
	 
	 private boolean saveFastaFile(int gene, MolecularData data, CommandRecord commandRec)
	 	{if (data == null)
			{return false;
			}	 	 
	 	 
	 	 pathSeq[gene] = PAMLPath + MesquiteFile.fileSeparator + fileNames[gene] + ".fasta";
		 incrementMenuResetSuppression();		

  		 MesquiteFile tempDataFile = (MesquiteFile)coord.doCommand("newLinkedFile", StringUtil.tokenize(pathSeq[gene]), commandRec, CommandChecker.defaultChecker);
 		
 		 StringBuffer exportBuffer = new StringBuffer();
		 exportBuffer.append("   " + taxa.getNumTaxa() + " " + data.getNumChars() + "\n\n");
		 
		 for (int it = 0; it < newTaxa.getNumTaxa(); it++)
		 	{exportBuffer.append(newTaxa.getTaxonName(it) + "\n");
		 	 for (int ic = 0; ic < data.getNumChars(); ic++)
		 	 	{int state = (int)data.getState(ic, it);
		 		 if (state == 8)
		 	 		{exportBuffer.append("T");
		 	 		}
		 	 	 else if (state == 4)
		 	 		{exportBuffer.append("G");
		 	 		}
		 	 	 else if (state == 2)
		 	 		{exportBuffer.append("C");
		 	 		}
		 	 	 else if (state == 1)
		 	 		{exportBuffer.append("A");
		 	 		}
		 	 	 else
		 	 	 	{exportBuffer.append("?");
		 	 	 	}
		 	 	}
		 	 exportBuffer.append("\n\n");
		 	}
		 
		  if (tempDataFile.openWriting(true))
			 {tempDataFile.writeLine(String.valueOf(exportBuffer));
			 }
		  tempDataFile.closeWriting();
		  tempDataFile.close();
		
		  return true;
	 	}	

	 private boolean runShellScript()
	 	{try
		   {ShellScriptUtil.setScriptFileToBeExecutable(scriptPath);
		   }
		 catch (IOException e)
		   {e.printStackTrace();
		   }
		 
		 Process proc = ShellScriptUtil.executeScript(scriptPath);
		 
		 return true;
	 	}
	 
	 private boolean writeShellScript(CommandRecord commandRec)
	 	{StringBuffer shellScript = new StringBuffer(1000);
		 String geneAndTree;  
	 	
		 for (int j = 1; j < numGenes + 1; j++)
			 	{for (int i = 1; i < numTrees + 1; i++)
			 		{geneAndTree = fileNames[j - 1] + "_tree" + i;
			 		 shellScript.append("cd " + PAMLPath + "\n");
			 		 shellScript.append("echo\n");
			 		 shellScript.append("echo ============= Analyzing Tree " + i + " of " + numTrees + " for Gene " + j + " of " + numGenes + " [ " + fileNames[j - 1] + " ] =============\n");
			 		 shellScript.append("echo Number " + treeArray[i - 1] + " of " + treeSourceTask.getNumberOfTrees(taxa, commandRec) + " Trees in Tree Set\n\n");
			 		 shellScript.append("echo\n");
			 		 shellScript.append("echo Calculating model parameter values in PAML...\n");
			 		 shellScript.append("./baseml " + geneAndTree + ".ctl > " + "out_tree" + i + "." + fileNames[j - 1] + "\n");
			 		 shellScript.append("grep lnL " + geneAndTree + ".out \n");
			 		 shellScript.append("cd " + MultiPath + "\n");
			 		 shellScript.append("./paml2modelinf " + PAMLPath + MesquiteFile.fileSeparator + geneAndTree + ".out modelinf." + fileNames[j - 1] + "\n");
			 		 shellScript.append("cp " + pathSeq[j - 1] + " testseq\n");
			 		 shellScript.append("echo\n");
			 		 shellScript.append("echo Calculating branch lengths in estbranches...\n");
			 		 shellScript.append("cp hmmcntrl_" + geneAndTree + ".dat hmmcntrl.dat\n");
			 		 shellScript.append("./estbranches oest." + geneAndTree  + " > out.oest." + geneAndTree + "\n");
			 		 shellScript.append("grep 'FINAL LIKE' out.oest." + geneAndTree + "\n");
			 		 shellScript.append("echo\n");

			 		}
			 	}
		  
		 shellScript.append("echo If the likelihoods obtained in PAML and estbranches for a given tree are very different, one or both of these programs failed to converge on the maximum likelihood estimate, and that tree should be excluded from the final analysis.\n");
		 shellScript.append("echo\n\n");
		 
		 scriptPath = PAMLPath + MesquiteFile.fileSeparator + "datingAnalysis.bat";
		 MesquiteFile.putFileContents(scriptPath, shellScript.toString(), true);
		 
		 return true;
	 	}
	 
	 private boolean writeHMMDatFile(int gene, int treeNumber, CommandRecord commandRec)
	 	{String pathHMMdat = MultiPath + MesquiteFile.fileSeparator + "hmmcntrl_" + fileNames[gene] + "_tree" + treeNumber + ".dat";
		  
		 MesquiteFile HMMDatFile = (MesquiteFile)coord.doCommand("newLinkedFile", StringUtil.tokenize(pathHMMdat), commandRec, CommandChecker.defaultChecker);
		  
		 StringBuffer hmmDatBuffer = new StringBuffer();
		  
		 hmmDatBuffer.append("   /* Which Model to use? */\n");
		 hmmDatBuffer.append("modelinf." + fileNames[gene] + "\n");
		 hmmDatBuffer.append("L  /* How much output? Options: L = Loud mode (prints more output, the \n");
		 hmmDatBuffer.append("      default), Q = Quiet mode (prints less output - use with parametric \n");
		 hmmDatBuffer.append("      bootstrap) */\n");	  
		 hmmDatBuffer.append("D  /* Predict Secondary Structure? Options: P= predict, D = do not predict\n");
		 hmmDatBuffer.append("      (the default option) */\n");
		 hmmDatBuffer.append("N  /* Does user tree specify names (N) or specify order (O) of sequences\n");
		 hmmDatBuffer.append("      in sequence data file? */\n");
		 hmmDatBuffer.append("   /* The topology is in the file listed below*/\n");
		 hmmDatBuffer.append("treeForDating2_tree" + treeNumber + "\n");
		 hmmDatBuffer.append("   /* End of hmmcntrl.dat */");
		  
		 if (HMMDatFile.openWriting(true))
		  	{HMMDatFile.writeLine(hmmDatBuffer.toString());
		  	}
		  
		 HMMDatFile.closeWriting();
 		 HMMDatFile.close();
		 
 		 return true;
	 	}
	 
	 private boolean writePAMLCTLFile(int gene, int treeNumber, CommandRecord commandRec)
	 	{String pathCTL = PAMLPath + MesquiteFile.fileSeparator + fileNames[gene] + "_tree" + treeNumber + ".ctl";
		  
		 MesquiteFile PAMLctlFile = (MesquiteFile)coord.doCommand("newLinkedFile", StringUtil.tokenize(pathCTL), commandRec, CommandChecker.defaultChecker);
		  
		 StringBuffer ctlBuffer = new StringBuffer();
		  
		 ctlBuffer.append("      seqfile = " + PAMLPath + MesquiteFile.fileSeparator + fileNames[gene] + ".fasta\n");
		 ctlBuffer.append("     treefile = " + PAMLPath + MesquiteFile.fileSeparator + "treeForDating_tree" + treeNumber + "\n");
		 ctlBuffer.append("      outfile = " + PAMLPath + MesquiteFile.fileSeparator + fileNames[gene] + "_tree" + treeNumber + ".out\n");
		 ctlBuffer.append("        noisy = 9\n");
		 ctlBuffer.append("      verbose = 1\n");
		 ctlBuffer.append("      runmode = 0\n");
		 ctlBuffer.append("        model = 3\n");
		 ctlBuffer.append("        Mgene = 0\n");
		 ctlBuffer.append("    fix_kappa = 0\n");
		 ctlBuffer.append("        kappa = 5\n");
		 ctlBuffer.append("    fix_alpha = 0\n");
		 ctlBuffer.append("        alpha = 0.5\n");
		 ctlBuffer.append("       Malpha = 0\n");
		 ctlBuffer.append("        ncatG = 5\n");
		 ctlBuffer.append("        nparK = 0\n");
		 ctlBuffer.append("        clock = 0\n");
		 ctlBuffer.append("        nhomo = 1\n");
		 ctlBuffer.append("        getSE = 0\n");
		 ctlBuffer.append(" RateAncestor = 0\n");
		 ctlBuffer.append("   Small_Diff = 7e-6\n");
		 ctlBuffer.append("    cleandata = 0\n");
		  
		 if (PAMLctlFile.openWriting(true))
		 	{PAMLctlFile.writeLine(ctlBuffer.toString());
		  	}
		 		 
		 PAMLctlFile.closeWriting();
		 PAMLctlFile.close();
		 
		 return true;
	 	}
	 
	 private int[] makeTreeArray(CommandRecord commandRec) 
	 	{int[] treeArray = new int[numTrees];
	 	 
	 	 Random rng;
	 	 rng = new Random();
	 	 rng.setSeed(System.currentTimeMillis());
	 	 
	 	 int numTreesInSet = treeSourceTask.getNumberOfTrees(taxa, commandRec);
	 	 
	 	 for (int i = 0; i < numTrees; i++)
	 	 	{treeArray[i] = (int)(rng.nextDouble() * (numTreesInSet));		 
	 	 	}
		 
	 	 return treeArray;
	 	}
	 
	 private boolean saveTreeFile(int treeNumber, TreeSource treeSourceTask, CommandRecord commandRec)
	 	{Tree treeForDating = treeSourceTask.getTree(taxa, treeArray[treeNumber - 1], commandRec);
		  
		 String pathTree = PAMLPath + MesquiteFile.fileSeparator + "treeForDating_tree" + treeNumber;
		 MesquiteFile tempTreeFile = (MesquiteFile)coord.doCommand("newLinkedFile", StringUtil.tokenize(pathTree), commandRec, CommandChecker.defaultChecker);
		 
		 String treeDescription = "";
		 treeDescription = writeDatingTree(newTaxa, treeForDating, 2, treeDescription) + ";";
		 
		 tempTreeFile.openWriting(true);
		 tempTreeFile.writeLine(treeDescription);
		 tempTreeFile.closeWriting();
		 tempTreeFile.close();

		 String pathTree2 = MultiPath + MesquiteFile.fileSeparator + "treeForDating2_tree" + treeNumber;
		 MesquiteFile tempTreeFile2 = (MesquiteFile)coord.doCommand("newLinkedFile", StringUtil.tokenize(pathTree2), commandRec, CommandChecker.defaultChecker);
		 
		 tempTreeFile2.openWriting(true);
		 tempTreeFile2.writeLine("junkline\n" + treeDescription);		 
		 tempTreeFile2.closeWriting();
		 tempTreeFile2.close();
		 
		 return true;
	 	}
	 
	 public String shortenTaxonName(String name, int index, int length)
	 	{String returnName = "";
		 char currentChar;
		 int orderOfMag = Integer.toString(index).length();
		 for (int i = 0; i < length; i++)
	 		{currentChar = name.charAt(i);
	 		 if (returnName.length() < length - orderOfMag)
	 			{if (currentChar != ' ')
	 				{returnName += currentChar;
	 				}
	 			}
	 		 else
	 		 	{break;
	 		 	}
	 		}
	 	 returnName += Integer.toString(index);
		 return returnName;
	 	}
	 
	 public String writeDatingTree(Taxa taxa, Tree tree, int node, String treeDescription)
	 	{if (tree.nodeIsInternal(node))
			{treeDescription = treeDescription + '(';
			 int thisSister = tree.firstDaughterOfNode(node);
			 treeDescription = writeDatingTree(taxa, tree, thisSister, treeDescription);
			 while (tree.nodeExists(thisSister = tree.nextSisterOfNode(thisSister)))
			 	{treeDescription = treeDescription + ',';
				 treeDescription = writeDatingTree(taxa, tree, thisSister, treeDescription);
			 	}
			 treeDescription = treeDescription + ')';
			}
		 else
			{treeDescription = treeDescription + StringUtil.tokenize(taxa.getTaxonName(tree.taxonNumberOfNode(node)));
			}
	 	
	 	 return treeDescription;
	 	}
	 
	 public String getName()
	 	{return "Bayesian Dating";
	 	}
	 
	 public String getExplanation()
	    {return "Performs Thorne and Kishino Bayesian dating analysis.";
	    }

	}
