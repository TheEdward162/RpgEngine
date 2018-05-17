package com.edwardium.RPGEngine.GameEntity.GameObject;

import com.edwardium.RPGEngine.Control.SceneController.PlaySceneController;
import com.edwardium.RPGEngine.GameEntity.GameHitbox;
import com.edwardium.RPGEngine.IO.JsonBuilder;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Utility.GameSerializable;
import com.edwardium.RPGEngine.Utility.Vector2D;

import javax.json.JsonObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class GameObject implements GameSerializable {

	public Vector2D position;
	public String name;

	public float maxRotationSpeed = -0.1f;
	protected float rotation = 0;

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

	public void updatePhysics(float elapsedTime, float environmentDensity) {
		this.applyForce(calculateResistanceForce(environmentDensity).scale(elapsedTime));
		this.position.add(Vector2D.scale(this.velocity, elapsedTime));
	}
	public void updateLights(PlaySceneController gsc) {

	}

	public void render(Renderer gameRenderer) {

	}
	public void renderHitbox(Renderer gameRenderer) {
		if (isDrawn && this.hitbox != null) {
			GameHitbox.renderHitbox(gameRenderer, this.position, this.rotation, this.hitbox);
		}
	}
	public void renderHitbox(Renderer gameRenderer, Color color) {
		if (isDrawn && this.hitbox != null) {
			GameHitbox.renderHitbox(gameRenderer, this.position, this.rotation, this.hitbox, color);
		}
	}

	public GameObject applyForce(Vector2D force) {
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

		float crossSection = this.hitbox.calculateCrossSection(this.velocity);
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

	public void collideWith(GameObject other, Vector2D mySideNormal, Vector2D otherSideNormal) {

	}

	public Vector2D getFacingDirection() {
		return new Vector2D(1, 0).setAngle(this.rotation);
	}

	/**
	 * @return Rotation angle. In radians.
	 */
	public float getRotation() {
		return this.rotation;
	}

	/**
	 * @param angle Rotation angle. In radians.
	 * @param force Whether to force the full rotation or adhere to {@code maxRotationSpeed}.
	 * @return This reference.
	 */
	public GameObject rotateBy(float angle, boolean force) {
		// constrain angle
		if (!force && maxRotationSpeed >= 0) {
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

	/**
	 * Equivalent to calling {@code rotateBy(angle, false)}
	 * @see GameObject#rotateBy(float, boolean)
	 *
	 * @param angle Rotation angle. In radians.
	 * @return This reference.
	 */
	public GameObject rotateBy(float angle) {
		return rotateBy(angle, false);
	}

	/**
	 * This function rotates this object until it's rotation angle is equal to angle.
	 *
	 * @param angle Angle to rotate towards. In radians.
	 * @param force Whether to force the full rotation or adhere to maxRotationSpeed.
	 * @return This reference.
	 */
	public GameObject rotateTo(float angle, boolean force) {
		Vector2D angledVector = Vector2D.fromAM(angle, 1);
		float rotationDelta = angledVector.angleBetween(Vector2D.fromAM(this.rotation, 1));
		return rotateBy(rotationDelta, force);
	}

	/**
	 * Equivalent to calling {@code rotateTo(angle, false)}
	 * @see GameObject#rotateTo(float, boolean)
	 *
	 * @param angle Angle to rotate towards. In radians.
	 * @return This reference.
	 */
	public GameObject rotateTo(float angle) {
		return rotateTo(angle, false);
	}

	/**
	 * This function rotates this object either to the left or to the right by {@code maxRotationSpeed} (or 1 if {@code maxRotationSpeed < 0}).
	 *
	 * @param left Whether to rotate left or right.
	 * @return This reference.
	 */
	public GameObject rotateToward(boolean left) {
		float rotationDelta = new Vector2D(1, 0).setAngle(this.rotation + (left ? 1 : -1)).angleBetween(new Vector2D(1, 0).setAngle(this.rotation));
		if (maxRotationSpeed >= 0) {
			rotationDelta = maxRotationSpeed * Math.signum(rotationDelta);
		}

		return rotateBy(rotationDelta);
	}

	/**
	 * @param target Target to face.
	 * @param force Whether to force full rotation or adhere to {@code maxRotationSpeed}.
	 * @param threshold Return value threshold.
	 * @return Whether this object is facing the target point within threshold.
	 */
	public boolean rotateToPoint(Vector2D target, boolean force, float threshold) {
		float rotationDelta = Vector2D.subtract(target, this.position).angleBetween(Vector2D.fromAM(this.rotation, 1));
		rotateBy(rotationDelta, force);

		rotationDelta = Vector2D.subtract(target, this.position).angleBetween(Vector2D.fromAM(this.rotation, 1));
		return rotationDelta <= threshold;
	}

	/**
	 * Equivalent to calling {@code rotateToPoint(target, force, 0.5 / 180 * Math.PI)}
	 * @see GameObject#rotateToPoint(Vector2D, boolean, float)
	 *
	 * @param target Target to face.
	 * @param force Whether to force full rotation or adhere to {@code maxRotationSpeed}.
	 * @return Whether this object is facing the target point within {@code 0.5 / 180 * Math.PI} threshold.
	 */
	public boolean rotateToPoint(Vector2D target, boolean force) {
		return rotateToPoint(target, force, (float)(0.5 / 180 * Math.PI));
	}

	/**
	 * Equivalent to calling {@code rotateToPoint(target, false)}
	 *
	 * @param target Target to face.
	 * @return Whether this object is facing the target point within {@code 0.5 / 180 * Math.PI} threshold.
	 * @see GameObject#rotateToPoint(Vector2D, boolean)
	 */
	public boolean rotateToPoint(Vector2D target) {
		return rotateToPoint(target, false);
	}

	protected GameObject membersFromJson(JsonObject sourceObj) {
		this.position = Vector2D.fromJSON(sourceObj.getJsonObject("position"), this.position);

		try {
			this.name = sourceObj.getString("name");
		} catch (NullPointerException | ClassCastException ignored) { }

		try {
			this.maxRotationSpeed = (float)sourceObj.getJsonNumber("maxRotationSpeed").doubleValue();
		} catch (NullPointerException | ClassCastException ignored) { }

		try {
			this.rotation = (float)sourceObj.getJsonNumber("rotation").doubleValue();
		} catch (NullPointerException | ClassCastException ignored) { }

		try {
			this.mass = (float)sourceObj.getJsonNumber("mass").doubleValue();
		} catch (NullPointerException | ClassCastException ignored) { }

		this.velocity = Vector2D.fromJSON(sourceObj.getJsonObject("velocity"), this.velocity);

		try {
			this.doesCollide = sourceObj.getBoolean("doesCollide");
		} catch (NullPointerException | ClassCastException ignored) { }

		try {
			JsonObject hitboxObject = sourceObj.getJsonObject("hitbox");
			if (hitboxObject != null) {
				this.hitbox = GameHitbox.fromJSON(hitboxObject);
			}
		} catch (NullPointerException | ClassCastException ignored) { }

		try {
			this.dragCoefficient = (float)sourceObj.getJsonNumber("dragCoefficient").doubleValue();
		} catch (NullPointerException | ClassCastException ignored) { }

		try {
			this.isDrawn = sourceObj.getBoolean("isDrawn");
		} catch (NullPointerException | ClassCastException ignored) { }

		try {
			this.toDelete = sourceObj.getBoolean("toDelete");
		} catch (NullPointerException | ClassCastException ignored) { }

		return this;
	}
	protected JsonBuilder toJSONBuilder() {
		JsonBuilder builder = new JsonBuilder().add("cname", getClass().getCanonicalName());

		if (this.position.getMagnitude() != 0)
			builder.add("position", position);

		if (this.velocity.getMagnitude() != 0)
			builder.add("velocity", velocity);

		builder.add_optional("rotation", rotation, 0)
				.add_optional("doesCollide", doesCollide, true)
				.add_optional("dragCoefficient", dragCoefficient, 0.1f)
				.add_optional("isDrawn", isDrawn, true)
				.add_optional("toDelete", toDelete, false);

		return builder;
	}

	public static GameObject fromJSON(JsonObject sourceObj) {
		String className = null;
		try {
			className = sourceObj.getString("cname");
		} catch (NullPointerException | ClassCastException ignored) {
			return null;
		}

		try {
			Class<?> clazz = Class.forName(className);
			Constructor<?> ctor = clazz.getConstructor(JsonObject.class);
			return (GameObject) ctor.newInstance(sourceObj);
		} catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			return null;
		}
	}
}
