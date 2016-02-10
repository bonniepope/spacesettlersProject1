package pope5889;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.DoNothingAction;
import spacesettlers.actions.MoveAction;
import spacesettlers.actions.MoveToObjectAction;
import spacesettlers.actions.PurchaseCosts;
import spacesettlers.actions.PurchaseTypes;
import spacesettlers.clients.ExampleKnowledge;
import spacesettlers.clients.TeamClient;
import spacesettlers.graphics.SpacewarGraphics;
import spacesettlers.objects.AbstractActionableObject;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Beacon;
import spacesettlers.objects.Ship;
import spacesettlers.objects.powerups.SpaceSettlersPowerupEnum;
import spacesettlers.objects.resources.ResourcePile;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Position;

/**
 * Prioritizes collecting asteroids over collecting beacons
 * 
 * @author Collins and Pope (based heavily on PacifistHeuristicAsteroidCollector by Professor McGovern)
 */
public class PriorityAsteroidCollector extends TeamClient {
	HashMap <UUID, Ship> asteroidToShipMap;
	HashMap <UUID, Boolean> aimingForBase;
	
	MyState state;
	

	/**
	 * Assigns ships to specific actions
	 */
	public Map<UUID, AbstractAction> getMovementStart(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects) {
		HashMap<UUID, AbstractAction> actions = new HashMap<UUID, AbstractAction>();

		// loop through each ship
		for (AbstractObject actionable :  actionableObjects) {
			if (actionable instanceof Ship) {
				Ship ship = (Ship) actionable;

				AbstractAction action;
				action = getAsteroidCollectorAction(space, ship);
				actions.put(ship.getId(), action);
				
			} else {
				// it is a base.  Heuristically decide when to use the shield (TODO)
				actions.put(actionable.getId(), new DoNothingAction());
			}
		} 
		return actions;
	}
	
	/**
	 * Gets the action for the asteroid prioritizing ship
	 * @param space
	 * @param ship
	 * @return
	 */
	private AbstractAction getAsteroidCollectorAction(Toroidal2DPhysics space,
			Ship ship) {
		//AbstractAction current = ship.getCurrentAction();
		Position currentPosition = ship.getPosition();
		
		//if the ship's resources are less than 750, 
		//move toward high resource value free asteroids
		if(ship.getResources().getTotal() < state.getGoalResources()){
			aimingForBase.put(ship.getId(), false);
			Asteroid asteroidFirstChoice = pickHighestValueFreeAsteroid(space, ship);
			Asteroid asteroidSecondChoice = findNearestAsteroid(space, ship);
			Beacon beacon = pickNearestBeacon(space, ship);
			double beaconDistance = space.findShortestDistance(ship.getPosition(), beacon.getPosition());
			double asteroid1Distance = space.findShortestDistance(ship.getPosition(), asteroidFirstChoice.getPosition());
			double asteroid2Distance = space.findShortestDistance(ship.getPosition(), asteroidSecondChoice.getPosition());
			AbstractAction newAction = null;

			//if asteroid1 isn't null and if asteroid1 is closer than asteroid2, 
			//move towards asteroid1
			if ((asteroidFirstChoice != null) && (asteroid1Distance < asteroid2Distance)) {
				asteroidToShipMap.put(asteroidFirstChoice.getId(), ship);
				newAction = new MoveToObjectAction(space, currentPosition, asteroidFirstChoice);
			}
			//else if asteroid 2 isn't null
			//move towards asteroid2
			else{ 
				asteroidToShipMap.put(asteroidSecondChoice.getId(), ship);
				newAction = new MoveToObjectAction(space, currentPosition, asteroidSecondChoice);
			}
			/*else{
				newAction = new MoveToObjectAction(space, currentPosition, beacon);
			}*/
			return newAction;	
		}
		// if the ship equal to or greater than the amount of goal resources, take it back to base
		else if (ship.getResources().getTotal() >= state.getGoalResources()) {
			Base base = findNearestBase(space, ship);
			AbstractAction newAction = new MoveToObjectAction(space, currentPosition, base);
			aimingForBase.put(ship.getId(), true);
			return newAction;
		}
		// did we bounce off the base?
		else if(ship.getResources().getTotal() == 0 && ship.getEnergy() > 2000 && aimingForBase.containsKey(ship.getId()) && aimingForBase.get(ship.getId())) {
			//current = null;
			aimingForBase.put(ship.getId(), false);
		}
		else{
			Beacon beacon = pickNearestBeacon(space, ship);
			AbstractAction newAction = null;
			// if there is no beacon, then go find an asteroid
			if (beacon == null) {
				Asteroid asteroid = pickHighestValueFreeAsteroid(space, ship);
				newAction = new MoveToObjectAction(space, currentPosition, asteroid);
			} else {
				newAction = new MoveToObjectAction(space, currentPosition, beacon);
			}
			aimingForBase.put(ship.getId(), false);
			return newAction;
		}
		return ship.getCurrentAction();
	}
	/**
	 * Find the base for this team nearest to this ship
	 * 
	 * @param space
	 * @param ship
	 * @return
	 */
	private Base findNearestBase(Toroidal2DPhysics space, Ship ship) {
		double minDistance = Double.MAX_VALUE;
		Base nearestBase = null;

		for (Base base : space.getBases()) {
			if (base.getTeamName().equalsIgnoreCase(ship.getTeamName())) {
				double dist = space.findShortestDistance(ship.getPosition(), base.getPosition());
				if (dist < minDistance) {
					minDistance = dist;
					nearestBase = base;
				}
			}
		}
		return nearestBase;
	}

	/**
	 * Returns the asteroid of highest value that isn't already being chased by this team
	 * 
	 * @return
	 */
	private Asteroid pickHighestValueFreeAsteroid(Toroidal2DPhysics space, Ship ship) {
		Set<Asteroid> asteroids = space.getAsteroids();
		int bestMoney = Integer.MIN_VALUE;
		Asteroid bestAsteroid = null;

		for (Asteroid asteroid : asteroids) {
			if (!asteroidToShipMap.containsKey(asteroid)) {
				if (asteroid.isMineable() && asteroid.getResources().getTotal() > bestMoney) {
					bestMoney = asteroid.getResources().getTotal();
					bestAsteroid = asteroid;
				}
			}
		}
		//System.out.println("Best asteroid has " + bestMoney);
		return bestAsteroid;
	}

	/**
	 * Find the nearest asteroid
	 * 
	 * @param space
	 * @param ship
	 * @return
	 */
	
	private Asteroid findNearestAsteroid(Toroidal2DPhysics space, Ship ship){

		double minDistance = Double.MAX_VALUE;
		Asteroid nearestAsteroid = null;
		
		for(Asteroid asteroid : space.getAsteroids()){
			double dist = space.findShortestDistance(ship.getPosition(), asteroid.getPosition());
			if((dist < minDistance) && (asteroid != null) && (asteroid.isMineable())){
				minDistance = dist;
				nearestAsteroid = asteroid;
			}
			else{
				nearestAsteroid = pickHighestValueFreeAsteroid(space, ship);
			}
		}
		return nearestAsteroid;
	}
	
	/**
	 * Find the nearest beacon to this ship
	 * @param space
	 * @param ship
	 * @return
	 */
	private Beacon pickNearestBeacon(Toroidal2DPhysics space, Ship ship) {
		// get the current beacons
		Set<Beacon> beacons = space.getBeacons();

		Beacon closestBeacon = null;
		double bestDistance = Double.POSITIVE_INFINITY;

		for (Beacon beacon : beacons) {
			double dist = space.findShortestDistance(ship.getPosition(), beacon.getPosition());
			if (dist < bestDistance) {
				bestDistance = dist;
				closestBeacon = beacon;
			}
		}
		return closestBeacon;
	}

	@Override
	public void getMovementEnd(Toroidal2DPhysics space, Set<AbstractActionableObject> actionableObjects) {
		ArrayList<Asteroid> finishedAsteroids = new ArrayList<Asteroid>();

		for (UUID asteroidId : asteroidToShipMap.keySet()) {
			Asteroid asteroid = (Asteroid) space.getObjectById(asteroidId);
			if (asteroid == null || !asteroid.isAlive()) {
 				finishedAsteroids.add(asteroid);
				//System.out.println("Removing asteroid from map");
			}
		}
		for (Asteroid asteroid : finishedAsteroids) {
			asteroidToShipMap.remove(asteroid);
		}
	}
	
	/**
	 * Demonstrates one way to read in knowledge from a file
	 */
	@Override
	public void initialize(Toroidal2DPhysics space) {
		asteroidToShipMap = new HashMap<UUID, Ship>();
		aimingForBase = new HashMap<UUID, Boolean>();
		
		/*XStream xstream = new XStream();
		xstream.alias("ExampleKnowledge", ExampleKnowledge.class);
		try { 
			myKnowledge = (ExampleKnowledge) xstream.fromXML(new File(knowledgeFile));
		} catch (XStreamException e) {
			// if you get an error, handle it other than a null pointer because
			// the error will happen the first time you run
			myKnowledge = new ExampleKnowledge();
		} */
	}

	/**
	 * Demonstrates saving out to the xstream file
	 * You can save out other ways too.  This is a human-readable way to examine
	 * the knowledge you have learned.
	 */
	@Override
	public void shutDown(Toroidal2DPhysics space) {
		/*XStream xstream = new XStream();
		xstream.alias("ExampleKnowledge", ExampleKnowledge.class);
		try { 
			// if you want to compress the file, change FileOuputStream to a GZIPOutputStream
			xstream.toXML(myKnowledge, new FileOutputStream(new File(knowledgeFile)));
		} catch (XStreamException e) {
			// if you get an error, handle it somehow as it means your knowledge didn't save
			// the error will happen the first time you run
			myKnowledge = new ExampleKnowledge();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			myKnowledge = new ExampleKnowledge();
		} */
	}

	@Override
	public Set<SpacewarGraphics> getGraphics() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	/**
	 * If there is enough resourcesAvailable, buy a base.  Place it by finding a ship that is sufficiently
	 * far away from the existing bases
	 */
	public Map<UUID, PurchaseTypes> getTeamPurchases(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects, 
			ResourcePile resourcesAvailable, 
			PurchaseCosts purchaseCosts) {
		
		HashMap<UUID, PurchaseTypes> purchases = new HashMap<UUID, PurchaseTypes>();
		double BASE_BUYING_DISTANCE = 200;
		boolean bought_base = false;

		if (purchaseCosts.canAfford(PurchaseTypes.BASE, resourcesAvailable)) {
			for (AbstractActionableObject actionableObject : actionableObjects) {
				if (actionableObject instanceof Ship) {
					Ship ship = (Ship) actionableObject;
					Set<Base> bases = space.getBases();

					// how far away is this ship to a base of my team?
					double maxDistance = Double.MIN_VALUE;
					for (Base base : bases) {
						if (base.getTeamName().equalsIgnoreCase(getTeamName())) {
							double distance = space.findShortestDistance(ship.getPosition(), base.getPosition());
							if (distance > maxDistance) {
								maxDistance = distance;
							}
						}
					}
					if (maxDistance > BASE_BUYING_DISTANCE) {
						purchases.put(ship.getId(), PurchaseTypes.BASE);
						bought_base = true;
						//System.out.println("Buying a base!!");
						break;
					}
				}
			}		
		} 
		return purchases;
	}

	/**
	 * The priority asteroid collector doesn't use powerups
	 * @param space 
	 * @param actionableObjects
	 * @return
	 */
	@Override
	public Map<UUID, SpaceSettlersPowerupEnum> getPowerups(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects) {
		HashMap<UUID, SpaceSettlersPowerupEnum> powerUps = new HashMap<UUID, SpaceSettlersPowerupEnum>();
		
		return powerUps;
	}

}

