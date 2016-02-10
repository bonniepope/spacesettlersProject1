package pope5889;

/**
 * Knowledge representation for PriorityAsteroidCollector
 * 
 * @author Collins and Pope
 *
 */
public class MyState {
		
	String teamName;
	
	int goal_resources = 750;
	double low_health = 1000;

	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}
	
	public int getGoalResources(){
		return goal_resources;
	}
	
	public double getLowHealth(){
		return low_health; 
	}
	public void setGoalResources(int goal){
		goal_resources = goal;
	}
	
	public void setLowHealth(double health){
		low_health = health;
	}
	
	
}
