package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameProjectile;

import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItem;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.IO.JsonBuilder;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Utility.Vector2D;

import javax.json.JsonObject;

public abstract class GameProjectile extends GameItem {

	protected float distanceTravelled = 0f;
	protected float timeTravelled = 0f;

	protected float maximumDistance = -1f;
	protected float maximumTime = -1f;
	protected float minimumSpeed = -1f;

	public float damage = 0f;

	protected GameProjectile(Vector2D position, String name, Vector2D velocity) {
		super(position, name);

		this.velocity = velocity;
		this.mass = 0.008f;
	}

	@Override
	public TextureInfo getInventoryTexture() {
		return null;
	}

	@Override
	public TextureInfo getHeldTexture() {
		return null;
	}

	@Override
	public Vector2D getHeldSize() {
		return null;
	}

	@Override
	public void collideWith(GameObject other, Vector2D otherSideNormal) {
		if (other instanceof GameCharacter) {
			((GameCharacter) other).damage(this.damage);
			this.toDelete = true;
		}
	}

	@Override
	public void update(float elapsedTime, float environmentDensity) {
		this.distanceTravelled += this.velocity.getMagnitude() * elapsedTime;
		this.timeTravelled += elapsedTime;

		if ((maximumDistance > 0 && distanceTravelled >= maximumDistance)
				|| (maximumTime > 0 && timeTravelled >= maximumTime)
				|| (minimumSpeed > 0 && velocity.getMagnitude() <= minimumSpeed))
		{
			this.toDelete = true;
		}

		super.update(elapsedTime, environmentDensity);
	}

	@Override
	protected GameObject membersFromJson(JsonObject sourceObj) {
		super.membersFromJson(sourceObj);

		try {
			this.distanceTravelled = (float)sourceObj.getJsonNumber("distanceTravelled").doubleValue();
		} catch (NullPointerException | ClassCastException ignored) { }

		try {
			this.timeTravelled = (float)sourceObj.getJsonNumber("timeTravelled").doubleValue();
		} catch (NullPointerException | ClassCastException ignored) { }

		try {
			this.maximumDistance = (float)sourceObj.getJsonNumber("maximumDistance").doubleValue();
		} catch (NullPointerException | ClassCastException ignored) { }

		try {
			this.maximumTime = (float)sourceObj.getJsonNumber("maximumTime").doubleValue();
		} catch (NullPointerException | ClassCastException ignored) { }

		try {
			this.minimumSpeed = (float)sourceObj.getJsonNumber("minimumSpeed").doubleValue();
		} catch (NullPointerException | ClassCastException ignored) { }

		try {
			this.damage = (float)sourceObj.getJsonNumber("damage").doubleValue();
		} catch (NullPointerException | ClassCastException ignored) { }

		return this;
	}

	@Override
	protected JsonBuilder toJSONBuilder() {
		JsonBuilder builder = super.toJSONBuilder();

		if (maximumDistance >= 0)
			builder.add("maximumDistance", maximumDistance).add_optional("distanceTravelled", distanceTravelled, 0f);


		if (maximumTime >= 0)
			builder.add("maximumTime", maximumTime).add_optional("timeTravelled", timeTravelled, 0f);

		if (minimumSpeed >= 0)
			builder.add("minimumSpeed", minimumSpeed);

		return builder;
	}
}
