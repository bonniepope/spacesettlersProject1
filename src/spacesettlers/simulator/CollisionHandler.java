package spacesettlers.simulator;

import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Beacon;
import spacesettlers.objects.Ship;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.weapons.EMP;
import spacesettlers.objects.weapons.Missile;
import spacesettlers.utilities.Vector2D;

/**
 * Handles collisions between all types of objects.  Implements the rules of spacewar for 
 * bouncing off objects versus picking them up, etc.  Implemented in a central place
 * to make it easy to change as need be.
 * 
 * Note: people have observed ships "sticking" to asteroids or bases when they hit them at low
 * velocity. This isn't a bug in the physics simulator.  If you have a lower mass object hitting 
 * an object of much higher mass at a low velocity, you would expect the low mass one to essentially
 * stick to the higher mass one.  The solution is to move quickly away from the objects after you collide.
 * 
 * 
 * @author amy
 */
public class CollisionHandler {
    public static final double COLLISION_PENALTY = 2.0;

	class CollisionData {
		double v1, v2;
	}
	
	/**
	 * Collide the objects and then take care of side effects based on object type
	 * 
	 * @param object1
	 * @param object2
	 * @param space
	 */
	public void collide(AbstractObject object1, AbstractObject object2, Toroidal2DPhysics space) {
		// if either object is a beacon, handle that (and don't elastically collide)
		if (object1.getClass() == Beacon.class) {
			beaconCollision((Beacon) object1, object2);
			return;
		} else if (object2.getClass() == Beacon.class) {
			beaconCollision((Beacon) object2, object1);
			return;
		}

		// if either object is a missile, handle that (and don't elastically collide)
		if (object1.getClass() == Missile.class) {
			missileCollision((Missile) object1, object2);
		} else if (object2.getClass() == Missile.class) {
			missileCollision((Missile) object2, object1);
		}
		
		// if either object is a EMP, handle that (and don't elastically collide)
		if (object1.getClass() == EMP.class) {
			EMPCollision((EMP) object1, object2);
		} else if (object2.getClass() == EMP.class) {
			EMPCollision((EMP) object2, object1);
		}
		
		// only elastically collide if it isn't a beacon, missile, or other weapon
		if (!object1.isMoveable()) {
			elasticCollision2DWithNonMoveableObject(object2, object1, space);
		} else if (!object2.isMoveable()) {
			elasticCollision2DWithNonMoveableObject(object1, object2, space);
		} else {
			elasticCollision2D(object1, object2, space);
		}

		// if it is a ship, give it an energy penalty for running into the object
		if (object1.getClass() == Ship.class) {
			shipCollision((Ship) object1);
		} 
		if (object2.getClass() == Ship.class) {
			shipCollision((Ship) object2);
		}

		// handle mineable asteroid collisions (e.g. mine them if needed)
		if (object1.getClass() == Asteroid.class) {
			asteroidCollision((Asteroid) object1, object2);
		} else if (object2.getClass() == Asteroid.class) {
			asteroidCollision((Asteroid) object2, object1);
		}
		
		// handle base collisions
		if (object1.getClass() == Base.class) {
			baseCollision((Base) object1, object2);
		} else if (object2.getClass() == Base.class) {
			baseCollision((Base) object2, object1);
		}
		
	}
	
	/**
	 * Collide with a missile
	 * @param object1
	 * @param object2
	 */
	private void missileCollision(Missile missile, AbstractObject object2) {
		// get the ship that fired this
		Ship firingShip = missile.getFiringShip();
		firingShip.decrementWeaponCount();

		// did it hit a ship?
		if (object2.getClass() == Ship.class) {
			Ship ship = (Ship) object2;
			
			// only take damage if not shielded
			if (!ship.isShielded()) {
				ship.updateEnergy(missile.getDamage());
				firingShip.incrementDamage(-missile.getDamage());
			}
			
			// it hit a ship
			firingShip.incrementHits();

			// if the bullet killed the ship, credit the ship that hit it
			if (ship.getEnergy() <= 0) {
				//System.out.println("ship " + firingShip.getTeamName() + " stealing resourcesAvailable " + shipMoney + " from " + ship.getTeamName() + ship.getId());
				
				// it killed a ship
				firingShip.incrementKills();
			}

		}
		
		// did it hit a base?
		if (object2.getClass() == Base.class) {
			Base base = (Base) object2;
			
			// only take damage if not shielded
			if (!base.isShielded()) {
				base.updateEnergy(missile.getDamage());
				firingShip.incrementDamage(-missile.getDamage());
			}
			
			// it hit a base
			firingShip.incrementHits();

		}
		
		// Handle a bullet hitting a bullet
		if (object2.getClass() == Missile.class) {
			object2.setAlive(false);
			Ship otherFiringShip = ((Missile) object2).getFiringShip();
			otherFiringShip.decrementWeaponCount();
		}
		
		// make the missile die
		missile.setAlive(false);
	}

	/**
	 * Collide with a EMP
	 * @param object1
	 * @param object2
	 */
	private void EMPCollision(EMP emp, AbstractObject object2) {
		// get the ship that fired
		Ship firingShip = emp.getFiringShip();
		firingShip.decrementWeaponCount();
		
		if (object2.getClass() == Ship.class) {
			Ship ship = (Ship) object2;
			
			// only take a hit if not shielded
			if (!ship.isShielded()) {
				ship.updateEnergy(emp.getDamage());
				ship.setFreezeCount(emp.getFreezeCount());
			}
			
			// it hit a ship
			firingShip.incrementHits();

		}
		
		if (object2.getClass() == Base.class) {
			Base base = (Base) object2;
			
			// only take a hit if not shielded
			if (!base.isShielded()) {
				base.updateEnergy(emp.getDamage());
				base.setFreezeCount(emp.getFreezeCount());
			}
			
			// it hit a base
			firingShip.incrementHits();
		}
		
		// Handle a emp hitting a emp (no damage but both weapons die)
		if (object2.getClass() == EMP.class) {
			object2.setAlive(false);
			Ship otherFiringShip = ((EMP) object2).getFiringShip();
			otherFiringShip.decrementWeaponCount();
		}
		
		emp.setAlive(false);
		
	}

	
	/**
	 * Collide with an asteroid
	 * 
	 * @param asteroid
	 * @param object
	 */
	public void asteroidCollision(Asteroid asteroid, AbstractObject object) {
		// if the asteroid isn't mineable, nothing changes
		if (!asteroid.isMineable()) {
			return;
		}
		
		// if a ship ran into it, it "mines" the asteroid
		if (object.getClass() == Ship.class) {
			Ship ship = (Ship) object;
			ship.addResources(asteroid.getResources());
			asteroid.setAlive(false);
			//System.out.println("ship " + ship.getTeamName() + ship.getId() +" now has resourcesAvailable " + ship.getMoney());
		}
		
		//if (object.getClass() == Asteroid.class) {
		//	object.setAlive(false);
		//}

	}

	/**
	 * Give the ship an energy penalty for running into the object
	 * 
	 * @param ship
	 */
	public void shipCollision(Ship ship) {
		double penalty = -Math.abs(COLLISION_PENALTY * ship.getPosition().getTotalTranslationalVelocity());
		ship.updateEnergy((int)(penalty));
	}

	/**
	 * Collide with a beacon
	 * @param beacon
	 * @param object
	 */
	public void beaconCollision(Beacon beacon, AbstractObject object) {
		// beacons die when they are touched (respawned elsewhere)
		beacon.setAlive(false);

		if (object.getClass() == Ship.class) {
			Ship ship = (Ship) object;
			ship.incrementBeaconCount();
			ship.updateEnergy(Beacon.BEACON_ENERGY_BOOST);
		}
	}
	
	/**
	 * Collide into the base
	 * 
	 * @param base
	 * @param object
	 */
	public void baseCollision(Base base, AbstractObject object) {
		if (object.getClass() == Ship.class) {
			Ship ship = (Ship) object;
			
			if (ship.getTeamName().equalsIgnoreCase(base.getTeamName())) {
				base.addResources(ship.getResources());
				ship.resetResources();
				double origEnergy = ship.getEnergy();
				ship.updateEnergy(base.getHealingEnergy());
				double energyChange = ship.getEnergy() - origEnergy;
				base.updateEnergy(-(int)energyChange);
				//System.out.println("ship " + ship.getTeamName() + ship.getId() + " left resourcesAvailable at base and now has resourcesAvailable " + ship.getMoney());
			}
		}
	}
	

	
	/**
	 * Elastically collide all objects following the vector formulation found at:
	 * 
	 * http://www.vobarian.com/collisions/2dcollisions2.pdf
	 * 
	 * @param object1 first object in the collision
	 * @param object2 second object in the collision
	 * @param space handle to space for distance calculations
	 */
	private void elasticCollision2D(AbstractObject object1,
			AbstractObject object2, Toroidal2DPhysics space) {
		// get the masses
		double m1 = object1.getMass();
		double m2 = object2.getMass();
		
		// now get the vector from the first to the second, get the unit normal and tangent
		Vector2D distanceVec = space.findShortestDistanceVector(object1.getPosition(), object2.getPosition());
		Vector2D unitNormal = distanceVec.getUnitVector();
		Vector2D unitTangent = new Vector2D(-unitNormal.getYValue(), unitNormal.getXValue());
		
		// get the velocity vectors
		Vector2D velocity1 = object1.getPosition().getTranslationalVelocity();
		Vector2D velocity2 = object2.getPosition().getTranslationalVelocity();

		// get the scalars in each direction
		double u1 = velocity1.dot(unitNormal);
		double u2 = velocity2.dot(unitNormal);
		double t1 = velocity1.dot(unitTangent);
		double t2 = velocity2.dot(unitTangent);
		
		// elastically collide in the one dimension
		CollisionData result = elasticCollision1D(u1, m1, u2, m2);
		
		// now get it back to the original space
		Vector2D vel1Normal = unitNormal.multiply(result.v1);
		Vector2D vel2Normal = unitNormal.multiply(result.v2);
		Vector2D vel1Tangent = unitTangent.multiply(t1);
		Vector2D vel2Tangent = unitTangent.multiply(t2);
		
		// add the normal and tangential parts
		Vector2D newVelocity1 = vel1Normal.add(vel1Tangent);
		Vector2D newVelocity2 = vel2Normal.add(vel2Tangent);
		
		object1.getPosition().setTranslationalVelocity(newVelocity1);
		object2.getPosition().setTranslationalVelocity(newVelocity2);
	}
	
	/**
	 * Elastically collide all objects following the vector formulation found at:
	 * 
	 * http://www.vobarian.com/collisions/2dcollisions2.pdf
	 * 
	 * @param object1 first object in the collision
	 * @param object2 second object in the collision
	 * @param space handle to space for distance calculations
	 */
	private void elasticCollision2DWithNonMoveableObject(AbstractObject movingObject,
			AbstractObject stationaryObject, Toroidal2DPhysics space) {
		// get the masses
		double m1 = movingObject.getMass();
		double m2 = stationaryObject.getMass();
		
		// now get the vector from the first to the second, get the unit normal and tangent
		Vector2D distanceVec = space.findShortestDistanceVector(movingObject.getPosition(), stationaryObject.getPosition());
		Vector2D unitNormal = distanceVec.getUnitVector();
		Vector2D unitTangent = new Vector2D(-unitNormal.getYValue(), unitNormal.getXValue());
		
		// get the velocity vectors
		Vector2D velocity1 = movingObject.getPosition().getTranslationalVelocity();

		// get the scalars in each direction
		double u1 = velocity1.dot(unitNormal);
		double t1 = velocity1.dot(unitTangent);
		
		// now just reverse the velocity for the first object
		double v1 = -u1;
		
		// now get it back to the original space
		Vector2D vel1Normal = unitNormal.multiply(v1);
		Vector2D vel1Tangent = unitTangent.multiply(t1);
		
		// add the normal and tangential parts
		Vector2D newVelocity1 = vel1Normal.add(vel1Tangent);
		
		movingObject.getPosition().setTranslationalVelocity(newVelocity1);
	}


	/**
	 * Set the movement in all directions to be 0
	 * 
	 * @param object
	 */
	public void resetMovement(AbstractObject object) {
		object.getPosition().setAngularVelocity(0);
		object.getPosition().getTranslationalVelocity().reset();
	}
	
	
	/**
	 * Elastic collisions in 1 dimenson, solved using the equations from wikipedia:
	 * 
	 * http://en.wikipedia.org/wiki/Elastic_collision
	 * 
	 * let
	 * u1 = velocity of item 1 before collision, m1 = mass of item 1
	 * u2 = velocity of item 2 before collision, m2 = mass of item 2
	 * v1 = velocity of item 1 after collision
	 * v2 = velocity of item 2 after collision
	 * 
	 * v1 = ((u1 * (m1 - m2)) + (2 * m2 * u2)) / (m1 + m2)
	 * v2 = ((u2 * (m2 - m1)) + (2 * m1 * u1)) / (m1 + m2)
	 * 
	 */
	public CollisionData elasticCollision1D(double u1, double m1, double u2, double m2) {
		double v1 = ((u1 * (m1 - m2)) + (2 * m2 * u2)) / (m1 + m2);
		double v2 = ((u2 * (m2 - m1)) + (2 * m1 * u1)) / (m1 + m2);

		CollisionData data = new CollisionData();
		data.v1 = v1;
		data.v2 = v2;
		
		return data;
	}



}
