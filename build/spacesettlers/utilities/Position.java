package spacesettlers.utilities;

/**
 * Position in space (x,y,orientation)
 * @author amy
 *
 */
public class Position {
	double x, y, orientation, angularVelocity;
	Vector2D velocity;

	public Position(double x, double y) {
		super();
		this.x = x;
		this.y = y;
		velocity = new Vector2D();
	}

	public Position(double x, double y, double orientation) {
		super();
		this.x = x;
		this.y = y;
		this.orientation = orientation;
		velocity = new Vector2D();
	}
	
	public Position(Vector2D vec) {
		super();
		this.x = vec.getXValue();
		this.y = vec.getYValue();
		orientation = 0;
		velocity = new Vector2D();
	}
	
	public Position deepCopy() {
		Position newPosition = new Position(x, y, orientation);
		newPosition.velocity = new Vector2D(velocity);
		newPosition.angularVelocity = angularVelocity;
		
		return newPosition;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getOrientation() {
		return orientation;
	}
	
	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getTotalTranslationalVelocity() {
		return velocity.getTotal();
	}
	
	public double getTranslationalVelocityX() {
		return velocity.getXValue();
	}
	
	public double getTranslationalVelocityY() {
		return velocity.getYValue();
	}
	
	public Vector2D getTranslationalVelocity() {
		return velocity;
	}
	
	public void setTranslationalVelocity(Vector2D newVel) {
		this.velocity = newVel;
	}
	

	public double getAngularVelocity() {
		return angularVelocity;
	}
	
	public void setOrientation(double orientation) {
		this.orientation = orientation;
	}
	
	public double getxVelocity() {
		return velocity.getXValue();
	}

	public void setxVelocity(double xVelocity) {
		velocity.setX(xVelocity);
	}

	public double getyVelocity() {
		return velocity.getYValue();
	}

	public void setyVelocity(double yVelocity) {
		velocity.setY(yVelocity);
	}

	public void setAngularVelocity(double angularVelocity) {
		this.angularVelocity = angularVelocity;
	}

	public String toString() {
		String str = "(" + x + " , " + y + ", " + orientation + ") velocity: " + velocity + ", " + angularVelocity;
		return str;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(angularVelocity);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(orientation);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((velocity == null) ? 0 : velocity.hashCode());
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		if (Double.doubleToLongBits(angularVelocity) != Double
				.doubleToLongBits(other.angularVelocity))
			return false;
		if (Double.doubleToLongBits(orientation) != Double
				.doubleToLongBits(other.orientation))
			return false;
		if (velocity == null) {
			if (other.velocity != null)
				return false;
		} else if (!velocity.equals(other.velocity))
			return false;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}
	
	
	
}
