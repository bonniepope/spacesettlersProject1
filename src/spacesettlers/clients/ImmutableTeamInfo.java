package spacesettlers.clients;

import spacesettlers.objects.resources.ResourcePile;

/**
 * Immutable class that holds the necessary team info to share with
 * other teams.
 * 
 * @author amy
 *
 */
public class ImmutableTeamInfo {
	/**
	 * current team score (set in the simulator, which knows how the team is being scored)
	 */
	double score;

	/**
	 * Name of the team 
	 */
	String teamName;
	
	/**
	 * available (unspent) resourcesAvailable from the asteroids and the total resourcesAvailable earned
	 */
	ResourcePile availableResources, totalResources;
	
	/**
	 * Keep track of the total beacons collected (for the ladder)
	 */
	int totalBeacons;
	
	/**
	 * The name that shows up in the ladder
	 */
	String ladderName;

	public ImmutableTeamInfo(Team team) {
		score = team.score;
		teamName = team.teamName;
		availableResources = team.availableResources;
		totalResources = team.totalResources;
		totalBeacons = team.totalBeacons;
		ladderName = team.ladderName;
	}

	public double getScore() {
		return score;
	}

	public String getTeamName() {
		return teamName;
	}

	public ResourcePile getAvailableResources() {
		return availableResources;
	}

	public ResourcePile getTotalResources() {
		return totalResources;
	}

	public int getTotalBeacons() {
		return totalBeacons;
	}

	public String getLadderName() {
		return ladderName;
	}

}
