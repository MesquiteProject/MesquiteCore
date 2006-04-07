/* Written by MTH using mesquite.treefarm.RandomResolve as a template */

package mesquite.cipres.RecIDCM3;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import mesquite.lib.CommandRecord;
import mesquite.lib.MesquiteTree;
import mesquite.lib.Tree;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.MatrixSourceCoord;
import mesquite.treefarm.lib.DetTreeModifier;
import mesquite.cipres.CipresLoader;
import mesquite.cipres.MesquiteCipresTypeConverter;

import org.cipres.CipresIDL.Rid3TreeImprove;
import org.cipres.communication.Facilitator;
import org.cipres.helpers.CipresServiceDialog;
import org.cipres.helpers.RegistryEntryWrapper;
import org.cipres.helpers.CipresRegistry;
import org.cipres.datatypes.TreeWrapper;

public class RecIDCM3 extends DetTreeModifier
{
	MatrixSourceCoord characterSourceTask;
	RegistryEntryWrapper rid3Wrapper = null;
	String cipresRoot = "";

	/*
	 * @todo: 1. How do we indicate that the user cancelled from recidcm3
	 * dialog? 2. How do we indicate that an error happened in recidcm3? 3. Use
	 * discreetAlert() instead of JOptionPane 4. Scripting? 5. Handle the other
	 * datatypes in addition to DNA. See MesquiteCipresTypeConverter.
	 * toCipresMatrix(). 6. Mesquite's "An operation is in progress" (or
	 * something like that) dialog sometimes appears on top of recidcm3 dialog.
	 */
	public void modifyTree(Tree tree, MesquiteTree modified, int which,
			CommandRecord commandRec)
	{

		org.cipres.CipresIDL.Tree cipresInitialTree = null;
		org.cipres.CipresIDL.Tree cipresFinalTree = null;
		org.cipres.CipresIDL.DataMatrix cipresMatrix = null;

		if (tree == null || modified == null || characterSourceTask == null)
			return;
		MCharactersDistribution matrix = characterSourceTask.getCurrentMatrix(
				tree, commandRec);
		if (matrix == null)
			return;

		try
		{
			try
			{

				// Facilitator handles CORBA and Cipres framework initialization
				// and can be
				// called multiple times; only the first call does any actual
				// work.
				// The application name argument is used to find properties in
				// cipres_config.properties
				// for this application. In particular,
				// <applicationname>.use_colocated_registry,
				// determines whether we instantiate a colocated registry or
				// look for an external one.
				Facilitator.initialize("mesquite");

			} catch (NoClassDefFoundError e)
			{
				throw new Exception("Mesquite failed to load the CIPRes library. " +
						"Mesquite needs to be told where CIPRes is installed.", e);
			} catch (org.cipres.registry.RegistryConfigError e)
			{
				//@ todo: add a cipres method that displays the preference setter and
				// configures cipres.  call it here.
				throw new Exception("CIPRes needs to be configured before it can be used in Mesquite. "
						+ "Please run the stand-alone CIPRes application to configure CIPRes.", e);
			}

			// Find a registry entry for a recidcm3 service and display it's
			// dialog.
			rid3Wrapper = CipresRegistry.getCipresServiceWrapper(
					Rid3TreeImprove.class, null, null);
			CipresServiceDialog dialog = rid3Wrapper.getServiceDialog(null);
			int status = dialog.showAndInitialize();
			if (status == CipresServiceDialog.OK)
			{
				// convert to cipres datatypes
				cipresMatrix = MesquiteCipresTypeConverter
						.toCipresMatrix(matrix);
				cipresInitialTree = MesquiteCipresTypeConverter
						.toCipresTree(modified);

				Rid3TreeImprove service = (Rid3TreeImprove) rid3Wrapper
						.getService();
				service.setTree(cipresInitialTree);
				service.setMatrix(cipresMatrix);
				cipresFinalTree = service.improveTree(null);

				// The current IDL for improveTree() doesn't allow an exception
				// to be thrown
				// so recidcm3 instead returns an "empty" tree if it runs into
				// an error.
				if (TreeWrapper.isEmpty(cipresFinalTree))
				{
					discreetAlert(commandRec,
							"Rec-i-dcm3 encountered an error.  The tree hasn't been modified.");
					cipresFinalTree = null;
				} else
				{
					// JOptionPane.showMessageDialog(null, "Final tree is: " +
					// TreeWrapper.asString(cipresFinalTree));
				}
			} else if (status == CipresServiceDialog.ERROR)
			{
				discreetAlert(commandRec,
						"Error initializing rec-i-dcm3 service.  Tree is unchanged.");
			} else
			{
				discreetAlert(commandRec,
						"Operation cancelled. Tree is unchanged.");
			}
		} catch (Exception e)
		{
			discreetAlert(commandRec, e.getMessage() + " Tree is unchanged.");
			e.printStackTrace();
		} finally
		{
			if (rid3Wrapper != null)
			{
				rid3Wrapper.releaseService();
			}
		}
		if (cipresFinalTree != null)
		{
			modified.readTree(cipresFinalTree.m_newick);
		}
	}

	public boolean startJob(String arguments, Object condition,
			CommandRecord commandRec, boolean hiredByName)
	{
		loadPreferences();
		String saveCipresRoot = cipresRoot;
		try
		{
			if (CipresLoader.loadCipres(cipresRoot))
			{
				cipresRoot = CipresLoader.getCipresRoot();
				if (!cipresRoot.equals(saveCipresRoot))
				{
					storePreferences();
				}
			} else
			{
				// user cancelled out of choose cipres dialog.
				return false;
			}
		}
		// Exception is thrown if directory doesn't contain the expected jar file or jar can't be loaded.
		catch (Exception e)
		{
			return sorry( commandRec, getName() + " couldn't start because " + e.getMessage());
		}

		characterSourceTask = (MatrixSourceCoord) hireEmployee(commandRec,
			MatrixSourceCoord.class, "Source of characters (for " + getName() + ")");
		if (characterSourceTask != null)
			return true;
		return sorry( commandRec, getName()
						+ " couldn't start because no source of characters was obtained.");
	}

	public void processPreferencesFromFile (String[] prefs) {
		if (prefs!=null && prefs.length>0)
			cipresRoot = prefs[0];
	}

	public String[] preparePreferencesForFile () {
		String[] prefs;
		prefs = new String[1];
		
		prefs[0] = cipresRoot;
		return prefs;
	}

	/*.................................................................................................................*/

	// .................................................................................................................
	public boolean isPrerelease()
	{
		return true;
	}

	// .................................................................................................................
	public boolean showCitation()
	{
		return true;
	}

	// .................................................................................................................
	public String getName()
	{
		return "CIPRES Rec-I-DCM3";
	}

	// .................................................................................................................
	public String getExplanation()
	{
		return "Searches for a better tree using recursive, iterative DCM3";
	}

	public boolean requestPrimaryChoice()
	{
		return false;
	}

	// .................................................................................................................
	public int getNumberOfTrees(Tree tree, CommandRecord commandRec)
	{
		return tree == null ? 0 : 1;
	}

}
