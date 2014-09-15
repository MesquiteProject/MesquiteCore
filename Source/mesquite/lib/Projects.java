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
import java.util.*;


/* ��������������������������� projects & files ������������������������������� */
/* ======================================================================== */
/** A class with a vector storing the projects currently active (one instantiation of this belongs to MesquiteTrunk)*/
public class Projects implements HNode {
	
	Vector projects;
	boolean[] colorsUsed;
	public static int projectsAdded = 0;//to catch memory leaks
	public static int projectsRemoved = 0; //to catch memory leaks
	public Projects() {
		if (ColorDistribution.numColorSchemes>0)
			colorsUsed = new boolean[ColorDistribution.numColorSchemes];
		else
			colorsUsed = new boolean[32];
		for (int i=0; i<colorsUsed.length; i++)
			colorsUsed[i]=false;
		projects = new Vector(1);
	}
	
	public int getNumProjects(){
		return projects.size();
	}
	
	/** Get p'th project. */
	public MesquiteProject getProject(int p) {
		if (p>=0 && p<projects.size())
			return (MesquiteProject)projects.elementAt(p);
		return null;
	}
	/** Get project with given id number. */
	public MesquiteProject getProjectByID(int id) {
		for (int i=0; i<projects.size(); i++){
			MesquiteProject project =(MesquiteProject)projects.elementAt(i);
			if (project.getID() == id)
				return project;
		}
		return null;
	}
	/** Add project to the list of projects. */
	public void addProject(MesquiteProject project) {
		projects.addElement(project);
		projectsAdded++;
		MesquiteTrunk.mesquiteTrunk.refreshBrowser(MesquiteProject.class);
	}
	private void resetColorsAvailable(){
		for (int i=0; i<colorsUsed.length; i++)
			colorsUsed[i]=false;
		for (int i=0; i<projects.size(); i++){
			MesquiteProject mp = (MesquiteProject)projects.elementAt(i);
			if (mp.getProjectColor()< colorsUsed.length)
				colorsUsed[mp.getProjectColor()] = true;
		}
	}
	private boolean colorAvailable(int which){
		if (which< colorsUsed.length)
			return !colorsUsed[which];
		return false;
	}
	/** Returns the next base color for the project's windows (used when project starts up) */
	public int requestNextColor(){
		for (int i=0; i<ColorDistribution.numColorSchemes; i++)
			if (colorAvailable(i)) {
				colorsUsed[i] = true;
				return i;
			}
		return 0;
		
	}
	/** Remove project from list of projects.  The File Coordinator is responsible for disposing of the files within the project. */
	public void removeProject(MesquiteProject project) {
		projects.removeElement(project);
		projectsRemoved++;
		resetColorsAvailable();
		MesquiteTrunk.mesquiteTrunk.refreshBrowser(MesquiteProject.class);
	}
	/** For HNode interface; returns projects themselves */
	public HNode[] getHDaughters(){
		if (projects.size()== 0)
			return null;
		HNode[] daughters = new HNode[projects.size()];
		for (int i = 0; i < projects.size(); i++)
			daughters[i] = (HNode)projects.elementAt(i);
		return daughters;
	}
	/** For HNode interface */
	public HNode getHMother(){
		return null;
	}
	/** For HNode interface */
	public String getName(){
		String name = null;
		if (getNumProjects() == 0)
			name = "No Projects Open";
		else	if (getNumProjects() == 1)
			name = Integer.toString(getNumProjects()) + " Project Open";
		else
			name = Integer.toString(getNumProjects()) + " Projects Open";
		if (MesquiteTrunk.author.hasDefaultSettings())
			return name;
		else
			return name + ".  Current Author: " + MesquiteTrunk.author.getName();
	}
	/** For HNode interface */
	public String getTypeName(){
		return null;
	}
	/** For HNode interface */
	public int getNumSupplements(){
		return 0;
	}
	/** For HNode interface */
	public String getSupplementName(int index){
		return null;
	}
	/** For HNode interface */
	public void hNodeAction(Container c, int x, int y, int action){
		if (action == HNode.MOUSEMOVE){
			MesquiteWindow f = MesquiteWindow.windowOfItem(c);
			if (f != null && f instanceof MesquiteWindow){
				((MesquiteWindow)f).setExplanation("This is a list of all active projects, the files that they comprise, and all of the major contained elements of information");
			}
		}
		else if (action == HNode.MOUSEEXIT){
			/*
			Frame f = MesquiteTrunk.mesquiteTrunk.containerOfModule();
			if (f != null && f instanceof MesquiteWindow){
				((MesquiteWindow)f).setExplanation("");
			}
			*/
		}
	}
	/** For HNode interface */
	public void hSupplementTouched(int index){
	}
	/* ---------------- for HNode interface ----------------------*/
	public Image getHImage(){
		return null;
	}
	/* ---------------- for HNode interface ----------------------*/
	public Color getHColor(){
		return Color.white;
		//return ColorTheme.getInterfaceBackground(); 
	}
	/* ---------------- for HNode interface ----------------------*/
	public boolean getHShow(){
		return true; 
	}
}

