/*
 * This software is part of the Tree Set Visualization module for Mesquite,
 * written by Jeff Klingner, Fred Clarke, and Denise Edwards.
 *
 * Copyright (c) 2002 by the University of Texas
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose without fee is hereby granted under the GNU Lesser General
 * Public License, as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version,
 * provided that this entire notice is included in all copies of any
 * software which are or include a copy or modification of this software
 * and in all copies of the supporting documentation for such software.
 *
 * THIS SOFTWARE IS BEING PROVIDED "AS IS", WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTY.  IN PARTICULAR, NEITHER THE AUTHORS NOR THE UNIVERSITY OF TEXAS
 * AT AUSTIN MAKE ANY REPRESENTATION OR WARRANTY OF ANY KIND CONCERNING THE
 * MERCHANTABILITY OF THIS SOFTWARE OR ITS FITNESS FOR ANY PARTICULAR PURPOSE.
 * IN NO CASE WILL THESE PARTIES BE LIABLE FOR ANY SPECIAL, INCIDENTAL,
 * CONSEQUENTIAL, OR OTHER DAMAGES THAT MAY RESULT FROM USE OF THIS SOFTWARE.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 *	Last change:  DE   16 Apr 2003   11:18 am
 */

package mesquite.treecomp.TreeSetVisualizationV2;

import mesquite.lib.*; //MesquiteWindow, MesquiteProject, MesquiteModule
import mesquite.lib.duties.*;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.BitSet;
import java.util.ArrayList;

/**
 * Class <code>PointSetVisualization</code> is an abstract superclass for
 * displaying <em>Point Sets</em>. Examples of <em>Point Sets</em> to be
 * displayed could be <em>Taxa Sets</em> or <em>Tree Sets</em>.
 */
public abstract class PointSetVisualization extends MesquiteWindow {

	/* members implementing basic functionality of MDS and point display */
	/** object that actually performs the mds calculations */
	protected MDS mds;
	/** Panel to display the points. */
	protected EmbeddingDisplayPanel embeddingDisplay;
	/** The big 2D Difference Matrix over all the trees (Computed at initialization). */
	protected SampledDiffMatrix itemDiffMatrix;
	/** Point locations in the current embedding (Shared with the MDS class). */
	private SharedPoints sharedPoints;
	/** Main thread that controls the MDS loop. */
	private MDSThread mdsThread;
	/** object to handle the details of multiple selection */
	protected SelectionManager selectionManager;
	/** number of items in the sample when sampled-MDS is enabled */
	private int sampleSize;
	/** Dimensionality of the MDS embedding */
	protected static int DIMENSIONS = 2;
	/** constant used to calibrate the stepSize value */
	protected static float STEPSIZE_DIVIDEND = 1.8245f;

	/* members for search animation functionality */
	/** a utility timer thread used to schedule the search animation */
	private java.util.Timer animationTimer;
	/** The task that actually does the animation, making one more item visible each time its run() is called */
	private java.util.TimerTask animationTask;
	/** rate at which items appear in the search animation (items per second) */
	private final static int DEFAULT_ANIMATION_RATE = 30;

	/* members for implementing tree score coloring */
	/*WPM Oct05:  the word "optimality" has been replaced by "tree score" throughout 
	 * because the scores need not reflect optimality. Instead, the scores are taken from any
	 * NumberForTree module.  Scores from MrBayes can still be obtained, but currently only by selecting
	 * Trees from MrBayes as the tree source and selecting MrBayes scores as the value for tree coloring.
	 */
	/** A score for each tree (likelihood, parsimony, etc.) used to compute colors */
	protected double[] treeScores;
	/** one color for each point, used when tree score Coloring is activated */
	protected Color[] treeScoreColors;
	/** a panel that displays the key to the tree score coloring */
	private ColorKey colorKey;
	
	/** the module that calculates the values to color the points*/
	NumberForTree treeScoresTask;
	
	/** reference to the current mesquite project, used to get the working directory to read in tree scores */
	private MesquiteProject project;
	/** color checkpoints used to define ColorGradient */
	private final static Color[] colorsForGradient = {Color.blue, Color.cyan, Color.green, Color.yellow, Color.red};
	/** the range of color used in tree score coloring and the corresponding key */
	protected final static ColorGradient colorGradient = new ColorGradient(colorsForGradient);
	
	/* User interface elements to which a reference needs to be maintained outside of the constructor */

	/** a text field to display the stress of the current embedding */
	private Label stressDisplayLabel;
	/* The following five interface elements are package-accessible so that the Mesquite wrapper class,
	   TreeSetVisualization, can control them in response to Mesquite Commands. */
	private TextField stepSizeField;
	private TextField animationRateField;
	private Checkbox sampleCheckbox;
	protected Checkbox treeScoreColoringCheckbox;
	private TextField sampleSizeField;
	
	/** commands to be able to put event handling on main Mesquite execution thread */
	private MesquiteCommand treeScoreColoringCheckboxCommand, animateButtonCommand, animationRateFieldCommand, sampleCheckboxCommand, sampleSizeFieldCommand, decStepSizeButtonCommand, incStepSizeButtonCommand, stepSizeFieldCommand, scrambleButtonCommand, startStopButtonCommand;
	/** overall panel containing everything */
	protected Panel pointSetPanel;

	/*  Window layout constants */
	/** height of the window in pixels */
	private static int WINDOW_HEIGHT = 643;
	/** width of the window in pixels */
	private static int WINDOW_WIDTH = 500;
	
	private MesquiteString treeScoresTaskName;
	
	Button startStopButton;
	
	Panel mainPanel;
	//private int numberOfItems;
	
	public boolean needToRecalculateTreeScores = true;
	
	/**
	 * Constructor for initializing objects
	 *
	 *@param  ownerModule    A reference to the MesquiteModule that utilizes the window.
	 *@param  numItems       the number of items to be visualized (trees, taxa, etc.)
	 */
	public PointSetVisualization(MesquiteModule ownerModule, int numItems) {
		super(ownerModule, true); // true means the Mesquite info bar will be shown in this window
		
		final int numberOfItems = numItems; //local constructor copy for accessibilty from inner classes
		project = ownerModule.getProject();
		
		treeScoresTaskName = new MesquiteString();
		if (ownerModule.numModulesAvailable(NumberForTree.class)>1){
			MesquiteSubmenuSpec mss = ownerModule.addSubmenu(null, "Values to Color Trees", ownerModule.makeCommand("setTreeScoreColorer", this), NumberForTree.class);
			mss.setSelected(treeScoresTaskName);
		}

		sampleSize = numberOfItems / 10 + 1; //initial sample size; sampling is off initially
		sharedPoints = new SharedPoints(numberOfItems, DIMENSIONS);
		itemDiffMatrix = new SampledDiffMatrix(numberOfItems);
		mds = new MDS(itemDiffMatrix, DIMENSIONS, STEPSIZE_DIVIDEND / numberOfItems);
		sharedPoints.setPoints(mds.getEmbedding());
		selectionManager = new SelectionManager(numberOfItems,this);
		embeddingDisplay = new EmbeddingDisplayPanel(selectionManager, sharedPoints);
		selectionManager.setEmbeddingDisplay(embeddingDisplay);
		selectionManager.setColorKey(null);

		/* Set up the MDS loop control thread */
		mdsThread = new MDSThread(mds, sharedPoints, this);
		// set the MDS thread to a lower priority than the parent (UI) thread to ensure a snappy interface
		mdsThread.setPriority(Thread.currentThread().getPriority() - 1);
		// Start the MDSthread here, on initialization.  waitFlag==true will cause it to suspend
		// before doing any calculations.  It will resume when notify() is called in response
		// to a push of the start button.
		mdsThread.waitFlag = true;
		mdsThread.start();

		// Set up the timer thread used for tree search animation
		animationTimer = new java.util.Timer();

		/*WPM Oct05: previously, the tree scores were obtained by reading a scores file from disk.  This has been
		replaced by the standard [procedure of hiring a NumberForTree module and using it to assign scores to the tree
		 */
		treeScoreColors = null;
		treeScores = null;

		/*###############################################################
		##  User interface definitions. Buttons, textfields, panels, etc.
		##  are intitialized, and user interface reactions are defined
		#################################################################*/
		
		/*WPM Oct05
		 * To avoid reentrancy issues, all execution (apart from graphics, AWT events and a few special cases)
		 * in Mesquite is to happen on a main Mesquite execution thread.  The thread is accessed
		 * via the text command line system.  Thus we need to create commands and then when
		 * events are received, the command.doItMainThread method is called.
		 */
		treeScoreColoringCheckboxCommand = new MesquiteCommand("setTreeScoreColoring", this);
		animateButtonCommand = new MesquiteCommand("animateButtonCommand", this); 
		animationRateFieldCommand = new MesquiteCommand("setAnimationRate", this);
		sampleCheckboxCommand = new MesquiteCommand("setSampling", this); 
		sampleSizeFieldCommand = new MesquiteCommand("setSampleSize", this); 
		decStepSizeButtonCommand = new MesquiteCommand("decStepSizeButtonCommand", this);
		incStepSizeButtonCommand = new MesquiteCommand("incStepSizeButtonCommand", this);
		stepSizeFieldCommand = new MesquiteCommand("setStepSize", this);
		scrambleButtonCommand = new MesquiteCommand("scrambleButtonCommand", this);
		startStopButtonCommand = new MesquiteCommand("startStopButtonCommand", this);
		
		/* button used to start and suspend the MDS caluclations. final so it can refer to itself */
		startStopButton = new Button("Start MDS");
		startStopButton.setActionCommand("start");
		//WPM Oct05 modified to put response on main execution thread
		startStopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startStopButtonCommand.doItMainThread(e.getActionCommand(), null, null);

				//System.out.println("Start/Stop pressed. Priority = " + Thread.currentThread().getPriority());
			}
		});

		/* button to randomize the embedding (to start MDS over) */
		Button scrambleButton = new Button("Scramble");
		//WPM Oct05 modified to put response on main execution thread
		scrambleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scrambleButtonCommand.doItMainThread(null, null, null);
			}
		});

		/* field to show the current step size and make it adjustable.  final so that it can be
		   referred to by other interface elements (sampling & plus/minus buttons) */
		stepSizeField = new TextField(7) {
			public void setText(String text) { // a new definition of setText() used to truncate the displayed text
				if (text.length() > 8) {
					if (text.charAt(text.length() - 3) == 'E') { // scientific notation, insignificant figs in the middle
						super.setText(text.substring(0,5) + text.substring(text.length() - 3, text.length()));
					} else { // not scientific notation, insignificant figs at the end
						super.setText(text.substring(0,8));
					}
				} else {
					super.setText(text);
				}
			}
		};
		stepSizeField.setText(Float.toString(STEPSIZE_DIVIDEND / numberOfItems));
		stepSizeField.setBackground(Color.white);
		//WPM Oct05 modified to put response on main execution thread
		stepSizeField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stepSizeFieldCommand.doItMainThread(null, null, null);
			}
		});

		/* a small '+' button to increase the step size a little */
		Button incStepSizeButton = new Button("+");
		//WPM Oct05 modified to put response on main execution thread
		incStepSizeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				incStepSizeButtonCommand.doItMainThread(null, null, null);
		}
		});

		/* a small '-' button to decrease the step size a little */
		//WPM Oct05 modified to put response on main execution thread
		Button decStepSizeButton = new Button("-");
		decStepSizeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				decStepSizeButtonCommand.doItMainThread(null, null, null);
			}
		});

		/* a text field where the user can set the sample size. Only enabled if sampling is enabled. */
		sampleSizeField = new TextField("0", 5);
		sampleSizeField.setBackground(Color.white);
		sampleSizeField.setEnabled(false);
		sampleSizeField.setEditable(false);
		sampleSizeField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				sampleSizeFieldCommand.doItMainThread(StringUtil.tokenize(sampleSizeField.getText()), null, null);
			}
		});

		/* a check boxed used to enable/disable sampling. */
		sampleCheckbox = new Checkbox("Sampled MDS", false);
		//WPM Oct05 modified to put response on main execution thread
		sampleCheckbox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (sampleCheckbox.getState())
					sampleCheckboxCommand.doItMainThread("on", null, null);
				else
					sampleCheckboxCommand.doItMainThread("off", null, null);
			}
		});

		/* Panel that holds the MDS Controls and the Embedding Display Panel. */
		// declared here so that treeScoreColoringCheckbox can refer to it
		mainPanel = new Panel(new BorderLayout(0,0));

		/* checkbox used to turn tree score coloring on and off */
		//WPM Oct05 modified to put response on main execution thread
		treeScoreColoringCheckbox = new Checkbox("Tree Coloring");
		treeScoreColoringCheckbox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (treeScoreColoringCheckbox.getState())
					treeScoreColoringCheckboxCommand.doItMainThread("on", null, null);
				else
					treeScoreColoringCheckboxCommand.doItMainThread("off", null, null);
			}
		});

		/* Used to control the rate of the tree search animation */
		animationRateField = new TextField(Integer.toString(DEFAULT_ANIMATION_RATE), 4);
		animationRateField.setBackground(Color.white);
		animationRateField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				animationRateFieldCommand.doItMainThread(StringUtil.tokenize(animationRateField.getText()), null, null);
			}
		});

		/* Button used to start the tree search animation */
		Button animateButton = new Button("Animate Tree Order");
		animateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				animateButtonCommand.doItMainThread(null, null, null);
			}
		});

		stressDisplayLabel = new Label("", Label.LEFT) {
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				if (d.width <= 60) {
					d.width = 60;
				}
				return d;
			}
		};

		// Build the organization of the control panel
		Panel sssPanel = new Panel(new GridLayout(2,1));
		sssPanel.add(startStopButton);
		sssPanel.add(scrambleButton);
		Panel sssFlowPanel = new Panel(new FlowLayout(FlowLayout.LEFT,0,0));
		sssFlowPanel.add(sssPanel);

		Panel stressPanel = new Panel(new GridLayout(2,1));
		stressPanel.add(new Label("Stress:", Label.CENTER));
		stressPanel.add(stressDisplayLabel);
		Panel stressFlowPanel = new Panel(new FlowLayout(FlowLayout.CENTER,0,0));
		stressFlowPanel.add(stressPanel);

		Panel plusMinusPanel = new Panel(new GridLayout(2,1));
		plusMinusPanel.add(incStepSizeButton);
		plusMinusPanel.add(decStepSizeButton);
		Panel stepSizePanel = new Panel(new GridLayout(2,1));
		stepSizePanel.add(new Label("Step Size:", Label.CENTER));
		stepSizePanel.add(stepSizeField);
		Panel stepSizeLocalFlow = new Panel();
		stepSizeLocalFlow.add(stepSizePanel);
		Panel stepSizeBorderPanel = new Panel(new BorderLayout());
		stepSizeBorderPanel.add(stepSizeLocalFlow,BorderLayout.CENTER);
		stepSizeBorderPanel.add(plusMinusPanel,BorderLayout.EAST);
		Panel stepSizeFlowPanel = new Panel(new FlowLayout(FlowLayout.RIGHT,0,0));
		stepSizeFlowPanel.add(stepSizeBorderPanel);

		Panel samplePanel = new Panel(new GridLayout(2,1));
		samplePanel.add(sampleCheckbox);
		Panel sampleSizePanel = new Panel(new FlowLayout(FlowLayout.CENTER,0,0));
		sampleSizePanel.add(new Label("Sample Size:", Label.RIGHT));
		sampleSizePanel.add(sampleSizeField);
		samplePanel.add(sampleSizePanel);
		Panel sampleFlowPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
		sampleFlowPanel.add(samplePanel);

		Panel leftMdsControlsPanel = new Panel(new FlowLayout(FlowLayout.CENTER,0,0));
		leftMdsControlsPanel.add(sssFlowPanel);
		leftMdsControlsPanel.add(stressFlowPanel);

		Panel mdsControlsPanel = new Panel(new BorderLayout(3,1));
		mdsControlsPanel.add(leftMdsControlsPanel,BorderLayout.WEST);
		mdsControlsPanel.add(sampleFlowPanel,BorderLayout.CENTER);
		mdsControlsPanel.add(stepSizeFlowPanel,BorderLayout.EAST);

		Component mdsLabel = new Component() {
			public Dimension getPreferredSize() { return new Dimension(18,0); }
			public void paint(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setColor(getBackground());
				g2.fillRect(0,0,getBounds().width,getBounds().height);
				g2.setColor(Color.white);
				g2.setFont(new Font(g2.getFont().getFontName(),Font.BOLD,16));
				java.awt.geom.Rectangle2D rec = g2.getFont().getStringBounds("MDS",g2.getFontRenderContext());
				int x = 15;
				int y = (getHeight() + rec.getBounds().width)/2;
				java.awt.geom.AffineTransform savedTransformation = g2.getTransform();
				g2.transform(java.awt.geom.AffineTransform.getRotateInstance(-Math.PI/2,x,y));
				g2.drawString("MDS",x,y);
				g2.setTransform(savedTransformation);
			}
		};
		mdsLabel.setBackground(Color.black);

		Panel overallMdsPanel = new Panel(new BorderLayout(0,1));
		overallMdsPanel.add(mdsLabel,BorderLayout.WEST);
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1; c.weighty = 1;
		c.insets = new Insets(0,5,0,5); // (top,left,bottom,right)
		Panel marginBagPanel = new Panel(new GridBagLayout());
		marginBagPanel.add(mdsControlsPanel,c);
		marginBagPanel.setBackground(getBackground());

		c.insets = new Insets(1,0,1,1); // (top,left,bottom,right)
		Panel mdsBagPanel = new Panel(new GridBagLayout());
		mdsBagPanel.setBackground(Color.black);
		mdsBagPanel.add(marginBagPanel,c);

		overallMdsPanel.add(mdsBagPanel,BorderLayout.CENTER);

		Panel bottomControlsPanel = new Panel(new FlowLayout(FlowLayout.CENTER,10,1));
		bottomControlsPanel.add(treeScoreColoringCheckbox);
		Panel animationFlowPanel = new Panel(new FlowLayout(FlowLayout.CENTER,3,3));
		animationFlowPanel.add(animateButton);
		animationFlowPanel.add(new Label("rate:",Label.RIGHT));
		animationFlowPanel.add(animationRateField);
		animationFlowPanel.add(new Label("items/sec",Label.LEFT));
		bottomControlsPanel.add(animationFlowPanel);

		Panel overallControlsPanel = new Panel(new BorderLayout(0,0));
		overallControlsPanel = new Panel(new BorderLayout(0,0));
		overallControlsPanel.add(overallMdsPanel, BorderLayout.CENTER);
		overallControlsPanel.add(bottomControlsPanel, BorderLayout.SOUTH);

		/* for layout with the postscript button */

		mainPanel.add(embeddingDisplay, BorderLayout.CENTER);
		mainPanel.add(overallControlsPanel, BorderLayout.NORTH);

		Panel everythingPanel = new Panel(new BorderLayout(0,3));
		everythingPanel.add(mainPanel, BorderLayout.CENTER);
		everythingPanel.add(selectionManager, BorderLayout.SOUTH);

		Panel borderBagPanel = new Panel();
		borderBagPanel.setLayout(new GridBagLayout());
		// an insets object is (top,left,bottom,right)
		c.insets = new Insets(10, 10, 3, 10);
		borderBagPanel.add(everythingPanel, c);

		pointSetPanel = new Panel();
		pointSetPanel.setLayout(new GridLayout(1,1));
		pointSetPanel.add(borderBagPanel);

		addToWindow(pointSetPanel);

		resetTitle();
		setWindowSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		setLocation(20,50);
		toFront();
	}
	
	//WPM Oct05 snapshot items reordered (bugs were present because of order) and changed somewhat
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot windowSnapshot = super.getSnapshot(file);
		windowSnapshot.addLine("setAnimationRate " + animationRateField.getText());
		windowSnapshot.addLine("setTreeScoreColorer", treeScoresTask);
		windowSnapshot.addLine("setTreeScoreColoring " + MesquiteBoolean.toOffOnString(treeScoreColoringCheckbox.getState()));
		windowSnapshot.addLine("setStepSize " + stepSizeField.getText());
		windowSnapshot.addLine("setSampleSize " + sampleSizeField.getText());
		windowSnapshot.addLine("setSampling " + MesquiteBoolean.toOffOnString(sampleCheckbox.getState()));
		return windowSnapshot;
	}

	public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {
		/* Commands to set the state of MDS and the window.  To avoid duplication of code and of error checking,
		   these five commands are implemented by mimicking user input.  They manipulate user interface
		   elements and then fire off action events on those elements to induce the needed state changes
		   in the module. */
		Parser parser = new Parser();
		if (checker.compare(this.getClass(), "Starts or stops the MDS", "[start or stop]", commandName, "startStopButtonCommand")) {
			String argument = parser.getFirstToken(arguments);
			if (argument != null && argument.equalsIgnoreCase("start")) {
				// Start MDS
				synchronized (mdsThread) {
					// Set the mds thread to run at a lower priority than the user interface thread.
					if (mdsThread.getPriority() >= Thread.currentThread().getPriority()) {
						mdsThread.setPriority(Thread.currentThread().getPriority() - 1);
					}
					mdsThread.waitFlag = false;
					mdsThread.notify();
				}
				startStopButton.setActionCommand("stop");
				startStopButton.setLabel("Stop MDS");
			} else {
				// Stop MDS
				mdsThread.waitFlag = true;// signal the mds thread to wait() at its next oportunity
				startStopButton.setActionCommand("start");
				startStopButton.setLabel("Start MDS");
			}
			startStopButton.repaint();

		}
		else if (checker.compare(this.getClass(), "Scrambles points", null, commandName, "scrambleButtonCommand")) {
			mds.randomize_nodes();
			sharedPoints.setPoints(mds.getEmbedding());
			embeddingDisplay.repaint();

		}
		else if (checker.compare(this.getClass(), "sets the step size", "[step size]", commandName, "setStepSize")) {
			stepSizeField.setText(parser.getFirstToken(arguments));
			/* First, check to see if they entered a legal step size.
			   For now, that just means its a number. Any number. */
				String enteredText = stepSizeField.getText();
				float newValue;
				try {
					newValue = Float.parseFloat(enteredText);
				} catch (NumberFormatException nfe) {
					return null;
				}
				if (newValue < 0) {
					newValue = 0;
				}
				mds.setStepSize(newValue);
				stepSizeField.setText(Float.toString(newValue));
				stepSizeField.selectAll();

		}
		else if (checker.compare(this.getClass(), "Increments step size", null, commandName, "incStepSizeButtonCommand")) {
			float d = mds.getStepSize();
			d *= 1.1;

			stepSizeField.setText(Float.toString(d));
			stepSizeField.selectAll();
			mds.setStepSize(d);

		}
		else if (checker.compare(this.getClass(), "Decrements step size", null, commandName, "decStepSizeButtonCommand")) {
			float d = mds.getStepSize();
			d *= (1 / (1.1));
			stepSizeField.setText(Float.toString(d));
			stepSizeField.selectAll();
			mds.setStepSize(d);

		}
		else if (checker.compare(this.getClass(), "sets the sample size", "[sample size]", commandName, "setSampleSize")) {
			sampleSizeField.setText(parser.getFirstToken(arguments));

			
			// First, check to see if they entered a legal sample size.
			boolean legalEntry = true;
			int newValue = 0;
			try {
				newValue = Integer.parseInt(sampleSizeField.getText());
			} catch (NumberFormatException nfe) {
				legalEntry = false;
			}
			if (newValue < 1 || newValue > itemDiffMatrix.getNumberOfItems()) {
				legalEntry = false;
			}
			if (legalEntry) {
				sampleSize = newValue;
				sampleSizeField.setText(Integer.toString(newValue));
				itemDiffMatrix.sampleByPoint(newValue);
				// get sample set
				BitSet sampleSet = new BitSet(itemDiffMatrix.getNumberOfItems());
				for (int i = 0; i < itemDiffMatrix.getNumberOfItems(); ++i) {
					if (itemDiffMatrix.pointInSample(i)) {
						sampleSet.set(i);
					}
				}
				// send sample set to embedding display
				embeddingDisplay.setSample(sampleSet);
				embeddingDisplay.repaint();
				mds.setStepSize(STEPSIZE_DIVIDEND / newValue);
				stepSizeField.setText(Float.toString(STEPSIZE_DIVIDEND / newValue));
			} else {
				// Illegal input; reset the field to contain the old sample size
				sampleSizeField.setText(Integer.toString(sampleSize));
			}
			sampleSizeField.selectAll();

		}
		else if (checker.compare(this.getClass(), "turns sampling on or off", "[on; off]", commandName, "setSampling")) {
			sampleCheckbox.setState(MesquiteBoolean.fromOffOnString(parser.getFirstToken(arguments)));
			BitSet sampleSet = new BitSet(itemDiffMatrix.getNumberOfItems());
			if (sampleCheckbox.getState()) {// Box is checked
				itemDiffMatrix.sampleByPoint(sampleSize);
				sampleSizeField.setEnabled(true);
				sampleSizeField.setEditable(true);
				sampleSizeField.setText(Integer.toString(sampleSize));
				sampleSizeField.selectAll();
				for (int i = 0; i < itemDiffMatrix.getNumberOfItems(); ++i) {
					if (itemDiffMatrix.pointInSample(i)) {
						sampleSet.set(i);
					}
				}
				mds.setStepSize(STEPSIZE_DIVIDEND / sampleSize);
				stepSizeField.setText(Float.toString(STEPSIZE_DIVIDEND / sampleSize));
			} else { // Box is unchecked
				itemDiffMatrix.disableSampling();
				sampleSizeField.setEnabled(false);
				sampleSizeField.setEditable(false);
				//for (int i = 0; i < sampleSet.length(); ++i) {
				//	sampleSet.clear(i);
				//}
				mds.setStepSize(STEPSIZE_DIVIDEND / itemDiffMatrix.getNumberOfItems());
				stepSizeField.setText(Float.toString(STEPSIZE_DIVIDEND / itemDiffMatrix.getNumberOfItems()));
			}
			embeddingDisplay.setSample(sampleSet);
			embeddingDisplay.repaint();

		}
		else if (checker.compare(this.getClass(), "Start the animation of sequence", null, commandName, "animateButtonCommand")) {

			// In case an animation is already underway, kill it.
			animationTimer.cancel();
			animationTimer = new java.util.Timer();

			// Make a new task to run the animation
			animationTask = new java.util.TimerTask() {
				int runCount = 0;
				final int lastRun = itemDiffMatrix.getNumberOfItems();
				// The run method is called periodically by the timer thread.
				public void run() {
					if (runCount < lastRun) {
						// Make one more point visible and call for a re-draw.
						embeddingDisplay.setLastShown(runCount);
						embeddingDisplay.repaint();
						if (colorKey != null) {
							colorKey.setLastShown(runCount);
							colorKey.repaint();
						}
						runCount++;
					} else {
						// Remove the current task from execution ability by the timer.
						cancel();
						animationRateField.setEnabled(true);
						animationRateField.setEditable(true);
					}
				}
			};

			boolean illegalRate = false;
			int animationRate = DEFAULT_ANIMATION_RATE;
			try {
				animationRate = Integer.parseInt(animationRateField.getText());
			} catch (NumberFormatException nfe) {
				illegalRate = true;
			}
			if (illegalRate || animationRate < 1 || animationRate > 1000) {
				animationRate = DEFAULT_ANIMATION_RATE;
				animationRateField.setText(Integer.toString(DEFAULT_ANIMATION_RATE));
			}

			// Start the animation -- It will kill itself when it is finished.
			animationTimer.scheduleAtFixedRate(animationTask, 0, 1000 / animationRate);
			// The rate cannot be changed while animation is running.
			animationRateField.setEnabled(false);
			animationRateField.setEditable(false);
		
		}
		else if (checker.compare(this.getClass(), "Sets the colorer of tree scores", "[name of module]", commandName, "setTreeScoreColorer")) {
			NumberForTree temp = (NumberForTree)getOwnerModule().replaceEmployee(commandRec, NumberForTree.class, arguments, "Values by which to color trees", treeScoresTask);

			if (temp != null) {
				treeScoresTask = temp;
				treeScoresTaskName.setValue(treeScoresTask.getName());
				needToRecalculateTreeScores = true;
				treeScoreColoringCheckbox.setState(true);
				refreshTreeScores();
			}
		}
		else if (checker.compare(this.getClass(), "turns tree score coloring on or off", "[on; off]", commandName, "setTreeScoreColoring")) {
			treeScoreColoringCheckbox.setState(MesquiteBoolean.fromOffOnString(parser.getFirstToken(arguments)));
			if (treeScoreColoringCheckbox.getState()) {// Box is checked
				if (treeScoresTask == null){
					treeScoresTask = (NumberForTree)getOwnerModule().hireEmployee(CommandRecord.nonscriptingRecord, NumberForTree.class, "Values by which to color tree");

				}
				if (treeScoresTask == null){
					treeScoreColoringCheckbox.setState(false);
					return null;
				}
			}
			refreshTreeScores();
		}
		else if (checker.compare(this.getClass(), "sets the animation rate", "[animation rate]", commandName, "setAnimationRate")) {
			animationRateField.setText(parser.getFirstToken(arguments));
			/* take no action - we don't want to actually start an animation. */
			return null;
		} else {
			/* All other commands are passed to our superclass (MesquiteWindow) for handling. */
			return super.doCommand(commandName, arguments, commandRec, checker);
		}
		return null;
	}
	
	/*WPM Oct05 dangerous circumstance:  here and elsewhere itemDiffMatrix.getNumberOfItems() is used
	 * to determine the number of trees.  I put some of these in the code, but I modelled it on other instances
	 * in the code.  These bug me a bit: relying on another object fundamenatlly charged with another duty
	 * to be the source of this basic information about the number of trees.  It could get us into trouble some day.
	 * Better to have a reliable variable (perhaps a MesquiteInteger so it can be passed into methods and changed)
	 * dedicated to remembering the number of trees
	 */
	public void refreshTreeScores(){
		if (treeScoreColoringCheckbox.getState()) {// Box is checked
			if ((needToRecalculateTreeScores || treeScoreColors == null) && treeScoresTask != null) {// This is the first time coloring has been activated
					calculateTreeScores(itemDiffMatrix.getNumberOfItems(), CommandRecord.nonscriptingRecord);  
				if (treeScores != null) {
					if (colorKey != null)
						mainPanel.remove(colorKey);
					colorKey = new ColorKey(treeScores, colorGradient, selectionManager);
					selectionManager.setColorKey(colorKey);
				}
			}
		}
		// If score reading fails, the checkbox will be set back to false and treeScoreColors will
		// still be null, so the following lines won't have any effect.
		embeddingDisplay.setTreeScoreColoring(treeScoreColoringCheckbox.getState());
		embeddingDisplay.setTreeScoreColors(treeScoreColors);
		if (colorKey != null){
			if (treeScoresTask != null)
		
				colorKey.setTreeScorer(treeScoresTask.getName());
			else
				colorKey.setTreeScorer(null);
			colorKey.repaint();
		}
		embeddingDisplay.repaint();
		if (colorKey != null) {
			if (treeScoreColoringCheckbox.getState()) {
				mainPanel.add(colorKey, BorderLayout.EAST);
			} else {
				mainPanel.remove(colorKey);
			}
			//took out a call to old selectionChanged() method here; not sure what it was for
			organizeDisplay();
		}
		
	}
	public void resetNumberOfItems(int newNumberOfItems) {
		sampleSize = newNumberOfItems / 10 + 1; //new default sample size;
		sharedPoints.resetNumberOfPoints(newNumberOfItems);
		itemDiffMatrix.resetNumberOfItems(newNumberOfItems);
		mds.resetNumberOfItems(newNumberOfItems);
		mds.setStepSize(STEPSIZE_DIVIDEND / newNumberOfItems);
		sharedPoints.setPoints(mds.getEmbedding());
		selectionManager.resetNumberOfItems(newNumberOfItems, this);
		embeddingDisplay.resetNumberOfItems(newNumberOfItems);
		embeddingDisplay.repaint();
	}

	public Dimension getMinimumSize() { return new Dimension(100,100); }

	/**
	 * Called when the Points Set Module is cleaning up. It
	 * asks the MDS Thread to exit cleanly
	 */
	public void haltThreads() {
		mdsThread.exitFlag = true;
	}


	/**
	 * Computes the contents of the big difference matrix.
	 *
	 * @return  true if the calulation was completed, false if it is cancelled by the user before completion.
	 */
	public abstract boolean computeDM();

	protected abstract void newSelection(java.util.BitSet selection, String selectionName);

	protected abstract void updateSelection(java.util.BitSet selection, String selectionName);

	protected abstract void removeSelection(String selectionName);

	protected abstract void activateSelection(String selectionName);


	/** This is called when the window is resized by the user. */
	public void windowResized() {
		organizeDisplay();
	}

	/** This is called after the window is resized to layout the new window size and its components. */
	public void organizeDisplay() {
		pointSetPanel.setBounds(0, 0, getWidth(), getHeight());
		pointSetPanel.validate();
		pointSetPanel.repaint();
		// For layout debugging:
		// pointSetPanel.list();
	}

	/**
	 * This method set the stress size of the current embedding.
	 *
	 *@param  stress   The indicated size of the stress field.
	 */
	protected void setStress(float stress) {
		stressDisplayLabel.setText(Float.toString(stress));
		stressDisplayLabel.repaint();
	}


	/**
	 * Called every time the MDS Thread completes an iteration of MDS.
	 * Updates the stress display and tells the embedding display to repaint
	 * itself (by accessing the shared points to get the new embedding put
	 * there by MDS)
	 */
	protected void mds_iteration_complete() {
		setStress(sharedPoints.getStress());
		embeddingDisplay.repaint();
	}

	protected abstract void calculateTreeScores(int numberOfScores, CommandRecord commandRec);
	
	/* WPM Oct05 this method was modified extensively and moved to TreeSetViz.java as calculateTreeScores
	private void readOptimalityScores(int numberOfScores, CommandRecord commandRec) {
		treeScores = new double[numberOfScores];
		java.io.FileReader input;

		FileDialog scoresFileDialog = new FileDialog(new Frame());
		scoresFileDialog.setTitle("Which optimality scores do you want to use?\n " +
		                          "You need to pick a file formated like a MrBayes .p file, " +
								  "a tab-separated layout with one line of column headings and " +
								  "the optimality scores in the second column.  It must have at " +
								  " least as many entries as there are trees you want to color.");

								  // pi add blurb in here about PAUP files
								  // this is currently too long, does not fit in title

		scoresFileDialog.setMode(FileDialog.LOAD);
		scoresFileDialog.setVisible(true);
		//at this point, the file dialog becomes visible and the user chooses a file
		boolean userPushedCancel = (scoresFileDialog.getFile() == null);
		boolean inputSuccess = false;

		if (!userPushedCancel) {
			java.io.File scoresFile = new java.io.File(scoresFileDialog.getDirectory(), scoresFileDialog.getFile());
			try {
				//System.out.print("Opening " + scoresFile + "...");
				input = new java.io.FileReader(scoresFile);//open scores file
				//System.out.println("success!");
				char lastChar = 'x';
				final char END_OF_STREAM_MARKER = (char) -1;
				StringBuffer currentScore = new StringBuffer();
				//skip over first line (column headings)
				while (lastChar != '\n' && lastChar != '\r' && lastChar != END_OF_STREAM_MARKER) {
					lastChar = (char) input.read();
				}
				//System.out.println("Skipped column headings.");
				//Skip Second line if file is MrBayes V2
				while ((lastChar == '\n' || lastChar == '\r') && lastChar != END_OF_STREAM_MARKER)
					lastChar = (char) input.read();
				if( !((lastChar>='0')&&(lastChar<='9')) ) {
					while (lastChar != '\n' && lastChar != '\r' && lastChar != END_OF_STREAM_MARKER) {
						lastChar = (char) input.read();
					}
				}
				for (int i = 0; i < numberOfScores; ++i) {
					// skip over first column (tree name or tree index)
					while (lastChar != '\t' && lastChar != END_OF_STREAM_MARKER) {
						lastChar = (char) input.read();
					}
					lastChar = (char) input.read();
					//System.out.print("skiped first column...");
					// read in the score
					// pi added lastChar !='\n' condition to read PAUP files
					while (lastChar != '\t' && lastChar != END_OF_STREAM_MARKER && lastChar !='\n') {
						currentScore.append(lastChar);
						lastChar = (char) input.read();
					}
					//System.out.print("read score: " + currentScore + "...");
					// skip the rest of the line
					while (lastChar != '\n' && lastChar != '\r' && lastChar != END_OF_STREAM_MARKER) {
						lastChar = (char) input.read();
					}
					//System.out.println("skipped the rest of the line");
					treeScores[i] = Double.parseDouble(currentScore.toString());
					if (treeScores[i] < 0.0)
						treeScores[i] *= -1.0;
					currentScore.delete(0, currentScore.length());
				}
				input.close();
				inputSuccess = true;
			} catch (java.io.FileNotFoundException excep) {
				System.out.println("Couldn't open file with optimality scores: " + excep.getMessage());
			} catch (NumberFormatException excep) {
				System.out.println("Error parsing optimality value: " + excep.getMessage());
			} catch (java.io.IOException excep) {
				System.out.println("Error reading character of optimality scores file: " + excep.getMessage());
			}
		} //if user didn't press cancel

		if (inputSuccess) {
	
			// Translate tree scores into colors
			treeScoreColors = new Color[treeScores.length];

			// Find the maximum and minimum optimalities
			double max = treeScores[0];
			double min = treeScores[0];
			for (int i = 0; i < treeScores.length; ++i) {
				max = Math.max(treeScores[i], max);
				min = Math.min(treeScores[i], min);
			}

			// Transform range of scores to [0..1] and translate them to colors
			double transformedScore;
			for (int i = 0; i < treeScores.length; ++i) {
				transformedScore = treeScores[i] - min;//translate
				if (max > min) {
					transformedScore /= (max - min);//scale
				}
				treeScoreColors[i] = colorGradient.computeColor(transformedScore);
			}
		} else {
			treeScores = null;
			treeScoreColoringCheckbox.setState(false);
		}
	}//getTreeScores
*/
	public void saveAsPostscript() {
		FileDialog psFileDialog = new FileDialog(new Frame());
		psFileDialog.setTitle("Where do you want to save the postscript?");
		psFileDialog.setMode(FileDialog.SAVE);
		psFileDialog.setVisible(true);
		/* at this point, the file dialog becomes visible and the user chooses a file */

		boolean userPushedCancel = (psFileDialog.getFile() == null);
		if (!userPushedCancel) {
			try {
				/* Open the output file and tell the embedding display to draw into it */
				java.io.File postscriptFile = new java.io.File(psFileDialog.getDirectory(), psFileDialog.getFile());
				java.io.PrintWriter postscriptOutputStream = new java.io.PrintWriter(new java.io.FileWriter(postscriptFile));
				embeddingDisplay.drawInPostscript(postscriptOutputStream);
				postscriptOutputStream.close();
			} catch (java.io.IOException excep) {
				System.out.println("Trouble opening file to write postscript");
			}
		}
	}

	/* This routine uses the Java API to print the embedding.  We abandoned it
	   because the output was rasterized. */
	/*public void printAsPostscript() {
		java.awt.print.PrinterJob printJob = java.awt.print.PrinterJob.getPrinterJob();
		printJob.setPrintable(new java.awt.print.Printable() {
			public int print (Graphics g, java.awt.print.PageFormat pageFormat, int page) {
				embeddingDisplay.paint(g);
				if (page == 0) {
					return java.awt.print.Printable.PAGE_EXISTS;
				} else {
					return java.awt.print.Printable.NO_SUCH_PAGE;
				}
			}
		});
		java.awt.print.Paper tempPaper = new java.awt.print.Paper();
		java.awt.print.PageFormat pageFormat = new java.awt.print.PageFormat();
		pageFormat.setPaper(tempPaper);
		if (printJob.printDialog()) {
			try {
				printJob.print();
			} catch (java.awt.print.PrinterException excep) {
				System.out.println("Printer exception: " + excep.getMessage());
			}
		}
	} */
}


/**
 * A class to encapsulate the key to the tree score coloring feature.  It draws a gradient
 * color bar, indexed by scores so that the viewer can associate colors with specific scores.
 * Also, little arrows can be drawn pointing at a spot in the gradient to correspond to
 * selected items.
 *
 *@author     Jeff Klingner
 *@created    Spring 2002
 */
class ColorKey extends Panel {

	// Color constants
	private final static Color textColor = Color.black;
	private final static Color arrowColor = Color.black;

	// How many index labels to write along the length of the color bar
	private final static int numberOfLabels = 6;
	// Endpoints of the key's range
	private double max;
	private double min;

	// Defines the color range across the key
	private ColorGradient colorGradient;
	// values for each item index, used to determint where each arrow lies
	private double[] treeScores;
	// reference to the selection manager is used to find which points are in the active selection and what their icon is
	private SelectionManager selectionManager;
	// only arrows with indices <= lastShown will be drawn; -1 means draw them all
	private int lastShown;
	
	String treeScorerName = null;

	// A whole bunch of constant parameters to determine the appearnce of the key
	private static final int labelMargin = 5;// pixels right of the labels
	private static final double labelAngle = -(Math.PI / 2);// radians clockwise - negative sign makes it counter-clockwise
	private static final int barMargin = 10;// pixels on each side of the color bar
	private static final int arrowMargin = 5;// pixels beside the arrows' bases
	private static final int arrowWidth = 6;// pixels across the base of the arrows
	private static final int iconWidth = 10; //pixels of space given to selection set icons

	// These next three specify division of the space between the margins, thes should add up to one.
	private static final double arrowPortion = 0.3;
	private static final double barPortion = 0.3;
	private static final double labelPortion = 0.40;
	// The totol width of the color key in pixels
	private static final int colorKeyWidth = 90;

	public ColorKey(double[] treeScores, ColorGradient colorGradient, SelectionManager selectionManager) {
		super();// Panel's constructor
		this.treeScores = treeScores;
		this.selectionManager = selectionManager;
		max = treeScores[0];
		min = treeScores[0];
		for (int i=0; i < treeScores.length; ++i) {
			max = Math.max(treeScores[i], max);
			min = Math.min(treeScores[i], min);
		}
		this.colorGradient = colorGradient;
		lastShown = treeScores.length;
	}

	/** called by the animation thread to effect the sequential appearance of indicator arrows */
	public void setLastShown(int last) { lastShown = last; }
	
	/** called by the main window when the user activates/deactivates tree score coloring */
	protected void setTreeScorer(String name) { treeScorerName = name; }

	/** establishes a minimum width for proper window component layout */
	public Dimension getPreferredSize() {  return new Dimension(colorKeyWidth, 0); }

	/**WPM Oct05 a text rotator from the days before Java 2.  Used for name of tree score module*/
	TextRotator textRotator = new TextRotator();
	/**
	 * The main event: draws the color bar, the index labels, and the arrows
	 *
	 * @param  g  graphics context in which to do the drawing.  Must be of type Graphics2D.
	 */
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		int width = getBounds().width;
		int height = getBounds().height;
		int x;
		int y;

		// We have three things to draw: the labels, the color bar, and the arrows.

		// Start with the labels
		x = arrowMargin + (int) ((width - labelMargin - arrowMargin) * (arrowPortion + barPortion));
		String currentLabel;
		for (int i = 0; i < numberOfLabels; ++i) {
			y = (height - barMargin) - (int) ((height - 2 * barMargin) * ((float) i / (float) (numberOfLabels - 1)));
			currentLabel = Double.toString(min + (i * (max - min) / numberOfLabels));
			g2.drawString(currentLabel, x, y);
		}
		
		
		//Second, draw the color bar
		y = height - barMargin;
		// set x to the left edge of the bar
		x = arrowMargin + (int) ((width - labelMargin - arrowMargin) * (arrowPortion));
		
		
		//WPM Oct05 drawing the name of the tree scorer.  PERHAPS DRAW IN SOME OTHER WAY
		if (treeScorerName != null)
			textRotator.drawRotatedText(treeScorerName, g2, this, x-12, height/2);

		while (y >= barMargin) {
			g2.setColor(colorGradient.computeColor((double) (height - barMargin - y) / (double) (height - 2 * barMargin)));
			g2.drawLine(x, y, x + (int) ((width - labelMargin - arrowMargin) * (barPortion)), y);
			--y;
		}

		//Finally, draw the arrows
		// Define the arrow shape
		Polygon arrow = new Polygon();
		arrow.addPoint(0, 0);// apex
		arrow.addPoint((int) -(((width - labelMargin - arrowMargin) * (arrowPortion))-iconWidth), arrowWidth / 2);// upper base
		arrow.addPoint((int) -(((width - labelMargin - arrowMargin) * (arrowPortion))-iconWidth), -arrowWidth / 2);// lower base
		g2.setPaint(arrowColor);
		//g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		//g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		//set x to the left edge of the bar.
		x = arrowMargin + (int) ((width - labelMargin - arrowMargin) * (arrowPortion));
		double arrowTotal = 0.0;
		int numberOfArrowsDrawn = 0;
		MultiSelections selections = selectionManager.getSelections();
		BitSet arrowSet = selections.getSelection(selections.getActiveSelection());
		int iconNumber = selections.getIconNumber(selections.getActiveSelection());
		for (int i = 0; i <= lastShown; ++i) {
			if (arrowSet.get(i)) {
				y = (height - barMargin) - (int) ((height - 2 * barMargin) * (treeScores[i] - min) / (max - min));
				arrow.translate(x, y);
				g2.fill(arrow);
				PointIcons.draw(iconNumber,g2,new Point(arrowMargin + iconWidth/2,y));
				arrow.translate(-x, -y);
				numberOfArrowsDrawn++;
				arrowTotal += treeScores[i];
			}
		}
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		//g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);


		// If any arrows were drawn, draw their average line
		if (numberOfArrowsDrawn > 0) {
			double arrowAverage = arrowTotal / numberOfArrowsDrawn;
			g2.setColor(Color.black);
			y = (height - barMargin) - (int) ((height - 2 * barMargin) * (arrowAverage - min) / (max - min));
			x = arrowMargin + (int) ((width - labelMargin - arrowMargin) * (arrowPortion));
			g2.drawLine(x, y, x + (int) ((width - labelMargin - arrowMargin) * (barPortion)), y);
		}
	}
}

/**
 * A class to encapsulate the details of computing the color at a given position on
 * a color gradient
 *
 * @author     Jeff Klingner
 * @created    Spring 2002
 */
class ColorGradient {
	private Color[] checkpoints;

	public ColorGradient(Color[] checkpoints) {
		this.checkpoints = new Color[checkpoints.length];
		System.arraycopy(checkpoints, 0, this.checkpoints, 0, checkpoints.length);
	}

	/**
	 * The range is indexed by floats in the range 0.0 - 1.0 inclusive.  Pass in a value
	 * in that range to get the corresponding color.  Anything else will get you black.  The
	 * color passed back is constructed by this method.
	 */
	public Color computeColor(double value) {
		Color c;
		if (value >= 0.0 && value <= 1.0 && !Double.isNaN(value) && !Double.isInfinite(value)) {
			int precedingCheckpoint;
			int followingCheckpoint;
			double colorIndex;
			int red;
			int green;
			int blue;
			precedingCheckpoint = (int) Math.round(Math.floor(value * (checkpoints.length - 1)));
			followingCheckpoint = precedingCheckpoint + 1;
			if (followingCheckpoint >= checkpoints.length) {// only occurs for 1.0
				precedingCheckpoint--;
				followingCheckpoint--;
			}
			colorIndex = (value * (checkpoints.length - 1) - (double) precedingCheckpoint);
			//System.out.println("value = " + value);
			//System.out.println("preceding checkpoint: " + precedingCheckpoint + "   following = " + followingCheckpoint + "     color index = " + colorIndex);
			red = (int) Math.round((1 - colorIndex) * checkpoints[precedingCheckpoint].getRed() + colorIndex * checkpoints[followingCheckpoint].getRed());
			green = (int) Math.round((1 - colorIndex) * checkpoints[precedingCheckpoint].getGreen() + colorIndex * checkpoints[followingCheckpoint].getGreen());
			blue = (int) Math.round((1 - colorIndex) * checkpoints[precedingCheckpoint].getBlue() + colorIndex * checkpoints[followingCheckpoint].getBlue());
			//System.out.println("red = " + red + ", green = " + green + ", blue = " + blue);
			c = new Color(red, green, blue);
		} else {
			c = new Color(0, 0, 0);
		}
		return c;
	}
}


/**
 * Panel in which the point embedding is displayed
 */
class EmbeddingDisplayPanel extends Panel {
	// Some constants that define the behavior of the embedding panel's user interface
	private final static Color embeddingBackGroundColor = Color.black;
	private final static Color embeddingUnselectedPointColor = Color.white;
	private final static Color embeddingSelectedPointColor = Color.red;
	private final static Color embeddingSelectionBoxColor = Color.white;
	private final static Color embeddingSampledPointColor = Color.blue;

	// Bigger sensitivity means you can clicker farther from a point and still select it.
	private final static int singleClickSensitivity = 4;

	/** Needed for double-buffering (see paint() method) */
	private Image im;
	private Graphics2D buf;

	/** The local, transformed for display, embedding */
	private Point[] localPoints;
	/** number of dimensions in the embedding */
	private int dimensions;
	/** the rectangle that is dragged around points to select them */
	private Box selectionBox;
	/** So that this panel can pass along selection events */
	private SelectionManager selectionManager;
	/** a convenience member used for resetting of selectionSet */
	private BitSet emptySet;
	/** An all-true bitset used for drawing of all points in spite of selection */
	private BitSet fullSet;
	/** The points selected by the user in a selection event; passed to the selection manager */
	private BitSet selectionSet;
	/** a temporary bit vector to keep track of which points aren't in any selections; they are drawn plainly */
	private BitSet unselectedPoints;
	/** representation of which points should be drawn differently because they are in the MDS sample */
	private BitSet sampleSet;
	/** shared data with the mds thread */
	private SharedPoints sharedPoints;
	/** local copy of the current embedding (as set by MDS)  */
	private float[][] pointsFromMDS;
	/** Flag for activation of point coloring by tree scores */
	private boolean treeScoreColoring;
	private Color[] treeScoreColors;

	/** Only points with indices 0-lastShown are drawn.
	 *  set by animation thread to cause the sequetial appearance of points */
	private int lastShown;

	public EmbeddingDisplayPanel(SelectionManager selectionManager, SharedPoints sharedPoints) {
		super();
		this.selectionManager = selectionManager;
		this.sharedPoints = sharedPoints;
		dimensions = 2;
		localPoints = new Point[sharedPoints.getPoints().length];
		for (int i = 0; i < localPoints.length; i++) {
			localPoints[i] = new Point();
		}
		selectionBox = new Box();
		selectionSet = new BitSet(localPoints.length);
		unselectedPoints = new BitSet(localPoints.length);
		emptySet = new BitSet(localPoints.length);
		sampleSet = new BitSet(localPoints.length); // initially empty
		fullSet = new BitSet(localPoints.length);
		for (int i = 0; i < localPoints.length; i++) {
			fullSet.set(i);
		}
		treeScoreColoring = false;
		treeScoreColors = null;

		lastShown = localPoints.length - 1;


		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				// Record the press location, in case this is the start of a drag selection
				// This point is also used as the location of a single click if the mouse button
				//   come up without any dragging.
				selectionBox.anchor = e.getPoint();
			}

			public void mouseClicked(MouseEvent e) {
				// Select closest point.  If no points are nearby, select nothing.
				// If the Ctrl key was also pressed, the selection is cumulative
				selectFromPoint(selectionBox.anchor, e.getModifiers());
			}

			public void mouseReleased(MouseEvent e) {
				// If this was a dragging event, turn off the dragging box and select every point that was in it.
				// If the Ctrl key was also pressed, the selection is cumulative
				// The conditional here is false if the relase event is from a click rather than a drag.
				if (selectionBox.enabled) {
					selectFromBox(selectionBox, e.getModifiers());
				}
				selectionBox.enabled = false;
			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				// Enter (or remain in) the box selection state.  Update the dragging box
				selectionBox.enabled = true;
				selectionBox.floater = e.getPoint();
				repaint();// because the selection box has changed
			}
		});
	}

	public void resetNumberOfItems(int newNumberOfItems) {
		localPoints = new Point[newNumberOfItems];
		for (int i = 0; i < localPoints.length; i++) {
			localPoints[i] = new Point();
		}
		selectionSet = new BitSet(newNumberOfItems);
		unselectedPoints = new BitSet(newNumberOfItems);
		emptySet = new BitSet(newNumberOfItems);
		sampleSet = new BitSet(newNumberOfItems); // initially empty
		fullSet = new BitSet(newNumberOfItems);
		for (int i = 0; i < newNumberOfItems; i++) {
			fullSet.set(i);
		}
		lastShown = newNumberOfItems - 1;
	}


	/**
	 * Override Panel's setBounds so that I can replace the internal drawing buffer
	 * with one of the appropriate dimensions whenever the embedding panel is
	 * re-sized.
	 */
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		im = null;  // setting these to null signals that they need to be reconstructed
		buf = null; // to account for the new panel size
	}

	/** This method draws the embedding. Colors are defined by static class variables */
	public void drawEmbedding(Graphics2D g2) {
		// I draw my own background
		g2.setPaint(embeddingBackGroundColor);
		g2.fillRect(0, 0, getBounds().width, getBounds().height);

		// Eventually, I will put the transformation from the point set provided by MDS to
		// the point set of the 2D display, possibly incorporating a viewing angle if the
		// visualization is in 3D.  For now, only 2D Display works.

		if (dimensions != 2) {
			g2.setColor(embeddingUnselectedPointColor);
			g2.drawString("Sorry, only 2D drawing is supported for now.", 10, 10);
		} else {
			// Draw the points
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			MultiSelections selections = selectionManager.getSelections();

			// First, draw all of the selected points, and keep track of which ones never show up
			unselectedPoints.or(fullSet); // at first, no points are known to be selected
			for (int i = 0; i < selections.getNumberOfSelections(); ++i) { // for each selection
				drawPointSet(selections.getSelection(i),selections.getIconNumber(i),embeddingSelectedPointColor,g2);
				unselectedPoints.andNot(selections.getSelection(i));
			}
			// Next draw all points in the unselected Color
			drawPointSet(unselectedPoints,PointIcons.DOT,embeddingUnselectedPointColor,g2);
			// Finally, draw the sample highlights
			drawPointSet(sampleSet,PointIcons.SAMPLE,embeddingSampledPointColor,g2);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

			// Next, draw the dragging box, if it exists
			if (selectionBox.enabled) {
				g2.setColor(embeddingSelectionBoxColor);
				g2.drawLine(selectionBox.anchor.x, selectionBox.anchor.y, selectionBox.anchor.x, selectionBox.floater.y);
				g2.drawLine(selectionBox.anchor.x, selectionBox.anchor.y, selectionBox.floater.x, selectionBox.anchor.y);
				g2.drawLine(selectionBox.anchor.x, selectionBox.floater.y, selectionBox.floater.x, selectionBox.floater.y);
				g2.drawLine(selectionBox.floater.x, selectionBox.anchor.y, selectionBox.floater.x, selectionBox.floater.y);
			}
		}
	}

	private final void drawPointSet(BitSet b, int iconNumber, Color c, Graphics g) {
		g.setColor(c);
		for (int i=0; i <= lastShown; ++i) {
			if (b.get(i)) {
				if (treeScoreColoring && treeScoreColors != null &&  i< treeScoreColors.length) {
					g.setColor(treeScoreColors[i]);
				}
				PointIcons.draw(iconNumber,g,localPoints[i]);
			}
		}
	}

	public void drawInPostscript(java.io.PrintWriter psOutput) {
		/* the whole page is 612x792 points (72 points per inch).  The bounding box used
		   will be a square 7.5 inches wide, centered on both axes of the page */
		/* origin of postscript coordinate system is the lower left corner */
		Point lowerLeft  = new Point(36,126);  // 0.5 in from right edge, 1.75 in from botton
		Point upperRight = new Point(576,666); // 0.5 in from left edge,  1.75 in from top
		int pointRadius = 2;

		/* Print the header of the postscript file, as given by Dr. Amenta */
		psOutput.println("%!PS-Adobe-2.0 EPSF-1.2");
		psOutput.println("%%BoundingBox: " + lowerLeft.x + " " + lowerLeft.y + " " + upperRight.x + " " + upperRight.y);
		psOutput.println("/inch {72 mul} def");
		psOutput.println("0.008 inch 0.008 inch scale");
		psOutput.println();
		psOutput.println("1 setlinewidth");
		psOutput.println("1 setlinecap");
		psOutput.println("1 setlinejoin");
		psOutput.println();

		Color currentColor;
		for (int i=0; i<localPoints.length; ++i) {
			/* choose the color */
			if (treeScoreColoring && treeScoreColors != null) {
				currentColor = treeScoreColors[i];
			} else {
				currentColor = Color.black;
			}
			psOutput.println( currentColor.getRed()   / 255.0f + " " +
			                  currentColor.getGreen() / 255.0f + " " +
							  currentColor.getBlue()  / 255.0f + " setrgbcolor");

			/* draw the point */
			psOutput.println("newpath");
			psOutput.println((localPoints[i].x + lowerLeft.x)  + " " + (localPoints[i].y + lowerLeft.y) +
			                  " " + pointRadius + " 0 360 arc");
			psOutput.println("closepath ");
			psOutput.println("fill");
			psOutput.println();
		}

		/* print the footer of the file */
		psOutput.println("showpage");
	}


	/** called when the embedding display needs to be repainted; implements double-buffering. */
	public void paint(Graphics g) {
		//System.out.println("Display thread did a draw.");
		// Access the shared data area to get the latest embedding written by the mds thread
		updatePoints();

		if (im == null) { // true on first run or after a resize
			// Create a buffer for drawing to do double buffering and avoid flicker
			im = createImage(getSize().width, getSize().height);
			buf = (Graphics2D) im.getGraphics();
		}
		// Draw the panel in the internal buffer
		drawEmbedding(buf);
		// Flash it to the screen all at once
		g.drawImage(im, 0, 0, this);
	}


	/**
	 * Trival overriding of Component's update() to eliminate the unnecessary
	 * background wipe and reduce flicker
	 */
	public void update(Graphics g) { paint(g); }

	/** called by the main window when the sampling set is changed */
	protected void setSample(BitSet sample) { this.sampleSet = sample; }


	/** called by the main window when the user activates/deactivates tree score coloring */
	protected void setTreeScoreColoring(boolean b) { treeScoreColoring = b; }

	/** called the first time tree score coloring is enabled to pass a reference to the color vector */
	protected void setTreeScoreColors(Color[] c) { treeScoreColors = c; }

	/**
	 *  This is the accessor used to implement animation.  It is called by the animation thread.
	 *  Synchronization is not necessary because an out-of-sync-by-one value for lastShown
	 *  does not cause an error.
	 */
	protected void setLastShown(int last) { lastShown = last; }

	/**
	 * Called when the user clicks in the picture without dragging. Selects the
	 * nearest point, or no no points if none are near the click.
	 * selectinBox.anchor was set on the mousePressedEvent.
	 *
	 * @param  p             click location
	 * @param  modifierKeys  were Control or Shift (or both) pressed?
	 */
	private void selectFromPoint(Point p, int modifierKeys) {
		int closestPoint = -1;// Dummy value, means no nearby point has been found so far
		double closestDistance = 0;
		double currentDistance;
		for (int i = 0; i < localPoints.length; i++) {
			// If the point is within n pixels of the click, check the distance
			if ((Math.abs(p.x - localPoints[i].x) < singleClickSensitivity)
				 && (Math.abs(p.y - localPoints[i].y) < singleClickSensitivity)) {
				currentDistance = Math.sqrt((p.x - localPoints[i].x) * (p.x - localPoints[i].x)
					 + (p.y - localPoints[i].y) * (p.y - localPoints[i].y));
				if (closestPoint == -1 || currentDistance <= closestDistance) {
					closestPoint = i;
					closestDistance = currentDistance;
				}
			}
		}
		// initialize selection set by setting all bits to false
		selectionSet.and(emptySet);
		if (closestPoint != -1) { // click was close to at least one point
			selectionSet.set(closestPoint);
		} // otherwise, no points at all are in this selection set
		// Pass the selection event to the selection manager
		selectionManager.selectionEvent(selectionSet, modifierKeys);
		repaint();
	}


	/**
	 * Called after a selection box has been dragged. All points inside the box
	 * (edges inclusive) are selected.
	 *
	 * @param  b   the box made by the user by clicking and dragging
	 */
	private void selectFromBox(Box b, int modifierKeys) {
		// I make a rectangle out of the box here so I can utilize the convenient Rectangle.contains method.
		Rectangle rec = new Rectangle( Math.min(b.anchor.x, b.floater.x), Math.min(b.anchor.y, b.floater.y),
		                               Math.abs(b.anchor.x - b.floater.x), Math.abs(b.anchor.y - b.floater.y) );
		// initialize the selection set by clearing all of its bits
		selectionSet.and(emptySet);
		for (int i = 0; i < localPoints.length; i++) {
			// All points within the rectangle are selected
			if (rec.contains(localPoints[i])) {
				selectionSet.set(i);
			}
		}
		// Pass the selection event to the selection manager
		selectionManager.selectionEvent(selectionSet, modifierKeys);
		repaint(); // because the selection box needs to vanish
	}

	/**
	 * Description of the Method
	 */
	private void updatePoints() {
		// Access the shared data area for the points written by the MDS thread
		pointsFromMDS = sharedPoints.getPoints();

		float min_x = pointsFromMDS[0][0];
		float min_y = pointsFromMDS[0][1];
		float max_x = pointsFromMDS[0][0];
		float max_y = pointsFromMDS[0][1];
		// First, pass over the points once to find the minimums and maximums for each dimension.
		for (int i = 0; i < pointsFromMDS.length; i++) {
			if (pointsFromMDS[i][0] < min_x) {
				min_x = pointsFromMDS[i][0];
			}
			if (pointsFromMDS[i][1] < min_y) {
				min_y = pointsFromMDS[i][1];
			}
			if (pointsFromMDS[i][0] > max_x) {
				max_x = pointsFromMDS[i][0];
			}
			if (pointsFromMDS[i][1] > max_y) {
				max_y = pointsFromMDS[i][1];
			}
		}
		// Now to find the embeddings, translate the points so the minimum is at zero, scale them
		// so that the maximum is at the display width (minus margins), and round them to integers
		// To avoid aspect distortion, the smaller of the two scale factors is used for both axes.
		// Points are centered on the larger axis.
		int margin = 5;
		int embeddingWidth = getWidth() - (2 * margin);
		int embeddingHeight = getHeight() - (2 * margin);
		float scale_x = ((float) embeddingWidth) / (max_x - min_x);
		float scale_y = ((float) embeddingHeight) / (max_y - min_y);
		float scale;
		float x_offset;
		float y_offset;
		if (scale_x < scale_y) {
			scale = scale_x;
			x_offset = 0;
			y_offset = (embeddingHeight - (max_y - min_y) * scale_x) / 2;
		} else {
			scale = scale_y;
			x_offset = (embeddingWidth - (max_x - min_x) * scale_y) / 2;
			y_offset = 0;
		}
		for (int i = 0; i < pointsFromMDS.length; i++) {
			localPoints[i].setLocation( (margin + Math.round((float) ((pointsFromMDS[i][0] - min_x) * scale) + x_offset)),
				                        (margin + Math.round((float) ((pointsFromMDS[i][1] - min_y) * scale) + y_offset)) );
		}
	}


	/** A small class (like a struct, really) used internally to encapsulate details of the point-selection box */
	private class Box {
		/** true during dragging */
		boolean enabled;
		/** origin of the box (the location of the mousePressed event) */
		Point anchor;
		/** corner opposite the orgin (locatin of the pointer during dragging */
		Point floater;
	}
}

class SelectionManager extends Panel {
	private MultiSelections ms;
	private EmbeddingDisplayPanel embeddingDisplay;
	private ColorKey colorKey;
	private Component selectionDisplay;
	private ScrollPane selectionPane;
	private static final int selectionBoxWidth = 130;
	private static final int selectionBoxHeight = 15;


	public SelectionManager(int numberOfPoints, PointSetVisualization mainWindow) {
		ms = new MultiSelections(numberOfPoints,mainWindow);

		selectionDisplay = new Component() {
			public void paint(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				for (int i=0; i<ms.getNumberOfSelections(); ++i) {
					drawSelection(i==ms.getActiveSelection(),ms.getIconNumber(i),ms.getUserNumber(i),
								  ms.getSelectionSize(i),1+(i%2)*(selectionBoxWidth+2),1+(i/2)*(selectionBoxHeight+2),g2);
				}
			}
			public Dimension getPreferredSize() {
				return new Dimension(4+2*selectionBoxWidth, 1+(selectionBoxHeight+2)*((ms.getNumberOfSelections()+1)/2));
			}
		};
/*		selectionDisplay.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				System.out.println("Click at " + e.getPoint());
			}
			public void mousePressed(MouseEvent e) {
				System.out.println("Press at " + e.getPoint());
			}
			public void mouseReleased(MouseEvent e) {
				System.out.println("Release at " + e.getPoint());
			}
		});  */
		selectionDisplay.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {

				//Check if the click happened in a selection box
				Rectangle r = new Rectangle();
				r.width = selectionBoxWidth;
				r.height = selectionBoxHeight;
				for(int i=0; i<ms.getNumberOfSelections(); ++i) {
					r.x = 1 + (i%2) * (selectionBoxWidth+2);
					r.y = 1 + (i/2) * (selectionBoxHeight+2);
					if (r.contains(e.getPoint())) {
						ms.setActiveSelection(i);
						selectionDisplay.repaint();
						if (colorKey != null) {
							colorKey.repaint();
						}
						return;
					}
				}
			}
		});


		selectionPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED) {
			public Dimension getPreferredSize() {
				Dimension d = selectionDisplay.getPreferredSize();
				d.width += getVScrollbarWidth();
				d.height = 1 + (selectionBoxHeight+2) * 3;
				return new Dimension(d.width,d.height);
			}
		};

		Button addSelectionButton = new Button("New Selection");
		addSelectionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ms.addSelection();
				selectionDisplay.invalidate(); // Preferred size may have changed
				selectionDisplay.repaint();
				selectionPane.validate(); // create scrollbars if needed
				selectionPane.repaint();
				if (colorKey != null) {
					colorKey.repaint();
				}
			}
		});
		Button removeSelectionButton = new Button("Remove Selection");
		removeSelectionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (ms.getNumberOfSelections() > 1) {
					ms.removeSelection();
					selectionDisplay.invalidate(); // size may be different
					selectionDisplay.repaint();
					selectionPane.validate(); // create scrollbars if needed
					selectionPane.repaint();
				} else if (ms.getNumberOfSelections() == 1) {
					// remove all points from the first selection by removing it and adding a new one.
					ms.removeSelection();
					// at this point, MS is in an inconsistant state
					ms.addSelection();
					// sanity is restored
				}
				selectionDisplay.repaint();
				embeddingDisplay.repaint();
				if (colorKey != null) {
					colorKey.repaint(); //because the active selection may have changed.
				}
			}
		});

		selectionPane.getVAdjustable().setUnitIncrement(selectionBoxHeight+2);
		selectionPane.add(selectionDisplay);

		Panel buttonPanel = new Panel(new GridLayout(2,1));
		buttonPanel.add(addSelectionButton);
		buttonPanel.add(removeSelectionButton);
		Panel fullPanel = new Panel(new FlowLayout(FlowLayout.CENTER,5,0));
		fullPanel.add(buttonPanel);
		fullPanel.add(selectionPane);

		setLayout(new GridLayout(1,1));
		add(fullPanel);
	}

	public void simulateRemoveButton(){
	      	if (ms.getNumberOfSelections() > 1) {
			ms.removeSelection();
			selectionDisplay.invalidate(); // size may be different
			selectionDisplay.repaint();
			selectionPane.validate(); // create scrollbars if needed
			selectionPane.repaint();
		} else if (ms.getNumberOfSelections() == 1) {
			// remove all points from the first selection by removing it and adding a new one.
			ms.removeSelection();
			// at this point, MS is in an inconsistant state
			ms.addSelection();
			// sanity is restored
		}
		selectionDisplay.repaint();
		embeddingDisplay.repaint();
		if (colorKey != null) {
			colorKey.repaint(); //because the active selection may have changed.
		}
	}//simulateRemoveButton


	public void resetNumberOfItems(int newNumberOfItems, PointSetVisualization mainWindow) {
		for (int i=0; i< ms.getNumberOfSelections(); ++i) {
			ms.removeSelection();
		}
		ms = new MultiSelections(newNumberOfItems, mainWindow);
	}

	public void setEmbeddingDisplay(EmbeddingDisplayPanel ed) { this.embeddingDisplay = ed; }
	public void setColorKey(ColorKey ck) { this.colorKey = ck; }
	public MultiSelections getSelections() { return ms; }

	public void selectionEvent(BitSet selectionSet, int modifierKeys) {
		boolean withCtrl = (modifierKeys & MouseEvent.CTRL_MASK) != 0;
		boolean withShift = (modifierKeys & MouseEvent.SHIFT_MASK) != 0;
		// control takes precedence over shift
		if (withCtrl) { // add to the selection
			selectionSet.or(ms.getSelection(ms.getActiveSelection()));
		} else if (withShift) { //toggle selection
			selectionSet.xor(ms.getSelection(ms.getActiveSelection()));
		}
	      if (!selectionSet.equals(ms.getSelection(ms.getActiveSelection()))) { //the selection has changed
			//update selection data structure
			ms.setSelection(ms.getActiveSelection(),selectionSet);
			// repaint self (probably a new number of points in the active selection)
			selectionDisplay.repaint();
			// induce repaint on the embedding display because selection has changed
			embeddingDisplay.repaint();
			// likewise for the colorKey, but only if it exists
			if (colorKey != null) {
				colorKey.repaint();
			}
		}
	}//selectionEvent

	private void drawSelection(boolean active, int iconNumber, int userNumber, int numberOfPoints, int x, int y, Graphics2D g2) {
		g2.setColor(Color.white);
		g2.fillRect(x+1,y+1,selectionBoxWidth-1,selectionBoxHeight-1);
		g2.setColor(Color.black);
		g2.drawRect(x,y,selectionBoxWidth,selectionBoxHeight);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		PointIcons.draw(iconNumber,g2,new Point(x+9,y+selectionBoxHeight/2));
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		String selectionString;
		if (numberOfPoints == 1) {
			selectionString = "Selection " + userNumber + ": 1 point";
		} else {
			selectionString = "Selection " + userNumber + ": " + numberOfPoints + " points";
		}
		g2.drawString(selectionString, x+19, y+selectionBoxHeight/2+4);
		if (active) {
			g2.setXORMode(Color.white);
			g2.fillRect(x+1,y+1,selectionBoxWidth-1,selectionBoxHeight-1);
			g2.setPaintMode();
		}
	}
}

/** An encapsulation of the multiple selections of points that come and go as
 *  The user explores the tree set.  This is mostly a convenience class to keep
 *  everything together.  It does not watch its state carefully, and because it
 *  exports direct references to its data structures, it is possible for a caller
 *  to trash it.  As all callers are in the TreeSetModule package, this should
 *  be OK.  */
class MultiSelections {
	/** A reference to the main program window, used so that the creation, updating,
	 *  and removal of selections can be signalled and action can be taken (like
	 *  creating a secondary window with a consensus tree in it.) */
	private PointSetVisualization mainWindow;
	/** The number of items being visualized.  Determines the length of the bit vectors */
	private int numberOfItems;
	/** The active selection is the one that is modified by mouse events in the point display. */
	private int activeSelection;
	/** dynamically size data structure to hold all of the selection bit vectors (and associated data) */
	private ArrayList selections;

	/** A small inner class to encompass all the details of a selection.  Used like a C++ struct */
	private class Selection {
		BitSet bitSet;
		int size;
		int userNumber;
		int iconNumber;
		boolean unused;
		public Selection(BitSet b, int s, int u, int i) {
			this.bitSet=b; this.size=s; this.userNumber=u; this.iconNumber=i; this.unused = true;
		}
	}

	/** Simple constructor that initializes the data structures and creates the default
	 *  initial selection.  This selection is called "Selection 1", has icon 0 (a plus sign),
	 *  and initially contains no points. */
	public MultiSelections(int numberOfItems, PointSetVisualization mainWindow) {
		this.numberOfItems = numberOfItems;
		this.mainWindow = mainWindow;
		selections = new ArrayList(8);
		selections.add(new Selection(new BitSet(numberOfItems), 0, 1, 0));
		activeSelection = 0;
	}

	/* Here are a bunch of very simple wrapper-style accessor methods.  This is what I meant
	 * by saying that the data structures are not well protected.  These methods instead serve
	 * to hide the internal organization of the selections and to spare the caller the hassle
	 * (and syntactic mess) of casting */
	public final int getNumberOfSelections() { return selections.size(); }
	public final int getSelectionSize(int n) { return ((Selection)selections.get(n)).size; }
	public final BitSet getSelection(int n) { return ((Selection)selections.get(n)).bitSet; }
	public final int getUserNumber(int n) { return ((Selection)selections.get(n)).userNumber; }
	public final int getActiveSelection() { return activeSelection; }
	public final int getIconNumber(int n) { return ((Selection)selections.get(n)).iconNumber; }
	private final boolean getUnused(int n) { return ((Selection)selections.get(n)).unused; }
	private final void markUsed(int n) { ((Selection)selections.get(n)).unused = false; }

	public final void setActiveSelection(int n) {
		activeSelection = n;
		mainWindow.activateSelection("Selection " + getUserNumber(n));
		mainWindow.toFront();
	}

	/** This method is called to modify an existing selection.  That selection is updated internally
	 *  and the call to the main window is made in order to induce the creation of tree windows. */
	public final void setSelection(int n, BitSet s) {
		if (getUnused(n)) { // this is a virgin selection, so we will make a tree window for it
			mainWindow.newSelection((BitSet) s.clone(),"Selection " + getUserNumber(n));
			markUsed(n);
		} else { // a tree window already exists; change it
			mainWindow.updateSelection((BitSet) s.clone(),"Selection " + getUserNumber(n));
		}
		((Selection)selections.get(n)).bitSet = (BitSet) s.clone();
		((Selection)selections.get(n)).size = countBits(s);
	}

	/** Creates a new empty selection.  It picks an unused point icon if one exists.
	 *  (This method depends on the fact that there are 6 icons that are worth recycling.
	 *  (recycling = reused after being part of a selection that was deleted)  If that
	 *  changes, you'll have to fix it. */
	public void addSelection() {
		// Pick an icon number -- the lowest not already in use
		int newUserNumber;
		if (getNumberOfSelections() > 0) {
			newUserNumber = getUserNumber(getNumberOfSelections()-1) + 1;
		} else {
			newUserNumber = 1;
		}
		int newIconNumber = -1; //-1 is a dummy value that indicates unassigned
		boolean taken;
		for (int i=0; i<6; ++i) { // for each icon from smallest to largest
			taken = false;
			for (int j=0; j<getNumberOfSelections(); ++j) { //check all selections to see if it is in use
				if (getIconNumber(j) == i) {
					taken = true;
					break; //don't check any more selections
				}
			}
			if (!taken) {
				newIconNumber = i;
				break; // don't check any more icon numbers
			}
		}
		if (newIconNumber == -1) { // all icons taken; use a digit instead
			newIconNumber = newUserNumber;
		}
		selections.add(new Selection(new BitSet(numberOfItems), 0, newUserNumber, newIconNumber));
		activeSelection = getNumberOfSelections() - 1;
	}

	/** Removes a selection permanently.  If it used an interesting icon (numbers 0-5) that icon
	 *  may be recycled in the future, but for a selection with a different user number. Together
	 *  with addSelection and setSelection, this method uses the three abstract methods in PSV to
	 *  effect the module-level response to selection changes.  In the case of tree set viz, that
	 *  means making a window to display the consensus tree of the trees in the selection. */
	public void removeSelection() {
		if (!getUnused(activeSelection)) { // a tree window was made, get rid of it
			mainWindow.removeSelection("Selection " + getUserNumber(activeSelection));
		}
		selections.remove(activeSelection);
		if (activeSelection >= getNumberOfSelections() ) {
			activeSelection--;

		}

	}


	public void removeAllSelections() {
	    	for(int i=0; i<=getNumberOfSelections()-1; i++){
			removeSelection();
	    	}
	}


	/** Counts the number of bits that are true in the BitSet b.  Used internally to determine the
	 *  size of changed selection sets */
	private static int countBits(BitSet b) {
		int count = 0;
		for (int i=0; i<b.length(); ++i ) {
			if (b.get(i)) {
				++count;
			}
		}
		return count;
	}
}

/** This is a class that draws the little shapes that indicate points for
 *  each selection.  (x's squares, triangles, etc.)  It has no constructor
 *  and nothing but static methods.  Not very object-oriented but the best
 *  way I could think of to share the drawing code among the point display,
 *  the color key, and the selection manager.
 *
 *  Each littli icon is uniquely identified by an integer.  Non-negative
 *  integers are used to indicate selections; the first six correspond to
 *  actual shapes, and beyond that digits are just printed.  Negative numbers
 *  are for other stuff, like plain old point with no selection or modifications
 *  and sampling.  Eventual, this may support zoom-in indication too.
 *
 *  The methods of this class are static and final and fairly simple, so they
 *  should be inlined if you compile with optimizations enabled.
 */
class PointIcons {
	/** The icon used for a plain, unmodified point */
	public static final int DOT = -1;
	/** The icon used for points in the sample (a wide circle) */
	public static final int SAMPLE = -2;

	/** Draws point n at location p in graphics context g.  This is the only
	 *  public method of the class.  The state of the graphics context is not
	 *  changed by draw().  You should pick the color you want before calling
	 *  draw(), and turning on antialiasing is recommended for good-looking
	 *  icons. */
	public static final void draw(int n, Graphics g, Point p) {
		switch (n) {
			case DOT: drawDot(g,p); break;
			case SAMPLE: drawSampled(g,p); break;
			case 0: drawPlus(g,p); break;
			case 1: drawX(g,p); break;
			case 2: drawCircle(g,p); break;
			case 3: drawSquare(g,p); break;
			case 4: drawDiamond(g,p); break;
			case 5: drawTriangle(g,p); break;
		   default: drawDigit(n,g,p); break;
		}
	}

	private static final void drawDot(Graphics g, Point p) {
		int pointSize = 1;// radius of circles drawn to represent the points
		g.fillOval(p.x - pointSize, p.y - pointSize, 2 * pointSize + 1, 2 * pointSize + 1);
	}

	private static final void drawSampled(Graphics g, Point p) {
		int pointSize = 5;
		g.drawOval(p.x - pointSize, p.y - pointSize, 2 * pointSize + 1, 2 * pointSize + 1);
	}

	private static final void drawPlus(Graphics g, Point p) {
		/* draw a plus sign */
		g.drawLine(p.x - 3, p.y, p.x + 3, p.y);
		g.drawLine(p.x, p.y + 3, p.x, p.y - 3);
	}

	public static final void drawX(Graphics g, Point p) {
		/* draw an X */
		g.drawLine(p.x-3, p.y-3, p.x+3, p.y+3);
		g.drawLine(p.x-3, p.y+3, p.x+3, p.y-3);
	}

	public static final void drawCircle(Graphics g, Point p) {
		/* draw a circle */
		int radius = 3;
		g.drawOval(p.x - radius, p.y - radius, 2 * radius + 1, 2 * radius + 1);
	}

	public static final void drawSquare(Graphics g, Point p) {
		/* draw a square */
		int size = 3;
		g.drawLine(p.x-size, p.y-size, p.x+size, p.y-size);
		g.drawLine(p.x+size, p.y-size, p.x+size, p.y+size);
		g.drawLine(p.x+size, p.y+size, p.x-size, p.y+size);
		g.drawLine(p.x-size, p.y+size, p.x-size, p.y-size);
	}

	public static final void drawDiamond(Graphics g, Point p) {
		int xSize = 4;
		int ySize = 5;
		/* draw a diamond*/
		g.drawLine(p.x, p.y-ySize, p.x+xSize, p.y);
		g.drawLine(p.x+xSize, p.y, p.x, p.y+ySize);
		g.drawLine(p.x, p.y+ySize, p.x-xSize, p.y);
		g.drawLine(p.x-xSize, p.y, p.x, p.y-ySize);
	}

	public static final void drawTriangle(Graphics g, Point p) {
		/* draw a triangle */
		g.drawLine(p.x, p.y-4, p.x+3, p.y+4);
		g.drawLine(p.x+3, p.y+4, p.x-3, p.y+4);
		g.drawLine(p.x-3, p.y+4, p.x, p.y-3);
	}

	public static final void drawDigit(int n, Graphics g, Point p) {
		/* draw the digit n */
		g.drawString(Integer.toString(n), p.x-3, p.y+4);
	}
}




/**
 * This is a class I wrote to encapsulate all of the interaction between the mds
 * thread and the display (main) thread. The two threads communicate through
 * shared data (a set of points that represents the current embedding). There is
 * no other shared resouce. (Except for processor time on a single-processor
 * system. The two threads will have the same priority and yield to each other
 * after each loop iteration to make sure that one of them doesn't hog the CPU.)
 *
 *@author     Jeff Klingner
 */
class SharedPoints {

	private MDSPoint[] points;
	private float[][] bare_points;
	private int n_dims;
	private float stress;

	public SharedPoints(int number_of_points, int number_of_dimensions) {
		n_dims = number_of_dimensions;
		points = new MDSPoint[number_of_points];
		bare_points = new float[number_of_points][];
		for (int i = 0; i < number_of_points; i++) {
			points[i] = new MDSPoint(number_of_dimensions);
			bare_points[i] = new float[number_of_dimensions];
		}
	}

	public synchronized void resetNumberOfPoints(int newNumberOfPoints) {
		points = new MDSPoint[newNumberOfPoints];
		bare_points = new float[newNumberOfPoints][];
		for (int i = 0; i < newNumberOfPoints; i++) {
			points[i] = new MDSPoint(n_dims);
			bare_points[i] = new float[n_dims];
		}
	}

	public synchronized void setPoints(MDSPoint[] new_points) {
		// At entry (when called by the mds thread) this class is locked and
		// the display window can't access the points.
		for (int i = 0; i < points.length; i++) {
			for (int j = 0; j < n_dims; j++) {
				points[i].setComponent(j, new_points[i].getComponent(j));
			}
		}
		// At exit, the lock is released (by the mds thread) and getPoints
		// can be called by the main thread (to do display).
	}

	public synchronized void setStress(float stress) {
		this.stress = stress;
	}

	public synchronized float getStress() {
		return stress;
	}

	/** Called by the display/interaction class to get the current embedding */
	public synchronized float[][] getPoints() {
		// At entry (when called by the main thread to get the points for display)
		// a lock is acquired.  If the mds thread wants to write these points, it
		// will block until the lock is released.
		for (int i = 0; i < points.length; i++) {
			for (int j = 0; j < n_dims; j++) {
				bare_points[i][j] = points[i].getComponent(j);
			}
		}
		return bare_points;
		// At exit, the lock held by the display thread is released and the
		// mds thread is free to write to the points again.
	}
}

class MDSThread extends Thread {
	/**
	 *  set by MDSWindow in response to a press of the "Stop" button.  If set,
	 *  the thread will call wait() on itself to block until notified.  Whoever
	 *  sets this to true needs to reset it to false before notification.
	 */
	public boolean waitFlag;
	/** set by MDSWindow during exit and cleanup.  Causes thread to return and die. */
	public boolean exitFlag;

	/** the object in which the calculations happen */
	private MDS mds;
	/** for communication with the display and user interface thread */
	private SharedPoints sharedPoints;
	/** the object handling user interface and embedding display */
	private PointSetVisualization mdsWindow;

	public MDSThread(MDS mds, SharedPoints sharedPoints, PointSetVisualization mdsWindow) {
		super();
		this.mds = mds;
		this.sharedPoints = sharedPoints;
		this.mdsWindow = mdsWindow;
		waitFlag = false;
		exitFlag = false;
	}


	/** Main processing method for the MDSThread object */
	public void run() {
		int centering_check_counter = 0;

		while (true) {// loop forever. Thread will terminate when exitFlag is set by MDSWindow
			try {
				// sleeping is a good idea inside any hard loop
				sleep(5); //milliseconds

				if (waitFlag) {
					synchronized (this) {// required by the language to avoid race conditions on wait/notify
						while (waitFlag) {// while loop so that if we are notified by something else (unexpected), we won't resume
							wait();// the MDS thread suspends itself
						}
					}
				}
			} catch (InterruptedException e) { // can get interrupted during a wait() call.
				// An interrupt is unexpected.  Should be woken instead by a call to notify()
				System.out.println("MDS thread wait() was interrupted!  What happened?!");
			}
			// If notify() is called at a point where waitFlag is false, execution resumes here.

			// Check for exit.  Do this right after wait() so that if the user wants to exit while
			// the MDS thread is suspended, no useless computation will be done before this thread
			// dies.
			if (exitFlag) {
				return;// stop execution, ending the life of this MDSThread
			}

			// Do a computational step.
			mds.doOneIteration();

			// Every CENTERING_CHECK_PERIOD iterations, recenter (recentering only has an effect if
			// the embedding has drifted beyond a given threshold.
			centering_check_counter = (centering_check_counter + 1) % mds.CENTERING_CHECK_PERIOD;
			if (centering_check_counter == 0) {
				mds.center_embedding();
			}

			// Compute the stress of the current embedding
			/* Stress calculation is now integrated into the main doOneIteration() method */
			// mds.compute_stress();

			// Write the results to the shared data area
			sharedPoints.setPoints(mds.getEmbedding());
			sharedPoints.setStress(mds.getStress());

			// Tell the main window that an iteration has been completed.
			// This will induce a redraw of the embedding display and the stress display
			mdsWindow.mds_iteration_complete();

			//System.out.println("MDS thread ran an iteration. Priority = " + Thread.currentThread().getPriority());
		}
	}
}
