/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trunk;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;

import mesquite.lib.MesquiteModule;
import mesquite.lib.ui.ColorTheme;
import mesquite.lib.ui.MQPanel;
import mesquite.lib.ui.MQTextArea;
import mesquite.lib.ui.MQTextField;
import mesquite.lib.ui.MesquiteDialog;
import mesquite.lib.ui.MesquiteWindow;



/*===============================================*/
public class WelcomeDialog extends MesquiteDialog {
	TextField t; 
	public WelcomeDialog (MesquiteWindow parent,  String title, String label) {
		super(parent, title);
		
		BorderLayout layout = new BorderLayout();
		setLayout(layout);
		setBackground(ColorTheme.getInterfaceBackground());
		Font f = new Font ("Dialog", Font.PLAIN, 12);
		Font fSmall = new Font ("Serif", Font.PLAIN, 6);
		TextArea labstr1;
		add("North", labstr1 = new MQTextArea (label, 10,10, TextArea.SCROLLBARS_VERTICAL_ONLY));
		
		Panel buttons = new MQPanel();
		Button cancel;
		Button ok;
		buttons.add("West", t = new MQTextField(" "));
		buttons.add("East", ok = new Button("OK"));
		setDefaultButton("OK");
		
		ok.setFont(f);
		labstr1.setFont(f);
		
		add("South", buttons);
		setSize(350, 250);
		labstr1.requestFocus();
		MesquiteWindow.centerWindow(this);
		setVisible(true);
	}
	

	public void buttonHit(String buttonLabel, Button button) {
		Dimension d = t.getSize();
		Graphics g = getGraphics();
		FontMetrics fm = g.getFontMetrics(g.getFont());
		g.dispose();
		int sw = fm.stringWidth(" ");
		int ma = fm.getMaxAscent();
		int md = fm.getMaxDescent();
		MesquiteModule.textEdgeCompensationHeight = d.height-ma-md;
		MesquiteModule.textEdgeCompensationWidth = d.width-sw;
	}
}

