package com.edwardium.RPGEngine.GameEntity.GameObject;

import com.edwardium.RPGEngine.Control.SceneController.GameSceneController;
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

	public void updatePhysics(float elapsedTime, float environmentDensity) {
		this.applyForce(calculateResistanceForce(environmentDensity).scale(elapsedTime));
		this.position.add(Vector2D.scale(this.velocity, elapsedTime));
	}
	public void updateLights(GameSceneController gsc) {

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
