package spacesettlers.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.KeyStroke;

import spacesettlers.clients.Team;
import spacesettlers.configs.SpaceSettlersConfig;
import spacesettlers.objects.AbstractObject;
import spacesettlers.simulator.SpaceSettlersSimulator;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Position;

/**
 * Main GUI for the Space Settlers simulator
 * @author amy
 *
 */
public class SpaceSettlersGUI {
	JFrame mainFrame;
	
	JSpaceSettlersComponent mainComponent;
	
	JSpaceSettlersInfoPanel infoPanel;
	
	boolean isPaused = false;
	
	SpaceSettlersSimulator simulator;
	
	/**
	 * Make a new GUI
	 * @param config
	 */
	public SpaceSettlersGUI(SpaceSettlersConfig config, SpaceSettlersSimulator simulator) {
		super();
		this.simulator = simulator;
		
		mainFrame = new JFrame("Space Settlers");

		// get the screen size, code from:
		// http://stackoverflow.com/questions/3680221/how-can-i-get-the-monitor-size-in-java
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double screenWidth = screenSize.getWidth() - 50;
		double screenHeight = screenSize.getHeight() - 50;
		System.out.println("Screen width " + screenWidth);
		System.out.println("Screen height " + screenHeight);
		mainFrame.setPreferredSize(new Dimension((int)screenWidth,(int)screenHeight));

		// make the inner panel and components
		infoPanel = new JSpaceSettlersInfoPanel(simulator);
		infoPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		mainComponent = new JSpaceSettlersComponent(config.getHeight(), config.getWidth());
		JScrollPane mainScrollPane = new JScrollPane(mainComponent);
		//JScrollPane infoScrollPane = new JScrollPane(infoPanel);
		//infoScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		//infoScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		// put them into the main layout
		mainFrame.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = 1.0;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = GridBagConstraints.RELATIVE;
		constraints.fill = GridBagConstraints.BOTH;
		mainFrame.add(mainScrollPane, constraints);
		
        constraints.gridx = 1;
        constraints.gridy = 0;
		constraints.weightx = 0.0;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		mainFrame.add(infoPanel, constraints);
		
		// create a help menu
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Help");
		menuBar.add(menu);
		JMenuItem item = new JMenuItem("Interface help");
		item.setAccelerator(KeyStroke.getKeyStroke('h'));
		menu.add(item);
		item.addActionListener(new HelpMenuListener());
		
		mainFrame.setJMenuBar(menuBar);
		
		// add a key listener to handle pauses
		mainFrame.addKeyListener(new KeyAdapter() {
			/**
			 * Listen to the GUI level key commands
			 */
			@Override
			public void keyPressed(KeyEvent e) {
				char key = e.getKeyChar();
				if (key == 'p' || key == 'P') {
					togglePause();
				} else if (key == '+') {
					doubleSpeed();
				} else if (key == '-') {
					slowSpeed();
				}
			}
		}
		);
		
		// add any client key & mouse listeners that want to be added
		for (Team team : simulator.getTeams()) {
			KeyAdapter listener = team.getKeyAdapter();
			if (listener != null) {
				mainFrame.addKeyListener(listener);
			}

			MouseAdapter mouseListen = team.getMouseAdapter();
			if (listener != null) {
				mainComponent.addMouseListener(mouseListen);
				//mainComponent.addMouseMotionListener(mouseListen);
			}

		}
		
		// add the mouse listener for the info boxes
		mainComponent.addMouseListener(new GUIMouseListener());
		

		// finally draw it
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setResizable(false);
		mainFrame.pack();
		mainFrame.setVisible(true);
		//System.out.println("Info component width x height " + infoPanel.getWidth() + " x " + infoPanel.getHeight());
		//System.out.println("Main component width x height " + mainPanel.getWidth() + " x " + mainPanel.getHeight());
	}

	/**
	 * Main GUI mouse interface.  Pops up a info area in the info panel if the user LEFT clicks on an object
	 * 
	 * @author amy
	 *
	 */
	class GUIMouseListener extends MouseAdapter {
		@Override
		public void mouseReleased(MouseEvent e) {
			Point point = e.getPoint();
		
			Position clickPosition = new Position(point.getX(), point.getY());
			
			// only listens to left clicks
			if (e.getButton() == MouseEvent.BUTTON1) {
				// get the set of all objects and figure out if the user clicked inside an object
				Set<AbstractObject> allObjects = simulator.getAllObjects();
				Toroidal2DPhysics space = simulator.getSimulatedSpace();
				//System.out.println("Received a click at " + clickPosition);
				
				for (AbstractObject obj : allObjects) {
					//System.out.println("Object " + obj + " distance to click " + space.findShortestDistance(clickPosition, obj.getPosition()) + " radius is " + obj.getRadius());
					if (space.findShortestDistance(clickPosition, obj.getPosition()) <= obj.getRadius()) {
						infoPanel.setClickedObject(obj);
						//System.out.println("Click matched object " + obj);
						return;
					}
				}
				
			}
		}
	}


	/**
	 * Toggle the paused state
	 */
	protected void togglePause() {
		isPaused = !isPaused;
		simulator.setPaused(isPaused);

		if (isPaused) {
			System.out.println("Pausing spacewar");
		} else {
			System.out.println("Unpausing spacewar");
		}
		
	}

	/**
	 * Double the sim speed (by halving graphics sleep)
	 */
	protected void doubleSpeed() {
		int newSpeed = Math.max(5, simulator.getGraphicsSleep() / 2);
		simulator.setGraphicsSleep(newSpeed);
	}

	/**
	 * Slow the sim speed (by doubling graphics sleep)
	 */
	protected void slowSpeed() {
		int newSpeed = Math.min(simulator.getGraphicsSleep() * 2, 240);
		simulator.setGraphicsSleep(newSpeed);
	}

	
	/**
	 * Redraws the graphics
	 * @param simulator
	 */
	public void redraw() {
		infoPanel.setSimulator(simulator);
		infoPanel.updateData();
		//mainFrame.paintComponents(getGraphics());
		mainComponent.setSimulator(simulator);
		mainFrame.repaint();
	}
	
	/**
	 * Listener for the help menu
	 * @author amy
	 *
	 */
	public class HelpMenuListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			JFrame helpFrame = new JFrame("Keyboard and Mouse commands");
			
			String helpText = "Summary of all the keyboard commands.\n\n\n";
			helpText += " Main GUI commands:\n";
			helpText += "p/P  pauses and unpauses the simulation\n";
			helpText += "+  speeds up the simulation\n";
			helpText += "-  slows down the simulation\n";
			helpText += "h  brings up this menu\n\n";

			helpText +=	"Summary of the commands for the keyboard and mouse for the human client.\n\n\n";
			helpText += " Keyboard commands:\n";
			helpText += " Use the arrow keys to move in the associated direction.  Note that they give you acceleration in the direction of the arrow.\n";
			helpText += " The space bar will fire missiles.\n\n";
			helpText += " Mouse commands;\n";
			helpText += " Right click or alt-click in the GUI to have your agent fly to that location.  Don't forget the world is toroidal!";
			JTextArea helpTextArea = new JTextArea(helpText);
			helpTextArea.setEditable(false);
			
			helpFrame.add(helpTextArea);
			
			helpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			helpFrame.setResizable(false);
			helpFrame.pack();
			helpFrame.setVisible(true);


		}

	}




}
