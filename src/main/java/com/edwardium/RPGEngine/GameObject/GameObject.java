package com.edwardium.RPGEngine.GameObject;

import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Vector2D;

public abstract class GameObject {

	public Vector2D position;

	protected float maxRotationSpeed = -0.1f;
	protected float rotation = 0;

	protected float mass = 1;
	protected Vector2D velocity;

	public boolean isDrawn = true;

	protected GameObject() {
		this(new Vector2D(0, 0));
	}

	protected GameObject(Vector2D position) {
		this.position = position;
		this.velocity = new Vector2D();
	}

	public void update(float elapsedTime, float velocityDiminishFactor) {
		this.position.add(Vector2D.multiply(this.velocity, elapsedTime));

		this.velocity.multiply(velocityDiminishFactor * elapsedTime);
	}
	public void render(Renderer gameRenderer) {}

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

	public GameObject rotateToPoint(Vector2D target) {
		float rotationDelta = Vector2D.subtract(target, this.position).angleBetween(Vector2D.fromAM(this.rotation, 1));
		return rotateBy(rotationDelta);
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
}
