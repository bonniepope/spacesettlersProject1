package spacesettlers.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import spacesettlers.clients.Team;
import spacesettlers.simulator.SpaceSettlersSimulator;

/**
 * Shows the information for a team in the GUI
 * 
 * @author amy
 */
public class TeamInfoPanel extends JPanel {
	Team team;
	GridBagConstraints constraints;
	ResourcesPanel resourcesPanel;
	
	public TeamInfoPanel(Team team) {
		this.team = team;
		
		setBorder(BorderFactory.createLineBorder(team.getTeamColor(), 3));
		constraints = new GridBagConstraints();
		//constraints.insets = new Insets(10, 10, 10, 10);
		setLayout(new GridBagLayout());
		
		JLabel name = new JLabel(team.getLadderName());
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		add(name, constraints);

		resourcesPanel = new ResourcesPanel();
		constraints.gridx = 0;
		constraints.gridy = 1;
		add(resourcesPanel, constraints);
	}

	public void updateData(SpaceSettlersSimulator simulator) {
		resourcesPanel.updateData(simulator, team.getLadderName());
	}
	
}
