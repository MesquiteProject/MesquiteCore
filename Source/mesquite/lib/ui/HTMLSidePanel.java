/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)

 */
package mesquite.lib.ui;

import java.awt.*;

import javax.swing.*;

import mesquite.lib.MesquiteModule;


/* ======================================================================== */

public class HTMLSidePanel extends MousePanel{
	public static final int width = 300;
	public static final int height = 36;
	JEditorPane panel;
	JScrollPane scrollPane;
	Image goaway;
	MesquiteModule pw;
	String title;
	public HTMLSidePanel (MesquiteModule pw, String title){
		super();
		this.pw = pw;
		this.title = title;
		panel = new MesqJEditorPane("text/html","<html></html>");
		setLayout(null);
		panel.setVisible(true);
		goaway = MesquiteImage.getImage(MesquiteModule.getRootImageDirectoryPath() + "goAway.gif");
        scrollPane = new  MQJScrollPane(); 
        scrollPane.getViewport().add( panel,  BorderLayout.CENTER ); 
		add(scrollPane);
		setBackground(ColorDistribution.brown);
		setForeground(Color.white);
		panel.setBackground(Color.white);
		setSize(width, 500);
		scrollPane.setBounds(0, height, width, getBounds().height-height);
	}
	public void setTitle(String s){
		this.title = s;
	}
	public void setSize(int w, int h){
		scrollPane.setSize(w, h-height);
		super.setSize(w, h);
	}
	public void setBounds(int x, int y, int w, int h){
		scrollPane.setBounds(0, height, w, h-height);
		super.setBounds(x, y, w, h);
	}
	public void setText(String s){
		if (s == null)
			s = "";
		panel.setText(s);
	}
	/* to be used by subclasses to tell that panel touched */
   	public void mouseUp(int modifiers, int x, int y, MesquiteTool tool) {
		if (y> 2 && y< 2 + 16 && x >= 2 && x <= 2 + 16) 
			pw.panelGoAway(this);
		
	}
	public void paint(Graphics g){
  		if (title != null)
  			g.drawString(title, 20, height - 22);
	   	g.drawImage(goaway, 2, 2, this);
		g.fillRect(0, height-2, getBounds().width, 2);
	}

	
}

