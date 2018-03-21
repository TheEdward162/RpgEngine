package com.edwardium.RPGEngine.GameEntity.GameObject;

import com.edwardium.RPGEngine.GameEntity.GameHitbox;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Vector2D;

public abstract class GameObject {

	public Vector2D position;
	public String name;

	public float maxRotationSpeed = -0.1f;
	public float rotation = 0;

	public float mass = 1;
	public Vector2D velocity;

	public boolean doesCollide = true;
	public GameHitbox hitbox = new GameHitbox(20f);
	public float dragCoefficient = 0.1f;

	public boolean isDrawn = true;
	public boolean toDelete = false;

	protected GameObject() {
		this(new Vector2D(0, 0));
	}

	protected GameObject(Vector2D position) {
		this(position, "");
	}

	protected GameObject(Vector2D position, String name) {
		this.position = position;
		this.velocity = new Vector2D();
		this.name = name;
	}

	public void update(float elapsedTime, float environmentDensity) {
		this.applyForce(calculateResistanceForce(environmentDensity).multiply(elapsedTime));
		this.position.add(Vector2D.multiply(this.velocity, elapsedTime));
	}

	public void render(Renderer gameRenderer, boolean drawHitbox) {
		if (isDrawn && drawHitbox) {
			if (this.hitbox != null) {
				GameHitbox.renderHitbox(gameRenderer, this.position, this.rotation, this.hitbox);
			}
		}
	}

	protected GameObject applyForce(Vector2D force) {
		this.velocity.add(Vector2D.divide(force, this.mass));

		return this;
	}
	public GameObject changeMass(float newMass) {
		float m = newMass / mass;
		this.mass = newMass;
		this.velocity.divide(m);

		return this;
	}
	protected Vector2D calculateResistanceForce(float environmentDensity) {
		// we are going to use a modified drag equation
		// Fd = 1/2 * p * v^2 * Cd * A
		// p is density, v is speed, Cd is drag coefficient and A is cross section area

		// note that this works as long as the object is not spinning
		// which bullets tend to be
		// so yeah, fml

		if (this.velocity.getMagnitude() == 0)
			return new Vector2D();

		float crossSection = this.hitbox.calculateCrossSection(new Vector2D(this.velocity));
		float dragForce = (float)(1.0 / 2.0 * environmentDensity * Math.pow(velocity.getMagnitude(), 2) * dragCoefficient * crossSection);
		if (dragForce > velocity.getMagnitude())
			dragForce = velocity.getMagnitude();

		return new Vector2D(velocity).inverse().setMagnitude(dragForce);
	}

	public GameHitbox.CollisionInfo checkCollision(GameObject other) {
		if (doesCollide && this.hitbox != null && other.doesCollide && other.hitbox != null) {
			return this.hitbox.checkCollision(this.position, this.velocity, this.rotation, other.hitbox, other.position, other.velocity, other.rotation);
		} else {
			return null;
		}
	}

	public void collideWith(GameObject other, Vector2D otherSideNormal) {

	}

	public Vector2D getFacingDirection() {
		return new Vector2D(1, 0).setAngle(this.rotation);
	}

	public GameObject rotateBy(float angle) {
		// constrain angle
		if (maxRotationSpeed >= 0) {
			if (angle > maxRotationSpeed) {
				angle = maxRotationSpeed;
			} else if (angle < -maxRotationSpeed) {
				angle = -maxRotationSpeed;
			}
		}

		this.rotation += angle;

		// normalize
		while (this.rotation >= Math.PI * 2) {
			this.rotation -= Math.PI * 2;
		}

		while (this.rotation < 0) {
			this.rotation += Math.PI * 2;
		}

		return this;
	}
	public GameObject rotateTo(float angle) {
		Vector2D angledVector = Vector2D.fromAM(angle, 1);
		float rotationDelta = angledVector.angleBetween(Vector2D.fromAM(this.rotation, 1));
		return rotateBy(rotationDelta);
	}
	public GameObject rotateToward(boolean left) {
		float rotationDelta = new Vector2D(1, 0).setAngle(this.rotation + (left ? 1 : -1)).angleBetween(new Vector2D(1, 0).setAngle(this.rotation));
		if (maxRotationSpeed >= 0) {
			rotationDelta = maxRotationSpeed * Math.signum(rotationDelta);
		}

		return rotateBy(rotationDelta);
	}

	public boolean rotateToPoint(Vector2D target) {
		float rotationDelta = Vector2D.subtract(target, this.position).angleBetween(Vector2D.fromAM(this.rotation, 1));
		rotateBy(rotationDelta);

		rotationDelta = Vector2D.subtract(target, this.position).angleBetween(Vector2D.fromAM(this.rotation, 1));
		return rotationDelta <= 0.5 / 180 * Math.PI;
	}
}
