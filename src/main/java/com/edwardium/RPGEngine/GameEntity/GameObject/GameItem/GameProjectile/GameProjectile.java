package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameProjectile;

import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItem;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameWall;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Vector2D;

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

		if (	(maximumDistance > 0 && distanceTravelled >= maximumDistance)
				|| (maximumTime > 0 && timeTravelled >= maximumTime)
				|| (minimumSpeed > 0 && velocity.getMagnitude() <= minimumSpeed))
		{
			this.toDelete = true;
		}

		super.update(elapsedTime, environmentDensity);
	}
}
